/* $HeadURL::                                                                                    $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util

import org.plos.configuration.ConfigurationStore
import org.topazproject.xml.transform.cache.CachedSource

/**
 * Wrap a fedora article in an object to extract additional information.
 *
 * @author Eric Brown
 */
class FedoraArticle {
  def article
  String url
  List authors = new ArrayList()
  List contributors = new ArrayList()
  int volume
  int issue

  /**
   * Construct a fedora article object.
   *
   * @param doi The article doi to read in
   */
  FedoraArticle(String doi) {
    // Get an XmlSlurper object and use our resolver (without resolver, parsing takes forever)
    def slurper = new XmlSlurper()
    slurper.setEntityResolver(CachedSource.getResolver())

    // Get configuration information we need
    def conf = ConfigurationStore.getInstance().configuration
    def fedoraUri = conf.getString("topaz.services.fedora.base-url") + "get"

    this.url = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"
    this.article = slurper.parse(new URL(this.url).getContent())
    article.front.'article-meta'.'contrib-group'.contrib.each() {
      def name = ((it.name.@'name-style' == "eastern") 
                       ? "${it.name.surname} ${it.name.'given-names'}"
                       : "${it.name.'given-names'} ${it.name.surname}")
      switch(it.@'contrib-type') {
      case 'author': this.authors += name.toString(); break
      case 'contributor': this.contributors += name.toString(); break
      }
    }
    this.volume = Integer.valueOf(article.front.'article-meta'.volume.toString())
    this.issue = Integer.valueOf(article.front.'article-meta'.issue.toString())
  }

  /**
   * Command line to test FedoraArticle.
   */
  static void main(String[] args) {
    ConfigurationStore.getInstance().loadDefaultConfiguration()
    args = ToolHelper.fixArgs(args)
    // TODO: accept and parse real arguments
    def fa = new FedoraArticle(args[0])
    println "Url: " + fa.url
    println "Authors: " + fa.authors
    println "Contributors: " + fa.contributors
    println "Volume/Issue: ${fa.volume}/${fa.issue}"
//    new XmlNodePrinter().print(new XmlParser().parseText(fa.content))
  }
}
