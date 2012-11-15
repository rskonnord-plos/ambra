package org.ambraproject.action.article;

import com.opensymphony.xwork2.Action;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.article.ArticleType;

import java.util.List;

/**
 * A Struts action for a page that includes {@code article_header.ftl}.
 */
public interface ArticleHeaderAction extends Action {

  public abstract List<AuthorView> getAuthors();

  public abstract ArticleType getArticleType();

  public abstract String getAuthorNames();

  public abstract String getContributingAuthors();

  public abstract boolean getIsPeerReviewed();

  public abstract boolean getHasAboutAuthorContent();

}
