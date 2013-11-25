/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.article;

import org.ambraproject.ApplicationException;
import org.ambraproject.views.CitedArticleView;
import org.ambraproject.views.SearchHit;
import org.ambraproject.views.UserProfileInfo;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;
import org.ambraproject.views.article.CitationInfo;
import org.ambraproject.views.article.RelatedArticleInfo;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.models.Category;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.models.CitedArticleEditor;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.models.Volume;
import org.ambraproject.service.crossref.CrossRefLookupService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.permission.PermissionsService;
import org.ambraproject.views.ArticleCategory;
import org.ambraproject.views.AssetView;
import org.ambraproject.views.JournalView;
import org.ambraproject.views.article.BaseArticleInfo;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.net.URI;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * @author Joe Osowski
 */

public class ArticleServiceImpl extends HibernateServiceImpl implements ArticleService {
  private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

  private PermissionsService permissionsService;
  private CrossRefLookupService crossRefLookupService;

  /**
   * Determines if the articleURI is of type researchArticle
   *
   * @param article The article object
   * @return True if the article is a research article
   * @throws org.ambraproject.ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  @Override
  public boolean isResearchArticle(final Article article)
      throws ApplicationException, NoSuchArticleIdException {
    // resolve article type and supported properties
    ArticleType articleType = ArticleType.getDefaultArticleType();

    for (String artTypeUri : article.getTypes()) {
      if (ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri)) != null) {
        articleType = ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri));
        break;
      }
    }

    if (articleType == null) {
      throw new ApplicationException("Unable to resolve article type for: " + article.getDoi());
    }

    return ArticleType.isResearchArticle(articleType);
  }

  /**
   * Determines if the articleURI is of type researchArticle
   *
   * @param articleInfo The articleInfo Object
   * @return True if the article is a research article
   * @throws org.ambraproject.ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isResearchArticle(final ArticleInfo articleInfo)
      throws ApplicationException, NoSuchArticleIdException {
    ArticleType articleType = ArticleType.getDefaultArticleType();

    for (String artTypeUri : articleInfo.getTypes()) {
      if (ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri)) != null) {
        articleType = ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri));
        break;
      }
    }

    if (articleType == null) {
      throw new ApplicationException("Unable to resolve article type for: " + articleInfo.getDoi());
    }

    return ArticleType.isResearchArticle(articleType);
  }

  /**
   * Determines if the articleURI is of type Expression of Concern
   *
   * @param articleInfo The articleInfo Object
   * @return True if the article is a Expression of Concern article
   * @throws org.ambraproject.ApplicationException
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isEocArticle(final BaseArticleInfo articleInfo)
      throws ApplicationException, NoSuchArticleIdException {
    ArticleType articleType = ArticleType.getDefaultArticleType();

    for (String artTypeUri : articleInfo.getTypes()) {
      if (ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri)) != null) {
        articleType = ArticleType.getKnownArticleTypeForURI(URI.create(artTypeUri));
        break;
      }
    }
    if (articleType == null) {
      throw new ApplicationException("Unable to resolve article type for: " + articleInfo.getDoi());
    }

    return ArticleType.isEocArticle(articleType);
  }

  /**
   * Change an articles state.
   *
   * @param articleDoi uri
   * @param authId the authorization ID of the current user
   * @param state   state
   *
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(rollbackFor = {Throwable.class})
  public void setState(final String articleDoi, final String authId, final int state) throws NoSuchArticleIdException {
    permissionsService.checkPermission(Permission.INGEST_ARTICLE, authId);

    List articles = hibernateTemplate.findByCriteria(DetachedCriteria.forClass(Article.class)
          .add(Restrictions.eq("doi", articleDoi)));

    if (articles.size() == 0) {
      throw new NoSuchArticleIdException(articleDoi);
    }

    Article a = (Article)articles.get(0);
    a.setState(state);

    //Remove relationships if this article is being disabled
    //they will be created on re-ingest if necessary, but if both articles in a reciprocal relationship are
    //disabled and have the relationships removed from xml, we want to the relationships to be gone when both are reingested
    if (state == Article.STATE_DISABLED) {
      a.getRelatedArticles().clear();
    }

    hibernateTemplate.update(a);

    //log whenever someone disables or unpublishes an article
    if ((state == Article.STATE_UNPUBLISHED ||
          state == Article.STATE_DISABLED)
        && log.isInfoEnabled()) {
      DetachedCriteria criteria = DetachedCriteria.forClass(UserProfile.class)
          .setProjection(Projections.property("displayName"))
          .add(Restrictions.eq("authId",authId));
      String userName = (String) hibernateTemplate.findByCriteria(criteria, 0, 1).get(0);
      userName = userName == null ? "UNKNOWN" : userName;
      log.info("User '{}' {} the article {}",
          new String[] {userName, state == Article.STATE_DISABLED ?
            "disabled" : "unpublished", articleDoi});
    }
  }

  /**
   * Get the ids of all articles satisfying the given criteria.
   * <p/>
   * This method calls <code>getArticles(...)</code> then parses the Article IDs from that List.
   * <p/>
   *
   * @param params
   * @return the (possibly empty) list of article ids.
   * @throws java.text.ParseException if any of the dates could not be parsed
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<String> getArticleDOIs(final ArticleServiceSearchParameters params) throws ParseException {

    List<Article> articles = getArticles(params);
    List<String> articleIds = new ArrayList<String>(articles.size());

    for (Article article : articles) {
      articleIds.add(article.getDoi());
    }

    return articleIds;
  }

  /**
   * @inheritDoc
   */
  @Transactional (readOnly = true)
  public List<SearchHit> getRandomRecentArticles(String journal_eIssn, List<URI>articleTypesToShow,
                                                 int numDaysInPast, int articleCount)
  {
    //end date is most recent midnight
    Calendar endDate = GregorianCalendar.getInstance();
    endDate.set(Calendar.HOUR_OF_DAY, 23);
    endDate.set(Calendar.MINUTE, 59);
    endDate.set(Calendar.SECOND, 59);

    Calendar startDate = (Calendar)endDate.clone();
    //This may look a bit off, but we want a larger value here without making it too large.
    //The initial query should return results outside of the defined window.  But not too large a result set
    //We may need to tune this a bit.
    int numDaysTemp = -(numDaysInPast * 2) - 30;
    startDate.add(Calendar.DAY_OF_YEAR, numDaysTemp);

    //Get 30 days worth of articles first
    List<SearchHit> recentArticles = getArticles(startDate, endDate, articleTypesToShow, journal_eIssn);
    List<SearchHit> results = new ArrayList<SearchHit>();

    startDate = (Calendar)endDate.clone();
    startDate.add(Calendar.DAY_OF_YEAR, -numDaysInPast);

    //First grab all the articles that fall into the defined window
    //Regardless of articleCount.  We want to randomize before we limit
    //So each time the method returns, it returns a different list
    for(SearchHit searchHit : recentArticles) {
      if(searchHit.getDate().after(startDate.getTime())) {
        results.add(searchHit);
      }
    }

    //We assume the list is sorted by date desc (Most recent first)
    //So we can reduce the list by the count of the results so far for a minor
    //performance boost
    recentArticles = recentArticles.subList(results.size(), recentArticles.size());

    //If we still don't have enough, decrement the start date and try again
    //But let's not go on forever, only back 30 days.  (in this case 10 loops, each iteration is 3 days)
    int loop = 0;
    while(results.size() < articleCount && loop < 10) {
      startDate.add(Calendar.DAY_OF_YEAR, -3);
      for(SearchHit searchHit : recentArticles) {
        if(searchHit.getDate().after(startDate.getTime())) {
          results.add(searchHit);
        }
      }
      loop++;
    }

    //Shuffle results
    Collections.shuffle(results);

    //pare down the actual number of recent articles to match articleCount
    if (results.size() > articleCount) {
      results = results.subList(0, articleCount);
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  private List<SearchHit> getArticles(final Calendar startDate, final Calendar endDate,
                                      final List<URI> articleTypesToShow,
                                      final String journal_eIssn)
  {
    // if articleTypesToShow is empty, then all types of articles should be returned

    return (List<SearchHit>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        //Expected SQL Query:
        //select distinct a.doi, a.title, a.date from article a join articleTypes at on a.articleID = at.articleID
        //where a.eIssn = '%journal_eIssn%' and a.date between '%date%' and '%date%'
        //at.type in ('%article type%', '%article type%')
        String[] types = new String[articleTypesToShow.size()];

        for(int a = 0; a < articleTypesToShow.size(); a++) {
          types[a] = articleTypesToShow.get(a).toString();
        }

        String sql = "select distinct a.doi, a.title, a.date " +
            "from article a join articleType atype on a.articleID = atype.articleID " +
            "where a.eIssn = :eIssn " +
            "and a.date between :startDate and :endDate ";

        if (articleTypesToShow.size() > 0) {
          sql = sql + "and atype.type in :types ";
        }
        sql = sql + "order by a.date desc";

        Query query = session.createSQLQuery(sql)
          .setString("eIssn", journal_eIssn)
          .setCalendar("startDate", startDate)
          .setCalendar("endDate", endDate);

        if (articleTypesToShow.size() > 0) {
          query.setParameterList("types", types);
        }

        List<Object[]> articleResults = query.list();
        List<SearchHit> searchResults = new ArrayList<SearchHit>();

        for (int i = 0; i < articleResults.size(); i++) {
          Object[] res = articleResults.get(i);
          String doi = (String) res[0];
          String title = (String) res[1];
          Date pubDate = (Date) res[2];

          searchResults.add(SearchHit.builder()
            .setUri(doi)
            .setTitle(title)
            .setDate(pubDate)
            .build());
        }

        return searchResults;
      }
    });
  }

