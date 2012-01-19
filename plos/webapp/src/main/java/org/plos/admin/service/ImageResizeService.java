/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import com.sun.media.jai.codec.FileSeekableStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.configuration.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.model.article.ArticleType;

/**
 * ImageResizeService resizes images and converts them to PNG format for articles
 * based on the ArticleType. The ArticleType supplies configuration parameters for 
 * small, medium, and large images. An ImageResizeService object should be instantiated
 * for each operation and an ArticleType passed to the constructor. The ImageResizeService
 * provides a limited set of public methods for producing converted images. 
 *
 * @author Alex Worden, stevec
 */
public class ImageResizeService {
  private static final Log log = LogFactory.getLog(ImageResizeService.class);

  private int height;
  private int width;
  private String inputImageFileName;
  private String outputImageFileName;
  private File location;
  private File inputImageFile;
  private File outputImageFile;
  private ImageSetConfig imageSetConfig;
  private Configuration configuration;

  public ImageResizeService(ImageSetConfig imgSetConfig) throws ImageResizeException {
    this.imageSetConfig = imgSetConfig;
    configuration = ConfigurationStore.getInstance().getConfiguration();
    if (configuration.isEmpty()) {
      log.warn("Configuration has no property values");
    } else {
      String directory = configuration.getString("topaz.utilities.image-magick.temp-directory");
      if (directory == null) {
        log.warn("Property topaz.utilities.image-magick.temp-directory not configured");
      } else {
        setWorkingDirectory(directory);
      }
    }

    Integer disambiguation = new Integer(hashCode());
    inputImageFileName = "_" + disambiguation + "current";
    outputImageFileName = "_" + disambiguation + "final.png";
  }

  /**
   * Set a working directory on the file system to be used for temporary image storage. 
   * The JVM must have read and write privileges to this existing directory. 
   *
   * @param directory - the path to this directory
   */
  public void setWorkingDirectory(String directory) throws ImageResizeException {
    location = new File(directory);
    
    // Try to create the directory if it doesn't exist
    if (!location.exists()) {
      try {
        location.mkdirs();
      } catch (Exception se) {
        throw new ImageResizeException("Failed to create working directory " +
        		"at location: '"+directory+"'", se);
      }
    } else {
      if (!location.isDirectory()) {
        throw new ImageResizeException("Directory path not a valid directory: '"+directory+"'");
      }
    }

    if (!location.canRead()) {
      throw new ImageResizeException("Denied read access for directory '"+directory+"'");
    }

    if (!location.canWrite()) {
      throw new ImageResizeException("Denied write access for directory '"+directory+"'");
    }
  }

  /**
   * Get the working directory configured for temporary image storage. 
   * @return
   * @throws ImageResizeException
   */
  public File getWorkingDirectory() throws ImageResizeException {
    
    return location;
  }
  
  /**
   * Initialize members inputImageFile, outputImageFile, width and height as follows:
   *
   * inputImageFile - is an extant file populated with the contents of image
   * outputImageFile - is a non-extant file which is, nonetheless, a valid instance of File
   * width - the current width of the image stored in member inputImageFile
   * height - the current height of the image stored in member inputImageFile
   *
   * @param image - the image data to be stored in member inputImageFile
   * @throws ImageResizeException
   */
  private void preOperation(final byte[] image) throws ImageResizeException {
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    inputImageFile = new File(location,inputImageFileName);
    outputImageFile = new File(location,outputImageFileName);

    try {
      if (!inputImageFile.createNewFile()) {
        try {
          inputImageFile.delete();
        } catch (SecurityException e) {}

        throw new ImageResizeException("Unable to create temporary file: '" +
                                       inputImageFile.getCanonicalPath()+"'");
      }

      if (outputImageFile.exists()) {
        throw new ImageResizeException("Temporary file: '" +
                                       outputImageFile.getCanonicalPath() + 
                                       "' already exists");
      }

      final ByteArrayInputStream in = new ByteArrayInputStream(image);
      FileSeekableStream fss = null;
      try {
        final FileOutputStream out = new FileOutputStream(inputImageFile);

        try {
          imageRetrievalService.transferImage(in,out);
        } catch (ImageRetrievalServiceException e) {
          throw new ImageResizeException(e);
        } finally {
          out.close();
        }

        fss  = new FileSeekableStream (inputImageFile);

        final RenderedOp srcImage = JAI.create("Stream", fss);
        width = srcImage.getWidth();
        height = srcImage.getHeight();
        srcImage.dispose();
      } catch (SecurityException e) {
        throw new ImageResizeException(e);
      } finally {
        if (fss != null) {
          fss.close();
        }
        in.close();
      }
    } catch (SecurityException e) {
      throw new ImageResizeException(e);
    } catch (IOException e) {
      throw new ImageResizeException(e);
    }
  }

