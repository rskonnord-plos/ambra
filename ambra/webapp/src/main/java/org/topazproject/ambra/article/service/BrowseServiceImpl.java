/*
* $HeadURL$
* $Id$
*
* Copyright (c) 2006-2011 by Public Library of Science
*     http://plos.org
*     http://ambraproject.org
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

package org.topazproject.ambra.article.service;

import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.article.BrowseResult;
import org.topazproject.ambra.article.action.TOCArticleGroup;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.UserProfileInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.model.article.*;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.search.SearchHit;
import org.topazproject.ambra.search.service.SolrServerFactory;
import org.topazproject.ambra.service.HibernateServiceImpl;
import org.topazproject.ambra.solr.SolrServiceUtil;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author Alex Worden, stevec
 */
public class BrowseServiceImpl extends HibernateServiceImpl implements BrowseService {
  private static final Logger log = LoggerFactory.getLogger(BrowseServiceImpl.class);

  private static final String ARTBYCAT_LIST_KEY   = "ArtByCat-";
  private static final String DATE_LIST_KEY       = "DateList-";
  private static final String ARTBYDATE_LIST_KEY  = "ArtByDate-";
  private static final String ISSUE_KEY           = "Issue-";

  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private SolrServerFactory serverFactory;

  private static final int getTimeToLive() {
    // time value is in minutes
    int ttl = configuration.getInt("ambra.services.browse.time", 15);
    // time to live value should be in seconds
    ttl = ttl * 60;
    return ttl;
  }

  private static final Integer getYear(String date) {
    return Integer.valueOf(date.substring(0, 4));
  }

  private static final Integer getMonth(String date) {
    return Integer.valueOf(date.substring(5, 7));
  }

  private static final Integer getDay(String date) {
    return Integer.valueOf(date.substring(8, 10));
  }

  private Cache browseSolrCache;
  private JournalService journalService;
  private ArticlePersistenceService articlePersistenceService;

  /**
   * @param journalService The journal-service to use.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param articlePersistenceService The articlePersistenceService to use.
   */
  @Required
  public void setArticlePersistenceService(ArticlePersistenceService articlePersistenceService) {
    this.articlePersistenceService = articlePersistenceService;
  }

  /**
   * @param browseSolrCache The browse solr cache to use.
   */
  @Required
  public void setBrowseSolrCache(Cache browseSolrCache) {
    this.browseSolrCache = browseSolrCache;
    // the time to live will be short enough that we are not going to worry about invalidator logic
  }

  /**
   * Get the dates of all articles with a <code>state</code> of <code>ACTIVE</code> (meaning the articles have been
   * published). The outer map is a map of years, the next inner map a map of months, and finally the innermost is a
   * list of days. <br/>
   *
   * @return the article dates.
   */
  @Transactional(readOnly = true)
  public Years getArticleDates(final String journalKey) {
    Boolean useCache = configuration.getBoolean("ambra.services.browse.cache", true);

    if (useCache) {
      String cacheKey = DATE_LIST_KEY + journalKey;
      int ttl = getTimeToLive();
      return browseSolrCache.get(cacheKey, ttl,
          new Cache.SynchronizedLookup<Years, RuntimeException>(cacheKey.intern()) {
            @SuppressWarnings("synthetic-access")
            @Override
            public Years lookup() throws RuntimeException {
              return loadArticleDates(journalKey);
            }
          });
    } else {
      return loadArticleDates(journalKey);
    }
  }

