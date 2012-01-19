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

/**
 * This class performs the operations of conversion and resizing of images.
 * The final image format is assumed to be png. The desired image sizes are
 * passed to the public methods.
 *
 * @author stevec
 */
public class ImageResizeService {
  private static final Log log = LogFactory.getLog(ImageResizeService.class);

  private int height;
  private int width;
  private String directory;
  private String inputImageFileName;
  private String outputImageFileName;
  private File location;
  private File inputImageFile;
  private File outputImageFile;

  public ImageResizeService() throws ImageResizeException {
    final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();
    if (configuration.isEmpty()) {
      throw new ImageResizeException("ERROR: configuration has no property values");
    }

    final String directory = configuration.getString("topaz.utilities.image-magick.temp-directory");
    if (directory == null) {
      throw new ImageResizeException("ERROR: configuration failed to associate a value with " +
                                     "property topaz.utilities.image-magick.temp-directory");
    }

    setDirectory(directory);
    final Integer disambiguation = new Integer(hashCode());
    inputImageFileName = "_" + disambiguation + "current";
    outputImageFileName = "_" + disambiguation + "final.png";
  }

  /**
   * Mutator for member directory (from which member location is constructed)
   * where the image files are to be created.
   *
   * @param directory - the path to this directory
   */
  public void setDirectory(final String directory) throws ImageResizeException {
    this.directory = directory;
    location = new File(this.directory);

    if (!location.isDirectory()) {
      throw new ImageResizeException("ERROR: " + this.getClass().getCanonicalName() +
                                     " requires a valid directory");
    }

    if (!location.canRead()) {
      throw new ImageResizeException("ERROR: " + this.getClass().getCanonicalName() +
                                     " requires a directory from which the process may read");
    }

    if (!location.canWrite()) {
      throw new ImageResizeException("ERROR: " + this.getClass().getCanonicalName() +
                                     " requires a directory to which the process may write.");
    }
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

        throw new ImageResizeException("ERROR: unable to create temporary file: " +
                                       inputImageFile.getCanonicalPath());
      }

      if (outputImageFile.exists()) {
        throw new ImageResizeException("ERROR: temporary file: " +
                                       outputImageFile.getCanonicalPath() +
                                       " already exists");
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
        throw new ImageResizeException("ERROR: unable to delete temporary file: " +
                                       inputImageFileName);
      }
    } catch (SecurityException e) {
      throw new ImageResizeException(e);
    } finally {
      try {
        if (outputImageFile.exists() && !outputImageFile.delete()) {
          throw new ImageResizeException("ERROR: unable to delete temporary file: " +
                                         outputImageFileName);
        }
      } catch (SecurityException e) {
        throw new ImageResizeException(e);
      }
    }
  }

  /**
   * Convert the image and apply no scaling
   *
   * @throws ImageResizeException
   */
  private void createScaledImage() throws ImageResizeException {
    createScaledImage(this.width,this.height);
  }

  /**
   * Convert the image and apply desired scaling
   *
   * @param newWidth - the desired width to which the image should be scaled
   * @param newHeight - the desired height to which the image should be scaled
   * @throws ImageResizeException
   */
  private void createScaledImage(final int newWidth,final int newHeight)
        throws ImageResizeException {
    final ImageMagicExecUtil util = new ImageMagicExecUtil();
    final boolean status = util.convert(this.inputImageFile,outputImageFile,newWidth,newHeight);

    if (!status) {
      log.fatal("ERROR: operation convert failed");
      throw new ImageResizeException(new Exception("operation convert failed"));
    }
  }

  /**
   * Read the transformed PNG image from member outputImageFile, write it to a
   * buffer and return the resulting byte array.
   *
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private synchronized byte[] getPNGByteArray() throws ImageResizeException {
    assert(outputImageFile != null && outputImageFile.exists() && outputImageFile.length() > 0);

    final byte[] result;
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    try {
      final InputStream in = new FileInputStream(this.outputImageFile);
      final long fileSize = this.outputImageFile.length();

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
  private byte[] performNoScaling () throws ImageResizeException {
    createScaledImage();
    return getPNGByteArray();
  }

  /**
   * Scale the image to a maximum height of fixHeight (while preserving the
   * aspect ratio) and return a PNG version of it
   *
   * @param fixHeight
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleFixHeight(final float fixHeight) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (this.height > fixHeight) {
      scale = fixHeight / this.height;
      newHeight = (int) fixHeight;
      newWidth = (int) (this.width * scale);
    } else {
      scale = 1;
      newHeight = this.height;
      newWidth = this.width;
    }

    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }

  /**
   * Scale the image to a maximum width of fixWidth (while preserving the
   * aspect ratio) and return a PNG version of it
   *
   * @param fixWidth
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleFixWidth(final float fixWidth) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (this.width > fixWidth) {
      scale = fixWidth / this.width;
      newWidth = (int) fixWidth;
      newHeight = (int) (this.height * scale);
    } else {
      scale = 1;
      newWidth = width;
      newHeight = this.height;
    }

    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }

  /**
   * Scale the image to a maximum of oneSide in both directions (while
   * preserving the aspect ratio) and return a PNG version of it
   *
   * @param oneSide
   * @return byte array of the scaled and transformed (PNG) image
   * @throws ImageResizeException
   */
  private byte[] scaleLargestDim(final float oneSide) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if ((this.height > this.width) && (this.height > oneSide)) {
      scale = oneSide / this.height;
      newHeight = (int) oneSide;
      newWidth = (int) (this.width * scale);
    } else if (this.width > oneSide) {
      scale = oneSide / this.width;
      newWidth = (int) oneSide;
      newHeight = (int) (this.height * scale);
    } else {
      scale = 1;
      newWidth = this.width;
      newHeight = this.height;
    }

    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }

  /**
   * Scale the image to 70 pixels in width into PNG
   *
   * @return byte array of the new small PNG image
   * @throws ImageResizeException
   */
  public byte[] getSmallScaledImage(final byte[] initialImage) throws ImageResizeException {
    try {
      preOperation(initialImage);
      return scaleFixWidth(70.0f);
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
    try {
      preOperation(initialImage);
      return scaleLargestDim(600.0f);
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
    try {
      preOperation(initialImage);
      return performNoScaling();
    } finally {
      postOperation();
    }
  }
}
