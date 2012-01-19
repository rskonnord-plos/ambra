/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
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
import org.plos.util.ToolHelper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.plos.models.Comment
import org.plos.models.Rating
import org.plos.models.RatingSummary
import org.plos.models.Reply
import org.plos.models.ReplyThread
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.impl.SessionFactoryImpl
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
def fedoraSvc = new PasswordProtectedService(CONF.getString("ambra.topaz.blobStore.fedora.uri"),
                                             CONF.getString("ambra.topaz.blobStore.fedora.userName"),
                                             CONF.getString("ambra.topaz.blobStore.fedora.password"))
def apim = APIMStubFactory.create(fedoraSvc)

// Setup OTM
def factory = new SessionFactoryImpl();
def itql = new ItqlStore(URI.create(CONF.getString("ambra.topaz.tripleStore.mulgara.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("ambra.models.articles")), null);
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

System.exit(0)
