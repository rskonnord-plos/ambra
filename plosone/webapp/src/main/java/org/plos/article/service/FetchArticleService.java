/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article.service;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.annotation.service.AnnotationInfo;
import org.plos.annotation.service.AnnotationWebService;
import org.plos.annotation.service.Annotator;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.models.Article;
import org.plos.util.CacheAdminHelper;
import org.plos.util.FileUtils;
import org.plos.util.TextUtils;
import org.plos.util.ArticleXMLUtils;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;


/**
 * Fetch article service
 */
public class FetchArticleService {

  private String encodingCharset;
  private ArticleXMLUtils articleXmlUtils;

  private static final Log log = LogFactory.getLog(FetchArticleService.class);
  private AnnotationWebService annotationWebService;

  private GeneralCacheAdministrator articleCacheAdministrator;

  private static final String CACHE_KEY_ARTICLE_INFO = "CACHE_KEY_ARTICLE_INFO";

  private String getTransformedArticle(final String articleURI) throws ApplicationException {
    try {
      return articleXmlUtils.getTransformedDocument(getAnnotatedContentAsDocument(articleURI));
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error ("Could not transform article: " + articleURI, e);
      }
      if (e instanceof ApplicationException) {
        throw (ApplicationException)e;
      } else {
        throw new ApplicationException (e);
      }
    }
  }

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.plos.ApplicationException ApplicationException
   * @throws java.rmi.RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public String getURIAsHTML(final String articleURI) throws ApplicationException,
                          RemoteException, NoSuchArticleIdException {
    String escapedURI = FileUtils.escapeURIAsPath(articleURI);

    Object res = CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                               articleURI/* + topazUserId*/, -1,
                                               new String[] { escapedURI }, "transformed article",
                                               new CacheAdminHelper.CacheUpdater<Object>() {
        public Object lookup(boolean[] updated) {
          try {
            String a = getTransformedArticle(articleURI);
            updated[0] = true;
            return a;
          } catch (Exception e) {
            return e;
          }
        }
      }
    );

    if (res instanceof ApplicationException)
      throw (ApplicationException) res;
    if (res instanceof NoSuchArticleIdException)
      throw (NoSuchArticleIdException) res;
    if (res instanceof RemoteException)
      throw (RemoteException) res;
    if (res instanceof RuntimeException)
      throw (RuntimeException) res;
    return (String) res;
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
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI)
      throws ParserConfigurationException, SAXException, IOException, URISyntaxException,
             ApplicationException, NoSuchArticleIdException,TransformerException{
    return TextUtils.getAsXMLString(getAnnotatedContentAsDocument(articleURI));
  }

  private Document getAnnotatedContentAsDocument(final String infoUri)
      throws IOException, NoSuchArticleIdException, ParserConfigurationException, SAXException,
             ApplicationException {
    final String contentUrl;
    try {
      contentUrl = articleXmlUtils.getArticleService().
                                   getObjectURL(infoUri, articleXmlUtils.getArticleRep());
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(infoUri,
                                         "(representation=" + articleXmlUtils.getArticleRep() + ")",
                                         ex);
    }

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
    final DocumentBuilder builder = articleXmlUtils.createDocBuilder();
    if (annotations.length != 0) {
      return Annotator.annotateAsDocument(content, annotations, builder);
    }
    try {
      return builder.parse(content.getInputStream());
    } catch (Exception e){
      if (log.isErrorEnabled()) {
        log.error("Could not apply annotations to article: " + contentUrl, e);
      }
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
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state)
      throws ApplicationException {
    try {
      return articleXmlUtils.getArticleService().getArticleIds(startDate, endDate, state, true);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
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
   * @param articleXmlUtils The articleXmlUtils to set.
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }

  /**
   * @param articleURI articleURI
   * @return Article
   * @throws ApplicationException ApplicationException
   * @see ArticleOtmService#Article(java.net.URI)
   */
  public Article getArticleInfo(final String articleURI) throws ApplicationException {
    // do caching here rather than at articleOtmService level because we want the cache key
    // and group to be article specific
    Article artInfo = CacheAdminHelper.getFromCache(articleCacheAdministrator,
                                             articleURI + CACHE_KEY_ARTICLE_INFO, -1,
                                             new String[] { FileUtils.escapeURIAsPath(articleURI) },
                                             "objectInfo",
                                             new CacheAdminHelper.CacheUpdater<Article>() {
        public Article lookup(boolean[] updated) {
          try {
            Article artInfo = articleXmlUtils.getArticleService().getArticle(new URI(articleURI));
            updated[0] = true;
            if (log.isDebugEnabled())
              log.debug("retrieved objectInfo from TOPAZ for article URI: " + articleURI);
            return artInfo;
          } catch (NoSuchArticleIdException nsaie) {
            if (log.isErrorEnabled())
              log.error("Failed to get object info for article URI: " + articleURI, nsaie);
            return null;
          } catch (URISyntaxException use) {
            throw new RuntimeException("article uri '" + articleURI + "' is not a valid URI", use);
          }
        }
      }
    );

    if (artInfo == null)
      throw new ApplicationException("Failed to get object info " + articleURI);

    return artInfo;
  }
}
