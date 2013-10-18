package org.ambraproject.views.article;

import org.ambraproject.util.TextUtils;
import org.ambraproject.views.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Info about the article for home page
 */
public class HomePageArticleInfo implements Serializable {

  private static final Logger log = LoggerFactory.getLogger(HomePageArticleInfo.class);
  private static final int TITLE_LENGTH = 120;

  private String doi;
  private String title;
  private String authors;
  private String strkImgURI;
  private String description;
  private String truncatedAuthors;

  public HomePageArticleInfo() {

  }

  public HomePageArticleInfo(SearchHit hit) {
    setDoi(hit.getUri());
    setTitle(hit.getTitle());
    setStrkImgURI(hit.getStrikingImage());
    setDescription(hit.getAbstract());
    setAuthors(hit.getCreator());
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthors() {
    return authors;
  }

  public void setAuthors(String authors) {
    this.authors = authors;       //Make a list of first, second, third creators

    if (authors != null) {
      this.truncatedAuthors = TextUtils.makeAuthorString(authors.split(","));
    }
  }

  public String getStrkImgURI() {
    return strkImgURI;
  }

  public void setStrkImgURI(String strkImgURI) {
    this.strkImgURI = strkImgURI;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTruncatedTitle() {
    return TextUtils.truncateTextCloseOpenTag(this.title, TITLE_LENGTH);
  }

  public String getTruncatedAuthors() {
    return truncatedAuthors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HomePageArticleInfo)) return false;

    HomePageArticleInfo that = (HomePageArticleInfo) o;

    if (authors != null ? !authors.equals(that.authors) : that.authors != null) return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (doi != null ? !doi.equals(that.doi) : that.doi != null) return false;
    if (strkImgURI != null ? !strkImgURI.equals(that.strkImgURI) : that.strkImgURI != null) return false;
    if (title != null ? !title.equals(that.title) : that.title != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = doi != null ? doi.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (authors != null ? authors.hashCode() : 0);
    result = 31 * result + (strkImgURI != null ? strkImgURI.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}
