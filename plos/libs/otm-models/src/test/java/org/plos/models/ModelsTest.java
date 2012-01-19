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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.Test;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;

/**
 * Test the model definitions.
 *
 * @author Pradeep Krishnan
 */
public class ModelsTest {
  private SessionFactory factory = new SessionFactoryImpl();
  private Class[]        classes =
    new Class[] {
                  Annotation.class, Annotea.class, Article.class, AuthenticationId.class,
                  Category.class, Citation.class, Comment.class, Correction.class, DublinCore.class,
                  FoafPerson.class, ObjectInfo.class, PLoS.class, Rating.class, RatingContent.class,
                  RatingSummary.class, RatingSummaryContent.class, RelatedArticle.class,
                  License.class, Reply.class,
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
  
  /**
   * Test if common models can be serialized, e.g. used in a serializable cache.
   *
   * @throws ClassNotFoundException on (de)serialization error.
   * @throws IOException on (de)serialization error.
   */
  @Test
  public void serializationTest() throws ClassNotFoundException, IOException {
    
    // persist to file
    Article article = new Article();
    Correction correction = new Correction();
    Citation citation = new Citation();

    File articleTmpFile = File.createTempFile("org.plos.models-serializationTest-", ".obj");
    FileOutputStream fos = new FileOutputStream(articleTmpFile);
    ObjectOutputStream out =  new ObjectOutputStream(fos);
    out.writeObject(article);
    out.writeObject(citation);
    out.writeObject(correction);
    out.close();
    
    // restore from file
    FileInputStream fis = new FileInputStream(articleTmpFile);
    ObjectInputStream in = new ObjectInputStream(fis);
    Article restoredArticle = (Article) in.readObject();
    Citation restoredCitation = (Citation) in.readObject();
    Correction restoredCorrection = (Correction) in.readObject();
    in.close();
    
    assert restoredArticle != null;
    assert restoredCitation != null;
    assert restoredCorrection != null;
  }
}
