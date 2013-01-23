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

package org.ambraproject.web;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that counts and exposes the number of active sessions in a servlet
 * container.
 */
public class SessionCounter implements HttpSessionListener {

  private static AtomicInteger sessionCount = new AtomicInteger();

  @Override
  public void sessionCreated(HttpSessionEvent arg0) {
    sessionCount.incrementAndGet();
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent arg0) {
    sessionCount.decrementAndGet();
  }

  /**
   * @return the current count of active sessions in this servlet container
   */
  public static int getSessionCount() {
    return sessionCount.get();
  }
}
