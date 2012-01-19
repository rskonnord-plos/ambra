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

package org.topazproject.otm.query;

import org.topazproject.otm.AbstractTest;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.MinusCriterion;
import org.topazproject.otm.criterion.NECriterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.ProxyCriterion;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.CriteriaFilterDefinition;
import org.topazproject.otm.filter.DisjunctiveFilterDefinition;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.filter.OqlFilterDefinition;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.Reply;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for OQL.
 */
public class OqlTest extends AbstractTest {
  private static final Log log = LogFactory.getLog(OqlTest.class);

  void setUp() {
    graphs = [['ri',     'otmtest1', null],
              ['prefix', 'prefix',   'http://mulgara.org/mulgara#PrefixGraph'.toURI()],
              ['xsd',    'xsd',      'http://mulgara.org/mulgara#XMLSchemaModel'.toURI()],
              ['str',    'str',      'http://topazproject.org/graphs#StringCompare'.toURI()]];
    super.setUp();

    rdf.sessFactory.preload(Annotation.class);
    rdf.sessFactory.preload(Article.class);
    rdf.sessFactory.preload(PublicAnnotation.class);
    rdf.sessFactory.preload(Reply.class);
    rdf.sessFactory.validate()

    rdf.sessFactory.setClassMetadata(
        new ClassMetadata([(EntityMode.POJO):new ClassBinder(Object.class)], "Object",
                          Collections.EMPTY_SET, Collections.EMPTY_SET, "ri", null,
                          Collections.EMPTY_SET, null, Collections.EMPTY_SET,
                          Collections.EMPTY_SET))
  }

