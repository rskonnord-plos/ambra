package org.ambraproject.search;

import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
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

  private static final String WEEKLY_FREQUENCY = "WEEKLY";
  private static final String PRODUCTION_MODE = "PRODUCTION";
  private static final String QA_MODE = "QA";

  private TemplateMailer mailer;
  private String mailFromAddress;
  private String sendMode;
  private String sendModeQAEMail;
  private String alertHtmlEmail;
  private String alertTextEmail;
  private String savedSearchHtmlEmail;
  private String savedSearchTextEmail;
  private String imagePath;

  /**
   * @inheritDoc
   */
  public void sendSavedSearch(SavedSearchJob searchJob) {

    log.debug("Received thread Name: {}", Thread.currentThread().getName());
    log.debug("Send emails for search ID: {}. {}", searchJob.getSavedSearchQueryID(), searchJob.getFrequency());

    final Map<String, Object> context = new HashMap<String, Object>();

    context.put("searchParameters", searchJob.getSearchParams());
    context.put("searchHitList", searchJob.getSearchHitList());
    context.put("startTime", searchJob.getStartDate());
    context.put("endTime", searchJob.getEndDate());
    context.put("imagePath", this.imagePath);

    //Create message
    Multipart content = createContent(context, searchJob.getType());

    List<Object[]> searchDetails = getSavedSearchDetails(searchJob.getSavedSearchQueryID(), searchJob.getFrequency());

    String fromAddress = this.mailFromAddress;

    for(int a = 0; a < searchDetails.size(); a++) {
      String toAddress = (String)searchDetails.get(a)[1];
      String subject;

      //TODO: Move subjects to config?
      if(searchJob.getType().equals(SavedSearchType.USER_DEFINED)) {
        subject = "PLOS Search Alert - " + searchDetails.get(a)[2];

        log.debug("Job result count: {}", searchJob.getSearchHitList().size());

        //We might filter the search hitlist based on publish and the last time the search was run for each user 
        //here.  We track the last time a search was run in the user's savedSearch table, seemed like overkill to
        //to me though.  

        if(searchJob.getSearchHitList().size() > 0) {

          log.debug("Sending mail: {}", toAddress);

          mail(toAddress, fromAddress, subject, context, content);
        } else {
          log.debug("Not sending mail: {}", toAddress);
        }
      } else {
        subject = "PLOS Journal Alert";

        log.debug("Job Result count: {}", searchJob.getSearchHitList().size());
        log.debug("Sending mail: {}", toAddress);

        mail(toAddress, fromAddress, subject, context, content);
      }

      //When results are sent update the records to indicate
      markSearchRun((Long)searchDetails.get(a)[0], searchJob.getFrequency(), searchJob.getEndDate());
    }

    log.debug("Completed thread Name: {}", Thread.currentThread().getName());
    log.debug("Completed send request for search ID: {}. {}", searchJob.getSavedSearchQueryID(), searchJob.getFrequency());
  }

  private void mail(String toAddress, String fromAddress, String subject, Map<String, Object> context,
    Multipart content) {

    //If sendMode empty, do nothing
    if(sendMode != null) {
      if(sendMode.toUpperCase().equals(PRODUCTION_MODE)) {
        mailer.mail(toAddress, fromAddress, subject, context, content);
        log.debug("Mail sent, mode: {}, address: {}", new Object[] { PRODUCTION_MODE, toAddress});
      }

      if(sendMode.toUpperCase().equals(QA_MODE)) {
        mailer.mail(sendModeQAEMail, fromAddress, "(" + toAddress + ")" + subject, context, content);
        log.debug("Mail sent, mode: {}, address: {}", new Object[] { QA_MODE, sendModeQAEMail});
      }

      //If sendMode does not match "production" or "QA", do nothing
    }
  }

  @SuppressWarnings("unchecked")
  private void markSearchRun(Long savedSearchID, String frequency, Date endDate)
  {
    SavedSearch savedSearch = hibernateTemplate.get(SavedSearch.class, savedSearchID);

    if(savedSearch == null) {
      throw new RuntimeException("Could not find savedSearch: " + savedSearchID);
    }

    if(frequency.equals(WEEKLY_FREQUENCY)) {
      savedSearch.setLastWeeklySearchTime(endDate);
    } else {
      savedSearch.setLastMonthlySearchTime(endDate);
    }

    hibernateTemplate.update(savedSearch);

    log.debug("Updated Last {} saved Search time for Saved Search ID: {}", frequency, savedSearchID);
  }

  private Multipart createContent(Map<String, Object> context, SavedSearchType type) {
    try {
      if(type.equals(SavedSearchType.JOURNAL_ALERT)) {
        return mailer.createContent(this.alertTextEmail, this.alertHtmlEmail, context);
      } else {
        return mailer.createContent(this.savedSearchTextEmail, this.savedSearchHtmlEmail, context);
      }
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

  @Required
  public void setMailFromAddress(String mailFromAddress) {
    this.mailFromAddress = mailFromAddress;
  }

  @Required
  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  @Required
  public void setAlertHtmlEmail(String alertHtmlEmail) {
    this.alertHtmlEmail = alertHtmlEmail;
  }

  @Required
  public void setAlertTextEmail(String alertTextEmail) {
    this.alertTextEmail = alertTextEmail;
  }

  @Required
  public void setSavedSearchHtmlEmail(String savedSearchHtmlEmail) {
    this.savedSearchHtmlEmail = savedSearchHtmlEmail;
  }

  @Required
  public void setSavedSearchTextEmail(String savedSearchTextEmail) {
    this.savedSearchTextEmail = savedSearchTextEmail;
  }

  @Required
  public void setSendMode(String sendMode) {
    this.sendMode = sendMode;
  }

  @Required
  public void setSendModeQAEMail(String sendModeQAEMail) {
    this.sendModeQAEMail = sendModeQAEMail;
  }
}
