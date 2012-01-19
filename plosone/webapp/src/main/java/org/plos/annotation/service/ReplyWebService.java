/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.plos.annotation.service.BaseAnnotation.DELETE_MASK;
import static org.plos.annotation.service.BaseAnnotation.FLAG_MASK;
import static org.plos.annotation.service.BaseAnnotation.PUBLIC_MASK;

import org.plos.models.Reply;
import org.plos.models.ReplyThread;

import org.plos.permission.service.PermissionWebService;

import org.topazproject.otm.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

/**
 * Wrapper over reply web service
 */
public class ReplyWebService extends BaseAnnotationService {
  private static final Log          log         = LogFactory.getLog(ReplyWebService.class);
  private final RepliesPEP          pep;
  private final FedoraHelper        fedora;
  private Session                   session;
  private PermissionWebService      permissions;

  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   * @throws URISyntaxException DOCUMENT ME!
   * @throws ServiceException DOCUMENT ME!
   */
  public ReplyWebService() throws IOException, URISyntaxException, ServiceException {
    try {
      pep = new RepliesPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe                           = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }

    fedora = new FedoraHelper();
  }

  /**
   * Create a reply
   *
   * @param mimeType mimeType
   * @param root root
   * @param inReplyTo inReplyTo
   * @param title title
   * @param body body
   * @param annotationService annotationService
   *
   * @return a new reply
   *
   * @throws RemoteException RemoteException
   * @throws UnsupportedEncodingException UnsupportedEncodingException
   */
  public String createReply(final String mimeType, final String root, final String inReplyTo,
                            final String title, final String body,
                            final AnnotationService annotationService)
                     throws RemoteException, UnsupportedEncodingException {
    pep.checkAccess(pep.CREATE_REPLY, URI.create(root));

    if (!root.equals(inReplyTo))
      pep.checkAccess(pep.CREATE_REPLY, URI.create(inReplyTo));

    final String contentType = getContentType(mimeType);
    String       bodyUri;
    String       user;

    try {
      fedora.getContext().activate();
      user                   = fedora.getContext().getUserName();
      bodyUri                = fedora.createBody(contentType, body.getBytes(getEncodingCharset()),
                                                 "Reply", "Reply Body");
    } finally {
      fedora.getContext().passivate();
    }

    if (log.isDebugEnabled())
      log.debug("created fedora object " + bodyUri + " for reply ");

    final Reply r = new ReplyThread();
    r.setMediator(getApplicationId());
    r.setType(getDefaultType());
    r.setRoot(URI.create(root));
    r.setInReplyTo(URI.create(inReplyTo));
    r.setTitle(title);

    if (isAnonymous())
      r.setAnonymousCreator(user);
    else
      r.setCreator(user);

    r.setBody(URI.create(bodyUri));
    r.setCreated(new Date());

    String  newId      =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<String>() {
          public String run(Transaction tx) {
            return tx.getSession().saveOrUpdate(r);
          }
        });

    boolean propagated = false;

    try {
      permissions.propagatePermissions(newId, new String[] { bodyUri });
      propagated = true;

      if (log.isDebugEnabled())
        log.debug("propagated permissions for reply " + newId + " to " + bodyUri);
    } finally {
      if (!propagated) {
        if (log.isDebugEnabled())
          log.debug("failed to propagate permissions for reply " + newId + " to " + bodyUri);

        try {
          TransactionHelper.doInTx(session,
                                   new TransactionHelper.Action<String>() {
              public String run(Transaction tx) {
                tx.getSession().delete(r);

                return "ok";
              }
            });

          fedora.purgeObjects(new String[] { fedora.uri2PID(bodyUri) });
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("failed to delete partially created reply " + newId, t);
        }
      }
    }

