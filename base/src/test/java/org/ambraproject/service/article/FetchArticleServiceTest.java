/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.article;

import org.ambraproject.action.BaseTest;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.service.xml.XMLService;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.article.ArticleInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FetchArticleServiceTest extends BaseTest {

  @Autowired
  protected FetchArticleService fetchArticleService;

  @Autowired
  protected ArticleService articleService;

  @Autowired
  protected FileStoreService fileStoreService;

  @Autowired
  protected XMLService xmlService;

  @DataProvider(name = "articlesForCorrespondingTest")
  public Object[][] getArticlesForCorrespondingTest()
  {
    //<corresp id="cor1">* E-mail:
    // <email xlink:type="simple">maud.hertzog@ibcg.biotoul.fr</email> (MH);
    // <email xlink:type="simple">philippe.chavrier@curie.fr</email> (PC)</corresp>
    ArticleInfo a1 = new ArticleInfo("info:doi/10.1371/journal.pone.0052627");
    List<String> authors1 = new ArrayList<String>() {{
      add("<a href=\"mailto:maud.hertzog@ibcg.biotoul.fr\">maud.hertzog@ibcg.biotoul.fr</a> (MH)");
      add("<a href=\"mailto:philippe.chavrier@curie.fr\">philippe.chavrier@curie.fr</a> (PC)");
    }};

    ArticleInfo a2 = new ArticleInfo("info:doi/10.1371/journal.pone.0023160");
    List<String> authors2 = new ArrayList<String>() {{
      add("<a href=\"mailto:bach@bu.edu\">bach@bu.edu</a>");
    }};

    ArticleInfo a3 = new ArticleInfo("info:doi/10.1371/image.ppat.v04.i11");
    List<String> authors3 = new ArrayList<String>();

    return new Object[][] { { a1, authors1 }, { a2, authors2 }, { a3, authors3 } };
  }

  @DataProvider(name = "snippetsForCorrespondingTest")
  public Object[][] getSnippetsForCorrespondingTest() throws FileNotFoundException, IOException
  {
    return new Object[][] {
      {
        "<corresp id=\"cor1\">* To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">david.schatz@yale.edu</email></corresp>\n",
        new String[] { "<a href=\"mailto:david.schatz@yale.edu\">david.schatz@yale.edu</a>" }
      },
      {
        "<corresp id=\"cor1\">* To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">jeisen@tigr.org</email></corresp>\n",
        new String[] { "<a href=\"mailto:jeisen@tigr.org\">jeisen@tigr.org</a>" }
      },
      {
        "<corresp id=\"cor1\">* E-mail: <email xlink:type=\"simple\">ningolia@fas.harvard.edu</email>\n",
        new String[] { "<a href=\"mailto:ningolia@fas.harvard.edu\">ningolia@fas.harvard.edu</a>" }
      },
      {
        "<corresp id=\"cor1\">* To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">jurka@girinst.org</email> (JJ), Email: <email xlink:type=\"simple\">larionov@mail.nih.gov</email> (VL)</corresp>\n",
        new String[] {
          "<a href=\"mailto:jurka@girinst.org\">jurka@girinst.org</a> (JJ)",
          "<a href=\"mailto:larionov@mail.nih.gov\">larionov@mail.nih.gov</a> (VL)" }
      },
      {
        "<corresp id=\\\"cor1\\\">* To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">jbroach@molbio.princeton.edu</email></corresp>\n",
        new String[] { "<a href=\"mailto:jbroach@molbio.princeton.edu\">jbroach@molbio.princeton.edu</a>", }
      },
      {
        "<corresp id=\"cor1\">*To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">zon@enders.tch.harvard.edu</email></corresp>\n",
        new String[] { "<a href=\"mailto:zon@enders.tch.harvard.edu\">zon@enders.tch.harvard.edu</a>", }
      },

      {
        "<corresp id=\"cor1\">* E-mail: <email xlink:type=\"simple\">Kopan@wustl.edu</email></corresp>\n",
        new String[] { "<a href=\"mailto:Kopan@wustl.edu\">Kopan@wustl.edu</a>",}
      },

      {
        "<corresp id=\"n101\">* To whom correspondence should be addressed. E-mail: <email xlink:type=\"simple\">nzr6@cdc.gov</email>\n",
        new String[] { "<a href=\"mailto:nzr6@cdc.gov\">nzr6@cdc.gov</a>", }
      },
      {
        "<corresp id=\"cor1\">* E-mail: <email xlink:type=\"simple\">jeff.leonard@oregonstate.edu</email></corresp>\n",
        new String[] { "<a href=\"mailto:jeff.leonard@oregonstate.edu\">jeff.leonard@oregonstate.edu</a>", }
      }
    };
  }

  @DataProvider(name = "articlesWithDummyAffils")
  public Object[][] getArticlesWithDummyAffils()
  {
    //Test an author with a suffix on their name
    ArticleInfo a1 = new ArticleInfo("info:doi/10.1371/journal.pone.0002879");
    List<AuthorView> authors1 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("John M.")
        .setSurnames("Logsdon")
        .setSuffix("Jr")
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:john-logsdon@uiowa.edu\">john-logsdon@uiowa.edu</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Biology test");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test corresponding author
    ArticleInfo a2 = new ArticleInfo("info:doi/10.1371/journal.pbio.1001335");
    List<AuthorView> authors2 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Mariano")
        .setSurnames("Carrión-Vázquez")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:mcarrion@cajal.csic.es\">mcarrion@cajal.csic.es</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Instituto Cajal"); add("Instituto Madrileño");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test deceased author
    ArticleInfo a3 = new ArticleInfo("info:doi/10.1371/journal.pntd.0001165");
    List<AuthorView> authors3 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Nicholas J. S.")
        .setSurnames("Lwambo")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(true)
        .setCorresponding(null)
        .setRelatedFootnote(false)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("National Institute for Medical Research, Mwanza Medical Research Centre, Mwanza, Tanzania");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //additional sets of equally contributing authors.
    ArticleInfo a4 = new ArticleInfo("info:doi/10.1371/journal.pone.0023160");
    List<AuthorView> authors4 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Markus M.")
        .setSurnames("Bachschmid")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(true)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:bach@bu.edu\">bach@bu.edu</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Vascular Biology Section, Boston University Medical Center, Boston, Massachusetts, United States of America");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{ add("<p><span class=\"rel-footnote\">¶</span>These authors also contributed equally to this work.</p>"); }})
        .build());

      add(AuthorView.builder()
        .setGivenNames("David R.")
        .setSurnames("Pimental")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setRelatedFootnote(true)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Myocardial Biology Unit, Boston University Medical Center, Boston, Massachusetts, United States of America");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"rel-footnote\">¶</span>These authors also contributed equally to this work.</p>");
        }})
        .build());
    }};

    //Test alternate address
    ArticleInfo a5 = new ArticleInfo("info:doi/10.1371/journal.pone.0020568");
    List<AuthorView> authors5 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Oliver")
        .setSurnames("Liesenfeld")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setRelatedFootnote(false)
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: Roche Molecular Diagnostics, Pleasanton, California, United States of America");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Institute of Microbiology and Hygiene, Charité Universitätsmedizin Berlin, Berlin, Germany");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Iana")
        .setSurnames("Parvanova")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setRelatedFootnote(false)
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: Bavarian Research Alliance GmbH (BayFOR), Munich, Germany");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Institute for Genetics, University of Cologne, Cologne, Germany");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test 'other' author attribute
    ArticleInfo a6 = new ArticleInfo("info:doi/10.1371/journal.pone.0032315");
    List<AuthorView> authors6 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("the Danish SAB Study Group Consortium")
        .setSurnames(null)
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(null)
        .setRelatedFootnote(false)
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p>Membership of the Danish SAB Study Group Consortium is provided in the Acknowledgments.</p>");
        }})
        .build());
    }};

    //Additional test for corresponding author
    ArticleInfo a7 = new ArticleInfo("info:doi/10.1371/journal.pmed.0020073");
    List<AuthorView> authors7 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("William")
        .setSurnames("Pao")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(true)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">*</span>To whom correspondence should be addressed. E-mail: <a href=\"mailto:paow@mskcc.org\">paow@mskcc.org</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Program in Cancer Biology and Genetics, Memorial Sloan-Kettering Cancer Center, New York, New York, United States of America");
          add("Thoracic Oncology Service, Department of Medicine, Memorial Sloan-Kettering Cancer Center, New York, New York, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test for articles with multiple corresponding authors
    ArticleInfo a8 = new ArticleInfo("info:doi/10.1371/journal.pone.0029914");
    List<AuthorView> authors8 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Monica E.")
        .setSurnames("Embers")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:members@tulane.edu\">members@tulane.edu</a> (MEE); <a href=\"mailto:Philipp@tulane.edu\">Philipp@tulane.edu</a> (MTP)")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Divisions of Bacteriology & Parasitology, Tulane National Primate Research Center, Tulane University Health Sciences Center, Covington, Louisiana, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Mario T.")
        .setSurnames("Philipp")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:members@tulane.edu\">members@tulane.edu</a> (MEE); <a href=\"mailto:Philipp@tulane.edu\">Philipp@tulane.edu</a> (MTP)")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Divisions of Bacteriology & Parasitology, Tulane National Primate Research Center, Tulane University Health Sciences Center, Covington, Louisiana, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test for articles with 'on behalf of' node defined
    ArticleInfo a9 = new ArticleInfo("info:doi/10.1371/journal.pone.0047391");
    List<AuthorView> authors9 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("David")
        .setSurnames("Dalmau")
        .setSuffix(null)
        .setOnBehalfOf("on behalf of Busia OR Study Group")
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:ddalmau@mutuaterrassa.cat\">ddalmau@mutuaterrassa.cat</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Hospital Universitari MútuaTerrassa, Medicine Department, Terrassa, Spain");
          add("Fundació Docència i Recerca MutuaTerrassa, Terrassa, Spain");
        }})
        .setCustomFootnotes(null)
        .build());
    }};

    //Test for articles with 'on behalf of' node defined + plus extra data
    ArticleInfo a10 = new ArticleInfo("info:doi/10.1371/journal.pone.0050788");
    List<AuthorView> authors10 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("François")
        .setSurnames("Goffinet")
        .setSuffix(null)
        .setOnBehalfOf("for the EVAPRIMA group")
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setRelatedFootnote(false)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("INSERM UMR S953, Epidemiological Research Unit on Perinatal Health and Women’s and Children’s Health, Pierre et Marie Curie University, Paris, France");
          add("Department of Obstetrics and Gynaecology, Port-Royal Maternity, Cochin Saint-Vincent-de-Paul Hospital, Assistance Publique des Hopitaux de Paris, Université Paris Descartes, Sorbonne Paris Cité, Paris, France");
          add("Premup Foundation, Paris, France");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p>Membership of the EVAPRIMA group is provided in the Acknowledgments.</p>");
        }})
        .build());
    }};

    //Test for articles with multiple corresponding authors
    ArticleInfo a11 = new ArticleInfo("info:doi/10.1371/journal.pone.0047597");
    List<AuthorView> authors11 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Allison R.")
        .setSurnames("Baker")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(true)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: Clinical Services Group, Hospital Corporations of America, Nashville, Tennessee, United States of America");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Epidemiology and Biostatistics, Case Western Reserve University, Cleveland, Ohio, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Feiyou")
        .setSurnames("Qiu")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(true)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Epidemiology and Biostatistics, Case Western Reserve University, Cleveland, Ohio, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("April Kaur")
        .setSurnames("Randhawa")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Medicine, University of Washington School of Medicine, Seattle, Washington, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("David J.")
        .setSurnames("Horne")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Medicine, University of Washington School of Medicine, Seattle, Washington, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Mark D.")
        .setSurnames("Adams")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: J. Craig Venter Institute, San Diego, California, United States of America");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Genetics and Center for Proteomics and Bioinformatics, Case Western Reserve University, Cleveland, Ohio, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Muki")
        .setSurnames("Shey")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("South African Tuberculosis Vaccine Initiative, Institute of Infectious Diseases and Molecular Medicine and School of Child and Adolescent Health, University of Cape Town, South Africa");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Jill")
        .setSurnames("Barnholtz-Sloan")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Epidemiology and Biostatistics, Case Western Reserve University, Cleveland, Ohio, United States of America");
          add("Case Comprehensive Cancer Center, Case Western Reserve University, Cleveland, Ohio, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Harriet")
        .setSurnames("Mayanja-Kizza")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Uganda – Case Western Reserve University Research Collaboration, Cleveland, Ohio, United States of America, and Kampala, Uganda");
          add("Makerere University School of Medicine and Mulago Hospital, Kampala, Uganda");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Gilla")
        .setSurnames("Kaplan")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Public Health Research Institute, University of Medicine and Dentistry of New Jersey, Newark, New Jersey, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Willem A.")
        .setSurnames("Hanekom")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("South African Tuberculosis Vaccine Initiative, Institute of Infectious Diseases and Molecular Medicine and School of Child and Adolescent Health, University of Cape Town, South Africa");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("W. Henry")
        .setSurnames("Boom")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Medicine, Case Western Reserve University, Cleveland, Ohio, United States of America");
          add("Uganda – Case Western Reserve University Research Collaboration, Cleveland, Ohio, United States of America, and Kampala, Uganda");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Thomas R.")
        .setSurnames("Hawn")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(true)
        .setCorresponding(null)
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Medicine, University of Washington School of Medicine, Seattle, Washington, United States of America");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"rel-footnote\">¶</span>These authors are joint senior authors on this work.</p>");
        }})
        .build());

      add(AuthorView.builder()
        .setGivenNames("Catherine M.")
        .setSurnames("Stein")
        .setSuffix(null)
        .setOnBehalfOf("for the Tuberculosis Research Unit (TBRU) and South African Tuberculosis Vaccine Initiative Team (SATVI)")
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(true)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:catherine.stein@case.edu\">catherine.stein@case.edu</a>")
        .setCurrentAddresses(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Department of Epidemiology and Biostatistics, Case Western Reserve University, Cleveland, Ohio, United States of America");
          add("Uganda – Case Western Reserve University Research Collaboration, Cleveland, Ohio, United States of America, and Kampala, Uganda");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"rel-footnote\">¶</span>These authors are joint senior authors on this work.</p>");
        }})
        .build());
    }};

    //Test for multiple addresses
    ArticleInfo a12 = new ArticleInfo("info:doi/10.1371/journal.pone.0052627");
    List<AuthorView> authors12 = new ArrayList<AuthorView>() {{
      add(AuthorView.builder()
        .setGivenNames("Maud")
        .setSurnames("Hertzog")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> <a href=\"mailto:maud.hertzog@ibcg.biotoul.fr\">maud.hertzog@ibcg.biotoul.fr</a> (MH); <a href=\"mailto:philippe.chavrier@curie.fr\">philippe.chavrier@curie.fr</a> (PC)")
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: Laboratoire de Microbiologie et Génétique Moléculaire, LMGM-CNRS UMR 5100, Université Paul Sabatier, Toulouse, France");
          add("CNRS, UMR 6061, Institut Génétique et Développement de Rennes, Université Rennes 1, UEB, IFR 140, Rennes, France");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Membrane and Cytoskeleton Dynamics, Institut Curie, Research Center, CNRS- UMR144, Paris, France");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Pedro")
        .setSurnames("Monteiro")
        .setSuffix(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setRelatedFootnote(false)
        .setCorresponding(null)
        .setCurrentAddresses(new ArrayList<String>() {{
          add("Current address: Laboratoire de Microbiologie et Génétique Moléculaire, LMGM-CNRS UMR 5100, Université Paul Sabatier, Toulouse, France");
        }})
        .setAffiliations(new ArrayList<String>() {{
          add("Membrane and Cytoskeleton Dynamics, Institut Curie, Research Center, CNRS- UMR144, Paris, France");
        }})
        .setCustomFootnotes(null)
        .build());
    }};


    return new Object[][] { { a1, authors1 }, { a2, authors2 }, { a3, authors3 },
      { a4, authors4 } , { a5, authors5 }, { a6, authors6 }, { a7, authors7 }, { a8, authors8 },
      { a9, authors9 }, { a10, authors10 }, { a11, authors11 }, { a12, authors12 } };
  }

  @DataProvider(name = "articleInfos")
  public Object[][] getArticleInfos() {
    // nlm 2.0 article
    String doi1 = "info:doi/10.1371/journal.pone.0023176";

    Article article1 = new Article();
    article1.setState(Article.STATE_ACTIVE);
    article1.setDoi(doi1);
    article1.setTitle("Fibronectin Unfolding Revisited: Modeling Cell Traction-Mediated Unfolding of the Tenth Type-III Repeat");
    article1.seteIssn("1932-6203");
    article1.setArchiveName("archive name from the database");
    article1.setDescription("description from the database");

    article1.setAssets(new ArrayList<ArticleAsset>(1));

    ArticleAsset asset1 = new ArticleAsset();
    asset1.setDoi(doi1);
    asset1.setExtension("XML");
    asset1.setContentType("text/xml");
    article1.getAssets().add(asset1);

    article1.setCitedArticles(new ArrayList<CitedArticle>());

    CitedArticle citedArticle1 = new CitedArticle();
    citedArticle1.setKey("1");
    citedArticle1.setYear(1982);
    citedArticle1.setDisplayYear("1982");
    citedArticle1.setVolumeNumber(115);
    citedArticle1.setVolume("115");
    citedArticle1.setIssue("5");
    citedArticle1.setTitle("Estimating household and community transmission parameters for influenza.");
    citedArticle1.setPages("736-51");
    citedArticle1.seteLocationID("736");
    citedArticle1.setJournal("Am J Epidemiol");
    citedArticle1.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");

    article1.getCitedArticles().add(citedArticle1);

    dummyDataStore.store(article1);

    ArticleInfo articleInfo1 = new ArticleInfo(doi1);
    articleInfo1.setCitedArticles(new ArrayList<CitedArticle>());
    articleInfo1.getCitedArticles().add(citedArticle1);


    // nlm 3.0 article
    String doi2 = "info:doi/10.1371/journal.pcbi.1002692";

    Article article2 = new Article();
    article2.setState(Article.STATE_ACTIVE);
    article2.setDoi(doi2);
    article2.setTitle("Connecting Macroscopic Observables and Microscopic Assembly Events in Amyloid Formation Using Coarse Grained Simulations");
    article2.seteIssn("1553-7358");
    article2.setArchiveName("archive name from the database");
    article2.setDescription("description from the database");

    article2.setAssets(new ArrayList<ArticleAsset>(1));

    ArticleAsset asset2 = new ArticleAsset();
    asset2.setDoi(doi2);
    asset2.setExtension("XML");
    asset2.setContentType("text/xml");
    article2.getAssets().add(asset2);

    article2.setCitedArticles(new ArrayList<CitedArticle>());

    CitedArticleAuthor citedArticleAuthor = new CitedArticleAuthor();
    citedArticleAuthor.setGivenNames("W");
    citedArticleAuthor.setSurnames("Balch");

    CitedArticle citedArticle2 = new CitedArticle();
    citedArticle2.setKey("1");
    citedArticle2.setYear(2003);
    citedArticle2.setDisplayYear("2003");
    citedArticle2.setVolumeNumber(161);
    citedArticle2.setVolume("161");
    citedArticle2.setTitle("Amyloid as a natural product");
    citedArticle2.setPages("461-462");
    citedArticle2.seteLocationID("461");
    citedArticle2.setJournal("J Cell Biol");
    citedArticle2.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");

    article2.getCitedArticles().add(citedArticle2);
    citedArticle2.getAuthors().add(citedArticleAuthor);

    dummyDataStore.store(article2);

    ArticleInfo articleInfo2 = new ArticleInfo(doi2);
    articleInfo2.setCitedArticles(new ArrayList<CitedArticle>());
    articleInfo2.getCitedArticles().add(citedArticle2);

    return new Object[][] {
      { articleInfo1 }, { articleInfo2 }
    };
  }

  @DataProvider(name = "articleAffiliations")
  public Object[][] getArticleAffiliations(){

    String doi = "info:doi/10.1371/journal.pone.0048915";
    Article a = new Article(doi);
    //add the asset record for the XML.  The fileService needs this
    List<ArticleAsset> assetsForArticle1 = new LinkedList<ArticleAsset>();
    ArticleAsset asset1ForArticle1 = new ArticleAsset();
    asset1ForArticle1.setContentType("XML");
    asset1ForArticle1.setContextElement("Fake ContextElement for asset1ForArticle1");
    asset1ForArticle1.setDoi("info:doi/10.1371/journal.pone.0048915");
    asset1ForArticle1.setExtension("XML");
    asset1ForArticle1.setSize(1000001l);
    asset1ForArticle1.setCreated(new Date());
    asset1ForArticle1.setLastModified(new Date());
    assetsForArticle1.add(asset1ForArticle1);
    a.setAssets(assetsForArticle1);

    dummyDataStore.store(a);

    //LinkedHashMap for ordering
    Map<String, List<AuthorView>> affiliationSets = new LinkedHashMap<String, List<AuthorView>>() {{

      //lots of affiliates and authors
      put("Department of Physiology (Animal Physiology II), Faculty of Biology, Universidad Complutense, Instituto de Investigación Sanitaria del Hospital Clínico San Carlos, Madrid, Spain",
          new ArrayList<AuthorView>() {{
            add(AuthorView.builder()
                .setGivenNames("Virginia")
                .setSurnames("Mela")
                .build());
            add(AuthorView.builder()
                .setGivenNames("Álvaro")
                .setSurnames("Llorente-Berzal")
                .build());
            add(AuthorView.builder()
                .setGivenNames("María-Paz")
                .setSurnames("Viveros")
                .build());
          }});

      put("Department of Endocrinology, Hospital Infantil Universitario Niño Jesús, Instituto de Investigación Biomédica Princesa, Madrid, Spain",
          new ArrayList<AuthorView>(){{
            add(AuthorView.builder()
                .setGivenNames("Francisca")
                .setSurnames("Díaz")
                .build());
            add(AuthorView.builder()
                .setGivenNames("Jesús")
                .setSurnames("Argente")
                .build());
            add(AuthorView.builder()
                .setGivenNames("Julie A.")
                .setSurnames("Chowen")
                .build());
          }});

      put("Department of Pediatrics, Universidad Autónoma de Madrid, Madrid, Spain",
          new ArrayList<AuthorView>(){{
            add(AuthorView.builder()
                .setGivenNames("Jesús")
                .setSurnames("Argente")
                .build());
          }});

      put("CIBER de Fisiopatología de Obesidad y Nutrición, Instituto Carlos III, Madrid, Spain",
          new ArrayList<AuthorView>(){{
            add(AuthorView.builder()
                .setGivenNames("Francisca")
                .setSurnames("Díaz")
                .build());
            add(AuthorView.builder()
                .setGivenNames("Jesús")
                .setSurnames("Argente")
                .build());
            add(AuthorView.builder()
                .setGivenNames("Julie A.")
                .setSurnames("Chowen")
                .build());
          }});
    }};

    //TODO - cover following cases when time permits
//same author shows up in multiple affiliates
//    info:doi/10.1371/journal.pmed.0020073

    //affiliates without authors
    //10.1371/journal.pmed.0020073


    return new Object[][]{{doi, affiliationSets}};
  }

  /**
   * Make sure article affiliations are listed in order in which they appear in xml
   *
   * @param doi
   */
  @Test(dataProvider = "articleAffiliations")
  public void testGetAffiliations(String doi, Map<String, List<AuthorView>> affiliationSets) throws Exception {

    String fsid = FSIDMapper.doiTofsid(doi, "XML");
    InputStream is = fileStoreService.getFileInStream(fsid);
    org.w3c.dom.Document doc = xmlService.createDocBuilder().parse(is);

    assertNotNull(doc, "Couldn't find " + doi);

    //perhaps a sub-optimal implementation of duo iteration, but otherwise it seems like a lot of boilerplate
    //for just a test
    Iterator testData = affiliationSets.entrySet().iterator();
    for (Map.Entry<String, List<AuthorView>> mutEntry : fetchArticleService.getAuthorsByAffiliation(doc, fetchArticleService.getAuthors(doc)).entrySet()) {

      Map.Entry<String, List<AuthorView>> testDatum = (Map.Entry<String, List<AuthorView>>) testData.next();

      assertEquals(mutEntry.getKey(),testDatum.getKey(), "Article affiliation names don't match");

      Iterator testAuthors = testDatum.getValue().iterator();
      for(AuthorView mutAuthor : mutEntry.getValue()){

        AuthorView testAuthor = (AuthorView)testAuthors.next();
        assertEquals(mutAuthor.getFullName(), testAuthor.getFullName(),"Author names don't match");
      }

      //make sure we've covered all test data
      assertFalse(testAuthors.hasNext(),"Not all test authors accounted for");

    }
    //make sure we've covered all test data
    assertFalse(testData.hasNext(), "Not all test affiliations accounted for");
  }

  /**
   * This is testing the xsl transform of the article xml
   */
  @Test(dataProvider = "articleInfos")
  public void testGetArticleAsHTML(ArticleInfo articleInfo) throws Exception {

    // note, addExtraCitationInfo function throws an error if the citations in the article xml doesn't match the
    // citedArticles list in the articleInfo object
    String output = fetchArticleService.getArticleAsHTML(articleInfo);
    Document doc = Jsoup.parseBodyFragment(output);

    // test all the different aspects of the xsl transformation
    for (HtmlChecker validator : HtmlChecker.values()) {
      validator.check(doc);
    }
  }

  @Test(dataProvider = "articlesForCorrespondingTest")
  public void testCorrespondingAuthors(ArticleInfo article, List<String> authors) throws Exception {
    String fsid = FSIDMapper.doiTofsid(article.getDoi(), "XML");
    InputStream fs = fileStoreService.getFileInStream(fsid);
    org.w3c.dom.Document dom = xmlService.createDocBuilder().parse(fs);

    List<String> results = fetchArticleService.getCorrespondingAuthors(dom);

    assertEquals(authors.size(), results.size(), "Differing count of authors");
    assertEquals(results, authors);
  }

  @Test(dataProvider = "snippetsForCorrespondingTest")
  public void correspondingTest(String authorNode, String[] results) throws Exception {
   List<String> rs = FetchArticleServiceImpl.parseOutAuthorEmails(authorNode);

    assertEquals(rs.toArray(new String[rs.size()]), results);
  }

  @Test(dataProvider = "articlesWithDummyAffils")
  public void testGetAuthors(ArticleInfo article, List<AuthorView> testAuthors) throws Exception {
    String fsid = FSIDMapper.doiTofsid(article.getDoi(), "XML");
    InputStream fs = fileStoreService.getFileInStream(fsid);
    org.w3c.dom.Document dom = xmlService.createDocBuilder().parse(fs);

    assertNotNull(dom, "Problem loading documenty");

    logger.info(article.getDoi());

    List<AuthorView> authors = fetchArticleService.getAuthors(dom);

    assertTrue(authors.size() > 0, "No authors found");
    assertTrue(testAuthors.size() > 0, "No test authors found");
    assertEquals(testAuthors.size(), authors.size(), "Differing count of authors");

    //For debugging:
    for(int a = 0; a < testAuthors.size(); a++) {
      logger.info("Test: " + a);
      printAuthor(testAuthors.get(a));
      logger.info("Result: " + a);
      printAuthor(authors.get(a));
    }

    assertEquals(testAuthors, authors);
  }

  /**
   * Data provider for article Eoc body
   * @return
   */
  @DataProvider(name = "articlesWithEoc")
  public Object[][] getArticlesWithEoc()
  {
    //Test an  article with Expression of concern
    ArticleInfo a1 = new ArticleInfo("info:doi/10.1371/journal.pone.0049703");

    String eocTest = "<p><strong>Expression of Concern: Novel Allelic Variants in the Canine Cyclooxgenase-2 (Cox-2) " +
        "Promoter Are Associated with Renal Dysplasia in Dogs</strong></p>\n" +
        "    \n" +
        "      \n" +
        "      <p>After the publication of this article, a number of concerns were raised in relation to different " +
        "aspects of the research reported. The <italic>PLOS ONE</italic> editors carried out an evaluation of the " +
        "history of the manuscript, which revealed that due to a failure in the peer review process, several aspects " +
        "of the research were not adequately evaluated before publication.</p>\n" +
        "      <p>As a result, the <italic>PLOS ONE</italic> editors have undertaken a thorough re-examination of " +
        "this study, involving both external and internal advisers. This assessment has revealed the following " +
        "concerns regarding the study:</p>\n" +
        "      <ul class=\"bulletlist\">\n" +
        "        \n" +
        "<li>\n" +
        "          <p>The description of the alleles in the article is inadequate.</p>\n" +
        "        </li>\n" +
        "        \n" +
        "<li>\n" +
        "          <p>There are concerns over the study design employed to study the association; a single-gene " +
        "association study based on Cox-2 or a genome-wide association study have been recommended as more " +
        "appropriate approaches to study this association.</p>\n" +
        "        </li>\n" +
        "        \n" +
        "<li>\n" +
        "          <p>The validity of the control population employed in the study is compromised as it " +
        "involved a different breed.</p>\n" +
        "        </li>\n" +
        "        \n" +
        "<li>\n" +
        "          <p>There are concerns about the strength of the evidence shown to support an association " +
        "between the Cox-2 variant and the dogs' phenotypes, as the evidence from other breeds suggests that " +
        "this may be a neutral DNA variant.</p>\n" +
        "        </li>\n" +
        "      </ul>\n" +
        "      <p>In the light of the concerns outlined above, the <italic>PLOS ONE</italic> editors are issuing " +
        "this Expression of Concern in order to make readers aware of the concerns about the reliability of the " +
        "results and conclusions reported in the article.</p>";

    dummyDataStore.store(a1) ;
    return new Object[][] { {a1, eocTest} };
  }

  /**
   * tests if the Eoc body is returned correctly  or not
   * @param articleInfo
   * @param eocTest
   * @throws Exception
   */
  @Test (dataProvider = "articlesWithEoc")
  public void testEoc(ArticleInfo articleInfo,String eocTest) throws Exception {
    String fsid = FSIDMapper.doiTofsid(articleInfo.getDoi(), "XML");
     InputStream fs = fileStoreService.getFileInStream(fsid);
     org.w3c.dom.Document dom = xmlService.createDocBuilder().parse(fs);

    String eoc = fetchArticleService.getEocBody(dom);
    assertEquals(eocTest.trim(), eoc.trim(), "Expression of concerns didn't match");

  }

  private void printAuthor(AuthorView av) {
    logger.info("---------------------------------");
    logger.info("getFullName :" + av.getFullName());
    logger.info("getGivenNames :" + av.getGivenNames());
    logger.info("getSuffix :" + av.getSuffix());
    logger.info("getSurnames :" + av.getSurnames());
    logger.info("getCorresponding :" + av.getCorresponding());
    logger.info("getOnBehalfOf :" + av.getOnBehalfOf());
    logger.info("getDeceased :" + av.getDeceased());
    logger.info("getRelatedFootnote :" + av.getRelatedFootnote());
    logger.info("getEqualContrib :" + av.getEqualContrib());

    for(String curAddress : av.getCurrentAddresses()) {
      logger.info("curAddress :" + curAddress);
    }

    for(String affil : av.getAffiliations()) {
      logger.info("affil :" + affil);
    }

    for(String note : av.getCustomFootnotes()) {
      logger.info("note :" + note);
    }

    logger.info("---------------------------------");
  }
}