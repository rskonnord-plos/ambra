/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.article.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.Annotator;
import org.topazproject.ambra.article.AuthorExtra;
import org.topazproject.ambra.article.CitationReference;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.service.XMLService;
import org.topazproject.ambra.util.TextUtils;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch article service.
 */
public class TopazFetchArticleService implements FetchArticleService {

  /**
   * All Article(transformed)/ArticleInfo/Annotation/Citation cache activity is syncronized on
   * ARTICLE_LOCK.
   */
  public  static final String ARTICLE_LOCK     = "ArticleAnnotationCache-Lock-";
  private static final String ARTICLE_KEY      = "ArticleAnnotationCache-Article-";

  private XMLService articleTransformService;

  private static final Logger log = LoggerFactory.getLogger(TopazFetchArticleService.class);
  private AnnotationService annotationService;
  private ArticlePersistenceService articleService;

  private Cache articleAnnotationCache;
  private Invalidator invalidator;

  private String getTransformedArticle(final String articleURI)
      throws ApplicationException, NoSuchArticleIdException {
    try {
      return articleTransformService.getTransformedDocument(getAnnotatedContentAsDocument(articleURI));
    } catch (ApplicationException ae) {
      throw ae;
    } catch (NoSuchArticleIdException nsae) {
      throw nsae;
    } catch (Exception e) {
      throw new ApplicationException (e);
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getURIAsHTML(final String articleURI) throws Exception {
    final Object lock = (ARTICLE_LOCK + articleURI).intern();  // lock @ Article level

    return articleAnnotationCache.get(ARTICLE_KEY  + articleURI, -1,
            new Cache.SynchronizedLookup<String, Exception>(lock) {
              public String lookup() throws Exception {
                return getTransformedArticle(articleURI);
              }
            });
  }

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws javax.xml.parsers.ParserConfigurationException ParserConfigurationException
   * @throws org.xml.sax.SAXException SAXException
   * @throws java.io.IOException IOException
   * @throws java.net.URISyntaxException URISyntaxException
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @throws org.topazproject.ambra.article.service.NoSuchArticleIdException NoSuchArticleIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI)
      throws ParserConfigurationException, SAXException, IOException, URISyntaxException,
             ApplicationException, NoSuchArticleIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   *
   * @param articleDOI - the DOI of the (Article) content
   * @return Article DOM document
   * @throws java.io.IOException
   * @throws org.topazproject.ambra.article.service.NoSuchArticleIdException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   * @throws org.topazproject.ambra.ApplicationException
   */
  private Document getAnnotatedContentAsDocument(final String articleDOI)
      throws IOException, NoSuchArticleIdException, ParserConfigurationException, SAXException,
             ApplicationException {
    DataSource content;

    try {
      content = articleService.getContent(articleDOI, articleTransformService.getArticleRep());
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(articleDOI,
                                         "(representation=" + articleTransformService.getArticleRep() + ")",
                                         ex);
    }

    final ArticleAnnotation[] annotations = annotationService.listAnnotations(articleDOI, null);
    return applyAnnotationsOnContentAsDocument(content, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument(DataSource content,
                                                       ArticleAnnotation[] annotations)
          throws ApplicationException {
    try {
      if (log.isDebugEnabled())
        log.debug("Parsing article xml ...");

      Document doc = articleTransformService.createDocBuilder().parse(content.getInputStream());
      if (annotations.length == 0)
        return doc;

      if (log.isDebugEnabled())
        log.debug("Applying " + annotations.length + " annotations to article ...");

      return Annotator.annotateAsDocument(doc, annotations);
    } catch (Exception e){
      if (log.isErrorEnabled()) {
        log.error("Could not apply annotations to article: " + content.getName(), e);
      }
      throw new ApplicationException("Applying annotations failed for resource:" +
                                     content.getName(), e);
    }
  }

  /**
   * Getter for AnnotationService
   *
   * @return the annotationService
   */
  public AnnotationService getAnnotationService() {
    return annotationService;
  }

  /**
   * Setter for annotationService
   *
   * @param annotationService annotationService
   */
  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }


  /**
   * @param articleService The articleService to set.
   */
  @Required
  public void setArticleService(ArticlePersistenceService articleService) {
    this.articleService = articleService;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      articleAnnotationCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * @param articleTransformService The articleXmlUtils to set.
   */
  @Required
  public void setArticleTransformService(XMLService articleTransformService) {
    this.articleTransformService = articleTransformService;
  }

  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @return list of article uri's
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state)
      throws ApplicationException {
    try {
      return articleService.getArticleIds(startDate, endDate, null, null, state, true, 0);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @param ascending controls the sort order (by date).
   * @return list of article uri's
   * @throws org.topazproject.ambra.ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state, boolean ascending)
    throws ApplicationException {
    try {
      return articleService.getArticleIds(startDate, endDate, null, null, state, ascending, 0);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private class Invalidator extends AbstractObjectListener {
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      handleEvent(id, o, updates, false);
    }
    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      handleEvent(id, o, null, true);
    }
    private void handleEvent(String id, Object o, Updates updates, boolean removed) {
      if ((o instanceof Article) && removed) {
        if (log.isDebugEnabled())
          log.debug("Invalidating transformed-article for the article that was deleted.");
        articleAnnotationCache.remove(ARTICLE_KEY + id);
      } else if (o instanceof ArticleAnnotation) {
        if (log.isDebugEnabled())
          log.debug("ArticleAnnotation changed/deleted. Invalidating transformed-article " +
              " for the article this was annotating or is about to annotate.");
        articleAnnotationCache.remove(ARTICLE_KEY + ((ArticleAnnotation)o).getAnnotates().toString());
        if ((updates != null) && updates.isChanged("annotates")) {
           List<String> v = updates.getOldValue("annotates");
           if (v.size() == 1)
             articleAnnotationCache.remove(ARTICLE_KEY + v.get(0));
        }
      }
    }
  }

  /**
   * Get the article xml
   * @param articleURI article uri
   * @return article xml
   */
  public Document getArticleDocument(String articleURI) {
    Document doc = null;
    try {
      DataSource content = articleService.getContent(articleURI, articleTransformService.getArticleRep());
      doc = articleTransformService.createDocBuilder().parse(content.getInputStream());
    } catch (Exception e) {
      log.error("Error parsing the article xml");
    }

    return doc;
  }

  /**
   * Get the author affiliations for a given article
   * @param doc article xml
   * @return author affiliations
   */
  public ArrayList<AuthorExtra> getAuthorAffiliations(Document doc) {

    ArrayList<AuthorExtra> list = new ArrayList<AuthorExtra>();

    if (doc == null) {
      return list;
    }

    try {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      XPathExpression expr = xpath.compile("//contrib-group/contrib[@contrib-type='author']");
      Object result = expr.evaluate(doc, XPathConstants.NODESET);

      NodeList contribList = (NodeList) result;

      XPathExpression surNameExpr = xpath.compile("//name/surname");
      XPathExpression givenNameExpr = xpath.compile("//name/given-names");
      XPathExpression affExpr = xpath.compile("//xref[@ref-type='aff']");

      for (int i = 0; i < contribList.getLength(); i++) {
        String surName = null;
        String givenName = null;
        String affId = null;
        String affiliation = null;

        Node contribNode = contribList.item(i);

        // get surname
        DocumentFragment df = doc.createDocumentFragment();
        df.appendChild(contribNode);

        Object resultObj = surNameExpr.evaluate(df, XPathConstants.NODE);
        Node resultNode = (Node) resultObj;
        if (resultNode != null) {
          surName = resultNode.getTextContent();
        }

        // get given name
        resultObj = givenNameExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          givenName = resultNode.getTextContent();
        }

        // get affiliation id
        resultObj = affExpr.evaluate(contribNode, XPathConstants.NODESET);
        NodeList resultNodeList = (NodeList) resultObj;
        ArrayList<String> affiliations = new ArrayList<String>();
        if (resultNodeList != null) {
          for (int j = 0; j < resultNodeList.getLength(); j++) {
            Node xrefNode = resultNodeList.item(j);
            NamedNodeMap nnm = xrefNode.getAttributes();
            Node rid = nnm.getNamedItem("rid");
            affId = rid.getTextContent();

            XPathExpression affExpr2 = xpath.compile("//aff[@id='" + affId + "']/addr-line");
            Object affObj = affExpr2.evaluate(doc, XPathConstants.NODE);
            Node addrLineNode = (Node) affObj;
            if (addrLineNode != null) {
              affiliation = addrLineNode.getTextContent();
              affiliations.add(affiliation);
            }
          }
        }

        if (surName != null && givenName != null) {
          AuthorExtra as = new AuthorExtra();
          as.setAuthorName(givenName + " " + surName);
          as.setAffiliations(affiliations);
          list.add(as);
        }
      }

    } catch (Exception e) {
      log.error("Error occurred while gathering the author affiliations.", e);
    }

    return list;

  }

  /**
   * Get references for a given article
   * @param doc article xml
   * @return references
   */
  public ArrayList<CitationReference> getReferences(Document doc) {
    ArrayList<CitationReference> list = new ArrayList<CitationReference>();

    if (doc == null) {
      return list;
    }

    try {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression expr = xpath.compile("//ref-list/ref");
      Object result = expr.evaluate(doc, XPathConstants.NODESET);

      NodeList refList = (NodeList) result;

      XPathExpression typeExpr = xpath.compile("//citation | nlm-citation");
      XPathExpression titleExpr = xpath.compile("//article-title");
      XPathExpression authorsExpr = xpath.compile("//person-group[@person-group-type='author']/name");
      XPathExpression journalExpr = xpath.compile("//source");
      XPathExpression volumeExpr = xpath.compile("//volume");
      XPathExpression numberExpr = xpath.compile("//label");
      XPathExpression fPageExpr = xpath.compile("//fpage");
      XPathExpression lPageExpr = xpath.compile("//lpage");
      XPathExpression yearExpr = xpath.compile("//year");
      XPathExpression publisherExpr = xpath.compile("//publisher-name");

      for (int i = 0; i < refList.getLength(); i++) {

        Node refNode = refList.item(i);
        CitationReference citation = new CitationReference();

        DocumentFragment df = doc.createDocumentFragment();
        df.appendChild(refNode);

        // citation type
        Object resultObj = typeExpr.evaluate(df, XPathConstants.NODE);
        Node resultNode = (Node) resultObj;
        if (resultNode != null) {
          NamedNodeMap nnm = resultNode.getAttributes();
          Node nnmNode = nnm.getNamedItem("citation-type");
          citation.setCitationType(nnmNode.getTextContent());
        }

        // title
        resultObj = titleExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setTitle(resultNode.getTextContent());
        }

        // authors
        resultObj = authorsExpr.evaluate(df, XPathConstants.NODESET);
        NodeList resultNodeList = (NodeList) resultObj;
        ArrayList<String> authors = new ArrayList<String>();
        for (int j = 0; j < resultNodeList.getLength(); j++) {
          Node nameNode = resultNodeList.item(j);
          NodeList namePartList = nameNode.getChildNodes();
          String surName = "";
          String givenName = "";
          for (int k = 0; k < namePartList.getLength(); k++) {
            Node namePartNode = namePartList.item(k);
            if (namePartNode.getNodeName().equals("surname")) {
              surName = namePartNode.getTextContent();
            } else if (namePartNode.getNodeName().equals("given-names")) {
              givenName = namePartNode.getTextContent();
            }
          }
          authors.add(givenName + " " + surName);
        }

        citation.setAuthors(authors);

        // journal title
        resultObj = journalExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setJournalTitle(resultNode.getTextContent());
        }

        // volume
        resultObj = volumeExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setVolume(resultNode.getTextContent());
        }

        // citation number
        resultObj = numberExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setNumber(resultNode.getTextContent());
        }

        // citation pages
        String firstPage = null;
        String lastPage = null;
        resultObj = fPageExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          firstPage = resultNode.getTextContent();
        }

