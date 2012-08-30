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

package org.ambraproject.testutils;

import org.ambraproject.service.mailer.AmbraMailerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An inert mailer for testing, which caches all email that would have been sent through it if it were real.
 *
 * @author Ryan Skonnord
 */
public class DummyAmbraMailer extends AmbraMailerImpl {

  private final List<DummyEmail> emailsSent;

  public DummyAmbraMailer() {
    super();
    emailsSent = new ArrayList<DummyEmail>();

    setEmailThisArticleMap(Collections.<String, String>emptyMap());
    setFeedbackEmailMap(Collections.<String, String>emptyMap());
    setAutoIngestEmailMap(Collections.<String, String>emptyMap());
    setErrorEmailMap(Collections.<String, String>emptyMap());
  }


  /**
   * A container for the full set of email metadata received by the mailer superclass.
   */
  public static class DummyEmail {
    private final String toEmailAddresses;
    private final String fromEmailAddress;
    private final String subject;
    private final Map<String, Object> context;
    private final String textTemplateFilename;
    private final String htmlTemplateFilename;

    /**
     * @see org.topazproject.ambra.email.impl.FreemarkerTemplateMailer#mail
     */
    public DummyEmail(String toEmailAddresses, String fromEmailAddress, String subject, Map<String, Object> context, String textTemplateFilename, String htmlTemplateFilename) {
      this.toEmailAddresses = toEmailAddresses;
      this.fromEmailAddress = fromEmailAddress;
      this.subject = subject;
      this.context = Collections.unmodifiableMap(context);
      this.textTemplateFilename = textTemplateFilename;
      this.htmlTemplateFilename = htmlTemplateFilename;
    }

    public String getToEmailAddresses() {
      return toEmailAddresses;
    }

    public String getFromEmailAddress() {
      return fromEmailAddress;
    }

    public String getSubject() {
      return subject;
    }

    public Map<String, Object> getContext() {
      return context;
    }

    public String getTextTemplateFilename() {
      return textTemplateFilename;
    }

    public String getHtmlTemplateFilename() {
      return htmlTemplateFilename;
    }
  }


  /**
   * Get the cache of emails sent to this mailer, listed in the order they were sent.
   *
   * @return an unmodifiable list view of the emails
   */
  public List<DummyEmail> getEmailsSent() {
    return Collections.unmodifiableList(emailsSent);
  }

  /**
   * Clear the cache of emails sent to this mailer.
   */
  public void clear() {
    emailsSent.clear();
  }

  @Override
  public void mail(String toEmailAddresses, String fromEmailAddress, String subject, Map<String, Object> context, String textTemplateFilename, String htmlTemplateFilename) {
    DummyEmail m = new DummyEmail(toEmailAddresses, fromEmailAddress, subject, context, textTemplateFilename, htmlTemplateFilename);
    emailsSent.add(m);
  }

}
