/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.topazproject.ambra.admin.action;

import com.opensymphony.xwork2.Action;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import org.easymock.Capture;
import org.easymock.classextension.internal.MocksClassControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.FlagManagementService;
import org.topazproject.ambra.admin.service.FlaggedCommentRecord;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.otm.Session;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Dragisa Krsmanovic
 */
@ContextConfiguration
public class ProcessFlagsActionTest extends AbstractTestNGSpringContextTests {

  @Autowired
  protected MocksClassControl mocksController;
  @Autowired
  protected PDP mockPDP;
  @Autowired
  protected Session mockSession;
  @Autowired
  protected PermissionsService mockPermissionsService;
  @Autowired
  protected AnnotationService annotationService;

  @Test
  @DirtiesContext
  public void testTwoFlagsForSameFormalCorrection() throws Exception {

    String annotationId = "info:doi/123.456/annotation1";
    String flag1Id = "info:doi/123.456/flag1";
    String flag2Id = "info:doi/123.456/flag2";
    String[] flags = {
        flag1Id + "_" + annotationId,
        flag2Id + "_" + annotationId
    };


    Comment oldAnnotation = new Comment();
    String articleId = "articleId";
    oldAnnotation.setAnnotates(URI.create(articleId));
    oldAnnotation.setId(URI.create(annotationId));
    oldAnnotation.setBody(new AnnotationBlob());

    Comment flag1 = new Comment();
    flag1.setId(URI.create(flag1Id));
    flag1.setBody(new AnnotationBlob());

    Comment flag2 = new Comment();
    flag2.setId(URI.create(flag2Id));
    flag2.setBody(new AnnotationBlob());

    Article article = new Article();
    article.setId(oldAnnotation.getAnnotates());
    article.setDublinCore(new DublinCore());
    Citation articleCitation = new Citation();
    articleCitation.setCitationType("cit-type");
    article.getDublinCore().setBibliographicCitation(articleCitation);

    FlagManagementService flagManagementService = mocksController.createMock(FlagManagementService.class);
    AdminService adminService = mocksController.createMock(AdminService.class);

    expect(mockPDP.evaluate((EvaluationCtx) anyObject()))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    FormalCorrection newFormalCorrection = new FormalCorrection();
    newFormalCorrection.setId(URI.create(annotationId));
    newFormalCorrection.setBody(oldAnnotation.getBody());
    expect(mockSession.get(ArticleAnnotation.class, annotationId))
        .andReturn(oldAnnotation).once()
        .andReturn(null).once()
        .andReturn(newFormalCorrection).times(2);
    expect(mockSession.delete(oldAnnotation)).andReturn(annotationId).once();
    mockSession.flush();
    expectLastCall().times(2);


    expect(mockSession.get(Article.class, "articleId")).andReturn(article).once();
    expect(mockSession.saveOrUpdate(isA(Citation.class))).andReturn("citationId").once();
    Capture<FormalCorrection> newAnnotationCapture = new Capture<FormalCorrection>();
    expect(mockSession.saveOrUpdate(capture(newAnnotationCapture))).andReturn(annotationId).once();

    expect(mockSession.get(ArticleAnnotation.class, flag1Id)).andReturn(flag1).once();
    expect(mockSession.delete(flag1)).andReturn(flag1Id).once();
    mockPermissionsService.cancelPropagatePermissions(eq(flag1Id), (String[]) anyObject());
    expectLastCall().once();
    mockPermissionsService.cancelGrants(eq(flag1Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    mockPermissionsService.cancelRevokes(eq(flag1Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();

    expect(mockSession.get(ArticleAnnotation.class, flag2Id)).andReturn(flag2).once();
    expect(mockSession.delete(flag2)).andReturn(flag2Id).once();
    mockPermissionsService.cancelPropagatePermissions(eq(flag2Id), (String[]) anyObject());
    expectLastCall().once();
    mockPermissionsService.cancelGrants(eq(flag2Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    mockPermissionsService.cancelRevokes(eq(flag2Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();

    expect(flagManagementService.getFlaggedComments()).andReturn(new ArrayList<FlaggedCommentRecord>());
    expect(adminService.createJournalInfo(isA(String.class))).andReturn(new AdminService.JournalInfo());

    ManageFlagsAction action = new ManageFlagsAction();
    action.setAnnotationService(annotationService);
    action.setAdminService(adminService);
    action.setFlagManagementService(flagManagementService);
    action.setConvertToFormalCorrection(flags);
    Map requestAttributes = new HashMap();

    requestAttributes.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT,
                          new VirtualJournalContext("journal", "dfltJournal",  "http", 80, "localhost", 
                                                    "ambra-webapp", new ArrayList<String>()));
    action.setRequest(requestAttributes);

    mocksController.replay();
    assertEquals(action.processFlags(), Action.SUCCESS);
    mocksController.verify();
    assertNotNull(newAnnotationCapture.getValue().getBody());
  }
}
