/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import com.opensymphony.xwork.ActionSupport;
import static org.plos.Constants.SelectValues;

import java.util.Map;

/**
 * A provider of all listings for select boxes in an html page.
 */
public class ListingAction extends ActionSupport {

  public String execute() throws Exception {
    return SUCCESS;
  }

  /** return a map of all Organization Types */
  public Map<String, String> getAllOrganizationTypes() {
    return SelectValues.getAllOrganizationTypes();
  }

  /** return a map of all titles */
  public Map<String, String> getAllTitles() {
    return SelectValues.getAllTitles();
  }

  /** return a map of all position types */
  public Map<String, String> getAllPositionTypes() {
    return SelectValues.getAllPositionTypes();
  }

  /** return a map of all url descriptions */
  public Map<String, String> getAllUrlDescriptions() {
    return SelectValues.getAllUrlDescriptions();
  }
}
