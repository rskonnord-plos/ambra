/* $$HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/webapp/src/main/java/org/plos/admin#$$                                                                        $
 * $$Id ImageMagicExecUtil.java 2007-06-01 $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;

/**
 * ImageMagicExecUtil
 * 
 * @author jonnie
 */
public class ImageMagicExecUtil {
  private static final Log log = LogFactory.getLog(ImageMagicExecUtil.class);

  private final String pathToProgram;

  /**
   * Constructor - Determines the image magic program path from the
   * {@link ConfigurationStore}
   * 
   * @throws IllegalArgumentException When a valid path can't be resolved from
   *           the {@link ConfigurationStore}.
   */
  public ImageMagicExecUtil() throws IllegalArgumentException {
    final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

    if (configuration.isEmpty()) {
      throw new IllegalArgumentException("ERROR: configuration has no property values");
    }

    final String pathToProgramKey = "topaz.utilities.image-magick.executable-path";
    final String pathToProgram = configuration.getString(pathToProgramKey);

    if (pathToProgram == null) {
      throw new IllegalArgumentException("ERROR: configuration failed to associate a value with "
          + pathToProgramKey);
    }

    this.pathToProgram = pathToProgram;
  }

  /**
   * Constructor
   * 
   * @param pathToProgram
   * @throws IllegalArgumentException When the <code>pathToProgram</code> is
   *           <code>null</code> or invalid.
   */
  public ImageMagicExecUtil(String pathToProgram) throws ImageResizeException {
    if (pathToProgram == null) {
      throw new ImageResizeException("A path to the image magic program must be specified.");
    }
    this.pathToProgram = pathToProgram;
  }
  
  /**
   * This method calls the convert utility to create a new image with the
   * specified properties
   * 
   * @param input the object representing the file to be converted
   * @param output the file to be created (which results from the conversion
   *          operation)
   * @param imageWidth the maximum width of the converted and resized image
   * @param imageHeight the maximum height of the converted and resized image
   * @param quality the quality of compressed image (100 is best quality).
   * @return result an indication of whether the program was run successfully or
   *         not (true = success & false = failure)
   */
  public boolean convert(final File input, final File output, final int imageWidth,
      final int imageHeight, int quality) {
    final boolean result;
    final ArrayList<String> command = new ArrayList<String>(7);
    final String resizeOperation = "-resize";
    // for example: newDimensions = 1024x768
    final String newDimensions = imageWidth + "x" + imageHeight;
    final String compressionOperation = "-quality";
    int exitStatus = 1;

    // qualityLevel = 100 => perfect quality/no compression
    if (quality < 0 || quality > 100) {
      quality = 100;
    }

    try {
      command.add(this.pathToProgram);
      command.add(resizeOperation);
      command.add(newDimensions);
      command.add(compressionOperation);
      command.add(String.valueOf(quality));
      command.add(input.getCanonicalPath());
      command.add(output.getCanonicalPath());

      final String[] commandArray = command.toArray(new String[] {});

      if (log.isDebugEnabled()) {
        for (int position = 0; position < commandArray.length; position++) {
          log.debug(commandArray[position]);
        }
      }

      final Process proc = Runtime.getRuntime().exec(commandArray);

      while (true) {
        try {
          exitStatus = proc.waitFor();
          break;
        } catch (InterruptedException e) {
          if (log.isDebugEnabled()) {
            log.debug("Ignoring ...", e);
          }
        }
      }

      if (log.isDebugEnabled()) {
        log.debug("exit-status: " + exitStatus);
      }
    } catch (Exception e) {
      log.fatal("", e);
    }
    finally {
      result = (exitStatus == 0);
    }

    return result;
  }
}
