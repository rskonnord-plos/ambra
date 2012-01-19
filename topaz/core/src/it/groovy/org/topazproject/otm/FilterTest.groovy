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

package org.topazproject.otm;

import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.filter.OqlFilterDefinition;

/**
 * Integration tests for filters on object-read.
 */
public class FilterTest extends AbstractTest {
  void testGet() {
    // create data
    Class clsN = rdf.class('Name') {
      givenName ()
      surname   ()
    }
    Class clsEN = rdf.class('ExtName', type:'ExtName', extendsClass:'Name') {
      middleName ()
    }
    Class clsA = rdf.class('Addr') {
      street ()
    }

    for (inv in [false, true]) {
      Class cls = rdf.class("Test_${inv}") {
        state (type:'xsd:int')
        info (className:"Info_${inv}", fetch:'eager') {
          name (type:'ExtName', inverse:inv, fetch:'eager')
          addr (type:'Addr', inverse:inv, fetch:'eager')
        }
      }

      FilterDefinition fd1 =
          new OqlFilterDefinition('noBob', cls.getName(),
                          "select o from ${cls.getName()} o where o.info.name.givenName != 'Bob';")
      FilterDefinition fd2 =
          new OqlFilterDefinition('state', cls.getName(),
                          "select o from ${cls.getName()} o where o.state != :state;")
      FilterDefinition fd3 =
          new OqlFilterDefinition('noJack', 'Name',
                          "select n from Name n where n.givenName != 'Jack';")

      rdf.sessFactory.addFilterDefinition(fd1);
      rdf.sessFactory.addFilterDefinition(fd2);
      rdf.sessFactory.addFilterDefinition(fd3);

      def o1 = cls.newInstance(id:"foo:1".toURI(), state:1,
                               info:[name:[givenName:'Bob', surname:'Cutter', middleName:'Fritz'],
                                     addr:[street:'Milky Way']])
      def o2 = cls.newInstance(id:"foo:2".toURI(), state:2,
                               info:[name:[givenName:'Jack', surname:'Keller'],
                                     addr:[street:'Easy St']])
      def o3 = cls.newInstance(id:"foo:3".toURI(), state:3,
                               info:[name:[givenName:'Billy', surname:'Bob'],
                                     addr:[street:'Sesame St']])

      doInTx { s ->
        s.saveOrUpdate(o1)
        s.saveOrUpdate(o2)
        s.saveOrUpdate(o3)
      }

      // run tests
      doInTx { s ->
        // no filters
        assertEquals(o1, s.get(cls, o1.id.toString()));
        assertEquals(o2, s.get(cls, o2.id.toString()));
        assertEquals(o3, s.get(cls, o3.id.toString()));
      }

      doInTx { s ->
        // with filter on top-level class
        s.enableFilter('noBob');
        assertNull(s.get(cls, o1.id.toString()));
        assertEquals(o2, s.get(cls, o2.id.toString()));
        assertEquals(o3, s.get(cls, o3.id.toString()));
      }

      doInTx { s ->
        // with filter on top-level class
        s.enableFilter('state').setParameter('state', 3);
        assertEquals(o1, s.get(cls, o1.id.toString()));
        assertEquals(o2, s.get(cls, o2.id.toString()));
        assertNull(s.get(cls, o3.id.toString()));
      }

      doInTx { s ->
        // with filter on lower-level class
        s.enableFilter('noJack');
        s.enableFilter('state').setParameter('state', 3);
        def n2 = o2.info.name
        o2.info.name = null
        assertEquals(o1, s.get(cls, o1.id.toString()));
        assertEquals(o2, s.get(cls, o2.id.toString()));
        assertNull(s.get(cls, o3.id.toString()));
        o2.info.name = n2
      }

      // test filters on component-type of collections
      cls = rdf.class("Test2_${inv}") {
        state (type:'xsd:int')
        names1 (type:'ExtName', maxCard:-1, colMapping:'Predicate', inverse:inv)
        names2 (type:'ExtName', maxCard:-1, colMapping:'RdfSeq',    inverse:false)
        names3 (type:'ExtName', maxCard:-1, colMapping:'RdfList',   inverse:false)
      }

      def o4 = cls.newInstance(id:"bar:1".toURI(), state:1,
                 names1:[clsEN.newInstance(givenName:'Bob', surname:'Cutter'),
                         clsEN.newInstance(givenName:'Jack', surname:'Keller')],
                 names2:[clsEN.newInstance(givenName:'Bob', surname:'Cutter'),
                         clsEN.newInstance(givenName:'Jack', surname:'Keller')],
                 names3:[clsEN.newInstance(givenName:'Bob', surname:'Cutter'),
                         clsEN.newInstance(givenName:'Jack', surname:'Keller')])

      doInTx { s ->
        s.saveOrUpdate(o4)
      }

      doInTx { s ->
        // no filters
        def o = s.get(cls, o4.id.toString())
        if (o.names1[0].givenName != 'Bob') {
          def t = o.names1[0];
          o.names1[0] = o.names1[1];
          o.names1[1] = t;
        }
        assertEquals(o4, o);
      }

      doInTx { s ->
        // with filter(s)
        s.enableFilter('noJack');
        def o = s.get(cls, o4.id.toString())
        assertEquals([o4.names1[0]], o.names1);
        assertEquals([o4.names2[0]], o.names2);
        assertEquals([o4.names3[0]], o.names3);
      }

      // clean up
      doInTx { s ->
        s.delete(o1)
        s.delete(o2)
        s.delete(o3)
      }

      rdf.sessFactory.removeFilterDefinition(fd1.getFilterName());
      rdf.sessFactory.removeFilterDefinition(fd2.getFilterName());
      rdf.sessFactory.removeFilterDefinition(fd3.getFilterName());
    }
  }

