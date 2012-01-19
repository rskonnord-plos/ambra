package org.plos;

import org.w3c.dom.Document;

/**
 * Placeholder for Article class
 * 
 * @author Stephen Cheng
 * 
 */
public class Article {
  private int version;

  private String uri;

  private Document xmlDoc;

  public Article() {

  }

  /**
   * Retrieves the article identified by the <code>uri</code> param from the store
   * 
   * @param uri
   * @param signOnId
   * @param authToken
   * @return <code>Article</code> given by <code>uri</code> param
   */
  public static Article getArticle(String uri, String signOnId, String authToken) {
    return new Article();
  }

  /**
   * Returns all articles in the store as an array.
   * 
   * @param signOnId
   * @param authToken
   * @return <code>Article[]</code>
   */
  public static Article[] getAllArticles(String signOnId, String authToken) {
    return new Article[0];
  }

  /**
   * @return Returns the uri.
   */
  public String getUri() {
    return uri;
  }

  /**
   * @param uri
   *          The uri to set.
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * @return Returns the version.
   */
  public int getVersion() {
    return version;
  }

  /**
   * @param version
   *          The version to set.
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * @return Returns the xmlDoc.
   */
  public Document getXmlDoc() {
    return xmlDoc;
  }

  /**
   * @param xmlDoc
   *          The xmlDoc to set.
   */
  public void setXmlDoc(Document xmlDoc) {
    this.xmlDoc = xmlDoc;
  }

}
