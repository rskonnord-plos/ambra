/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.itql.TqlInterpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

/** 
 * This sends out invalidation messages to Ehcache cache's based on a set of configured rules.
 * 
 * Configuration properties:
 * <dl>
 *   <dt>topaz.fr.cacheInvalidator.invalidationInterval</dt>
 *   <dd>how often, in milliseconds, to make invalidation calls to the cache. If not specified
 *       this default to 5 seconds.</dd>
 *   <dt>topaz.fr.cacheInvalidator.rulesFile</dt>
 *   <dd>the location where the invalidation rules are stored. If this starts with a '/' it is
 *       treated as a resource; otherwise as a URL.</dd>
 *   <dt>topaz.fr.cacheInvalidator.ehcacheConfig</dt>
 *   <dd>the location of the config for Ehcache. If this starts with a '/' it is treated as a
 *       resource; otherwise as a URL. If null, the default config location "/ehcache.xml" is
 *       used.</dd>
 *   <dt>topaz.fr.cacheInvalidator.queryCache</dt>
 *   <dd>the name of the cache to use for storing rules-query results. If undefined it defaults
 *       to "queryCache". If no cache with the given name has been configured in Ehcache then
 *       no results will be cached. This cache would usually be a memory-only, lfu cache.</dd>
 * </dl>
 *
 * The DTD for the rules is:
 * <pre>
 * &lt;!DOCTYPE rules [
 *   &lt;!ELEMENT rules ((rule | aliasMap)*)&gt;
 *   &lt;!ELEMENT rule     (match, object)&gt;
 *   &lt;!ELEMENT aliasMap (entry)*&gt;
 * 
 *   &lt;!ELEMENT match    (s?, p?, o?, m?)&gt;
 *   &lt;!ELEMENT s        (#PCDATA)&gt;
 *   &lt;!ELEMENT p        (#PCDATA)&gt;
 *   &lt;!ELEMENT o        (#PCDATA)&gt;
 *   &lt;!ELEMENT m        (#PCDATA)&gt;
 * 
 *   &lt;!ELEMENT object   (cache, (key | query))&gt;
 *   &lt;!ELEMENT cache    (#PCDATA)&gt;
 *   &lt;!ELEMENT key      (#PCDATA)&gt;
 *   &lt;!ATTLIST key
 *       field (s | p | o | m) #IMPLIED&gt;
 *   &lt;!ELEMENT query    (#PCDATA)&gt;
 *     &lt;!-- ${x} (where x = 's', 'p', 'o', or 'm') will be replaced with the corresponding
 *        - value from the match.
 *        --&gt;
 * 
 *   &lt;!ELEMENT entry    (alias, value)&gt;
 *   &lt;!ELEMENT alias    (#PCDATA)&gt;
 *   &lt;!ELEMENT value    (#PCDATA)&gt;
 *     &lt;!-- ${dbUri} will be replaced with the current database-uri --&gt;
 * ]&gt;
 * </pre>
 *
 * Each rule consists of match section which determines when the rule is triggered, and an object
 * section which determines which cache entries are invalidated. The match section is one or more
 * elements that an inserted or deleted quad (triple + model) must match; only simple string
 * matches are supported, and all specified elements must match.
 *
 * <p>The object section specifies the name of the cache to which the invalidation should be sent,
 * and the key that should be invalidated. The key can either be one of the elements of the quad
 * that was matched, or it can be an itql query. Queries must return exactly two columns, a key
 * and a value. Each time the a rule is triggered the query is run before and after the
 * modifications are applied to the db and the two lists of (key, value) pairs are compared: all
 * keys which were not present before, are not present anymore, or whose values changed, will be
 * invalidated. To reduce the number of queries run, the query results are cached (see the
 * "queryCache" parameter above). Also, the guarantee is only that the first query will have run
 * some time before the modifications are applied, and the second query will run some time after
 * the modifications are applied, though this time will be short (in the range of seconds, though
 * the latter delay is controlled by the "invalidationInterval" parameter above). This means that
 * if, say, a value changes and then is immediately reset to the original value, or if a statement
 * is inserted and then immediately deleted again, that those changes may or may not trigger a
 * cache invalidation.
 *
 * <p>Example rules:
 * <pre>
 *   &lt;rule&gt;
 *     &lt;match&gt;
 *       &lt;p&gt;topaz:hasRoles&lt;/p&gt;
 *       &lt;m&gt;model:users&lt;/m&gt;
 *     &lt;/match&gt;
 *     &lt;object&gt;
 *       &lt;cache&gt;permit-admin&lt;/cache&gt;
 *       &lt;key field="s"/&gt;
 *     &lt;/object&gt;
 *   &lt;/rule&gt;
 * </pre>
 * With this rule, any time a triple, whose predicate is &lt;topaz:hasRoles&gt;, is inserted into
 * or removed from the model &lt;model:users&gt;, the cache-entry having the triple's subject as
 * the key is removed from the 'permit-admin' cache.
 *
 * <p>The following example shows the use of a query to determine the key to be invalidated:
 * <pre>
 *   &lt;rule&gt;
 *     &lt;match&gt;
 *       &lt;p&gt;topaz:propagate-permissions-to&lt;/p&gt;
 *       &lt;m&gt;model:pp&lt;/m&gt;
 *     &lt;/match&gt;
 *     &lt;object&gt;
 *       &lt;cache&gt;article-state&lt;/cache&gt;
 *       &lt;query&gt;
 *         select $s $state from &lt;model:ri&gt;
 *             where (&lt;${s}&gt; &lt;topaz:articleState&gt; $state)
 *             and (&lt;${s}&gt; &lt;topaz:propagate-permissions-to&gt; $s in &lt;model:pp&gt;);
 *       &lt;/query&gt;
 *     &lt;/object&gt;
 *   &lt;/rule&gt;
 * </pre>
 *
 * @author Ronald Tschalär
 */
