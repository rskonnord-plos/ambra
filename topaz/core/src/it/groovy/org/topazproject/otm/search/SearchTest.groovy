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

package org.topazproject.otm.search;

import org.topazproject.otm.AbstractTest;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Query;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Searchable;
import org.topazproject.otm.query.ResultChecker;
import org.topazproject.otm.query.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for Search.
 */
public class SearchTest extends AbstractTest {
  private static final Log log = LogFactory.getLog(SearchTest.class);

  void setUp() {
    graphs = [['ri',     'otmtest1', null],
              ['prefix', 'prefix',   'http://mulgara.org/mulgara#PrefixGraph'.toURI()],
              ['xsd',    'xsd',      'http://mulgara.org/mulgara#XMLSchemaModel'.toURI()],
              ['lucene', 'lucene',   'http://mulgara.org/mulgara#LuceneModel'.toURI()],
              ['str',    'str',      'http://topazproject.org/graphs#StringCompare'.toURI()]];
    super.setUp();

    rdf.sessFactory.preload(SearchTest1.class);
    rdf.sessFactory.preload(SearchTest2.class);
    rdf.sessFactory.preload(SearchTestSub.class);
    rdf.sessFactory.preload(SearchTestSub2.class);
    rdf.sessFactory.preload(SearchTestEmb.class);
    rdf.sessFactory.preload(SearchTestPP.class);
    rdf.sessFactory.validate()

    assert shouldFail(OtmException, {
      rdf.sessFactory.preload(SearchTestF1.class)
      rdf.sessFactory.validate()
    }).contains("can't be applied to a complex type")
  }

