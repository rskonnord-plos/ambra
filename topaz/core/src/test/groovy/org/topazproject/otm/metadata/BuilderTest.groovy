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

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.java.FieldBinder;

/**
 * Groovy-builder offline tests.
 */
public class BuilderTest extends GroovyTestCase {
  def rdf

  void setUp() {
    rdf = new RdfBuilder(defModel:'ri', defUriPrefix:'topaz:')
  }

  void testBasic() {
    Class cls = rdf.class('Test1', uriPrefix:'http://rdf.topazproject.org/RDF/') {
      uri   (isId:true)
      state (pred:'accountState', type:'xsd:int')
      name {
        givenName () 'Peter'
        surname   ()
      }
      goals (maxCard:-1, colType:'Set', colMapping:'RdfBag')
    }

    def obj = cls.newInstance(uri:'foo:1', state:1,
                              name:[id:'foo:n1'.toURI(), givenName:'John', surname:'Muir'],
                              goals:['one', 'two'] as Set)

    obj = cls.newInstance(uri:'foo:1', state:1, name:[id:'foo:n1'.toURI()],
                          goals:['one', 'two'] as Set)
    assertEquals(obj.name.class.newInstance(id:'foo:n1'.toURI(), givenName:'Peter'), obj.name)

    assert shouldFail(OtmException, {
      rdf.class() {
        state ()
      }
    }).contains('at least a class-name is required')

    assert shouldFail(OtmException, {
      rdf.class() {
        state ()
      }
    }).contains('at least a class-name is required')
  }

