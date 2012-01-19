/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.ehcache.Ehcache;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.jdom.Element;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.configuration.ConfigurationStore;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.models.Citation;
import org.plos.models.DublinCore;
import org.plos.models.UserProfile;
import org.plos.util.ArticleXMLUtils;
import org.plos.util.FileUtils;
import org.plos.web.VirtualJournalContext;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

/**
 * Get a variety of Article Feeds.
 *
 * @author Jeff Suttor
 * @author Eric Brown
 */
public class ArticleFeed extends BaseActionSupport {

  private ArticleOtmService articleOtmService;
  private Ehcache feedCache;

  private DocumentBuilderFactory factory;

  // WebWorks will set from URI param
  private String startDate;
  private String endDate;
  private String category;
  private String author;
  private int maxResults = -1;
  private boolean relativeLinks = false;
  private boolean extended = false;
  private String title;
  private String selfLink;

  // WebWorks PlosOneFeedResult parms
  private WireFeed wireFeed;

  /**
   * PLoS ONE Configuration
   */
  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static final Log log = LogFactory.getLog(ArticleFeed.class);

  private static final String ATOM_NS = "http://www.w3.org/2005/Atom"; // Tmp hack for categories

  final int DEFAULT_FEED_DURATION = configuration.getInteger("pub.feed.defaultDuration", 3);
  private ArticleXMLUtils articleXmlUtils;

  /**
   * Returns a feed based on interpreting the URI.
   *
   * @return webwork status code
   * @throws Exception Exception
   */
  public String execute() throws Exception {

    // Create a document builder factory and set the defaults
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);

    // use native HTTP to avoid WebWorks
    HttpServletRequest request = ServletActionContext.getRequest();
    String pathInfo = request.getPathInfo();
    URI uri;
    if (pathInfo == null) {
      uri = URI.create("/");
    } else {
      uri = URI.create(pathInfo);

    }

    // Compute startDate default as it is needed to compute cache-key (its default is tricky)
    if (startDate == null) {
        // default is to go back <= N months
        GregorianCalendar monthsAgo = new GregorianCalendar();
        monthsAgo.add(Calendar.MONTH, -DEFAULT_FEED_DURATION);
        monthsAgo.set(Calendar.HOUR_OF_DAY, 0);
        monthsAgo.set(Calendar.MINUTE, 0);
        monthsAgo.set(Calendar.SECOND, 0);
        startDate = monthsAgo.getTime().toString();
    }
    if (startDate.length() == 0)
      startDate = null; // shortuct for no startDate, show all articles
    if (log.isDebugEnabled()) {
      log.debug("generating feed w/startDate=" + startDate);
    }

    // Get feed if cached or generate feed by querying OTM
    String cacheKey = getCacheKey();
    net.sf.ehcache.Element e = feedCache.get(cacheKey);
    if (e != null) {
      if (log.isDebugEnabled())
        log.debug("Retrieved feed " + cacheKey + " from cache." +
                  " Created " + new Date(e.getCreationTime()) +
                  " last access " + new Date(e.getLastAccessTime()) +
                  " hit count " + e.getHitCount());
      wireFeed = (WireFeed) e.getValue();
    } else {
      wireFeed = getFeed(uri);
      feedCache.put(new net.sf.ehcache.Element(cacheKey, wireFeed));
      if (log.isDebugEnabled())
        log.debug("Built feed " + cacheKey);
    }

    // Action response type is PlosOneFeedResult, it will return wireFeed as a response.

