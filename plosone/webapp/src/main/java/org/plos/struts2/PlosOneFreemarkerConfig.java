/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;
import org.plos.web.VirtualJournalContextFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class to configure the FreeMarker templates with css and javascript files and the title of page.
 * Used so that we can have just one or two main templates and swap out the body section with
 * a Struts 2 result.
 *
 * @author Stephen Cheng
 */
public class PlosOneFreemarkerConfig {
  private static final Log log = LogFactory.getLog(PlosOneFreemarkerConfig.class);

  private static final String[] DEFAULT_CSS_FILES = {"/css/pone_iepc.css", "/css/pone_screen.css"};
  private static final String[] DEFAULT_JS_FILES = {"/javascript/all.js"};
  private static final String DEFAULT_TITLE = "Journal";

  private HashMap<String, JournalConfig> journals;
  private String dirPrefix;
  private String subdirPrefix;
  private String plosOneHost;
  private String casLoginURL;
  private String casLogoutURL;
  private String registrationURL;
  private String changePasswordURL;
  private String changeEmailURL;
  private String defaultJournalName;

  /**
   * Constructor that loads the list of css and javascript files and page titles for pages which
   * follow the standard templates.  Creates its own composite configuration by iterating over each
   * of the configs in the config to assemble a union of pages defined.
   *
   */
  public PlosOneFreemarkerConfig() {
    Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();
    if (log.isDebugEnabled()) {
      log.debug("Reading FreeMarker configuration");
    }
    dirPrefix = myConfig.getString("pub.app-context");
    subdirPrefix = myConfig.getString("pub.resource-sub-dir");
    plosOneHost = myConfig.getString("pub.host");
    casLoginURL = myConfig.getString("cas.url.login");
    casLogoutURL = myConfig.getString("cas.url.logout");
    registrationURL = myConfig.getString("plos-registration.url.registration");
    changePasswordURL = myConfig.getString("plos-registration.url.change-password");
    changeEmailURL = myConfig.getString("plos-registration.url.change-email");
    defaultJournalName = myConfig.getString("freemarker.default-journal-name");
    journals = new HashMap<String, JournalConfig>();

    int numConfigs = 1;
    if (myConfig instanceof CombinedConfiguration) {
      numConfigs = ((CombinedConfiguration)myConfig).getNumberOfConfigurations();
    }
    Configuration oneConfig;
    for (int c = 0; c < numConfigs; c++) {
      if (numConfigs > 1) {
        oneConfig = ((CombinedConfiguration)myConfig).getConfiguration(c);
      } else {
        oneConfig = myConfig;
      }
      int numJournals = oneConfig.getList("freemarker.journal.name").size();
      for (int k = 0; k < numJournals; k++) {
        final String journal = "freemarker.journal(" + k + ")";
        final String journalName = oneConfig.getString(journal + ".name");
        if (log.isDebugEnabled()) {
          log.debug("reading journal name: " + journalName);
        }
        JournalConfig jc = journals.get(journalName);
        if (jc == null) {
          if (log.isDebugEnabled()) {
            log.debug("journal Not found, creating: " + journalName);
          }
          jc = new JournalConfig();
          journals.put(journalName, jc);
        }

        if (jc.getDefaultTitle() == null) {
          final String title = oneConfig.getString(journal + ".default.title");
          if (title != null) {
            jc.setDefaultTitle(title);
          }
        }

        if (jc.getMetaDescription() == null) {
          final String metaDescription = oneConfig.getString(journal + ".metaDescription");
          if (metaDescription != null) {
            jc.setMetaDescription(metaDescription);
          }
        }

        if (jc.getMetaKeywords() == null) {
          final String metaKeywords= oneConfig.getString(journal + ".metaKeywords");
          if (metaKeywords != null) {
            jc.setMetaKeywords(metaKeywords);
          }
        }

        if (jc.getDisplayName() == null) {
          final String displayName = oneConfig.getString(journal + ".displayName");
          if (displayName != null) {
            jc.setDisplayName(displayName);
          }
        }

        if (jc.getArticleTitlePrefix() == null) {
          final String articleTitlePrefix= oneConfig.getString(journal + ".articleTitlePrefix");
          if (articleTitlePrefix != null) {
            jc.setArticleTitlePrefix(articleTitlePrefix);
          }
        }

        if (jc.getDefaultCss() == null) {
          final List fileList = oneConfig.getList(journal + ".default.css.file");
          String[] defaultCss;
          if (fileList.size() > 0) {
            defaultCss = new String[fileList.size()];
            Iterator iter = fileList.iterator();
            for (int i = 0; i < fileList.size(); i++) {
              defaultCss[i] = dirPrefix + subdirPrefix + (String)iter.next();
            }
            jc.setDefaultCss(defaultCss);
          }
        }

        if (jc.getDefaultJavaScript() == null) {
          final List fileList = oneConfig.getList(journal + ".default.javascript.file");
          String javascriptFile;
          String[] defaultJavaScript;
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
            jc.setDefaultJavaScript(defaultJavaScript);
          }
        }

        final int numPages = oneConfig.getList(journal + ".page.name").size();
        int numCss, numJavaScript, j;
        String pageName, page;

        HashMap<String, String> titles = jc.getTitles();
        if (titles == null) {
          titles = new HashMap<String, String>();
        }
        HashMap<String, String[]> cssFiles = jc.getCssFiles();
        if (cssFiles == null) {
          cssFiles = new HashMap<String, String[]>();
        }
        HashMap<String, String[]> javaScriptFiles = jc.getJavaScriptFiles();
        if (javaScriptFiles == null) {
          javaScriptFiles = new HashMap<String, String[]>();
        }

        String[] cssArray = null;
        String[] javaScriptArray = null;

        for (int i = 0; i < numPages; i++) {
          page = journal + ".page(" + i + ")";
          pageName = oneConfig.getString(page + ".name");
          if (log.isDebugEnabled())
            log.debug("Reading config for page name: " + pageName);

          if (!titles.containsKey(pageName)) {
            final String title = oneConfig.getString(page + ".title");
            if (title != null) {
              titles.put(pageName, title);
            }
          }

          if (!cssFiles.containsKey(pageName)) {
            Object obj = oneConfig.getProperty(page+".css");
            final boolean isDefined = (obj != null);
            numCss = oneConfig.getList(page + ".css.file").size();
            cssArray = new String[numCss];
            for (j = 0; j < numCss; j++) {
              cssArray[j] =  dirPrefix + subdirPrefix + oneConfig.getString(page + ".css.file(" + j + ")");
            }
            if ((numCss > 0) || (numCss == 0 && isDefined)) {
              cssFiles.put(pageName, cssArray);
            }
          }

          if (!javaScriptFiles.containsKey(pageName)) {
            Object obj = oneConfig.getProperty(page+".javascript");
            final boolean isDefined = (obj != null);
            numJavaScript = oneConfig.getList(page + ".javascript.file").size();
            javaScriptArray = new String[numJavaScript];

            for (j = 0; j < numJavaScript; j++) {
              String fileName = oneConfig.getString(page + ".javascript.file(" + j + ")");
              String filePath;
              if (fileName.endsWith(".ftl")) {
                filePath = subdirPrefix + fileName;
              } else {
                filePath = dirPrefix + subdirPrefix + fileName;
              }
              javaScriptArray[j] =  filePath;
            }
            if ((numJavaScript > 0) || (numJavaScript == 0 && isDefined)){
              javaScriptFiles.put(pageName, javaScriptArray);
            }
          }
        }
        jc.setCssFiles(cssFiles);
        jc.setJavaScriptFiles(javaScriptFiles);
        jc.setTitles(titles);
      }
    }

