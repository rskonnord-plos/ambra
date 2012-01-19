/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.plos.util.ToolHelper
import org.plos.models.Article
import org.plos.models.Category
import org.plos.models.Citation
import org.plos.models.PLoS
import org.plos.models.UserProfile
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.impl.SessionFactoryImpl
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.xml.transform.cache.CachedSource
import groovy.xml.StreamingMarkupBuilder
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

log = LogFactory.getLog(this.getClass());

// Use ToolHelper (currently in wrong place -- article-util) to patch args
args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'migration [-c config-overrides.xml] -a article-uri')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.a(args:1, 'article (e.g. info:doi/10.1371/journal.pone.nnnnnnn)')
cli.d(args:0, 'dump otm objects')
cli.n(args:0, 'show null entries too')
cli.t(args:0, 'test (dry-run)')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Stash args
DUMP = opt.d
DRYRUN = opt.t
SHOWNULLS = opt.n

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

// Get the article doi from the command line
def doi = opt.a

log.info("$doi started")

// Create the slurper object and add entity resolver (so reading articles are reasonably fast)
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

def fedoraUri = CONF.getString("topaz.services.fedora.base-url") + "get"
def articleUrl = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"

// Ask slurper to read article from fedora
def slurpedArticle = slurper.parse(new URL(articleUrl).getContent())
def articleMeta = slurpedArticle.front.'article-meta'
def journalMeta = slurpedArticle.front.'journal-meta'

