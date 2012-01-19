/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.ambra.search2;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Manage all of the parameters for a search.
 * <p/>
 * In every method that sets a String, a NULL is replaced with a String of zero length and all
 * input in trimmed.
 * <p/>
 * In every method that sets a String Array, a NULL is replaced with an array with zero elements
 * and every element is trimmed.  Elements that are trimmed to zero length are removed from the array.
 * <p/>
 * Dates are stored as Date objects, whether they are set as Strings or Dates.  Empty date Strings
 * create NULL Date objects.
 *
 * TODO: The "query" parameter is used only by Simple Search, so replace all "query" references with "textSearchAll" in the UI, then remove "query" parameter from this class.
 *
 * @author Scott Sterling
 */
public class SearchParameters implements Serializable {

  private static final long serialVersionUID = -7837640277704487921L;
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private String        query = "";
  private String        textSearchAll = "";
  private String        textSearchExactPhrase = "";
  private String        textSearchAtLeastOne = "";
  private String        textSearchWithout = "";
  private String        textSearchOption = "";
  private String[]      creator = {};
  private String        authorNameOp = "";
  private String        subjectCatOpt = "";
  private String        dateTypeSelect = "";
  private Date          startDate = null;
  private Date          endDate = null;
  private String[]      limitToCategory = {};
  private String        journalOpt = "";  //  Whether to limit a search to Journals in limitToJournal
  // Only search these Journals. If no journals were selected, default to the current journal.
  private String[]      limitToJournal = {};

  //  Formatting elements
  private int           startPage = 0;
  private int           pageSize = 0;

  // TODO: The "query" parameter is used only by Simple Search, so replace all "query" references with "textSearchAll" in the UI, then remove from this class.
  public String getQuery() {
    return query;
  }
  // TODO: The "query" parameter is used only by Simple Search, so replace all "query" references with "textSearchAll" in the UI, then remove from this class.
  public void setQuery(String query) {
    if (query == null || query.trim().length() < 1)
      this.query = "";
    else
      this.query = query.trim();
  }

  public String[] getCreator() {
    return creator;
  }
  public void setCreator(String[] creator) {
    if (creator == null || creator.length < 1) {
      this.creator = new String[]{};
    } else {
      List<String> creatorList = new LinkedList<String>();
      for (String author : creator) {
        if (author != null && author.trim().length() > 0)
          creatorList.add(author.trim());
      }
      this.creator = new String[creatorList.size()];
      int counter = 0;
      for (String author : creatorList) {
        this.creator[counter++] = author;
      }
    }
  }

  public String getAuthorNameOp() {
    return authorNameOp;
  }
  public void setAuthorNameOp(String authorNameOp) {
    if (authorNameOp == null || authorNameOp.trim().length() < 1)
      this.authorNameOp = "";
    else
      this.authorNameOp = authorNameOp.trim();
  }

  public String getTextSearchAll() {
    return textSearchAll;
  }
  public void setTextSearchAll(String textSearchAll) {
    if (textSearchAll == null || textSearchAll.trim().length() < 1)
      this.textSearchAll = "";
    else
      this.textSearchAll = textSearchAll.trim();
  }

  public String getTextSearchExactPhrase() {
    return textSearchExactPhrase;
  }
  public void setTextSearchExactPhrase(String textSearchExactPhrase) {
    if (textSearchExactPhrase == null || textSearchExactPhrase.trim().length() < 1)
      this.textSearchExactPhrase = "";
    else
      this.textSearchExactPhrase = textSearchExactPhrase.trim();
  }

  public String getTextSearchAtLeastOne() {
    return textSearchAtLeastOne;
  }
  public void setTextSearchAtLeastOne(String textSearchAtLeastOne) {
    if (textSearchAtLeastOne == null || textSearchAtLeastOne.trim().length() < 1)
      this.textSearchAtLeastOne = "";
    else
      this.textSearchAtLeastOne = textSearchAtLeastOne.trim();
  }

