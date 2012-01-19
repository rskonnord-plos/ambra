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
package org.topazproject.otm.samples;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

@UriPrefix(Rdf.topaz)
@Entity(graph = "ri")
public class NoRdfType {
  private String id;
  private String foo = "foo";
  private String bar = "bar";

  /**
   * Creates a new NoRdfType object.
   */
  public NoRdfType() {
  }

  /**
   * Creates a new NoRdfType object.
   *
   * @param id the id
   */
  public NoRdfType(String id) {
    this.id = id;
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  @Id
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get foo.
   *
   * @return foo as String.
   */
  public String getFoo() {
    return foo;
  }

  /**
   * Set foo.
   *
   * @param foo the value to set.
   */
  @Predicate
  public void setFoo(String foo) {
    this.foo = foo;
  }

  /**
   * Get bar.
   *
   * @return bar as String.
   */
  public String getBar() {
    return bar;
  }

  /**
   * Set bar.
   *
   * @param bar the value to set.
   */
  @Predicate
  public void setBar(String bar) {
    this.bar = bar;
  }
}
