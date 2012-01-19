/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.topazproject.ambra.article.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.article.ArchiveProcessException;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.util.XPathUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Implementaion of {@link IngestArchiveProcessor} that uses xpath to parse the article xml
 *
 * @deprecated Use {@link XslIngestArchiveProcessor} for now, to ensure that we parse article xml the same as pre-newhope.
 *  If the Article model simplifies, it may be worthwhile to change that though.
 * @author Alex Kudlick Date: 6/7/11
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
@Deprecated
public class IngestArchiveProcessorImpl implements IngestArchiveProcessor {
  private static final Logger log = LoggerFactory.getLogger(IngestArchiveProcessorImpl.class);


  private XPathUtil xpathUtil;

  private DocumentBuilder documentBuilder;

  private static Map<String, String> mimeTypes = new HashMap<String, String>();

  static {
    mimeTypes.put("aif", "audio/x-aiff");
    mimeTypes.put("aiff", "audio/x-aiff");
    mimeTypes.put("asf", "video/x-ms-asf");
    mimeTypes.put("asx", "video/x-ms-asf");
    mimeTypes.put("au", "audio/basic");
    mimeTypes.put("snd", "audio/basic");
    mimeTypes.put("avi", "video/x-msvideo");
    mimeTypes.put("bmp", "image/bmp");
    mimeTypes.put("bz2", "application/x-bzip");
    mimeTypes.put("bzip", "application/x-bzip");
    mimeTypes.put("csv", "text/comma-separated-values");
    mimeTypes.put("divx", "video/x-divx");
    mimeTypes.put("doc", "application/msword");
    mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    mimeTypes.put("dvi", "application/x-dvi");
    mimeTypes.put("eps", "application/eps");
    mimeTypes.put("gif", "image/gif");
    mimeTypes.put("gz", "application/x-gzip");
    mimeTypes.put("gzip", "application/x-gzip");
    mimeTypes.put("htm", "text/html");
    mimeTypes.put("html", "text/html");
    mimeTypes.put("icb", "application/x-molsoft-icb");
    mimeTypes.put("ief", "image/ief");
    mimeTypes.put("jpg", "image/jpeg");
    mimeTypes.put("jpeg", "image/jpeg");
    mimeTypes.put("jpe", "image/jpeg");
    mimeTypes.put("latex", "application/x-latex");
    mimeTypes.put("mid", "audio/midi");
    mimeTypes.put("midi", "audio/midi");
    mimeTypes.put("rmi", "audio/midi");
    mimeTypes.put("mov", "video/quicktime");
    mimeTypes.put("qt", "video/quicktime");
    mimeTypes.put("mp2", "audio/mpeg");
    mimeTypes.put("mp3", "audio/x-mpeg3");
    mimeTypes.put("mp4", "video/mp4");
    mimeTypes.put("mpg4", "video/mp4");
    mimeTypes.put("mpg", "video/mpeg");
    mimeTypes.put("mpeg", "video/mpeg");
    mimeTypes.put("m4v", "video/x-m4v");
    mimeTypes.put("pdf", "application/pdf");
    mimeTypes.put("png", "image/png");
    mimeTypes.put("png_s", "image/png");
    mimeTypes.put("png_m", "image/png");
    mimeTypes.put("png_l", "image/png");
    mimeTypes.put("pnm", "image/x-portable-anymap");
    mimeTypes.put("ppt", "application/vnd.ms-powerpoint");
    mimeTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    mimeTypes.put("ps", "application/postscript");
    mimeTypes.put("ra", "audio/x-realaudio");
    mimeTypes.put("ram", "audio/x-pn-realaudio");
    mimeTypes.put("rm", "audio/x-pn-realaudio");
    mimeTypes.put("rar", "application/x-rar-compressed");
    mimeTypes.put("ras", "image/x-cmu-raster");
    mimeTypes.put("rtf", "text/rtf");
    mimeTypes.put("swf", "application/x-shockwave-flash");
    mimeTypes.put("tar", "application/x-tar");
    mimeTypes.put("tif", "image/tiff");
    mimeTypes.put("tiff", "image/tiff");
    mimeTypes.put("txt", "text/plain");
    mimeTypes.put("wav", "audio/x-wav");
    mimeTypes.put("wma", "audio/x-ms-wma");
    mimeTypes.put("wmv", "video/x-ms-wmv");
    mimeTypes.put("xls", "application/vnd.ms-excel");
    mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    mimeTypes.put("xml", "text/xml");
    mimeTypes.put("xpm", "image/x-xpixmap");
    mimeTypes.put("zip", "application/zip");
  }


  public IngestArchiveProcessorImpl() {
    xpathUtil = new XPathUtil();
    xpathUtil.setNamespaceContext(new String[]{"xlink=http://www.w3.org/1999/xlink"});
  }

  /**
   * Set the document builder to use for constructing documents from the zip file entries
   *
   * @param documentBuilder - the document builder to use
   */
  @Required
  public void setDocumentBuilder(DocumentBuilder documentBuilder) {
    this.documentBuilder = documentBuilder;
  }