  /**
   * Get all of the articles satisfying the given criteria.

   * @param params
   *
   * @return all of the articles satisfying the given criteria (possibly null) Key is the Article DOI.  Value is the
   *         Article itself.
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<Article> getArticles(final ArticleServiceSearchParameters params) {

    return (List<Article>) this.hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria query = session.createCriteria(Article.class);

        if (params.getStates() != null && params.getStates().length > 0) {
          List<Integer> statesList = new ArrayList<Integer>(params.getStates().length);
          for (int state : params.getStates()) {
            statesList.add(state);
          }
          query.add(Restrictions.in("state", statesList));
        }
        if (params.geteIssn() != null) {
          query.add(Restrictions.eq("eIssn", params.geteIssn()));
        }
        if (params.getOrderField() != null) {
          if (params.isOrderAscending()) {
            query.addOrder(Order.asc(params.getOrderField()));
          } else {
            query.addOrder(Order.desc(params.getOrderField()));
          }
        }

        if (params.getStartDate() != null) {
          query.add(Restrictions.ge("date", params.getStartDate()));
        }
        if (params.getEndDate() != null) {
          query.add(Restrictions.le("date", params.getEndDate()));
        }

        if (params.getMaxResults() > 0) {
          query.setMaxResults(params.getMaxResults());
        }
        //Filter the results post-db access since the kind of restriction we want isn't easily representable in sql -
        //we want only articles such that the list of full names of the author list contains ALL the names in the given
        //author array.  This is NOT an 'in' restriction or an 'equals' restriction
        List<Article> queryResults = query.list();
        List<Article> filteredResults = new ArrayList<Article>(queryResults.size());

        for (Article article : queryResults) {
          if (matchesAuthorFilter(params.getAuthors(), article.getAuthors())
              && matchesCategoriesFilter(params.getCategories(), article.getCategories())) {
            filteredResults.add(article);
          }
        }

        return filteredResults;
      }
    });
  }

  /**
   * Get an Article by ID.
   *
   * @param articleID ID of Article to get.
   * @param authId the authorization ID of the current user
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true, noRollbackFor = {SecurityException.class})
  public Article getArticle(final Long articleID, final String authId) throws NoSuchArticleIdException
  {
    // sanity check parms
    if (articleID == null)
      throw new IllegalArgumentException("articleID == null");

    Article article = (Article)hibernateTemplate.load(Article.class, articleID);

    if (article == null) {
      throw new NoSuchArticleIdException(String.valueOf(articleID));
    }

    checkArticleState(article, authId);

    return article;
  }


  /**
   * Get an Article by URI.
   *
   * @param articleDoi URI of Article to get.
   * @param authId the authorization ID of the current user
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Override
  @Transactional(readOnly = true, noRollbackFor = {SecurityException.class})
  @SuppressWarnings("unchecked")
  public Article getArticle(final String articleDoi, final String authId) throws NoSuchArticleIdException {
    // sanity check parms
    if (articleDoi == null)
      throw new IllegalArgumentException("articleDoi == null");

    List<Article> articles = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("doi", articleDoi)));

    if (articles.size() == 0) {
      throw new NoSuchArticleIdException(articleDoi);
    }

    checkArticleState(articles.get(0), authId);

    return articles.get(0);
  }

  private void checkArticleState(Article article, String authId) throws NoSuchArticleIdException {
    //If the article is unpublished, it should not be returned if the user is not an admin
    if (article.getState() == Article.STATE_UNPUBLISHED) {
      try {
        permissionsService.checkPermission(Permission.VIEW_UNPUBBED_ARTICLES, authId);
      } catch(SecurityException se) {
        throw new NoSuchArticleIdException(article.getDoi());
      }
    }

    //If the article is disabled, don't display it ever
    if (article.getState() == Article.STATE_DISABLED) {
      throw new NoSuchArticleIdException(article.getDoi());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  public void checkArticleState(final String articleDoi, final String authId) throws NoSuchArticleIdException {
    //If the article is unpublished, it should not be returned if the user is not an admin

    List<Integer> results = hibernateTemplate.findByCriteria(DetachedCriteria.forClass(Article.class)
        .add(Restrictions.eq("doi", articleDoi))
        .setProjection(Projections.projectionList()
            .add(Projections.property("state")))
        ,0,1);

    if (results.size() == 0) {
      throw new NoSuchArticleIdException(articleDoi);
    }

    Integer articleState = results.get(0);

    if (articleState == Article.STATE_UNPUBLISHED) {
      try {
        permissionsService.checkPermission(Permission.VIEW_UNPUBBED_ARTICLES, authId);
      } catch(SecurityException se) {
        throw new NoSuchArticleIdException(articleDoi);
      }
    }

    //If the article is disabled, don't display it ever
    if (articleState == Article.STATE_DISABLED) {
      throw new NoSuchArticleIdException(articleDoi);
    }
  }

  /**
   * Get articles based on a list of Article id's.
   *
   * If an article is requested that the user does not have access to, it will not be returned
   *
   * @param articleDois list of article doi's
   * @param authId the authorization ID of the current user
   * @return <code>List&lt;Article&gt;</code> of articles requested
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<Article> getArticles(List<String> articleDois, final String authId) {
    // sanity check parms
    if (articleDois == null)
      throw new IllegalArgumentException("articleDois == null");

    List<Article> articles = new ArrayList<Article>();
    if(!articleDois.isEmpty()) {
      articles = hibernateTemplate.findByCriteria(
      DetachedCriteria.forClass(Article.class)
        .add(Restrictions.in("doi", articleDois)));
    }

    for(int a = 0; a < articles.size(); a++) {
      try {
        checkArticleState(articles.get(a), authId);
      } catch(NoSuchArticleIdException ex) {
        articles.remove(a);
      }
    }

    //Make sure the list of returned articles is in the same order as the requesting list.
    List<Article> articlesSorted = new ArrayList<Article>();

    for(String doi : articleDois) {
      for(Article article : articles) {
        if(article.getDoi().equals(doi)) {
          articlesSorted.add(article);
        }
      }
    }

    return articlesSorted;
  }

  /**
   * Get a List of all of the Journal/Volume/Issue combinations that contain the <code>articleURI</code> which was
   * passed in. Each primary List element contains a secondary List of six Strings which are, in order: <ul>
   * <li><strong>Element 0: </strong> Journal URI</li> <li><strong>Element 1: </strong> Journal key</li>
   * <li><strong>Element 2: </strong> Volume URI</li> <li><strong>Element 3: </strong> Volume name</li>
   * <li><strong>Element 4: </strong> Issue URI</li> <li><strong>Element 5: </strong> Issue name</li> </ul> A Journal
   * might have multiple Volumes, any of which might have multiple Issues that contain the <code>articleURI</code>. The
   * primary List will always contain one element for each Issue that contains the <code>articleURI</code>.
   *
   * @param articleDoi Article DOI that is contained in the Journal/Volume/Issue combinations which will be returned
   * @return All of the Journal/Volume/Issue combinations which contain the articleURI passed in
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<List<String>> getArticleIssues(final String articleDoi) {
    return (List<List<String>>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List<Object[]> articleIssues = session.createSQLQuery(
            "select {j.*}, {v.*}, {i.*} " +
              "from issueArticleList ial " +
              "join issue i on ial.issueID = i.issueID " +
              "join volume v on i.volumeID = v.volumeID " +
              "join journal j on v.journalID = j.journalID " +
              "where ial.doi = :articleURI " +
              "order by i.created desc ")
            .addEntity("j", Journal.class)
            .addEntity("v", Volume.class)
            .addEntity("i", Issue.class)
            .setString("articleURI", articleDoi)
            .list();

        List<List<String>> finalResults = new ArrayList<List<String>>(articleIssues.size());
        for (Object[] row : articleIssues) {
          Journal journal = (Journal) row[0];
          Volume volume = (Volume) row[1];
          Issue issue = (Issue) row[2];

          List<String> secondaryList = new ArrayList<String>();
          secondaryList.add(journal.getID().toString());  // Journal ID
          secondaryList.add(journal.getJournalKey());     // Journal Key
          secondaryList.add(volume.getVolumeUri());       // Volume URI
          secondaryList.add(volume.getDisplayName());     // Volume name
          secondaryList.add(issue.getIssueUri());         // Issue URI
          secondaryList.add(issue.getDisplayName());      // Issue name
          finalResults.add(secondaryList);
        }

        return finalResults;
      }
    });
  }

  /**
   * Get the articleInfo object for an article
   * @param articleDoi the ID of the article
   * @param authId the authorization ID of the current user
   * @return articleInfo
   */
  @Transactional(readOnly = true)
  @Override
  @SuppressWarnings("unchecked")
  public ArticleInfo getArticleInfo(final String articleDoi, final String authId) throws NoSuchArticleIdException {
    final Article article;

    article = getArticle(articleDoi, authId);

    return createArticleInfo(article, authId);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = true)
  @Override
  public ArticleInfo getArticleInfo(Long articleID, String authId) throws NoSuchArticleIdException {
    Article article = hibernateTemplate.get(Article.class, articleID);
    return createArticleInfo(article, authId);
  }

