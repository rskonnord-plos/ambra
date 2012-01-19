/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import java.util.Date;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.text.ParseException;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.service.ItqlInterpreterException;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.Answer.QueryAnswer;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.ws.alerts.Alerts;

import org.topazproject.feed.ArticleFeed;

/** 
 * This provides the implementation of the alerts service.
 * 
 * @author Eric Brown
 */
public class AlertsImpl implements Alerts {
  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final int    FETCH_SIZE     = CONF.getInt("topaz.alerts.fetchsize", 100);
  private static final Log    log            = LogFactory.getLog(AlertsImpl.class);

  private static final String MODEL_ALERTS   = "<" + CONF.getString("topaz.models.alerts") + ">";
  private static final String ALERTS_TYPE    =
      "<" + CONF.getString("topaz.models.alerts[@type]", "tucana:Model") + ">";
  private static final String MODEL_ARTICLES = "<" + CONF.getString("topaz.models.articles") + ">";
  private static final String MODEL_PREFS    = "<" + CONF.getString("topaz.models.preferences") + ">";
  private static final String MODEL_XSD      = "<" + CONF.getString("topaz.models.xsd") + ">";
  private static final String XSD_TYPE       = "<" + CONF.getString("topaz.models.xsd[@type]") + ">";

  // Email Alert Queries
  private static final String UPDATE_TIMESTAMP_ITQL =
    "delete select $user $pred $date from ${ALERTS} where $user $pred $date and " +
    " <${userId}> <topaz:timeStamp> $date from ${ALERTS};\n" +
    "insert <${userId}> <topaz:timeStamp> '${stamp}'^^<xsd:date> into ${ALERTS};";
  private static final String GET_TIMESTAMP_ITQL =
    "select $date from ${ALERTS} where <${userId}> <topaz:timeStamp> $date;";
  private static final String GET_USER_TIMESTAMPS_ITQL =
    "select $user $date from ${ALERTS} where <${userId}> <topaz:timeStamp> $date " +
    " and $date <tucana:after> ${date} in ${XSD};";
  private static final String GET_USER_ITQL =
    "select $timestamp " +
    "  subquery( select $user from ${PREFS} where " +
    "   $user      <topaz:hasPreferences>  $pref and " +
    "   $pref      <topaz:preference>      $prefn and " +
    "   $prefn     <topaz:prefName>        'alertsEmailAddress' and " +
    "   $prefn     <topaz:prefValue>       ${email} ) " +
    " from ${ALERTS} where " +
    "  $user       <topaz:timeStamp>       $timestamp and " +
    "  $timestamp  <tucana:before>         '${stamp}'^^<xsd:date> in ${XSD};";
  private static final String GET_NEXT_USERS_ITQL =
    "select $timestamp $user " +
    "  subquery( select $email from ${PREFS} where " +
    "   $user  <topaz:hasPreferences> $pref and " +
    "   $pref  <topaz:preference>     $prefn and " +
    "   $prefn <topaz:prefName>       'alertsEmailAddress' and " +
    "   $prefn <topaz:prefValue>      $email ) " +
    " from ${ALERTS} where " +
    "  $user       <topaz:timeStamp> $timestamp and " +
    "  $timestamp  <tucana:before>     '${stamp}'^^<xsd:date> in ${XSD} " +
    " order by $user " +
    " limit ${limit};";
  private static final String GET_USERS_FEED_ITQL =
    "select $art $title $date $state " +
    " subquery(select $description from ${ARTICLES} where $art <dc:description> $description) " +
    "   from ${ARTICLES} where " +
    "  <${userId}> <topaz:hasPreferences> $pref  in ${PREFS} and " +
    "  $pref       <topaz:preference>     $prefn in ${PREFS} and " +
    "  $prefn      <topaz:prefName>       'alertsCategories' in ${PREFS} and " +
    "  $prefn      <topaz:prefValue>      $cat   in ${PREFS} and " +
    " $art <dc:title>       $title and " +
    " $art <dc_terms:available> $date and " +
    " $art <topaz:hasCategory> $catobj and " +
    " $catobj <topaz:mainCategory> $cat and " +
    " $art <topaz:articleState> $state and " +
    " $date <tucana:before> '${endDate}' in ${XSD} and " +
    " $date <tucana:after>  '${startDate}' in ${XSD};";
  private static final String CLEAN_USER_ITQL =
    "delete select $user $pred $date from ${ALERTS} where $user $pred $date and " +
    " <${userId}> $pred $date from ${ALERTS};";
  private static final String CREATE_USER_ITQL =
    "insert <${userId}> <topaz:timeStamp> '${stamp}'^^<xsd:date> into ${ALERTS};";

