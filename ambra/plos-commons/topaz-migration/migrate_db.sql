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

/*
 *  NOTE: In order to run this script, you need to up the innodb buffer size; put
 *
 *  innodb_buffer_pool_size=256M
 *
 *  in the my.cnf file (this requires a database restart).  Make sure to put that line in the [mysqld] section
 *  See http://mrothouse.wordpress.com/2006/10/20/mysql-error-1206/
 */


use ambra;

/***********************************
**** Indexes and Foreign Keys  *****
************************************/

alter table DetachedCriteria
  add index (detachedCriteriaUri);

alter table Annotation
  add index (body),
  add index (creator),
  add index (bibliographicCitationUri),
  add constraint foreign key (bibliographicCitationUri) references Citation (CitationUri),
  add constraint foreign key (creator) references UserAccount (userAccountUri);

alter table TrackbackContent
  add index (trackbackContentUri);

alter table AggregationDetachedCriteria
  add index (detachedCriteriaUri),
  add constraint foreign key (detachedCriteriaUri) references DetachedCriteria (detachedCriteriaUri);

alter table Article
  add index (dublinCoreIdentifier),
  add index (isPartOf),
  add constraint foreign key (dublinCoreIdentifier) references DublinCore (articleUri),
  add constraint foreign key (isPartOf) references Article (articleUri);

alter table ArticleCategoryJoinTable
  add constraint foreign key (articleUri) references Article (articleUri),
  add constraint foreign key (articleCategoryUri) references Category (categoryUri);

alter table ArticlePartsJoinTable
  add index (objectInfoUri),
  add constraint foreign key (objectInfoUri) references ObjectInfo (objectInfoUri),
  add constraint foreign key (articleUri) references Article (articleUri);

alter table ArticleRelatedJoinTable
  add constraint foreign key (articleRelatedUri) references ArticleRelated (articleRelatedUri),
  add constraint foreign key (articleUri) references Article (articleUri);

alter table ArticleRepresentationsJoinTable
  add index (representationsUri),
  add index (articleUri),
  add constraint foreign key (representationsUri) references Representation (representationUri),
  add constraint foreign key (articleUri) references Article (articleUri);

alter table ArticleTypes
  add index (articleUri),
  add constraint foreign key (articleUri) references Article (articleUri);

alter table CitationAuthors
  add index (authorUri),
  add constraint foreign key (authorUri) references UserProfile (userProfileUri),
  add constraint foreign key (citationUri) references Citation (citationUri);

alter table CitationEditors
  add index (editorUri),
  add constraint foreign key (editorUri) references UserProfile (userProfileUri),
  add constraint foreign key (citationUri) references Citation (citationUri);

alter table CollaborativeAuthors
  add constraint foreign key (citationUri) references Citation (citationUri);

alter table CriteriaList
  add index (eqCriterionUri),
  add constraint foreign key (criteriaUri) references DetachedCriteria (detachedCriteriaUri),
  add constraint foreign key (eqCriterionUri) references Criteria (criteriaUri);

alter table DublinCore
  add index (bibliographicCitationUri),
  add constraint foreign key (bibliographicCitationUri) references Citation (citationUri);