  /**
   * Get the articleInfo object for an article
   * @param articleDois the ID of the article
   * @param authId the authorization ID of the current user
   * @return articleInfo
   */
  @Transactional(readOnly = true)
  @Override
  @SuppressWarnings("unchecked")
  public List<ArticleInfo> getArticleInfos(final List<String> articleDois, final String authId) {

    final List<Article> articles = getArticles(articleDois, authId);
    List<ArticleInfo> articleInfos = new ArrayList<ArticleInfo>();

    for(Article article : articles) {
      articleInfos.add(createArticleInfo(article, authId));
    }

    return articleInfos;
  }

  private ArticleInfo createArticleInfo(Article article, final String authId) {
    final ArticleInfo articleInfo = new ArticleInfo();

    articleInfo.setId(article.getID());
    //Set properties from the dublin core
    articleInfo.setDoi(article.getDoi());
    articleInfo.setDate(article.getDate());
    articleInfo.setTitle(article.getTitle());
    articleInfo.setVolume(article.getVolume());
    articleInfo.setIssue(article.getIssue());
    articleInfo.setJournal(article.getJournal());
    articleInfo.setDescription(article.getDescription());
    articleInfo.setRights(article.getRights());
    articleInfo.setPublisher(article.getPublisherName());
    articleInfo.seteIssn(article.geteIssn());
    articleInfo.setTypes(article.getTypes());
    articleInfo.setPages(article.getPages());
    articleInfo.setIssue(article.getIssue());
    articleInfo.setVolume(article.getVolume());
    articleInfo.seteLocationId(article.geteLocationId());
    articleInfo.setCitedArticles(article.getCitedArticles());
    //Set the citation info
    CitationInfo citationInfo = new CitationInfo();
    citationInfo.setId(URI.create(article.getDoi()));
    citationInfo.setCollaborativeAuthors(article.getCollaborativeAuthors());

    //set article asset views
    List<AssetView> aViews = new ArrayList<AssetView>();
    for (ArticleAsset asset : article.getAssets()) {
      aViews.add(new AssetView(asset.getDoi(), asset.getSize(), asset.getExtension()));
    }
    articleInfo.setArticleAssets(aViews);

    Set<Category> categories = article.getCategories();
    Set<ArticleCategory> catViews = new HashSet<ArticleCategory>(categories.size());


    //See if the user flagged any of the existing categories
    List<Long> flaggedCategories = getFlaggedCategories(article.getID(), authId);

    for(Category cat : categories) {
      catViews.add(
        ArticleCategory.builder()
          .setCategoryID(cat.getID())
          .setMainCategory(cat.getMainCategory())
          .setSubCategory(cat.getSubCategory())
          .setPath(cat.getPath())
          .setFlagged(flaggedCategories.contains(cat.getID()))
        .build()
      );
    }

    articleInfo.setCategories(catViews);
    List<ArticleCategory> orderedCategories = sortCategories(catViews);
    articleInfo.setOrderedCategories(orderedCategories);

    //authors (list of UserProfileInfo)
    //TODO: Refactor ArticleInfo and CitationInfo objects
    //there's no reason why authors need to be attached to the citation
    List<UserProfileInfo> authors = new ArrayList<UserProfileInfo>();
    for (ArticleAuthor ac : article.getAuthors()) {
      UserProfileInfo author = new UserProfileInfo();
      author.setRealName(ac.getFullName());
      authors.add(author);
    }
    citationInfo.setAuthors(authors);
    articleInfo.setCi(citationInfo);

    //set article type
    if (article.getTypes() != null) {
      articleInfo.setAt(article.getTypes());
    }

    Set<org.ambraproject.models.Journal> journals = article.getJournals();
    Set<JournalView> journalViews = new HashSet<JournalView>(journals.size());

    for(org.ambraproject.models.Journal journal : journals) {
      journalViews.add(new JournalView(journal));
    }

    articleInfo.setJournals(journalViews);

    //get related articles
    //this results in more queries than doing a join, but getArticle() already has security logic built in to it
    //and a very small percentage of articles even have related articles
    articleInfo.setRelatedArticles(new ArrayList<RelatedArticleInfo>(article.getRelatedArticles().size()));
    for (ArticleRelationship relationship : article.getRelatedArticles()) {
      if (relationship.getOtherArticleDoi() != null) {
        try {
          Article otherArticle = getArticle(relationship.getOtherArticleDoi(), authId);
          RelatedArticleInfo relatedArticleInfo = new RelatedArticleInfo();
          relatedArticleInfo.setUri(URI.create(otherArticle.getDoi()));
          relatedArticleInfo.setTitle(otherArticle.getTitle());
          relatedArticleInfo.setDoi(otherArticle.getDoi());
          relatedArticleInfo.setDate(otherArticle.getDate());
          relatedArticleInfo.seteIssn(otherArticle.geteIssn());
          relatedArticleInfo.setRelationType(relationship.getType());
          relatedArticleInfo.setTypes(otherArticle.getTypes());

          journals = otherArticle.getJournals();
          journalViews = new HashSet<JournalView>(journals.size());
          for(org.ambraproject.models.Journal journal : journals) {
            journalViews.add(new JournalView(journal));
          }
          relatedArticleInfo.setJournals(journalViews);

          List<String> relatedArticleAuthors = new ArrayList<String>(otherArticle.getAuthors().size());
          for (ArticleAuthor ac : otherArticle.getAuthors()) {
            relatedArticleAuthors.add(ac.getFullName());
          }
          relatedArticleInfo.setAuthors(relatedArticleAuthors);

          //set article type
          if (otherArticle.getTypes() != null) {
            relatedArticleInfo.setAt(otherArticle.getTypes());
          }

          if (!articleInfo.getRelatedArticles().contains(relatedArticleInfo)) {
            articleInfo.getRelatedArticles().add(relatedArticleInfo);
          }
        } catch (NoSuchArticleIdException e) {
          //exclude this article
        }
      }
    }

    log.debug("loaded ArticleInfo: id={}, articleTypes={}, " +
      "date={}, title={}, authors={}, related-articles={}",
      new Object[] {articleInfo.getDoi(), articleInfo.getArticleTypeForDisplay(), articleInfo.getDate(),
        articleInfo.getTitle(), Arrays.toString(articleInfo.getAuthors().toArray()),
        Arrays.toString(articleInfo.getRelatedArticles().toArray())});
    return articleInfo;
  }

