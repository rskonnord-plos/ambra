/* $HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/libs/scripts/src/main/scr#$
 * $Id: alertsinfo.groovy 3478 2007-08-16 17:51:22Z ebrown $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.plos.util.ToolHelper

import org.topazproject.mulgara.itql.ItqlHelper;

log = LogFactory.getLog(this.getClass())

// Use ToolHelper to patch args
args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'lockMulgara [-c config-overrides.xml]')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration
CONF = ToolHelper.loadConfiguration(opt.c)

def mulgaraUri = CONF.getString("topaz.services.itql.uri")
def operation = '';

println "Mulgara URI: $mulgaraUri"

itql = new ItqlHelper(URI.create(mulgaraUri))
  
lockCmd = "set autocommit off"
println "Issuing lock cmd: $lockCmd ..."
itql.doUpdate(lockCmd, null)

println "Running script"
Process myProcess = Runtime.getRuntime().exec('/bin/false')

lockCmd = "set autocommit on"
println "Issuing lock cmd: $lockCmd ..."
itql.doUpdate(lockCmd, null)

println " completed!"
