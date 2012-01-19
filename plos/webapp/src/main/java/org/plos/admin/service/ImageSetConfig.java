/* $HeadURL$
 * $Id$ 
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.admin.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;

public class ImageSetConfig {
  private static final Log log = LogFactory.getLog(ImageSetConfig.class);
  private static HashMap<String,ImageSetConfig> _imageSets = new HashMap<String, ImageSetConfig>();
  // This map stores Properties defined for each type of image used in an article
  private HashMap<String, Properties> imagePropertyMap = new HashMap<String, Properties>();
  private String name;
  // Keys to each type of image property map
  public static final String SMALL_IMAGE_PROP_KEY = "small";
  public static final String MEDIUM_IMAGE_PROP_KEY = "medium";
  public static final String LARGE_IMAGE_PROP_KEY = "large";
  // Keys to known image processing properties stored in each imagePropertyMap
  public static final String IMAGE_PROP_KEY_QUALITY = "quality";
  public static final String IMAGE_PROP_KEY_MAX_DIMENSION = "maxDimension";
  public static final String IMAGE_PROP_KEY_WIDTH = "width";

  // Set up a map of default image processing properties to be used if not defined 
  private static final String DEFAULT_IMAGE_SET_NAME = "#default";
  static {
    ImageSetConfig defaultImageSet = new ImageSetConfig(DEFAULT_IMAGE_SET_NAME);
    Properties defaultSmallProps = new Properties();
    defaultSmallProps.put(IMAGE_PROP_KEY_WIDTH, "70");
    defaultSmallProps.put(IMAGE_PROP_KEY_QUALITY, "70");
    defaultImageSet.addImageProperties(SMALL_IMAGE_PROP_KEY, defaultSmallProps);
    
    Properties defaultMediumProps = new Properties();
    defaultMediumProps.put(IMAGE_PROP_KEY_MAX_DIMENSION, "600");
    defaultMediumProps.put(IMAGE_PROP_KEY_QUALITY, "80");
    defaultImageSet.addImageProperties(MEDIUM_IMAGE_PROP_KEY, defaultMediumProps);
    
    Properties defaultLargeProps = new Properties();
    defaultLargeProps.put(IMAGE_PROP_KEY_QUALITY, "90");
    defaultImageSet.addImageProperties(LARGE_IMAGE_PROP_KEY, defaultLargeProps);
  }
  
  static {
    configureImageSets(ConfigurationStore.getInstance().getConfiguration());
  }
  
  public static ImageSetConfig getImageSetConfig(String name) {
    return _imageSets.get(name);
  }
  
  public void addImageProperties(String key, Properties props) {
    imagePropertyMap.put(key, props);
  }

  public static ImageSetConfig getDefaultImageSetConfig() {
    return _imageSets.get(DEFAULT_IMAGE_SET_NAME);
  }

  public String getName() {
    return name;
  }
  
  /**
   * Returns the image property for propertyName as defined in defaults.xml to be used 
   * for this ImageSetConfig.  
   * If not defined, attempt to use property defined for default ImageSetConfig. 
   * If not defined there, null is returned. 
   * 
   * @return fixed width size of small images to use with this ArticleType
   */
  public String getImageProperty(String imageType, String propertyName) {
    Properties props = imagePropertyMap.get(imageType);
    if ( (props != null) && (props.containsKey(propertyName)) ) {
      return props.getProperty(propertyName);
    }
    
    ImageSetConfig defaultSet = getDefaultImageSetConfig();
    if ((defaultSet != null) && (this != defaultSet)) {
      return defaultSet.getImageProperty(imageType, propertyName);
    }
    
    return null;
  }
  
  protected ImageSetConfig(String configName) {
    this.name = configName;
    _imageSets.put(configName, this);
  }

  private static void configureImageSets(Configuration myConfig) {
    int count = 0;
    String basePath = "topaz.utilities.image-magick.imageSetConfigs.imageSet";
    String imageSetName;
    do {
      String baseString = basePath+"("+count+").";
      imageSetName = myConfig.getString(baseString + "[@name]");
      if (imageSetName != null) {
        ImageSetConfig newConfig = new ImageSetConfig(imageSetName);
        
        newConfig.addImageProperties(SMALL_IMAGE_PROP_KEY, getAttributesOfConfigElement(myConfig, baseString + "small"));
        newConfig.addImageProperties(MEDIUM_IMAGE_PROP_KEY, getAttributesOfConfigElement(myConfig, baseString + "medium"));
        newConfig.addImageProperties(LARGE_IMAGE_PROP_KEY, getAttributesOfConfigElement(myConfig, baseString + "large"));
      }
      count++;
    } while (imageSetName != null);
  }

  /**
   * Extracts all the attributes found as the given XPath xml element from the dreadfully documented and 
   * unusable Apache Configuration object. Perhaps this method should be called suckBloodFromStone() :)
   * 
   * @param myConfig
   * @param baseString
   * @return
   */
  public static Properties getAttributesOfConfigElement(Configuration myConfig, String baseString) {
    Iterator<String> iter = myConfig.getKeys(baseString);
    Properties attributeProps = new Properties();
    while (iter.hasNext()) {
      String propKey = iter.next();
      int start = baseString.length();
      if (propKey.substring(start).startsWith("[@")) {
        int end = propKey.indexOf("]", start);
        if ((end > -1)) {
          String propName = propKey.substring(start+2, end);
          attributeProps.put(propName, myConfig.getString(propKey));
        }
      }
    }
    return attributeProps;
  }
  
}
