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

import java.util.HashSet;
import java.util.Set;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * An embedding sample.
 *
 * @author Pradeep Krishnan
  */
@UriPrefix(Rdf.topaz)
public class SampleEmbeddable {
  private String foo;
  private String bar;
  private Set<Annotation> set = new HashSet<Annotation>();

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

  /**
   * Get set.
   *
   * @return annotation set.
   */
  public Set<Annotation> getSet() {
    return set;
  }

  /**
   * Set set.
   *
   * @param set the value to set.
   */
  @Predicate
  public void setSet(Set<Annotation> set) {
    this.set = set;
  }
}
