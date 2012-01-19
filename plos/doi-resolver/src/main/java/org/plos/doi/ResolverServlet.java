/**
 *
 */
package org.plos.doi;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * The ResolverServlet attempts to translate the DOI in the incoming request into a URI on the plosone server
 * that will display the DOI correctly. Thus far, it handles Article and Annotation DOIs. It makes a call to
 * the semantic DB to determine the type(s) of the given DOI. Depending on the type of the resource described
 * by the DOI, the servlet matches the doi to regular expressions for each known journal (defined in 
 * /etc/topaz/plosone.xml or the hierarchy of config files). Each regular expression maps to a URI on the plosone
 * server that will display the resource described by the given DOI. 
 * 
 * In the case of annotation DOIs, they do not contain a reference to the journal, so the annotated root article 
 * must be found first in order to calculate the correct journal base URL. 
 * 
 * TODO: This should be re-implemented to be more generic so it can be more easily configured to support more types
 * of DOIs. Assumptions should not be made about the DOI format 
 * containing a reference to the journal. The known journals should be better defined. Better still, the DOI Resolver
 * should simply check for the existence of the DOI in the plosone server and if so, forward to the plosone server. 
 * In turn, plosone server should know how to display the appropriate result for any given DOI. 
 * 
 * @author Stephen Cheng, Alex Worden
 */
public class ResolverServlet extends HttpServlet{
  private static final Log log = LogFactory.getLog(ResolverServlet.class);
  private static final Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();
  /*private static final Pattern journalRegEx = Pattern.compile("/10\\.1371/journal\\.pone\\.\\d{7}");
  private static final Pattern figureRegEx = Pattern.compile("/10\\.1371/journal\\.pone\\.\\d{7}\\.[gt]\\d{3}");*/
  private static final String RDF_TYPE_ARTICLE = "http://rdf.topazproject.org/RDF/Article";
  private static final String RDF_TYPE_ANNOTATION = "http://www.w3.org/2000/10/annotation-ns#Annotation";
  private static final Pattern[] journalRegExs;
  private static final Pattern[] figureRegExs;
  private static final String[] urls;
  private static final int numJournals;
  private static final String errorPage;
  private static final String INFO_DOI_PREFIX = "info:doi";

  static {
    numJournals = myConfig.getList("pub.doi-journals.journal.url").size();
    urls = new String[numJournals];
    figureRegExs = new Pattern[numJournals];
    journalRegExs = new Pattern[numJournals];
    for (int i = 0; i < numJournals; i++) {
      urls[i] = myConfig.getString("pub.doi-journals.journal(" + i + ").url");
      StringBuilder pat = new StringBuilder("/").append(myConfig.getString("pub.doi-journals.journal(" + i + ").regex"));
      journalRegExs[i] = Pattern.compile(pat.toString());
      figureRegExs[i] = Pattern.compile(pat.append("\\.[gt]\\d{3}").toString());
    }
    errorPage = myConfig.getString("pub.webserver-url")+ myConfig.getString("pub.error-page");
    if (log.isTraceEnabled()) {
      for (int i = 0; i < numJournals; i++) {
        log.trace("JournalRegEx: " + journalRegExs[i].toString() +
                  "  ; figureRegEx: " + figureRegExs[i].toString() +
                  "  ; url: " + urls[i]);
      }
      log.trace( ("Error Page is: " + errorPage));
    }
  }


  /**
   * Tries to resolve a PLoS ONE doi from CrossRef into an application specific URL
   * First, tries to make sure the DOI looks like it is properly formed.  If it looks
   * like an article DOI, will attempt to do a type lookup in mulgara.  If it looks
   * like a figure or a table, will attempt to construct an article DOI and do that
   * lookup. Otherwise, will fail and send to PLoS ONE Page not Found error page.
   *
   * @param req the servlet request
   * @param resp the servlet response
   *
   */
  public void doGet(HttpServletRequest req, HttpServletResponse resp)  {
    String doi = req.getPathInfo();
    if (log.isTraceEnabled()) {
      log.trace ("Incoming doi = " + doi);
    }
    if (doi == null) {
      failWithError(resp);
      return;
    }
    try {
      doi = URLDecoder.decode(doi.trim(),"UTF-8");
    } catch (UnsupportedEncodingException uee) {
      doi = doi.trim();
    }
    try {
      String redirectURL = constructURL (doi);
      if (log.isDebugEnabled()) {
        log.debug("DOI ResolverServlet sending redirect to URI: "+redirectURL);
      }
      resp.sendRedirect(redirectURL);
    } catch (Exception e){
      log.warn("Could not resolve doi: " + doi, e);
      failWithError(resp);
    }
    return;
  }


