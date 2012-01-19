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
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.mulgara.itql.StringAnswer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.impl.SimpleTopazContext;

import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserRoles;

/** 
 * This provides the implementation of the user-roles service.
 * 
 * <p>A user's roles are stored as a list of (&lt;rolesNode&gt; &lt;topaz:role&gt; 'role') triples
 * where the rolesNode is associated with the foaf:OnlineAccount via &lt;topaz:hasRoles&gt;
 *
 * @author Ronald Tschalär
 */
public class UserRolesImpl implements UserRoles {
  private static final Log    log            = LogFactory.getLog(UserRolesImpl.class);

  private static final String FOAF_URI       = "http://xmlns.com/foaf/0.1/";

  private static final Configuration CONF    = ConfigurationStore.getInstance().getConfiguration();

  private static final String MODEL          = "<" + CONF.getString("topaz.models.users") + ">";
  private static final String MODEL_TYPE     =
      "<" + CONF.getString("topaz.models.users[@type]", "tucana:Model") + ">";
  private static final String ROLES_PATH_PFX = "roles";

  private static final Map    aliases;

  private static final String ITQL_TEST_USERID =
      ("select $userId from ${MODEL} where " +
       "  $userId <rdf:type> <foaf:OnlineAccount> and $userId <tucana:is> <${userId}>;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_GET_ROLES =
      ("select $role from ${MODEL} where " +
       "  <${userId}> <rdf:type> <foaf:OnlineAccount> and <${userId}> <topaz:hasRoles> $roles " +
       "  and $roles <topaz:role> $role;").
      replaceAll("\\Q${MODEL}", MODEL);

  private static final String ITQL_CLEAR_ROLES =
      ("delete select $s $p $o from ${MODEL} where $s $p $o and " +
       "  ( $s <tucana:is> <${userId}> and $p <tucana:is> <topaz:hasRoles> or " +
       "    $x <tucana:is> <${userId}> and $x <topaz:hasRoles> $s )" +
       " from ${MODEL};").
      replaceAll("\\Q${MODEL}", MODEL);

  private final TopazContext    ctx;
  private final UserRolesPEP    pep;
  private final String          baseURI;

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
   * Create a new user accounts manager instance. 
   *
   * @param pep  the policy-enforcer to use for access-control
   * @param ctx the topaz context
   */
  public UserRolesImpl(UserRolesPEP pep, TopazContext ctx) {
    this.pep  = pep;
    this.baseURI = ctx.getObjectBaseUri().toString();
    this.ctx = ctx;
  }

  /** 
   * Create a new user roles manager instance. 
   *
   * @param itql the mulgara itql-service
   * @param pep  the policy-enforcer to use for access-control
   * @throws IOException if an error occurred initializing the itql service
   * @throws ConfigurationException if any required config is missing
   */
  public UserRolesImpl(ItqlHelper itql, UserRolesPEP pep)
      throws IOException, ConfigurationException {
    this.pep  = pep;
    this.ctx = new SimpleTopazContext(itql, null, null);

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
   * @param pep        the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws IOException if an error occurred talking to the mulgara service
   * @throws ConfigurationException if any required config is missing
   */
  public UserRolesImpl(ProtectedService mulgaraSvc, UserRolesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new ItqlHelper(mulgaraSvc), pep);
  }

  /** 
   * Create a new user accounts manager instance. 
   *
   * @param mulgaraUri  the uri of the mulgara server
   * @param pep         the policy-enforcer to use for access-control
   * @throws ServiceException if an error occurred locating the mulgara service
   * @throws IOException if an error occurred talking to the mulgara service
   * @throws ConfigurationException if any required config is missing
   */
  public UserRolesImpl(URI mulgaraUri, UserRolesPEP pep)
      throws IOException, ServiceException, ConfigurationException {
    this(new UnProtectedService(mulgaraUri.toString()), pep);
  }

  public String[] getRoles(String userId) throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.GET_ROLES, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Getting roles for '" + userId + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    try {
      String qry = ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId) +
                   ItqlHelper.bindValues(ITQL_GET_ROLES, "userId", userId);
      StringAnswer ans = new StringAnswer(itql.doQuery(qry, aliases));

      List user = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      if (user.size() == 0)
        throw new NoSuchUserIdException(userId);

      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(1)).getRows();
      if (rows.size() == 0) {
        if (log.isDebugEnabled())
          log.debug("No roles found for '" + userId + "'");
        return null;
      }

      String[] roles = new String[rows.size()];
      for (int idx = 0; idx < roles.length; idx++)
        roles[idx] = ((String[]) rows.get(idx))[0];

      if (log.isDebugEnabled())
        log.debug("Roles found for '" + userId + "': '" + StringUtils.join(roles, "', '") + "'");

      return roles;
    } catch (AnswerException ae) {
      throw new RemoteException("Error getting roles for user '" + userId + "'", ae);
    }
  }

  public void setRoles(String userId, String[] roles)
      throws NoSuchUserIdException, RemoteException {
    pep.checkAccess(pep.SET_ROLES, ItqlHelper.validateUri(userId, "userId"));

    if (log.isDebugEnabled())
      log.debug("Setting roles for '" + userId + "': '" + StringUtils.join(roles, "', '") + "'");

    ItqlHelper itql = ctx.getItqlHelper();
    String txn = "set-roles " + userId;
    try {
      itql.beginTxn(txn);

      if (!userExists(userId))
        throw new NoSuchUserIdException(userId);

      StringBuffer cmd = new StringBuffer(100);

      cmd.append(ItqlHelper.bindValues(ITQL_CLEAR_ROLES, "userId", userId));

      if (roles != null && roles.length > 0) {
        String rolesId = getRolesId(userId);

        cmd.append("insert ");
        cmd.append("<").append(userId).append("> <topaz:hasRoles> <").append(rolesId).append("> ");

        for (int idx = 0; idx < roles.length; idx++) {
          if (roles[idx] != null)
            cmd.append("<").append(rolesId).append("> <topaz:role> '").
                append(ItqlHelper.escapeLiteral(roles[idx])).append("' ");
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
      StringAnswer ans =
          new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_TEST_USERID, "userId", userId), aliases));
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error testing if user '" + userId + "' exists", ae);
    }
  }

  /** 
   * Convert a user-id to a role-id. We assume a single roles node per user, so the id can be
   * computed algorithmically.
   * 
   * @param userId the user's id
   * @return the roles url
   */
  protected String getRolesId(String userId) {
    int slash = userId.lastIndexOf('/');
    return baseURI + ROLES_PATH_PFX + userId.substring(slash + 1);
  }
}
