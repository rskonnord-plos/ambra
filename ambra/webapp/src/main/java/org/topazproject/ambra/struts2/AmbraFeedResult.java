/* $HeadURL::                                                                            $ 
 * $Id::                                                      $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc. http://topazproject.org
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

package org.topazproject.ambra.struts2;

import org.apache.struts2.ServletActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.WireFeedOutput;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import java.io.Writer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.ambra.article.action.ArticleFeed;
import org.topazproject.ambra.article.service.ArticleFeedService;
import org.topazproject.ambra.article.service.FeedCacheKey;
import org.topazproject.ambra.article.service.ArticleFeedService.FEED_TYPES;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.util.ArticleXMLUtils;
import org.jdom.Element;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.ApplicationException;

/**
 * The <code>class AmbraFeedResult</code> creates and serializes the query results from the
 * <code>ArticleFeedAction</code> query. Article ID's are the <code>feedcache</code> cache key
 * are accessed via the Struts value stack. The result uses the Article ID's to fetch the relevant
 * articles from the datastore. The cache key contains parameters that were used for the query
 * as well as parameters (title, extend etc) that affect the format of the resulting feed.
 *
 * <h4>Action URI</h4>
 *
 * <h4>Parameters Past via Value Stack</h4>
 * <pre>
 * <strong>
 * Param                                                Description </strong>
 * List&lt;String&gt; IDs      List of article IDs that resulted from the the query.
 *                             Set via the ValueStack <code>ai.getStack().findValue("Ids")</code>
 *                             call
 *
 * ArticleFeed.Key cacheKey    The cache key with input parameters of the request. Some of which
 *                             affect the format of the output. Set via
 *                             <code>ai.getStack().findValue("cacheKey")</code> call
 *
 * </pre>
 *
 * @see       org.topazproject.ambra.article.service.FeedCacheKey
 * @see       ArticleFeed
 *
 * @author jsuttor
 */
public class AmbraFeedResult extends Feed implements Result {
  private List<Article>       articles;
  private List<WebAnnotation> annotations;
  private ArticleFeedService  articleFeedService;
  private ArticleXMLUtils     articleXmlUtils;

  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Log           log = LogFactory.getLog(AmbraFeedResult.class);
  private static final String        ATOM_NS = "http://www.w3.org/2005/Atom";
  private        final String fetchObjectAttachmentAction = "article/fetchObjectAttachment.action";

  // TODO: Plos specific and should be in a resource bundle
  private static final String  URL_DEF = "http://plosone.org/";
  private static final String  PLATFORM_NAME_DEF = "Public Library of Science";
  private static final String  FEED_TITLE_DEF = "PLoS ONE";
  private static final String  TAGLINE_DEF = "Publishing science, accelerating research";
  private static final String  ICON_DEF = "images/favicon.ico";
  private static final String  FEED_ID_DEF = "info:doi/10.1371/feed.pone";
  private static final String  FEED_NS_DEF = "http://www.plos.org/atom/ns#plos";
  private static final String  PREFIX_DEF = "plos";
  private static final String  EMAIL_DEF = "webmaster@plos.org";
  private static final String  COPYRIGHT_DEF = "This work is licensed under a Creative Commons " +
                                               "Attribution-Share Alike 3.0 License, " +
                                               "http://creativecommons.org/licenses/by-sa/3.0/";

  private String JRNL_URI() {
    String URI = CONF.getString("ambra.virtualJournals." + getCurrentJournal() + ".url",
        CONF.getString("ambra.platform.webserver-url", URL_DEF));
    return (URI.endsWith("/")) ? URI: URI + "/";
  }

  private String JOURNAL_NAME() {
    return jrnlConfGetStr("ambra.platform.name", PLATFORM_NAME_DEF);
  }

  private String FEED_TITLE()   {
    return jrnlConfGetStr("ambra.services.feed.title", FEED_TITLE_DEF);
  }

  private String FEED_TAGLINE() {
    return jrnlConfGetStr("ambra.services.feed.tagline", TAGLINE_DEF);
  }

  private String FEED_ICON()    {
    return jrnlConfGetStr("ambra.services.feed.icon", JRNL_URI() + ICON_DEF);
  }