  public String getTextSearchWithout() {
    return textSearchWithout;
  }
  public void setTextSearchWithout(String textSearchWithout) {
    if (textSearchWithout == null || textSearchWithout.trim().length() < 1)
      this.textSearchWithout = "";
    else
      this.textSearchWithout = textSearchWithout.trim();
  }

  public String getTextSearchOption() {
    return textSearchOption;
  }
  public void setTextSearchOption(String textSearchOption) {
    if (textSearchOption == null || textSearchOption.trim().length() < 1)
      this.textSearchOption = "";
    else
      this.textSearchOption = textSearchOption.trim();
  }

  public String getSubjectCatOpt() {
    return subjectCatOpt;
  }
  public void setSubjectCatOpt(String subjectCatOpt) {
    if (subjectCatOpt == null || subjectCatOpt.trim().length() < 1)
      this.subjectCatOpt = "";
    else
      this.subjectCatOpt = subjectCatOpt.trim();
  }

  public String getDateTypeSelect() {
    return dateTypeSelect;
  }
  public void setDateTypeSelect(String dateTypeSelect) {
    if (dateTypeSelect == null || dateTypeSelect.trim().length() < 1)
      this.dateTypeSelect = "";
    else
      this.dateTypeSelect = dateTypeSelect.trim();
  }

  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  /**
   * @return The startDate formatted as yyyy-MM-dd or a zero-length String if startDate is null
   */
  public String getStartDateAsString() {
    if (startDate == null)
      return "";
    else
      return dateFormat.format(startDate);
  }
  /**
   * Set the startDate to the Date indicated or to null if startDateAsString is null or blank
   *
   * @param startDateAsString The startDate formatted as yyyy-MM-dd
   * @throws ParseException Thrown if the startDateAsString is not formatted as yyyy-MM-dd
   */
  public void setStartDateAsString(String startDateAsString) throws ParseException {
    if (startDateAsString == null || startDateAsString.trim().length() < 1)
      this.startDate = null;
    else
      this.startDate = dateFormat.parse(startDateAsString.trim());
  }

  public Date getEndDate() {
    return endDate;
  }
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
  /**
   * @return The endDate formatted as yyyy-MM-dd or a zero-length String if endDate is null
   */
  public String getEndDateAsString() {
    if (endDate == null)
      return "";
    else
      return dateFormat.format(endDate);
  }
  /**
   * Set the endDate to the Date indicated or to null if endDateAsString is null or blank
   *
   * @param endDateAsString The endDate formatted as yyyy-MM-dd
   * @throws ParseException Thrown if the endDateAsString is not formatted as yyyy-MM-dd
   */
  public void setEndDateAsString(String endDateAsString) throws ParseException {
    if (endDateAsString == null || endDateAsString.trim().length() < 1)
      this.endDate = null;
    else
      this.endDate = dateFormat.parse(endDateAsString.trim());
  }

  /**
   * True if the startDate and endDate define a valid date range which can be used for searches
   * @return true if the startDate and endDate define a valid date range which can be used for searches
   */
  public boolean isDateRangeValid() {
    if (startDate == null || endDate == null || endDate.before(startDate))
      return false;
    else
      return true;
  }

  public String[] getLimitToCategory() {
    return limitToCategory;
  }
  public void setLimitToCategory(String[] limitToCategory) {
    if (limitToCategory == null || limitToCategory.length < 1) {
      this.limitToCategory = new String[]{};
    } else {
      List<String> limitToCategoryList = new LinkedList<String>();
      for (String category : limitToCategory) {
        if (category != null && category.trim().length() > 0)
          limitToCategoryList.add(category.trim());
      }
      this.limitToCategory = new String[limitToCategoryList.size()];
      int counter = 0;
      for (String category : limitToCategoryList) {
        this.limitToCategory[counter++] = category;
      }
    }
  }

  public String getJournalOpt() {
    return journalOpt;
  }
  public void setJournalOpt(String journalOpt) {
    if (journalOpt == null || journalOpt.trim().length() < 1)
      this.journalOpt = "";
    else
      this.journalOpt = journalOpt.trim();
  }

