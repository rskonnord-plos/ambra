/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora;

import java.util.Map;

import fedora.server.Server;

import fedora.server.errors.ModuleInitializationException;
import fedora.server.errors.ResourceIndexException;

import fedora.server.resourceIndex.ResourceIndexModule;

import fedora.server.storage.types.DigitalObject;

/**
 * Replacement for fedora's resource indexer. Currently forces a commit after every update to the
 * resource index since the delayed writes are not sufficient when the triple store is shared.
 *
 * @author Pradeep Krishnan
 */
public class ResourceIndexer extends ResourceIndexModule {
  /**
   * Creates a new ResourceIndexer object.
   *
   * @param moduleParameters parameter list from config file
   * @param server The fidora server instance
   * @param role Role of this module
   *
   * @throws ModuleInitializationException on an error
   */
  public ResourceIndexer(Map moduleParameters, Server server, String role)
                  throws ModuleInitializationException {
    super(moduleParameters, server, role);
  }

  /**
   * @see fedora.server.resourceIndex.ResourceIndex#addDigitalObject.
   */
  public void addDigitalObject(DigitalObject digitalObject)
                        throws ResourceIndexException {
    //super.addDigitalObject(digitalObject);
    commit();
  }

  /**
   * @see fedora.server.resourceIndex.ResourceIndex#modifyDigitalObject.
   */
  public void modifyDigitalObject(DigitalObject digitalObject)
                           throws ResourceIndexException {
    //super.modifyDigitalObject(digitalObject);
    commit();
  }

  /**
   * @see fedora.server.resourceIndex.ResourceIndex#deleteDigitalObject.
   */
  public void deleteDigitalObject(DigitalObject digitalObject)
                           throws ResourceIndexException {
    //super.deleteDigitalObject(digitalObject);
    commit();
  }
}
