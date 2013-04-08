/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.freemarker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ambraproject.web.VirtualJournalContextFilter;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * Class to configure the FreeMarker templates with css and javascript files and the title of page. Used so that we can
 * have just one or two main templates and swap out the body section with a Struts 2 result.
 *
 * @author Stephen Cheng
 */
public class AmbraFreemarkerConfig {
  private static final Logger log = LoggerFactory.getLogger(AmbraFreemarkerConfig.class);

  private static final ImmutableList<String> DEFAULT_CSS_FILES = ImmutableList.of("/css/iepc.css", "/css/screen.css");
  private static final ImmutableList<String> DEFAULT_JS_FILES = ImmutableList.of("/javascript/all.js");
  private static final String DEFAULT_TITLE = "Journal";
  private static final String DEFAULT_JOURNAL_NAME_CONFIG_KEY = "ambra.virtualJournals.default";
  private static final int DEFAULT_TEMPLATE_UPDATE_DELAY = 600;
  private static final int DEFAULT_TEMPLATE_CACHE_STRONG = 350;
  private static final int DEFAULT_TEMPLATE_CACHE_SOFT = 100;

  private final boolean debug;
  private final ImmutableMap<String, JournalConfig> journals;
  private final ImmutableMap<String, JournalConfig> journalsByIssn;
  private final ImmutableMap<String, String> journalUrls;
  private final ImmutableMap<String, String> journalsUrlsByIssn;
  private final Configuration freemarkerProperties;
  private final String dirPrefix;
  private final String subdirPrefix;
  private final String host;
  private final String casLoginURL;
  private final String casLogoutURL;
  private final String registrationURL;
  private final String changePasswordURL;
  private final String doiResolverURL;
  private final String changeEmailURL;
  private final String pubGetURL;
  private final String defaultJournalName;
  private final String orgName;
  private final String feedbackEmail;
  private final Date cisStartDate;
  private final int cache_storage_strong;
  private final int cache_storage_soft;
  private final int templateUpdateDelay;

