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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.BrowseService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.service.RepresentationInfo;
import org.plos.article.service.SecondaryObject;
import org.plos.article.util.ArticleUtil;
import org.plos.article.util.ArticleZip;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.ImageProcessingException;
import org.plos.article.util.ImageSetConfig;
import org.plos.article.util.IngestException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.configuration.ConfigurationStore;
import org.plos.journal.JournalService;
import org.plos.model.article.ArticleType;
import org.plos.models.Article;
import org.plos.models.Journal;
import org.plos.models.ObjectInfo;
import org.plos.util.FileUtils;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;
import org.topazproject.xml.transform.cache.CachedSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author alan Manage documents on server. Ingest and access ingested
 *         documents.
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
   * @param articleOtmService articleOtmService
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
   * @param filenameOrURL filenameOrURL
   * @throws URISyntaxException URISyntaxException
   * @return the local or remote file or url as a java.io.File
   */
  private File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      // access it as a local file resource
      return new File(FileUtils.getFileName(filenameOrURL));
    }
    return new File(resource.toURI());
  }

  /**
   * Deletes an article from Topaz, but does not flush the cache Useful for
   * deleting a recently ingested article that hasn't been published
   * @param objectURI - URI of the article to delete
   * @throws RemoteException
   * @throws NoSuchArticleIdException
   */
  public void delete(String objectURI) throws RemoteException, ServiceException,
      NoSuchArticleIdException, IOException {
    articleOtmService.delete(objectURI);
  }

  /**
   * Deletes articles from Topaz and flushes the servlet image cache and article
   * cache
   * @param objectURIs URIs of the articles to delete
   * @param servletContext Servlet Context under which the image cache exists
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> delete(String[] objectURIs, ServletContext servletContext) {
    List<String> msgs = new ArrayList<String>();
    for (String objectURI : objectURIs) {
      try {
        articleOtmService.delete(objectURI);
        msgs.add("Deleted: " + objectURI);
        if (log.isDebugEnabled()) log.debug("deleted article: " + objectURI);
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
   * @param pathname of file on server to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateArticleIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageProcessingException
   * @throws ServiceException
   */
  public String ingest(String pathname) throws IngestException, DuplicateArticleIdException,
      ImageProcessingException, IOException, SAXException, TransformerException, ServiceException {
    return ingest(new File(pathname));
  }

  /**
   * Ingest the file. If successful move it to the ingestedDocumentDirectory
   * then create the Transformed CrossRef xml file and deposit that in the
   * Directory as well.
   * @param file to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateArticleIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageProcessingException
   * @throws ServiceException
   */
  public String ingest(File file) throws IngestException, DuplicateArticleIdException,
      ImageProcessingException, IOException, SAXException, TransformerException, ServiceException {
    
    if(file == null) throw new IngestException("No ingest file specified");
    
    // do main ingest
    String uri;
    File ingestedDir = new File(ingestedDocumentDirectory);
    if (log.isDebugEnabled()) log.debug("Ingesting: " + file.toString() + "...");
    DataHandler dh = new DataHandler(file.toURL());
    uri = articleOtmService.ingest(dh);
    if (log.isInfoEnabled()) log.info("Ingested: " + file.toString());

    // process images
    try {
      processImages(uri, file);
    } catch (Throwable t) {
      if (log.isErrorEnabled()) log.error("Image processing failed for article " + uri, t);
      URI articleUri = null;
      try {
        articleUri = new URI(uri);
      } catch (URISyntaxException ue) {
      }
      ImageProcessingException ipe = new ImageProcessingException(t.getMessage(), t);
      ipe.setArticleURI(articleUri);
      throw ipe;
    }
    finally {
      try {
        dh.getOutputStream().close();
      } catch (Exception e) {
        // ignore
      }
    }
    if (log.isInfoEnabled()) log.info("Article images processed for: " + file.toString() + "...");
    
    // generate xref doc
    if (log.isInfoEnabled()) log.info("Generating Xref for file: " + file.toString() + " ...");
    generateCrossRefInfoDoc(file, uri);
    if (log.isInfoEnabled()) log.info("Xref generated");
    
    // relocate article zip file
    if (log.isInfoEnabled()) log.info("Relocating ingested document: " + file.toString() + " ...");
    if (!file.renameTo(new File(ingestedDir, file.getName()))) {
      throw new IOException("Cannot relocate ingested document: " + file.getName());
    }
    if (log.isInfoEnabled()) log.info("Ingested document moved to: " + ingestedDir);
    
    return uri;
  }

  /**
   * Index all Articles.
   * @return Map of doi, index result Strings.
   */
  public Map<String, String> indexArticles() throws ApplicationException {

    Map<String, String> results = new HashMap<String, String>();

    // repository is defined in config
    final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
    final String FGS_REPO = CONF.getString("topaz.fedoragsearch.repository");

    // let ArticleUtil use standard methods to get fedoragsearch operations
    FgsOperations[] fgs;
    try {
      fgs = ArticleUtil.getFgsOperations();
    } catch (ServiceException ex) {
      log.error(ex);
      throw new ApplicationException("Exception creating fedoragsearch operations", ex);
    }

    // query for all Article dois (in a Transaction)
    final String query = "select a.id doi from Article a order by doi;";
    Transaction tx = session.beginTransaction();
    final Results dois = session.createQuery(query).execute();
    tx.commit();

    // re-index each doi
    int indexCount = 0;
    while (dois.next()) {
      String doi = "doi:" + dois.get("doi").toString().substring(9).replace("/", "%2F");
      if (log.isDebugEnabled()) {
        log.debug("re-indexing Article: " + doi);
      }

      // index
      try {
        for (int onFgs = 0; onFgs < fgs.length; onFgs++) {
          long start = (new Date()).getTime();
          final String result = fgs[onFgs].updateIndex("fromPid", doi, FGS_REPO, null, null, null);
          long end = (new Date()).getTime();
          final String key = doi + "[" + onFgs + "] (" + (end - start) + "ms)";
          results.put(key, result);
          if (log.isDebugEnabled()) {
            log.debug("re-indexed Article: " + key + ", result: " + result);
          }
        }
      } catch (RemoteException re) {
        final String errorMessage = "Exception indexing Article: " + doi;
        log.error(errorMessage, re);
        throw new ApplicationException(errorMessage, re);
      }
      indexCount++;
    }

    if (log.isDebugEnabled()) {
      log.debug("re-indexed " + indexCount + " Articles");
    }

    return results;
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
      log.error("Unable to retrieve Article URI='" + uri + "'", e);
    }
    if (article == null) {
      log.warn("fetchArticleService.getArticleInfo() returned null for article URI: '" + uri
          + "' so using default image set");
      return ImageSetConfig.getDefaultImageSetConfig();
    }

    Set<URI> artTypes = article.getArticleType();
    ArticleType at = null;
    ImageSetConfig isc;
    for (URI artTypeUri : artTypes) {
      if ((at = ArticleType.getKnownArticleTypeForURI(artTypeUri)) != null) {
        if ((isc = ImageSetConfig.getImageSetConfig(at.getImageSetConfigName())) != null) {
          return isc;
        }
      }
    }

    log.debug("Unable to find ImageSetConfig for article: '" + uri + "'");
    return null;
  }

  /**
   * Factory method that resolves the appropriate IProcessedArticleImageProvider
   * impl given the article zip file.
   * @param articleZipFile The File of the article zip file
   * @return The resolved IProcessedArticleImageProvider impl
   */
  private IProcessedArticleImageProvider resolveProcessedArticleImageProvider(File articleZipFile) {
    assert articleZipFile != null;
    if(ArticleZip.isProcessedArticleZipFile(articleZipFile)) {
      return new PreProcessedArticleImageProvider(articleZipFile);
    }
    return new OnDemandArticleImageProvider();
  }

  /**
   * Processes a single article image.
   * @param so The {@link SecondaryObject} representing the article image
   *        "container".
   * @param imageSetConfig The ImageSetConfig to employ
   * @param repMimeTypes The native processed image mime-types to apply
   * @param paip The resolved IProcessedArticleImageProvider
   * @return New DataHandler instance holding the data of the processed article
   *         image.
   * @throws MalformedURLException When a bad repUrl is provided
   * @throws ImageProcessingException When an image processing related error
   *         occurrs.
   * @throws NoSuchObjectIdException
   * @throws RemoteException
   */
  private void processImage(SecondaryObject so, ImageSetConfig imageSetConfig, String[] repMimeTypes,
      IProcessedArticleImageProvider paip) throws MalformedURLException, ImageProcessingException,
      NoSuchObjectIdException, RemoteException {
    assert so != null && repMimeTypes != null && imageSetConfig != null && paip != null;
    
    RepresentationInfo ri = so.getRepresentations()[0];
    assert ri != null;
    if (log.isDebugEnabled()) {
      log.debug("Processing article image: " + ri.getURL() + " (size:" + ri.getSize() + ")");
    }
    for (String rmt : repMimeTypes) {
      ProcessedImageDataSource pids = paip.getProcessedArticleImage(new URL(ri.getURL()), imageSetConfig, rmt);
      DataHandler dh = new DataHandler(pids);
      articleOtmService.setRepresentation(so.getUri(), pids.getProcessedImageMimeType(), dh);
      if (log.isDebugEnabled()) {
        log.debug("Set processed article " + pids.getProcessedImageMimeType() + " image for "
            + so.getUri());
      }
    }
  }
  
  /**
   * ProcessedImageMimeTypeBinder - Defines the associations between image
   * contexts and their processed image mime-type counterparts.
   * @author jkirton
   */
  static final class ProcessedImageMimeTypeBinder {
    public static final String IMAGE_CONTEXT_FIG = "fig";
    public static final String IMAGE_CONTEXT_TABLE_WRAP = "table-wrap";
    public static final String IMAGE_CONTEXT_DISP_FORMULA = "disp-formula";
    public static final String IMAGE_CONTEXT_CHEM_STRUCT_WRAPPER = "chem-struct-wrapper";
    public static final String IMAGE_CONTEXT_INLINE_FORMULA = "inline-formula";

    public static final String[] smallMediumLarge = { "PNG_S", "PNG_M", "PNG_L" };
    public static final String[] singleLarge = { "PNG" };
    
    /**
     * Map of article image contexts and their associated processed image
     * mime-types.
     */
    public static final Map<String, String[]> bindings = new HashMap<String, String[]>();
    
    static {
      bindings.put(IMAGE_CONTEXT_FIG, smallMediumLarge);
      bindings.put(IMAGE_CONTEXT_TABLE_WRAP, smallMediumLarge);
      bindings.put(IMAGE_CONTEXT_DISP_FORMULA, singleLarge);
      bindings.put(IMAGE_CONTEXT_CHEM_STRUCT_WRAPPER, singleLarge);
      bindings.put(IMAGE_CONTEXT_INLINE_FORMULA, singleLarge);
    }
    
    /**
     * Provides the appropriate processed image mime-types for the given image
     * context.
     * <p>
     * <strong>IMPT: </strong>If the image context is <code>null</code> or
     * there is no defined binding, <code>null</code> is returned.
     * @param imageContext The image context
     * @return The associated processed image mime-types
     */
    public static String[] getProcessedImageMimeTypes(String imageContext) {
      return imageContext == null ? null : bindings.get(imageContext.toLowerCase());
    }
  }
  
  /**
   * Handles article image processing.
   * @param uri The ingested article URI String.
   * @param file The article zip file ref
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageProcessingException
   * @throws ImageStorageServiceException
   * @throws IOException
   */
  private void processImages(String uri, File file) throws NoSuchArticleIdException,
      NoSuchObjectIdException, ImageProcessingException, ImageStorageServiceException, IOException {
    assert uri != null && file != null;

    // resolve the image set config to employ
    if (log.isInfoEnabled()) log.info("Processing article images for: " + file.toString() + "...");
    ImageSetConfig imageSetConfig = getImageSetConfigForArticleUri(uri);
    if (imageSetConfig == null) {
      imageSetConfig = ImageSetConfig.getDefaultImageSetConfig();
    }
    assert imageSetConfig != null;
    if (log.isInfoEnabled()) log.info("Employing image set: " + imageSetConfig.toString() + "  for: " + file.toString() + "...");
    
    // determine the processed article image provider to employ 
    IProcessedArticleImageProvider paip = resolveProcessedArticleImageProvider(file);
    if (log.isInfoEnabled()) log.info("Employing : " + paip.toString() + "  for: " + file.toString() + "...");
    assert paip != null;
    
    // iterate the article images and process
    SecondaryObject[] objects = articleOtmService.listSecondaryObjects(uri);
    for (int i = 0; i < objects.length; ++i) {
      
      SecondaryObject object = objects[i];
      if (log.isDebugEnabled()) {
        log.debug("Retrieving ObjectInfo for: " + object.getUri() + " ...");
      }
      ObjectInfo info = articleOtmService.getObjectInfo(object.getUri());
      String context = info.getContextElement();
      if (context != null) {
        context = context.trim();
        if (log.isDebugEnabled()) {
          log.debug("Image context for " + info.toString() + " resolved to: " + context.toString());
        }
        
        // determine the processed image mime types for this image
        // NOTE: if there are no processed image mime-typs for the image context, we do NOT process the image
        final String[] piMimeTypes = ProcessedImageMimeTypeBinder.getProcessedImageMimeTypes(context);
        if(piMimeTypes != null) {
          assert piMimeTypes.length > 0;
          if(log.isInfoEnabled()) {
            log.info("Applying processed image mime-types: " + 
                ToStringBuilder.reflectionToString(piMimeTypes) + " for " + info.toString());
          }
          
          // process the image
          processImage(object, imageSetConfig, piMimeTypes, paip);
          // NOTE: Don't continue trying to process images if one of them failed
        }
      }
      else {
        if(log.isWarnEnabled()) log.warn("No image context for " + info.toString() + ". Skipping.");
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
        if (filenames[i].toLowerCase().endsWith(".zip")) documents.add(filenames[i]);
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
      List<String> articles = fetchArticleService.getArticleIds(null, null, new int[] {
        Article.STATE_DISABLED
      });
      Collections.sort(articles);
      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private void generateCrossRefInfoDoc(File file, String uri) throws ZipException, IOException,
      TransformerException, SAXException {
    ZipFile zip = new ZipFile(file);
    Enumeration<?> entries = zip.entries();

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
          }
          finally {
            source_xml.delete();
          }
          break;
        }
      }
    }
    finally {
      zip.close();
    }
  }

  private File crossRefXML(File src, File dest) throws TransformerFactoryConfigurationError,
      TransformerException, SAXException {

    Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(xslTemplate));
    t.setParameter("plosDoiUrl", plosDoiUrl);
    t.setParameter("plosEmail", plosEmail);
    Source s_source = new CachedSource(new InputSource(src.toURI().toString()));
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
   * @param uris uris to be published. Send CrossRef xml file to CrossRef - if
   *        it is _received_ ok then set article stat to active
   * @param vjMap a map giving the set of virtual-journals each article is to be
   *        published in
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> publish(String[] uris, Map<String, Set<String>> vjMap) {
    final List<String> msgs = new ArrayList<String>();
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
            if (200 != stat) throw new Exception("CrossRef status returned " + stat);
          } catch (HttpException he) {
            log.error("Could not connect to CrossRef", he);
            throw new Exception("Could not connect to CrossRef. " + he, he);
          } catch (IOException ioe) {
            log.error("Could not connect to CrossRef", ioe);
            throw new Exception("Could not connect to CrossRef. " + ioe, ioe);
          }
        }

        // mark article as active
        articleOtmService.setState(article, Article.STATE_ACTIVE);
        msgs.add("Published: " + article);
        if (log.isDebugEnabled()) log.debug("published article: '" + article + "'");

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
                  throw new Exception("Error adding article '" + art
                      + "' to non-existent journal '" + virtualJournal + "'");

                // add Article to Journal
                journal.getSimpleCollection().add(URI.create(art));

                // update Journal
                session.saveOrUpdate(journal);
                modifiedJournals.add(journal);

                final String message = "Article '" + art + "' was published in the journal '"
                    + virtualJournal + "'";
                msgs.add(message);
                if (log.isDebugEnabled()) log.debug(message);
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

  /**
   * @param browseService The BrowseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Sets the JournalService.
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Sets the otm util.
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
