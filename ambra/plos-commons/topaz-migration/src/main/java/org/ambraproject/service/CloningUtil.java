/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.service;

import org.topazproject.ambra.models.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A custom bean copier to clone Aggregation and Annotation objects.
 * <p/>
 * This allows us to use Hibernate's inheritance mapping strategies.  The problem with trying to directly save a topaz
 * object to MySQL using inheritance mapping is that the Topaz object is actually a proxy-subclass, and with inheritance
 * mappings, Hibernate is expecting the actual declared class.
 *
 * @author Alex Kudlick Date: Mar 31, 2011
 *         <p/>
 *         org.ambraproject.service
 */
public class CloningUtil {

  /**
   * Main utility method for this class.  Clone the given object. Supported classes are: <ul> <li>Journal</li>
   * <li>Volume</li> <li>Issue</li> <li>Rating</li> <li>RatingSummary</li> <li>Trackback</li> <li>Comment</li>
   * <li>MinorCorrection</li> <li>FormalCorrection</li> <li>Retraction</li> <li>Reply - If the object passed is an
   * instance of ReplyThread, a ReplyThread object will be returned</li> <li>Article</li></ul>
   *
   * @param original - the object to copy
   * @param clazz    - the class to make the copy an instance of
   * @param <T>      - the class type.  Must be one of the supported classes listed above
   * @return - a copy of the given object that is an actual instance of the given class.
   */
  @SuppressWarnings("unchecked")
  public static <T> T clone(T original, Class<T> clazz) {

    if (clazz.equals(Article.class)) {
      return (T) cloneArticle((Article) original);
    } else if (clazz.equals(Journal.class)) {
      return (T) cloneJournal((Journal) original);
    } else if (clazz.equals(Volume.class)) {
      return (T) cloneVolume((Volume) original);
    } else if (clazz.equals(Issue.class)) {
      return (T) cloneIssue((Issue) original);
    } else if (clazz.equals(Rating.class)) {
      return (T) cloneRating((Rating) original);
    } else if (clazz.equals(RatingSummary.class)) {
      return (T) cloneRatingSummary((RatingSummary) original);
    } else if (clazz.equals(Trackback.class)) {
      return (T) cloneTrackback((Trackback) original);
    } else if (clazz.equals(Comment.class)) {
      return (T) cloneComment((Comment) original);
    } else if (clazz.equals(MinorCorrection.class)) {
      return (T) cloneMinorCorrection((MinorCorrection) original);
    } else if (clazz.equals(FormalCorrection.class)) {
      return (T) cloneFormalCorrection((FormalCorrection) original);
    } else if (clazz.equals(Retraction.class)) {
      return (T) cloneRetraction((Retraction) original);
    } else if (clazz.equals(Reply.class)) {
      return (T) cloneReply((Reply) original);
    } else {
      throw new IllegalArgumentException("No support for cloning " + clazz);
    }
  }

  private static Article cloneArticle(Article original) {
    Article clone = new Article();
    //ObjectInfo properties
    copyObjectInfoProperties(original, clone);

    //articleType
    if (original.getArticleType() == null) {
      clone.setArticleType(new HashSet<URI>(0));
    } else {
      clone.setArticleType(new HashSet<URI>(original.getArticleType().size()));
      for (URI type : original.getArticleType()) {
        clone.getArticleType().add(type);
      }
    }

    //relatedArticles
    if (original.getRelatedArticles() == null) {
      clone.setRelatedArticles(new HashSet<RelatedArticle>(0));
    } else {
      clone.setRelatedArticles(new HashSet<RelatedArticle>(original.getRelatedArticles().size()));
      for (RelatedArticle art : original.getRelatedArticles()) {
        clone.getRelatedArticles().add(cloneRelatedArticle(art));
      }
    }

    //categories
    if (original.getCategories() == null) {
      clone.setCategories(new HashSet<Category>(0));
    } else {
      clone.setCategories(new HashSet<Category>(original.getCategories().size()));
      for (Category category : original.getCategories()) {
        clone.getCategories().add(cloneCategory(category));
      }
    }

    //Article Parts
    if (original.getParts() == null) {
      clone.setParts(null);
    } else {
      clone.setParts(new ArrayList<ObjectInfo>());
      for (ObjectInfo objectInfo : original.getParts()) {
        ObjectInfo newObject = new ObjectInfo();
        copyObjectInfoProperties(objectInfo, newObject);
        clone.getParts().add(newObject);
      }
    }

    clone.setState(original.getState());
    clone.setArchiveName(original.getArchiveName());

    return clone;
  }

