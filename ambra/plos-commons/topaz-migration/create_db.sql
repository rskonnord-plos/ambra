/*
 * $HeadURL:
 * $Id:
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *
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
 * limitations under the License.
 */

drop database if exists ambra;

create database ambra;

use ambra;

create table AggregationDetachedCriteria (
    aggregationUri varchar(255) not null collate utf8_bin,
    detachedCriteriaUri varchar(255) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (aggregationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AggregationSimpleCollection (
    aggregationArticleUri varchar(255) not null collate utf8_bin,
    uri varchar(255),
    sortOrder integer not null,
    primary key (aggregationArticleUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Annotation (
    annotationUri varchar(255) not null collate utf8_bin,
    class varchar(255) not null,
    creator varchar(255) collate utf8_bin,
    anonymousCreator varchar(255),
    creationDate datetime,
    title text,
    mediator varchar(255),
    state integer,
    type varchar(255),
    webType varchar(255),
    annotates varchar(255),
    supersedesUri varchar(255) collate utf8_bin,
    supersededByUri varchar(255) collate utf8_bin,
    context text,
    body varchar(255) collate utf8_bin,
    bibliographicCitationUri varchar(255) collate utf8_bin,
    primary key (annotationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AnnotationBlob (
    blobUri varchar(255) not null collate utf8_bin,
    body blob,
    ciStatement longtext,
    primary key (blobUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Article (
    articleUri varchar(100) not null collate utf8_bin,
    state integer,
    eIssn varchar(255),
    archiveName varchar(255),
    contextElement varchar(255),
    dublinCoreIdentifier varchar(100) collate utf8_bin,
    isPartOf varchar(100) collate utf8_bin,
    primary key (articleUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleCategoryJoinTable (
    articleUri varchar(100) not null collate utf8_bin,
    articleCategoryUri varchar(200) not null collate utf8_bin,
    primary key (articleUri, articleCategoryUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticlePartsJoinTable (
    articleUri varchar(100) not null collate utf8_bin,
    objectInfoUri varchar(100) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (articleUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleRelated (
    articleRelatedUri varchar(100) not null collate utf8_bin,
    articleUri varchar(255) collate utf8_bin,
    relationType varchar(255),
    primary key (articleRelatedUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleRelatedJoinTable (
    articleUri varchar(100) not null collate utf8_bin,
    articleRelatedUri varchar(100) not null collate utf8_bin,
    primary key (articleUri, articleRelatedUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleRepresentationsJoinTable (
    articleUri varchar(100) not null collate utf8_bin,
    representationsUri varchar(150) not null collate utf8_bin,
    primary key (articleUri, representationsUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleTypes (
    articleUri varchar(100) not null collate utf8_bin,
    typeUri varchar(255) collate utf8_bin
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AuthenticationId (
    authenticationIdUri varchar(100) not null collate utf8_bin,
    realm varchar(512),
    value varchar(512),
    primary key (authenticationIdUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Category (
    categoryUri varchar(200) not null collate utf8_bin,
    mainCategory varchar(500),
    subCategory varchar(500),
    primary key (categoryUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Citation (
    citationUri varchar(255) not null collate utf8_bin,
    keyColumn varchar(255),
    year integer,
    displayYear varchar(255),
    month varchar(255),
    day varchar(255),
    volumeNumber integer,
    volume varchar(255),
    issue varchar(255),
    title text,
    publisherLocation varchar(255),
    publisherName text,
    pages varchar(255),
    eLocationId varchar(255),
    journal varchar(255),
    note text,
    url varchar(255),
    doi varchar(255),
    summary varchar(10000),
    citationType varchar(255),
    primary key (citationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table CitationAuthors (
    citationUri varchar(255) not null collate utf8_bin,
    authorUri varchar(100) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table CitationEditors (
    citationUri varchar(255) not null collate utf8_bin,
    editorUri varchar(100) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table CollaborativeAuthors (
    citationUri varchar(255) not null collate utf8_bin,
    authorName varchar(255),
    sortOrder integer not null,
    primary key (citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Criteria (
    criteriaUri varchar(255) not null collate utf8_bin,
    fieldName varchar(255),
    value varchar(255),
    primary key (criteriaUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table CriteriaList (
    criteriaUri varchar(255) not null collate utf8_bin,
    eqCriterionUri varchar(255) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (criteriaUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DetachedCriteria (
    detachedCriteriaUri varchar(255) not null collate utf8_bin,
    alias varchar(255),
    primary key (detachedCriteriaUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCore (
    articleUri varchar(100) not null collate utf8_bin,
    title text,
    description text,
    date datetime,
    rights text,
    type varchar(255),
    language varchar(255),
    publisher varchar(255),
    format varchar(255),
    available datetime,
    issued datetime,
    submitted datetime,
    accepted datetime,
    copyrightYear integer,
    created datetime,
    modified datetime,
    conformsTo varchar(255),
    bibliographicCitationUri varchar(255) collate utf8_bin,
    primary key (articleUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreContributors (
    articleUri varchar(100) not null collate utf8_bin,
    name varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreCreators (
    articleUri varchar(100) not null collate utf8_bin,
    name varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreLicenses (
    articleUri varchar(100) not null collate utf8_bin,
    licenseUri varchar(150) not null collate utf8_bin,
    primary key (articleUri, licenseUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreReferences (
    articleUri varchar(100) not null collate utf8_bin,
    citationUri varchar(255) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (articleUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreSubjects (
    articleUri varchar(100) not null collate utf8_bin,
    subject varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table DublinCoreSummary (
    articleUri varchar(100) not null collate utf8_bin,
    text varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table EditorialBoard (
    editorialBoardUri varchar(255) not null collate utf8_bin,
    supersedesUri varchar(255) collate utf8_bin,
    supersededByUri varchar(255) collate utf8_bin,
    primary key (editorialBoardUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table EditorialBoardEditors (
    editorialBoarduri varchar(255) not null collate utf8_bin,
    userProfileUri varchar(100) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (editorialBoarduri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Interests (
    personUri varchar(100) not null collate utf8_bin,
    interest varchar(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Issue (
    aggregationUri varchar(255) not null collate utf8_bin,
    editorialBoardUri varchar(255) collate utf8_bin,
    dublinCoreIdentifier varchar(100) collate utf8_bin,
    supersedesUri varchar(255) collate utf8_bin,
    supersededByUri varchar(255) collate utf8_bin,
    displayName varchar(255),
    respectOrder bit,
    imageUri varchar(255) collate utf8_bin,
    primary key (aggregationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table IssueArticleList (
    aggregationUri varchar(255) not null collate utf8_bin,
    articleUri varchar(255) collate utf8_bin,
    sortOrder integer not null,
    primary key (aggregationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Journal (
    aggregationUri varchar(255) not null collate utf8_bin,
    editorialBoardUri varchar(255) collate utf8_bin,
    dublinCoreIdentifier varchar(100) collate utf8_bin,
    supersedesUri varchar(255) collate utf8_bin,
    supersededByUri varchar(255) collate utf8_bin,
    journalKey varchar(255),
    eIssn varchar(255),
    imageUri varchar(255),
    currentIssueUri varchar(255),
    primary key (aggregationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table JournalVolumeList (
    aggregationUri varchar(255) not null collate utf8_bin,
    volumeUri varchar(255) collate utf8_bin,
    sortOrder integer not null,
    primary key (aggregationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table License (
    licenseUri varchar(150) not null collate utf8_bin,
    primary key (licenseUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ObjectInfo (
    objectInfoUri varchar(100) not null collate utf8_bin,
    eIssn varchar(255),
    contextElement varchar(255),
    isPartOf varchar(100) collate utf8_bin,
    dublinCoreIdentifier varchar(100) collate utf8_bin,
    primary key (objectInfoUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ObjectInfoRepresentationsJoinTable (
    articleUri varchar(100) not null collate utf8_bin,
    representationsUri varchar(150) not null collate utf8_bin,
    primary key (articleUri, representationsUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table RatingContent (
    ratingContentUri varchar(255) not null collate utf8_bin,
    insightValue integer,
    reliabilityValue integer,
    styleValue integer,
    singleRatingValue integer,
    commentTitle varchar(255),
    commentValue text,
    ciStatement text,
    primary key (ratingContentUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table RatingSummaryContent (
    ratingSummaryContentUri varchar(255) not null collate utf8_bin,
    insightNumRatings integer,
    insightTotal double precision,
    reliabilityNumRatings integer,
    reliabilityTotal double precision,
    styleNumRatings integer,
    styleTotal double precision,
    singleRatingNumRatings integer,
    singleRatingTotal double precision,
    numUsersThatRated integer,
    primary key (ratingSummaryContentUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table RepliesJoinTable (
    originalThreadUri varchar(255) not null collate utf8_bin,
    replyThreadUri varchar(255) not null collate utf8_bin,
    sortOrder integer not null,
    primary key (originalThreadUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Reply (
    replyUri varchar(255) not null collate utf8_bin,
    isThread bit not null,
    creator varchar(255) collate utf8_bin,
    anonymousCreator varchar(255),
    creationDate datetime,
    title text,
    mediator varchar(255),
    state integer,
    type varchar(255),
    webType varchar(255),
    body varchar(255) collate utf8_bin,
    root varchar(255) collate utf8_bin,
    inReplyTo varchar(255) collate utf8_bin,
    primary key (replyUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ReplyBlob (
    blobUri varchar(255) not null collate utf8_bin,
    body blob,
    ciStatement longtext,
    primary key (blobUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Representation (
    representationUri varchar(150) not null collate utf8_bin,
    name varchar(255),
    contentType varchar(255),
    size bigint,
    lastModified datetime,
    objectInfoUri varchar(100),
    primary key (representationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Syndication (
    syndicationUri varchar(255) not null collate utf8_bin,
    articleUri varchar(255) collate utf8_bin,
    status varchar(255),
    target varchar(255),
    submissionCount integer,
    statusTimeStamp datetime,
    submitTimeStamp datetime,
    errorMessage text,
    primary key (syndicationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Trackback (
    trackbackUri varchar(255) not null collate utf8_bin,
    url varchar(255),
    excerpt text,
    blog_name varchar(255),
    primary key (trackbackUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table TrackbackContent (
    trackbackContentUri varchar(255) not null collate utf8_bin,
    title text,
    url varchar(255),
    blog_name varchar(255),
    excerpt text,
    primary key (trackbackContentUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserAccount (
    userAccountUri varchar(100) not null collate utf8_bin,
    state integer,
    userProfileUri varchar(100) collate utf8_bin,
    primary key (userAccountUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserAccountAuthIdJoinTable (
    userAccountUri varchar(100) not null collate utf8_bin,
    authenticationIdUri varchar(100) not null collate utf8_bin,
    primary key (userAccountUri, authenticationIdUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserAccountPreferencesJoinTable (
    userAccountUri varchar(100) not null collate utf8_bin,
    preferencesUri varchar(100) not null collate utf8_bin,
    primary key (userAccountUri, preferencesUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserAccountRoleJoinTable (
    userAccountUri varchar(100) not null collate utf8_bin,
    roleUri varchar(100) not null collate utf8_bin,
    primary key (userAccountUri, roleUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserPreference (
    userPreferenceUri varchar(100) not null collate utf8_bin,
    name varchar(255),
    primary key (userPreferenceUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserPreferenceValues (
    userPreferenceUri varchar(100) not null collate utf8_bin,
    value varchar(255),
    position integer not null,
    primary key (userPreferenceUri, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserPreferences (
    userPreferencesUri varchar(100) not null collate utf8_bin,
    appId varchar(255),
    primary key (userPreferencesUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserPreferencesJoinTable (
    userPreferencesUri varchar(100) not null collate utf8_bin,
    userPreferenceUri varchar(100) not null collate utf8_bin,
    primary key (userPreferencesUri, userPreferenceUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserProfile (
    userProfileUri varchar(100) not null collate utf8_bin,
    realName text,
    givenNames varchar(255),
    surName text,
    title text,
    gender varchar(255),
    email varchar(255),
    homePage varchar(512),
    weblog varchar(512),
    publications varchar(255),
    displayName varchar(255),
    suffix varchar(255),
    positionType varchar(255),
    organizationName varchar(512),
    organizationType varchar(255),
    organizationVisibility bool default false,
    postalAddress text,
    city varchar(255),
    country varchar(255),
    biography text,
    biographyText text,
    interestsText text,
    researchAreasText text,
    primary key (userProfileUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table UserRole (
    userRoleUri varchar(100) not null collate utf8_bin,
    roleUri varchar(255) collate utf8_bin,
    primary key (userRoleUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table Volume (
    aggregationUri varchar(255) not null collate utf8_bin,
    editorialBoardUri varchar(255) collate utf8_bin,
    dublinCoreIdentifier varchar(100) collate utf8_bin,
    supersedesUri varchar(255) collate utf8_bin,
    supersededByUri varchar(255) collate utf8_bin,
    displayName varchar(255),
    imageUri varchar(255) collate utf8_bin,
    primary key (aggregationUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table VolumeIssueList (
    aggregationUri varchar(255) not null collate utf8_bin,
    issueUri varchar(255) collate utf8_bin,
    sortOrder integer not null,
    primary key (aggregationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table CitedPerson (
  citedPersonUri varchar(255) not null collate utf8_bin,
  fullName text,
  givenNames varchar(255),
  surnames text,
  suffix varchar(255),
  isAuthor bool,
  primary key (citedPersonUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ReferencedAuthorCitationJoinTable (
  citationUri varchar(255) not null collate utf8_bin,
  sortOrder integer not null,
  citedPersonUri varchar(255) not null collate utf8_bin,
  primary key(citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ReferencedEditorCitationJoinTable (
  citationUri varchar(255) not null collate utf8_bin,
  sortOrder integer not null,
  citedPersonUri varchar(255) not null collate utf8_bin,
  primary key(citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleContributor (
  contributorUri varchar(255) not null collate utf8_bin,
  fullName text,
  givenNames varchar(255),
  surnames text,
  suffix varchar(255),
  isAuthor bool,
  primary key (contributorUri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleEditorJoinTable (
  articleUri varchar(100) not null collate utf8_bin,
  sortOrder integer not null,
  contributorUri varchar(255) not null collate utf8_bin,
  primary key(articleUri, sortOrder)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table ArticleAuthorJoinTable (
  articleUri varchar(100) not null collate utf8_bin,
  sortOrder integer not null,
  contributorUri varchar(255) not null collate utf8_bin,
  primary key(articleUri, sortOrder)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AnnotationAuthorCitationJoinTable (
  citationUri varchar(255) not null collate utf8_bin,
  sortOrder integer not null,
  contributorUri varchar(255) not null collate utf8_bin,
  primary key(citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AnnotationEditorCitationJoinTable (
  citationUri varchar(255) not null collate utf8_bin,
  sortOrder integer not null,
  contributorUri varchar(255) not null collate utf8_bin,
  primary key(citationUri, sortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