  void testSuperSub() {
    // create data
    Class dummy = rdf.class('Dummy', type:null) {
      fwd ()
      rev (inverse:true)
    }

    Class clsN = rdf.class('Name', extendsClass:'Dummy') {
      givenName ()
      surname   ()
    }
    Class clsEN = rdf.class('ExtName', type:'ExtName', extendsClass:'Name') {
      middleName ()
    }
    Class clsXN = rdf.class('Ext2Name', type:'Ext2Name', extendsClass:'Name') {
      middleName ()
    }

    for (col in [null, 'Predicate', 'RdfSeq', 'RdfList']) {
      for (inv in (!col || col == 'Predicate' ? [false, true] : [false])) {
        Class cls = rdf.class("Test_${col}_${inv}") {
          name  (type:'Name',     inverse:inv, fetch:'eager', maxCard: col ? -1 : 1, colMapping:col)
          ename (type:'ExtName',  inverse:inv, fetch:'eager', maxCard: col ? -1 : 1, colMapping:col)
          xname (type:'Ext2Name', inverse:inv, fetch:'eager', maxCard: col ? -1 : 1, colMapping:col)
          obj   (javaType:dummy,  inverse:inv, fetch:'eager', maxCard: col ? -1 : 1, colMapping:col)
        }

        FilterDefinition fd1 = new OqlFilterDefinition('noBobN', 'Name',
                        "select n from Name n where n.givenName != 'Bob';")
        FilterDefinition fd2 = new OqlFilterDefinition('noBobEN', 'ExtName',
                        "select n from ExtName n where n.givenName != 'Bob';")

        /* TODO: Enable these when we stop turning all filters into criteria (criteria doesn't
         * supoort the 2-level dereference inside the minus but only supports simple Criterion)
         * and when we have some sort of exists() function or '!= null' support or something in
         * Oql
        FilterDefinition fd3 = new OqlFilterDefinition('noBobON', cls.getName(),
              "select o from ${cls.getName()} o where o != null minus (o.name.givenName = 'Bob');")
        FilterDefinition fd4 = new OqlFilterDefinition('noBobOEN', cls.getName(),
              "select o from ${cls.getName()} o where o != null minus (o.ename.givenName = 'Bob');")
        */

        def fds = [fd1, fd2 /*, fd3, fd4 */]
        for (fd in fds)
          rdf.sessFactory.addFilterDefinition(fd);

        def n1 = clsN.newInstance(id:"name:1".toURI(), givenName:'Bob', surname:'Cutter')
        def n2 = clsEN.newInstance(id:"name:2".toURI(), givenName:'Bob', surname:'Cutter',
                                   middleName:'Fritz')
        def n3 = clsXN.newInstance(id:"name:3".toURI(), givenName:'Bob', surname:'Cutter',
                                   middleName:'Fritz')

        def dum1 = dummy.newInstance(id:"foo:dum1".toURI(), fwd:'a')
        def dum2 = dummy.newInstance(id:"foo:dum2".toURI(), rev:'b:b')

        def o1 = cls.newInstance(id:"foo:1".toURI(), name:  col ? [n1] : n1)
        def o2 = cls.newInstance(id:"foo:2".toURI(), name:  col ? [n2] : n2)
        def o3 = cls.newInstance(id:"foo:3".toURI(), name:  col ? [n3] : n3)
        def o4 = cls.newInstance(id:"foo:4".toURI(), ename: col ? [n2] : n2)
        def o5 = cls.newInstance(id:"foo:5".toURI(), xname: col ? [n3] : n3)
        def o6 = cls.newInstance(id:"foo:6".toURI(), obj:   col ? [dum1] : dum1)
        def o7 = cls.newInstance(id:"foo:7".toURI(), obj:   col ? [dum2] : dum2)

        def objs = [n1, n2, n3, o1, o2, o3, o4, o5, o6, o7]
        doInTx { s -> for (o in objs) s.saveOrUpdate(o) }

        // run tests
        doInTx { s ->
          // no filters
          assertEquals(n1, s.get(clsN, n1.id.toString()));
          assertEquals(n2, s.get(clsEN, n2.id.toString()));
          assertEquals(n3, s.get(clsXN, n3.id.toString()));
          assertEquals(o1, s.get(cls, o1.id.toString()));
          assertEquals(o2, s.get(cls, o2.id.toString()));
          assertEquals(o3, s.get(cls, o3.id.toString()));
          assertEquals(o4, s.get(cls, o4.id.toString()));
          assertEquals(o5, s.get(cls, o5.id.toString()));
          assertEquals(o6, s.get(cls, o6.id.toString()));
          assertEquals(o7, s.get(cls, o7.id.toString()));
        }

        doInTx { s ->
          // with filters on super-class (Name)
          s.enableFilter('noBobN');

          assertNull(s.get(clsN, n1.id.toString()));
          assertNull(s.get(clsEN, n2.id.toString()));
          assertNull(s.get(clsXN, n3.id.toString()));
          assertEquals(null, s.get(cls, o1.id.toString()).name);
          assertEquals(null, s.get(cls, o2.id.toString()).name);
          assertEquals(null, s.get(cls, o3.id.toString()).name);
          assertEquals(null, s.get(cls, o4.id.toString()).ename);
          assertEquals(null, s.get(cls, o5.id.toString()).xname);
          assertEquals(o6, s.get(cls, o6.id.toString()));
          assertEquals(o7, s.get(cls, o7.id.toString()));

          /* TODO: enable when the filter-def is enabled
          for (o in [o1, o2, o3, o4, o5])
            s.evict(s.get(o.getClass(), o.id.toString()))
          s.enableFilter('noBobON');

          assertNull(s.get(cls, o1.id.toString()));
          assertNull(s.get(cls, o2.id.toString()));
          assertNull(s.get(cls, o3.id.toString()));
          assertNull(s.get(cls, o4.id.toString()));
          assertNull(s.get(cls, o5.id.toString()));
          assertEquals(o6, s.get(cls, o6.id.toString()));
          assertEquals(o7, s.get(cls, o7.id.toString()));
          */
        }

        doInTx { s ->
          // with filters on sub-class (ExtName)
          s.enableFilter('noBobEN');

          assertEquals(n1, s.get(clsN, n1.id.toString()));
          assertNull(s.get(clsEN, n2.id.toString()));
          assertEquals(n3, s.get(clsXN, n3.id.toString()));

          assertEquals(o1, s.get(cls, o1.id.toString()));
          assertEquals(null, s.get(cls, o2.id.toString()).name);
          assertEquals(o3, s.get(cls, o3.id.toString()));
          assertEquals(null, s.get(cls, o4.id.toString()).ename);
          assertEquals(o5, s.get(cls, o5.id.toString()));
          assertEquals(o6, s.get(cls, o6.id.toString()));
          assertEquals(o7, s.get(cls, o7.id.toString()));

          /* TODO: enable when the filter-def is enabled
          for (o in [o1, o2, o3, o4, o5])
            s.evict(s.get(o.getClass(), o.id.toString()))
          s.enableFilter('noBobOEN');

          assertEquals(o1, s.get(cls, o1.id.toString()));
          assertEquals(o2, s.get(cls, o2.id.toString()));
          assertEquals(o3, s.get(cls, o3.id.toString()));
          assertNull(s.get(cls, o4.id.toString()));
          assertEquals(o5, s.get(cls, o5.id.toString()));
          assertEquals(o6, s.get(cls, o6.id.toString()));
          assertEquals(o7, s.get(cls, o7.id.toString()));
          */
        }

        // clean up
        doInTx { s -> for (o in objs) s.delete(o) }

        for (fd in fds)
          rdf.sessFactory.removeFilterDefinition(fd.getFilterName());
      }
    }
  }
}
