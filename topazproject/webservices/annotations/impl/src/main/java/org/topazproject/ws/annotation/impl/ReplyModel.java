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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.annotation.ReplyInfo;

/**
 * Reply meta-data.
 *
 * @author Pradeep Krishnan
 */
public class ReplyModel extends ReplyInfo {
  private static final Log log = LogFactory.getLog(ReplyModel.class);

  //
  static final URI tr = URI.create("http://www.w3.org/2001/03/thread#");
  static final URI rt = URI.create("http://www.w3.org/2001/12/replyType#");

  //
  static final URI tr_root      = tr.resolve("#root");
  static final URI tr_inReplyTo = tr.resolve("#inReplyTo");
  static final URI tr_Reply     = tr.resolve("#Reply");

  /**
   * Creates a ReplyModel with meta-data from a map.
   *
   * @param id the annotation id;
   * @param map meta-data as a map of name value pairs
   */
  public ReplyModel(String id, Map map) {
    setId(id);
    setType((String) map.get(AnnotationModel.r_type));
    setRoot((String) map.get(tr_root));
    setInReplyTo((String) map.get(tr_inReplyTo));
    setTitle((String) map.get(AnnotationModel.d_title));
    setCreator((String) map.get(AnnotationModel.d_creator));
    setCreated((String) map.get(AnnotationModel.a_created));
    setBody((String) map.get(AnnotationModel.a_body));
    setMediator((String) map.get(AnnotationModel.dt_mediator));
    setState(Integer.parseInt((String) map.get(AnnotationModel.topaz_state)));
  }

  /**
   * Clones this ReplyInfo.
   *
   * @param clz The sub-class of ReplyInfo to create
   *
   * @return the clone
   *
   * @throws InstantiationException if a newInstance fails on the clz
   * @throws IllegalAccessException if a newInstance fails on the clz
   */
  public ReplyInfo clone(Class clz) throws InstantiationException, IllegalAccessException {
    ReplyInfo to = (ReplyInfo) clz.newInstance();
    to.setId(getId());
    to.setType(getType());
    to.setRoot(getRoot());
    to.setInReplyTo(getInReplyTo());
    to.setTitle(getTitle());
    to.setCreator(getCreator());
    to.setCreated(getCreated());
    to.setBody(getBody());
    to.setMediator(getMediator());
    to.setState(getState());

    return to;
  }

  /**
   * Creates a ReplyInfo instance from an ITQL query result.
   *
   * @param id the reply-id
   * @param rows a list of name value pairs
   *
   * @return returns a predicate vs object map for the reply-id
   */
  public static ReplyInfo create(String id, List rows) {
    Map map = new HashMap();

    for (Iterator it = rows.iterator(); it.hasNext();) {
      Object[] cols      = (Object[]) it.next();
      URI      predicate = ((URIReference) (cols[0])).getURI();

      Object   o     = cols[1];
      String   value;

      if (!(o instanceof URIReference)) {
        if (o instanceof Literal)
          value = ((Literal) o).getLexicalForm();
        else
          value = o.toString();
      } else {
        URI v = ((URIReference) o).getURI();

        if (AnnotationModel.nil.equals((Object) v))
          continue;

        if (AnnotationModel.r_type.equals(predicate) && tr_Reply.equals((Object) v))
          continue;

        value = v.toString();
      }

      String prev = (String) map.put(predicate, value);

      if (prev != null) {
        log.warn("Unexpected duplicate triple found. Ignoring <" + id + "> <" + predicate + "> <"
                 + prev + ">");
      }
    }

    return new ReplyModel(id, map);
  }
}
