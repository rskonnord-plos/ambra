/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import com.sun.org.apache.xpath.internal.XPathAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.service.InvalidProxyTicketException;
import org.plos.user.PlosOneUser;
import org.plos.util.FileUtils;
import org.plos.util.TextUtils;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.xml.transform.cache.CachedSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Fetch article service
 */
public class FetchArticleService {
  private ArticleWebService articleService;
  private File xslTemplate;
  private File secondaryObjectXslTemplate;
  private String articleRep;
  private Templates translet;
  private Templates secondaryObjectTranslet;
  private boolean useTranslet = true;
  private Map<String, String> xmlFactoryProperty;
  private String encodingCharset;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private AnnotationWebService annotationWebService;
  private DocumentBuilderFactory factory;

  private GeneralCacheAdministrator articleCacheAdministrator;
  
  private static final String CACHE_KEY_ARTICLE_INFO = "CACHE_KEY_ARTICLE_INFO";
  
  public void init() {
    // Set the TransformerFactory system property.
    for (Map.Entry<String, String> entry : xmlFactoryProperty.entrySet()) {
      System.setProperty(entry.getKey(), entry.getValue());
    }

    // Create a document builder factory and set the defaults
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
  }


  private String getTransformedArticle(final String articleURI) throws ApplicationException, NoSuchIdException {
    try {
      final Transformer transformer = getTransformer();
      final Source domSource = getAnnotatedContentAsDOMSource(articleURI);
      final Writer writer = new StringWriter(100000);
      
      transformer.transform(domSource,
                            new StreamResult(writer));
      return (writer.toString());
    } catch (InvalidProxyTicketException ex) {
      throw ex;
    } catch (NoSuchIdException ex) {
      throw ex;
    } catch (Exception e) {
      log.error("Transformation of article failed", e);
      throw new ApplicationException("Transformation of article failed", e);
    }
  }
  
  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public String getURIAsHTML(final String articleURI) throws ApplicationException,
                          RemoteException, NoSuchIdException {
    final PlosOneUser pou = (PlosOneUser)articleService.getSessionMap().get(Constants.PLOS_ONE_USER_KEY);
    String topazUserId = "";
    if (pou != null) {
      topazUserId = pou.getUserId();
    }
    String theArticle = null;
    String escapedURI = FileUtils.escapeURIAsPath(articleURI);
    try {
      //for now, since all annotations are public, don't have to cache based on userID
      theArticle = (String)articleCacheAdministrator.getFromCache(articleURI/* + topazUserId*/); 
      if (log.isDebugEnabled()) {
        log.debug("retrived article from cache: " + articleURI /*+ " / " + topazUserId*/);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        //use grouping for future when annotations can be private
        theArticle= getTransformedArticle(articleURI);
        articleCacheAdministrator.putInCache(articleURI/* + topazUserId*/, 
                                   theArticle, new String[]{escapedURI});
        updated = true;
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(articleURI);
      }
    }
    return theArticle;
  }

  /**
   * Runs secondary object description through an XSL style sheet to produce HTML
   * 
   * @param description description
   * @return String representing the transformed XML fragment or the original XML string if an error occurs
   * @throws ApplicationException
   */
  
  public String getTranformedSecondaryObjectDescription(String description) throws ApplicationException {
    String transformedString = description;
    try {
      final DocumentBuilder builder = createDocBuilder();
      Document desc = builder.parse(new InputSource(new StringReader("<desc>" + description + "</desc>")));
      final DOMSource domSource = new DOMSource(desc);
      final Transformer transformer = getSecondaryObjectTranslet();
      final Writer writer = new StringWriter(1000);
      
      transformer.transform(domSource,new StreamResult(writer));
      transformedString = writer.toString(); 
    } catch (Exception e) {
      throw new ApplicationException(e);      
    }
    return transformedString;
  }
  
  
  private Transformer getTransformer() throws FileNotFoundException, TransformerException {
    if (useTranslet) {
      return getTranslet();
    }
    return getXSLTransformer();
  }

 
  /**
   * Set articleService
   * @param articleService articleService
   */
  public void setArticleService(final ArticleWebService articleService) {
    this.articleService = articleService;
  }

  private TransformerFactory tFactory;
  private StreamSource source;

