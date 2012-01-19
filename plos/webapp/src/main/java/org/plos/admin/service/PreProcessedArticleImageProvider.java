/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.admin.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.plos.article.util.ArticleZip;
import org.plos.article.util.ImageProcessingException;
import org.plos.article.util.ImageSetConfig;

/**
 * PreProcessedArticleImageProvider - Provides pre-processed article images.
 * @author jkirton
 */
final class PreProcessedArticleImageProvider implements IProcessedArticleImageProvider {

  private final ArticleZip articleZip;

  /**
   * Constructor
   * @param articleZipFile
   */
  public PreProcessedArticleImageProvider(File articleZipFile) {
    super();
    this.articleZip = new ArticleZip(articleZipFile);
  }

  public ProcessedImageDataSource getProcessedArticleImage(URL url, ImageSetConfig imageSetConfig,
      String mimeType) throws ImageProcessingException {
    assert url != null && mimeType != null;

    ZipFile zf = null;
    try {
      zf = articleZip.getZipFile();
      assert zf != null;

      // get the unique img token name
      // url FORMAT: blah/[journal.||something?]{img file name w/o ext}/TIF 
      String path = url.getPath(), imgTknNme;
      path = URLDecoder.decode(path, "UTF-8");
      int lindx = path.lastIndexOf('/');
      assert lindx > 0;
      int slindx = path.lastIndexOf('.', lindx);
      assert slindx > 0 && lindx > slindx;
      imgTknNme = path.substring(slindx + 1, lindx);

      // find the assoc. zip entry for the desired processed image
      Enumeration<? extends ZipEntry> enm = zf.entries();
      while (enm.hasMoreElements()) {
        ZipEntry ze = enm.nextElement();
        if (isMatch(imgTknNme, mimeType, ze)) {
          // found it
          final long lsize = ze.getSize();
          if(lsize > Integer.MAX_VALUE) {
            // shouldn't happen but guard for it nonetheless
            throw new Error("Encountered zip file entry whose size exceeds the max allowed for int type");
          }
          byte[] buf = new byte[(int) lsize];
          InputStream is = articleZip.getZipFile().getInputStream(ze);
          try {
            new DataInputStream(is).readFully(buf, 0, buf.length);
          } finally {
            is.close();
          }
          return new ProcessedPngImageDataSource(buf, mimeType);
        }
      }
    } catch (ZipException e) {
      throw new ImageProcessingException(e);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    } finally {
      if (zf != null) {
        try {
          articleZip.close();
        } catch (Exception e) {
        }
      }
    }

    throw new ImageProcessingException("Un-resolvable article image " + url.toString()
        + " for article zip file: " + articleZip.toString());
  }

  /**
   * Is the given zip entry a match for the requsted processed image?
   * @param imgTknNme Unique token that is a sub-string of the target image zip entry name 
   * @param mimeType The processed image mime-type
   * @param ze The ZipEntry
   * @return <code>if the zip entry matches
   * @throws ImageProcessingException Upon unhandled mime-type or otherwise.
   */
  private boolean isMatch(String imgTknNme, String mimeType, ZipEntry ze)
      throws ImageProcessingException {

    String zen = ze.getName();
    assert zen != null && zen.length() > 4;

    if (zen.indexOf(imgTknNme) < 0) return false;

    String pfx;
    if ("PNG_S".equals(mimeType)) {
      pfx = "S_";
    }
    else if ("PNG_M".equals(mimeType)) {
      pfx = "M_";
    }
    else if ("PNG_L".equals(mimeType) || "PNG".equals(mimeType)) {
      pfx = "L_";
    }
    else {
      throw new ImageProcessingException("Unhandled processed image mime-type: " + mimeType);
    }

    return zen.startsWith(pfx);
  }

  @Override
  public String toString() {
    return getClass().getName();
  }
}
