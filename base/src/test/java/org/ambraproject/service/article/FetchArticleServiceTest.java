package org.ambraproject.service.article;

import org.ambraproject.action.BaseTest;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.service.xml.XMLService;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.article.ArticleInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

public class FetchArticleServiceTest extends BaseTest {

  @Autowired
  protected FetchArticleService fetchArticleService;

  @Autowired
  protected ArticleService articleService;

  @Autowired
  protected FileStoreService fileStoreService;

  @Autowired
  protected XMLService xmlService;

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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> john-logsdon@uiowa.edu")
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> mcarrion@cajal.csic.es")
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(true)
        .setCorresponding(null)
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setGroupContrib(true)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> bach@bu.edu")
        .setAffiliations(new ArrayList<String>() {{
          add("Vascular Biology Section, Boston University Medical Center, Boston, Massachusetts, United States of America");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{ add("<p><span class=\"pilcro\">¶</span> These authors also contributed equally to this work.</p>"); }})
        .build());

      add(AuthorView.builder()
        .setGivenNames("David R.")
        .setSurnames("Pimental")
        .setSuffix(null)
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setGroupContrib(true)
        .setDeceased(false)
        .setCorresponding(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Myocardial Biology Unit, Boston University Medical Center, Boston, Massachusetts, United States of America");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"pilcro\">¶</span> These authors also contributed equally to this work.</p>");
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
        .setCurrentAddress("Current address: Roche Molecular Diagnostics, Pleasanton, California, United States of America")
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setAffiliations(new ArrayList<String>() {{
          add("Institute of Microbiology and Hygiene, Charité Universitätsmedizin Berlin, Berlin, Germany");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Iana")
        .setSurnames("Parvanova")
        .setSuffix(null)
        .setCurrentAddress("Current address: Bavarian Research Alliance GmbH (BayFOR), Munich, Germany")
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setAffiliations(null)
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"pilcro\">¶</span> Membership of the Danish SAB Study Group Consortium is provided in the Acknowledgments.</p>");
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(true)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">*</span>To whom correspondence should be addressed. E-mail: paow@mskcc.org")
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
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> members@tulane.edu (MEE); Philipp@tulane.edu (MTP)")
        .setAffiliations(new ArrayList<String>() {{
          add("Divisions of Bacteriology & Parasitology, Tulane National Primate Research Center, Tulane University Health Sciences Center, Covington, Louisiana, United States of America");
        }})
        .setCustomFootnotes(null)
        .build());

      add(AuthorView.builder()
        .setGivenNames("Mario T.")
        .setSurnames("Philipp")
        .setSuffix(null)
        .setCurrentAddress(null)
        .setOnBehalfOf(null)
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> members@tulane.edu (MEE); Philipp@tulane.edu (MTP)")
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
        .setCurrentAddress(null)
        .setOnBehalfOf("on behalf of Busia OR Study Group")
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding("<span class=\"email\">* E-mail:</span> ddalmau@mutuaterrassa.cat")
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
        .setCurrentAddress(null)
        .setOnBehalfOf("for the EVAPRIMA group")
        .setEqualContrib(false)
        .setDeceased(false)
        .setCorresponding(null)
        .setAffiliations(new ArrayList<String>() {{
          add("INSERM UMR S953, Epidemiological Research Unit on Perinatal Health and Women’s and Children’s Health, Pierre et Marie Curie University, Paris, France");
          add("Department of Obstetrics and Gynaecology, Port-Royal Maternity, Cochin Saint-Vincent-de-Paul Hospital, Assistance Publique des Hopitaux de Paris, Université Paris Descartes, Sorbonne Paris Cité, Paris, France");
          add("Premup Foundation, Paris, France");
        }})
        .setCustomFootnotes(new ArrayList<String>() {{
          add("<p><span class=\"pilcro\">¶</span>Membership of the EVAPRIMA group is provided in the Acknowledgments.</p>");
        }})
        .build());
    }};

    return new Object[][] { { a1, authors1 }, { a2, authors2 }, { a3, authors3 },
      { a4, authors4 } , { a5, authors5 }, { a6, authors6 }, { a7, authors7 }, { a8, authors8 },
      { a9, authors9 }, { a10, authors10 } };
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

    dummyDataStore.store(article2);

    ArticleInfo articleInfo2 = new ArticleInfo(doi2);
    articleInfo2.setCitedArticles(new ArrayList<CitedArticle>());
    articleInfo2.getCitedArticles().add(citedArticle2);

    return new Object[][] {
      { articleInfo1 }, { articleInfo2 }
    };
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

    //For debugging:
    for(AuthorView ac : testAuthors) {
      printAuthor(ac);
    }

    for(AuthorView ac : authors) {
      printAuthor(ac);
    }

    assertEquals(testAuthors.size(), authors.size(), "Differing count of authors");
    assertEquals(testAuthors, authors);
  }

  private void printAuthor(AuthorView av) {
    logger.info("---------------------------------");
    logger.info("getFullName :" + av.getFullName());
    logger.info("getGivenNames :" + av.getGivenNames());
    logger.info("getSuffix :" + av.getSuffix());
    logger.info("getSurnames :" + av.getSurnames());
    logger.info("getCorresponding :" + av.getCorresponding());
    logger.info("getCurrentAddress :" + av.getCurrentAddress());
    logger.info("getOnBehalfOf :" + av.getOnBehalfOf());
    logger.info("getDeceased :" + av.getDeceased());
    logger.info("getEqualContrib :" + av.getEqualContrib());

    for(String affil : av.getAffiliations()) {
      logger.info("affil :" + affil);
    }

    for(String note : av.getCustomFootnotes()) {
      logger.info("note :" + note);
    }

    logger.info("---------------------------------");
  }
}