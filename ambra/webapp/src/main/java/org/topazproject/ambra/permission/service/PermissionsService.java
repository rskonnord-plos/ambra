/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
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
package org.topazproject.ambra.permission.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.models.Ambra;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

import org.apache.struts2.ServletActionContext;

import com.sun.xacml.PDP;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * This provides the implementation of the permissions service.<p>Grants and Revokes are stored
 * in a seperate graphs with 1 triple per permission like this:<pre>
 * &lt;${resource}&gt; &lt;${permission}&gt; &lt;${principal}&gt;</pre></p>
 *
 * @author Pradeep Krishnan
 */
public class PermissionsService implements Permissions {
  private static final Logger log = LoggerFactory.getLogger(PermissionsService.class);

  public static final String GRANTS_GRAPH  = "<" + Ambra.GRAPH_PREFIX + "filter:graph=grants" + ">";
  public static final String REVOKES_GRAPH = "<" + Ambra.GRAPH_PREFIX + "filter:graph=revokes" + ">";
  public static final String PP_GRAPH      = "<" + Ambra.GRAPH_PREFIX + "filter:graph=pp" + ">";

  public static final String IMPLIES     = Rdf.topaz + "implies";
  public static final String PROPAGATES  = Rdf.topaz + "propagate-permissions-to";

