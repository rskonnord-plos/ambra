/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.impl.SimpleTopazContext;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.pap.Preferences;
import org.topazproject.ws.pap.UserPreference;

/** 
 * This provides the implementation of the user preferences service.
 * 
 * <p>Preferences are stored as follows: for each app-id there exists a node with
 * "&lt;user-id&gt; &lt;topaz:hasPreferences&gt; &lt;pref-id&gt;" and
 * "&lt;pref-id&gt; &lt;dc_terms:mediator&gt; &lt;app-id&gt;". Then, for each preference
 * associated with the app-id there's a node with predicates describing the name and values
 * as follows: "&lt;pref-id&gt; &lt;topaz:preference&gt; &lt;p-node&gt;" and
 * "&lt;p-node&gt; &lt;topaz:prefName&gt; '-name-'" and one or more
 * "&lt;p-node&gt; &lt;topaz:prefValue&gt; '-value-'".
 *
 * @author Ronald Tschalär
 */
public class PreferencesImpl implements Preferences {
  private static final Log    log            = LogFactory.getLog(PreferencesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL          = "<" + CONF.getString("topaz.models.preferences") + ">";
  private static final String MODEL_TYPE     =
    "<" + CONF.getString("topaz.models.preferences[@type]", "tucana:Model") + ">";
  private static final String USER_MODEL     = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String PREFS_PATH_PFX = "preferences";

  private static final Map    aliases;

  private static final String ITQL_CLEAR_PREFS =
      /* Note: there's some odd bug in Kowari 1.0.5. This query will fail under certain data
       * conditions with the error
       * (TuplesException) Prefix failed to meet defined minimum prefix { defined<=L[ -1 -1 -1 * ]
       * provided<=L[ 1446 ] }
       * Whatever that means. Kowari 1.1.0-pre2 has this fixed.
       */
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
           // individual preferences
       "   (<${userId}> <topaz:hasPreferences> $y and " +
       "    $y <dc_terms:mediator> ${appId} and $y <topaz:preference> $s or " +
           // the statements with the preferences node as subject
       "    <${userId}> <topaz:hasPreferences> $s and $s <dc_terms:mediator> ${appId} or " +
           // the statement with the preferences node as object
       "    $s <tucana:is> <${userId}> and $p <tucana:is> <topaz:hasPreferences> and " +
       "      $o <dc_terms:mediator> ${appId} )" +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_PREFS =
      ("select $p $o from ${MODEL} where " +
       "<${userId}> <topaz:hasPreferences> $prefs and $prefs <dc_terms:mediator> ${appId} and " +
       "$prefs <topaz:preference> $pr and $pr <topaz:prefName> $p and $pr <topaz:prefValue> $o;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${USER_MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${USER_MODEL}", USER_MODEL);

  private final TopazContext   ctx;
  private final PreferencesPEP pep;
  private final String         baseURI;

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
   * Create a new preferences instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz context
   *
   */
  public PreferencesImpl(PreferencesPEP pep, TopazContext ctx) {
    this.ctx   = ctx;
    this.pep   = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();
  }
  /** 
   * Create a new preferences manager instance. 
   *
   * @param itql the itql-helper to use to access the triple store
   * @param pep  the policy-enforcer to use for access-control
   * @throws IOException if an error occurred initializing the itql service
   * @throws ConfigurationException if any required config is missing
   */
  public PreferencesImpl(ItqlHelper itql, PreferencesPEP pep)
      throws IOException, ConfigurationException {
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
   * Create a new preferences manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara service
   */
  public PreferencesImpl(ProtectedService mulgaraSvc, PreferencesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraSvc), pep);
  }

