/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.SimpleTopazContext;

import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountLookup;

/** 
 * This provides the implementation of the user-accounts service.
 * 
 * <p>A user account is stored as a foaf:OnlineAccount node. The node's URI is the interal topaz
 * user id.
 *
 * @author Ronald Tschalär
 */
public class UserAccountsImpl implements UserAccounts, UserAccountLookup {
  private static final Log    log            = LogFactory.getLog(UserAccountsImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL          = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String MODEL_TYPE     =
      "<" + CONF.getString("topaz.models.users[@type]", "tucana:Model") + ">";
  private static final String ACCOUNT_PID_NS = "account";
  private static final String AUTH_PATH_PFX  = "authids";

  private static final Map    aliases;

  private static final String ITQL_CREATE_ACCT =
      ("insert <${userId}> <rdf:type> <foaf:OnlineAccount> " +
              "<${userId}> <topaz:hasAuthId> <${auth}>" +
              "<${auth}> <rdf:value> '${authId}'" +
              "<${auth}> <topaz:realm> 'local'" +
              "<${userId}> <topaz:accountState> '0'^^<xsd:int> into ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_DELETE_ACCT =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and $s <tucana:is> <${userId}> " +
       "  from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_USERID =
      ("select $userId from ${MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <topaz:hasAuthId> $auth and " +
       "  $auth <rdf:value> '${authId}';").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_STATE =
      ("select $state from ${MODEL} where " +
       " <${userId}> <rdf:type> <foaf:OnlineAccount> and <${userId}> <topaz:accountState> $state;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_STATE =
      ("delete select <${userId}> <topaz:accountState> $o from ${MODEL} where " +
       "  <${userId}> <rdf:type> <foaf:OnlineAccount> and <${userId}> <topaz:accountState> $o " +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_AUTH_IDS =
      ("select $authId from ${MODEL} where " +
       "  <${userId}> <rdf:type> <foaf:OnlineAccount> and <${userId}> <topaz:hasAuthId> $auth " +
       "  and $auth <rdf:value> $authId;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_AUTH_IDS =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
       "  ($s <tucana:is> <${userId}> and $p <tucana:is> <topaz:hasAuthId> or " +
       "   <${userId}> <topaz:hasAuthId> $s) " +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_FIND_DUP_AUTHIDS_PRE =
      ("select $authId count(" +
       "    select $userId from ${MODEL} " +
       "    where $userId <rdf:type> <foaf:OnlineAccount> and $userId <topaz:hasAuthId> $auth " +
       "          and $auth <rdf:value> $authId " +
       "  ) " +
       "  from ${MODEL} " +
       "  where (").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_FIND_DUP_AUTHIDS_POST =
      ("  ) having $k0 <tucana:occursMoreThan> '1.0'^^<http://www.w3.org/2001/XMLSchema#double>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private final TopazContext    ctx;
  private final UserAccountsPEP pep;
  private final String          baseURI;

  private String[] newAcctIds = new String[0];
  private int      newAcctIdIdx;

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
   * Create a new user lookup instance. <em>Only use this if all you intend to use is the
   * {@link UserAccountLookup UserAccountLookup} interface.</em>
   *
   * @param itql the mulgara itql-service
   * @throws IOException if an error occurred initializing the itql service
   */
  public UserAccountsImpl(ItqlHelper itql) throws IOException {
    this.pep     = null;
    this.baseURI = null;
    this.ctx = new SimpleTopazContext(itql, null, null);
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param pep  the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public UserAccountsImpl(UserAccountsPEP pep, TopazContext ctx) {
    this.pep     = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();
    this.ctx     = ctx;
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param itql the mulgara itql-service
   * @param apim the fedora management web-service
   * @param pep  the policy-enforcer to use for access-control
   * @throws IOException if an error occurred initializing the itql service
   * @throws ConfigurationException if any required config is missing
   */
  public UserAccountsImpl(ItqlHelper itql, FedoraAPIM apim, UserAccountsPEP pep)
      throws IOException, ConfigurationException {
    this.pep  = pep;
    this.ctx = new SimpleTopazContext(itql, apim, null);

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

  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param mulgaraSvc the mulgara web-service
   * @param fedoraSvc  the fedora management web-service
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   * @throws ConfigurationException if any required config is missing
   */
  public UserAccountsImpl(ProtectedService mulgaraSvc, ProtectedService fedoraSvc,
                          UserAccountsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraSvc), APIMStubFactory.create(fedoraSvc), pep);
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param fedoraUri   the uri of fedora
   * @param username    the username to talk to fedora
   * @param password    the password to talk to fedora
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara or fedora services
   * @throws IOException if an error occurred talking to the mulgara or fedora services
   */
  public UserAccountsImpl(URI mulgaraUri, URI fedoraUri, String username, String password,
                          UserAccountsPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()),
         new PasswordProtectedService(fedoraUri.toString(), username, password),
         pep);
  }

  public String createUser(String authId) throws DuplicateAuthIdException, RemoteException {
    pep.checkAccess(pep.CREATE_USER, URI.create(baseURI + ACCOUNT_PID_NS));

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "create user";
    try {
      itql.beginTxn(txn);

      String userId = getNewAcctId();
      while (userExists(itql, userId)) {
        // this shouldn't really happen...
        log.warn("Generated duplicate id '" + userId + "' - trying again...");
        userId = getNewAcctId();
      }

      String auth = getNewAuthId(userId, 0);

      Map params = new HashMap();
      params.put("userId", userId);
      params.put("auth", auth);
      params.put("authId", ItqlHelper.escapeLiteral(authId));

      itql.doUpdate(ItqlHelper.bindValues(ITQL_CREATE_ACCT, params), aliases);

      itql.commitTxn(txn);

      /* This checking for the dup-auth-id in a separate tx is ugly, but since threre's
       * no way to create a unique constraint and no equivalent of select-for-update, this
       * is the best we can do.
       */
      txn = "remove duplicate user";
      itql.beginTxn(txn);

      if (findDupAuthId(itql, new String[] { authId }) != null) {
        try {
          itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_ACCT, "userId", userId), aliases);
          itql.commitTxn(txn);
          txn = null;
        } catch (RemoteException re) {
          // we're SOL
          log.error("Failed to delete new user account with duplicate id! userId='" + userId +
                    "', authId='" + authId + "'");
        }
        throw new DuplicateAuthIdException(authId);
      }

      if (log.isDebugEnabled())
        log.debug("Created user '" + userId + "'");

      return userId;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        log.debug("Error rolling failed transaction", t);
      }
    }
  }

  public void deleteUser(String userId) throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.DELETE_USER, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Deleting user '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "delete " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(itql, userId))
        throw new NoSuchUserIdException(userId);

      itql.doUpdate(ItqlHelper.bindValues(ITQL_DELETE_ACCT, "userId", userId), aliases);

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

  public int getState(String userId) throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.GET_STATE, ItqlHelper.validateUri(userId, "userId"));

    return getStateInternal(userId);
  }

  public int getStateNoAC(String userId) throws NoSuchUserIdException, RemoteException {
    ItqlHelper.validateUri(userId, "userId");

    return getStateInternal(userId);
  }

  private int getStateInternal(String userId) throws NoSuchUserIdException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Getting the account state for '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_GET_STATE, "userId", userId),
                                        aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (rows.size() == 0) {
        if (!userExists(itql, userId))
          throw new NoSuchUserIdException(userId);

        log.error("No state found for user '" + userId + "' - resetting to 0");
        setStateInternal(userId, 0);
        return 0;
      }

      int state = Integer.parseInt(((String[]) rows.get(0))[0]);

      if (log.isDebugEnabled())
        log.debug("The account state for '" + userId + "' is " + state);

      return state;

    } catch (AnswerException ae) {
      throw new RemoteException("Error getting state for user '" + userId + "'", ae);
    } catch (NumberFormatException nfe) {
      log.error("Invalid state found for user '" + userId + "' - resetting to 0");
      setStateInternal(userId, 0);
      return 0;
    }
  }

  public void setState(String userId, int state) throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.SET_STATE, ItqlHelper.validateUri(userId, "userId"));
    setStateInternal(userId, state);
  }

  private void setStateInternal(String userId, int state)
      throws NoSuchUserIdException, RemoteException {
    if (log.isDebugEnabled())
      log.debug("Setting state for '" + userId + "' to " + state);

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-state " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(itql, userId))
        throw new NoSuchUserIdException(userId);

      StringBuffer cmd = new StringBuffer(100);

      cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_STATE, "userId", userId));

      cmd.append("insert <").append(userId).append("> <topaz:accountState> '").
                 append(state).append("'^^<xsd:int> ");
      cmd.append(" into ").append(MODEL).append(";");

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

  public String[] getAuthenticationIds(String userId)
      throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.GET_AUTH_IDS, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Getting auth-ids for '" + userId + "'");

    return getAuthenticationIds(ctx.getItqlHelper(), userId);
  }

  private String[] getAuthenticationIds(ItqlHelper itql, String userId)
      throws NoSuchUserIdException, RemoteException {
    try {
      String qry = ItqlHelper.bindValues(ITQL_GET_AUTH_IDS, "userId", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry, aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (rows.size() == 0 && !userExists(itql, userId))
        throw new NoSuchUserIdException(userId);

      String[] ids = new String[rows.size()];
      for (int idx = 0; idx < ids.length; idx++)
        ids[idx] = ((String[]) rows.get(idx))[0];

      return ids;
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting auth-ids for user '" + userId + "'", ae);
    }
  }

  public void setAuthenticationIds(String userId, String[] authIds)
      throws NoSuchUserIdException, DuplicateAuthIdException, RemoteException {
    pep.checkAccess(pep.SET_AUTH_IDS, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Setting auth-ids for '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-auth-ids " + userId;
    try {
      itql.beginTxn(txn);

      String[] oldIds = getAuthenticationIds(itql, userId);

      setAuthenticationIds(itql, userId, authIds);

      itql.commitTxn(txn);

      /* This checking for dup-auth-ids in a separate tx is ugly, but since threre's no
       * way to create a unique constraint and no equivalent of select-for-update, this
       * is the best we can do.
       */
      txn = "remove-duplicate-auth-ids " + userId;
      itql.beginTxn(txn);

      String dupId = findDupAuthId(itql, authIds);
      if (dupId != null) {
        try {
          setAuthenticationIds(itql, userId, oldIds);
          itql.commitTxn(txn);
          txn = null;
        } catch (RemoteException re) {
          // we're SOL
          log.error("Failed to reset auth-ids on user account with duplicate ids! userId='" +
                    userId + "'");
        }
        throw new DuplicateAuthIdException(dupId);
      }

    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        log.debug("Error rolling failed transaction", t);
      }
    }
  }

  private void setAuthenticationIds(ItqlHelper itql, String userId, String[] authIds)
      throws RemoteException {
    StringBuffer cmd = new StringBuffer(100);

    cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_AUTH_IDS, "userId", userId));

    if (authIds != null && authIds.length > 0) {
      cmd.append("insert ");
      for (int idx = 0; idx < authIds.length; idx++) {
        if (authIds[idx] != null) {
          String auth = getNewAuthId(userId, idx);
          cmd.append("<").append(userId).append("> <topaz:hasAuthId> <").
              append(auth).append("> ");
          cmd.append("<").append(auth).append("> <rdf:value> '").
              append(ItqlHelper.escapeLiteral(authIds[idx])).append("' ");
        }
      }
      cmd.append(" into ").append(MODEL).append(";");
    }

    itql.doUpdate(cmd.toString(), aliases);
  }

  public String lookUpUserByAuthId(String authId) throws RemoteException {
    pep.checkAccess(pep.LOOKUP_USER, URI.create(baseURI + ACCOUNT_PID_NS));

    return lookUpUserByAuthIdNoAC(authId);
  }

  public String lookUpUserByAuthIdNoAC(String authId) throws RemoteException {
    if (log.isDebugEnabled())
      log.debug("Looking up user for auth-id '" + authId + "'");

    if (authId == null)
      return null;

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      String qry =
          ItqlHelper.bindValues(ITQL_GET_USERID, "authId", ItqlHelper.escapeLiteral(authId));
      StringAnswer ans = new StringAnswer(itql.doQuery(qry, aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return (rows.size() > 0) ? ((String[]) rows.get(0))[0] : null;
    } catch (AnswerException ae) {
      throw new RemoteException("Error looking up user for auth-id '" + authId + "'", ae);
    }
  }

  /**
   * Check if an account for the given user exists.
   *
   * @param userId the user's internal id
   * @return true if the user has an account
   * @throws RemoteException if an error occurred talking to the db
   */
  protected boolean userExists(ItqlHelper itql, String userId) throws RemoteException {
    try {
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId),
                                        aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if user '" + userId + "' exists", ae);
    }
  }

  /** 
   * Look for duplicate authentication ids in the database. 
   * 
   * @param itql    the current ItqlHelper instance
   * @param authIds the list of ids to check
   * @return the first duplicate id, or null if none were found
   * @throws RemoteException if an error occurred talking to the database
   */
  protected String findDupAuthId(ItqlHelper itql, String[] authIds) throws RemoteException {
    if (authIds == null || authIds.length == 0)
      return null;

    StringBuffer qry = new StringBuffer(ITQL_FIND_DUP_AUTHIDS_PRE.length() + authIds.length * 30 +
                                        ITQL_FIND_DUP_AUTHIDS_POST.length());
    qry.append(ITQL_FIND_DUP_AUTHIDS_PRE);
    for (int idx = 0; idx < authIds.length; idx++)
      qry.append("$authId <tucana:is> '").append(ItqlHelper.escapeLiteral(authIds[idx])).
          append("' or ");
    qry.setLength(qry.length() - 4);
    qry.append(ITQL_FIND_DUP_AUTHIDS_POST);

    try {
      StringAnswer ans = new StringAnswer(itql.doQuery(qry.toString(), aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0 ? ((String[]) rows.get(0))[0] : null;
    } catch (AnswerException ae) {
      throw new RemoteException("Error searching for duplicate auth-ids", ae);
    }
  }

  /** 
   * Get an id (url) for a new account node. 
   * 
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewAcctId() throws RemoteException {
    if (newAcctIdIdx >= newAcctIds.length) {
      newAcctIds = ctx.getFedoraAPIM().getNextPID(new NonNegativeInteger("20"), ACCOUNT_PID_NS);
      newAcctIdIdx = 0;
    }

    return baseURI + newAcctIds[newAcctIdIdx++].replace(':', '/');
  }

  /** 
   * Get an id (url) for a new auth node. 
   * 
   * @param userId the user's id
   * @param idx    the index of the auth id
   * @return the url
   * @throws RemoteException if an error occurred getting the new id
   */
  protected synchronized String getNewAuthId(String userId, int idx) throws RemoteException {
    int slash = userId.lastIndexOf('/');
    return baseURI + AUTH_PATH_PFX + userId.substring(slash + 1) + "/" + idx;
  }
}
