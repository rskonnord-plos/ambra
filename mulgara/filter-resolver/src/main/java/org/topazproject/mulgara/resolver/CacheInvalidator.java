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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;
import org.mulgara.itql.ItqlInterpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

/** 
 * This invalidates the topaz caches on mulgara updates.
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
 * </dl>
 *
 * @author Ronald Tschalär
 */
class CacheInvalidator extends QueueingFilterHandler {
  private static final Logger logger = Logger.getLogger(CacheInvalidator.class);
  private static final String DEF_QC_NAME = "queryCache";

  private final Rule[]             rules;
  private final Map<String,String> aliases;
  private final Ehcache            queryCache;
  private final URI                dbURI;
  private       ItqlInterpreter    interpreter;

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

    // delay session creation because at this point we're already in a session-creation call
    this.dbURI = dbURI;

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

      NodeList eList = rules.getElementsByTagName("entry");
      for (int idx2 = 0; idx2 < eList.getLength(); idx2++) {
        Element e = (Element) eList.item(idx2);
        Element a = (Element) e.getElementsByTagName("alias").item(0);
        Element v = (Element) e.getElementsByTagName("value").item(0);

        res.put(a.getTextContent(), v.getTextContent().replaceAll("\\Q${dbUri}", dbURI.toString()));
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

    private static String getText(Element e) {
      return e.getTextContent();
    }

    private static String getChildText(Element p, String name) {
      Element c = getChild(p, name);
      return (c != null) ? getText(c) : null;
    }

    private static String expandAliases(String str, Map<String,String> aliases) {
      if (str == null)
        return null;

      for (Iterator<String> iter = aliases.keySet().iterator(); iter.hasNext(); ) {
        String alias = iter.next();
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

  private static class ModItem {
    final String cache;
    final String key;
    final String query;

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


  /* =====================================================================
   * ==== Everything below is run in the context of the Worker thread ====
   * =====================================================================
   */

  protected void handleQueuedItem(Object obj) throws IOException {
    ModItem mi = (ModItem) obj;

    // get the Ehcache instance
    Ehcache cache = CacheManager.getInstance().getEhcache(mi.cache);
    if (cache == null) {
      logger.warn("No cache configuration found for '" + mi.cache + "'");
      return;
    }

    // figure out the keys to invalidate
    Set<String> keys = new HashSet<String>();
    if (mi.key != null)
      keys.add(mi.key);
    else
      runQuery(mi, keys);

    // invalidate the keys
    for (Iterator<String> iter = keys.iterator(); iter.hasNext(); ) {
      String key = iter.next();
      if (logger.isDebugEnabled())
        logger.debug("Invalidating key '" + key + "' in cache '" + mi.cache + "'");

      try {
        cache.remove(key);
      } catch (IllegalStateException ise) {
        logger.warn("Failed to remove key '" + key + "' from cache '" + mi.cache + "'", ise);
      }
    }
  }

  private void runQuery(ModItem mi, Set<String> keys) {
    if (interpreter == null) {
      /* Delayed session and interpreter creation.
       *
       * This makes a whole bunch of assumptions regarding how LocalSessionFactory works and
       * under what environment we're being used. Basically we assume that A) all
       * LocalSessionFactory instances really use the same underlying SessionFactory, and B) that
       * somebody else has already set this up.
       */
      try {
        SessionFactory sf = SessionFactoryFinder.newSessionFactory(dbURI);
        interpreter = new ItqlInterpreter(sf.newSession(), Collections.EMPTY_MAP);
        logger.info("Created session and itql-interpreter");
      } catch (Exception e) {
        logger.error("Error creating session and itql-interpreter", e);
        return;
      }
    }

    if (logger.isTraceEnabled())
      logger.trace("Running query '" + mi.query + "'");

    try {
      // run the query and check for errors
      interpreter.executeCommand(mi.query);
      Answer answer = interpreter.getLastAnswer();
      if (answer == null) {
        if (interpreter.getLastError() != null) {
          logger.error("Error executing query '" + mi.query + "'", interpreter.getLastError());
          return;
        }
        if (interpreter.getLastMessage() != null) {
          logger.error("Unexpected message instead of result from query '" + mi.query + "': '" +
              interpreter.getLastMessage() + "'");
          return;
        }
      }

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

      // update the cache.
      QCacheKey              qcKey    = new QCacheKey(mi.cache, mi.query);
      net.sf.ehcache.Element prevElem = queryCache.get(qcKey);

      Map<String,Set<String>> prevRes = (Map<String,Set<String>>)
          (prevElem != null ? prevElem.getObjectValue() : Collections.EMPTY_MAP);

      if (logger.isTraceEnabled())
        logger.trace("Previous query results: " + prevRes);

      // invalidate new or changed values
      for (Iterator<String> iter = res.keySet().iterator(); iter.hasNext(); ) {
        String      key  = iter.next();

        Set<String> vals  = res.get(key);
        Set<String> ovals = prevRes.remove(key);

        if (ovals == null || !ovals.equals(vals))
          keys.add(key);
      }

      if (logger.isTraceEnabled())
        logger.trace("New or updated keys: " + keys);

      // invalidate deleted values
      keys.addAll(prevRes.keySet());

      if (logger.isTraceEnabled())
        logger.trace("Removed keys: " + prevRes.keySet());

      // update cache if anything changed
      if (keys.size() > 0)
        queryCache.put(new net.sf.ehcache.Element(qcKey, res));

    } catch (Exception e) {
      logger.error("Error executing query '" + mi.query + "'", e);
    }
  }

  private static class QCacheKey {
    final String cache;
    final String query;

    QCacheKey(String cache, String query) {
      this.cache = cache;
      this.query = query;
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
  }

  protected void idleCallback() {
  }

  protected void shutdownCallback() {
    CacheManager.getInstance().shutdown();
    logger.info("shut down cache-manager");
  }
}
