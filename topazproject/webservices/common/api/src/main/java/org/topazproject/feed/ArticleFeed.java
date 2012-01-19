/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.feed;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.rmi.RemoteException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;

import org.jrdf.graph.Literal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.Answer.QueryAnswer;

/**
 * Utility class for generating XML feeds that return a list of articles.
 *
 * @author Eric Brown
 */
public class ArticleFeed {
  private static final Log    log            = LogFactory.getLog(ArticleFeed.class);

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();
  private static final String MODEL_ARTICLES = "<" + CONF.getString("topaz.models.articles") + ">";
  private static final String MODEL_XSD      = "<" + CONF.getString("topaz.models.xsd") + ">";

  private static final String FEED_ITQL =
    "select $obj $title $date $state " +
    " subquery(select $description from ${ARTICLES} where $obj <dc:description> $description) " +
    "   from ${ARTICLES} where " +
    " $obj <dc:title> $title and " +
    " $obj <dc_terms:available> $date and " +
    " $obj <topaz:articleState> $state " +
    " ${args} " +
    " order by $date ${sort};";
  private static final String FIND_SUBJECTS_ITQL =
    "select '${obj}' $subject from ${ARTICLES} where " +
    " <${obj}> <dc:subject> $subject " +
    "order by $subject;";
  // TODO: Find sub-categories too
  private static final String FIND_CATEGORIES_ITQL =
    "select '${obj}' $category from ${ARTICLES} where " +
    " <${obj}> <topaz:hasCategory> $cat and " +
    " $cat <topaz:mainCategory> $category " +
    "order by $category;";
  private static final String FIND_AUTHORS_ITQL =
    "select '${obj}' $author from ${ARTICLES} where " +
    " <${obj}> <dc:creator> $author " +
    " order by $author;";

  private static final String XML_RESPONSE =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<articles>\n${articles}</articles>\n";
  private static final String XML_ARTICLE_TAG =
    "  <article>\n" +
    "    <uri>${uri}</uri>\n" +
    "    <title>${title}</title>\n" +
    "    <description>${description}</description>\n" +
    "    <date>${date}</date>\n" +
    "    ${authors}\n" +
    "    ${categories}\n" +
    "    ${subjects}\n" +
    "  </article>\n";

  private static XPathExpression dcCreatorXpath;
  private static XPathExpression dcSubjectXpath;
  /**
   * Compute the iTQL to find a set of articles based on date, category and author criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  indicates how sorting should be done (by date)
   * @return the iTQL needed to issue against kowari.
   */
  public static String getQuery(Date startDate, Date endDate, String[] categories, String[] authors,
                                int[] states, boolean ascending) {
    StringBuffer args = new StringBuffer(500);

    if (categories != null && categories.length > 0) {
      args.append(" and (");
      for (int i = 0; i < categories.length; i++)
        args.append("$obj <topaz:hasCategory> $cat and $cat <topaz:mainCategory> '").
          append(ItqlHelper.escapeLiteral(categories[i])).append("' or ");
      args.setLength(args.length() - 4);
      args.append(")");
    }

    if (authors != null && authors.length > 0) {
      args.append(" and (");
      for (int i = 0; i < authors.length; i++)
        args.append("$obj <dc:creator> '").append(ItqlHelper.escapeLiteral(authors[i])).
             append("' or ");
      args.setLength(args.length() - 4);
      args.append(")");
    }

    if (states != null && states.length > 0) {
      args.append(" and (");
      for (int i = 0; i < states.length; i++)
        args.append("$obj <topaz:articleState> '").append(states[i]).append("'^^<xsd:int> or ");
      args.setLength(args.length() - 4);
      args.append(")");
    }

    if (startDate != null)
      args.append(" and $date <tucana:after> '" + formatDate(incDay(startDate, -1)) +
                  "' in " + MODEL_XSD);
    if (endDate != null)
      args.append(" and $date <tucana:before> '" + formatDate(incDay(endDate, 1)) +
                  "' in " + MODEL_XSD);

    Map values = new HashMap();
    values.put("ARTICLES", MODEL_ARTICLES);
    values.put("args", args.toString());
    values.put("sort", ascending ? "asc" : "desc");
    return ItqlHelper.bindValues(FEED_ITQL, values);
  }

