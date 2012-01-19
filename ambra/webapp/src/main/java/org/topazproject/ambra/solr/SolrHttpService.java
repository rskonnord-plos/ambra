package org.topazproject.ambra.solr;

import org.w3c.dom.Document;

import java.util.Map;

/**
 * Interface for beans to make http requests to the PLoS Solr server
 * <p/>
 * <p/>
 * User: Alex Kudlick Date: Feb 15, 2011
 * <p/>
 * org.topazproject.ambra.solr
 */
public interface SolrHttpService {

  /**
   * Basic method for making a request to the Solr server, with a Map of key/value pairs to pass as parameters
   *
   * @param params - the params to pass to solr.  See <a href="http://wiki.plos.org/pmwiki.php/Topaz/SOLRSchema#SolrFieldList">
   *               this wiki page</a> for a list of solr fields
   * @return - A Document wrapper around the Solr response
   */
  public Document makeSolrRequest(Map<String, String> params) throws SolrException;


}
