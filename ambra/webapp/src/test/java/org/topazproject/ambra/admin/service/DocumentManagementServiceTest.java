/*
 * $HeadURL$
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

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.IngestException;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.Zip;
import org.topazproject.otm.Blob;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.easymock.classextension.IMocksControl;
import static org.easymock.classextension.EasyMock.createStrictControl;
import static org.easymock.classextension.EasyMock.expect;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Dragisa Krsmanovic
 */
public class DocumentManagementServiceTest {


  @BeforeTest
  public void setUp() {
    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    System.setProperty("javax.xml.transform.Transformer", "net.sf.saxon.Controller");
  }

  @Test
  public void testIngest() throws IngestException, DuplicateArticleIdException, IOException,
      URISyntaxException {

    IMocksControl ctl = createStrictControl();
    Zip zip = ctl.createMock(Zip.class);
    Ingester ingester = ctl.createMock(Ingester.class);
    ArticleOtmService articleOtmService = ctl.createMock(ArticleOtmService.class);
    SyndicationService syndicationService = ctl.createMock(SyndicationServiceImpl.class);
    Article article = new Article();
    String articleId = "info:doi/1234.678/abcd";
    String articleFilename = "info_doi_1234_678_abcd.xml";
    String docDirectory = "./";
    article.setId(URI.create(articleId));

    Set<Representation> representations = new HashSet<Representation>();
    Representation representation = new Representation();
    representations.add(representation);
    representation.setId("info:doi/1234.678/representation");

    Blob blob = ctl.createMock(Blob.class);
    representation.setBody(blob);
    representation.setName("XML");
    article.setRepresentations(representations);


    expect(articleOtmService.ingest(ingester, false)).andReturn(article);
    expect(blob.getInputStream()).andReturn(getClass().getResourceAsStream("/article/article1.xml"));
    // methods in log call
    expect(ingester.getZip()).andReturn(zip).times(0,1);
    expect(zip.getName()).andReturn("zip name").times(0,1);

    //  TODO: ??? get an actual LIst of Syndications that have real values ???
    expect(syndicationService.createSyndications(articleId))
        .andReturn(new LinkedList<SyndicationService.SyndicationDTO>());

    ctl.replay();
    DocumentManagementService service = new DocumentManagementService();
    service.setArticleOtmService(articleOtmService);
    service.setSyndicationService(syndicationService);
    service.setXslTemplate("/crossref.xsl");
    service.setPlosDoiUrl("http://www.plos.org/test-doi-resolver");
    service.setPlosEmail("test@plos.org");
    service.setDocumentDirectory(docDirectory);

    try {
      assertEquals(service.ingest(ingester, false), article);
      File doiCrossRef = new File(articleFilename);
      assertTrue(doiCrossRef.exists());
      assertTrue(doiCrossRef.length() > 0);
    } finally {
      File doiCrossRef = new File(docDirectory, articleFilename);
      if (doiCrossRef.exists())
        doiCrossRef.delete();
    }
    ctl.verify();
  }
}
