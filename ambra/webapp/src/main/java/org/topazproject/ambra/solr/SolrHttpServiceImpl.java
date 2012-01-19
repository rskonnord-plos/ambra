package org.topazproject.ambra.solr;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link org.topazproject.ambra.solr.SolrHttpService} that uses URL objects to make http requests to
 * the solr server
 * <p/>
 * <p/>
 * User: Alex Kudlick Date: Feb 15, 2011
 * <p/>
 * org.topazproject.ambra.solr
 */
public class SolrHttpServiceImpl implements SolrHttpService {

  private static final Logger log = LoggerFactory.getLogger(SolrHttpServiceImpl.class);
  private String solrUrl;
  private Configuration config;
  private SolrFieldConversion solrFieldConverter;
  private static final String NAME_ATTR = "name";
  private static final String DOI_ATTR = "id";
  private static final String TITLE_ATTR = "title";
  private static final String XML = "xml";
  private static final String URL_CONFIG_PARAM = "ambra.services.search.server.url";
  private static final String RETURN_TYPE_PARAM = "wt";
  private static final String Q_PARAM = "q";
  private static final String NO_FILTER = "*:*";
  /**
   * number of milliseconds to wait on a url connection to SOLR
   */
  private static final int CONNECTION_TIMEOUT = 100;

  @Override
  public Document makeSolrRequest(Map<String, String> params) throws SolrException {
    if (solrUrl == null || solrUrl.isEmpty()) {
      setSolrUrl(config.getString(URL_CONFIG_PARAM));
    }

    //make sure the return type is xml
    if (!params.keySet().contains(RETURN_TYPE_PARAM) || !params.get(RETURN_TYPE_PARAM).equals(XML)) {
      params.put(RETURN_TYPE_PARAM, XML);
    }
    //make sure that we include a 'q' parameter
    if (!params.keySet().contains(Q_PARAM)) {
      params.put(Q_PARAM, NO_FILTER);
    }

    String queryString = "?";
    for (String param : params.keySet()) {
      String value = params.get(param);
      if (queryString.length() > 1) {
        queryString += "&";
      }
      queryString += (cleanInput(param) + "=" + cleanInput(value));
    }

    URL url;
    String urlString = solrUrl + queryString;
    log.debug("Making Solr http request to " + urlString);
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      throw new SolrException("Bad Solr Url: " + urlString, e);
    }

    InputStream urlStream = null;
    Document doc = null;
    try {
      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(CONNECTION_TIMEOUT);
      connection.connect();
      urlStream = connection.getInputStream();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.parse(urlStream);
    } catch (IOException e) {
      throw new SolrException("Error connecting to the Solr server at " + solrUrl, e);
    } catch (ParserConfigurationException e) {
      throw new SolrException("Error configuring parser xml parser for solr response", e);
    } catch (SAXException e) {
      throw new SolrException("Solr Returned bad XML for url: " + urlString, e);
    } finally {
      //Close the input stream
      if (urlStream != null) {
        try {
          urlStream.close();
        } catch (IOException e) {
          log.error("Error closing url stream to Solr", e);
        }
      }
    }

    return doc;
  }

  /**
   * Clean a string input for insertion into a url
   *
   * @param param
   * @return
   */
  private String cleanInput(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return param;
    }
  }

  @Override
  public List<Pair<String, String>> getMostViewedArticles(String journal, int limit, Integer numDays) throws SolrException {
    Map<String, String> params = new HashMap<String, String>();
    params.put(RETURN_TYPE_PARAM, XML);
    params.put("fl", "id,title");
    params.put("fq", "doc_type:full AND !article_type_facet:\"Issue Image\" AND cross_published_journal_key:" + journal);
    params.put("start", "0");
    params.put("rows", String.valueOf(limit));
    params.put("indent", "off");
    String sortField = (numDays != null) ? solrFieldConverter.getViewCountingFieldName(numDays)
        : solrFieldConverter.getAllTimeViewsField();
    params.put("sort", sortField + " desc");

    Document doc = makeSolrRequest(params);

    List<Pair<String, String>> articleIds = new ArrayList<Pair<String, String>>(limit);

    //get the children of the "result" node
    NodeList docNodes = doc.getChildNodes().item(0)
        .getChildNodes().item(2).getChildNodes();

    for (int i = 0; i < docNodes.getLength(); i++) {
      Node docNode = docNodes.item(i);
      //doc nodes have a child node for each field returned
      if (docNode.getNodeName().equals("doc")) {
        String doi = null;
        String title = null;
        NodeList childNodes = docNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
          if (childNodes.item(j).getAttributes().getNamedItem(NAME_ATTR).getNodeValue().equals(DOI_ATTR)) {
            doi = childNodes.item(j).getChildNodes().item(0).getNodeValue();
          }
          if (childNodes.item(j).getAttributes().getNamedItem(NAME_ATTR).getNodeValue().equals(TITLE_ATTR)) {
            title = childNodes.item(j).getChildNodes().item(0).getNodeValue();
          }
        }
        if (doi != null && title != null) {
          articleIds.add(new Pair<String, String>(doi, title));
        }
      }
    }
    return articleIds;
  }

  public void setSolrUrl(String solrUrl) {
    if (solrUrl.contains("/select")) {
      this.solrUrl = solrUrl;
    } else {
      this.solrUrl = solrUrl.endsWith("/") ? solrUrl + "select" : solrUrl + "/select";
    }
    this.solrUrl = this.solrUrl.replaceAll("\\?", "");
  }

  public void setConfig(Configuration config) {
    this.config = config;
  }

  @Required
  public void setSolrFieldConverter(SolrFieldConversion solrFieldConverter) {
    this.solrFieldConverter = solrFieldConverter;
  }

}
