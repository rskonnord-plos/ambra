/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.authentication;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.CASFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * A service class that holds information to connect to a service protected by CAS authentication.
 *
 * @author Pradeep Krishnan
 */
public class CASProtectedService implements ProtectedService {
  private static Log  log         = LogFactory.getLog(CASProtectedService.class);
  private URI         originalUri;
  private String      modifiedUri;
  private CASReceipt  receipt;
  private HttpSession session;

  /**
   * Creates a ProtectedService instance that is protected by CAS Single Signon. The ticket is
   * appended to the service URI if the HTTPSession contains a validated CASReceipt and this
   * service has set up a ProxyTicketReceptor.
   *
   * @param uri the service uri
   * @param session the CASReceipt corresponding to an authenticated user or null
   *
   * @throws IOException when there is an error in contacting CAS server.
   * @throws URISyntaxException If the service uri is invalid
   */
  public CASProtectedService(String uri, HttpSession session)
                      throws IOException, URISyntaxException {
    this.session   = session;
    this.receipt =
      (session == null) ? null : (CASReceipt) session.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
    this.originalUri = new URI(uri);

    // If no authenticated user, assume an unprotected service
    if (receipt == null)
      modifiedUri = uri;
    else
      buildServiceUri();
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getServiceUri
   */
  public String getServiceUri() {
    return modifiedUri;
  }

  /**
   * Returns false always.
   *
   * @return Returns false
   */
  public boolean requiresUserNamePassword() {
    return false;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getUserName() {
    return null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#getUserName
   */
  public String getPassword() {
    return null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#hasRenewableCredentials
   */
  public boolean hasRenewableCredentials() {
    // If no authenticated user, credentials are not renewable
    return receipt != null;
  }

  /*
   * @see org,topazproject.authentication.ProtectedService#renew
   */
  public boolean renew() {
    try {
      return (receipt == null) ? false : buildServiceUri();
    } catch (IOException e) {
      log.warn("Failed to acquire CAS proxy ticket for " + originalUri, e);

      return false;
    }
  }

  private boolean buildServiceUri() throws IOException {
    String pt    = getCASTicket();
    URI    uri   = originalUri;
    String query = uri.getQuery();

    if ((query == null) || (query.length() == 0))
      query = "ticket=" + pt;
    else
      query += ("&ticket=" + pt);

    try {
      uri =
        new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                query, uri.getFragment());
    } catch (URISyntaxException e) {
      throw new Error(e); // shouldn't happen
    }

    modifiedUri = uri.toString();

    if (log.isInfoEnabled())
      log.info("Acquired CAS proxy ticket for " + modifiedUri);

    return true;
  }

  private String getCASTicket() throws IOException {
    // get the PGT-IOU from the CAS receipt. (contained in the validate response from CAS server) 
    String pgtIou = receipt.getPgtIou();

    if (pgtIou == null)
      throw new IOException("No PGT-IOU found. Ensure that a ProxyServlet is configured "
                            + "and is accessible via HTTPS to the CAS server.");

    // use the PGT-IOU to lookup the PGT deposited at our ProxyTicketReceptor servlet
    // and then use the PGT to get a new PT from CAS server.
    String pt = ProxyTicketReceptor.getProxyTicket(pgtIou, originalUri.toString());

    if (pt != null)
      return pt;

    // The most likely cause here is that PGT expired. User needs to login again
    session.removeAttribute(CASFilter.CAS_FILTER_RECEIPT);
    session.removeAttribute(CASFilter.CAS_FILTER_USER);
    receipt   = null;
    session   = null;
    throw new NoProxyTicketException("No proxy ticket: PGT may have expired.");
  }

  /**
   * An exception to indicate that we can't get a proxy ticket. This would invariably mean that the
   * user has to login to CAS server again and retry the request.
   */
  public static class NoProxyTicketException extends IOException {
    /**
     * Creates a NoProxyTicketException instance.
     *
     * @param msg the exception message
     */
    public NoProxyTicketException(String msg) {
      super(msg);
    }
  }
}
