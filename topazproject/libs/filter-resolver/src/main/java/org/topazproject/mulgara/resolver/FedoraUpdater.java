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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.mem.GraphImpl;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.JRDFSession;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.rdfxml.RdfXmlWriter;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.trippi.io.RIOTripleWriter;

import fedora.client.APIMStubFactory;
import fedora.client.Uploader;
import fedora.common.Constants;
import fedora.server.management.FedoraAPIM;

/** 
 * This takes care of updating Fedora with the new statements. The changed objects are queued, and
 * a worker periodically processes the queue and updates the affected datastreams of the affected
 * objects.
 * 
 * Configuration properties:
 * <dl>
 *   <dt>topaz.fr.fedoraUpdater.fedora.url</dt>
 *   <dd>the URL of the fedora server (required).</dd>
 *   <dt>topaz.fr.fedoraUpdater.fedora.user</dt>
 *   <dd>the username with which to authenticate to fedora (optional).</dd>
 *   <dt>topaz.fr.fedoraUpdater.fedora.pass</dt>
 *   <dd>the password with which to authenticate to fedora (optional).</dd>
 *   <dt>topaz.fr.fedoraUpdater.updateFilter.class</dt>
 *   <dd>the fully qualified name of the filter class to use (optional). If not set, defaults
 *       to <var>org.topazproject.kowari.DefaultUpdateFilter</var>.</dd>
 *   <dt>topaz.fr.fedoraUpdater.updateInterval</dt>
 *   <dd>how often, in milliseconds, to write the queued statements to fedora (required).</dd>
 * </dl>
    
 *
 * @author Ronald Tschalär
 */
class FedoraUpdater extends AbstractFilterHandler {
  private static final Logger logger = Logger.getLogger(FedoraUpdater.class);

  private static final URIReference RDF_TYPE;
  private static final URIReference FILT_MOD;

  private final Set             updQueue = new HashSet();
  private final Map             txQueue  = new HashMap();
  private final FedoraAPIM      apim;
  private final Uploader        uploader;
  private final URI             modelBase;
  private final SessionFactory  sessFactory;
  private final Map             dsModelMap = new HashMap();
  private final UpdateFilter    filter;
  private final XAResource      xaResource;
  private final Worker          worker;
  private       JRDFSession     sess;
  private       Xid             currentTxId;
  private volatile boolean      updateMaps;

