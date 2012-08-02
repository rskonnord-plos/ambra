/* $HeadURL::                                                                            $
 * $Id::                                                      $
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

package org.ambraproject.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.WireFeedOutput;
import org.ambraproject.ApplicationException;
import org.ambraproject.article.service.NoSuchObjectIdException;
import org.ambraproject.feed.service.FeedSearchParameters;
import org.ambraproject.feed.service.FeedService;
import org.ambraproject.feed.service.FeedService.FEED_TYPES;
import org.ambraproject.model.article.ArticleInfo;
import org.ambraproject.model.article.ArticleType;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.rating.service.RatingsService;
import org.ambraproject.service.XMLService;
import org.ambraproject.util.TextUtils;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.LinkbackView;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The <code>class AmbraFeedResult</code> creates and serializes the query results from the
 * <code>ArticleFeedAction</code> query. Values are accessed via the Struts value stack.
 *
 * @author jsuttor
 * @author russ
 * @author Joe Osowski
 * @see org.ambraproject.feed.service.FeedSearchParameters
 * @see org.ambraproject.feed.action.FeedAction
 */
public class AmbraFeedResult extends Feed implements Result {
  private FeedService feedService;
  private RatingsService ratingsService;
  private XMLService secondaryObjectService;

  private boolean includeformatting = false;

  private static final int MAX_ANNOTATION_BODY_LENGTH = 512;
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Logger log = LoggerFactory.getLogger(AmbraFeedResult.class);
  private static final String ATOM_NS = "http://www.w3.org/2005/Atom";
  private final String fetchObjectAttachmentAction = "article/fetchObjectAttachment.action";
  private final String RATING_TITLE = "Rating";

  // TODO: Should be in a resource bundle
  private static final String URL_DEF = "http://ambraproject.org/";
  private static final String PLATFORM_NAME_DEF = "Ambra";
  private static final String FEED_TITLE_DEF = "Ambra";
  private static final String TAGLINE_DEF = "Publishing science, accelerating research";
  private static final String ICON_DEF = "images/favicon.ico";
  private static final String FEED_ID_DEF = "info:doi/12.3456/feed.ambr";
  private static final String FEED_NS_DEF = "http://www.plos.org/atom/ns#plos";
  private static final String PREFIX_DEF = "plos";
  private static final String EMAIL_DEF = "webmaster@example.org";
  private static final String COPYRIGHT_DEF = "This work is licensed under a Creative Commons " +
      "Attribution-Share Alike 3.0 License, " +
      "http://creativecommons.org/licenses/by-sa/3.0/";

  private String JRNL_URI() {
    StringBuilder uri = new StringBuilder();
    HttpServletRequest request = ServletActionContext.getRequest();
    String pathInfo = request.getContextPath();

    uri.append(CONF.getString("ambra.virtualJournals." + getCurrentJournal() + ".url",
        CONF.getString("ambra.platform.webserver-url", URL_DEF)));

    if (pathInfo != null) {
      uri.append(pathInfo);
    }

    String journalURI = uri.toString();

    return (journalURI.endsWith("/")) ? journalURI : journalURI + "/";
  }

  private String JOURNAL_NAME() {
    return jrnlConfGetStr("ambra.platform.name", PLATFORM_NAME_DEF);
  }

  private String FEED_TITLE() {
    return jrnlConfGetStr("ambra.services.feed.title", FEED_TITLE_DEF);
  }

  private String FEED_TAGLINE() {
    return jrnlConfGetStr("ambra.services.feed.tagline", TAGLINE_DEF);
  }

  private String FEED_ICON() {
    return JRNL_URI() + jrnlConfGetStr("ambra.services.feed.icon", ICON_DEF);
  }

  private String FEED_ID() {
    return jrnlConfGetStr("ambra.services.feed.id", FEED_ID_DEF);
  }

  private String FEED_EXTENDED_NS() {
    return jrnlConfGetStr("ambra.services.feed.extended.namespace", FEED_NS_DEF);
  }

  private String FEED_EXTENDED_PREFIX() {
    return jrnlConfGetStr("ambra.services.feed.extended.prefix", PREFIX_DEF);
  }

  private String JOURNAL_EMAIL_GENERAL() {
    return jrnlConfGetStr("ambra.platform.email.general", EMAIL_DEF);
  }

  private String JOURNAL_COPYRIGHT() {
    return jrnlConfGetStr("ambra.platform.copyright", COPYRIGHT_DEF);
  }

  /**
   * AmbraFeedResult constructor. Creates a atom_1.0 wire feed with UTF-8 encoding.
   */
  public AmbraFeedResult() {
    super("atom_1.0");
    setEncoding("UTF-8");
  }