  /**
   * Disassociates members inputImageFile and outputImageFile from their
   * filesystem counterparts by deleting their filesystem counterparts.
   *
   * @throws ImageResizeException
   */
  private void postOperation() throws ImageResizeException {
    try {
      if (inputImageFile.exists() && !inputImageFile.delete()) {
        throw new ImageResizeException("Unable to delete temporary file: '" +
                                       inputImageFileName +"'");
      }
    } catch (SecurityException e) {
      throw new ImageResizeException(e);
    } finally {
      try {
        if (outputImageFile.exists() && !outputImageFile.delete()) {
          throw new ImageResizeException("Unable to delete temporary file: '" +
                                         outputImageFileName +"'");
        }
      } catch (SecurityException e) {
        throw new ImageResizeException(e);
      }
    }
  }

  /**
   * Convert the image and apply desired scaling
   *
   * @param newWidth - the desired width to which the image should be scaled
   * @param newHeight - the desired height to which the image should be scaled
   * @param quality - the quality of the image (100 = lossless compression)
   * @throws ImageResizeException
   */
  private void createScaledImage(int newWidth, int newHeight, int quality)
        throws ImageResizeException {
    final ImageMagicExecUtil util = new ImageMagicExecUtil();
    final boolean status = util.convert(this.inputImageFile,outputImageFile,newWidth,newHeight,quality);

    if (!status) {
      log.error("Failed to scale and convert input image: '"+inputImageFileName+"'");
      throw new ImageResizeException(new Exception("Operation convert failed for image: '"+inputImageFileName+"'"));
    }
  }

