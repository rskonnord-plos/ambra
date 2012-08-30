/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.action.article;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.models.Article;
import org.ambraproject.service.mailer.AmbraMailer;
import org.ambraproject.testutils.DummyAmbraMailer;
import org.ambraproject.testutils.DummyDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Skonnord
 */
public class EmailArticleActionTest extends AmbraWebTest {

  @Autowired
  protected EmailArticleAction action;

  @Autowired
  protected AmbraMailer mailer;

  @Autowired
  protected ArticleService articleService;

  @Autowired
  protected DummyDataStore dummyDataStore;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }


  private void assertInvalidField(String fieldName) throws Exception {
    String result = action.executeSend();
    assertEquals(result, Action.INPUT, "executeSend did not halt with an invalid " + fieldName);
    assertTrue(((DummyAmbraMailer) mailer).getEmailsSent().isEmpty(), "Message was sent with with an invalid " + fieldName);

    Map<String, List<String>> fieldErrors = action.getFieldErrors();
    assertEquals(fieldErrors.size(), 1, "Exactly one field should be invalid");
    assertTrue(fieldErrors.containsKey(fieldName), "Unexpected field is invalid");
    action.clearFieldErrors();
  }

  private static Article mockArticle() {
    Article article = new Article();
    article.setDoi("info:doi/10.1371/Fake-Doi-For-EmailArticleActionTest");
    article.setTitle("Fake Title for EmailArticleActionTest Article");
    article.setState(Article.STATE_ACTIVE);
    article.setDescription("Fake Description for EmailArticleActionTest Article");
    article.setJournal("Fake Journal for EmailArticleActionTest Article");
    return article;
  }

  @Test
  public void testExecuteSend() throws Exception {
    final Article article = mockArticle();
    dummyDataStore.store(article);

    final String emailFrom = "test.from@example.com";
    final String emailTo = "test.to@example.com";
    final String senderName = "Test Sender";
    final String note = "Test Note";

    final String articleURI = article.getDoi();
    final String journalName = article.getJournal();
    final String title = article.getTitle();

    action.setEmailFrom(emailFrom);
    action.setEmailTo(emailTo);
    action.setArticleURI(articleURI);
    action.setSenderName(senderName);
    action.setNote(note);
    action.setJournalName(journalName);


    // Test validation on fields one at a time

    action.setEmailFrom(null);
    assertInvalidField("emailFrom");
    action.setEmailFrom("invalid@@example.com");
    assertInvalidField("emailFrom");
    action.setEmailFrom(emailFrom);

    action.setEmailTo(null);
    assertInvalidField("emailTo");
    action.setEmailTo("invalid@@example.com");
    assertInvalidField("emailTo");
    action.setEmailTo(emailTo);

    action.setSenderName(null);
    assertInvalidField("senderName");
    action.setSenderName(senderName);

    action.setArticleURI(null);
    assertInvalidField("articleURI");
    action.setArticleURI("This is not a valid URI");
    assertInvalidField("articleURI");
    action.setArticleURI(articleURI);


    // Action should be in a valid state; check that it will send an email correctly
    String result = action.executeSend();
    assertEquals(result, Action.SUCCESS, "Action didn't report success");
    final List<DummyAmbraMailer.DummyEmail> emailsSent = ((DummyAmbraMailer) mailer).getEmailsSent();
    assertEquals(emailsSent.size(), 1, "Exactly one email should have been sent");
    final DummyAmbraMailer.DummyEmail mail = emailsSent.get(0);
    assertEquals(mail.getFromEmailAddress(), emailFrom, "From address not sent to mailer");
    assertEquals(mail.getToEmailAddresses(), emailTo, "To address not sent to mailer");
    assertTrue(mail.getSubject().indexOf(title) >= 0, "Article title doesn't appear in email subject");
    final Map<String, Object> context = mail.getContext();
    assertEquals(context.get("articleURI"), articleURI, "articleURI not found in context");
    assertEquals(context.get("description"), article.getDescription(), "description not found in context");
    assertEquals(context.get("journalName"), journalName, "journalName not found in context");
    assertEquals(context.get("note"), note, "note not found in context");
    assertEquals(context.get("senderName"), senderName, "senderName not found in context");
    assertEquals(context.get("title"), title, "title not found in context");

    ((DummyAmbraMailer) mailer).clear();
  }

}
