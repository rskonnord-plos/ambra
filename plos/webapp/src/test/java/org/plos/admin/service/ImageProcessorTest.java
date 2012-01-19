package org.plos.admin.service;

import java.util.regex.Pattern;

import org.plos.article.util.ImageProcessor;

import junit.framework.TestCase;

/**
 * ImageProcessorTest
 * @author jkirton
 */
public class ImageProcessorTest extends TestCase {

  /**
   * Tests the {@link ImageProcessor#procImgPfxPattern} pattern.
   */
  public void testPattern() {
    Pattern p = ImageProcessor.procImgPfxPattern;
    assert p.matcher("S_pone.000223344.gg.png").find() : "S_pone.000223344.gg.png did not match";
    assert p.matcher("M_pone.000223344.gg.png").find() : "M_pone.000223344.gg.png did not match";
    assert p.matcher("L_pone.000223344.gg.png").find() : "L_pone.000223344.gg.png did not match";
    assert !p.matcher("Z_pone.000223344.gg.png").find() : "Z_pone.000223344.gg.png matched and should not have";
    assert !p.matcher("pone.S_000223344.gg.png").find() : "pone.S_000223344.gg.png matched and should not have";
    assert !p.matcher("S_").find() : "S_ matched and should not have";
  }
}
