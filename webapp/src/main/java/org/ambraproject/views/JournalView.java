/*
 * $HeadURL: http://ambraproject.org/svn/ambra/ambra/branches/figure-improvements/webapp/src/main/java/org/ambraproject/views/AuthorView.java $
 * $Id: AuthorView.java 10738 2012-04-04 21:14:09Z akudlick $
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.views;

import org.ambraproject.models.Journal;

/**
 * Immutable view wrapper around a Journal
 * @author Joe Osowski
 */
public class JournalView {
  private final String title;
  private final String eIssn;
  private final String journalKey;

  public JournalView(Journal journal) {
    this.title = journal.getTitle();
    this.eIssn = journal.geteIssn();
    this.journalKey = journal.getJournalKey();
  }

  public String getTitle() {
    return title;
  }

  public String geteIssn() {
    return eIssn;
  }

  public String getJournalKey() {
    return journalKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JournalView that = (JournalView) o;

    if (eIssn != null ? !eIssn.equals(that.eIssn) : that.eIssn != null) return false;
    if (title != null ? !title.equals(that.title) : that.title != null) return false;
    if (journalKey != null ? !journalKey.equals(that.journalKey) : that.journalKey != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + (eIssn != null ? eIssn.hashCode() : 0);
    result = 31 * result + (journalKey != null ? journalKey.hashCode() : 0);
    return result;
  }
}