  /**
   * Read the transformed PNG image from member outputImageFile, write it to a
   * buffer and return the resulting byte array.
   * @param sourceFile The source image file of the byte array returned
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private synchronized byte[] getPNGByteArray(File sourceFile) throws ImageResizeException {
    assert(sourceFile != null && sourceFile.exists() && sourceFile.length() > 0);

    final byte[] result;
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    try {
      final InputStream in = new FileInputStream(sourceFile);
      final long fileSize = sourceFile.length();

      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream((int) fileSize);

        try {
          imageRetrievalService.transferImage(in,out);
          result = out.toByteArray();

          if (log.isDebugEnabled()) {
            log.debug("file size: " + result.length);
          }
        } catch (ImageRetrievalServiceException e) {
          throw new ImageResizeException(e);
        } finally {
          out.close();
        }
      } finally {
        in.close();
      }
    } catch (FileNotFoundException e) {
      throw new ImageResizeException(e);
    } catch (IOException e) {
      throw new ImageResizeException(e);
    }

    return result;
  }

  /**
   * Return a PNG version of the image without scaling it.
   *
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] performNoScaling(int quality) throws ImageResizeException {
    createScaledImage(this.width,this.height, quality);
    return getPNGByteArray(outputImageFile);
  }

  /**
   * Scale the image to a maximum height of fixHeight (while preserving the
   * aspect ratio) and return a PNG version of it
   *
   * @param fixHeight
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleFixHeight(int fixHeight, int quality) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (this.height > fixHeight) {
      scale = (float)fixHeight / (float)this.height;
      newHeight = fixHeight;
      newWidth = (int) (this.width * scale);
    } else {
      newHeight = this.height;
      newWidth = this.width;
    }

    
    createScaledImage(newWidth,newHeight, quality);

    return getPNGByteArray(outputImageFile);
  }

  /**
   * Scale the image to a maximum width of fixWidth (while preserving the
   * aspect ratio) and return a PNG version of it
   *
   * @param fixWidth
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleFixWidth(int fixWidth, int quality) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (this.width > fixWidth) {
      scale = (float)fixWidth / (float)this.width;
      newWidth = fixWidth;
      newHeight = (int) (this.height * scale);
    } else {
      newWidth = this.width;
      newHeight = this.height;
    }

    createScaledImage(newWidth, newHeight, quality);

    return getPNGByteArray(outputImageFile);
  }

  /**
   * Scale the image to a maximum of oneSide in both directions (while
   * preserving the aspect ratio) and return a PNG version of it
   *
   * @param oneSide
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleLargestDim(int oneSide, int quality) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if ((this.height > this.width) && (this.height > oneSide)) {
      scale = (float)oneSide / (float)this.height;
      newHeight = oneSide;
      newWidth = (int)(this.width * scale);
    } else if (this.width > oneSide) {
      scale = (float)oneSide / (float)this.width;
      newWidth = oneSide;
      newHeight = (int) (this.height * scale);
    } else {
      newWidth = this.width;
      newHeight = this.height;
    }

    createScaledImage(newWidth,newHeight, quality);

    return getPNGByteArray(outputImageFile);
  }

  /**
   * Scale the image to 70 pixels in width into PNG
   *
   * @return byte array of the new small PNG image
   * @throws ImageResizeException
   */
  public byte[] getSmallScaledImage(final byte[] initialImage) throws ImageResizeException {
    int width = Integer.parseInt(imageSetConfig.getImageProperty(
        ImageSetConfig.SMALL_IMAGE_PROP_KEY, ImageSetConfig.IMAGE_PROP_KEY_WIDTH));
    int quality = Integer.parseInt(imageSetConfig.getImageProperty(
        ImageSetConfig.SMALL_IMAGE_PROP_KEY, ImageSetConfig.IMAGE_PROP_KEY_QUALITY));
    try {
      preOperation(initialImage);
      return scaleFixWidth(width, quality);
    } finally {
      postOperation();
    }
  }

  /**
   * Scale the image to at most 600 pixels in either direction into a PNG
   *
   * @return byte array of the new medium size PNG image
   * @throws ImageResizeException
   */
  public byte[] getMediumScaledImage(final byte[] initialImage) throws ImageResizeException {
    int maxDim = Integer.parseInt(imageSetConfig.getImageProperty(
        ImageSetConfig.MEDIUM_IMAGE_PROP_KEY, ImageSetConfig.IMAGE_PROP_KEY_MAX_DIMENSION));
    int quality = Integer.parseInt(imageSetConfig.getImageProperty(
        ImageSetConfig.MEDIUM_IMAGE_PROP_KEY, ImageSetConfig.IMAGE_PROP_KEY_QUALITY));
    try {
      preOperation(initialImage);
      return scaleLargestDim(maxDim, quality);
    } finally {
      postOperation();
    }
  }

  /**
   * Don't scale the image, just return a PNG version of it
   *
   * @return byte array of the new PNG version of the image
   * @throws ImageResizeException
   */
  public byte[] getLargeScaledImage(final byte[] initialImage) throws ImageResizeException {
    int quality = Integer.parseInt(imageSetConfig.getImageProperty(
        ImageSetConfig.LARGE_IMAGE_PROP_KEY, ImageSetConfig.IMAGE_PROP_KEY_QUALITY));
    try {
      preOperation(initialImage);
      return performNoScaling(quality);
    } finally {
      postOperation();
    }
  }
}
