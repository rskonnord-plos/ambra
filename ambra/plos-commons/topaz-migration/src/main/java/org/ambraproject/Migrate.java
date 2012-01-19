/*
 * $HeadURL:
 * $Id:
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * @author Joe Osowski
 */
public class Migrate {
  private static Logger log = LoggerFactory.getLogger(Migrate.class);
  private static int maxThreads = 0;

  public static void main(String args[]) {
    int errorCount = 0;

    try {
      log.info("Migration starting up");

      maxThreads = Runtime.getRuntime().availableProcessors();

      TopazService topaz = new TopazService();
      MySQLService mysql = new MySQLService();

      Migrator migrator = Migrator.create(mysql, topaz)
        .threads(maxThreads)
        .recordsPerCommit(1);

      //Articles
      errorCount += migrator.queryAll(Article.class)
        .recordsPerCommit(1)
        .recordsPerRequest(50)
        .migrate();

      //Aggregations
      for (Class c : new Class[] { Journal.class, Volume.class, Issue.class } ) {
        errorCount += migrator.queryAll(c)
          .recordsPerCommit(50)
          .recordsPerRequest(250)
          .migrate();
      }

      //I think we can assume these values will come down as part of the
      //Article class (This was causing some recursion bugs with hibernate)
      //migrator.queryAll(Representation.class)
        //.recordsPerCommit(50)
        //.migrate();

      errorCount += migrator.queryAll(UserAccount.class)
        .recordsPerCommit(100)
        .recordsPerRequest(1000)
        .migrate();

      errorCount += migrator.queryAll(UserProfile.class)
        .recordsPerCommit(100)
        .recordsPerRequest(1000)
        .migrate();

      //Everything else
      for (Class c : new Class[] {
            Syndication.class, Citation.class,
            MinorCorrection.class, FormalCorrection.class, Retraction.class, Comment.class,
            Trackback.class, RatingSummary.class, Rating.class, Reply.class
          }
        )
      {
        errorCount += migrator.queryAll(c)
          .recordsPerCommit(250)
          .recordsPerRequest(2000)
          .migrate();
      }

      topaz.close();
      mysql.close();

      log.info("Migration Complete with " + errorCount + " processing error(s).");

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }
}
