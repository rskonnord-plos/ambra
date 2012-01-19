/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class to generate citations to client by making the resulting file an attachment and supplying the
 * correct file extension specified in xwork.xml
 * 
 * 
 * @author stevec
 *
 */
public class CitationResult extends FreemarkerResult  {

  private static final Log log = LogFactory.getLog(CitationResult.class);
  private String fileExtension;

  protected boolean preTemplateProcess(freemarker.template.Template template,
      freemarker.template.TemplateModel model) throws IOException{

    String doi = (String)invocation.getStack().findValue("citation.DOI");
    HttpServletResponse response = ServletActionContext.getResponse();
    try {
      response.addHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(doi, "UTF-8") + fileExtension);
    } catch (UnsupportedEncodingException uee) {
      response.addHeader("Content-disposition", "attachment; filename=citation" + fileExtension);
    }
    return super.preTemplateProcess(template, model);
  }

  /**
   * @param fileExtension The fileExtension to set.
   */
  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }
}