  void testBasic() {
    def checker = new ResultChecker(test:this)

    URI id1 = "http://localhost/annotation/1".toURI()
    URI id2 = "http://localhost/annotation/2".toURI()
    URI id3 = "http://localhost/annotation/3".toURI()

    URI id4 = "foo:1".toURI()
    URI id5 = "bar:1".toURI()

    doInTx { s ->
      Article art  = new Article(uri:id4, title:"Yo ho ho", description:"A bottle of Rum")
      s.saveOrUpdate(art)
      s.flush()

      Annotation a1 = new PublicAnnotation(id:id1, annotates:id4)
      Annotation a2 = new PublicAnnotation(id:id2, annotates:id4, supersedes:a1)
      Annotation a3 = new PublicAnnotation(id:id3, annotates:id5, supersedes:a2,
                                           foobar:[foo:'one', bar:'two'])
      a1.supersededBy = a2
      a2.supersededBy = a3

      s.saveOrUpdate(a1)
      s.saveOrUpdate(a2)
      s.saveOrUpdate(a3)
    }

    doInTx { s ->
      // single class, simple condition
      Results r = s.createQuery("select a from Article a where a.title = 'Yo ho ho';").execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4) }
      }

      // two classes, simple condition
      r = s.createQuery("""
          select art, ann from Article art, Annotation ann where ann.annotates = art order by ann;
          """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); object (class:PublicAnnotation.class, id:id1) }
        row { object (class:Article.class, uri:id4); object (class:PublicAnnotation.class, id:id2) }
      }

      // no results
      r = s.createQuery("""
            select art.publicAnnotations.note n from Article art
            where art.title = 'Yo ho ho' order by n;
            """).execute()
      checker.verify(r) {
      }

      // no results, cast, !=
      r = s.createQuery("""
            select ann from Annotation ann
            where cast(ann.annotates, Article).title != 'Yo ho ho' 
            order by ann;
            """).execute()
      checker.verify(r) {
      }

      // !=
      r = s.createQuery("""
          select ann from Annotation ann where ann.annotates != <foo:1> order by ann;
          """).execute()
      checker.verify(r) {
        row { object (class:PublicAnnotation.class, id:id3) }
      }

      r = s.createQuery("""
          select ann from Annotation ann where <foo:1> != ann.annotates order by ann;
          """).execute()
      checker.verify(r) {
        row { object (class:PublicAnnotation.class, id:id3) }
      }

      /* FIXME: this currently creates a query like
       * select $ann from <local:///topazproject#otmtest1> where
       *    $ann <rdf:type> <http://www.w3.org/2000/10/annotation-ns#Annotation>
       *      in <local:///topazproject#otmtest1>
       *    and $ann <http://www.w3.org/2000/10/annotation-ns#annotates> $x
       *      in <local:///topazproject#otmtest1>
       *    and (() minus ($oqltmp2_0 <mulgara:is> <foo:1> ))
       *    order by $ann;
       *
      r = s.createQuery("""
          select ann from Annotation ann where x := ann.annotates and <foo:1> != x order by ann;
          """).execute()
      checker.verify(r) {
        row { object (class:PublicAnnotation.class, id:id3) }
      }
      */

      // typed/untyped literal
      r = s.createQuery("select a.title, a.description from Article a where a.title = 'Yo ho ho';").
            execute()
      checker.verify(r) {
        row {
          literal ('Yo ho ho');
          literal ('A bottle of Rum', dt:'http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral')
        }
      }

      // minus
      r = s.createQuery("""
              select a.id from Annotation a where
                  a.annotates = <${id4}> minus a.supersedes = <${id1}>;"""
            ).execute()
      checker.verify(r) {
        row { uri (id1); }
      }

      r = s.createQuery("""
              select a.id id from Annotation a where
                  a.annotates = <${id4}> minus a.supersedes = <${id2}> order by id;"""
            ).execute()
      checker.verify(r) {
        row { uri (id1); }
        row { uri (id2); }
      }

      r = s.createQuery("""
              select a.id from Annotation a where
                  (a.annotates = <${id4}> or a.annotates = <${id5}>) minus
                    (a.supersedes = <${id1}> or a.id = <${id1}>);"""
            ).execute()
      checker.verify(r) {
        row { uri (id3); }
      }

      r = s.createQuery("""
              select a.id id from Annotation a where
                  (a.annotates = <${id4}> or a.annotates = <${id5}>) minus
                    ((a.supersedes = <${id1}> or a.id = <${id1}>) minus (a.id = <${id2}>))
              order by id;"""
            ).execute()
      checker.verify(r) {
        row { uri (id2); }
        row { uri (id3); }
      }

      // subquery
      r = s.createQuery("""
            select art,
              (select pa from Article a2 where pa := art.publicAnnotations order by pa)
            from Article art 
            where p := art.publicAnnotations order by art;
            """).execute()
      checker.verify(r) {
        row {
          object (class:Article.class, uri:id4)
          subq {
            row { object (class:PublicAnnotation.class, id:id1) }
            row { object (class:PublicAnnotation.class, id:id2) }
          }
        }
      }

      // count
      r = s.createQuery("""
            select art, count(art.publicAnnotations) from Article art order by art;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ("2.0") }
      }

      r = s.createQuery("""
            select art, count(art.publicAnnotations) c from Article art order by art;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ("2.0") }
      }

      // multiple orders, one or more by a constant
      r = s.createQuery("""
            select art, foo from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' order by art, foo;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ('yellow') }
      }

      r = s.createQuery("""
            select art, foo, bar from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' and bar := 'black'
            order by art, foo, bar;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ('yellow'); string ('black') }
      }

      r = s.createQuery("""
            select art, foo, bar from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' and bar := 'black'
            order by foo, art, bar;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ('yellow'); string ('black') }
      }

      r = s.createQuery("""
            select art, foo, bar from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' and bar := 'black'
            order by foo, bar, art;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ('yellow'); string ('black') }
      }

      // id field
      r = s.createQuery("select a.uri from Article a where a.title = 'Yo ho ho';").execute()
      checker.verify(r) {
        row { uri (id4) }
      }

      r = s.createQuery("select a from Article a where a.uri = <${id4}>;").execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4) }
      }

      // omitted where clause
      r = s.createQuery("select n from Annotation n order by n;").execute()
      checker.verify(r) {
        row { object (class:PublicAnnotation.class, id:id1) }
        row { object (class:PublicAnnotation.class, id:id2) }
        row { object (class:PublicAnnotation.class, id:id3) }
      }

      // unconstrained variables
      r = s.createQuery("select o from Object o order by o;").execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4) }
        row { object (class:PublicAnnotation.class, id:id1) }
        row { object (class:PublicAnnotation.class, id:id2) }
        row { object (class:PublicAnnotation.class, id:id3) }
      }

      // many <mulgara:equals>
      r = s.createQuery("""
          select a from Article a where b := a and c := a and d := a and
          e := b and f := b and g := b and h := c and i := c and
          j := f and k := f and l := f and m := f and n := k and o := k and p := k;
          """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4) }
      }
    }
  }

  void testPredicateExpressions() {
    // setup data
    Class cls = rdf.class('Test1') {
      name  ()
      age   (type:'xsd:int')
      birth (type:'xsd:date')
    }

    def o1 = cls.newInstance(name:'Bob',  age:5,   birth:new Date("6 Jun 1860"))
    def o2 = cls.newInstance(name:'Joe',  age:42,  birth:new Date("4 Feb 1970"))
    def o3 = cls.newInstance(name:'John', age:153, birth:new Date("2 Mar 2002"))

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
      s.saveOrUpdate(o3)
    }

    def checker = new ResultChecker(test:this)
    Results r

    doInTx { s ->
      r = s.createQuery("select t from Test1 t where t.{p -> p = <topaz:name>} = 'Bob';").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery(
            "select t from Test1 t where t.{p -> p = <topaz:name> or p = <topaz:age>} = 'Bob';").
            execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery("""
            select t from Test1 t where
              t.{p -> pp := p and ppp := pp and ppp = <topaz:name>} = 'Bob';
            """).execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery("select t from Test1 t where t.{p -> p = :p} = 'Bob';").
            setParameter("p", "topaz:name").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery("select n from Test1 t where n := t.{p -> p = :p1 or p = :p2} order by n;").
            setParameter("p1", "topaz:name").setParameter("p2", "topaz:age").execute()
      checker.verify(r) {
        row { string ('5') }
        row { string ('42') }
        row { string ('153') }
        row { string ('Bob') }
        row { string ('Joe') }
        row { string ('John') }
      }
    }
  }

  void testProjectionWildcard() {
    Class cls = rdf.class('Test1') {
      name  ()
      age   (type:'xsd:int', javaType:Integer)
      birth (type:'xsd:date')
    }

    // all fields set
    def o1 = cls.newInstance(name:'Bob',  age:5,   birth:new Date("6 Jun 1860"))
    doInTx { s ->
      s.saveOrUpdate(o1)
    }

    def checker = new ResultChecker(test:this)
    Results r

    doInTx { s ->
      r = s.createQuery("select t.* p from Test1 t order by p;").execute()
      checker.verify(r) {
        row { uri ("http://rdf.topazproject.org/RDF/age".toURI()) }
        row { uri ("http://rdf.topazproject.org/RDF/birth".toURI()) }
        row { uri ("http://rdf.topazproject.org/RDF/name".toURI()) }
        row { uri ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".toURI()) }
      }
    }

    // only some fields set
    def o2 = cls.newInstance(name:'Jack')
    doInTx { s ->
      s.delete(o1)
      s.saveOrUpdate(o2)
    }

    doInTx { s ->
      r = s.createQuery("select t.* p from Test1 t order by p;").execute()
      checker.verify(r) {
        row { uri ("http://rdf.topazproject.org/RDF/name".toURI()) }
        row { uri ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".toURI()) }
      }
    }

    // nested classes
    Class cls2 = rdf.class('Test2') {
      name  ()
      info1 () {
        address()
      }
      info2 (embedded:true) {
        sig ()
      }
    }

    def o3 = cls2.newInstance(name:'Bob', info1:[address:'easy st'], info2:[sig:'good day'])
    doInTx { s ->
      s.saveOrUpdate(o3)
    }

    doInTx { s ->
      r = s.createQuery("select t.* p from Test2 t order by p;").execute()
      checker.verify(r) {
        row { uri ("http://rdf.topazproject.org/RDF/info1".toURI()) }
        row { uri ("http://rdf.topazproject.org/RDF/name".toURI()) }
        row { uri ("http://rdf.topazproject.org/RDF/sig".toURI()) }
        row { uri ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".toURI()) }
      }

      r = s.createQuery("select t.* p from Info1 t order by p;").execute()
      checker.verify(r) {
        row { uri ("http://rdf.topazproject.org/RDF/address".toURI()) }
        row { uri ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".toURI()) }
      }
    }
  }

  void testFunctions() {
    // test lt, gt, le, ge
    Class cls = rdf.class('Test1') {
      name  ()
      age   (type:'xsd:int')
      birth (type:'xsd:date')
    }

    def o1 = cls.newInstance(name:'Bob',  age:5,   birth:new Date("6 Jun 1860"))
    def o2 = cls.newInstance(name:'Joe',  age:42,  birth:new Date("4 Feb 1970"))
    def o3 = cls.newInstance(name:'John', age:153, birth:new Date("2 Mar 2002"))

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
      s.saveOrUpdate(o3)
    }

    def checker = new ResultChecker(test:this)
    Results r

    doInTx { s ->
      for (test in [['birth', "'1970-02-04'^^<xsd:date>"], ['name', "'Joe'"],
                    /* ['age', "'42'^^<xsd:int>"] */]) {
        r = s.createQuery(
            "select t.name n from Test1 t where lt(t.${test[0]}, ${test[1]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Bob') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where gt(${test[1]}, t.${test[0]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Bob') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where le(t.${test[0]}, ${test[1]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Bob') }
          row { string ('Joe') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where ge(${test[1]}, t.${test[0]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Bob') }
          row { string ('Joe') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where gt(t.${test[0]}, ${test[1]}) order by n;").execute()
        checker.verify(r) {
          row { string ('John') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where lt(${test[1]}, t.${test[0]}) order by n;").execute()
        checker.verify(r) {
          row { string ('John') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where ge(t.${test[0]}, ${test[1]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Joe')  }
          row { string ('John') }
        }

        r = s.createQuery(
            "select t.name n from Test1 t where le(${test[1]}, t.${test[0]}) order by n;").execute()
        checker.verify(r) {
          row { string ('Joe')  }
          row { string ('John') }
        }
      }

      /* uncomment when the various resolvers support variables on both sides of the comparison
      r = s.createQuery("""
              select t1.name n1, t2.name n2 from Test1 t1, Test1 t2 where lt(t1.name, t2.name)
              order by n1, n2;
              """).execute()
      checker.verify(r) {
        row { string ('Bob'); string('Joe')  }
        row { string ('Bob'); string('John') }
        row { string ('Joe'); string('John') }
      }

      r = s.createQuery("""
              select t1.name n1, t2.name n2 from Test1 t1, Test1 t2 where le(t1.name, t2.name)
              order by n1, n2;
              """).execute()
      checker.verify(r) {
        row { string ('Bob');  string ('Bob')  }
        row { string ('Bob');  string ('Joe')  }
        row { string ('Bob');  string ('John') }
        row { string ('Joe');  string ('Joe')  }
        row { string ('Joe');  string ('John') }
        row { string ('John'); string ('John') }
      }

      r = s.createQuery("""
              select t1.name n1, t2.name n2 from Test1 t1, Test1 t2 where gt(t1.name, t2.name)
              order by n1, n2;
              """).execute()
      checker.verify(r) {
        row { string ('Joe');  string ('Bob') }
        row { string ('John'); string ('Bob') }
        row { string ('John'); string ('Joe') }
      }

      r = s.createQuery("""
              select t1.name n1, t2.name n2 from Test1 t1, Test1 t2 where ge(t1.name, t2.name)
              order by n1, n2;
              """).execute()
      checker.verify(r) {
        row { string ('Bob');  string ('Bob')  }
        row { string ('Joe');  string ('Bob')  }
        row { string ('Joe');  string ('Joe')  }
        row { string ('John'); string ('Bob')  }
        row { string ('John'); string ('Joe')  }
        row { string ('John'); string ('John') }
      }
      */

      for (test in [['lt', ['Bob']],  ['le', ['Bob', 'Joe']],
                    ['gt', ['John']], ['ge', ['Joe', 'John']]]) {
        r = s.createQuery("select t.name n from Test1 t where ${test[0]}(t.birth, :b) order by n;").
              setParameter("b", "1970-02-04").execute()
        checker.verify(r) {
          for (name in test[1])
            row { string (name) }
        }
      }

      for (test in [['gt', ['Bob']],  ['ge', ['Bob', 'Joe']],
                    ['lt', ['John']], ['le', ['Joe', 'John']]]) {
        r = s.createQuery("select t.name n from Test1 t where ${test[0]}(:b, t.birth) order by n;").
              setParameter("b", "1970-02-04").execute()
        checker.verify(r) {
          for (name in test[1])
            row { string (name) }
        }
      }
    }

    // test index()
    int cnt = 2;
    for (col in ['Predicate', 'RdfBag', 'RdfSeq', 'RdfAlt', 'RdfList']) {
      cls = rdf.class('Test' + cnt) {
        age   (type:'xsd:int')
        names (className:'Name' + cnt, maxCard:-1, colMapping:col) {
          first ()
          last  ()
        }
      }

      def nc = cls.getClassLoader().loadClass('Name' + cnt)
      cnt++;

      o1 = cls.newInstance(age:5, names:[nc.newInstance(first:'Bob', last:'Brown')])
      o2 = cls.newInstance(age:6, names:[nc.newInstance(first:'Charly', last:'Chan'),
                                         nc.newInstance(first:'Joe', last:'Jackson')])
      o3 = cls.newInstance(age:7, names:[nc.newInstance(first:'Charly', last:'Chan'),
                                         nc.newInstance(first:'Joe', last:'Jackson'),
                                         nc.newInstance(first:'Sally', last:'Sommer')])

      doInTx { s ->
        s.saveOrUpdate(o1)
        s.saveOrUpdate(o2)
        s.saveOrUpdate(o3)
      }
      doInTx { s ->
        r = s.createQuery("""
            select n.first f, n.last, index(n) from ${cls.name} t where
            t.age = '5'^^<xsd:int> and n := t.names order by f desc;""").execute()
        checker.verify(r) {
          row { string (o1.names[0].first); string(o1.names[0].last); string (0) }
        }

        r = s.createQuery("""
            select n.first f, n.last, index(n) from ${cls.name} t where
            t.age = '6'^^<xsd:int> and n := t.names order by f desc;""").execute()
        checker.verify(r) {
          if (col == 'Predicate') {
            row { string (o2.names[1].first); string(o2.names[1].last); string (0) }
            row { string (o2.names[0].first); string(o2.names[0].last); string (1) }
          } else {
            row { string (o2.names[1].first); string(o2.names[1].last); string (1) }
            row { string (o2.names[0].first); string(o2.names[0].last); string (0) }
          }
        }

        r = s.createQuery("""
            select n.first f, n.last, index(n) from ${cls.name} t where
            t.age = '7'^^<xsd:int> and n := t.names order by f desc;""").execute()
        checker.verify(r) {
          if (col == 'Predicate') {
            row { string (o3.names[2].first); string(o3.names[2].last); string ('0') }
            row { string (o3.names[1].first); string(o3.names[1].last); string ('1') }
            row { string (o3.names[0].first); string(o3.names[0].last); string ('2') }
          } else {
            row { string (o3.names[2].first); string(o3.names[2].last); string ('2') }
            row { string (o3.names[1].first); string(o3.names[1].last); string ('1') }
            row { string (o3.names[0].first); string(o3.names[0].last); string ('0') }
          }
        }
      }
    }
  }

  void testOrderLimitOffset() {
    // test order, limit, offset
    Class cls = rdf.class('Test1') {
      name  ()
      age   (type:'xsd:int')
      birth (type:'xsd:date')
    }

    def o1 = cls.newInstance(name:'Bob',  age:5,   birth:new Date("6 Jun 1860"))
    def o2 = cls.newInstance(name:'Joe',  age:42,  birth:new Date("4 Feb 1970"))
    def o3 = cls.newInstance(name:'John', age:153, birth:new Date("2 Mar 2002"))
    def o4 = cls.newInstance(name:'Paul', age:42,  birth:new Date("7 Jul 1970"))

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
      s.saveOrUpdate(o3)
      s.saveOrUpdate(o4)
    }

    def checker = new ResultChecker(test:this)
    Results r

    doInTx { s ->
      r = s.createQuery("select t.name n from Test1 t order by n asc;").execute()
      checker.verify(r) {
        row { string ('Bob') }
        row { string ('Joe') }
        row { string ('John') }
        row { string ('Paul') }
      }

      r = s.createQuery("select t.name n from Test1 t order by n desc;").execute()
      checker.verify(r) {
        row { string ('Paul') }
        row { string ('John') }
        row { string ('Joe') }
        row { string ('Bob') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age;").execute()
      checker.verify(r) {
        row { string ('5') }
        row { string ('42') }
        row { string ('153') }
      }

      r = s.createQuery("select t.age age, t.name name from Test1 t order by age, name;").execute()
      checker.verify(r) {
        row { string ('5');   string ('Bob')  }
        row { string ('42');  string ('Joe')  }
        row { string ('42');  string ('Paul') }
        row { string ('153'); string ('John') }
      }

      r = s.createQuery("select t.age age, t.name name from Test1 t order by age, name desc;").
            execute()
      checker.verify(r) {
        row { string ('5');   string ('Bob')  }
        row { string ('42');  string ('Paul') }
        row { string ('42');  string ('Joe')  }
        row { string ('153'); string ('John') }
      }

      r = s.createQuery(
            "select t.age age, foo from Test1 t where foo := 'yellow' order by age, foo;").
            execute()
      checker.verify(r) {
        row { string ('5');   string ('yellow') }
        row { string ('42');  string ('yellow') }
        row { string ('153'); string ('yellow') }
      }

      r = s.createQuery(
            "select t.age age, foo from Test1 t where foo := 'yellow' order by foo, age;").
            execute()
      checker.verify(r) {
        row { string ('5');   string ('yellow') }
        row { string ('42');  string ('yellow') }
        row { string ('153'); string ('yellow') }
      }

      r = s.createQuery(
            "select t.age age, foo from Test1 t where foo := 'yellow' order by foo asc, age;").
            execute()
      checker.verify(r) {
        row { string ('5');   string ('yellow') }
        row { string ('42');  string ('yellow') }
        row { string ('153'); string ('yellow') }
      }

      r = s.createQuery(
            "select t.age age, foo from Test1 t where foo := 'yellow' order by age, foo asc;").
            execute()
      checker.verify(r) {
        row { string ('5');   string ('yellow') }
        row { string ('42');  string ('yellow') }
        row { string ('153'); string ('yellow') }
      }

      r = s.createQuery("""
          select t.age, foo from Test1 t where
              t.age = '42'^^<xsd:int> and foo := 'yellow' order by foo;
          """).execute()
      checker.verify(r) {
        row { string ('42'); string ('yellow') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age offset 1;").execute()
      checker.verify(r) {
        row { string ('42') }
        row { string ('153') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age offset 2;").execute()
      checker.verify(r) {
        row { string ('153') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age limit 1 offset 1;").execute()
      checker.verify(r) {
        row { string ('42') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age limit 2 offset 1;").execute()
      checker.verify(r) {
        row { string ('42') }
        row { string ('153') }
      }

      r = s.createQuery("select t.age age from Test1 t order by age limit 2;").execute()
      checker.verify(r) {
        row { string ('5') }
        row { string ('42') }
      }
    }
  }

  void testEmbeddedClass() {
    // create data
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
      info (embedded:true) {
        personal (embedded:true) {
          name (embedded:true) {
            givenName ()
            surname   ()
          }
          address ()
        }
        external (embedded:true) {
          sig ()
        }
      }
    }

    URI id1 = "http://localhost/annotation/1".toURI()
    URI id2 = "http://localhost/test/1".toURI()
    URI id3 = "http://localhost/test/2".toURI()
    doInTx { s ->
      Annotation a1 = new PublicAnnotation(id:id1, foobar:[foo:'one', bar:'two'])
      s.saveOrUpdate(a1)

      def o1 = cls.newInstance(id:id2, state:4,
                   info:[personal:[name:[givenName:'Bob', surname:'Cutter'], address:'easy st']])
      s.saveOrUpdate(o1)
      def o2 = cls.newInstance(id:id3, state:2,
                   info:[personal:[name:[givenName:'Jack', surname:'Keller'], address:'skid row'],
                         external:[sig:'hello']])
      s.saveOrUpdate(o2)
    }

    // run tests
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      Results r =
          s.createQuery("select ann from Annotation ann where ann.foobar.bar = 'two';").execute()
      checker.verify(r) {
        row { object (class:Annotation.class, id:id1) }
      }

      r = s.createQuery(
          "select obj from Test1 obj where obj.info.personal.name.givenName = 'Jack';").execute()
      checker.verify(r) {
        row { object (class:cls, id:id3) }
      }

      r = s.createQuery(
            "select obj from Test1 obj where obj.info.personal.address != 'foo' order by obj;").
            execute()
      checker.verify(r) {
        row { object (class:cls, id:id2) }
        row { object (class:cls, id:id3) }
      }

      r = s.createQuery(
            "select obj from Test1 obj where obj.info.external.sig != 'foo' order by obj;").
            execute()
      checker.verify(r) {
        row { object (class:cls, id:id3) }
      }
    }
  }

  void testUriAndTypes() {
    // create data
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
      info () {
        name () {
          givenName ()
          surname   ()
        }
      }
    }

    def o1 = cls.newInstance(state:1, info:[name:[givenName:'Bob', surname:'Cutter']])
    def o2 = cls.newInstance(state:2, info:[name:[givenName:'Jack', surname:'Keller']])

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
    }

    // run tests
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      Results r = s.createQuery("""
          select obj from Test1 obj where
            obj.<http://rdf.topazproject.org/RDF/info>.<http://rdf.topazproject.org/RDF/name>.
                <http://rdf.topazproject.org/RDF/givenName> = 'Jack';
          """).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = s.createQuery("""
          select obj from Test1 obj where
            obj.info.<http://rdf.topazproject.org/RDF/name>.
                <http://rdf.topazproject.org/RDF/givenName> = 'Bob';
          """).execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery("""
          select obj.<http://rdf.topazproject.org/RDF/info> from Test1 obj where
            obj.info.name.<http://rdf.topazproject.org/RDF/givenName> = 'Bob';
          """).execute()
      checker.verify(r) {
        row { uri (o1.info.id) }
      }

      r = s.createQuery("""
          select obj.<http://rdf.topazproject.org/RDF/info>.<http://rdf.topazproject.org/RDF/name> n
            from Test1 obj order by n;
          """).execute()
      checker.verify(r) {
        if (o1.info.name.id < o2.info.name.id) {
          row { uri (o1.info.name.id) }
          row { uri (o2.info.name.id) }
        } else {
          row { uri (o2.info.name.id) }
          row { uri (o1.info.name.id) }
        }
      }

      assert shouldFail(QueryException, {
        r = s.createQuery("""
          select obj from Test1 obj where
            obj.<http://rdf.topazproject.org/RDF/info>.name.givenName != 'foo' order by obj;
          """).execute()
      }).contains("error parsing query")

      assert shouldFail(QueryException, {
        r = s.createQuery("""
          select obj from Test1 obj where
            obj.<http://rdf.topazproject.org/RDF/info>.<http://rdf.topazproject.org/RDF/name>.
                givenName != 'foo' order by obj;
          """).execute()
      }).contains("error parsing query")
    }
  }

  void testCollections() {
    URI id1 = "http://localhost/test/1".toURI()
    URI id2 = "http://localhost/test/2".toURI()

    int cnt = 0;

    // collections of simple type
    for (col in ['Predicate', 'RdfBag', 'RdfSeq', 'RdfAlt', 'RdfList']) {
      Class cls = rdf.class('Test' + cnt++) {
        name () 'Jack Rabbit'
        colors (maxCard:-1, colMapping:col)
      }
      def o1 = cls.newInstance(id:id1, colors:['cyan', 'grey', 'yellow'])
      def o2 = cls.newInstance(id:id2, colors:['magenta', 'sienna', 'bisque'])

      checkCollection([o1, o2], cls, "colors",
                      [["'fuchsia'"], []],
                      [["'cyan'"], [o1]], [["'sienna'"], [o2]], [["'yellow'"], [o1]],
                      [["'sienna'", "'magenta'"], [o2]],
                      [["'sienna'", "'yellow'"], [o1, o2]], [["'cyan'", "'bisque'"], [o1, o2]])
    }

    // collections of class
    Class c = rdf.class('Color') {
      color ()
    }

    URI cid1 = "http://localhost/test/color/1".toURI()
    URI cid2 = "http://localhost/test/color/2".toURI()
    URI cid3 = "http://localhost/test/color/3".toURI()
    URI cid4 = "http://localhost/test/color/4".toURI()
    URI cid5 = "http://localhost/test/color/5".toURI()
    URI cid6 = "http://localhost/test/color/6".toURI()

    def c1 = c.newInstance(id:cid1, color:'cyan')
    def c2 = c.newInstance(id:cid2, color:'grey')
    def c3 = c.newInstance(id:cid3, color:'yellow')
    def c4 = c.newInstance(id:cid4, color:'magenta')
    def c5 = c.newInstance(id:cid5, color:'sienna')
    def c6 = c.newInstance(id:cid6, color:'bisque')

    doInTx { s ->
      s.saveOrUpdate(c1)
      s.saveOrUpdate(c2)
      s.saveOrUpdate(c3)
      s.saveOrUpdate(c4)
      s.saveOrUpdate(c5)
      s.saveOrUpdate(c6)
    }

    for (col in ['Predicate', 'RdfBag', 'RdfSeq', 'RdfAlt', 'RdfList']) {
      Class cls = rdf.class('Test' + cnt++) {
        name () 'Jack Rabbit'
        colors (type:'Color', maxCard:-1, colMapping:col)
      }
      def o1 = cls.newInstance(id:id1, colors:[c1, c2, c3])
      def o2 = cls.newInstance(id:id2, colors:[c4, c5, c6])

      checkCollection([o1, o2], cls, "colors.color",
                      [["'fuchsia'"], []],
                      [["'cyan'"], [o1]], [["'sienna'"], [o2]], [["'yellow'"], [o1]],
                      [["'sienna'", "'magenta'"], [o2]],
                      [["'sienna'", "'yellow'"], [o1, o2]], [["'cyan'", "'bisque'"], [o1, o2]])

      checkCollection([o1, o2], cls, "colors",
                      [["<foo:bar>"], []],
                      [["<${cid4}>"], [o2]], [["<${cid2}>"], [o1]], [["<${cid6}>"], [o2]],
                      [["<${cid1}>", "<${cid2}>"], [o1]],
                      [["<${cid2}>", "<${cid4}>"], [o1, o2]],
                      [["<${cid5}>", "<${cid3}>"], [o1, o2]],
                      [["<${cid1}>", "<${cid6}>"], [o1, o2]])
    }
  }

  private void checkCollection(List obj, Class cls, String sel, List... tests) {
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      for (o in obj)
        s.saveOrUpdate(o)
    }

    doInTx { s ->
      for (test in tests) {
        def conds = test[0]
        def objs  = test[1]

        Results r = s.createQuery(
          "select o from ${cls.name} o where " + conds.collect{ "o.${sel} = ${it}" }.join(" or ") +
          " order by o;").execute()

        checker.verify(r) {
          for (o in objs)
            row { object ('class':cls, 'id':o.id) }
        }
      }
    }
  }

  void testGraphs() {
    def checker = new ResultChecker(test:this)

    // set up graphs
    for (num in 1..3) {
      def m = new GraphConfig("m${num}", "local:///topazproject#otmtest_m${num}".toURI(), null)
      rdf.sessFactory.addGraph(m);

      try { doInTx(true) { s -> s.dropGraph(m.getId()) } } catch (OtmException oe) { }
      doInTx{ s -> s.createGraph(m.getId()) }
    }

    // predicate stored with child graph
    Class cls = rdf.class('Test1', graph:'m1') {
      foo1 ()
      bar1 (className:'Bar11', graph:'m2', inverse:true) {
        foo2 ()
        bar2 (className:'Bar21', graph:'m3') {
          foo3 ()
        }
      }
    }

    def o1 = cls.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    def o2 = cls.newInstance(foo1:'f4', bar1:[foo2:'f5', bar2:[foo3:'f6']])

    doInTx { s ->
      s.saveOrUpdate(o1);
      s.saveOrUpdate(o2);

      Results r = s.createQuery("select t from Test1 t where " +
                            "t.foo1 = 'f1' and t.bar1.foo2 = 'f2' and t.bar1.bar2.foo3 = 'f3';").
                    execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      s.delete(o1);
      s.delete(o2);
    }

    // predicate stored with parent graph
    Class bcl2 = rdf.class('Bar22', graph:'m3') {
      foo3 ()
    }
    Class bcl1 = rdf.class('Bar12', graph:'m2') {
      foo2 ()
      bar2 (type:'Bar22', inverse:true)
    }
    Class cls1 = rdf.class('Test2', graph:'m1') {
      foo1 ()
      bar1 (type:'Bar12')
    }

    o1 = cls1.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    o2 = cls1.newInstance(foo1:'f4', bar1:[foo2:'f5', bar2:[foo3:'f6']])

    doInTx { s ->
      s.saveOrUpdate(o1);
      s.saveOrUpdate(o2);

      Results r = s.createQuery("select t from Test2 t where " +
                            "t.foo1 = 'f1' and t.bar1.foo2 = 'f2' and t.bar1.bar2.foo3 = 'f3';").
                    execute()
      checker.verify(r) {
        row { object (class:cls1, id:o1.id) }
      }

      s.delete(o1);
      s.delete(o2);
    }

    // non-entity class with no graph, but graph on predicate
    o1 = cls1.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    rdf.sessFactory.preload(NonEntity.class);
    rdf.sessFactory.validate()

    doInTx { s ->
      s.saveOrUpdate(o1);

      Results r = s.createQuery("select t from Test2 t where " +
                                "cast(cast(t, NonEntity).bar1, Bar12).foo2 = 'f2';").
                    execute()
      checker.verify(r) {
        row { object (class:cls1, id:o1.id) }
      }

      assert shouldFail(QueryException, {
        s.createQuery("select t from Test2 t where cast(t, NonEntity).bar1.foo2 = 'f2';").execute()
      }).contains("error parsing query")

      s.delete(o1);
    }
  }

  void testClassResolution() {
    def checker = new ResultChecker(test:this)

    Class cls = rdf.class('org.foo.Test1') {
      foo1 ()
    }

    def o1 = cls.newInstance(foo1:'f1')

    doInTx { s ->
      s.saveOrUpdate(o1);

      Results r = s.createQuery("select t from Test1 t where t.foo1 = 'f1';").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = s.createQuery("select t from org.foo.Test1 t where t.foo1 = 'f1';").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      assert shouldFail(QueryException, {
        r = s.createQuery("select t from org.bar.Test1 t where t.foo1 = 'f1';").execute()
      })

      s.delete(o1);
    }
  }

  void testParameters() {
    // create data
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
      info () {
        name () {
          givenName ()
          surname   ()
        }
      }
      blog (type:'xsd:anyURI', propType:'OBJECT')
    }

    def o1 = cls.newInstance(state:1, info:[name:[givenName:'Bob', surname:'Cutter']],
                             blog:"http://www.foo.com/".toURI(), id:"foo:1".toURI())
    def o2 = cls.newInstance(state:2, info:[name:[givenName:'Jack', surname:'Keller']],
                             blog:"http://www.bar.com/".toURI(), id:"foo:2".toURI())
    def o3 = cls.newInstance(state:3, info:[name:[givenName:'Billy', surname:'Bob']],
                             blog:"http://www.baz.com/".toURI(), id:"foo:3".toURI())

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
      s.saveOrUpdate(o3)
    }

    // run tests
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      // plain literal
      Query q = s.createQuery("select obj from Test1 obj where obj.info.name.givenName = :name;")
      assertEquals(['name'] as Set, q.getParameterNames())

      Results r = q.setParameter("name", "Jack").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = q.setPlainLiteral("name", "Jack", null).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = q.setTypedLiteral("name", "Jack", "xsd:string".toURI()).execute()
      checker.verify(r, warnings:true) {
      }

      r = q.setUri("name", "Jack:1".toURI()).execute()
      checker.verify(r, warnings:true) {
      }

      assert shouldFail(QueryException, {
        r = q.setUri("name", "Jack".toURI()).execute()
      })

      // typed literal
      q = s.createQuery("select obj from Test1 obj where obj.state = :state;")
      assertEquals(['state'] as Set, q.getParameterNames())

      r = q.setParameter("state", 1).execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = q.setPlainLiteral("state", "1", null).execute()
      checker.verify(r, warnings:true) {
      }

      r = q.setTypedLiteral("state", "1", "xsd:int".toURI()).execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
      }

      r = q.setUri("state", "Jack:1".toURI()).execute()
      checker.verify(r, warnings:true) {
      }

      assert shouldFail(QueryException, {
        r = q.setUri("state", "Jack".toURI()).execute()
      })

      // uri
      q = s.createQuery("select obj from Test1 obj where obj.blog = :blog;")
      assertEquals(['blog'] as Set, q.getParameterNames())

      r = q.setParameter("blog", "http://www.bar.com/").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = q.setParameter("blog", "http://www.bar.com/".toURI()).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = q.setPlainLiteral("blog", "http://www.bar.com/", null).execute()
      checker.verify(r, warnings:true) {
      }

      r = q.setTypedLiteral("blog", "http://www.bar.com/", "xsd:anyURI".toURI()).execute()
      checker.verify(r, warnings:true) {
      }

      r = q.setUri("blog", "http://www.bar.com/".toURI()).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      assert shouldFail(QueryException, {
        r = q.setUri("blog", "Jack".toURI()).execute()
      })

      // class
      q = s.createQuery("select obj from Test1 obj where obj.info = :info;")
      assertEquals(['info'] as Set, q.getParameterNames())

      r = q.setParameter("info", o2.info.id).execute()
      checker.verify(r, warnings:true) {
        row { object (class:cls, id:o2.id) }
      }

      q = s.createQuery("select obj from Test1 obj where obj.info.id = :info;")
      assertEquals(['info'] as Set, q.getParameterNames())

      r = q.setParameter("info", o2.info.id).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      // no type
      q = s.createQuery("select obj from Test1 obj where obj.<topaz:info> = :info;")
      assertEquals(['info'] as Set, q.getParameterNames())

      assert shouldFail(QueryException, {
        r = q.setParameter("info", o2.info.id.toString()).execute()
      })

      assert shouldFail(QueryException, {
        r = q.setParameter("info", o2.info.id).execute()
      })

      r = q.setUri("info", o2.info.id).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      r = q.setPlainLiteral("info", o2.info.id.toString(), null).execute()
      checker.verify(r) {
      }

      r = q.setTypedLiteral("info", o2.info.id.toString(), "xsd:anyURI".toURI()).execute()
      checker.verify(r) {
      }

      // same parameter twice
      q = s.createQuery("""
          select obj from Test1 obj where
            obj.info.name.givenName = :name or obj.info.name.surname = :name order by obj;
          """)
      assertEquals(['name'] as Set, q.getParameterNames())

      r = q.setParameter("name", "Bob").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o3.id) }
      }

      // two parameters
      q = s.createQuery("""
          select obj from Test1 obj
            where obj.info.name.givenName = :gname or obj.info.name.surname = :sname order by obj;
          """)
      assertEquals(['gname', 'sname'] as Set, q.getParameterNames())

      r = q.setParameter("gname", "Bob").setParameter("sname", "Keller").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o2.id) }
      }
    }
  }

  void testFilters() {
    // create data
    Class clsN = rdf.class('Name') {
      givenName ()
      surname   ()
    }

    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
      info (fetch:'eager') {
        name (className:'Name2', extendsClass:'Name', fetch:'eager') {
          middleName ()
        }
      }
      obj (javaType:URI.class)
    }

    Class clsEN = rdf.class('ExtName', type:'ExtName', extendsClass:'Name2') {
      suffix ()
    }

    def o1 = cls.newInstance(id:"foo:1".toURI(), state:1,
                             info:[name:[givenName:'Bob', surname:'Cutter', middleName:'Fritz']])
    def o2 = cls.newInstance(id:"foo:2".toURI(), state:2,
                             info:[name:[givenName:'Jack', surname:'Keller']])
    def o3 = cls.newInstance(id:"foo:3".toURI(), state:3,
                             info:[name:[givenName:'Billy', surname:'Bob']])
    def o4 = cls.newInstance(id:"foo:4".toURI(), state:3,
                       info:[name:clsEN.newInstance(givenName:'Billy', surname:'Bob', suffix:'Jr')])
    def o5 = cls.newInstance(id:"foo:5".toURI(), state:4, obj:"bar:5".toURI())

    doInTx { s ->
      for (o in [o1, o2, o3, o4, o5])
        s.saveOrUpdate(o)
    }

    FilterDefinition ofd1 =
        new OqlFilterDefinition('noBob', 'Test1',
                                "select o from Test1 o where o.info.name.givenName != 'Bob';")
    FilterDefinition ofd2 =
        new OqlFilterDefinition('state', 'Test1',
                                "select o from Test1 o where o.state != :state;")
    FilterDefinition ofd3 =
        new OqlFilterDefinition('noJack', 'Name',
                                "select n from Name n where n.givenName != 'Jack';")
    FilterDefinition ofd4 =
        new OqlFilterDefinition('noJr', 'ExtName',
                                "select n from ExtName n where n.suffix != 'Jr';")

    FilterDefinition cfd1 = new CriteriaFilterDefinition('noBob',
        new DetachedCriteria('Test1').createCriteria('info').createCriteria('name').
            add(Restrictions.ne('givenName', 'Bob')).parent.parent)
    FilterDefinition cfd2 = new CriteriaFilterDefinition('state',
        new DetachedCriteria('Test1').add(Restrictions.ne('state', new Parameter('state'))))
    FilterDefinition cfd3 = new CriteriaFilterDefinition('noJack',
        new DetachedCriteria('Name').add(Restrictions.ne('givenName', 'Jack')))
    FilterDefinition cfd4 = new CriteriaFilterDefinition('noJr',
        new DetachedCriteria('ExtName').add(Restrictions.ne('suffix', 'Jr')))

    def checker = new ResultChecker(test:this)

    // run plain filter tests
    for (t in [[ofd1, ofd2, ofd3, ofd4], [cfd1, cfd2, cfd3, cfd4]]) {
      // set up available filters
      for (fd in t) {
        rdf.sessFactory.removeFilterDefinition(fd.getFilterName());
        rdf.sessFactory.addFilterDefinition(fd);
      }

      // run oql tests
      doInTx { s ->
        // no filters
        Results r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o1.id) }
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
          row { object (class:cls, id:o5.id) }
        }

        // with filter(s)
        s.enableFilter('noBob');
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        s.disableFilter('noBob');
        s.enableFilter('state').setParameter('state', 3);
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o1.id) }
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o5.id) }
        }

        s.enableFilter('state').setParameter('state', 2);
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o1.id) }
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
          row { object (class:cls, id:o5.id) }
        }

        s.enableFilter('noBob');
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        // filter on non-root class
        s.disableFilter('state');
        assertNotNull(s.enableFilter('noJack'))
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        r = s.createQuery(
            "select obj from Test1 obj where obj.info.name.id != <foo:1> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        // filter on subclass
        s.disableFilter('noJack');
        assertNotNull(s.enableFilter('noJr'))
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        r = s.createQuery(
            "select obj from Test1 obj where obj.info.name.id != <foo:1> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
        }

        s.disableFilter('noBob');
        r = s.createQuery(
            "select obj, x from Test1 obj where x := cast(obj.obj, Object) order by x;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o5.id); object () }
        }

        // other constraints
        assertNotNull(s.enableFilter('noJack'))
        r = s.createQuery(
            "select obj from Test1 obj where obj.state = '3'^^<xsd:int> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
          row { object (class:cls, id:o4.id) }
        }

        r = s.createQuery(
            "select obj from Test1 obj where obj.state = '2'^^<xsd:int> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
        }

        r = s.createQuery("select obj.info.name from Test1 obj where obj.state = '2'^^<xsd:int>;").
              execute()
        checker.verify(r) {
        }

        s.disableFilter('noJack');
        r = s.createQuery("select obj.info.name from Test1 obj where obj.state = '2'^^<xsd:int>;").
              execute()
        checker.verify(r) {
          row { object (class:o2.info.name.getClass(), id:o2.info.name.id) }
        }
      }

      // run criteria tests
      doInTx { s ->
        // no filters
        List r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o1, o2, o3, o4, o5], r)

        // with filter(s)
        s.enableFilter('noBob');
        r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o2, o3, o4], r)

        s.disableFilter('noBob');
        s.enableFilter('state').setParameter('state', 3);
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o1, o2, o5], r)

        s.enableFilter('state').setParameter('state', 2);
        r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o1, o3, o4, o5], r)

        s.enableFilter('noBob');
        r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o3, o4], r)

        // filter on non-root class
        s.disableFilter('state');
        assertNotNull(s.enableFilter('noJack'))
        r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o2, o3, o4], r)

        r = s.createCriteria(cls).createCriteria('info').createCriteria('name').
              add(Restrictions.ne('id', 'foo:1'.toURI())).parent.parent.
              addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o3, o4], r)

        // filter on subclass
        s.disableFilter('noJack');
        assertNotNull(s.enableFilter('noJr'))
        r = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o2, o3, o4], r)

        r = s.createCriteria(cls).createCriteria('info').createCriteria('name').
              add(Restrictions.ne('id', 'foo:1'.toURI())).parent.parent.
              addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
        assertEquals([o2, o3], r)

        // other constraints
        s.disableFilter('noBob');
        assertNotNull(s.enableFilter('noJack'))
        r = s.createCriteria(cls).add(Restrictions.eq('state', 3)).addOrder(Order.asc('state')).
              addOrder(Order.asc('id')).list()
        assertEquals([o3, o4], r)

        r = s.createCriteria(cls).add(Restrictions.eq('state', 2)).addOrder(Order.asc('state')).
              addOrder(Order.asc('id')).list()
        assertEquals([o2], r)
      }
    }

    // run junction filter tests
    FilterDefinition jfd1 = new ConjunctiveFilterDefinition('notBobAndNotState', 'Test1').
                                addFilterDefinition(ofd1).addFilterDefinition(cfd2);
    FilterDefinition jfd2 = new DisjunctiveFilterDefinition('notBobOrNotState', 'Test1').
                                addFilterDefinition(cfd1).addFilterDefinition(ofd2);

    rdf.sessFactory.addFilterDefinition(jfd1);
    rdf.sessFactory.addFilterDefinition(jfd2);

    doInTx { s ->
      s.enableFilter('notBobAndNotState').getFilters('state')[0].setParameter('state', 2);
      Results r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
      }
      List l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o3, o4], l)

      s.enableFilter('notBobAndNotState').getFilters('state')[0].setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o2, o3, o4], l)

      s.disableFilter('notBobAndNotState');
      s.enableFilter('notBobOrNotState').getFilters('state')[0].setParameter('state', 2);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
        row { object (class:cls, id:o5.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o1, o2, o3, o4, o5], l)

      s.enableFilter('notBobOrNotState').getFilters('state')[0].setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
        row { object (class:cls, id:o5.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o2, o3, o4, o5], l)
    }

    FilterDefinition ofd5 =
        new OqlFilterDefinition('noKeller', 'Test1',
                                "select o from Test1 o where o.info.name.surname != 'Keller';")
    FilterDefinition cfd5 = new CriteriaFilterDefinition('noKeller',
        new DetachedCriteria('Test1').createCriteria('info').createCriteria('name').
            add(Restrictions.ne('surname', 'Keller')).parent.parent)

    FilterDefinition jfd3 =
        new ConjunctiveFilterDefinition('notBobAnd_notStateOrNotKeller', 'Test1').
            addFilterDefinition(ofd1).addFilterDefinition(
                new DisjunctiveFilterDefinition('notStateOrNotKeller', 'Test1').
                    addFilterDefinition(cfd2).addFilterDefinition(ofd5));

    FilterDefinition jfd4 =
        new DisjunctiveFilterDefinition('notStateOr_notBobAndNotKeller', 'Test1').
            addFilterDefinition(ofd2).addFilterDefinition(
                new ConjunctiveFilterDefinition('notBobAndNotKeller', 'Test1').
                    addFilterDefinition(cfd1).addFilterDefinition(cfd5));

    rdf.sessFactory.addFilterDefinition(jfd3);
    rdf.sessFactory.addFilterDefinition(jfd4);

    doInTx { s ->
      Filter stF = s.enableFilter('notBobAnd_notStateOrNotKeller').
                     getFilters('notStateOrNotKeller')[0].getFilters('state')[0];
      stF.setParameter('state', 2);
      Results r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
      }
      List l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o3, o4], l)

      stF.setParameter('state', 3);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o2, o3, o4], l)

      s.disableFilter('notBobAnd_notStateOrNotKeller');
      stF = s.enableFilter('notStateOr_notBobAndNotKeller').getFilters('state')[0];
      stF.setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
        row { object (class:cls, id:o5.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o2, o3, o4, o5], l)

      stF.setParameter('state', 2);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
        row { object (class:cls, id:o5.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o1, o3, o4, o5], l)

      stF.setParameter('state', 3);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
        row { object (class:cls, id:o4.id) }
        row { object (class:cls, id:o5.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).addOrder(Order.asc('id')).list()
      assertEquals([o1, o2, o3, o4, o5], l)
    }
  }

  void testFilterDefs() {
    doInTx { s ->
      // normal criteria -> oql
      DetachedCriteria dc = new DetachedCriteria("Article")
      dc.add(Restrictions.disjunction().
                         add(Restrictions.eq("title", "foo")).
                         add(Restrictions.eq("authors", new Parameter("auth")))).

        createCriteria("parts").
          add(Restrictions.ne("date", new Date("08 Jul 2007"))).
          add(Restrictions.conjunction().
                           add(Restrictions.le("state", 2)).
                           add(Restrictions.gt("rights", "none"))).

        createCriteria("nextObject").
          add(Restrictions.eq("dc_type", new URI("dc:type"))).
          add(Restrictions.eq("uri", new URI("foo:bar"))).
          add(Restrictions.minus(Restrictions.ne("pid", new URI("foo:baz")),
                                 Restrictions.eq("pid", new URI("bar:baz"))))

      assertEquals(1, dc.getParameterNames().size())
      assertEquals("auth", dc.getParameterNames().iterator().next())

      FilterDefinition cfd = new CriteriaFilterDefinition("critF", dc);
      assertEquals("select o from Article o where ((o.title = 'foo' or o.authors = 'blah')) and (v1 := cast(o.parts, ObjectInfo) and ((v1.date != '2007-07-08'^^<http://www.w3.org/2001/XMLSchema#date>) and ((le(v1.state, '2'^^<http://www.w3.org/2001/XMLSchema#int>) and gt(v1.rights, 'none'^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>))) and (v22 := cast(v1.nextObject, ObjectInfo) and ((v22.dc_type = <dc:type>) and (v22.uri = <foo:bar>) and (( (v22.pid != 'foo:baz') minus (v22.pid = 'bar:baz') ))))));", cfd.createFilter(s).setParameter('auth', 'blah').getQuery().toString())

      // referrer criteria -> oql
      dc = new DetachedCriteria("Article")
      dc.add(Restrictions.disjunction().
                         add(Restrictions.eq("title", "foo")).
                         add(Restrictions.eq("authors", new Parameter("auth")))).

        createReferrerCriteria("ObjectInfo", "isPartOf").
          add(Restrictions.ne("date", new Date("08 Jul 2007"))).
          add(Restrictions.conjunction().
                           add(Restrictions.le("state", 2)).
                           add(Restrictions.gt("rights", "none"))).

        createCriteria("nextObject").
          add(Restrictions.eq("dc_type", new URI("dc:type"))).
          add(Restrictions.eq("uri", new URI("foo:bar")))

      assertEquals(1, dc.getParameterNames().size())
      assertEquals("auth", dc.getParameterNames().iterator().next())

      cfd = new CriteriaFilterDefinition("critFR", dc)
      assertEquals("select o from Article o, ObjectInfo v1 where ((o.title = 'foo' or o.authors = 'blah')) and (o = cast(v1.isPartOf, Article) and ((v1.date != '2007-07-08'^^<http://www.w3.org/2001/XMLSchema#date>) and ((le(v1.state, '2'^^<http://www.w3.org/2001/XMLSchema#int>) and gt(v1.rights, 'none'^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>))) and (v22 := cast(v1.nextObject, ObjectInfo) and ((v22.dc_type = <dc:type>) and (v22.uri = <foo:bar>)))));", cfd.createFilter(s).setParameter('auth', 'blah').getQuery().toString())

      // cast criteria -> oql
      dc = new DetachedCriteria("Article")
      dc.add(Restrictions.disjunction().
                         add(Restrictions.eq("title", "foo")).
                         add(Restrictions.eq("authors", new Parameter("auth")))).

        createReferrerCriteria("Annotation", "annotates", true).
          add(Restrictions.ne("created", new Date("08 Jul 2007 GMT"))).

        createCriteria("supersedes", "PublicAnnotation").
          add(Restrictions.eq("note", "a dog"))

      assertEquals(1, dc.getParameterNames().size())
      assertEquals("auth", dc.getParameterNames().iterator().next())

      cfd = new CriteriaFilterDefinition("critFC", dc)
      assertEquals("select o from Article o, Annotation v1 where ((o.title = 'foo' or o.authors = 'blah')) and (o = cast(v1.annotates, Article) and ((v1.created != '2007-07-08T00:00:00.000Z'^^<http://www.w3.org/2001/XMLSchema#dateTime>) and (v21 := cast(v1.supersedes, PublicAnnotation) and ((v21.note = 'a dog')))));", cfd.createFilter(s).setParameter('auth', 'blah').getQuery().toString())

      // oql -> criteria
      def qry = """select o from Article o where
        (o.title = 'foo' or o.authors = :auth) and o.nextObject.uri = <foo:bar> and
        o.nextObject.dc_type = <dc:type> and p := o.parts and q := p.nextObject and
        (x := :x and (q.date = '2007' or q.rights = x and le(q.dc_type, <x:y>))) and
        (q.pid != 'foo:baz' minus q.pid = 'bar:baz');"""
      FilterDefinition ofd = new OqlFilterDefinition("oqlF", "Article", qry)
      Criteria c = ofd.createFilter(s).setParameter('auth', 'blah').getCriteria()

      assertEquals(1, c.criterionList.size())           // o.title = 'foo' or o.authorts = :auth
      assertInstanceOf(Disjunction, c.criterionList[0])
      assertEquals(2, c.criterionList[0].criterions.size())
      assertInstanceOf(EQCriterion, c.criterionList[0].criterions[0])
      assertInstanceOf(EQCriterion, c.criterionList[0].criterions[1])
      assertEquals('title', c.criterionList[0].criterions[0].fieldName)
      assertEquals('foo', c.criterionList[0].criterions[0].value)
      assertEquals('authors', c.criterionList[0].criterions[1].fieldName)
      assertInstanceOf(Parameter, c.criterionList[0].criterions[1].value)
      assertEquals('auth', c.criterionList[0].criterions[1].value.parameterName)

      assertEquals(3, c.children.size())
      sortChildren(c, ['parts', 'nextObject', 'nextObject'])
      assertEquals('parts', c.children[0].mapping.name)
      assertEquals('nextObject', c.children[1].mapping.name)
      assertEquals('nextObject', c.children[2].mapping.name)
      assertEquals('ObjectInfo', c.children[0].classMetadata.name)
      assertEquals('ObjectInfo', c.children[1].classMetadata.name)
      assertEquals('ObjectInfo', c.children[2].classMetadata.name)
      assertEquals(1, c.children[0].children.size())
      assertEquals(0, c.children[1].children.size())
      assertEquals(0, c.children[2].children.size())
      assertEquals(0, c.children[0].criterionList.size())
      assertEquals(1, c.children[1].criterionList.size())
      assertEquals(1, c.children[2].criterionList.size())
      assertInstanceOf(EQCriterion, c.children[1].criterionList[0])
      assertInstanceOf(EQCriterion, c.children[2].criterionList[0])
      assertEquals('dc_type', c.children[1].criterionList[0].fieldName)
      assertEquals('dc:type'.toURI(), c.children[1].criterionList[0].value)
      assertEquals('uri', c.children[2].criterionList[0].fieldName)
      assertEquals('foo:bar'.toURI(), c.children[2].criterionList[0].value)

      c = c.children[0].children[0]                     // 'q'
      assertEquals('nextObject', c.mapping.name)
      assertEquals('ObjectInfo', c.classMetadata.name)
      assertEquals(0, c.children.size())
      assertEquals(2, c.criterionList.size())

      Criterion n = c.criterionList[0]
      assertInstanceOf(Disjunction, n)
      assertEquals(2, n.criterions.size())
      assertInstanceOf(EQCriterion, n.criterions[0])
      assertInstanceOf(Conjunction, n.criterions[1])
      assertEquals('date', n.criterions[0].fieldName)
      assertEquals('2007', n.criterions[0].value)
      n = n.criterions[1]
      assertEquals(2, n.criterions.size())
      assertInstanceOf(EQCriterion, n.criterions[0])
      assertInstanceOf(ProxyCriterion, n.criterions[1])
      assertEquals('rights', n.criterions[0].fieldName)
      assertInstanceOf(Parameter, n.criterions[0].value)
      assertEquals('x', n.criterions[0].value.parameterName)
      assertEquals('le', n.criterions[1].function)
      assertEquals(2, n.criterions[1].arguments.length)
      assertEquals('dc_type', n.criterions[1].arguments[0])
      assertEquals('x:y'.toURI(), n.criterions[1].arguments[1])

      n = c.criterionList[1]
      assertInstanceOf(MinusCriterion, n)
      assertInstanceOf(NECriterion, n.minuend)
      assertInstanceOf(EQCriterion, n.subtrahend)
      assertEquals('pid', n.minuend.fieldName)
      assertEquals('foo:baz', n.minuend.value)
      assertEquals('pid', n.subtrahend.fieldName)
      assertEquals('bar:baz', n.subtrahend.value)

      /* oql w/ deref in proj -> referrer criteria 
       * note: this next query is not a valid filter query, but toCriteria() supports it
       */
      qry = """select n.nextObject from Article o where
        (o.title = 'foo' or o.authors = :auth) and o.nextObject.uri = <foo:bar> and
        n := o.parts and q := n.nextObject and n.dc_type = <dc:type> and
        (x := :x and (q.date = '2007' or q.rights = x and le(q.dc_type, <x:y>)));"""
      ofd = new OqlFilterDefinition("oqlR", "ObjectInfo", qry)
      c = ofd.createFilter(s).setParameter('auth', 'blah').getCriteria()

      assertEquals(0, c.criterionList.size())
      assertEquals(1, c.children.size())

      c = c.children[0]                         // 'n'
      assertEquals('nextObject', c.mapping.name)
      assertEquals('ObjectInfo', c.classMetadata.name)
      assert c.isReferrer()

      assertEquals(1, c.criterionList.size())   // n.dc_type = <dc:type>
      assertInstanceOf(EQCriterion, c.criterionList[0])
      assertEquals('dc_type', c.criterionList[0].fieldName)
      assertEquals('dc:type'.toURI(), c.criterionList[0].value)

      assertEquals(2, c.children.size())        // n := o.parts, q := n.nextObject
      sortChildren(c, ['parts', 'nextObject'])
      assertEquals('parts', c.children[0].mapping.name)
      assertEquals('nextObject', c.children[1].mapping.name)
      assertEquals('Article', c.children[0].classMetadata.name)
      assertEquals('ObjectInfo', c.children[1].classMetadata.name)
      assertEquals(true, c.children[0].isReferrer())
      assertEquals(false, c.children[1].isReferrer())

      Criteria ch = c.children[0]               // 'o'
      assertEquals(1, ch.criterionList.size())
      assertInstanceOf(Disjunction, ch.criterionList[0])
      assertEquals(2, ch.criterionList[0].criterions.size())
      assertInstanceOf(EQCriterion, ch.criterionList[0].criterions[0])
      assertInstanceOf(EQCriterion, ch.criterionList[0].criterions[1])
      assertEquals('title', ch.criterionList[0].criterions[0].fieldName)
      assertEquals('foo', ch.criterionList[0].criterions[0].value)
      assertEquals('authors', ch.criterionList[0].criterions[1].fieldName)
      assertInstanceOf(Parameter, ch.criterionList[0].criterions[1].value)
      assertEquals('auth', ch.criterionList[0].criterions[1].value.parameterName)

      assertEquals(1, ch.children.size())
      ch = ch.children[0]                       // o.nextObject
      assertEquals(0, ch.children.size())
      assertEquals(1, ch.criterionList.size())
      assertInstanceOf(EQCriterion, ch.criterionList[0])
      assertEquals('uri', ch.criterionList[0].fieldName)
      assertEquals('foo:bar'.toURI(), ch.criterionList[0].value)

      ch = c.children[1]                        // 'q'
      assertEquals(0, ch.children.size())
      assertEquals(1, ch.criterionList.size())
      assertInstanceOf(Disjunction, ch.criterionList[0])
      assertEquals(2, ch.criterionList[0].criterions.size())
      assertInstanceOf(EQCriterion, ch.criterionList[0].criterions[0])
      assertInstanceOf(Conjunction, ch.criterionList[0].criterions[1])
      assertEquals('date', ch.criterionList[0].criterions[0].fieldName)
      assertEquals('2007', ch.criterionList[0].criterions[0].value)
      assertEquals(2, ch.criterionList[0].criterions[1].criterions.size())
      assertInstanceOf(EQCriterion, ch.criterionList[0].criterions[1].criterions[0])
      assertInstanceOf(ProxyCriterion, ch.criterionList[0].criterions[1].criterions[1])
      assertEquals('rights', ch.criterionList[0].criterions[1].criterions[0].fieldName)
      assertInstanceOf(Parameter, ch.criterionList[0].criterions[1].criterions[0].value)
      assertEquals('x', ch.criterionList[0].criterions[1].criterions[0].value.parameterName)
      assertEquals('le', ch.criterionList[0].criterions[1].criterions[1].function)
      assertEquals(2, ch.criterionList[0].criterions[1].criterions[1].arguments.length)
      assertEquals('dc_type', ch.criterionList[0].criterions[1].criterions[1].arguments[0])
      assertEquals('x:y'.toURI(), ch.criterionList[0].criterions[1].criterions[1].arguments[1])

      // oql with casts -> criteria
      qry = """select o from Article o where o.title = 'foo' and
        cast(o.nextObject, Article).categories = 'foo' and
        cast(cast(o.dc_type, Annotation).annotates, Article).dc_type = <dc:type> and
        p := cast(o.parts, Annotation) and q := cast(p.annotates, ObjectInfo) and
        (q.date = '2007' or le(q.dc_type, <x:y>));"""
      ofd = new OqlFilterDefinition("oqlFC", "Article", qry)
      c = ofd.createFilter(s).getCriteria()

      assertEquals(1, c.criterionList.size())           // o.title = 'foo'
      assertInstanceOf(EQCriterion, c.criterionList[0])
      assertEquals('title', c.criterionList[0].fieldName)
      assertEquals('foo', c.criterionList[0].value)

      assertEquals(3, c.children.size())
      sortChildren(c, ['parts', 'nextObject', 'dc_type'])
      assertEquals('parts',      c.children[0].mapping.name)
      assertEquals('nextObject', c.children[1].mapping.name)
      assertEquals('dc_type',    c.children[2].mapping.name)
      assertEquals('Annotation', c.children[0].classMetadata.name)
      assertEquals('Article',    c.children[1].classMetadata.name)
      assertEquals('Annotation', c.children[2].classMetadata.name)
      assertEquals(1, c.children[0].children.size())
      assertEquals(0, c.children[1].children.size())
      assertEquals(1, c.children[2].children.size())
      assertEquals(0, c.children[0].criterionList.size())
      assertEquals(1, c.children[1].criterionList.size())
      assertEquals(0, c.children[2].criterionList.size())
      assertInstanceOf(EQCriterion, c.children[1].criterionList[0])
      assertEquals('categories', c.children[1].criterionList[0].fieldName)
      assertEquals('foo', c.children[1].criterionList[0].value)
      assertEquals(0, c.children[2].children[0].children.size())
      assertEquals(1, c.children[2].children[0].criterionList.size())
      assertInstanceOf(EQCriterion, c.children[2].children[0].criterionList[0])
      assertEquals('dc_type', c.children[2].children[0].criterionList[0].fieldName)
      assertEquals('dc:type'.toURI(), c.children[2].children[0].criterionList[0].value)

      c = c.children[0].children[0]                     // 'q'
      assertEquals('annotates', c.mapping.name)
      assertEquals('ObjectInfo', c.classMetadata.name)
      assertEquals(0, c.children.size())
      assertEquals(1, c.criterionList.size())
      assertInstanceOf(Disjunction, c.criterionList[0])
      assertEquals(2, c.criterionList[0].criterions.size())
      assertInstanceOf(EQCriterion, c.criterionList[0].criterions[0])
      assertInstanceOf(ProxyCriterion, c.criterionList[0].criterions[1])
      assertEquals('date', c.criterionList[0].criterions[0].fieldName)
      assertEquals('2007', c.criterionList[0].criterions[0].value)
      assertEquals('le', c.criterionList[0].criterions[1].function)
      assertEquals(2, c.criterionList[0].criterions[1].arguments.length)
      assertEquals('dc_type', c.criterionList[0].criterions[1].arguments[0])
      assertEquals('x:y'.toURI(), c.criterionList[0].criterions[1].arguments[1])

      // oql w/ casts -> referrer criteria 
      qry = """select n from Article a where a.title = 'foo' and
        b := cast(cast(a.nextObject, Annotation).annotates, Reply) and
        n := cast(b.root, Article) and q := cast(n.nextObject, Article)
        and n.dc_type = <dc:type> and
        (q.date = '2007' or le(cast(q, Article).dc_type, <x:y>));"""
      ofd = new OqlFilterDefinition("oqlRC", "Article", qry)
      c = ofd.createFilter(s).getCriteria()

      assertEquals('Article', c.classMetadata.name)
      assertEquals(1, c.criterionList.size())   // n.dc_type = <dc:type>
      assertInstanceOf(EQCriterion, c.criterionList[0])
      assertEquals('dc_type', c.criterionList[0].fieldName)
      assertEquals('dc:type'.toURI(), c.criterionList[0].value)

      assertEquals(2, c.children.size())        // n := b.root, q := n.nextObject 
      sortChildren(c, ['root', 'nextObject'])
      assertEquals('root',       c.children[0].mapping.name)
      assertEquals('nextObject', c.children[1].mapping.name)
      assertEquals('Reply',      c.children[0].classMetadata.name)
      assertEquals('Article',    c.children[1].classMetadata.name)
      assertEquals(true,  c.children[0].isReferrer())
      assertEquals(false, c.children[1].isReferrer())

      ch = c.children[0]                        // 'b := cast(a.nextObject, Annotation).annotates'
      assertEquals(1, ch.children.size())
      ch = ch.children[0]
      assertEquals('annotates',  ch.mapping.name)
      assertEquals('Annotation', ch.classMetadata.name)
      assertEquals(true,         ch.isReferrer())
      ch = ch.children[0]
      assertEquals('nextObject', ch.mapping.name)
      assertEquals('Article',    ch.classMetadata.name)
      assertEquals(true,         ch.isReferrer())

      assertEquals(0, ch.children.size())
      assertEquals(1, ch.criterionList.size())  // a.title
      assertInstanceOf(EQCriterion, ch.criterionList[0])
      assertEquals('title', ch.criterionList[0].fieldName)
      assertEquals('foo', ch.criterionList[0].value)

      ch = c.children[1]                        // 'q'
      assertEquals(0, ch.children.size())
      assertEquals(1, ch.criterionList.size())
      assertInstanceOf(Disjunction, ch.criterionList[0])
      assertEquals(2, ch.criterionList[0].criterions.size())
      assertInstanceOf(EQCriterion, ch.criterionList[0].criterions[0])
      assertInstanceOf(ProxyCriterion, ch.criterionList[0].criterions[1])
      assertEquals('date', ch.criterionList[0].criterions[0].fieldName)
      assertEquals('2007', ch.criterionList[0].criterions[0].value)
      assertEquals('le', ch.criterionList[0].criterions[1].function)
      assertEquals(2, ch.criterionList[0].criterions[1].arguments.length)
      assertEquals('dc_type', ch.criterionList[0].criterions[1].arguments[0])
      assertEquals('x:y'.toURI(), ch.criterionList[0].criterions[1].arguments[1])

      // oql w/ long chain of aliases backwards from projection -> more referrer criteria 
      qry = """select n from Article a where 
        n := m.isPartOf and
        m := o.parts    and
        o := p.isPartOf and
        p := a.parts
        ;"""
      ofd = new OqlFilterDefinition("oqlRC2", "Article", qry)
      c = ofd.createFilter(s).getCriteria()

      assertEquals('Article', c.classMetadata.name)
      assertEquals(0, c.criterionList.size())
      assertEquals(1, c.children.size())

      c = c.children[0];                        // n := m.isPartOf
      assertEquals('isPartOf',   c.mapping.name)
      assertEquals('ObjectInfo', c.classMetadata.name)
      assertEquals(true,         c.isReferrer())
      assertEquals(0, c.criterionList.size())
      assertEquals(1, c.children.size())

      c = c.children[0];                        // m := o.parts
      assertEquals('parts',      c.mapping.name)
      assertEquals('Article',    c.classMetadata.name)
      assertEquals(true,         c.isReferrer())
      assertEquals(0, c.criterionList.size())
      assertEquals(1, c.children.size())

      c = c.children[0];                        // o := p.isPartOf
      assertEquals('isPartOf',   c.mapping.name)
      assertEquals('ObjectInfo', c.classMetadata.name)
      assertEquals(true,         c.isReferrer())
      assertEquals(0, c.criterionList.size())
      assertEquals(1, c.children.size())

      c = c.children[0];                        // p := a.parts
      assertEquals('parts',      c.mapping.name)
      assertEquals('Article',    c.classMetadata.name)
      assertEquals(true,         c.isReferrer())
      assertEquals(0, c.criterionList.size())
      assertEquals(0, c.children.size())

      // two types for the same criteria
      qry = """select n from Article a where a.title = 'foo' and
               n := cast(a, Annotation).annotates;"""
      ofd = new OqlFilterDefinition("oqlT1", "Article", qry)
      assert shouldFail(QueryException, {
        c = ofd.createFilter(s).getCriteria()
      })

      qry = """select o from Article o where o.title = 'foo' and
        cast(cast(o, Annotation).annotates, Article).dc_type = <dc:type>;"""
      ofd = new OqlFilterDefinition("oqlT2", "Article", qry)
      assert shouldFail(QueryException, {
        c = ofd.createFilter(s).getCriteria()
      })

      qry = """select o from Article o where o.title = 'foo' and
        p := cast(o.parts, Annotation) and q := cast(p, ObjectInfo).nextObject;"""
      ofd = new OqlFilterDefinition("oqlT3", "Article", qry)
      assert shouldFail(QueryException, {
        c = ofd.createFilter(s).getCriteria()
      })
    }
  }

  private void sortChildren(Criteria parent, List memberOrder) {
    for (int idx = 0; idx < memberOrder.size() -1 ; idx++) {
      int pos = parent.children.findIndexOf(idx + 1) { it.mapping.name == memberOrder[idx] }
      if (pos > idx) {
        Criteria tmp = parent.children[idx]
        parent.children[idx] = parent.children[pos]
        parent.children[pos] = tmp
      }
    }
  }

  private void assertInstanceOf(Class cls, Object obj) {
    assert cls.isInstance(obj) : "${obj} is not an instance of ${cls}"
  }
}