  @Override
  public Article processArticle(ZipFile archive, Document articleXml) throws ArchiveProcessException {
    log.info("Processing ingest archive " + archive.getName() + " using xpath");
    try {
      Document manifest = extractXml(archive, "MANIFEST.xml");
      String articleId = xpathUtil.evaluate(manifest, "//articleBundle/article/@uri");
      Article article = new Article();
      article.setId(URI.create(articleId));
      article.setState(Article.STATE_UNPUBLISHED);
      String archiveName = archive.getName().contains(File.separator)
          ? archive.getName().substring(archive.getName().lastIndexOf(File.separator) + 1)
          : archive.getName();
      article.setArchiveName(archiveName);
      article.setDublinCore(parseDublinCore(articleId, articleXml));
      article.setCategories(parseCategories(articleXml));
      article.setArticleType(parseArticleTypes(articleXml));
      if (xpathUtil.selectSingleNode(articleXml, "//front/journal-meta/issn[@pub-type = 'epub']") != null) {
        article.seteIssn(xpathUtil.evaluate(articleXml, "//front/journal-meta/issn[@pub-type = 'epub']/text()"));
      }
      article.setRelatedArticles(parseRelatedArticles(articleXml));
      article.setParts(parseSecondaryObjects(manifest, articleXml, article, archive));
      article.setRepresentations(
          formatRepresentations(archive, xpathUtil.selectNodes(manifest,"//articleBundle/article/representation"), article));


      log.info("Finished parsing archive " + archive.getName());
      return article;
    } catch (IOException e) {
      throw new ArchiveProcessException("Error extracting manifest xml from archive: " + archive.getName(), e);
    } catch (SAXException e) {
      throw new ArchiveProcessException("Error building document from manifest xml; archive: " + archive.getName(), e);
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing article xml; archive: " + archive.getName(), e);
    }
  }

  private Set<Representation> formatRepresentations(ZipFile archive, NodeList representationNodes, ObjectInfo article) throws XPathExpressionException {
    Set<Representation> representations = new HashSet<Representation>(representationNodes.getLength());
    for (int i = 0; i < representationNodes.getLength(); i++) {
      Representation representation =  new Representation();
      String fileName = representationNodes.item(i).getAttributes().getNamedItem("entry").getNodeValue();

      final String contentType = getMimeType(fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase());
      representation.setName(
          representationNodes.item(i).getAttributes().getNamedItem("name").getNodeValue());
      representation.setContentType(contentType);
      representation.setObject(article);
      representation.setSize(archive.getEntry(fileName).getSize());
      representations.add(representation);
    }
    return representations;
  }

  @Override
  public Document extractArticleXml(ZipFile archive) throws ArchiveProcessException {
    try {
      Document manifest = extractXml(archive, "MANIFEST.xml");
      return extractXml(archive, xpathUtil.evaluate(manifest, "//articleBundle/article/@main-entry"));
    } catch (Exception e) {
      throw new ArchiveProcessException("Error extracting article xml from archive: " + archive.getName(), e);
    }
  }

  public DublinCore parseDublinCore(String identifier, Document articleXml) throws ArchiveProcessException {
    log.info("Parsing dublin core data for " + identifier);
    try {
      Node articleMeta = xpathUtil.selectSingleNode(articleXml, "//article/front/article-meta");
      String doi = xpathUtil.evaluate(articleXml, "//article/front/article-meta/article-id[@pub-id-type = 'doi']/text()");

      DublinCore dublinCore = new DublinCore();
      dublinCore.setIdentifier(identifier);
      dublinCore.setTitle(xpathUtil.evaluate(articleMeta, "//title-group/article-title/text()"));
      dublinCore.setType(URI.create("http://purl.org/dc/dcmitype/Text"));
      dublinCore.setFormat("text/xml");
      dublinCore.setLanguage("en");
      //set the pub date
      if (xpathUtil.selectNodes(articleMeta, "//pub-date").getLength() > 0) {
        Date pubDate = getPubDate(articleMeta);
        dublinCore.setDate(pubDate);
        dublinCore.setIssued(pubDate);
        dublinCore.setAvailable(pubDate);
      }
      Node receivedDateNode = xpathUtil.selectSingleNode(articleMeta, "//history/date[@date-type = 'received']");
      Node acceptedDateNode = xpathUtil.selectSingleNode(articleMeta, "//history/date[@date-type = 'accepted']");
      if (receivedDateNode != null) {
        dublinCore.setSubmitted(formatDate(receivedDateNode));
      }
      if (acceptedDateNode != null) {
        dublinCore.setAccepted(formatDate(acceptedDateNode));
      }
      //add contributors
      dublinCore.setCreators(getContributorNames(identifier, articleMeta, "author", articleXml));
      dublinCore.setContributors(getContributorNames(identifier, articleMeta, "contributor", articleXml));

      //set subjects
      List<String> subjects = convertToStringList(xpathUtil.selectNodes(articleMeta,
          "//article-categories/subj-group[@subj-group-type = 'Discipline']/subject/text()"));
      dublinCore.setSubjects(new HashSet<String>(subjects));
      if (xpathUtil.selectNodes(articleMeta, "//abstract").getLength() > 0) {
        dublinCore.setDescription(getDescription(articleMeta));
      }
      if (xpathUtil.selectSingleNode(articleXml, "//journal-meta/publisher/publisher-name") != null) {
        dublinCore.setPublisher(xpathUtil.evaluate(articleXml, "//journal-meta/publisher/publisher-name/text()"));
      }
      if (xpathUtil.selectSingleNode(articleMeta, "//copyright-statement") != null) {
        dublinCore.setRights(xpathUtil.evaluate(articleMeta, "//copyright-statement/text()").trim());
      }
      dublinCore.setConformsTo(
          URI.create("http://dtd.nlm.nih.gov/publishing/" +
              xpathUtil.evaluate(articleXml, "//article/@dtd-version") + "/journalpublishing.dtd")
      );
      try {
        dublinCore.setCopyrightYear(Integer.valueOf(xpathUtil.evaluate(articleMeta, "//copyright-year/text()")));
      } catch (NumberFormatException e) {
        //Ignore
      }

      setBibCitation(articleXml, articleMeta, dublinCore, doi);

      setReferences(articleXml, dublinCore, doi);

      return dublinCore;
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing article xml for " + identifier, e);
    } catch (UnsupportedEncodingException e) {
      throw new ArchiveProcessException("Couldn't encode URL for Bibliographic Citation; " + identifier, e);
    } catch (Exception e) {
      throw new ArchiveProcessException("Error processing article xml", e);
    }
  }

