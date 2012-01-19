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

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.PNGEncodeParam;

public class ImageResizeService {
  
  private static final Log log = LogFactory.getLog(ImageResizeService.class);
  
  // private BufferedImage image;
  private RenderedOp srcImage;
  
  private PNGEncodeParam encodeParam;
  
  private boolean isCMYK = false;
  
  private RenderingHints hints;
  
  public ImageResizeService() {
    // ImageIO.setUseCache(true);
    // ImageIO.setCacheDirectory(null);
    
    hints = new RenderingHints(null);//RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    //hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    /*hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);*/
    hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    /*hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    hints.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_ZERO));*/
    //hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
  }
  
  /**
   * Store the contents of the image located by the url parameter into the object
   * 
   * @param url
   * @throws MalformedURLException
   */
  public void captureImage(String url) throws MalformedURLException {
    srcImage = JAI.create("URL", new ParameterBlock().add(new URL(url)));
    if (log.isDebugEnabled()) {
      log.debug("retrieved image from URL: " + url.toString());
    }
    if (srcImage.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
      isCMYK = true;
      srcImage = convertCMYKtoRGB(srcImage);
      if (log.isDebugEnabled()) {
        log.debug("Image is of type CMYK");
      }
    }
  }
  
  private RenderedOp createBoxFilterScaledImage (float widthPercent, float heightPercent) {
    RenderedOp transformedImage;
    
    // Scale the image to half its size in each direction
    ParameterBlock pb;
    
    pb = new ParameterBlock();
    pb.addSource(srcImage);
    pb.add(3);
    pb.add(3);
    pb.add(3/2);
    pb.add(3/2);
    pb.add(hints);
    if (log.isDebugEnabled()) {
      log.debug("Applying BoxFilter");
    }
    transformedImage = JAI.create("BoxFilter", pb);
     
    pb = new ParameterBlock();
    pb.addSource(transformedImage);
    pb.add(widthPercent);
    pb.add(heightPercent);
    pb.add(0.0f);
    pb.add(0.0f);
    pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
    pb.add(hints);
    if (log.isDebugEnabled()) {
      log.debug("Applying Scaling");
    }
    transformedImage = JAI.create("scale", pb);

    /*    } else {
      pb = new ParameterBlock();
      pb.addSource(srcImage);
      pb.add((double) widthPercent);
      pb.add((double) heightPercent);
      pb.add(hints);
      if (log.isDebugEnabled()) {
        log.debug("SubsampleAverage");
      }
      transformedImage = JAI.create("SubsampleAverage", pb);
    //}
      
      /*pb = new ParameterBlock();
      pb.addSource(srcImage);
      pb.add((int)1 / widthPercent);
      pb.add((int) 1/ heightPercent);
      pb.add(hints);
      if (log.isDebugEnabled()) {
        log.debug("SubsampleAverage");
      }
      transformedImage = JAI.create("FilteredSubsample", pb);*/
      
    return transformedImage;
  }
  
  private RenderedOp createSubsampleAverageScaledImage (double widthPercent, double heightPercent) {
    RenderedOp transformedImage;
    
    // Scale the image to half its size in each direction
    ParameterBlock pb;
    
    /*if ((isCMYK)
        && ((srcImage.getWidth() * widthPercent > 512) || (srcImage.getHeight() * heightPercent > 512))) {
      pb = new ParameterBlock();
      pb.addSource(srcImage);
      pb.add(5);
      pb.add(5);
      if (log.isDebugEnabled()) {
        log.debug("Applying BoxFilter");
      }
      transformedImage = JAI.create("BoxFilter", pb);
      
      pb = new ParameterBlock();
      pb.addSource(transformedImage);
      pb.add(widthPercent);
      pb.add(heightPercent);
      pb.add(0.0f);
      pb.add(0.0f);
      pb.add(new InterpolationBicubic2(256));
      pb.add(hints);
      if (log.isDebugEnabled()) {
        log.debug("Applying Scaling");
      }
      transformedImage = JAI.create("Scale", pb);
    } else {*/
      pb = new ParameterBlock();
      pb.addSource(srcImage);
      pb.add(widthPercent);
      pb.add(heightPercent);
      pb.add(hints);
      if (log.isDebugEnabled()) {
        log.debug("SubsampleAverage");
      }
      transformedImage = JAI.create("SubsampleAverage", pb);
    //}
      
      /*pb = new ParameterBlock();
      pb.addSource(srcImage);
      pb.add((int)1 / widthPercent);
      pb.add((int) 1/ heightPercent);
      pb.add(hints);
      if (log.isDebugEnabled()) {
        log.debug("SubsampleAverage");
      }
      transformedImage = JAI.create("FilteredSubsample", pb);*/
      
    return transformedImage;
  }
  
  
  
  private byte[] renderedOpToPNGByteArray(RenderedOp inImage) {
    byte[] array = new byte[inImage.getHeight() * inImage.getWidth()];
    ByteArrayOutputStream stream = new ByteArrayOutputStream(array.length);
    JAI.create("encode", inImage, stream, "PNG");
    return stream.toByteArray();
    
  }
  
