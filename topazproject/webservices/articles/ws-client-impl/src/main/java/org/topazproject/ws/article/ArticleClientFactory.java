/* $HeadURL::                                                                         $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate Article web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class ArticleClientFactory extends AbstractReAuthStubFactory {
  private static ArticleClientFactory instance = new ArticleClientFactory();

  /**
   * Creates an Article service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Article service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Article create(ProtectedService service)
                        throws MalformedURLException, ServiceException {
    Article stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (Article) instance.newProxyStub(stub, service);

    return stub;
  }

  private Article createStub(ProtectedService service)
                      throws MalformedURLException, ServiceException {
    URL                   url     = new URL(service.getServiceUri());
    ArticleServiceLocator locator = new ArticleServiceLocator();

    locator.setMaintainSession(true);

    Article articles = locator.getArticleServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) articles;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return articles;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param articleServiceUri the uri for article service
   *
   * @return Returns an Article service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Article create(String articleServiceUri)
                        throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(articleServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