  public Set<Category> parseCategories(Document articleXml) throws ArchiveProcessException {
    try {
      NodeList categoryNodes = xpathUtil.selectNodes(articleXml,
          "//front/article-meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject");
      Set<Category> categories = new HashSet<Category>(categoryNodes.getLength());
      Set<String> duplicateStrings = new HashSet<String>(categoryNodes.getLength());

      for (int i = 0; i < categoryNodes.getLength(); i++) {
        String text = categoryNodes.item(i).getChildNodes().item(0).getNodeValue().trim();

        if (!duplicateStrings.contains(text)) {
          Category category = new Category();
          if (text.contains("/")) {
            category.setMainCategory(text.substring(0, text.indexOf("/")));
            category.setSubCategory(text.substring(text.indexOf("/")));
          } else {
            category.setMainCategory(text);
          }
          duplicateStrings.add(text);
          categories.add(category);
        }
      }
      return categories;
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing article categories", e);
    }
  }


  public Set<URI> parseArticleTypes(Document articleXml) throws ArchiveProcessException {
    try {
      Set<URI> articleTypes = new HashSet<URI>();
      final String uriBase = "http://rdf.plos.org/RDF/articleType/";
      articleTypes.add(URI.create(uriBase + xpathUtil.evaluate(articleXml, "/article/@article-type")));
      NodeList categoryNodes = xpathUtil.selectNodes(articleXml,
          "//article-meta/article-categories/subj-group[@subj-group-type = 'heading']/subject");
      for (int i = 0; i < categoryNodes.getLength(); i++) {
        final String type = URLEncoder.encode(categoryNodes.item(i).getChildNodes().item(0).getNodeValue(), "UTF-8");
        articleTypes.add(URI.create(uriBase + type));
      }
      return articleTypes;
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing article types", e);
    } catch (UnsupportedEncodingException e) {
      throw new ArchiveProcessException("Error encoding article type", e);
    }
  }


  private List<ObjectInfo> parseSecondaryObjects(Document manifest, Document articleXml, Article article, ZipFile archive) throws ArchiveProcessException {
    try {
      List<String> uris = convertToStringList(xpathUtil.selectNodes(manifest, "//articleBundle/object/@uri"));
      List<ObjectInfo> secondaryObjects = new ArrayList<ObjectInfo>(uris.size());

      for (String uri : uris) {
        ObjectInfo objectInfo = new ObjectInfo();
        objectInfo.setId(URI.create(uri));
        objectInfo.setIsPartOf(article);
        if (xpathUtil.selectSingleNode(articleXml, "//front/journal-meta/issn[@pub-type = 'epub']") != null) {
          objectInfo.seteIssn(xpathUtil.evaluate(articleXml,"//front/journal-meta/issn[@pub-type = 'epub']/text()"));
        }

        DublinCore dublinCore = new DublinCore();
        dublinCore.setIdentifier(uri);
        dublinCore.setDate(article.getDublinCore().getDate());

        NodeList representationNodes = xpathUtil.selectNodes(manifest,
            "//articleBundle/object[@uri = '" + uri + "']/representation");
        for (int i = 0; i < representationNodes.getLength(); i++) {
          final String fileName = representationNodes.item(i).getAttributes().getNamedItem("entry").getNodeValue();
          final String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
          if (mapSecondaryObjectType(extension) != null) {
            dublinCore.setType(mapSecondaryObjectType(extension));
            break;
          }
        }
        objectInfo.setRepresentations(formatRepresentations(archive, representationNodes, objectInfo));


        //add contributors
        if (article.getDublinCore().getCreators() != null) {
          dublinCore.setCreators(new HashSet<String>(article.getDublinCore().getCreators()));
        }
        if (article.getDublinCore().getContributors() != null) {
          dublinCore.setContributors(new HashSet<String>(article.getDublinCore().getContributors()));
        }

        dublinCore.setRights(article.getDublinCore().getRights());

        Node node = xpathUtil.selectSingleNode(articleXml, "/article/body//*[@xlink:href = '" + uri + "'][1]");
        if (node != null) {
          //Context Element is the element surrounding the reference to this object (which is usually an image)
          Node contextElement = node.getNodeName().equalsIgnoreCase("supplementary-material")
              ? node : node.getParentNode();
          for (int i = 0; i < contextElement.getChildNodes().getLength(); i++) {
            final Node item = contextElement.getChildNodes().item(i);
            if (item.getNodeName().equalsIgnoreCase("label")) {
              dublinCore.setTitle(getAllText(item));
            } else if (item.getNodeName().equalsIgnoreCase("caption")) {
              dublinCore.setDescription(getAllText(item, true));
            } else if (dublinCore.getTitle() != null && dublinCore.getDescription() != null) {
              //we're done looping through nodes
              break;
            }
          }
          objectInfo.setContextElement(contextElement.getNodeName());
        }
        objectInfo.setDublinCore(dublinCore);
        secondaryObjects.add(objectInfo);
      }

      Collections.sort(secondaryObjects, new Comparator<ObjectInfo>() {
        @Override
        public int compare(ObjectInfo objectInfo, ObjectInfo objectInfo1) {
          return objectInfo.getId().compareTo(objectInfo1.getId());
        }
      });

      return secondaryObjects;
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing secondary objects", e);
    }
  }

