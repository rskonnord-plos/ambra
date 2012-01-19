/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

import org.plos.util.ToolHelper

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'processimages [-v] [-c config-overrides.xml] [-o output.zip] article.zip' , writer : new PrintWriter ( System.out ))
cli.h(longOpt:'help', "help (this message)")
cli.o(args:1, 'output.zip - new zip file containing resized images')
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.v(args:0, 'verbose')


// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }
def String[] otherArgs = opt.arguments()
if (otherArgs.size() != 1) {
  cli.usage()
  return
}
def String articleFile = otherArgs[0]

VERBOSE = opt.v

if (VERBOSE) {
  println ('Processing file: ' + articleFile)
}

CONF = ToolHelper.loadConfiguration(opt.c)

IM_CONVERT = CONF.getString("topaz.utilities.image-magick.executable-path")
IM_IDENTIFY = CONF.getString("topaz.utilities.image-magick.identify-path")

def File outputFile
def File inputFile = null
def boolean completed = false
def ZipFile articleZip
def ZipOutputStream newZip

if (opt.o) {
  outputFile = new File (opt.o)
  articleZip = new ZipFile (articleFile)
  inputFile  = new File (articleFile)
  if (inputFile.getAbsolutePath().equals (outputFile.getAbsolutePath())) {
    println 'Original file cannot be overwritten'
    return
  }
} else {
  outputFile = new File (articleFile)
  outputFile = new File ('new_' + outputFile.getName())
  articleZip = new ZipFile (articleFile)
}


try {
  def ArrayList<String> imgNames = new ArrayList<String>()
  newZip = new ZipOutputStream(new FileOutputStream(outputFile))
  def InputStream reader = null
  def boolean isTif = false
  def FileOutputStream fos = null
  def File f = null

  for (entry in articleZip.entries()) {
    name = entry.getName()
    newZip.putNextEntry(new ZipEntry (name))
    reader = articleZip.getInputStream(entry)
    isTif = entry.getName().toLowerCase().endsWith('.tif')
    int numBytes = 0
    byte[] a = new byte[4096]
    if (isTif) {
      f = File.createTempFile('tmp_', name)
      if (VERBOSE) {
        println 'Created temp file: ' + f.getCanonicalPath()
      }
      fos = new FileOutputStream(f)
    }
    while ((numBytes = reader.read(a)) > 0) {
      newZip.write(a, 0, numBytes)
      if (isTif) {
        fos.write(a, 0, numBytes)
      }
    }
    reader.close()
    newZip.closeEntry()
    if (isTif) {
      fos.close()
      resizeImage(entry, imgNames, f)
      f.delete()
    }
  }

  if (VERBOSE) {
    println 'Number of resized images: ' + imgNames.size()
  }
  for (newImg in imgNames) {
    def File fileObj = new File (newImg)
    def FileInputStream inputStream = new FileInputStream(fileObj)
    if (VERBOSE) {
      println 'Adding to zip file: ' + newImg.substring(newImg.lastIndexOf(File.separator) + 1,
                                                        newImg.length())
    }
    newZip.putNextEntry(new ZipEntry(newImg.substring(newImg.lastIndexOf(File.separator) + 1,
                                                      newImg.length())))
    int numBytes = 0
    byte[] a = new byte[4096]
    while ((numBytes = inputStream.read(a)) > 0) {
      newZip.write(a, 0, numBytes)
    }
    inputStream.close()
    newZip.closeEntry()
    fileObj.delete()
  }
  completed = true;
} catch (ZipException ze) {
  println ("ZipException when processing file: " + articleFile)
  ze.printStackTrace(System.out)
} catch (IOException ioe) {
  println ("IOException when processing file: " + articleFile)
  ioe.printStackTrace(System.out)
} catch (IllegalStateException ise) {
  println ("IllegalStateException when processing file: " + articleFile)
  ise.printStackTrace(System.out)
} finally {
  try {
    articleZip.close()
    newZip.close()
  } catch (Exception e) {
  }
  if (!completed) {
    outputFile.delete()
  }
}

private void resizeImage(img, imgNames, file) {
  name =  img.getName()
  baseName = name.substring(0, name.length()-4) + '.png'
  newName = System.getProperty('java.io.tmpdir') + 'S_' + baseName
  if (VERBOSE) {
    println "Creating " + newName
  }
  def ant = new AntBuilder()   // create an antbuilder
  ant.exec(outputproperty:"cmdOut",
           errorproperty: "cmdErr",
           resultproperty:"cmdExit",
           failonerror: "true",
           executable: IM_CONVERT) {
             arg(line:'"' + file.getCanonicalPath() + '" -resize "70x>" ' + newName)
           }
  if (ant.project.properties.cmdExit == '0') {
    imgNames.add(newName)
  }

  if (VERBOSE) {
    println "return code:  ${ant.project.properties.cmdExit}"
    println "stderr:       ${ant.project.properties.cmdErr}"
    println "stdout:       ${ant.project.properties.cmdOut}"
  }

  newName = System.getProperty('java.io.tmpdir') + 'L_' + baseName
  if (VERBOSE) {
      println "Creating " + newName
  }
  ant = new AntBuilder()
  ant.exec(outputproperty:"cmdOut",
           errorproperty: "cmdErr",
           resultproperty:"cmdExit",
           failonerror: "true",
           executable: IM_CONVERT) {
             arg(line:'"' + file.getCanonicalPath() + '" ' + newName)
           }

  if (ant.project.properties.cmdExit == '0') {
    imgNames.add(newName)
  }
  if (VERBOSE) {
    println "return code:  ${ant.project.properties.cmdExit}"
    println "stderr:       ${ant.project.properties.cmdErr}"
    println "stdout:       ${ant.project.properties.cmdOut}"
  }

  if (VERBOSE) {
    println "Sizing " + name
  }
  ant = new AntBuilder()
  ant.exec(outputproperty:"cmdOut",
           errorproperty: "cmdErr",
           resultproperty:"cmdExit",
           failonerror: "true",
           executable: IM_IDENTIFY) {
             arg(line:'-quiet -format "%w %h" "' + file.getCanonicalPath() + '"')
           }
  if (VERBOSE) {
    println "return code:  ${ant.project.properties.cmdExit}"
    println "stderr:       ${ant.project.properties.cmdErr}"
    println "stdout:       ${ant.project.properties.cmdOut}"
  }

  dim = ant.project.properties.cmdOut.split(" ")
  height = dim[1]
  width = dim[0]

  if (VERBOSE) {
    println 'height = ' + height;
    println 'width= ' + width;
  }

  if (height > width)
    arg = "x600>"
  else
    arg="600x>"

  newName = System.getProperty('java.io.tmpdir') + 'M_' + baseName

  if (VERBOSE) {
    println "Creating " + newName
  }
  ant = new AntBuilder()   // create an antbuilder
  ant.exec(outputproperty:"cmdOut",
           errorproperty: "cmdErr",
           resultproperty:"cmdExit",
           failonerror: "true",
           executable: IM_CONVERT) {
             arg(line:'"' + file.getCanonicalPath() + '" -resize "' + arg + '" ' + newName)
           }
  if (ant.project.properties.cmdExit == '0') {
    imgNames.add(newName)
  }

  if (VERBOSE) {
    println "return code:  ${ant.project.properties.cmdExit}"
    println "stderr:       ${ant.project.properties.cmdErr}"
    println "stdout:       ${ant.project.properties.cmdOut}"
  }
}
