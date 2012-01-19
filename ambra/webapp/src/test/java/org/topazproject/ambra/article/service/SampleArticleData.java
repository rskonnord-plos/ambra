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

package org.topazproject.ambra.article.service;

import org.testng.annotations.DataProvider;
import org.topazproject.ambra.models.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Data provider class that returns an article's xml and the fully populated article
 *
 * @author Alex Kudlick Date: 6/8/11 <p/>                                       categories.add(category);
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
public class SampleArticleData {
  private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @DataProvider(name = "sampleArticle")
  public static Object[][] getSampleArticleData() throws Exception {
    File testFile = new File(SampleArticleData.class.getClassLoader().getResource("test-ingest.zip").toURI());
    ZipFile archive = new ZipFile(testFile);
    Article article = new Article();
    article.setId(URI.create("info:doi/10.1371/journal.pmed.0050082"));
    article.setDublinCore(getExpectedDublinCore());
    article.setCategories(getSampleCategories());
    article.seteIssn("1549-1676");
    article.setArticleType(getSampleArticleTypes());
    article.setRepresentations(getSampleRepresentations());

    List<ArticleContributor> authors = new ArrayList<ArticleContributor>(2);
    ArticleContributor author1 = new ArticleContributor();
    author1.setFullName("David M Benedek");
    author1.setGivenNames("David M");
    author1.setSurnames("Benedek");
    author1.setSuffix("");
    authors.add(author1);
    ArticleContributor author2 = new ArticleContributor();
    author2.setFullName("Robert J Ursano");
    author2.setGivenNames("Robert J");
    author2.setSurnames("Ursano");
    author2.setSuffix("");
    authors.add(author2);

    article.setAuthors(authors);

    return new Object[][]{
        {archive, article}
    };
  }

  @DataProvider(name = "alteredZip")
  public static Object[][] getAlteredZip() throws Exception {
    File original = new File(SampleArticleData.class.getClassLoader().getResource("altered-ingest-original.zip").toURI());
    File altered = new File(SampleArticleData.class.getClassLoader().getResource("altered-ingest-new.zip").toURI());
    ZipFile originalArchiveAddFiles = new ZipFile(original);
    ZipFile alteredArchiveAddFiles = new ZipFile(altered);
    List<URI> newImageUris = new ArrayList<URI>(13);
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g001"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g002"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g003"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g004"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g005"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g006"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g007"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.g008"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.s001"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.s002"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.s003"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.s004"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.s005"));
    newImageUris.add(URI.create("info:doi/10.1371/journal.pgen.1002295.xml"));

    File removedImgsAltered = new File(SampleArticleData.class.getClassLoader().getResource("ingest-remove-img-altered.zip").toURI());
    File removedImgsOrig = new File(SampleArticleData.class.getClassLoader().getResource("ingest-remove-img-orig.zip").toURI());

    return new Object[][]{
        {originalArchiveAddFiles, alteredArchiveAddFiles, newImageUris},
        {new ZipFile(removedImgsOrig), new ZipFile(removedImgsAltered), new LinkedList<URI>()}
    };
  }

  public static Set<URI> getSampleArticleTypes() throws Exception {
    Set<URI> types = new HashSet<URI>();
    types.add(URI.create("http://rdf.plos.org/RDF/articleType/article-commentary"));
    types.add(URI.create("http://rdf.plos.org/RDF/articleType/Perspective"));
    return types;
  }

  public static Set<Category> getSampleCategories() throws Exception {
    Set<Category> categories = new HashSet<Category>(2);
    Category category1 = new Category();
    category1.setMainCategory("Mental Health");
    categories.add(category1);
    Category category2 = new Category();
    category2.setMainCategory("Public Health and Epidemiology");
    categories.add(category2);

    return categories;
  }

