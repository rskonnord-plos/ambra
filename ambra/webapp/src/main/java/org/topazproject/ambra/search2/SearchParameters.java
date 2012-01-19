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
import java.util.Arrays;

/**
 * Store all of the parameters for a search.
 *
 * @author Scott Sterling
 */
public class SearchParameters implements Serializable {

  private static final long serialVersionUID = -7837640277704487921L;

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
  private String        startDate = "";
  private String        endDate = "";
  private String[]      limitToCategory = {};
  private String        journalOpt = "";  //  Whether to limit a search to Journals in limitToJournal
  // Only search these Journals. If no journals were selected, default to the current journal.
  private String[]      limitToJournal = {};

  //  Formatting elements
  private int           startPage = 0;
  private int           pageSize = 0;

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getTextSearchAll() {
    return textSearchAll;
  }

  public void setTextSearchAll(String textSearchAll) {
    this.textSearchAll = textSearchAll;
  }

  public String getTextSearchExactPhrase() {
    return textSearchExactPhrase;
  }

  public void setTextSearchExactPhrase(String textSearchExactPhrase) {
    this.textSearchExactPhrase = textSearchExactPhrase;
  }

  public String getTextSearchAtLeastOne() {
    return textSearchAtLeastOne;
  }

  public void setTextSearchAtLeastOne(String textSearchAtLeastOne) {
    this.textSearchAtLeastOne = textSearchAtLeastOne;
  }

  public String getTextSearchWithout() {
    return textSearchWithout;
  }

  public void setTextSearchWithout(String textSearchWithout) {
    this.textSearchWithout = textSearchWithout;
  }

  public String getTextSearchOption() {
    return textSearchOption;
  }

  public void setTextSearchOption(String textSearchOption) {
    this.textSearchOption = textSearchOption;
  }

  public String[] getCreator() {
    return creator;
  }

  public void setCreator(String[] creator) {
    this.creator = creator;
  }

  public String getAuthorNameOp() {
    return authorNameOp;
  }

  public void setAuthorNameOp(String authorNameOp) {
    this.authorNameOp = authorNameOp;
  }

  public String getSubjectCatOpt() {
    return subjectCatOpt;
  }

  public void setSubjectCatOpt(String subjectCatOpt) {
    this.subjectCatOpt = subjectCatOpt;
  }

  public String getDateTypeSelect() {
    return dateTypeSelect;
  }

  public void setDateTypeSelect(String dateTypeSelect) {
    this.dateTypeSelect = dateTypeSelect;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String[] getLimitToCategory() {
    return limitToCategory;
  }

  public void setLimitToCategory(String[] limitToCategory) {
    this.limitToCategory = limitToCategory;
  }

  public String getJournalOpt() {
    return journalOpt;
  }

  public void setJournalOpt(String journalOpt) {
    this.journalOpt = journalOpt;
  }

  public String[] getLimitToJournal() {
    return limitToJournal;
  }

  public void setLimitToJournal(String[] limitToJournal) {
    this.limitToJournal = limitToJournal;
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
        ", startDate='" + startDate + '\'' +
        ", endDate='" + endDate + '\'' +
        ", limitToCategory=" + (limitToCategory == null ? null : Arrays.asList(limitToCategory)) +
        ", journalOpt='" + journalOpt + '\'' +
        ", limitToJournal=" + (limitToJournal == null ? null : Arrays.asList(limitToJournal)) +
        ", startPage=" + startPage +
        ", pageSize=" + pageSize +
        '}';
  }
}