  /**
   * Main entry point into the WireFeed result. Once the <code>ArticleFeedAction</code> has preformed the query and
   * provided access to the Article ID's on the value stack it is the Results responsibility to get the article
   * information and construct the actual Atom feed formatted output. The feed result is not currently cached.
   *
   * @param ai action invocation context
   * @throws Exception
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public void execute(ActionInvocation ai) throws Exception {
    HttpServletRequest request = ServletActionContext.getRequest();
    String pathInfo = request.getPathInfo();

    if (request.getParameter("includeformatting") != null) {
      if (request.getParameter("includeformatting").equals("true")) {
        includeformatting = true;
      }
    }

    // URI is either "/" or the pathInfo
    final URI uri = (pathInfo == null) ? URI.create("/") : URI.create(pathInfo);

    String JOURNAL_URI = JRNL_URI();
    setXmlBase(JRNL_URI());

    // Get the article IDs that were cached by the feed.
    FeedSearchParameters searchParams = (FeedSearchParameters) ai.getStack().findValue("searchParameters");

    List<Link> otherLinks = new ArrayList<Link>();

    // Add the self link
    otherLinks.add(newLink(searchParams, uri.toString()));
    setOtherLinks(otherLinks);
    setId(newFeedID(searchParams));
    setTitle(newFeedTitle(searchParams));

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

    String xmlBase = (searchParams.getRelativeLinks() ? "" : JOURNAL_URI);

    FEED_TYPES t = searchParams.feedType();

    List<ArticleInfo> articles = (List<ArticleInfo>) ai.getStack().findValue("Articles");

    // Add each Article or Annotations as a Feed Entry
    List<Entry> entries = null;
    List<LinkbackView> trackbacks = null;

    switch (t) {
      case Annotation:
        trackbacks = (List<LinkbackView>) ai.getStack().findValue("trackbacks");
      case FormalCorrection:
      case Note:
      case MinorCorrection:
      case Retraction:
      case Comment:
      case Rating:
      case Reply:
        List<AnnotationView> annotations = (List<AnnotationView>) ai.getStack().findValue("annotations");
        entries = buildAnnotationFeed(xmlBase, annotations, trackbacks, searchParams.getMaxResults(),
            searchParams.getFormatting());
        break;
      case Trackback:
        trackbacks = (List<LinkbackView>) ai.getStack().findValue("trackbacks");
        entries = buildAnnotationFeed(xmlBase, null, trackbacks, searchParams.getMaxResults(), searchParams.getFormatting());
        break;
      case Article:
        Document solrResult = (Document) ai.getStack().findValue("ResultFromSolr");
        entries = buildArticleFeed(searchParams, xmlBase, solrResult);
        break;
      case Issue:
        //I assume here the feed is anonymous and unpublished articles will never be
        //included, If this isn't correct, a method needs to be added to fetch the
        //current user ID from the session
        entries = buildIssueFeed(searchParams, xmlBase, articles);
        break;
    }

    setEntries(entries);
    output();
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Annotion Ids found by the query action.
   *
   * @param xmlBase     xml base url
   * @param annotations list of web annotations
   * @param trackbacks  list of trackbacks
   * @param maxResults  maximum number of results to display
   * @param formatting  if this parameter has the value FeedService.FEED_FORMATTING_COMPLETE, then display the entire
   *                    text of every available field
   * @return List of entries for the feed
   * @throws Exception Exception
   */
  private List<Entry> buildAnnotationFeed(String xmlBase,
                                          List<AnnotationView> annotations,
                                          List<LinkbackView> trackbacks,
                                          int maxResults,
                                          String formatting)
      throws Exception {

    // Combine annotations and trackbacks sorted by date
    SortedMap<Date, Object> map = new TreeMap<Date, Object>(Collections.reverseOrder());

    if (annotations != null) {
      for (AnnotationView annotation : annotations) {
        if (annotation.getType().equals(AnnotationType.REPLY)) {
          //AnnotationView rootAnnotation = getAnnotationRoot(annotation);
          map.put(annotation.getCreated(), annotation);
        } else {
          map.put(annotation.getCreated(), annotation);
        }

      }
    }

    if (trackbacks != null) {
      for (LinkbackView trackback : trackbacks) {
        map.put(trackback.getCreated(), trackback);
      }
    }

    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    int i = 0;
    for (Object view : map.values()) {
      entries.add(newEntry(view, xmlBase, formatting));

      // i starts with 1, if maxResults=0 this will not interrupt the loop
      if (++i == maxResults)
        break;
    }
    return entries;
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the Article Ids found by the query action.
   *
   * @param searchParams cache/data model
   * @param xmlBase      xml base url
   * @param articles     list of articles
   * @return List of entries for feed.
   * @throws NoSuchObjectIdException When an article does not exist
   */
  private List<Entry> buildIssueFeed(FeedSearchParameters searchParams, String xmlBase, List<ArticleInfo> articles)
      throws NoSuchObjectIdException {
    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    for (ArticleInfo article : articles) {
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
      Link selfLink = newArticleLink(article.getDoi(), article.getTitle(), xmlBase);
      altLinks.add(selfLink);

      //Assume here that each article has an XML and PDF representation
      altLinks.add(newAltLink(article, "XML", xmlBase));
      altLinks.add(newAltLink(article, "PDF", xmlBase));

      // Add alternative links to this entry
      entry.setAlternateLinks(altLinks);

      // List will be created by newAuthorsList
      List<Person> authors = new ArrayList<Person>();

      // Returns Comma delimited string of author names and Adds People to the authors list.
      String authorNames = newAuthorsList(searchParams.isExtended(), article, authors);
      entry.setAuthors(authors);

      // Add foreign markup
      if (searchParams.isExtended()) {
        // All our foreign markup
        List<Element> foreignMarkup = newForeignMarkUp(article);

        if (foreignMarkup.size() > 0) {
          entry.setForeignMarkup(foreignMarkup);
        }
      }

      List<Content> contents = newContentsList(searchParams, article, authorNames);
      entry.setContents(contents);

      // Add completed Entry to List
      entries.add(entry);
    }
    return entries;
  }

  /**
   * Build a <code>List&lt;Entry&gt;</code> from the articles found from solr
   *
   * @param searchParams data model
   * @param xmlBase      xml base url
   * @param result       list of articles
   * @return List of entries for the feed
   */
  private List<Entry> buildArticleFeed(FeedSearchParameters searchParams, String xmlBase, Document result) {
    // Add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();

    // default is 2pm local time
    int publishTime = CONF.getInt("ambra.services.feed.publishTime", 14);

    NodeList nodes = result.getElementsByTagName("result");
    NodeList docs = null;
    // there should be only one 
    if (nodes.getLength() == 1) {
      Node node = nodes.item(0);
      docs = node.getChildNodes();
    }

    // looping through children of result element
    for (int i = 0; i < docs.getLength(); i++) {
      // doc element
      Node doc = docs.item(i);

      Entry entry = new Entry();

      String volume = null;
      String issue = null;
      String articleType = null;
      String abstractText = null;
      String copyright = null;
      NodeList authorsForContent = null;
      Node subjectHierarchyNode = null;

      // children elements of doc element
      NodeList fields = doc.getChildNodes();

      for (int j = 0; j < fields.getLength(); j++) {
        Node field = fields.item(j);
        NamedNodeMap nnm = field.getAttributes();
        Node attrNameNode = nnm.getNamedItem("name");
        String attrName = attrNameNode.getNodeValue();

        if (attrName.equals("id")) {

          // id
          entry.setId("info:doi/" + field.getTextContent());

        } else if (attrName.equals("copyright")) {

          // rights
          copyright = field.getTextContent();

        } else if (attrName.equals("publication_date")) {

          // published and updated dates

          // the only values we care about are the month, day and year
          String date = field.getTextContent();
          int year = Integer.valueOf(date.substring(0, 4));
          int month = Integer.valueOf(date.substring(5, 7));
          int day = Integer.valueOf(date.substring(8, 10));

          // we want the local time zone
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.YEAR, year);
          // month value is 0 based
          cal.set(Calendar.MONTH, month - 1);
          cal.set(Calendar.DAY_OF_MONTH, day);
          cal.set(Calendar.HOUR_OF_DAY, publishTime);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);

          entry.setPublished(cal.getTime());
          entry.setUpdated(cal.getTime());

        } else if (attrName.equals("title_display")) {

          // title
          if (includeformatting) {
            Content title = new Content();
            title.setType("html");
            title.setValue(field.getTextContent());
            entry.setTitleEx(title);
          } else {
            entry.setTitle(TextUtils.simpleStripAllTags(field.getTextContent()));
          }

        } else if (attrName.equals("author_display")) {

          // display ALL the authors in the content element
          // authors and collab authors
          authorsForContent = field.getChildNodes();

        } else if (attrName.equals("author_without_collab_display")) {

          // authors (without the collaborative authors)
          ArrayList<Person> authors = newAuthorsList(searchParams, field);
          entry.setAuthors(authors);

        } else if (attrName.equals("author_collab_only_display")) {

          // contributors (collaborative authors)
          List<Person> contributors = new ArrayList<Person>();
          NodeList children = field.getChildNodes();
          for (int k = 0; k < children.getLength(); k++) {
            Node child = children.item(k);
            Person contributor = new Person();
            contributor.setName(child.getTextContent());
            contributors.add(contributor);
          }
          entry.setContributors(contributors);

        } else if (attrName.equals("volume")) {

          // volume (used for ForeignMarkup)
          volume = field.getTextContent();

        } else if (attrName.equals("issue")) {

          // issue (used for ForeignMarkup)
          issue = field.getTextContent();

        } else if (attrName.equals("article_type")) {

          // article type (used for ForeignMarkup)
          articleType = field.getTextContent();

        } else if (attrName.equals("subject_hierarchy")) {
          subjectHierarchyNode = field;

        } else if (attrName.equals("abstract_primary_display")) {

          // abstract (used in Contents)
          abstractText = field.getTextContent();
        }
      }

      // foreign markup
      if (searchParams.isExtended()) {
        List<Element> foreignMarkup = newForeignMarkUp(subjectHierarchyNode, volume, issue, articleType);
        if (foreignMarkup.size() > 0) {
          entry.setForeignMarkup(foreignMarkup);
        }
      }

      // alternative links
      List<Link> altLinks = newAltLinks(entry.getId(), xmlBase, entry.getTitle());
      // Add alternative links to this entry
      entry.setAlternateLinks(altLinks);

      // contents 
      List<Content> contents = newContentsList(searchParams, authorsForContent, abstractText);
      entry.setContents(contents);

      // rights
      if (copyright != null) {
        entry.setRights(copyright);
      } else {
        // Default is CC BY SA 3.0
        entry.setRights(JOURNAL_COPYRIGHT());
      }

      // Add completed Entry to List
      entries.add(entry);
    }
    return entries;
  }

  /**
   * Create a feed entry with Id, Rights, Title, Published and Updated set.
   *
   * @param object an AnnotationView or Trackback
   * @return Entry feed entry
   * @throws UnsupportedEncodingException
   */
  private Entry newEntry(Object object, String xmlBase, String formatting)
      throws UnsupportedEncodingException, ApplicationException {
    Entry entry = new Entry();

    List<Link> altLinks = new ArrayList<Link>();
    List<Person> authors = new ArrayList<Person>();
    String annotationType = null;
    Link selfLink = null;
    Link relLink = null;

    if (object instanceof AnnotationView) {
      AnnotationView view = (AnnotationView) object;
      entry.setId("info:doi/" + view.getAnnotationUri());
      entry.setPublished(view.getCreated());
      entry.setUpdated(view.getCreated());

      selfLink = newSelfLink(view, xmlBase);
      relLink = newArticleLink(view.getArticleDoi(), view.getArticleTitle(), xmlBase);

      Person person = new Person();
      person.setName(view.getCreatorFormattedName());
      authors.add(person);

      annotationType = view.getType().toString();

      if (view.getType() == AnnotationType.RATING) {
        if (view.getTitle() == null) {
          entry.setTitle(RATING_TITLE);
        } else {
          entry.setTitle(view.getOriginalTitle());
        }
      } else {
        entry.setTitle(view.getOriginalTitle());
      }
    } else if (object instanceof LinkbackView) {
      LinkbackView trackback = (LinkbackView) object;

      selfLink = newSelfLink(trackback, xmlBase);
      relLink = newArticleLink(trackback.getArticleDoi(), trackback.getArticleTitle(), xmlBase);

      Person person = new Person();
      person.setName(trackback.getBlogName());
      authors.add(person);

      entry.setId(trackback.getUrl());
      entry.setPublished(trackback.getCreated());
      entry.setUpdated(trackback.getCreated());
      entry.setTitle(trackback.getTitle());
      annotationType = "Trackback";
    } else {
      throw new RuntimeException("Unandled class of " + object.getClass().toString() + " recieved.");
    }

    selfLink.setRel("alternate");
    relLink.setRel("related");

    // Add alternative links to this entry
    altLinks.add(selfLink);
    altLinks.add(relLink);

    entry.setAlternateLinks(altLinks);
    entry.setAuthors(authors);

    List<Content> contents = newAnnotationsList(
        relLink, annotationType, authors,
        getBody(object, FeedService.FEED_FORMATTING_COMPLETE.equals(formatting)), formatting);

    entry.setContents(contents);

    List<com.sun.syndication.feed.atom.Category> categories = newCategoryList(annotationType);
    entry.setCategories(categories);

    entry.setRights(JOURNAL_COPYRIGHT());
    entry.setAlternateLinks(altLinks);

    return entry;
  }

  /**
   * Creates a <code>List&lt;Element&gt;</code> that consist of foreign markup elements. In this case the elements
   * created consist of volume, issue category information.
   *
   * @param article the article
   * @return <code>List&lt;Elements&gt;</code> of foreign markup elements with issue, volume and category information
   */
  private List<Element> newForeignMarkUp(ArticleInfo article) {
    // All our foreign markup
    List<Element> foreignMarkup = new ArrayList<Element>();

    // Add volume
    if (article.getVolume() != null) {
      Element volume = new Element("volume", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
      volume.setText(article.getVolume());
      foreignMarkup.add(volume);
    }
    // Add issue
    if (article.getIssue() != null) {
      Element issue = new Element("issue", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
      issue.setText(article.getIssue());
      foreignMarkup.add(issue);
    }

    //Add the article type to the extended feed element.
    for (ArticleType ar : article.getArticleTypes()) {
      if (ar != null) {
        Element articleType = new Element("article-type", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());

        articleType.setText(ar.getHeading());
        foreignMarkup.add(articleType);
      }
    }

    Set<ArticleCategory> categories = article.getCategories();

    if (categories != null) {
      for (ArticleCategory category : categories) {
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

    return foreignMarkup;
  }

  private String getBody(Object view, boolean includeCompetingInterest) throws UnsupportedEncodingException {
    String body = "";

    if (view instanceof AnnotationView) {
      AnnotationView annot = (AnnotationView) view;

      if (annot.getType() == AnnotationType.RATING) {
        org.ambraproject.models.Rating rating = ratingsService.getRating(annot.getID());

        StringBuilder ratingBody = new StringBuilder();
        ratingBody.append("<div><ul>");
        if (rating.getSingleRating() > 0) {
          ratingBody.append("<li>Rating: ")
              .append(Integer.toString(rating.getSingleRating()))
              .append("</li>");
        } else {
          ratingBody.append("<li>Insight: ")
              .append(Integer.toString(rating.getInsight()))
              .append("</li>")
              .append("<li>Reliability: ")
              .append(Integer.toString(rating.getReliability()))
              .append("</li>")
              .append("<li>Style: ")
              .append(Integer.toString(rating.getStyle()))
              .append("</li>");
        }
        ratingBody.append("</ul></div>")
            .append(rating.getBody());
        if (includeCompetingInterest && rating.getCompetingInterestBody() != null
            && rating.getCompetingInterestBody().trim().length() > 0) {
          ratingBody.append("<p/><strong>Competing Interest Statement</strong>: " + rating.getCompetingInterestBody());
        }
        body = ratingBody.toString();

      } else {
        body = TextUtils.makeHtmlLineBreaks(annot.getBody());
        if (includeCompetingInterest && annot.getBody() != null
            && annot.getCompetingInterestStatement() != null
            && annot.getCompetingInterestStatement().trim().length() > 0) {
          body += "<p/><strong>Competing Interest Statement</strong>: "
              + TextUtils.makeHtmlLineBreaks(annot.getCompetingInterestStatement());
        }
      }
    } else if (view instanceof LinkbackView) {
      LinkbackView trackback = (LinkbackView) view;

      if (trackback.getExcerpt() != null) {
        body = trackback.getExcerpt();
      }
    }

    return body;
  }

  /**
   * Creates a <code>List&lt;Element&gt;</code> that consist of foreign markup elements. In this case the elements
   * created consist of volume, issue category information.
   *
   * @param subject     categories
   * @param volume      volume
   * @param issue       issue
   * @param articleType article type
   * @return <code>List&lt;Elements&gt;</code> of foreign markup elements with issue, volume and category information
   */
  private List<Element> newForeignMarkUp(Node subject, String volume, String issue, String articleType) {
    List<Element> foreignMarkup = new ArrayList<Element>();

    if (subject != null) {
      // subject, category
      NodeList children = subject.getChildNodes();

      for (int k = 0; k < children.getLength(); k++) {
        Node node = children.item(k);
        String category = node.getTextContent();
        int index = category.indexOf("/");

        Element feedCategory = new Element("category", ATOM_NS);

        // existing logic assumes that subject hierarchy is two level deep
        if (index == -1) {
          feedCategory.setAttribute("term", category);
          feedCategory.setAttribute("label", category);
        } else {
          // main category and subcategory
          String subCategory = category.substring(index + 1);
          category = category.substring(0, index);

          feedCategory.setAttribute("term", category);
          feedCategory.setAttribute("label", category);

          Element feedSubCategory = new Element("category", ATOM_NS);

          feedSubCategory.setAttribute("term", subCategory);
          feedSubCategory.setAttribute("label", subCategory);

          feedCategory.addContent(feedSubCategory);
        }
        foreignMarkup.add(feedCategory);
      }
    }

    // volume
    if (volume != null) {
      Element elemVolume = new Element("volume", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
      elemVolume.setText(volume);
      foreignMarkup.add(elemVolume);
    }

    // issue
    if (issue != null) {
      Element elemIssue = new Element("issue", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
      elemIssue.setText(issue);
      foreignMarkup.add(elemIssue);
    }

    //Add the article type to the extended feed element.
    if (articleType != null) {
      Element elemArticleType = new Element("article-type", FEED_EXTENDED_PREFIX(), FEED_EXTENDED_NS());
      elemArticleType.setText(articleType);
      foreignMarkup.add(elemArticleType);
    }

    return foreignMarkup;
  }

  /**
   * Creates a description of article contents in HTML format. Currently the description consists of the Author (or
   * Authors if extended format) and the DublinCore description of the article.
   *
   * @param searchParams input parameters
   * @param article      the article
   * @param authorNames  string concatenated list of author names
   * @return List<Content> consisting of HTML descriptions of the article and author
   */
  private List<Content> newContentsList(FeedSearchParameters searchParams, ArticleInfo article, String authorNames) {
    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();

    description.setType("html");

    try {
      StringBuilder text = new StringBuilder();

      if (!searchParams.isExtended()) {
        text.append("<p>by ").append(authorNames).append("</p>\n");
      }

      if (article.getDescription() != null) {
        String content = secondaryObjectService.getTransformedDescription(article.getDescription());
        content = TextUtils.simpleStripAllTags(content);
        text.append(content);
      }
      description.setValue(text.toString());

    } catch (ApplicationException e) {
      log.error("Error getting description", e);
      description.setValue("<p>Internal server error.</p>");
    }
    contents.add(description);

    return contents;
  }

  /**
   * Creates a description of article contents in HTML format.  It contains abstract and authors
   *
   * @param searchParams      input parameters
   * @param authorsForContent list of authors
   * @param abstractText      abstract
   * @return List<Content> consisting of HTML descriptions of the article and author
   */
  private List<Content> newContentsList(FeedSearchParameters searchParams, NodeList authorsForContent, String abstractText) {
    // contents
    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();
    description.setType("html");

    StringBuilder text = new StringBuilder();

    // If this is a normal feed (not extended), add to content
    if (authorsForContent != null) {
      if (!searchParams.isExtended()) {
        StringBuilder authorNames = new StringBuilder();
        for (int k = 0; k < authorsForContent.getLength(); k++) {
          if (authorNames.length() > 0) {
            authorNames.append(", ");
          }
          Node node = authorsForContent.item(k);
          authorNames.append(node.getTextContent());
        }
        text.append("<p>by ").append(authorNames.toString()).append("</p>\n");
      }
    }

    if (abstractText != null) {
      text.append(abstractText);
    }
    description.setValue(text.toString());

    contents.add(description);

    return contents;
  }

  /**
   * Creates a description of annotation contents in HTML format. Currently the description consists of the Author (or
   * Authors if extended format) and the DublinCore description of the article.
   *
   * @param link             Link to the article
   * @param entryTypeDisplay Text to describe type of entry (e.g., FormalCorrection, Reply, etc)
   * @param authors          All of the authors for this annotation.  At present, there should be only one author per
   *                         annotation, but this method supports the display of multiple authors
   * @param comment          The main body of text that will be displayed by the feed
   * @return List<Content> containing HTML-formatted descriptions of the article, author, etc
   * @throws ApplicationException ApplicationException
   */
  private List<Content> newAnnotationsList(Link link, String entryTypeDisplay,
                                           List<Person> authors, String comment,
                                           String formatting)
      throws ApplicationException {

    List<Content> contents = new ArrayList<Content>();
    Content description = new Content();
    description.setType("html");

    StringBuilder text = new StringBuilder();
    text.append("<p>");
    if (entryTypeDisplay != null)
      text.append(entryTypeDisplay).append(" on ");

    String body = null;
    if (comment != null) {
      if (feedService.FEED_FORMATTING_COMPLETE.equals(formatting)) {
        body = TextUtils.makeHtmlLineBreaks(comment);
      } else {
        if (comment.length() > MAX_ANNOTATION_BODY_LENGTH) {
          body = comment.substring(0, MAX_ANNOTATION_BODY_LENGTH) + " ...";
        }
      }
    }

    text.append(" <a href=")
        .append(link.getHref())
        .append('>')
        .append(link.getTitle())
        .append("</a></p>")
        .append("<p>");
    if (authors != null && authors.size() > 0) {
      StringBuilder authorNames = new StringBuilder();
      for (Person author : authors) {
        authorNames.append(", ").append(author.getName());
      }
      text.append(" By ")
          .append(authorNames.substring(2, authorNames.length()))
          .append(": ");
    }
    text.append(body)
        .append("</p>");
    description.setValue(text.toString());
    contents.add(description);

    return contents;
  }

  /**
   * This routine creates and returns a List&lt;Person&gt; authors listed in DublinCore for the article.
   *
   * @param extended Is this an extended?
   * @param article  the article
   * @param authors  modified and returned <code>List&lt;Person&gt;</code> of article authors.
   * @return String of authors names.
   */
  private String newAuthorsList(boolean extended, ArticleInfo article, List<Person> authors) {
    StringBuilder authorNames = new StringBuilder();

    List<String> sourceAuthors = article.getAuthors();

    if (extended) {
      /* If extended then create a list of persons
       * containing all the authors.
       */
      for (String name : sourceAuthors) {
        Person person = new Person();
        person.setName(name);
        authors.add(person);

        if (authorNames.length() > 0)
          authorNames.append(", ");

        authorNames.append(name);
      }
    } else if (sourceAuthors.size() >= 1) {
      // Not extended therefore there will only be one author.
      Person person = new Person();
      String author = sourceAuthors.get(0);
      person.setName(author);
      authors.add(person);

      // Build a comma delimited list of author names
      for (String name : sourceAuthors) {

        if (authorNames.length() > 0)
          authorNames.append(", ");

        authorNames.append(name);
      }

      if (sourceAuthors.size() > 1)
        person.setName(author + " et al.");
    }

    return authorNames.toString();
  }

  /**
   * Get the list of authors for an entry
   *
   * @param searchParams input parameters
   * @param field        author node
   * @return list of authors
   */
  private ArrayList<Person> newAuthorsList(FeedSearchParameters searchParams, Node field) {
    ArrayList<Person> authors = new ArrayList<Person>();

    NodeList children = field.getChildNodes();
    if (searchParams.isExtended()) {
      // If extended then create a list of persons containing all the authors.
      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        Person person = new Person();
        person.setName(node.getTextContent());
        authors.add(person);
      }
    } else if (children.getLength() >= 1) {
      // Not extended therefore there will only be one author.
      Person person = new Person();
      String author = children.item(0).getTextContent();
      if (children.getLength() > 1) {
        author = author + " et al.";
      }
      person.setName(author);
      authors.add(person);
    }
    return authors;
  }

  /**
   * Create alternate link for the different representaions of the article.
   *
   * @param article        the article
   * @param representation a respresentation of the article
   * @param xmlBase        XML base
   * @return Link  an alternate link to the article
   */
  private Link newAltLink(ArticleInfo article, String representation, String xmlBase) {
    Link altLink = new Link();

    altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + article.getDoi() +
        "&representation=" + representation);
    altLink.setRel("related");

    if (includeformatting) {
      altLink.setTitle("(" + representation + ") " + article.getTitle());
    } else {
      altLink.setTitle("(" + representation + ") " + TextUtils.simpleStripAllTags(article.getTitle()));
    }
    altLink.setType(representation);

    return altLink;
  }

  /**
   * Create a link to the annotation itself.
   *
   * @param view    the annotationView
   * @param xmlBase xml base of article
   * @return link to the article
   */
  private Link newSelfLink(Object view, String xmlBase) {
    if (view instanceof AnnotationView) {
      StringBuilder href = new StringBuilder();
      href.append(xmlBase);

      AnnotationView annot = (AnnotationView) view;

      if (annot.getType() == AnnotationType.RATING) {
        href.append("rate/getArticleRatings.action?articleURI=")
            .append(annot.getArticleDoi());
      } else {
        String id;

        if (annot.getType() == AnnotationType.REPLY) {
          id = annot.getParentID().toString();
        } else {
          id = annot.getID().toString();
        }

        href.append("annotation/listThread.action?inReplyTo=")
            .append(id)
            .append("&root=")
            .append(id);
      }

      // add anchor
      if (annot.getType() == AnnotationType.REPLY) {
        href.append('#').append(annot.getID().toString());
      } else if (annot.getType() == AnnotationType.RATING) {
        href.append('#').append(annot.getID().toString());
      }

      Link link = new Link();
      link.setRel("alternate");
      link.setHref(href.toString());

      if (annot.getType() == AnnotationType.RATING) {
        link.setTitle(RATING_TITLE);
      } else {
        link.setTitle(annot.getTitle());
      }

      return link;
    } else if (view instanceof LinkbackView) {
      StringBuilder href = new StringBuilder();

      LinkbackView trackback = (LinkbackView) view;

      href.append(trackback.getUrl());

      Link link = new Link();
      link.setRel("alternate");
      link.setHref(href.toString());
      link.setTitle(trackback.getTitle());

      return link;
    } else {
      throw new RuntimeException("Invalid type of " + view.getClass().toString() + " received.");
    }
  }

  /**
   * Create a link to the article itself.
   *
   * @param doi     the doi of the article
   * @param title   the title of the article
   * @param xmlBase xml base of article
   * @return link to the article
   */
  private Link newArticleLink(String doi, String title, String xmlBase) {
    Link link = new Link();

    String url = doiToUrl(doi);

    link.setRel("alternate");
    link.setHref(xmlBase + "article/" + url);

    if (includeformatting) {
      link.setTitle(title);
    } else {
      link.setTitle(TextUtils.simpleStripAllTags(title));
    }

    return link;

  }

  /**
   * Create all the alternative links
   *
   * @param id      article id
   * @param xmlBase xml base of the article
   * @param title   title of the article
   * @return list of alternative links
   */
  private List<Link> newAltLinks(String id, String xmlBase, String title) {
    // alternative links
    List<Link> altLinks = new ArrayList<Link>();

    if ((id != null && id.length() > 0) && (xmlBase != null) && (title != null && title.length() > 0)) {
      // Link to article via xmlbase
      Link selfLink = new Link();
      String url = doiToUrl(id);
      selfLink.setRel("alternate");
      selfLink.setHref(xmlBase + "article/" + url);
      selfLink.setTitle(title);
      altLinks.add(selfLink);

      // alternate links
      // pdf
      Link altLink = new Link();
      altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + id +
          "&representation=PDF");
      altLink.setRel("related");
      altLink.setTitle("(PDF) " + title);
      altLink.setType("application/pdf");
      altLinks.add(altLink);

      // xml
      altLink = new Link();
      altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + id +
          "&representation=XML");
      altLink.setRel("related");
      altLink.setTitle("(XML) " + title);
      altLink.setType("text/xml");
      altLinks.add(altLink);
    }

    return altLinks;
  }

  /**
   * Create a feed entry with Id, Rights, Title, Published and Updated set.
   *
   * @param article the article
   * @return Entry  feed entry
   */
  private Entry newEntry(ArticleInfo article) {
    Entry entry = new Entry();

    entry.setId(article.getDoi());

    // Respect Article specific rights
    String rights = article.getRights();
    if (rights != null) {
      entry.setRights(rights);
    } else {
      // Default is CC BY SA 3.0
      entry.setRights(JOURNAL_COPYRIGHT());
    }

    if (includeformatting) {
      entry.setTitle(article.getTitle());
    } else {
      entry.setTitle(TextUtils.simpleStripAllTags(article.getTitle()));
    }
    entry.setPublished(article.getDate());
    entry.setUpdated(article.getDate());

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
   * If a self link was provided by the user create a <code>Link</code> based on the user input information contained in
   * the searchParams.
   *
   * @param searchParams data model
   * @param uri          uri of regquest
   * @return <code>Link</code> user provide link.
   */
  private Link newLink(FeedSearchParameters searchParams, String uri) {
    if (searchParams.getSelfLink() == null || searchParams.getSelfLink().equals("")) {
      if (uri.startsWith("/")) {
        searchParams.setSelfLink(JRNL_URI().substring(0, JRNL_URI().length() - 1) + uri);
      } else {
        searchParams.setSelfLink(JRNL_URI() + uri);
      }
    }

    Link newLink = new Link();
    newLink.setRel("self");
    newLink.setHref(searchParams.getSelfLink());
    newLink.setTitle(FEED_TITLE());

    return newLink;
  }

  /**
   * Build an atom feed categroy list for for the WebAnnotation.
   *
   * @param displayName Name of the entry.
   * @return List of  atom categories
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
   * @param searchParams input parameters
   * @return String identifier generated for this feed
   */
  private String newFeedID(FeedSearchParameters searchParams) {
    String id = FEED_ID();
    String[] categories = searchParams.getCategories();
    if (categories != null && categories.length > 0) {
      String categoryId = "";
      for (String category : categories) {
        categoryId += "categories=" + category + "&";
      }
      id += "?" + categoryId.substring(0, categoryId.length() - 1);
    }
    if (searchParams.getAuthor() != null)
      id += "?author=" + searchParams.getAuthor();

    return id;
  }

  /**
   * Create a feed Title string from the the key.Category and key.Author fields
   *
   * @param searchParams input parameters
   * @return String feed title.
   */
  private String newFeedTitle(FeedSearchParameters searchParams) {
    String feedTitle = searchParams.getTitle();

    if (feedTitle == null) {
      feedTitle = FEED_TITLE();

      String[] categories = searchParams.getCategories();
      if (categories != null && categories.length > 0) {
        String categoryTitle = StringUtils.join(categories, ", ");
        feedTitle += " - Category " + categoryTitle;
      }

      if (searchParams.getAuthor() != null)
        feedTitle += " - Author " + searchParams.getAuthor();
    }
    return feedTitle;
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
   *
   * @param doi the doi to URL encode
   * @return URl encoded doi or the original doi if not UTF-8.
   */
  private String doiToUrl(String doi) {
    String url = doi;

    try {
      url = URLEncoder.encode(url, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
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
  private String jrnlConfGetStr(String key, String defaultValue) {
    String path = "ambra.virtualJournals." + getCurrentJournal() + "." + key;
    return CONF.getString(path, CONF.getString(key, defaultValue));
  }

  /**
   * Get the journal name.
   *
   * @return the name of the current journal
   */
  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
        getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * articleXmlUtils provide methods to manipulate the XML of the articles (transformations etc)
   *
   * @param secondaryObjectService a set of XML transformation utilities
   */
  @Required
  public void setSecondaryObjectService(XMLService secondaryObjectService) {
    this.secondaryObjectService = secondaryObjectService;
  }

  /**
   * @param feedService Article Feed Service
   */
  @Required
  public void setFeedService(FeedService feedService) {
    this.feedService = feedService;
  }

  @Required
  public void setRatingsService(RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }
}

