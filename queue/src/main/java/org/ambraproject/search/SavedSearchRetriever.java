/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.search;

import org.ambraproject.views.SavedSearchView;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.PropertyProjection;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: stumu Date: 9/26/12 Time: 12:19 PM To change this template use File | Settings |
 * File Templates.
 */
public interface SavedSearchRetriever {

  enum AlertType {
    WEEKLY(Restrictions.eq("s.weekly", true),
      Projections.property("s.lastWeeklySearchTime")),
    MONTHLY(Restrictions.eq("s.monthly", true),
      Projections.property("s.lastMonthlySearchTime"));

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
   * Take a type of saved searches, and retrieve a map from email to search string
   * @param alertType
   * @return
   */
   public List<SavedSearchView> retrieveSearchAlerts(final AlertType alertType);


}