  private final AlertsPEP    pep;
  private final TopazContext ctx;

  /**
   * Class to stash user data while reading from Kowari.
   */
  static class UserData {
    String userId;
    String emailAddress;
    String stamp;
  }

  /**
   * Initialize the ITQL model. 
   *
   * @param itql itql handle to use
   */
  public static void initializeModel(ItqlHelper itql) throws RemoteException {
    itql.doUpdate("create " + MODEL_XSD + " " + XSD_TYPE + ";", null);
    itql.doUpdate("create " + MODEL_ALERTS + " " + ALERTS_TYPE + ";", null);
  }

  /**
   * Create a new permission instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public AlertsImpl(AlertsPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;
  }
  /**
   * Create a new alerts service instance.
   *
   * @param itqlService   the itql web-service
   * @param fedoraService the fedora web-service
   * @param pep           the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the itql or fedora services
   * @throws IOException if an error occurred talking to the itql or fedora services
   */
  public AlertsImpl(ProtectedService itqlService, ProtectedService fedoraService, AlertsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this.pep = pep;

    ItqlHelper itql = new ItqlHelper(itqlService);
    FedoraAPIM apim = APIMStubFactory.create(fedoraService);
    ctx = new SimpleTopazContext(itql, apim, null);
  }

  /**
   * Create a new alerts instance.
   *
   * @param mulgaraUri is the uri to kowari/mulgara.
   * @throws MalformedURLException if the URI is invalid
   * @throws ServiceException if an error occurred locating the itql service
   * @throws RemoteException
   */
  public AlertsImpl(URI mulgaraUri)
      throws MalformedURLException, ServiceException, RemoteException {
    this.pep = null; // means we are super-user

    ItqlHelper itql = new ItqlHelper(mulgaraUri);
    ctx = new SimpleTopazContext(itql, null, null);
  }


  // See Alerts.java interface
  public void startUser(String userId) throws RemoteException {
    this.startUser(userId, Calendar.getInstance().getTime().toString());
  }

  // See Alerts.java interface
  public void startUser(String userId, String date) throws RemoteException {
    checkAccess(pep.START_USER, userId);
    ItqlHelper.validateUri(userId, "user-id");

    Calendar cal = Calendar.getInstance();
    cal.setTime(ArticleFeed.parseDateParam(date));
    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    values.put("stamp", AlertsHelper.getTimestamp(cal));
    String query = ItqlHelper.bindValues(AlertsImpl.CREATE_USER_ITQL, values);
    ctx.getItqlHelper().doUpdate(query, null);
  }

  // See Alerts.java interface
  public void clearUser(String userId) throws RemoteException {
    checkAccess(pep.CLEAR_USER, userId);
    ItqlHelper.validateUri(userId, "user-id");

    Map values = new HashMap();
    values.put("ALERTS", AlertsImpl.MODEL_ALERTS);
    values.put("userId", userId);
    String query = ItqlHelper.bindValues(AlertsImpl.CLEAN_USER_ITQL, values);
    ctx.getItqlHelper().doUpdate(query, null);
  }

