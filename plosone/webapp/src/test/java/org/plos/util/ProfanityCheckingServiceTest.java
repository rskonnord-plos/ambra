/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

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
