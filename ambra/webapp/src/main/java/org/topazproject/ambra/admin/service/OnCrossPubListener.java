/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

package org.topazproject.ambra.admin.service;

/**
 * All classes that want to be notified when article is cross published (or cross un-published)
 * should implement this interface and be register in AdminService.
 *
 * @author Dragisa Krsmanovic
 */
public interface OnCrossPubListener {
  
  /**
   * After article has been cross published or un-published all registered listeners are invoked
   * through this method.
   *
   * @param articleId ID of the cross published article
   * @throws Exception if operation failed. Will cause cross publish operation to fail.
   */
  public void articleCrossPublished(String articleId) throws Exception;
  
}