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
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Action that displays the stack traces of all running threads in this JVM.
 */
public class ThreadDumpAction extends InternalIpAction {

  @Override
  public String execute() throws Exception {
    if (!checkAccess()) {
      throw new IllegalAccessException();
    }
    PrintWriter writer = ServletActionContext.getResponse().getWriter();
    Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
    writeThreads(writer, threadMap);
    return null;
  }

  private void writeThreads(PrintWriter writer, Map<Thread, StackTraceElement[]> threadMap) {
    for (Map.Entry<Thread, StackTraceElement[]> entry : threadMap.entrySet()) {
      writer.println(String.format("%s (id %d)", entry.getKey().getName(),
          entry.getKey().getId()));
      for (StackTraceElement ste : entry.getValue()) {
        writer.println("  " + ste);
      }
      writer.println();
    }
  }
}