  private static Category cloneCategory(Category category) {
    Category clone = new Category();
    clone.setId(category.getId());
    clone.setMainCategory(category.getMainCategory());
    clone.setSubCategory(category.getSubCategory());
    return clone;
  }

  private static RelatedArticle cloneRelatedArticle(RelatedArticle original) {
    RelatedArticle clone = new RelatedArticle();
    clone.setArticle(original.getArticle());
    clone.setId(original.getId());
    clone.setRelationType(original.getRelationType());
    return clone;
  }

  private static void copyObjectInfoProperties(ObjectInfo original, ObjectInfo clone) {
    clone.setId(original.getId());
    clone.setDublinCore(cloneDublinCore(original.getDublinCore(), original.getId().toString()));

    //Make sure we don't loop infinitely if 'isPartOf' is actually the object itself
    if (original.getIsPartOf() == null) {
      clone.setIsPartOf(null);
    //I'm not convinced the following bit of code is needed.  When copying "isPartOf" it will always be
    //a reference to an article class that is copied over as a parent of this object.
    //If this turns out to not be the case, we'll run into recursion problems with article parts and
    //something will have to be figured out besides commenting out this code
    //} else if (!original.getIsPartOf().getId().equals(original.getId()) && cloneArticle == true) {
    //  clone.setIsPartOf(cloneArticle(original.getIsPartOf()));
    } else {
      Article isPartOf = new Article();
      isPartOf.setId(original.getIsPartOf().getId());
      clone.setIsPartOf(isPartOf);
    }
    if (original.getRepresentations() != null) {
      clone.setRepresentations(new HashSet<Representation>(original.getRepresentations().size()));
      for (Representation rep : original.getRepresentations()) {
        clone.getRepresentations().add(cloneRepresentation(rep, clone));
      }
    } else {
      clone.setRepresentations(null);
    }
    clone.setContextElement(original.getContextElement());
    clone.seteIssn(original.geteIssn());
  }

  private static Representation cloneRepresentation(Representation original, ObjectInfo parent) {
    if (original == null) {
      return null;
    }
    Representation clone = new Representation();
    clone.setId(original.getId());
    clone.setName(original.getName());
    clone.setContentType(original.getContentType());
    clone.setSize(original.getSize());
    clone.setLastModified(original.getLastModified());

    //Make sure we don't loop infinitely (at least for easily identifiable loops)
    if (parent == null || (original.getObject() != null && !original.getObject().getId().equals(parent.getId()))) {
      ObjectInfo obj = new ObjectInfo();
      copyObjectInfoProperties(original.getObject(), obj);
      clone.setObject(obj);
    } else {
      clone.setObject(original.getObject() == null ? null : parent);
    }

    //Not going to copy Body
    //TODO: Implement later?
    //clone.setBody();

    return clone;
  }

  private static Reply cloneReply(Reply original) {
    if (original instanceof ReplyThread) {
      return cloneReplyThread((ReplyThread) original);
    }
    Reply reply = new Reply();
    copyAnnoteaProperties(original, reply, Reply.class);
    reply.setId(original.getId());
    reply.setRoot(original.getRoot());
    reply.setInReplyTo(original.getInReplyTo());
    reply.setType(original.getType());
    return reply;
  }

  private static ReplyThread cloneReplyThread(ReplyThread original) {
    ReplyThread reply = new ReplyThread();
    copyAnnoteaProperties(original, reply, ReplyThread.class);
    reply.setId(original.getId());
    reply.setRoot(original.getRoot());
    reply.setInReplyTo(original.getInReplyTo());
    reply.setType(original.getType());
    reply.setReplies(new ArrayList<ReplyThread>(original.getReplies().size()));
    for (ReplyThread rt : original.getReplies()) {
      reply.getReplies().add(cloneReplyThread(rt)); //Hopefully this doesn't loop infinitely
    }
    return reply;
  }

