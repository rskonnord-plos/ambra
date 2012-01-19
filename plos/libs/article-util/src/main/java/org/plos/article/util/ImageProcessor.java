/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.configuration.ConfigurationStore;
import org.plos.model.article.ArticleType;

/**
 * ImageProcessor - Processes images contained in a "raw" un-adulterated article
 * zip file creating a new article zip file containing the additional processed
 * images.
 * @author jkirton
 */
public final class ImageProcessor {
  private final Log log = LogFactory.getLog(ImageProcessor.class);
  
  /**
   * Regex for detecting processed images files in a processed article zip file.
   */
  public static final Pattern procImgPfxPattern = Pattern.compile("^S_.+|^M_.+|^L_.+");

  /**
   * The default output dir is the config's ingest source dir.
   */
  private static final String CONF_KEY_OUTPUT_DIR = "pub.spring.ingest.source";

  private static final String TKN_ARTICLE_TYPE_PREFIX_A = "<subj-group subj-group-type=\"heading\">";
  private static final String TKN_ARTICLE_TYPE_PREFIX_B = "<subject>";

  /**
   * The output dir. Newly created processed article zip files are placed here.
   */
  private final File outputDir;

  /**
   * Overwrite existing processed article zip files if they exist?
   */
  private final boolean overwrite;

  /**
   * Constructor
   */
  public ImageProcessor() {
    this(null, false);
  }

  /**
   * Constructor - This constructor derives necessary configuration from the
   * {@link ConfigurationStore}.
   * @param overwrite Overwrite processed article zip file?
   * @throws IllegalStateException When configuration properties can't be
   *         resolved or when found configuration properties themselves are
   *         invalid.
   */
  public ImageProcessor(boolean overwrite) throws IllegalArgumentException {
    this(null, overwrite);
  }

  /**
   * Constructor
   * @param outputDirPath if <code>null</code>, the
   *        {@link ConfigurationStore} is queried
   * @param overwrite Overwrite processed article zip file?
   * @throws IllegalArgumentException When any of the given paths are not
   *         resolvable to existing directories
   */
  public ImageProcessor(String outputDirPath, boolean overwrite) throws IllegalArgumentException {
    super();
    if (outputDirPath == null) {
      Configuration cfg = ConfigurationStore.getInstance().getConfiguration();
      assert cfg != null;
      outputDirPath = cfg.getString(CONF_KEY_OUTPUT_DIR);
    }
    outputDir = resolveDir(outputDirPath, "output");
    this.overwrite = overwrite;
    
    if(log.isInfoEnabled()) {
      log.info(toString());
    }
  }

  /**
   * Resolves a given dir path to a File
   * @param path The path to the directory
   * @param descriptor The path descriptor used when an error occurrs.
   * @return New File instance
   * @throws IllegalAccessException If the <code>path</code> arg is not
   *         resolvable to an existing directory.
   */
  private File resolveDir(String path, String descriptor) throws IllegalArgumentException {
    if (path == null) {
      throw new IllegalArgumentException("Un-resolvable " + descriptor + " directory path");
    }
    File f = new File(path);
    if (!f.isDirectory()) {
      throw new IllegalArgumentException("Invalid " + descriptor + " directory: " + path);
    }
    return f;
  }

