package org.ambraproject.action.article;

import com.opensymphony.xwork2.Action;
import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.JournalView;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;

import java.util.List;
import java.util.Set;

/**
 * A Struts action for a page that includes {@code article_header.ftl}.
 */
public interface ArticleHeaderAction extends Action {

  public abstract String getArticleURI();

  public abstract ArticleInfo getArticleInfoX();

  public abstract Set<JournalView> getJournalList();

  public abstract List<AuthorView> getAuthors();

  public abstract ArticleType getArticleType();

  public abstract String getAuthorNames();

  public abstract String getContributingAuthors();

  public abstract boolean getHasAboutAuthorContent();

  public abstract boolean getIsResearchArticle();

  public abstract Set<ArticleCategory> getCategories();
}
