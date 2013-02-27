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

import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.views.SavedSearchView;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: stumu Date: 9/26/12 Time: 1:59 PM To change this template use File | Settings |
 * File Templates.
 */
public class SavedSearchRetrieverImpl extends HibernateServiceImpl implements SavedSearchRetriever {
   private static final Logger log = LoggerFactory.getLogger(SavedSearchRetrieverImpl.class);

  /**
   * Take a type of saved searches, and retrieve a map from email to search string
   *
   * @param alertType
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<SavedSearchView> retrieveSearchAlerts(AlertType alertType) {

    List<Object[]> searchObjList = null;
    List<SavedSearchView> savedSearchViews = new ArrayList<SavedSearchView>();

    searchObjList = (List<Object[]>)hibernateTemplate.findByCriteria(DetachedCriteria.forClass(UserProfile.class)
                    .createAlias("savedSearches", "s")
                    .add(alertType.getTypeCriterion())
                    .setFetchMode("savedSearches", FetchMode.JOIN)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .setProjection(Projections.projectionList()
                        .add(Projections.property("s.ID"))
                        .add(Projections.property("s.searchName"))
                        .add(Projections.property("s.searchParams"))
                        .add(alertType.getTypeProjection())       //lastWeeklySearchTime (or) lastMonthlySearchTime
                        .add(Projections.property("email")))
      );

    if(searchObjList != null && !searchObjList.isEmpty()){

      for(Object[] obj:searchObjList ){

        savedSearchViews.add(new SavedSearchView((Long)obj[0],(String)obj[1],(String)obj[2],(Date)obj[3],(String)obj[4])) ;
        log.debug("Updating Last "+ alertType.name() +" Saved Search Time for Saved Search ID: {}", (Long)obj[0]);

        SavedSearch savedSearch = hibernateTemplate.get(SavedSearch.class, (Long)obj[0]);
          if (savedSearch != null) {
            if(alertType.name().equals("WEEKLY")){
              savedSearch.setLastWeeklySearchTime(Calendar.getInstance().getTime());
            }else{
              savedSearch.setLastMonthlySearchTime(Calendar.getInstance().getTime());
            }
            hibernateTemplate.update(savedSearch);
          }
        log.debug("Updated Last "+ alertType.name() +" Saved Search Time for Saved Search ID: {}", (Long)obj[0]);
      }
    }
    return savedSearchViews;
  }
}