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
package org.topazproject.otm.mapping.java;

import java.util.Collections;

import junit.framework.TestCase;

/**
 * Test for primitive null value setting.
 *
 * @author Pradeep Krishnan
  */
public class NullValueTest extends TestCase {
  public void testNull() {
    assertEquals(null, ScalarFieldBinder.nullValue(Boolean.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Character.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Byte.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Short.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Integer.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Long.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Float.class));
    assertEquals(null, ScalarFieldBinder.nullValue(Double.class));

    Primitives p = new Primitives();

    ScalarFieldBinder b = new ScalarFieldBinder(new Property(Primitives.class, "bool"), null, null);
    p.setBool(true);
    assertEquals(true, p.bool);
    b.set(p, Collections.emptyList());
    assertEquals(false, p.bool);

    b = new ScalarFieldBinder(new Property(Primitives.class, "c"), null, null);
    p.setC('a');
    assertEquals('a', p.c);
    b.set(p, Collections.emptyList());
    assertEquals('\u0000', p.c);

    b = new ScalarFieldBinder(new Property(Primitives.class, "f"), null, null);
    p.setF(42.0f);
    assertEquals(42.0f, p.f);
    b.set(p, Collections.emptyList());
    assertEquals(0.0f, p.f);

    b = new ScalarFieldBinder(new Property(Primitives.class, "d"), null, null);
    p.setD(42.0d);
    assertEquals(42.0d, p.d);
    b.set(p, Collections.emptyList());
    assertEquals(0.0d, p.d);

    b = new ScalarFieldBinder(new Property(Primitives.class, "b"), null, null);
    p.setB((byte) 42);
    assertEquals(42, p.b);
    b.set(p, Collections.emptyList());
    assertEquals(0, p.b);

    b = new ScalarFieldBinder(new Property(Primitives.class, "s"), null, null);
    p.setS((short) 42);
    assertEquals(42, p.s);
    b.set(p, Collections.emptyList());
    assertEquals(0, p.s);

    b = new ScalarFieldBinder(new Property(Primitives.class, "i"), null, null);
    p.setI(42);
    assertEquals(42, p.i);
    b.set(p, Collections.emptyList());
    assertEquals(0, p.i);

    b = new ScalarFieldBinder(new Property(Primitives.class, "l"), null, null);
    p.setL(42L);
    assertEquals(42L, p.l);
    b.set(p, Collections.emptyList());
    assertEquals(0L, p.l);
  }

  private static class Primitives {
    boolean bool;
    char    c;
    byte    b;
    short   s;
    int     i;
    long    l;
    float   f;
    double  d;

    public void setBool(boolean bool) {
      this.bool = bool;
    }

    public void setC(char c) {
      this.c = c;
    }

    public void setB(byte b) {
      this.b = b;
    }

    public void setS(short s) {
      this.s = s;
    }

    public void setI(int i) {
      this.i = i;
    }

    public void setL(long l) {
      this.l = l;
    }

    public void setF(float f) {
      this.f = f;
    }

    public void setD(double d) {
      this.d = d;
    }
  }
}
