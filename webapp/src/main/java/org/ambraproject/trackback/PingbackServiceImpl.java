/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.trackback;

import org.ambraproject.models.Article;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Pingback;
import org.apache.commons.configuration.Configuration;
import org.apache.xmlrpc.XmlRpcException;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class PingbackServiceImpl extends LinkbackServiceImpl implements PingbackService {

  private static final Logger log = LoggerFactory.getLogger(PingbackServiceImpl.class);

  private Configuration configuration;

  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected Configuration getConfiguration() {
    return configuration;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public Long createPingback(URI sourceUri, URI targetUri, String pbServerHost) throws XmlRpcException {
    final URL targetUrl;
    try {
      targetUrl = targetUri.toURL();
    } catch (MalformedURLException e) {
      throw PingbackFault.TARGET_DNE.getException();
    }
    Article target = getArticleFromTargetUri(targetUrl, pbServerHost);

    LinkValidator matchTarget = new LinkValidator() {
      @Override
      public boolean isValid(URL link) {
        // They give us the URL they claim to use, so just check for that without worrying about cross-published URLs
        return targetUrl.equals(link);
      }
    };
    BlogLinkDigest blogInfo;
    try {
      blogInfo = examineBlogPage(sourceUri.toURL(), matchTarget);
    } catch (IOException e) {
      // Generally means that the source page can't be accessed or parsed
      throw PingbackFault.SOURCE_DNE.getException(e);
    }
    if (blogInfo.getLink() == null) {
      throw PingbackFault.NO_LINK_TO_TARGET.getException();
    }

    Long preexisting = (Long) hibernateTemplate.findByCriteria(DetachedCriteria.forClass(Pingback.class)
        .setProjection(Projections.rowCount())
        .add(Restrictions.eq("url", sourceUri.toString()))
        .add(Restrictions.eq("articleID", target.getID()))
    ).get(0);
    if (preexisting > 0) {
      throw PingbackFault.ALREADY_REGISTERED.getException();
    }

    Pingback pb = new Pingback();
    pb.setUrl(sourceUri.toString());
    pb.setTitle(blogInfo.getTitle());
    pb.setArticleID(target.getID());

    return (Long) hibernateTemplate.save(pb);
  }

  /**
   * Look up an article from the absolute target URI provided within a pingback request. If the argument (which
   * originated from an external source) can't be resolved to an article, throw an exception containing the fault code
   * to send as a response to the server that provided the URI.
   * <p/>
   * We want journals to behave independently whether or not they share a back end. So, the link is valid only if: <ul>
   * <li>the link contains the DOI of an article,</li> <li>the link's hostname belongs to a journal where that article
   * was published, and</li> <li>the pingback was addressed to the same hostname.</li> </ul>
   *
   * @param link         an absolute URI
   * @param pbServerHost the host on which the pingback was received
   * @return the article
   * @throws XmlRpcException with the {@link PingbackFault#TARGET_DNE} fault code if the target URI does not exist or is
   *                         not recognizable (where possible, it is preferable to throw a {@link
   *                         PingbackFault#INVALID_TARGET} fault code if the target URI goes to an existing page other
   *                         than a permalink to a pingback-supporting article)
   */
  private Article getArticleFromTargetUri(URL link, String pbServerHost) throws XmlRpcException {
    // Find the DOI
    boolean usesResolver = true;
    String doi = InboundLinkTranslator.GLOBAL_RESOLVER.getDoi(link);
    if (doi == null) {
      doi = InboundLinkTranslator.forLocalResolver(configuration).getDoi(link);
    }
    if (doi == null) {
      usesResolver = false; // Need to validate the hostname against journals after we have the article
      doi = InboundLinkTranslator.forAnyJournal(configuration).getDoi(link);
    }

    if (doi == null) {
      throw PingbackFault.TARGET_DNE.getException();
    }

    // Look up the article referenced by that DOI, if any
    List<Article> queryResults = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("doi", doi))
            .setFetchMode("journals", FetchMode.JOIN)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
    if (queryResults.isEmpty()) {
      throw PingbackFault.TARGET_DNE.getException();
    }
    Article article = queryResults.get(0);

    if (usesResolver) {
      return article;
    }

    // Validate the hostname
    for (Journal journal : article.getJournals()) {
      String journalHost = InboundLinkTranslator.getHostnameForJournal(journal.getJournalKey(), configuration);
      if (journalHost.equals(pbServerHost)) {
        // This journal's site received the pingback, so require that the link goes there
        InboundLinkTranslator journalTranslator = InboundLinkTranslator.forJournal(journal, configuration);
        if (journalTranslator.getDoi(link) != null) {
          return article;
        }
        break; // The link destination doesn't match the pingback destination
      }
    }
    throw PingbackFault.TARGET_DNE.getException();
  }

}