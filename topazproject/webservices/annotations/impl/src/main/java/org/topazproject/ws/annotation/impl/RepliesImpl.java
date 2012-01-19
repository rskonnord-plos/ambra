/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation.impl;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.common.impl.TopazContext;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.NoSuchAnnotationIdException;
import org.topazproject.ws.annotation.Replies;
import org.topazproject.ws.annotation.ReplyInfo;
import org.topazproject.ws.annotation.ReplyThread;
import org.topazproject.ws.permissions.impl.PermissionsImpl;
import org.topazproject.ws.permissions.impl.PermissionsPEP;

/**
 * Implementation of Replies.
 *
 * @author Pradeep Krishnan
 */
public class RepliesImpl implements Replies {
  private static final Log    log          = LogFactory.getLog(RepliesImpl.class);
  private static final Map    aliases      = ItqlHelper.getDefaultAliases();
  private static final String REPLY_PID_NS = "reply";

  //
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  //
  private static final String MODEL = "<" + CONF.getString("topaz.models.annotations") + ">";

  //
  private static final String ITQL_CREATE =
    ("insert <${id}> <r:type> <tr:Reply> <${id}> <r:type> <${type}> <${id}> <topaz:state> '0'"
    + " <${id}> <tr:root> <${root}> <${id}> <a:created> '${created}'"
    + " <${id}> <tr:inReplyTo> <${inReplyTo}> <${id}> <a:body> <${body}>"
    + " <${id}> <${creator}> '${user}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  // 
  private static final String ITQL_INSERT_TITLE =
    ("insert <${id}> <d:title> '${title}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  // 
  private static final String ITQL_INSERT_MEDIATOR =
    ("insert <${id}> <dt:mediator> '${mediator}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_CHECK_ID =
    ("select $s from ${MODEL} where $s <r:type> <tr:Reply> and $s <tucana:is> <${id}>        ;")
     .replaceAll("\\Q${MODEL}", MODEL);

  // don't use it for (inReplyTo == root)
  private static final String ITQL_CHECK_IN_REPLY_TO =
    ("select $s from ${MODEL} where $s <r:type> <tr:Reply> and $s <tucana:is> <${inReplyTo}> "
    + " and $s <tr:root> <${root}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_DELETE_ID =
    ("delete select $s $p $o from ${MODEL} where $s $p $o and $s <tucana:is> <${id}> from ${MODEL};")
     .replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET_DELETE_LIST_FOR_ID =
    ("select $s $b from ${MODEL} where $s <a:body> $b and "
    + " (walk ($c <tr:inReplyTo> <${id}> and $s <tr:inReplyTo> $c) or $s <tucana:is> <${id}>) "
    + " and $s <tr:root> $r and <${id}> <tr:root> $r;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET_DELETE_LIST_IN_REPLY_TO =
    ("select $s $b from ${MODEL} where $s <a:body> $b and "
    + " walk ($c <tr:inReplyTo> <${inReplyTo}> and $s <tr:inReplyTo> $c) "
    + " and $s <tr:root> <${root}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_GET =
    ("select $p $o from ${MODEL} where $s $p $o and "
    + "$s <r:type> <tr:Reply> and $s <tucana:is> <${id}>;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_LIST_REPLIES =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) $date from ${MODEL}"
    + " where $s <tr:inReplyTo> <${inReplyTo}> and $s <tr:root> <${root}>"
    + " and $s <r:type> <tr:Reply> and $s <a:created> $date order by $date;") //
    .replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String ITQL_LIST_ALL_REPLIES =
    ("select $s subquery(select $p $o from ${MODEL} where $s $p $o) $date from ${MODEL} "
    + " where walk($c <tr:inReplyTo> <${inReplyTo}> and $s <tr:inReplyTo> $c)"
    + " and $s <tr:root> <${root}> and $s <r:type> <tr:Reply>                "
    + " and $s <a:created> $date order by $date;").replaceAll("\\Q${MODEL}", MODEL);

  //
  private static final String SET_STATE_ITQL =
    ("delete select <${id}> <topaz:state> $o from ${MODEL}"
    + " where <${id}> <topaz:state> $o from ${MODEL};"
    + " insert <${id}> <topaz:state> '${state}' into ${MODEL};").replaceAll("\\Q${MODEL}", MODEL);

  static {
    aliases.put("a", AnnotationModel.a.toString());
    aliases.put("r", AnnotationModel.r.toString());
    aliases.put("d", AnnotationModel.d.toString());
    aliases.put("tr", ReplyModel.tr.toString());
    aliases.put("dt", AnnotationModel.dt.toString());
  }

  private final RepliesPEP      pep;
  private final TopazContext    ctx;
  private final FedoraHelper    fedora;
  private final PermissionsImpl permissions;

  /**
   * Creates a new RepliesImpl object.
   *
   * @param pep The xacml pep
   * @param ctx The topaz context
   */
  public RepliesImpl(RepliesPEP pep, TopazContext ctx) {
    this.pep           = pep;
    this.ctx           = ctx;
    this.fedora        = new FedoraHelper(ctx);
    this.permissions   = new PermissionsImpl(new PermissionsPEP.Proxy(pep), ctx);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String body)
                     throws NoSuchAnnotationIdException, RemoteException {
    ItqlHelper.validateUri(body, "body");

    return createReply(mediator, type, root, inReplyTo, anonymize, title, body, null, null);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public String createReply(String mediator, String type, String root, String inReplyTo,
                            boolean anonymize, String title, String contentType, byte[] content)
                     throws NoSuchAnnotationIdException, RemoteException {
    if (contentType == null)
      throw new NullPointerException("'contentType' cannot be null");

    if (content == null)
      throw new NullPointerException("'content' cannot be null");

    return createReply(mediator, type, root, inReplyTo, anonymize, title, null, contentType, content);
  }

  private String createReply(String mediator, String type, String root, String inReplyTo,
                             boolean anonymize, String title, String body, String contentType,
                             byte[] content) throws NoSuchAnnotationIdException, RemoteException {
    if (type == null)
      type = "http://www.w3.org/2001/12/replyType#Comment";
    else
      ItqlHelper.validateUri(type, "type");

    URI rootUri      = ItqlHelper.validateUri(root, "root");
    URI inReplyToUri = ItqlHelper.validateUri(inReplyTo, "inReplyTo");

    checkInReplyTo(rootUri, inReplyToUri);

    pep.checkAccess(pep.CREATE_REPLY, rootUri);

    if (!inReplyToUri.equals(rootUri))
      pep.checkAccess(pep.CREATE_REPLY, inReplyToUri);

    String id     = getNextId();
    String create = ITQL_CREATE;
    Map    values = new HashMap();
    String user   = ctx.getUserName();

    if (user == null)
      user = "anonymous";

    if (body == null) {
      body = fedora.createBody(contentType, content, "Reply", "Reply Body");

      if (log.isDebugEnabled())
        log.debug("created fedora object " + body + " for reply " + id);
    }

    values.put("id", id);
    values.put("type", type);
    values.put("root", root);
    values.put("inReplyTo", inReplyTo);
    values.put("body", body);
    values.put("user", user);
    values.put("created", ItqlHelper.getUTCTime());

    values.put("creator", (anonymize ? "topaz:anonymousCreator" : "dc:creator"));

    if (title != null) {
      values.put("title", ItqlHelper.escapeLiteral(title));
      create += ITQL_INSERT_TITLE;
    }

    if (mediator != null) {
      values.put("mediator", ItqlHelper.escapeLiteral(mediator));
      create += ITQL_INSERT_MEDIATOR;
    }

    ItqlHelper itql = ctx.getItqlHelper();
    String     txn = "create-reply:" + id;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(ItqlHelper.bindValues(create, values), aliases);
      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Error rolling failed transaction", t);
      }
    }

    boolean propagated = false;

    try {
      permissions.propagatePermissions(id, new String[] { body });
      propagated = true;
    } finally {
      if (!propagated) {
        try {
          String del = ItqlHelper.bindValues(ITQL_DELETE_ID, "id", id);
          txn = "delete-partially-created-reply:" + id;
          itql.beginTxn(txn);
          itql.doUpdate(del, aliases);
          itql.commitTxn(txn);
          txn = null;
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("Error deleting partially created reply", t);

          try {
            if (txn != null)
              itql.rollbackTxn(txn);
          } catch (Throwable t2) {
            if (log.isDebugEnabled())
              log.debug("Error rolling failed transaction", t2);
          }
        }
      }
    }

    if (log.isDebugEnabled())
      log.debug("created reply " + id + " for " + inReplyTo + " with root " + root + " with body "
                + body);

    return id;
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#deleteReplies
   */
  public void deleteReplies(String root, String inReplyTo)
                     throws NoSuchAnnotationIdException, RemoteException {
    checkInReplyTo(ItqlHelper.validateUri(root, "root"),
                   ItqlHelper.validateUri(inReplyTo, "inReplyTo"));

    String txn   = "delete replies to " + inReplyTo;
    String query = ITQL_GET_DELETE_LIST_IN_REPLY_TO;
    query = ItqlHelper.bindValues(query, "root", root, "inReplyTo", inReplyTo);

    doDelete(txn, query);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#deleteReplies
   */
  public void deleteReplies(String id) throws NoSuchAnnotationIdException, RemoteException {
    pep.checkAccess(pep.DELETE_REPLY, checkId(ItqlHelper.validateUri(id, "id")));

    String txn   = "delete " + id;
    String query = ItqlHelper.bindValues(ITQL_GET_DELETE_LIST_FOR_ID, "id", id);

    doDelete(txn, query);
  }

  private void doDelete(String txn, String query) throws RemoteException {
    String[]   purgeList;

    ItqlHelper itql = ctx.getItqlHelper();

    try {
      itql.beginTxn(txn);

      Answer ans = new Answer(itql.doQuery(query, aliases));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      int    c    = rows.size();
      List   pids = new ArrayList(c);

      for (int i = 0; i < c; i++) {
        Object[]     cols = (Object[]) rows.get(i);
        URIReference ref = (URIReference) cols[0];
        URI          id  = ref.getURI();

        ref = (URIReference) cols[1];

        String body = ref.getURI().toString();

        if (log.isDebugEnabled())
          log.debug("deleting " + id + " as part of " + txn);

        pep.checkAccess(pep.DELETE_REPLY, id);

        if (body.startsWith("info:fedora"))
          pids.add(fedora.uri2PID(body));

        String del = ItqlHelper.bindValues(ITQL_DELETE_ID, "id", id.toString());
        itql.doUpdate(del, aliases);
      }

      itql.commitTxn(txn);
      txn         = null;
      purgeList   = (String[]) pids.toArray(new String[0]);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    } finally {
      try {
        if (txn != null) {
          if (log.isDebugEnabled())
            log.debug("failed to " + txn);

          itql.rollbackTxn(txn);
        }
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Error rolling failed transaction", t);
      }
    }

    fedora.purgeObjects(purgeList);
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#getReplyInfo
   */
  public ReplyInfo getReplyInfo(String id) throws NoSuchAnnotationIdException, RemoteException {
    pep.checkAccess(pep.GET_REPLY_INFO, ItqlHelper.validateUri(id, "id"));

    try {
      String query = ItqlHelper.bindValues(ITQL_GET, "id", id);

      Answer ans  = new Answer(ctx.getItqlHelper().doQuery(query, aliases));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchAnnotationIdException(id);

      return buildReplyInfo(id, rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#listReplies
   */
  public ReplyInfo[] listReplies(String root, String inReplyTo)
                          throws NoSuchAnnotationIdException, RemoteException {
    pep.checkAccess(pep.LIST_REPLIES,
                    checkInReplyTo(ItqlHelper.validateUri(root, "root"),
                                   ItqlHelper.validateUri(inReplyTo, "inReplyTo")));

    try {
      String query = ItqlHelper.bindValues(ITQL_LIST_REPLIES, "root", root, "inReplyTo", inReplyTo);

      Answer ans = new Answer(ctx.getItqlHelper().doQuery(query, aliases));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildReplyInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#listAllReplies
   */
  public ReplyInfo[] listAllReplies(String root, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException {
    pep.checkAccess(pep.LIST_ALL_REPLIES,
                    checkInReplyTo(ItqlHelper.validateUri(root, "root"),
                                   ItqlHelper.validateUri(inReplyTo, "inReplyTo")));

    try {
      String query =
        ItqlHelper.bindValues(ITQL_LIST_ALL_REPLIES, "root", root, "inReplyTo", inReplyTo);

      Answer ans = new Answer(ctx.getItqlHelper().doQuery(query, aliases));

      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildReplyInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#createReply
   */
  public ReplyThread getReplyThread(String rootId, String inReplyTo)
                             throws NoSuchAnnotationIdException, RemoteException {
    ReplyInfo[] replies = listAllReplies(rootId, inReplyTo);

    HashMap     map = new HashMap(replies.length * 2);

    for (int i = 0; i < replies.length; i++) {
      ReplyModel reply = (ReplyModel) replies[i];

      try {
        map.put(URI.create(reply.getId()), reply.clone(ReplyThread.class));
      } catch (InstantiationException e) {
        throw new Error(e);
      } catch (IllegalAccessException e) {
        throw new Error(e);
      }
    }

    URI         rootUri = URI.create(inReplyTo);
    ReplyThread root = new ReplyThread();
    root.setRoot(rootId);
    root.setId(inReplyTo);

    for (int i = 0; i < replies.length; i++) {
      URI         id    = URI.create(replies[i].getId());
      ReplyThread reply = (ReplyThread) map.get(id);

      URI         uri    = URI.create(reply.getInReplyTo());
      ReplyThread parent = uri.equals(rootUri) ? root : (ReplyThread) map.get(uri);

      if (parent == null)
        throw new Error("can't find " + uri + " replied by " + id);

      ReplyThread[] cur = parent.getReplies();
      ReplyThread[] nu;

      if (cur == null)
        nu = new ReplyThread[1];
      else {
        nu = new ReplyThread[cur.length + 1];
        System.arraycopy(cur, 0, nu, 0, cur.length);
      }

      nu[nu.length - 1] = reply;
      parent.setReplies(nu);
    }

    return root;
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#setAnnotationState
   */
  public void setReplyState(String id, int state)
                     throws RemoteException, NoSuchAnnotationIdException {
    URI thisUri = ItqlHelper.validateUri(id, "id");
    pep.checkAccess(pep.SET_REPLY_STATE, thisUri);
    checkId(thisUri);

    String set = ItqlHelper.bindValues(SET_STATE_ITQL, "id", id, "state", "" + state);

    ItqlHelper itql = ctx.getItqlHelper();
    String     txn = "set-state:" + id;

    try {
      itql.beginTxn(txn);
      itql.doUpdate(set, aliases);
      itql.commitTxn(txn);
      txn = null;
    } finally {
      try {
        if (txn != null)
          itql.rollbackTxn(txn);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Error rolling failed transaction", t);
      }
    }
  }

  /*
   * @see org.topazproject.ws.annotation.Replies#listReplies
   */
  public ReplyInfo[] listReplies(String mediator, int state)
                          throws RemoteException {
    pep.checkAccess(pep.LIST_REPLIES_IN_STATE, URI.create(ctx.getObjectBaseUri() + REPLY_PID_NS));

    try {
      String query =
        "select $a subquery(select $ap $ao from " + MODEL + " where $a $ap $ao) from " + MODEL
        + " where $a <r:type> <tr:Reply>";

      if (mediator != null)
        query += (" and $a <dt:mediator> '" + ItqlHelper.escapeLiteral(mediator) + "'");

      if (state == 0)
        query += " and exclude($a $p '0') and $p <tucana:is> <topaz:state>;";
      else
        query += (" and $a <topaz:state> '" + state + "';");

      Answer ans  = new Answer(ctx.getItqlHelper().doQuery(query, aliases));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      return buildReplyInfoList(rows);
    } catch (AnswerException e) {
      throw new RemoteException("Error querying RDF", e);
    }
  }

  private URI checkId(URI id) throws RemoteException, NoSuchAnnotationIdException {
    try {
      String query = ItqlHelper.bindValues(ITQL_CHECK_ID, "id", id.toString());
      Answer ans  = new Answer(ctx.getItqlHelper().doQuery(query, aliases));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchAnnotationIdException(id.toString());

      return id;
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  private URI checkInReplyTo(URI root, URI inReplyTo)
                      throws RemoteException, NoSuchAnnotationIdException {
    if (inReplyTo.equals(root))
      return inReplyTo;

    try {
      String query =
        ItqlHelper.bindValues(ITQL_CHECK_IN_REPLY_TO, "root", root.toString(), "inReplyTo",
                              inReplyTo.toString());
      Answer ans  = new Answer(ctx.getItqlHelper().doQuery(query, aliases));
      List   rows = ((Answer.QueryAnswer) ans.getAnswers().get(0)).getRows();

      if (rows.size() == 0)
        throw new NoSuchAnnotationIdException(inReplyTo.toString());

      return inReplyTo;
    } catch (AnswerException e) {
      throw new RemoteException("Query failed", e);
    }
  }

  private ReplyInfo buildReplyInfo(String id, List rows) {
    ReplyInfo info = ReplyModel.create(id, rows);
    info.setBody(fedora.getBodyURL(info.getBody()));

    return info;
  }

  private ReplyInfo[] buildReplyInfoList(List rows) {
    ReplyInfo[] replies = new ReplyInfo[rows.size()];

    for (int i = 0; i < replies.length; i++) {
      Object[]           cols           = (Object[]) rows.get(i);
      URIReference       ref            = (URIReference) cols[0];
      String             id             = ref.getURI().toString();
      Answer.QueryAnswer subQueryAnswer = (Answer.QueryAnswer) cols[1];
      replies[i] = buildReplyInfo(id, subQueryAnswer.getRows());
    }

    return replies;
  }

  private String getNextId() throws RemoteException {
    return ctx.getObjectBaseUri() + fedora.getNextId(REPLY_PID_NS).replace(':', '/');
  }
}