  @Override
  @Transactional(readOnly = true)
  public ArticleInfo getBasicArticleView(Long articleID) throws NoSuchArticleIdException {
    if (articleID == null) {
      throw new NoSuchArticleIdException("Null id");
    }
    log.debug("loading up title and doi for article: {}", articleID);
    ArticleInfo articleInfo = getBasicArticleViewArticleInfo(articleID);
    return articleInfo;
  }

  @Override
  @Transactional(readOnly = true)
  public ArticleInfo getBasicArticleView(String articleDoi) throws NoSuchArticleIdException {
    if (articleDoi == null) {
      throw new NoSuchArticleIdException("Null doi");
    }
    log.debug("loading up title and doi for article: {}", articleDoi);

    ArticleInfo articleInfo = getBasicArticleViewArticleInfo(articleDoi);

    return articleInfo;
  }

  /**
   * Return a list of categories this user flagged for this article
   *
   * @param articleID an articleID
   * @param authID the user's authorization ID
   *
   * @return list of category IDs this user flagged for this article
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  private List<Long> getFlaggedCategories(final long articleID, final String authID) {
    if(authID != null && authID.length() > 0) {
      return hibernateTemplate.execute(new HibernateCallback<List<Long>>() {
        public List<Long> doInHibernate(Session session) throws HibernateException, SQLException {
          List<BigInteger> categories = session.createSQLQuery(
            "select acf.categoryID from articleCategoryFlagged acf " +
              "join userProfile up on up.userProfileID = acf.userProfileID " +
              "where up.authId = :authID and acf.articleID = :articleID")
            .setString("authID", authID)
            .setLong("articleID", articleID)
            .list();

          List<Long> results = new ArrayList<Long>();

          for(BigInteger row : categories) {
            results.add(row.longValue());
          }

          return results;
        }
      });
    } else {
      return new ArrayList<Long>();
    }
  }

  /**
   * Returns ArticleInfo object with articleID, doi, title, authors, collaborativeAuthors and article type populated
   * @param articleIdentifier articleID or articleDoi
   * @return ArticleInfo object with articleID, doi, title, authors, collaborativeAuthors and article type populated
   * @throws NoSuchArticleIdException
   */
  private ArticleInfo getBasicArticleViewArticleInfo (Object articleIdentifier) throws NoSuchArticleIdException {

    Object[] results = new Object[0];
    List<ArticleAuthor> authors;
    List<String> collabAuthors;
    List<String> articleTypes;

    try {

      DetachedCriteria dc =  DetachedCriteria.forClass(Article.class)
          .setProjection(Projections.projectionList()
              .add(Projections.id())
              .add(Projections.property("doi"))
              .add(Projections.property("title"))
          );

      if (articleIdentifier instanceof Long) {
        dc.add(Restrictions.eq("ID", articleIdentifier));
      } else if (articleIdentifier instanceof String) {
        dc.add(Restrictions.eq("doi", articleIdentifier));
      }

      results = (Object[]) hibernateTemplate.findByCriteria(dc,0,1).get(0);

      authors = hibernateTemplate.find("from ArticleAuthor where articleID = ?", results[0]);

      collabAuthors = hibernateTemplate.find("select elements(article.collaborativeAuthors) from Article as article where id = ?", results[0]);

      articleTypes = hibernateTemplate.find("select elements(article.types) from Article as article where id = ?", results[0]);


    } catch (IndexOutOfBoundsException e) {
      throw new NoSuchArticleIdException(articleIdentifier.toString());
    }

    ArticleInfo articleInfo = new ArticleInfo();
    articleInfo.setId((Long) results[0]);
    articleInfo.setDoi((String) results[1]);
    articleInfo.setTitle((String) results[2]);

    List<String> authors2 = new ArrayList<String>(authors.size());
    for (ArticleAuthor ac : authors) {
      authors2.add(ac.getFullName());
    }
    articleInfo.setAuthors(authors2);

    articleInfo.setCollaborativeAuthors(collabAuthors);

    articleInfo.setAt(new HashSet<String>(articleTypes));

    return articleInfo;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public CitedArticleView getCitedArticle(long citedArticleID) {
    //TODO, unit test needed SE-133
    List<String> results = (List<String>)hibernateTemplate.findByCriteria(
      DetachedCriteria.forClass(Article.class)
        .setProjection(Projections.property("doi"))
        .createCriteria("citedArticles", "ca")
        .add(Restrictions.eq("ca.ID", citedArticleID)));

    if(results.size() == 0) {
      return null;
    }

    CitedArticle citedArticle = hibernateTemplate.get(CitedArticle.class, citedArticleID);
    String doi = results.get(0);

    return new CitedArticleView(doi, citedArticle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public void setCitationDoi(CitedArticle citedArticle, String doi) {
    citedArticle.setDoi(doi);
    hibernateTemplate.update(citedArticle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public String refreshCitedArticle(Long citedArticleID) throws Exception {
    log.debug("refreshArticleCitation for citedArticleID: {}", citedArticleID);
    StringBuilder sb = new StringBuilder();

    CitedArticle citedArticle = hibernateTemplate.get(CitedArticle.class, citedArticleID);

    for(CitedArticleEditor editor : citedArticle.getEditors()) {
      sb.append(", ");
      sb.append(editor.getFullName());
    }

    for(CitedArticleAuthor author : citedArticle.getAuthors()) {
      sb.append(", ");
      sb.append(author.getFullName());
    }

    if(citedArticle.getTitle() != null) {
      sb.append(", \"");
      sb.append(citedArticle.getTitle());
      sb.append("\"");
    }

    if(citedArticle.getJournal() != null) {
      sb.append(", ");
      sb.append(citedArticle.getJournal());
    }

    if(citedArticle.getPublisherLocation() != null) {
      sb.append(", ");
      sb.append(citedArticle.getPublisherLocation());
    }

    if(citedArticle.getPublisherName() != null) {
      sb.append(", ");
      sb.append(citedArticle.getPublisherName());
    }

    if(citedArticle.getPages() != null) {
      sb.append(", ");
      sb.append(citedArticle.getPages());
    }

    if(citedArticle.geteLocationID() != null) {
      sb.append(", ");
      sb.append(citedArticle.geteLocationID());
    }

    if(citedArticle.getYear() != null) {
      sb.append(", ");
      sb.append(citedArticle.getYear());
    }

    if(citedArticle.getVolume() != null) {
      sb.append(", ");
      sb.append(citedArticle.getVolume());
    }

    if(citedArticle.getIssue() != null) {
      sb.append(", ");
      sb.append(citedArticle.getIssue());
    }

    if(citedArticle.getNote() != null) {
      sb.append(", ");
      sb.append(citedArticle.getNote());
    }

    for(String name : citedArticle.getCollaborativeAuthors()) {
      sb.append(", ");
      sb.append(name);
    }

    String doi = null;

    if(sb.length() == 0) {
      log.debug("No data for citation ({}), not searching for DOI", citedArticleID);
    } else {
      doi = crossRefLookupService.findDoi(sb.toString());

      if (doi != null && !doi.isEmpty()) {
        //A fix for FEND-1077. crossref seems to append a URL to the DOI.  WTF
        doi = doi.replace("http://dx.doi.org/","");

        log.debug("refreshArticleCitation doi found: {}", doi);
        setCitationDoi(citedArticle, doi);
      } else {
        log.debug("refreshArticleCitation nothing found");
      }
    }

    return doi;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public List<Category> setArticleCategories(Article article, List<String> categoryStrings) {
    List<Category> categories = new ArrayList<Category>(categoryStrings.size());
    Set<String> uniqueLeafs = new HashSet<String>();

    for (String s : categoryStrings) {
      if (s.charAt(0) != '/') {
        throw new IllegalArgumentException("Bad category: " + s);
      }
      Category category = new Category();
      category.setPath(s);

      //We want a count of distinct lead nodes.  When this
      //Reaches eight stop.  Note the second check, we can be at
      //eight uniqueLeafs, but still finding different paths.  Stop
      //Adding when a new unique leaf is found.  Yes, a little confusing
      if (uniqueLeafs.size() == 8 &&
        //getSubCategory returns leaf node of the path
        !uniqueLeafs.contains(category.getSubCategory())
        ) {
        break;
      } else {
        //getSubCategory returns leaf node of the path
        uniqueLeafs.add(category.getSubCategory());
        categories.add(category);
      }
    }

    // TODO: article.categories is a set according to Hibernate, but it should be
    // a list, since the taxonomy server returns a list of categories sorted
    // in descending order of the match.  Need to change the ambra hibernate stuff
    // if we want to change that.
    article.setCategories(new HashSet<Category>(categories));
    updateWithExistingCategories(article);

    return categories;
  }

  /**
   * Update the article to reference any already existing categories in the database.
   *
   * @param article the article to update
   */
  private void updateWithExistingCategories(Article article) {

    // I was having an issue where the first call to hibernateTemplate.findByCriteria below
    // was triggering a "flush"... saving a dirty but uncommitted object to the DB.  The
    // object being saved was a newly-created category that had a duplicate in the DB, which
    // triggered a duplicate key exception.  Of course, this is the whole point of this
    // method... to prevent this from happening.  The following two lines fix this, but it
    // seems kind of wrong.  This happened from a standalone app not running in a servlet
    // container, and I suspect that I was somehow misconfiguring my session factory
    // or transaction manager or something (but this was the only solution I found).
    int oldFlushMode = hibernateTemplate.getFlushMode();
    hibernateTemplate.setFlushMode(HibernateAccessor.FLUSH_COMMIT);
    try {
      Set<Category> existingCategories = article.getCategories();
      if (existingCategories != null && !existingCategories.isEmpty()) {
        Set<Category> correctCategories = new HashSet<Category>(existingCategories.size());
        for (Category category : existingCategories) {
          try {
            Category existingCategory;
            if (category.getSubCategory() != null) {
              existingCategory = (Category) hibernateTemplate.findByCriteria(
                  DetachedCriteria.forClass(Category.class)
                      .add(Restrictions.eq("path", category.getPath())), 0, 1).get(0);
            } else {
              existingCategory = (Category) hibernateTemplate.findByCriteria(
                  DetachedCriteria.forClass(Category.class)
                      .add(Restrictions.eq("path", category.getPath())), 0, 1).get(0);
            }
            correctCategories.add(existingCategory);
          } catch (IndexOutOfBoundsException e) {
            //category must not have existed
            correctCategories.add(category);
          }
        }
        article.setCategories(correctCategories);
      }
    } finally {
      hibernateTemplate.setFlushMode(oldFlushMode);
    }
  }


  /**
   * Help0er method for getArticleIds() since the restriction we want doesn't appear
   *
   * @param authorFilter
   * @param authors
   * @return
   */
  private boolean matchesAuthorFilter(String[] authorFilter, List<ArticleAuthor> authors) {
    if (authorFilter != null && authorFilter.length > 0) {
      List<String> authorNames = new ArrayList<String>(authors.size());
      for (ArticleAuthor author : authors) {
        authorNames.add(author.getFullName());
      }
      for (String author : authorFilter) {
        if (!authorNames.contains(author)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Helper method for getArticleIds(), since the type of restriction needed doesn't seem to be do-able in HQL or with
   * criteria - we want articles where each of the Strings in the category array is the main category of one of the
   * article's categories
   *
   * @param filterCategories - the array of main categories that was passed in to getArticleIds() to use to filter
   *                         results
   * @param categorySet      - the 'categories' property of an article
   * @return - true if the article passes the category filter, false otherwise
   */
  private boolean matchesCategoriesFilter(String[] filterCategories, Set<Category> categorySet) {
    if (filterCategories != null && filterCategories.length > 0) {

      if (filterCategories.length > categorySet.size()) {
        return false; //can't possibly contain all the categories if there's more of them than you have
      }

      //Just get the main category
      Set<String> mainCategories = new HashSet<String>(categorySet.size());
      for (Category category : categorySet) {
        mainCategories.add(category.getMainCategory());
      }
      //check that all the filter categories are in their
      for (String cat : filterCategories) {
        if (!mainCategories.contains(cat)) {
          return false;
        }
      }

    }
    return true;
  }

  /**
   * @param permissionsService the permissions service to use
   */
  @Required
  public void setPermissionsService(PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * @param crossRefLookupService the crossreflookup service to use
   */
  @Required
  public void setCrossRefLookupService(CrossRefLookupService crossRefLookupService) {
    this.crossRefLookupService = crossRefLookupService;
  }

  /**
   * This method sorts the categories in alphabetical order. It uses the overridden
   * compareTo() method in the ArticleCategory to compare the subcategories for sorting;
   * if the subcategory does not exist (in case of one-level deep categories)
   * this method uses the main category.
   *
   * @param categoryViews the 'categories' property of an article as a list
   * @return the alphabetically ordered categories
   */
 public List<ArticleCategory> sortCategories (Set<ArticleCategory> categoryViews) {
   List<ArticleCategory> orderedCategories = new ArrayList<ArticleCategory>();
   orderedCategories.addAll(categoryViews);
   Collections.sort(orderedCategories);
    return orderedCategories;
  }
}