    processVirtualJournalConfig(myConfig);

    if (log.isTraceEnabled()){
      Set<Entry<String, JournalConfig>> allJournals = journals.entrySet();
      Iterator<Entry<String, JournalConfig>> iter = allJournals.iterator();
      while (iter.hasNext()) {
        Entry<String, JournalConfig> e = iter.next();
        JournalConfig j = e.getValue();
        log.trace("Journal: " + e.getKey());
        log.trace("Journal url: " + j.getUrl());
        log.trace("Default Title: " + j.getDefaultTitle());
        log.trace("Default CSS: " + printArray(j.getDefaultCss()));
        log.trace("Default JavaScript: " + printArray(j.getDefaultCss()));
        HashMap<String, String[]> map = j.getCssFiles();
        Set<String> pageNames = map.keySet();
        Iterator<String> pgIter = pageNames.iterator();
        while (pgIter.hasNext()) {
          String name = pgIter.next();
          log.trace("PageName: " + name);
          log.trace("CSS FILES: " + printArray(map.get(name)));
        }
        map = j.getJavaScriptFiles();
        pageNames = map.keySet();
        pgIter = pageNames.iterator();
        while (pgIter.hasNext()) {
          String name = pgIter.next();
          log.trace("PageName: " + name);
          log.trace("JS FILES: " + printArray(map.get(name)));
        }

        HashMap<String, String> m = j.getTitles();
        pageNames = m.keySet();
        pgIter = pageNames.iterator();
        while (pgIter.hasNext()) {
          String name = pgIter.next();
          log.trace("PageName: " + name);
          log.trace("Title: " + m.get(name));
        }
      }
      log.trace("Dir Prefix: " + dirPrefix);
      log.trace("SubDir Prefix: " + subdirPrefix);
      log.trace("Pub host: " + plosOneHost);
      log.trace("Cas url login: " + casLoginURL);
      log.trace("Case url logout: " + casLogoutURL);
      log.trace("Registration URL: " + registrationURL);
      log.trace("Registration Change Pass URL: " + changePasswordURL);
      log.trace("Registration Change EMail URL: " + changeEmailURL);
      log.trace("Default Journal Name: " + defaultJournalName);
    }
    if(log.isDebugEnabled()) {
      log.debug("End FreeMarker Configuration Reading");
    }
  }

  private String printArray(String[] in) {
    StringBuilder s = new StringBuilder();
    if (in != null) {
      for (String i : in) {
        s.append(i);
        s.append(", ");
      }
    }
    return s.toString();
  }

  private void processVirtualJournalConfig (Configuration configuration) {
    final Collection<String> virtualJournals = configuration.getList(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_JOURNALS);
    String defaultVirtualJournal = configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT + ".journal");
    JournalConfig jour = null;

    if ((defaultVirtualJournal != null) && (!"".equals(defaultVirtualJournal))) {
      jour = journals.get(defaultVirtualJournal);
      if (jour != null) {
        jour.setUrl(configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT +
                                            ".url"));
      }
    }

    final Iterator allJournals = virtualJournals.iterator();
    while(allJournals.hasNext()) {
      final String journalName = (String) allJournals.next();
      jour = journals.get(journalName);
      if (jour != null) {
        jour.setUrl(configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS +
                                            "." + journalName + ".url"));
      }
    }
  }

  /**
   * Gets the title for the given template and journal name.
   * Return the default value if not defined
   *
   * @param templateName
   * @param journalName
   * @return Returns the title given a template name.
   */
  public String getTitle(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getTitles().get(templateName);
    if (retVal == null) {
      retVal = jc.getDefaultTitle();
      if ((retVal == null) && !usingDefault) {
        jc = journals.get(defaultJournalName);
        retVal = jc.getTitles().get(templateName);
        if (retVal == null) {
          retVal = jc.getDefaultTitle();
        }
      }
    }
    return retVal != null ? retVal : DEFAULT_TITLE;
  }

  /**
   * Gets title for page defined in templateName and uses the defaultJournal name
   *
   * @param templateName
   * @return page title
   */
  public String getTitle(String templateName) {
    return getTitle (templateName, defaultJournalName);
  }

  /**
   * Gets the array of CSS files associated with templateName and journalName
   * or returns the default values if not available.
   *
   * @param templateName
   * @param journalName
   * @return Returns list of css files given a template name.
   */
  public String[] getCss(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String[] retVal = jc.getCssFiles().get(templateName);
    if (retVal == null) {
      retVal = jc.getDefaultCss();
      if ((retVal == null) && !usingDefault) {
        jc = journals.get(defaultJournalName);
        retVal = jc.getCssFiles().get(templateName);
        if (retVal == null) {
          retVal = jc.getDefaultCss();
        }
      }
    }
    return retVal != null ? retVal : DEFAULT_CSS_FILES;
  }

  /**
   * Retrieves css files for given page in the default journal
   *
   * @param templateName
   * @return array of css filename for the page
   */
  public String[] getCss (String templateName){
    return getCss(templateName, defaultJournalName);
  }

  /**
   * Gets the array of JavaScript files associated with templateName and journalName
   * or returns the default values if not available.
   *
   * @param templateName
   * @param journalName
   * @return Returns the list of JavaScript files given a template name.
   */
  public String[] getJavaScript(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String[] retVal = jc.getJavaScriptFiles().get(templateName);
    if (retVal == null) {
      retVal = jc.getDefaultJavaScript();
      if ((retVal == null) && !usingDefault) {
        jc = journals.get(defaultJournalName);
        retVal = jc.getJavaScriptFiles().get(templateName);
        if (retVal == null) {
          retVal = jc.getDefaultJavaScript();
        }
      }
    }
    return retVal != null ? retVal : DEFAULT_JS_FILES;
  }

  /**
   * Gets the array of javascript files for the default journal and the specificed page name
   *
   * @param templateName
   * @return list of javascript files for the given page
   */
  public String[] getJavaScript (String templateName){
    return getJavaScript (templateName, defaultJournalName);
  }

  /**
   * Gets meta keywords for journal
   *
   * @param journalName
   * @return meta keywords
   */
  public String getMetaKeywords(String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getMetaKeywords();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getMetaKeywords();
    }
    return retVal != null ? retVal : "";
  }

  /**
   * gets meta description for journal
   *
   * @param journalName
   * @return meta description
   */
  public String getMetaDescription(String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getMetaDescription();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getMetaDescription();
    }
    return retVal != null ? retVal : "";
  }

  /**
   * Gets display name for journal
   *
   * @param journalName
   * @return display name
   */
  public String getDisplayName(String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getDisplayName();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getDisplayName();
    }
    return retVal != null ? retVal : "";
  }


  /**
   * gets prefix for article title
   *
   * @param journalName
   * @return article title prefix
   */
  public String getArticleTitlePrefix (String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getArticleTitlePrefix();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getArticleTitlePrefix();
    }
    return retVal != null ? retVal : "";
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

  /**
   * @return Returns the journalContextAttributeKey
   */
  public String getJournalContextAttributeKey() {
    return org.plos.web.VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT;
  }

  /**
   * @return Returns the user attribute key
   */
  public String getUserAttributeKey() {
    return org.plos.Constants.PLOS_ONE_USER_KEY;
  }


  /**
   * Returns the URL for a given journal given its key
   *
   * @param journalKey
   * @return URL of journal
   */
  public String getJournalUrl (String journalKey) {
    JournalConfig jc = journals.get(journalKey);
    String url = "";
    if (jc != null) {
      url = jc.getUrl();
    }
    return url;
  }


  private class JournalConfig {
    private HashMap<String, String[]> cssFiles;
    private HashMap<String, String[]> javaScriptFiles;
    private HashMap<String, String> titles;

    private String[] defaultCss;
    private String[] defaultJavaScript;
    private String defaultTitle;

    private String metaKeywords;
    private String metaDescription;
    private String articleTitlePrefix;
    private String displayName;
    private String url;

    public JournalConfig () {
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
     * @return Returns the defaultCss.
     */
    public String[] getDefaultCss() {
      return defaultCss;
    }
    /**
     * @param defaultCss The defaultCss to set.
     */
    public void setDefaultCss(String[] defaultCss) {
      this.defaultCss = defaultCss;
    }
    /**
     * @return Returns the defaultJavaScript.
     */
    public String[] getDefaultJavaScript() {
      return defaultJavaScript;
    }
    /**
     * @param defaultJavaScript The defaultJavaScript to set.
     */
    public void setDefaultJavaScript(String[] defaultJavaScript) {
      this.defaultJavaScript = defaultJavaScript;
    }
    /**
     * @return Returns the defaultTitle.
     */
    public String getDefaultTitle() {
      return defaultTitle;
    }
    /**
     * @param defaultTitle The defaultTitle to set.
     */
    public void setDefaultTitle(String defaultTitle) {
      this.defaultTitle = defaultTitle;
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
     * @return Returns the articleTitlePrefix.
     */
    public String getArticleTitlePrefix() {
      return articleTitlePrefix;
    }
    /**
     * @param articleTitlePrefix The articleTitlePrefix to set.
     */
    public void setArticleTitlePrefix(String articleTitlePrefix) {
      this.articleTitlePrefix = articleTitlePrefix;
    }
    /**
     * @return Returns the metaDescription.
     */
    public String getMetaDescription() {
      return metaDescription;
    }
    /**
     * @param metaDescription The metaDescription to set.
     */
    public void setMetaDescription(String metaDescription) {
      this.metaDescription = metaDescription;
    }
    /**
     * @return Returns the metaKeywords.
     */
    public String getMetaKeywords() {
      return metaKeywords;
    }
    /**
     * @param metaKeywords The metaKeywords to set.
     */
    public void setMetaKeywords(String metaKeywords) {
      this.metaKeywords = metaKeywords;
    }
    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
      return displayName;
    }
    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl() {
      return url;
    }

    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
      this.url = url;
    }
  }
}