  private static final String ITQL_LIST                 =
    "select $p from ${GRAPH} where <${resource}> $p <${principal}>;";
  private static final String ITQL_LIST_PP              =
    "select $o from ${GRAPH} where <${s}> <${p}> $o;".replaceAll("\\Q${GRAPH}", PP_GRAPH);
  private static final String ITQL_LIST_PP_TRANS        =
    ("select $o from ${GRAPH} where <${s}> <${p}> $o " +
     " or trans(<${s}> <${p}> $o and $s <${p}> $o);").replaceAll("\\Q${GRAPH}", PP_GRAPH);
  private static final String ITQL_INFER_PERMISSION     =
    ("select $s from ${PP_GRAPH} where $s $p $o in ${GRAPH} " +
     "and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> " +
     "      or $s <${PP}> <${resource}> " +
     "      or trans($s <${PP}> <${resource}> and $s <${PP}> $res))" +
     "and ($p <mulgara:is> <${permission}> or $p <mulgara:is> <${ALL}> " +
     "      or $p <${IMPLIES}> <${permission}> " +
     "      or trans($p <${IMPLIES}> <${permission}> and $p <${IMPLIES}> $perm)) " +
     "and ($o <mulgara:is> <${principal}> or $o <mulgara:is> <${ALL}>);"
    ).replaceAll("\\Q${PP_GRAPH}", PP_GRAPH).replaceAll("\\Q${PP}", PROPAGATES)
     .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);
  private static final String ITQL_RESOURCE_PERMISSIONS =
    ("select $p $o from ${PP_GRAPH} where ($s $p $o in ${GRAPH} " +
     "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> " +
     "      or $s <${PP}> <${resource}> " +
     "      or trans($s <${PP}> <${resource}> and $s <${PP}> $res))" +
     ") or ($s $impliedBy $o in ${GRAPH} " +
     "   and ($impliedBy <${IMPLIES}> $p " +
     "      or trans($impliedBy <${IMPLIES}> $p)) " +
     "   and ($s <mulgara:is> <${resource}> or $s <mulgara:is> <${ALL}> " +
     "      or $s <${PP}> <${resource}> " +
     "      or trans($s <${PP}> <${resource}> and $s <${PP}> $res))" + ");"
    ).replaceAll("\\Q${PP_GRAPH}", PP_GRAPH).replaceAll("\\Q${PP}", PROPAGATES)
     .replaceAll("\\Q${IMPLIES}", IMPLIES).replaceAll("\\Q${ALL}", ALL);

  private Ehcache        grantsCache;
  private Ehcache        revokesCache;
  private PermissionsPEP pep;

  @Required
  public void setGrantsEhCache(Ehcache grantsCache) {
    this.grantsCache = grantsCache;
  }

  @Required
  public void setRevokesEhCache(Ehcache revokesCache) {
    this.revokesCache = revokesCache;
  }

  /**
   * Creates a new PermissionsService object.
   *
   */
  public PermissionsService() {
  }

  @Required
  public void setPermissionsPdp(PDP pdp) {
    this.pep = new PermissionsPEP(pdp);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#grant
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void grant(String resource, String[] permissions, String[] principals)
             throws OtmException {
    updateGraph(ServicePermissions.GRANT, GRANTS_GRAPH, grantsCache,
                resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#revoke
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void revoke(String resource, String[] permissions, String[] principals)
              throws OtmException {
    updateGraph(ServicePermissions.REVOKE, REVOKES_GRAPH, revokesCache,
                resource, permissions, principals, true);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#cancleGrants
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws OtmException {
    updateGraph(ServicePermissions.CANCEL_GRANTS, GRANTS_GRAPH, grantsCache,
                resource, permissions, principals, false);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#cancelRevokes
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws OtmException {
    updateGraph(ServicePermissions.CANCEL_REVOKES, REVOKES_GRAPH, revokesCache,
                resource, permissions, principals, false);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#listGrants
   */
  @Transactional(readOnly = true)
  public String[] listGrants(String resource, String principal)
                      throws OtmException {
    return listPermissions(ServicePermissions.LIST_GRANTS, GRANTS_GRAPH,
                           resource, principal);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#listRevokes
   */
  @Transactional(readOnly = true)
  public String[] listRevokes(String resource, String principal)
                       throws OtmException {
    return listPermissions(ServicePermissions.LIST_REVOKES, REVOKES_GRAPH,
                           resource, principal);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#implyPermission
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void implyPermissions(String permission, String[] implies)
                        throws OtmException {
    updatePP(ServicePermissions.IMPLY_PERMISSIONS, permission, IMPLIES, implies, true);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#cancelImplyPermission
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelImplyPermissions(String permission, String[] implies)
                              throws OtmException {
    updatePP(ServicePermissions.CANCEL_IMPLY_PERMISSIONS, permission, IMPLIES, implies, false);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#listImpliedPermissions
   */
  @Transactional(readOnly = true)
  public String[] listImpliedPermissions(String permission, boolean transitive)
                                  throws OtmException {
    return listPP(ServicePermissions.LIST_IMPLIED_PERMISSIONS, permission, IMPLIES, transitive);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#propagatePermissions
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void propagatePermissions(String resource, String[] to)
                            throws OtmException {
    updatePP(ServicePermissions.PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, true);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#cancelPropagatePermissions
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void cancelPropagatePermissions(String resource, String[] to)
                                  throws OtmException {
    updatePP(ServicePermissions.CANCEL_PROPAGATE_PERMISSIONS, resource, PROPAGATES, to, false);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#listPermissionPropagations
   */
  @Transactional(readOnly = true)
  public String[] listPermissionPropagations(String resource, boolean transitive)
                                      throws OtmException {
    return listPP(ServicePermissions.LIST_PERMISSION_PROPAGATIONS,
                  resource, PROPAGATES, transitive);
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#isGranted
   */
  @Transactional(readOnly = true)
  public boolean isGranted(String resource, String permission, String principal)
                    throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (grantsCache == null)
      return isInferred(GRANTS_GRAPH, resource, permission, principal);

    Map<String, List<String>> map = getCacheEntry(grantsCache, resource);

    if (map != null) {
      if (log.isDebugEnabled())
        log.debug("grants-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, GRANTS_GRAPH);
      grantsCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    List<String> list = map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  /*
   * @see org.topazproject.ambra.permission.service.Permissions#isGranted
   */
  @Transactional(readOnly = true)
  public boolean isRevoked(String resource, String permission, String principal)
                    throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    if (revokesCache == null)
      return isInferred(REVOKES_GRAPH, resource, permission, principal);

    Map<String, List<String>> map = getCacheEntry(revokesCache, resource);

    if (map != null) {
      if (log.isDebugEnabled())
        log.debug("revokes-cache: cache hit for " + resource);
    } else {
      map = createPermissionMap(resource, REVOKES_GRAPH);
      revokesCache.put(new Element(resource, map));

      if (log.isDebugEnabled())
        log.debug("grants-cache: cache miss for " + resource);
    }

    List<String> list = map.get(permission);

    return (list != null) && (list.contains(principal) || list.contains(ALL));
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<String>> getCacheEntry(Ehcache cache, String key) {
    Element element = cache.get(key);
    if (element == null)
      return null;

    return (Map<String, List<String>>) element.getValue();
  }

  private void updateGraph(String action, String graph, Ehcache cache, String resource,
                           String[] permissions, String[] principals, boolean insert)
                    throws OtmException {
    permissions = validateUriList(permissions, "permissions", false);

    if (permissions.length == 0)
      return;

    if ((principals == null) || (principals.length == 0))
      throw new NullPointerException("principal");

    principals = validateUriList(principals, "principals", false);

    pep.checkAccess(action, RdfUtil.validateUri(resource, "resource"));

    StringBuilder sb = new StringBuilder(512);

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
      cmd = "insert " + triples + " into " + graph + ";";
    else
      cmd = "delete " + triples + " from " + graph + ";";

    getCurrentSession().doNativeUpdate(cmd);

    if (cache != null)
      cache.remove(resource);

    if (log.isInfoEnabled()) {
      log.info(action + " succeeded for resource " + resource + "\npermissions:\n"
               + Arrays.asList(permissions) + "\nprincipals:\n" + Arrays.asList(principals));
    }
  }

  private void updatePP(String action, final String subject, final String predicate,
                        String[] objects, boolean insert)
                 throws OtmException {
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

    pep.checkAccess(action, RdfUtil.validateUri(subject, sLabel));

    StringBuilder sb = new StringBuilder(512);

    for (int i = 0; i < objects.length; i++) {
      sb.append("<").append(subject).append("> ");
      sb.append("<").append(predicate).append("> ");
      sb.append("<").append(objects[i]).append("> ");
    }

    String       triples = sb.toString();
    final String cmd;

    if (insert)
      cmd = "insert " + triples + " into " + PP_GRAPH + ";";
    else
      cmd = "delete " + triples + " from " + PP_GRAPH + ";";

    getCurrentSession().doNativeUpdate(cmd);

    if ((grantsCache == null) && (revokesCache == null))
      return;

    if (!PROPAGATES.equals(predicate)) {
      // implied permissions changed.
      if (grantsCache != null)
        grantsCache.removeAll();

      if (revokesCache != null)
        revokesCache.removeAll();
    } else {
      Results ans = getCurrentSession().doNativeQuery(
                        RdfUtil.bindValues(ITQL_LIST_PP_TRANS, "s", subject, "p", predicate));
      while (ans.next()) {
        String res = ans.getString(0);

        if (grantsCache != null)
          grantsCache.remove(res);

        if (revokesCache != null)
          revokesCache.remove(res);
      }
    }
  }

  private String[] listPermissions(String action, String graph, String resource, String principal)
                            throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    RdfUtil.validateUri(principal, "principal");

    pep.checkAccess(action, RdfUtil.validateUri(resource, "resource"));

    Map<String, String> map = new HashMap<String, String>(3);
    map.put("resource", resource);
    map.put("principal", principal);
    map.put("GRAPH", graph);

    String  query = RdfUtil.bindValues(ITQL_LIST, map);
    Results ans   = getCurrentSession().doNativeQuery(query);

    List<String> result = new ArrayList<String>();
    while (ans.next())
      result.add(ans.getString(0));

    return result.toArray(new String[result.size()]);
  }

  private String[] listPP(String action, String subject, String predicate, boolean transitive)
                   throws OtmException {
    String sLabel;

    if (PROPAGATES.equals(predicate)) {
      sLabel   = "resource";
    } else if (IMPLIES.equals(predicate)) {
      sLabel   = "permission";
    } else {
      sLabel   = "subject";
    }

    pep.checkAccess(action, RdfUtil.validateUri(subject, sLabel));

    String query = transitive ? ITQL_LIST_PP_TRANS : ITQL_LIST_PP;
    query = RdfUtil.bindValues(query, "s", subject, "p", predicate);

    Results ans = getCurrentSession().doNativeQuery(query);

    List<String> result = new ArrayList<String>();
    while (ans.next())
      result.add(ans.getString(0));

    return result.toArray(new String[result.size()]);
  }

  private boolean isInferred(String graph, String resource, String permission, String principal)
                      throws OtmException {
    if (principal == null)
      throw new NullPointerException("principal");

    RdfUtil.validateUri(resource, "resource");
    RdfUtil.validateUri(permission, "permission");
    RdfUtil.validateUri(principal, "principal");

    HashMap<String, String> values = new HashMap<String, String>();
    values.put("resource", resource);
    values.put("permission", permission);
    values.put("principal", principal);
    values.put("GRAPH", graph);

    String  query = RdfUtil.bindValues(ITQL_INFER_PERMISSION, values);
    Results ans   = getCurrentSession().doNativeQuery(query);

    try {
      return ans.next();
    } finally {
      ans.close();
    }
  }

  private String[] validateUriList(String[] list, String name, boolean nullOk) {
    if (list == null)
      throw new NullPointerException(name + " list can't be null");

    // eliminate duplicates
    list   = (new HashSet<String>(Arrays.asList(list))).toArray(new String[0]);

    name   = name + " list item";

    for (int i = 0; i < list.length; i++) {
      if (list[i] != null)
        RdfUtil.validateUri(list[i], name);
      else if (!nullOk)
        throw new NullPointerException(name + " can't be null");
    }

    return list;
  }

  private Map<String, List<String>> createPermissionMap(String resource, String graph)
        throws OtmException {
    String query =
      RdfUtil.bindValues(ITQL_RESOURCE_PERMISSIONS, "resource", resource, "GRAPH", graph);

    Results                   ans = getCurrentSession().doNativeQuery(query);
    Map<String, List<String>> map = new HashMap<String, List<String>>();

    while (ans.next()) {
      List<String> list = map.get(ans.getString(0));

      if (list == null) {
        list = new ArrayList<String>();
        map.put(ans.getString(0), list);
      }

      list.add(ans.getString(1));
    }

    return map;
  }

  private Session getCurrentSession() {
    return (Session) WebApplicationContextUtils
      .getRequiredWebApplicationContext(ServletActionContext.getServletContext())
      .getBean("otmSession");
  }
}
