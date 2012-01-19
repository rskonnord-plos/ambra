/* $HeadURL::                                                                            $
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
package org.topazproject.ambra.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

import org.topazproject.ambra.util.ProfanityCheckingService;

public class ProfanityCheckingServiceTest extends TestCase {
  public void testShouldCatchProfaneText() {
    final ProfanityCheckingService service = new ProfanityCheckingService();
    final Collection<String> profaneWordList = new ArrayList<String>();
    profaneWordList.add("ASS");
    profaneWordList.add("bush");
    service.setProfaneWords(profaneWordList);
    found("ass", service);
    found(" ass", service);
    found("  ass", service);
    found("  \nass", service);
    found("aSS", service);
    found("am ass", service);
    found(".ass", service);
    found(" some ass", service);
    found("+ass", service);
    found("-ass", service);
    found(" ass", service);
    found(" some before Ass and some after", service);
    found(" some /n before and some after/n before Ass and some after", service);
    found(" (Ass ", service);
    found("[Ass]", service);
    found("[Ass] and bush", 2, service);
  }

  public void testShouldAllowTextWhichIsNotProfane() {
    final ProfanityCheckingService service = new ProfanityCheckingService();
    final Collection<String> profaneWordList = new ArrayList<String>();
    profaneWordList.add("BUSH");
    service.setProfaneWords(profaneWordList);
    notFound("ambush", service);
    notFound(" some ambush", service);
    notFound(" amBush ", service);
    notFound(" some before amBush and some after", service);
    notFound(" some /n before some before some /n before amBush and some after /n adter", service);
  }

  private void notFound(final String content, final ProfanityCheckingService service) {
    assertEquals("["+content+"]", 0, service.validate(content).size());
  }

  private void found(final String content, final ProfanityCheckingService service) {
    found(content, 1, service);
  }

  private void found(final String content, final int total, final ProfanityCheckingService service) {
    assertEquals("["+content+"]", total, service.validate(content).size());
  }

}
