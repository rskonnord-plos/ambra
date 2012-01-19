/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.permission.service;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

import org.topazproject.otm.Connection;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.stores.ItqlStore.ItqlStoreConnection;

import org.apache.struts2.ServletActionContext;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * This provides the implementation of the permissions service.<p>Grants and Revokes are stored
 * in a seperate models with 1 triple per permission like this:<pre>
 * &lt;${resource}&gt; &lt;${permission}&gt; &lt;${principal}&gt;</pre></p>
 *
 * @author Pradeep Krishnan
 */
public class PermissionsImpl implements Permissions {
  private static final Log log = LogFactory.getLog(PermissionsImpl.class);

  //
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  //
  private static final String GRANTS_MODEL       =
    "<" + CONF.getString("topaz.models.grants") + ">";
  private static final String REVOKES_MODEL      =
    "<" + CONF.getString("topaz.models.revokes") + ">";
  private static final String PP_MODEL           = "<" + CONF.getString("topaz.models.pp") + ">";
  private static final String GRANTS_MODEL_TYPE  =
    "<" + CONF.getString("topaz.models.grants[@type]", "mulgara:Model") + ">";
  private static final String REVOKES_MODEL_TYPE =
    "<" + CONF.getString("topaz.models.revokes[@type]", "mulgara:Model") + ">";
  private static final String PP_MODEL_TYPE      =
    "<" + CONF.getString("topaz.models.pp[@type]", "mulgara:Model") + ">";

  //
  private static final String IMPLIES    = ItqlHelper.TOPAZ_URI + "implies";
  private static final String PROPAGATES = ItqlHelper.TOPAZ_URI + "propagate-permissions-to";

