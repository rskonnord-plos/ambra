/* $HeadURL::                                                                            $ 
 * $Id::                                                      $
 *
 * Copyright (c) 2006-2009 by Topaz, Inc. http://topazproject.org
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collections;

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
import org.topazproject.ambra.feed.service.FeedService;
import org.topazproject.ambra.feed.service.ArticleFeedCacheKey;
import org.topazproject.ambra.feed.service.FeedService.FEED_TYPES;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.service.XMLService;
import org.jdom.Element;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.models.RatingContent;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.model.article.ArticleType;

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
 * @see       org.topazproject.ambra.feed.service.ArticleFeedCacheKey
 * @see       org.topazproject.ambra.feed.action.FeedAction
 *
 * @author jsuttor
 */
public class AmbraFeedResult extends Feed implements Result {
  private FeedService feedService;
  private AnnotationService annotationService;
  private ReplyService replyService;
  private XMLService secondaryObjectService;

  private static final int    MAX_ANNOTATION_BODY_LENGTH = 512;
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
    ArticleFeedCacheKey cacheKey = (ArticleFeedCacheKey)ai.getStack().findValue("cacheKey");

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

    String xmlBase = (cacheKey.getRelativeLinks() ? "" : JOURNAL_URI);


    FEED_TYPES t =  cacheKey.feedType();

    List<String> articleIds = (List<String>) ai.getStack().findValue("Ids");

    // Add each Article or Annotations as a Feed Entry
    List<Entry> entries = null;

    switch (t) {
      case Annotation:
      case FormalCorrection:
      case MinorCorrection:
      case Retraction:
      case Rating:
      case Trackback:
      case Comment:
        List<Annotation> annotations = annotationService.getAnnotations(articleIds);
        List<String> replyIds = (List<String>) ai.getStack().findValue("ReplyIds");
        List<Reply> replies = replyService.getReplies(replyIds);
        entries = buildAnnotationFeed(xmlBase, annotations, replies, cacheKey.getMaxResults());
        break;
      case Article :
      case Issue :
        List<Article> articles = feedService.getArticles(articleIds);
        entries = buildArticleFeed(cacheKey, xmlBase, articles);
        break;
    }

    setEntries(entries);
    output();
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Annotion Ids found
   * by the query action.
   *
   * @param xmlBase   xml base url
   * @param annotations list of web annotations
   * @param replies list of web replies
   * @param maxResults maximum number of results to display
   * @return List of entries for the feed
   * @throws Exception   Exception
   */
  private List<Entry> buildAnnotationFeed(String xmlBase,
                                          List<Annotation> annotations,
                                          List<Reply> replies,
                                          int maxResults)
      throws Exception {

    // Combine annotations and replies sorted by date
    SortedMap<Date, Annotea> map = new TreeMap<Date, Annotea>(Collections.reverseOrder());

    for (Annotation annotation : annotations) {
      map.put(annotation.getCreated(), annotation);
    }

    for (Reply reply : replies) {
      map.put(reply.getCreated(), reply);
    }

    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    int i = 0;
    for (Annotea annot : map.values()) {

      Entry entry = newEntry(annot);

      List<String> articleIds = new ArrayList<String>();
      if (annot instanceof Reply) {
        Reply reply = (Reply) annot;
        Annotation rootAnnotation = annotationService.getArticleAnnotation(reply.getRoot());
        articleIds.add(rootAnnotation.getAnnotates().toString());
      } else {
        Annotation annotation = (Annotation) annot;
        articleIds.add(annotation.getAnnotates().toString());
      }

      List<Article> art = feedService.getArticles(articleIds);

      // Link to annotation via xmlbase
      Link selfLink = newSelfLink(annot, xmlBase);

      List<Link> altLinks = new ArrayList<Link>();
      altLinks.add(selfLink);

      // Link to article via xmlbase
      selfLink = newSelfLink(art.get(0), xmlBase);
      selfLink.setRel("related");
      altLinks.add(selfLink);

      // Add alternative links to this entry
      entry.setAlternateLinks(altLinks);

      // List will be created by newAuthorsList
      List<Person> authors = new ArrayList<Person>();

      if (annot instanceof Trackback) {
        Trackback trackback = (Trackback) annot;
        if (trackback.getBlog_name() != null) {
          Person person = new Person();
          person.setName(trackback.getBlog_name());
          authors.add(person);
        }
      } else {
        Person person = new Person();
        UserAccount ua = feedService.getUserAcctFrmID(annot.getCreator());
        if (ua != null) {
          person.setName(getUserName(ua));
        } else {
          person.setName("Unknown");
        }
        authors.add(person);
      }

      entry.setAuthors(authors);

      String annotationType = getType(annot);

      List <Content> contents = newAnnotationsList(selfLink, annotationType, getBody(annot));
      entry.setContents(contents);

      List<com.sun.syndication.feed.atom.Category> categories = newCategoryList(annotationType);
      entry.setCategories(categories);

      // Add completed Entry to List
      entries.add(entry);

      // i starts with 1, if maxResults=0 this will not interrupt the loop
      if (++i == maxResults)
        break;
    }
    return entries;
  }

