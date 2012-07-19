/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.article.service;

import org.ambraproject.ApplicationException;
import org.ambraproject.article.FigureSlideShow;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreException;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.Journal;
import org.ambraproject.permission.service.PermissionsService;
import org.ambraproject.service.HibernateServiceImpl;
import org.ambraproject.service.XMLService;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.im4java.core.IM4JavaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Scott Sterling
 * @author Joe Osowski
 */
public class ArticleAssetServiceImpl extends HibernateServiceImpl implements ArticleAssetService {

  private static final Logger log = LoggerFactory.getLogger(ArticleAssetServiceImpl.class);
  private PermissionsService permissionsService;
  private ArticleService articleService;
  private FileStoreService fileStoreService;
  private XMLService secondaryObjectService;
  private String templatesDirectory;
  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;
  private static final List<String> FIGURE_AND_TABLE_CONTEXT_ELEMENTS = new ArrayList<String>(2);

  static {
    FIGURE_AND_TABLE_CONTEXT_ELEMENTS.add("fig");
    FIGURE_AND_TABLE_CONTEXT_ELEMENTS.add("table-wrap");
    FIGURE_AND_TABLE_CONTEXT_ELEMENTS.add("alternatives");
  }

  private static final int MAX_AUTHORS_IN_CITATION = 4; // any more are "et al."