  private static FormalCorrection cloneFormalCorrection(FormalCorrection original) {
    FormalCorrection corr = new FormalCorrection();

    copyAnnoteaProperties(original, corr, FormalCorrection.class);
    copyAnnotationProperties(original, corr);
    corr.setBibliographicCitation(original.getBibliographicCitation());
    return corr;
  }

  private static Retraction cloneRetraction(Retraction original) {
    Retraction retraction = new Retraction();
    copyAnnoteaProperties(original, retraction, Retraction.class);
    copyAnnotationProperties(original, retraction);
    retraction.setBibliographicCitation(original.getBibliographicCitation());
    return retraction;
  }

  private static MinorCorrection cloneMinorCorrection(MinorCorrection original) {
    MinorCorrection corr = new MinorCorrection();
    copyAnnoteaProperties(original, corr, MinorCorrection.class);
    copyAnnotationProperties(original, corr);
    return corr;
  }

  private static Comment cloneComment(Comment original) {
    Comment comment = new Comment();
    copyAnnoteaProperties(original, comment, MinorCorrection.class);
    copyAnnotationProperties(original, comment);
    return comment;
  }

  private static Rating cloneRating(Rating original) {
    Rating rating = new Rating();
    copyAnnoteaProperties(original, rating, Rating.class);
    copyAnnotationProperties(original, rating);
    return rating;
  }

  private static RatingSummary cloneRatingSummary(RatingSummary original) {
    RatingSummary ratingSummary = new RatingSummary();
    copyAnnoteaProperties(original, ratingSummary, RatingSummary.class);
    copyAnnotationProperties(original, ratingSummary);
    return ratingSummary;
  }

  private static Trackback cloneTrackback(Trackback original) {
    Trackback trackback = new Trackback();
    copyAnnoteaProperties(original, trackback, Trackback.class);
    copyAnnotationProperties(original, trackback);
    //No need to copy Trackback-specific properties since the getters all return things from the body
    return trackback;
  }

  @SuppressWarnings("unchecked")
  public static void copyAnnotationProperties(Annotation source, Annotation target) {
    //Annotation properties
    target.setId(source.getId());
    target.setAnnotates(source.getAnnotates());
    target.setContext(source.getContext());
    target.setSupersedes(source.getSupersedes());
    target.setSupersededBy(source.getSupersedes());

  }

  public static void copyAnnoteaProperties(Annotea source, Annotea target, Class<? extends Annotea> clazz) {
    target.setCreated(source.getCreated());
    target.setCreator(source.getCreator());
    target.setAnonymousCreator(source.getAnonymousCreator());
    target.setTitle(source.getTitle());
    target.setMediator(source.getMediator());
    target.setState(source.getState());
    copyAnnoteaBody(source, target, clazz);
    //these don't do anything, but it doesn't matter since the getters all return constants
    target.setType(source.getType());
    target.setWebType(source.getWebType());

  }

