/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.io.IOException;

import java.rmi.RemoteException;

import javax.activation.DataHandler;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.ExceptionUtils;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;

import org.topazproject.ws.annotation.impl.AnnotationsImpl;
import org.topazproject.ws.annotation.impl.AnnotationsPEP;

import org.topazproject.xacml.ws.WSXacmlUtil;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The implementation of the annotation service.
 */
public class AnnotationServicePortSoapBindingImpl implements Annotations, ServiceLifecycle {
  private static Log   log  = LogFactory.getLog(AnnotationServicePortSoapBindingImpl.class);
  private TopazContext ctx  = new WSTopazContext(getClass().getName());
  private Annotations  impl = null;

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      AnnotationsPEP pep = new WSAnnotationsPEP((ServletEndpointContext) context);
      ctx.init(context);
      impl = (Annotations) ImplInvocationHandler.newProxy(new AnnotationsImpl(pep, ctx), ctx, log);
    } catch (Exception e) {
      throw new ServiceException("", e);
    }
  }

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#destroy
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title, String body)
                          throws NoSuchAnnotationIdException, RemoteException {
    return impl.createAnnotation(mediator, type, annotates, context, supersedes, anonymize, title,
                                 body);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#createAnnotation
   */
  public String createAnnotation(String mediator, String type, String annotates, String context,
                                 String supersedes, boolean anonymize, String title,
                                 String contentType, byte[] content)
                          throws NoSuchAnnotationIdException, RemoteException {
    return impl.createAnnotation(mediator, type, annotates, context, supersedes, anonymize, title,
                                 contentType, content);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation
   */
  public void deleteAnnotation(String id, boolean deletePreceding)
                        throws NoSuchAnnotationIdException, RemoteException {
    impl.deleteAnnotation(id, deletePreceding);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo
   */
  public AnnotationInfo getAnnotationInfo(String id)
                                   throws NoSuchAnnotationIdException, RemoteException {
    return impl.getAnnotationInfo(id);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getLatestAnnotations
   */
  public AnnotationInfo[] getLatestAnnotations(String id)
                                        throws NoSuchAnnotationIdException, RemoteException {
    return impl.getLatestAnnotations(id);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getPrecedingAnnotations
   */
  public AnnotationInfo[] getPrecedingAnnotations(String id)
                                           throws NoSuchAnnotationIdException, RemoteException {
    return impl.getPrecedingAnnotations(id);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public AnnotationInfo[] listAnnotations(String mediator, String on, String type)
                                   throws RemoteException {
    return impl.listAnnotations(mediator, on, type);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#setAnnotationState
   */
  public void setAnnotationState(String id, int state)
                          throws NoSuchAnnotationIdException, RemoteException {
    impl.setAnnotationState(id, state);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations
   */
  public AnnotationInfo[] listAnnotations(String mediator, int state)
                                   throws RemoteException {
    return impl.listAnnotations(mediator, state);
  }

  /*
   * @see org.topazproject.ws.annotation.Annotations#getAnnotatedContent
   */
  public DataHandler getAnnotatedContent(String resource, String resourceURL, DataHandler content,
                                         String mediator, String type)
                                  throws RemoteException {
    return impl.getAnnotatedContent(resource, resourceURL, content, mediator, type);
  }

  private static class WSAnnotationsPEP extends AnnotationsPEP {
    static {
      init(WSAnnotationsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAnnotationsPEP(ServletEndpointContext context)
                     throws IOException, ParsingException, UnknownIdentifierException {
      super(WSXacmlUtil.lookupPDP(context, "topaz.annotations.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