  @DataProvider(name = "sampleSecondaryObjects")
  public static Object[][] getSampleSecondaryObjects() throws Exception {
    File testFile = new File(SampleArticleData.class.getClassLoader().getResource("test-ingest-with-parts.zip").toURI());
    ZipFile archive = new ZipFile(testFile);
    Article isPartOf = new Article();
    isPartOf.setId(URI.create("info:doi/10.1371/journal.pntd.0000241"));

    Set<String> creators = new HashSet<String>();
    creators.add("Zilahatou B. Tohon");
    creators.add("Halima B. Mainassara");
    creators.add("Amadou Garba");
    creators.add("Ali E. Mahamane");
    creators.add("Elisa Bosqué-Oliva");
    creators.add("Maman-Laminou Ibrahim");
    creators.add("Jean-Bernard Duchemin");
    creators.add("Suzanne Chanteau");
    creators.add("Pascal Boisier");

    final String rights = "Tohon et al. This is an open-access article distributed under the terms of the Creative" +
        " Commons Attribution License, which permits unrestricted use, distribution, and reproduction in any medium," +
        " provided the original author and source are credited.";

    List<ObjectInfo> parts = new LinkedList<ObjectInfo>();
    ObjectInfo part1 = new ObjectInfo();
    part1.setId(URI.create("info:doi/10.1371/journal.pntd.0000241.t001"));
    part1.seteIssn("1935-2735");
    part1.setIsPartOf(isPartOf);
    part1.setContextElement("table-wrap");
    DublinCore dublinCore1 = new DublinCore();
    dublinCore1.setIdentifier("info:doi/10.1371/journal.pntd.0000241.t001");
    dublinCore1.setDate(dateFormatter.parse("2008-05-28 00:00:00"));
    dublinCore1.setRights(rights);
    dublinCore1.setType(URI.create("http://purl.org/dc/dcmitype/StillImage"));
    dublinCore1.setTitle("Table 1");
    dublinCore1.setDescription("<title>Prevalence (%) of anaemia according to the absence or to the presence of " +
        "several potential risk factors at the baseline data collection</title>");
    dublinCore1.setCreators(creators);
    part1.setDublinCore(dublinCore1);
    Set<Representation> representations1 = new HashSet<Representation>(4);
    Representation representation1_1 = new Representation();
    representation1_1.setName("TIF");
    representation1_1.setContentType("image/tiff");
    representation1_1.setSize(245880);
    representations1.add(representation1_1);

    Representation representation1_2 = new Representation();
    representation1_2.setName("PNG_S");
    representation1_2.setContentType("image/png");
    representation1_2.setSize(14433);
    representations1.add(representation1_2);

    Representation representation1_3 = new Representation();
    representation1_3.setName("PNG_M");
    representation1_3.setContentType("image/png");
    representation1_3.setSize(95902);
    representations1.add(representation1_3);

    Representation representation1_4 = new Representation();
    representation1_4.setName("PNG_L");
    representation1_4.setContentType("image/png");
    representation1_4.setSize(138172);
    representations1.add(representation1_4);
    part1.setRepresentations(representations1);
    parts.add(part1);


    ObjectInfo part2 = new ObjectInfo();
    part2.setId(URI.create("info:doi/10.1371/journal.pntd.0000241.t002"));
    part2.seteIssn("1935-2735");
    part2.setIsPartOf(isPartOf);
    part2.setContextElement("table-wrap");
    DublinCore dublinCore2 = new DublinCore();
    dublinCore2.setIdentifier("info:doi/10.1371/journal.pntd.0000241.t002");
    dublinCore2.setDate(dateFormatter.parse("2008-05-28 00:00:00"));
    dublinCore2.setRights(rights);
    dublinCore2.setType(URI.create("http://purl.org/dc/dcmitype/StillImage"));
    dublinCore2.setTitle("Table 2");
    dublinCore2.setDescription("<title>Evolution of the prevalence (%) of the main morbidity indicators between " +
        "initial survey and re-assessment one year later (paired analysis)</title>");
    dublinCore2.setCreators(creators);
    part2.setDublinCore(dublinCore2);

    Set<Representation> representations2 = new HashSet<Representation>(4);
    Representation representation2_1 = new Representation();
    representation2_1.setName("TIF");
    representation2_1.setContentType("image/tiff");
    representation2_1.setSize(400768);
    representations2.add(representation2_1);

    Representation representation2_2 = new Representation();
    representation2_2.setName("PNG_S");
    representation2_2.setContentType("image/png");
    representation2_2.setSize(18685);
    representations2.add(representation2_2);

    Representation representation2_3 = new Representation();
    representation2_3.setName("PNG_M");
    representation2_3.setContentType("image/png");
    representation2_3.setSize(161218);
    representations2.add(representation2_3);

    Representation representation2_4 = new Representation();
    representation2_4.setName("PNG_L");
    representation2_4.setContentType("image/png");
    representation2_4.setSize(227830);
    representations2.add(representation2_4);

    part2.setRepresentations(representations2);

    parts.add(part2);
    return new Object[][]{
        {archive, parts}
    };
  }

