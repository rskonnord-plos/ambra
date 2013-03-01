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
import java.util.Date;
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

    List<Object[]> searchDetails = getSavedSearchDetails(searchJob.getSavedSearchQueryID(), searchJob.getType());

    //TODO: move to config?
    String fromAddress = "admin@plos.org";

    for(int a = 0; a < searchDetails.size(); a++) {
      //TODO: provide override for dev mode and allow QA to adjust in ambra.xml
      String toAddress = (String)searchDetails.get(a)[1];

      //TODO: Change based on type / move to config
      String subject = "PLOS Search Alert - " + searchDetails.get(a)[2];

      mailer.mail(toAddress, fromAddress, subject, context, content);

      //When a results are sent updated the records to indicate
      markSent((Long)searchDetails.get(a)[0], searchJob.getType(), searchJob.getEndDate());
    }

    log.info("Completed thread Name: {}", Thread.currentThread().getName());
    log.info("Completed send request for search ID: {}. {}", searchJob.getSavedSearchQueryID(), searchJob.getType());
  }

  @SuppressWarnings("unchecked")
  private void markSent(Long savedSearchID, String type, Date endDate)
  {
    SavedSearch savedSearch = hibernateTemplate.get(SavedSearch.class, savedSearchID);

    if(savedSearch == null) {
      throw new RuntimeException("Could not find savedSearch: " + savedSearchID);
    }

    if(type.equals("WEEKLY")) {
      savedSearch.setLastWeeklySearchTime(endDate);
    } else {
      savedSearch.setLastMonthlySearchTime(endDate);
    }

    hibernateTemplate.update(savedSearch);

    log.debug("Updated Last {} saved Search time for Saved Search ID: {}", type, savedSearchID);
  }

  private Multipart createContent(Map<String, Object> context) {
    try {
      //TODO: Move filenames to configuration
      //TODO: Use different style template based on search type
      return mailer.createContent("email-text.ftl", "email-html.ftl", context);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    } catch(MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getSavedSearchDetails(Long savedSearchQueryID, String type) {
    SavedSearchRetriever.AlertType alertType = SavedSearchRetriever.AlertType.valueOf(type);

    DetachedCriteria criteria = DetachedCriteria.forClass(UserProfile.class)
      .setProjection(Projections.distinct(Projections.projectionList()
        .add(Projections.property("ss.ID"))
        .add(Projections.property("email"))
        .add(Projections.property("ss.searchName"))))
      .createAlias("savedSearches", "ss")
      .createAlias("ss.searchQuery", "q")
      .add(Restrictions.eq("q.ID", savedSearchQueryID));

    if(alertType == SavedSearchRetriever.AlertType.WEEKLY) {
      criteria.add(Restrictions.eq("ss.weekly", true));
    }

    if(alertType == SavedSearchRetriever.AlertType.MONTHLY) {
      criteria.add(Restrictions.eq("ss.monthly", true));
    }

    return hibernateTemplate.findByCriteria(criteria);
  }

  @Required
  public void setMailer(TemplateMailer mailer) {
    this.mailer = mailer;
  }
}