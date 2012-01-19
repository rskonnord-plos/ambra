/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

import java.io.IOException;

import java.lang.ref.SoftReference;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;

import org.plos.configuration.ConfigurationStore;

import org.plos.user.UserAccountsInterceptor;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.apache.struts2.ServletActionContext;

/**
 * A TopazContext implementation that wraps a jax-rpc context.
 *
 * @author Pradeep Krishnan
 */
public class WSTopazContext implements TopazContext {
  private static final String        serverName;
  private static final URI           objectBaseUri;
  private static final URI           fedoraBaseUri;
  private static final Configuration apimConfig;
  private static final Configuration upldConfig;

  static {
    Configuration root = ConfigurationStore.getInstance().getConfiguration();
    apimConfig   = root.subset("topaz.services.fedora");
    upldConfig   = root.subset("topaz.services.fedoraUploader");
    serverName   = root.getString("topaz.server.hostname");

    String objectBase = root.getString("topaz.objects.base-uri");
    String fedoraBase = root.getString("topaz.services.fedora.uri");

    ItqlHelper.validateUri(upldConfig.getString("uri"), "topaz.services.fedoraUploader.uri");

    objectBaseUri = ItqlHelper.validateUri(objectBase, "topaz.objects.base-uri");

    URI uri = ItqlHelper.validateUri(fedoraBase, "topaz.services.fedora.uri");

    if (uri.getHost().equals("localhost")) {
      try {
        uri = new URI(uri.getScheme(), null, serverName, uri.getPort(), uri.getPath(), null, null);
      } catch (URISyntaxException use) {
        throw new Error(use); // Can't happen
      }
    }

    fedoraBaseUri = uri;
  }

  private final String sessionKey;

  private static class TLC {
    private boolean     active  = false;
    private FedoraAPIM  apim    = null;
    private Uploader    upld    = null;
    private HandleCache cache   = null;
    private HttpSession session = null;
  }

  private final ThreadLocal tc =
    new ThreadLocal() {
      protected Object initialValue() {
        return new TLC();
      }
    };

/**
   * Creates a new WSTopazContext object.
   *
   * @param sessionNs The namespace to use for storing objects in HttpSession
   */
  public WSTopazContext(String sessionNs) {
    sessionKey = sessionNs + ".handle-cache";
  }

  /*
   * inherited javadoc
   */
  public void init(Object object) {
  }

  /*
   * inherited javadoc
   */
  public void destroy() {
  }

  /*
   * inherited javadoc
   */
  public void activate() {
    ((TLC) tc.get()).active = true;
  }

  /*
   * inherited javadoc
   */
  public void passivate() {
    TLC tlc = (TLC) tc.get();
    tlc.active = false;

    if (tlc.apim != null) {
      tlc.cache.returnObject(tlc.apim);
      tlc.apim = null;
    }

    if (tlc.upld != null) {
      tlc.cache.returnObject(tlc.upld);
      tlc.upld = null;
    }

    tlc.cache     = null;
    tlc.session   = null;
  }

  /*
   * inherited javadoc
   */
  public boolean isActive() {
    return ((TLC) tc.get()).active;
  }

  /*
   * inherited javadoc
   */
  public ServletContext getServletContext() {
    return ServletActionContext.getServletContext();
  }

  /*
   * inherited javadoc
   */
  public Principal getUserPrincipal() throws IllegalStateException {
    HttpSession session = getHttpSession();

    if (session == null)
      return null;

    final String userName = getUserName();

    if (userName == null)
      return null;

    return new Principal() {
        public String getName() {
          return userName;
        }
      };
  }

  /*
   * inherited javadoc
   */
  public HttpSession getHttpSession() throws IllegalStateException {
    if (!((TLC) tc.get()).active)
      throw new IllegalStateException("not active");

    return ServletActionContext.getRequest().getSession();
  }

  /*
   * inherited javadoc
   */
  public String getUserName() throws IllegalStateException {
    HttpSession session = getHttpSession();

    if (session == null)
      return null;

    return (String) session.getAttribute(UserAccountsInterceptor.USER_KEY);
  }

