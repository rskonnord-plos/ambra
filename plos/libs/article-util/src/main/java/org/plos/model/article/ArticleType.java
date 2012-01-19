/* $HeadURL: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/webapp/src/main/java/org/plos/model/article/ArticleType.java $
 * $Id: ArticleType.java 5139 2008-03-21 23:17:26Z jkirton $ 
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.model.article;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.plos.configuration.ConfigurationStore;

@SuppressWarnings("serial")
public class ArticleType implements Serializable {
  /**
   * The article heading denoting a research type article.
   */
  public static final String ARTICLE_TYPE_HEADING_RESEARCH = "Research Article";

  // private static final Log log = LogFactory.getLog(ArticleType.class);

  private static HashMap<String, ArticleType> _knownArticleTypes = new HashMap<String, ArticleType>();
  private static List<ArticleType> _articleTypeOrder = new ArrayList<ArticleType>();
  private static HashMap<String, ArticleType> _newArticleTypes = new HashMap<String, ArticleType>();
  private static ArticleType theDefaultArticleType = null;
  static {
    configureArticleTypes(ConfigurationStore.getInstance().getConfiguration());
  }

  private final URI uri;
  private final String heading;
  private String imageConfigName;

  private ArticleType(URI articleTypeUri, String displayHeading) {
    uri = articleTypeUri;
    heading = displayHeading;
  }

  /**
   * Returns an ArticleType if configured in defaults.xml (etc) or null otherwise
   * @param uri
   * @return
   */
  public static ArticleType getKnownArticleTypeForURI(URI uri) {
    return _knownArticleTypes.get(uri.toString());
  }
  
  /**
   * Searches the known article types for one whose heading matches the one given.
   * <p><strong>NOTE: </strong>There is no guarantee article type headings are unique 
   * and this method simply returns the first encountered match. 
   * @param heading The sought heading 
   * @return ArticleType or <code>null</code> if not found.
   */
  public static ArticleType getKnownArticleTypeForHeading(String heading) {
    if(heading != null) {
      for(ArticleType at : _knownArticleTypes.values()) {
        if(heading.equals(at.heading)) {
          return at;
        }
      }
    }
    return null;
  }

  /**
   * Returns an ArticleType object for the given URI. If one does not exist for that URI and
   * createIfAbsent is true, a new ArticleType shall be created and added to a list of types 
   * (although shall not be recognized as an official ArticleType by getKnownArticleTypeForURI). 
   * If createIfAbsent is false, an ArticleType shall not be created and null shall be returned. 
   * @param uri
   * @param createIfAbsent
   * @return The ArticleType for the given URI
   */
  public static ArticleType getArticleTypeForURI(URI uri, boolean createIfAbsent) {
    ArticleType at = uri == null ? null : _knownArticleTypes.get(uri.toString());
    if (at == null) {
      at = _newArticleTypes.get(uri.toString());
      if ((at == null) && createIfAbsent) {
        String uriStr = uri.toString();
        if (uriStr.contains("/")) {
          uriStr = uriStr.substring(uriStr.indexOf('/'));
          try {
            uriStr = URLDecoder.decode(uriStr, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            // ignore and just use encoded uriStr :(
          }
          at = new ArticleType(uri, uriStr);
          _newArticleTypes.put(uri.toString(), at);
        }
      }
    }
    return at;
  }
  
  /**
   * Ensures that the same object instance is used for identical URIs. 
   * The readResolve() method is called when deserializing an object. ArticleType objects
   * are serialized and deserialized when propagated over the ehcache from one VM to another. 
   * @return
   * @throws java.io.ObjectStreamException
   */
  private Object readResolve() throws java.io.ObjectStreamException
  {
    return getArticleTypeForURI(this.uri, true);
  }

  public static ArticleType addArticleType(URI uri, String heading) {
    if (_knownArticleTypes.containsKey(uri.toString())) {
      return _knownArticleTypes.get(uri.toString());
    }
    ArticleType at = new ArticleType(uri, heading);
    _knownArticleTypes.put(uri.toString(), at);
    _articleTypeOrder.add(at);
    return at;
  }

  public URI getUri() {
    return uri;
  }

  public String getHeading() {
    return heading;
  }

  /**
   * Returns an unmodifiable ordered list of known ArticleTypes as read in order from the configuration
   * in configureArticleTypes(). 
   * 
   * @return Collection of ArticleType(s)
   */
  public static List<ArticleType> getOrderedListForDisplay() {
    return Collections.unmodifiableList(_articleTypeOrder);
  }

  /**
	 * Read in the ArticleTypes from the pubApp configuration (hint: normally defined in defauls.xml) 
	 * and add them to the list of known ArticleType(s). The order of article types found in the 
	 * configuration is significant and is returned in a Collection from getOrderedListForDisplay(). 
	 * The defaultArticleType is set to the first article type defined unless configured explicitly. 
	 */
	public static void configureArticleTypes(Configuration myConfig) {
    int count = 0;
    String basePath = "pub.articleTypeList.articleType";
    String uriStr;
    String headingStr;
    // Iterate through the defined article types. This is ugly since the index needs 
    // to be given in xpath format to access the element, so we calculate a base string
    // like: pub.articleTypeList.articleType(x) and check if it's non-null for typeUri
    do {
      String baseString = (new StringBuffer(basePath).append("(").append(count)
          .append(").")).toString();
      uriStr = myConfig.getString(baseString + "typeUri");
      headingStr = myConfig.getString(baseString + "typeHeading");
      if ((uriStr != null) && (headingStr != null)) {
        ArticleType at = addArticleType(URI.create(uriStr), headingStr);
        if (("true".equalsIgnoreCase(myConfig.getString(baseString + "default"))) || 
            (theDefaultArticleType == null)) {
          theDefaultArticleType = at;
        }
        at.setImageSetConfigName(myConfig.getString(baseString + "imageSetConfigName"));
      }
    	count++;
    } while (uriStr != null);
	}

	public void setImageSetConfigName(String imgConfigName) {
	  this.imageConfigName = imgConfigName;
  }
	
  public String getImageSetConfigName() {
    return imageConfigName;
  }

  public static ArticleType getDefaultArticleType() {
    return theDefaultArticleType;
  }
  
  /**
   * Is the given {@link ArticleType} research related?
   * @param articleType
   * @return true/false
   */
  public static boolean isResearchArticle(ArticleType articleType) {
    return articleType == null ? false : ARTICLE_TYPE_HEADING_RESEARCH.equals(articleType.getHeading());
  }

  /**
   * Is the collection of {@link ArticleType}s research related?
   * <p>
   * This method returns <code>true</code> when one occurrence of {@link ArticleType#ARTICLE_TYPE_HEADING_RESEARCH}
   * is encountered.
   * 
   * @param articleTypeURIs
   * @return true/false
   */
  public static boolean isResearchArticle(Collection<ArticleType> articleTypes) {
    if(articleTypes != null) {
      for(ArticleType at : articleTypes) {
        if(isResearchArticle(at)) return true;
      }
    }
    return false;
  }
  
  /**
   * Override equals() to verify if the the compared ArticleType is equal to this instance. 
   * If super.equals() returns false, we compare the uri. Note that readResolve() is implemented
   * above so that the super.equals() object-identity comparison should succeed if this
   * object was deserialized. This implementation is a safety net in case that fails. 
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    }
    if (obj instanceof ArticleType) {
      if (this.getUri() != null) {
        return (this.getUri().equals(((ArticleType)obj).getUri()));
      }
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getUri()).hashCode();
  }
  
  @Override
  public String toString() {
    return heading;
  }
}
