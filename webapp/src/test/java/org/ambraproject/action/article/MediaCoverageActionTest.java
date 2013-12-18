/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;


public class MediaCoverageActionTest extends AmbraWebTest {

  @Autowired
  protected MediaCoverageAction action;

  @Test
  public void testExecuteRequiredFields() throws Exception {

    action.setUri("");

    action.setName("");
    action.setEmail("");
    action.setLink("");
    action.setComment("");

    action.setRecaptcha_challenge_field("");
    action.setRecaptcha_response_field("");

    assertEquals(action.execute(), Action.ERROR, "The result of the request was not the same");

    Map<String, List<String>> fieldErrors = action.getFieldErrors();

    List<String> msgList = new ArrayList();
    msgList.add("This field is required.");

    assertEquals(fieldErrors.size(), 3, "The number of error messages was not the same");
    assertEquals(fieldErrors.get("link"), msgList, "The error message was not the same");
    assertEquals(fieldErrors.get("name"), msgList, "The error message was not the same");
    assertEquals(fieldErrors.get("email"), msgList, "The error message was not the same");
  }

  @Test
  public void testExecuteInputValidation() throws Exception {

    action.setUri("info:doi/10.1371/journal.pone.0005723");
    action.setName("Your Name");

    action.setEmail("email");
    action.setLink("dsfaljjklklklsa");

    assertEquals(action.execute(), Action.ERROR, "The result of the request was not the same");

    Map<String, List<String>> actualFieldErrors = action.getFieldErrors();

    List<String> emailMsg = new ArrayList<String>();
    emailMsg.add("Invalid e-mail address");

    List<String> linkMsg = new ArrayList<String>();
    linkMsg.add("Invalid Media link URL");

    assertEquals(actualFieldErrors.size(), 2, "The number of error messages was not the same");
    assertEquals(actualFieldErrors.get("email"), emailMsg, "The error message for email was not the same");
    assertEquals(actualFieldErrors.get("link"), linkMsg, "The error message for link was not the same");
  }

  @Test
  public void testUrlInputValidation() throws Exception {
    // only link is incorrect
    action.setUri("info:doi/10.1371/journal.pone.0005723");
    action.setName("Your Name");

    action.setEmail("email@email.com");
    action.setLink("dsfaljjklklklsa");

    Method method = action.getClass().getDeclaredMethod("validateInput");
    method.setAccessible(true);
    Object result = method.invoke(action, null);

    assertEquals(result, false, "The output of the validateInput method was not the same");

    action.clearFieldErrors();
    assertEquals(action.execute(), Action.ERROR, "The result of the request was not the same");

    Map<String, List<String>> actualFieldErrors = action.getFieldErrors();

    List<String> linkMsg = new ArrayList<String>();
    linkMsg.add("Invalid Media link URL");

    assertEquals(actualFieldErrors.size(), 1, "The number of error messages was not the same");
    assertEquals(actualFieldErrors.get("link"), linkMsg, "The error message for link was not the same");
  }

  @Test
  public void testEmailInputValidation() throws Exception {
    // only email is incorrect
    action.setUri("info:doi/10.1371/journal.pone.0005723");
    action.setName("Your Name");

    action.setEmail("email");
    action.setLink("http://www.plos.org");

    Method method = action.getClass().getDeclaredMethod("validateInput");
    method.setAccessible(true);
    Object result = method.invoke(action, null);

    assertEquals(result, false, "The output of the validateInput method was not the same");

    action.clearFieldErrors();
    assertEquals(action.execute(), Action.ERROR, "The result of the request was not the same");

    Map<String, List<String>> actualFieldErrors = action.getFieldErrors();

    List<String> emailMsg = new ArrayList<String>();
    emailMsg.add("Invalid e-mail address");

    assertEquals(actualFieldErrors.size(), 1, "The number of error messages was not the same");
    assertEquals(actualFieldErrors.get("email"), emailMsg, "The error message for email was not the same");
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}

