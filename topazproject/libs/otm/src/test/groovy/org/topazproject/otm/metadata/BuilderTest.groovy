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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;

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
    assert obj.name == obj.name.class.newInstance(id:'foo:n1'.toURI(), givenName:'Peter')

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

    assert cm.type          == 'http://rdf.topazproject.org/RDF/Test1'
    assert cm.model         == 'ri'
    assert cm.fields.size() == 1

    def m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'http://rdf.topazproject.org/RDF/state'
    assert !m.hasInverseUri()
    assert m.model    == null

    // relative-uri overrides, typed literal
    cls = rdf.class('Test2', type:'Test2', model:'m2') {
      state (pred:'p2', type:'xsd:int')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'http://rdf.topazproject.org/RDF/Test2'
    assert cm.model         == 'm2'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == Integer.TYPE
    assert m.dataType == 'http://www.w3.org/2001/XMLSchema#int'
    assert m.uri      == 'http://rdf.topazproject.org/RDF/p2'
    assert !m.hasInverseUri()
    assert m.model    == null

    // absolute-uri overrides, class type
    Class cls2 = cls
    cls = rdf.class('Test3', type:'foo:Test3', model:'m3') {
      state (pred:'foo:p3', type:'Test2')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'foo:Test3'
    assert cm.model         == 'm3'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == cls2
    assert m.dataType == null
    assert m.uri      == 'foo:p3'
    assert !m.hasInverseUri()
    assert m.model    == null

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

    assert cm.type          == 'foo:Test4'
    assert cm.model         == 'm4'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == rdf.sessFactory.getClassMetadata('State').sourceClass
    assert m.dataType == null
    assert m.uri      == 'foo:p4'
    assert !m.hasInverseUri()
    assert m.model    == 'm41'

    cm = rdf.sessFactory.getClassMetadata('State')

    assert cm.type          == 'bar4:State'
    assert cm.model         == 'm41'
    assert cm.fields.size() == 2

    m = cm.fields.asList()[0]
    assert m.name     == 'value'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'bar4:value'
    assert !m.hasInverseUri()
    assert m.model    == 'm42'

    m = cm.fields.asList()[1]
    assert m.name          == 'history'
    assert m.type          == List.class
    assert m.componentType == rdf.sessFactory.getClassMetadata('History').sourceClass
    assert m.dataType      == null
    assert m.uri           == 'bar4:history'
    assert !m.hasInverseUri()
    assert m.model    == null

    cm = rdf.sessFactory.getClassMetadata('History')

    assert cm.type          == 'bar4:History'
    assert cm.model         == 'm41'
    assert cm.fields.size() == 1

    m = cm.fields.asList()[0]
    assert m.name     == 'value'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'bar4:value'
    assert !m.hasInverseUri()
    assert m.model    == null

    // uri type
    cls = rdf.class('Test5', type:'foo:Test5', model:'m5') {
      state (pred:'foo:p5', type:'xsd:anyURI', inverse:true)
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'foo:Test5'
    assert cm.model         == 'm5'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == URI.class
    assert m.dataType == null
    assert m.uri      == 'foo:p5'
    assert m.hasInverseUri()
    assert m.model    == null

    // no default prefix defined
    rdf.defUriPrefix = null
    cls = rdf.class('Test6', uriPrefix:'p6:') {
      state (type:'xsd:anyURI')
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == 'p6:Test6'
    assert cm.model         == 'ri'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == URI.class
    assert m.dataType == null
    assert m.uri      == 'p6:state'
    assert !m.hasInverseUri()
    assert m.model    == null

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

    assert cm.type          == null
    assert cm.model         == 'ri'
    assert cm.fields.size() == 1

    m = cm.fields.iterator().next()
    assert m.name     == 'state'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'http://rdf.topazproject.org/RDF/state'
    assert !m.hasInverseUri()
    assert m.model    == null

    Class sup = rdf.class('Test12') {
      state ()
    }
    cls = rdf.class('Test13', type:null, extendsClass:'Test12') {
      blah ()
    }
    cm = rdf.sessFactory.getClassMetadata(cls)

    assert cm.type          == null
    assert cm.model         == 'ri'
    assert cm.fields.size() == 2

    m = cm.fields.asList()[0]
    assert m.name     == 'blah'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'http://rdf.topazproject.org/RDF/blah'
    assert !m.hasInverseUri()
    assert m.model    == null

    m = cm.fields.asList()[1]
    assert m.name     == 'state'
    assert m.type     == String.class
    assert m.dataType == null
    assert m.uri      == 'http://rdf.topazproject.org/RDF/state'
    assert !m.hasInverseUri()
    assert m.model    == null
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
    assert obj.goals instanceof Set

    // List, RdfSeq
    cls = rdf.class('Test2') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfSeq')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof List

    // List, RdfList
    cls = rdf.class('Test3') {
      goals (maxCard:-1, colType:'List', colMapping:'RdfList')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof List

    // String[], RdfAlt
    cls = rdf.class('Test4') {
      goals (maxCard:-1, colType:'Array', colMapping:'RdfAlt')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:['one', 'two'])
    assert obj.goals instanceof String[]

    // int[], predicate
    cls = rdf.class('Test5') {
      goals (maxCard:-1, colType:'Array', type:'xsd:int', colMapping:'Predicate')
    }
    obj = cls.newInstance(id:'foo:1'.toURI(), goals:[1, 2])
    assert obj.goals instanceof int[]

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
    assert obj != null
    assert !obj.equals(null)
    assert obj == ext.newInstance(id:'foo:1'.toURI(), state:42, color:'blue')
    assert obj != ext.newInstance(id:'foo:2'.toURI(), state:42, color:'blue')
    assert obj != ext.newInstance(id:'foo:1'.toURI(), state:43, color:'blue')
    assert obj != ext.newInstance(id:'foo:1'.toURI(), state:42, color:'red')

    assert obj.hashCode() == 'foo:1'.toURI().hashCode()

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
    assert obj.hashCode() == 'foo:1'.hashCode()

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
    assert obj.hashCode() == 'foo:1'.hashCode()
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

    def mappers = rdf.sessFactory.getClassMetadata(cls).fields
    assert mappers.size() == 3
    for (Mapper m : mappers)
      assert m.serializer != null
    assert mappers.name.sort() == [ 'name.givenName', 'name.surname', 'state' ]

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

    mappers = rdf.sessFactory.getClassMetadata(cls).fields
    assert mappers.size() == 3
    for (Mapper m : mappers)
      assert m.serializer != null
    assert mappers.name.sort() ==
                      [ 'info.personal.name.givenName', 'info.personal.name.surname', 'state' ]

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
/*
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
    }).contains('Duplicate Rdf uri')

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
    }).contains('Duplicate Rdf uri')
*/
  }
}
