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

import org.topazproject.ws.annotation.AnnotationInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationModel extends AnnotationInfo {
  private static final Log log = LogFactory.getLog(AnnotationModel.class);

  //
  static final URI a               = URI.create("http://www.w3.org/2000/10/annotation-ns#");
  static final URI r               = URI.create(ItqlHelper.RDF_URI);
  static final URI d               = URI.create(ItqlHelper.DC_URI);
  static final URI dt              = URI.create(ItqlHelper.DC_TERMS_URI);
  static final URI topaz           = URI.create(ItqlHelper.TOPAZ_URI);
  static final URI nil             = URI.create(ItqlHelper.RDF_URI + "nil");
  static final URI a_Annotation    = a.resolve("#Annotation");
  static final URI r_type          = r.resolve("#type");
  static final URI a_annotates     = a.resolve("#annotates");
  static final URI a_context       = a.resolve("#context");
  static final URI d_creator       = d.resolve("creator");
  static final URI d_title         = d.resolve("title");
  static final URI a_created       = a.resolve("#created");
  static final URI a_body          = a.resolve("#body");
  static final URI dt_replaces     = dt.resolve("replaces");
  static final URI dt_isReplacedBy = dt.resolve("isReplacedBy");
  static final URI dt_mediator     = dt.resolve("mediator");
  static final URI topaz_state     = topaz.resolve("state");

  /**
   * Append the xmlns attributes used by annotation meta data to an element. Useful for declaring
   * this in a container node so as to reduce verbosity in the child nodes.
   *
   * @param element A container element to which NS attributes have to be appended.
   */
  public static void appendNSAttr(Element element) {
    String xmlns = "http://www.w3.org/2000/xmlns/";

    element.setAttributeNS(xmlns, "xmlns:r", r.toString());
    element.setAttributeNS(xmlns, "xmlns:a", a.toString());
    element.setAttributeNS(xmlns, "xmlns:d", d.toString());
    element.setAttributeNS(xmlns, "xmlns:dt", dt.toString());
    element.setAttributeNS(xmlns, "xmlns:topaz", topaz.toString());
  }

  /**
   * Creates a new AnnotationModel object.
   *
   * @param id the annotation id;
   * @param map meta-data as a map of name value pairs
   */
  public AnnotationModel(String id, Map map) {
    setId(id);
    setType((String) map.get(r_type));
    setAnnotates((String) map.get(a_annotates));
    setContext((String) map.get(a_context));
    setCreator((String) map.get(d_creator));
    setCreated((String) map.get(a_created));
    setBody((String) map.get(a_body));
    setSupersedes((String) map.get(dt_replaces));
    setSupersededBy((String) map.get(dt_isReplacedBy));
    setTitle((String) map.get(d_title));
    setMediator((String) map.get(dt_mediator));
    setState(Integer.parseInt((String) map.get(topaz_state)));
  }

  /**
   * Append annotation meta data to a parent node.
   *
   * @param parent the annotation node
   * @param annotation the annotation to append
   */
  public static void appendToNode(Node parent, AnnotationInfo annotation) {
    String   rNs     = r.toString();
    String   aNs     = a.toString();
    String   dNs     = d.toString();
    String   dtNs    = dt.toString();
    String   topazNs = topaz.toString();

    Document document = parent.getOwnerDocument();
    Element  node;

    node = document.createElementNS(rNs, "r:type");
    node.setAttributeNS(rNs, "r:resource", annotation.getType());
    parent.appendChild(node);

    node = document.createElementNS(aNs, "a:annotates");
    node.setAttributeNS(rNs, "r:resource", annotation.getAnnotates());
    parent.appendChild(node);

    node = document.createElementNS(aNs, "a:context");
    node.appendChild(document.createTextNode(annotation.getContext()));
    parent.appendChild(node);

    node = document.createElementNS(dNs, "d:creator");
    node.setAttributeNS(rNs, "r:resource", annotation.getCreator());
    parent.appendChild(node);

    node = document.createElementNS(aNs, "a:created");
    node.appendChild(document.createTextNode(annotation.getCreated()));
    parent.appendChild(node);

    node = document.createElementNS(aNs, "a:body");
    node.setAttributeNS(rNs, "r:resource", annotation.getBody());
    parent.appendChild(node);

    String supersedes = annotation.getSupersedes();

    if (supersedes != null) {
      node = document.createElementNS(dtNs, "dt:replaces");
      node.setAttributeNS(rNs, "r:resource", supersedes);
      parent.appendChild(node);
    }

    String supersededBy = annotation.getSupersededBy();

    if (supersededBy != null) {
      node = document.createElementNS(dtNs, "dt:isReplacedBy");
      node.setAttributeNS(rNs, "r:resource", supersededBy);
      parent.appendChild(node);
    }

    String title = annotation.getTitle();

    if (title != null) {
      node = document.createElementNS(dNs, "d:title");
      node.appendChild(document.createTextNode(title));
      parent.appendChild(node);
    }

    String mediator = annotation.getMediator();

    if (mediator != null) {
      node = document.createElementNS(dtNs, "dt:mediator");
      node.appendChild(document.createTextNode(mediator));
      parent.appendChild(node);
    }

    node = document.createElementNS(topazNs, "topaz:state");
    node.appendChild(document.createTextNode("" + annotation.getState()));
    parent.appendChild(node);
  }

  /**
   * Creates an AnnotationInfo object from an ITQL query result.
   *
   * @param id the annotation-id
   * @param rows a list of name value pairs (predicate, object)
   *
   * @return returns the newly created Annotation Info
   */
  public static AnnotationInfo create(String id, List rows) {
    Map map = new HashMap();

    for (Iterator it = rows.iterator(); it.hasNext();) {
      Object[] cols      = (Object[]) it.next();
      URI      predicate = (URI) getColumnValue(cols[0]);
      Object   object    = getColumnValue(cols[1]);

      if (nil.equals(object))
        continue;

      if (r_type.equals(predicate) && a_Annotation.equals(object))
        continue;

      String prev = (String) map.put(predicate, object.toString());

      if (prev != null) {
        log.warn("Unexpected duplicate triple found. Ignoring <" + id + "> <" + predicate + "> <"
                 + prev + ">");
      }
    }

    return new AnnotationModel(id, map);
  }

  /**
   * Get the column value for an ITQL result column.
   *
   * @param o the raw value from ITQL result
   *
   * @return the value that is of any use
   */
  static Object getColumnValue(Object o) {
    if (o instanceof URIReference)
      return ((URIReference) o).getURI();

    if (o instanceof Literal)
      return ((Literal) o).getLexicalForm();

    return o;
  }
}
