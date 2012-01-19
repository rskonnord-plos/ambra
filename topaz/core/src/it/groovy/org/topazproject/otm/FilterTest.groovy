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
        info (className:"Info_${inv}") {
          name (type:'ExtName', inverse:inv)
          addr (type:'Addr', inverse:inv)
        }
      }

      FilterDefinition fd1 =
          new OqlFilterDefinition('noBob', cls.getName(), "o where o.info.name.givenName != 'Bob'")
      FilterDefinition fd2 =
          new OqlFilterDefinition('state', cls.getName(), 'o where o.state != :state')
      FilterDefinition fd3 =
          new OqlFilterDefinition('noJack', 'Name', "n where n.givenName != 'Jack'")

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
}
