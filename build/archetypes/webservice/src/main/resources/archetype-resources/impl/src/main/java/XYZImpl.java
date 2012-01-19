#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package ${package}.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.common.impl.TopazContext;
import ${package}.${Svc};

/** 
 * This provides the implementation of the ${service} service.
 * 
 * @author foo
 */
public class ${Svc}Impl implements ${Svc} {
  private static final Log log = LogFactory.getLog(${Svc}Impl.class);

  private final ${Svc}PEP pep;
  private final TopazContext ctx;

  /**
   * Create a new ${service} service instance.
   *
   * @param pep           the policy-enforcer to use for access-control
   * @param ctx           the topaz api call context
   */
  public ${Svc}Impl(${Svc}PEP pep, TopazContext ctx) {
    this.pep = pep;
    this.ctx = ctx;
  }
}
