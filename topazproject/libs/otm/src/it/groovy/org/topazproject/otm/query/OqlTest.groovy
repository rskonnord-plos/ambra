/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.ProxyCriterion;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.CriteriaFilterDefinition;
import org.topazproject.otm.filter.DisjunctiveFilterDefinition;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.filter.OqlFilterDefinition;
import org.topazproject.otm.metadata.RdfBuilder;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PublicAnnotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for OQL.
 */
public class OqlTest extends GroovyTestCase {
  private static final Log log = LogFactory.getLog(OqlTest.class);

  def rdf;
  def store;

  void setUp() {
    store = new ItqlStore("http://localhost:9091/mulgara-service/services/ItqlBeanService".toURI())
    rdf = new RdfBuilder(
        sessFactory:new SessionFactory(tripleStore:store), defModel:'ri', defUriPrefix:'topaz:')

    for (c in [['ri',     'otmtest1', null],
               ['prefix', 'prefix',   'mulgara:PrefixModel'.toURI()],
               ['xsd',    'xsd',      'mulgara:XMLSchemaModel'.toURI()],
               ['str',    'str',      'http://topazproject.org/models#StringCompare'.toURI()]]) {
      def m = new ModelConfig(c[0], "local:///topazproject#${c[1]}".toURI(), c[2])
      rdf.sessFactory.addModel(m)
      try { store.dropModel(m); } catch (Throwable t) { }
      store.createModel(m)
    }

    rdf.sessFactory.preload(Annotation.class);
    rdf.sessFactory.preload(Article.class);
    rdf.sessFactory.preload(PublicAnnotation.class);
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
            select art, count(art.publicAnnotations) from Article art 
            where p := art.publicAnnotations order by art;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ("2.0") }
      }

      // multiple orders, one by a constant
      r = s.createQuery("""
            select art, foo from Article art 
            where art.<rdf:type> = <topaz:Article> and foo := 'yellow' order by art, foo;
            """).execute()
      checker.verify(r) {
        row { object (class:Article.class, uri:id4); string ('yellow') }
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
      rdf.sessFactory.setClassMetadata(
          new ClassMetadata(Object.class, "Object", null, Collections.EMPTY_SET, "ri", null, null,
                            Collections.EMPTY_SET));

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
            "select t.name n from Test1 t where le(t.${test[0]}, ${test[1]}) order by n;").execute()
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
            "select t.name n from Test1 t where ge(t.${test[0]}, ${test[1]}) order by n;").execute()
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
    for (col in ['Predicate', 'RdfBag', 'RdfSeq', 'RdfAlt' /* , 'RdfList' */]) {
      Class cls = rdf.class('Test' + cnt++) {
        name () 'Jack Rabbit'
        colors (maxCard:-1, colMapping:col)
      }
      def o1 = cls.newInstance(id:id1, colors:['cyan', 'grey', 'yellow'])
      def o2 = cls.newInstance(id:id2, colors:['magenta', 'sienna', 'bisque'])

      checkCollection([o1, o2], cls, "colors", "'fuchsia'", "'sienna'", ["'sienna'", "'yellow'"])
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

    for (col in ['Predicate', 'RdfBag', 'RdfSeq', 'RdfAlt' /* , 'RdfList' */]) {
      Class cls = rdf.class('Test' + cnt++) {
        name () 'Jack Rabbit'
        colors (type:'Color', maxCard:-1, colMapping:col)
      }
      def o1 = cls.newInstance(id:id1, colors:[c1, c2, c3])
      def o2 = cls.newInstance(id:id2, colors:[c4, c5, c6])

      checkCollection([o1, o2], cls, "colors.color", "'fuchsia'", "'sienna'",
                      ["'sienna'", "'yellow'"])

      checkCollection([o1, o2], cls, "colors", "<foo:bar>", "<${cid5}>", ["<${cid5}>", "<${cid2}>"])
    }
  }

  private void checkCollection(List obj, Class cls, String sel, String none, String one, List two) {
    def checker = new ResultChecker(test:this)

    doInTx { s ->
      for (o in obj)
        s.saveOrUpdate(o)
    }

    doInTx { s ->
      Results r = s.createQuery("select o from ${cls.name} o where o.${sel} = ${none};").execute()
      checker.verify(r) {
      }

      r = s.createQuery("select o from ${cls.name} o where o.${sel} = ${one};").execute()
      checker.verify(r) {
        row { object (class:cls, id:obj[1].id) }
      }

      r = s.createQuery(
        "select o from ${cls.name} o where o.${sel} = ${two[0]} or o.${sel} = ${two[1]} order by o;"
      ).execute()
      checker.verify(r) {
        row { object (class:cls, id:obj[0].id) }
        row { object (class:cls, id:obj[1].id) }
      }

      for (o in obj)
        s.delete(o)
    }
  }

  void testModels() {
    def checker = new ResultChecker(test:this)

    // set up models
    for (num in 1..3) {
      def m = new ModelConfig("m${num}", "local:///topazproject#otmtest_m${num}".toURI(), null)
      rdf.sessFactory.addModel(m);

      try { store.dropModel(m); } catch (OtmException oe) { }
      store.createModel(m)
    }

    // predicate stored with child model
    Class cls = rdf.class('Test1', model:'m1') {
      foo1 ()
      bar1 (className:'Bar11', model:'m2', inverse:true) {
        foo2 ()
        bar2 (className:'Bar21', model:'m3') {
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

    // predicate stored with parent model
    Class bcl2 = rdf.class('Bar22', model:'m3') {
      foo3 ()
    }
    Class bcl1 = rdf.class('Bar12', model:'m2') {
      foo2 ()
      bar2 (type:'Bar22', inverse:true)
    }
    Class cls1 = rdf.class('Test2', model:'m1') {
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
      blog (type:'xsd:anyURI')
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
      assert q.getParameterNames() == ['name'] as Set

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

      assert shouldFail(Exception, {
        r = q.setUri("name", "Jack".toURI()).execute()
      }).contains("is not absolute")

      // typed literal
      q = s.createQuery("select obj from Test1 obj where obj.state = :state;")
      assert q.getParameterNames() == ['state'] as Set

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

      assert shouldFail(Exception, {
        r = q.setUri("state", "Jack".toURI()).execute()
      }).contains("is not absolute")

      // uri
      q = s.createQuery("select obj from Test1 obj where obj.blog = :blog;")
      assert q.getParameterNames() == ['blog'] as Set

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

      assert shouldFail(Exception, {
        r = q.setUri("blog", "Jack".toURI()).execute()
      }).contains("is not absolute")

      // class
      q = s.createQuery("select obj from Test1 obj where obj.info = :info;")
      assert q.getParameterNames() == ['info'] as Set

      r = q.setParameter("info", o2.info.id).execute()
      checker.verify(r, warnings:true) {
        row { object (class:cls, id:o2.id) }
      }

      q = s.createQuery("select obj from Test1 obj where obj.info.id = :info;")
      assert q.getParameterNames() == ['info'] as Set

      r = q.setParameter("info", o2.info.id).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

      // no type
      q = s.createQuery("select obj from Test1 obj where obj.<topaz:info> = :info;")
      assert q.getParameterNames() == ['info'] as Set

      assert shouldFail(QueryException, {
        r = q.setParameter("info", o2.info.id.toString()).execute()
      })

      r = q.setParameter("info", o2.info.id).execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
      }

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
      assert q.getParameterNames() == ['name'] as Set

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
      assert q.getParameterNames() == ['gname', 'sname'] as Set

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
      info () {
        name (className:'Name2', extendsClass:'Name') {
          middleName ()
        }
      }
    }

    def o1 = cls.newInstance(id:"foo:1".toURI(), state:1,
                             info:[name:[givenName:'Bob', surname:'Cutter', middleName:'Fritz']])
    def o2 = cls.newInstance(id:"foo:2".toURI(), state:2,
                             info:[name:[givenName:'Jack', surname:'Keller']])
    def o3 = cls.newInstance(id:"foo:3".toURI(), state:3,
                             info:[name:[givenName:'Billy', surname:'Bob']])

    doInTx { s ->
      s.saveOrUpdate(o1)
      s.saveOrUpdate(o2)
      s.saveOrUpdate(o3)
    }

    FilterDefinition ofd1 =
        new OqlFilterDefinition('noBob', 'Test1', "o where o.info.name.givenName != 'Bob'")
    FilterDefinition ofd2 =
        new OqlFilterDefinition('state', 'Test1', 'o where o.state != :state')
    FilterDefinition ofd3 =
        new OqlFilterDefinition('noJack', 'Name', "n where n.givenName != 'Jack'")

    FilterDefinition cfd1 = new CriteriaFilterDefinition('noBob',
        new DetachedCriteria('Test1').createCriteria('info').createCriteria('name').
            add(Restrictions.ne('givenName', 'Bob')).parent.parent)
    FilterDefinition cfd2 = new CriteriaFilterDefinition('state',
        new DetachedCriteria('Test1').add(Restrictions.ne('state', new Parameter('state'))))
    FilterDefinition cfd3 = new CriteriaFilterDefinition('noJack',
        new DetachedCriteria('Name').add(Restrictions.ne('givenName', 'Jack')))

    def checker = new ResultChecker(test:this)

    // run plain filter tests
    for (t in [[ofd1, ofd2, ofd3], [cfd1, cfd2, cfd2]]) {
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
        }

        // with filter(s)
        s.enableFilter('noBob');
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
        }

        s.disableFilter('noBob');
        s.enableFilter('state').setParameter('state', 3);
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o1.id) }
          row { object (class:cls, id:o2.id) }
        }

        s.enableFilter('state').setParameter('state', 2);
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o1.id) }
          row { object (class:cls, id:o3.id) }
        }

        s.enableFilter('noBob');
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
        }

        // filter on non-root class
        s.disableFilter('state');
        assert s.enableFilter('noJack') != null;
        r = s.createQuery("select obj from Test1 obj order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o2.id) }
          row { object (class:cls, id:o3.id) }
        }

        r = s.createQuery(
            "select obj from Test1 obj where obj.info.name.id != <foo:1> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
        }

        // other constraints
        s.disableFilter('noBob');
        r = s.createQuery(
            "select obj from Test1 obj where obj.state = '3'^^<xsd:int> order by obj;").execute()
        checker.verify(r) {
          row { object (class:cls, id:o3.id) }
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
        List r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o1, o2, o3], r)

        // with filter(s)
        s.enableFilter('noBob');
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o2, o3], r)

        s.disableFilter('noBob');
        s.enableFilter('state').setParameter('state', 3);
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o1, o2], r)

        s.enableFilter('state').setParameter('state', 2);
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o1, o3], r)

        s.enableFilter('noBob');
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o3], r)

        // filter on non-root class
        s.disableFilter('state');
        assert s.enableFilter('noJack') != null;
        r = s.createCriteria(cls).addOrder(Order.asc('state')).list()
        assertEquals([o2, o3], r)

        r = s.createCriteria(cls).createCriteria('info').createCriteria('name').
              add(Restrictions.ne('id', 'foo:1'.toURI())).parent.parent.
              addOrder(Order.asc('state')).list()
        assertEquals([o3], r)

        // other constraints
        s.disableFilter('noBob');
        r = s.createCriteria(cls).add(Restrictions.eq('state', 3)).addOrder(Order.asc('state')).
              list()
        assertEquals([o3], r)

        r = s.createCriteria(cls).add(Restrictions.eq('state', 2)).addOrder(Order.asc('state')).
              list()
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
      }
      List l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o3], l)

      s.enableFilter('notBobAndNotState').getFilters('state')[0].setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o2, o3], l)

      s.disableFilter('notBobAndNotState');
      s.enableFilter('notBobOrNotState').getFilters('state')[0].setParameter('state', 2);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o1, o2, o3], l)

      s.enableFilter('notBobOrNotState').getFilters('state')[0].setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o2, o3], l)
    }

    FilterDefinition ofd4 =
        new OqlFilterDefinition('noKeller', 'Test1', "o where o.info.name.surname != 'Keller'")
    FilterDefinition cfd4 = new CriteriaFilterDefinition('noKeller',
        new DetachedCriteria('Test1').createCriteria('info').createCriteria('name').
            add(Restrictions.ne('surname', 'Keller')).parent.parent)

    FilterDefinition jfd3 =
        new ConjunctiveFilterDefinition('notBobAnd_notStateOrNotKeller', 'Test1').
            addFilterDefinition(ofd1).addFilterDefinition(
                new DisjunctiveFilterDefinition('notStateOrNotKeller', 'Test1').
                    addFilterDefinition(cfd2).addFilterDefinition(ofd4));

    FilterDefinition jfd4 =
        new DisjunctiveFilterDefinition('notStateOr_notBobAndNotKeller', 'Test1').
            addFilterDefinition(ofd2).addFilterDefinition(
                new ConjunctiveFilterDefinition('notBobAndNotKeller', 'Test1').
                    addFilterDefinition(cfd1).addFilterDefinition(cfd4));

    rdf.sessFactory.addFilterDefinition(jfd3);
    rdf.sessFactory.addFilterDefinition(jfd4);

    doInTx { s ->
      Filter stF = s.enableFilter('notBobAnd_notStateOrNotKeller').
                     getFilters('notStateOrNotKeller')[0].getFilters('state')[0];
      stF.setParameter('state', 2);
      Results r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o3.id) }
      }
      List l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o3], l)

      stF.setParameter('state', 3);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o2, o3], l)

      s.disableFilter('notBobAnd_notStateOrNotKeller');
      stF = s.enableFilter('notStateOr_notBobAndNotKeller').getFilters('state')[0];
      stF.setParameter('state', 1);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o2, o3], l)

      stF.setParameter('state', 2);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o1, o3], l)

      stF.setParameter('state', 3);
      r = s.createQuery("select obj from Test1 obj order by obj;").execute()
      checker.verify(r) {
        row { object (class:cls, id:o1.id) }
        row { object (class:cls, id:o2.id) }
        row { object (class:cls, id:o3.id) }
      }
      l = s.createCriteria(cls).addOrder(Order.asc('state')).list()
      assertEquals([o1, o2, o3], l)
    }
  }

  void testFilterDefs() {
    doInTx { s ->
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
          add(Restrictions.eq("uri", new URI("foo:bar")))

      assert dc.getParameterNames().size() == 1;
      assert dc.getParameterNames().iterator().next() == "auth"

      FilterDefinition cfd = new CriteriaFilterDefinition("critF", dc);
      assert cfd.createFilter(s).setParameter('auth', 'blah').getQuery().toString() == "select o from Article o where ((o.title = 'foo' or o.authors = 'blah')) and (v1 := o.parts and ((v1.date != '2007-07-08Z'^^<http://www.w3.org/2001/XMLSchema#date>) and ((le(v1.state, '2'^^<http://www.w3.org/2001/XMLSchema#int>) and gt(v1.rights, 'none'^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>))) and (v22 := v1.nextObject and ((v22.dc_type = <dc:type>) and (v22.uri = <foo:bar>)))));"

      def qry = """o where
        (o.title = 'foo' or o.authors = :auth) and o.nextObject.uri = <foo:bar> and
        o.nextObject.dc_type = <dc:type> and p := o.parts and q := p.nextObject and
        (x := :x and (q.date = '2007' or q.rights = x and le(q.dc_type, <x:y>)))""";
      FilterDefinition ofd = new OqlFilterDefinition("oqlF", "Article", qry)
      Criteria c = ofd.createFilter(s).setParameter('auth', 'blah').getCriteria();

      assert c.criterionList.size() == 1
      assert c.criterionList[0] instanceof Disjunction
      assert c.criterionList[0].criterions.size() == 2
      assert c.criterionList[0].criterions[0] instanceof EQCriterion
      assert c.criterionList[0].criterions[1] instanceof EQCriterion
      assert c.criterionList[0].criterions[0].fieldName == 'title'
      assert c.criterionList[0].criterions[0].value == 'foo'
      assert c.criterionList[0].criterions[1].fieldName == 'authors'
      assert c.criterionList[0].criterions[1].value instanceof Parameter
      assert c.criterionList[0].criterions[1].value.parameterName == 'auth'

      assert c.children.size() == 3
      assert c.children[0].mapping.name == 'parts'
      assert c.children[1].mapping.name == 'nextObject'
      assert c.children[2].mapping.name == 'nextObject'
      assert c.children[0].children.size() == 1
      assert c.children[1].children.size() == 0
      assert c.children[2].children.size() == 0
      assert c.children[0].criterionList.size() == 0
      assert c.children[1].criterionList.size() == 1
      assert c.children[2].criterionList.size() == 1
      assert c.children[1].criterionList[0] instanceof EQCriterion
      assert c.children[2].criterionList[0] instanceof EQCriterion
      assert c.children[1].criterionList[0].fieldName == 'uri'
      assert c.children[1].criterionList[0].value == 'foo:bar'.toURI()
      assert c.children[2].criterionList[0].fieldName == 'dc_type'
      assert c.children[2].criterionList[0].value == 'dc:type'.toURI()
      c = c.children[0].children[0]
      assert c.mapping.name == 'nextObject'
      assert c.children.size() == 0
      assert c.criterionList.size() == 1
      assert c.criterionList[0] instanceof Disjunction
      assert c.criterionList[0].criterions.size() == 2
      assert c.criterionList[0].criterions[0] instanceof EQCriterion
      assert c.criterionList[0].criterions[1] instanceof Conjunction
      assert c.criterionList[0].criterions[0].fieldName == 'date'
      assert c.criterionList[0].criterions[0].value == '2007'
      assert c.criterionList[0].criterions[1].criterions.size() == 2
      assert c.criterionList[0].criterions[1].criterions[0] instanceof EQCriterion
      assert c.criterionList[0].criterions[1].criterions[1] instanceof ProxyCriterion
      assert c.criterionList[0].criterions[1].criterions[0].fieldName == 'rights'
      assert c.criterionList[0].criterions[1].criterions[0].value instanceof Parameter
      assert c.criterionList[0].criterions[1].criterions[0].value.parameterName == 'x'
      assert c.criterionList[0].criterions[1].criterions[1].function == 'le'
      assert c.criterionList[0].criterions[1].criterions[1].arguments.length == 2
      assert c.criterionList[0].criterions[1].criterions[1].arguments[0] == 'dc_type'
      assert c.criterionList[0].criterions[1].criterions[1].arguments[1] == 'x:y'.toURI()
    }
  }

  private def doInTx(Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      def r = c(s)
      s.transaction.commit()
      return r
    } catch (OtmException e) {
      try {
        s.transaction.rollback()
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
      log.error("error: ${e}", e)
      throw e
    } finally {
      try {
        s.close();
      } catch (OtmException oe) {
        log.warn("close failed", oe);
      }
    }
  }
}

class ResultChecker extends BuilderSupport {
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
          test.assertNotNull(res.warnings);
        else {
          if (res.warnings)
            test.log.error "Got warnings: " +res.warnings.join(System.getProperty("line.separator"))
          test.assertNull(res.warnings);
        }
        break;

      case 'row':
        test.assertTrue(res.next())
        col = 0;
        break;

      case 'object':
        def o = res.get(col++);
        for (a in attributes) {
          if (a.key == 'class')
            test.assertTrue(a.value.isAssignableFrom(o."${a.key}"))
          else
            test.assertEquals(a.value, o."${a.key}")
        }
        break;

      case 'string':
        test.assertEquals(value, res.getString(col++));
        break;

      case 'uri':
        test.assertEquals((value instanceof URI) ? value : value.toURI(), res.getURI(col++));
        break;

      case 'nul':
        test.assertNull(res.get(col++));
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
    if (parent == null || node == 'subq')
      test.assertFalse(res.next())

    if (node == 'subq') {
      res = resHist.pop()
      col = colHist.pop()
    }
  }
}
