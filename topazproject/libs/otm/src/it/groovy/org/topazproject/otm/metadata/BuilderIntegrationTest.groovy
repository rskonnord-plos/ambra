/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.metadata;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Integration tests for groovy-builder.
 */
public class BuilderIntegrationTest extends GroovyTestCase {
  def rdf;
  def store;

  void setUp() {
    store = new ItqlStore("http://localhost:9091/mulgara-service/services/ItqlBeanService".toURI())
    rdf = new RdfBuilder(
        sessFactory:new SessionFactory(tripleStore:store), defModel:'ri', defUriPrefix:'topaz:')

    def ri = new ModelConfig("ri", "local:///topazproject#otmtest1".toURI(), null)
    rdf.sessFactory.addModel(ri);

    try {
      store.dropModel(ri)
    } catch (Throwable t) {
    }
    store.createModel(ri)
  }

  void testSimple() {
    Class t1 = rdf.class("Test1", uriPrefix:'http://rdf.topazproject.org/RDF/') {
      uri   (isId:true)
      state (pred:'accountState', type:'xsd:int')
      name {
        givenName () 'Peter'
        surname   ()
      }
      goals (maxCard:-1, colMapping:'RdfBag')
    }

    def i1 = t1.newInstance(uri:'foo:1', state:1,
                            name:[id:'foo:n1'.toURI(), givenName:'John', surname:'Muir'],
                            goals:['one', 'two'])

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == i1 }

    i1 = t1.newInstance(uri:'foo:1', state:1, name:[id:'foo:n1'.toURI()], goals:['one', 'two'])

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == i1 }

    doInTx { s -> s.delete(i1) }
    doInTx { s -> assert s.get(t1, "foo:1") == null }
  }

  void testDatatypes() {
    Class cls = rdf.class('Test1') {
      b_bool   (type:'xsd:boolean')

      n_byte   (type:'xsd:byte')
      n_short  (type:'xsd:short')
      n_int    (type:'xsd:int')
      n_long   (type:'xsd:long')
      n_float  (type:'xsd:float')
      n_double (type:'xsd:double')

      l_date   (type:'xsd:date', javaType:Long.class)
      l_time   (type:'xsd:time', javaType:Long.class)
      l_datTim (type:'xsd:dateTime', javaType:Long.class)

      s_untypd ()
      s_string (type:'xsd:string')
      s_xmllit (type:'rdf:XMLLiteral')
      s_uri    (type:'rdf:anyURI', javaType:String.class)

      u_uri    (type:'xsd:anyURI')
      u_url    (type:'xsd:anyURI', javaType:URL.class)

      d_date   (type:'xsd:date')
      d_time   (type:'xsd:time')
      d_datTim (type:'xsd:dateTime')
    }

    def obj = cls.newInstance(b_bool:true, n_byte:42, n_short:4242, n_int:42424242,
                              n_long:4242424242424242L, n_float:1.42, n_double:1.3377223993,
                              s_untypd:'hello', s_string:'bye', s_xmllit:'<title>foobar</title>',
                              s_uri:'foo:bar/baz',
                              u_uri:'bar:blah/blah'.toURI(), u_url:'http://bar/baz'.toURL(),
                              d_date:new Date('Oct 23 2006'),
                              d_time:new Date('Jan 1 1970 11:42:34'),
                              d_datTim:new Date('Jan 12 1999 11:42:34'), 
                              l_date:new Date('Oct 23 2006').time,
                              l_time:new Date('Jan 1 1970 11:42:34').time,
                              l_datTim:new Date('Jan 12 1999 11:42:34').time )

    doInTx { s-> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals obj, s.get(cls, obj.id.toString()) }

    Class cls2 = rdf.class('Test2', uriPrefix:'http://rdf.topazproject.org/RDF/', type:"foo:bar") {
      ser (pred:'date')
    }
    Class cls3 = rdf.class('Test3', uriPrefix:'http://rdf.topazproject.org/RDF/', type:"foo:bar") {
      date (pred:'date', type:'xsd:date')
    }

    def obj2 = cls2.newInstance(id:'foo:1'.toURI(), ser:'2006-12-20')
    doInTx { s-> s.saveOrUpdate(obj2) }
    doInTx { s -> assertEquals new Date('Dec 20 2006'), s.get(cls3, obj2.id.toString()).date }

    obj2 = cls2.newInstance(id:'foo:2'.toURI(), ser:'2006-12-20Z')
    doInTx { s-> s.saveOrUpdate(obj2) }
    doInTx { s -> assertEquals new Date('Dec 20 2006 GMT'), s.get(cls3, obj2.id.toString()).date }
  }

  void testIdGenerator() {
    // default gen
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
    }

    def obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id != null

    // explicit gen
    cls = rdf.class('Test2', idGenerator:'GUID') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id != null

    // no gen
    cls = rdf.class('Test3', idGenerator:null) {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx { s -> s.saveOrUpdate(obj) } })

    // inherited (no) gen
    Class base = rdf.class('Base4', idGenerator:null) {
    }

    cls = rdf.class('Test4', extendsClass:'Base4') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assert obj.id == 'foo:1'.toURI()

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx { s -> s.saveOrUpdate(obj) } })
  }

  void testModels() {
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

    def obj = cls.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1')
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[]])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

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

    obj = cls.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1')
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[]])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == obj }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assert s.get(cls, obj.id.toString()) == null }
  }

  void testMostSpecificSubClass() {
    Class base = rdf.class("BaseClass", type:'base:type') {
      uri   (isId:true)
    }

    Class wrong = rdf.class("WrongOne", extendsClass:"BaseClass") {
      name 'Test2'
    }
    
    Class right = rdf.class("RightOne", type:'sub:type', extendsClass:"BaseClass") {
    }
 
    Class p = rdf.sessFactory.mostSpecificSubClass(base, ['base:type', 'sub:type'])
    assert right == p
    
    p = rdf.sessFactory.mostSpecificSubClass(wrong, ['base:type', 'sub:type'])
    assert wrong == p;
    
    p = rdf.sessFactory.mostSpecificSubClass(right, ['base:type', 'sub:type'])
    assert right == p;

    p = rdf.sessFactory.mostSpecificSubClass(wrong, ['base:type'])
    assert wrong == p;

    p = rdf.sessFactory.mostSpecificSubClass(right, ['base:type'])
    assert null == p;

    p = rdf.sessFactory.mostSpecificSubClass(base, ['base:type'])
    assert wrong == p;

    p = rdf.sessFactory.mostSpecificSubClass(base, ['junk:type'])
    assert null == p;

    p = rdf.sessFactory.mostSpecificSubClass(base, [])
    assert base == p;
  }

  private def doInTx(Closure c) {
    Session s = rdf.sessFactory.openSession()
    s.beginTransaction()
    try {
      c(s)
    } finally {
      s.transaction.commit()
      s.close()
    }
  }
}
