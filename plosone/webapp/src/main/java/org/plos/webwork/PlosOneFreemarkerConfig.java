/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.webwork;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class to configure the FreeMarker templates with css and javascript files and the title of page.
 * Used so that we can have just one or two main templates and swap out the body section with
 * a Webwork result.
 * 
 * 
 * @author Stephen Cheng
 *
 */
public class PlosOneFreemarkerConfig {
  private static final Log log = LogFactory.getLog(PlosOneFreemarkerConfig.class);
  
  private HashMap<String, String[]> cssFiles;
  private HashMap<String, String[]> javaScriptFiles;
  private HashMap<String, String> titles;
  
  private String[] defaultCss;
  private String[] defaultJavaScript;
  private String defaultTitle;
  
  private String dirPrefix;
  private String subdirPrefix;
  
  private String plosOneHost;
  private String casLoginURL;
  private String casLogoutURL;
  private String registrationURL;
  private String changePasswordURL;
  private String changeEmailURL;

  private static final String[] DEFAULT_CSS_FILES = {"/css/pone_iepc.css", "/css/pone_screen.css"};
  private static final String[] DEFAULT_JS_FILES = {"/javascript/all.js"};
  private static final String DEFAULT_TITLE = "PLoS ONE";

  /**
   * Constructor that loads the list of css and javascript files and page titles for pages which
   * follow the standard templates.  
   * 
   */
  public PlosOneFreemarkerConfig() {
    Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();
    dirPrefix = myConfig.getString("plosone.app-context");
    subdirPrefix = myConfig.getString("plosone.resource-sub-dir");
    plosOneHost = myConfig.getString("plosone.host");
    casLoginURL = myConfig.getString("cas.url.login");
    casLogoutURL = myConfig.getString("cas.url.logout");
    registrationURL = myConfig.getString("plos-registration.url.registration");
    changePasswordURL = myConfig.getString("plos-registration.url.change-password");
    changeEmailURL = myConfig.getString("plos-registration.url.change-email");
    
    String title = myConfig.getString("default.title");
    if (title != null) {
      defaultTitle = title;
    } else {
      defaultTitle = DEFAULT_TITLE;
    }
    
    List fileList = myConfig.getList("default.css.file");
    if (fileList.size() > 0) {
      defaultCss = new String[fileList.size()];
      Iterator iter = fileList.iterator();
      for (int i = 0; i < fileList.size(); i++) {
        defaultCss[i] = dirPrefix + subdirPrefix + (String)iter.next();
      }
    } else {
      defaultCss = DEFAULT_CSS_FILES;
    }
    
    fileList = myConfig.getList("default.javascript.file");
    String javascriptFile;
    if (fileList.size() > 0) {
      defaultJavaScript = new String[fileList.size()];
      Iterator iter = fileList.iterator();
      for (int i = 0; i < fileList.size(); i++) {
        javascriptFile = (String)iter.next();
    	if (javascriptFile.endsWith(".ftl")) {
    	  defaultJavaScript[i] = subdirPrefix + javascriptFile;
        } else {
          defaultJavaScript[i] = dirPrefix + subdirPrefix +javascriptFile;
        }
      }
    } else {
      defaultJavaScript = DEFAULT_JS_FILES;
    }
    
    int numPages = myConfig.getList("page.name").size();
    int numCss, numJavaScript, j;
    String pageName, page;
    titles = new HashMap<String, String>();
    cssFiles = new HashMap<String, String[]>();
    javaScriptFiles = new HashMap<String, String[]>();
    

    String[] cssArray = null;
    String[] javaScriptArray = null;
    
    for (int i = 0; i < numPages; i++) {
      page = "page(" + i + ")";
      pageName = myConfig.getString(page + ".name");
      if (log.isDebugEnabled()){
        log.debug("Reading config for page name: " + pageName);
      }
      titles.put(pageName, myConfig.getString(page + ".title"));
      numCss = myConfig.getList(page + ".css.file").size();
      numJavaScript = myConfig.getList(page + ".javascript.file").size();
      cssArray = new String[numCss];
      javaScriptArray = new String[numJavaScript];
      for (j = 0; j < numCss; j++) {
        cssArray[j] =  dirPrefix + subdirPrefix + myConfig.getString(page + ".css.file(" + j + ")");
      }
      if (numCss > 0) {
        cssFiles.put(pageName, cssArray);
      }
      
      for (j = 0; j < numJavaScript; j++) {
        String fileName = myConfig.getString(page + ".javascript.file(" + j + ")");
        String filePath;
        if (fileName.endsWith(".ftl")) {
          filePath = subdirPrefix + fileName;
        } else {
          filePath = dirPrefix + subdirPrefix + fileName;
        }
        javaScriptArray[j] =  filePath;
      }
      if (numJavaScript > 0) {
        javaScriptFiles.put(pageName, javaScriptArray);
      }
    }
    if (log.isDebugEnabled()){
      log.debug("End PlosOne Configuration Reading");
    }
  
  }
  
