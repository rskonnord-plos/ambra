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

package org.ambraproject.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.service.mailer.AmbraMailer;
import org.ambraproject.testutils.DummyAmbraMailer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.ambraproject.ambra.email.impl.FreemarkerTemplateMailer;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * @author Ryan Skonnord
 */
public class FeedbackActionTest extends AmbraWebTest {

  @Autowired
  protected FeedbackAction action;

  @Autowired
  protected AmbraMailer mailer;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }


  /**
   * Assert that the action is in an invalid state because exactly one required field is null.
   *
   * @param fieldName the name of the field that should be null
   * @throws Exception
   */
  private void assertMissingField(String fieldName) throws Exception {
    String result = action.executeSend();
    assertEquals(result, Action.INPUT, "executeSend did not halt with " + fieldName + " == null");
    assertTrue(((DummyAmbraMailer) mailer).getEmailsSent().isEmpty(), "Message was sent with " + fieldName + " == null");

    Map<String, List<String>> fieldErrors = action.getFieldErrors();
    assertEquals(fieldErrors.size(), 1, "Exactly one field should be invalid");
    assertTrue(fieldErrors.containsKey(fieldName), "Unexpected field is missing");
    action.clearFieldErrors();
  }

  @Test
  public void testExecuteSend() throws Exception {
    final String subject = "Test Subject";
    final String name = "Test Name";
    final String fromEmailAddress = "test.from@example.com";
    final String note = "Test Note";

    action.setSubject(subject);
    action.setName(name);
    action.setFromEmailAddress(fromEmailAddress);
    action.setNote(note);

    // Null out fields one at a time and check that validation catches them

    action.setSubject(null);
    assertMissingField("subject");
    action.setSubject(subject);

    action.setName(null);
    assertMissingField(FreemarkerTemplateMailer.USER_NAME_KEY);
    action.setName(name);

    action.setFromEmailAddress(null);
    assertMissingField(action.FROM_EMAIL_ADDRESS_KEY);
    action.setFromEmailAddress(fromEmailAddress);

    action.setNote(null);
    assertMissingField("note");
    action.setNote(note);

    // Action should be in a valid state; check that it will send an email correctly
    String result = action.executeSend();
    assertEquals(result, Action.SUCCESS, "executeSend did not report success");
    List<DummyAmbraMailer.DummyEmail> emailsSent = ((DummyAmbraMailer) mailer).getEmailsSent();
    assertEquals(emailsSent.size(), 1, "One message should have been sent to mailer");
    final DummyAmbraMailer.DummyEmail mail = emailsSent.get(0);
    assertEquals(mail.getSubject(), subject, "Subject not sent to mailer");
    assertEquals(mail.getFromEmailAddress(), fromEmailAddress, "fromEmailAddress not sent to mailer");
    final Map<String, Object> context = mail.getContext();
    assertEquals(context.get(action.FROM_EMAIL_ADDRESS_KEY), fromEmailAddress, "Email address not found in context");
    assertEquals(context.get(FreemarkerTemplateMailer.USER_NAME_KEY), name, "Name not found in context");
    assertEquals(context.get("note"), note, "Note not found in context");
    assertFalse(StringUtils.isBlank((String) context.get("userInfo")), "User info not written to context");

    ((DummyAmbraMailer) mailer).clear();
  }

}
