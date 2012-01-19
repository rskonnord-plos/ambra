/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.ambraproject.hibernate;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metadata.ClassMetadata;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.*;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Id generator to mimic the id generation from topaz.  Assigns a randomly generated URI (or URI-like String, as
 * appropriate), with the appropriate URI prefix per class.  Id generation is not supported for the following classes:
 *<ul>
 *   <li>Article</li>
 *   <li>DublinCore</li>
 *   <li>ObjectInfo</li>
 *</ul>
 * These classes must have Ids assigned before saving.
 *
 *
 * @author Alex Kudlick Date: 5/2/11
 * @author Joe Osowski  Date: 6/8/11
 *
 *         <p/>
 *         org.topazproject.ambra.orm
 */
public class AmbraIdGenerator implements IdentifierGenerator {
  /**
   * Set holding the classes that have String ids instead of URI ids
   */
  private static final Set<Class> entitiesWithStringId = new HashSet<Class>();

  private String prefix = null;

  /**
   * Map containing the URI prefix for each class
   */
  private static final Map<Class, String> uriPrefixes = new HashMap<Class, String>();

  public AmbraIdGenerator()
  {
    this.prefix = System.getProperty(ConfigurationStore.SYSTEM_OBJECT_ID_PREFIX);

    if(this.prefix == null) {
      throw new RuntimeException(ConfigurationStore.SYSTEM_OBJECT_ID_PREFIX + " is not defined as a system property.");
    }
  }

  /**
   * Return the current prefix used on all generated IDs
   * @return
   */
  public String getPrefix()
  {
    return prefix;
  }

  static {
    entitiesWithStringId.add(AnnotationBlob.class);
    entitiesWithStringId.add(DublinCore.class);
    entitiesWithStringId.add(RatingContent.class);
    entitiesWithStringId.add(RatingSummaryContent.class);
    entitiesWithStringId.add(ReplyBlob.class);
    entitiesWithStringId.add(TrackbackContent.class);
    entitiesWithStringId.add(Representation.class);
    entitiesWithStringId.add(TextRepresentation.class);

    //Annotations
    uriPrefixes.put(Comment.class, "annotation");
    uriPrefixes.put(Rating.class, "annotation");
    uriPrefixes.put(MinorCorrection.class, "annotation");
    uriPrefixes.put(RatingSummary.class, "annotation");
    uriPrefixes.put(FormalCorrection.class, "annotation");
    uriPrefixes.put(Trackback.class, "annotation");
    uriPrefixes.put(Representation.class, "representation");
    uriPrefixes.put(TextRepresentation.class, "representation");
    uriPrefixes.put(RatingContent.class, "ratingContent");
    uriPrefixes.put(TrackbackContent.class, "trackbackContent");
    uriPrefixes.put(AnnotationBlob.class, "annoteaBodyId:");
    uriPrefixes.put(RatingSummaryContent.class, "ratingSummaryContent");
    uriPrefixes.put(Reply.class, "reply");
    uriPrefixes.put(ReplyThread.class, "reply");
    //User Accounts / Profiles
    uriPrefixes.put(UserAccount.class, "account");
    uriPrefixes.put(UserPreference.class, "preferences");
    uriPrefixes.put(UserPreferences.class, "preferences");
    uriPrefixes.put(UserProfile.class, "profile");
    uriPrefixes.put(UserRole.class, "roles");
    //Aggregations
    uriPrefixes.put(Journal.class, "aggregation");
    uriPrefixes.put(Volume.class, "aggregation");
    uriPrefixes.put(Issue.class, "aggregation");
    //Miscellany
    uriPrefixes.put(AuthenticationId.class, "authids");
    uriPrefixes.put(Category.class, "category");
    uriPrefixes.put(Citation.class, "citation");
    uriPrefixes.put(EditorialBoard.class, "editorialBoard");
    uriPrefixes.put(CitedPerson.class, "citedPerson");
    uriPrefixes.put(ArticleContributor.class, "articleContributor");
    uriPrefixes.put(RelatedArticle.class, "relatedArticle");
  }

  public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
    final Class entityClass = Hibernate.getClass(object);

    ClassMetadata classMetadata = session.getFactory()
        .getClassMetadata(entityClass);

    if (classMetadata != null && classMetadata.getIdentifier(object, session) != null) {
      //entity already has an id either set as property or stored in the db
      return classMetadata.getIdentifier(object, session);
    }  else {
      //generate the id
      StringBuilder id = new StringBuilder();

      id.append(prefix)
        .append(uriPrefixes.get(entityClass))
        .append("/")
        .append(UUID.randomUUID().toString());

      if (entitiesWithStringId.contains(entityClass)) {
        return id.toString();
      }

      return URI.create(id.toString());
    }
  }
}