  //
  private static final String ITQL_LIST                 =
    "select $p from ${MODEL} where <${resource}> $p <${principal}>;";
  private static final String ITQL_LIST_PP              =
    "select $o from ${MODEL} where <${s}> <${p}> $o".replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_LIST_PP_TRANS        =
    ("select $o from ${MODEL} where <${s}> <${p}> $o "
    + " or (trans($s <${p}> $o) and $s <mulgara:is> <${s}>);").replaceAll("\\Q${MODEL}", PP_MODEL);
  private static final String ITQL_INFER_PERMISSION     =
    ("select $s from ${PP_MODEL} where $s $p $o in ${MODEL} "
    + "and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>)) "
    + "and ($p <mulgara:is> <${permission}> or $p <mulgara:is> <${ALL}> "
    + "      or $p <${IMPLIES}> <${permission}> "
    + "      or (trans($p <${IMPLIES}> $perm) and $perm <mulgara:is> <${permission}>)) "
    + "and ($o <mulgara:is> <${principal}> or $o <mulgara:is> <${ALL}>)" //
    ).replaceAll("\\Q${PP_MODEL}", PP_MODEL).replaceAll("\\Q${PP}", PROPAGATES)
      .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);
  private static final String ITQL_RESOURCE_PERMISSIONS =
    ("select $p $o from ${PP_MODEL} where ($s $p $o in ${MODEL} " //
    + "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>))"
    + ") or ($s $impliedBy $o in ${MODEL} " //
    + "   and ($impliedBy <${IMPLIES}> $p " //
    + "      or trans($impliedBy <${IMPLIES}> $p)) " //
    + "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> "
    + "      or $s <${PP}> <${resource}> "
    + "      or (trans($s <${PP}> $res) and $res <mulgara:is> <${resource}>))" + ")" //
    ).replaceAll("\\Q${PP_MODEL}", PP_MODEL).replaceAll("\\Q${PP}", PROPAGATES)
      .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);

  //
  private static Ehcache grantsCache  = initCache("permission-grants");
  private static Ehcache revokesCache = initCache("permission-revokes");

  private static Ehcache initCache(String name) {
    Ehcache cache = null;

    try {
      cache = CacheManager.getInstance().getEhcache(name);
    } catch (CacheException ce) {
      log.error("Error getting cache-manager", ce);
    } catch (IllegalStateException ise) {
      log.error("Error getting cache", ise);
    }

    if (cache == null)
      log.info("No cache configuration found for " + name + ".");
    else
      log.info("Cache configuration found for " + name + ".");

    return cache;
  }

  /**
   * Initialize the permissions ITQL model.
   *
   * @param itql itql handle to use
   *
   * @throws RemoteException on a failure
   */
  public static void initializeModel(ItqlHelper itql) throws RemoteException {
    if (((grantsCache != null) && (grantsCache.getSize() != 0))
         || ((revokesCache != null) && (revokesCache.getSize() != 0)))
      return; // xxx: cache has entries perhaps from peers. so initialized is a good guess

    itql.doUpdate("create " + GRANTS_MODEL + " " + GRANTS_MODEL_TYPE + ";", null);
    itql.doUpdate("create " + REVOKES_MODEL + " " + REVOKES_MODEL_TYPE + ";", null);
    itql.doUpdate("create " + PP_MODEL + " " + PP_MODEL_TYPE + ";", null);

    Configuration conf        = CONF.subset("topaz.permissions.impliedPermissions");

    StringBuffer  sb          = new StringBuffer();
    List          permissions = conf.getList("permission[@uri]");
    int           c           = permissions.size();

    for (int i = 0; i < c; i++) {
      List implies = conf.getList("permission(" + i + ").implies[@uri]");
      log.info("config contains " + permissions.get(i) + " implies " + implies);

      for (int j = 0; j < implies.size(); j++) {
        sb.append("<").append(permissions.get(i)).append("> ");
        sb.append("<").append(IMPLIES).append("> ");
        sb.append("<").append(implies.get(j)).append("> ");
      }
    }

    String triples = sb.toString();
    String cmd     = "insert " + triples + " into " + PP_MODEL + ";";

    String txn     = "load implied-permissions from config";

    try {
      if (permissions.size() > 0) {
        itql.beginTxn(txn);
        itql.doUpdate(cmd, null);
        itql.commitTxn(txn);
      }
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Error rolling failed transaction", t);
      }
    }

    if (grantsCache != null)
      grantsCache.removeAll();

    if (revokesCache != null)
      revokesCache.removeAll();
  }

  //
  private final PermissionsPEP pep;

  /**
   * Create a new permission instance.
   *
   * @param pep the policy-enforcer to use for access-control
   */
  public PermissionsImpl(PermissionsPEP pep) {
    this.pep   = pep;
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#grant
   */
  public void grant(String resource, String[] permissions, String[] principals)
             throws RemoteException {
    updateModel(pep.GRANT, GRANTS_MODEL, grantsCache, resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#revoke
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException {
    updateModel(pep.REVOKE, REVOKES_MODEL, revokesCache, resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancleGrants
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws RemoteException {
    updateModel(pep.CANCEL_GRANTS, GRANTS_MODEL, grantsCache, resource, permissions, principals,
                false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelRevokes
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws RemoteException {
    updateModel(pep.CANCEL_REVOKES, REVOKES_MODEL, revokesCache, resource, permissions, principals,
                false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listGrants
   */
  public String[] listGrants(String resource, String principal)
                      throws RemoteException {
    return listPermissions(pep.LIST_GRANTS, GRANTS_MODEL, resource, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listRevokes
   */
  public String[] listRevokes(String resource, String principal)
                       throws RemoteException {
    return listPermissions(pep.LIST_REVOKES, REVOKES_MODEL, resource, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#implyPermission
   */
  public void implyPermissions(String permission, String[] implies)
                        throws RemoteException {
    updatePP(pep.IMPLY_PERMISSIONS, permission, IMPLIES, implies, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelImplyPermission
   */
  public void cancelImplyPermissions(String permission, String[] implies)
                              throws RemoteException {
    updatePP(pep.CANCEL_IMPLY_PERMISSIONS, permission, IMPLIES, implies, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listImpliedPermissions
   */
  public String[] listImpliedPermissions(String permission, boolean transitive)
                                  throws RemoteException {
    return listPP(pep.LIST_IMPLIED_PERMISSIONS, permission, IMPLIES, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#propagatePermissions
   */
  public void propagatePermissions(String resource, String[] to)
                            throws RemoteException {
    updatePP(pep.PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, true);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelPropagatePermissions
   */
  public void cancelPropagatePermissions(String resource, String[] to)
                                  throws RemoteException {
    updatePP(pep.CANCEL_PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, false);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listPermissionPropagations
   */
  public String[] listPermissionPropagations(String resource, boolean transitive)
                                      throws RemoteException {
    return listPP(pep.LIST_PERMISSION_PROPAGATIONS, resource, PROPAGATES, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isGranted(String resource, String permission, String principal)
                    throws RemoteException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (grantsCache == null)
      return isInferred(GRANTS_MODEL, resource, permission, principal);

    HashMap map;
    Element element = grantsCache.get(resource);

    if (element != null) {
      map = (HashMap) element.getValue();

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, GRANTS_MODEL);
      grantsCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    ArrayList list = (ArrayList) map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isRevoked(String resource, String permission, String principal)
                    throws RemoteException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (revokesCache == null)
      return isInferred(REVOKES_MODEL, resource, permission, principal);

    HashMap map;
    Element element = revokesCache.get(resource);

    if (element != null) {
      map = (HashMap) element.getValue();

      if (log.isDebugEnabled())
        log.debug("revokes-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, REVOKES_MODEL);
      revokesCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    ArrayList list = (ArrayList) map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  private void updateModel(String action, String model, Ehcache cache, String resource,
                           String[] permissions, String[] principals, boolean insert)
                    throws RemoteException {
    permissions = validateUriList(permissions, "permissions", false);

    if (permissions.length == 0)
      return;

    if ((principals == null) || (principals.length == 0))
      throw new NullPointerException("principal");

    principals = validateUriList(principals, "principals", false);

    pep.checkAccess(action, ItqlHelper.validateUri(resource, "resource"));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < principals.length; i++) {
      String principal = principals[i];
      for (int j = 0; j < permissions.length; j++) {
        sb.append("<").append(resource).append("> ");
        sb.append("<").append(permissions[j]).append("> ");
        sb.append("<").append(principal).append("> ");
      }
    }

    String triples = sb.toString();

    String cmd;

    if (insert)
      cmd = "insert " + triples + " into " + model + ";";
    else
      cmd = "delete " + triples + " from " + model + ";";

    doUpdate(cmd);

    if (cache != null)
      cache.remove(resource);

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private void updatePP(String action, final String subject, final String predicate,
                        String[] objects, boolean insert)
                 throws RemoteException {
    String sLabel;
    String oLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel       = "resource";
      oLabel       = "to[]";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
      oLabel   = "implies[]";
    } else {
      sLabel   = "subject";
      oLabel   = "object[]";
    }

    objects = validateUriList(objects, oLabel, false);

    if (objects.length == 0)
      return;

    pep.checkAccess(action, ItqlHelper.validateUri(subject, sLabel));

    StringBuffer sb = new StringBuffer(512);

    for (int i = 0; i < objects.length; i++) {
      sb.append("<").append(subject).append("> ");
      sb.append("<").append(predicate).append("> ");
      sb.append("<").append(objects[i]).append("> ");
    }

    String       triples = sb.toString();
    final String cmd;

    if (insert)
      cmd = "insert " + triples + " into " + PP_MODEL + ";";
    else
      cmd = "delete " + triples + " from " + PP_MODEL + ";";

    StringAnswer ans     =
      doInTxn(new Action<StringAnswer>() {
          public StringAnswer run(ItqlHelper itql) throws RemoteException {
            itql.doUpdate(cmd, null);

            try {
              if (((grantsCache != null) || (revokesCache != null)) && PROPAGATES.equals(predicate))
                return new StringAnswer(itql.doQuery(ItqlHelper.bindValues(ITQL_LIST_PP_TRANS, "s",
                                                                           subject, "p", predicate),
                                                     null));

              return null;
            } catch (AnswerException ae) {
              throw new RemoteException("Error while querying propagated resources", ae);
            }
          }
        });

    if (ans == null) {
      // implied permissions changed.
      if (grantsCache != null)
        grantsCache.removeAll();

      if (revokesCache != null)
        revokesCache.removeAll();
    } else {
      List rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      int  c    = rows.size();

      for (int i = 0; i < c; i++) {
        String res = ((String[]) rows.get(i))[0];

        if (grantsCache != null)
          grantsCache.remove(res);

        if (revokesCache != null)
          revokesCache.remove(res);
      }
    }
  }

  private String[] listPermissions(String action, String model, String resource, String principal)
                            throws RemoteException {
    if (principal == null)
      throw new NullPointerException("principal");

    ItqlHelper.validateUri(principal, "principal");

    pep.checkAccess(action, ItqlHelper.validateUri(resource, "resource"));

    try {
      HashMap map = new HashMap(3);
      map.put("resource", resource);
      map.put("principal", principal);
      map.put("MODEL", model);

      String       query  = ItqlHelper.bindValues(ITQL_LIST, map);

      StringAnswer ans    = new StringAnswer(doQuery(query));
      List         rows   = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      String[]     result = new String[rows.size()];

      for (int i = 0; i < result.length; i++)
        result[i] = ((String[]) rows.get(i))[0];

      return result;
    } catch (AnswerException ae) {
      throw new RemoteException("Error listing permissions for resource '" + resource
                                + "' and principal '" + principal + "'", ae);
    }
  }

  private String[] listPP(String action, String subject, String predicate, boolean transitive)
                   throws RemoteException {
    String sLabel;
    String oLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel   = "resource";
      oLabel   = "permission-propagates";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
      oLabel   = "implied-permissions";
    } else {
      sLabel   = "subject";
      oLabel   = "objects";
    }

    pep.checkAccess(action, ItqlHelper.validateUri(subject, sLabel));

    String query = transitive ? ITQL_LIST_PP_TRANS : ITQL_LIST_PP;
    query = ItqlHelper.bindValues(query, "s", subject, "p", predicate);

    try {
      StringAnswer ans    = new StringAnswer(doQuery(query));
      List         rows   = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      String[]     result = new String[rows.size()];

      for (int i = 0; i < result.length; i++)
        result[i] = ((String[]) rows.get(i))[0];

      return result;
    } catch (AnswerException ae) {
      throw new RemoteException("Error while loading " + oLabel + " for " + subject, ae);
    }
  }

  private boolean isInferred(String model, String resource, String permission, String principal)
                      throws RemoteException {
    if (principal == null)
      throw new NullPointerException("principal");

    ItqlHelper.validateUri(resource, "resource");
    ItqlHelper.validateUri(permission, "permission");
    ItqlHelper.validateUri(principal, "principal");

    HashMap values = new HashMap();
    values.put("resource", resource);
    values.put("permission", permission);
    values.put("principal", principal);
    values.put("MODEL", model);

    String query = ItqlHelper.bindValues(ITQL_INFER_PERMISSION, values);

    try {
      StringAnswer ans  = new StringAnswer(doQuery(query));
      List         rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();

      return rows.size() > 0;
    } catch (AnswerException ae) {
      throw new RemoteException("Error while querying inferred permissions", ae);
    }
  }

  private String[] validateUriList(String[] list, String name, boolean nullOk) {
    if (list == null)
      throw new NullPointerException(name + " list can't be null");

    // eliminate duplicates
    list   = (String[]) (new HashSet(Arrays.asList(list))).toArray(new String[0]);

    name   = name + " list item";

    for (int i = 0; i < list.length; i++) {
      if (list[i] != null)
        ItqlHelper.validateUri(list[i], name);
      else if (!nullOk)
        throw new NullPointerException(name + " can't be null");
    }

    return list;
  }

  private HashMap createPermissionMap(String resource, String model)
                               throws RemoteException {
    String query =
      ItqlHelper.bindValues(ITQL_RESOURCE_PERMISSIONS, "resource", resource, "MODEL", model);

    try {
      StringAnswer ans  = new StringAnswer(doQuery(query));
      List         rows = ((StringAnswer.StringQueryAnswer) ans.getAnswers().get(0)).getRows();
      HashMap      map  = new HashMap();

      for (int i = 0; i < rows.size(); i++) {
        String[]  result = (String[]) rows.get(i);
        ArrayList list   = (ArrayList) map.get(result[0]);

        if (list == null) {
          list = new ArrayList();
          map.put(result[0], list);
        }

        list.add(result[1]);
      }

      return map;
    } catch (AnswerException ae) {
      throw new RemoteException("Error while querying inferred permissions", ae);
    }
  }

  private <T> T doInTxn(Action<T> action) throws RemoteException {
    boolean     wasActive = false;
    Transaction txn       = null;

    try {
      Session s = (Session) WebApplicationContextUtils
        .getRequiredWebApplicationContext(ServletActionContext.getServletContext())
        .getBean("otmSession");

      txn         = s.getTransaction();
      wasActive   = (txn != null);

      if (txn == null)
        txn = s.beginTransaction();

      if (txn == null)
        throw new RemoteException("Failed to start an otm transaction");

      Connection isc = txn.getConnection();

      if (!(isc instanceof ItqlStoreConnection))
        throw new RemoteException("Expecting an instance of " + ItqlStoreConnection.class);

      ItqlHelper itql = ((ItqlStoreConnection) isc).getItqlHelper();

      if (itql == null)
        throw new RemoteException("Failed to get an instance of " + ItqlHelper.class);

      T res = action.run(itql);

      if (!wasActive)
        txn.commit();

      txn = null;

      return res;
    } catch (RuntimeException e) {
      throw new RemoteException("Unable to execute transaction ", e);
    } finally {
      try {
        if (!wasActive && (txn != null))
          txn.rollback();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("rollback failed", t);
      }
    }
  }

  private String doQuery(final String query) throws RemoteException {
    return doInTxn(new Action<String>() {
        public String run(ItqlHelper itql) throws RemoteException {
          return itql.doQuery(query, null);
        }
      });
  }

  private void doUpdate(final String cmd) throws RemoteException {
    doInTxn(new Action<String>() {
        public String run(ItqlHelper itql) throws RemoteException {
          itql.doUpdate(cmd, null);

          return cmd;
        }
      });
  }

  private static interface Action<T> {
    public T run(ItqlHelper itql) throws RemoteException;
  }
  ;
}
