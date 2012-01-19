/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util

import org.apache.commons.lang.text.StrMatcher
import org.apache.commons.lang.text.StrTokenizer

import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.XMLConfiguration

import org.plos.configuration.ConfigurationStore

/**
 * Fix mave java:exec command line parsing. Should not impact args run outside
 * maven unless they contain spaces.
 *
 * @param args The command line arguments
 * @return A fixed version of the args
 */
static String[] fixArgs(String[] args) {
  if (args.size() > 0 && args[0] == null) args = [ ]
  if (args != null && args.length == 1)
    args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher()).tokenArray
  return args
}

/**
 * Must initialize configuration before anybody tries to use it (at class load time)
 *
 * @param xmlConfigFileOverride The filename of an xml file that provides configuration overrides.
 * @return A commons-config Configuration instance
 */
static Configuration loadConfiguration(xmlConfigFileOverride) {
  ConfigurationStore.instance.loadDefaultConfiguration()
  def conf = new CompositeConfiguration()
  if (xmlConfigFileOverride)
    conf.addConfiguration(new XMLConfiguration(xmlConfigFileOverride))
  conf.addConfiguration(ConfigurationStore.instance.configuration)
  return conf
}
