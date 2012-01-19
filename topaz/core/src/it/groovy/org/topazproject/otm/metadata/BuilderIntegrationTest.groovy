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

package org.topazproject.otm.metadata;

import org.topazproject.otm.AbstractTest;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;

/**
 * Integration tests for groovy-builder.
 */
public class BuilderIntegrationTest extends AbstractTest {
  void setUp() {
    graphs = [['ri', 'otmtest1', null], ['m2', 'otmtest2', null]];
    super.setUp();
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
    doInTx { s -> assertEquals(i1, s.get(t1, "foo:1")) }

    i1 = t1.newInstance(uri:'foo:1', state:1, name:[id:'foo:n1'.toURI()], goals:['one', 'two'])

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assertEquals(i1, s.get(t1, "foo:1")) }

    doInTx { s -> s.delete(i1) }
    doInTx { s -> assertNull(s.get(t1, "foo:1")) }
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
    assertEquals('foo:1'.toURI(), obj.id)

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assertNotNull(obj.id)

    // explicit gen
    cls = rdf.class('Test2', idGenerator:'GUID') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assertEquals('foo:1'.toURI(), obj.id)

    obj = cls.newInstance(state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assertNotNull(obj.id)

    // no gen
    cls = rdf.class('Test3', idGenerator:null) {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assertEquals('foo:1'.toURI(), obj.id)

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx(true) { s -> s.saveOrUpdate(obj) } })

    // inherited (no) gen
    Class base = rdf.class('Base4', idGenerator:null) {
    }

    cls = rdf.class('Test4', extendsClass:'Base4') {
      state (type:'xsd:int')
    }

    obj = cls.newInstance(id:'foo:1'.toURI(), state:42)
    doInTx { s -> s.saveOrUpdate(obj) }
    assertEquals('foo:1'.toURI(), obj.id)

