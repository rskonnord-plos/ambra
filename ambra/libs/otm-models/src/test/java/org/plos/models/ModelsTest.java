/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.plos.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import org.plos.models.support.fedora.AnnotationFedoraBlob;
import org.plos.models.support.fedora.AnnotationFedoraBlobFactory;

import org.testng.annotations.Test;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.fedora.otm.FedoraBlob;
import org.topazproject.fedora.otm.FedoraBlobStore;

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
                  EditorialBoard.class, Correction.class, FormalCorrection.class, Blob.class,
                  ReplyBlob.class, AnnotationBlob.class
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

    FedoraBlobStore  blobStore =
      new FedoraBlobStore("http://localhost:9090/fedora/services/management", "fedoraAdmin", "fedoraAdmin");
    blobStore.addBlobFactory(new AnnotationFedoraBlobFactory());

    FedoraBlob rb = blobStore.toBlob(factory.getClassMetadata(AnnotationBlob.class), 
                                       "info:fedora/PLoS:42", null, null);
    assert rb instanceof AnnotationFedoraBlob;
    AnnotationFedoraBlob b = (AnnotationFedoraBlob) rb;
    assert "text/plain;UTF-8".equals(b.getContentType());
    assert "Annotation".equals(b.getContentModel());
    assert "Annotation Body".equals(b.getDatastreamLabel());
    assert "PLoS:42".equals(b.getPid());
    assert "BODY".equals(b.getDsId());
    rb = blobStore.toBlob(factory.getClassMetadata(ReplyBlob.class), "info:fedora/PLoS:42", null, null);
    assert rb instanceof AnnotationFedoraBlob;
    b = (AnnotationFedoraBlob) rb;
    assert "text/plain;UTF-8".equals(b.getContentType());
    assert "Reply".equals(b.getContentModel());
    assert "Reply Body".equals(b.getDatastreamLabel());
    assert "PLoS:42".equals(b.getPid());
    assert "BODY".equals(b.getDsId());
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
