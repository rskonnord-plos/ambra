/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import org.testng.annotations.Test;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

/**
 * Test the model definitions.
 *
 * @author Pradeep Krishnan
 */
public class ModelsTest {
  private SessionFactory factory = new SessionFactory();
  private Class[]        classes =
    new Class[] {
                  Annotation.class, Annotea.class, Article.class, AuthenticationId.class,
                  Category.class, Citation.class, Comment.class, DublinCore.class, FoafPerson.class,
                  ObjectInfo.class, PLoS.class, Rating.class, RatingContent.class,
                  RatingSummary.class, RatingSummaryContent.class, License.class, Reply.class,
                  ReplyThread.class, UserAccount.class, UserPreference.class, UserPreferences.class,
                  UserProfile.class, UserRole.class, Journal.class, Issue.class, Aggregation.class,
                  EditorialBoard.class
    };

  /**
   * Tests if the models we define can be pre-loaded.
   *
   * @throws OtmException on an error
   */
  @Test
  public void preloadTest() throws OtmException {
    for (Class c : classes)
      factory.preload(c);
  }
}