  public String[] getLimitToJournal() {
    return limitToJournal;
  }
  public void setLimitToJournal(String[] limitToJournal) {
    if (limitToJournal == null || limitToJournal.length < 1) {
      this.limitToJournal = new String[]{};
    } else {
      List<String> limitToJournalList = new LinkedList<String>();
      for (String journal : limitToJournal) {
        if (journal != null && journal.trim().length() > 0)
          limitToJournalList.add(journal.trim());
      }
      this.limitToJournal = new String[limitToJournalList.size()];
      int counter = 0;
      for (String journal : limitToJournalList) {
        this.limitToJournal[counter++] = journal;
      }
    }
  }

  public int getStartPage() {
    return startPage;
  }
  public void setStartPage(int startPage) {
    this.startPage = startPage;
  }

  public int getPageSize() {
    return pageSize;
  }
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Creates a deep copy of this SearchParameters object.
   *
   * @return a deep copy of this SearchParameters object.
   */
  public SearchParameters copy() {
    SearchParameters sp = new SearchParameters();
    sp.setQuery(this.getQuery());
    sp.setTextSearchAll(this.getTextSearchAll());
    sp.setTextSearchAtLeastOne(this.getTextSearchAtLeastOne());
    sp.setTextSearchExactPhrase(this.getTextSearchExactPhrase());
    sp.setTextSearchWithout(this.getTextSearchWithout());
    sp.setTextSearchOption(this.getTextSearchOption());
    sp.setCreator(this.getCreator().clone());
    sp.setAuthorNameOp(this.getAuthorNameOp());
    sp.setSubjectCatOpt(this.getSubjectCatOpt());
    sp.setDateTypeSelect(this.getDateTypeSelect());
    if (this.getStartDate() == null)
      sp.setStartDate(null);
    else
      sp.setStartDate((Date)this.getStartDate().clone());
    if (this.getEndDate() == null)
      sp.setEndDate(null);
    else
      sp.setEndDate((Date)this.getEndDate().clone());
    sp.setLimitToCategory(this.getLimitToCategory().clone());
    sp.setJournalOpt(this.getJournalOpt());
    sp.setLimitToJournal(this.getLimitToJournal().clone());
    sp.setStartPage(this.getStartPage());
    sp.setPageSize(this.getPageSize());
    return sp;
  }

  /**
   * Check if there are enough of the right kinds of terms to perform a search.
   * Returns <code>true</code> if there are non-zero-length Strings in any of the creator,
   * textSearchAll, textSearchAtLeastOne, textSearchExactPhrase, or textSearchWithout parameters.
   * 
   * @return true if a search can be performed on the submitted terms
   */
  public boolean isContainsSearchTerms() {
    if (getCreator().length > 0
        || getTextSearchAll().length() > 0 || getTextSearchAtLeastOne().length() > 0
        || getTextSearchExactPhrase().length() > 0 || getTextSearchWithout().length() > 0
        || ( getStartDate() != null && getEndDate() != null && ! getStartDate().equals(getEndDate()) ) )
      return true;
    return false;
  }

  @Override
  public String toString() {
    return "SearchParameters{" +
        "query='" + query + '\'' +
        ", textSearchAll='" + textSearchAll + '\'' +
        ", textSearchExactPhrase='" + textSearchExactPhrase + '\'' +
        ", textSearchAtLeastOne='" + textSearchAtLeastOne + '\'' +
        ", textSearchWithout='" + textSearchWithout + '\'' +
        ", textSearchOption='" + textSearchOption + '\'' +
        ", creator=" + (creator == null ? null : Arrays.asList(creator)) +
        ", authorNameOp='" + authorNameOp + '\'' +
        ", subjectCatOpt='" + subjectCatOpt + '\'' +
        ", dateTypeSelect='" + dateTypeSelect + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", limitToCategory=" + (limitToCategory == null ? null : Arrays.asList(limitToCategory)) +
        ", journalOpt='" + journalOpt + '\'' +
        ", limitToJournal=" + (limitToJournal == null ? null : Arrays.asList(limitToJournal)) +
        ", startPage=" + startPage +
        ", pageSize=" + pageSize +
        '}';
  }
}
