/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.plos.util.ToolHelper
import org.plos.article.util.ImageProcessor

long startTime = System.currentTimeMillis()
def seconds = { return (int) (((System.currentTimeMillis() - startTime) / 1000) + 0.5f) }

def cli = new CliBuilder(usage: 'processimages [-c config-overrides.xml] [-d output dir] [-o overwrite?] article.zip', writer : new PrintWriter ( System.out ))

cli.h(longOpt:'help', 'usage information')
cli.c(argName:'file', longOpt:'config', args:1, 'config file override')
cli.d(argName:'path', longOpt:'output directory', args:1, 'dir to put new article zip file containing the processed images')
cli.o(longOpt:'overwrite', 'overwrite existing processed article zip file?')

def opt = cli.parse(args);
if(!opt) return;

if (opt.h) { cli.usage(); return }

String[] otherArgs = opt.arguments()
if (otherArgs.size() != 1) {
  cli.usage()
  return
}

// this is the article zip file to be processed
String articleFile = otherArgs[0]

println 'Loading configuration...'
ToolHelper.loadConfiguration(opt.c)

// get an image processor to do the work 
final ImageProcessor ip;
try {
  ip = new ImageProcessor(opt.d, opt.o)
}
catch(Throwable t) {
  cli.usage()
  return
}

// go
println ('Processing file: ' + articleFile + '...')
ip.process(new File(articleFile))
println ('Processing complete (' + seconds() + 's)')