    return newId;
  }

  /**
   * Mark the reply as deleted.
   *
   * @param replyId replyId
   *
   * @throws RemoteException RemoteException
   */
  public void deleteReply(final String replyId) throws RemoteException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Reply>() {
          public Reply run(Transaction tx) {
            Reply a = tx.getSession().get(Reply.class, replyId);
            a.setState(DELETE_MASK);
            tx.getSession().saveOrUpdate(a);

            return a;
          }
        });
  }

  /**
   * Delete the subtree and not just mark it as deleted.
   *
   * @param root root
   * @param inReplyTo inReplyTo
   *
   * @throws RemoteException RemoteException
   */
  public void deleteReplies(final String root, final String inReplyTo)
                     throws RemoteException {
    final List<Reply> all  =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Reply>>() {
          public List<Reply> run(Transaction tx) {
            return tx.getSession().createCriteria(ReplyThread.class)
                      .add(Restrictions.eq("root", root))
                      .add(Restrictions.walk("replies", inReplyTo)).list();
          }
        });

    List<String> pids      = new ArrayList(all.size());

    for (Reply r : all) {
      pep.checkAccess(pep.DELETE_REPLY, r.getId());
      pids.add(fedora.uri2PID(r.getBody().toString()));
    }

    TransactionHelper.doInTx(session,
                             new TransactionHelper.Action<String>() {
        public String run(Transaction tx) {
          Session s = tx.getSession();

          for (Reply r : all)
            s.delete(r);

          return "ok";
        }
      });

    fedora.purgeObjects((String[]) pids.toArray(new String[pids.size()]));

    for (Reply r : all) {
      try {
        permissions.cancelPropagatePermissions(r.getId().toString(),
                                               new String[] { r.getBody().toString() });
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to cancel the propagated permissions on " + r.getId(), t);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param target DOCUMENT ME!
   *
   * @throws RemoteException DOCUMENT ME!
   */
  public void deleteReplies(final String target) throws RemoteException {
    if (log.isDebugEnabled()) {
      log.debug("deleting reply and descendants with id: " + target);
    }

    final List<Reply>  all  = new ArrayList();
    final List<String> pids = new ArrayList();
    final ReplyThread  root =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<ReplyThread>() {
          public ReplyThread run(Transaction tx) {
            ReplyThread r = tx.getSession().get(ReplyThread.class, target);

            if (r != null)
              add(r);

            return r;
          }

          private void add(ReplyThread r) {
            all.add(r);
            pids.add(fedora.uri2PID(r.getBody().toString()));

            for (ReplyThread t : r.getReplies())
              add(t);
          }
        });

    for (Reply r : all)
      pep.checkAccess(pep.DELETE_REPLY, r.getId());

    TransactionHelper.doInTx(session,
                             new TransactionHelper.Action<String>() {
        public String run(Transaction tx) {
          tx.getSession().delete(root); // ... and cascade

          return "ok";
        }
      });

    fedora.purgeObjects((String[]) pids.toArray(new String[pids.size()]));

    for (Reply r : all) {
      try {
        permissions.cancelPropagatePermissions(r.getId().toString(),
                                               new String[] { r.getBody().toString() });
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to cancel the propagated permissions on " + r.getId(), t);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param replyId replyId
   *
   * @return a reply
   *
   * @throws RemoteException RemoteException
   * @throws IllegalArgumentException DOCUMENT ME!
   */
  public ReplyInfo getReplyInfo(final String replyId) throws RemoteException {
    pep.checkAccess(pep.GET_REPLY_INFO, URI.create(replyId));

    Reply a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Reply>() {
          public Reply run(Transaction tx) {
            return tx.getSession().get(Reply.class, replyId);
          }
        });

    if (a == null)
      throw new IllegalArgumentException("invalid reply id: " + replyId);

    return new ReplyInfo(a, fedora);
  }

  /**
   * DOCUMENT ME!
   *
   * @param root root
   * @param inReplyTo inReplyTo
   *
   * @return list of replies
   *
   * @throws RemoteException
   */
  public ReplyInfo[] listReplies(final String root, final String inReplyTo)
                          throws RemoteException {
    List<Reply> all =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Reply>>() {
          public List<Reply> run(Transaction tx) {
            return tx.getSession().createCriteria(Reply.class).add(Restrictions.eq("root", root))
                      .add(Restrictions.eq("inReplyTo", inReplyTo)).list();
          }
        });

    List<Reply> l   = new ArrayList(all.size());

    for (Reply a : all) {
      try {
        pep.checkAccess(pep.GET_REPLY_INFO, a.getId());
        l.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId()
                    + " and therefore removed from list");
      }
    }

    ReplyInfo[] replies = new ReplyInfo[l.size()];

    int         i       = 0;

    for (Reply a : l)
      replies[i++] = new ReplyInfo(a, fedora);

    return replies;
  }

  /**
   * DOCUMENT ME!
   *
   * @param root root
   * @param inReplyTo inReplyTo
   *
   * @return list all replies
   *
   * @throws RemoteException
   */
  public ReplyInfo[] listAllReplies(final String root, final String inReplyTo)
                             throws RemoteException {
    List<Reply> all =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Reply>>() {
          public List<Reply> run(Transaction tx) {
            return tx.getSession().createCriteria(Reply.class)
                      .add(Restrictions.eq("root", root))
                      .add(Restrictions.walk("inReplyTo", inReplyTo)).list();
          }
        });

    List<Reply> l   = new ArrayList(all.size());

    for (Reply a : all) {
      try {
        pep.checkAccess(pep.GET_REPLY_INFO, a.getId());
        l.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing reply " + a.getId()
                    + " and therefore removed from list");
      }
    }

    ReplyInfo[] replies = new ReplyInfo[l.size()];

    int         i       = 0;

    for (Reply a : l)
      replies[i++] = new ReplyInfo(a, fedora);

    return replies;
  }

  /**
   * Unflag a reply
   *
   * @param replyId replyId
   *
   * @throws RemoteException RemoteException
   */
  public void unflagReply(final String replyId) throws RemoteException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Reply>() {
          public Reply run(Transaction tx) {
            Reply a = tx.getSession().get(Reply.class, replyId);
            a.setState(0);
            tx.getSession().saveOrUpdate(a);

            return a;
          }
        });
  }

  /**
   * Set the reply as flagged
   *
   * @param replyId replyId
   *
   * @throws RemoteException RemoteException
   */
  public void setFlagged(final String replyId) throws RemoteException {
    pep.checkAccess(pep.SET_REPLY_STATE, URI.create(replyId));

    Reply a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Reply>() {
          public Reply run(Transaction tx) {
            Reply a = tx.getSession().get(Reply.class, replyId);
            a.setState(FLAG_MASK | PUBLIC_MASK);
            tx.getSession().saveOrUpdate(a);

            return a;
          }
        });
  }

  /**
   * List the set of replies in a specific administrative state.
   *
   * @param mediator if present only those replies that match this mediator are returned
   * @param state the state to filter the list of replies by or 0 to return replies in any
   *        administartive state
   *
   * @return an array of replies; if no matching replies are found, an empty array is returned
   *
   * @throws RemoteException if some error occured
   */
  public ReplyInfo[] listReplies(final String mediator, final int state)
                          throws RemoteException {
    pep.checkAccess(pep.LIST_REPLIES_IN_STATE, pep.ANY_RESOURCE);

    List<Reply> l       =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Reply>>() {
          public List<Reply> run(Transaction tx) {
            Criteria c = tx.getSession().createCriteria(Reply.class);
            if (mediator != null)
                c.add(Restrictions.eq("mediator", mediator));
            if (state == 0)
              c.add(Restrictions.ne("state", "0"));
            else
              c.add(Restrictions.eq("state", "" + state));

            return c.list();
          }
        });

    ReplyInfo[] replies = new ReplyInfo[l.size()];

    int         i       = 0;

    for (Reply a : l)
      replies[i++] = new ReplyInfo(a, fedora);

    return replies;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the PermissionWebService
   *
   * @param permissionWebService permissionWebService
   */
  @Required
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissions = permissionWebService;
  }
}
