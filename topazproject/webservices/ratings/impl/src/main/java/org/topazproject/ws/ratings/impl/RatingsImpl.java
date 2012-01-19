/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.ratings.ObjectRating;
import org.topazproject.ws.ratings.ObjectRatingStats;
import org.topazproject.ws.ratings.Ratings;

/** 
 * This provides the implementation of the ratings service.
 * 
 * @author foo
 */
public class RatingsImpl implements Ratings {
  private static final Log log = LogFactory.getLog(RatingsImpl.class);

  private static final String FOAF_URI         = "http://xmlns.com/foaf/0.1/";

  private static final Configuration CONF      = ConfigurationStore.getInstance().getConfiguration();
  private static final String MODEL            = "<" + CONF.getString("topaz.models.ratings") + ">";
  private static final String MODEL_TYPE       =
      "<" + CONF.getString("topaz.models.ratings[@type]", "tucana:Model") + ">";
  private static final String USER_MODEL       = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String RATINGS_PATH_PFX = "ratings";

  private static final Map    aliases;

  private static final String ITQL_CLEAR_RATINGS =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
           // individual ratings
       "   (<${userId}> <topaz:hasRatings> $y and " +
       "      $y <dc_terms:mediator> ${appId} and $y <topaz:object> ${object} and " +
       "      $y <topaz:rating> $s or " +
           // the statements with the ratings node as subject
       "    <${userId}> <topaz:hasRatings> $s and " +
       "      $s <dc_terms:mediator> ${appId} and $s <topaz:object> ${object} or " +
           // the statement with the ratings node as object
       "    $s <tucana:is> <${userId}> and $p <tucana:is> <topaz:hasRatings> and " +
       "      $o <dc_terms:mediator> ${appId} and $o <topaz:object> ${object} )" +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_RATINGS =
      ("select $cat $rtg ${object} ${appId} from ${MODEL} where " +
       "<${userId}> <topaz:hasRatings> $ratings and " +
       "$ratings <dc_terms:mediator> ${appId} and $ratings <topaz:object> ${object} and " +
       "$ratings <topaz:rating> $r and $r <topaz:category> $cat and $r <topaz:value> $rtg;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${USER_MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private static final String ITQL_CLEAR_STATS =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
           // individual ratings
       "   (<${object}> <topaz:hasRatingStats> $y and " +
       "      $y <dc_terms:mediator> '${appId}' and $y <topaz:ratingStats> $s or " +
           // the statements with the ratings node as subject
       "    <${object}> <topaz:hasRatingStats> $s and $s <dc_terms:mediator> '${appId}' or " +
           // the statement with the ratings node as object
       "    $s <tucana:is> <${object}> and $p <tucana:is> <topaz:hasRatingStats> and " +
       "      $o <dc_terms:mediator> '${appId}' )" +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_STATS =
      ("select $cat $N $sum_x $sum_x2 from ${MODEL} where " +
       "<${object}> <topaz:hasRatingStats> $stats and $stats <dc_terms:mediator> ${appId} and " +
       "$stats <topaz:ratingStats> $s and $s <topaz:category> $cat and $s <topaz:numRatings> $N " +
       "and $s <topaz:sumX> $sum_x and $s <topaz:sumX2> $sum_x2;").
      replaceAll("\\Q${MODEL}", MODEL);

  private final RatingsPEP   pep;
  private final TopazContext ctx;
  private final String       baseURI;

  static {
    aliases = ItqlHelper.getDefaultAliases();
    aliases.put("foaf", FOAF_URI);
  }

  /**
   * Initialize the ITQL model. 
   *
   * @param itql itql handle to use
   */
  public static void initializeModel(ItqlHelper itql) throws RemoteException {
    itql.doUpdate("create " + MODEL + " " + MODEL_TYPE + ";", aliases);
  }

  /**
   * Create a new ratings service instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public RatingsImpl(RatingsPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();
  }

  /** 
   * Create a new ratings service instance. 
   *
   * @param itql the itql-helper to use to access the triple store
   * @param pep  the policy-enforcer to use for access-control
   * @throws IOException if an error occurred initializing the itql service
   * @throws ConfigurationException if any required config is missing
   */
  public RatingsImpl(ItqlHelper itql, RatingsPEP pep) throws IOException, ConfigurationException {
    this.pep  = pep;

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    conf = conf.subset("topaz");

    if (!conf.containsKey("objects.base-uri"))
      throw new ConfigurationException("missing key 'topaz.objects.base-uri'");
    baseURI = conf.getString("objects.base-uri");

    try {
      new URI(baseURI);
    } catch (URISyntaxException use) {
      throw new ConfigurationException("key 'topaz.objects.base-uri' does not contain a valid URI",
                                       use);
    }
    ctx = new SimpleTopazContext(itql, null, null);
  }

  /**
   * Create a new ratings service instance.
   *
   * @param itqlService   the itql web-service
   * @param pep           the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the itql or fedora services
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the itql or fedora services
   */
  public RatingsImpl(ProtectedService itqlService, RatingsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(itqlService), pep);
  }

