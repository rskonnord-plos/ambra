/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.service.raptor;

import org.ambraproject.views.AcademicEditorView;

import java.io.IOException;
import java.util.List;

public interface RaptorService {
  /**
   * Get a list of academic editors from the raptor server
   *
   * @return a list of academic editors from the raptor server
   * @throws IOException
   */
  public List<AcademicEditorView> getAcademicEditor() throws IOException;
}
