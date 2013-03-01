package org.ambraproject.search;

import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.ambraproject.email.TemplateMailer;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Send saved searches
 *
 * @author Joe Osowski
 */
public class SavedSearchSenderImpl extends HibernateServiceImpl implements SavedSearchSender {
  private static final Logger log = LoggerFactory.getLogger(SavedSearchSenderImpl.class);

  private TemplateMailer mailer;

  /**
   * @inheritDoc
   */
  public void sendSavedSearch(SavedSearchJob searchJob) {

    log.info("Received thread Name: {}", Thread.currentThread().getName());
    log.info("Send emails for search ID: {}. {}", searchJob.getSavedSearchQueryID(), searchJob.getType());

    final Map<String, Object> context = new HashMap<String, Object>();

    context.put("searchParameters", searchJob.getSearchParams());
    context.put("searchHitList", searchJob.getSearchHitList());
    context.put("startTime", searchJob.getStartDate());
    context.put("endTime", searchJob.getEndDate());

    //TODO: Move to config
    context.put("imagePath", "/bleh.gif");

    //Create message
    Multipart content = createContent(context);

    List<String> emails = getSavedSearchEmails(searchJob.getSavedSearchQueryID(), searchJob.getType());

    //TODO: provide override for dev mode and allow QA to adjust in ambra.xml
    String toAddresses = StringUtils.join(emails, " ");

    //TODO: move to config?
    String fromAddress = "admin@plos.org";

    String subject = "bleh";

    mailer.mail(toAddresses, fromAddress, subject, context, content);

    log.info("Completed thread Name: {}", Thread.currentThread().getName());
    log.info("Completed send request for search ID: {}. {}", searchJob.getSavedSearchQueryID(), searchJob.getType());

    //When a results are sent updated the records to indicate
    markSent(searchJob);
  }

  @SuppressWarnings("unchecked")
  private void markSent(SavedSearchJob searchJob)
  {
    //Find all the saved searches associated with this search
    List<SavedSearch> savedSearches = hibernateTemplate.findByCriteria(DetachedCriteria.forClass(SavedSearch.class)
      .createAlias("searchQuery", "q")
      .add(Restrictions.eq("q.ID", searchJob.getSavedSearchQueryID()))
      .add(Restrictions.eq("weekly", searchJob.getType().equals("WEEKLY"))));

    for(SavedSearch savedSearch : savedSearches) {
      if(searchJob.getType().equals("WEEKLY")) {
        savedSearch.setLastWeeklySearchTime(searchJob.getEndDate());
      } else {
        savedSearch.setLastMonthlySearchTime(searchJob.getEndDate());
      }

      hibernateTemplate.update(savedSearch);

      log.debug("Updated Last {} saved Search Time for Saved Search ID: {}", searchJob.getType(), savedSearch.getID());
    }
  }

  private Multipart createContent(Map<String, Object> context) {
    try {
      //TODO: Move filenames to configuration
      return mailer.createContent("email-text.ftl", "email-html.ftl", context);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    } catch(MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> getSavedSearchEmails(Long savedSearchQueryID, String type) {
    SavedSearchRetriever.AlertType alertType = SavedSearchRetriever.AlertType.valueOf(type);

    DetachedCriteria criteria = DetachedCriteria.forClass(UserProfile.class)
      .setProjection(Projections.property("email"))
      .createAlias("savedSearches", "ss")
      .createAlias("ss.searchQuery", "q")
      .add(Restrictions.eq("q.ID", savedSearchQueryID));

    if(alertType == SavedSearchRetriever.AlertType.WEEKLY) {
      criteria.add(Restrictions.eq("ss.weekly", true));
    }

    if(alertType == SavedSearchRetriever.AlertType.MONTHLY) {
      criteria.add(Restrictions.eq("ss.monthly", true));
    }

    List<String> results = hibernateTemplate.findByCriteria(criteria);
    List<String> emails = new ArrayList<String>();

    for(String email : results) {
      emails.add(email);
    }

    return emails;
  }

  @Required
  public void setMailer(TemplateMailer mailer) {
    this.mailer = mailer;
  }
}