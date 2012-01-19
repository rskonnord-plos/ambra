/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.annotation.service;

import org.plos.ApplicationException;
import org.plos.annotation.Commentary;
import org.plos.user.service.UserService;
import org.plos.models.ArticleAnnotation;
import org.plos.models.Reply;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

/**
 * A kind of utility class to convert types between topaz and plosone types fro Annotations and Replies
 */
public class AnnotationConverter {
  private UserService userService;

  /**
   * @param annotations an array of annotations
   * @return an array of Annotation objects as required by the web layer
   * @throws org.plos.ApplicationException
   */
  @Transactional(readOnly = true)
  public WebAnnotation[] convert(final ArticleAnnotation[] annotations) throws ApplicationException {
    final WebAnnotation wa[]  = new WebAnnotation[annotations.length];

    for (int i = 0; i < annotations.length; i++)
      wa[i] = convert(annotations[i]);

    return wa;
  }

  /**
   * @param annotation annotation
   * @return the Annotation
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public WebAnnotation convert(final ArticleAnnotation annotation) throws ApplicationException {
    return new WebAnnotation(annotation, userService) {
      protected String getOriginalBodyContent() throws ApplicationException {
        try {
          return annotation.getBody().getText();
        } catch (Exception e) {
          throw new ApplicationException("Failed to load annotation body", e);
        }
      }
    };

  }

  /**
   * Creates a hierarchical array of replies based on the flat array passed in.
   * 
   * @param replies an array of Replies
   * @return an array of Reply objects as required by the web layer
   * @throws org.plos.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public WebReply[] convert(final Reply[] replies) throws ApplicationException {
    return convert (replies, null);
  }

  /**
   * Creates a hierarchical array of replies based on the flat array passed in.
   * Fills in Commentary com parameter as appropriate
   * 
   * @param replies
   * @param com
   * @return the hierarchical replies
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public WebReply[] convert(final Reply[] replies, Commentary com) throws ApplicationException {
    final List<WebReply> plosoneReplies = new ArrayList<WebReply>();
    final LinkedHashMap<String, WebReply> repliesMap = new LinkedHashMap<String, WebReply>(replies.length);
    int numReplies = replies.length;
    String latestReplyTime = null;

    String annotationId = null;
    if (numReplies > 0) {
      annotationId = replies[0].getRoot();
      latestReplyTime = replies[numReplies -1].getCreatedAsString();
    }

    for (final Reply reply : replies) {
      final WebReply convertedObj = convert(reply);
      repliesMap.put(reply.getId().toString(), convertedObj);

      final String replyTo = reply.getInReplyTo();
      //Setup the top level replies
      if (replyTo.equals(annotationId)) {
        plosoneReplies.add(convertedObj);
      }
    }

    //Thread the replies in a parent/child structure
    for (final Map.Entry<String, WebReply> entry : repliesMap.entrySet()) {
      final WebReply savedReply = entry.getValue();
      final String inReplyToId = savedReply.getInReplyTo();

      if (!inReplyToId.equals(annotationId)) {
        final WebReply inReplyTo = repliesMap.get(inReplyToId);
        // If the replies are in reply to another reply and that reply isn't present
        // then just add them to the top. This only happens when the array passed in is a subtree
        if (null == inReplyTo) {
          plosoneReplies.add(savedReply);
        } else {
          inReplyTo.addReply(savedReply);
         }
      }
    }

    WebReply[] returnArray = plosoneReplies.toArray(new WebReply[plosoneReplies.size()]);
    if (com != null) {
      com.setReplies(returnArray);
      com.setLastModified(latestReplyTime);
      com.setNumReplies(numReplies);
    }
    return returnArray;
  }

  /**
   * @param reply reply
   * @return the reply for the web layer
   * @throws ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public WebReply convert(final Reply reply) throws ApplicationException {

    return new WebReply(reply, userService) {
      protected String getOriginalBodyContent() throws ApplicationException {
        try {
          return reply.getBody().getText();
        } catch (Exception e) {
          throw new ApplicationException("Failed to load reply body", e);
        }
      }

    };
  }

  /**
   * @return Returns the userService.
   */
  protected UserService getUserService() {
    return userService;
  }

  /**
   * @param userService The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
