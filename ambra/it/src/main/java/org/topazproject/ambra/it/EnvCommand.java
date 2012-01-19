/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
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
package org.topazproject.ambra.it;

/**
 * A command line wrapper for Env.groovy
 *
 * @author Pradeep Krishnan
 */
public class EnvCommand {
  /**
   * Execute the command
   *
   * @param args install-location, command
   *
   * @throws Exception on an error
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: EnvCommand <install-location> [start/stop/install/restore] " + 
          "[data-artifact (eg. org.topazproject.ambra:plosone-it-data:07)]");
      return;
    }
      
      StringBuilder buf = new StringBuilder();
      for (int i=0; i<args.length; i++) {
          buf.append(" ");
          buf.append(args[i]);
      }
    System.out.println("EnvCommand invoked with args:"+buf.toString());
    
    String envParam = (args.length > 0) ? args[0] : "install/basic"; 
    String cmd = (args.length > 1) ? args[1] : "start";
    String data = (args.length > 2) ? args[2] : null;
      // simple space seperated for now
    String cmdArgs[] = ((args.length > 3) && (args[3] != null)) ? args[3].split(" ") : new String[0];
      System.out.println("Invoking method '"+cmd+"' on Env.groovy with Env='"+
                         envParam + "' and data='" + data + "'");

    Env    env = new Env(envParam, data);
    env.invokeMethod(cmd, cmdArgs);

    if (cmd.equals("start")) {
      Object block = new Object();

      while (true) {
        synchronized (block) {
          block.wait();
        }
      }
    }
  }
}
