/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.raptor;

import org.ambraproject.action.BaseTest;
import org.ambraproject.util.FileUtils;
import org.ambraproject.views.AcademicEditorView;
import org.apache.commons.httpclient.HttpClientMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;

public class RaptorServiceTest extends BaseTest {
  @Autowired
  protected RaptorServiceImpl raptorService;

  @DataProvider(name = "aedata")
  public Object[][] getRecords() {
    List<AcademicEditorView> results = new ArrayList<AcademicEditorView>();

    results.add(AcademicEditorView.builder()
      .setId("PONE-5895")
      .setName("Harald HHW Schmidt")
      .setLastName("Schmidt")
      .setInstitute("Maastricht University")
      .setCountry("NETHERLANDS")
      .setSubjects(new ArrayList<String>() {{
        add("Biomarkers");
        add("Cardiovascular");
        add("Cardiovascular pharmacology");
        add("Cardiovascular system");
        add("Drug discovery");
        add("Drug research and development");
        add("General pathology");
        add("Hypertension");
        add("Pathology");
        add("Pharmacodynamics");
        add("Stroke");
        add("Vascular biology");
      }})
      .setType("section_editor")
      .setJournalKey("PLoSONE")
      .build());

    results.add(AcademicEditorView.builder()
      .setId("PONE-9843")
      .setName("Mark Isalan")
      .setLastName("Isalan")
      .setInstitute("Center for Genomic Regulation")
      .setCountry("SPAIN")
      .setSubjects(new ArrayList<String>() {{
        add("Biochemistry");
        add("Bioengineering");
        add("Biological systems engineering");
        add("Biology");
        add("Bionanotechnology");
        add("Biotechnology");
        add("Computational biology");
        add("DNA-binding proteins");
        add("DNA structure");
        add("Gene networks");
        add("Genetic engineering");
        add("Genetics");
        add("Microarrays");
        add("Pattern formation");
        add("Protein engineering");
        add("Proteins");
        add("Proteomics");
        add("Regulatory networks");
        add("Synthetic biology");
        add("Systems biology");
        add("Theoretical biology");
      }})
      .setType("section_editor")
      .setJournalKey("PLoSONE")
      .build());

    return new Object[][] { { results.toArray() } };
  }


  @Test(dataProvider = "aedata")
  public void testGetAE(Object[] values) throws Exception {
    String text = FileUtils.getTextFromUrl(BaseTest.class.getClassLoader().getResource("aedata.csv"));
    HttpClientMock mockHttpClient = new HttpClientMock(200, text);

    raptorService.setHttpClient(mockHttpClient);

    List<AcademicEditorView> results = raptorService.getAcademicEditor();

    assertEquals(results.toArray(), values);
  }
}