class CacheInvalidator extends QueueingFilterHandler<CacheInvalidator.ModItem> {
  private static final Logger logger = Logger.getLogger(CacheInvalidator.class);
  private static final String DEF_QC_NAME = "queryCache";

  private final Rule[]             rules;
  private final Map<String,String> aliases;
  private final Ehcache            queryCache;
  private final XAResource         xaResource;
  private       Session            session;
  private final SessionFactory     sessFactory;
  private final TqlInterpreter     parser = new TqlInterpreter();

  /** 
   * Create a new cache-invalidator instance. 
   * 
   * @param config  the configuration to use
   * @param base    the prefix under which the current <var>config</var> was retrieved
   * @param dbURI   the uri of our database
   * @throws Exception 
   */
  public CacheInvalidator(Configuration config, String base, URI dbURI) throws Exception {
    super(0, getInvIval(config), "CacheInvalidator-Worker", false, logger);
    xaResource = new CIXAResource();

    config = config.subset("cacheInvalidator");
    base  += ".cacheInvalidator";

    // parse the rules file
    String rulesLoc = config.getString("rulesFile", null);
    if (rulesLoc == null)
      throw new IOException("Missing configuration entry '" + base + ".rulesFile");

    URL loc = findResOrURL(rulesLoc);
    if (loc == null)
      throw new IOException("Rules-file '" + rulesLoc + "' not found");

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setIgnoringComments(true);
    builderFactory.setCoalescing(true);

    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Element rules = builder.parse(new InputSource(loc.toString())).getDocumentElement();

    this.aliases = parseAliases(rules, dbURI);
    this.rules   = parseRules(rules, aliases);

    // set up the Ehcache
    String ehcConfigLoc = config.getString("ehcacheConfig", null);
    if (ehcConfigLoc != null) {
      loc = findResOrURL(ehcConfigLoc);
      if (loc == null)
        throw new IOException("Ehcache config file '" + ehcConfigLoc + "' not found");

      CacheManager.create(loc);
    } else
      CacheManager.create();

    String qcName = config.getString("queryCache", DEF_QC_NAME);
    queryCache    = CacheManager.getInstance().getEhcache(qcName);
    if (queryCache != null)
      logger.info("Using cache '" + qcName + "' for the query caching");
    else
      logger.info("No cache named '" + qcName + "' found - disabling query caching");

    // delay session creation because at this point we're already in a session-creation call
    /* This makes a whole bunch of assumptions regarding how LocalSessionFactory works and
     * under what environment we're being used. Basically we assume that A) all
     * LocalSessionFactory instances really use the same underlying SessionFactory, and B) that
     * somebody else has already set this up.
     */
    sessFactory = SessionFactoryFinder.newSessionFactory(dbURI);

    // we're ready
    worker.start();
  }