  public Set<RelatedArticle> parseRelatedArticles(Document articleXml) throws ArchiveProcessException {
    try {
      Integer count = Integer.valueOf(xpathUtil.evaluate(articleXml, "count(//front/article-meta/related-article)"));
      Set<RelatedArticle> relatedArticles = new HashSet<RelatedArticle>(count);
      for (int i = 1; i <= count; i++) {
        RelatedArticle relatedArticle = new RelatedArticle();
        relatedArticle.setArticle(
            URI.create(xpathUtil.evaluate(articleXml, "//front/article-meta/related-article[" + i + "]/@xlink:href")));
        relatedArticle.setRelationType(
            xpathUtil.evaluate(articleXml, "//front/article-meta/related-article[" + i + "]/@related-article-type"));
        relatedArticles.add(relatedArticle);
      }

      return relatedArticles;
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing related articles", e);
    }
  }

  /**
   * Parse the Bibliographic Citation from the xml and set it on the Dublin core
   * <p/>
   * NOTE: The author and editor list of UserProfiles will only have the name properties set
   *
   * @param articleXml  - the entire article xml document
   * @param articleMeta - the node from the xml document representing article meta data
   * @param dublinCore  - the dublin core being created
   * @param doi         - the doi of the article
   * @throws XPathExpressionException - if there's an error parsing the xml
   * @throws java.io.UnsupportedEncodingException
   *                                  - if a URL couldn't be encoded for the Citation url
   */
  private void setBibCitation(Document articleXml, Node articleMeta, DublinCore dublinCore, String doi) throws XPathExpressionException, UnsupportedEncodingException {
    Calendar date = Calendar.getInstance();
    date.setTime(dublinCore.getDate());

    Citation citation = new Citation();
    citation.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation.setYear(date.get(Calendar.YEAR));
    citation.setDisplayYear(String.valueOf(date.get(Calendar.YEAR)));

    final int month = date.get(Calendar.MONTH) + 1;
    citation.setMonth(month < 10 ? "0" + month : String.valueOf(month));

    final String day = date.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + date.get(Calendar.DAY_OF_MONTH)
        : String.valueOf(date.get(Calendar.DAY_OF_MONTH));
    citation.setDay(day);

    citation.setVolume(xpathUtil.evaluate(articleMeta, "//volume/text()"));
    if (citation.getVolume() != null) {
      try {
        citation.setVolumeNumber(Integer.valueOf(citation.getVolume()));
      } catch (NumberFormatException e) {
        //Ignore
      }
    }
    citation.setIssue(xpathUtil.evaluate(articleMeta, "//issue/text()"));
    citation.setPublisherLocation(xpathUtil.evaluate(articleXml, "//front/journal-meta/publisher/publisher-loc/text()"));
    citation.setPublisherName(xpathUtil.evaluate(articleXml, "//front/journal-meta/publisher/publisher-name/text()"));
    citation.setTitle(xpathUtil.evaluate(articleMeta, "//title-group/article-title/text()"));
    if (xpathUtil.selectSingleNode(articleXml, "//front/article-meta/counts/page-count") != null) {
      citation.setPages("1-" + xpathUtil.evaluate(articleMeta, "//counts/page-count/@count"));
    }
    citation.setELocationId(xpathUtil.evaluate(articleMeta, "//elocation-id/text()"));
    if (xpathUtil.selectSingleNode(articleXml, "//front/journal-meta/journal-id[@journal-id-type = 'nlm-ta']") != null) {
      citation.setJournal(xpathUtil.evaluate(articleXml, "//front/journal-meta/journal-id[@journal-id-type = 'nlm-ta']/text()"));
    } else {
      citation.setJournal(xpathUtil.evaluate(articleXml, "//front/journal-meta/journal-title/text()"));
    }

    final Node noteNode = xpathUtil.selectSingleNode(articleMeta, "//author-notes/fn[1]");
    if (noteNode != null) {
      citation.setNote(getAllText(noteNode));
    }
    NodeList collabAuthorNodes = xpathUtil.selectNodes(articleMeta, "//contrib-group/contrib[contrib-type = 'author']/text()");
    citation.setCollaborativeAuthors(convertToStringList(collabAuthorNodes));
    citation.setDoi(xpathUtil.evaluate(articleMeta, "//article-id[@pub-id-type = 'doi']/text()"));
    citation.setSummary(dublinCore.getDescription());
    citation.setUrl("http://dx.doi.org/" + URLEncoder.encode(doi, "UTF-8"));

    citation.setEditors(getArticleUserProfiles(articleXml, articleMeta, "editor", doi));
    citation.setAuthors(getArticleUserProfiles(articleXml, articleMeta, "author", doi));

    replaceEmptyStringProperties(citation);
    dublinCore.setBibliographicCitation(citation);
  }

