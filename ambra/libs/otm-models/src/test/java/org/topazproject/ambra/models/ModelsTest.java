/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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
package org.topazproject.ambra.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.ambra.models.support.fedora.AnnotationFedoraBlob;
import org.topazproject.ambra.models.support.fedora.AnnotationFedoraBlobFactory;
import org.topazproject.fedora.otm.FedoraBlob;
import org.topazproject.fedora.otm.FedoraBlobStore;
import org.topazproject.fedora.otm.FedoraBlobFactory;

/**
 * Test the model definitions.
 *
 * @author Pradeep Krishnan
 */
public class ModelsTest {
  private SessionFactory factory;
  private Class<?>[]        classes =
    new Class<?>[] {
                  Annotation.class, Annotea.class, Article.class, AuthenticationId.class,
                  Category.class, Citation.class, Comment.class, Correction.class, DublinCore.class,
                  FoafPerson.class, ObjectInfo.class, Ambra.class, Rating.class, RatingContent.class,
                  RatingSummary.class, RatingSummaryContent.class, RelatedArticle.class,
                  License.class, Reply.class, ReplyThread.class, UserAccount.class, UserPreference.class,
                  UserPreferences.class, UserProfile.class, UserRole.class, Journal.class, Issue.class,
                  Aggregation.class, EditorialBoard.class, Correction.class, FormalCorrection.class,
                  Retraction.class, StreamedBlob.class, ReplyBlob.class, AnnotationBlob.class,
                  Syndication.class
    };

  /**
   * Tests if the models we define can be pre-loaded.
   *
   * @throws OtmException on an error
   */

  @BeforeClass
  public void setUp() {
    factory = new SessionFactoryImpl();
  }

  @Test
  public void preloadTest() throws OtmException {
    factory.addAlias("annoteaBodyId", "info:fedora/");
    for (Class<?> c : classes)
      factory.preload(c);

    factory.validate();

    FedoraBlob rb;
    String fedoraUri = "http://localhost:9090/fedora/services/management";
    FedoraBlobStore  blobStore = new FedoraBlobStore(fedoraUri, "fedoraAdmin", "fedoraAdmin");

    blobStore.addBlobFactory(new AnnotationFedoraBlobFactory("Ambra", "info:fedora/"));

    rb = toBlob(blobStore, factory.getClassMetadata(AnnotationBlob.class), "info:fedora/Ambra:42");
    assert rb instanceof AnnotationFedoraBlob;
    AnnotationFedoraBlob b = (AnnotationFedoraBlob) rb;
    assert "text/plain;UTF-8".equals(b.getContentType());
    assert "Annotation".equals(b.getContentModel());
    assert "Annotation Body".equals(b.getDatastreamLabel());
    assert "Ambra:42".equals(b.getPid());
    assert "BODY".equals(b.getDsId());
    rb = toBlob(blobStore, factory.getClassMetadata(ReplyBlob.class), "info:fedora/Ambra:42");
    assert rb instanceof AnnotationFedoraBlob;
    b = (AnnotationFedoraBlob) rb;
    assert "text/plain;UTF-8".equals(b.getContentType());
    assert "Reply".equals(b.getContentModel());
    assert "Reply Body".equals(b.getDatastreamLabel());
    assert "Ambra:42".equals(b.getPid());
    assert "BODY".equals(b.getDsId());

    ClassMetadata cm = factory.getClassMetadata(Rating.class);
    RdfMapper m = cm.getMapperByUri(factory, factory.expandAlias("annotea:body"), false, null);
    assert m != null;
    assert m.isAssociation();
    assert m.getAssociatedEntity().equals("RatingContent");
  }

  private FedoraBlob toBlob(FedoraBlobStore bs, ClassMetadata cm, String id) throws OtmException {
    FedoraBlobFactory bf   = bs.mostSpecificBlobFactory(id);

    if (bf == null)
      throw new OtmException("Can't find a blob factory for " + id);

    return bf.createBlob(cm, id, null, null);
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
    FormalCorrection correction = new FormalCorrection();
    Retraction retraction = new Retraction();
    Citation citation = new Citation();

    File articleTmpFile = File.createTempFile("org.topazproject.ambra.models-serializationTest-", ".obj");
    FileOutputStream fos = new FileOutputStream(articleTmpFile);
    ObjectOutputStream out =  new ObjectOutputStream(fos);
    out.writeObject(article);
    out.writeObject(citation);
    out.writeObject(correction);
    out.writeObject(retraction);
    out.close();

    // restore from file
    FileInputStream fis = new FileInputStream(articleTmpFile);
    ObjectInputStream in = new ObjectInputStream(fis);
    Article restoredArticle = (Article) in.readObject();
    Citation restoredCitation = (Citation) in.readObject();
    FormalCorrection restoredCorrection = (FormalCorrection) in.readObject();
    Retraction restoredRetraction = (Retraction) in.readObject();
    in.close();

    assert restoredArticle != null;
    assert restoredCitation != null;
    assert restoredCorrection != null;
    assert restoredRetraction != null;
  }
}
