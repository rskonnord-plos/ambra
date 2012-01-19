/* $HeadURL::                                                                            $
 * $Id:AnnotationActionsTest.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.annotation.action;

import static com.opensymphony.xwork2.Action.SUCCESS;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.activation.URLDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.annotation.Context;
import org.topazproject.ambra.annotation.ContextFormatter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.AnnotationsPEP;
import org.topazproject.ambra.annotation.service.Flag;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.annotation.service.WebReply;
import org.topazproject.ambra.article.action.FetchArticleAction;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.permission.service.PermissionsService;

import com.opensymphony.xwork2.Action;

public class AnnotationActionsTest extends BaseAmbraTestCase {
  private static final String target = "http://here.is/viru";
  private final String body = "spmething that I always wanted to say about everything and more about nothing\n";
  private final String ciStatement = "I hearby disclose that I work for PLOS";
  final String ANON_PRINCIPAL = "anonymous:user/";
//  private final String target = "doi:10.1371/annotation/21";

  private static final Log log = LogFactory.getLog(AnnotationActionsTest.class);
  private static String annotationId = "doi:somedefaultvalue";
  private final String PROFANE_WORD = "BUSH";
//  private String testXmlTarget = "file:webapp/src/test/resources/test.xml";
  private String testXmlTarget = "info:doi/10.1371/journal.pone.0000008";


  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();

    String BASE_TEST_PATH = "src/test/resources/";
    final String resourceToIngest = BASE_TEST_PATH  + "pone.0000008.zip";

    final URL article = getAsUrl(resourceToIngest);

    try {
      testXmlTarget = getArticleOtmService().ingest(new Ingester(new URLDataSource(article)), false).
                                             getId().toString();
    } catch(DuplicateArticleIdException ex) {
      //article has already been ingested
    }

  }

  public void testSequencedTests() throws Exception {
    deleteAllAnnotations(target);
    deleteAllReplies(target);
    final String annotationId = createAnnotation(target, false);
    createReply(annotationId);
    listAnnotations(target);
    listReplies(annotationId);
  }

  public void deleteAllAnnotations(final String target) throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(SUCCESS, listAnnotationAction.execute());

    for (final WebAnnotation annotation : listAnnotationAction.getAnnotations()) {
      final String annotationId1 = annotation.getId();
      resetAnnotationPermissionsToDefault(annotationId1, ANON_PRINCIPAL);
      DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId1, false);
      assertEquals(SUCCESS, deleteAnnotationAction.deleteAnnotation());
    }
  }

  public void deleteAllReplies(final String target) throws Exception {
    DeleteReplyAction deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setRoot(target);
    deleteReplyAction.setInReplyTo(target);
    assertEquals(SUCCESS, deleteReplyAction.deleteReplyWithRootAndReplyTo());
  }

  public void testDeleteAnnotationsRemovesPrivateAnnotations() throws Exception {
    String annotationId = createAnnotation(target, false);
    DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, false);
    assertEquals(SUCCESS, deleteAnnotationAction.deleteAnnotation());
    log.debug("annotation deleted with id:" + annotationId);
    assertEquals(0, deleteAnnotationAction.getActionErrors().size());

    final GetAnnotationAction getAnnotationAction1 = getGetAnnotationAction();
    getAnnotationAction1.setAnnotationId(annotationId);
    assertEquals(Action.ERROR, getAnnotationAction1.execute());
  }

  public void testDeleteAnnotationsMarksPublicAnnotationsAsDeleted() throws Exception {
    String annotationId = createAnnotation(target, true);
    DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, false);
    assertEquals(SUCCESS, deleteAnnotationAction.deleteAnnotation());
    log.debug("annotation marked as deleted with id:" + annotationId);
    assertEquals(0, deleteAnnotationAction.getActionErrors().size());

    final WebAnnotation annotation = retrieveAnnotation(annotationId);

    resetAnnotationPermissionsToDefault(annotationId, ANON_PRINCIPAL);
  }

  public void testDeleteRepliesWithId() throws Exception {
    final String annotationId = createAnnotation(target, true);

    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, body);
    assertEquals(SUCCESS, createReplyAction.execute());
    final String replyId = createReplyAction.getReplyId();
    log.debug("annotation created with id:" + replyId);
    assertNotNull(replyId);

    DeleteReplyAction deleteReplyAction = getDeleteReplyAction();
    deleteReplyAction.setId(replyId);
    assertEquals(SUCCESS, deleteReplyAction.deleteReplyWithId());
    log.debug("annotation deleted with id:" + replyId);

    final WebReply reply = retrieveReply(replyId);

    resetAnnotationPermissionsToDefault(annotationId, ANON_PRINCIPAL);
  }

  public void listAnnotations(final String target) throws Exception {
    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    assertEquals(SUCCESS, listAnnotationAction.execute());
    assertEquals(1, listAnnotationAction.getAnnotations().length);
  }

  private String getContext(CreateAnnotationAction caa) throws Exception {
    return ContextFormatter.asXPointer(
        new Context(caa.getStartPath(), caa.getStartOffset(), caa.getEndPath(), caa.getEndOffset(), caa.getTarget()));
  }

  public String createAnnotation(final String target, final boolean publicVisibility) throws Exception {
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, ciStatement);
    final String context = getContext(createAnnotationAction);
    createAnnotationAction.setIsPublic(publicVisibility);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getCommentTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getComment());
    assertEquals(ciStatement, savedAnnotation.getCIStatement());

    return annotationId;
  }

  public void testCreatePrivateAnnotation() throws Exception {
    final String title = "AnnotationPrivate";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, ciStatement);
    final String context = getContext(createAnnotationAction);
    final boolean visibility = false;
    createAnnotationAction.setIsPublic(visibility);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getCommentTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getComment());
    assertEquals(ciStatement, savedAnnotation.getCIStatement());

    AnnotationActionsTest.annotationId = annotationId;
  }

  public void createReply(final String annotationId) throws Exception {
    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(SUCCESS, createReplyAction.execute());
    final String replyId = createReplyAction.getReplyId();
    log.debug("annotation created with id:" + replyId);
    assertNotNull(replyId);

    final GetReplyAction getReplyAction = getGetReplyAction();
    getReplyAction.setReplyId(replyId);
    assertEquals(SUCCESS, getReplyAction.execute());
    final WebReply savedReply = getReplyAction.getReply();
    assertEquals(annotationId, savedReply.getRoot());
    assertEquals(annotationId, savedReply.getInReplyTo());
    assertEquals(title, savedReply.getCommentTitle());
    assertEquals(body, savedReply.getComment());
  }

  public void testCreateThreadedReplies() throws Exception {
    final String replyId;
    {
      final String title = "Reply1 to annotation ";
      final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
      assertEquals(SUCCESS, createReplyAction.execute());
      replyId = createReplyAction.getReplyId();
      log.debug("reply created with id:" + replyId);
      assertNotNull(replyId);

      final GetReplyAction getReplyAction = getGetReplyAction();
      getReplyAction.setReplyId(replyId);
      assertEquals(SUCCESS, getReplyAction.execute());
      final WebReply savedReply = getReplyAction.getReply();
      assertEquals(annotationId, savedReply.getRoot());
      assertEquals(annotationId, savedReply.getInReplyTo());
      assertEquals(title, savedReply.getCommentTitle());

      assertEquals(body, savedReply.getComment());
    }

    {
      final String title = "Reply1 to Reply1";
      final String replyBody2 = "some text in response to the earlier teply";
      final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, replyId, title, "text/plain", replyBody2);
      assertEquals(SUCCESS, createReplyAction.execute());
      final String replyToReplyId = createReplyAction.getReplyId();
      log.debug("reply created with id:" + replyToReplyId);
      assertNotNull(replyToReplyId);

      final GetReplyAction getReplyAction = getGetReplyAction();
      getReplyAction.setReplyId(replyToReplyId);
      assertEquals(SUCCESS, getReplyAction.execute());
      final WebReply savedReply = getReplyAction.getReply();
      assertEquals(annotationId, savedReply.getRoot());
      assertEquals(replyId, savedReply.getInReplyTo());
      assertEquals(title, savedReply.getCommentTitle());

      assertEquals(replyBody2, savedReply.getComment());
    }

  }

  public void testListThreadedReplies() throws Exception {
    final String title = "threadedTitle for Annotation";
    final String threadedTitle = "Threaded reply test threadedTitle";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();

    class CreateReply {
      /** return replyId */
      public String execute(final String annotationId, final String replyToId, final String title, final String body) throws Exception {
        final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, replyToId, title, "text/plain", body);
        assertEquals(SUCCESS, createReplyAction.execute());
        return createReplyAction.getReplyId();
      }
    }

    final String replyA = new CreateReply().execute(annotationId, annotationId, threadedTitle, body);
    final String replyAA = new CreateReply().execute(annotationId, replyA, threadedTitle, body);
    final String replyAAA = new CreateReply().execute(annotationId, replyAA, threadedTitle, body);
    final String replyAAB = new CreateReply().execute(annotationId, replyAA, threadedTitle, body);
    final String replyB = new CreateReply().execute(annotationId, annotationId, threadedTitle, body);
    final String replyBA = new CreateReply().execute(annotationId, replyB, threadedTitle, body);
    final String replyBB = new CreateReply().execute(annotationId, replyB, threadedTitle, body);

    {//List the replies to the annotation
      final ListReplyAction listReplyAction = getListReplyAction();
      listReplyAction.setRoot(annotationId);
      listReplyAction.setInReplyTo(annotationId);
      assertEquals(SUCCESS, listReplyAction.listAllReplies());

      final WebReply[] replies = listReplyAction.getReplies();

      final Collection<String> list = new ArrayList<String>();
      boolean codeRan = false;
      for (final WebReply reply : replies) {
        final String testReplyId = reply.getId();
        list.add(testReplyId);
        if (testReplyId.equals(replyB)) {
          codeRan = true;
          assertEquals(2, reply.getReplies().length);
        }
      }

      assertTrue(codeRan);

      assertEquals(2, replies.length);
      assertTrue(list.contains(replyA));
      assertFalse(list.contains(replyAA));
      assertTrue(list.contains(replyB));
    }

    {//List the replies to the reply
      final ListReplyAction listReplyAction = getListReplyAction();
      listReplyAction.setRoot(annotationId);
      listReplyAction.setInReplyTo(replyA);
      assertEquals(SUCCESS, listReplyAction.listAllReplies());

      final WebReply[] replies = listReplyAction.getReplies();

      final Collection<String> list = new ArrayList<String>();
      boolean codeRan = false;
      for (final WebReply reply : replies) {
        final String testReplyId = reply.getId();
        list.add(testReplyId);
        if (testReplyId.equals(replyAA)) {
          codeRan = true;
          assertEquals(2, reply.getReplies().length);
        }
      }

      assertTrue(codeRan);

      assertEquals(1, replies.length);
      assertTrue(list.contains(replyAA));
      assertFalse(list.contains(replyB));
    }

    deleteAllReplies(annotationId);
  }

  public void listReplies(final String annotationId) throws Exception {
    final ListReplyAction listReplyAction = getListReplyAction();
    listReplyAction.setRoot(annotationId);
    listReplyAction.setInReplyTo(annotationId);
    assertEquals(SUCCESS, listReplyAction.execute());
    assertEquals(1, listReplyAction.getReplies().length);
  }

  public void testCreateAnnotationShouldFailDueToProfanityInBody() throws Exception {
    final String body = "something that I always wanted to say " + PROFANE_WORD;
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.hasErrors());
  }

  public void testCreateAnnotationShouldFailDueToProfanityInTitle() throws Exception {
    final String body = "something that I always wanted to say";
    final String title = "Annotation " + PROFANE_WORD;
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(Action.ERROR, createAnnotationAction.execute());
    assertTrue(createAnnotationAction.hasErrors());
  }

  public void testCreateReplyShouldFailDueToProfanityInBody() throws Exception {
    final String body = "something that I always wanted to say " + PROFANE_WORD;
    final String title = "Reply1";
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.ERROR, createReplyAction.execute());
    assertTrue(createReplyAction.hasErrors());
  }

  public void testCreateReplyShouldFailDueToProfanityInTitle() throws Exception {
    final String body = "something that I always wanted to say";
    final String title = "Reply1 " + PROFANE_WORD;
    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, "text/plain", body);
    assertEquals(Action.ERROR, createReplyAction.execute());
    assertTrue(createReplyAction.hasErrors());
  }

  public void testGetAnnotationShouldDeclawTheBodyContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always <div>document.write('Booooom');office.cancellunch('tuesday')</div>";
    final String declawedBody = "something that I always &lt;div&gt;document.write('Booooom');office.cancellunch('tuesday')&lt;/div&gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final WebAnnotation savedAnnotation = getAnnotationConverter()
               .convert(getAnnotationService().getArticleAnnotation(annotationId1), true, true);
    assertEquals(declawedBody, savedAnnotation.getComment());

  }

  public void testGetAnnotationShouldDeclawTheTitleDueToSecurityImplications() throws Exception {
    final String body = "something that I think";

    final String title = "Annotation1 <&>";
    final String declawedTitle = "Annotation1 &lt;&amp;&gt;";

    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, "text/plain", body);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId1 = createAnnotationAction.getAnnotationId();

    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId1);
    assertNotNull(savedAnnotation);
    assertEquals(declawedTitle, savedAnnotation.getCommentTitle());

  }

  public void testGetReplyShouldDeclawTheBodyContentDueToSecurityImplications() throws Exception {
    final String body = "something that I always & < >";
    final String declawedBody = "something that I always &amp; &lt; &gt;";
//    final String body = "something that I always $div$document.write('Booooom');office.cancellunch('tuesday')$/div$";
//    final String declawedBody = "something that I always dollardivdollardocument.write('Booooom');office.cancellunch('tuesday')dollar/divdollar";

    final String title = "Annotation1";

    final CreateReplyAction createAnnotationAction = getCreateReplyAction(annotationId, annotationId, title, body);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String id = createAnnotationAction.getReplyId();

    final WebReply savedReply = getAnnotationConverter()
                            .convert(getReplyService().getReply(id), true, true);
    assertEquals(declawedBody, savedReply.getComment());
  }

  public void testGetReplyShouldDeclawTheTitleContentDueToSecurityImplications() throws Exception {
    final String body = "something that I think";

    final String title = "reply <&>";
    final String declawedTitle = "reply &lt;&amp;&gt;";

    final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, title, body);
    assertEquals(SUCCESS, createReplyAction.execute());
    final String replyId1 = createReplyAction.getReplyId();

    final GetReplyAction getReplyAction = getGetReplyAction();
    getReplyAction.setReplyId(replyId1);
    assertEquals(SUCCESS, getReplyAction.execute());
    final WebReply savedReply = getReplyAction.getReply();
    assertNotNull(savedReply);
    assertEquals(declawedTitle, savedReply.getCommentTitle());
  }

  public void testPublicAnnotationShouldHaveTheRightGrantsAndRevokations() throws Exception {
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, ciStatement);
    final String context = getContext(createAnnotationAction);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getCommentTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getComment());
    assertEquals(ciStatement, savedAnnotation.getCIStatement());

    final AnnotationService annotationService = getAnnotationService();
    final PermissionsService permissionsService = getPermissionsService();
    annotationService.setPublicPermissions(annotationId);

    final WebAnnotation annotation = retrieveAnnotation(annotationId);

    final List<String> grantsList = Arrays.asList(permissionsService.listGrants(annotationId, Constants.Permission.ALL_PRINCIPALS));
    assertTrue(grantsList.contains(AnnotationsPEP.GET_ANNOTATION_INFO));

    final String currentUser = ANON_PRINCIPAL;

    final List<String> revokesList = Arrays.asList(permissionsService.listRevokes(annotationId, currentUser));
    assertTrue(revokesList.contains(AnnotationsPEP.DELETE_ANNOTATION));
    assertTrue(revokesList.contains(AnnotationsPEP.SUPERSEDE));
    resetAnnotationPermissionsToDefault(annotationId, currentUser);
  }

  private void resetAnnotationPermissionsToDefault(final String annotationId, final String currentUser) throws RemoteException {
    final PermissionsService permissionsService = getPermissionsService();
    //Cleanup - Reset the permissions so that these annotations can be deleted by other unit tests
    permissionsService.cancelRevokes(
            annotationId,
            new String[] {AnnotationsPEP.DELETE_ANNOTATION, AnnotationsPEP.SUPERSEDE},
            new String[] {currentUser}
    );

    permissionsService.cancelGrants(
            annotationId,
            new String[] {AnnotationsPEP.GET_ANNOTATION_INFO},
            new String[] {Constants.Permission.ALL_PRINCIPALS}
    );
  }

  public void testAnnotatedContentSpanningNodes() throws Exception {
//    final String testXml =
//      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
//      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
//      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

    final String target = testXmlTarget;

    final String startPath1    = "/doc/chapter/title";
    final String endPath1    = "/doc/chapter/para";
    final String context1Body = "My annotation content 1";
    final String title       = "Title";
    final String statement       = "Competing Interest statement";

    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    listAnnotationAction.execute();
    WebAnnotation[] annotations = listAnnotationAction.getAnnotations();

    for (final WebAnnotation annotation : annotations) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId(), true);
      deleteAnnotationAction.deleteAnnotation();
    }

    class AnnotationCreator {
      public String execute(final String target, final String startPath, final String endPath, final String title, final String body, final String statement) throws Exception {
        final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, statement);
        createAnnotationAction.setStartPath(startPath);
        createAnnotationAction.setStartOffset(3);
        createAnnotationAction.setEndPath(endPath);
        createAnnotationAction.setEndOffset(9);
        final String context = getContext(createAnnotationAction);
        log.debug("context = " + context);
        assertEquals(SUCCESS, createAnnotationAction.execute());
        return createAnnotationAction.getAnnotationId();
      }
    }

    final Collection<String> annotationIdList = new ArrayList<String>();
    annotationIdList.add(
            new AnnotationCreator().execute(target, startPath1, endPath1, title, context1Body, statement));

    final String annotatedContent = getFetchArticleService().getAnnotatedContent(target);
    for (final String annotationId : annotationIdList) {
      assertTrue(annotatedContent.contains(annotationId));
    }

    for (final String annotationId : annotationIdList) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, true);
      deleteAnnotationAction.deleteAnnotation();
    }
  }

  public void testAnnotatedContentInTheSameNode() throws Exception {
//    final String testXml =
//      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
//      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
//      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

//    String target = "http://localhost:9080/existingArticle/test.xml";
//    String target = "http://localhost:8080/ambra-webapp/article/fetchArticle.action?articleURI=info:doi/10.1371%2Fjournal.pone.0000008";
//    final String target = getArticleOtmService().getObjectURL("info:doi/10.1371/journal.pone.0000008", "XML");
    final String target = "info:doi/10.1371/journal.pone.0000008";
    log.debug("target =" + target);

    final String startPath1    = "/article[1]/body[1]/sec[2]/sec[1]/p[4]";
    final int startOffset1 = 1;
    final String endPath1    = startPath1;
    final int endOffset1 = 20;
    final String context1Body = "Content for the first annotation1";
    final String statement1 = "Content for the first competing interest statement";

    final String startPath2    = "/article[1]/body[1]/sec[2]/sec[2]/p[1]";
    final int startOffset2 = 1;
    final String endPath2    = startPath2;
    final int endOffset2 = 60;
    final String context2Body = "Content for the second annotation1";
    final String statement2 = "Content for the second competing interest statement";

    final String title       = "Title";

    final ListAnnotationAction listAnnotationAction = getListAnnotationAction();
    listAnnotationAction.setTarget(target);
    listAnnotationAction.execute();
    WebAnnotation[] annotations = listAnnotationAction.getAnnotations();

    for (final WebAnnotation annotation : annotations) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotation.getId(), true);
      deleteAnnotationAction.deleteAnnotation();
    }

    class AnnotationCreator {
      public String execute(final String target, final String startPath, final int startOffset, final String endPath, final int endOffset, final String title, final String body, final String statement) throws Exception {
        final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, statement);
        createAnnotationAction.setStartPath(startPath);
        createAnnotationAction.setStartOffset(startOffset);
        createAnnotationAction.setEndPath(endPath);
        createAnnotationAction.setEndOffset(endOffset);

        final String context = getContext(createAnnotationAction);
        log.debug("context = " + context);
        assertEquals(SUCCESS, createAnnotationAction.execute());
        return createAnnotationAction.getAnnotationId();
      }
    }

    final Collection<String> annotationIdList = new ArrayList<String>();
    annotationIdList.add(
            new AnnotationCreator().execute(target, startPath1, startOffset1, endPath1, endOffset1, title, context1Body, statement1));
    annotationIdList.add(
            new AnnotationCreator().execute(target, startPath2, startOffset2, endPath2, endOffset2, title, context2Body, statement2));

    final String annotatedContent = getFetchArticleService().getAnnotatedContent(target);

    for (final String annotationId : annotationIdList) {
      assertTrue(annotatedContent.contains(annotationId));
    }

    for (final String annotationId : annotationIdList) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, true);
      deleteAnnotationAction.deleteAnnotation();
    }

    if (true) { //to test if the xsl transformation worked fine and got any annotation in the output
      final FetchArticleAction fetchArticleAction = getFetchArticleAction();
      fetchArticleAction.setArticleURI(target);
      assertEquals(SUCCESS, fetchArticleAction.execute());

      final String transformedArticle = fetchArticleAction.getTransformedArticle();
      log.debug("Annotated content = " + transformedArticle);

      //As the body is not inlined by the current annotator.
      assertTrue(transformedArticle.contains(target));
    }
  }

  private WebReply retrieveReply(final String replyId) throws Exception {
    final GetReplyAction getReplyAction = getGetReplyAction();
    getReplyAction.setReplyId(replyId);
    assertEquals(SUCCESS, getReplyAction.execute());
    return getReplyAction.getReply();
  }

  private WebAnnotation retrieveAnnotation(final String annotationId) throws Exception {
    final GetAnnotationAction getAnnotationAction = getGetAnnotationAction();
    getAnnotationAction.setAnnotationId(annotationId);
    assertEquals(SUCCESS, getAnnotationAction.execute());
    return getAnnotationAction.getAnnotation();
  }

  private Flag retrieveFlag(final String flagId) throws Exception {
    final GetFlagAction getFlagAction = getGetFlagAction();
    getFlagAction.setFlagId(flagId);
    assertEquals(SUCCESS, getFlagAction.execute());
    return getFlagAction.getFlag();
  }

  private DeleteAnnotationAction getDeleteAnnotationAction(final String annotationId, final boolean deletePreceding) {
    final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction();
    deleteAnnotationAction.setAnnotationId(annotationId);
    return deleteAnnotationAction;
  }

  private CreateReplyAction getCreateReplyAction(final String root, final String inReplyTo, final String title, final String body) {
    return getCreateReplyAction(root, inReplyTo, title, "text/plain", body);
  }

  private CreateReplyAction getCreateReplyAction(final String root, final String inReplyTo, final String title, final String mimeType, final String body) {
    final CreateReplyAction createReplyAction = getCreateReplyAction();
    createReplyAction.setRoot(root);
    createReplyAction.setInReplyTo(inReplyTo);
    createReplyAction.setCommentTitle(title);
    createReplyAction.setMimeType(mimeType);
    createReplyAction.setComment(body);
    return createReplyAction;
  }

  private CreateAnnotationAction getCreateAnnotationAction(final String target, final String title, final String body, final String statement) {
    return getCreateAnnotationAction(target, title, "text/plain", body, statement);
  }

  private CreateAnnotationAction getCreateAnnotationAction(final String target, final String title, final String mimeType, final String body, final String statement) {
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction();
    createAnnotationAction.setCommentTitle(title);
    createAnnotationAction.setTarget(target);
    createAnnotationAction.setStartPath("/");
    createAnnotationAction.setStartOffset(1);
    createAnnotationAction.setEndPath("/");
    createAnnotationAction.setEndOffset(2);
    createAnnotationAction.setMimeType(mimeType);
    createAnnotationAction.setComment(body);
    createAnnotationAction.setCiStatement(statement);
    return createAnnotationAction;
  }

  public void testAnnotatedContentWithSimpleStringRange() throws Exception {
    final String testXml =
      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

    final String subject     = testXmlTarget;
    String       context1    = "foo:bar#xpointer(string-range(/,'Hello+world'))";
    String       context2    = "foo:bar#xpointer(string-range(/,'indeed,+wonderful'))";
    String       context3    = "foo:bar#xpointer(string-range(/,'world,+indeed'))";
    String       title       = "Title";
    AnnotationService service = getAnnotationService();
    WebAnnotation[] annotations = getAnnotationConverter()
             .convert(service.listAnnotations(subject, null), true, true);

    for (final WebAnnotation annotation : annotations) {
      service.deleteAnnotation(annotation.getId());
    }

    final Collection<String> annotationIdList = new ArrayList<String>();
    AmbraUser ambraUser = new AmbraUser("123");
    annotationIdList.add(
      service.createComment(subject, context1, null, title, "text/plain", "body", "statement",false, ambraUser));
    annotationIdList.add(
      service.createComment(subject, context2, null, title, "text/plain", "body", "statement", false, ambraUser));
    annotationIdList.add(
      service.createComment(subject, context3, null, title, "text/plain", "body", "statement", false, ambraUser));

    String annotatedContent = getFetchArticleService().getAnnotatedContent(subject);
    for (final String annotationId : annotationIdList) {
      assertTrue(annotatedContent.contains(annotationId));
    }

    for (final String annotationId : annotationIdList) {
      final DeleteAnnotationAction deleteAnnotationAction = getDeleteAnnotationAction(annotationId, true);
      deleteAnnotationAction.deleteAnnotation();
    }
  }

  public void testAnnotatedContentWithSimpleStringRangeLocAndLength() throws Exception {
    final String testXml =
      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

    final String subject     = testXmlTarget;
    String       context1    = "foo:bar#xpointer(string-range(/,'Hello+world'))";
    String       context2    = "foo:bar#xpointer(string-range(/doc/chapter/title,'',0,5)[1])";
    String       context3    = "foo:bar#xpointer(string-range(/,'world,+indeed'))";
    String       title       = "Title";
    AnnotationService service = getAnnotationService();
    WebAnnotation[] annotations = getAnnotationConverter()
              .convert(service.listAnnotations(subject, null), true, true);

    for (final WebAnnotation annotation : annotations) {
      service.deleteAnnotation(annotation.getId());
    }

    AmbraUser ambraUser = new AmbraUser("123");
    service.createComment(subject, context1, null, title, "text/plain", "body", "statement", false, ambraUser);
    service.createComment(subject, context2, null, title, "text/plain", "body", "statement", false, ambraUser);
    service.createComment(subject, context3, null, title, "text/plain", "body", "statement", false, ambraUser);

    String content = getFetchArticleService().getAnnotatedContent(subject);
    log.debug(content);

    annotations = getAnnotationConverter()
              .convert(service.listAnnotations(subject, null), true, true);
    for (final WebAnnotation annotation : annotations)
      service.deleteAnnotation(annotation.getId());
  }

  public void testFlagActions() throws Exception {
    //Create an annotation
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, ciStatement);
    createAnnotationAction.setIsPublic(true);
    final String context = getContext(createAnnotationAction);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    //Retrieve an annotation
    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());
    assertEquals(title, savedAnnotation.getCommentTitle());
    assertEquals(context, savedAnnotation.getContext());
    assertEquals(body, savedAnnotation.getComment());
    assertEquals(ciStatement, savedAnnotation.getCIStatement());

    final String reasonCode = "spam";
    final String flagComment = "This a viagra selling spammer. " +
            "We should do something about it. Maybe we should start giving away viagra for free.";

    {
      //Create a flag
      final String flagId = createFlag(reasonCode, annotationId, flagComment);

      //Retrieve a flagged annotation
      final WebAnnotation flaggedAnnotation = retrieveAnnotation(annotationId);
      assertEquals(target, flaggedAnnotation.getAnnotates());
      assertEquals(title, flaggedAnnotation.getCommentTitle());
      assertEquals(context, flaggedAnnotation.getContext());
      assertEquals(body, flaggedAnnotation.getComment());

      //Retrieve a flag
      final Flag flag = retrieveFlag(flagId);
      assertEquals(annotationId, flag.getAnnotates());
      assertEquals(flagComment, flag.getComment());
      assertEquals(reasonCode, flag.getReasonCode());

      //Delete a flag
      final DeleteFlagAction deleteFlagAction = getDeleteFlagAction();
      deleteFlagAction.setFlagId(flagId);
      assertEquals(SUCCESS, deleteFlagAction.execute());
      log.debug("Flag comment DELETED with id:" + flagId);

      //Retrieve a flag
      final Flag deletedFlag = retrieveFlag(flagId);
      assertEquals(annotationId, deletedFlag.getAnnotates());
      assertEquals(reasonCode, deletedFlag.getReasonCode());

      //Retrieve an annotation
      final WebAnnotation flaggedAnnotationAfterFlagDeleted = retrieveAnnotation(annotationId);
      assertEquals(target, flaggedAnnotationAfterFlagDeleted.getAnnotates());
    }

    {
      //Create a reply
      final String replyTitle = "ReplyToBeFlagged";
      final String replyComment = "some kind of reply to annotation";
      final CreateReplyAction createReplyAction = getCreateReplyAction(annotationId, annotationId, replyTitle, replyComment);
      assertEquals(SUCCESS, createReplyAction.execute());
      final String replyId = createReplyAction.getReplyId();

      //Retrieve a reply
      final WebReply reply = retrieveReply(replyId);

      //Create a flag against a reply
      final CreateFlagAction createReplyFlagAction = getCreateFlagAction(reasonCode, replyId, flagComment);
      assertEquals(SUCCESS, createReplyFlagAction.execute());
      final String flagAnnotationId = createReplyFlagAction.getAnnotationId();
      log.debug("Flag for reply created with id:" + flagAnnotationId);

      //Retrieve a flagged reply
      final WebReply flaggedReply = retrieveReply(replyId);
      assertEquals(annotationId, flaggedReply.getRoot());
      assertEquals(replyTitle, flaggedReply.getCommentTitle());
      assertEquals(replyComment, flaggedReply.getComment());

      //Retrieve a flag
      final Flag flag = retrieveFlag(flagAnnotationId);
      assertEquals(replyId, flag.getAnnotates());
      assertEquals(flagComment, flag.getComment());
      assertEquals(reasonCode, flag.getReasonCode());

      //Delete a flag
      final DeleteFlagAction deleteFlagAction = getDeleteFlagAction();
      deleteFlagAction.setFlagId(flagAnnotationId);
      assertEquals(SUCCESS, deleteFlagAction.execute());
      log.debug("Flag comment DELETED with id:" + flagAnnotationId);

      //Retrieve a flag
      final Flag deletedFlag = retrieveFlag(flagAnnotationId);
      assertEquals(replyId, deletedFlag.getAnnotates());
      assertEquals(reasonCode, deletedFlag.getReasonCode());

      //Retrieve the previously flagged reply
      final WebReply flaggedReplyAfterFlagDeleted = retrieveReply(replyId);
      assertEquals(annotationId, flaggedReplyAfterFlagDeleted.getRoot());

      log.debug("Reply with id:" + flagAnnotationId + " unflagged");
    }
  }

  public void testListFlags() throws Exception {
    //cleanup
    deleteAllAnnotations(target);

    //Create an annotation
    final String title = "Annotation1";
    final CreateAnnotationAction createAnnotationAction = getCreateAnnotationAction(target, title, body, ciStatement);
    createAnnotationAction.setIsPublic(true);
    assertEquals(SUCCESS, createAnnotationAction.execute());
    final String annotationId = createAnnotationAction.getAnnotationId();
    log.debug("annotation created with id:" + annotationId);
    assertNotNull(annotationId);

    //Retrieve an annotation
    final WebAnnotation savedAnnotation = retrieveAnnotation(annotationId);
    assertEquals(target, savedAnnotation.getAnnotates());

    final String reasonCode = "spam";
    final String flagComment = "This a resume dumping spammer.";

    final List<String> flagsCreated = new ArrayList<String>();

    //Create a few flags
    for (int i = 0; i < 3; i++) {
      final String flagId = createFlag(reasonCode, annotationId, flagComment);
      flagsCreated.add(flagId);
    }

    ListFlagAction listFlagAction = getListFlagAction();
    listFlagAction.setTarget(annotationId);
    assertEquals(SUCCESS, listFlagAction.execute());
    assertEquals(flagsCreated.size(), listFlagAction.getFlags().length);

    for (final Iterator it = flagsCreated.iterator(); it.hasNext();) {
      final String flagId = (String) it.next();
      final DeleteFlagAction deleteFlagAction = getDeleteFlagAction();
      deleteFlagAction.setFlagId(flagId);
      assertEquals(SUCCESS, deleteFlagAction.execute());
      it.remove();
    }

    listFlagAction = getListFlagAction();
    listFlagAction.setTarget(annotationId);
    assertEquals(SUCCESS, listFlagAction.execute());
    assertEquals(flagsCreated.size(), listFlagAction.getFlags().length);

    deleteAllAnnotations(annotationId);
  }

  private String createFlag(final String reasonCode, final String annotationId, final String flagComment) throws Exception {
    final CreateFlagAction createFlagAction = getCreateFlagAction(reasonCode, annotationId, flagComment);
    assertEquals(SUCCESS, createFlagAction.execute());
    final String flagId = createFlagAction.getAnnotationId();
    log.debug("Flag comment created with id:" + flagId);
    return flagId;
  }

  private CreateFlagAction getCreateFlagAction(final String reasonCode, final String annotationId, final String flagComment) {
    final CreateFlagAction createFlagAction = getCreateFlagAction();
    createFlagAction.setReasonCode(reasonCode);
    createFlagAction.setTarget(annotationId);
    createFlagAction.setComment(flagComment);
    return createFlagAction;
  }
}
