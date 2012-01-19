/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.webwork;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.webwork.views.freemarker.FreemarkerManager;
import com.opensymphony.webwork.views.freemarker.ScopesHashModel;

import com.opensymphony.xwork.util.OgnlValueStack;


/**
 * Custom Freemarker Manager to load up the configuration files for css, javascript, and titles of pages
 * 
 * @author Stephen Cheng
 *
 */
public class PlosOneFreemarkerManager extends FreemarkerManager {

    private PlosOneFreemarkerConfig fmConfig;
    
    /**
     * Sets the custom configuration object via Spring injection
     * 
     * @param fmConfig
     */
    public PlosOneFreemarkerManager(PlosOneFreemarkerConfig fmConfig) {
      this.fmConfig = fmConfig;
    }
    
    
    /**
     * Subclass from parent to add the freemarker configuratio object globally
     * 
     * @see com.opensymphony.webwork.views.freemarker.FreemarkerManager
     */
    public void populateContext(ScopesHashModel model, OgnlValueStack stack, Object action, 
                                HttpServletRequest request, HttpServletResponse response) {
      super.populateContext(model, stack, action, request, response);
      model.put("freemarker_config", fmConfig);
    }
}