    obj = cls.newInstance(state:42)
    shouldFail(OtmException, { doInTx(true) { s -> s.saveOrUpdate(obj) } })
  }

  void testGraphs() {
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
      bar1 (className:'Bar11', graph:'m2', inverse:true, fetch:'eager') {
        foo2 ()
        bar2 (className:'Bar21', graph:'m3', fetch:'eager') {
          foo3 ()
        }
      }
    }

    def obj = cls.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1')
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[]])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

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

    obj = cls.newInstance(foo1:'f1', bar1:[foo2:'f2', bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1')
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[]])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }

    obj = cls.newInstance(foo1:'f1', bar1:[bar2:[foo3:'f3']])
    doInTx { s -> s.saveOrUpdate(obj) }
    doInTx { s -> assertEquals(obj, s.get(cls, obj.id.toString())) }

    doInTx { s -> s.delete(obj) }
    doInTx { s -> assertNull(s.get(cls, obj.id.toString())) }
  }

  void testCollTypeLookAhead() {
    def mc = 0
    for (graph in ['ri', 'm2']) {
      def type = 'foo:Assoc' + mc
      Class ass = rdf.class('Assoc' + mc, type:type, graph:graph) {
        label()
      }

      def cnt = mc * 10
      for (colType in ['Predicate', 'RdfList', 'RdfBag', 'RdfSeq', 'RdfAlt']) {
        Class cls = rdf.class('Test' + cnt, type:'foo:Test' + cnt) {
          col (pred:'foo:p1', type:'foo:Test' + cnt, colMapping: colType, maxCard:-1
                                ,cascade:['child'])
          lis (pred:'foo:p2', type:type, colMapping: colType, maxCard:-1
                                ,cascade:['child'])
          label()
        }

        def obj = cls.newInstance(id:'foo:obj'.toURI(), label:'obj')
        def col = cls.newInstance(id:'foo:col'.toURI(), label:'col')
        def lis = ass.newInstance(id:'foo:lis'.toURI(), label:'lis')
        obj.col = [col]
        obj.lis = [lis]

        doInTx { s-> s.saveOrUpdate(obj) }

        doInTx { s ->
          def o = s.get(cls, 'foo:obj')
          def c = s.get(cls, 'foo:col')
          def l = s.get(ass, 'foo:lis')
          assertNotNull o
          assertNotNull c
          assertNotNull l
          assertEquals('obj', o.label)
          assertEquals('col', c.label)
          assertEquals('lis', l.label)
          assertNotNull o.col
          assertEquals(1, o.col.size())
          assertEquals(c, o.col[0])
          assertNotNull o.lis
          assertEquals(1, o.lis.size())
          assertEquals(l, o.lis[0])
          s.delete(o)
          assertNull s.get(cls, 'foo:obj')
          assertNull s.get(cls, 'foo:col')
          assertNull s.get(ass, 'foo:lis')
        }

        doInTx { s ->
          assertNull s.get(cls, 'foo:obj')
          assertNull s.get(cls, 'foo:col')
          assertNull s.get(ass, 'foo:lis')
        }

        cnt++
      }
      mc++
    }
  }

  void testCascade() {
    Class cls = rdf.class('Test1', type:'foo:Test1') {
      sel  (pred:'foo:p1', type:'foo:Test1', cascade:['saveOrUpdate'])
      all  (pred:'foo:p2', type:'foo:Test1', cascade:['child'])
      none (pred:'foo:p3', type:'foo:Test1', cascade:[])
    }

    def obj = cls.newInstance(id:'foo:obj'.toURI())
    def sel = cls.newInstance(id:'foo:sel'.toURI())
    def all = cls.newInstance(id:'foo:all'.toURI())
    def none = cls.newInstance(id:'foo:none'.toURI())
    obj.sel = sel
    obj.all = all
    obj.none = none

    doInTx { s-> s.saveOrUpdate(obj) }

    doInTx { s ->
      assertNotNull s.get(cls, 'foo:sel')
      assertNotNull s.get(cls, 'foo:all')
      assertNull s.get(cls, 'foo:none')
      def o = s.get(cls, 'foo:obj')
      assertNotNull o.sel
      assertNotNull o.all
      assertNull o.none
      o.none = none
      s.saveOrUpdate(o)
      s.saveOrUpdate(none)
    }

    doInTx { s ->
      assertNotNull s.get(cls, 'foo:sel')
      assertNotNull s.get(cls, 'foo:all')
      assertNotNull s.get(cls, 'foo:none')
      def o = s.get(cls, 'foo:obj')
      assertNotNull o.sel
      assertNotNull o.all
      assertNotNull o.none
      o.sel = null
      o.all = null
      o.none = null
      s.saveOrUpdate(o)
    }

    doInTx { s ->
      assertNotNull s.get(cls, 'foo:sel')
      assertNull s.get(cls, 'foo:all')
      assertNotNull s.get(cls, 'foo:none')
      def o = s.get(cls, 'foo:obj')
      assertNull o.sel
      assertNull o.all
      assertNull o.none
      o.sel = s.get(cls, 'foo:sel')
      o.all = cls.newInstance(id:'foo:all'.toURI())
      o.none = s.get(cls, 'foo:none')
      s.saveOrUpdate(o)
    }

    doInTx { s ->
      assertNotNull s.get(cls, 'foo:sel')
      assertNotNull s.get(cls, 'foo:all')
      assertNotNull s.get(cls, 'foo:none')
      def o = s.get(cls, 'foo:obj')
      assertNotNull o.sel
      assertNotNull o.all
      assertNotNull o.none
      s.delete(o)
    }

    doInTx { s ->
      assertNotNull s.get(cls, 'foo:sel')
      assertNull s.get(cls, 'foo:all')
      assertNotNull s.get(cls, 'foo:none')
      assertNull s.get(cls, 'foo:obj')
    }
  }

  void testEagerLoad() {
    Class cls = rdf.class('Test1', type:'foo:Test1') {
      sel  (pred:'foo:p1', type:'foo:Test1', fetch:'eager')
    }

    def obj = cls.newInstance(id:'foo:obj'.toURI())
    def sel = cls.newInstance(id:'foo:sel'.toURI())
    obj.sel = sel
    sel.sel = obj
    doInTx { s-> s.saveOrUpdate(obj) }
    doInTx { s ->
      obj = s.get(cls, 'foo:sel')
    }
    assertNotNull obj
    sel = obj.sel
    assertNotNull sel
    assertEquals(obj, sel.sel)
  }

  void testBlobs() {
    Class t1 = rdf.class("Test1") {
      uri   (isId:true)
      state (type:'xsd:int')
      data  (isBlob:true)
    }

    def i1 = t1.newInstance(uri:'foo:1', state:1, data:"hello".getBytes())

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assertEquals(i1, s.get(t1, "foo:1")) }

    i1 = t1.newInstance(uri:'foo:1', state:2, data:"bye".getBytes())

    doInTx { s -> s.saveOrUpdate(i1) }
    doInTx { s -> assertEquals(i1, s.get(t1, "foo:1")) }

    doInTx { s -> s.delete(i1) }
    doInTx { s -> assertNull(s.get(t1, "foo:1")) }
  }
}
