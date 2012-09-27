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

package org.ambraproject.service.article;

import org.ambraproject.ApplicationException;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.xml.XMLService;
import org.ambraproject.views.AuthorExtra;
import org.ambraproject.views.CitationReference;
import org.ambraproject.views.article.ArticleInfo;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetch article service.
 */
public class FetchArticleServiceImpl extends HibernateServiceImpl implements FetchArticleService {
  private static final Logger log = LoggerFactory.getLogger(FetchArticleServiceImpl.class);
  private static final String ARTICLE_LOCK = "ArticleHtmlCache-Lock-";

  private XMLService articleTransformService;
  private FileStoreService fileStoreService;
  private Cache articleHtmlCache;

  private String getTransformedArticle(final ArticleInfo article)
      throws ApplicationException, NoSuchArticleIdException {
    try {
      DataSource content = getArticleXML(article.getDoi());
      Document doc = articleTransformService.createDocBuilder().parse(content.getInputStream());

      doc = addExtraCitationInfo(doc, article.getCitedArticles());

      return articleTransformService.getTransformedDocument(doc);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param article The article to transform
   * @return String representing the annotated article as HTML
   * @throws org.ambraproject.ApplicationException ApplicationException
   */
  @Override
  @Transactional(readOnly = true)
  public String getArticleAsHTML(final ArticleInfo article) throws Exception {
    final Object lock = (ARTICLE_LOCK + article.getDoi()).intern(); //lock @ Article level

    String content = articleHtmlCache.get(article.getDoi(),
      new Cache.SynchronizedLookup<String, Exception>(lock) {
        @Override
        public String lookup() throws Exception {
          return getTransformedArticle(article);
        }
      });
    
    return content;
  }

  private DataSource getArticleXML(final String articleDoi)
    throws NoSuchArticleIdException {
    String fsid = FSIDMapper.doiTofsid(articleDoi, "XML");

    if (fsid == null)
      throw new NoSuchArticleIdException(articleDoi);

    List assets = hibernateTemplate.findByCriteria(DetachedCriteria.forClass(ArticleAsset.class)
          .add(Restrictions.eq("doi", articleDoi))
          .add(Restrictions.eq("extension", "XML")));

    if(assets.size() == 0)
      throw new NoSuchArticleIdException(articleDoi);

    return new ByteArrayDataSource(fileStoreService, fsid, (ArticleAsset)assets.get(0));
  }

  /**
   * @param articleTransformService The articleXmlUtils to set.
   */
  @Required
  public void setArticleTransformService(XMLService articleTransformService) {
    this.articleTransformService = articleTransformService;
  }

  /**
   * Get the article xml
   * @param article article uri
   *
   * @return article xml
   */
  public Document getArticleDocument(final ArticleInfo article) {
    Document doc = null;
    DataSource content = null;
    String articleURI = article.getDoi();

    try {
      content = getArticleXML(articleURI);
    } catch (Exception e) {
      log.warn("Article " + articleURI + " not found.");
      return null;
    }

    try {
      doc = articleTransformService.createDocBuilder().parse(content.getInputStream());
    } catch (Exception e) {
      log.error("Error parsing the article xml for article " + articleURI, e);
      return null;
    }

    return doc;
  }

  /**
   * Get the author affiliations for a given article
   * @param doc article xml
   * @param doc article xml
   * @return author affiliations
   */
  public ArrayList<AuthorExtra> getAuthorAffiliations(Document doc) {

    ArrayList<AuthorExtra> list = new ArrayList<AuthorExtra>();
    Map<String, String> affiliateMap = new HashMap<String, String>();

    if (doc == null) {
      return list;
    }

    try {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      XPathExpression affiliationListExpr = xpath.compile("//aff");
      XPathExpression affiliationAddrExpr = xpath.compile("//addr-line");

      NodeList affiliationNodeList = (NodeList) affiliationListExpr.evaluate(doc, XPathConstants.NODESET);

      // Map all affiliation id's to their affiliation strings
      for (int i = 0; i < affiliationNodeList.getLength(); i++) {
        Node node =  affiliationNodeList.item(i);
        // Not all <aff>'s have the 'id' attribute.
        String id = (node.getAttributes().getNamedItem("id") == null) ? "" : node.getAttributes().getNamedItem("id").getTextContent();
        // Not all <aff> id's are affiliations.
        if (id.startsWith("aff")) {
          DocumentFragment df = doc.createDocumentFragment();
          df.appendChild(node);
          String address = ((Node)affiliationAddrExpr.evaluate(df, XPathConstants.NODE)).getTextContent();
          affiliateMap.put(id,address);
        }
      }

      XPathExpression authorExpr = xpath.compile("//contrib-group/contrib[@contrib-type='author']");
      XPathExpression surNameExpr = xpath.compile("//name/surname");
      XPathExpression givenNameExpr = xpath.compile("//name/given-names");
      XPathExpression affExpr = xpath.compile("//xref[@ref-type='aff']");

      NodeList authorList = (NodeList) authorExpr.evaluate(doc, XPathConstants.NODESET);

      for (int i=0; i < authorList.getLength(); i++) {
        Node cnode = authorList.item(i);
        DocumentFragment df = doc.createDocumentFragment();
        df.appendChild(cnode);
        Node sNode = (Node) surNameExpr.evaluate(df, XPathConstants.NODE);
        Node gNode = (Node) givenNameExpr.evaluate(df, XPathConstants.NODE);
        
        // Either surname or givenName can be blank
        String surname = (sNode == null) ? "" : sNode.getTextContent();
        String givenName = (gNode == null) ? "" : gNode.getTextContent(); 
        // If both are null then don't bother to add
        if ((sNode != null) || (gNode != null)) {
          NodeList affList = (NodeList) affExpr.evaluate(df, XPathConstants.NODESET);
          ArrayList<String> affiliations = new ArrayList<String>();

          // Build a list of affiliations for this author
          for (int j = 0; j < affList.getLength(); j++) {
            Node anode = affList.item(j);
            String affId = anode.getAttributes().getNamedItem("rid").getTextContent();
            affiliations.add(affiliateMap.get(affId));
          }

          AuthorExtra authorEx = new AuthorExtra();
          authorEx.setAuthorName(surname, givenName);
          authorEx.setAffiliations(affiliations);
          list.add(authorEx);
        }
      }
    } catch (Exception e) {
      log.error("Error occurred while gathering the author affiliations.", e);
    }

    return list;
  }

  /**
   * @inheritDoc
   */
  public String getCorrespondingAuthor(Document doc)
  {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    try {
      XPathExpression xpr = xpath.compile("//corresp/email");
      NodeList nodeList = (NodeList) xpr.evaluate(doc, XPathConstants.NODESET);

      if(nodeList.getLength() > 0) {
        //TODO: Test this code across many articles
        return nodeList.item(0).getFirstChild().getTextContent();
      }

    } catch (XPathExpressionException ex) {
      log.error("Error occurred while gathering the author correspondence.", ex);
    }

    return null;
  }

  /**
   * @inheritDoc
   */
  public String getAuthorContributions(Document doc)
  {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    try {
      XPathExpression xpr = xpath.compile("//author-notes/fn[@fn-type='con']");
      NodeList nodeList = (NodeList) xpr.evaluate(doc, XPathConstants.NODESET);

      if(nodeList.getLength() > 0) {
        //TODO: Test this code across many articles
        return nodeList.item(0).getTextContent();
      }

    } catch (XPathExpressionException ex) {
      log.error("Error occurred while gathering the author correspondence.", ex);
    }

    return null;
  }

  /**
   * Returns a list of ref nodes from the ref-list of the DOM.
   *
   * @param doc DOM representation of the XML
   * @return NodeList of ref elements
   * @throws XPathExpressionException
   */
  private NodeList getReferenceNodes(Document doc) throws XPathExpressionException {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr = xpath.compile("//back/ref-list[title='References']/ref");
    Object result = expr.evaluate(doc, XPathConstants.NODESET);

    NodeList refList = (NodeList) result;

    if (refList.getLength() == 0) {
      expr = xpath.compile("//back/ref-list/ref");
      result = expr.evaluate(doc, XPathConstants.NODESET);
      refList = (NodeList) result;
    }
    return refList;
  }

  /**
   * Returns the publication type attribute (Journal, Book, etc) of a citation node.
   *
   * @param citationNode citation element
   * @return publication type
   */
  private String getCitationType(Node citationNode) {
    NamedNodeMap nnm = citationNode.getAttributes();
    Node nnmNode = nnm.getNamedItem("citation-type");

    // nlm 3.0 has this attribute listed as 'publication-type'
    nnmNode = nnmNode == null ? nnm.getNamedItem("publication-type") : nnmNode;

    // some old articles do not have this attribute
    return nnmNode == null ? null : nnmNode.getTextContent();
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
      NodeList refList = getReferenceNodes(doc);

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      XPathExpression typeExpr = xpath.compile("//citation | //nlm-citation | //element-citation");
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
          String citationType = getCitationType(resultNode);
          if (citationType != null) {
            citation.setCitationType(citationType);
          }
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

  /**
   * Indicates whether the given cited article has enough data to render a "find this article online" link.
   *
   * @param citedArticle the cited article of interest
   * @return true if the citedArticle has a non-empty title, it has a non-empty DOI, or it has authors information
   */
  private boolean citedArticleIsValid(CitedArticle citedArticle) {
    return StringUtils.isNotBlank(citedArticle.getTitle()) || StringUtils.isNotBlank(citedArticle.getDoi())
        || citedArticle.getAuthors().size() > 0;
  }

  /**
   * Decorates the citation elements of the XML DOM with extra information from the citedArticle table in the DB.
   * An extraCitationInfo element is appended to each citation element.  It will contain between one and two
   * attributes with the extra info: citedArticleID, the DB primary key, and doi, the DOI string, if it exists.
   *
   * @param doc DOM of the XML
   * @param citedArticles List of CitedArticle persistent objects
   * @return modified DOM
   * @throws ApplicationException
   */
  private Document addExtraCitationInfo(Document doc, List<CitedArticle> citedArticles) throws ApplicationException {
    if (citedArticles.isEmpty()) {
      return doc;  // This happens in some unit tests.
    }
    try {
      NodeList referenceList = getReferenceNodes(doc);

      // TODO: can this ever happen?
      if (referenceList.getLength() != citedArticles.size()) {
        throw new ApplicationException(String.format("Article has %d citedArticles but %d references",
            citedArticles.size(), referenceList.getLength()));
      }
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression citationExpr = xpath.compile("./citation|./nlm-citation|./element-citation|./mixed-citation");
      for (int i = 0; i < referenceList.getLength(); i++) {
        Node referenceNode = referenceList.item(i);
        Node citationNode = (Node) citationExpr.evaluate(referenceNode, XPathConstants.NODE);
        CitedArticle citedArticle = citedArticles.get(i);
        if (citationNode != null && "journal".equals(getCitationType(citationNode))
            && citedArticleIsValid(citedArticle)) {
          Element extraInfo = doc.createElement("extraCitationInfo");
          citationNode.appendChild(extraInfo);
          extraInfo.setAttribute("citedArticleID", Long.toString(citedArticle.getID()));
          String doi = citedArticle.getDoi();
          if (doi != null && !doi.isEmpty()) {
            extraInfo.setAttribute("doi", doi);
          }
        }
      }
    } catch (XPathExpressionException xpee) {
      throw new ApplicationException(xpee);
    }
    return doc;
  }

  /**
   * @param articleHtmlCache The Article(transformed) cache to use
   */
  @Required
  public void setArticleHtmlCache(Cache articleHtmlCache) {
    this.articleHtmlCache = articleHtmlCache;
  }

  /**
   * @param fileStoreService The fileStoreService to use
   */
  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  private static class ByteArrayDataSource implements DataSource {
    private final FileStoreService fileStoreService;
    private final String fsid;
    private final ArticleAsset asset;

    public ByteArrayDataSource(FileStoreService fileStoreService, String fsid, ArticleAsset asset) {
      this.fileStoreService = fileStoreService;
      this.fsid = fsid;
      this.asset = asset;
    }

    public String getName() {
      return asset.getDoi() + "#" + asset.getExtension();
    }

    public String getContentType() {
      String ct = asset.getContentType();
      return (ct != null) ? ct : "application/octet-stream";
    }

    public InputStream getInputStream() throws IOException {
      InputStream fs = null;

      try {
        fs = fileStoreService.getFileInStream(fsid);
      } catch (Exception e) {
        throw new IOException(e.getMessage(), e);
      }
      return fs;
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("writing not supported");
    }
  }
}