  /** 
   * Create a new ratings services instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara service
   */
  public RatingsImpl(URI mulgaraUri, RatingsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraUri), pep);
  }

  public ObjectRating[] getRatings(String appId, String userId, String object)
      throws NoSuchUserIdException, RemoteException {
    pep.checkObjectAccess(pep.GET_RATINGS, ItqlHelper.validateUri(userId, "userId"),
                          ItqlHelper.validateUri(object, "object"));

    if (log.isDebugEnabled())
      log.debug("Getting ratings for '" + object + "', app='" + appId + "', user='" + userId + "'");

    ObjectRating[] ratings = (ObjectRating[]) getRatingsInternal(appId, userId, object, false);

    if (log.isDebugEnabled())
      log.debug("Ratings for '" + object + "', app='" + appId + "', user='" + userId + "': '" +
                join(ratings, "', '") + "'");

    return ratings;
  }

  private Object getRatingsInternal(String appId, String userId, String object, boolean wantMap)
      throws NoSuchUserIdException, RemoteException {
    StringAnswer ans;
    try {
      Map params = new HashMap();
      params.put("userId", userId);
      params.put("object", (object != null) ? "<" + object + ">" : "$obj");
      params.put("appId", formatAppId(appId));

      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId) +
                   ItqlHelper.bindValues(ITQL_GET_RATINGS, params);

      ans = new StringAnswer(ctx.getItqlHelper().doQuery(qry, aliases));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting ratings for object '" + userId + "'", ae);
    }

    List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (user.size() == 0)
      throw new NoSuchUserIdException(userId);

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
    if (rows.size() == 0)
      return null;

    if (!wantMap) {
      ObjectRating[] ratings = new ObjectRating[rows.size()];
      int idx = 0;
      for (Iterator iter = rows.iterator(); iter.hasNext(); idx++)
        ratings[idx] = parseResult((String[]) iter.next());

      return ratings;

    } else {
      Map res = new HashMap();

      for (Iterator iter = rows.iterator(); iter.hasNext(); ) {
        String[]     row = (String[]) iter.next();
        OAKey        oa  = new OAKey(row[2], row[3]);
        ObjectRating or  = parseResult(row);

        List or_list = (List) res.get(oa);
        if (or_list == null)
          res.put(oa, or_list = new ArrayList());

        or_list.add(or);
      }

      return res;
    }
  }

  private static class OAKey {
    String object;
    String appId;

    OAKey(String object, String appId) {
      this.object = object;
      this.appId  = appId;
    }

    public boolean equals(Object o) {
      if (!(o instanceof OAKey))
        return false;
      return ((OAKey) o).object.equals(object) && ((OAKey) o).appId.equals(appId);
    }

    public int hashCode() {
      return object.hashCode() + appId.hashCode();
    }
  }

  private static final ObjectRating parseResult(String[] row) {
    ObjectRating res = new ObjectRating();
    res.setCategory(row[0]);
    try {
      res.setRating(Float.parseFloat(row[1]));
    } catch (NumberFormatException nfe) {
      log.error("Error parsing rating value '" + row[1] + "'", nfe);
      res.setRating(0);
    }

    return res;
  }

  public void setRatings(String appId, String userId, String object, ObjectRating[] ratings)
      throws NoSuchUserIdException, RemoteException {
    if (userId == null)
      throw new NullPointerException("userId may not be null");

    pep.checkObjectAccess(pep.SET_RATINGS, ItqlHelper.validateUri(userId, "userId"),
                          (object != null) ? ItqlHelper.validateUri(object, "object") : null);

    if (appId == null && ratings != null)
      throw new IllegalArgumentException("ratings must be null if app-id is null");
    if (object == null && ratings != null)
      throw new IllegalArgumentException("ratings must be null if object is null");

    if (log.isDebugEnabled())
      log.debug("Setting ratings for '" + object + "', app='" + appId + "', user='" + userId +
                "': '" + join(ratings, "', '") + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-ratings " + object;
    try {
      itql.beginTxn(txn);

      Map old_ratings = (Map) getRatingsInternal(appId, userId, object, true);

      StringBuffer cmd = new StringBuffer(100);

      Map params = new HashMap();
      params.put("userId", userId);
      params.put("object", (object != null) ? "<" + object + ">" : "$object");
      params.put("appId", formatAppId(appId));

      cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_RATINGS, params));

      if (ratings != null && ratings.length > 0) {
        String ratingsId = getRatingsId(userId, appId, object);

        cmd.append("insert ");

        addReference(cmd, userId, "topaz:hasRatings", ratingsId);
        addReference(cmd, ratingsId, "topaz:object", object);
        addLiteralVal(cmd, ratingsId, "dc_terms:mediator", appId);

        for (int idx = 0; idx < ratings.length; idx++) {
          if (ratings[idx] == null)
            continue;

          String rid = ratingsId + "/" + idx;
          addReference(cmd, ratingsId, "topaz:rating", rid);
          addLiteralVal(cmd, rid, "topaz:category", ratings[idx].getCategory());
          addLiteralVal(cmd, rid, "topaz:value", Float.toString(ratings[idx].getRating()));
        }

        cmd.append(" into ").append(MODEL).append(";");
      }

      itql.doUpdate(cmd.toString(), aliases);

      if (old_ratings != null) {
        for (Iterator iter = old_ratings.keySet().iterator(); iter.hasNext(); ) {
          OAKey oa = (OAKey) iter.next();
          List oldRatings = (List) old_ratings.get(oa);
          updateStats(oa.appId, oa.object, oldRatings, ratings);
        }
      }

      if ((old_ratings == null || old_ratings.isEmpty()) && ratings != null)
        updateStats(appId, object, null, ratings);

      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        log.debug("Error rolling failed transaction", t);
      }
    }
  }

  private static final void addLiteralVal(StringBuffer buf, String subj, String pred, String lit) {
    if (lit == null)
      return;

    buf.append("<").append(subj).append("> <").append(pred).append("> '").
        append(ItqlHelper.escapeLiteral(lit)).append("' ");
  }

  private static final void addReference(StringBuffer buf, String subj, String pred, String url) {
    if (url == null)
      return;

    buf.append("<").append(subj).append("> <").append(pred).append("> <").append(url).append("> ");
  }

  private void updateStats(String appId, String object, List oldRatings, ObjectRating[] newRatings)
      throws RemoteException {
    // retrieve current stats
    List current = getRatingStatsInternal(appId, object);

    // subtract old stats, if any
    if (oldRatings != null) {
      for (Iterator iter = oldRatings.iterator(); iter.hasNext(); ) {
        ObjectRating or = (ObjectRating) iter.next();

        for (Iterator iter2 = current.iterator(); iter2.hasNext(); ) {
          String[] row = (String[]) iter2.next();  // cat, N, sum_x, sum_x2
          if (or.getCategory().equals(row[0])) {
            float x = or.getRating();
            row[1] = Integer.toString(Integer.parseInt(row[1]) - 1);
            row[2] = Double.toString(Double.parseDouble(row[2]) - x);
            row[3] = Double.toString(Double.parseDouble(row[3]) - x * x);
            break;
          }
        }
      }
    }

    // add new stats, if any
    if (newRatings != null) {
      for (int idx = 0; idx < newRatings.length; idx++) {
        ObjectRating or = newRatings[idx];
        boolean found = false;

        for (Iterator iter = current.iterator(); iter.hasNext(); ) {
          String[] row = (String[]) iter.next();  // cat, N, sum_x, sum_x2

          if (or.getCategory().equals(row[0])) {
            float x = or.getRating();
            row[1] = Integer.toString(Integer.parseInt(row[1]) + 1);
            row[2] = Double.toString(Double.parseDouble(row[2]) + x);
            row[3] = Double.toString(Double.parseDouble(row[3]) + x * x);
            found = true;
            break;
          }
        }

        if (!found) {
          float x = or.getRating();
          current.add(
              new String[] { or.getCategory(), "1", Double.toString(x), Double.toString(x * x) });
        }
      }
    }

    // save new stats
    StringBuffer cmd = new StringBuffer(100);

    cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_STATS, "object", object,
                                     "appId", ItqlHelper.escapeLiteral(appId)));

    int clr_len = cmd.length();

    String statsId = getStatsId(appId, object);

    cmd.append("insert ");

    addReference(cmd, object, "topaz:hasRatingStats", statsId);
    addLiteralVal(cmd, statsId, "dc_terms:mediator", appId);

    int idx = 0;
    for (Iterator iter = current.iterator(); iter.hasNext(); ) {
      String[] row = (String[]) iter.next();    // cat, N, sum_x, sum_x2
      if (row[1].equals("0"))
        continue;

      String sid = statsId + "/" + idx++;
      addReference(cmd, statsId, "topaz:ratingStats", sid);

      addLiteralVal(cmd, sid, "topaz:category", row[0]);
      addLiteralVal(cmd, sid, "topaz:numRatings", row[1]);
      addLiteralVal(cmd, sid, "topaz:sumX", row[2]);
      addLiteralVal(cmd, sid, "topaz:sumX2", row[3]);
    }

    cmd.append(" into ").append(MODEL).append(";");

    if (idx == 0)
      cmd.setLength(clr_len);   // don't insert anything if no stats

    ctx.getItqlHelper().doUpdate(cmd.toString(), aliases);
  }

  public ObjectRatingStats[] getRatingStats(String appId, String object) throws RemoteException {
    pep.checkAccess(pep.GET_STATS, ItqlHelper.validateUri(object, "object"));

    if (log.isDebugEnabled())
      log.debug("Getting stats for '" + object + "', app='" + appId + "'");

    List rows = getRatingStatsInternal(appId, object);
    if (rows.size() == 0) {
      if (log.isDebugEnabled())
        log.debug("No rating stats found for '" + object + "', app='" + appId + "'");
      return null;
    }

    ObjectRatingStats[] stats = new ObjectRatingStats[rows.size()];
    int idx = 0;
    for (Iterator iter = rows.iterator(); iter.hasNext(); ) {
      String[] res = (String[]) iter.next();

      try {
        int    N      = Integer.parseInt(res[1]);
        double sum_x  = Double.parseDouble(res[2]);
        double sum_x2 = Double.parseDouble(res[3]);
        double avg    = sum_x / N;
        double var    = sum_x2 / N - avg * avg;

        ObjectRatingStats ors = new ObjectRatingStats();
        ors.setCategory(res[0]);
        ors.setNumberOfRatings(N);
        ors.setAverage((float) avg);
        ors.setVariance((float) var);

        stats[idx++] = ors;
      } catch (NumberFormatException nfe) {
        log.error("Error parsing one of the stats values '" + join(res, "', '") + "'", nfe);
      }
    }

    if (log.isDebugEnabled())
      log.debug("Rating stats for '" + object + "', app='" + appId + "': '" +
                join(stats, "', '") + "'");

    return stats;
  }

  private List getRatingStatsInternal(String appId, String object) throws RemoteException {
    StringAnswer ans;
    try {
      String qry =
          ItqlHelper.bindValues(ITQL_GET_STATS, "object", object, "appId", formatAppId(appId));
      ans = new StringAnswer(ctx.getItqlHelper().doQuery(qry, aliases));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting ratings for object '" + object + "'", ae);
    }

    return ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
  }

  private static final String formatAppId(String appId) {
    return (appId != null) ? "'" + ItqlHelper.escapeLiteral(appId) + "'" : "$appId";
  }

  /** 
   * Convert an object-id and app-id to a ratings-id. There's a single ratings-node per
   * (user, object, app-id) tuple, so the ratings id can be computed algorithmically.
   * 
   * @param userId  the user's id
   * @param appId   the app id
   * @param object  the object uri
   * @return the ratings id
   */
  protected String getRatingsId(String userId, String appId, String object) {
    int slash = userId.lastIndexOf('/');
    return baseURI + RATINGS_PATH_PFX + userId.substring(slash + 1) + "/" + hash(appId) + "/" +
           hash(object);
  }

  /** 
   * Convert an object-id and app-id to a stats-id. There's a single stats-node per
   * (object, app-id) tuple, so the stats id can be computed algorithmically.
   * 
   * @param appId   the app id
   * @param object  the object uri
   * @return the stats id
   */
  protected String getStatsId(String appId, String object) {
    return baseURI + RATINGS_PATH_PFX + "/" + hash(appId) + "/" + hash(object);
  }

  private static final String hash(String str) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      return new String(Base64.encodeBase64(md.digest(str.getBytes("ISO-8859-1"))), "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);  // can't happen
    } catch (NoSuchAlgorithmException nsae) {
      throw new RuntimeException(nsae); // can't happen
    }
  }

  private static final String join(Object list, String sep) {
    if (list == null)
      return null;

    StringBuffer sb = new StringBuffer(200);

    if (list instanceof Collection) {
      for (Iterator iter = ((Collection) list).iterator(); iter.hasNext(); )
        append(sb, iter.next(), sep);
    } else {
      for (int idx = 0; idx < ((Object[]) list).length; idx++)
        append(sb, ((Object[]) list)[idx], sep);
    }

    if (sb.length() > 0)
      sb.setLength(sb.length() - sep.length());

    return sb.toString();
  }

  private static final void append(StringBuffer sb, Object obj, String sep) {
    if (obj == null)
      return;

    if (obj instanceof ObjectRating) {
      ObjectRating o = (ObjectRating) obj;
      sb.append("ObjectRating[category=").append(o.getCategory()).append(", rating=").
         append(o.getRating()).append("]");
    } else if (obj instanceof ObjectRatingStats) {
      ObjectRatingStats o = (ObjectRatingStats) obj;
      sb.append("ObjectRatingStats[category=").append(o.getCategory()).append(", num-ratings=").
         append(o.getNumberOfRatings()).append(", average=").append(o.getAverage()).
         append(", variance=").append(o.getVariance()).append("]");
    } else {
      sb.append(obj);
    }

    sb.append(sep);
  }
}
