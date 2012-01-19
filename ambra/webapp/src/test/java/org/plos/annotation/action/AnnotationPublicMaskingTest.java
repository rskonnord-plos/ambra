/* $HeadURL::                                                                            $
 * $Id:AnnotationPublicMaskingTest.java 722 2006-10-02 16:42:45Z viru $
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
package org.plos.annotation.action;

import junit.framework.TestCase;
import static org.plos.annotation.service.WebAnnotation.FLAG_MASK;
import static org.plos.annotation.service.WebAnnotation.PUBLIC_MASK;
import static org.plos.annotation.service.WebAnnotation.DELETE_MASK;

public class AnnotationPublicMaskingTest extends TestCase {
  public void testMasksAreDifferent() {
    final int[] masks = new int[]{PUBLIC_MASK, FLAG_MASK, DELETE_MASK};
    for (int i = 0; i < masks.length; i++) {
      final int rootMask = masks[i];
      for (int j = i + 1; j < masks.length; j++) {
        assertTrue(rootMask != masks[j]);
      }
    }
  }

  public void testShouldFindBitIntegerPublic() {
    assertEquals(PUBLIC_MASK, PUBLIC_MASK & 0x001);
    assertEquals(PUBLIC_MASK, PUBLIC_MASK & 0x07);
    assertEquals(PUBLIC_MASK, PUBLIC_MASK & 0x06003);
    assertEquals(PUBLIC_MASK, PUBLIC_MASK & 0x00d5);
  }
  
  public void testShouldFindBitIntegerNonPublic() {
    assertEquals(0, PUBLIC_MASK & 0x000);
    assertEquals(0, PUBLIC_MASK & 0x10);
    assertEquals(0, PUBLIC_MASK & 0x0b010);
    assertEquals(0, PUBLIC_MASK & 0x0996);
  }

  public void testShouldFindBitIntegerFlagged() {
    assertEquals(FLAG_MASK, FLAG_MASK & 0x002);
    assertEquals(FLAG_MASK, FLAG_MASK & 0x07);
    assertEquals(FLAG_MASK, FLAG_MASK & 0x06003);
    assertEquals(FLAG_MASK, FLAG_MASK & 0x00d6);
  }

  public void testShouldFindBitIntegerNonFlagged() {
    assertEquals(0, FLAG_MASK & 0x000);
    assertEquals(0, FLAG_MASK & 0x10);
    assertEquals(0, FLAG_MASK & 0x0b010);
    assertEquals(0, FLAG_MASK & 0x0999);
  }

  public void testShouldFindBitIntegerDeleted() {
    assertEquals(DELETE_MASK, DELETE_MASK & 0x004);
    assertEquals(DELETE_MASK, DELETE_MASK & 0x07);
    assertEquals(DELETE_MASK, DELETE_MASK & 0x06005);
    assertEquals(DELETE_MASK, DELETE_MASK & 0x00d6);
  }

  public void testShouldFindBitIntegerNonDeleted() {
    assertEquals(0, DELETE_MASK & 0x000);
    assertEquals(0, DELETE_MASK & 0x10);
    assertEquals(0, DELETE_MASK & 0x0b010);
    assertEquals(0, DELETE_MASK & 0x0999);
  }

}
