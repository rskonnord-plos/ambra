/* $HeadURL::                                                                            $
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

package org.topazproject.ambra.article.service;

import java.io.Serializable;

/**
 * CollaborativeAuthor - Simple class to represent the collaborative author(s) for citation
 * purposes.
 * 
 * @author jkirton
 */
public class CollaborativeAuthor implements Serializable {

  /**
   * The collective single name given for the collaborative author(s). Ref:
   * {@link http://dtd.nlm.nih.gov/publishing/tag-library/2.0/n-x630.html}
   */
  private String nameRef;

  /**
   * @return the nameRef
   */
  public String getNameRef() {
    return nameRef;
  }

  /**
   * @param nameRef the nameRef to set
   */
  public void setNameRef(String nameRef) {
    this.nameRef = nameRef;
  }
}