  private static final long getInvIval(Configuration config) {
    return config.getLong("cacheInvalidator.invalidationInterval", 5000L);
  }

  private static final URL findResOrURL(String loc) throws MalformedURLException {
    return (loc.charAt(0) == '/') ? CacheInvalidator.class.getResource(loc) : new URL(loc);
  }

  private static final Map<String,String> parseAliases(Element rules, URI dbURI) throws Exception {
    NodeList amList = rules.getElementsByTagName("aliasMap");
    Map<String,String> res = new HashMap<String,String>();

    for (int idx = 0; idx < amList.getLength(); idx++) {
      Element am = (Element) amList.item(idx);

      NodeList eList = am.getElementsByTagName("entry");
      for (int idx2 = 0; idx2 < eList.getLength(); idx2++) {
        Element e = (Element) eList.item(idx2);
        Element a = (Element) e.getElementsByTagName("alias").item(0);
        Element v = (Element) e.getElementsByTagName("value").item(0);

        res.put(getText(a), getText(v).replaceAll("\\Q${dbUri}", dbURI.toString()));
      }
    }

    return res;
  }

  private static final Rule[] parseRules(Element rules, Map aliases) throws Exception {
    NodeList r = rules.getElementsByTagName("rule");
    List<Rule> res = new ArrayList<Rule>();
    for (int idx = 0; idx < r.getLength(); idx++)
      res.add(new Rule((Element) r.item(idx), aliases));

    logger.info("Loaded " + res.size() + " rules: " + res);
    return res.toArray(new Rule[res.size()]);
  }

  public XAResource getXAResource() {
    return xaResource;
  }

  public void modelRemoved(URI filterModel, URI realModel) throws ResolverException {
    // FIXME: implement
  }