// Setup OTM
def factory = new SessionFactoryImpl();
def itql = new ItqlStore(URI.create(CONF.getString("topaz.services.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("topaz.models.articles")), null);
def p = new ModelConfig("profiles", URI.create(CONF.getString("topaz.models.profiles")), null);
factory.setTripleStore(itql)
factory.addModel(ri);
factory.addModel(p);
factory.preload(Article.class);
factory.preload(Category.class);
factory.preload(Citation.class);
factory.preload(UserProfile.class);
def session = factory.openSession();
def tx = session.beginTransaction();

// Citation Map -> Map NLM Citation type to RDF types
def citationMap = [ 'book':PLoS.bibtex + "Book",
                    'commun':PLoS.PLoS_CitationTypes + "Informal",
                    'confproc':PLoS.bibtex + "Conference",
                    'discussion':PLoS.PLoS_CitationTypes + "Discussion",
                    'gov':PLoS.PLoS_CitationTypes + "Government",
                    'journal':PLoS.bibtex + "Article",
                    'list':PLoS.PLoS_CitationTypes + "List",
                    'other':PLoS.bibtex + "Misc",
                    'patent':PLoS.PLoS_CitationTypes + "Patent",
                    'thesis':PLoS.PLoS_CitationTypes + "Thesis",
                    'web':PLoS.PLoS_CitationTypes + "Web" ]

// Read article from mulgara
def article = session.get(Article.class, doi)

String getName(name) {
  return ((name.@'name-style' == "eastern") 
           ? "${name.surname} ${name.'given-names'}"
           : "${name.'given-names'} ${name.surname}").toString()
}

String tostr(value) {
  String s = value?.toString()
  if (s != null && s.size() == 0)
    s = null
  return s
}

def toint(value) {
  try {
    return tostr(value)?.toInteger()
  } catch (Exception e) {
    log.warn("Unexpected $e")
    return null
  }
}

// Try to extract an integer or return null
def findInt(value, fieldName, minLength) {
  try {
    def orig = tostr(value)
    if (!orig) return null
    def started = false
    def extracted = ""
    // this could/should be a regex, but this has been tested...
    for (c in orig) {
      if (c >= '0' && c <= '9') {
        extracted += c
        started = true
      } else {
        if (started) {
          if (extracted && extracted.size() < minLength) {
            extracted = ""
            started = false
          } else if (extracted) {
            break
          }
        }
      }
    }
    if (!extracted) {
      log.warn("Expected int for $fieldName, got '$orig', unable to extract anything, returning null")
      return null
    } else if (!value.equals(extracted)) {
      log.warn("Expected int for $fieldName, got '$orig', extracted '$extracted'")
    }
    return extracted.toInteger()
  } catch (Exception e) {
    log.warn("Unexpected $e")
    return null
  }
}

def getXml(element) {
  String s = element?.toString()
  if (s == null || s.size() == 0)
    return null
  def xml = new StreamingMarkupBuilder().bind { mkp.yield(element) }
  def match = xml =~ />(.*)</
  try {
    return match[0][1] // Strip element being retrieved - we just want its content
  } catch (IndexOutOfBoundsException e) {
    log.warn("Unable to strip xml elements from '$xml': $match")
    return xml
  }
}

// Build lists of affiliations and authors (TODO: ordered contributors too?)
Set affiliations = new HashSet()
articleMeta.aff.institution.each() { aff -> affiliations.add(aff.toString()) }
List authors = new ArrayList()
List editors = new ArrayList()
articleMeta.'contrib-group'.contrib.each() { contrib ->
  def user = new UserProfile()
  user.realName = getName(contrib.name)
  user.givenNames = contrib.name.'given-names'
  user.surnames = contrib.name.surname
  contrib.xref.each() { xref ->
    switch(xref.'@ref-type') {
//    case 'corresp': bc.note += articleMeta.'author-notes'.corresp.'@id'[xref.'@rid'] + "\n"; break
//    case 'aff': user.organizationName = articleMeta.aff.'@id'[xref.'@rid'].'addr-line'; break
    }
  }
  switch(contrib.@'contrib-type') {
  case 'author': authors += user; break
  case 'editor': editors += user; break
  }
}

// Log what we are doing
log.info("$doi migrating")

// Update article
def dc = article.dublinCore
def bc = dc.bibliographicCitation = new Citation()

bc.id                    = new URI(tostr(article.id.toString() + "/bibliographicCitation"))
bc.year                  = toint(articleMeta.'pub-date'[0].year)
bc.displayYear           = articleMeta.'pub-date'[0].year
bc.month                 = toint(articleMeta.'pub-date'[0].month)
bc.volumeNumber          = toint(articleMeta.volume)
bc.volume                = articleMeta.volume
bc.issue                 = tostr(articleMeta.issue)
bc.title                 = dc.title
bc.publisherLocation     = tostr(journalMeta.publisher.'publisher-loc')
bc.publisherName         = tostr(journalMeta.publisher.'publisher-name')
if (articleMeta.counts.'page-count')
  bc.pages                 = "1-" + tostr(articleMeta.counts.'page-count'.'@count')
bc.journal               = tostr(journalMeta.'journal-title')
bc.note                  = tostr(articleMeta.'author-notes'.fn)
bc.summary               = dc.description
bc.url                   = "http://dx.plos.org/${dc.identifier.substring(9)}"
bc.editors               = (editors ? editors : null)
bc.authors               = (authors ? authors : null)
bc.citationType          = PLoS.bibtex + "Article"

// dc.confirmsTo should scrape from the article, but for migration from 0.7, this works
dc.conformsTo            = new URI('http://dtd.nlm.nih.gov/publishing/2.0/journalpublishing.dtd')
dc.copyrightYear         = toint(articleMeta.'copyright-year')
dc.references            = []
// TODO: Set dc.license -- doc has article.article-meta.copyright-statement, but no bloody URI!

// Add the different types the article conforms to
article.articleType = new HashSet()
// PMC type
def articleType = new URI(PLoS.PLOS_ArticleType + tostr(slurpedArticle.'@article-type'))
article.articleType.add(articleType)
// PLoS type
articleMeta.'article-categories'.'subj-group'.each() { subj_group ->
  if (subj_group.'@subj-group-type' == "heading") {
    subj_group.subject.each() { subj ->
      article.articleType.add(new URI(PLoS.PLOS_ArticleType +
         URLEncoder.encode(tostr(subj), "UTF-8").replace("+", "%20")))
    }
  }
}

// add issn/eIssn
def addIssn = { field, val ->
  article."${field}" = val
  for (part in article.parts)
    part."${field}" = val
}

for (issn in journalMeta.issn) {
  switch(issn.'@pub-type') {
    case 'ppub': addIssn('issn', tostr(issn)); break
    case 'epub': addIssn('eIssn', tostr(issn)); break
  }
}

// Handle references
slurpedArticle.back.'ref-list'.ref.each() { src ->
  def cit = new Citation()

  authors = new ArrayList()
  editors = new ArrayList()
  src.citation.'person-group'.each() { person ->
    person.name.each() { name ->
      def user = new UserProfile()
      user.realName   = getName(name)
      user.givenNames = name.'given-names'
      user.surnames   = name.surname
      switch(person.@'person-group-type') {
        case 'author': authors += user; break
        case 'editor': editors += user; break
      }
    }
  }

  def pages = tostr(src.citation.'page-range'[0])
  def fpage = tostr(src.citation.fpage)
  def lpage = tostr(src.citation.lpage)
  if (!pages) {
    if (fpage && lpage)
      pages = "$fpage-$lpage"
    else if (fpage)
      pages = fpage
  }

  def name              = tostr(src.'@id')
  cit.id                = new URI(article.id.toString() + '/reference#' + src.'@id')
  cit.key               = tostr(src.label)
  // Try providing both String and Integer type of years
  cit.year              = findInt(src.citation.year[0], "$name:year", 4)
  cit.displayYear       = src.citation.year[0]
  cit.month             = tostr(src.citation.month[0])
  // Try providing both String and Integer type for volume
  cit.volumeNumber      = findInt(src.citation.volume[0], "$name:volume", 0)
  cit.volume            = src.citation.volume[0]
  cit.issue             = tostr(src.citation.issue[0])
  cit.title             = getXml(src.citation.'article-title'[0])
  if (!cit.title)
    cit.title           = getXml(src.citation.source[0])
  cit.publisherLocation = tostr(src.citation.'publisher-loc'[0])
  cit.publisherName     = tostr(src.citation.'publisher-name'[0])
  cit.pages             = pages
  cit.note              = tostr(src.citation.comment[0])
  cit.authors           = (authors ? authors : null)
  cit.editors           = (editors ? editors : null)
  cit.url               = tostr(src.citation.@'xlink:role')

  def citationType = tostr(src.citation.@'citation-type')
  if (citationType) {
    // Get the RDF type associated with the NLM citation type
    def citRdfType = citationMap.get(citationType)
    if (!citRdfType)
      citRdfType = PLoS.bibtex + "Misc"
    cit.citationType = citRdfType

    if (citationType.equals('journal') || citationType.equals('confproc'))
      cit.journal = tostr(src.citation.source[0])
  }

  dc.references.add(cit)
}


// TODO: All these routines should be a separate utility (or at least part of a library)
def max(a, b) { return (a) < (b) ? (b) : (a) }
def trunc(s, len) { s = s.toString(); return s.size() <= len ? s : s[0..(len-3)] + "..." }
def abbreviate(s) {
  def v = s[0]
  s[1..-1].each() { if (it <= 'Z' || it == ']') v += it.toLowerCase() }
  return v
}
def abbreviateClass(s) {
  def v = ""
  s.each() { if (it <= 'Z' && it >= 'A') v += it }
  return v
}

// Dump a map or a java-object
def dump(obj, prefix, indent, width) {
  def map = new TreeMap(obj instanceof Map ? obj : obj.getProperties())
  def keyLength = 0
  map.each() { 
    if (it.value != null || SHOWNULLS) keyLength = max(keyLength, it.key.toString().size())
  }
  map.each() { prop ->
    if (prop.key != "nextObject" && prop.key != "isPartOf" && prop.key != "class") {
      if (prop.value != null || SHOWNULLS)
        printf "%s %${keyLength}s: ", prefix, prop.key
      switch (prop.value) {
        case null: 
          if (SHOWNULLS) println 'null'
          break
        case java.util.Date:
        case java.lang.Integer:
          println trunc(prop.value, width - keyLength - indent - 2)
          break
        case java.lang.String:
          println "'" + trunc(prop.value, width - keyLength - indent - 4) + "'"
          break
        case java.net.URI:
          println "<" + trunc(prop.value, width - keyLength - indent - 4) + ">"
          break
        case org.plos.models.DublinCore:
        case org.plos.models.Citation:
        case org.plos.models.UserProfile:
        case org.plos.models.Category:
        case org.plos.models.ObjectInfo:
          println prop.value.class.name + ":"
          def abbrev = abbreviate(prop.key) + '.' + abbreviateClass(prop.value.class.name)
          dump(prop.value, prefix + abbrev + ':', indent + 2, width)
          break
        case java.util.Collection:
          println "${prop.value.size()} element(s) (${prop.value.class.name})"
          int i = 0
          def tmpmap = new HashMap()
          prop.value.each() {  tmpmap["[$i]"] = it; i++ }
          dump(tmpmap, prefix + prop.key, indent + 2, width)
          break
        default: 
          println prop.value.getClass().getName()
          break
      }
    }
  }
}

if (DUMP) {
  dump(article, "a:", 0, 100)
}
if (!DRYRUN) {
  println "Updating mulgara..."
  log.info("$doi updating mulgara")
  try {
    session.saveOrUpdate(article)
    tx.commit()
    session.close()
  } catch (Throwable t) {
    log.error("Unable to save article", t);
  }
}

// TODO: Deal with errors (i.e. catch exceptions)
