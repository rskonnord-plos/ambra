package org.ambraproject.views;

import org.ambraproject.models.SavedSearchQuery;

/**
 * Immutable view wrapper around a SavedSearchQuery
 *
 * Each search has a serialized string of parameters, this
 * class is used in collections representing the unique set of searches
 * to avoid executing a search more then once
 *
 * @author Joe Osowski
 */
public class SavedSearchQueryView {
  private final Long ID;
  private final String searchParams;
  private final String hash;

  public SavedSearchQueryView(SavedSearchQuery params) {
    this.ID = params.getID();
    this.searchParams = params.getSearchParams();
    this.hash = params.getHash();
  }

  public SavedSearchQueryView(Long ID, String searchParams, String hash) {
    this.ID = ID;
    this.searchParams = searchParams;
    this.hash = hash;
  }

  public String getSearchParams() {
    return searchParams;
  }

  public String getHash() {
    return hash;
  }

  public Long getID() {
    return ID;
  }
}