  private String getType(Annotea annot) {
    if (annot instanceof Comment)
      return "Comment";
    if (annot instanceof Rating)
      return "Rating";
    if (annot instanceof Reply)
      return "Reply";
    if (annot instanceof MinorCorrection)
      return "MinorCorrection";
    if (annot instanceof FormalCorrection)
      return "FormalCorrection";
    if (annot instanceof Retraction)
      return "Retraction";
    if (annot instanceof Trackback)
      return "Trackback";
    return null;
  }

  private String getBody(Annotea annot) throws UnsupportedEncodingException {
    String body = "";
    if (annot instanceof ArticleAnnotation) {
      ArticleAnnotation annotation = (ArticleAnnotation) annot;
      if (annotation.getBody() != null && annotation.getBody().getBody() != null)
        body = new String(annotation.getBody().getBody(), annotationService.getEncodingCharset());
    } else if (annot instanceof Reply) {
      Reply reply = (Reply) annot;
      if (reply.getBody() != null && reply.getBody().getBody() != null)
        body = new String(reply.getBody().getBody(), annotationService.getEncodingCharset());
    } else if (annot instanceof Rating) {
      RatingContent content = ((Rating) annot).getBody();
      if (content != null) {
        StringBuilder ratingBody = new StringBuilder();
        ratingBody.append("<div><ul>");
        if (content.getSingleRatingValue() > 0) {
          ratingBody.append("<li>Rating: ")
                    .append(Integer.toString(content.getSingleRatingValue()))
                    .append("</li>");
        } else {
          ratingBody.append("<li>Insight: ")
                    .append(Integer.toString(content.getInsightValue()))
                    .append("</li>")
                    .append("<li>Reliability: ")
                    .append(Integer.toString(content.getReliabilityValue()))
                    .append("</li>")
                    .append("<li>Style: ")
                    .append(Integer.toString(content.getStyleValue()))
                    .append("</li>");
        }
        ratingBody.append("</ul></div>")
                .append(content.getCommentValue());
        body = ratingBody.toString();
      }
    } else if (annot instanceof Trackback) {
      Trackback trackback = (Trackback) annot;
      if (trackback.getBody() != null && trackback.getBody().getExcerpt() != null) {
        body = trackback.getBody().getExcerpt();
      }
    }
    return body;
  }

