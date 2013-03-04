package org.ambraproject.search;

import com.google.gson.Gson;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.service.search.SearchParameters;
import org.ambraproject.views.SavedSearchHit;

import java.util.Date;
import java.util.List;

/**
 * POJO for passing around search results within queue routes
 *
 * @author Joe Osowski
 */
public class SavedSearchJob {
  private Long savedSearchQueryID;
  private String searchString;
  private String hash;
  private String frequency;
  private SavedSearchType type;
  private Date startDate;
  private Date endDate;
  private List<SavedSearchHit> searchHitList;

  public SavedSearchJob(Long savedSearchQueryID, String searchString, String hash, SavedSearchType type, String frequency) {
    this.savedSearchQueryID = savedSearchQueryID;
    this.searchString = searchString;
    this.hash = hash;
    this.type = type;
    this.frequency = frequency;
  }

  public SavedSearchJob(Long savedSearchQueryID, String searchString, String hash, SavedSearchType type, String frequency, Date startDate, Date endDate, List<SavedSearchHit> searchHitList) {
    this.savedSearchQueryID = savedSearchQueryID;
    this.searchString = searchString;
    this.hash = hash;
    this.type = type;
    this.frequency = frequency;
    this.startDate = startDate;
    this.endDate = endDate;
    this.searchHitList = searchHitList;
  }

  public Long getSavedSearchQueryID() {
    return savedSearchQueryID;
  }

  public void setSavedSearchQueryID(Long savedSearchQueryID) {
    this.savedSearchQueryID = savedSearchQueryID;
  }

  public String getSearchString() {
    return searchString;
  }

  public SearchParameters getSearchParams() {
    Gson gson = new Gson();
    return gson.fromJson(this.searchString, SearchParameters.class);
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public SavedSearchType getType() {
    return type;
  }

  public void setType(SavedSearchType type) {
    this.type = type;
  }

  public String getFrequency() {
    return frequency;
  }

  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public List<SavedSearchHit> getSearchHitList() {
    return searchHitList;
  }

  public void setSearchHitList(List<SavedSearchHit> searchHitList) {
    this.searchHitList = searchHitList;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(SavedSearchJob job) {
    return new Builder(job);
  }

  public static class Builder {
    private Long savedSearchQueryID;
    private String searchString;
    private String hash;
    private SavedSearchType type;
    private String frequency;
    private Date startDate;
    private Date endDate;
    private List<SavedSearchHit> searchHitList;

    private Builder() {
      super();
    }

    private Builder(SavedSearchJob job) {
      super();

      this.savedSearchQueryID = job.savedSearchQueryID;
      this.searchString = job.getSearchString();
      this.hash = job.getHash();
      this.type = job.getType();
      this.frequency = job.getFrequency();
      this.searchHitList = job.getSearchHitList();
    }

    public Builder setSavedSearchQueryID(Long savedSearchQueryID) {
      this.savedSearchQueryID = savedSearchQueryID;
      return this;
    }

    public Builder setSearchString(String searchParams) {
      this.searchString = searchParams;
      return this;
    }

    public Builder setHash(String hash) {
      this.hash = hash;
      return this;
    }

    public Builder setType(SavedSearchType type) {
      this.type = type;
      return this;
    }

    public Builder setFrequency(String frequency) {
      this.frequency = frequency;
      return this;
    }

    public void setStartDate(Date startDate) {
      this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
      this.endDate = endDate;
    }

    public Builder setSearchHitList(List<SavedSearchHit> searchHitList) {
      this.searchHitList = searchHitList;
      return this;
    }

    public SavedSearchJob build() {
      return new SavedSearchJob(
        this.savedSearchQueryID,
        this.searchString,
        this.hash,
        this.type,
        this.frequency,
        this.startDate,
        this.endDate,
        this.searchHitList);
    }
  }
}
