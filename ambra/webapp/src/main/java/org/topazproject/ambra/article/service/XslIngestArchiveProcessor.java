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

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.article.ArchiveProcessException;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.util.XPathUtil;
import org.topazproject.xml.transform.EntityResolvingSource;
import org.topazproject.xml.transform.cache.CachedSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * {@link IngestArchiveProcessor} that uses an xsl stylesheet to format the article xml into an easy to parse xml
 *
 * @author Alex Kudlick Date: 6/20/11
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
public class XslIngestArchiveProcessor implements IngestArchiveProcessor {
  private static final Logger log = LoggerFactory.getLogger(XslIngestArchiveProcessor.class);

  private DocumentBuilder documentBuilder;
  private TransformerFactory transformerFactory;
  private String xslStyleSheet;
  private Configuration configuration;
  private XPathUtil xPathUtil;


  public XslIngestArchiveProcessor() {
    transformerFactory = new TransformerFactoryImpl();
    transformerFactory.setURIResolver(new URLResolver());
    transformerFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
    transformerFactory.setAttribute("http://saxon.sf.net/feature/strip-whitespace", "none");
    transformerFactory.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing a stylesheet", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing a stylesheet", te);
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing a stylesheet", te);
      }
    });
    xPathUtil = new XPathUtil();
  }

  private static class URLResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
      if (href.length() == 0)
        return null;  // URL doesn't handle this case properly, so let default resolver handle it

      try {
        URL url = new URL(new URL(base), href);
        return new StreamSource(url.toString());
      } catch (MalformedURLException mue) {
        log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
            "default URIResolver", mue);
        return null;
      }
    }
  }

  /**
   * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
   */
  private static class ZipURIResolver extends URLResolver {
    private final ZipFile zip;

    public ZipURIResolver(ZipFile zip) {
      this.zip = zip;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      if (log.isDebugEnabled())
        log.debug("resolving: base='" + base + "', href='" + href + "'");

      if (!base.startsWith("zip:"))
        return super.resolve(href, base);

      try {
        InputSource src = resolveToIS(base, href);
        if (src == null)
          return null;

        return new EntityResolvingSource(src, new EntityResolver() {
          public InputSource resolveEntity(String publicId, String systemId)
              throws SAXException, IOException {
            if (systemId != null && systemId.startsWith("zip:"))
              return resolveToIS("zip:/", systemId);
            return CachedSource.getResolver().resolveEntity(publicId, systemId);
          }
        });
      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      } catch (SAXException se) {
        throw new TransformerException(se);
      }
    }

    private InputSource resolveToIS(String base, String rel) throws IOException {
      URI uri = URI.create(base).resolve(rel);
      InputStream is = zip.getInputStream(zip.getEntry(uri.getPath().substring(1)));
      if (is == null)         // hack to deal with broken AP zip's that contain absolute paths
        is = zip.getInputStream(zip.getEntry(uri.getPath()));

      if (log.isDebugEnabled())
        log.debug("resolved: uri='" + uri + "', found=" + (is != null));

      if (is == null)
        return null;

      InputSource src = new InputSource(is);
      src.setSystemId(uri.toString());

      return src;
    }
  }

  /**
   * Set the xsl style sheet to use
   *
   * @param xslStyleSheet - The classpath-relative location of an xsl stylesheet to use to process the article xml
   */
  @Required
  public void setXslStyleSheet(String xslStyleSheet) {
    this.xslStyleSheet = xslStyleSheet;
  }

  /**
   * Set the document builder to use for constructing documents from the zip file entries
   *
   * @param documentBuilder - the document builder to use
   */
  @Required
  public void setDocumentBuilder(DocumentBuilder documentBuilder) {
    this.documentBuilder = documentBuilder;
  }

  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Article processArticle(ZipFile archive, Document articleXml) throws ArchiveProcessException {
    InputStream xsl =  null;
    try {
      String zipInfo = describeZip(archive);
      xsl = getClass().getClassLoader().getResourceAsStream(xslStyleSheet);
      if (xsl == null) {
        throw new ArchiveProcessException("Couldn't open stylesheet: " + xslStyleSheet);
      }
      Document transformedXml = transformZip(archive, zipInfo, xsl,
          configuration.getString("ambra.platform.doiUrlPrefix", null));

      Article article = parseTransformedXml(transformedXml);
      String archiveName = archive.getName().contains(File.separator)
          ? archive.getName().substring(archive.getName().lastIndexOf(File.separator) + 1)
          : archive.getName();
      article.setArchiveName(archiveName);
      return article;
    } catch (IOException e) {
      throw new ArchiveProcessException("Error reading from Zip archive", e);
    } catch (TransformerException e) {
      throw new ArchiveProcessException("Error transforming Article xml", e);
    } catch (XPathExpressionException e) {
      throw new ArchiveProcessException("Error parsing transformed xml", e);
    } catch (ParseException e) {
      throw new ArchiveProcessException("Error parsing dates from transformed xml", e);
    } finally {
      if (xsl != null) {
        try {
          xsl.close();
        } catch (IOException e) {
          log.warn("Error closing input stream for xsl stylesheet");
        }
      }
    }
  }

  /**
   * TODO: use an xml unmarshaller to do this - see <a href="http://static.springsource.org/spring-ws/site/reference/html/oxm.html">this
   * TODO: page</a> for some spring-wrapped versions (mmmmm... spring).  For this it would behoove us to reformat the transformed xml
   * TODO: to match the object model
   *
   * @param transformedXml the result of the xsl transform on the article xml
   * @return a fully-populated, unsaved article object
   * @throws XPathExpressionException if there's an error parsing the xml
   * @throws ParseException           if there's an error parsing dates
   */
  private Article parseTransformedXml(Document transformedXml) throws XPathExpressionException, ParseException {
    Article article = new Article();
    article.setId(URI.create(xPathUtil.evaluate(transformedXml, "//Article/@id")));
    article.setState(Integer.valueOf(xPathUtil.evaluate(transformedXml,"//Article/state/text()")));

    NodeList articleTypeNodes = xPathUtil.selectNodes(transformedXml, "//Article/articleType/text()");
    Set<URI> articleTypes = new HashSet<URI>(articleTypeNodes.getLength());
    for (int i = 0; i < articleTypeNodes.getLength(); i++) {
      articleTypes.add(URI.create(articleTypeNodes.item(i).getNodeValue()));
    }
    article.setArticleType(articleTypes);
    article.seteIssn(xPathUtil.evaluate(transformedXml, "//Article/eIssn"));

    article.setDublinCore(parseDublinCore(transformedXml, "//Article/dublinCore"));

    int categoryCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/categories)"));
    Set<Category> categories = new HashSet<Category>(categoryCount);
    Set<String> storedCategories = new HashSet<String>(categoryCount); //make sure we don't store duplicate main/sub categories
    for (int i = 1; i <= categoryCount; i++) {
      Category category = new Category();
      String mainCategory = xPathUtil.evaluate(transformedXml, "//Article/categories[" + i + "]/mainCategory/text()");
      String subCategory = xPathUtil.evaluate(transformedXml, "//Article/categories[" + i + "]/subCategory/text()");
      if (!storedCategories.contains(mainCategory + subCategory)) {
        if (!mainCategory.isEmpty()) {
          category.setMainCategory(mainCategory);
        }
        if (!subCategory.isEmpty()) {
          category.setSubCategory(subCategory);
        }
        categories.add(category);
        storedCategories.add(mainCategory + subCategory);
      }
    }
    article.setCategories(categories);

    Set<Representation> representations = parseRepresentations(transformedXml, "//Article/representations", article);
    article.setRepresentations(representations);

    int partCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/parts)"));
    List<ObjectInfo> parts = new ArrayList<ObjectInfo>(partCount);
    for (int i = 1; i <= partCount; i++) {
      ObjectInfo part = new ObjectInfo();
      part.setId(URI.create(xPathUtil.evaluate(transformedXml, "//Article/parts[" + i + "]/@id")));
      part.setDublinCore(parseDublinCore(transformedXml, "//Article/parts[" + i + "]/dublinCore"));
      part.setRepresentations(parseRepresentations(transformedXml, "//Article/parts[" + i + "]/representations", part));
      part.seteIssn(xPathUtil.evaluate(transformedXml, "//Article/parts[" + i + "]/eIssn/text()"));
      part.setContextElement(xPathUtil.evaluate(transformedXml, "//Article/parts[" + i + "]/contextElement/text()"));
      part.setIsPartOf(article);
      parts.add(part);
    }
    article.setParts(parts);

    int relatedArticleCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/relatedArticles)"));
    Set<RelatedArticle> relatedArticles = new HashSet<RelatedArticle>(relatedArticleCount);
    for (int i = 1; i <= relatedArticleCount; i++) {
      RelatedArticle relatedArticle = new RelatedArticle();
      relatedArticle.setArticle(URI.create(xPathUtil.evaluate(transformedXml, "//Article/relatedArticles[" + i + "]/article/text()")));
      relatedArticle.setRelationType(xPathUtil.evaluate(transformedXml, "//Article/relatedArticles[" + i + "]/relationType/text()"));
      relatedArticles.add(relatedArticle);
    }
    article.setRelatedArticles(relatedArticles);
    article.setAuthors(parseArticleContributors(transformedXml, "authors"));
    article.setEditors(parseArticleContributors(transformedXml, "editors"));

    return article;
  }

  private List<ArticleContributor> parseArticleContributors(Document transformedXml, String type) throws XPathExpressionException {
    String nodeXpath = "//Article/dublinCore/bibliographicCitation/" + type;
    int authorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + ")"));
    List<ArticleContributor> contributors = new ArrayList<ArticleContributor>(authorCount);
    boolean isAuthor = type.equals("authors");
    for (int i = 1; i <= authorCount; i++) {
      ArticleContributor author = new ArticleContributor();
      author.setFullName(xPathUtil.evaluate(transformedXml, nodeXpath + "[" + i + "]/realName/text()"));
      author.setGivenNames(xPathUtil.evaluate(transformedXml, nodeXpath + "[" + i + "]/givenNames/text()"));
      author.setSurnames(xPathUtil.evaluate(transformedXml, nodeXpath + "[" + i + "]/surnames/text()"));
      author.setSuffix(xPathUtil.evaluate(transformedXml, nodeXpath + "[" + i + "]/suffix/text()"));
      author.setIsAuthor(isAuthor);
      contributors.add(author);
    }
    return contributors;
  }

  private Set<Representation> parseRepresentations(Document transformedXml, String baseXpath, ObjectInfo objectInfo) throws XPathExpressionException {
    int count = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + baseXpath + ")"));
    Set<Representation> representations = new HashSet<Representation>(count);
    for (int i = 1; i <= count; i++) {
      Representation representation = new Representation();
      representation.setName(xPathUtil.evaluate(transformedXml, baseXpath + "[" + i + "]/name/text()"));
      representation.setContentType(xPathUtil.evaluate(transformedXml, baseXpath + "[" + i + "]/contentType/text()"));
      representation.setSize(Long.valueOf(xPathUtil.evaluate(transformedXml, baseXpath + "[" + i + "]/size/text()")));
      representation.setObject(objectInfo);
      representations.add(representation);
    }
    return representations;
  }

  private DublinCore parseDublinCore(Document transformedXml, String nodeXPath) throws XPathExpressionException, ParseException {
    DublinCore dublinCore = new DublinCore();
    dublinCore.setIdentifier(xPathUtil.evaluate(transformedXml, nodeXPath + "/identifier/text()"));
    dublinCore.setType(URI.create(xPathUtil.evaluate(transformedXml, nodeXPath + "/type/text()")));
    if (xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/title") != null) {
      dublinCore.setTitle(getAllText(xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/title")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/format/text()").isEmpty()) {
      dublinCore.setFormat(xPathUtil.evaluate(transformedXml, nodeXPath + "/format/text()"));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/language/text()").isEmpty()) {
      dublinCore.setLanguage(xPathUtil.evaluate(transformedXml, nodeXPath + "/language/text()"));
    }

    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/date/text()").isEmpty()) {
      dublinCore.setDate(dateFormatter.parse(xPathUtil.evaluate(transformedXml, nodeXPath + "/date/text()").replaceAll(" UTC", "")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/issued/text()").isEmpty()) {
      dublinCore.setIssued(dateFormatter.parse(xPathUtil.evaluate(transformedXml, nodeXPath + "/issued/text()").replaceAll(" UTC", "")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/available/text()").isEmpty()) {
      dublinCore.setAvailable(dateFormatter.parse(xPathUtil.evaluate(transformedXml, nodeXPath + "/available/text()").replaceAll(" UTC", "")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/submitted/text()").isEmpty()) {
      dublinCore.setSubmitted(dateFormatter.parse(xPathUtil.evaluate(transformedXml, nodeXPath + "/submitted/text()").replaceAll(" UTC", "")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/accepted/text()").isEmpty()) {
      dublinCore.setAccepted(dateFormatter.parse(xPathUtil.evaluate(transformedXml, nodeXPath + "/accepted/text()").replaceAll(" UTC", "")));
    }
    NodeList creatorNodes = xPathUtil.selectNodes(transformedXml, nodeXPath + "/creators/text()");
    Set<String> creators = new HashSet<String>(creatorNodes.getLength());
    for (int i = 0; i < creatorNodes.getLength(); i++) {
      creators.add(creatorNodes.item(i).getNodeValue());
    }
    dublinCore.setCreators(creators);

    int subjectCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXPath + "/subjects)"));
    Set<String> subjects = new HashSet<String>(subjectCount);
    for (int i = 1; i <= subjectCount; i++) {
      subjects.add(getAllText(xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/subjects[" + i + "]")));
    }
    dublinCore.setSubjects(subjects);

    if (xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/description") != null) {
      dublinCore.setDescription(getAllText(xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/description")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/publisher/text()").isEmpty()) {
      dublinCore.setPublisher(xPathUtil.evaluate(transformedXml, nodeXPath + "/publisher/text()"));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/rights/text()").isEmpty()) {
      dublinCore.setRights(xPathUtil.evaluate(transformedXml, nodeXPath + "/rights/text()"));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/conformsTo/text()").isEmpty()) {
      dublinCore.setConformsTo(URI.create(xPathUtil.evaluate(transformedXml, nodeXPath + "/conformsTo/text()")));
    }
    if (!xPathUtil.evaluate(transformedXml, nodeXPath + "/copyrightYear/text()").isEmpty()) {
      dublinCore.setCopyrightYear(Integer.valueOf(xPathUtil.evaluate(transformedXml, nodeXPath + "/copyrightYear/text()")));
    }

    if (xPathUtil.selectSingleNode(transformedXml, nodeXPath + "/bibliographicCitation") != null) {
      dublinCore.setBibliographicCitation(
          parseCitation(transformedXml, nodeXPath + "/bibliographicCitation", true));
    }

    int referenceCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXPath + "/references)"));
    List<Citation> references = new ArrayList<Citation>(referenceCount);
    for (int i = 1; i <= referenceCount; i++) {
      references.add(parseCitation(transformedXml, nodeXPath + "/references[" + i + "]", false));
    }
    dublinCore.setReferences(references);

    return dublinCore;
  }

  private Citation parseCitation(Document transformedXml, String nodeXpath, boolean isBibCitation) throws XPathExpressionException {
    Citation citation = new Citation();
    String type = xPathUtil.evaluate(transformedXml, nodeXpath + "/citationType/text()");
    if (!type.isEmpty()) {
      citation.setCitationType(type);
    }
    String year = xPathUtil.evaluate(transformedXml, nodeXpath + "/year/text()");
    if (!year.isEmpty()) {
      citation.setYear(Integer.valueOf(year));
    }
    String displayYear = xPathUtil.evaluate(transformedXml, nodeXpath + "/displayYear/text()");
    if (!displayYear.isEmpty()) {
      citation.setDisplayYear(displayYear);
    }
    String month = xPathUtil.evaluate(transformedXml, nodeXpath + "/month/text()");
    if (!month.isEmpty()) {
      citation.setMonth(month);
    }
    String day = xPathUtil.evaluate(transformedXml, nodeXpath + "/day/text()");
    if (!day.isEmpty()) {
      citation.setDay(day);
    }
    String volume = xPathUtil.evaluate(transformedXml, nodeXpath + "/volume/text()");
    if (!volume.isEmpty()) {
      citation.setVolume(volume);
    }
    String volumeNumber = xPathUtil.evaluate(transformedXml, nodeXpath + "/volumeNumber/text()");
    if (!volumeNumber.isEmpty()) {
      citation.setVolumeNumber(Integer.valueOf(volumeNumber));
    }
    String publisherLocation = xPathUtil.evaluate(transformedXml, nodeXpath + "/publisherLocation/text()");
    if (!publisherLocation.isEmpty()) {
      citation.setPublisherLocation(publisherLocation);
    }
    String publisherName = xPathUtil.evaluate(transformedXml, nodeXpath + "/publisherName/text()");
    if (!publisherName.isEmpty()) {
      citation.setPublisherName(publisherName);
    }
    String pages = xPathUtil.evaluate(transformedXml, nodeXpath + "/pages/text()");
    if (!pages.isEmpty()) {
      citation.setPages(pages);
    }
    String eLocationId = xPathUtil.evaluate(transformedXml, nodeXpath + "/eLocationId/text()");
    if (!eLocationId.isEmpty()) {
      citation.setELocationId(eLocationId);
    }
    String journal = xPathUtil.evaluate(transformedXml, nodeXpath + "/journal/text()");
    if (!journal.isEmpty()) {
      citation.setJournal(journal);
    }
    String issue = xPathUtil.evaluate(transformedXml, nodeXpath + "/issue/text()");
    if (!issue.isEmpty()) {
      citation.setIssue(issue);
    }
    String key = xPathUtil.evaluate(transformedXml, nodeXpath + "/key/text()");
    if (!key.isEmpty()) {
      citation.setKey(key);
    }
    String url = xPathUtil.evaluate(transformedXml, nodeXpath + "/url/text()");
    if (!url.isEmpty()) {
      citation.setUrl(url);
    }
    String doi = xPathUtil.evaluate(transformedXml, nodeXpath + "/doi/text()");
    if (!doi.isEmpty()) {
      citation.setDoi(doi);
    }

    int collabAuthorCount = Integer.valueOf(
        xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/collaborativeAuthors)"));
    List<String> collabAuthors = new ArrayList<String>(collabAuthorCount);
    for (int i = 1; i <= collabAuthorCount; i++) {
      collabAuthors.add(xPathUtil.evaluate(transformedXml, nodeXpath + "/collaborativeAuthors[" + i + "]/text()"));
    }
    citation.setCollaborativeAuthors(collabAuthors);

    Node noteNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/note");
    if (noteNode != null) {
      citation.setNote(getAllText(noteNode));
    }
    Node titleNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/title");
    if (titleNode != null) {
      citation.setTitle(getAllText(titleNode));
    }
    Node summaryNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/summary");
    if (summaryNode != null) {
      citation.setSummary(getAllText(summaryNode));
    }

    if (!isBibCitation) {
      //Set the people referenced by the article in this citation
      int authorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/authors)"));
      int editorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/editors)"));
      List<CitedPerson> authors = new ArrayList<CitedPerson>(authorCount);
      List<CitedPerson> editors = new ArrayList<CitedPerson>(editorCount);
      for (int i = 1; i <= authorCount; i++) {
        CitedPerson author = new CitedPerson();
        author.setFullName(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + i + "]/realName/text()"));
        author.setGivenNames(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + i + "]/givenNames/text()"));
        author.setSurnames(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + i + "]/surnames/text()"));
        author.setIsAuthor(true);
        authors.add(author);
      }
      for (int i = 1; i <= editorCount; i++) {
        CitedPerson editor = new CitedPerson();
        editor.setFullName(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + i + "]/realName/text()"));
        editor.setGivenNames(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + i + "]/givenNames/text()"));
        editor.setSurnames(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + i + "]/surnames/text()"));
        editor.setIsAuthor(false);
        editors.add(editor);
      }
      citation.setReferencedArticleAuthors(authors);
      citation.setReferencedArticleEditors(editors);
    }
    return citation;
  }

  /**
   * Helper method to get all the text of child nodes of a given node
   *
   * @param node - the node to use as base
   * @return - all nested text in the node
   */
  private String getAllText(Node node) {

    String text = "";
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (Node.TEXT_NODE == childNode.getNodeType()) {
        text += childNode.getNodeValue();
      } else if (Node.ELEMENT_NODE == childNode.getNodeType()) {
        text += "<" + childNode.getNodeName() + ">";
        text += getAllText(childNode);
        text += "</" + childNode.getNodeName() + ">";
      }
    }
    return text.replaceAll("[\n\t]", "").trim();
  }

  /**
   * Run the zip file through the xsl stylesheet
   *
   *
   * @param zip          the zip archive containing the items to ingest
   * @param zipInfo      the document describing the zip archive (adheres to zip.dtd)
   * @param handler      the stylesheet to run on <var>zipInfo</var>; this is the main script
   * @param doiUrlPrefix DOI URL prefix
   * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
   * @throws javax.xml.transform.TransformerException
   *          if an error occurs during the processing
   */
  private Document transformZip(ZipFile zip, String zipInfo, InputStream handler, String doiUrlPrefix)
      throws TransformerException {
    Transformer t = transformerFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setURIResolver(new ZipURIResolver(zip));

    // override the doi url prefix if one is specified in the config
    if (doiUrlPrefix != null)
      t.setParameter("doi-url-prefix", doiUrlPrefix);

    /*
     * Note: it would be preferable (and correct according to latest JAXP specs) to use
     * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
     * Hence we need to use Saxon's API's in order to get at those messages.
     */
    final StringWriter msgs = new StringWriter();
    ((Controller) t).makeMessageEmitter();
    ((Controller) t).getMessageEmitter().setWriter(msgs);
    t.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing zip", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }
    });

    Source inp = new StreamSource(new StringReader(zipInfo), "zip:/");
    DOMResult res = new DOMResult();

    try {
      t.transform(inp, res);
    } catch (TransformerException te) {
      if (msgs.getBuffer().length() > 0)
        throw new TransformerException(msgs.toString(), te);
      else
        throw te;
    }
    if (msgs.getBuffer().length() > 0)
      throw new TransformerException(msgs.toString());

    return (Document) res.getNode();
  }


  @Override
  public Document extractArticleXml(ZipFile archive) throws ArchiveProcessException {
    try {
      Document manifest = extractXml(archive, "MANIFEST.xml");
      String xmlFileName = xPathUtil.evaluate(manifest, "//article/@main-entry");
      return extractXml(archive, xmlFileName);
    } catch (Exception e) {
      throw new ArchiveProcessException("Error extracting article xml from archive: " + archive.getName(), e);
    }
  }

  /**
   * Helper method to extract XML from the zip file
   *
   * @param zipFile  - the zip file containing the file to extract
   * @param fileName - the file to extract
   * @return - the parsed xml file
   * @throws java.io.IOException      - if there's a problem reading from the zip file
   * @throws org.xml.sax.SAXException - if there's a problem parsing the xml
   */
  private Document extractXml(ZipFile zipFile, String fileName) throws IOException, SAXException {
    InputStream inputStream = null;
    try {
      inputStream = zipFile.getInputStream(zipFile.getEntry(fileName));
      return documentBuilder.parse(inputStream);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Error closing zip input stream during ingest processing", e);
        }
      }
    }
  }

  /**
   * Generate a description of the given zip archive.
   *
   * @param zip the zip archive to describe
   * @return the xml doc describing the archive (adheres to zip.dtd)
   * @throws IOException if an exception occurred reading the zip archive
   */
  public static String describeZip(ZipFile zip) throws IOException {
    StringBuilder res = new StringBuilder(500);
    res.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    res.append("<ZipInfo");
    if (zip.getName() != null)
      res.append(" name=\"").append(attrEscape(zip.getName())).append("\"");
    res.append(">\n");

    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements())
      entry2xml(entries.nextElement(), res);

    res.append("</ZipInfo>\n");
    return res.toString();
  }

  /**
   * Generate a description for a single zip-entry.
   *
   * @param ze  the zip entry to describe.
   * @param buf the buffer to place the description into
   */
  private static void entry2xml(ZipEntry ze, StringBuilder buf) {
    buf.append("<ZipEntry name=\"").append(attrEscape(ze.getName())).append("\"");

    if (ze.isDirectory())
      buf.append(" isDirectory=\"true\"");
    if (ze.getCrc() >= 0)
      buf.append(" crc=\"").append(ze.getCrc()).append("\"");
    if (ze.getSize() >= 0)
      buf.append(" size=\"").append(ze.getSize()).append("\"");
    if (ze.getCompressedSize() >= 0)
      buf.append(" compressedSize=\"").append(ze.getCompressedSize()).append("\"");
    if (ze.getTime() >= 0)
      buf.append(" time=\"").append(ze.getTime()).append("\"");

    if (ze.getComment() != null || ze.getExtra() != null) {
      buf.append(">\n");

      if (ze.getComment() != null)
        buf.append("<Comment>").append(xmlEscape(ze.getComment())).append("</Comment>\n");
      if (ze.getExtra() != null)
        buf.append("<Extra>").append(base64Encode(ze.getExtra())).append("</Extra>\n");

      buf.append("</ZipEntry>\n");
    } else {
      buf.append("/>\n");
    }
  }

  private static String xmlEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  private static String attrEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;");
  }

  private static String base64Encode(byte[] data) {
    try {
      return new String(Base64.encodeBase64(data), "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);  // can't happen
    }
  }

  /**
   * This method is useful for debugging, so you can put dom2String(xml) in the watch list of the debugger and see the
   * xml
   *
   * @param dom the xml to turn in to a string
   * @return the xml as a string
   */
  private String dom2String(Node dom) {
    try {
      StringWriter sw = new StringWriter(500);
      Transformer t = transformerFactory.newTransformer();
      t.transform(new DOMSource(dom), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException te) {
      log.error("Error converting dom to string", te);
      return "";
    }
  }

}