  /**
   * Constructor that loads the list of css and javascript files and page titles for pages which follow the standard
   * templates.  Creates its own composite configuration by iterating over each of the configs in the config to assemble
   * a union of pages defined.
   *
   * @param configuration Ambra configuration
   * @throws Exception Exception
   */
  public AmbraFreemarkerConfig(Configuration configuration) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("Creating FreeMarker configuration");
    }
    debug = configuration.getBoolean("struts.devMode");
    dirPrefix = configuration.getString("ambra.platform.appContext");
    subdirPrefix = configuration.getString("ambra.platform.resourceSubDir");
    host = configuration.getString("ambra.platform.host");
    casLoginURL = configuration.getString("ambra.services.cas.url.login");
    casLogoutURL = configuration.getString("ambra.services.cas.url.logout");
    registrationURL = configuration.getString("ambra.services.registration.url.registration");
    changePasswordURL = configuration.getString("ambra.services.registration.url.change-password");
    changeEmailURL = configuration.getString("ambra.services.registration.url.change-email");
    doiResolverURL = configuration.getString("ambra.services.crossref.plos.doiurl");
    pubGetURL = configuration.getString("ambra.services.pubget.url");
    defaultJournalName = configuration.getString(DEFAULT_JOURNAL_NAME_CONFIG_KEY);
    orgName = configuration.getString("ambra.platform.name");
    feedbackEmail = configuration.getString("ambra.platform.email.feedback");
    cache_storage_strong = configuration.getInt("ambra.platform.template_cache.strong",
        DEFAULT_TEMPLATE_CACHE_STRONG);
    cache_storage_soft = configuration.getInt("ambra.platform.template_cache.soft",
        DEFAULT_TEMPLATE_CACHE_SOFT);
    templateUpdateDelay = configuration.getInt("ambra.platform.template_cache.update_delay",
        DEFAULT_TEMPLATE_UPDATE_DELAY);
    String date = configuration.getString("ambra.platform.cisStartDate");
    freemarkerProperties = configuration.subset("ambra.platform.freemarker");

    if (date == null) {
      throw new Exception("Could not find the cisStartDate node in the " +
          "ambra platform configuration.  Make sure the " +
          "ambra/platform/cisStartDate node exists.");
    }

    try {
      cisStartDate = DateFormat.getDateInstance(DateFormat.SHORT).parse(date);
    } catch (ParseException ex) {
      throw new Exception("Could not parse the cisStartDate value of \"" + date +
          "\" in the ambra platform configuration.  Make sure the cisStartDate is in the " +
          "following format: dd/mm/yyyy", ex);
    }

    Map<String, JournalConfigBuilder> builders = Maps.newLinkedHashMap();
    builders = loadConfig(builders, configuration);
    builders = processVirtualJournalConfig(builders, configuration);
    if (!builders.containsKey(defaultJournalName)) {
      log.warn("No journal configuration found for default name ({}). Applying all defaults.", defaultJournalName);
      builders.put(defaultJournalName, new JournalConfigBuilder());
    }
    journals = buildJournals(builders);

    // Now that the "journals" Map exists, index that map by Eissn to populate "journalsByEissn".
    ImmutableMap.Builder<String, JournalConfig> journalsByIssnBuilder = ImmutableMap.builder();
    for (Entry<String, JournalConfig> e : journals.entrySet()) {
      String issn = e.getValue().getIssn();
      if (issn != null) {
        journalsByIssnBuilder.put(issn, e.getValue());
      } else {
        log.warn("No ISSN found for journal: {}", e.getKey());
      }
    }
    journalsByIssn = journalsByIssnBuilder.build();

    journalUrls = buildUrlMap(journals);
    journalsUrlsByIssn = buildUrlMap(journalsByIssn);

    if (log.isTraceEnabled()) {
      for (Entry<String, JournalConfig> e : journals.entrySet()) {
        JournalConfig j = e.getValue();
        log.trace("Journal: " + e.getKey());
        log.trace("Journal url: " + j.getUrl());
        log.trace("Default Title: " + j.getDefaultTitle());
        log.trace("Default CSS: " + j.getDefaultCss());
        log.trace("Default JavaScript: " + j.getDefaultCss());
        Map<String, ? extends Collection<String>> map = j.getCssFiles();
        for (Entry<String, ? extends Collection<String>> entry : map.entrySet()) {
          log.trace("PageName: " + entry.getKey());
          log.trace("CSS FILES: " + entry.getValue());
        }
        map = j.getJavaScriptFiles();
        for (Entry<String, ? extends Collection<String>> entry : map.entrySet()) {
          log.trace("PageName: " + entry.getKey());
          log.trace("JS FILES: " + entry.getValue());
        }

        for (Entry<String, String> entry : j.getTitles().entrySet()) {
          log.trace("PageName: " + entry.getKey());
          log.trace("Title: " + entry.getValue());
        }
      }
      log.trace("Dir Prefix: " + dirPrefix);
      log.trace("SubDir Prefix: " + subdirPrefix);
      log.trace("Host: " + host);
      log.trace("Cas url login: " + casLoginURL);
      log.trace("Case url logout: " + casLogoutURL);
      log.trace("Registration URL: " + registrationURL);
      log.trace("Registration Change Pass URL: " + changePasswordURL);
      log.trace("Registration Change EMail URL: " + changeEmailURL);
      log.trace("DOI Resolver URL: " + doiResolverURL);
      log.trace("PubGet URL:" + pubGetURL);
      log.trace("Default Journal Name: " + defaultJournalName);
    }
    if (log.isDebugEnabled()) {
      log.debug("End FreeMarker Configuration Reading");
    }
  }

  private ImmutableMap<String, JournalConfig> buildJournals(Map<String, JournalConfigBuilder> builders) {
    ImmutableMap.Builder<String, JournalConfig> mapBuilder = ImmutableMap.builder();
    for (Entry<String, JournalConfigBuilder> entry : builders.entrySet()) {
      mapBuilder.put(entry.getKey(), entry.getValue().build());
    }
    return mapBuilder.build();
  }

  private Map<String, JournalConfigBuilder> loadConfig(Map<String, JournalConfigBuilder> journalBuilders, Configuration myConfig) {
    if (!(myConfig instanceof CombinedConfiguration))
      loadJournals(journalBuilders, myConfig);
    else {
      int numConfigs = ((CombinedConfiguration) myConfig).getNumberOfConfigurations();
      for (int c = 0; c < numConfigs; c++)
        loadConfig(journalBuilders, ((CombinedConfiguration) myConfig).getConfiguration(c));
    }
    return journalBuilders;
  }

  private void loadJournals(Map<String, JournalConfigBuilder> journalBuilders, Configuration configuration) {
    int numJournals = configuration.getList("ambra.freemarker.journal.name").size();
    for (int k = 0; k < numJournals; k++) {
      final String journal = "ambra.freemarker.journal(" + k + ")";
      final String journalName = configuration.getString(journal + ".name");
      if (log.isDebugEnabled()) {
        log.debug("reading journal name: " + journalName);
      }
      JournalConfigBuilder jc = journalBuilders.get(journalName);
      if (jc == null) {
        if (log.isDebugEnabled()) {
          log.debug("journal Not found, creating: " + journalName);
        }
        jc = new JournalConfigBuilder();
        journalBuilders.put(journalName, jc);
      }

      if (jc.getDefaultTitle() == null) {
        final String title = configuration.getString(journal + ".default.title");
        if (title != null) {
          jc.setDefaultTitle(title);
        }
      }

      if (jc.getMetaDescription() == null) {
        final String metaDescription = configuration.getString(journal + ".metaDescription");
        if (metaDescription != null) {
          jc.setMetaDescription(metaDescription);
        }
      }

      if (jc.getMetaKeywords() == null) {
        final String metaKeywords = configuration.getString(journal + ".metaKeywords");
        if (metaKeywords != null) {
          jc.setMetaKeywords(metaKeywords);
        }
      }

      if (jc.getHashTag() == null) {
        final String hashtag = configuration.getString(journal + ".hashTag");
        if (hashtag != null) {
          jc.setHashTag(hashtag);
        }
      }

      if (jc.getDisplayName() == null) {
        final String displayName = configuration.getString(journal + ".displayName");
        if (displayName != null) {
          jc.setDisplayName(displayName);
        }
      }

      if (jc.getArticleTitlePrefix() == null) {
        final String articleTitlePrefix = configuration.getString(journal + ".articleTitlePrefix");
        if (articleTitlePrefix != null) {
          jc.setArticleTitlePrefix(articleTitlePrefix);
        }
      }

      if (jc.getDefaultCss() == null) {
        final List fileList = configuration.getList(journal + ".default.css.file");
        String[] defaultCss;
        if (fileList.size() > 0) {
          defaultCss = new String[fileList.size()];
          Iterator iter = fileList.iterator();
          for (int i = 0; i < fileList.size(); i++) {
            defaultCss[i] = dirPrefix + subdirPrefix + iter.next();
          }
          jc.setDefaultCss(defaultCss);
        }
      }

      if (jc.getDefaultJavaScript() == null) {
        final List fileList = configuration.getList(journal + ".default.javascript.file");
        String javascriptFile;
        String[] defaultJavaScript;
        if (fileList.size() > 0) {
          defaultJavaScript = new String[fileList.size()];
          Iterator iter = fileList.iterator();
          for (int i = 0; i < fileList.size(); i++) {
            javascriptFile = (String) iter.next();
            if (javascriptFile.endsWith(".ftl")) {
              defaultJavaScript[i] = subdirPrefix + javascriptFile;
            } else {
              defaultJavaScript[i] = dirPrefix + subdirPrefix + javascriptFile;
            }
          }
          jc.setDefaultJavaScript(defaultJavaScript);
        }
      }

      final int numPages = configuration.getList(journal + ".page.name").size();

      for (int i = 0; i < numPages; i++) {
        String page = journal + ".page(" + i + ")";
        String pageName = configuration.getString(page + ".name");
        if (log.isDebugEnabled())
          log.debug("Reading config for page name: " + pageName);

        if (!jc.getTitles().containsKey(pageName)) {
          final String title = configuration.getString(page + ".title");
          if (title != null) {
            jc.getTitles().put(pageName, title);
          }
        }

        if (!jc.getCssFiles().containsKey(pageName)) {
          List<String> list = configuration.getList(page + ".css.file");
          String[] cssArray = new String[list.size()];
          int j = 0;
          for (String fileName : list) {
            cssArray[j++] = dirPrefix + subdirPrefix + fileName;
          }
          if (cssArray.length > 0 ||
              cssArray.length == 0 && configuration.getProperty(page + ".css") != null) {
            jc.getCssFiles().put(pageName, cssArray);
          }
        }

        if (!jc.getJavaScriptFiles().containsKey(pageName)) {
          List<String> list = configuration.getList(page + ".javascript.file");
          String[] javaScriptArray = new String[list.size()];
          int j = 0;
          for (String fileName : list) {
            if (fileName.endsWith(".ftl")) {
              javaScriptArray[j++] = subdirPrefix + fileName;
            } else {
              javaScriptArray[j++] = dirPrefix + subdirPrefix + fileName;
            }
          }

          if (javaScriptArray.length > 0 ||
              javaScriptArray.length == 0 && configuration.getProperty(page + ".javascript") != null) {
            jc.getJavaScriptFiles().put(pageName, javaScriptArray);
          }
        }
      }
    }

  }

  private Map<String, JournalConfigBuilder> processVirtualJournalConfig(Map<String, JournalConfigBuilder> journalBuilders, Configuration configuration) {
    final Collection<String> virtualJournals =
        configuration.getList(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_JOURNALS);
    String defaultVirtualJournal =
        configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT +
            ".journal");
    JournalConfigBuilder jour;

    if ((defaultVirtualJournal != null) && (!"".equals(defaultVirtualJournal))) {
      jour = journalBuilders.get(defaultVirtualJournal);
      if (jour != null) {
        jour.setUrl(configuration.getString(
            VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT + ".url"));
        jour.setIssn(configuration.getString(
            VirtualJournalContextFilter.CONF_VIRTUALJOURNALS_DEFAULT + ".eIssn"));
      }
    }

    for (final String journalName : virtualJournals) {
      jour = journalBuilders.get(journalName);
      if (jour != null) {
        jour.setUrl(configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS +
            "." + journalName + ".url"));
        jour.setIssn(configuration.getString(VirtualJournalContextFilter.CONF_VIRTUALJOURNALS +
            "." + journalName + ".eIssn"));
      }
    }
    return journalBuilders;
  }

  /**
   * Gets the title for the given template and journal name. Return the default value if not defined
   *
   * @param templateName Template name
   * @param journalName  Journal name
   * @return Returns the title given a template name.
   */
  public String getTitle(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    JournalConfig defaultJc = journals.get(defaultJournalName);

    String defaultTemplateName = "/" + trimJournalFromTemplatePath(templateName);

    String title = null;

    // Try to find title for template
    if (jc != null)
      title = jc.getTitles().get(defaultTemplateName);

    if (title != null)
      return title;

    // Try to find title for template in default journal
    if (defaultJc != null)
      title = defaultJc.getTitles().get(defaultTemplateName);

    if (title != null)
      return title;

    // Try default title in current journal
    if (jc != null)
      title = jc.getDefaultTitle();

    if (title != null)
      return title;

    // Try default title in default journal
    if (defaultJc != null)
      title = defaultJc.getDefaultTitle();

    if (title != null)
      return title;

    // Use hardcoded default title
    return DEFAULT_TITLE;
  }

  /**
   * @return <code>true</code> if struts is in dev mode
   */
  public boolean getDebug() {
    return debug;
  }

  /**
   * Gets title for page defined in templateName and uses the defaultJournal name
   *
   * @param templateName Template name
   * @return page title
   */
  public String getTitle(String templateName) {
    return getTitle(templateName, defaultJournalName);
  }


  /**
   * Gets the array of CSS files associated with templateName and journalName or returns the default values if not
   * available.
   *
   * @param templateName Template name
   * @param journalName  Journal name
   * @return Returns list of css files given a template name.
   */
  public ImmutableList<String> getCss(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String defaultTemplateName = "/" + trimJournalFromTemplatePath(templateName);
    ImmutableList<String> retVal = getCssForJournal(jc, templateName, defaultTemplateName);
    if (retVal == null) {

      if (!usingDefault) {
        JournalConfig defaultJc = journals.get(defaultJournalName);
        retVal = getCssForJournal(defaultJc, templateName, defaultTemplateName);
      }
    }
    return retVal != null ? retVal : DEFAULT_CSS_FILES;
  }

  private ImmutableList<String> getCssForJournal(JournalConfig jc, String templateName,
                                                 String defaultTemplateName) {
    ImmutableList<String> retVal = jc.getCssFiles().get(templateName);
    if (retVal != null)
      return retVal;

    retVal = jc.getCssFiles().get(defaultTemplateName);
    if (retVal != null)
      return retVal;

    return jc.getDefaultCss();
  }

  /**
   * Retrieves css files for given page in the default journal
   *
   * @param templateName Template name
   * @return array of css filename for the page
   */
  public ImmutableList<String> getCss(String templateName) {
    return getCss(templateName, defaultJournalName);
  }

  /**
   * Gets the array of JavaScript files associated with templateName and journalName or returns the default values if
   * not available.
   *
   * @param templateName Template name
   * @param journalName  Journal name
   * @return Returns the list of JavaScript files given a template name.
   */
  public ImmutableList<String> getJavaScript(String templateName, String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String defaultTemplateName = "/" + trimJournalFromTemplatePath(templateName);
    ImmutableList<String> retVal = getJavascriptsForJournal(jc, templateName, defaultTemplateName);
    if (retVal == null) {

      if (!usingDefault) {
        JournalConfig defaultJc = journals.get(defaultJournalName);
        retVal = getJavascriptsForJournal(defaultJc, templateName, defaultTemplateName);
      }
    }
    return retVal != null ? retVal : DEFAULT_JS_FILES;
  }

  private ImmutableList<String> getJavascriptsForJournal(JournalConfig jc, String templateName,
                                                         String defaultTemplateName) {
    ImmutableList<String> retVal = jc.getJavaScriptFiles().get(templateName);
    if (retVal != null)
      return retVal;

    retVal = jc.getJavaScriptFiles().get(defaultTemplateName);
    if (retVal != null)
      return retVal;

    return jc.getDefaultJavaScript();
  }

  /**
   * Gets the array of javascript files for the default journal and the specificed page name
   *
   * @param templateName Template name
   * @return list of javascript files for the given page
   */
  public ImmutableList<String> getJavaScript(String templateName) {
    return getJavaScript(templateName, defaultJournalName);
  }

  /**
   * Gets meta keywords for journal
   *
   * @param journalName Journal name
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
   * @param journalName Journal name
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
   * Gets hashTag for social media for the journal
   *
   * @param journalName Journal name
   * @return hashTag for social media
   */
  public String getHashTag(String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getHashTag();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getHashTag();
    }
    return retVal != null ? retVal : "";
  }

  /**
   * Gets display name for journal
   *
   * @param journalName Journal name
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
   * Gets display name for journal
   *
   * @param eissn Journal eissn
   * @return display name
   */
  public String getDisplayNameByEissn(String eissn) {
    JournalConfig jc = journalsByIssn.get(eissn);
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
   * Get the issn which is the unique identifier for this journal
   *
   * @param journalName The name of the journal for which an issn is sought
   * @return The issn which is the unique identifier for this journal
   */
  public String getIssn(String journalName) {
    JournalConfig jc = journals.get(journalName);
    boolean usingDefault = false;
    if (jc == null) {
      usingDefault = true;
      jc = journals.get(defaultJournalName);
    }
    String retVal = jc.getIssn();
    if ((retVal == null) && !usingDefault) {
      jc = journals.get(defaultJournalName);
      retVal = jc.getIssn();
    }
    return retVal != null ? retVal : "";
  }


  /**
   * gets prefix for article title
   *
   * @param journalName Journal name
   * @return article title prefix
   */
  public String getArticleTitlePrefix(String journalName) {
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
   * Build a map from the provided map's keys to the journal URLs.
   *
   * @param journals a map of journals with arbitrary keys
   * @return an unmodifiable map from the same keys to journal URLs
   */
  private static ImmutableMap<String, String> buildUrlMap(Map<String, JournalConfig> journals) {
    ImmutableMap.Builder<String, String> urls = ImmutableMap.builder();
    for (Entry<String, JournalConfig> entry : journals.entrySet()) {
      String key = entry.getKey();
      String url = entry.getValue().getUrl();
      if (url != null) {
        urls.put(key, url);
      } else {
        log.warn("No URL found for journal: {}", key);
      }
    }
    return urls.build();
  }

  /**
   * Get the custom settings defined in the configuration file under the xml node:
   * <p/>
   * ambra/platform/freemarker
   * <p/>
   * These nodes will be listed out as named value pairs that can later be referenced by freemarker The XML node will be
   * the key, and the value paired with this key.
   *
   * @return the key to search for.
   */
  public String get(String setting) {
    if (freemarkerProperties != null) {
      if (freemarkerProperties.containsKey(setting)) {
        return freemarkerProperties.getString(setting);
      }
    }
    return "";
  }

  /**
   * @return Returns the dirPrefix.
   */
  public String getDirPrefix() {
    return dirPrefix;
  }

  /**
   * @return Returns the subdirPrefix.
   */
  public String getSubdirPrefix() {
    return subdirPrefix;
  }

  /**
   * @return Returns the casLoginURL.
   */
  public String getCasLoginURL() {
    return casLoginURL;
  }

  /**
   * @return Returns the host.
   */
  public String getHost() {
    return host;
  }

  /**
   * @return Returns the casLogoutURL.
   */
  public String getCasLogoutURL() {
    return casLogoutURL;
  }

  /**
   * @return Returns the registrationURL.
   */
  public String getRegistrationURL() {
    return registrationURL;
  }

  /**
   * Getter for changePasswordURL.
   *
   * @return Value of changePasswordURL.
   */
  public String getChangePasswordURL() {
    return changePasswordURL;
  }

  /**
   * @return Returns the changeEmailURL.
   */
  public String getChangeEmailURL() {
    return changeEmailURL;
  }

  /**
   * @return Returns the doiResolverURL.
   */
  public String getDoiResolverURL() {
    return doiResolverURL;
  }

  /**
   * Get <a href="http://pubget.com">PubGet</a> service URL.
   *
   * @return PubGet URL
   */
  public String getPubGetURL() {
    return pubGetURL;
  }

  /**
   * @return Returns the journalContextAttributeKey
   */
  public String getJournalContextAttributeKey() {
    return org.ambraproject.web.VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT;
  }

  /**
   * @return Returns the user attribute key
   */
  public String getUserAttributeKey() {
    return org.ambraproject.Constants.AMBRA_USER_KEY;
  }


  /**
   * Returns the URL for a given journal given its key
   *
   * @param journalKey Journal key
   * @return URL of journal
   */
  public String getJournalUrl(String journalKey) {
    JournalConfig jc = journals.get(journalKey);
    String url = "";
    if (jc != null) {
      url = jc.getUrl();
    }
    return url;
  }

  /**
   * Returns the URL for a journal given that journal's ISSN value
   *
   * @param journalIssn Journal ISSN
   * @return URL of journal
   */
  public String getJournalUrlFromIssn(String journalIssn) {
    JournalConfig jc = journalsByIssn.get(journalIssn);
    String url = "";
    if (jc != null) {
      url = jc.getUrl();
    }
    return url;
  }

  /**
   * Utility procedure that takes out journal-specific beggining of template name. For example templateName
   * /journals/plosone/index.ftl becomes index.ftl
   *
   * @param templateName Freemarker template name
   * @return Freemarker template name without leading journal path
   */
  public static String trimJournalFromTemplatePath(String templateName) {
    // Trim the beginning "journals/<journal_name>"
    StringTokenizer tokenizer = new StringTokenizer(templateName, "/");
    StringBuilder stringBuilder = new StringBuilder();
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.equals("journals")) {
        // skip next
        tokenizer.nextToken();
      } else {
        if (stringBuilder.length() != 0)
          stringBuilder.append('/');
        stringBuilder.append(token);
      }
    }

    return stringBuilder.toString();
  }

  /**
   * Get the maximum number of templates to store in the soft cache
   *
   * @return cache_storage_soft
   */
  public int getCache_storage_soft() {
    return cache_storage_soft;
  }

  /**
   * Get the maximum number of templates to store in the strong cache
   *
   * @return cache_storage_strong
   */
  public int getCache_storage_strong() {
    return cache_storage_strong;
  }


  /*
   * NOT quite the same as an ImmutableListMap, because this can make the distinction between a key being mapped to an
   * empty list versus being entirely absent. (Calling ImmutableListMap.get on an absent key returns an empty list.)
   */
  private static <K, V> ImmutableMap<K, ImmutableList<V>> immutableListValues(Map<? extends K, ? extends V[]> map) {
    if (map == null) {
      return null;
    }
    ImmutableMap.Builder<K, ImmutableList<V>> builder = ImmutableMap.builder();
    for (Entry<? extends K, ? extends V[]> entry : map.entrySet()) {
      builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
    }
    return builder.build();
  }

  private static class JournalConfig {

    private final ImmutableMap<String, ImmutableList<String>> cssFiles;
    private final ImmutableMap<String, ImmutableList<String>> javaScriptFiles;
    private final ImmutableMap<String, String> titles;

    private final ImmutableList<String> defaultCss;
    private final ImmutableList<String> defaultJavaScript;

    private final String defaultTitle;
    private final String metaKeywords;
    private final String metaDescription;
    private final String articleTitlePrefix;
    private final String displayName;
    private final String hashTag;
    private final String url;
    private final String issn;

    private JournalConfig(JournalConfigBuilder builder) {
      super();
      this.cssFiles = immutableListValues(builder.cssFiles);
      this.javaScriptFiles = immutableListValues(builder.javaScriptFiles);
      this.titles = (builder.titles == null) ? null
          : ImmutableMap.copyOf(builder.titles);
      this.defaultCss = (builder.defaultCss == null) ? null
          : ImmutableList.copyOf(builder.defaultCss);
      this.defaultJavaScript = (builder.defaultJavaScript == null) ? null
          : ImmutableList.copyOf(builder.defaultJavaScript);
      this.defaultTitle = builder.defaultTitle;
      this.metaKeywords = builder.metaKeywords;
      this.metaDescription = builder.metaDescription;
      this.articleTitlePrefix = builder.articleTitlePrefix;
      this.displayName = builder.displayName;
      this.hashTag = builder.hashTag;
      this.url = builder.url;
      this.issn = builder.issn;
    }

    public ImmutableMap<String, ImmutableList<String>> getCssFiles() {
      return cssFiles;
    }

    public ImmutableMap<String, ImmutableList<String>> getJavaScriptFiles() {
      return javaScriptFiles;
    }

    public ImmutableMap<String, String> getTitles() {
      return titles;
    }

    public ImmutableList<String> getDefaultCss() {
      return defaultCss;
    }

    public ImmutableList<String> getDefaultJavaScript() {
      return defaultJavaScript;
    }

    public String getDefaultTitle() {
      return defaultTitle;
    }

    public String getMetaKeywords() {
      return metaKeywords;
    }

    public String getMetaDescription() {
      return metaDescription;
    }

    public String getArticleTitlePrefix() {
      return articleTitlePrefix;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getHashTag() {
      return hashTag;
    }

    public String getUrl() {
      return url;
    }

    public String getIssn() {
      return issn;
    }

  }

  private static class JournalConfigBuilder {

    private Map<String, String[]> cssFiles = Maps.newLinkedHashMap();
    private Map<String, String[]> javaScriptFiles = Maps.newLinkedHashMap();
    private Map<String, String> titles = Maps.newLinkedHashMap();

    private String[] defaultCss;
    private String[] defaultJavaScript;

    private String defaultTitle;
    private String metaKeywords;
    private String metaDescription;
    private String articleTitlePrefix;
    private String displayName;
    private String hashTag;
    private String url;
    private String issn;

    public String getArticleTitlePrefix() {
      return articleTitlePrefix;
    }

    public Map<String, String[]> getCssFiles() {
      return cssFiles;
    }

    public String[] getDefaultCss() {
      return defaultCss;
    }

    public String[] getDefaultJavaScript() {
      return defaultJavaScript;
    }

    public String getDefaultTitle() {
      return defaultTitle;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getHashTag() {
      return hashTag;
    }

    public String getIssn() {
      return issn;
    }

    public Map<String, String[]> getJavaScriptFiles() {
      return javaScriptFiles;
    }

    public String getMetaDescription() {
      return metaDescription;
    }

    public String getMetaKeywords() {
      return metaKeywords;
    }

    public Map<String, String> getTitles() {
      return titles;
    }

    public String getUrl() {
      return url;
    }

    public JournalConfigBuilder setDefaultCss(String[] defaultCss) {
      this.defaultCss = defaultCss;
      return this;
    }

    public JournalConfigBuilder setDefaultJavaScript(String[] defaultJavaScript) {
      this.defaultJavaScript = defaultJavaScript;
      return this;
    }

    public JournalConfigBuilder setDefaultTitle(String defaultTitle) {
      this.defaultTitle = defaultTitle;
      return this;
    }

    public JournalConfigBuilder setMetaKeywords(String metaKeywords) {
      this.metaKeywords = metaKeywords;
      return this;
    }

    public JournalConfigBuilder setMetaDescription(String metaDescription) {
      this.metaDescription = metaDescription;
      return this;
    }

    public JournalConfigBuilder setArticleTitlePrefix(String articleTitlePrefix) {
      this.articleTitlePrefix = articleTitlePrefix;
      return this;
    }

    public JournalConfigBuilder setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public JournalConfigBuilder setHashTag(String hashTag) {
      this.hashTag = hashTag;
      return this;
    }

    public JournalConfigBuilder setUrl(String url) {
      this.url = url;
      return this;
    }

    public JournalConfigBuilder setIssn(String issn) {
      this.issn = issn;
      return this;
    }

    public JournalConfig build() {
      return new JournalConfig(this);
    }
  }


  /**
   * @return the orgName
   */
  public String getOrgName() {
    return orgName;
  }

  /**
   * @return the feedbackEmail
   */
  public String getFeedbackEmail() {
    return feedbackEmail;
  }

  /**
   * Get the CIS Starting date
   *
   * @return the CIS start date
   */
  public Date getCisStartDate() {
    return this.cisStartDate;
  }

  /**
   * Get the CIS Starting date in milliseconds
   *
   * @return the CIS start date in milliseconds
   */
  public long getCisStartDateMillis() {
    return this.cisStartDate.getTime();
  }

  public String getDefaultJournalName() {
    return defaultJournalName;
  }

  /**
   * Return the current template update delay setting
   *
   * @return
   */
  public int getTemplateUpdateDelay() {
    return templateUpdateDelay;
  }

  /**
   * Return a full map of journal URLs. Getting values from this map is functionally interchangeable with {@link
   * #getJournalUrl(String)} (assuming the key is present). This method is a convenience in FreeMarker, where the map
   * can be accessed as a FreeMarker hash variable.
   *
   * @return the map from journal keys to URLs
   */
  public Map<String, String> getJournalUrls() {
    return journalUrls;
  }

  /**
   * Return a full map of journal URLs, keyed by eIssn.
   *
   * @return the map from eIssns to URLs
   */
  public Map<String, String> getJournalsUrlsByIssn() {
    return journalsUrlsByIssn;
  }

}
