package org.ambraproject.search;

import org.ambraproject.views.SavedSearchView;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.ambraproject.email.TemplateMailer;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Send saved searches
 */
public class SavedSearchSenderImpl implements SavedSearchSender {
  private static final Logger log = LoggerFactory.getLogger(SavedSearchSenderImpl.class);

  private TemplateMailer mailer;

  /**
   * @inheritDoc
   */
  public void sendSavedSearch(SavedSearchView savedSearchView) {
  log.info("Received thread Name: {}", Thread.currentThread().getName());
  log.info("Starting send request for: {}", savedSearchView.getSearchName());

  final Map<String, Object> context = new HashMap<String, Object>();

  Date startTime;
  Date endTime = Calendar.getInstance().getTime();

  if(savedSearchView.getMonthly()) {
    //30 days into the past
    Calendar date = Calendar.getInstance();
    date.add(Calendar.DAY_OF_MONTH, -30);
    startTime = date.getTime();
  } else {
    //7 days into the past
    Calendar date = Calendar.getInstance();
    date.add(Calendar.DAY_OF_MONTH, -7);
    startTime = date.getTime();
  }

  context.put("searchHitList", null);
  context.put("startTime", startTime);
  context.put("endTime", endTime);

  //TODO: Move to config
  context.put("imagePath", "/bleh.gif");

  //Create message

  Multipart content = createContent(context);

//  List<String> emails = journalService.getJournalAlertSubscribers(alert.getAlertID());
//
//  //TODO: provide override for dev mode and allow QA to adjust in ambra.xml
//  String toAddresses = StringUtils.join(emails, " ");
//
//  //TODO: move to config?
//  String fromAddress = "admin@plos.org";
//
//  mailer.mail(toAddresses, fromAddress, alert.getEmailSubject(), context, content);
//
//  log.info("Completed thread Name: {}", Thread.currentThread().getName());
//  log.info("Completed send request for: {}", alert.getAlertKey());
  }

  private Multipart createContent(Map<String, Object> context) {
    try {
      //TODO: Move filenames to configuration
      return mailer.createContent("etoc-text.ftl", "etoc-html.ftl", context);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    } catch(MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Required
  public void setMailer(TemplateMailer mailer) {
    this.mailer = mailer;
  }
}