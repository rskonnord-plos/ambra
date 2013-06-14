/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.search;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.PropertyProjection;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

/**
 * @author stumu
 * @author Joe Osowski
 */
public interface SavedSearchRetriever {

  enum AlertType {
    WEEKLY(Restrictions.eq("weekly", true),
      Projections.property("lastWeeklySearchTime")),
    MONTHLY(Restrictions.eq("monthly", true),
      Projections.property("lastMonthlySearchTime"));

    private Criterion typeCriterion;
    private PropertyProjection typeProjection;

    private AlertType(Criterion typeCriterion,PropertyProjection projections){
      this.typeCriterion = typeCriterion;
      this.typeProjection = projections;
    }

    Criterion getTypeCriterion(){
      return typeCriterion;
    }

    PropertyProjection getTypeProjection(){
      return typeProjection;
    }
  };

  /**
   * Retrieve a list of unique searches to perform based on the passed in type
   *
   * @param alertType the alertType
   * @param startTime the time to use as the start date.  Can be null, but if specified will override
   * @param endTime the time to use as the end date.  Can be null, but if specified will override
   * @return a list of uniqueSearches
   */
   public List<SavedSearchJob> retrieveSearchAlerts(final AlertType alertType, Date startTime, Date endTime);
}