  @SuppressWarnings("unchecked")
  private static void copyAnnoteaBody(Annotea source, Annotea target, Class<? extends Annotea> clazz) {
    if (source.getBody() == null) {
      target.setBody(null);
      return;
    }
    //Trackback has TrackbackContent body
    if (clazz.equals(Trackback.class)) {
      TrackbackContent origBody = (TrackbackContent) source.getBody();
      TrackbackContent body = new TrackbackContent();
      body.setId(origBody.getId());
      body.setTitle(origBody.getTitle());
      body.setUrl(origBody.getUrl());
      body.setBlog_name(origBody.getBlog_name());
      body.setExcerpt(origBody.getExcerpt());
      target.setBody(body);
    }
    //Reply and ReplyThread have ReplyBlob bodies
    else if (clazz.equals(Reply.class) || clazz.equals(ReplyThread.class)) {
      ReplyBlob origBody = (ReplyBlob) source.getBody();
      ReplyBlob body = new ReplyBlob();
      body.setId(origBody.getId());
      body.setCIStatement(origBody.getCIStatement());
      body.setBody(origBody.getBody());
      target.setBody(body);
    }
    //Subclasses of Article Annotation have AnnotationBlob bodies
    else if (clazz.equals(Comment.class) || clazz.equals(MinorCorrection.class)
        || clazz.equals(FormalCorrection.class) || clazz.equals(Retraction.class)) {
      AnnotationBlob origBody = (AnnotationBlob) source.getBody();
      AnnotationBlob body = new AnnotationBlob();
      body.setBody(origBody.getBody());
      body.setId(origBody.getId());
      body.setCIStatement(origBody.getCIStatement());
      target.setBody(body);
    }
    //Rating has RatingContent body
    else if (clazz.equals(Rating.class)) {
      RatingContent origBody = (RatingContent) source.getBody();
      RatingContent body = new RatingContent();
      body.setId(origBody.getId());
      body.setInsightValue(origBody.getInsightValue());
      body.setReliabilityValue(origBody.getReliabilityValue());
      body.setStyleValue(origBody.getStyleValue());
      body.setSingleRatingValue(origBody.getSingleRatingValue());
      body.setCommentTitle(origBody.getCommentTitle());
      body.setCommentValue(origBody.getCommentValue());
      body.setCIStatement(origBody.getCIStatement());
      target.setBody(body);
    }
    //RatingSummary has RatingSummaryContent
    else if (clazz.equals(RatingSummary.class)) {
      RatingSummaryContent origBody = (RatingSummaryContent) source.getBody();
      RatingSummaryContent body = new RatingSummaryContent();
      body.setId(origBody.getId());
      body.setInsightNumRatings(origBody.getInsightNumRatings());
      body.setInsightTotal(origBody.getInsightTotal());
      body.setReliabilityNumRatings(origBody.getReliabilityNumRatings());
      body.setReliabilityTotal(origBody.getReliabilityTotal());
      body.setStyleNumRatings(origBody.getStyleNumRatings());
      body.setStyleTotal(origBody.getStyleTotal());
      body.setSingleRatingNumRatings(origBody.getSingleRatingNumRatings());
      body.setSingleRatingTotal(origBody.getSingleRatingTotal());
      body.setNumUsersThatRated(origBody.getNumUsersThatRated());
      target.setBody(body);
    } else {
      throw new IllegalArgumentException("Can't copy body for " + clazz);
    }
  }

  private static void copyAggregationProperties(Aggregation source, Aggregation target) {
    target.setId(source.getId());
    target.setEditorialBoard(source.getEditorialBoard());
    target.setDublinCore(cloneDublinCore(source.getDublinCore(), source.getId().toString()));
    target.setSimpleCollection(source.getSimpleCollection());
    target.setSmartCollectionRules(source.getSmartCollectionRules());
    target.setSupersedes(source.getSupersedes());
    target.setSupersededBy(source.getSupersededBy());
  }

  private static DublinCore cloneDublinCore(DublinCore source, String defaultIdentifier) {
    if (source == null) {
      return null;
    }
    DublinCore clone = new DublinCore();
    clone.setTitle(source.getTitle());
    clone.setDescription(source.getDescription());
    clone.setCreators(source.getCreators());
    clone.setDate(source.getDate());
    //Hibernate relies on the identifier as a primary key, so we can't have a null one
    if (source.getIdentifier() != null) {
      clone.setIdentifier(source.getIdentifier());
    } else {
      clone.setIdentifier(defaultIdentifier);
    }
    clone.setRights(source.getRights());
    clone.setType(source.getType());
    clone.setContributors(source.getContributors());
    clone.setSubjects(source.getSubjects());
    clone.setLanguage(source.getLanguage());
    clone.setPublisher(source.getPublisher());
    clone.setFormat(source.getFormat());
    clone.setSource(source.getSource());
    clone.setAvailable(source.getAvailable());
    clone.setIssued(source.getIssued());
    clone.setSubmitted(source.getSubmitted());
    clone.setAccepted(source.getAccepted());
    clone.setCopyrightYear(source.getCopyrightYear());
    clone.setSummary(source.getSummary());
    clone.setBibliographicCitation(cloneCitation(source.getBibliographicCitation()));
    clone.setCreated(source.getCreated());
    clone.setLicense(source.getLicense());
    clone.setModified(source.getModified());
    if (source.getReferences() != null) {
      clone.setReferences(new ArrayList<Citation>(source.getReferences().size()));
      for (Citation citation : source.getReferences()) {
        clone.getReferences().add(cloneCitation(citation));
      }
    } else {
      clone.setReferences(null);
    }
    clone.setConformsTo(source.getConformsTo());
    return clone;
  }

