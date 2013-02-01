package org.ambraproject.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.util.Iterator;

/**
 * @author Joe Osowski
 * @author Jen Song
 * @author Dragisa Krsmanovic
 *
 * A collection of utilities for a class to extend and use and working with
 * Article DOM objects
 *
 */
public abstract class ArticleXMLReader {

  private static final Logger log = LoggerFactory.getLogger(ArticleXMLReader.class);

  protected static final String AMBRA_NAMESPACE = "http://www.ambraproject.org/article/additionalInfo";
  private XPathExpression doiExpr;
  private XPathExpression ambraExpr;
  private XPathExpression newSubjectExpr;
  private XPathExpression oldSubjectExpr;

  public ArticleXMLReader() throws XPathExpressionException {
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    xpath.setNamespaceContext(new AmbraNamespaceContext());

    this.doiExpr = xpath.compile("/article/front/article-meta/article-id[@pub-id-type='doi']/text()");
    this.ambraExpr = xpath.compile("/article/ambra:ambra");
    this.newSubjectExpr = xpath.compile("//article-meta/article-categories/subj-group[@subj-group-type='Discipline-v2']");
    this.oldSubjectExpr = xpath.compile("//article-meta/article-categories/subj-group[@subj-group-type='Discipline']/subject");
  }

  protected NodeList getNewSubjects(Document article) throws XPathExpressionException {
    return (NodeList)newSubjectExpr.evaluate(article, XPathConstants.NODESET);
  }

  protected NodeList getOldSubjects(Document article) throws XPathExpressionException {
    return (NodeList)oldSubjectExpr.evaluate(article, XPathConstants.NODESET);
  }

  protected String getDoi(Document article) throws XPathExpressionException {
    return (String) doiExpr.evaluate(article, XPathConstants.STRING);
  }

  protected  Element getAdditionalInfo(Document article) throws XPathExpressionException {
    Element additionalInfoElement = (Element)ambraExpr.evaluate(article, XPathConstants.NODE);

    if (additionalInfoElement == null) {
      additionalInfoElement = article.createElementNS(AMBRA_NAMESPACE, "ambra");
      article.getDocumentElement().appendChild(additionalInfoElement);
    }

    return additionalInfoElement;
  }

  private static class AmbraNamespaceContext implements NamespaceContext {

    public String getNamespaceURI(String prefix) {
      if (prefix == null) {
        throw new NullPointerException("Null prefix");
      } else if ("ambra".equals(prefix)) {
        return AMBRA_NAMESPACE;
      } else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
      return XMLConstants.NULL_NS_URI;
    }

    // This method isn't necessary for XPath processing.

    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.

    public Iterator getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }

  }
}

