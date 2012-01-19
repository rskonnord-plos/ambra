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
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.plos.models.Comment
import org.plos.models.Rating
import org.plos.models.RatingSummary
import org.plos.models.Reply
import org.plos.models.ReplyThread
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.otm.query.Results
import org.topazproject.fedora.client.APIMStubFactory
import org.topazproject.fedora.client.FedoraAPIM
import org.topazproject.authentication.PasswordProtectedService

log = LogFactory.getLog(this.getClass());

// Use ToolHelper (currently in wrong place -- article-util) to patch args
args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'deleteannotaiton [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.t(args:0, 'test run')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

// Test mode
TEST = opt.t

// Setup fedora
def fedoraSvc = new PasswordProtectedService(CONF.getString("topaz.services.fedora.uri"),
                                             CONF.getString("topaz.services.fedora.userName"),
                                             CONF.getString("topaz.services.fedora.password"))
def apim = APIMStubFactory.create(fedoraSvc)

// Setup OTM
def factory = new SessionFactory();
def itql = new ItqlStore(URI.create(CONF.getString("topaz.services.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("topaz.models.articles")), null);
factory.setTripleStore(itql)
factory.addModel(ri)
factory.preload(Comment.class)
factory.preload(Rating.class)
factory.preload(RatingSummary.class)
factory.preload(Reply.class)
factory.preload(ReplyThread.class)
def session = factory.openSession();
def tx = session.beginTransaction();

opt.arguments().each() { uri ->
  // TODO: Delete from fedora & mulgara
  def query = "select a from Comment a where a.annotates = <$uri>;"
  def results = session.createQuery(query).execute()
  while (results.next()) {
    def a = results.get('a')
    println "Deleting ${a.id}: ${a.body} (${a.title})"
    session.delete(a)
    if (a.body.toString().startsWith("info:fedora")) {
      def pid = a.body.toString()[12..-1]
      try {
        if (!TEST)
          apim.purgeObject(pid, "Purged object", false)
      } catch (Exception e) {
        tx.rollback()
        throw e
      }
    }
  }
}

if (!TEST)
  tx.commit()