        resultObj = lPageExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          lastPage = resultNode.getTextContent();
        }

        if (firstPage != null) {
          if (lastPage != null) {
            citation.setPages(firstPage + "-" + lastPage);
          } else {
            citation.setPages(firstPage);
          }
        }

        // citation year
        resultObj = yearExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setYear(resultNode.getTextContent());
        }

        // citation publisher
        resultObj = publisherExpr.evaluate(df, XPathConstants.NODE);
        resultNode = (Node) resultObj;
        if (resultNode != null) {
          citation.setPublisher(resultNode.getTextContent());
        }

        list.add(citation);
      }

    } catch (Exception e) {
      log.error("Error occurred while gathering the citation references.", e);
    }

    return list;

  }

  /**
   * Returns abbreviated journal name
   * @param doc article xml
   * @return abbreviated journal name
   */
  public String getJournalAbbreviation(Document doc) {
    String journalAbbrev = "";

    if (doc == null) {
      return journalAbbrev;
    }

    try {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      XPathExpression expr = xpath.compile("//journal-meta/journal-id[@journal-id-type='nlm-ta']");
      Object resultObj = expr.evaluate(doc, XPathConstants.NODE);
      Node resultNode = (Node) resultObj;
      if (resultNode != null) {
        journalAbbrev = resultNode.getTextContent();
      }
    } catch (Exception e) {
      log.error("Error occurred while getting abbreviated journal name.", e);
    }

    return journalAbbrev;

  }
  
}