  /**
   * Get an XSL transformer.
   * @return Transformer
   * @throws TransformerException TransformerException
   */
  private Transformer getXSLTransformer() throws TransformerException {
    if (null == tFactory || null == source) {
      // 1. Instantiate a TransformerFactory.
      tFactory = TransformerFactory.newInstance();
      source = new StreamSource(xslTemplate);
    }

    // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
    return tFactory.newTransformer(source);
  }

  
  
  
  /**
   * Get a translet - a compiled stylesheet.
   * @return translet
   * @throws TransformerException TransformerException
   * @throws FileNotFoundException FileNotFoundException
   */
  public Transformer getTranslet() throws TransformerException, FileNotFoundException {
    if (null == translet) {
      // Instantiate the TransformerFactory, and use it with a StreamSource
      // XSL stylesheet to create a translet as a Templates object.
      final TransformerFactory tFactory = TransformerFactory.newInstance();
      translet = tFactory.newTemplates(new StreamSource(xslTemplate));
    }

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return translet.newTransformer();
  }
  
  
  /**
   * Get a translet - a compiled stylesheet - for the secondary objects.
   * @return translet
   * @throws TransformerException TransformerException
   * @throws FileNotFoundException FileNotFoundException
   */
  public Transformer getSecondaryObjectTranslet() throws TransformerException, FileNotFoundException {
    if (null == secondaryObjectTranslet) {
      // Instantiate the TransformerFactory, and use it with a StreamSource
      // XSL stylesheet to create a translet as a Templates object.
      final TransformerFactory tFactory = TransformerFactory.newInstance();
      secondaryObjectTranslet = tFactory.newTemplates(new StreamSource(secondaryObjectXslTemplate));
    }

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return secondaryObjectTranslet.newTransformer();
  }

  /**
   * Sets the xmlFactoryProperty
   * 
   * @param xmlFactoryProperty Map of xmlFactory Properties
   */
  public void setXmlFactoryProperty(final Map<String, String> xmlFactoryProperty) {
    this.xmlFactoryProperty = xmlFactoryProperty;
  }

