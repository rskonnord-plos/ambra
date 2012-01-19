/* $HeadURL$
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

package org.topazproject.util.functional;

/**
 * Utility functions for currying functors with N arguments down to functors with N-1 arguments
 * These methods remove the LAST parameter.
 *
 * @author Paul Gearon
 */
public class F {
  static final <T1,R> Fn<R> curry(final Fn1<T1,R> fna, final T1 arg) {
    return new Fn<R>() { public R fn() { return fna.fn(arg); } };
  }

  static final <T1,T2,R> Fn1<T1,R> curry(final Fn2<T1,T2,R> fna, final T2 arg) {
    return new Fn1<T1,R>() { public R fn(T1 a) { return fna.fn(a, arg); } };
  }

  static final <T1,R,E extends Exception> FnE<R,E> curry(final Fn1E<T1,R,E> fna, final T1 arg) {
    return new FnE<R,E>() { public R fn() throws E { return fna.fn(arg); } };
  }

  static final <T1,T2,R,E extends Exception> Fn1E<T1,R,E> curry(final Fn2E<T1,T2,R,E> fna,
                                                                final T2 arg) {
    return new Fn1E<T1,R,E>() { public R fn(T1 a) throws E { return fna.fn(a, arg); } };
  }
}
