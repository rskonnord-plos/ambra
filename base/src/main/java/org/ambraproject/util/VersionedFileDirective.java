/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.util;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.configuration.ConfigurationStore;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for templates that render links to static files.  Subclasses can use the value of getFingerprint to
 * modify the link, such that whenever the file changes, the link will also change.  In this way, browser caching
 * should function correctly--the browser will use the copy from its cache if and only if the file remains unchanged
 * on the server.
 * <p/>
 * This class uses caching internally for performance, so a server restart is necessary when any static files
 * referenced by subclasses are changed.
 */
public abstract class VersionedFileDirective implements TemplateDirectiveModel {

  private static final Logger log = LoggerFactory.getLogger(VersionedFileDirective.class);

  /**
   * Frequency with which the fingerprintCache is cleared, in milliseconds.  mbaehr said that 15 minutes is a
   * value that is frequently used throughout our site.
   */
  private static final int CACHE_PURGE_DELAY = 15 * 60 * 1000;

  /**
   * Cache used to store fingerprint values.  We do this since it might significantly impact the performance of the
   * server if we re-checksum every file every time the server gets a request for it.
   * <p/>
   * If the number of .js and .css files in the entire application becomes large, we might consider changing this
   * to a cache that evicts elements, instead of keeping everything in memory.
   */
  private Map<String, String> fingerprintCache = new ConcurrentHashMap<String, String>();

  /**
   * Timer used to clear out fingerprintCache occasionally.  This is because we cannot assume that there will be a
   * server restart when new .js or .css files are deployed.
   */
  private Timer cachePurgeTimer;

  public VersionedFileDirective() {
    cachePurgeTimer = new Timer("fingerprintCache purging timer", true);
    cachePurgeTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        fingerprintCache.clear();
        log.info("timer task cleared fingerprintCache");
      }
    }, CACHE_PURGE_DELAY, CACHE_PURGE_DELAY);
  }

  /**
   * Produces a String value suitable for rendering in HTML for the given binary data.
   */
  private String encodeText(byte[] data) {
    BASE64Encoder encoder = new BASE64Encoder();
    String base64 = encoder.encodeBuffer(data);

    // Make the returned value a little prettier by replacing slashes with underscores, and removing the trailing
    // "=".
    base64 = base64.replace('/', '_').trim();
    return base64.substring(0, base64.length() - 1);
  }

  /**
   * Returns a base64-encoded fingerprint of the contents of a file.
   *
   * @param filepath the real filesystem path of the file being served
   * @return base64-encoded fingerprint of the file's contents
   * @throws IOException
   * @throws TemplateException
   */
  String getFingerprint(String filepath) throws IOException, TemplateException {
    String cached = fingerprintCache.get(filepath);
    if (cached != null) {
      return cached;
    }

    byte[] buffer = IOUtils.toByteArray(new FileInputStream(filepath));
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException nsae) {
      throw new TemplateModelException(nsae);
    }
    messageDigest.update(buffer);
    String fingerprint = encodeText(messageDigest.digest());
    fingerprintCache.put(filepath, fingerprint);
    return fingerprint;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (params.get("file") == null) {
      throw new TemplateModelException("file parameter is required");
    }
    String filename = params.get("file").toString();

    // We need a ServletContext in order to convert application-base paths into real paths.  It's a little
    // cumbersome to get one from here.
    HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
    String path;
    try {
      path = getRealPath(filename, request);
    } catch (ServletException se) {
      throw new TemplateModelException(se);
    }

    // There are some style and script tags in the codebase that refer to non-existent files.  Just do nothing
    // in these cases.
    File test = new File(path);
    if (test.exists()) {
      environment.getOut().write(getLink(filename, getFingerprint(path), params));
    }
  }

  /**
   * Resolves the filesystem path based on a web request path.  This is made complicated by
   * {@link org.ambraproject.web.VirtualJournalMappingFilter}, which remaps certain paths to journal-specific paths.
   *
   * @param path The original web request path
   * @param request The request object we're currently serving.  Note that this request will have a different path
   *     than the path param.
   * @return The path to the resource on the filesystem
   * @throws ServletException
   */
  private String getRealPath(final String path, HttpServletRequest request) throws ServletException {
    VirtualJournalContext vjc =
        (VirtualJournalContext) request.getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    ServletContext servletContext = request.getSession().getServletContext();

    // This is somewhat of a hack.  VirtualJournalContext.mapRequest was originally written to be called with an
    // HttpServletRequest (supplied by VirtualJournalMappingFilter).  To reuse the code, we create a fake request
    // for the resource in question, populating only the fields that matter.
    HttpServletRequest fakeRequest = new HttpServletRequestWrapper(request) {
      public String getRequestURI() {
        return path;
      }

      public String getContextPath() {
        return "";
      }

      public String getServletPath() {
        return path;
      }

      public String getPathInfo() {
        return null;
      }
    };
    Configuration configuration = ConfigurationStore.getInstance().getConfiguration();
    HttpServletRequest mappedRequest = vjc.mapRequest(fakeRequest, configuration,
        servletContext);

    // If getPathInfo is null, the request was not remapped, and is intended to be served from the root context.  In
    // that case we can get the real path from the ServletContext.
    return mappedRequest.getPathInfo() != null ? mappedRequest.getPathInfo() : servletContext.getRealPath(path);
  }

  /**
   * Returns the link that will be rendered.
   *
   * @param filename the static file being served (relative to the webapp base context)
   * @param fingerprint checksum of the file's contents
   * @param params parameters passed to the directive
   * @return HTML link to the static file.  The exact form will be subclass-specific (for instance, a link tag for
   *     css or a script tag for javascript).
   */
  public abstract String getLink(String filename, String fingerprint, Map params) throws TemplateException;
}