public class ResultChecker extends BuilderSupport {
  private Stack resHist = new Stack()
  private Stack colHist = new Stack()

  private Results res;
  private int     col = 0;

  GroovyTestCase test;

  protected Object createNode(Object name) {
    return createNode(name, null, null)
  }

  protected Object createNode(Object name, Object value) {
    return createNode(name, null, value)
  }

  protected Object createNode(Object name, Map attributes) {
    return createNode(name, attributes, null)
  }

  protected Object createNode(Object name, Map attributes, Object value) {
    switch (name) {
      case 'verify':
        res = value;
        if (attributes?.get('warnings'))
          test.assertNotNull("Expected warnings but got none", res.warnings);
        else {
          if (res.warnings)
            test.log.error "Got warnings: " +res.warnings.join(System.getProperty("line.separator"))
          test.assertNull("Got unexpected warnings", res.warnings);
        }
        break;

      case 'row':
        test.assertTrue("Not enough rows in result", res.next())
        col = 0;
        break;

      case 'object':
        def o = res.get(col++);
        for (a in attributes) {
          if (a.key == 'class') {
            def v = o."${a.key}"
            test.assertTrue("${v} is not a subclass of ${a.value.name}",
                            a.value.isAssignableFrom(v))
          } else {
            test.assertEquals(a.value, a.key.split('\\.').inject(o, { x, f -> x."${f}" }))
          }
        }
        break;

      case 'string':
        test.assertEquals(value.toString(), res.getString(col++));
        break;

      case 'uri':
        test.assertEquals((value instanceof URI) ? value : value.toURI(), res.getURI(col++));
        break;

      case 'literal':
        def lit = res.getLiteral(col++);
        test.assertEquals(value, lit.getValue());

        if (attributes?.dt)
          test.assertEquals(
              (attributes.dt instanceof URI) ? attributes.dt : attributes.dt.toURI(),
              lit.getDatatype());
        else
          test.assertNull("datatype on '${lit}' is not null", lit.getDatatype());

        if (attributes?.lang)
          test.assertEquals(attributes.lang, lit.getLanguage());
        else
          test.assertNull("language-tag on '${lit}' is not null", lit.getLanguage());

        break;

      case 'nul':
        test.assertNull("column ${col} is not null", res.get(col++));
        break;

      case 'subq':
        def q = res.getSubQueryResults(col++);
        resHist.push(res)
        colHist.push(col)
        res = q
        break;

      default:
        throw new Exception("unsupported item '${name}'");
    }

    return name
  }

  protected void setParent(Object parent, Object child) {
  }

  protected void nodeCompleted(Object parent, Object node) {
    if (parent == null || node == 'subq') {
      test.assertFalse("Extra rows in result", res.next())
      res.close()
    }

    if (node == 'subq') {
      res = resHist.pop()
      col = colHist.pop()
    }
  }
}

private class NonEntity {
  URI bar1;

  @Predicate(uri = "topaz:bar1", graph = "m1")
  void setBar1(URI bar1) {
    this.bar1 = bar1;
  }
}