  // See Alerts.java interface
  public boolean sendAlerts(String endDate, int count) {
    checkAccess(pep.SEND_ALERTS, "dummy:dummy");
    int cnt = 0;
    try {
      Calendar end = Calendar.getInstance();
      end.setTime(ArticleFeed.parseDateParam(endDate));

      for (AlertMessages msgsIt = new AlertMessages(end); msgsIt.hasNext() && count-- > 0; ) {
        Email msg = (Email)msgsIt.next();
        AlertsHelper.sendEmail(msg);
        cnt++;
      }
      log.info("Sent " + cnt + " alerts");
    } catch (Exception e) {
      log.warn("Problem sending alerts", e);
      return false;
    }

    /* TODO: Update users that were never started:
     * select $user count( select $timestamp from <rmi://localhost/fedora#alerts>
     *  where $user <http://rdf.topazproject.org/RDF/alerts/timestamp> $timestamp)
     *  from <rmi://localhost/fedora#preferences> where
     *  $user <http://rdf.topazproject.org/RDF/hasPreferences> $pref and
     *  $pref <http://rdf.topazproject.org/RDF/preference> $prefm and
     *  $prefm <http://rdf.topazproject.org/RDF/prefName> 'alertsCategories'
     *  having $k0 <tucana:occurs>
     *  '1.0'^^<http://www.w3.org/2001/XMLSchema#double>;
     */

    return true; // Everything went okay
  }

  // See Alerts.java interface
  public boolean sendAlert(String endDate, String emailAddress) throws RemoteException {
    checkAccess(pep.SEND_ALERTS, "dummy:dummy");
    try {
      UserData user = getUser(endDate, emailAddress);
      if (user == null)
        return false;

      Email message = processUser(user, endDate);
      if (message == null)
        return false;

      AlertsHelper.sendEmail(message);
      return true;
    } catch (AnswerException ae) {
      throw new RemoteException("Error talking to mulgara", ae);
    } catch (EmailException ee) {
      throw new RemoteException("Error sending alert", ee);
    } catch (AlertsGenerationException age) {
      throw new RemoteException("Error generating alert", age);
    }
  }

  // See Alerts.java interface
  public boolean sendAllAlerts() {
    checkAccess(pep.SEND_ALERTS, "dummy:dummy");
    Calendar c = Calendar.getInstance();
    AlertsHelper.rollCalendar(c, -1);
    return this.sendAlerts(c.getTime().toString(), 0);
  }

  /**
   * Iterator over alerts we need to send out.
   *
   * We read in a couple of records at a time. Update the user's timestamps one at a time.
   * Send the message. Then read in a few more records.
   */
  class AlertMessages { // implements Iterator {
    Iterator usersIt; // Iterator over UserData records. Is null if no more data.
    String   endDate; // endDate for alert messages (inclusive)
    String   nextDay;
    Email    message; // Current message

    /**
     * Create AlertMessages iterator.
     *
     * @param endDate is the last day to include for alert messages (inclusive).
     */
    AlertMessages(Calendar endDate) throws RemoteException, AnswerException {
      assert endDate != null;
      this.endDate = AlertsHelper.getTimestamp(endDate);
      this.nextDay = AlertsHelper.rollTimestamp(this.endDate, 1);

      // Read first N records and set iterator
      Collection users = getNextUsers(this.endDate, FETCH_SIZE);
      if (users != null)
        this.usersIt = users.iterator();
    }

    public boolean hasNext() throws AnswerException, RemoteException,
        EmailException, AlertsGenerationException {
      while (true) {
        if (this.usersIt == null)
          return false;

        while (this.usersIt.hasNext()) {
          Email message = processUser((UserData) this.usersIt.next(), this.endDate);
          if (message != null) {
            this.message = message;
            return true; // Okay, we found a user with articles
          } else
            continue;
        }

        // Have reached end of iterator, read more records
        Collection users = getNextUsers(this.endDate, FETCH_SIZE);
        if (users == null || users.size() == 0) {
          this.usersIt = null;
          return false;
        }
        this.usersIt = users.iterator();
      }
    }

    public Object next() throws NoSuchElementException, RemoteException, AnswerException,
        EmailException, AlertsGenerationException {
      if (this.message == null)
        this.hasNext(); // Fetch the next message (if there is one)

      if (this.message == null)
        throw new NoSuchElementException("No more Alert messages");

      Email message = this.message;
      this.message = null; // If next() is called again, we want the next message, not this one
      return message;
    }