  /**
   * Given an answer from Kowari, build the initial set of articles that were returned.
   *
   * Dates are needed only because query is currently somewhat crippled. So we do additional
   * filtering here.
   *
   * @param articlesAnswer is the response received from kowari.
   * @return a map of uri to L{ArticleFeedData} for each article received
   */
  public static Map getArticlesSummary(Answer articlesAnswer) {
    LinkedHashMap articles = new LinkedHashMap();
    QueryAnswer answer = (QueryAnswer) articlesAnswer.getAnswers().get(0);

    for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
      Object[] row = (Object[]) rowIt.next();
      Date date = null;

      try {
        date = parseDate(((Literal) row[2]).getLexicalForm());
      } catch (ParseException pe) {
        log.warn("Ignoring bad date: " + row[2].toString(), pe);
        // XXX: Should we show the message or not?
        continue; // Don't show article
      }

      ArticleFeedData article = new ArticleFeedData();
      article.uri         = row[0].toString();
      article.title       = ((Literal) row[1]).getLexicalForm();
      article.date        = date;
      article.state       = Integer.parseInt(((Literal) row[3]).getLexicalForm());

      List descriptions = ((QueryAnswer) row[4]).getRows();
      if (descriptions.size() > 0)
        article.description = ((Literal) ((Object[]) descriptions.get(0))[0]).getLexicalForm();

      articles.put(article.uri, article);
    }