alter table DublinCoreContributors
  add index (articleUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table DublinCoreCreators
  add index (articleUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table DublinCoreLicenses
  add constraint foreign key (licenseUri) references License (licenseUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table DublinCoreReferences
  add index (citationUri),
  add constraint foreign key (citationUri) references Citation (citationUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table DublinCoreSubjects
  add index (articleUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table DublinCoreSummary
  add index (articleUri),
  add constraint foreign key (articleUri) references DublinCore (articleUri);

alter table EditorialBoardEditors
  add index (userProfileUri),
  add constraint foreign key (userProfileUri) references UserProfile (userProfileUri),
  add constraint foreign key (editorialBoarduri) references EditorialBoard (editorialBoardUri);

alter table Interests
  add index (personUri),
  add constraint foreign key (personUri) references UserProfile (userProfileUri);

alter table Issue
  add index (dublinCoreIdentifier),
  add index (editorialBoardUri),
  add constraint foreign key (dublinCoreIdentifier) references DublinCore (articleUri),
  add constraint foreign key (editorialBoardUri) references EditorialBoard (editorialBoardUri);

alter table IssueArticleList
  add constraint foreign key (aggregationUri) references Issue (aggregationUri);

alter table Journal
  add index (dublinCoreIdentifier),
  add index (editorialBoardUri),
  add constraint foreign key (dublinCoreIdentifier) references DublinCore (articleUri),
  add constraint foreign key (editorialBoardUri) references EditorialBoard (editorialBoardUri);

alter table JournalVolumeList
  add constraint foreign key (aggregationUri) references Journal (aggregationUri);

alter table ObjectInfo
  add index (dublinCoreIdentifier),
  add index (isPartOf),
  add constraint foreign key (dublinCoreIdentifier) references DublinCore (articleUri),
  add constraint foreign key (isPartOf) references Article (articleUri);

alter table ObjectInfoRepresentationsJoinTable
  add constraint foreign key (representationsUri) references Representation (representationUri),
  add constraint foreign key (articleUri) references ObjectInfo (objectInfoUri);

alter table RepliesJoinTable
  add index (replyThreadUri),
  add constraint foreign key (originalThreadUri) references Reply (replyUri),
  add constraint foreign key (replyThreadUri) references Reply (replyUri);

alter table Reply
  add index (body),
  add constraint foreign key (creator) references UserAccount (userAccountUri),
  add constraint foreign key (body) references ReplyBlob (blobUri);

alter table Representation
  add index (objectInfoUri);

alter table Trackback
  add constraint foreign key (trackbackUri) references Annotation (annotationUri);

alter table UserAccount
  add index (userProfileUri),
  add constraint foreign key (userProfileUri) references UserProfile (userProfileUri);

alter table UserAccountAuthIdJoinTable
  add constraint foreign key (userAccountUri) references UserAccount (userAccountUri),
  add constraint foreign key (authenticationIdUri) references AuthenticationId (authenticationIdUri);

alter table UserAccountPreferencesJoinTable
  add constraint foreign key (userAccountUri) references UserAccount (userAccountUri),
  add constraint foreign key (preferencesUri) references UserPreferences (userPreferencesUri);

alter table UserAccountRoleJoinTable
  add constraint foreign key (userAccountUri) references UserAccount (userAccountUri),
  add constraint foreign key (roleUri) references UserRole (userRoleUri);

alter table UserPreferenceValues
  add constraint foreign key (userPreferenceUri) references UserPreference (userPreferenceUri);

alter table UserPreferencesJoinTable
  add constraint foreign key (userPreferencesUri) references UserPreferences (userPreferencesUri),
  add constraint foreign key (userPreferenceUri) references UserPreference (userPreferenceUri);

alter table Volume
  add index (dublinCoreIdentifier),
  add index (editorialBoardUri),
  add constraint foreign key (dublinCoreIdentifier) references DublinCore (articleUri),
  add constraint foreign key (editorialBoardUri) references EditorialBoard (editorialBoardUri);

alter table VolumeIssueList
  add constraint foreign key (aggregationUri) references Volume (aggregationUri);

alter table UserProfile
  add index (displayName),
  add index (email);

/************************************************************
 * Remove user profiles for authors and editors of Articles *
 ************************************************************/

-- Author and editor join tables
insert into ArticleAuthorJoinTable
  (select a.articleUri, ca.sortOrder, ca.authorUri
    from Article a join DublinCore dc on a.articleUri = dc.articleUri
    join Citation c on dc.bibliographicCitationUri = c.citationUri
    join CitationAuthors ca on c.citationUri = ca.citationUri);

insert into ArticleEditorJoinTable
  (select a.articleUri, ce.sortOrder, ce.editorUri
    from Article a join DublinCore dc on a.articleUri = dc.articleUri
    join Citation c on dc.bibliographicCitationUri = c.citationUri
    join CitationEditors ce on c.citationUri = ce.citationUri);

-- Author and Editor tables
insert into ArticleContributor
  (select ajt.contributorUri, up.realName,
    up.givenNames, up.surName, up.suffix, true
    from ArticleAuthorJoinTable ajt
    join UserProfile up on ajt.contributorUri = up.userProfileUri);

insert into ArticleContributor
  (select ajt.contributorUri, up.realName,
    up.givenNames, up.surName, up.suffix, false
    from ArticleEditorJoinTable ajt
    join UserProfile up on ajt.contributorUri = up.userProfileUri);

-- Join tables from Citation to cited people
insert into ReferencedAuthorCitationJoinTable (
  select ca.citationUri, ca.sortOrder, ca.authorUri
  from DublinCoreReferences ref
  join CitationAuthors ca on ref.citationUri = ca.citationUri
);
insert into ReferencedEditorCitationJoinTable (
  select ce.citationUri, ce.sortOrder, ce.editorUri
  from DublinCoreReferences ref
  join CitationEditors ce on ref.citationUri = ce.citationUri
);

-- Actual table for referenced people
insert into CitedPerson (
  select ca.authorUri, up.realName,
    up.givenNames, up.surName, up.suffix, true
    from DublinCoreReferences ref
    join CitationAuthors ca on ref.citationUri = ca.citationUri
    join UserProfile up on ca.authorUri = up.userProfileUri
);

insert into CitedPerson (
  select ce.editorUri, up.realName,
    up.givenNames, up.surName, up.suffix, false
    from DublinCoreReferences ref
    join CitationEditors ce on ref.citationUri = ce.citationUri
    join UserProfile up on ce.editorUri = up.userProfileUri
);

-- Join tables for Annotation's Citation's authors and editors
insert into AnnotationAuthorCitationJoinTable (
 select ca.citationUri, ca.sortOrder, ca.authorUri
 from Annotation a, Citation c, CitationAuthors ca
 where c.citationUri = a.bibliographicCitationUri
 and ca.citationUri = c.citationUri
);

insert into AnnotationEditorCitationJoinTable (
select ce.citationUri, ce.sortOrder, ce.editorUri
  from Annotation a, Citation c, CitationEditors ce
  where c.citationUri = a.bibliographicCitationUri
   and ce.citationUri = c.citationUri
);

-- Add Annotation's Citation's authors and editors
insert into ArticleContributor
 (select ca.authorUri, up.realName, up.givenNames, up.surName, up.suffix, true
  from Annotation a, Citation c, CitationAuthors ca, UserProfile up
  where c.citationUri = a.bibliographicCitationUri
   and ca.citationUri = c.citationUri
   and up.userProfileUri = ca.authorUri);

insert into ArticleContributor
 (select ce.editorUri, up.realName, up.givenNames, up.surName, up.suffix, false
  from Annotation a, Citation c, CitationEditors ce, UserProfile up
  where c.citationUri = a.bibliographicCitationUri
   and ce.citationUri = c.citationUri
   and up.userProfileUri = ce.editorUri);


-- Indexes and foreign keys

alter table ArticleAuthorJoinTable
  add index (contributorUri),
  add constraint foreign key (articleUri) references Article (articleUri),
  add constraint foreign key (contributorUri) references ArticleContributor (contributorUri);

alter table ArticleEditorJoinTable
  add index (contributorUri),
  add constraint foreign key (articleUri) references Article (articleUri),
  add constraint foreign key (contributorUri) references ArticleContributor (contributorUri);

alter table ReferencedAuthorCitationJoinTable
  add index (citedPersonUri),
  add constraint foreign key (citationUri) references Citation (citationUri),
  add constraint foreign key (citedPersonUri) references CitedPerson (citedPersonUri);

alter table ReferencedEditorCitationJoinTable
  add index (citedPersonUri),
  add constraint foreign key (citationUri) references Citation (citationUri),
  add constraint foreign key (citedPersonUri) references CitedPerson (citedPersonUri);

alter table AnnotationAuthorCitationJoinTable
  add index (contributorUri),
  add constraint foreign key (citationUri) references Citation (citationUri),
  add constraint foreign key (contributorUri) references ArticleContributor (contributorUri);

alter table AnnotationEditorCitationJoinTable
  add index (contributorUri),
  add constraint foreign key (citationUri) references Citation (citationUri),
  add constraint foreign key (contributorUri) references ArticleContributor (contributorUri);


-- delete the links to authors / editors
delete ca from CitationAuthors ca inner join DublinCore dc on ca.citationUri = dc.bibliographicCitationUri;
delete ce from CitationEditors ce inner join DublinCore dc on ce.citationUri = dc.bibliographicCitationUri;

-- delete the links to annotation's citation's authors and editors
delete ca from CitationAuthors ca inner join Annotation a on ca.citationUri = a.bibliographicCitationUri;
delete ce from CitationEditors ce inner join Annotation a on ce.citationUri = a.bibliographicCitationUri;

-- delete the links to reference profiles
delete ca from CitationAuthors ca inner join DublinCoreReferences dc on ca.citationUri = dc.citationUri;
delete ce from CitationEditors ce inner join DublinCoreReferences dc on ce.citationUri = dc.citationUri;

-- Delete the old profiles
delete up from UserProfile up inner join ArticleContributor ac on up.userProfileUri = ac.contributorUri;
delete up from UserProfile up inner join CitedPerson cp on up.userProfileUri = cp.citedPersonUri;


-- Updating one annotation that takes too long to evaluate
update Annotation
set context = 'info:doi/10.1371/journal.pcbi.1000021#xpointer(string-range(%2Farticle%5B1%5D%2Fbody%5B1%5D%2Fsec%5B5%5D%2Fsupplementary-material%5B35%5D%2Fcaption%5B1%5D%2Fp%5B1%5D%2C+%27%27%2C+128%2C+10%29%5B1%5D)'
where annotationUri = 'info:doi/10.1371/annotation/1c55be5f-ecd7-49be-91c1-91881be60297';