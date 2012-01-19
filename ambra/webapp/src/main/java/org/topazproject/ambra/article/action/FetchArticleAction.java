/* $HeadURL::                                                                            $
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
package org.topazproject.ambra.article.action;

import org.topazproject.ambra.action.BaseSessionAwareActionSupport;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.rating.service.RatingsService;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.TrackbackService;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.ReplyService;
import org.topazproject.ambra.annotation.Commentary;
import org.topazproject.otm.RdfUtil;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * This class fetches the information from the service tier for the artcle
 * Tabs.  Common data is defined in the setCommonData.  One method is defined
 * for each tab.
 *
 * Freemarker builds rest like URLs, inbound and outbound as
 * defined in the /WEB-INF/urlrewrite.xml file. These URLS then map to the
 * methods are referenced in the struts.xml file.
 *
 * ex: http://localhost/article/related/info%3Adoi%2F10.1371%2Fjournal.pone.0299
 *
 * Gets rewritten to:
 *
 * http://localhost/fetchRelatedArticle.action&amp;articleURI=info%3Adoi%2F10.1371%2Fjournal.pone.0299
 *
 * Struts picks this up and translates it call the FetchArticleRelated method
 * ex: &lt;action name="fetchRelatedArticle"
 *    class="org.topazproject.ambra.article.action.FetchArticleAction"
 *    method="FetchArticleRelated"&gt;
 * 
 */
public class FetchArticleAction extends BaseSessionAwareActionSupport {
  private static final Logger log = LoggerFactory.getLogger(FetchArticleAction.class);
  private final ArrayList<String> messages = new ArrayList<String>();

  private String articleURI;
  private String transformedArticle;
  private String annotationId = "";
  private String annotationSet = "";

  private List<WebAnnotation> formalCorrections = new ArrayList<WebAnnotation>();
  private List<WebAnnotation> minorCorrections = new ArrayList<WebAnnotation>();
  private List<WebAnnotation> retractions = new ArrayList<WebAnnotation>();
  private List<WebAnnotation> discussions = new ArrayList<WebAnnotation>();
  private List<WebAnnotation> comments = new ArrayList<WebAnnotation>();

  private boolean isResearchArticle;
  private boolean hasRated;

  private ArticleInfo articleInfoX;
  private Article articleInfo;
  private ArticleType articleType;
  private Commentary[] commentary;
  private List<List<String>> articleIssues;
  private List<Trackback> trackbackList;

  private ReplyService replyService;
  private AnnotationConverter annotationConverter;
  private FetchArticleService fetchArticleService;
  private AnnotationService annotationService;
  private BrowseService browseService;
  private JournalService journalService;
  private RatingsService ratingsService;
  private ArticleOtmService articleOtmService;
  private TrackbackService trackbackService;

  private Set<Journal> journalList;
  private RatingsService.AverageRatings averageRatings;

