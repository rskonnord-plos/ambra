/* 
 * $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedoragsearch.service;

import java.net.URL;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;

/**
 * Utility to manually update fedoragsearch index.
 *
 * Usage:
 *   java org.topazproject.fedoragsearch.UpdateIndex [Service URL] action [value] [repo]
 *
 * 
 *
 * @author Eric Brown
 */
public class UpdateIndex {
  protected static final String FGS_SVC_URL =
    "http://localhost:9090/fedoragsearch/services/FgsOperations";
  protected static final String USAGE =
    "java org.topazproject.fedoragsearch.UpdateIndex [Service URL] action [value]\n" +
    "  where action = createEmpty|fromFoxmlFiles|fromPid|deletePid";
  
  public static void main(String[] args) {
    // No args, display usage
    if (args.length == 0) {
      System.out.println(USAGE);
      return;
    }

    // See if first param is URL
    int argn = 0;
    String url = FGS_SVC_URL;
    if (args[0].startsWith("h") || args[0].startsWith("H"))
      url = args[argn++];

    // No action, display Usage
    if (args.length <= argn) {
      System.out.println(USAGE);
      return;
    }
    
    String action = args[argn++];
    String value = null;
    String repositoryName = "Topaz";
    
    if (args.length > argn)
      value = args[argn++];
    if (args.length > argn)
      repositoryName = args[argn++];
    
    try {
      // Create fedoragsearch service
      FgsOperations fgs = new FgsOperationsServiceLocator().getOperations(new URL(url));
      String result = fgs.updateIndex(action, value, repositoryName, null, null, null);
      System.out.println(result);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

}
