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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.mulgara.itql.ItqlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URI;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationModel {
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
   * Append annotation meta data to a parent node.
   *
   * @param parent the annotation node
   * @param annotation the annotation to append
   */
  public static void appendToNode(final Node parent, final AnnotationInfo annotation) {
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
}
