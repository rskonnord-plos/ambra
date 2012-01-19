/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject;

import org.ambraproject.service.Migrator;
import org.ambraproject.service.MySQLService;
import org.ambraproject.service.TopazService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.models.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to run a migration incrementally, filtering objects that have an 'updated' date/time property to only get the
 * new ones
 *
 * @author Alex Kudlick Date: Apr 5, 2011
 *         <p/>
 *         org.ambraproject.service
 */
public class IncrementalMigration {
  private static Logger log = LoggerFactory.getLogger(IncrementalMigration.class);
  private static final String DATE_FORMAT = "yyyy/MM/dd";
  private static final DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

  public static void main(String[] args) {
    if (args.length != 1) {
      log.error("Must pass in a date in the form " + DATE_FORMAT);
      return;
    }
    TopazService topaz;
    MySQLService mysql;
    Date pubDate;
    try {
      log.info("Initializing Topaz service");
      topaz = new TopazService();
      log.info("Initializing MySQL service");
      mysql = new MySQLService();
    } catch (Exception e) {
      log.error("Error setting up services", e);
      return;
    }
    try {
      pubDate = dateFormatter.parse(args[0]);
      log.info("parsed date: " + pubDate);
    } catch (Exception e) {
      log.error("Error parsing input date: " + args[0] + "; should be of the form " + DATE_FORMAT, e);
      return;
    }
    migrate(topaz, mysql, pubDate);
  }

  private static void migrate(TopazService topaz, MySQLService mysql, Date pubDate) {
    int numThreads = Runtime.getRuntime().availableProcessors() * 2;
    Migrator migrator = Migrator.create(mysql, topaz)
        .threads(numThreads);

    //Articles
    migrator.forClass(Article.class)
        .createQuery("select art.id from Article art where ge(art.dublinCore.created,:date) " +
            "or ge(art.dublinCore.modified,:date) " +
            "or ge(art.dublinCore.date,:date)")
        .addQueryParameter("date", pubDate)
        .migrate();

    //Aggregations
    for (Class c : new Class[]{Journal.class, Volume.class, Issue.class}) {
      migrator.forClass(c)
          .createQuery("select c.id from " + c.getSimpleName() + " c where ge(c.dublinCore.created,:date) " +
              "or ge(c.dublinCore.modified,:date) " +
              "or ge(c.dublinCore.date,:date)")
          .addQueryParameter("date", pubDate)
          .migrate();
    }

    //Syndications
    migrator.forClass(Syndication.class)
        .createQuery("select s.id from Syndication s where ge(s.submitTimestamp,:date) " +
            "or ge(s.statusTimestamp,:date)")
        .addQueryParameter("date", pubDate)
        .migrate();

    //Representations
    migrator.forClass(Representation.class)
        .createQuery("select r.id from Representation r where ge(r.lastModified,:date)")
        .addQueryParameter("date", pubDate)
        .recordsPerCommit(1).threads(1)
        .migrate();

    //Everything else
    for (Class c : new Class[]{UserAccount.class, UserProfile.class, Citation.class,
        MinorCorrection.class, FormalCorrection.class, Retraction.class, Comment.class,
        Trackback.class, RatingSummary.class, Rating.class}) {
      migrator.queryAll(c).recordsPerCommit(6).threads(numThreads).migrate();
    }
  }

}
