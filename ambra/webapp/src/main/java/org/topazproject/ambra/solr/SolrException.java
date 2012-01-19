package org.topazproject.ambra.solr;

/**
 * User: Alex Kudlick Date: Feb 17, 2011
 */
public class SolrException extends Exception{
  public SolrException() {
  }

  public SolrException(String message) {
    super(message);
  }

  public SolrException(String message, Throwable cause) {
    super(message, cause);
  }

  public SolrException(Throwable cause) {
    super(cause);
  }
}
