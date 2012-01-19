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
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import static org.plos.action.HomePageAction.WEEK_ARTICLE_CACHE_KEY;
import org.plos.article.service.ArticleWebService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.service.SecondaryObject;
import org.plos.util.FileUtils;
import org.topazproject.common.DuplicateIdException;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.NoSuchArticleIdException;
import org.topazproject.ws.article.NoSuchObjectIdException;
import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.ws.article.RepresentationInfo;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * 
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public class DocumentManagementService {
  
  private static final Log log = LogFactory.getLog(DocumentManagementService.class);
  
  private ArticleWebService articleWebService;
  
  private FetchArticleService fetchArticleService;
  
  private String documentDirectory;
  
  private String ingestedDocumentDirectory;
  
  private CrossRefPosterService crossRefPosterService;
  
  private File xslTemplate;
  
  private GeneralCacheAdministrator articleCacheAdministrator;
  
  public DocumentManagementService() {
  }
  
  public void init() {
  }
  
  /**
   * Set the article web service
   * 
   * @param articleWebService
   *          articleWebService
   */
  public void setArticleWebService(final ArticleWebService articleWebService) {
    this.articleWebService = articleWebService;
  }
  
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }
  
  public void setDocumentDirectory(final String documentDirectory) {
    this.documentDirectory = documentDirectory;
  }
  
  public String getDocumentDirectory() {
    return documentDirectory;
  }
  
  public void setIngestedDocumentDirectory(final String ingestedDocumentDirectory) {
    this.ingestedDocumentDirectory = ingestedDocumentDirectory;
  }
  
  public void setCrossRefPosterService(final CrossRefPosterService crossRefPosterService) {
    this.crossRefPosterService = crossRefPosterService;
  }
  
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
   * Deletes an article from Topaz
   * 
   * @param objectURI -
   *          URI of the article to delete
   * @throws RemoteException
   * @throws NoSuchIdException
   */
  public void delete(String objectURI) throws RemoteException, NoSuchIdException {
    articleWebService.delete(objectURI);
  }
  
  /**
   * 
   * @param pathname
   *          of file on server to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageResizeException
   */
  public String ingest(String pathname) throws IngestException, DuplicateIdException,
  ImageResizeException, IOException, TransformerException, NoSuchArticleIdException,
  NoSuchObjectIdException {
    return ingest(new File(pathname));
  }
  
  /**
   * Ingest the file. If succesful move it to the ingestedDocumentDirectory then create the
   * Transformed CrossRef xml file and deposit that in the Directory as well.
   * 
   * 
   * @param file
   *          to be ingested
   * @return URI of ingested document
   * @throws IngestException
   * @throws DuplicateIdException
   * @throws IOException
   * @throws TransformerException
   * @throws NoSuchArticleIdException
   * @throws NoSuchObjectIdException
   * @throws ImageResizeException
   */
  public String ingest(File file) throws IngestException, DuplicateIdException,
  ImageResizeException, IOException, TransformerException, NoSuchArticleIdException,
  NoSuchObjectIdException {
    String uri;
    File ingestedDir = new File(ingestedDocumentDirectory);
    log.info("Ingesting: " + file);
    DataHandler dh = new DataHandler(file.toURL());
    uri = articleWebService.ingest(dh);
    log.info("Ingested: " + file);
    try {
      resizeImages(uri);
    } catch (Exception e) {
      log.error("Resize images failed for article " + uri, e);
      ImageResizeException ire = new ImageResizeException(uri);
      ire.initCause(e);
      throw ire;
    }
    try {
      dh.getOutputStream().close();
    } catch (Exception e) {
      
    }
    log.info("Resized images");
    generateCrossRefInfoDoc(file, uri);
    log.info("Generated Xref for file: " + file);
    if (!file.renameTo(new File(ingestedDir, file.getName()))) {
      throw new IOException("Cannot relocate ingested documented " + file.getName());
    }
    log.info("Ingested and relocated " + file + ":" + uri);
    return uri;
  }
  
  private void resizeImages(String uri) throws NoSuchArticleIdException, NoSuchObjectIdException,
  HttpException, IOException {
    
    ImageResizeService irs;
    
    SecondaryObject[] objects = articleWebService.listSecondaryObjects(uri);
    SecondaryObject object = null;
    for (int i = 0; i < objects.length; ++i) {
      // try {
      irs = new ImageResizeService();
      object = objects[i];
      if (log.isDebugEnabled()) {
        log.debug("retrieving Object Info for: " + object.getUri());
      }
      ObjectInfo info = articleWebService.getObjectInfo(object.getUri());
      if (log.isDebugEnabled()) {
        log.debug("object Info " + info);
        log.debug("contextElement" + info.getContextElement());
      }
      String context = info.getContextElement();
      if (context != null) {
        context = context.trim();
        if (context.equalsIgnoreCase("fig")) {
          RepresentationInfo rep = object.getRepresentations()[0];
          if (log.isDebugEnabled()) {
            log.debug("Found image to resize: " + rep.getURL() + " repsize-" + rep.getSize());
          }
          irs.captureImage(rep.getURL());
          log.debug("Captured image");
          articleWebService.setRepresentation(object.getUri(), "PNG_S", new DataHandler(
              new PngDataSource(irs.getSmallImageBoxScaled())));
          log.debug("Set small");
          articleWebService.setRepresentation(object.getUri(), "PNG_M", new DataHandler(
              new PngDataSource(irs.getMediumImageBoxScaled())));
          log.debug("Set medium");
          articleWebService.setRepresentation(object.getUri(), "PNG_L", new DataHandler(
              new PngDataSource(irs.getLargeImageBoxScaled())));
          log.debug("Set large");
        } else if (context.equalsIgnoreCase("table-wrap")) { 
          RepresentationInfo rep = object.getRepresentations()[0];
          if (log.isDebugEnabled()) {
            log.debug("Found image to resize: " + rep.getURL() + " repsize-" + rep.getSize());
          }
          irs.captureImage(rep.getURL());
          log.debug("Captured image");
          articleWebService.setRepresentation(object.getUri(), "PNG_S", new DataHandler(
              new PngDataSource(irs.getSmallImageSubsample())));
          log.debug("Set small");
          articleWebService.setRepresentation(object.getUri(), "PNG_M", new DataHandler(
              new PngDataSource(irs.getMediumImageSubsample())));
          log.debug("Set medium");
          articleWebService.setRepresentation(object.getUri(), "PNG_L", new DataHandler(
              new PngDataSource(irs.getLargeImageSubsample())));
          log.debug("Set large");
        } else if (context.equals("disp-formula") || context.equals("chem-struct-wrapper")) {
          RepresentationInfo rep = object.getRepresentations()[0];
          if (log.isDebugEnabled()) {
            log.debug("Found image to resize for disp-forumla: " + rep.getURL());
          }
          irs.captureImage(rep.getURL());
          log.debug("Captured image");
          articleWebService.setRepresentation(object.getUri(), "PNG", new DataHandler(
              new PngDataSource(irs.getLargeImageSubsample())));
        } else if (context.equals("inline-formula")) {
          RepresentationInfo rep = object.getRepresentations()[0];
          if (log.isDebugEnabled()) {
            log.debug("Found image to resize for inline formula: " + rep.getURL());
          }
          irs.captureImage(rep.getURL());
          log.debug("Captured image");
          articleWebService.setRepresentation(object.getUri(), "PNG", new DataHandler(
              new PngDataSource(irs.getLargeImageSubsample())));
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
  public ArrayList<String> getUploadableFiles() {
    ArrayList<String> documents = new ArrayList<String>();
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
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Collection<String> getPublishableFiles() throws RemoteException, ApplicationException {
    ArrayList<String> articles = fetchArticleService.getArticles(null, null,
        new int[] { Article.ST_DISABLED });
    Collections.sort(articles);
    return articles;
  }
  
  private void generateCrossRefInfoDoc(File file, String uri) throws ZipException, IOException,
  TransformerException {
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
  TransformerFactoryConfigurationError, TransformerException {
    
    Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(xslTemplate));
    
    StreamSource s_source = new StreamSource(src);
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
   * @param uri
   *          uri to be published Send CrossRef xml file top CrossRef -- if it is _received_ ok then
   *          set article stat to active
   * @throws Exception
   */
  public void publish(String uri) throws Exception {
    File xref = new File(ingestedDocumentDirectory, uriToFilename(uri) + ".xml");
    int stat;
    if (!xref.exists()) {
      throw new IOException("Cannot find CrossRef xml");
    }
    stat = crossRefPosterService.post(xref);
    if (200 != stat) {
      throw new Exception("CrossRef status returned " + new Integer(200).toString());
    }
    articleWebService.setState(uri, Article.ST_ACTIVE);
    articleCacheAdministrator.flushEntry(WEEK_ARTICLE_CACHE_KEY);
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
      log.info("PngDataSource type=" + ct + " size=" + content.length);
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
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }
  
  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }
}