  static {
    try {
      GraphElementFactory gef = new GraphImpl().getElementFactory();
      RDF_TYPE = gef.createResource(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
      FILT_MOD = gef.createResource(FilterResolver.MODEL_TYPE);
    } catch (GraphException ge) {
      throw new RuntimeException("Error creating resource-references", ge);
    } catch (GraphElementFactoryException gefe) {
      throw new RuntimeException("Error creating resource-references", gefe);
    }
  }

  /** 
   * Create a new udpater instance.
   *
   * @param config the configuration to use
   * @param base   the prefix under which the current <var>config</var> was retrieved
   * @param dbURI  the database uri
   */
  public FedoraUpdater(Configuration config, String base, URI dbURI) throws Exception {
    this(new URI(myConfig(config).getString("fedora.url", null)),
         myConfig(config).getString("fedora.user", null),
         myConfig(config).getString("fedora.pass", null),
         dbURI,
         SessionFactoryFinder.newSessionFactory(dbURI, false),
         myConfig(config).getLong("updateInterval", 10000L),
         myConfig(config).containsKey("updateFilter.class") ?
             (UpdateFilter) Class.forName(myConfig(config).getString("updateFilter.class"),
                                          true, Thread.currentThread().getContextClassLoader()).
                            newInstance() :
             null);
  }

  private static final Configuration myConfig(Configuration config) {
    return config.subset("fedoraUpdater");
  }

  /** 
   * Create a new udpater instance.
   * 
   * @param serverURL    the URL of the fedora server's API-A/API-M webservice
   * @param username     the username with which to authenticate to fedora
   * @param password     the password with which to authenticate to fedora
   * @param modelBase    the base URI of the models
   * @param sessFactory  the factory to use to get db sessions
   * @param updInternal  the number of milliseconds to wait between updates
   * @param updateFilter the filter instance to use; may be null in which case the default
   *                     processing occurs
   * @throws IOException if an error occured setting up the fedora client or updater
   * @throws ServiceException if an error occured setting up the fedora client or updater
   * @throws InstantiationException if thrown trying to create an instance of
   *                                <var>updateFilter</var>
   * @throws IllegalAccessException if thrown trying to create an instance of
   *                                <var>updateFilter</var>
   */
  private FedoraUpdater(URI serverURL, String username, String password, URI modelBase,
                        SessionFactory sessFactory, long updInternal, UpdateFilter updateFilter)
      throws IOException, ServiceException, InstantiationException, IllegalAccessException {
    this.sessFactory = sessFactory;
    this.modelBase   = modelBase;

    /* create the instances needed to access fedora.
     * Note that we don't use FedoraClient directly (FedoraClient.getAPIM()) because that
     * goes off and initializes (messes up) the log4j stuff. Instead, we just go to the soap
     * factory directly.
     */
    apim = APIMStubFactory.getStub(serverURL.getScheme(), serverURL.getHost(), serverURL.getPort(),
                                   username, password);
    uploader = new Uploader(serverURL.getScheme(), serverURL.getHost(), serverURL.getPort(),
                            username, password);

    filter = (updateFilter != null) ? updateFilter : new DefaultUpdateFilter();

    updateModelMaps();

    xaResource = new UpdaterXAResource();

    worker = new Worker(updInternal);
    worker.start();
  }

  /**
   * Signal to the updater that a model has been created or deleted.
   */
  private void updateModelMaps() {
    updateMaps = true;
  }

  public void modelCreated(URI filterModel, URI realModel) throws ResolverException {
    getDS(filterModel); // check there's a proper ds attribute
    updateModelMaps();
  }

  public void modelRemoved(URI filterModel, URI realModel) {
    updateModelMaps();
  }

  public void modelModified(URI filterModel, URI realModel, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException {
    queueMod(getDS(filterModel), stmts, occurs, resolverSession);
  }

  private String getDS(URI uri) throws ResolverException {
    String modelName = uri.getRawFragment();

    String[] params = modelName.substring(modelName.indexOf(':') + 1).split(";");
    for (int idx = 0; idx < params.length; idx++) {
      if (params[idx].startsWith("ds="))
        return params[idx].substring(3);
    }

    throw new ResolverException("invalid model name encountered: '" + uri + "' - must be of " +
                                "the form <dbURI>#filter:model=<model>;ds=<datastream>");
  }

  /**
   * Queue a list of model changes.
   *
   * @param ds              the datastream the statements affect
   * @param statements      the list of rdf statements
   * @param occurs          whether the statements represent inserts (true) or deletes (false)
   * @param resolverSession the resolver session
   */
  private void queueMod(String ds, Statements statements, boolean occurs,
                        ResolverSession resolverSession)
      throws ResolverException {
    synchronized (txQueue) {
      Set queue = (Set) txQueue.get(currentTxId);
      if (queue == null)
        txQueue.put(currentTxId, queue = new HashSet());

      try {
        statements.beforeFirst();
        while (statements.next()) {
          URIReference s = toURINode(resolverSession, statements.getSubject());
          queue.add(new ModItem(s, ds));
          if (logger.isDebugEnabled())
            logger.debug("Queued '" + s + "' for update");
        }
      } catch (TuplesException te) {
        throw new ResolverException("Error getting statements", te);
      }
    }
  }

  /** 
   * Return the XAResource representing this instance. The XAResource is used to know
   * when a transaction is being committed or rolled-back.
   * 
   * @return the xa-resource
   */
  public XAResource getXAResource() {
    return xaResource;
  }

  /**
   * Flush all pending updates and shut down.
   */
  public void close() {
    logger.info("Flushing updater");
    worker.interrupt();
    try {
      worker.join();
    } catch (InterruptedException ie) {
      logger.warn("interrupted while waiting for updates to be flushed");
    }
  }


  /* =====================================================================
   * ==== Everything below is run in the context of the Worker thread ====
   * =====================================================================
   */

  private void processQueue() {
    if (logger.isDebugEnabled())
      logger.debug("Processing update queue");

    // get our db session
    if (sess == null) {
      try {
        sess = (JRDFSession) sessFactory.newJRDFSession();
      } catch (QueryException qe) {
        logger.warn("Failed to get db session - retrying later", qe);
        return;
      }
    }

    // make a copy of the queue and clear it
    final Set mods = new HashSet();
    synchronized (updQueue) {
      mods.addAll(updQueue);
      updQueue.clear();
    }

    if (mods.size() > 0 && updateMaps) {
      try {
        doUpdateModelMaps();
      } catch (TuplesException te) {
        logger.warn("Failed to update model maps - retrying later", te);
        return;
      } catch (GraphException ge) {
        logger.warn("Failed to update model maps - retrying later", ge);
        return;
      } catch (ResolverException re) {
        logger.warn("Failed to update model maps - retrying later", re);
        return;
      }
      updateMaps = false;
    }

    // process each subject
    for (Iterator iter = mods.iterator(); iter.hasNext(); ) {
      ModItem mod = (ModItem) iter.next();
      try {
        updateSubject(mod.subject, mod.datastream, sess);
      } catch (Exception e) {
        logger.warn("Failed to update '" + mod.subject + "' - requeueing for later", e);

        synchronized (updQueue) {
          updQueue.add(mod);
        }
      }
    }
  }

  private void doUpdateModelMaps() throws GraphException, TuplesException, ResolverException {
    Map dsMM = new HashMap();

    Answer ans = sess.find(modelBase.resolve("#"), null, RDF_TYPE, FILT_MOD);
    ans.beforeFirst();
    while (ans.next()) {
      URIReference m = (URIReference) ans.getObject(0);

      String model = FilterResolver.getModelName(m.getURI());
      String ds    = getDS(m.getURI());

      Set models = (Set) dsMM.get(ds);
      if (models == null)
        dsMM.put(ds, models = new HashSet());

      models.add(model);
    }

    dsModelMap.clear();
    dsModelMap.putAll(dsMM);
  }

  private void updateSubject(URIReference subj, String ds, JRDFSession sess) throws Exception {
    if (logger.isDebugEnabled())
      logger.debug("Processing '" + subj + "', '" + ds + "'...");

    String[] models = (String[]) ((Set) dsModelMap.get(ds)).toArray(new String[0]);

    // get fedora object-id for subject
    String objId = filter.getFedoraPID(subj.getURI(), models, ds);
    if (objId == null)
      return;

    // get final subject-uri to use
    URI subjURI = filter.processSubject(subj.getURI(), models, ds);

    // create writers for RDF/XML docs; in case of RELS-EXT we need to split into RELS-EXT and DC
    ByteArrayOutputStream dsBaos = new ByteArrayOutputStream(200);
    ByteArrayOutputStream dcBaos = ds.equals("RELS-EXT") ?  new ByteArrayOutputStream(200) : null;

    MultiWriter writer = new MultiWriter(
        (dcBaos != null) ? (RdfDocumentWriter) new SplitWriter(dcBaos, dsBaos) :
                           (RdfDocumentWriter) new RdfXmlWriter(dsBaos));

    RIOTripleWriter rtw = new RIOTripleWriter(new SubjectReWriter(writer, subjURI), new HashMap());

    // get all statements in all relevant models for this subject
    writer.getDelegate().startDocument();
    boolean hasStmts = false;

    for (Iterator iter = ((Set) dsModelMap.get(ds)).iterator(); iter.hasNext(); ) {
      String model = (String) iter.next();
      Answer ans = sess.find(modelBase.resolve("#" + model), subj, null, null);
      rtw.write(new MulgaraTripleIterator(ans));
      hasStmts |= ans.getRowCardinality() != Answer.ZERO;
    }

    writer.getDelegate().endDocument();

    // got the docs
    byte[] dsCont = dsBaos.toByteArray();
    byte[] dcCont = (dcBaos != null) ? dcBaos.toByteArray() : null;

    // update datastream
    if (!hasStmts) {
      if (logger.isDebugEnabled())
        logger.debug("Purging datastream '" + ds + "' on object '" + objId + "'");

      try {
        apim.purgeDatastream(objId, ds, null, "Removed by FedoraUpdater", false);
      } catch (RemoteException re) {
        if (isNoSuchObject(re) || isNoSuchDatastream(re))
          // the statements got removed again before the update happened
          logger.debug("Datastream '" + ds + "' on object '" + objId + "' doesn't seem to exist",
                       re);
        else
          throw re;
      }
    } else {
      if (logger.isDebugEnabled())
        logger.debug("Updating datastream '" + ds + "' on object '" + objId + "'");

      try {
        apim.modifyDatastreamByValue(objId, ds, null, null, true, null, null,
            dsCont, null, "Modifed by FedoraUpdater", false);
      } catch (RemoteException re) {
        if (isNoSuchObject(re)) {
          logger.debug("Object '" + objId + "' doesn't seem to exist - creating it", re);
          createObject(objId, "RDF subject node", "RDF");
        } else if (isNoSuchDatastream(re)) {
          logger.debug(ds + " for object '" + objId + "' doesn't seem to exist - creating it", re);
        } else
          throw re;

        String reLoc = uploader.upload(new ByteArrayInputStream(dsCont));
        apim.addDatastream(objId, ds, new String[0], "RDF statements", true, "text/xml",
            null, reLoc, "X", "A", "Created by FedoraUpdater");
      }
    }

    // update dublin-core if necessary
    if (dcCont != null) {
      if (logger.isDebugEnabled())
        logger.debug("Updating datastream 'DC' on object '" + objId + "'");

      try {
        apim.modifyDatastreamByValue(objId, "DC", null, null, true, null, null,
                                     dcCont, null, "Modifed by FedoraUpdater", false);
      } catch (RemoteException re) {
        if (isNoSuchObject(re) && !hasStmts)
          // the statements got removed again before the update happened
          logger.debug("Object '" + objId + "' doesn't seem to exist", re);
        else
          throw re;
      }
    }

    if (logger.isDebugEnabled())
      logger.debug("'" + subj + "', '" + ds + "' updated");
  }

  private static boolean isNoSuchObject(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return (msg != null &&
            msg.startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException:"));
  }

  private static boolean isNoSuchDatastream(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();

    // Fedora 2.1.0: return (msg != null && msg.startsWith("java.lang.NullPointerException:"));

    // Fedora 2.1.1:
    return (msg != null && msg.equals("java.lang.Exception: Uncaught exception from Fedora Server"));
  }

  private void createObject(String objId, String label, String cModel) throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<foxml:digitalObject xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
    xml.append("           xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"\n");
    xml.append("           xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd\"\n");
    xml.append("           PID=\"" + xmlEscape(objId) + "\">\n");
    xml.append("  <foxml:objectProperties>\n");
    xml.append("    <foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>\n");
    xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"" + xmlEscape(label) + "\"/>\n");
    xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"" + xmlEscape(cModel) + "\"/>\n");
    xml.append("  </foxml:objectProperties>\n");
    xml.append("</foxml:digitalObject>");
    String objXML = xml.toString();

    apim.ingest(objXML.getBytes("UTF-8"), "foxml1.0", "Created by FedoraUpdater");
  }

  private static final String xmlEscape(String in) {
    return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
  }

  private static URIReference toURINode(ResolverSession resolverSession, long node)
      throws ResolverException {
    Node globalNode = null;

    // Globalise the node
    try {
      globalNode = resolverSession.globalize(node);
    } catch (GlobalizeException ge) {
      throw new ResolverException("Couldn't globalize node " + node, ge);
    }

    // Check that our node is a URIReference
    if (!(globalNode instanceof URIReference))
      throw new ResolverException("Node parameter " + globalNode + " isn't a URI reference");

    return (URIReference) globalNode;
  }

  /** 
   * This represents the updater as a resource. It is used to ensure we only do updates on
   * stuff that's been committed in order to prevent spurious Fedora object creations.
   */
  private class UpdaterXAResource extends DummyXAResource {
    public void start(Xid xid, int flags) {
      currentTxId = xid;
    }

    public void end(Xid xid, int flags) {
      currentTxId = null;
    }

    public void commit(Xid xid, boolean onePhase) {
      Set queue;
      synchronized (txQueue) {
        queue = (Set) txQueue.remove(xid);
      }

      if (queue != null) {
        synchronized (updQueue) {
          updQueue.addAll(queue);
        }
      }
    }

    public void rollback(Xid xid) {
      synchronized (txQueue) {
        txQueue.remove(xid);
      }
    }
  }

  private static class ModItem {
    final URIReference subject;
    final String       datastream;

    public ModItem(URIReference subject, String datastream) {
      this.subject    = subject;
      this.datastream = datastream;
    }

    public int hashCode() {
      return subject.hashCode() + datastream.hashCode();
    }

    public boolean equals(Object other) {
      if (!(other instanceof ModItem))
        return false;
      ModItem o = (ModItem) other;
      return (subject.equals(o.subject) && datastream.equals(o.datastream));
    }
  }

  /**
   * This implements a writer that splits the statements into a DC and a RELS-EXT stream according
   * to whether the predicate is from DC or not. Furthermore, it completely ignores fedora system
   * predicates.
   */
  private static class SplitWriter implements RdfDocumentWriter {
    private static final String OAI_DC_PFX = "http://www.openarchives.org/OAI/2.0/oai_dc/";

    private final RdfDocumentWriter dcWriter;
    private final RdfDocumentWriter reWriter;

    public SplitWriter(OutputStream dcStream, OutputStream reStream) {
      dcWriter = new RdfXmlWriter(dcStream);
      reWriter = new RdfXmlWriter(reStream);
    }

    public void setNamespace(String prefix, String name) throws IOException {
      dcWriter.setNamespace(prefix, name);
      reWriter.setNamespace(prefix, name);
    }

    public void startDocument() throws IOException {
      dcWriter.startDocument();
      reWriter.startDocument();
    }

    public void endDocument() throws IOException {
      dcWriter.endDocument();
      reWriter.endDocument();
    }

    public void writeStatement(Resource subject, org.openrdf.model.URI predicate, Value object)
        throws IOException {
      if (predicate.getNamespace().startsWith(Constants.FEDORA_SYSTEM_DEF_URI))
        ;       // ignore
      else if (predicate.getNamespace().startsWith(Constants.DC.uri) ||
               predicate.getNamespace().startsWith(OAI_DC_PFX))
        dcWriter.writeStatement(subject, predicate, object);
      else
        reWriter.writeStatement(subject, predicate, object);
    }

    public void writeComment(String comment) throws IOException {
      dcWriter.writeComment(comment);
      reWriter.writeComment(comment);
    }
  }

  /**
   * A superclass for filtering RdfDocumentWriter's.
   */
  private static abstract class FilterWriter implements RdfDocumentWriter {
    protected final RdfDocumentWriter delegate;

    protected FilterWriter(RdfDocumentWriter delegate) {
      this.delegate = delegate;
    }

    public void setNamespace(String prefix, String name) throws IOException {
      delegate.setNamespace(prefix, name);
    }

    public void startDocument() throws IOException {
      delegate.startDocument();
    }

    public void endDocument() throws IOException {
      delegate.endDocument();
    }

    public void writeStatement(Resource subject, org.openrdf.model.URI predicate, Value object)
        throws IOException {
      delegate.writeStatement(subject, predicate, object);
    }

    public void writeComment(String comment) throws IOException {
      delegate.writeComment(comment);
    }

    public RdfDocumentWriter getDelegate() {
      return delegate;
    }
  }

  /**
   * This implements a writer that ignores startDocument() and endDocument() so it can be used
   * to write multiple results to the same document.
   */
  private static class MultiWriter extends FilterWriter {
    public MultiWriter(RdfDocumentWriter delegate) {
      super(delegate);
    }

    public void startDocument() throws IOException {
    }

    public void endDocument() throws IOException {
    }
  }

  /**
   * This implements a writer that replaces the subject node of each triple.
   */
  private static class SubjectReWriter extends FilterWriter {
    private final Resource newSubj;

    public SubjectReWriter(RdfDocumentWriter delegate, URI subject) {
      super(delegate);

      newSubj = new ValueFactoryImpl().createURI(subject.toString());
    }

    public void writeStatement(Resource subject, org.openrdf.model.URI predicate, Value object)
        throws IOException {
      delegate.writeStatement(newSubj, predicate, object);
    }
  }

  private class Worker extends Thread {
    private final long interval;

    public Worker(long interval) {
      super("FedoraUpdate-Worker");
      setDaemon(true);
      this.interval = interval;
    }

    public void run() {
      while (true) {
        try {
          sleep(interval);
        } catch (InterruptedException ie) {
          logger.warn("Worker thread interrupted - exiting", ie);
          break;
        }

        try {
          processQueue();
        } catch (Throwable t) {
          logger.error("Caught exception processing queue", t);
        }
      }

      // flush anything left
      processQueue();
    }
  }
}
