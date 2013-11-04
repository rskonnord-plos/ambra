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
package org.ambraproject.service.cottagelabs;

/**
 * Methods to talk to the cottage labs API
 */
public interface CottageLabsLicenseService {

  /**
   * For a given DOI, query the cottage labs API and get the license if it is available
   *
   * @param doi the DOI to get the license for
   * @return the license response object
   * @throws Exception
   */
  public Object findLicense(String doi) throws Exception;
}
