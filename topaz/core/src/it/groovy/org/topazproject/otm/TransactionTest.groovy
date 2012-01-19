/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import org.topazproject.otm.samples.Article;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for transactions.
 *
 * <p>Note that some of these are somewhat dependent on Mulgara's behaviour regarding the allowed
 * concurrency of transactions.
 */
public class TransactionTest extends AbstractTest {
  private static final Log log = LogFactory.getLog(TransactionTest.class)

  void setUp() {
    super.setUp()

    rdf.sessFactory.preload(Article.class)
  }

  void testCommit() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    // write object
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.saveOrUpdate(o1)
    s.transaction.commit()

    // read in same session
    s.clear()
    s.beginTransaction()
    def res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)
    s.transaction.commit()

    // read in different session
    s = rdf.sessFactory.openSession()
    s.beginTransaction()
    res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)
    s.transaction.commit()
  }

  void testRollback() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    // write object
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.saveOrUpdate(o1)
    s.transaction.rollback()

    // read null in same session
    s.clear()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.transaction.rollback()

    // read null in different session
    s = rdf.sessFactory.openSession()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.transaction.rollback()
  }

  void testRollbackOnly() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    // write and roll back
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.saveOrUpdate(o1)
    s.transaction.setRollbackOnly()
    assertTrue(s.transaction.isRollbackOnly())
    s.transaction.rollback()

    // read null, write and commit-fail
    s.clear()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.saveOrUpdate(o1)
    s.transaction.setRollbackOnly()
    assertTrue(s.transaction.isRollbackOnly())
    shouldFail(OtmException.class) { s.transaction.commit() }

    // read null
    s.clear()
    s.beginTransaction()
    assertNull(s.get(Article.class, o1.uri.toString()))
    assertFalse(s.transaction.isRollbackOnly())
    s.transaction.rollback()
  }

  void testActive() {
    // tx is null
    Session s = rdf.sessFactory.openSession()
    assertNull(s.transaction)

    // tx not null after start
    s.beginTransaction()
    assertNotNull(s.transaction)

    // tx already active
    shouldFail(OtmException) { s.beginTransaction() }

    // tx null after commit
    s.transaction.commit()
    assertNull(s.transaction)

    // tx null after rollback
    s.beginTransaction()
    assertNotNull(s.transaction)
    s.transaction.rollback()
    assertNull(s.transaction)

    // tx null after failed commit
    s.beginTransaction()
    assertNotNull(s.transaction)
    s.transaction.setRollbackOnly()
    shouldFail(OtmException) { s.transaction.commit() }
    assertNull(s.transaction)

    // tx null after rollback-only rollback
    s.beginTransaction()
    assertNotNull(s.transaction)
    s.transaction.setRollbackOnly()
    s.transaction.rollback()
    assertNull(s.transaction)
  }

  void testTimeout() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    // explicit timeout, commit within the timeout
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction(false, 10)
    s.saveOrUpdate(o1)
    s.transaction.commit()

    // explicit timeout, wait for timeout to trigger, verify rollback
    s.clear()
    s.beginTransaction(false, 2)
    def res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    res.title = "The end of things"

    Thread.sleep(2000);

    assertTrue(s.transaction.isRollbackOnly())
    shouldFail(OtmException.class) { s.transaction.commit() }

    // no timeout, make sure it goes back to default
    s.clear()
    s.beginTransaction(false, -1)
    res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    res.title = "The end of things"

    Thread.sleep(2000);

    assertFalse(s.transaction.isRollbackOnly())
    s.transaction.commit()
  }

  /* This assumes the underlying store (e.g. Mulgara) supports/implements read-only txn's */
  void testReadOnly() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    // write object
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction(false, -1)
    assertNull(s.get(Article.class, o1.uri.toString()))
    s.saveOrUpdate(o1)
    s.transaction.commit()

    // read, set-ro, modify, commit-fail
    s = rdf.sessFactory.openSession()
    s.beginTransaction(true, -1)
    def res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)
    res.title = "The end of things"
    shouldFail(OtmException) { s.transaction.commit() }

    // read, set-ro, modify, flush-fail, rollback
    s = rdf.sessFactory.openSession()
    s.beginTransaction(true, -1)
    res = s.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)
    res.title = "The end of things"
    shouldFail(OtmException) { s.flush() }
    s.transaction.rollback()
  }

  /* This assumes the underlying store (e.g. Mulgara) supports overlapping readers and writers */
  void testOverlappingReaderWriter() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    /* There is some trickyness in the following: because JTA TM's associate each global
     * transaction with the current thread, we must use separate threads in order to run
     * independent overlapping transactions.
     */

    // read null
    Session sr = rdf.sessFactory.openSession()
    sr.beginTransaction(true, -1)
    assertNull(sr.get(Article.class, o1.uri.toString()))
    sr.transaction.commit()

    // write object, keep tx open
    def sem = false
    def lck = "lock"

    def tw = doInThread {
      Session sw = rdf.sessFactory.openSession()
      sw.beginTransaction(false, -1)
      assertNull(sw.get(Article.class, o1.uri.toString()))
      sw.saveOrUpdate(o1)
      sw.flush()

      synchronized (lck) {
        sem = true
        lck.notify()

        while (sem)
          lck.wait()
      }

      sw.transaction.commit()
    }

    synchronized (lck) {
      while (!sem)
        lck.wait()
    }

    // read null
    sr.beginTransaction(true, -1)
    assertNull(sr.get(Article.class, o1.uri.toString()))
    sr.transaction.commit()

    // read null, keep tx open
    sr.beginTransaction(true, -1)
    def x = sr.get(Article.class, o1.uri.toString())
    assertNull(sr.get(Article.class, o1.uri.toString()))

    // commit write
    synchronized (lck) { sem = false; lck.notify() }
    tw.join()

    // read still null, close tx
    assertNull(sr.get(Article.class, o1.uri.toString()))
    sr.transaction.commit()

    // read non-null
    sr.beginTransaction(true, -1)
    def res = sr.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)
    sr.transaction.commit()
  }

  /* This assumes the underlying store (e.g. Mulgara) supports overlapping readers */
  void testOverlappingReaders() {
    def o1 = new Article(uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things")

    /* There is some trickyness in the following: because JTA TM's associate each global
     * transaction with the current thread, we must use separate threads in order to run
     * independent overlapping transactions.
     */

    // write object
    Session sw = rdf.sessFactory.openSession()
    sw.beginTransaction(false, -1)
    assertNull(sw.get(Article.class, o1.uri.toString()))
    sw.saveOrUpdate(o1)
    sw.transaction.commit()

    // read object, keep tx open
    Session sr = rdf.sessFactory.openSession()
    sr.beginTransaction(true, -1)
    def res = sr.get(Article.class, o1.uri.toString())
    assertEquals(o1.uri, res.uri)
    assertEquals(o1.title, res.title)
    assertNull(res.articleDate)

    // do second read
    def tr = doInThread {
      Session r2 = rdf.sessFactory.openSession()
      r2.beginTransaction(true, -1)
      def res2 = sr.get(Article.class, o1.uri.toString())
      assertEquals(o1.uri, res2.uri)
      assertEquals(o1.title, res2.title)
      assertNull(res2.articleDate)
      r2.transaction.commit()
    }
    tr.join()

    // do third read, keeping tx open
    def sem = false
    def lck = "lock"

    tr = doInThread {
      Session r2 = rdf.sessFactory.openSession()
      r2.beginTransaction(true, -1)
      def res2 = sr.get(Article.class, o1.uri.toString())
      assertEquals(o1.uri, res2.uri)
      assertEquals(o1.title, res2.title)
      assertNull(res2.articleDate)

      synchronized (lck) {
        sem = true
        lck.notify()

        while (sem)
          lck.wait()
      }

      r2.transaction.commit()
    }

    synchronized (lck) {
      while (!sem)
        lck.wait()
    }

    // commit first read-tx
    sr.transaction.commit()

    // commit third read
    synchronized (lck) { sem = false; lck.notify() }
    tr.join()
  }

  /* This assumes the underlying store (e.g. Mulgara) supports concurrent readers and writers */
  void testMultiReaderSingleWriter() {
    log.info "Starting MRSW stress test... (this may take a little time)"
    def iter = 100

    // write initial object
    Class cls = rdf.class('TestMRSW') {
      str  ()
      num  (type:'xsd:int')
      date (type:'xsd:date')
    }

    def o1 = cls.newInstance(str:'Zero', num:0, date:new Date())
    doInTx { s ->
      s.saveOrUpdate(o1)
    }

    // start writers
    def writer = { val ->
      Session s = rdf.sessFactory.openSession()
      for (int idx in 1 ..iter) {
        s.clear()
        s.beginTransaction(false, -1)

        def obj = s.get(cls, o1.id.toString())

        obj.num = val
        s.flush()

        obj.str = val ? 'One' : 'Zero'
        s.flush()

        obj.date = new Date()
        s.transaction.commit()
      }
      s.close()
    }

    def writers = [ doInThread(writer.curry(0)), doInThread(writer.curry(1)) ]

    // start readers
    boolean end = false

    def reader = { changed, idx ->
      def prevObj = doInTx { s -> return s.get(cls, o1.id.toString()) }

      Session s = rdf.sessFactory.openSession()
      while (!end) {
        s.clear()
        s.beginTransaction(true, -1)

        def obj = s.get(cls, o1.id.toString())

        assertEquals("Inconsistent object read: num=${obj.num}, str=${obj.str}",
                     obj.num ? 'One' : 'Zero', obj.str)
        assertTrue("Timestamp went backwards: prev=${prevObj.date}, cur=${obj.date}",
                   obj.date >= prevObj.date)

        if (obj.date > prevObj.date || obj.num != prevObj.num) {
          synchronized (changed) { changed[idx]++ }
        }

        prevObj = obj

        s.transaction.commit()

        synchronized (o1) { }
      }
      s.close()
    }

    def changed = [0, 0, 0]
    def readers = [
      doInThread(reader.curry(changed, 0)), doInThread(reader.curry(changed, 1)),
      doInThread(reader.curry(changed, 2))
    ]

    // wait for writers to finish, and then end readers
    writers.each { it.join() }

    synchronized (o1) { end = true }
    readers.each { it.join() }

    // ensure we had some valid tests
    synchronized (changed) { }
    for (idx in 0..<changed.size)
      assertTrue("Not enough value transitions in reader ${idx}: ${changed[idx]}",
                 changed[idx] > (iter/10))

    log.info "MRSW stress test completed"
  }

  private Thread doInThread(Closure c) {
     Thread t = new Thread(c as Runnable)
     // FIXME: we should signal the failure to the test so the test can fail properly
     t.setUncaughtExceptionHandler(
         { thr, exc -> exc.printStackTrace(); System.exit(1) } as Thread.UncaughtExceptionHandler)
     t.start()
     return t
  }
}