  private String FEED_ID() {
    return jrnlConfGetStr("ambra.services.feed.id", FEED_ID_DEF);
  }

  private String FEED_EXTENDED_NS(){
    return jrnlConfGetStr("ambra.services.feed.extended.namespace", FEED_NS_DEF);
  }

  private String FEED_EXTENDED_PREFIX() {
    return jrnlConfGetStr("ambra.services.feed.extended.prefix", PREFIX_DEF);
  }

  private String JOURNAL_EMAIL_GENERAL(){
    return jrnlConfGetStr("ambra.platform.email.general", EMAIL_DEF);
  }

  private String JOURNAL_COPYRIGHT() {
    return jrnlConfGetStr("ambra.platform.copyright", COPYRIGHT_DEF);
  }

  /**
   *  AmbraFeedResult constructor. Creates a atom_1.0 wire feed with UTF-8 encoding.
   */
  public AmbraFeedResult() {
    super("atom_1.0");
    setEncoding("UTF-8");
  }

  /**
   * Main entry point into the WireFeed result. Once the <code>ArticleFeedAction</code> has
   * preformed the query and provided access to the Article ID's on the value stack it is the
   * Results responsibility to get the article information and construct the actual Atom feed
   * formatted output. The feed result is not currently cached.
   *
   * @param ai  action invocation context
   * @throws Exception
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public void execute(ActionInvocation ai) throws Exception {
    HttpServletRequest request = ServletActionContext.getRequest();
    String pathInfo = request.getPathInfo();

    // URI is either "/" or the pathInfo
    final URI uri = (pathInfo == null) ? URI.create("/") : URI.create(pathInfo);

    String JOURNAL_URI = JRNL_URI();
    setXmlBase(JRNL_URI());

    // Get the article IDs that were cached by the feed.
    FeedCacheKey cacheKey = (FeedCacheKey)ai.getStack().findValue("cacheKey");
    List<String> articleIds = (List<String>) ai.getStack().findValue("Ids");
    FEED_TYPES t =  cacheKey.feedType();

    switch (t) {
      case Annotation:
      case FormalCorrectionAnnot:
      case MinorCorrectionAnnot:
      case CommentAnnot:
        annotations = articleFeedService.getAnnotations(articleIds);
        break;
      case Article :
        articles = articleFeedService.getArticles(articleIds);
    }

    String xmlBase = (cacheKey.getRelativeLinks() ? "" : JOURNAL_URI);

    List<Link> otherLinks = new ArrayList<Link>();

    // Add the self link
    otherLinks.add(newLink(cacheKey, uri.toString() ));
    setOtherLinks(otherLinks);
    setId(newFeedID(cacheKey));
    setTitle(newFeedTitle(cacheKey));

    Content tagline = new Content();

    tagline.setValue(FEED_TAGLINE());
    setTagline(tagline);
    setUpdated(new Date());
    setIcon(FEED_ICON());
    setLogo(FEED_ICON());
    setCopyright(JOURNAL_COPYRIGHT());

    List<Person> feedAuthors = new ArrayList<Person>();

    feedAuthors.add(plosPerson());
    setAuthors(feedAuthors);

    // Add each Article as a Feed Entry
    List<Entry> entries = null;

    switch (t) {
      case Annotation:
      case FormalCorrectionAnnot:
      case MinorCorrectionAnnot:
      case CommentAnnot:
        entries = buildAnnotationFeed(xmlBase);
        break;
      case Article :
        entries = buildArticleFeed(cacheKey, xmlBase);
    }

    setEntries(entries);
    output();
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Annotion Ids found
   * by the query action.
   *
   * @param xmlBase   xml base url
   * @return List of entries for the feed
   * @throws Exception   Exception
   */
  private List<Entry> buildAnnotationFeed(String xmlBase) throws Exception {
    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    for (WebAnnotation annot : annotations) {
      Entry entry = newEntry(annot);
      List<String> ids = new ArrayList<String>();
      List<Article> art;
      List<Link> altLinks = new ArrayList<Link>();

      ids.add(annot.getAnnotates());
      art = articleFeedService.getArticles(ids);

      // Link to annotation via xmlbase
      Link selfLink = newSelfLink(annot, xmlBase);
      altLinks.add(selfLink);

      // Link to article via xmlbase
      selfLink = newSelfLink(art.get(0), xmlBase);
      selfLink.setRel("related");
      altLinks.add(selfLink);

      // Add alternative links to this entry
      entry.setAlternateLinks(altLinks);

      // List will be created by newAuthorsList
      List<Person> authors = new ArrayList<Person>();
      Person person = new Person();

      UserAccount ua = articleFeedService.getUserAcctFrmID(annot.getCreator());
      if (ua != null)
        person.setName(ua.getProfile().getDisplayName());
      else
        person.setName("Unknown");

      authors.add(person);
      entry.setAuthors(authors);

      List <Content> contents = newAnnotationsList(annot, selfLink);
      entry.setContents(contents);

      List<com.sun.syndication.feed.atom.Category> categories = newCategoryList(annot);
      entry.setCategories(categories);

      // Add completed Entry to List
      entries.add(entry);
    }
    return entries;
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Article Ids found
   * by the query action.
   *
   * @param cacheKey   cache/data model
   * @param xmlBase     xml base url
   * @return List of entries for feed.
   */
  private List<Entry> buildArticleFeed(FeedCacheKey cacheKey, String xmlBase) {
    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    for (Article article : articles) {
      /*
       * Article may be removed by the time
       * it a actually retrieved. A null
       * may be the result so skip.
       */
      if (article == null)
        continue;

      Entry entry = newEntry(article);

      List<Link> altLinks = new ArrayList<Link>();

      // Link to article via xmlbase
      Link selfLink = newSelfLink(article, xmlBase);
      altLinks.add(selfLink);

      // Get a list of alternative representations of the article
      Set<Representation> representations = article.getRepresentations();

      // Add alternate links for each representation of the article
      if (representations != null) {
        for (Representation rep : representations) {
          Link altLink = newAltLink(article, rep, xmlBase);
          altLinks.add(altLink);
        }
      }
      // Add alternative links to this entry
      entry.setAlternateLinks(altLinks);

      // List will be created by newAuthorsList
      List<Person> authors = new ArrayList<Person>();

      // Returns Comma delimited string of author names and Adds People to the authors list.
      String authorNames = newAuthorsList(cacheKey, article, authors);
      entry.setAuthors(authors);

      // Get a list of contributors and add them to the list
      List<Person> contributors = newContributorsList(article);
      entry.setContributors(contributors);

      // Add foreign markup
      if (cacheKey.isExtended()) {
        // All our foreign markup
        List<Element> foreignMarkup = newForeignMarkUp(article);

        if (foreignMarkup.size() > 0) {
          entry.setForeignMarkup(foreignMarkup);
        }
      }

      List <Content> contents = newContentsList(cacheKey, article, authorNames, authors.size());
      entry.setContents(contents);

      // Add completed Entry to List
      entries.add(entry);
    }
    return entries;
  }

  /**
   * Creates a <code>List&lt;Element&gt;</code> that consist of foreign markup elements.
   * In this case the elements created consist of volume, issue category information.
   *
   * @param article   the article
   * @return <code>List&lt;Elements&gt;</code> of foreign markup elements with
   *         issue, volume and category information
   */
  private List<Element> newForeignMarkUp(Article article) {
    // All our foreign markup
    List<Element> foreignMarkup = new ArrayList<Element>();
    Citation bc = article.getDublinCore().getBibliographicCitation();

    // Volume & issue
    if (bc != null) {
      // Add volume
      if (bc.getVolume() != null) {
        Element volume = new Element("volume", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
        volume.setText(bc.getVolume());
        foreignMarkup.add(volume);
      }
      // Add issue
      if (bc.getIssue() != null) {
        Element issue = new Element("issue", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
        issue.setText(bc.getIssue());
        foreignMarkup.add(issue);
      }
    }

    Set<Category> categories = article.getCategories();

    if (categories != null) {
      for (Category category : categories) {
        Element feedCategory = new Element("category", ATOM_NS);

        feedCategory.setAttribute("term", category.getMainCategory());
        feedCategory.setAttribute("label", category.getMainCategory());

        String subCategory = category.getSubCategory();

        if (subCategory != null) {
          Element feedSubCategory = new Element("category", ATOM_NS);

          feedSubCategory.setAttribute("term", subCategory);
          feedSubCategory.setAttribute("label", subCategory);
          feedCategory.addContent(feedSubCategory);
        }

        foreignMarkup.add(feedCategory);
      }
    }

    return  foreignMarkup;
  }

  /**
   * Creates a description of article contents in HTML format. Currently the description consists of
   * the Author (or Authors if extended format) and the DublinCore description of the article.
   *
   * @param cacheKey    cache key and input parameters
   * @param article     the article
   * @param authorNames string concatenated list of author names
   * @param numAuthors  author count
   * @return List<Content> consisting of HTML descriptions of the article and author
   */
  private List<Content>newContentsList(FeedCacheKey cacheKey, Article article, String authorNames,
      int numAuthors) {
    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();
    DublinCore dc = article.getDublinCore();

    description.setType("html");

    try {
      StringBuilder text = new StringBuilder();

      // If this is a normal feed (not extended) and there's more than one author, add to content
      if ((!cacheKey.isExtended()) && numAuthors > 1) {
        text.append("<p>by ").append(authorNames).append("</p>\n");
      }

      if (dc.getDescription() != null) {
        text.append(articleXmlUtils.transformArticleDescriptionToHtml(dc.getDescription()));
      }
      description.setValue(text.toString());

    } catch (Exception e) {
      log.error(e);
      description.setValue("<p>Internal server error.</p>");
    }
    contents.add(description);

    return contents;
  }

  /**
   * Creates a description of article contents in HTML format. Currently the description consists of
   * the Author (or Authors if extended format) and the DublinCore description of the article.
   *
   * @param annotation  annotation to convert into Content element
   * @param link        link to the article
   *
   * @return List<Content> consisting of HTML descriptions of the article and author
   *
   * @throws ApplicationException   ApplicationException
   */

  private List<Content>newAnnotationsList(WebAnnotation annotation, Link link)
                         throws ApplicationException {
    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();
    String displayName = annotation.getDisplayName();
    description.setType("html");

    StringBuilder text = new StringBuilder();
    text.append("<p>");
    if (displayName != null) {
      String d = displayName + " on ";
      text.append(d);
    }

    {
      String d = " <a href=" + link.getHref() + ">" + link.getTitle() + "</a></p>";
      text.append(d);
      d = "<p>" + annotation.getComment() + "</p>";
      text.append(d);
    }
    description.setValue(text.toString());
    contents.add(description);

    return contents;
  }

  /**
   * Create a <code>List&lt;Person&gt;</code> of DublinCore specified that contributed to this
   * article.
   *
   * @param article the article
   * @return <code>List&lt;Person&gt;</code> of contributors to the article.
   */
  private List<Person> newContributorsList(Article article) {
    List<Person> newContributors = new ArrayList<Person>();
    Set<String> contributors = article.getDublinCore().getContributors();

    for (String contributor: contributors) {
      Person person = new Person();
      person.setName(contributor);
      newContributors.add(person);
    }

    return newContributors;
  }

  /**
   * This routine creates and returns a List&lt;Person&gt; authors listed in DublinCore for the
   * article.
   *
   * @param cacheKey  cache key and input parameters
   * @param article   the article
   * @param authors   modified and returned <code>List&lt;Person&gt;</code> of article authors.
   * @return String of authors names.
   */
  private String newAuthorsList(FeedCacheKey cacheKey, Article article, List<Person> authors) {
    Citation bc = article.getDublinCore().getBibliographicCitation();
    String authorNames = "";

    if (bc != null) {
      List<UserProfile> authorProfiles = bc.getAuthors();

      if (cacheKey.isExtended()) {
        /* If extended then create a list of persons
         * containing all the authors.
         */
        for (UserProfile profile: authorProfiles) {
          Person person = new Person();
          person.setName(profile.getRealName());
          authors.add(person);

          if (authorNames.length() > 0)
            authorNames += ", ";

          authorNames += profile.getRealName();
        }
      } else if (authorProfiles.size() >= 1) {
        // Not extended therefore there will only be one author.
        Person person = new Person();
        String author = authorProfiles.get(0).getRealName();

        person.setName(author);
        authors.add(person);
        // Build a comma delimitted list of author names
        for (UserProfile profile: authorProfiles) {

          if (authorNames.length() > 0)
            authorNames += ", ";

          authorNames += profile.getRealName();
        }
        if (authorProfiles.size() > 1)
          person.setName(author + " et al.");
      }
    } else {
      // This should only happen for older, unmigrated articles
      log.warn("No bibliographic citation (is article '" + article.getId() + "' migrated?)");
    }

    return authorNames;
  }

  /**
   * Create alternate link for the different representaions of the article.
   *
   * @param article the article
   * @param rep     a respresentation of the article
   * @param xmlBase XML base
   * @return  Link  an alternate link to the article
   */
  private Link newAltLink(Article article, Representation rep, String xmlBase) {
    Link altLink = new Link();
    DublinCore dc = article.getDublinCore();

    altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + dc.getIdentifier() +
        "&representation=" + rep.getName());
    altLink.setRel("related");
    altLink.setTitle("(" + rep.getName() + ") " + dc.getTitle());
    altLink.setType(rep.getContentType());

    return altLink;
  }

  /**
   * Create a link to the annotation itself.
   *
   * @param annot  the annotation
   * @param xmlBase  xml base of article
   * @return  link to the article
   */
  private Link newSelfLink(WebAnnotation annot, String xmlBase) {
    Link link = new Link();
    String href = xmlBase + "annotation/listThread.action?inReplyTo=";
    String url = doiToUrl(annot.getId());

    link.setRel("alternate");
    link.setHref(href + url + "&root=" + url);
    link.setTitle(annot.getCommentTitle());
    return link;
  }

  /**
   * Create a link to the article itself.
   *
   * @param article  the article
   * @param xmlBase  xml base of article
   * @return  link to the article
   */
  private Link newSelfLink(Article article, String xmlBase) {
    Link link = new Link();
    DublinCore dc = article.getDublinCore();
    String url = doiToUrl(dc.getIdentifier());

    link.setRel("alternate");
    link.setHref(xmlBase + "article/" + url);
    link.setTitle(dc.getTitle());
    return link;
  }

  /**
   * Create a feed entry with Id, Rights, Title, Published and Updated set.
   *
   * @param article  the article
   * @return Entry  feed entry
   */
  private Entry newEntry(Article article) {
    Entry entry = new Entry();
    DublinCore dc = article.getDublinCore();

    entry.setId(dc.getIdentifier());

    // Respect Article specific rights
    String rights = dc.getRights();
    if (rights != null) {
      entry.setRights(rights);
    } else {
      // Default is CC BY SA 3.0
      entry.setRights(JOURNAL_COPYRIGHT());
    }

    entry.setTitle(dc.getTitle());
    entry.setPublished(dc.getAvailable());
    entry.setUpdated(dc.getAvailable());

    return entry;
  }

  /**
   * Create a feed entry with Id, Rights, Title, Published and Updated set.
   *
   * @param annot an annotation
   * @return Entry  feed entry
   */
  private Entry newEntry(WebAnnotation annot) {
    Entry entry = new Entry();

    entry.setId(annot.getId());
    entry.setRights(JOURNAL_COPYRIGHT());

    entry.setTitle(annot.getCommentTitle());
    entry.setPublished(annot.getCreatedAsDate());
    entry.setUpdated(annot.getCreatedAsDate());

    return entry;
  }

  /**
   * Create a default Plos person element.
   *
   * @return Person with journal email, journal name, journal URI.
   */
  private Person plosPerson() {
    Person plos = new Person();

    plos.setEmail(JOURNAL_EMAIL_GENERAL());
    plos.setName(JOURNAL_NAME());
    plos.setUri(JRNL_URI());

    return plos;
  }

  /**
   * If a self link was provided by the user create a <code>Link</code> based on the user input
   * information contained in the cachekey.
   *
   * @param cacheKey cache and data model
   * @param uri      uri of regquest
   *
   * @return <code>Link</code> user provide link.
   */
  private Link newLink(FeedCacheKey cacheKey, String uri) {
    if (cacheKey.getSelfLink() == null || cacheKey.getSelfLink().equals("")) {
      if (uri.startsWith("/")) {
        cacheKey.setSelfLink(JRNL_URI().substring(0, JRNL_URI().length() - 1) + uri);
      } else {
        cacheKey.setSelfLink(JRNL_URI() + uri);
      }
    }

    Link newLink = new Link();
    newLink.setRel("self");
    newLink.setHref(cacheKey.getSelfLink());
    newLink.setTitle(FEED_TITLE());

    return newLink;
  }

  /**
   * Build an atom feed categroy list for for the WebAnnotation.
   *
   * @param annotation build category element from annotation
   *
   * @return  List of  atom categories
   */
  public List<com.sun.syndication.feed.atom.Category> newCategoryList(WebAnnotation annotation) {
    List<com.sun.syndication.feed.atom.Category> categories =
        new ArrayList<com.sun.syndication.feed.atom.Category>();
    com.sun.syndication.feed.atom.Category cat = new com.sun.syndication.feed.atom.Category();

    cat.setTerm(annotation.getDisplayName());
    cat.setLabel(annotation.getDisplayName());
    categories.add(cat);

    return categories;
  }

  /**
   * Creates a Feed ID from the Config File value + key.Category + key.Author
   *
   * @param cacheKey cache key and input parameters
   *
   * @return String identifier generated for this feed
   */
  private String newFeedID(FeedCacheKey cacheKey) {
    String id = FEED_ID();
    if (cacheKey.getCategory() != null && cacheKey.getCategory().length() > 0)
      id += "?category=" + cacheKey.getCategory();
    if (cacheKey.getAuthor() != null)
      id += "?author=" + cacheKey.getAuthor();

    return id;
  }

  /**
   * Create a feed Title string from the the key.Category and key.Author fields in the cache entry.
   *
   * @param cacheKey cache key and input parameters
   * @return String feed title.
   */
  private String newFeedTitle(FeedCacheKey cacheKey) {
    String feedTitle = cacheKey.getTitle();

    if (feedTitle == null) {
      feedTitle = FEED_TITLE();

      if (cacheKey.getCategory() != null && cacheKey.getCategory().length() > 0)
        feedTitle += " - Category " + cacheKey.getCategory();
      if (cacheKey.getAuthor() != null)
        feedTitle += " - Author " + cacheKey.getAuthor();
    }
    return  feedTitle;
  }

  /**
   * Serialize and output the feed information.
   *
   * @throws IOException if the ouput fails to write
   */
  private void output() throws IOException {
    // work w/HTTP directly, avoid WebWorks interactions
    HttpServletResponse httpResp = ServletActionContext.getResponse();

    httpResp.setContentType("application/atom+xml");
    httpResp.setCharacterEncoding(this.getEncoding());

    Writer respStrm = httpResp.getWriter();
    WireFeedOutput respOut = new WireFeedOutput();

    try {
      respOut.output(this, respStrm);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      // respOut.output throws an exception if respStrm is null
      respStrm.close();
    }
  }

  /**
   * Convert an doi to Trl encoding if possible.
   * @param doi  the doi to URL encode
   * @return URl encoded doi or the original doi if not UTF-8.
   */
  private String doiToUrl(String doi) {
    String url = doi;

    try {
      url = URLEncoder.encode(url, "UTF-8");
    } catch(UnsupportedEncodingException uee) {
      log.error("UTF-8 not supported?", uee);
    }
    return url;
  }

  /**
   * Get a String from the Configuration looking first for a Journal override.
   *
   * @param key          to lookup.
   * @param defaultValue if key is not found.
   * @return value for key.
   */
  private String jrnlConfGetStr( String key, String defaultValue) {
    String path = "ambra.virtualJournals." + getCurrentJournal() + "." + key;
    return CONF.getString(path, CONF.getString(key, defaultValue));
  }

  /**
   * Get the journal name.
   *
   * @return  the name of the current journal
   */
  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
        getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * articleXmlUtils provide methods to manipulate the XML
   * of the articles (transformations etc)
   *
   * @param articleXmlUtils  a set of XML transformation utilities
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }

  /**
   * @param  articleFeedService    Article Feed Service
   */
  public void setArticleFeedService(ArticleFeedService articleFeedService) {
    this.articleFeedService = articleFeedService;
  }
}