  /*
   * inherited javadoc
   */
  public String getServerName() {
    return serverName;
  }

  /*
   * inherited javadoc
   */
  public URI getObjectBaseUri() {
    return objectBaseUri;
  }

  /*
   * inherited javadoc
   */
  public URI getFedoraBaseUri() {
    return fedoraBaseUri;
  }

  /*
   * inherited javadoc
   */
  public FedoraAPIM getFedoraAPIM() throws RemoteException, IllegalStateException {
    TLC tlc = (TLC) tc.get();

    if (tlc.apim != null)
      return tlc.apim;

    tlc.apim = (FedoraAPIM) getHandle(FedoraAPIM.class);

    if (tlc.apim != null)
      return tlc.apim;

    try {
      ProtectedService svc = ProtectedServiceFactory.createService(apimConfig, tlc.session);
      tlc.apim = APIMStubFactory.create(svc);
    } catch (URISyntaxException e) {
      throw new Error(e); // already tested; so shouldn't happend
    } catch (MalformedURLException e) {
      throw new Error(e);
    } catch (ServiceException e) {
      throw new RemoteException("", e);
    } catch (IOException e) {
      throw new RemoteException("", e);
    }

    return tlc.apim;
  }

  /*
   * inherited javadoc
   */
  public Uploader getFedoraUploader() throws RemoteException, IllegalStateException {
    TLC tlc = (TLC) tc.get();

    if (tlc.upld != null)
      return tlc.upld;

    tlc.upld = (Uploader) getHandle(Uploader.class);

    if (tlc.upld != null)
      return tlc.upld;

    try {
      ProtectedService svc = ProtectedServiceFactory.createService(upldConfig, tlc.session);
      tlc.upld = new Uploader(svc);
    } catch (URISyntaxException e) {
      throw new Error(e); // already tested; so shouldn't happend
    } catch (IOException e) {
      throw new RemoteException("", e);
    }

    return tlc.upld;
  }

  /*
   * inherited javadoc
   */
  public FedoraAPIA getFedoraAPIA() throws RemoteException, IllegalStateException {
    throw new UnsupportedOperationException("not implemented");
  }

  private Object getHandle(Class clazz) throws IllegalStateException {
    TLC tlc = (TLC) tc.get();

    if (tlc.cache != null)
      return tlc.cache.borrowObject(clazz);

    if (tlc.session == null)
      tlc.session = getHttpSession();

    tlc.cache = (HandleCache) tlc.session.getAttribute(sessionKey);

    if (tlc.cache != null)
      return tlc.cache.borrowObject(clazz);

    tlc.cache = new HandleCache();
    tlc.session.setAttribute(sessionKey, tlc.cache);

    return null;
  }

  /**
   * There is one cache per HttpSession. Usually the cache only contains the two session
   * bound handleshandles (apim, upld). However if the client is multi-threaded and issues
   * multiple calls to us, we have to  create more handle objects since these are typically not
   * multi-thread safe.<p>Using a SoftReference here since the handles are heavy wieght and
   * so we should let the gc get to it under heavy load.</p>
   */
  private static class HandleCache {
    private SoftReference[] refs  = new SoftReference[2];
    int                     count = 0;

    public synchronized Object borrowObject(Class clazz) {
      for (int i = count - 1; i >= 0; i--) {
        Object o = refs[i].get();

        if (clazz.isInstance(o)) {
          if (--count != i)
            System.arraycopy(refs, i + 1, refs, i, count - i);

          return o;
        }
      }

      return null;
    }

    public synchronized void returnObject(Object borrowed) {
      if (count >= refs.length) {
        SoftReference[] newRefs = new SoftReference[refs.length * 2];
        System.arraycopy(refs, 0, newRefs, 0, refs.length);
        refs = newRefs;
      }

      refs[count++] = new SoftReference(borrowed);
    }
  }
}
