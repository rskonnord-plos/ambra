/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.ehcache.Ehcache;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

import org.plos.article.service.BrowseService;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.service.SecondaryObject;
import org.plos.model.article.ArticleType;
import org.plos.models.Article;
import org.plos.models.Journal;
import org.plos.models.ObjectInfo;
import org.plos.article.service.RepresentationInfo;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.IngestException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.journal.JournalService;
import org.plos.util.FileUtils;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;
import org.topazproject.xml.transform.cache.CachedSource;

import org.springframework.beans.factory.annotation.Required;

/**
 *
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public class DocumentManagementService {

  private static final Log log = LogFactory.getLog(DocumentManagementService.class);

  private ArticleOtmService articleOtmService;

  private FetchArticleService fetchArticleService;

  private String documentDirectory;

  private String ingestedDocumentDirectory;

  private CrossRefPosterService crossRefPosterService;

  private File xslTemplate;

  private BrowseService browseService;

  private JournalService journalService;

  private Session session;

  private String plosDoiUrl;

  private String plosEmail;

  private boolean sendToXref;

  public DocumentManagementService() {
  }

  public void init() {
  }

  /**
   * Set the article web service
   *
   * @param articleOtmService
   *          articleOtmService
   */
  @Required
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  @Required
  public void setDocumentDirectory(final String documentDirectory) {
    this.documentDirectory = documentDirectory;
  }

  public String getDocumentDirectory() {
    return documentDirectory;
  }

  @Required
  public void setIngestedDocumentDirectory(final String ingestedDocumentDirectory) {
    this.ingestedDocumentDirectory = ingestedDocumentDirectory;
  }

  @Required
  public void setCrossRefPosterService(final CrossRefPosterService crossRefPosterService) {
    this.crossRefPosterService = crossRefPosterService;
  }

  @Required
  public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL
   *          filenameOrURL
   * @throws URISyntaxException
   *           URISyntaxException
   * @return the local or remote file or url as a java.io.File
   */
  private File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      // access it as a local file resource
      return new File(FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Deletes an article from Topaz, but does not flush the cache
   * Useful for deleting a recently ingested article that hasn't been published
   *
   * @param objectURI -
   *          URI of the article to delete
   * @throws RemoteException
   * @throws NoSuchArticleIdException
   */
  public void delete(String objectURI)
      throws RemoteException, ServiceException, NoSuchArticleIdException, IOException {
    articleOtmService.delete(objectURI);
  }

  /**
   * Deletes articles from Topaz and flushes the servlet image cache and article cache
   *
   * @param objectURIs     URIs of the articles to delete
   * @param servletContext Servlet Context under which the image cache exists
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> delete(String[] objectURIs, ServletContext servletContext) {
    List<String> msgs = new ArrayList<String>();
    for (String objectURI : objectURIs) {
      try {
        articleOtmService.delete(objectURI);
        msgs.add("Deleted: " + objectURI);
        if (log.isDebugEnabled())
          log.debug("deleted article: " + objectURI);
      } catch (Exception e) {
        log.error("Could not delete article: " + objectURI, e);
        msgs.add("Error deleting: " + objectURI + " - " + e);
      }
    }

    browseService.notifyArticlesDeleted(objectURIs);
    fetchArticleService.removeFromArticleCache(objectURIs);

    return msgs;
  }

  /**
   *
   * @param pathname
   *          of file on server to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateArticleIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageResizeException
   * @throws ServiceException
   */
  public String ingest(String pathname) throws IngestException, DuplicateArticleIdException,
  ImageResizeException, IOException, SAXException, TransformerException, NoSuchArticleIdException,
  NoSuchObjectIdException, ServiceException {
    return ingest(new File(pathname));
  }

  /**
   * Ingest the file. If successful move it to the ingestedDocumentDirectory then create the
   * Transformed CrossRef xml file and deposit that in the Directory as well.
   *
   *
   * @param file
   *          to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateArticleIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageResizeException
   * @throws ServiceException
   */
  public String ingest(File file) throws IngestException, DuplicateArticleIdException,
  ImageResizeException, IOException, SAXException, TransformerException, NoSuchArticleIdException,
  NoSuchObjectIdException, ServiceException {
    String uri;
    File ingestedDir = new File(ingestedDocumentDirectory);
    if (log.isDebugEnabled()) {
      log.debug("Ingesting: " + file);
    }
    DataHandler dh = new DataHandler(file.toURL());
    uri = articleOtmService.ingest(dh);
    
    ImageSetConfig imageSetConf = getImageSetConfigForArticleUri(uri);
    if (imageSetConf == null) {
      imageSetConf = ImageSetConfig.getDefaultImageSetConfig();
    }
    
    if (log.isInfoEnabled()) {
      log.info("Ingested: " + file);
    }
    try {
      resizeImages(uri, imageSetConf);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Resize images failed for article " + uri, e);
      }
      URI articleUri = null;
      try {
        articleUri = new URI (uri);
      } catch (URISyntaxException ue) {
      }
      ImageResizeException ire = new ImageResizeException(articleUri, e);
      throw ire;
    } finally {
      try {
        dh.getOutputStream().close();
      } catch (Exception e) {
        // ignore
      }
    }

    if (log.isInfoEnabled()) {
      log.info("Resized images");
    }
    generateCrossRefInfoDoc(file, uri);
    if (log.isInfoEnabled()) {
      log.info("Generated Xref for file: " + file);
    }
    if (!file.renameTo(new File(ingestedDir, file.getName()))) {
      throw new IOException("Cannot relocate ingested documented " + file.getName());
    }
    if (log.isInfoEnabled()) {
      log.info("Ingested and relocated " + file + ":" + uri);
    }
    return uri;
  }

  /**
   * Find the ImageSetConfig for the given Article URI String. 
   * @param uri of the article
   * @return ArticleType of the given article URI
   */
  private ImageSetConfig getImageSetConfigForArticleUri(String uri) {
    Article article = null;
    try {
      article = fetchArticleService.getArticleInfo(uri);
    } catch (ApplicationException e) {
      log.error("Unable to retrieve Article URI='"+uri+"'", e);
    }
    if (article == null) {
      log.warn("fetchArticleService.getArticleInfo() returned null for article URI: '"+uri+"' so using default image set");
      return ImageSetConfig.getDefaultImageSetConfig();
    }
    
    Set<URI> artTypes = article.getArticleType();
    ArticleType at = null;
    ImageSetConfig isc;
    for (URI artTypeUri : artTypes) {
      if ((at = ArticleType.getKnownArticleTypeForURI(artTypeUri))!= null) {
        if ((isc = ImageSetConfig.getImageSetConfig(at.getImageSetConfigName())) != null) {
          return isc;
        }
      }
    }
    
    log.debug("Unable to find ImageSetConfig for article: '"+uri+"'");
    return null;
  }

  private void resizeImages(String uri, ImageSetConfig imageSetConfig) throws NoSuchArticleIdException, NoSuchObjectIdException,
                                               ImageResizeException, ImageStorageServiceException,
                                               HttpException, IOException {
    ImageResizeService irs;
    ImageStorageService iss;

    SecondaryObject[] objects = articleOtmService.listSecondaryObjects(uri);
    SecondaryObject object = null;
    for (int i = 0; i < objects.length; ++i) {
      irs = new ImageResizeService(imageSetConfig);
      object = objects[i];
      if (log.isDebugEnabled()) {
        log.debug("retrieving Object Info for: " + object.getUri());
      }
      ObjectInfo info = articleOtmService.getObjectInfo(object.getUri());
      if (log.isDebugEnabled()) {
        log.debug("object Info " + info);
        log.debug("contextElement" + info.getContextElement());
      }
      String context = info.getContextElement();
      if (context != null) {
        context = context.trim();
        iss = new ImageStorageService();
        if (context.equalsIgnoreCase("fig")) {
          RepresentationInfo rep = object.getRepresentations()[0];

          if (log.isDebugEnabled()) {
            log.debug("Found image to resize: " + rep.getURL() + " repsize-" + rep.getSize());
          }

          iss.captureImage(new URL(rep.getURL()));
          final byte[] originalBytes = iss.getBytes();
          articleOtmService.setRepresentation(object.getUri(), "PNG_S", new DataHandler(
              new PngDataSource(irs.getSmallScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set small image for " + object.getUri());
          }
          articleOtmService.setRepresentation(object.getUri(), "PNG_M", new DataHandler(
              new PngDataSource(irs.getMediumScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set medium image for " + object.getUri());
          }
          articleOtmService.setRepresentation(object.getUri(), "PNG_L", new DataHandler(
              new PngDataSource(irs.getLargeScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set large image for " + object.getUri());
          }
        } else if (context.equalsIgnoreCase("table-wrap")) {
          RepresentationInfo rep = object.getRepresentations()[0];

          if (log.isDebugEnabled()) {
            log.debug("Found image to resize: " + rep.getURL() + " repsize-" + rep.getSize());
          }

          iss.captureImage(new URL(rep.getURL()));
          final byte[] originalBytes = iss.getBytes();
          articleOtmService.setRepresentation(object.getUri(), "PNG_S", new DataHandler(
              new PngDataSource(irs.getSmallScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set small image for " + object.getUri());
          }
          articleOtmService.setRepresentation(object.getUri(), "PNG_M", new DataHandler(
              new PngDataSource(irs.getMediumScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set medium image for " + object.getUri());
          }
          articleOtmService.setRepresentation(object.getUri(), "PNG_L", new DataHandler(
              new PngDataSource(irs.getLargeScaledImage(originalBytes))));
          if (log.isDebugEnabled()) {
            log.debug("Set large image for " + object.getUri());
          }
        } else if (context.equals("disp-formula") || context.equals("chem-struct-wrapper")) {
          RepresentationInfo rep = object.getRepresentations()[0];

          if (log.isDebugEnabled()) {
            log.debug("Found image to resize for disp-forumla: " + rep.getURL());
          }

          iss.captureImage(new URL(rep.getURL()));
          final byte[] originalBytes = iss.getBytes();
          articleOtmService.setRepresentation(object.getUri(), "PNG", new DataHandler(
              new PngDataSource(irs.getLargeScaledImage(originalBytes))));
        } else if (context.equals("inline-formula")) {
          RepresentationInfo rep = object.getRepresentations()[0];

          if (log.isDebugEnabled()) {
            log.debug("Found image to resize for inline formula: " + rep.getURL());
          }

          iss.captureImage(new URL(rep.getURL()));
          final byte[] originalBytes = iss.getBytes();
          articleOtmService.setRepresentation(object.getUri(), "PNG", new DataHandler(
              new PngDataSource(irs.getLargeScaledImage(originalBytes))));
        }
        // Don't continue trying to process images if one of them failed
        /*
         * } catch (Exception e) {
         *
         * log.error("Couldn't resize image : " + ((object == null) ? "null" : object.getUri())); }
         */
      }
    }
  }

  /**
   * @return List of filenames of files in uploadable directory on server
   */
  public List<String> getUploadableFiles() {
    List<String> documents = new ArrayList<String>();
    File dir = new File(documentDirectory);
    if (dir.isDirectory()) {
      String filenames[] = dir.list();
      for (int i = 0; i < filenames.length; ++i) {
        if (filenames[i].toLowerCase().endsWith(".zip"))
          documents.add(filenames[i]);
      }
    }

    Collections.sort(documents);
    return documents;
  }

  /**
   * @return A list of URIs of ingested documents in ST_DISABLED
   * @throws ApplicationException
   */
  public Collection<String> getPublishableFiles() throws ApplicationException {
    try {
      List<String> articles =
          fetchArticleService.getArticleIds(null, null, new int[] { Article.STATE_DISABLED });
      Collections.sort(articles);
      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private void generateCrossRefInfoDoc(File file, String uri) throws ZipException, IOException,
  TransformerException, SAXException {
    ZipFile zip = new ZipFile(file);
    Enumeration entries = zip.entries();

    try {
      while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if (entry.getName().toLowerCase().endsWith(".xml")) {
          File source_xml = File.createTempFile("xref-doi-src", ".xml");
          File target_xml = new File(ingestedDocumentDirectory, uriToFilename(uri) + ".xml");

          BufferedInputStream fis = new BufferedInputStream(zip.getInputStream(entry));
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(source_xml));
          byte[] buf = new byte[(int) entry.getSize()];
          int size;
          while (-1 != (size = fis.read(buf))) {
            bos.write(buf, 0, size);
          }
          bos.flush();
          bos.close();
          fis.close();
          try {
            target_xml = crossRefXML(source_xml, target_xml);
          } finally {
            source_xml.delete();
          }
          break;
        }
      }
    } finally {
      zip.close();
    }
  }

  private File crossRefXML(File src, File dest) throws IOException,
  TransformerFactoryConfigurationError, TransformerException, SAXException {

    Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(xslTemplate));
    t.setParameter("plosDoiUrl", plosDoiUrl);
    t.setParameter("plosEmail", plosEmail);
    Source       s_source = new CachedSource(new InputSource(src.toURI().toString()));
    StreamResult s_result = new StreamResult(dest);
    t.transform(s_source, s_result);
    return dest;
  }

  /**
   * @param uri
   * @return a string usable as a distinct filename - ':', '/' and '.' -> '_'
   */
  private String uriToFilename(String uri) {
    return uri.replace(':', '_').replace('/', '_').replace('.', '_');
  }

  /**
   * @param uris  uris to be published. Send CrossRef xml file to CrossRef - if it is _received_ ok
   *              then set article stat to active
   * @param vjMap a map giving the set of virtual-journals each article is to be published in
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> publish(String[] uris, Map<String, Set<String>> vjMap) {
    final List<String> msgs             = new ArrayList<String>();
    final Set<Journal> modifiedJournals = new HashSet<Journal>();

    // publish articles
    for (String article : uris) {
      try {
        // send to cross-ref
        if (sendToXref) {
          File xref = new File(ingestedDocumentDirectory, uriToFilename(article) + ".xml");
          if (!xref.exists())
            throw new IOException("Cannot find CrossRef xml: " + uriToFilename(article) + ".xml");

          try {
            int stat = crossRefPosterService.post(xref);
            if (200 != stat)
              throw new Exception("CrossRef status returned " + stat);
          } catch (HttpException he) {
            log.error ("Could not connect to CrossRef", he);
            throw new Exception("Could not connect to CrossRef. " + he, he);
          } catch (IOException ioe) {
            log.error ("Could not connect to CrossRef", ioe);
            throw new Exception("Could not connect to CrossRef. " + ioe, ioe);
          }
        }

        // mark article as active
        articleOtmService.setState(article, Article.STATE_ACTIVE);
        msgs.add("Published: " + article);
        if (log.isDebugEnabled())
          log.debug("published article: '" + article + "'");

        // register with journals
        final Set<String> vjs = vjMap.get(article);
        if (vjs != null) {
          final String art = article;
          TransactionHelper.doInTxE(session, new TransactionHelper.ActionE<Void, Exception>() {
            public Void run(Transaction tx) throws Exception {
              for (String virtualJournal : vjs) {
                // get Journal by name
                final Journal journal = journalService.getJournal(virtualJournal);
                if (journal == null)
                  throw new Exception("Error adding article '" + art +
                                      "' to non-existent journal '" + virtualJournal + "'");

                // add Article to Journal
                journal.getSimpleCollection().add(URI.create(art));

                // update Journal
                session.saveOrUpdate(journal);
                modifiedJournals.add(journal);

                final String message =
                  "Article '" + art + "' was published in the journal '" + virtualJournal + "'";
                msgs.add(message);
                if (log.isDebugEnabled())
                  log.debug(message);
              }

              return null;
            }
          });
        }
      } catch (Exception e) {
        log.error("Could not publish article: '" + article + "'", e);
        msgs.add("Error publishing: '" + article + "' - " + e.toString());
      }
    }

    // notify journal service
    TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        for (Journal journal : modifiedJournals) {
          try {
            journalService.journalWasModified(journal);
          } catch (Exception e) {
            log.error("Error updating journal '" + journal + "'", e);
            msgs.add("Error updating journal '" + journal + "' - " + e.toString());
          }
        }

        return null;
      }
    });

    // notify browse service
    browseService.notifyArticlesAdded(uris);

    return msgs;
  }

  private static class PngDataSource implements DataSource {
    private final byte[] src;

    private final String ct;

    public PngDataSource(byte[] content) {
      this(content, "image/png");
    }

    public PngDataSource(byte[] content, String contType) {
      src = content;
      ct = contType;
      if (log.isDebugEnabled()) {
        log.debug("PngDataSource type=" + ct + " size=" + content.length);
      }
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(src);
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("Not supported");
    }

    public String getContentType() {
      return ct;
    }

    public String getName() {
      return "png";
    }
  }

  /**
   * @param browseService The BrowseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * @param plosDxUrl The plosDxUrl to set.
   */
  @Required
  public void setPlosDoiUrl(String plosDoiUrl) {
    this.plosDoiUrl = plosDoiUrl;
  }

  /**
   * @param sendToXref The sendToXref to set.
   */
  @Required
  public void setSendToXref(boolean sendToXref) {
    this.sendToXref = sendToXref;
  }

  /**
   * @param plosEmail The plosEmail to set.
   */
  @Required
  public void setPlosEmail(String plosEmail) {
    this.plosEmail = plosEmail;
  }
}