  private static DublinCore getExpectedDublinCore() throws ParseException {
    DublinCore dublinCore = new DublinCore();
    dublinCore.setIdentifier("info:doi/10.1371/journal.pmed.0050082");
    dublinCore.setTitle("Exposure to War as a Risk Factor for Mental Disorders");
    dublinCore.setType(URI.create("http://purl.org/dc/dcmitype/Text"));
    dublinCore.setFormat("text/xml");
    dublinCore.setLanguage("en");
    dublinCore.setPublisher("Public Library of Science");
    dublinCore.setDate(dateFormatter.parse("2008-04-01 00:00:00"));
    dublinCore.setIssued(dateFormatter.parse("2008-04-01 00:00:00"));
    dublinCore.setAvailable(dateFormatter.parse("2008-04-01 00:00:00"));
    dublinCore.setRights("Benedek and Ursano. This is an open-access article distributed under the terms of the " +
        "Creative Commons Attribution License, which permits unrestricted use, distribution, and reproduction in any " +
        "medium, provided the original author and source are credited.");
    dublinCore.setDescription("<p>The authors discuss a new study on the prevalence of mental disorders in Lebanon.</p>");
    //creators
    Set<String> creators = new HashSet<String>(2);
    creators.add("David M Benedek");
    creators.add("Robert J Ursano");
    creators.add("Test Collab Author");
    creators.add("Test Collab Author2");
    dublinCore.setCreators(creators);
    //subjects
    Set<String> subjects = new HashSet<String>(2);
    subjects.add("Mental Health");
    subjects.add("Public Health and Epidemiology");
    dublinCore.setSubjects(subjects);
    dublinCore.setConformsTo(URI.create("http://dtd.nlm.nih.gov/publishing/2.0/journalpublishing.dtd"));
    dublinCore.setCopyrightYear(2008);

    Citation citation = new Citation();
    citation.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation.setYear(2008);
    citation.setDisplayYear("2008");
    citation.setMonth("04");
    citation.setDay("01");
    citation.setVolume("5");
    citation.setVolumeNumber(5);
    citation.setIssue("4");
    citation.setTitle("Exposure to War as a Risk Factor for Mental Disorders");
    citation.setPublisherLocation("San Francisco, USA");
    citation.setPublisherName("Public Library of Science");
    citation.setPages("1-3");
    citation.setELocationId("e82");
    citation.setJournal("PLoS Med");
    citation.setNote("David M. Benedek is Associate Professor of Psychiatry, Senior Scientist and Associate Director," +
        " and Robert J. Ursano is Professor of Psychiatry and Neuroscience and Director at the Center for " +
        "the Study of Traumatic Stress, Department of Psychiatry, Uniformed Services University, Bethesda, " +
        "Maryland, United States of America.");
    citation.setSummary("<p>The authors discuss a new study on the prevalence of mental disorders in Lebanon.</p>");
    citation.setUrl("http://dx.doi.org/10.1371%2Fjournal.pmed.0050082");
    citation.setDoi("10.1371/journal.pmed.0050082");
    List<String> collabAuthors = new ArrayList<String>(2);
    collabAuthors.add("Test Collab Author");
    collabAuthors.add("Test Collab Author2");
    citation.setCollaborativeAuthors(collabAuthors);

    dublinCore.setBibliographicCitation(citation);
    dublinCore.setReferences(getExpectedReferences());

    return dublinCore;
  }

