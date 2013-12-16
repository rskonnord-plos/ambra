/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.annotation;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 3/9/12
 */
public class CreateAnnotationActionTest extends AmbraWebTest {

  @Autowired
  protected CreateAnnotationAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void testCreateComment() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createNote.org",
        "displayNameForCreateNote", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-note-action");
    dummyDataStore.store(article);

    String title = "bawitdaba";
    String body = "My name is Kiiiiiiiiiiiiiiiiiiiid - Kid Rock!";
    String ciStatement = "I work for the Skoll Foundation";
    action.setCommentTitle(title);
    action.setComment(body);
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(true);
    action.setCiStatement(ciStatement);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action returned field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    assertNotNull(action.getAnnotationId(), "action had null annotation id");
    Long id = Long.valueOf(action.getAnnotationId());
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, id);
    assertNotNull(storedAnnotation, "Didn't store annotation to the db");
    assertEquals(storedAnnotation.getType(), AnnotationType.COMMENT, "Created annotation with incorrect type");
    assertEquals(storedAnnotation.getTitle(), title, "stored annotation had incorrect title");
    assertEquals(storedAnnotation.getBody(), body, "stored annotation had incorrect body");
    assertEquals(storedAnnotation.getCompetingInterestBody(), ciStatement, "stored annotation had incorrect ci statement");
    assertNotNull(storedAnnotation.getAnnotationUri(), "didn't generate an annotation uri");
  }

  @Test
  public void testCreateInlineComment() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createInlineNote.org",
        "displayNameForCreateInlineNote", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-inlineNote-action");
    dummyDataStore.store(article);

    String title = "Red-Handed";
    String body = "Ruby suddenly disappears after being seen with a mysterious man. " +
        "Meanwhile, the back story of Red Riding hood is revealed. ";
    String expectedXpointer = "id:doi-for-create-inlineNote-action#xpointer(string-range%28%2Farticle%5B1%5D%2Fbody%5B1%5D%2Fsec%5B1%5D%2Fp%5B3%5D%2C+%27%27%2C+107%2C+533%29%5B1%5D)";
    action.setCommentTitle(title);
    action.setComment(body);
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(false);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action returned field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    assertNotNull(action.getAnnotationId(), "action had null annotation id");
    Long id = Long.valueOf(action.getAnnotationId());
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, id);
    assertNotNull(storedAnnotation, "Didn't store annotation to the db");
    assertEquals(storedAnnotation.getType(), AnnotationType.COMMENT, "Created annotation with incorrect type");
    assertEquals(storedAnnotation.getTitle(), title, "stored annotation had incorrect title");
    assertEquals(storedAnnotation.getBody(), body, "stored annotation had incorrect body");
    assertNull(storedAnnotation.getCompetingInterestBody(), "stored annotation had incorrect ci statement");
    assertNotNull(storedAnnotation.getAnnotationUri(), "didn't generate an annotation uri");

  }

  @Test
  public void testCreateWithNoBody() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithNoBody.org",
        "displayNameForCreateWithNoBody", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-WithNoBody-action");
    dummyDataStore.store(article);

    action.setCommentTitle("test title");
    action.setComment("");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(false);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return input");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return correct number of field errors");
    assertNotNull(action.getFieldErrors().get("comment"), "action didn't return field errors on comment box");
  }

  @Test
  public void testCreateWithCiCheckedAndNoCiStatement() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithCiProblems.org",
        "displayNameForCreateWithCiProblems", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-WithCiProblems-action");
    dummyDataStore.store(article);

    action.setCommentTitle("test title");
    action.setComment("foo");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(true);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return input");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return correct number of field errors");
    assertNotNull(action.getFieldErrors().get("statement"), "action didn't return field errors on ci statment box");
  }

  @Test
  public void testCreateWithProfaneTitle() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithProfaneTitle.org",
        "displayNameForCreateWithProfaneTitle", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-withProfaneTitle-action");
    dummyDataStore.store(article);

    action.setCommentTitle("ass");
    action.setComment("A clean body");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(false);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return success");
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return expected number of field errors");
    assertNotNull(action.getFieldErrors().get("commentTitle"), "Action didn't return field error for title");
  }

  @Test
  public void testCreateWithProfaneBody() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithProfaneBody.org",
        "displayNameForCreateWithProfaneBody", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-withProfaneBody-action");
    dummyDataStore.store(article);

    action.setCommentTitle("A very clean title");
    action.setComment("ass");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(false);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return input");
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return expected number of field errors");
    assertNotNull(action.getFieldErrors().get("comment"), "Action didn't return field error for comment body");
  }

  @Test
  public void testCreateWithProfaneCiStatement() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithProfaneCiStatement.org",
        "displayNameForCreateWithProfaneCiStatement", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-withProfaneCiStatement-action");
    dummyDataStore.store(article);

    action.setCommentTitle("A clean title");
    action.setComment("A clean body");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(true);
    action.setCiStatement("ass");

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return input");
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return expected number of field errors");
    assertNotNull(action.getFieldErrors().get("ciStatement"), "Action didn't return field error for ci statement");
  }

  @Test
  public void testCreateWithLongTitle() throws Exception {
    UserProfile creator = new UserProfile(
        "email@createWithLongTitle.org",
        "displayNameForCreateWithLongTitle", "pass");
    dummyDataStore.store(creator);
    login(creator);
    Article article = new Article("id:doi-for-create-withLongTitle-action");
    dummyDataStore.store(article);

    action.setCommentTitle("IT is pretty well established that animals are capable of suffering; we’ve come a long " +
        "way since Descartes famously compared them to nonfeeling machines put on earth to serve man. (Rousseau " +
        "later countered this, saying that animals shared \"some measure\" of human nature and should partake of " +
        "\"natural right.\") No matter where you stand on this spectrum, you probably agree that it’s a noble " +
        "goal to reduce the level of the suffering of animals raised for meat in industrial conditions. " +
        "And this puts it over 500");
    action.setComment("test body");
    action.setTarget(article.getDoi());
    action.setIsCompetingInterest(false);
    action.setCiStatement(null);

    String result = action.execute();
    assertEquals(result, Action.INPUT, "action didn't return input");
    assertEquals(action.getFieldErrors().size(), 1, "Action didn't return expected number of field errors");
    assertNotNull(action.getFieldErrors().get("commentTitle"), "Action didn't return field error for title");
  }
}
