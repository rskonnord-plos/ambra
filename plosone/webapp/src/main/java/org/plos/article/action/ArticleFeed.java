/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import com.opensymphony.webwork.ServletActionContext;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.util.FedoraArticle;
import org.plos.configuration.ConfigurationStore;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.util.FileUtils;

import org.topazproject.xml.transform.cache.CachedSource;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;


/**
 * Get a variety of Article Feeds.
 *
 * @author Jeff Suttor
 */
public class ArticleFeed extends BaseActionSupport {

  private ArticleOtmService articleOtmService;

  private Templates toHtmlTranslet;
  private DocumentBuilderFactory factory;

  // WebWorks will set from URI param
  private String startDate;
  private String endDate;
  private String category;
  private String author;
  private int maxResults = -1;
  private String representation;

  // WebWorks PlosOneFeedResult parms
  private WireFeed wireFeed;

  /**
   * PLoS ONE Configuration
   */
  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static final Log log = LogFactory.getLog(ArticleFeed.class);

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

    // generate feed
    wireFeed = getFeed(uri);

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
    final String PLOSONE_URI           = configuration.getString("plosone.webserver-url", "http://plosone.org/");
    final String PLOSONE_NAME          = configuration.getString("plosone.name",          "Public Library of Science");
    final String PLOSONE_EMAIL_GENERAL = configuration.getString("plosone.email.general", "webmaster@plos.org");
    final String PLOSONE_COPYRIGHT     = configuration.getString("plosone.copyright",
      "This work is licensed under a Creative Commons Attribution-Share Alike 3.0 License, http://creativecommons.org/licenses/by-sa/3.0/");
    final String FEED_TITLE            = configuration.getString("plosone.feed.title",    "PLoS ONE Alerts: PLoS ONE Journal");
    final String FEED_TAGLINE          = configuration.getString("plosone.feed.tagline",  "Publishing science, accelerating research");
    final String FEED_ICON             = configuration.getString("plosone.feed.icon",     PLOSONE_URI + "images/pone_favicon.ico");

    // use WebWorks to get Action URIs
    // TODO: WebWorks ActionMapper is broken, hand-code URIs
    final String fetchArticleAction = "article/fetchArticle.action";
    final String fetchObjectAttachmentAction = "article/fetchObjectAttachment.action";

    // Atom 1.0 is default
    Feed feed = new Feed("atom_1.0");

    feed.setEncoding("UTF-8");
    feed.setXmlBase(PLOSONE_URI);

    // must link to self
    Link self = new Link();
    self.setRel("self");
    self.setHref(uri.toString());
    self.setTitle(FEED_TITLE);
    List<Link> otherLinks = new ArrayList();
    otherLinks.add(self);
    feed.setOtherLinks(otherLinks);

    feed.setId(uri.toString());  // TODO: used canned URIs
    feed.setTitle(FEED_TITLE);
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

    // was startDate= URI param specified?
    if (startDate == null) {
        // default is to go back <= 3 months
        GregorianCalendar threeMonthsAgo = new GregorianCalendar();
        threeMonthsAgo.add(Calendar.MONTH, -3);
        startDate = threeMonthsAgo.getTime().toString();
    }
    if (log.isDebugEnabled()) {
      log.debug("generating feed w/startDate=" + startDate);
    }

    // ignore endDate, default is null

    // was category= URI param specified?
    List<String> categoriesList = new ArrayList();
    if (category != null) {
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

    // sort by date, ascending
    HashMap<String, Boolean> sort = new HashMap();
    sort.put("date", true);

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
    } catch (RemoteException ex) {
      throw new ApplicationException(ex);
    }
    if (log.isDebugEnabled()) {
      log.debug("feed query returned " + articles.size() + "articles");
    }