    return articles;
  }

  /**
   * Given a set of articles, return the queries necessary to get their details
   * (categories and authors).
   *
   * @param articles is the list to query.
   * @return the queries (all in one string) that need to be issued to kowari.
   */
  public static String getDetailsQuery(Collection articles) {
    StringBuffer queryBuffer = new StringBuffer();

    // Build up set of queries
    for (Iterator i = articles.iterator(); i.hasNext(); ) {
      ArticleFeedData article = (ArticleFeedData)i.next();
      if (article == null)
        continue; // should never happen
      Map values = new HashMap();
      values.put("ARTICLES", MODEL_ARTICLES);
      values.put("obj", article.uri);
      queryBuffer.append(ItqlHelper.bindValues(FIND_SUBJECTS_ITQL, values));
      queryBuffer.append(ItqlHelper.bindValues(FIND_CATEGORIES_ITQL, values));
      queryBuffer.append(ItqlHelper.bindValues(FIND_AUTHORS_ITQL, values));
    }

    return queryBuffer.toString();
  }

  /**
   * Given a collection of articles and a response from kowari, add to the articles' meta-data.
   *
   * @param articles is the collection of articles to take more meta-data
   * @param dcs      is the collection of DC stream uris for articles
   * @param detailsAnswer is the response from kowari.
   */
  public static void addArticlesDetails(Map articles, Map dcs, StringAnswer detailsAnswer) {
    List answers = detailsAnswer.getAnswers();

    for (Iterator answersIt = answers.iterator(); answersIt.hasNext(); ) {
      Object ans = answersIt.next();
      if (ans instanceof String) {
        // This is Ronald trying to be helpful. It isn't something we're interested in!
        continue;
      }

      QueryAnswer answer = (QueryAnswer)ans;
      List rows = answer.getRows();

      // If the query returned nothing, just move on
      if (rows.size() == 0)
        continue;

      // Column name represents type of query we did -- for categories or authors
      String column = answer.getVariables()[1];
      String uri = ((String[])rows.get(0))[0];
      ArticleFeedData article = (ArticleFeedData)articles.get(uri);
      if (article == null) {
        // We should never get this - means something changed underneath us
        log.warn("Didn't find article " + uri + " on " + column + " query");
        continue;
      }

      // Get the list of values we want
      List values = new LinkedList();
      for (Iterator rowIt = rows.iterator(); rowIt.hasNext(); )
        values.add(((String[])rowIt.next())[1]);
      if (column.equals("subject")) article.subjects = values;
      else if (column.equals("category")) article.categories = values;
      else if (column.equals("author")) article.authors = values;

      Object dc = dcs.get(uri);
      if (dc != null) {
        try {
          article.authors = getListFromDC(dc, getDcCreatorXpath());
          article.subjects = getListFromDC(dc, getDcSubjectXpath());
        } catch (Exception e) {
          log.warn("Failed to get authors and subject from DC data-stream for " + uri, e);
        }
      }
    }
  }

  private static XPathExpression getDcCreatorXpath() throws Exception {
    if (dcCreatorXpath != null)
      return dcCreatorXpath;
    dcCreatorXpath = getDcListXpath("//dc:creator/text()");
    return dcCreatorXpath;
  }

  private static XPathExpression getDcSubjectXpath() throws Exception {
    if (dcSubjectXpath != null)
      return dcSubjectXpath;
    dcSubjectXpath = getDcListXpath("//dc:subject/text()");
    return dcSubjectXpath;
  }

  private static XPathExpression getDcListXpath(String xpathExpr) throws Exception {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    xpath.setNamespaceContext(new NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("dc".equals(prefix)) return ItqlHelper.DC_URI;
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        return XMLConstants.NULL_NS_URI;
      }
      public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
      }
      public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
      }
    });

    return xpath.compile(xpathExpr);
  }

  private static List getListFromDC(Object dc, XPathExpression expression) throws Exception {
    NodeList nodes = (NodeList)expression.evaluate(dc, XPathConstants.NODESET);
    List list = new ArrayList(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++)
      list.add(nodes.item(i).getNodeValue());

    return list;
  }

  /**
   * Given a collection of articles, return the appropriate feed (as XML).
   *
   * @param articles is the articles to generate the feed for.
   * @return the XML feed as a string.
   */
  public static String buildXml(Collection articles) {
    String articlesXml = "";
    for (Iterator articleIt = articles.iterator(); articleIt.hasNext(); ) {
      ArticleFeedData article = (ArticleFeedData)articleIt.next();

      StringBuffer authorsSb = new StringBuffer();
      if (article.authors != null && article.authors.size() > 0) {
        for (Iterator authorsIt = article.authors.iterator(); authorsIt.hasNext(); ) {
          authorsSb.append("      <author>");
          authorsSb.append(authorsIt.next());
          authorsSb.append("</author>\n");
        }
        authorsSb.insert(0, "<authors>\n");
        authorsSb.append("    </authors>");
      }

      StringBuffer categoriesSb = new StringBuffer();
      if (article.categories != null && article.categories.size() > 0) {
        for (Iterator categoriesIt = article.categories.iterator(); categoriesIt.hasNext(); ) {
          categoriesSb.append("      <category>");
          categoriesSb.append(categoriesIt.next());
          categoriesSb.append("</category>\n");
        }
        categoriesSb.insert(0, "<categories>\n");
        categoriesSb.append("    </categories>");
      }

      StringBuffer subjectsSb = new StringBuffer();
      if (article.subjects != null && article.subjects.size() > 0) {
        for (Iterator subjectsIt = article.subjects.iterator(); subjectsIt.hasNext(); ) {
          subjectsSb.append("      <subject>");
          subjectsSb.append(subjectsIt.next());
          subjectsSb.append("</subject>\n");
        }
        subjectsSb.insert(0, "<subjects>\n");
        subjectsSb.append("    </subjects>");
      }

      Map values = new HashMap();
      /* internationalize () */
      values.put("uri", article.uri);
      values.put("title", article.title);
      values.put("description", article.description);
      values.put("date", formatDate(article.date));
      values.put("authors", authorsSb.toString());
      values.put("subjects", subjectsSb.toString());
      values.put("categories", categoriesSb.toString());
      articlesXml += ItqlHelper.bindValues(XML_ARTICLE_TAG, values);
    }

    Map values = new HashMap();
    values.put("articles", articlesXml);
    return ItqlHelper.bindValues(XML_RESPONSE, values);
  }

  /**
   * Parse a kowari date into a java Date object.
   *
   * @param iso8601date is the date string to parse.
   * @return a java Date object.
   * @throws ParseException if there is a problem parsing the string
   */
  public static Date parseDate(String iso8601date) throws ParseException {
    // Obvious formats:
    final String[] defaultFormats = new String [] {
      "yyyy-MM-dd", "y-M-d", "y-M-d'T'H:m:s", "y-M-d'T'H:m:s.S",
      "y-M-d'T'H:m:s.Sz", "y-M-d'T'H:m:sz" };

    // TODO: Replace with fedora.server.utilities.DateUtility some how?
    // XXX: Deal with ' ' instead of 'T'
    // XXX: Deal with timezone in iso8601 format (not java's idea)

    return DateUtils.parseDate(iso8601date, defaultFormats);
  }

  /**
   * Format a date object to a string to insert into the feed's XML.
   *
   * If the date object is null, return an empty string.
   *
   * @param date is the date object to format
   * @return a string representation
   */
  public static String formatDate(Date date) {
    if (date == null)
      return "";
//    return DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date); // XXX: Use in future?
    return DateFormatUtils.ISO_DATE_FORMAT.format(date);
  }

  /**
   * Roll a Date instance days forward or backward.
   */
  static Date incDay(Date d, int count) {
    return new Date(d.getTime() + count * 1000 * 24 * 3600);
  }

  /**
   * Convert a date pased in as a string to a Date object. Support both string representations
   * of the Date object and iso8601 formatted dates.
   *
   * @param date the string to convert to a Date object
   * @return a date object (or null if date is null)
   * @throws RemoteException if unable to parse date
   */
  public static Date parseDateParam(String date) throws RemoteException {
    if (date == null)
      return null;
    try {
      return new Date(date);
    } catch (IllegalArgumentException iae) {
      try {
        return ArticleFeed.parseDate(date);
      } catch (ParseException pe) {
        throw new RemoteException("Unable to parse date parameter (as Date-string or iso8601): " +
                                  date, pe);
      }
    }
  }
}
