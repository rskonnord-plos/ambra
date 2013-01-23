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

package org.ambraproject.action.debug;

import org.ambraproject.action.InternalIpAction;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.filestore.impl.FileSystemImpl;
import org.ambraproject.filestore.impl.MogileFSImpl;
import org.ambraproject.web.SessionCounter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.configuration.ConfigurationStore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Action class that gathers a bunch of debugging information about a running
 * ambra instance.
 */
public class DebugInfoAction extends InternalIpAction {

  private static final Logger log = LoggerFactory.getLogger(DebugInfoAction.class);

  private Date timestamp;

  private String host;

  private String hostIp;

  private String buildInfo;

  private double jvmFreeMemory;

  private double jvmTotalMemory;

  private double jvmMaxMemory;

  private String tomcatVersion;

  private int sessionCount;

  private String configuration;

  private String dbUrl;

  private String dbUser;

  private String solrUrl;

  private String filestore;

  private String cmdLine;

  @Override
  public String execute() throws Exception {
    if (!checkAccess()) {
      return ERROR;
    }
    timestamp = new Date(System.currentTimeMillis());
    Runtime rt = Runtime.getRuntime();
    jvmFreeMemory = (double) rt.freeMemory() / (1024.0 * 1024.0);
    jvmTotalMemory = (double) rt.totalMemory() / (1024.0 * 1024.0);
    jvmMaxMemory = (double) rt.maxMemory() / (1024.0 * 1024.0);
    HttpServletRequest req = ServletActionContext.getRequest();
    tomcatVersion = ServletActionContext.getServletContext().getServerInfo();
    sessionCount = SessionCounter.getSessionCount();
    host = req.getLocalName();
    hostIp = req.getLocalAddr();
    buildInfo = generateBuildInfo();

    // The easiest way I found to get the URL and username for the DB.
    // It's not that easy and involves opening a connection...
    Context initialContext = new InitialContext();
    Context context = (Context) initialContext.lookup("java:comp/env");
    DataSource ds = (DataSource) context.lookup("jdbc/AmbraDS");
    Connection conn = null;
    try {
      conn = ds.getConnection();
      DatabaseMetaData metadata = conn.getMetaData();
      dbUrl = metadata.getURL();
      dbUser = metadata.getUserName();
    } finally {
      conn.close();
    }

    Configuration config = ConfigurationStore.getInstance().getConfiguration();
    FileStoreService filestoreService = (FileStoreService) context.lookup("ambra/FileStore");
    filestore = filestoreService.toString();
    solrUrl = (String) config.getProperty("ambra.services.search.server.url");
    configuration = dumpConfig(config);
    cmdLine = IOUtils.toString(new FileInputStream("/proc/self/cmdline"));

    return SUCCESS;
  }

  /**
   * Reads a file from the ambra-base .jar in the classpath.
   *
   * @param path filepath, relative to the jar
   * @return contents of the file, or null if it was not found
   * @throws IOException
   */
  private String readFileFromAmbraJar(String path) throws IOException {

    // Scan through all such files in the classpath, and pull out the one
    // associated with the ambra-base .jar.
    URL url = null;
    for (Enumeration<URL> manifests
        = Thread.currentThread().getContextClassLoader().getResources(path);
        manifests.hasMoreElements();) {
      url = manifests.nextElement();
      if (url.getFile().contains("/ambra-base-")) {
        break;
      }
    }
    InputStream is = url.openStream();
    String result = null;
    try {
      result = IOUtils.toString(is);
    } finally {
      is.close();
    }
    return result;
  }

  private static final Pattern YEAR_PATTERN = Pattern.compile("#.+20\\d{2}$");

  /**
   * Generates an informative message describing who built the application and when.
   */
  private String generateBuildInfo() throws IOException {
    String manifest = readFileFromAmbraJar("META-INF/MANIFEST.MF");
    String builder = null;
    if (manifest != null) {
      for (String line : manifest.split("\n")) {
        if (line != null && line.startsWith("Built-By:")) {
          builder = line.substring("Built-By:".length()).trim();
          break;
        }
      }
    }
    String properties = readFileFromAmbraJar(
        "META-INF/maven/org.ambraproject/ambra-base/pom.properties");
    String buildDate = null;
    String version = null;
    if (properties != null) {
      for (String line : properties.split("\n")) {

        // Currently maven puts the build date on the second line, prepended
        // with a #.  Look for anything that vaguely resembles a date.
        Matcher match = YEAR_PATTERN.matcher(line);
        if (match.matches()) {
          buildDate = line.substring(1).trim();
        } else {
          if (line != null && line.startsWith("version=")) {
            version = line.substring("version=".length()).trim();
          }
        }
      }
    }
    builder = builder == null ? "" : "by " + builder;
    buildDate = buildDate == null ? "" : "on " + buildDate;
    version = version == null ? "" : String.format("(%s)", version);
    return String.format("Built %s %s %s", builder, buildDate, version);
  }

  /**
   * Returns a string with information about many of the ambra.* properties
   * in a Configuration.  Intended to be informational-only, not exhaustive.
   */
  private String dumpConfig(Configuration config) {
    StringBuilder result = new StringBuilder();
    Iterator iter = config.getKeys();
    while (iter.hasNext()) {
      String key = (String) iter.next();

      // Don't display the virtual journal stuff; it's long and not particularly useful.
      if (key.startsWith("ambra.") && !key.startsWith("ambra.virtualJournals.")) {
        Object value = config.getProperty(key);

        // Attempt to dereference other properties referred to by "${foo}" notation.
        if (value instanceof String) {
          String valueStr = (String) value;
          if (valueStr.startsWith("${")) {
            String refKey = valueStr.substring(2, valueStr.length() - 1);
            Object refValue = config.getProperty(refKey);
            if (refValue != null) {
              value = String.format("%s -> %s", valueStr, refValue);
            }
          }
        }
        result.append(key)
            .append(": ")
            .append(value)
            .append('\n');
      }
    }
    return result.toString();
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getHost() {
    return host;
  }

  public String getHostIp() {
    return hostIp;
  }

  public String getBuildInfo() {
    return buildInfo;
  }

  public double getJvmFreeMemory() {
    return jvmFreeMemory;
  }

  public double getJvmTotalMemory() {
    return jvmTotalMemory;
  }

  public double getJvmMaxMemory() {
    return jvmMaxMemory;
  }

  public String getTomcatVersion() {
    return tomcatVersion;
  }

  public int getSessionCount() {
    return sessionCount;
  }

  public String getConfiguration() {
    return configuration;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public String getDbUser() {
    return dbUser;
  }

  public String getSolrUrl() {
    return solrUrl;
  }

  public String getFilestore() {
    return filestore;
  }

  public String getCmdLine() {
    return cmdLine;
  }
}
