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

import java.net.URI;
import java.util.Date;

import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.SubView;
import org.topazproject.otm.annotations.View;

import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.ObjectInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration tests for views.
 */
public class ViewTest extends AbstractTest {
  private static final Log log = LogFactory.getLog(ViewTest.class);

  void setUp() {
    super.setUp();

    rdf.sessFactory.preload(Article.class);
    rdf.sessFactory.preload(ViewOne.class);
    rdf.sessFactory.preload(ViewTwo.class);
    rdf.sessFactory.preload(ViewThree.class);
    rdf.sessFactory.preload(ViewThreePart.class);
    rdf.sessFactory.preload(ViewFour.class);
    rdf.sessFactory.preload(ViewFourPart.class);
    rdf.sessFactory.preload(ViewFive.class);
  }

  void testGet() {
    def o1 = new Article([uri: "http://foo.com/bar/baz".toURI(), title: "The sum of things",
                          date: new Date("02 Nov 2007"), categories:["Fruits", "Fish"],
                          authors:["James Kirchner", "Sandra Hollister"],
                          parts:[
                            new ObjectInfo([uri: "http://foo.com/bar/baz/p1".toURI(),
                                            identifier: "info:doi/10.1371/baz/part1",
                                            representations: ["PDF", "PNG"]]),
                          ]])
    def o2 = new Article([uri: "http://foo.com/bar/duh".toURI(), title: "Omega-3",
                          date: new Date("23 Jul 2007"), categories:["Veggies", "Fruits"],
                          authors:["Peter Bellum", "Beth Dirnhum"],
                          parts:[
                            new ObjectInfo([uri: "http://foo.com/bar/duh/p1".toURI(),
                                            identifier: "info:doi/10.1371/duh/part1",
                                            representations: ["PNG"]]),
                            new ObjectInfo([uri: "http://foo.com/bar/duh/p2".toURI(),
                                            identifier: "info:doi/10.1371/duh/part2",
                                            representations: ["XML", "HTML"]])
                          ]])

    doInTx { s ->
      s.saveOrUpdate(o1);
      s.saveOrUpdate(o2);
    }

    doInTx { s ->
      // basic test
      ViewOne res1 = s.get(ViewOne.class, o1.uri.toString())
      assertEquals(o1.uri,  res1.uri)
      assertEquals(o1.date, res1.date)

      res1 = s.get(ViewOne.class, o2.uri.toString())
      assertEquals(o2.uri,  res1.uri)
      assertEquals(o2.date, res1.date)

      // subqueries, field-list != projection-list
      ViewTwo res2 = s.get(ViewTwo.class, o1.uri.toString())
      assertEquals(o1.title,  res2.title)
      assertEquals(o1.authors.toList().sort(), res2.authors.toList().sort())
      assertEquals(o1.authors.toList().sort(), res2.authorsList.sort())
      assertEquals(o1.authors.toList().sort(), res2.authorsSet.toList().sort())

      res2 = s.get(ViewTwo.class, o2.uri.toString())
      assertEquals(o2.title,  res2.title)
      assertEquals(o2.authors.toList().sort(), res2.authors.toList().sort())
      assertEquals(o2.authors.toList().sort(), res2.authorsList.sort())
      assertEquals(o2.authors.toList().sort(), res2.authorsSet.toList().sort())

      // sub-views, count
      ViewThree res3 = s.get(ViewThree.class, o1.uri.toString())
      assertEquals(o1.uri.toString(), res3.id)
      assertEquals(o1.authors.size(), res3.authors)
      assertEquals(o1.parts.size(),   res3.parts.size())
      def parts = o1.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      res3.parts[0].identifier)
      assertEquals(parts[0].representations, res3.parts[0].representations)

      res3 = s.get(ViewThree.class, o2.uri.toString())
      assertEquals(o2.uri.toString(), res3.id)
      assertEquals(o2.authors.size(), res3.authors)
      assertEquals(o2.parts.size(),   res3.parts.size())
      parts = o2.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      res3.parts[0].identifier)
      assertEquals(parts[0].representations, res3.parts[0].representations)
      assertEquals(parts[1].identifier,      res3.parts[1].identifier)
      assertEquals(parts[1].representations, res3.parts[1].representations)

