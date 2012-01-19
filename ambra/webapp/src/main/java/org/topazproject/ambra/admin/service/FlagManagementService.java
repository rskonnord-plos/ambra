/* $HeadURL::                                                                            $
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

package org.topazproject.ambra.admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.Flag;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.Annotation;

import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

/**
 * Manage flagged annotations on the server. This allows for administration follow-up of community
 * feedback/comments etc.
 *
 * @author alan
 */
public class FlagManagementService {
  private static final Logger log = LoggerFactory.getLogger(FlagManagementService.class);
  private AnnotationService annotationService;
  private AnnotationConverter converter;
  private Session session;

  @Transactional(readOnly = true)
  public Collection<FlaggedCommentRecord> getFlaggedComments() throws ApplicationException {
    ArrayList<FlaggedCommentRecord> commentrecords = new ArrayList<FlaggedCommentRecord>();

    // Check flags on Annotation (and therefore Ratings) and Replies.
    // TODO: add 'instanceof' in Oql (eg. 'a instanceof Annotation or a instanceof Reply')
    StringBuilder sb = new StringBuilder(500);
    sb.append("select f.id, a from Comment f, Annotea a where f.annotates = a and ((");
    for (String type : session.getSessionFactory().getClassMetadata(Annotation.class).getTypes()) {
      sb.append("a.<rdf:type> = <").append(type).append("> and ");
    }
    sb.setLength(sb.length()-5);
    sb.append(") or (");
    for (String type : session.getSessionFactory().getClassMetadata(Reply.class).getTypes()) {
      sb.append("a.<rdf:type> = <").append(type).append("> and ");
    }
    sb.setLength(sb.length()-5);
    sb.append("));");

    Query query = session.createQuery(sb.toString());
    Results r = query.execute();

    while (r.next()) {
      String id = r.getString(0);
      Flag flag = null;
      try {
        ArticleAnnotation ann = annotationService.getArticleAnnotation(id);
        if (ann != null)
          flag = new Flag(converter.convert(ann, true, true));
      } catch (SecurityException e) {
        if (log.isInfoEnabled())
          log.info("No permission to load Flag: " + id, e);
      }

      if (flag == null)
        continue;

      Annotea<?> a = (Annotea<?>) r.get(1);
      String title = (a instanceof Rating) ? ((Rating)a).getBody().getCommentTitle() : a.getTitle();
      String root  = (a instanceof Reply) ? ((Reply)a).getRoot() : null;
      String wt    = a.getWebType();

      boolean isGeneralComment = (a instanceof Annotation) && ((Annotation)a).getContext() == null;

      String reasonCode;
      String commentBody;
      boolean isBroken = false;
      try {
        reasonCode = flag.getReasonCode();
      } catch (ApplicationException e) {
        isBroken = true;
        reasonCode = "-missing-";
        log.error("Error parsing reason code for flag " + flag.getId(), e);
      }
      
      try {
        commentBody = flag.getComment();
      } catch (ApplicationException e) {
        isBroken = true;
        commentBody = "-missing-";
        log.error("Error parsing comment for flag " + flag.getId(), e);
      }

      FlaggedCommentRecord fcr = new FlaggedCommentRecord(flag.getId(), flag.getAnnotates(), title,
          commentBody, flag.getCreated(), flag.getCreatorName(), flag.getCreator(),
          root, reasonCode, wt, isGeneralComment, isBroken);
      commentrecords.add(fcr);
    }

    Collections.sort(commentrecords);
    return commentrecords;
  }

  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter converter) {
    this.converter = converter;
  }
}