  /**
   * Get articles in the given category. One "page" of articles will be returned, i.e. articles pageNum * pageSize ..
   * (pageNum + 1) * pageSize - 1 . Note that less than a pageSize articles may be returned, either because it's the end
   * of the list or because some articles are not accessible.
   *
   * @param catName    the category for which to return the articles
   * @param pageNum    the page-number for which to return articles; 0-based
   * @param pageSize   the number of articles per page
   * @param numArt     (output) the total number of articles in the given category
   * @param journalKey the key of the current journal
   * @return the articles.
   */
  @Transactional(readOnly = true)
  public List<SearchHit> getArticlesByCategory(final String catName, final int pageNum, final int pageSize,
                                                 long[] numArt, final String journalKey) {

    BrowseResult result;

    Boolean useCache = configuration.getBoolean("ambra.services.browse.cache", true);

    if (useCache) {
      final String cacheKey = ARTBYCAT_LIST_KEY + journalKey + "-" + catName + "-" + pageNum + "-" + pageSize;
      int ttl = getTimeToLive();

      result = browseSolrCache.get(cacheKey, ttl,
          new Cache.SynchronizedLookup<BrowseResult, RuntimeException>(cacheKey.intern()) {
            @Override
            public BrowseResult lookup() throws RuntimeException {
              return getArticlesByCategoryViaSolr(catName, pageNum, pageSize, journalKey);
            }
          });
    } else {
      result = getArticlesByCategoryViaSolr(catName, pageNum, pageSize, journalKey);
    }

    numArt[0] = result.getTotal();

    // making a shallow copy of the list and returning that
    ArrayList<SearchHit> copyOfArticles = (ArrayList<SearchHit>) result.getArticles().clone();

    return copyOfArticles;
  }

