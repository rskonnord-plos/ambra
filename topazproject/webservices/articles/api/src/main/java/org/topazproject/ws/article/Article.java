/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.article;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.activation.DataHandler;

/** 
 * Article storage and retrieval.
 * 
 * @author Ronald Tschalär
 */
public interface Article extends Remote {
  /**
   * Permissions associated with Article storage and retrieval.
   */
  public static interface Permissions {
    /** The action that represents an ingest operation in XACML policies. */
    public static final String INGEST_ARTICLE = "articles:ingestArticle";

    /** The action that represents a delete operation in XACML policies. */
    public static final String DELETE_ARTICLE = "articles:deleteArticle";

    /** The action that represents a set-state operation in XACML policies. */
    public static final String SET_ARTICLE_STATE = "articles:setArticleState";

    /** The action that represents a get-object-url operation in XACML policies. */
    public static final String GET_OBJECT_URL = "articles:getObjectURL";

    /** The action that represents a set-representation operation in XACML policies. */
    public static final String SET_REPRESENTATION = "articles:setRepresentation";

    /** The action that represents a set-author-user-ids operation in XACML policies. */
    public static final String SET_AUTHOR_USER_IDS = "articles:setAuthorUserIds";

    /** The action that represents a get-object-info operation in XACML policies. */
    public static final String GET_OBJECT_INFO = "articles:getObjectInfo";

    /** The action that represents a list-secondary-objects operation in XACML policies. */
    public static final String LIST_SEC_OBJECTS = "articles:listSecondaryObjects";

    /** The action that represents checking if we can access a specific article. */
    public static final String READ_META_DATA = "articles:readMetaData";
  }

  /** Article state of "Active" */
  public static final int ST_ACTIVE   = 0;
  /** Article state of "Disabled" */
  public static final int ST_DISABLED = 1;

  /** 
   * Add a new article.
   * 
   * @param zip    a zip archive containing the article and associated objects. The content type
   *               should be <var>application/zip</var>. If possible this should contain the name
   *               of the zip too.
   * @return the URI of the new article
   * @throws DuplicateArticleIdException if the article already exists (as determined by its URI)
   * @throws IngestException if there's a problem ingesting the archive
   * @throws RemoteException if some other error occured
   */
  public String ingest(DataHandler zip)
      throws DuplicateArticleIdException, IngestException, RemoteException;

  /** 
   * Marks an article as superseded by another article.
   * 
   * @param oldArt the URI of the article that has been superseded by <var>newUri</var>
   * @param newArt the URI of the article that supersedes <var>oldUri</var>
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void markSuperseded(String oldArt, String newArt)
      throws NoSuchArticleIdException, RemoteException;

  /** 
   * Change an article's state.
   * 
   * @param article the URI of the article (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @param state   the new state
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void setState(String article, int state) throws NoSuchArticleIdException, RemoteException;

  /** 
   * Delete an article. Note that it may not be possible to find and therefore erase all traces
   * from the ingest.
   * 
   * @param article the URI of the article (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void delete(String article) throws NoSuchArticleIdException, RemoteException;

  /** 
   * Get the URL from which the object's contents can retrieved via GET. Note that this method may
   * return a URL even when object or the representation don't exist, in which case the URL may
   * return a 404 response.
   * 
   * @param obj the URI of the object (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @param rep the desired representation of the object
   * @return the URL, or null if the desired representation does not exist
   * @throws NoSuchObjectIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public String getObjectURL(String obj, String rep)
      throws NoSuchObjectIdException, RemoteException;

  /** 
   * Create or update a representation of an object. The object itself must exist; if the specified
   * representation does not exist, it is created, otherwise the current one is replaced.
   * 
   * @param obj      the URI of the object
   * @param rep      the name of this representation
   * @param content  the actual content that makes up this representation; if this contains a
   *                 content-type then that will be used; otherwise the content-type will be
   *                 set to <var>application/octet-stream</var>; may be null, in which case
   *                 the representation is removed.
   * @throws NoSuchObjectException if the object does not exist
   * @throws RemoteException if some other error occured
   * @throws NullPointerException if any of the parameters are null
   */
  public void setRepresentation(String obj, String rep, DataHandler content)
      throws NoSuchObjectIdException, RemoteException;

  /** 
   * Set the list user-ids for the authors of the article. This completely replaces any previous
   * list for the article.
   * 
   * @param article the URI of the article (e.g. "info:doi/10.1371/journal.pbio.003811")
   * @param userIds the list of user-ids of authors of the article; may be null in which
   *                case any existing list is erased.
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public void setAuthorUserIds(String article, String[] userIds)
      throws NoSuchArticleIdException, RemoteException;

  /** 
   * Get the info for a single object. This may be either an article or a secondary object. 
   * 
   * @param obj the URI of the object
   * @return the object's info
   * @throws NoSuchObjectIdException if the object does not exist
   * @throws RemoteException if some other error occured
   */
  public ObjectInfo getObjectInfo(String obj) throws NoSuchObjectIdException, RemoteException;

  /** 
   * Get the list of secondary objects for the specified article. 
   * 
   * @param article the URI of the article
   * @return the (possibly empty) list of secondary objects; these will be in the same order
   *         as they (first) appear in the article.
   * @throws NoSuchArticleIdException if the article does not exist
   * @throws RemoteException if some other error occured
   */
  public ObjectInfo[] listSecondaryObjects(String article)
      throws NoSuchArticleIdException, RemoteException;

  /**
   * Get list of articles for a given set of categories or authors bracked by specified
   * times. List is returned as an XML string of the following format:
   * <pre>
   *   &lt;articles&gt;
   *     &lt;article&gt;
   *       &lt;uri&gt;...&lt;/uri&gt;
   *       &lt;title&gt;...&lt;/title&gt;
   *       &lt;description&gt;...&lt;/description&gt;
   *       &lt;date&gt;YYY-MM-DD&lt;/date&gt;
   *       &lt;authors&gt;
   *         &lt;author&gt;...&lt;/author&gt;
   *         ...
   *       &lt;/authors&gt;
   *       &lt;categories&gt;
   *         &lt;category&gt;...&lt;/category&gt;
   *         ...
   * </pre>
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
   *                   be appropriate. For archive display, ascending would be appropriate.
   * @return the xml for the specified feed
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public String getArticles(String startDate, String endDate, String[] categories, String[] authors,
                            int[] states, boolean ascending) throws RemoteException;

  /**
   * Get list of articles for a given set of categories or authors bracked by specified
   * times. 
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
   *                   be appropriate. For archive display, ascending would be appropriate.
   * @return the (possibly empty) list of articles.
   * @throws RemoteException if there was a problem talking to the alerts service
   */
  public ArticleInfo[] getArticleInfos(String startDate, String endDate,
                                       String[] categories, String[] authors, int[] states,
                                       boolean ascending) throws RemoteException;

  /**
   * Get the list of most commented articles. This currently returns
   * articles with most number of annotations.
   *
   * @param maxArticles the maximum number of articles to return
   * @return the (possibly empty) list of objects; these will be in the
   *         descending order of total number of annotations.
   * @throws RemoteException if there was any problem accessing the remote
   *         service or processing errors
   */
  public ObjectInfo[] getCommentedArticles(int maxArticles)
    throws RemoteException;
}
