/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.security.Guard;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.article.service.FetchArticleService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.configuration.ConfigurationStore;
import org.plos.models.Article;
import org.plos.search.SearchResultPage;
import org.plos.search.SearchUtil;
import org.plos.xacml.AbstractSimplePEP;
import org.plos.xacml.XacmlUtil;

/**
 * Store the progress of a search. That is, when a search is done, we get the first N results
 * and don't get more until more are requested.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class Results {
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Log           log  = LogFactory.getLog(Results.class);
  private SearchPEP                  pep;
  private SearchWebService           service;
  private FetchArticleService        fetchArticleService;
  private CachingIterator            cache;
  private String                     query;
  private int                        totalHits = 0;

  /**
   * Construct a search Results object.
   *
   * @param query               the lucene query to build the search results for.
   * @param service             the fedoragsearch service
   * @param fetchArticleService the FetchArticleService.
   */
  public Results(String query, SearchWebService service, FetchArticleService fetchArticleService) {
    this.service  = service;
    this.fetchArticleService = fetchArticleService;
    this.query    = query;
    this.cache    = new CachingIterator(new GuardedIterator(new HitIterator(), new HitGuard()));
    try {
      this.pep    = new SearchPEP();
    } catch (Exception e) {
      throw new Error("Failed to create SearchPEP", e);
    }
  }

  /**
   * Return a search results page.
   *
   * @param startPage the page number to return (starts with 0)
   * @param pageSize  the number of entries to return
   * @return The results for one page of a search.
   * @throws UndeclaredThrowableException if there was a problem retrieving the search results.
   *         It likely wraps a RemoteException (talking to the search webapp) or an IOException
   *         parsing the results.
   */
  public SearchResultPage getPage(int startPage, int pageSize) {

    ArrayList<SearchHit> hits = new ArrayList<SearchHit>(pageSize);
    int                  cnt  = 0; // Actual number of hits retrieved

    // Jump to the record we want
    cache.gotoRecord(startPage * pageSize);

    // Copy records out of our cache into our hits
    while (cache.hasNext() && cnt < pageSize) {
      hits.add((SearchHit) cache.next());
      cnt++;
    }

    // If we know we're at the end, set our total size
    if (!cache.hasNext())
      totalHits = cache.getCurrentSize();

    return new SearchResultPage(totalHits, pageSize, hits);
  }

  /**
   * @return The total number of records lucene thinks we have. This may be inaccurate if
   *         XACML filters any out.
   */
  public int getTotalHits() {

    cache.hasNext(); // Read at least one record to populate totalHits instance variable
    return totalHits;
  }

  /**
   * Class that uses fedoragsearch to back this custom iterator.<p>
   *
   * Use this in a chain of iterators.
   */
  private class HitIterator implements Iterator {
    private ArrayList<SearchHit> items    = new ArrayList<SearchHit>();
    private Iterator             iter     = items.iterator();
    private int                  position = 0;

    public boolean hasNext() {
      if (!iter.hasNext()) {
        try {
          String xml = service.find(query, position,
                                    CONF.getInt("pub.search.fetchSize", 10),
                                    CONF.getInt("pub.search.snippetsMax", 3),
                                    CONF.getInt("pub.search.fieldMaxLength", 50),
                                    CONF.getString("pub.search.index", "TopazIndex"),
                                    CONF.getString("pub.search.resultPage", "copyXml"));

          if (log.isDebugEnabled())
            log.debug("HitIterator: Got results: " + xml);

          // Not sure if using a SearchResultPage is the right way here... (but it works)
          SearchResultPage results = SearchUtil.convertSearchResultXml(xml);
          items.addAll(results.getHits());
          if (totalHits == 0) // Just play safe, not sure what happens at EOF
            totalHits = results.getTotalNoOfResults();

          iter = items.listIterator(position);
          position += results.getHits().size();
        } catch (Exception e) {
          // It is possible we could throw a RemoteException or IOException
          throw new UndeclaredThrowableException(e, "Error talking to search service");
        }
      }

      return iter.hasNext();
    }

    public Object next() {
      if (hasNext())
        return iter.next();
      else
        throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * The XACML Policy-Enforcement-Point for search. We have only one operation:
   * articles:readMetaData.
   */
  private static class SearchPEP extends AbstractSimplePEP {
    public    static final String     READ_METADATA         = "articles:readMetaData";
    protected static final String[]   SUPPORTED_ACTIONS     = new String[] { READ_METADATA };
    protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] { null };

    static {
      init(SearchPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public SearchPEP() throws IOException, ParsingException, UnknownIdentifierException {
      this(XacmlUtil.lookupPDP("topaz.search.pdpName"), XacmlUtil.createSubjAttrs());
    }

    protected SearchPEP(PDP pdp, Set subjAttrs)
        throws IOException, ParsingException, UnknownIdentifierException {
      super(pdp, subjAttrs);
    }
  }

  /**
   * A guard that uses XACML and OTM to ensure an article's meta data should be visible to
   * the current user in the current journal (OTM filter).
   *
   * @see GuardedIterator
   */
  private class HitGuard implements Guard {
    public void checkGuard(Object object) throws SecurityException {
      SearchHit    hit = (SearchHit) object;
      final String uri = hit.getPid();

      // Verify xacml allows (initially used for <topaz:articleState> ... but may be more)
      pep.checkAccess(SearchPEP.READ_METADATA, URI.create(uri));

      // verify that Aricle exists, is accessible by user, is in Journal, etc., be cache aware
      try {
        Article article = fetchArticleService.getArticleInfo(uri);
        if (article == null) {
          throw new SecurityException(new NoSuchArticleIdException(uri));
        }
      } catch (ApplicationException ae) {
        if (ae.getCause() instanceof NoSuchArticleIdException) {
          throw new SecurityException(ae.getCause());
        } else {
          throw new SecurityException(ae);
        }
      }
    }
  }
}
