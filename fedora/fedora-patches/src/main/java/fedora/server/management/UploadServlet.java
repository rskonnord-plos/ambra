/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Educational Community License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.opensource.org/licenses/ecl1.txt">
 * http://www.opensource.org/licenses/ecl1.txt.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2002-2006 by
 * The Rector and Visitors of the University of Virginia and Cornell University.
 * All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */
package fedora.server.management;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;

import fedora.common.Constants;

import fedora.server.Context;
import fedora.server.Logging;
import fedora.server.ReadOnlyContext;
import fedora.server.Server;

import fedora.server.errors.InitializationException;
import fedora.server.errors.ServerException;
import fedora.server.errors.authorization.AuthzException;
import fedora.server.errors.servletExceptionExtensions.RootException;

/**
 * Accepts and HTTP Multipart POST of a file from an authorized user, and if  successful,
 * returns a status of "201 Created" and a text/plain  response with a single line containing an
 * opaque identifier that can be  used to later submit to the appropriate API-M method. If it
 * fails it  will return a non-201 status code with a text/plain explanation. The submitted file
 * must be named "file", must not be accompanied by any other parameters. Note: This class relies
 * on a patched version of cos.jar that provides an alternate constructor for MultiPartParser,
 * allowing for the upload of  files over 2GB in size.
 *
 * @author cwilper@cs.cornell.edu
 * @version $Id: UploadServlet.java,v 1.17 2005/10/21 16:22:55 cwilper Exp $
 */
public class UploadServlet extends HttpServlet implements Logging {
  /**
   * Content type for all responses.
   */
  private static final String CONTENT_TYPE_TEXT = "text/plain";

  /**
   * Instance of the Fedora server.
   */
  private static Server s_server = null;

  /**
   * Instance of Management subsystem (for storing uploaded files).
   */
  private static Management s_management = null;

  public static final String ACTION_LABEL = "Upload";

  /**
   * The servlet entry point.  http://host:port/fedora/management/upload
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {
    String  actionLabel = "uploading a file";
    Context context     = ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, request);

    try {
      MultipartParser parser = new MultipartParser(request, Integer.MAX_VALUE, true, false, null);
      Part            part   = parser.readNextPart();

      if ((part != null) && part.isFile()) {
        if (part.getName().equals("file")) {
          String temp = saveAndGetId(context, (FilePart) part);
          sendResponse(HttpServletResponse.SC_CREATED, temp, response);
        } else {
          sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Content must be named \"file\"",
                       response);
        }
      } else {
        if (part == null) {
          sendResponse(HttpServletResponse.SC_BAD_REQUEST, "No data sent.", response);
        } else {
          sendResponse(HttpServletResponse.SC_BAD_REQUEST, "No extra parameters allowed", response);
        }
      }
    } catch (AuthzException ae) {
      throw RootException.getServletException(ae, request, ACTION_LABEL, new String[0]);
    } catch (Exception e) {
      e.printStackTrace();
      sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                   e.getClass().getName() + ": " + e.getMessage(), response);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException {
    String id = request.getQueryString();

    try {
      InputStream in = s_management.getTempStream(id);

      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("application/octet-stream");

      /*
       *  Hack to get to the actual tmp file so that we can set the Content-Length.
       *  Without the Content-Length header, the stream gets truncated at 8192 bytes.
       */
      File tmpDir = new File(getServer().getHomeDir(), "management/upload");
      String internalId = id.substring(11);
      File tmpFile = new File(tmpDir, internalId);
      response.setHeader("Content-Length", "" + tmpFile.length());

      OutputStream out = response.getOutputStream();

      byte[]       buf = new byte[16384];
      int          len = 0;

      while ((len = in.read(buf, 0, buf.length)) != -1) {
        out.write(buf, 0, len);
      }

      out.close();
    } catch (ServerException sre) {
      logFine("File not found '" + id + "': " + sre);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, id);
    }
  }

  public void sendResponse(int status, String message, HttpServletResponse response) {
    try {
      if (status == HttpServletResponse.SC_CREATED) {
        logFine("Successful upload, id=" + message);
      } else {
        logWarning("Failed upload: " + message);
      }

      response.setStatus(status);
      response.setContentType(CONTENT_TYPE_TEXT);

      PrintWriter w = response.getWriter();
      w.println(message);
    } catch (Exception e) {
      logSevere("Could not send a response: " + e.getClass().getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  private String saveAndGetId(Context context, FilePart filePart)
                       throws ServerException, IOException {
    return s_management.putTempStream(context, filePart.getInputStream());
  }

  /**
   * Initialize servlet.  Gets a reference to the fedora Server object.
   *
   * @throws ServletException If the servet cannot be initialized.
   */
  public void init() throws ServletException {
    try {
      s_server       = Server.getInstance(new File(System.getProperty("fedora.home")), false);
      s_management   = (Management) s_server.getModule("fedora.server.management.Management");

      if (s_management == null) {
        throw new ServletException("Unable to get Management module from server.");
      }
    } catch (InitializationException ie) {
      throw new ServletException("Unable to get Fedora Server instance." + ie.getMessage());
    }
  }

  public final Server getServer() {
    return s_server;
  }

  /**
   * Logs a SEVERE message, indicating that the server is inoperable or unable to start.
   *
   * @param message The message.
   */
  public final void logSevere(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logSevere(m.toString());
  }

  public final boolean loggingSevere() {
    return getServer().loggingSevere();
  }

  /**
   * Logs a WARNING message, indicating that an undesired (but non-fatal) condition occured.
   *
   * @param message The message.
   */
  public final void logWarning(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logWarning(m.toString());
  }

  public final boolean loggingWarning() {
    return getServer().loggingWarning();
  }

  /**
   * Logs an INFO message, indicating that something relatively uncommon and interesting
   * happened, like server or module startup or shutdown, or a periodic job.
   *
   * @param message The message.
   */
  public final void logInfo(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logInfo(m.toString());
  }

  public final boolean loggingInfo() {
    return getServer().loggingInfo();
  }

  /**
   * Logs a CONFIG message, indicating what occurred during the server's (or a module's)
   * configuration phase.
   *
   * @param message The message.
   */
  public final void logConfig(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logConfig(m.toString());
  }

  public final boolean loggingConfig() {
    return getServer().loggingConfig();
  }

  /**
   * Logs a FINE message, indicating basic information about a request to the server (like
   * hostname, operation name, and success or failure).
   *
   * @param message The message.
   */
  public final void logFine(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logFine(m.toString());
  }

  public final boolean loggingFine() {
    return getServer().loggingFine();
  }

  /**
   * Logs a FINER message, indicating detailed information about a request to the server
   * (like the full request, full response, and timing information).
   *
   * @param message The message.
   */
  public final void logFiner(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logFiner(m.toString());
  }

  public final boolean loggingFiner() {
    return getServer().loggingFiner();
  }

  /**
   * Logs a FINEST message, indicating method entry/exit or extremely verbose information
   * intended to aid in debugging.
   *
   * @param message The message.
   */
  public final void logFinest(String message) {
    StringBuffer m = new StringBuffer();
    m.append(getClass().getName());
    m.append(": ");
    m.append(message);
    getServer().logFinest(m.toString());
  }

  public final boolean loggingFinest() {
    return getServer().loggingFinest();
  }
}
