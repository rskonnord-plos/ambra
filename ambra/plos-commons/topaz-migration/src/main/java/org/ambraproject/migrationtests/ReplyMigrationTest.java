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

package org.ambraproject.migrationtests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyThread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;


/**
 * @author Alex Kudlick Date: Mar 22, 2011
 *         <p/>
 *         org.plos.topazMigration
 */
public class ReplyMigrationTest extends BaseMigrationTest {

  //Number of replies to load up at a time (topaz sessions need to be restarted periodically, which happens on each block)
  private static final int INCREMENT_SIZE = 50;

  @DataProvider(name = "replies")
  public Iterator<Object[]> getReplies() {
    return new MigrationDataIterator(this, Reply.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "replies")
  public void diffReplys(Reply mysqlReply, Reply topazReply) {
    assertNotNull(topazReply, "Topaz failed to return a(n) reply thread; check the migration logs for more information");
    assertNotNull(mysqlReply, "Mysql didn't return reply thread for id: " + topazReply.getId());

    topazReply = loadTopazObject(topazReply.getId(), Reply.class);

    compareReplies(mysqlReply, topazReply);

    if (topazReply instanceof ReplyThread) {
      assertTrue(mysqlReply instanceof ReplyThread);
      List<Reply> topazReplies = new ArrayList<Reply>(((ReplyThread) topazReply).getReplies());
      List<Reply> mysqlReplies = new ArrayList<Reply>(((ReplyThread) mysqlReply).getReplies());

      assertEquals(mysqlReplies.size(), topazReplies.size(),
          "Mysql and Topaz didn't have the same number of replies for thread " + mysqlReply.getId());
      for (int i = 0; i < topazReplies.size(); i++) {
        //Just compare Id's of Replies in thread
        assertEquals(mysqlReplies.get(i).getId(), topazReplies.get(i).getId(),
            "Mysql and Topaz didn't have matching replies for thread: " + mysqlReply.getId());
      }
    }
  }

  private void compareReplies(Reply mysqlReply, Reply topazReply) {
    AnnotationMigrationTest.compareAnnoteaProperties(mysqlReply, topazReply);

    assertEquals(mysqlReply.getRoot(), topazReply.getRoot(),
        "Mysql and Topaz didn't have matching roots for reply: " + mysqlReply.getId());
    assertEquals(mysqlReply.getInReplyTo(), topazReply.getInReplyTo(),
        "Mysql and Topaz didn't have matching inReplyTo values for reply: " + mysqlReply.getId());
  }
}