  /** Set the XSL Template to be used for transformation
   * @param xslTemplate xslTemplate
   * @throws java.net.URISyntaxException URISyntaxException
   */
  public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    log.debug("XSL template location = " + file.getAbsolutePath());
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @throws URISyntaxException URISyntaxException
   * @return the local or remote file or url as a java.io.File
   */
  public File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      //access it as a local file resource
      return new File(FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Set the representation of the article that we want to work with
   * @param articleRep articleRep
   */
  public void setArticleRep(final String articleRep) {
    this.articleRep = articleRep;
  }

  /**
   * Whether or not use the translet
   * @param useTranslet true if useTranslet, false otherwise
   */
  public void setUseTranslet(final boolean useTranslet) {
    this.useTranslet = useTranslet;
  }

  /**
   * Return the annotated content as a DOMSource for a given articleUri
   * @param articleURI articleURI
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public Source getAnnotatedContentAsDOMSource(final String articleURI) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchIdException {
    //final DocumentBuilder builder = createDocBuilder();

   // final Document doc = builder.parse(getAnnotatedContentAsInputStream(articleURI));

    // Prepare the DOM source
    return new DOMSource(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI) throws ParserConfigurationException, 
                                    SAXException, IOException, URISyntaxException, 
                                    ApplicationException, NoSuchIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  /**
   * Get the xmlFileURL as a DOMSource.
   * @param xmlFileURL xmlFileURL
   * @return an instance of DOMSource
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.topazproject.common.NoSuchIdException NoSuchIdException
   */
  public Source getDOMSource(final String xmlFileURL) throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ApplicationException, NoSuchIdException {
    final DocumentBuilder builder = createDocBuilder();

    Document doc;
    try {
      doc = builder.parse(getAsFile(xmlFileURL));
    } catch (Exception e) {
      doc = builder.parse(xmlFileURL);
    }

    // Prepare the DOM source
    return new DOMSource(doc);
  }

  private DocumentBuilder createDocBuilder() throws ParserConfigurationException {
    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(CachedSource.getResolver());
    return builder;
  }

  private Document getAnnotatedContentAsDocument(final String infoUri) throws IOException,
          NoSuchIdException, ParserConfigurationException, SAXException, ApplicationException {
    final String contentUrl = articleService.getObjectURL(infoUri, articleRep);
    return getAnnotatedContentAsDocument(contentUrl, infoUri);
  }
  
  
  private Document getAnnotatedContentAsDocument(final String contentUrl, final String infoUri)
          throws IOException, ParserConfigurationException, ApplicationException {
    final AnnotationInfo[] annotations = annotationWebService.listAnnotations(infoUri);
    return applyAnnotationsOnContentAsDocument (contentUrl, annotations);
  }

  private Document applyAnnotationsOnContentAsDocument (final String contentUrl, 
                                                        final AnnotationInfo[] annotations)
          throws IOException, ParserConfigurationException, ApplicationException {
    
    final DataHandler content = new DataHandler(new URLDataSource(new URL(contentUrl)));
    final DocumentBuilder builder = createDocBuilder();
    if (annotations.length != 0) {
      return Annotator.annotateAsDocument(content, annotations, builder);
    }
    try {
      return builder.parse(content.getInputStream());
    } catch (Exception e){
      log.error(e, e);
      throw new ApplicationException("Applying annotations failed for resource:" + contentUrl, e);
    }
  }
  
 
  /**
   * Getter for AnnotatationWebService
   * 
   * @return the annotationWebService
   */
  public AnnotationWebService getAnnotationWebService() {
    return annotationWebService;
  }

  /**
   * Setter for annotationWebService
   * 
   * @param annotationWebService annotationWebService
   */
  public void setAnnotationWebService(final AnnotationWebService annotationWebService) {
    this.annotationWebService = annotationWebService;
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @param state  array of matching state values
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public ArrayList<String> getArticles(final String startDate, final String endDate, final int[] state) throws ApplicationException {
    final ArrayList<String> articles = new ArrayList<String>();

    try {
      final String articlesDoc = articleService.getArticles(startDate, endDate, state, true);

      // Create the builder and parse the file
      final Document articleDom = factory.newDocumentBuilder().parse(new InputSource(new StringReader(articlesDoc)));

      // Get the matching elements
      final NodeList nodelist = XPathAPI.selectNodeList(articleDom, "/articles/article/uri");

      for (int i = 0; i < nodelist.getLength(); i++) {
        final Element elem = (Element) nodelist.item(i);
        final String uri = elem.getTextContent();
        final String decodedArticleUri = URLDecoder.decode(uri, encodingCharset);
        articles.add(decodedArticleUri);
      }

      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public Collection<String> getArticles(final String startDate, final String endDate) throws ApplicationException {
	  return getArticles(startDate, endDate, null);
  }
  
  /**
   * Set the encoding charset
   * @param encodingCharset encodingCharset
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
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


  /**
   * @param secondaryObjectXslTemplate The secondaryObjectXslTemplate to set.
   */
  public void setSecondaryObjectXslTemplate(String secondaryObjectXslTemplate) throws URISyntaxException{
    File file = getAsFile(secondaryObjectXslTemplate);
    if (!file.exists()) {
      file = new File(secondaryObjectXslTemplate);
    }
    log.debug("secondary objectXSL template location = " + file.getAbsolutePath());
    this.secondaryObjectXslTemplate = file;
  }

  /**
   * @see org.plos.article.service.ArticleWebService#getObjectInfo(String)
   * @param articleURI articleURI
   * @return ObjectInfo
   * @throws ApplicationException ApplicationException
   */
  public ObjectInfo getArticleInfo(final String articleURI) throws ApplicationException {
    // do caching here rather than at articleWebService level because we want the cache key
    // and group to be article specific
    ObjectInfo artInfo;
    
    try {
      artInfo = (ObjectInfo)articleCacheAdministrator.getFromCache(articleURI + CACHE_KEY_ARTICLE_INFO); 
      if (log.isDebugEnabled()) {
        log.debug("retrieved objectInfo from cache for: " + articleURI);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        artInfo = articleService.getObjectInfo(articleURI);
        articleCacheAdministrator.putInCache(articleURI + CACHE_KEY_ARTICLE_INFO, artInfo, 
                                             new String[]{FileUtils.escapeURIAsPath(articleURI)});
        updated = true;
        if (log.isDebugEnabled()) {
          log.debug("retrieved objectInfo from TOPAZ for article URI: " + articleURI);
        }        
      } catch (RemoteException e) {
        if (log.isErrorEnabled()) {  
          log.error("Failed to get object info for article URI: " + articleURI, e);
        }
        throw new ApplicationException("Failed to get object info " + articleURI, e);
      } catch (NoSuchIdException nsie) {
        if (log.isErrorEnabled()) {  
          log.error("Failed to get object info for article URI: " + articleURI, nsie);
        }
        throw new ApplicationException("Failed to get object info " + articleURI, nsie);
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(articleURI + CACHE_KEY_ARTICLE_INFO);
      } 
    }
    return artInfo;
  }
}