  private static List<Citation> getExpectedReferences() {
    List<Citation> references = new ArrayList<Citation>(18);

    Citation citation = new Citation();
    citation.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation.setKey("1");
    citation.setYear(1991);
    citation.setDisplayYear("1991");
    citation.setTitle("Psychiatric disorders in America: The epidemiologic catchment area study");
    citation.setPublisherLocation("New York");
    citation.setPublisherName("Free Press");
    citation.setNote("editors");
    references.add(citation);


    Citation citation2 = new Citation();
    citation2.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation2.setKey("2");
    citation2.setYear(2000);
    citation2.setDisplayYear("2000");
    citation2.setVolume("34");
    citation2.setVolumeNumber(34);
    citation2.setTitle("Australia's mental health: An overview of the general population survey.");
    citation2.setPages("197-205");
    citation2.setELocationId("197");
    citation2.setJournal("Aust NZ J Psychiatry");
    references.add(citation2);


    Citation citation3 = new Citation();
    citation3.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation3.setKey("3");
    citation3.setYear(1998);
    citation3.setDisplayYear("1998");
    citation3.setVolume("33");
    citation3.setVolumeNumber(33);
    citation3.setTitle("Prevalence of psychiatric disorder in the general population: Results of the Netherlands Mental Health Survey and Incidences Study (NEMESIS).");
    citation3.setPages("587-595");
    citation3.setELocationId("587");
    citation3.setJournal("Soc Psychiatry Epidemiol");
    references.add(citation3);


    Citation citation4 = new Citation();
    citation4.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation4.setKey("4");
    citation4.setYear(2001);
    citation4.setDisplayYear("2001");
    citation4.setVolume("36");
    citation4.setVolumeNumber(36);
    citation4.setTitle("Al Ain Community Psychiatry Survey I. Prevalence and socio-demographic correlates.");
    citation4.setPages("20-28");
    citation4.setELocationId("20");
    citation4.setJournal("Soc Psychiatr Epidemiol");

    references.add(citation4);

    Citation citation5 = new Citation();
    citation5.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation5.setKey("5");
    citation5.setYear(1989);
    citation5.setDisplayYear("1989");
    citation5.setVolume("155");
    citation5.setVolumeNumber(155);
    citation5.setTitle("Epidemiology of mental disorders in young adults of a newly urbanized area in Khartoum, Sudan.");
    citation5.setPages("44-47");
    citation5.setELocationId("44");
    citation5.setJournal("Br J Psychiatry");

    references.add(citation5);

    Citation citation6 = new Citation();
    citation6.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation6.setKey("6");
    citation6.setYear(1998);
    citation6.setDisplayYear("1998");
    citation6.setVolume("248");
    citation6.setVolumeNumber(248);
    citation6.setTitle("Major depression and external stressors: The Lebanon War.");
    citation6.setPages("225-230");
    citation6.setELocationId("225");
    citation6.setJournal("Eur Arch Psychiatry Clin Neurosci");

    references.add(citation6);

    Citation citation7 = new Citation();
    citation7.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation7.setKey("7");
    citation7.setYear(2002);
    citation7.setDisplayYear("2002");
    citation7.setVolume("92");
    citation7.setVolumeNumber(92);
    citation7.setTitle("Population attributable fractions of psychiatric disorders and behavioral outcomes associated with combat exposures among U.S. men.");
    citation7.setPages("59-63");
    citation7.setELocationId("59");
    citation7.setJournal("Am J Public Health");

    references.add(citation7);

    Citation citation8 = new Citation();
    citation8.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation8.setKey("8");
    citation8.setYear(2004);
    citation8.setDisplayYear("2004");
    citation8.setVolume("351");
    citation8.setVolumeNumber(351);
    citation8.setTitle("Combat duty in Iraq and Afghanistan: Mental health problems and barriers to care.");
    citation8.setPages("13-22");
    citation8.setELocationId("13");
    citation8.setJournal("N Engl J Med");

    references.add(citation8);

    Citation citation9 = new Citation();
    citation9.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation9.setKey("9");
    citation9.setYear(1980);
    citation9.setDisplayYear("1980");
    citation9.setTitle("Diagnostic and statistical manual");
    citation9.setPublisherLocation("Washington (D. C.)");
    citation9.setPublisherName("American Psychiatric Press");

    references.add(citation9);

    Citation citation10 = new Citation();
    citation10.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation10.setKey("10");
    citation10.setYear(1981);
    citation10.setDisplayYear("1981");
    citation10.setVolume("38");
    citation10.setVolumeNumber(38);
    citation10.setTitle("National Institute of Mental Health diagnostic interview schedule: Its history, characteristics and validity.");
    citation10.setPages("381-389");
    citation10.setELocationId("381");
    citation10.setJournal("Arch Gen Psychiatry");

    references.add(citation10);

    Citation citation11 = new Citation();
    citation11.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation11.setKey("11");
    citation11.setYear(2004);
    citation11.setDisplayYear("2004");
    citation11.setVolume("13");
    citation11.setVolumeNumber(13);
    citation11.setTitle("The World Mental Health (WMH) survey initiative version of the World Health Organization (WHO) Composite International Diagnostic Interview (CIDI).");
    citation11.setPages("95-121");
    citation11.setELocationId("95");
    citation11.setJournal("Int J Methods Psychiatr Res");

    references.add(citation11);

    Citation citation12 = new Citation();
    citation12.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation12.setKey("12");
    citation12.setYear(2007);
    citation12.setDisplayYear("2007");
    citation12.setTitle("Individual and community responses to disasters.");
    citation12.setPublisherLocation("Cambridge");
    citation12.setPublisherName("Cambridge University Press");
    citation12.setPages("3-26");
    citation12.setELocationId("3");
    citation12.setNote("In");

    List<UserProfile> editors1 = new ArrayList<UserProfile>(4);
    UserProfile editor1_1 = new UserProfile();
    editor1_1.setRealName("RJ Ursano");
    editor1_1.setGivenNames("RJ");
    editor1_1.setSurnames("Ursano");
    editors1.add(editor1_1);

    UserProfile editor1_2 = new UserProfile();
    editor1_2.setRealName("CS Fullerton");
    editor1_2.setGivenNames("CS");
    editor1_2.setSurnames("Fullerton");
    editors1.add(editor1_2);
    UserProfile editor1_3 = new UserProfile();
    editor1_3.setRealName("L Weisaeth");
    editor1_3.setGivenNames("L");
    editor1_3.setSurnames("Weisaeth");
    editors1.add(editor1_3);

    UserProfile editor1_4 = new UserProfile();
    editor1_4.setRealName("B Raphael");
    editor1_4.setGivenNames("B");
    editor1_4.setSurnames("Raphael");
    editors1.add(editor1_4);

    citation12.setEditors(editors1);

    references.add(citation12);

    Citation citation13 = new Citation();
    citation13.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation13.setKey("13");
    citation13.setYear(2007);
    citation13.setDisplayYear("2007");
    citation13.setTitle("Armed conflicts report.");
    citation13.setNote("Available: http://www.ploughshares.ca/libraries/ACRText/ACR-TitlePageRev.htm. Accessed 29 February 2008");

    references.add(citation13);

    Citation citation14 = new Citation();
    citation14.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation14.setKey("14");
    citation14.setYear(2003);
    citation14.setDisplayYear("2003");
    citation14.setTitle("Preparing for the psychological consequences of terrorism: A public health strategy.");
    citation14.setNote("Available: http://www.nap.edu/catalog.php?record_id=10717. Accessed 29 February 2008");

    references.add(citation14);

    Citation citation15 = new Citation();
    citation15.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation15.setKey("15");
    citation15.setYear(2007);
    citation15.setDisplayYear("2007a");
    citation15.setTitle("Public health and disaster mental health: Preparing, responding and recovering.");
    citation15.setPublisherLocation("Cambridge");
    citation15.setPublisherName("Cambridge University Press");
    citation15.setPages("311-326");
    citation15.setELocationId("311");
    citation15.setNote("In");

    List<UserProfile> editors2 = new ArrayList<UserProfile>();
    UserProfile editor2_1 = new UserProfile();
    editor2_1.setRealName("RJ Ursano");
    editor2_1.setGivenNames("RJ");
    editor2_1.setSurnames("Ursano");
    editors2.add(editor2_1);

    UserProfile editor2_2 = new UserProfile();
    editor2_2.setRealName("CS Fullerton");
    editor2_2.setGivenNames("CS");
    editor2_2.setSurnames("Fullerton");
    editors2.add(editor2_2);

    UserProfile editor2_3 = new UserProfile();
    editor2_3.setRealName("L Weisaeth");
    editor2_3.setGivenNames("L");
    editor2_3.setSurnames("Weisaeth");
    editors2.add(editor2_3);

    UserProfile editor2_4 = new UserProfile();
    editor2_4.setRealName("B Raphael");
    editor2_4.setGivenNames("B");
    editor2_4.setSurnames("Raphael");
    editors2.add(editor2_4);

    citation15.setEditors(editors2);

    references.add(citation15);

    Citation citation16 = new Citation();
    citation16.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation16.setKey("16");
    citation16.setYear(2008);
    citation16.setDisplayYear("2008");
    citation16.setVolume("5");
    citation16.setVolumeNumber(5);
    citation16.setTitle("Lifetime prevalence of mental disorders in Lebanon: First onset, treatment, and exposure to war.");
    citation16.setPages("e61");
    citation16.setELocationId("e61");
    citation16.setJournal("PLoS Med");
    citation16.setNote("doi:10.1371/journal.pmed.0050061");

    references.add(citation16);

    Citation citation17 = new Citation();
    citation17.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Article");
    citation17.setKey("17");
    citation17.setYear(2007);
    citation17.setDisplayYear("2007");
    citation17.setVolume("6");
    citation17.setVolumeNumber(6);
    citation17.setTitle("Lifetime prevalence and age-of-onset distributions of mental disorders in the World Health Organization's World Mental Health Survey.");
    citation17.setPages("168-176");
    citation17.setELocationId("168");
    citation17.setJournal("Initiative World Psychiatry");

    references.add(citation17);

    Citation citation18 = new Citation();
    citation18.setCitationType("http://purl.org/net/nknouf/ns/bibtex#Misc");
    citation18.setKey("18");
    citation18.setYear(2001);
    citation18.setDisplayYear("2001");
    citation18.setTitle("Global health atlas.");
    citation18.setNote("Available: http://www.who.int/globalatlas/DataQuery/default.asp. Accessed 29 February 2008");
    references.add(citation18);

    return references;
  }

  private static Set<Representation> getSampleRepresentations() {
    Set<Representation> representations = new HashSet<Representation>(2);

    Representation xml = new TextRepresentation();
    xml.setName("XML");
    xml.setContentType("text/xml");
    xml.setSize(31799);
    representations.add(xml);

    Representation pdf = new Representation();
    pdf.setName("PDF");
    pdf.setContentType("application/pdf");
    pdf.setSize(91325);
    representations.add(pdf);

    return representations;
  }

}
