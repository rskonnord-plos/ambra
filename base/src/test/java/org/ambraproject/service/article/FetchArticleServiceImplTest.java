package org.ambraproject.service.article;

import org.ambraproject.action.BaseTest;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.views.article.ArticleInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class FetchArticleServiceImplTest extends BaseTest {

  @Autowired
  protected FetchArticleService fetchArticleService;

  @Autowired
  protected FileStoreService fileStoreService;


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

    ArticleInfo articleInfo1 = new ArticleInfo();
    articleInfo1.setDoi(doi1);
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

    ArticleInfo articleInfo2 = new ArticleInfo();
    articleInfo2.setDoi(doi2);
    articleInfo2.setCitedArticles(new ArrayList<CitedArticle>());
    articleInfo2.getCitedArticles().add(citedArticle2);


    return new Object[][]{
        {articleInfo1}, {articleInfo2}
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
}


