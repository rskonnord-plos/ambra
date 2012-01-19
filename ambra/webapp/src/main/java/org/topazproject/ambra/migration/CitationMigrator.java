/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.migration;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.cache.OtmInterceptor;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.Ambra;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;
import org.topazproject.xml.transform.cache.CachedSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.xml.DomReader;

/**
 * Migrate ambra 0.9 Citation data to ambra 0.9.1. Ingest in 0.9.1
 * now picks up more Citation properties. So it is pretty much a
 * re-ingest - but not quite. So most of the things here are copied
 * from Ingester.
 *
 * @author Pradeep Krishnan
 */
public class CitationMigrator implements Runnable {
  private static final Log log = LogFactory.getLog(CitationMigrator.class);
  private final TransformerFactory tFactory;
  private SessionFactory sf;
  private OtmInterceptor oi;
  private int txnTimeout = 600;
  private int blobThrottle = 20;
  private boolean background = true;
  private Set<String> errorSet = new HashSet<String>();

  /**
   * Create a CitationMigrator object.
   */
  public CitationMigrator() {
    tFactory = new TransformerFactoryImpl();
    tFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
    tFactory.setAttribute("http://saxon.sf.net/feature/strip-whitespace", "none");
    tFactory.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing a stylesheet", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing a stylesheet", te);
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing a stylesheet", te);
      }
    });

  }

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  @Required
  public void setOtmInterceptor(OtmInterceptor oi) {
    this.oi = oi;
  }

  /**
   * The number of blobs to index/re-index per txn.
   *
   * @param blobThrottle the throttling to apply
   */
  public void setBlobThrottle(int blobThrottle) {
    this.blobThrottle = blobThrottle;
  }

  /**
   * The txn time out value to set. Defaults to 10min.
   *
   * @param txnTimeout the timeout value to set
   */
  public void setTxnTimeout(int txnTimeout) {
    this.txnTimeout = txnTimeout;
  }
  /**
   * Whether to run this migration in back-ground allowing web-traffic to proceed.
   *
   * @param val the background flag value
   */
  public void setBackground(boolean val) {
    background = val;
  }

  public void init() {
    if (!background)
      migrate();
    else {
      Thread t = new Thread(this, "Citation-Migrator");
      t.setPriority(Thread.NORM_PRIORITY - 1);
      t.setDaemon(true);
      t.start();
    }
  }

  public void run() {
    try {
      migrate();
    } catch (RuntimeException e) {
      log.fatal("Uncaught exception. Exiting ...", e);
    } catch (Error e) {
      log.fatal("Uncaught error. Exiting ...", e);
    }
  }

  /**
   * Run thru all the migrations.
   *
   * @return the count of articles successfully migrated
   */
  public long migrate() {
    log.info("Citation Migrations starting ...");

    Session session = sf.openSession(oi);

    try {
      long total = 0;

      int count;
      do {
        int err = errorSet.size();
        count = migrate(session, blobThrottle + err, txnTimeout);
        total += (count - (errorSet.size() - err));
      } while (count > 0);

      if (errorSet.size() > 0)
        log.fatal("Failed to migrate " + errorSet.size() + " articles. Succeeded for "
                  + total + " articles.");
      else if (total == 0)
        log.info("Nothing to do. Citations are all migrated.");
      else
        log.warn("Successfully migrated " + total + " articles.");

      return total;
    } finally {
      try {
        session.close();
      } catch (Exception e) {
        log.warn("Failed to close session", e);
      }
    }
  }

  private int migrate(Session session, final int throttle, int timeout) {
    return TransactionHelper.doInTx(session, false, timeout, new TransactionHelper.Action<Integer>() {
      public Integer run(Transaction tx) {
        return migrate(tx.getSession(), throttle);
      }
    });
  }

  private int migrate(Session session, int throttle) {
    String ri = Ambra.GRAPH_PREFIX + "ri";
    String at = Rdf.topaz + "Article";
    String bbc = Rdf.dc_terms + "bibliographicCitation";
    String cid = Rdf.dc + "identifier";
    Results r = session.doNativeQuery("select $s from <" + ri + "> where $s <rdf:type> <" + at
                                     + "> minus ($s <" + bbc + "> $c and $c <" + cid + "> $d) "
                                     + "limit " + throttle + ";");
    int count = 0;
    while (r.next()) {
      String id = r.getString(0);
      if (errorSet.contains(id))
        continue;
      try {
        log.info("Migrating " + id);
        migrate(id, session);
        count++;
      } catch (Exception e) {
        log.error("Failed to migrate <" + id + ">.", e);
        errorSet.add(id);
        count++;
      } finally {
        session.clear();
      }
    }

    return count;
  }

  private void migrate(String id, Session session) {
    if (log.isDebugEnabled())
      log.debug("Loading article <" + id + "> ...");
    Article a = session.get(Article.class, id);
    Map<String, Citation> citMap = buildCitMap(a.getDublinCore().getReferences());

    if (log.isDebugEnabled())
      log.debug("Re-building Article object-info from xml for <" + id + "> ...");
    Document doc = getObjectInfo(a);

    if (log.isTraceEnabled())
      log.trace("Extracted " + dom2String(doc));

    if (log.isDebugEnabled())
      log.debug("Unmarshalling Article object-info for <" + id + "> ...");
    Article newArt = unmarshal(doc);

    if (log.isDebugEnabled())
      log.debug("Validating Article object for <" + id + "> ...");
    validate(newArt);

    Map<String, Citation> ncitMap = buildCitMap(newArt.getDublinCore().getReferences());

    if (!ncitMap.keySet().equals(citMap.keySet())) {
      Set<String> nk = new HashSet<String>(ncitMap.keySet());
      Set<String> ok = new HashSet<String>(citMap.keySet());
      nk.removeAll(citMap.keySet());
      ok.removeAll(ncitMap.keySet());

      StringBuilder msg = new StringBuilder("Mismatch in References for <").append(id).append(">.");
      if (!nk.isEmpty()) {
        msg.append("Found the following differing citations: ").append(nk);
      }

      if (!ok.isEmpty()) {
        msg.append("Did not find the following existing citations: ").append(ok);
      }

      throw new OtmException(msg.toString());
    }

    if (log.isDebugEnabled())
      log.debug("Updating Citations for <" + id + "> ...");
    updateCitation("bibliographic-citation", a.getDublinCore().getBibliographicCitation(),
                   newArt.getDublinCore().getBibliographicCitation());

    for (String key : ncitMap.keySet())
      updateCitation(key, citMap.get(key), ncitMap.get(key));

    if (log.isDebugEnabled())
      log.debug("Writing Citations for <" + id + "> ...");
    session.flush();
  }

  private Map<String, Citation> buildCitMap(List<Citation> cits) {
    Map<String, Citation> citMap = new HashMap<String, Citation>();
    if (cits != null) {
      for (Citation cit : cits) {
        String key = generateKey(cit);
        if (citMap.containsKey(key))
          throw new OtmException("Duplicate Citations found for: " + key);
        citMap.put(key, cit);
      }
    }
    return citMap;
  }

  private String generateKey(Citation cit) {
    StringBuilder key = new StringBuilder();

    if ((cit.getKey() != null) && (cit.getKey().length() > 0)) {
      key.append("key=").append(cit.getKey());
    } else {
      key.append(",type=").append(cit.getCitationType());
      key.append(",title=").append(cit.getTitle());
      key.append(",year=").append(cit.getYear());
      key.append(",month=").append(cit.getMonth());
      key.append(",volume=").append(cit.getVolume());
      key.append(",issue=").append(cit.getIssue());
      key.append(",publisher=").append(cit.getPublisherName());
      key.append(",pages=").append(cit.getPages());
      key.append(",url=").append(cit.getUrl());
      key.append(",summary=").append(cit.getSummary());
      key.append(",note=").append(cit.getNote());
    }
    return key.toString();
  }

  private void updateCitation(String key, Citation old, Citation nu) {
    // Apply the changes from r6692, r6774 and r6867
    old.setELocationId(nu.getELocationId());
    old.setDay(nu.getDay());
    old.setCollaborativeAuthors(nu.getCollaborativeAuthors());
    old.setDoi(nu.getDoi());
    old.setJournal(nu.getJournal());

    updateUsers(key, old.getAuthors(), nu.getAuthors());
    updateUsers(key, old.getEditors(), nu.getEditors());
  }

  private void updateUsers(String key, List<UserProfile> old, List<UserProfile> nu) {
    if (old == null)
      old = new ArrayList<UserProfile>();

    if (nu == null)
      nu = new ArrayList<UserProfile>();

    if (old.size() < nu.size())
      throw new OtmException("Mismatch in Citation " + key + ". Expecting "
                             + old.size() + " users, got " + nu.size() + " instead");

    // ordered list. (rdf:Seq)
    for (int i = 0; i < nu.size(); i++)
      updateUser(key, old.get(i), nu.get(i));
  }

  private void updateUser(String key, UserProfile old, UserProfile nu) {
    String k1 = getNotNull(old.getGivenNames()) + getNotNull(old.getSurnames());
    String k2 = getNotNull(nu.getGivenNames()) + getNotNull(nu.getSurnames());
    if (!k1.equals(k2))
      throw new OtmException("Mismatch in Citation " + key + ". Expecting '" + k1 + "', got '" +
                             k2 + "'");

    // Apply the changes from r6692, r6774 and r6867
    old.setRealName(nu.getRealName());
    old.setSuffix(nu.getSuffix());
  }

  private String getNotNull(String s) {
    return (s == null) ? "" : s.trim();
  }

  private  Document getObjectInfo(Article a) {
    try {
      return buildObjectInfo(a.getId(), getSource(a), findObjectHandler());
    } catch (TransformerException e) {
      throw new OtmException("transform error", e);
    } catch (SAXException e) {
      throw new OtmException("parsing error", e);
    }
  }

  private String findObjectHandler() {
    return CitationMigrator.class.getResource("citations.xslt").toString();
  }

  private Source getSource(Article a) throws SAXException {
    Representation main = a.getRepresentation("XML");
    return new CachedSource(new InputSource(main.getBody().getInputStream()));
  }

  private Document buildObjectInfo(URI id, Source inp, String handler)
      throws TransformerException, SAXException {
    Transformer t = tFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    // override the doi url prefix if one is specified in the config
    final String doiUrlPrefix = ConfigurationStore.getInstance().getConfiguration().
      getString("ambra.platform.doiUrlPrefix", null);
    if (doiUrlPrefix != null)
      t.setParameter("doi-url-prefix", doiUrlPrefix);

    // set up the article-uri for copy into the output
    t.setParameter("article-uri", id.toString());

    /* Note: it would be preferable (and correct according to latest JAXP specs) to use
     * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
     * Hence we need to use Saxon's API's in order to get at those messages.
     */
    final StringWriter msgs = new StringWriter();
    ((Controller) t).makeMessageEmitter();
    ((Controller) t).getMessageEmitter().setWriter(msgs);
    t.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing zip", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }
    });

    DOMResult res = new DOMResult();
    try {
      t.transform(inp, res);
    } catch (TransformerException te) {
      if (msgs.getBuffer().length() > 0)
        throw new TransformerException(msgs.toString(), te);
      else
        throw te;
    }
    if (msgs.getBuffer().length() > 0)
      throw new TransformerException(msgs.toString());

    return (Document) res.getNode();
  }

  private Article unmarshal(Document doc) {
    XStream unmarshaller = getUnmarshaller();

    for (Element child : getChildren(doc.getDocumentElement(), "Article")) {
      return (Article) unmarshaller.unmarshal(new DomReader(child));
    }

    throw new OtmException("Could not find child-element <Article> in document.");
  }

  private void validate(Article article) {
    if (article.getDublinCore() == null)
      throw new OtmException("Missing dublin-core in <" + article.getId() + ">");
    if (article.getDublinCore().getBibliographicCitation() == null)
      throw new OtmException("Missing bibliographicCitation in <" + article.getId() + ">");
    if (article.getDublinCore().getBibliographicCitation().getDoi() == null)
      throw new OtmException("Missing bibliographicCitation.doi in <" + article.getId() + ">");
  }

  private List<Element> getChildren(Element parent, String child) {
    List<Element> res = new ArrayList<Element>();

    NodeList items = parent.getChildNodes();
    for (int idx = 0; idx < items.getLength(); idx++) {
      if (items.item(idx) instanceof Element) {
        Element c = (Element) items.item(idx);
        if (child == null || c.getTagName().equals(child))
          res.add(c);
      }
    }

    return res;
  }

  private XStream getUnmarshaller() {
    final XStream xstream = new XStream(null, null, getClass().getClassLoader(),
                                        new Ingester.CollectionMapper(new XStream().getMapper()));

    xstream.setMode(XStream.ID_REFERENCES);

    xstream.registerConverter(new SingleValueConverter() {
      @SuppressWarnings("unchecked")
      public boolean canConvert(Class type) {
        return type == URI.class;
      }

      public String toString(Object obj) {
        return obj.toString();
      }

      public Object fromString(String str) {
        try {
          return new URI(str);
        } catch (Exception e) {
          throw new XStreamException(str, e);
        }
      }
    });

    xstream.alias("Article", Article.class);

    return xstream;
  }

  private String dom2String(Node dom) {
    try {
      StringWriter sw = new StringWriter(500);
      Transformer t = tFactory.newTransformer();
      t.transform(new DOMSource(dom), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException te) {
      log.error("Error converting dom to string", te);
      return "";
    }
  }
}
