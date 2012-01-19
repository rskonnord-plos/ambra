/* $HeadURL$
 * $Id$
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

package org.topazproject.ambra.admin.action;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.capture;
import org.easymock.classextension.IMocksControl;
import org.easymock.Capture;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.FlagManagementService;
import org.topazproject.ambra.admin.service.FlaggedCommentRecord;
import org.topazproject.otm.Session;
import com.opensymphony.xwork2.Action;
import com.sun.xacml.PDP;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;

import java.net.URI;
import java.util.ArrayList;

/**
 * @author Dragisa Krsmanovic
 */
public class ProcessFlagsActionTest {


  @Test
  public void testTwoFlagsForSameFormalCorrection() throws Exception {

    String annotationId = "info:doi/123.456/annotation1";
    String flag1Id = "info:doi/123.456/flag1";
    String flag2Id = "info:doi/123.456/flag2";
    String[] flags = {
        flag1Id + "_" + annotationId,
        flag2Id + "_" + annotationId
    };


    Comment oldAnnotation = new Comment();
    oldAnnotation.setId(URI.create(annotationId));
    oldAnnotation.setBody(new AnnotationBlob());

    Comment flag1 = new Comment();
    flag1.setId(URI.create(flag1Id));
    flag1.setBody(new AnnotationBlob());

    Comment flag2 = new Comment();
    flag2.setId(URI.create(flag2Id));
    flag2.setBody(new AnnotationBlob());

    IMocksControl ctrl = createControl();
    Session session = ctrl.createMock(Session.class);
    PermissionsService permissionsService = ctrl.createMock(PermissionsService.class);

    DocumentManagementService documentManagementService = ctrl.createMock(DocumentManagementService.class);
    FlagManagementService flagManagementService = ctrl.createMock(FlagManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);

    PDP pdp = ctrl.createMock(PDP.class);
    expect(pdp.evaluate((EvaluationCtx) anyObject()))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    FormalCorrection newFormalCorrection = new FormalCorrection();
    newFormalCorrection.setId(URI.create(annotationId));
    newFormalCorrection.setBody(oldAnnotation.getBody());
    expect(session.get(ArticleAnnotation.class, annotationId))
        .andReturn(oldAnnotation).once()
        .andReturn(null).once()
        .andReturn(newFormalCorrection).times(2);
    expect(session.delete(oldAnnotation)).andReturn(annotationId).once();
    session.flush();
    expectLastCall().times(2);

    Capture<FormalCorrection> newAnnotationCapture = new Capture<FormalCorrection>();
    expect(session.saveOrUpdate(capture(newAnnotationCapture))).andReturn(annotationId).once();

    expect(session.get(ArticleAnnotation.class, flag1Id)).andReturn(flag1).once();
    expect(session.delete(flag1)).andReturn(flag1Id).once();
    permissionsService.cancelPropagatePermissions(eq(flag1Id), (String[]) anyObject());
    expectLastCall().once();
    permissionsService.cancelGrants(eq(flag1Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    permissionsService.cancelRevokes(eq(flag1Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();

    expect(session.get(ArticleAnnotation.class, flag2Id)).andReturn(flag2).once();
    expect(session.delete(flag2)).andReturn(flag2Id).once();
    permissionsService.cancelPropagatePermissions(eq(flag2Id), (String[]) anyObject());
    expectLastCall().once();
    permissionsService.cancelGrants(eq(flag2Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    permissionsService.cancelRevokes(eq(flag2Id), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();

    expect(documentManagementService.getPublishableFiles()).andReturn(new ArrayList<String>());
    expect(documentManagementService.getUploadableFiles()).andReturn(new ArrayList<String>());
    expect(flagManagementService.getFlaggedComments()).andReturn(new ArrayList<FlaggedCommentRecord>());
    expect(adminService.createJournalInfo()).andReturn(new AdminService.JournalInfo());

    AnnotationService annotationService = new AnnotationService();
    annotationService.setOtmSession(session);
    annotationService.setPermissionsService(permissionsService);
    annotationService.setAnnotationsPdp(pdp);
    ProcessFlagsAction action = new ProcessFlagsAction();
    action.setAnnotationService(annotationService);
    action.setDocumentManagementService(documentManagementService);
    action.setAdminService(adminService);
    action.setFlagManagementService(flagManagementService);
    action.setConvertToFormalCorrection(flags);

    ctrl.replay();
    assertEquals(action.execute(), Action.SUCCESS);
    ctrl.verify();
    assertNotNull(newAnnotationCapture.getValue().getBody());
  }
}