  void testFields() {
    // all defaults, untyped literal
    Class cls = rdf.class('Test1') {
      state ()
    }
    ClassMetadata cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('http://rdf.topazproject.org/RDF/Test1', cm.type)
    assertEquals('ri', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    def m = cm.rdfMappers.iterator().next()
    def l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('http://rdf.topazproject.org/RDF/state', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    // relative-uri overrides, typed literal
    cls = rdf.class('Test2', type:'Test2', model:'m2') {
      state (pred:'p2', type:'xsd:int')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('http://rdf.topazproject.org/RDF/Test2', cm.type)
    assertEquals('m2', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(Integer.TYPE, l.type)
    assertEquals('http://www.w3.org/2001/XMLSchema#int', m.dataType)
    assertEquals('http://rdf.topazproject.org/RDF/p2', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    // absolute-uri overrides, class type
    Class cls2 = cls
    cls = rdf.class('Test3', type:'foo:Test3', model:'m3') {
      state (pred:'foo:p3', type:'Test2')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('foo:Test3', cm.type)
    assertEquals('m3', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(cls2, l.type)
    assertEquals(null, m.dataType)
    assertEquals('foo:p3', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    // nested class type
    cls = rdf.class('Test4', type:'foo:Test4', model:'m4') {
      state (pred:'foo:p4', model:'m41', uriPrefix:'bar4:') {
        value (model:'m42')
        history (maxCard:-1) {
          value ()
        }
      }
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('foo:Test4', cm.type)
    assertEquals('m4', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(rdf.sessFactory.getClassMetadata('State').getEntityBinder(EntityMode.POJO).
                 sourceClass, l.type)
    assertNull(m.dataType)
    assertEquals('foo:p4', m.uri)
    assertFalse(m.hasInverseUri())
    assertEquals('m41', m.model)

    cm = rdf.sessFactory.getClassMetadata('State')

    assertEquals('bar4:State', cm.type)
    assertEquals('m41', cm.model)
    assertEquals(2, cm.rdfMappers.size())

    m = cm.rdfMappers.asList()[0]
    l = m.getBinder(EntityMode.POJO)
    assertEquals('value', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('bar4:value', m.uri)
    assertFalse(m.hasInverseUri())
    assertEquals('m42', m.model)

    m = cm.rdfMappers.asList()[1]
    l = m.getBinder(EntityMode.POJO)
    assertEquals('history', m.name)
    assertEquals(List.class, l.type)
    assertEquals(rdf.sessFactory.getClassMetadata('History').getEntityBinder(EntityMode.POJO).
                 sourceClass, l.componentType)
    assertNull(m.dataType)
    assertEquals('bar4:history', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    cm = rdf.sessFactory.getClassMetadata('History')

    assertEquals('bar4:History', cm.type)
    assertEquals('m41', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.asList()[0]
    l = m.getBinder(EntityMode.POJO)
    assertEquals('value', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('bar4:value', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    // uri type
    cls = rdf.class('Test5', type:'foo:Test5', model:'m5') {
      state (pred:'foo:p5', type:'xsd:anyURI', inverse:true)
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('foo:Test5', cm.type)
    assertEquals('m5', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(URI.class, l.type)
    assertTrue(m.typeIsUri())
    assertEquals('foo:p5', m.uri)
    assertTrue(m.hasInverseUri())
    assertNull(m.model)

    // no default prefix defined
    rdf.defUriPrefix = null
    cls = rdf.class('Test6', uriPrefix:'p6:') {
      state (type:'xsd:anyURI')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('p6:Test6', cm.type)
    assertEquals('ri', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(URI.class, l.type)
    assertEquals(Rdf.xsd + 'anyURI', m.dataType)
    assertEquals('p6:state', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    // no prefix defined, default type
    assert shouldFail(OtmException, {
      rdf.class('Test7') {
        state (pred:'foo:state')
      }
    }).contains('no uri-prefix has been configured')

    // no prefix defined, simple type
    assert shouldFail(OtmException, {
      rdf.class('Test8', type:'Test8') {
        state (pred:'foo:state')
      }
    }).contains('no uri-prefix has been configured')

    assert shouldFail(OtmException, {
      rdf.class('Test9', type:'foo:Test9') {
        state (type:'anyURI', pred:'foo:state')
      }
    }).contains('no uri-prefix has been configured')

    // no model defined
    rdf.defModel = null
    assert shouldFail(OtmException, {
      rdf.class('foo:Test10', uriPrefix:'p6:') {
        state ()
      }
    }).contains('No model has been set')

    // explicit null type
    rdf.defUriPrefix = 'topaz:'
    rdf.defModel     = 'ri'
    cls = rdf.class('Test11', type:null) {
      state ()
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertNull(cm.type)
    assertEquals('ri', cm.model)
    assertEquals(1, cm.rdfMappers.size())

    m = cm.rdfMappers.iterator().next()
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('http://rdf.topazproject.org/RDF/state', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    Class sup = rdf.class('Test12') {
      state ()
    }
    cls = rdf.class('Test13', type:null, extendsClass:'Test12') {
      blah ()
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertNull(cm.type)
    assertEquals('ri', cm.model)
    assertEquals(2, cm.rdfMappers.size())

    m = cm.rdfMappers.asList()[0]
    l = m.getBinder(EntityMode.POJO)
    assertEquals('blah', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('http://rdf.topazproject.org/RDF/blah', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)

    m = cm.rdfMappers.asList()[1]
    l = m.getBinder(EntityMode.POJO)
    assertEquals('state', m.name)
    assertEquals(String.class, l.type)
    assertNull(m.dataType)
    assertEquals('http://rdf.topazproject.org/RDF/state', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)
  }

  void testDatatypes() {
    // test valid
    int cnt = 0
    for (t in [['xsd:string', String.class, 'hello'], ['xsd:anyURI', URI.class, 'a:hello'.toURI()],
               ['xsd:byte', Byte.TYPE, 42], ['xsd:short', Short.TYPE, 4242],
               ['xsd:int', Integer.TYPE, 42424242], ['xsd:long', Long.TYPE, 424242424242424242L],
               ['xsd:float', Float.TYPE, 1.42], ['xsd:double', Double.TYPE, 1.3333333333333],
               ['xsd:boolean', Boolean.TYPE, true], ['xsd:date', Date.class, new Date()],
               ['xsd:time', Date.class, new Date()], ['xsd:dateTime', Date.class, new Date()],
               ['rdf:XMLLiteral', String.class, '<title>A Fine Day</title>']]) {
      Class cls = rdf.class('Test' + cnt++) {
        foo (type:t[0])
      }

      def obj = cls.newInstance(id:'foo:1'.toURI(), foo:t[2])
      assertEquals(t[1], obj.class.getDeclaredField('foo').type)
    }

    // test invalid
    assert shouldFail(OtmException, {
      rdf.class('Test' + cnt++) {
        foo (type:'xsd:NonNegativeInteger')
      }
    }).contains('Unsupported xsd type')
  }

  void testDefaultValues() {
    // non-collections
    int cnt = 0
    for (t in [['xsd:string', 'hello', 'hello'], ['xsd:byte', 42, 42], ['xsd:short', 4242, 4242],
               ['xsd:int', 42424242, 42424242],
               ['xsd:long', 424242424242424242L, 424242424242424242L], ['xsd:float', 1.42f, 1.42f],
               ['xsd:double', 1.3333333333333d, 1.3333333333333d], ['xsd:boolean', true, true],
               ['xsd:anyURI', 'a:hello'.toURI(), 'a:hello'.toURI()],
               ['xsd:anyURI', 'a:hello', 'a:hello'.toURI()],
               ['xsd:dateTime', 'Jan 12 11:42:34 1999', new Date('Jan 12 11:42:34 1999')],
               ['xsd:dateTime', new Date('Jan 12 1999 11:42:34'), new Date('Jan 12 1999 11:42:34')],
               ['xsd:dateTime', 1178494705000L, new Date('Sun May 06 16:38:25 PDT 2007')],
               ['xsd:date', 'Jan 12 1999', new Date('Jan 12 1999')],
               ['xsd:date', new Date('Jan 12 1999'), new Date('Jan 12 1999')],
               ['xsd:date', 1178494705000L, new Date('Sun May 06 16:38:25 PDT 2007')],
               ['xsd:time', 'Jan 12 1999 11:42:34', new Date('Jan 12 1999 11:42:34')],
               ['xsd:time', new Date('Jan 12 1999 11:42:34'), new Date('Jan 12 1999 11:42:34')],
               ['xsd:time', 1178494705000L, new Date('Sun May 06 16:38:25 PDT 2007')]]) {
      Class cls = rdf.class('Test' + cnt++) {
        foo (type:t[0]) t[1]
      }

      def obj = cls.newInstance()
      assertEquals(t[2], obj.foo)
    }

    // lists
    for (t in [['xsd:string', 'hello', ['hello']],
               ['xsd:string', ['hello', 'bye'], ['hello', 'bye']],
               ['xsd:boolean', true, [true]],
               ['xsd:boolean', [true, false], [true, false]],
               ['xsd:byte', 42, [42]],
               ['xsd:byte', [124, 42], [124, 42]],
               ['xsd:short', 4242, [4242]],
               ['xsd:short', [12433, 4242], [12433, 4242]],
               ['xsd:int', 42424242, [42424242]],
               ['xsd:int', [124336789, 42424242], [124336789, 42424242]],
               ['xsd:long', 4242424242424242L, [4242424242424242L]],
               ['xsd:long', [124336789876L, 4242424242424242L], [124336789876L, 4242424242424242L]],
               ['xsd:float', 1.74f, [1.74f]],
               ['xsd:float', [3.28f, 1.74f], [3.28f, 1.74f]],
               ['xsd:double', 1.3333333333333d, [1.3333333333333d]],
               ['xsd:double', [3.28773d, 1.3333333333333d], [3.28773d, 1.3333333333333d]],
               ['xsd:anyURI', 'a:hello'.toURI(), ['a:hello'.toURI()]],
               ['xsd:anyURI', ['a:hello'.toURI(), 'b:bye'.toURI()],
                              ['a:hello'.toURI(), 'b:bye'.toURI()]],
               ['xsd:dateTime', 'Jan 12 11:42:34 1999', [new Date('Jan 12 11:42:34 1999')]],
               ['xsd:dateTime', ['Jan 12 11:42:34 1999', 'Aug 23 10:43:33 2001'],
                        [new Date('Jan 12 11:42:34 1999'), new Date('Aug 23 10:43:33 2001')]] ]) {
      Class cls = rdf.class('Test' + cnt++) {
        foo (type:t[0], maxCard:-1) t[1]
      }

      def obj = cls.newInstance()
      assertEquals(t[2], obj.foo)
    }

    // sets
    for (t in [['xsd:string', 'hello', ['hello'] as Set],
               ['xsd:string', ['hello', 'bye'], ['hello', 'bye'] as Set],
               ['xsd:string', ['hello', 'bye'] as Set, ['hello', 'bye'] as Set],
               ['xsd:short', 4242, [4242] as Set],
               ['xsd:short', [12433, 4242], [12433, 4242] as Set],
               ['xsd:short', [12433, 4242] as Set, [12433, 4242] as Set],
               ['xsd:anyURI', 'a:hello'.toURI(), ['a:hello'.toURI()] as Set],
               ['xsd:anyURI', ['a:hello'.toURI(), 'b:bye'.toURI()],
                              ['a:hello'.toURI(), 'b:bye'.toURI()] as Set],
               ['xsd:anyURI', ['a:hello'.toURI(), 'b:bye'.toURI()] as Set,
                              ['a:hello'.toURI(), 'b:bye'.toURI()] as Set] ]) {
      Class cls = rdf.class('Test' + cnt++) {
        foo (type:t[0], maxCard:-1, colType:'Set') t[1]
      }

      def obj = cls.newInstance()
      assertEquals(t[2], obj.foo)
    }

    // arrays
    for (t in [['xsd:string', 'hello', ['hello'] as String[]],
               ['xsd:string', ['hello', 'bye'], ['hello', 'bye'] as String[]],
               ['xsd:short', 4242, [4242] as short[]],
               ['xsd:short', [12433, 4242], [12433, 4242] as short[]],
               ['xsd:anyURI', 'a:hello'.toURI(), ['a:hello'.toURI()] as URI[]],
               ['xsd:anyURI', ['a:hello'.toURI(), 'b:bye'.toURI()],
                              ['a:hello'.toURI(), 'b:bye'.toURI()] as URI[]] ]) {
      Class cls = rdf.class('Test' + cnt++) {
        foo (type:t[0], maxCard:-1, colType:'Array') t[1]
      }

      def obj = cls.newInstance()
      assertArrayEquals(t[2], obj.foo)
    }
  }

  protected assertArrayEquals(short[] expected, short[] value) {
    String message = "expected array: " + expected + " value array: " + value
    assertNotNull(message + ": expected should not be null", expected)
    assertNotNull(message + ": value should not be null", value)
    assertEquals(message, expected.length, value.length)
    for (int i in 0 ..< expected.length)
      assertEquals("value[" + i + "] when " + message, expected[i], value[i])
  }

  void testCollections() {
    // Set, RdfBag
    Class cls = rdf.class('Test1') {
      goals (maxCard:-1, colType:'Set', colMapping:'RdfBag')
    }

    def obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'] as Set)
    assert obj.goals instanceof Set : obj.goals

    // List, RdfSeq
    cls = rdf.class('Test2') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfSeq')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof List : obj.goals

    // List, RdfList
    cls = rdf.class('Test3') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfList')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof List : obj.goals

    // String[], RdfAlt
    cls = rdf.class('Test4') {
      goals (maxCard:-1, colType:'Array', colMapping:'RdfAlt')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof String[] : obj.goals

    // int[], predicate
    cls = rdf.class('Test5') {
      goals (maxCard:-1, colType:'Array', type:'xsd:int', colMapping:'Predicate')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:[1, 2])
    assert obj.goals instanceof int[] : obj.goals

    // illegal collection type
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test6') {
        goals (maxCard:-1, colType:'Foo', colMapping:'Predicate')
      }
    }).contains('Unknown collection type')

    // illegal collection mapping
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test7') {
        goals (maxCard:-1, colType:'List', colMapping:'cool')
      }
    }).contains('Unknown collection-mapping type')
  }

  void testDuplicatePred() {
   Class cls;

   // duplicate
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test1') {
        f1 (maxCard:-1, colType:'Array', type:'xsd:anyURI', propType:'OBJECT', 
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'xsd:anyURI', propType:'OBJECT', 
                        colMapping:'Predicate', pred:'test:f')
      }
    }).contains('Duplicate')

    cls = rdf.class('Test1'){}
    cls = rdf.class('Test2') {
        f1 (maxCard:-1, colType:'Array', type:'xsd:anyURI', propType:'OBJECT', 
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'xsd:anyURI', propType:'OBJECT', 
                        colMapping:'Predicate', pred:'test:f', inverse:true)
    }
    def obj = cls.newInstance(id:'foo:1'.toURI())

    cls = rdf.class('Test3') {
        f1 (maxCard:-1, colType:'Array', type:'Test1', 
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'Test2',
                        colMapping:'Predicate', pred:'test:f')
    }
    obj = cls.newInstance(id:'foo:1'.toURI())

    // duplicate
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test4') {
        f1 (maxCard:-1, colType:'Array', type:'xsd:anyURI', propType:'OBJECT',
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'Test2',
                        colMapping:'Predicate', pred:'test:f')
      }
    }).contains('Duplicate')

    // invalid collection mapping
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test5') {
        f1 (maxCard:-1, colType:'Array', type:'Test1', 
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'Test2',
                        colMapping:'RdfSeq', pred:'test:f')
      }
    }).contains('colType')

    // unsupported(see #840) model mapping
    assert shouldFail(OtmException.class, {
      cls = rdf.class('Test6') {
        f1 (maxCard:-1, colType:'Array', type:'Test1',
                        colMapping:'Predicate', pred:'test:f')
        f2 (maxCard:-1, colType:'Array', type:'Test2', model:'foo',
                        colMapping:'Predicate', pred:'test:f')
      }
    }).contains('model')
  }

  void testIdField() {
    // explicit id-field
    Class cls = rdf.class('Test1') {
      uri (isId:true)
      state (type:'xsd:int')
    }
    def obj = cls.newInstance(uri:'foo:1', state:1)
    shouldFail(MissingPropertyException, { obj.id })

    // generated id-field
    cls = rdf.class('Test2') {
      state (type:'xsd:int')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), state:1)

    // id-field name collision
    assert shouldFail(OtmException, {
      cls = rdf.class('Test2') {
        id    ()
        state (type:'xsd:int')
      }
    }).contains("one field is already named 'id'")

    // multiple id-fields
    assert shouldFail(OtmException, {
      cls = rdf.class('Test2') {
        id1   (isId:true)
        id2   (isId:true)
        state (type:'xsd:int')
      }
    }).contains('more than one id-field defined')

    // collection id-field
    assert shouldFail(OtmException, {
      cls = rdf.class('Test2') {
        id1   (isId:true, maxCard:-1)
        state (type:'xsd:int')
      }
    }).contains('id-fields may not be collections')
  }

  void testClassInheritance() {
    // basic extending
    Class base = rdf.class('Base1', isAbstract:true) {
      state (type:'xsd:int')
    }

    Class ext = rdf.class('Ext1', extendsClass:'Base1') {
      color ()
    }

    shouldFail(NoSuchFieldException, { base.getDeclaredField('id') })

    def obj  = ext.newInstance(id:'foo:1'.toURI(), state:42, color:'blue')
    assertNotNull(obj)
    assertFalse(obj.equals(null))
    assertEquals(ext.newInstance(id:'foo:1'.toURI(), state:42, color:'blue'), obj)
    assert obj != ext.newInstance(id:'foo:2'.toURI(), state:42, color:'blue') : obj
    assert obj != ext.newInstance(id:'foo:1'.toURI(), state:43, color:'blue') : obj
    assert obj != ext.newInstance(id:'foo:1'.toURI(), state:42, color:'red') : obj

    assertEquals('foo:1'.toURI().hashCode(), obj.hashCode())

    // id inheritance
    base = rdf.class('Base2', isAbstract:true) {
      uri   (isId:true)
      state (type:'xsd:int')
    }

    ext = rdf.class('Ext2', extendsClass:'Base2') {
      color ()
    }

    obj = ext.newInstance(uri:'foo:1', state:42, color:'blue')
    shouldFail(MissingPropertyException, { obj.id })
    assertEquals('foo:1'.hashCode(), obj.hashCode())

    // non-abstract base
    base = rdf.class('Base3') {
      uri   (isId:true)
      state (type:'xsd:int')
    }

    ext = rdf.class('Ext3', extendsClass:'Base3') {
      color ()
    }

    obj = ext.newInstance(uri:'foo:1', state:42, color:'blue')
    shouldFail(MissingPropertyException, { obj.id })
    assertEquals('foo:1'.hashCode(), obj.hashCode())
  }

  void testEmbeddedClass() {
    // simple embedding
    Class cls = rdf.class('Test1') {
      state (type:'xsd:int')
      name (embedded:true) {
        givenName ()
        surname   ()
      }
    }

    def mappers = rdf.sessFactory.getClassMetadata(cls).rdfMappers
    assertEquals(3, mappers.size())
    for (Mapper m : mappers)
      assertNotNull(m.getBinder(EntityMode.POJO).serializer)
    assertEquals([ 'name.givenName', 'name.surname', 'state' ], mappers.name.sort())

    // nested embeddings
    cls = rdf.class('Test2') {
      state (type:'xsd:int')
      info (embedded:true) {
        personal (embedded:true) {
          name (embedded:true, className:'Name2') {
            givenName ()
            surname   ()
          }
        }
      }
    }

    mappers = rdf.sessFactory.getClassMetadata(cls).rdfMappers
    assertEquals(3, mappers.size())
    for (Mapper m : mappers)
      assertNotNull(m.getBinder(EntityMode.POJO).serializer)
    assertEquals([ 'info.personal.name.givenName', 'info.personal.name.surname', 'state' ],
                 mappers.name.sort())

    // embedded with max-card > 1
    assert shouldFail(OtmException, {
      rdf.class('Test3') {
        state (type:'xsd:int')
        name (embedded:true, className:'Name4', maxCard:-1) {
          givenName ()
          surname   ()
        }
      }
    }).contains('embedded fields must have max-cardinality 1')

    // embedding a non-class
    assert shouldFail(OtmException, {
      rdf.class('Test4') {
        state (type:'xsd:int')
        name (embedded:true)
      }
    }).contains('only class types may be embedded')

    // duplicate field names
    assert shouldFail(OtmException, {
      cls = rdf.class('Test5') {
        state (type:'xsd:int')
        info (embedded:true, className:'Info5') {
          personal (embedded:true, className:'Personal5') {
            name (embedded:true, className:'Name5') {
              givenName ()
              surname   ()
            }
            address ()
          }
          external (embedded:true, className:'External5') {
            address ()
          }
        }
      }
    }).contains('Duplicate predicate uri')

    assert shouldFail(OtmException, {
      cls = rdf.class('Test6') {
        state (type:'xsd:int')
        info (embedded:true, className:'Info6') {
          personal (embedded:true, className:'Personal6') {
            name (embedded:true, className:'Name6') {
              givenName ()
              surname   ()
            }
            address ()
          }
          external (embedded:true, className:'External6') {
            surname ()
          }
        }
      }
    }).contains('Duplicate predicate uri')
  }

  void testPackages() {
    // non-default package
    Class cls = rdf.class('org.foo.Test1') {
      state ()
    }
    ClassMetadata cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('org.foo.Test1', cls.name)
    assertEquals('http://rdf.topazproject.org/RDF/org.foo.Test1', cm.type)

    cm = rdf.sessFactory.getClassMetadata('Test1')

    assertEquals('org.foo.Test1', cls.name)
    assertEquals('http://rdf.topazproject.org/RDF/org.foo.Test1', cm.type)

    cm = rdf.sessFactory.getClassMetadata('org.foo.Test1')

    assertEquals('org.foo.Test1', cls.name)
    assertEquals('http://rdf.topazproject.org/RDF/org.foo.Test1', cm.type)

    // default package
    cls = rdf.class('Test2') {
      state ()
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('Test2', cls.name)
    assertEquals('http://rdf.topazproject.org/RDF/Test2', cm.type)

    cm = rdf.sessFactory.getClassMetadata('Test2')

    assertEquals('Test2', cls.name)
    assertEquals('http://rdf.topazproject.org/RDF/Test2', cm.type)
  }

  void testErrorHandling() {
    rdf.class('Test1') {
    }

    rdf.class('Test2') {
      foo (type:'Test1')
    }

    shouldFail(OtmException, {
      rdf.class('Test3') {
        foo (type:'Test4')
      }
    });

    shouldFail(OtmException, {
      rdf.class('Test5') {
        foo (type:'Test3')
      }
    });

    shouldFail(OtmException, {
      rdf.class('Test6') {
        foo (type:'Test4')
      }
    });
  }


  void testSubClassMetadata() {

    Class notypec = rdf.class("NoType", type:null) {
      uri   (isId:true)
    }

    Class basec = rdf.class("BaseClass", type:'base:type', extendsClass:"NoType") {
    }

    Class wrongc = rdf.class("WrongOne", extendsClass:"BaseClass") {
      name 'Test2'
    }

    Class rightc = rdf.class("RightOne", type:'sub:type', extendsClass:"BaseClass") {
    }

    ClassMetadata nt = rdf.sessFactory.getClassMetadata(notypec);
    ClassMetadata base = rdf.sessFactory.getClassMetadata(basec);
    ClassMetadata wrong = rdf.sessFactory.getClassMetadata(wrongc);
    ClassMetadata right = rdf.sessFactory.getClassMetadata(rightc);

    assertTrue(nt.isAssignableFrom(base))
    assertTrue(nt.isAssignableFrom(right))
    assertTrue(nt.isAssignableFrom(wrong))
    assertTrue(base.isAssignableFrom(base))
    assertTrue(base.isAssignableFrom(right))
    assertTrue(base.isAssignableFrom(wrong))
    assertTrue(right.isAssignableFrom(right))
    assertFalse(right.isAssignableFrom(wrong))
    assertFalse(right.isAssignableFrom(base))
    assertTrue(wrong.isAssignableFrom(wrong))
    assertFalse(wrong.isAssignableFrom(right))
    assertFalse(wrong.isAssignableFrom(base))

    ClassMetadata p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, ['base:type', 'sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, ['sub:type', 'base:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(wrong, EntityMode.POJO, ['base:type', 'sub:type'])
    assertEquals(wrong, p)

    p = rdf.sessFactory.getSubClassMetadata(wrong, EntityMode.POJO, ['sub:type', 'base:type'])
    assertEquals(wrong, p)

    p = rdf.sessFactory.getSubClassMetadata(right, EntityMode.POJO, ['base:type', 'sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(right, EntityMode.POJO, ['sub:type', 'base:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(wrong, EntityMode.POJO, ['base:type'])
    assertEquals(wrong, p)

    p = rdf.sessFactory.getSubClassMetadata(right, EntityMode.POJO, ['base:type'])
    assertNull(p)

    p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, ['base:type'])
    assertEquals(wrong, p)

    p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, ['sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(right, EntityMode.POJO, ['sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(wrong, EntityMode.POJO, ['sub:type'])
    assertNull(p)

    p = rdf.sessFactory.getSubClassMetadata(base, EntityMode.POJO, ['junk:type'])
    assertNull(p)

    p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, ['junk:type'])
    assertEquals(nt, p)

    p = rdf.sessFactory.getSubClassMetadata(nt, EntityMode.POJO, [])
    assertEquals(nt, p)

    //p = rdf.sessFactory.getSubClassMetadata(null, EntityMode.POJO, [])
    //assertEquals(nt, p)

    p = rdf.sessFactory.getSubClassMetadata(base, EntityMode.POJO, [])
    assertNull(p)

    p = rdf.sessFactory.getSubClassMetadata(null, EntityMode.POJO, ['sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(null, EntityMode.POJO, ['base:type'])
    assert (right == p) || (wrong == p) : p

    p = rdf.sessFactory.getSubClassMetadata(null, EntityMode.POJO, ['base:type', 'sub:type'])
    assertEquals(right, p)

    p = rdf.sessFactory.getSubClassMetadata(null, EntityMode.POJO, ['sub:type', 'base:type'])
    assertEquals(right, p)
  }

  void testCascadeType() {
    Class cls = rdf.class('Test1', type:'foo:Test1', model:'m1') {
      sel  (pred:'foo:p1', type:'foo:Test1', cascade:['delete','saveOrUpdate'])
      all  (pred:'foo:p2', type:'foo:Test1')
      none (pred:'foo:p3', type:'foo:Test1', cascade:[])
    }
    ClassMetadata cm = rdf.sessFactory.getClassMetadata(cls)

    assertEquals('foo:Test1', cm.type)
    assertEquals('m1', cm.model)
    assertEquals(3, cm.rdfMappers.size())

    Mapper m = cm.rdfMappers.asList()[0]
    FieldBinder l = (FieldBinder)m.getBinder(EntityMode.POJO)
    assertEquals('sel', m.name)
    assertEquals(cls, l.type)
    assertNull(m.dataType)
    assertEquals('foo:p1', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)
    assertTrue(m.isCascadable(CascadeType.delete))
    assertTrue(m.isCascadable(CascadeType.saveOrUpdate))
    assertFalse(m.isCascadable(CascadeType.merge))
    assertFalse(m.isCascadable(CascadeType.refresh))

    m = cm.rdfMappers.asList()[1]
    l = (FieldBinder)m.getBinder(EntityMode.POJO)
    assertEquals('all', m.name)
    assertEquals(cls, l.type)
    assertNull(m.dataType)
    assertEquals('foo:p2', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)
    assertTrue(m.isCascadable(CascadeType.delete))
    assertTrue(m.isCascadable(CascadeType.saveOrUpdate))
    assertTrue(m.isCascadable(CascadeType.merge))
    assertTrue(m.isCascadable(CascadeType.refresh))

    m = cm.rdfMappers.asList()[2]
    l = (FieldBinder)m.getBinder(EntityMode.POJO)
    assertEquals('none', m.name)
    assertEquals(cls, l.type)
    assertNull(m.dataType)
    assertEquals('foo:p3', m.uri)
    assertFalse(m.hasInverseUri())
    assertNull(m.model)
    assertFalse(m.isCascadable(CascadeType.delete))
    assertFalse(m.isCascadable(CascadeType.saveOrUpdate))
    assertFalse(m.isCascadable(CascadeType.merge))
    assertFalse(m.isCascadable(CascadeType.refresh))
  }
}
