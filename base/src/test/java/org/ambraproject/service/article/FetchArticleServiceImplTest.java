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

import java.util.ArrayList;

public class FetchArticleServiceImplTest extends BaseTest {

  @Autowired
  protected FetchArticleService fetchArticleService;

  @Autowired
  protected FileStoreService fileStoreService;


  /**
   * This is testing the xsl transform of the article xml
   */
  public void testGetArticleAsHTML() throws Exception {

    String doi = "info:doi/10.1371/journal.pone.0023176";

    Article article = new Article();
    article.setState(Article.STATE_ACTIVE);
    article.setDoi(doi);
    article.setTitle("Fibronectin Unfolding Revisited: Modeling Cell Traction-Mediated Unfolding of the Tenth Type-III Repeat");
    article.seteIssn("1932-6203");
    article.setArchiveName("archive name from the database");
    article.setDescription("description from the database");

    article.setAssets(new ArrayList<ArticleAsset>(1));

    ArticleAsset asset = new ArticleAsset();
    asset.setDoi(doi);
    asset.setExtension("XML");
    asset.setContentType("text/xml");
    article.getAssets().add(asset);

    article.setCitedArticles(new ArrayList<CitedArticle>());

    CitedArticle citedArticle = new CitedArticle();
    citedArticle.setKey("1");
    citedArticle.setYear(1982);
    citedArticle.setDisplayYear("1982");
    citedArticle.setVolumeNumber(115);
    citedArticle.setVolume("115");
    citedArticle.setIssue("5");
    citedArticle.setTitle("Estimating household and community transmission parameters for influenza.");
    citedArticle.setPages("736-51");
    citedArticle.seteLocationID("736");
    citedArticle.setJournal("Am J Epidemiol");
    citedArticle.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");

    article.getCitedArticles().add(citedArticle);

    dummyDataStore.store(article);


    ArticleInfo articleInfo = new ArticleInfo();
    articleInfo.setDoi(doi);
    articleInfo.setCitedArticles(new ArrayList<CitedArticle>());
    articleInfo.getCitedArticles().add(citedArticle);


    // note, addExtraCitationInfo function throws an error if the citations in the article xml doesn't match the
    // citedArticles list in the articleInfo object
    String output = fetchArticleService.getArticleAsHTML(articleInfo);
    Document doc = Jsoup.parseBodyFragment(output);

    // test all the differetn aspects of the xsl transformation
    for (HtmlChecker validator : HtmlChecker.allCheckers()) {
      validator.check(doc);
    }

  }
}


