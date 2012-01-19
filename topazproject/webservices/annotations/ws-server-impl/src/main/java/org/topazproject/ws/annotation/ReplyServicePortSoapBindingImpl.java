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

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.ExceptionUtils;
import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;

import org.topazproject.ws.annotation.impl.RepliesImpl;
import org.topazproject.ws.annotation.impl.RepliesPEP;

import org.topazproject.xacml.ws.WSXacmlUtil;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The implementation of the reply service.
 */
public class ReplyServicePortSoapBindingImpl implements Replies, ServiceLifecycle {
  private static Log   log  = LogFactory.getLog(ReplyServicePortSoapBindingImpl.class);
  private TopazContext ctx  = new WSTopazContext(getClass().getName());
  private Replies      impl = null;

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      RepliesPEP pep = new WSRepliesPEP((ServletEndpointContext) context);

      ctx.init(context);
      impl = (Replies) ImplInvocationHandler.newProxy(new RepliesImpl(pep, ctx), ctx, log);
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
   * @see org.topazproject.ws.annotation.Reply#createReply
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String body)
                     throws RemoteException, NoSuchAnnotationIdException {
    return impl.createReply(mediator, type, root, inReplyTo, anonymize, title, body);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#createReply
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String contentType, byte[] content)
                     throws RemoteException, NoSuchAnnotationIdException {
    return impl.createReply(mediator, type, root, inReplyTo, anonymize, title, contentType, content);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#deleteReplies
   */
  public void deleteReplies(String root, String inReplyTo)
                     throws NoSuchAnnotationIdException, RemoteException {
    impl.deleteReplies(root, inReplyTo);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#deleteReplies
   */
  public void deleteReplies(String id) throws NoSuchAnnotationIdException, RemoteException {
    impl.deleteReplies(id);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#getReplyInfo
   */
  public ReplyInfo getReplyInfo(String id) throws NoSuchAnnotationIdException, RemoteException {
    return impl.getReplyInfo(id);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyInfo[] listReplies(String root, String inReplyTo)
                          throws NoSuchAnnotationIdException, RemoteException {
    return impl.listReplies(root, inReplyTo);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyInfo[] listAllReplies(String root, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException {
    return impl.listAllReplies(root, inReplyTo);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyThread getReplyThread(String root, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException {
    return impl.getReplyThread(root, inReplyTo);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#setReplyState
   */
  public void setReplyState(String id, int state)
                     throws NoSuchAnnotationIdException, RemoteException {
    impl.setReplyState(id, state);
  }

  /*
   * @see org.topazproject.ws.annotation.Reply#listReplies
   */
  public ReplyInfo[] listReplies(String mediator, int state)
                          throws RemoteException {
    return impl.listReplies(mediator, state);
  }

  private static class WSRepliesPEP extends RepliesPEP {
    static {
      init(WSRepliesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSRepliesPEP(ServletEndpointContext context)
                 throws IOException, ParsingException, UnknownIdentifierException {
      super(WSXacmlUtil.lookupPDP(context, "topaz.replies.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
