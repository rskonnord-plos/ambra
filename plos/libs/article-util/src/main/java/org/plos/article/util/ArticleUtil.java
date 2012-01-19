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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.models.ObjectInfo;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.stores.ItqlStore;

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
  private static final String        queueDir    = CONF.getString("pub.spring.ingest.source", "/var/spool/plosone/ingestion-queue");
  private static final String        ingestedDir = CONF.getString("pub.spring.ingest.destination", "/var/spool/plosone/ingested");

  private final Uploader   uploader;
  private final FedoraAPIM apim;
  private final Session sess;
  private final FgsOperations[] fgs;
  private final Ingester   ingester;
  private static final URI        fedoraServer = getFedoraBaseUri();

  /**
   * Create article utilities from default configuration values.<p>
   *
   * Currently we're not using CAS to talk to fedora, mulgara or search, so interfacing
   * to article-utils this way is probably just fine.
   */
  public ArticleUtil()
      throws URISyntaxException, MalformedURLException, ServiceException, RemoteException {
    this(createSession());
  }

  private static Session createSession() throws URISyntaxException {
    SessionFactory factory = new SessionFactory();
    ItqlStore      itql    = new ItqlStore(new URI(CONF.getString("topaz.services.itql.uri")));
    factory.setTripleStore(itql);
    return factory.openSession();
  }

  public ArticleUtil(Session sess)
      throws MalformedURLException, ServiceException, RemoteException {

    this(CONF.getString("topaz.services.fedora.uri"),
         CONF.getString("topaz.services.fedora.userName"),
         CONF.getString("topaz.services.fedora.password"),
         CONF.getString("topaz.services.fedoraUploader.uri"),
         CONF.getString("topaz.services.fedoraUploader.userName"),
         CONF.getString("topaz.services.fedoraUploader.password"),
         sess);
  }

  /**
   * Create new article utilities.
   *
   * @param fedoraUri the fedora service uri
   * @param fedoraUserName the fedora service username
   * @param fedoraPasswd  the fedora service passwd
   * @param uploadUri the fedora upload service uri
   * @param uploadUserName the fedora upload service username
   * @param uploadPasswd the fedora upload service passwd
   * @param sess the OTM session
   */
  public ArticleUtil(String fedoraUri, String fedoraUserName, String fedoraPasswd,
                     String uploadUri, String uploadUserName, String uploadPasswd,
                     Session sess)
      throws MalformedURLException, ServiceException, RemoteException {
    this.uploader = new Uploader(uploadUri, uploadUserName, uploadPasswd);
    this.apim     = APIMStubFactory.create(fedoraUri, fedoraUserName, fedoraPasswd);
    this.fgs      = getFgsOperations();
    this.sess     = sess;
    this.ingester = new Ingester(sess, apim, uploader, fgs);
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

  public static long setDatastream(String pid, String rep, String ct, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    try {
      FedoraAPIM apim = APIMStubFactory.create(
                                      CONF.getString("topaz.services.fedora.uri"),
                                      CONF.getString("topaz.services.fedora.userName"),
                                      CONF.getString("topaz.services.fedora.password"));
      Uploader upld = new Uploader(
         CONF.getString("topaz.services.fedoraUploader.uri"),
         CONF.getString("topaz.services.fedoraUploader.userName"),
         CONF.getString("topaz.services.fedoraUploader.password"));

      if (content != null) {
        CountingInputStream cis = new CountingInputStream(content.getInputStream());
        String reLoc = upld.upload(cis);
        try {
          apim.modifyDatastreamByReference(pid, rep, null, null, false, ct, null, reLoc, "A",
                                           "Updated datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist yet - " +
                      "creating it"); // we don't need to log the exception here since this is not an error! 

          apim.addDatastream(pid, rep, new String[0], "Represention", false, ct, null, reLoc, "M",
                             "A", "New representation");
        }

        return cis.getByteCount();
      } else {
        try {
          apim.purgeDatastream(pid, rep, null, "Purged datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist", re);
        }

        return 0;
      }
    } catch (RemoteException re) {
      if (isNoSuchObject(re))
        throw new NoSuchObjectIdException(pid, "object '" + pid + "' doesn't exist in fedora", re);
      else
        throw re;
    } catch (IOException ioe) {
      throw new RemoteException("Error uploading representation", ioe);
    } catch (ServiceException se) {
      throw new RemoteException("Error uploading representation", se);
    }
  }

  private static boolean isNoSuchObject(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null &&
           msg.startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException:");
  }

  private static boolean isNoSuchDatastream(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null && msg.equals("java.lang.Exception: Uncaught exception from Fedora Server");
  }

  /**
   * Delete an article. Note that it may not be possible to find and therefore erase all traces
   * from the ingest.
   *
   * @param article the URI of the article (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @param tx a mulgara transaction within which to do the delete
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occurred
   * @throws IOException if there was a problem moving files back to the ingest queue
   * @throws ArticleDeleteException 
   */
  public static void delete(String article, Transaction tx)
      throws NoSuchArticleIdException, RemoteException, IOException, ArticleDeleteException {
    try {
      log.debug("Deleting '" + article + "'");
      delete(article, tx, APIMStubFactory.create(
                                      CONF.getString("topaz.services.fedora.uri"),
                                      CONF.getString("topaz.services.fedora.userName"),
                                      CONF.getString("topaz.services.fedora.password")),
                            getFgsOperations());

      // Clean up spool directories
      File ingestedXmlFile = new File(ingestedDir, article.replaceAll("[:/.]", "_") + ".xml");
      log.debug("Deleting '" + ingestedXmlFile + "'");
      try {
        FileUtils.forceDelete(ingestedXmlFile);
      } catch (FileNotFoundException fnfe) {
        log.info("'" + ingestedXmlFile + "' does not exist - cannot delete: " + fnfe);
      }
      if (!queueDir.equals(ingestedDir)) {
        String fname = article.substring(25) + ".zip";
        File fromFile = new File(ingestedDir, fname);
        File toFile   = new File(queueDir,    fname);
        log.debug("Copying '" + fromFile + "' to '" + toFile + "'");
        try {
          FileUtils.copyFile(fromFile, toFile);
          log.debug("Deleting '" + fromFile + "'");
          FileUtils.forceDelete(fromFile);
        } catch (FileNotFoundException fnfe) {
          log.info("Could not copy '" + fromFile + "' to '" + toFile + "': " + fnfe);
        }
      }
    } catch (MalformedURLException e) {
      throw new RemoteException("Bad configuration", e);
    } catch (ServiceException e) {
      throw new RemoteException("Failed to load search client stubs", e);
    }
  }

  private static void delete(String article, Transaction tx, FedoraAPIM apim, FgsOperations[] fgs)
      throws NoSuchArticleIdException, RemoteException, ArticleDeleteException {
    ArticleDeleteException ade = new ArticleDeleteException();
    Session session = tx.getSession();

    // load article and objects
    Article a = (Article) session.get(Article.class, article);
    if (a == null)
      throw new NoSuchArticleIdException(article);

    ObjectInfo oi = a;
    while (oi != null)
      oi = oi.getNextObject();
    for (Category c : a.getCategories())
      ;

    if (log.isDebugEnabled())
      log.debug("deleting all objects for uri '" + article + "'");

    // delete the article from mulgara first (in case of problems)
    try {
    session.delete(a);
		} catch (OtmException e) {
			ade.addException(e);
		}

    oi = a;
    while (oi != null) {
      if (log.isDebugEnabled())
        log.debug("deleting uri '" + oi.getId() + "'");

      // Remove article from full-text index first
      String result = "";
      for (int i = 0; i < fgs.length; i++) {
        try {
          result = fgs[i].updateIndex("deletePid", oi.getPid(), FGS_REPO, null, null, null);
        } catch (RemoteException re) {
          ade.addException(re);
          log.error("Deleted pid '" + oi.getPid() +
                    "' from some server(s). But not from server " +
                    i + ". Cleanup required.", re);
        }
      }

      if (log.isDebugEnabled())
        log.debug("Removed '" + oi.getPid() + "' from full-text index:\n" + result);

      // Remove from fedora
      try {
        apim.purgeObject(oi.getPid(), "Purged object", false);
      } catch (RemoteException re) {
        if (!FedoraUtil.isNoSuchObjectException(re))
          ade.addException(re);
        log.warn("Tried to remove non-existent object '" + oi.getPid() + "'");
      }

      oi = oi.getNextObject();
    }

    // remove category objects from Fedora
    for (Category c : a.getCategories()) {
      try {
        apim.purgeObject(c.getPid(), "Purged object", false);
      } catch (RemoteException re) {
        if (!FedoraUtil.isNoSuchObjectException(re)) {
          ade.addException(re);
        }
        log.warn("Tried to remove non-existent object '" + c.getPid() + "'");
      }
    }
    
    if (ade.getExceptionList().size()>0) {
    	throw ade;
    }
  }

  public static String getFedoraDataStreamURL(String pid, String ds) {
    String path = "/fedora/get/" + pid + "/" + ds;
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
}