    public void remove() { // thorws UnsupportedOperationException {
      throw new UnsupportedOperationException("Cannot manually remove messages");
    }
  }

  private Email processUser(UserData user, String endDate)
      throws RemoteException, AlertsGenerationException, EmailException {
    updateTimestamp(user.userId, endDate);

    // Get articles we want a feed on for a specific user bounded by user.stamp and endDate
    Collection articles = getUserArticles(user, endDate);
    if (articles == null || articles.size() == 0)
      return null; // No articles, so no alert for this user. Skip him.

    Email message = AlertsHelper.getEmail(articles);
    message.setSubject("PlOS-One Alert");
    message.setFrom("DO-NOT-REPLY@plosone.org");
    message.addTo(user.emailAddress);
    message.addHeader("X-Topaz-Userid", user.userId);
    message.addHeader("X-Topaz-endDate", endDate);
    message.addHeader("X-Topaz-startDate", user.stamp);
    message.addHeader("X-Topaz-Articles", articles.toString());
    message.addHeader("X-Topaz-Categories", "N/A"); // iTQL hides these from us

    if (log.isDebugEnabled())
      log.debug("hasNext user " + user.userId + " " + user.stamp + "-" + endDate +
                " msg: " + articles.toString());
    return message; // Okay, we found a user with articles
  }


  /**
   * Get the next N users that have alerts from their timestamp until endDate (usually set
   * to yesterday).
   *
   * @param endDate The date to get alerts until
   * @param count The number of users to get
   * @return a list of users
   */
  private Collection getNextUsers(String endDate, int count)
      throws RemoteException, AnswerException {
    LinkedHashMap users = new LinkedHashMap();

    Map values = new HashMap();
    values.put("PREFS", MODEL_PREFS);
    values.put("ALERTS", MODEL_ALERTS);
    values.put("XSD", MODEL_XSD);
    values.put("limit", "" + count);
    values.put("stamp", endDate);
    // TODO: This query should include count(*) from ARTICLES delimited by dates
    String query = ItqlHelper.bindValues(AlertsImpl.GET_NEXT_USERS_ITQL, values);
    String response = ctx.getItqlHelper().doQuery(query, null);
    Answer result = new Answer(response);
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // Iteratoe over returned users putting them in our data structure
    for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
      Object[] row = (Object[])rowIt.next();

      UserData user = new UserData();
      user.userId = ((URIReference)row[1]).getURI().toString();
      user.stamp = ((Literal)row[0]).getLexicalForm();

      QueryAnswer subAnswer = (QueryAnswer)row[2]; // from sub-query
      Object[] subRow = (Object[])subAnswer.getRows().get(0);
      user.emailAddress = subRow[0].toString();

      if (log.isDebugEnabled())
        log.debug("Found user " + user.userId + " " + user.emailAddress + " " + user.stamp);
      users.put(user.userId, user);
    }