    // add each Article as a Feed Entry
    List<Entry> entries = new ArrayList();
    for (Article article : articles) {
      Entry entry = new Entry();

      // TODO: how much more meta-data is possible
      // e.g. article.getDc_type(), article.getFormat(), etc.

      entry.setId(article.getIdentifier());

      // respect Article specific rights
      String rights = article.getRights();
      if (rights != null) {
        entry.setRights(rights);
      } else {
        // default is CC BY SA 3.0
        entry.setRights(PLOSONE_COPYRIGHT);
      }

      entry.setTitle(article.getTitle());
      entry.setPublished(article.getAvailable());
      entry.setUpdated(article.getAvailable());

      // links
      List<Link> altLinks = new ArrayList();

      // must link to self, do it first so link is favored
      Link entrySelf = new Link();
      entrySelf.setRel("alternate");
      entrySelf.setHref(fetchArticleAction + "?articleURI=" + article.getIdentifier());
      entrySelf.setTitle(article.getTitle());
      altLinks.add(entrySelf);

      // alternative representation links
      Set<String> representations = article.getRepresentations();
      if (representations != null) {
        for (String representation : representations) {
          Link altLink = new Link();
          altLink.setHref(fetchObjectAttachmentAction + "?uri=" + article.getIdentifier() + "&representation=" + representation);
          altLink.setRel("related");
          altLink.setTitle("(" + representation + ") " + article.getTitle());
          altLink.setType(FileUtils.getContentType(representation));
          altLinks.add(altLink);
        }
      }

      // set all alternative links
      entry.setAlternateLinks(altLinks);

      // TODO: Authors,Contributors should be in Article order in OTM,
      // use Fedora workaround until database is re-ingested
      FedoraArticle fedoraArticle = null;
      try {
        fedoraArticle = new FedoraArticle(article.getIdentifier());
      } catch (RuntimeException groovyRuntime) {
        log.warn(groovyRuntime);  // call to attention
        throw groovyRuntime;
      }

      // Authors
      List authors = fedoraArticle.getAuthors();
      if (authors != null) {
        List<Person> authorList = new ArrayList();
        Iterator onAuthor = authors.iterator();
        while (onAuthor.hasNext()) {
          String author = (String) onAuthor.next();
          Person person = new Person();
          person.setName(author);
          // TODO: setEmail, setUri
          authorList.add(person);
        }
        entry.setAuthors(authorList);
      }

      // contributors
      List contributors = fedoraArticle.getContributors();
      if (contributors != null) {
        List<Person> contributorList = new ArrayList();
        Iterator onContributor = contributors.iterator();
        while (onContributor.hasNext()) {
          String contributor = (String) onContributor.next();
          Person person = new Person();
          person.setName(contributor);
          // TODO: setEmail, setUri
          contributorList.add(person);
        }
        entry.setContributors(contributorList);
      }

      Set<Category> categories = article.getCategories();
      if (categories != null) {
        List<com.sun.syndication.feed.atom.Category> feedCategoryList = new ArrayList();
        for (Category category : categories) {
          com.sun.syndication.feed.atom.Category feedCategory = new com.sun.syndication.feed.atom.Category();
          feedCategory.setTerm(category.getMainCategory());
          // TODO: what's the URI for our categories
          // feedCategory.setScheme(category.getPid());
          feedCategory.setLabel(category.getMainCategory());
          feedCategoryList.add(feedCategory);

          // subCategory?
          String subCategory = category.getSubCategory();
          if (subCategory != null) {
            subCategory += " (Subcategory)";
            com.sun.syndication.feed.atom.Category feedSubCategory = new com.sun.syndication.feed.atom.Category();
            feedSubCategory.setTerm(subCategory);
            // TODO: what's the URI for our categories
            // feedSubCategory.setScheme();
            feedSubCategory.setLabel(subCategory);
            feedCategoryList.add(feedSubCategory);
          }
        }
        entry.setCategories(feedCategoryList);
      }

      // atom:content
      List <Content> contents = new ArrayList();
      Content description = new Content();
      description.setType("html");
      try {
        description.setValue(transformToHtml(article.getDescription()));
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
   * Representation to return results in.
   */
  public void setRepresentation(final String representation) {
    this.representation = representation;
  }
  /**
   * Transform article XML to HTML for inclusion in the feed
   */
  private String transformToHtml(String description) throws ApplicationException {
    String transformedString = description;
    try {
      final DocumentBuilder builder = createDocBuilder();
      Document desc = builder.parse(new InputSource(new StringReader("<desc>" + description + "</desc>")));
      final DOMSource domSource = new DOMSource(desc);
      final Transformer transformer = getTranslet();
      final Writer writer = new StringWriter();

      transformer.transform(domSource,new StreamResult(writer));
      transformedString = writer.toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }

    // PLoS stylesheet leaves "END_TITLE" as a marker for other processes
    transformedString = transformedString.replace("END_TITLE", "");
    return transformedString;
  }

  /**
   * Get a translet - a compiled stylesheet - for the secondary objects.
   *
   * @return translet
   * @throws TransformerException TransformerException
   * @throws FileNotFoundException FileNotFoundException
   */
  private Transformer getTranslet() throws ApplicationException, TransformerException, FileNotFoundException, URISyntaxException {
    if (toHtmlTranslet == null) {
      // Instantiate the TransformerFactory, and use it with a StreamSource
      // XSL stylesheet to create a translet as a Templates object.
      final TransformerFactory tFactory = TransformerFactory.newInstance();
      final URL resource = getClass().getResource(System.getProperty("secondaryObjectXslTemplate", "/objInfo.xsl"));
      if (resource == null) {
        throw new ApplicationException("Failed to get stylesheet");
      }
      toHtmlTranslet = tFactory.newTemplates(new StreamSource(new File(resource.toURI())));
    }

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return toHtmlTranslet.newTransformer();
  }

  /**
   * Create a DocumentBuilder with a cache aware entity resolver.
   */
  private DocumentBuilder createDocBuilder() throws ParserConfigurationException {
    // Create the builder w/specific entity resolver.
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(CachedSource.getResolver());
    return builder;
  }
}