  /**
   * Get the Article Asset by URI.
   *
   * @param assetUri uri
   * @param authId   the authorization ID of the current user
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
  public ArticleAsset getSuppInfoAsset(final String assetUri, final String authId) throws NoSuchObjectIdException {
    // sanity check parms
    if (assetUri == null)
      throw new IllegalArgumentException("URI == null");
    checkPermissions(assetUri, authId);

    try {
      return (ArticleAsset) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(ArticleAsset.class)
              .add(Restrictions.eq("doi", assetUri)), 0, 1)
          .get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new NoSuchObjectIdException(assetUri);
    }
  }

  /**
   * Get the Article Representation Assets by URI
   * <p/>
   * This probably returns XML and PDF all the time
   *
   * @param articleDoi uri
   * @param authId     the authorization ID of the current user
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ArticleAsset> getArticleXmlAndPdf(final String articleDoi, final String authId)
      throws NoSuchObjectIdException {
    checkPermissions(articleDoi, authId);
    return hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(ArticleAsset.class)
            .add(Restrictions.eq("doi", articleDoi)));
  }

  /**
   * Get the Article Asset by URI and type.
   *
   * @param assetUri       uri
   * @param representation the representation value (XML/PDF)
   * @param authId         the authorization ID of the current user
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
  public ArticleAsset getArticleAsset(final String assetUri, final String representation, final String authId)
      throws NoSuchObjectIdException {

    // sanity check parms
    if (assetUri == null)
      throw new IllegalArgumentException("URI == null");

    if (representation == null) {
      throw new IllegalArgumentException("representation == null");
    }
    checkPermissions(assetUri, authId);
    try {
      return (ArticleAsset) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(ArticleAsset.class)
              .add(Restrictions.eq("doi", assetUri))
              .add(Restrictions.eq("extension", representation)), 0, 1).get(0);
    } catch (DataAccessException e) {
      throw new NoSuchObjectIdException(assetUri);
    }
  }

  @SuppressWarnings("unchecked")
  private void checkPermissions(String assetDoi, String authId) throws NoSuchObjectIdException {
    int state;
    try {
      state = (Integer) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .setProjection(Projections.property("state"))
              .createCriteria("assets")
              .add(Restrictions.eq("doi", assetDoi)), 0, 1).get(0);
    } catch (IndexOutOfBoundsException e) {
      //article didn't exist
      throw new NoSuchObjectIdException(assetDoi);
    }

    //If the article is in an unpublished state, none of the related objects should be returned
    if (Article.STATE_UNPUBLISHED == state) {
      try {
        permissionsService.checkPermission(Permission.VIEW_UNPUBBED_ARTICLES, authId);
      } catch (SecurityException se) {
        throw new NoSuchObjectIdException(assetDoi);
      }
    }

    //If the article is disabled don't return the object ever
    if (Article.STATE_DISABLED == state) {
      throw new NoSuchObjectIdException(assetDoi);
    }
  }

  /**
   * Return a list of Figures and Tables of a DOI.
   *
   * @param articleDoi DOI.
   * @param authId     the authorization ID of the current user
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  @Transactional(readOnly = true)
  public ArticleAssetWrapper[] listFiguresTables(final String articleDoi, final String authId) throws NoSuchArticleIdException {
    //TODO:
    // Do we have to get the article here to get the assets?  We can't we just get a list of assets
    // after we check to see if the article is published?  Also, can we not do a select distinct instead of
    // getting back a large set of assets and then filtering the list via java code below?

    Article article = articleService.getArticle(articleDoi, authId);
    //keep track of dois we've added to the list so we don't duplicate assets for the same image
    Map<String, ArticleAssetWrapper> dois = new HashMap<String, ArticleAssetWrapper>(article.getAssets().size());
    List<ArticleAssetWrapper> results = new ArrayList<ArticleAssetWrapper>(article.getAssets().size());
    for (ArticleAsset asset : article.getAssets()) {
      if (FIGURE_AND_TABLE_CONTEXT_ELEMENTS.contains(asset.getContextElement())) {
        ArticleAssetWrapper articleAssetWrapper;
        if (!dois.containsKey(asset.getDoi())) {
          articleAssetWrapper = new ArticleAssetWrapper(asset, smallImageRep, mediumImageRep, largeImageRep);
          results.add(articleAssetWrapper);
          dois.put(asset.getDoi(), articleAssetWrapper);
        } else {
          articleAssetWrapper = dois.get(asset.getDoi());
        }
        // set the size of different representation.
        String extension = asset.getExtension();
        if ("PNG_L".equals(extension)) {
          articleAssetWrapper.setSizeLarge(asset.getSize());
        } else if ("TIF".equals(extension)) {
          articleAssetWrapper.setSizeTiff(asset.getSize());
        }
      }
    }
    return results.toArray(new ArticleAssetWrapper[results.size()]);
  }

  @Override
  public Long getArticleID(ArticleAsset articleAsset) {
    final Long assetID = articleAsset.getID();
    Object result = hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        return session.createSQLQuery("select articleID from articleAsset where articleAssetID = ?")
            .setParameter(0, assetID).list().get(0);
      }
    });
    if (result instanceof BigInteger) {
      return ((BigInteger) result).longValue();
    } else {
      return (Long) result;
    }
  }

  /**
   * Get the data for powerpoint
   *
   * @param assetDoi
   * @param authId
   * @return
   * @throws NoSuchArticleIdException
   */
  @Override
  public InputStream getPowerPointSlide(String assetDoi, String authId) throws NoSuchArticleIdException, NoSuchObjectIdException, ApplicationException {

    long startTime = Calendar.getInstance().getTimeInMillis();

    //get the article
    String articleDoi = assetDoi.substring(0, assetDoi.lastIndexOf('.'));
    Article article = articleService.getArticle(articleDoi, authId);

    //construct the title for ppt.
    ArticleAsset articleAsset = getArticleAsset(assetDoi, "PNG_M", authId);
    String desc = getArticleDescription(articleAsset);
    String title = articleAsset.getTitle();
    title = (title == null) ? desc : title + ". " + desc;

    /*
    * Show the journal name and logo of article in which it published.
    * An article can be cross-published but we always want to show the logo and URL of the source journal.
    */
    String journalName = (String) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Journal.class)
            .add(Restrictions.eq("eIssn", article.geteIssn()))
            .setProjection(Projections.property("journalKey")), 0, 1).get(0);
    String logoPath = templatesDirectory + "/journals/" + journalName + "/webapp/images/logo.png";
    String pptUrl = "http://www." + journalName.toLowerCase() + ".org/article/" + articleDoi;

    String citation = getCitationInfo(article);

    ByteArrayOutputStream tempOutputStream = null;
    File tempFile = null;

    try {
      URL citationLink = new URL(pptUrl);
      byte[] image = fileStoreService.getFileByteArray(FSIDMapper.doiTofsid(assetDoi, "PNG_M"));
      tempFile = File.createTempFile("tmp_image", "." + "png_m");
      String imgAbsolutePath = fileStoreService.copyFileFromStore(FSIDMapper.doiTofsid(assetDoi, "PNG_M"), tempFile).getAbsolutePath();

      tempOutputStream = new ByteArrayOutputStream(image.length);
      FigureSlideShow slideShow = new FigureSlideShow(title, citation, journalName, citationLink, image, logoPath, imgAbsolutePath);
      slideShow.write(tempOutputStream);
      return new ByteArrayInputStream(tempOutputStream.toByteArray());
    } catch (FileStoreException e) {
      log.error("Error fetching image from file store for doi: " + assetDoi, e);
      return null;
    } catch (IOException e) {
      log.error("Error creating powerpoint slide for doi: " + assetDoi, e);
      return null;
    } catch (IM4JavaException e) {
      log.error("Error creating powerpoint slide for doi: " + assetDoi, e);
      return null;
    } catch (InterruptedException e) {
      log.error("Error creating powerpoint slide for doi: " + assetDoi, e);
      return null;
    } finally {
      long totalTime = Calendar.getInstance().getTimeInMillis() - startTime;
      log.info("processing power point slide for " + assetDoi + " took " + totalTime + " milliseconds");
      if (tempOutputStream != null) {
        try {
          tempOutputStream.close();
        } catch (IOException e) {
          log.warn("Error closing temporary output stream when creating power point slide for " + assetDoi, e);
        }
      }
      if(tempFile != null){
        try {
          tempFile.delete();
        } catch (Exception e) {
          log.warn("Error deleting the temp file when creating power point slide for " + assetDoi, e);
        }

      }
    }
  }


  private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>");

  /**
   * get the article description
   *
   * @param articleAsset
   * @return
   * @throws ApplicationException
   */
  private static String getArticleDescription(ArticleAsset articleAsset) throws ApplicationException {
    String description = "";
    if (articleAsset.getDescription() != null) {
      Matcher m = TITLE_PATTERN.matcher(articleAsset.getDescription());
      if (m.find()) {
        description = m.group(1);
      }
    }
    return description;
  }

  /**
   * get the citation information for powerpoint
   *
   * @param article
   * @return
   */
  private static String getCitationInfo(Article article) {

    List<ArticleAuthor> articleAuthors = article.getAuthors();
    List<String> collabAuthors = article.getCollaborativeAuthors();
    List<String> authors = new ArrayList<String>(articleAuthors.size() + collabAuthors.size());
    for (Iterator<ArticleAuthor> it = articleAuthors.iterator(); it.hasNext(); ) {
      ArticleAuthor author = it.next();
      authors.add(author.getSurnames() + " " + toShortFormat(author.getGivenNames()));
    }
    authors.addAll(collabAuthors);

    StringBuilder citation = new StringBuilder();

    int maxIndex = Math.min(authors.size(), MAX_AUTHORS_IN_CITATION);
    for (int i = 0; i < maxIndex - 1; i++) {
      String author = authors.get(i);
      citation.append(author).append(", ");
    }

    if (maxIndex > 0) {
      String lastAuthor = authors.get(maxIndex - 1);
      citation.append(lastAuthor);
      if (authors.size() > MAX_AUTHORS_IN_CITATION) {
        citation.append(", et al.");
      }
    }

    //append the year, title, journal, volume, issue and eLocationId information
    citation.append(" (").append(new SimpleDateFormat("yyyy").format(article.getDate())).append(") ")
        .append(article.getTitle()).append(". ")
        .append(article.getJournal()).append(" ")
        .append(article.getVolume())
        .append("(").append(article.getIssue()).append("): ")
        .append(article.geteLocationId()).append(". ")
        .append("doi:").append(article.getDoi().replaceFirst("info:doi/", ""));

    return citation.toString();
  }

  /**
   * Function to format the author names
   *
   * @param name
   * @return
   */
  private static String toShortFormat(String name) {
    if (name == null)
      return null;

    String[] givenNames = name.split(" ");
    StringBuilder sb = new StringBuilder();
    for (String givenName : givenNames) {
      if (givenName.length() > 0) {
        if (givenName.matches(".*\\p{Pd}\\p{L}.*")) {
          // Handle names with dash
          String[] sarr = givenName.split("\\p{Pd}");
          for (int i = 0; i < sarr.length; i++) {
            if (i > 0) {
              sb.append('-');
            }

            if (sarr[i].length() > 0) {
              sb.append(sarr[i].charAt(0));
            }
          }
        } else {
          sb.append(givenName.charAt(0));
        }
      }
    }

    return sb.toString();

  }

  @Required
  public void setTemplatesDirectory(String templatesDirectory) {
    this.templatesDirectory = templatesDirectory;
  }

  /**
   * @param permissionsService the permissions service to use
   */
  @Required
  public void setPermissionsService(PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * @param articleService the article service to use
   */
  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  /**
   * Set the small image representation
   *
   * @param smallImageRep smallImageRep
   */
  public void setSmallImageRep(final String smallImageRep) {
    this.smallImageRep = smallImageRep;
  }

  /**
   * Set the medium image representation
   *
   * @param mediumImageRep mediumImageRep
   */
  public void setMediumImageRep(final String mediumImageRep) {
    this.mediumImageRep = mediumImageRep;
  }

  /**
   * Set the large image representation
   *
   * @param largeImageRep largeImageRep
   */
  public void setLargeImageRep(final String largeImageRep) {
    this.largeImageRep = largeImageRep;
  }

  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  public void setSecondaryObjectService(XMLService secondaryObjectService) {
    this.secondaryObjectService = secondaryObjectService;
  }
}