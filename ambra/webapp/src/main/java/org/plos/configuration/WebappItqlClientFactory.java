/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.plos.configuration;

import java.net.URI;
import java.net.URL;

import org.topazproject.mulgara.itql.DefaultItqlClientFactory;

/**
 * The webapp implementation {@link ItqlClientFactory ItqlClientFactory}.
 *
 * Provide webapp specific config for embedded, local:, Mulgara.
 *
 * @author Jeff Suttor
 */
public class WebappItqlClientFactory extends DefaultItqlClientFactory {
  private static final String DEF_WEB_CONF = "mulgara-web-config.xml";
  private static WebappItqlClientFactory singleton;

  /**
   * Because mulgara does not currently support multiple embedded instances, we must ensure
   * that only a single instance is used.
   *
   * @return the singleton instance
   */
  public static synchronized WebappItqlClientFactory getInstance() {
    if (singleton == null)
      singleton = new WebappItqlClientFactory();
    return singleton;
  }

  private WebappItqlClientFactory() {
    setDbDir(System.getProperty("ambra.topaz.tripleStore.mulgara.dbDir"));
  }

  @Override
  public URL getLocalConf(URI uri) {
    return WebappItqlClientFactory.class.getResource(DEF_WEB_CONF);
  }
}
