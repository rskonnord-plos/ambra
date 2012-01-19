package org.topazproject.ambra.solr;

import org.topazproject.ambra.util.Pair;
import org.w3c.dom.Document;

import java.util.List;
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

  /**
   * Get the most viewed Articles from Solr
   *
   * @param journal - the journal to get articles from
   * @param limit   - The number of articles to return
   * @param numDays - the number of days over which to count the views. If null; the default is over all time
   * @return - a list of dois and titles of the most viewed articles, in order
   */
  //TODO: make this a list of articles instead of pairs
  //This is *bad practice* because it mixes the view (i.e. "we just want to see the title and doi") with 
  //the model (i.e. the "most viewed articles") ... but ArticleOtmService is also bad
  public List<Pair<String,String>> getMostViewedArticles(String journal, int limit, Integer numDays) throws SolrException;

}
