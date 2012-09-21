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

package org.ambraproject.util;

import freemarker.template.TemplateException;

import java.util.Map;

/**
 * Freemarker template that renders a script tag pointing at a javascript file.  In order to facilitate correct browser
 * caching, a fake parameter is appended to the end of the link with a checksum of the file's contents.  In this way,
 * when the file changes, the browser should request a new copy.
 * <p/>
 * See the superclass javadoc for further usage and implementation notes.
 */
public class VersionedJSDirective extends VersionedFileDirective {

  @Override
  public String getLink(String filename, String fingerprint, Map params) throws TemplateException {
    return String.format("<script type=\"text/javascript\" src=\"%s?v=%s\"></script>\n", filename, fingerprint);
  }
}
