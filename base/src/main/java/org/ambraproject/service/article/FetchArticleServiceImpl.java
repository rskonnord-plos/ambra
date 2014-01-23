/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
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
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.rhino.shared.AuthorsXmlExtractor;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.xml.XMLService;
import org.ambraproject.util.TextUtils;
import org.ambraproject.util.XPathUtil;
import org.ambraproject.views.AuthorView;
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

import javax.activation.DataSource;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetch article service.
 */
public class FetchArticleServiceImpl extends HibernateServiceImpl implements FetchArticleService {
  private static final Logger log = LoggerFactory.getLogger(FetchArticleServiceImpl.class);
  private static final String ARTICLE_LOCK = "ArticleHtmlCache-Lock-";

  private XPathUtil xPathUtil = new XPathUtil();
  private XMLService articleTransformService;
  private FileStoreService fileStoreService;
  private Cache articleHtmlCache;
  private String guestCrossRefUrl;

  /**
   * For the articleInfo, get the article HTML
   *
   * @param article the articleInfo object
   *
   * @return the article HTML
   *
   * @throws ApplicationException
   * @throws NoSuchArticleIdException
   */
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
   *
   * @param article The article to transform
   * @return String representing the annotated article as HTML
   * @throws org.ambraproject.ApplicationException
   *          ApplicationException
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