  public void modelModified(URI filterModel, URI realModel, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException {
    try {
      stmts.beforeFirst();
      while (stmts.next()) {
        try {
          String s = toString(resolverSession, stmts.getSubject());
          String p = toString(resolverSession, stmts.getPredicate());
          String o = toString(resolverSession, stmts.getObject());

          ModItem mi = getModItem(s, p, o, realModel.toString());
          if (mi != null) {
            if (logger.isTraceEnabled())
              logger.trace("Matched '" + s + "' '" + p + "' '" + o + "' '" + realModel +
                           "' to rule " + findMatchingRule(s, p, o, realModel.toString()));

            queue(mi);
          }
        } catch (ResolverException re) {
          logger.error("Error getting statement", re);
        }
      }
    } catch (TuplesException te) {
      throw new ResolverException("Error getting statements", te);
    }
  }

  private static String toString(ResolverSession resolverSession, long node)
      throws ResolverException {
    Node globalNode = null;

    // Globalise the node
    try {
      globalNode = resolverSession.globalize(node);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize node " + node, ge);
    }

    // Turn it into a string
    String str = nodeToString(globalNode);
    if (str == null)
      throw new ResolverException("Unsupported node type " + globalNode.getClass().getName());

    return str;
  }

  private static final String nodeToString(Object node) {
    if (node instanceof URIReference)
      return ((URIReference) node).getURI().toString();

    if (node instanceof Literal) {
      Literal l = (Literal) node;
      return l.getLexicalForm().replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
    }

    if (node instanceof BlankNodeImpl)
      return "$_" + ((BlankNodeImpl) node).getNodeId();

    return null;
  }

  private ModItem getModItem(String s, String p, String o, String m) {
    for (int idx = 0; idx < rules.length; idx++) {
      ModItem mi = rules[idx].match(s, p, o, m);
      if (mi != null)
        return mi;
    }

    return null;
  }

  private Rule findMatchingRule(String s, String p, String o, String m) {
    for (int idx = 0; idx < rules.length; idx++) {
      ModItem mi = rules[idx].match(s, p, o, m);
      if (mi != null)
        return rules[idx];
    }

    return null;
  }

  private static String getText(Element e) {
    /* should be just this, but mulgara is using an old version of xerces
    return e.getTextContent();
    */
    org.w3c.dom.Node text = e.getFirstChild();
    if (text == null)
      return "";
    if (!(text instanceof Text))
      throw new IllegalArgumentException("Expected text, but found node '" +
                                         text.getNodeName() + "'");
    return ((Text) text).getData();
  }

  private static class Rule {
    public final int NONE = -1;
    public final int CNST = 0;
    public final int SUBJ = 1;
    public final int PRED = 2;
    public final int OBJ  = 3;
    public final int MODL = 4;

    private final String s;
    private final String p;
    private final String o;
    private final String m;

    private final String cache;
    private final int    keySelector;
    private final String key;
    private final String query;

    public Rule(String s, String p, String o, String m, int keySelector, String key, String cache,
                String query) {
      this.s           = s;
      this.p           = p;
      this.o           = o;
      this.m           = m;
      this.keySelector = keySelector;
      this.key         = key;
      this.cache       = cache;
      this.query       = query;
    }

    public Rule(Element rule, Map<String,String> aliases) {
      Element match = getChild(rule, "match");
      s = expandAliases(getChildText(match, "s"), aliases);
      p = expandAliases(getChildText(match, "p"), aliases);
      o = expandAliases(getChildText(match, "o"), aliases);
      m = expandAliases(getChildText(match, "m"), aliases);

      Element object = (Element) rule.getElementsByTagName("object").item(0);
      cache = getChildText(object, "cache");
      Element k = getChild(object, "key");
      Element q = getChild(object, "query");

      if (k != null) {
        if (k.hasAttribute("field")) {
          String f = k.getAttribute("field");
          if (f.equals("s"))
            keySelector = SUBJ;
          else if (f.equals("p"))
            keySelector = PRED;
          else if (f.equals("o"))
            keySelector = OBJ;
          else if (f.equals("m"))
            keySelector = MODL;
          else
            throw new IllegalArgumentException("Unknown field type '" + f + "'");
          key = null;
        } else {
          keySelector = CNST;
          key         = getText(k);
        }
        query = null;
      } else {
        keySelector = NONE;
        key         = null;
        query = expandAliases(getText(q), aliases);
      }
    }

    private static Element getChild(Element p, String name) {
      return (Element) p.getElementsByTagName(name).item(0);
    }

    private static String getChildText(Element p, String name) {
      Element c = getChild(p, name);
      return (c != null) ? getText(c) : null;
    }

    private static String expandAliases(String str, Map<String,String> aliases) {
      if (str == null)
        return null;

      for (String alias : aliases.keySet()) {
        String value = aliases.get(alias);
        str = str.replaceAll("\\b" + alias + ":", value);
      }

      return str;
    }

    public ModItem match(String s, String p, String o, String m) {
      if ((this.s == null || this.s.equals(s)) &&
          (this.p == null || this.p.equals(p)) &&
          (this.o == null || this.o.equals(o)) &&
          (this.m == null || this.m.equals(m))) {
        switch (keySelector) {
          case NONE:
            return new ModItem(cache, null, query.replaceAll("\\Q${s}", s).replaceAll("\\Q${p}", p).
                                                replaceAll("\\Q${o}", o).replaceAll("\\Q${m}", m));
          case CNST:
            return new ModItem(cache, key);
          case SUBJ:
            return new ModItem(cache, s);
          case PRED:
            return new ModItem(cache, p);
          case OBJ:
            return new ModItem(cache, o);
          case MODL:
            return new ModItem(cache, m);
        }
      }

      return null;
    }

    public String toString() {
      return "Rule[match=[s=" + strOrStar(s) + ", p=" + strOrStar(p) + ", o=" + strOrStar(o) +
                          ", m=" + strOrStar(m) + "], object=[cache='" + cache + "', key=" +
                          keyToStr() + ", query='" + query + "']]";
    }

    private static String strOrStar(String x) {
      return (x != null) ? "'" + x + "'" : "*";
    }

    private String keyToStr() {
      switch (keySelector) {
        case NONE:
          return "-";
        case CNST:
          return "'" + key + "'";
        case SUBJ:
          return "<subj>";
        case PRED:
          return "<pred>";
        case OBJ:
          return "<obj>";
        case MODL:
          return "<model>";
        default:
          return "-unknown-" + keySelector + "-";
      }
    }
  }

  static class ModItem {
    final String cache;
    final String key;
    final String query;
          Map<String,Set<String>> beforeMod;

    ModItem(String cache, String key) {
      this.cache = cache;
      this.key   = key;
      this.query = null;
    }

    ModItem(String cache, String dummy, String query) {
      this.cache = cache;
      this.key   = null;
      this.query = query;
    }

    public int hashCode() {
      return cache.hashCode() ^ (key != null ? key.hashCode() : query.hashCode());
    }

    public boolean equals(Object o) {
      if (!(o instanceof ModItem))
        return false;

      ModItem mi = (ModItem) o;
      return (mi.cache.equals(cache) &&
              (mi.key != null ? mi.key.equals(key) : mi.query.equals(query)));
    }
  }

  /**
   * This exists to capture query states before the commit in order to be able to do a proper diff.
   */
  private class CIXAResource extends QueueingXAResource {
    public int prepare(Xid xid) throws XAException {
      // get the list of modifications
      List<ModItem> queue;
      synchronized (txQueue) {
        queue = txQueue.get(xid);
      }

      if (queue == null)
        return XA_OK;

      /* for each query-based item, get the query results from before the mods. If the results
       * are not in the cache then run the query now.
       */
      List<ModItem> qryItems = new ArrayList<ModItem>();
      for (ModItem mi : queue) {
        if (mi.query == null)
          continue;

        net.sf.ehcache.Element prevElem =
            (queryCache != null) ? queryCache.get(new QCacheKey(mi)) : null;
        if (prevElem != null)
          mi.beforeMod = (Map<String,Set<String>>) prevElem.getObjectValue();
        else
          qryItems.add(mi);
      }

      // run all queries and remember and cache their results
      if (!qryItems.isEmpty()) {
        QueryRunner qr = getQueryRunner();
        try {
          qr.runQueries(qryItems);
        } catch (InterruptedException ie) {
          throw (XAException) new XAException(XAException.XAER_RMERR).initCause(ie);
        } finally {
          returnQueryRunner(qr);
        }

        if (queryCache != null) {
          for (ModItem mi : qryItems)
            queryCache.put(new net.sf.ehcache.Element(new QCacheKey(mi), mi.beforeMod));
        }
      }

      return XA_OK;
    }
  }

  // this will be no larger than the maximum number of concurrent writers, which currently is 1
  private final List<QueryRunner> qrPool = new ArrayList<QueryRunner>();

  private QueryRunner getQueryRunner() {
    synchronized (qrPool) {
      return qrPool.isEmpty() ? newQueryRunner() : qrPool.remove(qrPool.size() - 1);
    }
  }

  private void returnQueryRunner(QueryRunner qr) {
    synchronized (qrPool) {
      qrPool.add(qr);
    }
  }

  private QueryRunner newQueryRunner() {
    QueryRunner qr = new QueryRunner("QueryRunner", newSession());
    qr.start();
    return qr;
  }

  private synchronized Session newSession() {
    try {
      return sessFactory.newSession();
    } catch (Exception e) {
      logger.error("Error creating session", e);
      return null;
    }
  }


  /* =====================================================================
   * ==== Everything below is run in the context of the Worker thread ====
   * =====================================================================
   */

  protected void handleQueuedItem(ModItem mi) throws IOException {
    // get the Ehcache instance
    Ehcache cache = CacheManager.getInstance().getEhcache(mi.cache);
    if (cache == null) {
      logger.warn("No cache configuration found for '" + mi.cache + "'");
      return;
    }

    // figure out the keys to invalidate
    Set<String> keys = (mi.key != null) ? Collections.singleton(mi.key) : getKeysFromQuery(mi);

    // invalidate the keys
    for (String key : keys) {
      if (logger.isDebugEnabled())
        logger.debug("Invalidating key '" + key + "' in cache '" + mi.cache + "'");

      try {
        cache.remove(key);
      } catch (IllegalStateException ise) {
        logger.warn("Failed to remove key '" + key + "' from cache '" + mi.cache + "'", ise);
      }
    }
  }

  private Set<String> getKeysFromQuery(ModItem mi) {
    Set<String> keys = new HashSet<String>();

    // get our session
    if (session == null) {
      session = newSession();
      if (session == null)
        return keys;
      logger.info("Created session");
    }

    try {
      // do the query
      Map<String,Set<String>> res = getQueryResults(mi.query, session);

      // get previous results
      Map<String,Set<String>> prevRes =
          (mi.beforeMod != null) ? mi.beforeMod : Collections.EMPTY_MAP;

      if (logger.isTraceEnabled())
        logger.trace("Previous query results: " + prevRes);

      // invalidate new or changed values
      for (String key : res.keySet()) {
        Set<String> vals  = res.get(key);
        Set<String> ovals = prevRes.get(key);

        if (ovals == null || !ovals.equals(vals))
          keys.add(key);
      }

      if (logger.isTraceEnabled())
        logger.trace("New or updated keys: " + keys);

      // invalidate deleted values
      Set<String> remKeys = new HashSet<String>(prevRes.keySet());
      remKeys.removeAll(res.keySet());

      keys.addAll(remKeys);

      if (logger.isTraceEnabled())
        logger.trace("Removed keys: " + remKeys);

      // update cache if anything changed
      if (!keys.isEmpty() && queryCache != null)
        queryCache.put(new net.sf.ehcache.Element(new QCacheKey(mi), res));

    } catch (Exception e) {
      logger.error("Error executing query '" + mi.query + "'", e);
    }

    return keys;
  }

  private Map<String,Set<String>> getQueryResults(String query, Session sess) throws Exception {
    if (logger.isTraceEnabled())
      logger.trace("Running query '" + query + "'");

    // run the query and check for errors
    Answer answer = sess.query(parser.parseQuery(query));

    // gather up the results, grouping them by key
    Map<String,Set<String>> res = new HashMap<String,Set<String>>();

    try {
      answer.beforeFirst();
      while (answer.next()) {
        String key = nodeToString(answer.getObject(0));
        String val = nodeToString(answer.getObject(1));

        Set<String> vals = res.get(key);
        if (vals == null)
          res.put(key, vals = new HashSet<String>());
        vals.add(val);
      }
    } finally {
      try {
        answer.close();
      } catch (TuplesException te) {
        logger.warn("Error closing answer", te);
      }
    }

    if (logger.isTraceEnabled())
      logger.trace("Query results: " + res);

    return res;
  }

  private static class QCacheKey {
    final String cache;
    final String query;

    QCacheKey(ModItem mi) {
      this.cache = mi.cache;
      this.query = mi.query;
    }

    public int hashCode() {
      return cache.hashCode() ^ query.hashCode();
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof QCacheKey))
        return false;
      QCacheKey o = (QCacheKey) obj;
      return (o.cache.equals(cache) && o.query.equals(query));
    }

