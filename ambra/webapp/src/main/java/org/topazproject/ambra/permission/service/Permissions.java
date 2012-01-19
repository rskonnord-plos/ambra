/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.permission.service;

/**
 * This defines the permissions service.
 *
 * @author Pradeep Krishnan
 */
public interface Permissions {
  /**
   * A URI to indicate all resources, permissions or subjects. eg. to grant all permissions
   * to a principal on resource, the permissions URI can be set to a this URI. To grant a specific
   * permission to all subjects on all resources, the resource and subject URIs can be set to this
   * URI.
   */
  public static final String ALL = "http://rdf.topazproject.org/RDF/permissions#all";

  /**
   * Permissions associated with permissions service.
   */
  public static interface ServicePermissions {
    /**
     * The action that represents a permission grant operation in XACML policies.
     */
    public static final String GRANT = "permissions:grant";

    /**
     * The action that represents a permission revoke operation in XACML policies.
     */
    public static final String REVOKE = "permissions:revoke";

    /**
     * The action that represents canceling a permission grant operation in XACML policies.
     */
    public static final String CANCEL_GRANTS = "permissions:cancelGrants";

    /**
     * The action that represents cancelling a permission revoke operation in XACML
     * policies.
     */
    public static final String CANCEL_REVOKES = "permissions:cancelRevokes";

    /**
     * The action that represents a list permission grants operation in XACML policies.
     */
    public static final String LIST_GRANTS = "permissions:listGrants";

    /**
     * The action that represents a list permission revokes operation in XACML policies.
     */
    public static final String LIST_REVOKES = "permissions:listRevokes";

    /**
     * The action that represents an implyPermissions operation in XACML policies.
     */
    public static final String IMPLY_PERMISSIONS = "permissions:implyPermissions";

    /**
     * The action that represents a cancelImplyPermissions operation in XACML policies.
     */
    public static final String CANCEL_IMPLY_PERMISSIONS = "permissions:cancelImplyPermissions";

    /**
     * The action that represents a listImpliedPermissions operation in XACML policies.
     */
    public static final String LIST_IMPLIED_PERMISSIONS = "permissions:listImpliedPermissions";

    /**
     * The action that represents a propagatePermissions operation in XACML policies.
     */
    public static final String PROPAGATE_PERMISSIONS = "permissions:propagatePermissions";

    /**
     * The action that represents a cancelPropagatePermissions operation in XACML policies.
     */
    public static final String CANCEL_PROPAGATE_PERMISSIONS =
      "permissions:cancelPropagatePermissions";

    /**
     * The action that represents a listPropagatePermissions operation in XACML policies.
     */
    public static final String LIST_PERMISSION_PROPAGATIONS =
      "permissions:listPermissionPropagations";
  }

  /**
   * Grants permissions. Adds these to a grants list. A permit biased PEP may allow access to
   * any request if a match is found in the grants list. Similarly a deny biased PEP may deny
   * access to any request if no matching permissions are found in the grants list.
   *
   * @param resource the resource uri for which permissions are granted.
   * @param permissions the list of permissions being granted. (as a list of uris)
   * @param principals the list ofprincipals to whom this grant applies to. (as a list of uris) May
   *        be <code>null</code>, empty or may contain <code>null</code> to indicate the user that
   *        is making this call.
   */
  public void grant(String resource, String[] permissions, String[] principals);

  /**
   * Revokes permissions. Adds these to a revokes list. A deny biased PEP will deny access to
   * any request if a match is found in the revokes list. Similarly a permit biased PEP will allow
   * access to any request if no matching permissions are found in the revokes list.
   *
   * @param resource the resource uri for which permissions are revoked
   * @param permissions the list of permissions being revoked. (as a list of uris)
   * @param principals the list ofprincipals to whom this revoke applies to. (as a list of uris)
   *        May be <code>null</code>, empty or may contain <code>null</code> to indicate the user
   *        that is making this call.
   */
  public void revoke(String resource, String[] permissions, String[] principals);

  /**
   * Cancel earlier grants. Deletes permissions from a grants list.
   *
   * @param resource the resource uri for which permission grants are to be cancelled
   * @param permissions the list of permission grants being cancelled.
   * @param principals the list of principals to whom this cancellation applies to
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals);

  /**
   * Cancel earlier revokes. Deletes permissions from a revokes list.
   *
   * @param resource the resource uri for which permission revokes are to be cancelled
   * @param permissions the list of permission grants being cancelled.
   * @param principals the list of principals to whom this cancellation applies to
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals);

  /**
   * List explicit permission grants for a resource. (Note: does not imply permits).
   *
   * @param resource the resource uri for which permissions are to be listed
   * @param principal the principal for whom the query is perfromed or <code>null</code> for the
   *        user that is making this call.
   *
   * @return Returns the list of grants
   */
  public String[] listGrants(String resource, String principal);

  /**
   * List explicit permission grants for a resource. (Note: does not imply denials).
   *
   * @param resource the resource uri for which permissions are to be listed
   * @param principal the principal for whom the query is perfromed or <code>null</code> for the
   *        user that is making this call.
   *
   * @return Returns the list of revokes
   */
  public String[] listRevokes(String resource, String principal);

  /**
   * Add new permissions to the list of permissions directly implied by a permission.
   *
   * @param permission the permission that implies others
   * @param implies the list of permissions that are to be directly implied
   */
  public void implyPermissions(String permission, String[] implies);

  /**
   * Remove permissions from the list of permissions directly implied by a permission.
   *
   * @param permission the permission that implies other permissions
   * @param implies the list of permissions that are not to be directly implied
   */
  public void cancelImplyPermissions(String permission, String[] implies);

  /**
   * Return the list of permissions directly implied by a permission.
   *
   * @param permission the permission that implies other permissions
   * @param transitive include permissions that are transitive
   *
   * @return Returns the list of permissions that are directly implied
   */
  public String[] listImpliedPermissions(String permission, boolean transitive);

  /**
   * Propagate permissions on a resource to the supplied list of resources.
   *
   * @param resource the resource whose permissions are to be propagated
   * @param to the list of resources to propagate permissions to
   */
  public void propagatePermissions(String resource, String[] to);

  /**
   * Cancel the propagation of permissions to the list of resources.
   *
   * @param resource the resource whose permissions are to be propagated
   * @param to the list of resources to propagate permissions to
   */
  public void cancelPropagatePermissions(String resource, String[] to);

  /**
   * List the resources to which the permissions of the given resource are propagated.
   *
   * @param resource the resource whose permissions are propagated
   * @param transitive include resources that are transitive
   *
   * @return the list of resources to which permissions are propagated
   */
  public String[] listPermissionPropagations(String resource, boolean transitive);

  /**
   * Checks the grants list for a match. All transitive grants via implied permissions and
   * propagated permissions are also checked.
   *
   * @param resource the resource uri to match.
   * @param permission the permission to match.
   * @param principal the principal to match or <code>null</code> to indicate the user making this
   *        call.
   *
   * @return Returns true if a matching grant is found in the grants list
   */
  public boolean isGranted(String resource, String permission, String principal);

  /**
   * Checks the revokes list for a match. All transitive revokes via implied permissions and
   * propagated permissions are also checked.
   *
   * @param resource the resource uri to match.
   * @param permission the permission to match.
   * @param principal the principal to match or <code>null</code> to indicate the user making this
   *        call.
   *
   * @return Returns true if a matching revoke is found in the grants list
   */
  public boolean isRevoked(String resource, String permission, String principal);
}