  /**
   * Get articles in the given date range, from newest to oldest, of the given article type(s). One "page" of articles
   * will be returned, i.e. articles pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less than a pageSize
   * articles may be returned, either because it's the end of the list or because some articles are not accessible.
   * <p/>
   * Note: this method assumes the dates are truly just dates, i.e. no hours, minutes, etc.
   * <p/>
   * If the <code>articleTypes</code> parameter is null or empty, then all types of articles are returned.
   * <p/>
   * This method should never return null.
   *
   * @param startDate    the earliest date for which to return articles (inclusive)
   * @param endDate      the latest date for which to return articles (exclusive)
   * @param articleTypes The URIs indicating the types of articles which will be returned, or null for all types
   * @param pageNum      the page-number for which to return articles; 0-based
   * @param pageSize     the number of articles per page, or -1 for all articles
   * @param numArt       (output) the total number of articles in the given category
   * @param journalKey   The key of the current journal
   * @return the articles.
   */
  @Transactional(readOnly = true)
  public List<SearchHit> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                           final List<URI> articleTypes, final int pageNum,
                                           final int pageSize, long[] numArt, final String journalKey) {

    BrowseResult result;
    Boolean useCache = configuration.getBoolean("ambra.services.browse.cache", true);

    if (useCache) {
      String mod = journalKey + "-" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis();
      String cacheKey = ARTBYDATE_LIST_KEY + mod + "-" + pageNum + "-" + pageSize;
      int ttl = getTimeToLive();

      result = browseSolrCache.get(cacheKey, ttl,
          new Cache.SynchronizedLookup<BrowseResult, RuntimeException>(cacheKey.intern()) {
            @Override
            public BrowseResult lookup() throws RuntimeException {
              return getArticlesByDateViaSolr(startDate, endDate, articleTypes, pageNum, pageSize, journalKey);
            }
          });
    } else {
      result = getArticlesByDateViaSolr(startDate, endDate, articleTypes, pageNum, pageSize, journalKey);
    }

    numArt[0] = result.getTotal();

    // return a shallow copy of the list.
    // HomePageAction.initRecentArticles function modifies the list that's returned from this function and that can
    // modify the list in the cache.
    // If the callers ever start to modify the objects in the list, we need to make a deep copy and return that.
    ArrayList<SearchHit> copyOfArticles = (ArrayList<SearchHit>) result.getArticles().clone();

    return copyOfArticles;
  }

  /**
   * Get a list of article-counts for each category.
   *
   * @param journalKey The current journal
   * @return the category infos.
   */
  @Transactional(readOnly = true)
  public SortedMap<String, Long> getArticlesByCategory(final String journalKey) {
    Boolean useCache = configuration.getBoolean("ambra.services.browse.cache", true);

    if (useCache) {
      final String cacheKey = ARTBYCAT_LIST_KEY + journalKey;
      int ttl = getTimeToLive();
      return browseSolrCache.get(cacheKey, ttl,
          new Cache.SynchronizedLookup<SortedMap<String, Long>, RuntimeException>(cacheKey.intern()) {
            @Override
            public SortedMap<String, Long> lookup() throws RuntimeException {
              return getArticlesByCategoryViaSolr(journalKey);
            }
          });
    } else {
      return getArticlesByCategoryViaSolr(journalKey);
    }
  }

  /**
   * Get Issue information.
   *
   * @param doi DOI of Issue.
   * @return the Issue information.
   */
  @Transactional(readOnly = true)
  public IssueInfo getIssueInfo(final URI doi) {
    return getIssueInfo2(doi);
  }

  /**
   * Get Issue information inside of a Transaction.
   *
   * @param issueDOI DOI of Issue.
   * @return the Issue information.
   */
  private IssueInfo getIssueInfo2(final URI issueDOI) {
    return (IssueInfo)hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        // get the Issue
        final Issue issue = (Issue) hibernateTemplate.get(Issue.class, issueDOI);
        if (issue == null) {
          log.error("Failed to retrieve Issue for doi='" + issueDOI.toString() + "'");
          return null;
        }

        // get the image Article
        URI imageArticle = null;
        String description = null;
        if ((issue.getImage() != null)
            && (issue.getImage().toString().length() != 0)) {
          final Article article = (Article) session.get(Article.class, issue.getImage());
          if (article != null) {
            imageArticle = issue.getImage();
            description = article.getDublinCore().getDescription();
          }
        }

        // derive prev/next Issue, "parent" Volume
        URI prevIssueURI = null;
        URI nextIssueURI = null;
        Volume parentVolume = null;

        List<String> results = session
            .createSQLQuery("select aggregationUri from VolumeIssueList where issueUri = :doi")
            .setParameter("doi", issueDOI.toString()).list();
        if (results.size() > 0) {
          URI volumeURI = URI.create(results.get(0));
          parentVolume = (Volume)session.load(Volume.class,  volumeURI);
        }
        if (parentVolume != null) {
          final List<URI> issues = parentVolume.getIssueList();
          final int issuePos = issues.indexOf(issueDOI);
          prevIssueURI = (issuePos == 0) ? null : issues.get(issuePos - 1);
          nextIssueURI = (issuePos == issues.size() - 1) ? null : issues.get(issuePos + 1);
        } else {
          log.warn("Issue: " + issue.getId() + ", not contained in any Volumes");
        }

        IssueInfo issueInfo = new IssueInfo(issue.getId(), issue.getDisplayName(), prevIssueURI,
            nextIssueURI, imageArticle, description,
            parentVolume == null ? null : parentVolume.getId());
        issueInfo.setArticleUriList(getArticleList(issue));
        return issueInfo;

      }
    });
  }

  /**
   * Return the ID of the latest issue from the latest volume. If no issue exists in the latest volume, then look at the
   * previous volume and so on. The Current Issue for each Journal should be configured via the admin console. This
   * method is a reasonable way to get the most recent Issue if Current Issue was not set.
   *
   * @param journal The journal in which to seek the most recent Issue
   * @return The most recent Issue from the most recent Volume, or null if there are no Issues
   */
  public URI getLatestIssueFromLatestVolume(Journal journal) {
    List<VolumeInfo> vols = getVolumeInfosForJournal(journal);
    if (vols.size() > 0) {
      for (VolumeInfo volInfo : vols) {
        IssueInfo latestIssue = null;
        List<IssueInfo> issuesInVol = volInfo.getIssueInfos();
        if (issuesInVol.size() > 0) {
          latestIssue = issuesInVol.get(issuesInVol.size() - 1);
        }
        if (latestIssue != null) {
          return latestIssue.getId();
        }
      }
    }
    return null;
  }

  /**
   * Returns the list of ArticleInfos contained in this Issue. The list will contain only ArticleInfos for Articles that
   * the current user has permission to view.
   *
   * @param issueDOI Issue ID
   * @param authId the AuthId of the current user
   * @return List of ArticleInfo objects.
   */
  @Transactional(readOnly = true)
  public List<ArticleInfo> getArticleInfosForIssue(final URI issueDOI, String authId) {

    IssueInfo iInfo = getIssueInfo(issueDOI);
    List<ArticleInfo> aInfos = new ArrayList<ArticleInfo>();

    for (URI articleDoi : iInfo.getArticleUriList()) {
      ArticleInfo ai = getArticleInfo(articleDoi, authId);
      if (ai == null) {
        log.warn("Article " + articleDoi + " missing; member of Issue " + issueDOI);
        continue;
      }
      aInfos.add(ai);
    }
    return aInfos;
  }

  /**
   * Get a VolumeInfo for the given id. This only works if the volume is in the current journal.
   *
   * @param id Volume ID
   * @return VolumeInfo
   */
  @Transactional(readOnly = true)
  public VolumeInfo getVolumeInfo(URI id, String journalKey) {
    // Attempt to get the volume infos from the cached journal list...
    List<VolumeInfo> volumes = getVolumeInfosForJournal(journalService.getJournal(journalKey));
    for (VolumeInfo vol : volumes) {
      if (id.equals(vol.getId())) {
        return vol;
      }
    }

    /*
     * If we have no luck with the cached journal list, attempt to load the volume re-using
     * loadVolumeInfos();
     */
    List<URI> l = new ArrayList<URI>();
    l.add(id);
    List<VolumeInfo> vols = loadVolumeInfos(l);
    if ((vols != null) && (vols.size() > 0)) {
      return vols.get(0);
    }
    return null;
  }

  /**
   * Returns a list of VolumeInfos for the given Journal. VolumeInfos are sorted in reverse order to reflect most common
   * usage. Uses the pull-through cache.
   *
   * @param journal To find VolumeInfos for.
   * @return VolumeInfos for journal in reverse order.
   */
  @Transactional(readOnly = true)
  public List<VolumeInfo> getVolumeInfosForJournal(final Journal journal) {
    final List<URI> volumeDois = journal.getVolumes();
    List<VolumeInfo> volumeInfos = loadVolumeInfos(volumeDois);
    Collections.reverse(volumeInfos);
    return volumeInfos;
  }

  /**
   * Get VolumeInfos. Note that the IssueInfos contained in the volumes have not been instantiated with the
   * ArticleInfos.
   *
   * @param volumeDois to look up.
   * @return volumeInfos.
   */
  private List<VolumeInfo> loadVolumeInfos(final List<URI> volumeDois) {
    List<VolumeInfo> volumeInfos = new ArrayList<VolumeInfo>();

    // get the Volumes
    for (int onVolumeDoi = 0; onVolumeDoi < volumeDois.size(); onVolumeDoi++) {
      final URI volumeDoi = volumeDois.get(onVolumeDoi);
      final Volume volume = (Volume) hibernateTemplate.get(Volume.class, volumeDoi);
      if (volume == null) {
        log.error("unable to load Volume: " + volumeDoi);
        continue;
      }
      // get the image Article, may be null
      URI imageArticle = null;
      String description = null;
      if (volume.getImage() != null) {
        final Article article = (Article) hibernateTemplate.get(Article.class, volume.getImage());
        if (article != null) {
          imageArticle = volume.getImage();
          description = article.getDublinCore().getDescription();
        }
      }

      List<IssueInfo> issueInfos = new ArrayList<IssueInfo>();
      for (final URI issueDoi : volume.getIssueList()) {
        issueInfos.add(getIssueInfo(issueDoi));
      }

      // calculate prev/next
      final URI prevVolumeDoi = (onVolumeDoi == 0) ? null : volumeDois.get(onVolumeDoi - 1);
      final URI nextVolumeDoi = (onVolumeDoi == volumeDois.size() - 1) ? null
          : volumeDois.get(onVolumeDoi + 1);
      final VolumeInfo volumeInfo = new VolumeInfo(volume.getId(), volume.getDisplayName(),
          prevVolumeDoi, nextVolumeDoi, imageArticle, description, issueInfos);
      volumeInfos.add(volumeInfo);
    }

    return volumeInfos;
  }

  private ArticleInfo loadArticleInfo(final URI id) {
    return (ArticleInfo)hibernateTemplate.execute(new HibernateCallback() {
      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        //TODO: make this a stored query in hibernate or else move this out to a more general location
        Article article = (Article) session.get(Article.class, id);
        if (article == null)
          return null;
        ArticleInfo articleInfo = new ArticleInfo();

        articleInfo.setId(article.getId());
        //Set properties from the dublin core
        if (article.getDublinCore() != null) {
          articleInfo.setDate(article.getDublinCore().getDate());
          articleInfo.setTitle(article.getDublinCore().getTitle());
          articleInfo.setDescription(article.getDublinCore().getDescription());
          articleInfo.setPublisher(article.getDublinCore().getPublisher());

          //Set the citation info
          if (article.getDublinCore().getBibliographicCitation() != null) {
            Citation citation = article.getDublinCore().getBibliographicCitation();
            CitationInfo citationInfo = new CitationInfo();
            citationInfo.setId(citation.getId());
            citationInfo.setCollaborativeAuthors(citation.getCollaborativeAuthors());
            //authors (list of UserProfileInfo)
            //TODO: Refactor ArticleInfo and CitationInfo objects - there's no reason why authors need to be attached to the citation
            List<UserProfileInfo> authors = new ArrayList<UserProfileInfo>();
            for (ArticleContributor ac : article.getAuthors()) {
              UserProfileInfo author = new UserProfileInfo();
              author.setId(ac.getId());
              author.setRealName(ac.getFullName());
              authors.add(author);
            }
            citationInfo.setAuthors(authors);

            articleInfo.setCi(citationInfo);
          }
        }

        //set related articles
        if (article.getRelatedArticles() != null && article.getRelatedArticles().size() > 0) {
          Set<RelatedArticle> relatedArticles = article.getRelatedArticles();
          Set<RelatedArticleInfo> relatedArticleInfos = new HashSet<RelatedArticleInfo>(relatedArticles.size());

          StringBuilder relatedArticleInfoQuery = new StringBuilder();
          relatedArticleInfoQuery.append("select dc.articleURI,dc.title from DublinCore dc where dc.articleURI in (");

          int i = 0;
          for (RelatedArticle relatedArticle : relatedArticles) {
            relatedArticleInfoQuery.append("'");
            relatedArticleInfoQuery.append(relatedArticle.getArticle());

            i++;

            if(i < relatedArticles.size()) {
              relatedArticleInfoQuery.append("',");
            } else {
              relatedArticleInfoQuery.append("') order by dc.date desc;");
            }
          }

          Query query = session.createSQLQuery(relatedArticleInfoQuery.toString());

          for (Object[] row : (List<Object[]>)query.list()) {
            RelatedArticleInfo relatedArticleInfo = new RelatedArticleInfo();
            relatedArticleInfo.setUri(URI.create((String)row[0]));
            relatedArticleInfo.setTitle((String) row[1]);
            relatedArticleInfos.add(relatedArticleInfo);
          }

          articleInfo.setRelatedArticles(relatedArticleInfos);
        }

        //set categories
        if (article.getCategories() != null) {
          for (Category category : article.getCategories()) {
            if (category.getMainCategory() != null)
              articleInfo.getSubjects().add(category.getMainCategory());
            if (category.getSubCategory() != null)
              articleInfo.getSubjects().add(category.getSubCategory());
          }
        }
        //set article type
        if (article.getArticleType() != null) {
          articleInfo.setAt(article.getArticleType());
        }

        //set journals
        /*fixed for NHOPE-103 */
        Set<String> journalName= journalService.getJournalNameForObject(id);

        articleInfo.setJournals(journalName);

        //set Corrections
        articleInfo.setCorrections(new HashSet<URI>(
            session.createQuery(
                "select fc.id from FormalCorrection fc where fc.annotates = :id"
            ).setParameter("id", article.getId()).list()
        ));
        //set Retractions
        articleInfo.setRetractions(new HashSet<URI>(
            session.createQuery(
                "select r.id from Retraction r where r.annotates = :id"
            ).setParameter("id", article.getId()).list()
        ));

        if (log.isDebugEnabled()) {
          log.debug("loaded ArticleInfo: id='" + articleInfo.getId() +
              "', articleTypes='" + articleInfo.getArticleTypes() +
              "', date='" + articleInfo.getDate() +
              "', title='" + articleInfo.getTitle() +
              "', authors='" + articleInfo.getAuthors() +
              "', related-articles='" + articleInfo.getRelatedArticles() + "'");
        }

        return articleInfo;
      }
    });
  }

  private Years loadArticleDates(String journalKey) {
    Years dates = new Years();

    SolrQuery query = createCommonQuery(journalKey);

    query.setFacet(true);
    query.addFacetField("publication_date");
    query.setFacetLimit(-1);
    query.setFacetMinCount(1);

    query.setRows(0);

    try {
      QueryResponse response = this.serverFactory.getServer().query(query);
      FacetField ff = response.getFacetField("publication_date");
      List<FacetField.Count> counts = ff.getValues();
      for (FacetField.Count count : counts) {
        String publicationDate = count.getName();
        Integer y = getYear(publicationDate);
        Integer m = getMonth(publicationDate);
        Integer d = getDay(publicationDate);
        dates.getMonths(y).getDays(m).add(d);
      }
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
    }

    return dates;
  }

  /**
   * Get the articleInfo object for an article
   * @param id the ID of the article
   * @param authId the authorization ID of the current user
   * @return articleInfo
   */
  @Transactional(readOnly = true)
  public ArticleInfo getArticleInfo(final URI id, final String authId) {
    try {
      Article a = articlePersistenceService.getArticle(id, authId);
    } catch (SecurityException se) {
      log.debug("Filtering URI " + id + " from Article list due to SecurityException", se);
      return null;
    } catch (NoSuchArticleIdException ex) {
      log.debug("Filtering URI " + id + " NoSuchArticleIdException", ex);
      return null;
    }


    return loadArticleInfo(id);
  }

  /**
   * Given a list of Article Groups with correctly ordered articles create a CSV string of article URIs. The URIs will
   * be in the order that they show up on the TOC.
   *
   * @param articleGroups the list of TOCArticleGroup to process.
   * @return a string of a comma separated list of article URIs
   */
  public String articleGrpListToCSV(List<TOCArticleGroup> articleGroups) {
    StringBuilder articleList = new StringBuilder();
    Iterator i = articleGroups.listIterator();

    // Group Loop
    while (i.hasNext()) {
      TOCArticleGroup ag = (TOCArticleGroup) i.next();
      Iterator y = ag.articles.listIterator();

      // Article Loop
      while (y.hasNext()) {
        ArticleInfo ai = (ArticleInfo) y.next();
        articleList.append(ai.id.toString());

        if (y.hasNext())
          articleList.append(',');
      }
      if (i.hasNext())
        articleList.append(',');
    }
    return articleList.toString();
  }

  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(URI issueURI, String authId) {
    Issue issue = (Issue) hibernateTemplate.get(Issue.class, issueURI);

    // If the issue does not exist then return an empty list
    if (issue == null)
      return new ArrayList<TOCArticleGroup>();

    return getArticleGrpList(issue, authId);
  }

  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(Issue issue, String authId) {
    List<TOCArticleGroup> groupList = new ArrayList<TOCArticleGroup>();

    for (ArticleType at : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup newArticleGroup = new TOCArticleGroup(at);
      groupList.add(newArticleGroup);
    }

    return buildArticleGroups(issue, groupList, authId);
  }

  /**
   *
   */
  public List<TOCArticleGroup> buildArticleGroups(Issue issue, List<TOCArticleGroup> articleGroups, String authId) {
    List<ArticleInfo> articlesInIssue = getArticleInfosForIssue(issue.getId(), authId);
    /*
     * For every article that is of the same ArticleType as a TOCArticleGroup, add it to that group.
     * Articles can appear in multiple TOCArticleGroups.
     */
    for (ArticleInfo ai : articlesInIssue)
      for (TOCArticleGroup ag : articleGroups)
        for (ArticleType articleType : ai.getArticleTypes())
          if (ag.getArticleType().equals(articleType)) {
            ag.addArticle(ai);
            break;
          }

    Iterator iter = articleGroups.listIterator();
    Integer i = 0;

    while (iter.hasNext()) {
      TOCArticleGroup ag = (TOCArticleGroup) iter.next();
      // remove the group if it has no articles
      if (ag.articles.size() == 0) {
        iter.remove();
        continue;
      }
      // If we respect order then don't sort.
      if (!issue.getRespectOrder()) {
        ag.setId("tocGrp_" + (i++));
        ag.sortArticles();
      }
    }
    return articleGroups;
  }

  /**
   * Get ordered list of articles. Either from articleList or from simpleCollection if articleList is empty.
   *
   * @param issue
   * @return List of article URI's
   */
  public List<URI> getArticleList(Issue issue) {
    List<URI> articleList = issue.getArticleList();
    if (articleList.isEmpty() && !issue.getSimpleCollection().isEmpty())
      return new ArrayList<URI>(issue.getSimpleCollection());

    return articleList;
  }

  /**
   * ArticleInfo objects store lists of FormalCorrection id's because ArticleInfo is object that is not cached in the
   * ObjectCache and it cannot hold referencews to OTM entities because it will cause it to fail serialization. We need
   * to fetch FormalCorrection objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all FormalCorrection objects contained in articleGroups
   */
  public Map<URI, FormalCorrection> getCorrectionMap(List<TOCArticleGroup> articleGroups) {
    Map<URI, FormalCorrection> correctionMap = new HashMap<URI, FormalCorrection>();

    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        if (articleInfo.getCorrections() != null) {
          for (URI correctionId : articleInfo.getCorrections()) {
            correctionMap.put(
                correctionId,
                (FormalCorrection) hibernateTemplate.get(FormalCorrection.class, correctionId)
            );
          }
        }
      }
    }

    return correctionMap;
  }

  /**
   * ArticleInfo objects store lists of Retraction id's because ArticleInfo is object that is not cached in the
   * ObjectCache and it cannot hold referencews to OTM entities because it will cause it to fail serialization. We need
   * to fetch Retraction objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all Retraction objects contained in articleGroups
   */
  public Map<URI, Retraction> getRetractionMap(List<TOCArticleGroup> articleGroups) {
    Map<URI, Retraction> retractionMap = new HashMap<URI, Retraction>();

    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        if (articleInfo.getRetractions() != null) {
          for (URI retractionId : articleInfo.getRetractions()) {
            retractionMap.put(
                retractionId,
                (Retraction) hibernateTemplate.get(Retraction.class, retractionId)
            );
          }
        }
      }
    }

    return retractionMap;
  }

  /**
   * Sets the solr server factory object
   * @param serverFactory solr server factory
   */
  public void setServerFactory(SolrServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }

  /**
   * Get a list of article counts for each category
   * @return category info
   */
  private SortedMap<String, Long> getArticlesByCategoryViaSolr(String journalKey) {

    SortedMap<String, Long> categories = new TreeMap<String, Long>();

    SolrQuery query = createCommonQuery(journalKey);

    query.setFacet(true);
    query.addFacetField("subject_level_1");
    query.setFacetLimit(-1);
    query.setFacetMinCount(1);

    query.setRows(0);

    try {
      QueryResponse response = this.serverFactory.getServer().query(query);
      FacetField ff = response.getFacetField("subject_level_1");
      List<FacetField.Count> counts = ff.getValues();
      if (counts != null) {
        for (FacetField.Count count : counts) {
          categories.put(count.getName(), count.getCount());
        }
      }
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
    }

    return categories;
  }

  /**
   * Returns a list of articles for a given category
   * @param category category
   * @param pageNum page number for which to return articles; 0-based
   * @param pageSize number of articles per page
   * @return articles
   */
  public BrowseResult getArticlesByCategoryViaSolr(final String category, final int pageNum, final int pageSize, String journalKey) {

    ArrayList<SearchHit> articles = new ArrayList<SearchHit>();
    long total = 0;

    SolrQuery query = createCommonQuery(journalKey);

    query.addField("title_display");
    query.addField("author_display");
    query.addField("article_type");
    query.addField("publication_date");
    query.addField("id");
    query.addField("abstract_primary_display");
    query.addField("eissn");

    query.addFilterQuery("subject_level_1:\"" + category + "\"");

    query.setSortField("publication_date", SolrQuery.ORDER.desc);

    query.setStart(pageNum * pageSize);
    query.setRows(pageSize);

    try {
      QueryResponse response = this.serverFactory.getServer().query(query);
      SolrDocumentList documentList = response.getResults();
      total = documentList.getNumFound();
      for (SolrDocument document : documentList) {

        SearchHit sh = createArticleBrowseDisplay(document, query.toString());

        articles.add(sh);
      }
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
    }

    BrowseResult result = new BrowseResult();
    result.setTotal(total);
    result.setArticles(articles);

    return result;
  }

  /**
   * Returns list of articles in a given date range, from newest to oldest
   * @param startDate start date
   * @param endDate end date
   * @param articleTypes The URIs indicating the types of articles which will be returned. or null for all types
   * @param pageNum the page-number for which to return articles; 0-based
   * @param pageSize the number of articles per page, or -1 for all articles
   * @return the articles
   */
  private BrowseResult getArticlesByDateViaSolr(final Calendar startDate, final Calendar endDate,
                                                final List<URI> articleTypes, final int pageNum, final int pageSize,
                                                String journalKey) {

    ArrayList<SearchHit> articles = new ArrayList<SearchHit>();
    long totalSize = 0;

    SolrQuery query = createCommonQuery(journalKey);

    query.addField("title_display");
    query.addField("author_display");
    query.addField("article_type");
    query.addField("publication_date");
    query.addField("id");
    query.addField("abstract_primary_display");
    query.addField("eissn");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String sDate = sdf.format(startDate.getTime());
    String eDate = sdf.format(endDate.getTime());

    sDate = sDate + "T00:00:00Z";
    eDate = eDate + "T00:00:00Z";

    query.addFilterQuery("publication_date:[" + sDate + " TO " + eDate + "]");

    StringBuffer sb = new StringBuffer();
    if (articleTypes != null && articleTypes.size() > 0) {
      for (URI uri : articleTypes) {
        String path = uri.getPath();
        int index = path.lastIndexOf("/");
        if (index != -1) {
          String articleType = path.substring(index + 1);
          sb.append("\"").append(articleType).append("\"").append(" OR ");
        }
      }
      String articleTypesQuery = sb.substring(0, sb.length() - 4);

      if (articleTypesQuery.length() > 0) {
        query.addFilterQuery("article_type_facet:" + articleTypesQuery);
      }
    }

    query.addSortField("publication_date", SolrQuery.ORDER.desc);
    query.addSortField("id", SolrQuery.ORDER.asc);

    query.setStart(pageNum * pageSize);
    query.setRows(pageSize);

    log.info("getArticlesByDate Solr Query:" + query.toString());

    try {
      QueryResponse response = this.serverFactory.getServer().query(query);
      SolrDocumentList documentList = response.getResults();
      totalSize = documentList.getNumFound();

      for (SolrDocument document : documentList) {

        SearchHit sh = createArticleBrowseDisplay(document, query.toString());

        articles.add(sh);
      }
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
    }

    BrowseResult result = new BrowseResult();
    result.setArticles(articles);
    result.setTotal(totalSize);

    return result;
  }

  /**
   * Creates a commonly used SolrQuery object
   * @return pre-populated SolrQuery object
   */
  private SolrQuery createCommonQuery(String journalKey) {
    SolrQuery query = new SolrQuery("*:*");

    query.addFilterQuery("doc_type:full");
    query.addFilterQuery("!article_type_facet:\"Issue Image\"");
    query.addFilterQuery("cross_published_journal_key:" + journalKey);

    return query;
  }

  /**
   * Populates the SearchHit object using SolrDocument object (from search result)
   * @param document one search result
   * @return populated SearchHit objects
   */
  private SearchHit createArticleBrowseDisplay(SolrDocument document, String query) {

    String id = SolrServiceUtil.getFieldValue(document, "id", String.class, query);
    String message = id == null ? query : id;
    String title = SolrServiceUtil.getFieldValue(document, "title_display", String.class, message);
    Date publicationDate = SolrServiceUtil.getFieldValue(document, "publication_date", Date.class, message);
    String eissn = SolrServiceUtil.getFieldValue(document, "eissn", String.class, message);
    String articleType = SolrServiceUtil.getFieldValue(document, "article_type", String.class, message);
    String abstractDisplay = SolrServiceUtil.getFieldValue(document, "abstract_primary_display", String.class, message);

    List<String> authorList = SolrServiceUtil.getFieldMultiValue(document, message, String.class, "author_display");

    SearchHit hit = new SearchHit(
        null, id, title, null, authorList, publicationDate, eissn, null, articleType);

    hit.setAbstractPrimary(abstractDisplay);

    return hit;
  }

  /**
   * Checks to see if solr is up or not
   * @throws org.apache.solr.client.solrj.SolrServerException
   * @throws java.io.IOException
   */
  public void pingSolr() throws SolrServerException, IOException {
    this.serverFactory.getServer().ping();
  }
}