  /** 
   * Create a new preferences manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws ConfigurationException if any required config is missing
   * @throws IOException if an error occurred talking to the mulgara service
   */
  public PreferencesImpl(URI mulgaraUri, PreferencesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraUri), pep);
  }

  public UserPreference[] getPreferences(String appId, String userId)
      throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.GET_PREFERENCES, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Getting preferences for '" + userId + "', app='" + appId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    StringAnswer ans;
    try {
      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId) +
                   ItqlHelper.bindValues(ITQL_GET_PREFS, "userId", userId,
                                         "appId", formatAppId(appId));
      ans = new StringAnswer(itql.doQuery(qry, aliases));
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting preferences for user '" + userId + "'", ae);
    }

    List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
    if (user.size() == 0)
      throw new NoSuchUserIdException(userId);

    List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
    if (rows.size() == 0)
      return null;

    Map prefs = new HashMap();

    for (int idx = 0; idx < rows.size(); idx++) {
      String[] row = (String[]) rows.get(idx);

      List vl = (List) prefs.get(row[0]);
      if (vl == null)
        prefs.put(row[0], vl = new ArrayList());
      vl.add(row[1]);
    }

    UserPreference[] res = new UserPreference[prefs.size()];
    int idx = 0;
    for (Iterator iter = prefs.entrySet().iterator(); iter.hasNext(); idx++) {
      Map.Entry pref = (Map.Entry) iter.next();

      res[idx] = new UserPreference();
      res[idx].setName((String) pref.getKey());
      res[idx].setValues((String[]) ((List) pref.getValue()).toArray(new String[0]));
    }

    return res;
  }

  public void setPreferences(String appId, String userId, UserPreference[] prefs)
      throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.SET_PREFERENCES, ItqlHelper.validateUri(userId, "userId"));

    if (appId == null && prefs != null)
      throw new IllegalArgumentException("prefs must be null if app-id is null");

    if (log.isDebugEnabled())
      log.debug("Setting preferences for '" + userId + "', app='" + appId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-prefs " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(userId))
        throw new NoSuchUserIdException(userId);

      StringBuffer cmd = new StringBuffer(100);

      cmd.append(
          ItqlHelper.bindValues(ITQL_CLEAR_PREFS, "userId", userId, "appId", formatAppId(appId)));

      if (prefs != null && prefs.length > 0) {
        cmd.append("insert ");

        String prefId = getPrefsId(userId, appId);

        addReference(cmd, userId, "topaz:hasPreferences", prefId);
        addLiteralVal(cmd, prefId, "dc_terms:mediator", appId);

        for (int idx = 0; idx < prefs.length; idx++) {
          String[] values = prefs[idx].getValues();
          if (values == null || values.length == 0)
            continue;

          String pid = prefId + "/" + idx;
          addReference(cmd, prefId, "topaz:preference", pid);
          addLiteralVal(cmd, pid, "topaz:prefName", prefs[idx].getName());

          for (int idx2 = 0; idx2 < values.length; idx2++)
            addLiteralVal(cmd, pid, "topaz:prefValue", values[idx2]);
        }

        cmd.append(" into ").append(MODEL).append(";");
      }

      itql.doUpdate(cmd.toString(), aliases);

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

  private static final String formatAppId(String appId) {
    return (appId != null) ?  "'" + ItqlHelper.escapeLiteral(appId) + "'" : "$appId";
  }

  /**
   * Check if an account for the given user exists.
   *
   * @param userId the user's internal id
   * @return true if the user has an account
   * @throws RemoteException if an error occurred talking to the db
   */
  protected boolean userExists(String userId) throws RemoteException {
    ItqlHelper itql = ctx.getItqlHelper();
    try {
      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry, aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if user '" + userId + "' exists", ae);
    }
  }

  /** 
   * Convert a profile-id and app-id to a preferences-id. We assume a single preferences node per
   * app-id for a given profId, so the preferences id can be computed algorithmically.
   * 
   * @param userId  the user id
   * @param appId   the app id
   * @return the preferences id
   */
  protected String getPrefsId(String userId, String appId) {
    int slash = userId.lastIndexOf('/');
    try {
      return baseURI + PREFS_PATH_PFX + userId.substring(slash + 1) + "/" +
             URLEncoder.encode(appId, "UTF-8");
    } catch (UnsupportedEncodingException uue) {
      throw new RuntimeException("Unexpected encoding problem", uue);   // can't really happen
    }
  }
}
