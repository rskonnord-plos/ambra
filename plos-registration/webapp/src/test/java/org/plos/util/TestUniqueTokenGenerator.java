/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.util;

import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;

/**
 *
 */
public class TestUniqueTokenGenerator extends TestCase {
  public void testShouldGenerateUniqueTokens() {
    final int loopCount = 1000;
    final Set<String> set = new HashSet<String>(loopCount);
    for (int i = 0; i < loopCount; i++) {
      set.add(TokenGenerator.getUniqueToken());
    }
    
    assertEquals(loopCount, set.size());
  }
}