  void testBasic() {
    byte[] body = 'This should really be a very long blurb, but I\'m too lazy to type that...'.
                  getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTest1(id: 'foo:test1'.toURI()))
    }

    doInTx { s ->
      s.saveOrUpdate(new SearchTest1(id: 'foo:test1'.toURI(), text: 'A bottle of Rum', body: body))
    }

    doInTx { s ->
      // search on rdf prop with deref in arg
      Results r = s.createQuery("select s from SearchTest1 s where search(s.text, 'bottle');").
                    execute()
      checker.verify(r) {
        row { object (class:SearchTest1.class, id:'foo:test1'.toURI()) }
      }

      // search on rdf prop with deref in alias
      r = s.createQuery("select s from SearchTest1 s where a := s.text and search(a, 'bottle');").
            execute()
      checker.verify(r) {
        row { object (class:SearchTest1.class, id:'foo:test1'.toURI()) }
      }

      // search on blob prop with deref in arg
      r = s.createQuery("select s from SearchTest1 s where search(s.body, 'blurb');").execute()
      checker.verify(r) {
        row { object (class:SearchTest1.class, id:'foo:test1'.toURI()) }
      }

      // search on blob prop with deref in arg
      assert shouldFail(OtmException, {
        s.createQuery("select s from SearchTest1 s where a := s.body and search(a, 'blurb');").
          execute()
      })
    }

    doInTx { s ->
      s.delete(new SearchTest1(id: 'foo:test1'.toURI()))
    }
  }

  void testSubClass() {
    byte[] body = 'This should really be a very long blurb, but I\'m too lazy to type that...'.
                  getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTestSub(id: 'foo:testsub'.toURI()))
      s.delete(new SearchTestSub2(id: 'foo:testsub2'.toURI()))
    }

    doInTx { s ->
      s.saveOrUpdate(new SearchTestSub(id: 'foo:testsub'.toURI(), text: 'A bottle of Rum',
                                       body: body))
      s.saveOrUpdate(new SearchTestSub2(id: 'foo:testsub2'.toURI(), text: 'A bottle of Tequila',
                                        body: body))
    }

    doInTx { s ->
      // basic search
      Results r = s.createQuery("select s from SearchTestSub s where search(s.text, 'bottle');").
                    execute()
      checker.verify(r) {
        row { object (class:SearchTestSub.class, id:'foo:testsub'.toURI()) }
      }

      r = s.createQuery("select s from SearchTestSub s where search(s.body, 'blurb');").execute()
      checker.verify(r) {
        row { object (class:SearchTestSub.class, id:'foo:testsub'.toURI()) }
      }

      r = s.createQuery("select s from SearchTestSub2 s where search(s.text, 'tequila');").execute()
      checker.verify(r) {
        row { object (class:SearchTestSub2.class, id:'foo:testsub2'.toURI()) }
      }

      r = s.createQuery("select s from SearchTestSub2 s where search(s.body, 'lazy');").execute()
      checker.verify(r) {
        row { object (class:SearchTestSub2.class, id:'foo:testsub2'.toURI()) }
      }

      // ensure it was properly overriden
      /* enable this when OQL supports specifying graphs explicitly
      r = s.createQuery("select s from SearchTestSub s where search(s.<topaz:textsub>, 'bottle');").
            execute()
      checker.verify(r) {
        row { object (class:SearchTestSub.class, id:'foo:testsub'.toURI()) }
      }

      r = s.createQuery("select s from SearchTestSub s where search(s.<topaz:text>, 'bottle');").
            execute()
      checker.verify(r) {
      }
      */
    }

    doInTx { s ->
      s.delete(new SearchTestSub(id: 'foo:testsub'.toURI()))
      s.delete(new SearchTestSub2(id: 'foo:testsub2'.toURI()))
    }
  }

  void testEmbedded() {
    byte[] body = 'This should really be a very long blurb, but I\'m too lazy to type that...'.
                  getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTestEmb(s1: new SearchTest1(id: 'foo:testemb'.toURI())))
    }

    doInTx { s ->
      s.saveOrUpdate(new SearchTestEmb(
             s1: new SearchTest1(id: 'foo:testemb'.toURI(), text: 'A bottle of Rum', body: body)))
    }

    doInTx { s ->
      // search on rdf prop with deref in arg
      Results r = s.createQuery("select s from SearchTestEmb s where search(s.s1.text, 'bottle');").
                    execute()
      checker.verify(r) {
        row { object (class:SearchTestEmb.class, 's1.id':'foo:testemb'.toURI()) }
      }

      // search on rdf prop with deref in alias
      r = s.createQuery(
            "select s from SearchTestEmb s where a := s.s1.text and search(a, 'bottle');").
            execute()
      checker.verify(r) {
        row { object (class:SearchTestEmb.class, 's1.id':'foo:testemb'.toURI()) }
      }

      // search on blob prop with deref in arg
      r = s.createQuery("select s from SearchTestEmb s where search(s.s1.body, 'blurb');").execute()
      checker.verify(r) {
        row { object (class:SearchTestEmb.class, 's1.id':'foo:testemb'.toURI()) }
      }

      // search on blob prop with deref in arg
      assert shouldFail(OtmException, {
        s.createQuery("select s from SearchTestEmb s where a := s.s1.body and search(a, 'blurb');").
          execute()
      })
    }

    doInTx { s ->
      s.delete(new SearchTestEmb(s1: new SearchTest1(id: 'foo:testemb'.toURI())))
    }
  }

  void testTwoStep() {
    byte[] body1  = '''Hence the Phrygians, that primeval race, call me Pessinuntica, the Mother of
                       the Gods'''.getBytes("UTF-8")
    byte[] body2  = '''But those who are illumined by the earliest rays of that divinity, the Sun,
                       when he rises, the Aethopians, the Arii, and the Egyptians, so skilled in
                       ancient learning, worshipping me with ceremonies quite appropriate, call me
                       by my true name, Queen Isis.'''.getBytes("UTF-8")
    byte[] body11 = '''I, who am Nature, the parent of all things, the mistress of all the elements,
                       the primordial offspring of time, the supreme among divinities, the queen of
                       departed spirits, the first of the celestials, and the uniform
                       manifestation of the gods and goddesses'''.getBytes("UTF-8")
    byte[] body21 = '''Queen of Heaven, whether thou art the genial Ceres, the prime parent of
                       fruits, who, joyous at the discovery of thy daughter, didst banish the savage
                       nutriment of the ancient acorn, and, pointing out a better food, dost now
                       till the Eleusinian soil.'''.getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTest2(id: 'foo:test1'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test11'.toURI()))
      s.delete(new SearchTest2(id: 'foo:test2'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test21'.toURI()))
    }

    doInTx { s ->
      SearchTest2 t1 = new SearchTest2(id: 'foo:test1'.toURI(), text: '99 bottles of Fear',
                                       body: body1,
                                       s1: new SearchTest1(id: 'foo:test11'.toURI(), text: 'Oh boy',
                                                           body: body11))
      s.saveOrUpdate(t1)

      SearchTest2 t2 = new SearchTest2(id: 'foo:test2'.toURI(), text: 'A bottle of Rum',
                                       body: body2,
                                       s1: new SearchTest1(id: 'foo:test21'.toURI(), text: 'Oh grl',
                                                           body: body21))
      s.saveOrUpdate(t2)
    }

    doInTx { s ->
      Results r = s.createQuery("""
          select s from SearchTest2 s where search(s.s1.text, 'boy');""").execute()
      checker.verify(r) {
        row { object (class:SearchTest2.class, id:'foo:test1'.toURI()) }
      }

      r = s.createQuery("""
          select s from SearchTest2 s where search(s.s1.text, 'Oh') and search(s.body, 'rises');
          """).execute()
      checker.verify(r) {
        row { object (class:SearchTest2.class, id:'foo:test2'.toURI()) }
      }

      r = s.createQuery("""
          select s from SearchTest2 s where s1 := s.s1 and search(s1.body, 'parent') and
                                            s.text = '99 bottles of Fear';""").
          execute()
      checker.verify(r) {
        row { object (class:SearchTest2.class, id:'foo:test1'.toURI()) }
      }

      r = s.createQuery("""
          select s from SearchTest2 s where st := s.s1.text and search(st, 'grl') and
                                            s.text = 'A bottle of Rum';""").
          execute()
      checker.verify(r) {
        row { object (class:SearchTest2.class, id:'foo:test2'.toURI()) }
      }
    }

    doInTx { s ->
      s.delete(new SearchTest2(id: 'foo:test1'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test11'.toURI()))
      s.delete(new SearchTest2(id: 'foo:test2'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test21'.toURI()))
    }
  }

  void testScore() {
    byte[] body1  = '''Hence the Phrygians, that primeval race, call me Pessinuntica, the Mother of
                       the Gods'''.getBytes("UTF-8")
    byte[] body2  = '''But those who are illumined by the earliest rays of that divinity, the Sun,
                       when he rises, the Aethopians, the Arii, and the Egyptians, so skilled in
                       ancient learning, worshipping me with ceremonies quite appropriate, call me
                       by my true name, Queen Isis.'''.getBytes("UTF-8")
    byte[] body11 = '''I, who am Nature, the parent of all things, the mistress of all the elements,
                       the primordial offspring of time, the supreme among divinities, the queen of
                       departed spirits, the first of the celestials, and the uniform
                       manifestation of the gods and goddesses'''.getBytes("UTF-8")
    byte[] body21 = '''Queen of Heaven, whether thou art the genial Ceres, the prime parent of
                       fruits, who, joyous at the discovery of thy daughter, didst banish the savage
                       nutriment of the ancient acorn, and, pointing out a better food, dost now
                       till the Eleusinian soil.'''.getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTest2(id: 'foo:test1'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test11'.toURI()))
      s.delete(new SearchTest2(id: 'foo:test2'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test21'.toURI()))
    }

    doInTx { s ->
      SearchTest2 t1 = new SearchTest2(id: 'foo:test1'.toURI(), text: '99 bottles of Fear',
                                       body: body1,
                                       s1: new SearchTest1(id: 'foo:test11'.toURI(), text: 'Oh boy',
                                                           body: body11))
      s.saveOrUpdate(t1)

      SearchTest2 t2 = new SearchTest2(id: 'foo:test2'.toURI(), text: 'A bottle of Rum',
                                       body: body2,
                                       s1: new SearchTest1(id: 'foo:test21'.toURI(), text: 'Oh rat',
                                                           body: body21))
      s.saveOrUpdate(t2)
    }

    doInTx { s ->
      Results r = s.createQuery("""
          select s, c1 from SearchTest2 s where search(s.s1.body, 'Ceres', c1);""").execute()
      checker.verify(r) {
        row {
          object (class:SearchTest2.class, id:'foo:test2'.toURI());
          //literal ('0.4811627268791199', dt:'http://www.w3.org/2001/XMLSchema#double')
        }
      }

      r = s.createQuery("""
          select s, c1, c2 from SearchTest2 s where search(s.s1.body, 'Ceres', c1) and
                                                    search(s.text, 'bottle', c2);""").execute()
      checker.verify(r) {
        row {
          object (class:SearchTest2.class, id:'foo:test2'.toURI());
          //literal ('0.4811627268791199', dt:'http://www.w3.org/2001/XMLSchema#double')
          //literal ('1.3950897455215454', dt:'http://www.w3.org/2001/XMLSchema#double')
        }
      }

      r = s.createQuery("""
          select s, c1 from SearchTest2 s where search(s.s1.body, 'queen', c1) order by c1;""").
          execute()
      checker.verify(r) {
        row {
          object (class:SearchTest2.class, id:'foo:test2'.toURI());
          //literal ('0.34877243638038635', dt:'http://www.w3.org/2001/XMLSchema#double')
        }
        row {
          object (class:SearchTest2.class, id:'foo:test1'.toURI());
          //literal ('0.41852694749832153', dt:'http://www.w3.org/2001/XMLSchema#double')
        }
      }

      /* Doesn't work until xsd-resolver can compare against values not in the string-pool
      r = s.createQuery("""
          select s from SearchTest2 s
              where search(s.s1.body, 'queen', c1) and gt(c1, '1.7'^^<xsd:double>);""").
          execute()

      checker.verify(r) {
        row {
          object (class:SearchTest2.class, id:'foo:test1'.toURI());
          literal ('0.41852694749832153', dt:'http://www.w3.org/2001/XMLSchema#double')
        }
      }
      */
    }

    doInTx { s ->
      s.delete(new SearchTest2(id: 'foo:test1'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test11'.toURI()))
      s.delete(new SearchTest2(id: 'foo:test2'.toURI()))
      s.delete(new SearchTest1(id: 'foo:test21'.toURI()))
    }
  }

  void testPreProcessor() {
    String text = '''<head><title>Syncretism</title><script>bloody hell</script></head>
                     <body><p>Hence the Phrygians, that<br>primeval<br>race,<br>call<br>me
                     Pessinuntica, the Mother of the Gods</body>'''
    byte[] body = '''<doc><title>Syncretism</title><p>But those who are illumined by the earliest
                     rays of that<x>divinity,</x>the Sun, when he<y>rises,</y>the Aethopians...
                     </p></doc>'''.getBytes("UTF-8")
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      s.delete(new SearchTestPP(id: 'foo:test3'.toURI()))
    }

    doInTx { s ->
      SearchTestPP t1 = new SearchTestPP(id: 'foo:test3'.toURI(), text: text, body: body)
      s.saveOrUpdate(t1)
    }

    doInTx { s ->
      Results r = s.createQuery("""
          select s from SearchTestPP s where search(s.body, 'divinity');""").execute()
      checker.verify(r) {
        row { object (class:SearchTestPP.class, id:'foo:test3'.toURI()); }
      }

      r = s.createQuery("""
          select s from SearchTestPP s where search(s.text, 'bloody');""").execute()
      checker.verify(r) {
      }

      r = s.createQuery("""
          select s from SearchTestPP s where search(s.text, 'primeval');""").execute()
      checker.verify(r) {
        row { object (class:SearchTestPP.class, id:'foo:test3'.toURI()); }
      }
    }

    doInTx { s ->
      s.delete(new SearchTestPP(id: 'foo:test3'.toURI()))
    }
  }
}

@Entity(graph = 'ri')
class SearchTest1 {
  URI    id;
  String text;
  byte[] body;

  @Id
  void setId(URI id) {
    this.id = id;
  }

  @Searchable(index = 'lucene')
  @Predicate(uri = 'topaz:text')
  void setText(String text) {
    this.text = text;
  }

  @Searchable(index = 'lucene', uri = 'topaz:body')
  @Blob
  void setBody(byte[] body) {
    this.body = body;
  }
}

@Entity(graph = 'ri')
class SearchTest2 {
  URI         id;
  String      text;
  byte[]      body;
  SearchTest1 s1;

  @Id
  void setId(URI id) {
    this.id = id;
  }

  @Searchable(index = 'lucene')
  @Predicate(uri = 'topaz:text2')
  void setText(String text) {
    this.text = text;
  }

  @Searchable(index = 'lucene', uri = 'topaz:body2')
  @Blob
  void setBody(byte[] body) {
    this.body = body;
  }

  @Predicate(uri = 'topaz:s1')
  void setS1(SearchTest1 s1) {
    this.s1 = s1;
  }
}

@Entity(graph = 'ri')
class SearchTestEmb {
  SearchTest1 s1;

  @Embedded
  void setS1(SearchTest1 s1) {
    this.s1 = s1;
  }
}

@Entity(graph = 'ri')
class SearchTestSub extends SearchTest1 {
  @Searchable(index = 'lucene', uri = 'topaz:textsub')
  @Predicate    // <= FIXME: remove when subclassing bug is fixed
  void setText(String text) {
    super.setText(text);
  }

  @Searchable(index = 'lucene', uri = 'topaz:bodysub')
  @Blob
  void setBody(byte[] body) {
    super.setBody(body);
  }
}

@Entity(graph = 'ri')
class SearchTestSub2 extends SearchTest1 {
}

@Entity(graph = 'ri')
class SearchTestPP {
  URI         id;
  String      text;
  byte[]      body;

  @Id
  void setId(URI id) {
    this.id = id;
  }

  @Searchable(index = 'lucene', preProcessor = HtmlTagStripper.class)
  @Predicate(uri = 'topaz:text3')
  void setText(String text) {
    this.text = text;
  }

  @Searchable(index = 'lucene', uri = 'topaz:body3', preProcessor = XmlTagStripper.class)
  @Blob
  void setBody(byte[] body) {
    this.body = body;
  }
}

@Entity(graph = 'ri')
class SearchTestF1 {
  URI          id;
  SearchTestPP other;

  @Id
  void setId(URI id) {
    this.id = id;
  }

  @Searchable(index = 'lucene')
  @Predicate(uri = 'topaz:other')
  void setOther(SearchTestPP other) {
    this.other = other;
  }
}