    // tell WebWorks success
    return SUCCESS;
  }

  /*
   * Parse the URI to generate an OTM query.  Return the results as a WireFeed.
   *
   * @parm uri URI to parse to generate an OTM query.
   * @return Query results as a WireFeed.
   */
  private WireFeed getFeed(URI uri) throws ApplicationException {

    // get default values from config file
    final String journal = getCurrentJournal();
    // must end w/trailing slash
    String PLOSONE_URI  = configuration.getString("pub.virtualJournals." + journal + ".url",
      configuration.getString("pub.webserver-url", "http://plosone.org/"));
    if (!PLOSONE_URI.endsWith("/")) {
        PLOSONE_URI += "/";
    }
    final String PLOSONE_NAME = journalConfGetString(configuration, journal,
            "pub.name", "Public Library of Science");
    final String PLOSONE_EMAIL_GENERAL = journalConfGetString(configuration, journal,
            "pub.email.general", "webmaster@plos.org");
    final String PLOSONE_COPYRIGHT = journalConfGetString(configuration, journal,
            "pub.copyright",
            "This work is licensed under a Creative Commons Attribution-Share Alike 3.0 License, http://creativecommons.org/licenses/by-sa/3.0/");
    final String FEED_TITLE = journalConfGetString(configuration, journal,
            "pub.feed.title", "PLoS ONE");
    final String FEED_TAGLINE = journalConfGetString(configuration, journal,
            "pub.feed.tagline", "Publishing science, accelerating research");
    final String FEED_ICON = journalConfGetString(configuration, journal,
            "pub.feed.icon", PLOSONE_URI + "images/pone_favicon.ico");
    final String FEED_ID = journalConfGetString(configuration, journal,
            "pub.feed.id", "info:doi/10.1371/feed.pone");
    final String FEED_EXTENDED_NS = journalConfGetString(configuration, journal,
            "pub.feed.extended.namespace", "http://www.plos.org/atom/ns#plos");
    final String FEED_EXTENDED_PREFIX = journalConfGetString(configuration, journal,
            "pub.feed.extended.prefix", "plos");

    // use WebWorks to get Action URIs
    // TODO: WebWorks ActionMapper is broken, hand-code URIs
    final String fetchObjectAttachmentAction = "article/fetchObjectAttachment.action";

    // Atom 1.0 is default
    Feed feed = new Feed("atom_1.0");

    feed.setEncoding("UTF-8");
    feed.setXmlBase(PLOSONE_URI);

    String xmlBase = (relativeLinks ? "" : PLOSONE_URI);
    if (selfLink == null || selfLink.equals("")) {
      if (uri.toString().startsWith("/")) {
        selfLink = PLOSONE_URI.substring(0, PLOSONE_URI.length() - 1) + uri;
      } else {
        selfLink = PLOSONE_URI + uri;
      }
    }

    // must link to self
    Link self = new Link();
    self.setRel("self");
    self.setHref(selfLink);
    self.setTitle(FEED_TITLE);
    List<Link> otherLinks = new ArrayList();
    otherLinks.add(self);
    feed.setOtherLinks(otherLinks);

    String id = FEED_ID;
    if (category != null && category.length() > 0)
      id += "?category=" + category;
    if (author != null)
      id += "?author=" + author;
    feed.setId(id);

    if (title != null)
      feed.setTitle(title);
    else {
      String feedTitle = FEED_TITLE;
      if (category != null && category.length() > 0)
        feedTitle += " - Category " + category;
      if (author != null)
        feedTitle += " - Author " + author;
      feed.setTitle(feedTitle);
    }

    Content tagline = new Content();
    tagline.setValue(FEED_TAGLINE);
    feed.setTagline(tagline);
    feed.setUpdated(new Date());
    // TODO: bug in Rome ignores icon/logo :(
    feed.setIcon(FEED_ICON);
    feed.setLogo(FEED_ICON);
    feed.setCopyright(PLOSONE_COPYRIGHT);

    // make PLoS the author of the feed
    Person plos = new Person();
    plos.setEmail(PLOSONE_EMAIL_GENERAL);
    plos.setName(PLOSONE_NAME);
    plos.setUri(PLOSONE_URI);
    List<Person> feedAuthors = new ArrayList();
    feedAuthors.add(plos);
    feed.setAuthors(feedAuthors);

    // build up OTM query, take URI params first, then sensible defaults

    // ignore endDate, default is null

    // was category= URI param specified?
    List<String> categoriesList = new ArrayList();
    if (category != null && category.length() > 0) {
      categoriesList.add(category);
      if (log.isDebugEnabled()) {
        log.debug("generating feed w/category=" + category);
      }
    }

    // was author= URI param specified?
    List<String> authorsList = new ArrayList();
    if (author != null) {
      authorsList.add(author);
      if (log.isDebugEnabled()) {
        log.debug("generating feed w/author=" + author);
      }
    }

    // was maxResults= URI param specified?
    if (maxResults <= 0) {
      maxResults = 30;  // default
    }

    // sort by date, descending
    HashMap<String, Boolean> sort = new HashMap();
    sort.put("dublinCore.date", false);

    List<Article> articles = null;
    try {
      articles = articleOtmService.getArticles(
        startDate,             // start date
        endDate,               // end date
        categoriesList.toArray(new String[categoriesList.size()]),  // categories
        authorsList.toArray(new String[authorsList.size()]),        // authors
        Article.ACTIVE_STATES, // states
        sort,                  // sort by
        maxResults);           // max results
    } catch (ParseException ex) {
      throw new ApplicationException(ex);
    }
    if (log.isDebugEnabled()) {
      log.debug("feed query returned " + articles.size() + " articles");
    }

    // add each Article as a Feed Entry
    List<Entry> entries = new ArrayList();
    for (Article article : articles) {
      Entry entry = new Entry();
      DublinCore dc = article.getDublinCore();

      // TODO: how much more meta-data is possible
      // e.g. article.getDc_type(), article.getFormat(), etc.

      entry.setId(dc.getIdentifier());

      // respect Article specific rights
      String rights = dc.getRights();
      if (rights != null) {
        entry.setRights(rights);
      } else {
        // default is CC BY SA 3.0
        entry.setRights(PLOSONE_COPYRIGHT);
      }

      entry.setTitle(dc.getTitle());
      entry.setPublished(dc.getAvailable());
      entry.setUpdated(dc.getAvailable());

      // links
      List<Link> altLinks = new ArrayList();

      // must link to self, do it first so link is favored
      Link entrySelf = new Link();
      entrySelf.setRel("alternate");
      try {
        entrySelf.setHref(xmlBase + "article/" + URLEncoder.encode(dc.getIdentifier(), "UTF-8"));
      } catch(UnsupportedEncodingException uee) {
        entrySelf.setHref(xmlBase + "article/" + dc.getIdentifier());
        log.error("UTF-8 not supported?", uee);
      }
      entrySelf.setTitle(dc.getTitle());
      altLinks.add(entrySelf);

      // alternative representation links
      Set<String> representations = article.getRepresentations();
      if (representations != null) {
        for (String representation : representations) {
          Link altLink = new Link();
          altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + dc.getIdentifier() + "&representation=" + representation);
          altLink.setRel("related");
          altLink.setTitle("(" + representation + ") " + dc.getTitle());
          altLink.setType(FileUtils.getContentType(representation));
          altLinks.add(altLink);
        }
      }

      // set all alternative links
      entry.setAlternateLinks(altLinks);

      // Authors
      String authorNames = ""; // Sometimes added to article content for feed
      List<Person> authors = new ArrayList<Person>();
      Citation bc = article.getDublinCore().getBibliographicCitation();
      if (bc != null) {
        List<UserProfile> authorProfiles = bc.getAuthors();
        for (UserProfile profile: authorProfiles) {
          Person person = new Person();
          person.setName(profile.getRealName());
          authors.add(person);

          if (authorNames.length() > 0)
            authorNames += ", ";
          authorNames += profile.getRealName();
        }
      } else // This should only happen for older, unmigrated articles
        log.warn("No bibliographic citation (is article '" + article.getId() + "' migrated?)");

      // We only want one author on the regular feed
      if (extended)
        entry.setAuthors(authors);
      else if (authors.size() >= 1) {
        List<Person> oneAuthor = new ArrayList<Person>(1);
        String name = authors.get(0).getName();
        Person person = new Person();
        if (authors.size() > 1)
          person.setName(name + " et al.");
        else
          person.setName(name);
        oneAuthor.add(person);
        entry.setAuthors(oneAuthor);
      }

      // Contributors - TODO: Get ordered list when available
      List<Person> contributors = new ArrayList<Person>();
      for (String contributor: article.getDublinCore().getContributors()) {
        Person person = new Person();
        person.setName(contributor);
        contributors.add(person);
      }
      entry.setContributors(contributors);

      // All our foreign markup
      List<Element> foreignMarkup = new ArrayList<Element>();

      // Volume & issue
      if (extended && bc != null) {
        // Add volume
        if (bc.getVolume() != null) {
          Element volume = new Element("volume", FEED_EXTENDED_PREFIX, FEED_EXTENDED_NS);
          volume.setText(bc.getVolume().toString());
          foreignMarkup.add(volume);
        }
        // Add issue
        if (bc.getIssue() != null) {
          Element issue = new Element("issue", FEED_EXTENDED_PREFIX, FEED_EXTENDED_NS);
          issue.setText(bc.getIssue());
          foreignMarkup.add(issue);
        }
      }

      Set<Category> categories = article.getCategories();
      if (categories != null) {
        for (Category category : categories) {
          // TODO: How can we get NS to be automatically filled in from Atom?
          Element feedCategory = new Element("category", ATOM_NS);
          feedCategory.setAttribute("term", category.getMainCategory());
          // TODO: what's the URI for our categories
          // feedCategory.setScheme(category.getPid());
          feedCategory.setAttribute("label", category.getMainCategory());

          // subCategory?
          String subCategory = category.getSubCategory();
          if (subCategory != null) {
            Element feedSubCategory = new Element("category", ATOM_NS);
            feedSubCategory.setAttribute("term", subCategory);
            // TODO: what's the URI for our categories
            // feedSubCategory.setScheme();
            feedSubCategory.setAttribute("label", subCategory);
            feedCategory.addContent(feedSubCategory);
          }

          foreignMarkup.add(feedCategory);
        }
      }

      // Add foreign markup
      if (extended && foreignMarkup.size() > 0)
        entry.setForeignMarkup(foreignMarkup);

      // atom:content
      List <Content> contents = new ArrayList();
      Content description = new Content();
      description.setType("html");
      try {
        StringBuffer text = new StringBuffer();
        // If this is a nomral feed (not extended) and there's more than one author, add to content
        if ((!extended) && authors.size() > 1) {
          text.append("<p>by ").append(authorNames).append("</p>\n");
        }
        if (dc.getDescription() != null) {
        	text.append(articleXmlUtils.transformArticleDescriptionToHtml(dc.getDescription()));
        }
        description.setValue(text.toString());
      } catch (Exception e) {
        log.error(e);
        description.setValue("<p>Internal server error.</p>");
        // keep generating feed
      }
      contents.add(description);
      entry.setContents(contents);

      // add completed Entry to List
      entries.add(entry);
    }

    // set feed entries to the articles
    feed.setEntries(entries);

   return feed;
  }

  /**
   * Set articleOtmService
   *
   * @param articleOtmService articleOtmService
   */
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  private String getCacheKey() {
    Map<String, String> key = new TreeMap<String, String>();
    key.put("journal", getCurrentJournal());
    if (startDate != null)
      key.put("sd", startDate);
    if (endDate != null)
      key.put("ed", endDate);
    if (category != null && category.length() > 0)
      key.put("cat", category);
    if (author != null)
      key.put("aut", author);
    if (maxResults != -1)
      key.put("cnt", Integer.toString(maxResults));
    if (!relativeLinks)
      key.put("rel", "true");
    if (!extended)
      key.put("ext", "true");
    if (title != null)
      key.put("tit", title);
    if (selfLink != null)
      key.put("self", selfLink);

    return key.toString();
  }

  /**
   * Set ehcache instance via spring
   *
   * @param the ehcache instance
   */
  public void setFeedCache(final Ehcache feedCache) {
    this.feedCache = feedCache;
  }

  /**
   * Allow WebWorks to get the param wireFeed
   *
   * @return The WireFeed.
   */
  public WireFeed getWireFeed() {
    return wireFeed;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setStartDate(final String startDate) {
    this.startDate = startDate;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setEndDate(final String endDate) {
    this.endDate = endDate;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setCategory(final String category) {
    this.category = category;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setAuthor(final String author) {
    this.author = author;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setRelativeLinks(final boolean relativeLinks) {
    this.relativeLinks = relativeLinks;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setExtended(final boolean extended) {
    this.extended = extended;
  }

  /**
   * WebWroks will set from URI param
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setSelfLink(final String selfLink) {
    this.selfLink = selfLink;
  }

  /**
   *  Get a String from the Configuration looking first for a Journal override.
   *
   * @param configuration to use.
   * @param journal name.
   * @param key to lookup.
   * @param defaultValue if key is not found.
   * @return value for key.
   */
  private String journalConfGetString(Configuration configuration, String journal, String key,
          String defaultValue) {
    return configuration.getString("pub.virtualJournals." + journal + "." + key,
            configuration.getString(key, defaultValue));
  }

  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
      getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * Called by Spring to initialize an articleXmlUtils reference. 
   * 
   * @param articleXmlUtils The articleXmlUtils to set.
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }
}
