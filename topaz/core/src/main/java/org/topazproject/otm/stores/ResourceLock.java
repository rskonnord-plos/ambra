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
package org.topazproject.otm.stores;

import java.util.HashMap;
import java.util.Map;

/**
 * A read-write lock where locks are owned by a resource rather than a thread.
 * eg. locks owned by a Connection or Transaction. This is similar in concept to
 * {@link java.util.concurrent.locks.ReadWriteLock}, but without tying lock ownership
 * to threads.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> The type of the owning resource
 */
public class ResourceLock<T>  {
  private Map<T, int[]> entries = new HashMap<T, int[]>();
  private T owner = null;
  private int sc = 0;
  private static final int RD = 0;
  private static final int WR = 1;

  /**
   * Acquire the read lock for the given resource. Read lock may be acquired as long as there
   * are no write-locks or the write-lock is owned by the same resource. When the lock cannot
   * be acquired, the calling thread will enter into a {@link #wait()} and will contest for
   * acquiring the lock when the condition that caused a wait is lifted.
   * <p>
   * Purpose of acquiring a read-lock is to lock-out the writers while the caller performs
   * a read operation.
   *
   * @param resource the resource requesting read ownership
   *
   * @throws InterruptedException on an interrupt
   */
  public synchronized void acquireRead(T resource) throws InterruptedException {
    while ((owner != null) && (owner != resource))
      wait();
    mark(resource, RD, 1);
  }

  /**
   * Release the read lock for the given resource and notifies all waiters.
   *
   * @param resource the resource for which the lock is to be released
   *
   * @see #readCount(Object)
   */
  public synchronized void releaseRead(T resource) {
    mark(resource, RD, -1);
    notifyAll();
  }

  /**
   * Acquire the write lock for the given resource. Write lock may only be acquired when
   * there are no lockers (read or write) or when the locker is the same resource.
   * When the lock cannot be acquired, the calling thread will enter into a {@link #wait()}
   * and will contest for acquiring the lock when the condition that caused a wait is lifted.
   * <p>
   * Purpose of acquiring a write-lock is to lock-out others while the caller performs updates.
   *
   * @param resource the resource requesting write ownership
   *
   * @throws InterruptedException on an interrupt
   */
  public synchronized void acquireWrite(T resource) throws InterruptedException {
    while ((owner != null) && (owner != resource) && (sc > readCount(resource)))
      wait();
    owner = resource;
    mark(resource, WR, 1);
  }

  /**
   * Release the write lock for the given resource and notifies all waiters when this
   * is the last thread to release the write lock for the resource.
   *
   * @param resource the resource for which the lock is to be released
   */
  public synchronized void releaseWrite(T resource) {
    if ((mark(resource, WR, -1) <= 0) && (owner == resource)) {
      owner = null;
      notifyAll();
    }
  }

  /**
   * Returns the number of read-locks held by this resource.
   *
   * @param resource the resource owning the locks
   *
   * @return the count of read-locks held by this resource
   */
  public synchronized int readCount(T resource) {
    return mark(resource, RD, 0);
  }

  /**
   * Returns the number of write-locks held by this resource.
   *
   * @param resource the resource owning the locks
   *
   * @return the count of write-locks held by this resource
   */
  public synchronized int writeCount(T resource) {
    return mark(resource, WR, 0);
  }

  private int mark(T resource, int idx, int inc) {
    int[] e = entries.get(resource);
    if (e == null)
      entries.put(resource, e = new int[] {0, 0});

    e[idx] += inc;

    if ((idx == RD) && (e[RD] >= 0))
      sc += inc;

    if ((e[RD] <= 0) && (e[WR] <= 0))
      entries.remove(resource);

    return e[idx];
  }
}