      // view-in-view
      ViewFour res4 = s.get(ViewFour.class, o1.uri.toString())
      assertEquals(o1.uri.toString(), res4.id)
      assertEquals(o1.parts.size(),   res4.parts.size())
      parts = o1.parts.toList().sort({ it.identifier })
      def rp = res4.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      rp[0].identifier)
      assertEquals(parts[0].representations, rp[0].representations)

      res4 = s.get(ViewFour.class, o2.uri.toString())
      assertEquals(o2.uri.toString(), res4.id)
      assertEquals(o2.parts.size(),   res4.parts.size())
      parts = o2.parts.toList().sort({ it.identifier })
      rp    = res4.parts.toList().sort({ it.identifier })
      assertEquals(parts[0].identifier,      rp[0].identifier)
      assertEquals(parts[0].representations, rp[0].representations)
      assertEquals(parts[1].identifier,      rp[1].identifier)
      assertEquals(parts[1].representations, rp[1].representations)

      // view-in-view (non-collection)
      ViewFive res5 = s.get(ViewFive.class, o1.uri.toString())
      assertEquals(o1.uri.toString(), res5.id)
      assertEquals(o1.parts.toList()[0].identifier,      res5.part.identifier)
      assertEquals(o1.parts.toList()[0].representations, res5.part.representations)

      // cleanup
      s.delete(o1)
      s.delete(o2)
    }
  }
}

/* basic view test */
@View(query = "select a.date date from Article a where a.uri = :id ;")
class ViewOne {
  @Id
  URI uri;

  @Projection("date")
  Date date;
}

/* testing projection not used (date), projection used multiple times (authors), and subqueries */
@View(query = """select a.uri, a.date, a.title title, (select a.authors from Article aa) authors
                 from Article a where a.uri = :id;""")
class ViewTwo {
  @Id
  URI uri;

  @Projection("title")
  String title;

  @Projection("authors")
  String[] authors;

  @Projection("authors")
  List<String> authorsList;

  @Projection("authors")
  Set<String> authorsSet;
}

/* testing sub-views */
@View(query = """select a.uri, count(a.authors) numAuth,
                  (select oi.uri, oi.identifier ident,
                   (select oi.representations from ObjectInfo oi2) reps
                   from ObjectInfo oi where oi = a.parts order by ident) parts
                 from Article a where a.uri = :id;""")
class ViewThree {
  @Id
  String id;

  @Projection("numAuth")
  int authors;

  @Projection("parts")
  List<ViewThreePart> parts;
}

@SubView
class ViewThreePart {
  @Projection("ident")
  String identifier;

  @Projection("reps")
  Set<String> representations;
}

/* testing views-in-views */
@View(query = """select a.uri,
                   (select p from Article aa where p := cast(a.parts, ViewFourPart)) parts
                 from Article a where a.uri = :id;""")
class ViewFour {
  @Id
  String id;

  @Projection(value="parts", fetch=FetchType.lazy)
  List<ViewFourPart> parts;
}

@View(query = """select oi.uri, oi.identifier ident,
                   (select oi.representations from ObjectInfo oi2) reps
                 from ObjectInfo oi where oi.uri = :id;""")
class ViewFourPart {
  @Id
  String id;

  @Projection("ident")
  String identifier;

  @Projection("reps")
  Set<String> representations;
}

/* testing view-in-views (non-collection) */
@View(query = """select a.uri, p from Article a
                 where a.uri = :id and p := cast(a.parts, ViewFourPart);""")
class ViewFive {
  @Id
  String id;

  @Projection("p")
  ViewFourPart part;
}
