/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.plos.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerSet;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.client.FedoraAPIM;

import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;

/**
 * Article interfaces that go beyond the triple-store.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class ArticleUtil {
  private static final Log           log   = LogFactory.getLog(ArticleUtil.class);
  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  private static final String        MODEL = "<" + CONF.getString("topaz.models.articles") + ">";
  private static final String        MODEL_PP = "<" + CONF.getString("topaz.models.pp") + ">";
  private static final List          FGS_URLS  = CONF.getList("topaz.fedoragsearch.urls.url");
  private static final String        FGS_REPO  = CONF.getString("topaz.fedoragsearch.repository");

  private final Uploader   uploader;
  private final FedoraAPIM apim;
  private final ItqlHelper itql;
  private final FgsOperations[] fgs;
  private final Ingester   ingester;
  private final URI        fedoraServer;

  private static final String ITQL_GET_PID =
      ("select $pid from ${MODEL} where <${uri}> <topaz:isPID> $pid;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_OBJ =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
       "  $s <tucana:is> <${subj}> from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_PP =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
       "  $s <tucana:is> <${subj}> from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL_PP);

  private static final String ITQL_FIND_OBJS =
      ("select $obj $pid from ${MODEL} where " +
          // ensure it's an article
       "  <${subj}> <rdf:type> <topaz:Article> and $obj <topaz:isPID> $pid and (" +
          // find all related objects
       "  <${subj}> <dc_terms:hasPart> $obj or <${subj}> <topaz:hasCategory> $obj " +
          // find the article itself
       "  or $obj <tucana:is> <${subj}> );").
      replaceAll("\\Q${MODEL}", MODEL);

  /**
   * Create article utilities from default configuration values.<p>
   *
   * Currently we're not using CAS to talk to fedora, mulgara or search, so interfacing
   * to article-utils this way is probably just fine.
   */
  public ArticleUtil()
      throws MalformedURLException, ServiceException, RemoteException {
    this(new PasswordProtectedService(CONF.getString("topaz.services.fedora.uri"),
                                      CONF.getString("topaz.services.fedora.userName"),
                                      CONF.getString("topaz.services.fedora.password")),
         new PasswordProtectedService(CONF.getString("topaz.services.fedoraUploader.uri"),
                                      CONF.getString("topaz.services.fedoraUploader.userName"),
                                      CONF.getString("topaz.services.fedoraUploader.password")),
         new UnProtectedService(CONF.getString("topaz.services.itql.uri")));
  }

  /**
   * Create new article utilities.
   *
   * @param fedoraSvc the fedora service moniker
   * @param uploadSvc the fedora upload service moniker
   * @param mulgaraSvc the mulgara service moniker
   */
  public ArticleUtil(ProtectedService fedoraSvc,
                     ProtectedService uploadSvc,
                     ProtectedService mulgaraSvc)
      throws MalformedURLException, ServiceException, RemoteException {
    this.uploader = new Uploader(uploadSvc);
    this.apim     = APIMStubFactory.create(fedoraSvc);
    this.itql     = new ItqlHelper(mulgaraSvc);
    this.fgs      = getFgsOperations();
    this.ingester = new Ingester(itql, apim, uploader, fgs);
    this.fedoraServer = getFedoraBaseUri();
  }

  /**
   * Add a new article.
   *
   * @param zip    a zip archive containing the article and associated objects. The content type
   *               should be <var>application/zip</var>. If possible this should contain the name
   *               of the zip too.
   * @return the URI of the new article
   * @throws DuplicateArticleIdException if the article already exists (as determined by its URI)
   * @throws IngestException if there's a problem ingesting the archive
   * @throws RemoteException if some other error occured
   */
  public String ingest(Zip zip) throws DuplicateArticleIdException, IngestException {
    return ingester.ingest(zip);
  }

  /**
   * Delete an article. Note that it may not be possible to find and therefore erase all traces
   * from the ingest.
   *
   * @param article the URI of the article (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void delete(String article) throws NoSuchArticleIdException, RemoteException {
    String txn = "delete " + article;
    try {
      // TODO: Remove article via otm
      itql.beginTxn(txn);

      String[][] objList = findAllObjects(article, itql);
      if (log.isDebugEnabled())
        log.debug("deleting all objects for uri '" + article + "'");

      for (int idx = 0; idx < objList.length; idx++) {
        String uri = objList[idx][0];
        String pid = objList[idx][1];

        if (log.isDebugEnabled())
          log.debug("deleting uri '" + uri + "'");

        // Remove article from full-text index first
        String result = "";
        RemoteException firstRE = null;
        for (int i = 0; i < fgs.length; i++) {
          try {
            result = fgs[i].updateIndex("deletePid", pid, FGS_REPO, null, null, null);
          } catch (RemoteException re) {
            if (i == 0)
              throw re;
            if (firstRE == null)
              firstRE = re;
            log.error("Deleted pid '" + pid + "' from some server(s). But not from server " +
                      i + ". Cleanup required.", re);
          }
        }
        if (firstRE != null)
          throw firstRE;

        if (log.isDebugEnabled())
          log.debug("Removed '" + pid + "' from full-text index:\n" + result);

        // Remove from fedora
        try {
          apim.purgeObject(pid, "Purged object", false);
        } catch (RemoteException re) {
          if (!FedoraUtil.isNoSuchObjectException(re))
            throw re;
          log.warn("Tried to remove non-existent object '" + pid + "'");
        }
        itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_OBJ, "subj", uri), null);
        itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_PP, "subj", uri), null);
      }

      itql.commitTxn(txn);
      txn = null;
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        log.debug("Error rolling failed transaction", t);
      }
    }
  }

  /**
   * Get the URL from which the object's contents can retrieved via GET. Note that this method may
   * return a URL even when object or the representation don't exist, in which case the URL may
   * return a 404 response.
   *
   * @param obj the URI of the object (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @param rep the desired representation of the object
   * @return the URL, or null if the desired representation does not exist
   * @throws NoSuchObjectIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public String getObjectURL(String obj, String rep)
      throws NoSuchObjectIdException, RemoteException {
    String pid = getPIDForURI(obj, itql);
    if (pid == null)
      throw new NoSuchObjectIdException(obj, "with representation: " + rep);

    String path = "/fedora/get/" + pid + "/" + rep;
    return fedoraServer.resolve(path).toString();
  }

  private static FgsOperations[] getFgsOperations() throws ServiceException {
    FgsOperations ops[] = new FgsOperations[FGS_URLS.size()];
    for (int i = 0; i < ops.length; i++) {
      String url = FGS_URLS.get(i).toString();
      try {
        ops[i] = new FgsOperationsServiceLocator().getOperations(new URL(url));
      } catch (MalformedURLException mue) {
        throw new ServiceException("Invalid fedoragsearch URL '" + url + "'", mue);
      }
      if (ops[i] == null)
        throw new ServiceException("Unable to create fedoragsearch service at '" + url + "'");
    }
    return ops;
  }

  private static URI getFedoraBaseUri() {
    String fedoraBase = CONF.getString("topaz.services.fedora.uri");
    URI uri = ItqlHelper.validateUri(fedoraBase, "topaz.services.fedora.uri");
    if (uri.getHost().equals("localhost")) {
      try {
        String serverName = CONF.getString("topaz.server.hostname");
        uri = new URI(uri.getScheme(), null, serverName, uri.getPort(), uri.getPath(), null, null);
      } catch (URISyntaxException use) {
        throw new Error(use); // Can't happen
      }
    }

    return uri;
  }

  protected String[][] findAllObjects(String subj, ItqlHelper itql)
      throws NoSuchArticleIdException, RemoteException, AnswerException {
    ItqlHelper.validateUri(subj, "subject");

    StringAnswer ans =
        new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_FIND_OBJS, "subj", subj), null));
    List uris = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (uris.size() == 0)
      throw new NoSuchArticleIdException(subj);

    String[][] res = new String[uris.size()][];
    for (int idx = 0; idx < res.length; idx++)
      res[idx] = (String[]) uris.get(idx);

    return res;
  }

  private String getPIDForURI(String uri, ItqlHelper itql) throws RemoteException {
    try {
      AnswerSet ans =
          new AnswerSet(itql.doQuery(ItqlHelper.bindValues(ITQL_GET_PID, "uri", uri), null));
      ans.next();

      AnswerSet.QueryAnswerSet info = ans.getQueryResults();

      if (!info.next())
        return null;

      return info.getString("pid");
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }
}
