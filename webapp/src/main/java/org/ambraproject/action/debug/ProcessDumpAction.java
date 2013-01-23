/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.debug;

import org.ambraproject.action.InternalIpAction;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;

import java.io.PrintWriter;

/**
 * Action that basically just renders the output of "ps uaxwww".
 */
public class ProcessDumpAction extends InternalIpAction {

  @Override
  public String execute() throws Exception {
    if (!checkAccess()) {
      throw new IllegalAccessException();
    }
    PrintWriter writer = ServletActionContext.getResponse().getWriter();
    String dump = getProcessDump();
    for (String s : dump.split("\n")) {
      writer.println(s);
    }
    return null;
  }

  private String getProcessDump() throws Exception {
    Process process = null;
    String result;
    try {
      process = Runtime.getRuntime().exec("ps uaxwww");
      result = IOUtils.toString(process.getInputStream());
      process.waitFor();
    } finally {
      process.destroy();
    }
    return result;
  }
}