    return users.values();
  }

  /**
   * Find a specific user (based on his alert's email address) that has alerts. If there is
   * no user registered for this email address OR if the user has no alerts, null will be
   * returned.
   *
   * @param endDate The date to get alerts until
   * @param emailAddress The user's registered alert email address.
   * @return The UserData or null.
   */
  private UserData getUser(String endDate, String emailAddress)
      throws RemoteException, AnswerException {
    Map values = new HashMap();
    values.put("PREFS", MODEL_PREFS);
    values.put("ALERTS", MODEL_ALERTS);
    values.put("XSD", MODEL_XSD);
    values.put("stamp", endDate);
    values.put("email", emailAddress);
    String query = ItqlHelper.bindValues(AlertsImpl.GET_USER_ITQL, values);
    String response = ctx.getItqlHelper().doQuery(query, null);
    Answer result = new Answer(response);
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // TODO: Should just check to see if we have one or zero results
    for (Iterator rowIt = answer.getRows().iterator(); rowIt.hasNext(); ) {
      Object[] row = (Object[])rowIt.next();

      UserData user = new UserData();
      user.stamp = ((Literal)row[0]).getLexicalForm();

      QueryAnswer subAnswer = (QueryAnswer)row[1]; // from sub-query
      Object[] subRow = (Object[])subAnswer.getRows().get(0);
      user.userId = ((URIReference)subRow[0]).getURI().toString();
      
      user.emailAddress = emailAddress;
      if (log.isDebugEnabled())
        log.debug("Found user " + user.userId + " " + user.emailAddress + " " + user.stamp);
      return user;
    }

    return null;
  }

  /**
   * Get timestamp user last received update. (Unused?)
   */
  private String getUserTimestamp(String userId) throws RemoteException, AnswerException {
    Map values = new HashMap();
    values.put("ALERTS", MODEL_ALERTS);
    values.put("userId", userId);
    String query = ItqlHelper.bindValues(AlertsImpl.GET_TIMESTAMP_ITQL, values);
    StringAnswer result = new StringAnswer(ctx.getItqlHelper().doQuery(query, null));
    QueryAnswer  answer = (QueryAnswer)result.getAnswers().get(0);

    // If user has no timestamp yet, then return today
    if (answer.getRows().size() == 0)
      return AlertsHelper.getTimestamp(Calendar.getInstance());

    // Return the timestamp we found in the ALERTS Kowari Model
    return ((String[])answer.getRows().get(0))[0];
  }

  /**
   * Update a users timestamp in the database.
   */
  private void updateTimestamp(String userId, String stamp) throws RemoteException {
    // TODO: Make this transactional AND recover if something fails...
    Map values = new HashMap();
    values.put("ALERTS", MODEL_ALERTS);
    values.put("userId", userId);
    values.put("stamp", stamp);
    String query = ItqlHelper.bindValues(AlertsImpl.UPDATE_TIMESTAMP_ITQL, values);
    ctx.getItqlHelper().doUpdate(query, null);
  }


  /**
   * Get the xml for a feed for a specific user's alerts preferences.
   *
   * @param user is the userId and timestamp associated with a user.
   * @param endDate is the YYYY-MM-DD that we want to go until.
   * @return the articles for this user
   */
  private Collection getUserArticles(UserData user, String endDate) throws RemoteException {
    if (endDate == null)
      endDate = AlertsHelper.getTimestamp(Calendar.getInstance()); // today

    HashMap values = new HashMap();
    values.put("ARTICLES", MODEL_ARTICLES);
    values.put("PREFS", MODEL_PREFS);
    values.put("XSD", MODEL_XSD);
    values.put("userId", user.userId);
    values.put("endDate", endDate);
    values.put("startDate", user.stamp);
    // TODO: Bracket by user.stamp and endDate once articles have date properly typed
    String query = ItqlHelper.bindValues(AlertsImpl.GET_USERS_FEED_ITQL, values);
    return this.getArticles(query);
  }

  /**
   * Get the articles associated with a feed given a query and dates.
   *
   * The query must return $art $title $description $date.
   *
   * @param query is the iTQL query that returns the articles we're interested in.
   */
  protected Collection getArticles(String query)
      throws RemoteException {
    try {
      Answer articlesAnswer = new Answer(ctx.getItqlHelper().doQuery(query, null));
      Map articles = ArticleFeed.getArticlesSummary(articlesAnswer);

      for (Iterator it = articles.keySet().iterator(); it.hasNext(); ) {
        String art = (String)it.next();
        try {
          checkAccess(pep.READ_META_DATA, art);
        } catch (SecurityException se) {
          articles.remove(art);
          if (log.isDebugEnabled())
            log.debug(art, se);
        }
      }

      String detailsQuery = ArticleFeed.getDetailsQuery(articles.values());
      StringAnswer detailsAnswer =
          new StringAnswer(ctx.getItqlHelper().doQuery(detailsQuery, null));
      ArticleFeed.addArticlesDetails(articles, detailsAnswer);

      return articles.values();
    } catch (AnswerException ae) {
      throw new RemoteException("Error querying RDF", ae);
    }
  }

  protected void checkAccess(String action, String uri) {
    pep.checkAccess(action, URI.create(uri));
  }
}