  /**
   * For the articleDOI, get the article XML datasource
   *
   * @param articleDoi the articleInfo object
   *
   * @return the article XML
   *
   * @throws ApplicationException
   * @throws NoSuchArticleIdException
   */
  private DataSource getArticleXML(final String articleDoi)
      throws NoSuchArticleIdException {
    String fsid = FSIDMapper.doiTofsid(articleDoi, "XML");

    if (fsid == null)
      throw new NoSuchArticleIdException(articleDoi);

    List assets = hibernateTemplate.findByCriteria(DetachedCriteria.forClass(ArticleAsset.class)
        .add(Restrictions.eq("doi", articleDoi))
        .add(Restrictions.eq("extension", "XML")));

    if (assets.size() == 0)
      throw new NoSuchArticleIdException(articleDoi);

    return new ByteArrayDataSource(fileStoreService, fsid, (ArticleAsset) assets.get(0));
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
   *
   * @param article article uri
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
   * Get the authors and some meta data for a given article.
   *
   * @param doc article xml
   * @return author affiliations
   */
  public List<AuthorView> getAuthors(Document doc) {
    return AuthorsXmlExtractor.getAuthors(doc, xPathUtil);
  }

  /**
   *
   * @param doc the article xml document in question
   *
   * @param authors list of article authors
   *
   * @return an xml-sorted map of article affiliates and their respective authors
   */
  public Map<String, List<AuthorView>> getAuthorsByAffiliation(Document doc, List<AuthorView> authors) throws RuntimeException {

    Map<String, List<AuthorView>> authorsByAffiliation = new LinkedHashMap<String, List<AuthorView>>();

    try {
      /*
      <String, String> in the following case is, in xpath parlance, <//aff@id>,<//aff/addr-line/text() but
      AuthorView cues on the //aff/addr-line/text() part, so we need to add a level of indirection
      */
      Map<String, String> originalAffiliateMap = AuthorsXmlExtractor.getAffiliateMap(doc, xPathUtil);
      for (Map.Entry<String, String> entry : originalAffiliateMap.entrySet()) {
        authorsByAffiliation.put(entry.getValue(), new ArrayList<AuthorView>());
      }

      for (AuthorView currentAuthorView : authors) {
        for (String affiliate : currentAuthorView.getAffiliations()) {

          List<AuthorView> authorList = authorsByAffiliation.get(affiliate);
          if (authorList != null) {
            authorsByAffiliation.get(affiliate).add(currentAuthorView);
          } else {
            log.error(new StringBuilder("Could not associate ").append(currentAuthorView.getFullName()).append(" with institution ").append(affiliate).toString());
          }

        }
      }
    } catch (XPathException e) {
      throw new RuntimeException();
    }

      //make sure to return only non-empty lists
      Map<String, List<AuthorView>> tempAuthorAffiliations = new LinkedHashMap<String, List<AuthorView>>();
      for(Map.Entry<String, List<AuthorView>> affiliationMapping: authorsByAffiliation.entrySet()){

        if (affiliationMapping.getValue().size() > 0) {
          tempAuthorAffiliations.put(affiliationMapping.getKey(), affiliationMapping.getValue());
        }

      }
      authorsByAffiliation = tempAuthorAffiliations;



    return authorsByAffiliation;

  }

  /**
   * Extract the body content from EoC article, clean the text and return it.
   * @param doc
   * @return expressionOfConcern text
   * @throws TransformerException
   * @throws XPathExpressionException
   */
  public String getEocBody(Document doc) throws TransformerException, XPathExpressionException {

    Node eocBody = xPathUtil.selectSingleNode(doc, "//body");
    Node eocTitle =  xPathUtil.selectSingleNode(doc, "//title-group/article-title");
    String bodyText = TextUtils.getAsXMLString(eocBody);

    for (int index = 0; index < AuthorsXmlExtractor.PATTERNS.length; index++) {
      bodyText = AuthorsXmlExtractor.PATTERNS[index].matcher(bodyText).replaceAll(
          AuthorsXmlExtractor.REPLACEMENTS[index]);
    }

    bodyText = "<p><strong>" + eocTitle.getTextContent() + "</strong></p>" + bodyText ;
    return bodyText;
  }

  /**
   * @param document        a document to search for nodes
   * @param xpathExpression XPath describing the nodes to find
   * @return a list of the text content of the nodes found, or {@code null} if none
   */
  private List<String> findTextFromNodes(Document document, String xpathExpression) {
    NodeList nodes;

    try {
      nodes = xPathUtil.selectNodes(document, xpathExpression);
    } catch (XPathExpressionException ex) {
      log.error("Error occurred while gathering text with: " + xpathExpression, ex);
      return null;
    }

    List<String> text = new ArrayList<String>(nodes.getLength());

    for (int i = 0; i < nodes.getLength(); i++) {
      text.add(nodes.item(i).getTextContent());
    }

    return text;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getCorrespondingAuthors(Document doc) {
    //Sample XML node:
    //<corresp id="cor1">* E-mail:
    // <email xlink:type="simple">maud.hertzog@ibcg.biotoul.fr</email> (MH);
    // <email xlink:type="simple">philippe.chavrier@curie.fr</email> (PC)</corresp>
    //<corresp xmlns:mml="http://www.w3.org/1998/Math/MathML"
    // xmlns:xlink="http://www.w3.org/1999/xlink"
    // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="cor1">* E-mail:
    // <email xlink:type="simple">maud.hertzog@ibcg.biotoul.fr</email> (MH);
    // <email xlink:type="simple">philippe.chavrier@curie.fr</email> (PC)</corresp>

    try {
      Node authNode = xPathUtil.selectSingleNode(doc, "//corresp");
      if (authNode != null) {
        String authors = TextUtils.getAsXMLString(authNode);
        return parseOutAuthorEmails(authors);
      }
    } catch (XPathExpressionException ex) {
      log.error("Error occurred while gathering text with: //corresp", ex);
    } catch (TransformerException ex) {
      log.error("Error occurred while gathering text with: //corresp", ex);
    }

    return new ArrayList<String>();
  }

  /**
   * For a given corresp XML node, parse out the article author's emails
   *
   * This method is static and public because we want to be able to access it via the unit test
   *
   * @param authors
   * @return
   */

  public static List<String> parseOutAuthorEmails(String authors) {
    List<String> result = new ArrayList<String>();

    //This fixes email links:
    String r = AuthorsXmlExtractor.transFormCorresponding(authors);

    //Remove prepending text
    r = r.replaceAll("<span.*?/span>", "");
    r = r.replaceFirst(".*?[Ee]-mail:", "");

    //Remove extra carriage return
    r = r.replaceAll("\\n", "");

    //Split on "<a" as the denotes a new email address
    String[] emails = r.split("(?=<a)");

    for(int a = 0; a < emails.length; a++) {
      if(emails[a].trim().length() > 0) {
        String email = emails[a];
        //Remove ; and "," from address
        email = email.replaceAll("[,;]","");
        email = email.replaceAll("[Ee]mail:","");
        email = email.replaceAll("[Ee]-mail:","");
        result.add(email.trim());
      }
    }

    return result;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getAuthorContributions(Document doc) {
    //TODO: Test this code across many articles
    return findTextFromNodes(doc, "//author-notes/fn[@fn-type='con']");
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getAuthorCompetingInterests(Document doc) {
    //TODO: Test this code across many articles
    return findTextFromNodes(doc, "//fn[@fn-type='conflict']");
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
   *
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
   *
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
   * Decorates the citation elements of the XML DOM with extra information from the citedArticle table in the DB. An
   * extraCitationInfo element is appended to each citation element.  It will contain between one and two attributes
   * with the extra info: citedArticleID, the DB primary key, and doi, the DOI string, if it exists.
   *
   * @param doc           DOM of the XML
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

      // If sortOrder on citedArticle has duplicate value, you will get below error.Ideally it should not happen
      // but since sortOrder is not unique it may be possible to update that field from backend to have duplicate value
      // Now index is on sortOrder(article.hbm.xml), index will be only on one of those of duplicate value and
      // hence citedArticle will have less count then the xml.
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
          setExtraCitationInfo(extraInfo, citedArticle);
          citationNode.appendChild(extraInfo);
        }
      }
    } catch (XPathExpressionException xpee) {
      throw new ApplicationException(xpee);
    }

    return doc;
  }

  /**
   * Set the extraInfo element based on the citedArticle.
   * @param extraInfo
   * @param citedArticle
   */
  private void setExtraCitationInfo(Element extraInfo, CitedArticle citedArticle) {
    extraInfo.setAttribute("citedArticleID", Long.toString(citedArticle.getID()));
    String doi = citedArticle.getDoi();

    if (doi != null && !doi.isEmpty()) {
      extraInfo.setAttribute("doi", doi);
    }

    String title = citedArticle.getTitle() == null ? "" : citedArticle.getTitle();
    String author = getAuthorStringForLookup(citedArticle);

    author = author.replaceAll("<[^>]+>", ""); // remove any HTML marker for query
    title = title.replaceAll("<[^>]+>", ""); // remove any HTML marker for query

    String crossRefUrl = createCrossRefUrl(doi, author, title);
    String pubMedUrl = createPubMedUrl(author, title);
    String googleScholarUrl = createGoogleScholarUrl(author, title);

    if (crossRefUrl != null && !crossRefUrl.isEmpty()) {
      extraInfo.setAttribute("crossRefUrl", crossRefUrl);
    }

    if (pubMedUrl != null && !pubMedUrl.isEmpty()) {
      extraInfo.setAttribute("pubMedUrl", pubMedUrl);
    }

    if (googleScholarUrl != null && !googleScholarUrl.isEmpty()) {
      extraInfo.setAttribute("googleScholarUrl", googleScholarUrl);
    }
  }

  /**
   * Set the crossRefUrl
   * @param doi
   * @param author
   * @param title
   * @return crossRefUrl
   */
  private String createCrossRefUrl(String doi, String author, String title) {
    String crossRefUrl;

    if (doi != null && !doi.isEmpty()) {
      crossRefUrl = "http://dx.doi.org/" + doi;
    } else {
      crossRefUrl = guestCrossRefUrl;
      try {
        crossRefUrl += "?auth2=" + URLEncoder.encode(author, "ISO-8859-1")
            + "&atitle2=" + URLEncoder.encode(title, "ISO-8859-1")
            + "&auth=" + URLEncoder.encode(author, "ISO-8859-1")
            + "&atitle=" + URLEncoder.encode(title, "ISO-8859-1");
      }
      catch (UnsupportedEncodingException ex) {
        log.info("ignoring exception in URLEncoder", ex);
      }
    }

    return crossRefUrl;
  }

  /**
   * set the pubMedUrl
   * @param author
   * @param title
   * @return pubMedUrl
   */
  private  String createPubMedUrl(String author, String title) {
    String pubMedUrl = null, pubMedAuthorQuery = "";

    if (author != null && !author.isEmpty()) {
      pubMedAuthorQuery = author + "[author] AND ";
    }
    try {
      pubMedUrl = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=PubMed&cmd=Search&doptcmdl=Citation&defaultField=Title+Word&term="
          + URLEncoder.encode(pubMedAuthorQuery, "UTF-8")
          + URLEncoder.encode(title, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.info("ignoring exception in URLEncoder", ex);
    }

    return pubMedUrl;
  }

  /**
   * set the googleScholarUrl
   * @param author
   * @param title
   * @return googleScholarUrl
   */
  private String createGoogleScholarUrl(String author, String title) {
    String googleScholarUrl = null, googleAuthorQuery = "";

    if (author != null && !author.isEmpty()) {
      googleAuthorQuery = "author:" + author + " ";
    }
    try {
      googleScholarUrl = "http://scholar.google.com/scholar?hl=en&safe=off&q="
          + URLEncoder.encode(googleAuthorQuery, "UTF-8")
          + "%22" + URLEncoder.encode(title, "UTF-8") + "%22";
    } catch (UnsupportedEncodingException ex) {
      log.info("ignoring exception in URLEncoder", ex);
    }

    return googleScholarUrl;
  }

  /**
   * Formats a citation's authors for searching in CrossRef.
   *
   * @param citedArticle persistent class representing the citation
   * @return String with author information formatted for a CrossRef query
   */
  private String getAuthorStringForLookup(CitedArticle citedArticle) {
    List<CitedArticleAuthor> authors = citedArticle.getAuthors();
    return (authors != null && authors.size() > 0) ? authors.get(0).getSurnames() : "";
  }

  /**
   *
   * @param guestCrossRefUrl The guestCrossRefUrl to use
   */
  @Required
  public void setGuestCrossRefUrl(String guestCrossRefUrl) {
    this.guestCrossRefUrl = guestCrossRefUrl;
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