  /**
   * Gets the title for the given template name. Return the default PLoS ONE if not defined
   * 
   * @param templateName 
   * @return Returns the title given a template name.
   */
  public String getTitle(String templateName) {
    String retVal = titles.get(templateName);
    if (retVal == null) {
      return defaultTitle;
    } else {
      return retVal;
    }
  }


  /**
   * Gets the array of CSS files associated with templateName or returns the default values
   * if not available.
   * 
   * @param templateName
   * @return Returns list of css files given a template name.
   */  
  public String[] getCss(String templateName) {
    String[] retVal = cssFiles.get(templateName);
    if (retVal == null) {
      return defaultCss;
    } else {
      return retVal;
    }
  }

  /**
   * Gets the array of JavaScript files associated with templateName or returns the default values
   * if not available.
   * 
   * @param templateName 
   * @return Returns the list of JavaScript files given a template name.
   */
  public String[] getJavaScript(String templateName) {
    String[] retVal = javaScriptFiles.get(templateName);
    if (retVal == null) {
      return defaultJavaScript;
    } else {
      return retVal;
    }
  }
  
  public String getContext() {
    return dirPrefix + subdirPrefix;
  }
  
  /**
   * @return Returns the dirPrefix.
   */
  public String getDirPrefix() {
    return dirPrefix;
  }

  /**
   * @param dirPrefix The dirPrefix to set.
   */
  public void setDirPrefix(String dirPrefix) {
    this.dirPrefix = dirPrefix;
  }

  /**
   * @return Returns the subdirPrefix.
   */
  public String getSubdirPrefix() {
    return subdirPrefix;
  }

  /**
   * @param subdirPrefix The subdirPrefix to set.
   */
  public void setSubdirPrefix(String subdirPrefix) {
    this.subdirPrefix = subdirPrefix;
  }

  /**
   * @return Returns the cssFiles.
   */
  public HashMap<String, String[]> getCssFiles() {
    return cssFiles;
  }

  /**
   * @param cssFiles The cssFiles to set.
   */
  public void setCssFiles(HashMap<String, String[]> cssFiles) {
    this.cssFiles = cssFiles;
  }

  /**
   * @return Returns the javaScriptFiles.
   */
  public HashMap<String, String[]> getJavaScriptFiles() {
    return javaScriptFiles;
  }

  /**
   * @param javaScriptFiles The javaScriptFiles to set.
   */
  public void setJavaScriptFiles(HashMap<String, String[]> javaScriptFiles) {
    this.javaScriptFiles = javaScriptFiles;
  }

  /**
   * @return Returns the titles.
   */
  public HashMap<String, String> getTitles() {
    return titles;
  }

  /**
   * @param titles The titles to set.
   */
  public void setTitles(HashMap<String, String> titles) {
    this.titles = titles;
  }

  /**
   * @return Returns the casLoginURL.
   */
  public String getCasLoginURL() {
    return casLoginURL;
  }

  /**
   * @param casLoginURL The casLoginURL to set.
   */
  public void setCasLoginURL(String casLoginURL) {
    this.casLoginURL = casLoginURL;
  }

  /**
   * @return Returns the plosOneHostname.
   */
  public String getPlosOneHost() {
    return plosOneHost;
  }

  /**
   * @param plosOneHost The plosOneHostname to set.
   */
  public void setPlosOneHost( String plosOneHost) {
    this.plosOneHost = plosOneHost;
  }

  /**
   * @return Returns the casLogoutURL.
   */
  public String getCasLogoutURL() {
    return casLogoutURL;
  }

  /**
   * @param casLogoutURL The casLogoutURL to set.
   */
  public void setCasLogoutURL(String casLogoutURL) {
    this.casLogoutURL = casLogoutURL;
  }

  /**
   * @return Returns the registrationURL.
   */
  public String getRegistrationURL() {
    return registrationURL;
  }

  /**
   * @param registrationURL The registrationURL to set.
   */
  public void setRegistrationURL(String registrationURL) {
    this.registrationURL = registrationURL;
  }

  /**
   * Getter for changePasswordURL.
   * @return Value of changePasswordURL.
   */
  public String getChangePasswordURL() {
    return changePasswordURL;
  }

  /**
   * Setter for changePasswordURL.
   * @param changePasswordURL Value to set for changePasswordURL.
   */
  public void setChangePasswordURL(final String changePasswordURL) {
    this.changePasswordURL = changePasswordURL;
  }

  /**
   * @return Returns the changeEmailURL.
   */
  public String getChangeEmailURL() {
    return changeEmailURL;
  }

  /**
   * @param changeEmailURL The changeEmailURL to set.
   */
  public void setChangeEmailURL(String changeEmailURL) {
    this.changeEmailURL = changeEmailURL;
  }
}
