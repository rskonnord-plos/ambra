/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-$today.year by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ambraproject.service.article;

import org.ambraproject.ApplicationException;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreException;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.Journal;
import org.ambraproject.service.permission.PermissionsService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.xml.XMLService;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
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

  /**
   * Get the Article Asset by URI.
   *
   * @param assetUri uri
   * @param authId   the authorization ID of the current user
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
  @Override
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
  @Override
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
      List<ArticleAsset> asset = hibernateTemplate.findByCriteria(
              DetachedCriteria.forClass(ArticleAsset.class)
                      .add(Restrictions.eq("doi", assetUri))
                      .add(Restrictions.eq("extension", representation)), 0, 1);
      if (asset != null && asset.size() > 0) {
        return asset.get(0);
      }

    } catch (DataAccessException e) {
      throw new NoSuchObjectIdException(assetUri);
    }
    return null;
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
   * @param authId the authorization ID of the current user
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  @Transactional(readOnly = true)
  @Override
  public ArticleAssetWrapper[] listFiguresTables(final String articleDoi, final String authId) throws NoSuchArticleIdException {
    //TODO:
    // Can we not do a select distinct instead of getting back a large set of assets
    // and then filtering the list via java code below?

    articleService.checkArticleState(articleDoi, authId);

    // if we get there, we are good
    // get assets
    List<ArticleAsset> assets = hibernateTemplate.find("select assets from Article article where article.doi = ?", articleDoi);

    //keep track of dois we've added to the list so we don't duplicate assets for the same image
    Map<String, ArticleAssetWrapper> dois = new HashMap<String, ArticleAssetWrapper>(assets.size());
    List<ArticleAssetWrapper> results = new ArrayList<ArticleAssetWrapper>(assets.size());
    for (ArticleAsset asset : assets) {
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
        if("PNG_L".equals(extension)){
          articleAssetWrapper.setSizeLarge(asset.getSize());
        } else if("TIF".equals(extension)) {
          articleAssetWrapper.setSizeTiff(asset.getSize());
        }
      }
    }
    return results.toArray(new ArticleAssetWrapper[results.size()]);
  }

  @Override
  @Transactional(readOnly = true)
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
   * @param assetDoi
   * @param authId
   * @return
   * @throws NoSuchArticleIdException
   */
  @Override
  @Transactional(readOnly = true)
  public InputStream getPowerPointSlide(String assetDoi, String authId) throws NoSuchArticleIdException, NoSuchObjectIdException, ApplicationException, IOException {
    
    long startTime = Calendar.getInstance().getTimeInMillis();
    String title = "";

    //get the article
    Article article = articleService.getArticle(assetDoi.substring(0, assetDoi.lastIndexOf('.')), authId);

    //get the article asset for "PNG_M"
    ArticleAsset articleAsset = getArticleAsset(assetDoi, "PNG_M", authId);

    //get the article description
    String desc = getArticleDescription(articleAsset);

    //construct the title for ppt.
    if(articleAsset.getTitle()!= null) {
      title = articleAsset.getTitle() + ". " + desc;
    } else {
      title = desc;
    }

    //get the citation info
    StringBuilder citation = getCitationInfo(article);

    ByteArrayOutputStream tempOutputStream = null;

    try {

      byte[] image = fileStoreService.getFileByteArray(FSIDMapper.doiTofsid(assetDoi, "PNG_M"));
      tempOutputStream = new ByteArrayOutputStream(image.length);

      //make the new slide
      SlideShow slideShow = new SlideShow();
      slideShow.setPageSize(new Dimension(792, 612)); // letter size: 11"x8.5", 1"=72px

      //set the picture box on particular location
      Picture picture = setPictureBox(image, slideShow);

      //create the slide
      Slide slide = slideShow.createSlide();

      //add the picture to slide
      slide.addShape(picture);

      //add the title to slide
      if(!title.isEmpty()){
        TextBox pptTitle = slide.addTitle();
        pptTitle.setText(title);
        pptTitle.setAnchor(new Rectangle(28, 22, 737, 36));
        RichTextRun rt = pptTitle.getTextRun().getRichTextRuns()[0];
        rt.setFontSize(16);
        rt.setBold(true);
        rt.setAlignment(TextBox.AlignCenter);
      }

      //add the citation text to slide
      TextBox pptCitationText = new TextBox();

      /**
       * show the journal name and logo of article in which it published
       * an article can be cross published but we always want to show logo and url
       * of source journal
       */

      int index = assetDoi.lastIndexOf(".");
      String articleDoi = assetDoi.substring(0, index);
      String eIssn;

      try {
        eIssn = (String) hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(Article.class)
                .add(Restrictions.eq("doi", articleDoi))
                .setProjection(Projections.property("eIssn")),0, 1).get(0);
      } catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Doi " + articleDoi + " didn't correspond to an article");
      }

      String journalName = (String) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Journal.class)
              .add(Restrictions.eq("eIssn", eIssn))
              .setProjection(Projections.property("journalKey")),0, 1).get(0);

      String pptUrl = "http://www." + journalName.toLowerCase() + ".org/article/" + articleDoi;

      pptCitationText.setText(citation.toString() + "\n" + pptUrl);
      pptCitationText.setAnchor(new Rectangle(35, 513, 723, 26));

      RichTextRun richTextRun = pptCitationText.getTextRun().getRichTextRuns()[0];
      richTextRun.setFontSize(12);

      String text = pptCitationText.getText();
      Hyperlink link = new Hyperlink();
      link.setAddress(pptUrl);
      link.setTitle("click to visit the article page");
      int linkId = slideShow.addHyperlink(link);
      int startIndex = text.indexOf(pptUrl);
      pptCitationText.setHyperlink(linkId, startIndex, startIndex + pptUrl.length());

      slide.addShape(pptCitationText);

      //add the logo to slide
      String str = templatesDirectory + "/journals/" + journalName + "/webapp/images/logo.png";
      File file =  new File(str);
      if(file.exists()) {
        InputStream input = new FileInputStream(file);
        Dimension dimension = getImageDimension(input);
        input.close();

        int logoIdx = slideShow.addPicture(file, Picture.PNG);
        Picture logo = new Picture(logoIdx);
        logo.setAnchor(new Rectangle(792 - 5 - dimension.width, 612 - 5 - dimension.height, dimension.width, dimension.height));
        slide.addShape(logo); 
      } else {
        log.warn("Logo for journal " + journalName + " not found at " + str);
      }

      slideShow.write(tempOutputStream);

      return new ByteArrayInputStream(tempOutputStream.toByteArray());

    } catch (FileStoreException e) {
      log.error("Error fetching image from file store for doi: " + assetDoi, e);
      return null;
    } catch (IOException e) {
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
    }
  }

  /**
   * get the image dimension
   * @param input
   * @return
   */
  private Dimension getImageDimension(InputStream input) {
    try {
      ImageInputStream in = ImageIO.createImageInputStream(input) ;
      try {
        Iterator readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          ImageReader reader = (ImageReader) readers.next();
          try {
            reader.setInput(in);
            return new Dimension(reader.getWidth(0), reader.getHeight(0));
          } finally {
          reader.dispose();
          }
        }
      } finally {
        if (in != null)
        in.close();
      }
    }
    catch (Exception ex) {
      log.error("cannot get image dimension", ex);
    }
    return new Dimension(0, 0);
  }

  /**
   * set the dimension of picture box
   * @param image
   * @param slideShow
   * @return
   * @throws IOException
   */
  private Picture setPictureBox(byte[] image, SlideShow slideShow) throws IOException {

    int index = slideShow.addPicture(image, Picture.PNG);

    InputStream input = new ByteArrayInputStream(image);
    Dimension dimension = getImageDimension(input);
    input.close();

    //get the image size
    int imW = dimension.width;
    int imH = dimension.height;

    //add the image to picture and add picture to shape
    Picture picture = new Picture(index);

    // Image box size 750x432 at xy=21,68

    if (imW > 0 && imH > 0) {
      double pgRatio = 750.0/432.0;
      double imRatio = (double) imW / (double) imH;
      if (pgRatio >= imRatio) {
        // horizontal center
        int mw = (int)((double) imW * 432.0 / (double) imH);
        int mx = 21 + (750 - mw) / 2;

        picture.setAnchor(new Rectangle(mx, 68, mw, 432));
      }
      else {
        // vertical center
        int mh = (int)((double) imH * 750.0 / (double) imW);
        int my = 68 + (432 - mh) / 2;

        picture.setAnchor(new Rectangle(21, my, 750, mh));
      }
    }

    return picture;
  }

  /**
   * get the article description
   *
   * @param articleAsset
   * @return
   * @throws ApplicationException
   */
  private String getArticleDescription(ArticleAsset articleAsset) throws ApplicationException {
    Pattern p = Pattern.compile("<title>(.*?)</title>");
    String description = "";
    if(articleAsset.getDescription() != null) {
      Matcher m = p.matcher(articleAsset.getDescription());
      if (m.find()) {
        description = m.group(1);
        description = description.replaceAll("<.*?>", "");
      }
    }
    return description;
  }

  /**
   * get the citation information for powerpoint
   * @param article
   * @return
   */
  private StringBuilder getCitationInfo(Article article) {

    List<ArticleAuthor> articleAuthors = article.getAuthors();
    List<String> collabAuthors = article.getCollaborativeAuthors();
    List<String> authors = new ArrayList<String>(articleAuthors.size() + collabAuthors.size());
    for (Iterator<ArticleAuthor> it = articleAuthors.iterator(); it.hasNext();) {
      ArticleAuthor author = it.next();
      authors.add(author.getSurnames() + " " + toShortFormat(author.getGivenNames()));
    }
    authors.addAll(collabAuthors);

    StringBuilder citation = new StringBuilder();

    int maxIndex = Math.min(authors.size(), 4);
    for (int i = 0; i < maxIndex - 1; i++) {
      String author = authors.get(i);
      citation.append(author).append(", ");
    }

    if(maxIndex > 0 ) {
      String lastAuthor = authors.get(maxIndex - 1);
      citation.append(lastAuthor);
      if (authors.size() > 4) {
        citation.append(", et al.");
      }
    }

    //append the year, title, journal, volume, issue and eLocationId information
    citation.append(" (").append(new SimpleDateFormat("yyyy").format(article.getDate())).append(") ")
      .append(article.getTitle().replaceAll("<.*?>", "")).append(". ")
      .append(article.getJournal()).append(" ")
      .append(article.getVolume())
      .append("(").append(article.getIssue()).append("): ")
      .append(article.geteLocationId()).append(". ")
      .append("doi:").append(article.getDoi().replaceFirst("info:doi/", ""));

    return citation;
  }

  /**
   * Function to format the author names
   * @param name
   * @return
   */
  private String toShortFormat(String name) {
    if (name == null)
      return null;

    String[] givenNames = name.split(" ");
    StringBuilder sb = new StringBuilder();
    for(String givenName :givenNames) {
      if (givenName.length() > 0) {
        if(givenName.matches(".*\\p{Pd}\\p{L}.*")) {
          // Handle names with dash
          String[] sarr = givenName.split("\\p{Pd}");
          for (int i = 0; i < sarr.length; i++) {
            if (i > 0) {
              sb.append('-');
            }

            if(sarr[i].length() > 0) {
              sb.append(sarr[i].charAt(0));
            }
          }
        }
        else {
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