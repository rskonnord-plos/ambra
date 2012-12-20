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
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
   * Get the authors and some meta data for a given article.  I wanted to make this method bigger, but
   * I ran out of time.
   *
   * @param doc article xml
   * @param doc article xml
   * @return author affiliations
   */
  public List<AuthorView> getAuthors(Document doc) {

    ArrayList<AuthorView> list = new ArrayList<AuthorView>();

    if (doc == null) {
      return list;
    }

    try {
      Map<String, String> affiliateMap = getAffiliateMap(doc);
      Map<String, String> addressMap = getAddressMap(doc);
      Map<String, String> otherFootnotesMap = getOtherFootnotesMap(doc);

      //Get all the authors
      NodeList authorList = xPathUtil.selectNodes(doc, "//contrib-group/contrib[@contrib-type='author']");

      for (int i = 0; i < authorList.getLength(); i++) {
        Node authorNode = authorList.item(i);

        //Create temp author document fragment to search out of
        DocumentFragment authorDoc = doc.createDocumentFragment();
        authorDoc.appendChild(authorNode);

        Node surNameNode = xPathUtil.selectSingleNode(authorDoc, "//name/surname");
        Node givenNameNode = xPathUtil.selectSingleNode(authorDoc, "//name/given-names");
        Node collabNameNode = xPathUtil.selectSingleNode(authorDoc, "//collab");
        Node behalfOfNode = xPathUtil.selectSingleNode(authorDoc, "//on-behalf-of");
        NodeList otherFootnotesNodeList = xPathUtil.selectNodes(authorDoc, "//xref[@ref-type='fn']");

        //Sometimes, an author is not a person, but a collab
        //Note:10.1371/journal.pone.0032315
        if (surNameNode == null && givenNameNode == null) {
          if(collabNameNode != null) {
            //If current node is a collab author.  Make sure previous author
            //Is not marked as "on behalf of"  If so, we can ignore this collab
            //It is assumed this collab contains the same text as the value of the
            //Previous authors "on behalf of" node
            if(list.size() > 0) {
              if(list.get(list.size() - 1).getOnBehalfOf() != null) {

                //Craziness ensues here.  Previous author has "on behalf of", lets append any
                //footnotes from this contrib to that author!
                for(int a = 0; a < otherFootnotesNodeList.getLength(); a++) {
                  Node node = otherFootnotesNodeList.item(a);

                  if(node.getAttributes().getNamedItem("rid") != null) {
                    String id = node.getAttributes().getNamedItem("rid").getTextContent();
                    String value = otherFootnotesMap.get(id);

                    if(value != null) {
                      AuthorView av = list.get(list.size() - 1);

                      //This may look a bit odd, but because the AuthorView is immutable
                      //I have to create a new copy to change any values
                      List<String> footnotes = new ArrayList<String>();
                      footnotes.addAll(av.getCustomFootnotes());

                      value = transFormFootnote(value);

                      footnotes.add(value);

                      list.set(list.size() - 1,
                        AuthorView.builder(av)
                        .setCustomFootnotes(footnotes)
                        .build());
                    }
                  }
                }

                break;
              }
            }
          }

          givenNameNode = collabNameNode;
        }

        // If both of these are null then don't bother to add
        if (surNameNode != null || givenNameNode != null) {
          Node suffixNode = xPathUtil.selectSingleNode(authorDoc, "//name/suffix");
          Node equalContribNode = xPathUtil.selectSingleNode(authorDoc, "//@equal-contrib");
          Node deceasedNode = xPathUtil.selectSingleNode(authorDoc, "//@deceased");
          Node addressNode = xPathUtil.selectSingleNode(authorDoc, "//xref[@ref-type='fn']/sup[contains(text(),'¤')]/..");
          Node corresAuthorNode = xPathUtil.selectSingleNode(authorDoc, "//xref[@ref-type='corresp']");
          NodeList affList = xPathUtil.selectNodes(authorDoc, "//xref[@ref-type='aff']");

          // Either surname or givenName can be blank
          String surname = (surNameNode == null) ? null : surNameNode.getTextContent();
          String givenName = (givenNameNode == null) ? null : givenNameNode.getTextContent();
          String suffix = (suffixNode == null) ? null : suffixNode.getTextContent();
          String onBehalfOf = (behalfOfNode == null) ? null : behalfOfNode.getTextContent();

          boolean equalContrib = (equalContribNode != null);
          boolean deceased = (deceasedNode != null);
          boolean groupContrib = false;

          String corresponding = null;
          String currentAddress = null;

          if(addressNode != null) {
            if(addressNode.getAttributes().getNamedItem("rid") != null) {
              String fnId = addressNode.getAttributes().getNamedItem("rid").getTextContent();
              currentAddress = addressMap.get(fnId);
            }
          }

          //Footnotes
          //Note this web page for notes on author footnotes:
          //http://wiki.plos.org/pmwiki.php/Publications/FootnoteSymbolOrder
          List<String> otherFootnotes = new ArrayList<String>();
          for(int a = 0; a < otherFootnotesNodeList.getLength(); a++) {
            Node node = otherFootnotesNodeList.item(a);

            if(node.getAttributes().getNamedItem("rid") != null) {
              String id = node.getAttributes().getNamedItem("rid").getTextContent();

              String value = otherFootnotesMap.get(id);

              if(value != null) {
                //Sometimes the "other" node is used to contrib equally as well
                //Is there a better way to do this?
                if(value.contains("contributed equally")) {
                  groupContrib = true;
                }

                value = transFormFootnote(value);

                otherFootnotes.add(value);
              }
            }
          }

          if(corresAuthorNode != null) {
            Node attr = corresAuthorNode.getAttributes().getNamedItem("rid");

            if(attr == null) {
              log.warn("No rid attribute found for xref ref-type=\"corresp\" node.");
            } else {
              String rid = attr.getTextContent();

              Node correspondAddrNode = xPathUtil.selectSingleNode(doc, "//author-notes/corresp[@id='" + rid + "']");

              if(correspondAddrNode == null) {
                log.warn("No node found for corrsponding author: author-notes/corresp[@id='\" + rid + \"']");
              } else {
                corresponding = correspondAddrNode.getTextContent();
                corresponding = transFormCorresponding(corresponding);
              }
            }
          }

          List<String> affiliations = new ArrayList<String>();

          // Build a list of affiliations for this author
          for (int j = 0; j < affList.getLength(); j++) {
            Node anode = affList.item(j);

            if(anode.getAttributes().getNamedItem("rid") != null) {
              String affId = anode.getAttributes().getNamedItem("rid").getTextContent();
              affiliations.add(affiliateMap.get(affId));
            }
          }

          AuthorView author = AuthorView.builder()
            .setGivenNames(givenName)
            .setSurnames(surname)
            .setSuffix(suffix)
            .setCurrentAddress(currentAddress)
            .setOnBehalfOf(onBehalfOf)
            .setEqualContrib(equalContrib)
            .setGroupContrib(groupContrib)
            .setDeceased(deceased)
            .setCorresponding(corresponding)
            .setAffiliations(affiliations)
            .setCustomFootnotes(otherFootnotes)
            .build();

          list.add(author);
        }
      }
    } catch (Exception e) {
      //TODO: Why does this die silently?
      log.error("Error occurred while gathering the author affiliations.", e);
    }

    return list;
  }

  /**
   * Kludge for FEND-794, A better ways of doing this?
   */
  private String transFormFootnote(String source) {
    String dest = source.replace("<sup>¶</sup>", "<span class=\"pilcro\">¶</span>");
    dest = dest.replaceAll("^<p>¶", "<p><span class=\"pilcro\">¶</span>");

    return dest;
  }

  /**
   * Kludge for FEND-794, A better ways of doing this?
   */
  private String transFormCorresponding(String source) {
    String dest = source.replaceAll("^E-mail:", "<span class=\"email\">* E-mail:</span>");
    dest = dest.replaceAll("^\\* E-mail:", "<span class=\"email\">* E-mail:</span>");
    dest = dest.replace("*To whom", "<span class=\"email\">*</span>To whom");
    dest = dest.replace("* To whom", "<span class=\"email\">*</span>To whom");

    return dest;
  }

  /**
   * Grab all affiliations and put them into their own map
   * @param doc
   * @return
   */
  private Map<String, String> getAffiliateMap(Document doc) throws XPathExpressionException {
    Map<String, String> affiliateMap = new HashMap<String, String>();

    NodeList affiliationNodeList = xPathUtil.selectNodes(doc, "//aff");

    //Map all affiliation id's to their affiliation strings
    for (int a = 0; a < affiliationNodeList.getLength(); a++) {
      Node node = affiliationNodeList.item(a);
      // Not all <aff>'s have the 'id' attribute.
      String id = (node.getAttributes().getNamedItem("id") == null) ? "" :
        node.getAttributes().getNamedItem("id").getTextContent();

      log.debug("Found affiliation node:" + id);

      // Not all <aff> id's are affiliations.
      if (id.startsWith("aff")) {
        DocumentFragment df = doc.createDocumentFragment();
        df.appendChild(node);

        StringBuilder res = new StringBuilder();

        if(xPathUtil.selectSingleNode(df, "//institution") != null) {
          res.append(xPathUtil.evaluate(df, "//institution"));
        }

        if(xPathUtil.selectSingleNode(df, "//addr-line") != null) {
          if(res.length() > 0) {
            res.append(" ");
          }
          res.append(xPathUtil.evaluate(df, "//addr-line"));
        }

        affiliateMap.put(id, res.toString());
      }
    }

    return affiliateMap;
  }

  private Map<String, String> getAddressMap(Document doc) throws XPathExpressionException {
    Map<String, String> addressMap = new HashMap<String, String>();

    //Grab all the Current address information and place them into a map
    NodeList currentAddressNodeList = xPathUtil.selectNodes(doc, "//fn[@fn-type='current-aff']");
    for (int a = 0; a < currentAddressNodeList.getLength(); a++) {
      Node node = currentAddressNodeList.item(a);
      String id = (node.getAttributes().getNamedItem("id") == null) ? "" :
        node.getAttributes().getNamedItem("id").getTextContent();

      log.debug("Current address node:" + id);

      DocumentFragment df = doc.createDocumentFragment();
      df.appendChild(node);

      String address = xPathUtil.evaluate(df, "//p");
      addressMap.put(id, address);
    }

    return addressMap;
  }

  private Map<String, String> getOtherFootnotesMap(Document doc) throws XPathExpressionException, TransformerException {
    Map<String, String> otherFootnotesMap = new HashMap<String, String>();

    //Grab all 'other' footnotes and put them into their own map
    NodeList footnoteNodeList = xPathUtil.selectNodes(doc, "//fn[@fn-type='other']");

    for (int a = 0; a < footnoteNodeList.getLength(); a++) {
      Node node = footnoteNodeList.item(a);
      // Not all <aff>'s have the 'id' attribute.
      String id = (node.getAttributes().getNamedItem("id") == null) ? "" :
        node.getAttributes().getNamedItem("id").getTextContent();

      log.debug("Found footnote node:" + id);

      DocumentFragment df = doc.createDocumentFragment();
      df.appendChild(node);

      String footnote = TextUtils.getAsXMLString(xPathUtil.selectSingleNode(df, "//p"));
      otherFootnotesMap.put(id, footnote);
    }

    return otherFootnotesMap;
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

    int length = nodes.getLength();
    if (length == 0) {
      return null;
    }

    List<String> text = new ArrayList<String>(length);

    for (int i = 0; i < length; i++) {
      Node item = nodes.item(i);
      text.add(item.getTextContent());
    }

    return text;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getCorrespondingAuthors(Document doc) {
    //TODO: Test this code across many articles
    return findTextFromNodes(doc, "//corresp/email");
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