  /**
   * Determines the associated ArticleType for an ingestable article zip file by
   * finding and interrogating the contained article xml file.
   * @param articleZip The ingestable article zip file as an {@link ArticleZip}
   *        instance
   * @return The associated ArticleType
   * @throws IOException When a streaming related error occurrs
   * @throws ImageProcessingException When the article type is unresolvable
   */
  private ArticleType resolveArticleType(ArticleZip articleZip) throws IOException,
      ImageProcessingException {

    // seek the article xml file zip entry
    String baseName = articleZip.getBaseName();
    Enumeration<? extends ZipEntry> entries = articleZip.getZipFile().entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.getName().startsWith(baseName) && entry.getName().endsWith(".xml")) {
        // found the article xml file entry
        byte[] bytes = new byte[2048];
        InputStream is = articleZip.getZipFile().getInputStream(entry);
        try {
          // we are assuming the sought token is within the first 2k of the xml file
          is.read(bytes);
          String s = new String(bytes);
          int sindx = s.indexOf(TKN_ARTICLE_TYPE_PREFIX_A);
          if (sindx >= 0) {
            sindx = s.indexOf(TKN_ARTICLE_TYPE_PREFIX_B, sindx);
            if(sindx >= 0) {
              sindx += TKN_ARTICLE_TYPE_PREFIX_B.length();
              int eindx = s.indexOf('<', sindx);
              s = s.substring(sindx, eindx);
              ArticleType at = ArticleType.getKnownArticleTypeForHeading(s);
              if (at == null) {
                throw new ImageProcessingException("Unknown article type: " + s);
              }
              if (log.isDebugEnabled())
                log.debug("Resolved article type (" + at.toString() + ") for article zip: "
                    + articleZip.toString());
              return at;
            }
          }
        }
        finally {
          is.close();
        }
      }
    }

    throw new ImageProcessingException("Could not find article type within article xml file: "
        + articleZip.toString());
  }

  /**
   * Processes a single "raw" article zip file.
   * @param inputFile The raw article zip file
   * @return A new zip file containing that contained in the given input file
   *         plus processed article images.
   * @throws IllegalArgumentException When the given article zip file is
   *         <code>null</code> or invalid.
   * @throws ImageProcessingException When an exception occurrs while
   *         processing.
   */
  public File process(File inputFile) throws IllegalArgumentException, ImageProcessingException {
    log.debug("Assembling article zip container...");
    ArticleZip articleZip;
    try {
      articleZip = new ArticleZip(inputFile);
    }
    catch(IllegalArgumentException iae) {
      throw new ImageProcessingException(iae.getMessage(), iae);
    }

    if (log.isInfoEnabled())
      log.info("Processing article zip file: '" + articleZip.toString() + "' ...");

    log.debug("Obtaining File handle for processed zip output...");
    File outputFile;
    try {
      String outPath = outputDir.getCanonicalPath() + File.separator
          + ArticleZip.PROCESSED_FILENAME_PREFIX + inputFile.getName();
      outputFile = new File(outPath);
    } catch (IOException ioe) {
      throw new ImageProcessingException(ioe.getMessage(), ioe);
    }
    if (outputFile.exists()) {
      if (overwrite) {
        try {
          outputFile.delete();
        } catch (SecurityException se) {
          throw new ImageProcessingException(
              "Unable to remove existing processed article zip file due to a security exception: "
                  + se.getMessage(), se);
        }
      }
      else {
        throw new ImageProcessingException("Processed article zip file already exists: "
            + outputFile.getName());
      }
    }

    try {
      process(articleZip, outputFile);
    } catch (IOException ioe) {
      throw new ImageProcessingException(ioe.getMessage(), ioe);
    }

    return outputFile;
  }

  /**
   * Process an article zip
   * @param articleZip A valid ArticleZip
   * @param outputFile A valid File handle
   * @throws IOException
   * @throws ImageProcessingException
   */
  private void process(ArticleZip articleZip, File outputFile) throws IOException,
      ImageProcessingException {

    // get a zip output stream handle
    ZipOutputStream newZip;
    try {
      newZip = new ZipOutputStream(new FileOutputStream(outputFile));
    } catch (FileNotFoundException fnfe) {
      throw new ImageProcessingException(fnfe);
    }

    // resolve the article type for this article zip
    final ArticleType at = resolveArticleType(articleZip);

    // iterate input zip file contents
    if (log.isDebugEnabled()) log.debug("Iterating over zip entries ...");
    boolean complete = false;
    try {
      Enumeration<? extends ZipEntry> entries = articleZip.getZipFile().entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName(), baseName = null;
        if (log.isDebugEnabled()) log.debug("Processing zip entry: " + name + "...");

        // add to new zip
        newZip.putNextEntry(new ZipEntry(name));

        // get input stream for zip entry
        InputStream reader = articleZip.getZipFile().getInputStream(entry);
        boolean isTif = name.toLowerCase().endsWith(".tif");
        int numBytes = 0;
        byte[] bytes = new byte[4096];
        FileOutputStream fosTiff = null;
        File fTiff = null;
        if (isTif) {
          baseName = name.substring(0, name.length() - 4);
          fTiff = File.createTempFile("tmp_", name);
          if (log.isDebugEnabled()) log.debug("Created temp file: " + fTiff.getCanonicalPath());
          fosTiff = new FileOutputStream(fTiff);
        }
        while ((numBytes = reader.read(bytes)) > 0) {
          newZip.write(bytes, 0, numBytes);
          if (isTif) {
            fosTiff.write(bytes, 0, numBytes);
          }
        }
        reader.close();
        newZip.closeEntry();
        if (isTif) {
          fosTiff.close();
          ImageSetConfig isc = at.getImageSetConfigName() == null ? ImageSetConfig
              .getDefaultImageSetConfig() : ImageSetConfig.getImageSetConfig(at
              .getImageSetConfigName());
          if (isc == null) {
            throw new ImageProcessingException(
                "Could not resolve an image set configuration from article type: " + at.toString());
          }
          if (log.isDebugEnabled())
            log.debug("Employing image set configuration: " + isc.toString());
          processImage(baseName, fTiff, isc, newZip);
          fTiff.delete();
        }
      }
      complete = true;
    } catch (IOException ioe) {
      throw new ImageProcessingException(ioe);
    }
    finally {
      try {
        articleZip.close();
        newZip.close();
      } catch (Exception e) {
      }
      if (!complete) {
        articleZip.getFile().delete();
      }
    }

  }

  /**
   * Processes an un-processed image creating temp files holding the processed
   * image data.
   * @param baseName The name of the zip file entry w/o an extension suffix
   * @param imgf The image File to process
   * @param isc The ImageSetConfig instance to employ
   * @param newZip The new zip file output stream
   * @throws IOException Upon streaming related error
   * @throws ImageProcessingException Upon processing error
   */
  private void processImage(String baseName, File imgf, ImageSetConfig isc, ZipOutputStream newZip)
      throws IOException, ImageProcessingException {
    final ImageResizeService irs = new ImageResizeService(isc);

    final byte[] unprocBytes = FileUtils.readFileToByteArray(imgf);
    String piName;
    char[] pfxs = new char[] {
    'S', 'M', 'L'
    };

    for (char pfx : pfxs) {
      piName = (new Character(pfx)).toString() + '_' + baseName + '.' + irs.getProcessedImageType();
      if (log.isDebugEnabled()) log.debug("Generating: " + piName + "...");
      newZip.putNextEntry(new ZipEntry(piName));
      switch (pfx) {
        case 'S':
          newZip.write(irs.getSmallScaledImage(unprocBytes));
          break;
        case 'M':
          newZip.write(irs.getMediumScaledImage(unprocBytes));
          break;
        case 'L':
          newZip.write(irs.getLargeScaledImage(unprocBytes));
          break;
      }
      newZip.closeEntry();
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("outputDir", outputDir).append("overwrite", overwrite)
        .toString();
  }
}
