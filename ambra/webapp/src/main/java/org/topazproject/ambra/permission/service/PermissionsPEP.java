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

import org.topazproject.ambra.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;

/**
 * The XACML PEP for the permission accounts manager.
 *
 * @author Pradeep Krishnan
 */
public class PermissionsPEP extends AbstractSimplePEP implements Permissions.ServicePermissions {
  /**
   * The list of all supported actions
   */
  protected static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   GRANT, REVOKE, CANCEL_GRANTS, CANCEL_REVOKES, LIST_GRANTS, LIST_REVOKES,
                   IMPLY_PERMISSIONS, CANCEL_IMPLY_PERMISSIONS, LIST_IMPLIED_PERMISSIONS,
                   PROPAGATE_PERMISSIONS, CANCEL_PROPAGATE_PERMISSIONS, LIST_PERMISSION_PROPAGATIONS
    };

  /**
   * The list of all supported obligations
   */
  protected static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null, null, null, null, null, null, null };

  static {
    init(PermissionsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  public PermissionsPEP(PDP pdp) {
    super(pdp);
  }
}