  /**
   * Fetch common data the article HTML text
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticle() {
    try {

      setCommonData();

      transformedArticle = fetchArticleService.getURIAsHTML(articleURI);

    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetch common data and annotations
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticleComments() {
    try {
      setCommonData();

      annotationSet = "comments";
      setAnnotations(annotationService.COMMENT_SET);

    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetch common data and article corrections
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticleCorrections() {
    try {
      setCommonData();

      annotationSet = "corrections";
      setAnnotations(annotationService.CORRECTION_SET);

    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetches common data and the trackback list.
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticleMetrics() {
    try {
      setCommonData();

      annotationSet = "comments";
      setAnnotations(annotationService.COMMENT_SET);

      trackbackList = trackbackService.getTrackbacks(articleURI, true);

    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetches common data and the trackback list.
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticleRelated() {
    try {
      setCommonData();

      trackbackList = trackbackService.getTrackbacks(articleURI, true);

    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetches common data and nothing else
   * @return "success" on succes, "error" on error
   */
  @Transactional(readOnly = true)
  public String fetchArticleCrossRef() {
    try {
      setCommonData();
    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: " + articleURI, e);
      return ERROR;
    } catch (Exception e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Sets up data used by the right hand column in the freemarker templates
   *
   * @throws ApplicationException when there is an error talking to the OTM
   * @throws NoSuchArticleIdException when the article can not be found
   */
  private void setCommonData() throws ApplicationException, NoSuchArticleIdException {

    RdfUtil.validateUri(articleURI, "articleURI=<" + articleURI + ">");

    articleInfoX = browseService.getArticleInfo(URI.create(articleURI));
    averageRatings = ratingsService.getAverageRatings(articleURI);
    journalList  = journalService.getJournalsForObject(URI.create(articleURI));
    isResearchArticle = articleOtmService.isResearchArticle(articleURI);
    hasRated = ratingsService.hasRated(articleURI, getCurrentUser());
    articleIssues = articleOtmService.getArticleIssues(URI.create(articleURI));

    ArticleAnnotation anns[] = annotationService.listAnnotations(articleURI, null);

    for (ArticleAnnotation a : anns) {
      if (a.getContext() == null) {
        discussions.add(annotationConverter.convert(a, false, false));
      } else if (a instanceof MinorCorrection) {
        minorCorrections.add(annotationConverter.convert(a, false, false));
      } else if (a instanceof FormalCorrection) {
        formalCorrections.add(annotationConverter.convert(a, true, true));
      } else if (a instanceof Retraction) {
        retractions.add(annotationConverter.convert(a, true, true));
      } else {
        comments.add(annotationConverter.convert(a, false, false));
      }
    }

    Article artInfo = articleOtmService.getArticle(URI.create(articleURI));
    artInfo.getCategories();

    this.articleInfo = artInfo;

    articleType = ArticleType.getDefaultArticleType();
    for (URI artTypeUri : artInfo.getArticleType()) {
      if (ArticleType.getKnownArticleTypeForURI(artTypeUri)!= null) {
        articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
        break;
      }
    }
  }

  /**
   * Grabs annotations from the service tier
   * @param annotationTypeClasses The type of annotation to grab.
   */
  private void setAnnotations(Set<Class<? extends ArticleAnnotation>> annotationTypeClasses) {
    WebAnnotation[] annotations = annotationConverter.convert(
        annotationService.listAnnotations(articleURI, annotationTypeClasses), true, false);

    commentary = new Commentary[annotations.length];
    Commentary com;

    if (annotations.length > 0) {
      for (int i = 0; i < annotations.length; i++) {
        com = new Commentary();
        com.setAnnotation(annotations[i]);

        try {
          annotationConverter.convert(replyService.listAllReplies(annotations[i].getId(),
                                                        annotations[i].getId()), com, false,
                                                         false);
        } catch (SecurityException t) {
          // don't error if you can't list the replies
          com.setNumReplies(0);
          com.setReplies(null);
        }
        commentary[i] = com;
      }
      Arrays.sort(commentary, new Commentary.Sorter());
    }
  }

  /** Set the fetch article service
   * @param articleOtmService ArticleOtmService
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

    /** Set the fetch article service
   * @param fetchArticleService fetchArticleService
   */
  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * @param trackBackservice The trackBackService to set.
   */
  @Required
  public void setTrackBackService(TrackbackService trackBackservice) {
    this.trackbackService = trackBackservice;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  @Required
  public void setReplyService(final ReplyService replyService) {
    this.replyService = replyService;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter annotationConverter) {
    this.annotationConverter = annotationConverter;
  }

  /**
   * Set the ratings service.
   *
   * @param ratingsService the ratings service
   */
  @Required
  public void setRatingsService(final RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  /**
   * @param annotationService The annotationService to set.
   */
  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @param journalService The journalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }


  /**
   * @return articleURI
   */
  @RequiredStringValidator(message = "Article URI is required.")
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Set articleURI to fetch the article for.
   * @param articleURI articleURI
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * @return transformed output
   */
  public String getTransformedArticle() {
    return transformedArticle;
  }

  /**
   * Get the type of annotations currently being listed
   * @return the annotation set either "comments" or "corrections"
   */
  public String getAnnotationSet() {
     return annotationSet;
  }

  /**
   * Return the ArticleInfo from the Browse cache.
   *
   * TODO: convert all usages of "articleInfo" (ObjectInfo) to use the Browse cache version of
   * ArticleInfo.  Note that for all templates to use ArticleInfo, it will have to
   * be enhanced.  articleInfo and articleInfoX are both present, for now, to support:
   *   - existing templates/services w/o a large conversion
   *   - access to RelatedArticles
   *
   * @return Returns the articleInfoX.
   */
  public ArticleInfo getArticleInfoX() {
    return articleInfoX;
  }

  /**
   * @return Returns the annotationId.
   */
  public String getAnnotationId() {
    return annotationId;
  }

  /**
   * @param annotationId The annotationId to set.
   */
  public void setAnnotationId(String annotationId) {
    this.annotationId = annotationId;
  }

  /**
   * Get the article object
   * @return Returns article.
   */
  public Article getArticleInfo() {
    return articleInfo;
  }

  /**
   * Gets the article Type
   * @return Returns articleType
   */
  public ArticleType getArticleType() {
    return articleType;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }

  /**
   * @return Returns the journalList.
   */
  public Set<Journal> getJournalList() {
    return journalList;
  }

  /**
   * @return Returns the names and URIs of all of the Journals, Volumes, and Issues
   *   to which this Article has been attached.  This includes "collections", but does not
   *   include the 
   */
  public List<List<String>> getArticleIssues() {
    return articleIssues;
  }

  /**
   * Tests if this article has been rated by the current user
   *
   * @return Returns the hasRated.
   */
  public boolean getHasRated() {
    return hasRated;
  }

  /**
   * @return the isResearchArticle
   */
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  /*
  * Gets averageRatings info
  *
  * @return returns averageRatings info
  * */
  public RatingsService.AverageRatings getAverageRatings() {
    return averageRatings;
  }

  /**
   * Get the total number of user comments
   * @return total number of user comments
   */
  public int getTotalComments() {
    return getNumDiscussions() + getNumComments() + getNumMinorCorrections() +
        getNumFormalCorrections() + getNumRetractions();
  }

  /**
   * Zero if this Article has not been retracted.  One if this Article has been retracted.
   * Having multiple Retractions for a single Article does not make sense.
   *
   * @return Returns the number of Retractions that have been associated to this Article.
   */
  public int getNumRetractions() {
    return retractions.size();
  }

  /**
   * @return Returns the numDiscussions.
   */
  public int getNumDiscussions() {
    return discussions.size();
  }

  /**
   * @return Returns the numComments.
   */
  public int getNumComments() {
    return comments.size();
  }

  /**
   * @return Returns the numMinorCorrections.
   */
  public int getNumMinorCorrections() {
    return minorCorrections.size();
  }

  /**
   * @return Returns the numFormalCorrections.
   */
  public int getNumFormalCorrections() {
    return formalCorrections.size();
  }

  /**
   * @return The commentary array
   */
  public Commentary[] getCommentary() {
    return commentary;
  }

  /**
   * @return Returns the trackbackList.
   */
  public List<Trackback> getTrackbackList() {
    return trackbackList;
  }

  /**
   * @return an array of formal corrections
   */
  public List<WebAnnotation> getFormalCorrections()
  {
    return this.formalCorrections;
  }

  /**
   * @return an array of retractions
   */
  public List<WebAnnotation> getRetractions()
  {
    return this.retractions;
  }

  /**
   * Return a list of this article's main categories
   * @return a Set<String> of category names
   * @throws ApplicationException when the article has not been set
   */
  public Set<String> getMainCategories() throws ApplicationException {
    Set<String> mainCats = new HashSet<String>();

    if(articleInfo == null) {
      throw new ApplicationException("Article not set");
    }

    for(Category curCategory : articleInfo.getCategories()) {
      mainCats.add(curCategory.getMainCategory());
    }

    return mainCats;
  }
}