  private String getUserName(UserAccount ua) {
    StringBuilder name = new StringBuilder();

    if (ua.getProfile().getGivenNames() != null && !ua.getProfile().getGivenNames().equals("")) {
      name.append(ua.getProfile().getGivenNames());
    }

    if (ua.getProfile().getSurnames() != null && !ua.getProfile().getSurnames().equals("")) {
      if (name.length() > 0)
        name.append(' ');
      name.append(ua.getProfile().getSurnames());
    }

    if (name.length() == 0)
      name.append(ua.getProfile().getDisplayName());

    return name.toString();
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Article Ids found
   * by the query action.
   *
   * @param cacheKey    cache/data model
   * @param xmlBase     xml base url
   * @param articles    list of articles
   * @return List of entries for feed.
   */
  private List<Entry> buildArticleFeed(ArticleFeedCacheKey cacheKey, String xmlBase, List<Article> articles) {
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

    //Add the article type to the extended feed element.
    for(URI uri : article.getArticleType()) {
      ArticleType ar = ArticleType.getKnownArticleTypeForURI(uri);
      if (ar != null) {
        Element articleType = new Element("article-type", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());

        articleType.setText(ar.getHeading());
        foreignMarkup.add(articleType);
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
  private List<Content>newContentsList(ArticleFeedCacheKey cacheKey, Article article, String authorNames,
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
        text.append(secondaryObjectService.getTransformedDescription(dc.getDescription()));
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
   * Creates a description of annotation contents in HTML format. Currently the description
   * consists of the Author (or Authors if extended format) and the DublinCore description
   * of the article.
   *
   * @param link        link to the article
   *
   * @param entryTypeDisplay text to describe type of entry : FormalCorrection, Reply ...
   * @param comment Comment
   * @return List<Content> consisting of HTML descriptions of the article and author
   *
   * @throws ApplicationException   ApplicationException
   */

  private List<Content> newAnnotationsList(Link link, String entryTypeDisplay, String comment)
      throws ApplicationException {

    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();
    description.setType("html");

    StringBuilder text = new StringBuilder();
    text.append("<p>");
    if (entryTypeDisplay != null)
      text.append(entryTypeDisplay).append(" on ");

    String body = null;
    if (comment != null)
      body = comment.length() > MAX_ANNOTATION_BODY_LENGTH ?
          comment.substring(0, MAX_ANNOTATION_BODY_LENGTH) + " ..." : comment;

    text.append(" <a href=")
        .append(link.getHref())
        .append('>')
        .append(link.getTitle())
        .append("</a></p>")
        .append("<p>")
        .append(body)
        .append("</p>");
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
  private String newAuthorsList(ArticleFeedCacheKey cacheKey, Article article, List<Person> authors) {
    Citation bc = article.getDublinCore().getBibliographicCitation();
    StringBuilder authorNames = new StringBuilder();

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
            authorNames.append(", ");

          authorNames.append(profile.getRealName());
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
            authorNames.append(", ");

          authorNames.append(profile.getRealName());
        }
        if (authorProfiles.size() > 1)
          person.setName(author + " et al.");
      }
    } else {
      // This should only happen for older, unmigrated articles
      log.warn("No bibliographic citation (is article '" + article.getId() + "' migrated?)");
    }

    return authorNames.toString();
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
  private Link newSelfLink(Annotea annot, String xmlBase) {
    StringBuilder href = new StringBuilder();
    if (annot instanceof Trackback) {
      Trackback trackback = (Trackback) annot;
      href.append(trackback.getUrl().toString());
    } else {

      href.append(xmlBase);
      if (annot instanceof Rating) {
        href.append("rate/getArticleRatings.action?articleURI=")
            .append(((Rating) annot).getAnnotates().toString());
      } else {
        String url;
        if (annot instanceof Reply) {
          url = ((Reply) annot).getRoot();
        } else {
          url = doiToUrl(annot.getId().toString());
        }
        href.append("annotation/listThread.action?inReplyTo=")
            .append(url)
            .append("&root=")
            .append(url);
      }

      // add anchor
      if (annot instanceof Reply || annot instanceof Rating) {
        href.append('#').append(annot.getId().toString());
      }
    }

    Link link = new Link();
    link.setRel("alternate");
    link.setHref(href.toString());
    link.setTitle(getTitle(annot));
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
  private Entry newEntry(Annotea annot) {
    Entry entry = new Entry();

    entry.setId(annot.getId().toString());
    entry.setRights(JOURNAL_COPYRIGHT());

    entry.setTitle(getTitle(annot));

    entry.setPublished(annot.getCreated());
    entry.setUpdated(annot.getCreated());

    return entry;
  }

  private String getTitle(Annotea annot) {
    String title = null;
    if (annot instanceof ArticleAnnotation || annot instanceof Reply) {
      title = annot.getTitle();
    } else if (annot instanceof Rating) {
      Rating rating = (Rating) annot;
      if (rating.getBody() != null) {
        title = rating.getBody().getCommentTitle();
        if (title == null || title.trim().equals(""))
          title = "Rating";
      } else {
        title = "Rating";
      }
    } else if (annot instanceof Trackback) {
      Trackback trackback = (Trackback) annot;
      if (trackback.getBody() != null) {
        title = trackback.getBody().getTitle();
        if (title == null || title.trim().equals(""))
          title = "Trackback";
      } else {
        title = "Trackback";
      }
    }

    return title;
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
  private Link newLink(ArticleFeedCacheKey cacheKey, String uri) {
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
   * @param displayName Name of the entry.
   * @return  List of  atom categories
   */
  public List<com.sun.syndication.feed.atom.Category> newCategoryList(String displayName) {
    List<com.sun.syndication.feed.atom.Category> categories =
        new ArrayList<com.sun.syndication.feed.atom.Category>();
    com.sun.syndication.feed.atom.Category cat = new com.sun.syndication.feed.atom.Category();

    cat.setTerm(displayName);
    cat.setLabel(displayName);
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
  private String newFeedID(ArticleFeedCacheKey cacheKey) {
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
  private String newFeedTitle(ArticleFeedCacheKey cacheKey) {
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
   * @param secondaryObjectService  a set of XML transformation utilities
   */
  @Required
  public void setSecondaryObjectService(XMLService secondaryObjectService) {
    this.secondaryObjectService = secondaryObjectService;
  }

  /**
   * @param  feedService    Article Feed Service
   */
  @Required
  public void setFeedService(FeedService feedService) {
    this.feedService = feedService;
  }

  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  @Required
  public void setReplyService(ReplyService replyService) {
    this.replyService = replyService;
  }
}