  /**
   * Just forwards to the PLoS ONE Page Not Found error page
   *
   * @param req the servlet request
   * @param resp the servlet response
   */
  public void doPost (HttpServletRequest req, HttpServletResponse resp)  {
    failWithError(resp);
    return;
  }

  private Set<String> lookupDOI (String doi) {
    URI doiURI = null;
    try {
      doiURI = new URI (INFO_DOI_PREFIX + doi);
    } catch (URISyntaxException use) {
      log.warn ("Couldn't create URI for doi:" + doi, use);
      return null;
    }
    try {
      DOITypeResolver resolver = new DOITypeResolver(new URI(
                                myConfig.getString("topaz.services.itql.uri")));
      return resolver.getRdfTypes(doiURI);
    } catch (Exception e) {
      log.warn ("Couldn't retrieve rdf types for " + doiURI.toString(), e);
      return null;
    }
  }


  private String constructURL (String doi) {
    StringBuilder redirectURL; //= new StringBuilder(myConfig.getString("pub.webserver-url"));
    Set<String> rdfTypes;

    Pattern journalRegEx, figureRegEx;

    for (int i = 0; i < numJournals; i++) {
      journalRegEx = journalRegExs[i];
      figureRegEx = figureRegExs[i];

      if (journalRegEx.matcher(doi).matches()) {
        rdfTypes = lookupDOI(doi);
        if (rdfTypes != null) {
          if (rdfTypes.contains(RDF_TYPE_ARTICLE)) {
            redirectURL = new StringBuilder(urls[i]);
            try {
              redirectURL.append(myConfig.getString("pub.article-action"))
                         .append(URLEncoder.encode(INFO_DOI_PREFIX, "UTF-8")).append(URLEncoder.encode(doi, "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
              if (log.isDebugEnabled()) {
                log.debug("Couldn't encode URL with UTF-8 encoding", uee); 
              }
              redirectURL.append(myConfig.getString("pub.article-action"))
                         .append(URLEncoder.encode(INFO_DOI_PREFIX)).append(URLEncoder.encode(doi));
            }
            if (log.isDebugEnabled()) {
              log.debug ("Matched: " + doi + "; redirecting to: " + redirectURL.toString());
            }
            return redirectURL.toString();
          }
        }
      }
      
      if (figureRegEx.matcher(doi).matches()) {
        String possibleArticleDOI = doi.substring(0, doi.length()-5);
        rdfTypes = lookupDOI(possibleArticleDOI);
        if (rdfTypes != null) {
          if (rdfTypes.contains(RDF_TYPE_ARTICLE)) {
            redirectURL = new StringBuilder(urls[i]);
            redirectURL.append(myConfig.getString("pub.figure-action1"))
                       .append(INFO_DOI_PREFIX).append(possibleArticleDOI)
                       .append(myConfig.getString("pub.figure-action2"))
                       .append(INFO_DOI_PREFIX).append(doi);
            if (log.isDebugEnabled()) {
              log.debug ("Matched: " + doi + "; redirecting to: " + redirectURL.toString());
            }
            return redirectURL.toString();
          }
        }
      }
    }

    rdfTypes = lookupDOI(doi);
    String doiUriStr = INFO_DOI_PREFIX + doi;
    if (rdfTypes.contains(RDF_TYPE_ANNOTATION)) {
      try {
        DOITypeResolver resolver = new DOITypeResolver(new URI(
          myConfig.getString("topaz.services.itql.uri")));
        String annotatedRoot = resolver.getAnnotatedRoot(doiUriStr);
        String rootUri = matchDoiToJournal(annotatedRoot);
        if (rootUri != null) {
          StringBuilder urlBuf = new StringBuilder(rootUri);
          urlBuf.append(myConfig.getString("pub.annotation-action")).append(URLEncoder.encode(doiUriStr, "UTF-8"));
          return urlBuf.toString();
        }
      } catch (Exception e) {
        log.error("Exception occurred attempting to resolve doi '"+doiUriStr+"' to an annotation", e);
        return errorPage;
      }
    }
    
    if (log.isDebugEnabled()) {
      log.debug ("Could not match: " + doiUriStr + "; redirecting to: " + errorPage);
    }
    return errorPage;
  }

  private String matchDoiToJournal(String doi) {
    if (doi.startsWith(INFO_DOI_PREFIX)) {
      doi = doi.substring(INFO_DOI_PREFIX.length());
    }
    for (int i=0; i < journalRegExs.length ; i++) {
      if (journalRegExs[i].matcher(doi).matches()) {
        return urls[i];
      }
    }
    return null;
  }
  
  private void failWithError(HttpServletResponse resp){
    try {
      resp.sendRedirect(errorPage);
    } catch (Exception e) {
      log.warn ("Couldn't redirect user to error page", e);
    }
  }
}