  private byte[] scaleFixHeight(float fixHeight, boolean isSubsample) {
    int height = srcImage.getHeight();
    float scale;
    if (height > fixHeight) {
      scale = fixHeight / height;
    } else {
      scale = 1;
    }
    RenderedOp newImage;
    if (scale == 1) {
      newImage = srcImage;
    } else {
      if (isSubsample) {
        newImage = createSubsampleAverageScaledImage(scale, scale);
      } else {
        newImage = createBoxFilterScaledImage(scale, scale);
      }
    }
    return renderedOpToPNGByteArray(newImage);
  }
  
  private byte[] scaleFixWidth(float fixWidth, boolean isSubsample) {
    int width = srcImage.getWidth();
    float scale;
    if (width > fixWidth) {
      scale = fixWidth / width;
    } else {
      scale = 1;
    }
    RenderedOp newImage;
    if (scale == 1) {
      newImage = srcImage;
    } else {
      if (isSubsample) {
        newImage = createSubsampleAverageScaledImage(scale, scale);
      } else {
        newImage = createBoxFilterScaledImage(scale, scale);
      }
    }
    return renderedOpToPNGByteArray(newImage);
  }
  
  private byte[] scaleLargestDim(float oneSide, boolean isSubsample) {
    if (log.isDebugEnabled()) {
      log.debug("oneSide = " + oneSide);
    }
    
    int height = srcImage.getHeight();
    int width = srcImage.getWidth();
    
    float scale;
    if ((height > width) && (height > oneSide)) {
      scale = oneSide / height;
    } else if (width > oneSide) {
      scale = oneSide / width;
    } else {
      scale = 1;
    }
    RenderedOp newImage;
    if (scale == 1) {
      newImage = srcImage;
    } else {
      if (isSubsample) {
        newImage = createSubsampleAverageScaledImage(scale, scale);
      } else {
        newImage = createBoxFilterScaledImage(scale, scale);
      }    }
    return renderedOpToPNGByteArray(newImage);
  }
  
  /**
   * Scale the image to 70 pixels in width into PNG
   * 
   * @return byte array of the new small image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getSmallImageBoxScaled() throws FileNotFoundException, IOException {
    return scaleFixWidth(70.0f, false);
  }

  /**
   * Scale the image to 70 pixels in width into PNG
   * 
   * @return byte array of the new small image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getSmallImageSubsample() throws FileNotFoundException, IOException {
    return scaleFixWidth(70.0f, true);
  }
  
  
  /**
   * Scale the image to at most 600 pixels in either direction into a PNG
   * 
   * @return byte array of the new medium size image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getMediumImageBoxScaled() throws FileNotFoundException, IOException {
    return scaleLargestDim(600.0f, false);
  }

  /**
   * Scale the image to at most 600 pixels in either direction into a PNG
   * 
   * @return byte array of the new medium size image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getMediumImageSubsample() throws FileNotFoundException, IOException {
    return scaleLargestDim(600.0f, true);
  }
  
  
  /**
   * Don't scale the image, just return a PNG version of it
   * 
   * @return byte array of the new PNG version of the image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getLargeImageBoxScaled() throws FileNotFoundException, IOException {
    return renderedOpToPNGByteArray(srcImage);
  }

  /**
   * Don't scale the image, just return a PNG version of it
   * 
   * @return byte array of the new PNG version of the image
   * @throws FileNotFoundException
   * @throws IOException
   */
  public byte[] getLargeImageSubsample() throws FileNotFoundException, IOException {
    return renderedOpToPNGByteArray(srcImage);
  }
  
  /**
   * 
   * 
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
 /* public byte[] getPageWidthImage() throws FileNotFoundException, IOException {
    return scaleFixWidth(400.0f);
  }
  
  public byte[] getInlineImage() throws FileNotFoundException, IOException {
    return scaleFixHeight(21.0f);
  }*/
  
  private RenderedOp convertCMYKtoRGB(RenderedOp op) {
    try {
      ParameterBlockJAI pb;
      ICC_Profile cmyk_profile = ICC_Profile.getInstance("CMYK.pf"); // CMYK.pf
      
      ICC_ColorSpace cmyk_icp = new ICC_ColorSpace(cmyk_profile);
      ColorModel cmyk_cm = RasterFactory.createComponentColorModel(op.getSampleModel()
          .getDataType(), cmyk_icp, false, false, Transparency.OPAQUE);
      
      ImageLayout cmyk_il = new ImageLayout();
      cmyk_il.setColorModel(cmyk_cm);
      RenderingHints cmyk_hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, cmyk_il);
      pb = new ParameterBlockJAI("format");
      pb.addSource(op);
      pb.setParameter("datatype", op.getSampleModel().getDataType());
      op = JAI.create("format", pb, cmyk_hints);
      ColorSpace rgb_icp = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      ColorModel rgb_cm = RasterFactory.createComponentColorModel(
          op.getSampleModel().getDataType(), rgb_icp, false, false, Transparency.OPAQUE);
      ImageLayout rgb_il = new ImageLayout();
      rgb_il.setSampleModel(rgb_cm.createCompatibleSampleModel(op.getWidth(), op.getHeight()));
      RenderingHints rgb_hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, rgb_il);
      pb = new ParameterBlockJAI("colorconvert");
      pb.addSource(op);
      pb.setParameter("colormodel", rgb_cm);
      op = JAI.create("colorconvert", pb, rgb_hints);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return op;
    
  }
}
