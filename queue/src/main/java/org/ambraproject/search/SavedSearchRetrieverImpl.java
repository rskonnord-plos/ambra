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
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @inheritDoc
 */
public class SavedSearchRetrieverImpl extends HibernateServiceImpl implements SavedSearchRetriever {
   private static final Logger log = LoggerFactory.getLogger(SavedSearchRetrieverImpl.class);

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<SavedSearchJob> retrieveSearchAlerts(AlertType alertType) {
    List<SavedSearchJob> searchJobs = new ArrayList<SavedSearchJob>();
    List<Object[]> paramsList = (List<Object[]>)hibernateTemplate.findByCriteria(DetachedCriteria.forClass(SavedSearch.class)
                    .createAlias("searchQuery", "s")
                    .add(alertType.getTypeCriterion())
                    .setFetchMode("searchQuery", FetchMode.JOIN)
                    .setProjection(Projections.distinct(Projections.projectionList()
                      .add(Projections.property("s.ID"))
                      .add(Projections.property("s.hash"))
                      .add(Projections.property("s.searchParams")))));

    for(Object[] obj : paramsList) {
      searchJobs.add(SavedSearchJob.builder()
          .setSavedSearchQueryID((Long)obj[0])
          .setHash((String)obj[1])
          .setSearchString((String)obj[2])
          .setType(alertType.name())
         .build());
    }

    log.debug("Returning {} saved search(es)", searchJobs.size());

    return searchJobs;
  }
}