  /**
   * Parse all the references and set them on the dublin core
   *
   * @param articleXml - the article xml to parse
   * @param dublinCore - the dublin core on which to set references
   * @param doi        - the doi.  Used for error messaging
   * @throws XPathExpressionException - if there's a problem parsing the xml
   */
  private void setReferences(Document articleXml, DublinCore dublinCore, String doi) throws XPathExpressionException {
    Integer count = Integer.valueOf(xpathUtil.evaluate(articleXml, "count(//article/back/ref-list/ref)"));
    List<Citation> references = new ArrayList<Citation>(count);
    for (int i = 1; i <= count; i++) {
      String nodeXpath = "//article/back/ref-list/ref[" + i + "]";
      Citation citation = new Citation();
      if (!xpathUtil.evaluate(articleXml, nodeXpath + "/citation/@citation-type").isEmpty()) {
        citation.setCitationType(mapCitationType(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/@citation-type")));
      }
      citation.setKey(xpathUtil.evaluate(articleXml, nodeXpath + "/label/text()"));
      String yearString = xpathUtil.evaluate(articleXml, nodeXpath + "/citation/year[1]");
      if (!yearString.isEmpty()) {
        citation.setDisplayYear(yearString);
        yearString = yearString.replaceAll("[^0-9]", "");
        try {
          citation.setYear(Integer.valueOf(yearString.substring(0, Math.min(4, yearString.length()))));
        } catch (NumberFormatException e) {
          //Ignore
        }
      }
      citation.setMonth(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/month[1]/text()"));
      citation.setDay(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/day[1]/text()"));
      final String volumeString = xpathUtil.evaluate(articleXml, nodeXpath + "/citation/volume[1]/text()");
      if (!volumeString.isEmpty()) {
        citation.setVolume(volumeString);
        try {
          citation.setVolumeNumber(Integer.valueOf(volumeString.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException e) {
          //Ignore
        }
      }
      citation.setIssue(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/issue[1]/text()"));

      if (xpathUtil.selectNodes(articleXml, nodeXpath + "/citation/article-title").getLength() > 0) {
        citation.setTitle(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/article-title[1]/text()"));
      } else if (xpathUtil.selectNodes(articleXml, nodeXpath + "/citation/source").getLength() > 0) {
        citation.setTitle(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/source[1]/text()"));
      }

      citation.setPublisherLocation(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/publisher-loc[1]/text()"));
      citation.setPublisherName(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/publisher-name[1]/text()"));
      if (xpathUtil.selectNodes(articleXml, nodeXpath + "/citation/page-range").getLength() > 0) {
        citation.setPages(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/page-range[1]/text()"));
      } else if (xpathUtil.selectSingleNode(articleXml, nodeXpath + "/citation/lpage") != null) {
        citation.setPages(
            xpathUtil.evaluate(articleXml, nodeXpath + "/citation/fpage/text()")
                + "-" + xpathUtil.evaluate(articleXml, nodeXpath + "/citation/lpage/text()"));
      } else {
        citation.setPages(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/fpage/text()"));
      }
      citation.setELocationId(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/fpage"));
      if ("journal".equals(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/@citation-type"))
          || "confproc".equals(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/@citation-type"))) {
        citation.setJournal(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/source[1]/text()"));
      }
      if (xpathUtil.selectSingleNode(articleXml, nodeXpath + "/citation/comment[1]") != null) {
        citation.setNote(getAllText(xpathUtil.selectSingleNode(articleXml, nodeXpath + "/citation/comment[1]")));
      }
      citation.setEditors(getReferenceUserProfiles(articleXml, nodeXpath, "editor", doi));
      citation.setAuthors(getReferenceUserProfiles(articleXml, nodeXpath, "author", doi));
      citation.setUrl(xpathUtil.evaluate(articleXml, nodeXpath + "/citation/@xlink:role"));
      replaceEmptyStringProperties(citation);
      references.add(citation);
    }
    dublinCore.setReferences(references);
  }

  private List<UserProfile> getArticleUserProfiles(Document articleXml, Node articleMeta, String contributorType, String doi) throws XPathExpressionException {
    Integer count = Integer.valueOf(
        xpathUtil.evaluate(articleMeta, "count(//contrib-group/contrib[@contrib-type = '" + contributorType + "'])"));
    List<UserProfile> userProfiles = new ArrayList<UserProfile>(count);
    for (int i = 1; i <= count; i++) {
      UserProfile userProfile = new UserProfile();
      final String nodeXpath = "//contrib-group/contrib[@contrib-type = '" + contributorType + "'][" + i + "]/name";

      final String realName = formatContributorName(articleXml, nodeXpath, doi);
      if (realName != null) {
        userProfile.setRealName(realName.trim());
      }
      userProfile.setSurnames(xpathUtil.evaluate(articleMeta, nodeXpath + "/surname/text()"));
      userProfile.setSuffix(xpathUtil.evaluate(articleMeta, nodeXpath + "/suffix/text()"));
      userProfile.setGivenNames(xpathUtil.evaluate(articleMeta, nodeXpath + "/given-names/text()"));
      userProfiles.add(userProfile);
    }
    return userProfiles;
  }

  private List<UserProfile> getReferenceUserProfiles(Document articleXml, String referenceXpath, String type, String doi) throws XPathExpressionException {
    Integer count = Integer.valueOf(
        xpathUtil.evaluate(articleXml, "count(" + referenceXpath + "/citation/person-group[@person-group-type = '" + type + "']/name)"));
    List<UserProfile> userProfiles = new ArrayList<UserProfile>(count);
    for (int i = 1; i <= count; i++) {
      UserProfile userProfile = new UserProfile();
      final String nodeXpath = referenceXpath + "/citation/person-group[@person-group-type = '" + type + "']/name[" + i + "]";

      final String realName = formatContributorName(articleXml, nodeXpath, doi);
      if (realName != null) {
        userProfile.setRealName(realName.trim());
      }
      userProfile.setSurnames(xpathUtil.evaluate(articleXml, nodeXpath + "/surname/text()"));
      userProfile.setSuffix(xpathUtil.evaluate(articleXml, nodeXpath + "/suffix/text()"));
      userProfile.setGivenNames(xpathUtil.evaluate(articleXml, nodeXpath + "/given-names/text()"));
      userProfiles.add(userProfile);
    }
    return userProfiles;
  }


  /**
   * Helper method to get the publication date, by checking nodes in order of preference
   *
   * @param articleMeta - the article meta node
   * @return - the publication date
   * @throws XPathExpressionException - if there's a problem parsing the xml
   */
  private Date getPubDate(Node articleMeta) throws XPathExpressionException {
    //pub date types, in preferred order
    for (String pubType : new String[]{"epub", "epub-ppub", "ppub", "ecorrected", "pcorrected"}) {
      Node dateNode = xpathUtil.selectSingleNode(articleMeta, "//pub-date[@pub-type = '" + pubType + "']");
      if (dateNode != null) {
        return formatDate(dateNode);
      }
    }
    //didn't find one of the pub date types above
    if (xpathUtil.selectSingleNode(articleMeta, "//pub-date[not(@pub-type)]") != null) {
      return formatDate(xpathUtil.selectSingleNode(articleMeta, "//pub-date[not(@pub-type)]"));
    } else {
      return formatDate(xpathUtil.selectNodes(articleMeta, "//pub-date").item(0));
    }
  }

  /**
   * Map from a citation type in article xml to an rdf type
   *
   * @param type - the type from the article xml
   * @return - the corresponding rdf type
   */
  private String mapCitationType(String type) {
    if (type.equalsIgnoreCase("book")) {
      return "http://purl.org/net/nknouf/ns/bibtex#Book";
    } else if (type.equalsIgnoreCase("commun")) {
      return "http://rdf.plos.org/RDF/citation/type#Informal";
    } else if (type.equalsIgnoreCase("confproc")) {
      return "http://purl.org/net/nknouf/ns/bibtex#Conference";
    } else if (type.equalsIgnoreCase("discussion")) {
      return "http://rdf.plos.org/RDF/citation/type#Discussion";
    } else if (type.equalsIgnoreCase("gov")) {
      return "http://rdf.plos.org/RDF/citation/type#Government";
    } else if (type.equalsIgnoreCase("journal")) {
      return "http://purl.org/net/nknouf/ns/bibtex#Article";
    } else if (type.equalsIgnoreCase("list")) {
      return "http://rdf.plos.org/RDF/citation/type#List";
    } else if (type.equalsIgnoreCase("other")) {
      return "http://purl.org/net/nknouf/ns/bibtex#Misc";
    } else if (type.equalsIgnoreCase("patent")) {
      return "http://rdf.plos.org/RDF/citation/type#Patent";
    } else if (type.equalsIgnoreCase("thesis")) {
      return "http://rdf.plos.org/RDF/citation/type#Thesis";
    } else if (type.equalsIgnoreCase("web")) {
      return "http://rdf.plos.org/RDF/citation/type#Web";
    }
    return null;
  }

  /**
   * Helper method to get the dublin core description, by checking nodes in order of preference
   * <p/>
   * TODO: Decide whether this String should include formatting, like html tags, new lines, and tabs.  Currently it
   * includes nested tags
   *
   * @param articleMeta - the article meta node
   * @return - the Description for the Dublin Core object, taken from an abstract summary
   * @throws XPathExpressionException - if there's a problem parsing the xml
   */
  private String getDescription(Node articleMeta) throws XPathExpressionException {
    for (String type : new String[]{"short", "web-summary", "toc", "summary", "ASCII"}) {
      final Node node = xpathUtil.selectSingleNode(articleMeta, "//abstract[@abstract-type = '" + type + "']");
      if (node != null) {
        return getAllText(node, true);
      }
    }
    if (xpathUtil.selectSingleNode(articleMeta, "//abstract[not(@abstract-type)]") != null) {
      return getAllText(xpathUtil.selectSingleNode(articleMeta, "//abstract[not(@abstract-type)]"), true);
    } else {
      return getAllText(xpathUtil.selectSingleNode(articleMeta, "//abstract[1]"), true);
    }
  }

  /**
   * Helper method to get all the text of child nodes of a given node
   *
   * @param node             - the node to use as base
   * @param includeChildTags - true to include the xml tags of child tags, false to not
   * @return - all nested text in the node
   */
  private String getAllText(Node node, boolean includeChildTags) {

    String text = "";
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (Node.TEXT_NODE == childNode.getNodeType()) {
        text += childNode.getNodeValue();
      } else if (Node.ELEMENT_NODE == childNode.getNodeType()) {
        if (includeChildTags) {
          text += "<" + childNode.getNodeName() + ">";
        }
        text += getAllText(childNode);
        if (includeChildTags) {
          text += "</" + childNode.getNodeName() + ">";
        }
      }
    }
    return text.replaceAll("[\n\t]", "").trim();
  }

  /**
   * Helper method to get all the text of child nodes of a given node
   *
   * @param node - the node to use as base
   * @return - the text of in the node
   */
  private String getAllText(Node node) {
    return getAllText(node, false);
  }

  /**
   * Helper method to convert convert a list of text nodes into a list of strings
   *
   * @param nodeList - a list of text nodes
   * @return - a list of strings with the text of each node
   */
  private List<String> convertToStringList(NodeList nodeList) {
    List<String> stringList = new ArrayList<String>(nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); i++) {
      stringList.add(nodeList.item(i).getNodeValue());
    }
    return stringList;
  }

  /**
   * Helper method to get the contributor names as strings
   *
   * @param doi          - the article doi.  Used for error messaging
   * @param articleMeta  - the article meta node
   * @param contribType- the type of contributor to get (contrib-type attribute)
   * @param articleXml   - the article xml
   * @return - a set of the contributer names of the appropriate type
   * @throws XPathExpressionException - if there's an error parsing the xml
   */
  private Set<String> getContributorNames(String doi, Node articleMeta, String contribType, Document articleXml) throws XPathExpressionException {
    Integer count = Integer.valueOf(
        xpathUtil.evaluate(articleMeta, "count(//contrib-group/contrib[@contrib-type = '" + contribType + "'])")
    );
    Set<String> contribNames = new HashSet<String>(count);
    for (int i = 1; i <= count; i++) {
      String nodeXpath = "//contrib-group/contrib[@contrib-type = '" + contribType + "'][" + i + "]/name";
      final String name = formatContributorName(articleXml, nodeXpath, doi);
      if (name != null) {
        contribNames.add(name.trim());
      }
    }
    return contribNames;
  }

  /**
   * Formats the contributor name
   *
   * @param articleXml - the article xml
   * @param nodeXPath  - an xpath string to the specific node containing the contributor name
   * @param doi        - the article doi.  Used for error messaging
   * @return - the formatted contributor name
   */
  private String formatContributorName(Document articleXml, String nodeXPath, String doi) {
    try {
      if (xpathUtil.selectSingleNode(articleXml, nodeXPath) != null) {
        if ("eastern".equalsIgnoreCase(xpathUtil.evaluate(articleXml, nodeXPath + "/@name-style"))) {
          return xpathUtil.evaluate(articleXml, nodeXPath + "surname/text()") + " " +
              xpathUtil.evaluate(articleXml, nodeXPath + "/given-names/text()") + " " +
              xpathUtil.evaluate(articleXml, nodeXPath + "/suffix/text()");
        } else {
          return xpathUtil.evaluate(articleXml, nodeXPath + "/given-names/text()") + " " +
              xpathUtil.evaluate(articleXml, nodeXPath + "/surname/text()") + " " +
              xpathUtil.evaluate(articleXml, nodeXPath + "/suffix/text()");
        }

      } else if (xpathUtil.selectSingleNode(articleXml, nodeXPath + "/../collab") != null) {
        return xpathUtil.evaluate(articleXml, nodeXPath + "/../collab/text()").trim();
      } else if (xpathUtil.selectSingleNode(articleXml, nodeXPath + "/../string-name") != null) {
        return xpathUtil.evaluate(articleXml, nodeXPath + "/../string-name/text()");
      } else {
        return null;
      }
    } catch (XPathExpressionException e) {
      log.warn("Error parsing author names for article: " + doi, e);
      return null;
    }
  }

  /**
   * Format a Date from a node
   *
   * @param dateNode - the node containing the date
   * @return - the date represented by the node
   * @throws XPathExpressionException - if there's an error parsing xml
   */
  private Date formatDate(Node dateNode) throws XPathExpressionException {
    Calendar date = Calendar.getInstance();
    //set the year, defaulting to the current year
    if (xpathUtil.selectSingleNode(dateNode, "//year") != null) {
      date.set(Calendar.YEAR,
          Integer.valueOf(xpathUtil.evaluate(dateNode, "//year/text()")));
    }
    //if there is a 'season' node, use defaults
    if (xpathUtil.selectSingleNode(dateNode, "//season") != null) {
      String season = xpathUtil.evaluate(dateNode, "//season/text()");
      if (season.equalsIgnoreCase("spring")) {
        date.set(Calendar.MONTH, Calendar.MARCH);
        date.set(Calendar.DAY_OF_MONTH, 21);
      } else if (season.equalsIgnoreCase("summer")) {
        date.set(Calendar.MONTH, Calendar.JUNE);
        date.set(Calendar.DAY_OF_MONTH, 21);
      } else if (season.equalsIgnoreCase("fall")) {
        date.set(Calendar.MONTH, Calendar.SEPTEMBER);
        date.set(Calendar.DAY_OF_MONTH, 23);
      } else if (season.equalsIgnoreCase("winter")) {
        date.set(Calendar.MONTH, Calendar.JANUARY);
        date.set(Calendar.DAY_OF_MONTH, 1);
      }
    } else if (xpathUtil.selectSingleNode(dateNode, "//month") != null) {
      //else set the month, defaulting to the current month
      date.set(Calendar.MONTH,
          Integer.valueOf(xpathUtil.evaluate(dateNode, "//month/text()")) - 1);
    }
    //set the day, using the node if available
    if (xpathUtil.selectSingleNode(dateNode, "//day") != null) {
      date.set(Calendar.DAY_OF_MONTH,
          Integer.valueOf(xpathUtil.evaluate(dateNode, "//day/text()")));
    }
    //if we've changed the year or month, set the day to 1
    else if (date.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)
        || date.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH)) {
      date.set(Calendar.DAY_OF_MONTH, 1);
    }
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    return date.getTime();
  }

  private void replaceEmptyStringProperties(Citation citation) {
    if (citation.getKey() != null && citation.getKey().isEmpty()) {
      citation.setKey(null);
    }
    if (citation.getDisplayYear() != null && citation.getDisplayYear().isEmpty()) {
      citation.setDisplayYear(null);
    }
    if (citation.getMonth() != null && citation.getMonth().isEmpty()) {
      citation.setMonth(null);
    }
    if (citation.getDay() != null && citation.getDay().isEmpty()) {
      citation.setDay(null);
    }
    if (citation.getVolume() != null && citation.getVolume().isEmpty()) {
      citation.setVolume(null);
    }
    if (citation.getIssue() != null && citation.getIssue().isEmpty()) {
      citation.setIssue(null);
    }
    if (citation.getTitle() != null && citation.getTitle().isEmpty()) {
      citation.setTitle(null);
    }
    if (citation.getPublisherLocation() != null && citation.getPublisherLocation().isEmpty()) {
      citation.setPublisherLocation(null);
    }
    if (citation.getPublisherName() != null && citation.getPublisherName().isEmpty()) {
      citation.setPublisherName(null);
    }
    if (citation.getPages() != null && citation.getPages().isEmpty()) {
      citation.setPages(null);
    }
    if (citation.getELocationId() != null && citation.getELocationId().isEmpty()) {
      citation.setELocationId(null);
    }
    if (citation.getJournal() != null && citation.getJournal().isEmpty()) {
      citation.setJournal(null);
    }
    if (citation.getNote() != null && citation.getNote().isEmpty()) {
      citation.setNote(null);
    }
    if (citation.getUrl() != null && citation.getUrl().isEmpty()) {
      citation.setUrl(null);
    }
    if (citation.getDoi() != null && citation.getDoi().isEmpty()) {
      citation.setDoi(null);
    }
    if (citation.getSummary() != null && citation.getSummary().isEmpty()) {
      citation.setSummary(null);
    }
    if (citation.getCitationType() != null && citation.getCitationType().isEmpty()) {
      citation.setCitationType(null);
    }
  }

  private URI mapSecondaryObjectType(String extension) {
    String mimeType = getMimeType(extension);
    String mediaType = mimeType.substring(0, mimeType.indexOf("/"));
    if (mediaType.equalsIgnoreCase("image")) return URI.create("http://purl.org/dc/dcmitype/StillImage");
    else if (mediaType.equalsIgnoreCase("video")) return URI.create("http://purl.org/dc/dcmitype/MovingImage");
    else if (mediaType.equalsIgnoreCase("audio")) return URI.create("http://purl.org/dc/dcmitype/Sound");
    else if (mediaType.equalsIgnoreCase("text")) return URI.create("http://purl.org/dc/dcmitype/Text");
    else if (mimeType.equalsIgnoreCase("application/vnd.ms-excel"))
      return URI.create("http://purl.org/dc/dcmitype/Dataset");

    return null;
  }

  private synchronized String getMimeType(String extension) {
    String mimeType = mimeTypes.get(extension.toLowerCase());
    return mimeType != null ? mimeType : "application/octet-stream";
  }

  /**
   * Helper method to extract XML from the zip file
   *
   * @param zipFile  - the zip file containing the file to extract
   * @param fileName - the file to extract
   * @return - the parsed xml file
   * @throws java.io.IOException      - if there's a problem reading from the zip file
   * @throws org.xml.sax.SAXException - if there's a problem parsing the xml
   */
  private Document extractXml(ZipFile zipFile, String fileName) throws IOException, SAXException {
    InputStream inputStream = null;
    try {
      inputStream = zipFile.getInputStream(zipFile.getEntry(fileName));
      return documentBuilder.parse(inputStream);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Error closing zip input stream during ingest processing", e);
        }
      }
    }
  }
}