    public String toString() {
      return "QCacheKey[cache='" + cache + "', query='" + query + "']";
    }
  }

  protected void idleCallback() {
  }

  protected void shutdownCallback() {
    CacheManager.getInstance().shutdown();
    logger.info("shut down cache-manager");
  }

  /**
   * This is used to run queries from another thread.
   */
  private class QueryRunner extends Thread {
    private final Session       session;
    private       List<ModItem> qryItems;

    /**
     * Create a query-runne.
     *
     * @param name    the thread-name for this thread
     * @param session the session to use for the queries
     */
    public QueryRunner(String name, Session session) {
      super(name);
      setDaemon(true);
      this.session = session;
    }

    /**
     * Run the queries and stores the results in the mod-item again. Notifies the thread and waits
     * for it to complete the queries.
     *
     * @param qryItems the mod-items with the queries to run
     * @throws InterruptedException if waiting for the results is interrupted
     */
    public synchronized void runQueries(List<ModItem> qryItems) throws InterruptedException {
      assert this.qryItems == null;

      this.qryItems = qryItems;
      notify();

      while (this.qryItems != null)
        wait();
    }

    @Override
    public synchronized void run() {
      while (true) {
        // wait for a job
        try {
          while (qryItems == null)
            wait();
        } catch (InterruptedException ie) {
          logger.warn("QueryRunner thread interrupted - exiting", ie);
          break;
        }

        // run the queries
        try {
          for (ModItem mi : qryItems) {
            try {
              mi.beforeMod = CacheInvalidator.this.getQueryResults(mi.query, session);
            } catch (Throwable e) {
              logger.warn("Failed to run query '" + mi.query +
                          "' - not all invalidations may get sent", e);
            }
          }
        } finally {
          qryItems = null;
          notify();
        }
      }
    }
  }
}