  private static Citation cloneCitation(Citation original) {
    if (original == null) {
      return null;
    }
    Citation clone = new Citation();
    clone.setId(original.getId());
    clone.setKey(original.getKey());
    clone.setYear(original.getYear());
    clone.setDisplayYear(original.getDisplayYear());
    clone.setMonth(original.getMonth());
    clone.setDay(original.getDay());
    clone.setVolumeNumber(original.getVolumeNumber());
    clone.setVolume(original.getVolume());
    clone.setIssue(original.getIssue());
    clone.setTitle(original.getTitle());
    clone.setPublisherLocation(original.getPublisherLocation());
    clone.setPublisherName(original.getPublisherName());
    clone.setPages(original.getPages());
    clone.setELocationId(original.getELocationId());
    clone.setJournal(original.getJournal());
    clone.setNote(original.getNote());

    if (original.getEditors() != null) {
      clone.setEditors(new ArrayList<UserProfile>(original.getEditors().size()));
      for (UserProfile up : original.getEditors()) {
        clone.getEditors().add(cloneUserProfile(up));
      }
    } else {
      clone.setEditors(null);
    }
    if (original.getAuthors() != null) {
      clone.setAuthors(new ArrayList<UserProfile>(original.getAuthors().size()));
      for (UserProfile up : original.getAuthors()) {
        clone.getAuthors().add(cloneUserProfile(up));
      }
    } else {
      clone.setAuthors(null);
    }
    clone.setCollaborativeAuthors(original.getCollaborativeAuthors());
    clone.setUrl(original.getUrl());
    clone.setDoi(original.getDoi());
    clone.setSummary(original.getSummary());
    clone.setCitationType(original.getCitationType());

    return clone;
  }

  private static UserProfile cloneUserProfile(UserProfile original) {
    UserProfile clone = new UserProfile();
    clone.setId(original.getId());
    clone.setRealName(original.getRealName());
    clone.setGivenNames(original.getGivenNames());
    clone.setSurnames(original.getSurnames());
    clone.setTitle(original.getTitle());
    clone.setGender(original.getGender());
    clone.setEmail(original.getEmail());
    clone.setHomePage(original.getHomePage());
    clone.setWeblog(original.getWeblog());
    clone.setPublications(original.getPublications());
    clone.setInterests(original.getInterests());
    clone.setDisplayName(original.getDisplayName());
    clone.setSuffix(original.getSuffix());
    clone.setPositionType(original.getPositionType());
    clone.setOrganizationName(original.getOrganizationName());
    clone.setOrganizationType(original.getOrganizationType());
    clone.setPostalAddress(original.getPostalAddress());
    clone.setCity(original.getCity());
    clone.setCountry(original.getCountry());
    clone.setBiography(original.getBiography());
    clone.setBiographyText(original.getBiographyText());
    clone.setInterestsText(original.getInterestsText());
    clone.setResearchAreasText(original.getResearchAreasText());
    return clone;
  }

  private static Journal cloneJournal(Journal original) {
    Journal journal = new Journal();
    copyAggregationProperties(original, journal);
    journal.setKey(original.getKey());
    journal.seteIssn(original.geteIssn());
    journal.setCurrentIssue(original.getCurrentIssue());
    journal.setVolumes(original.getVolumes());
    journal.setImage(original.getImage());
    return journal;
  }

  private static Volume cloneVolume(Volume original) {
    Volume volume = new Volume();
    copyAggregationProperties(original, volume);
    volume.setDisplayName(original.getDisplayName());
    volume.setImage(original.getImage());
    volume.setIssueList(original.getIssueList());
    return volume;
  }

  private static Issue cloneIssue(Issue original) {
    Issue issue = new Issue();
    copyAggregationProperties(original, issue);
    issue.setDisplayName(original.getDisplayName());
    issue.setArticleList(original.getArticleList());
    issue.setRespectOrder(original.getRespectOrder());
    issue.setImage(original.getImage());
    return issue;
  }

}
