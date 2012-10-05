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

package org.ambraproject.service.trackback;

import org.ambraproject.models.Article;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Linkback;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.views.LinkbackView;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class LinkbackServiceImpl extends HibernateServiceImpl implements LinkbackService {

  private static final Logger log = LoggerFactory.getLogger(LinkbackServiceImpl.class);
  protected static final String DOI_RESOLVER_HOST = "dx.doi.org";
  private static final String DEFAULT_DOI_SCHEME = "info:doi/";

  protected abstract Configuration getConfiguration();


  /**
   * {@inheritDoc}
   */
  @Override
  public BlogLinkDigest examineBlogPage(URL blogUrl, LinkValidator linkValidator) throws IOException {
    log.debug("Validating blog at {}", blogUrl);

    // Trick gets Swing's HTML parser
    HTMLEditorKit.Parser parser = (new HTMLEditorKit() {
      public Parser getParser() {
        return super.getParser();
      }
    }).getParser();


    // Read HTML file into string
    StringBuilder html = new StringBuilder();
    BufferedReader bufferedReader = null;
    try {
      InputStream inputStream = blogUrl.openStream();
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        html.append(line);
      }

      //parse the html, looking for links
      LinkCallback callback = new LinkCallback(linkValidator);
      parser.parse(new StringReader(html.toString()), callback, true);
      return callback.makeDigest();
    } finally {
      //close our reader (closes all the encapsulated streams)
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          log.error("Error closing buffered input reader to " + blogUrl, e);
        }
      }
    }
  }

  protected static String fetchJournalName(HibernateTemplate hibernateTemplate, String eIssn) {
    return (String) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Journal.class)
            .add(Restrictions.eq("eIssn", eIssn))
            .setProjection(Projections.property("journalKey")),
        0, 1).get(0);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LinkbackView> getLinkbacksForArticle(String articleDoi) {
    return getLinkbacksForArticle(Linkback.class, articleDoi);
  }

  protected List<LinkbackView> getLinkbacksForArticle(Class<? extends Linkback> type, String articleDoi) {
    if (StringUtils.isEmpty(articleDoi)) {
      throw new IllegalArgumentException("No Doi specified");
    }
    Long articleId;
    String articleTitle;
    try {
      Object[] articleRow = (Object[]) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", articleDoi))
              .setProjection(Projections.projectionList()
                  .add(Projections.id())
                  .add(Projections.property("title"))
              ), 0, 1
      ).get(0);
      articleId = (Long) articleRow[0];
      articleTitle = (String) articleRow[1];
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Doi " + articleDoi + " didn't correspond to an article");
    }
    log.debug("loading up linkbacks for article {}", articleDoi);

    List<? extends Linkback> linkbacks = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(type)
            .add(Restrictions.eq("articleID", articleId))
            .addOrder(Order.desc("created"))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );
    List<LinkbackView> results = new ArrayList<LinkbackView>(linkbacks.size());
    for (Linkback linkback : linkbacks) {
      results.add(new LinkbackView(linkback, articleDoi, articleTitle));
    }

    log.info("Loaded {} linkbacks for {}", results.size(), articleDoi);
    return results;
  }

  @Override
  public int countLinkbacksForArticle(String articleDoi) {
    return countLinkbacksForArticle(Linkback.class, articleDoi);
  }

  protected int countLinkbacksForArticle(Class<? extends Linkback> type, String articleDoi) {
    if (StringUtils.isEmpty(articleDoi)) {
      throw new IllegalArgumentException("Didn't specify an article doi");
    }
    Long articleId;
    try {
      articleId = (Long) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", articleDoi))
              .setProjection(Projections.id()), 0, 1
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Doi: " + articleDoi + " didn't correspond to an article");
    }

    // Get a list of row counts, one for each subtype. Return their sum.
    List<? extends Number> counts = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(type)
            .add(Restrictions.eq("articleID", articleId))
            .setProjection(Projections.rowCount())
    );
    int sum = 0;
    for (Number count : counts) {
      sum += count.intValue();
    }
    return sum;
  }

  /**
   * Parser callback that examines HTML (typically a blog post) to see if there is a link to the article URL in it. It
   * also picks up the page title, and can yield both pieces of data as a {@link BlogLinkDigest}.
   * <p/>
   * Once the parser using this callback has found enough data for a complete {@link BlogLinkDigest}, the callback will
   * throw a {@code ParserEarlyHaltException} to interrupt the parser. Any code calling the parser must catch (and will
   * generally ignore) the exception.
   */
  protected static final class LinkCallback extends HTMLEditorKit.ParserCallback {

    private final LinkValidator linkValidator;

    private boolean atTitle = false;
    private URL link = null;
    private String title = null;

    private LinkCallback(LinkValidator linkValidator) {
      this.linkValidator = linkValidator;
    }

    //Callback method
    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
      if (HTML.Tag.A == tag) {
        String href = (String) attributes.getAttribute(HTML.Attribute.HREF);
        if (href == null) {
          return;
        }

        URL blogLink;
        try {
          blogLink = new URL(href);
        } catch (MalformedURLException e) {
          return; // Ignore invalid or non-URL links
        }
        if (linkValidator.isValid(blogLink)) {
          this.link = blogLink;
        }
      } else if (HTML.Tag.TITLE == tag) {
        // Valid HTML has no elements nested in <title>, so expect the next handleText call to have the title
        atTitle = true;
      }
    }

    @Override
    public void handleText(char[] data, int pos) {
      if (atTitle) {
        title = String.valueOf(data);
        atTitle = false;
      }
    }

    public BlogLinkDigest makeDigest() {
      return new BlogLinkDigest(link, title);
    }

  }

  /**
   * Signals that we have everything we need from an external HTML page. Throw it to interrupt the parser.
   */
  private static class ParserEarlyHaltException extends RuntimeException {
    private ParserEarlyHaltException() {
      super();
    }
  }

}
