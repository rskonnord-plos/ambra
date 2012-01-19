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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.dom.ranges.SelectionRange;
import org.topazproject.dom.ranges.SelectionRangeList;

import org.topazproject.ws.annotation.AnnotationInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.XPointerAPI;
import it.unibo.cs.xpointer.datatype.LocationList;

/**
 * Creates an annotated version of the content.
 *
 * @author Pradeep Krishnan
 */
public class Annotator {
  private static final Log log    = LogFactory.getLog(Annotator.class);
  private static String    AML_NS = "http://topazproject.org/aml/";

  /**
   * Creates an annotated document.
   *
   * @param content the source document
   * @param annotations the list of annotations to apply
   *
   * @return the annotated document
   *
   * @throws RemoteException on a failure
   */
  public static DataHandler annotate(DataHandler content, AnnotationInfo[] annotations)
                              throws RemoteException {
    try {
      return serialize(annotate(parse(content), annotations));
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  /**
   * Creates an annotated document.
   *
   * @param document the source document
   * @param annotations the list of annotations to apply
   *
   * @return the annotated document
   *
   * @throws URISyntaxException if at least one annotation context is an invalid URI
   * @throws TransformerException if at least one annotation context is an invalid xpointer
   *         expression
   */
  public static Document annotate(Document document, AnnotationInfo[] annotations)
                           throws URISyntaxException, TransformerException {
    LocationList[] lists = evaluate(document, annotations);

    Regions        regions = new Regions(document);

    for (int i = 0; i < lists.length; i++)
      regions.addRegion(lists[i], annotations[i]);

    regions.surroundContents(AML_NS, "aml:annotated", "aml:id", "aml:first");

    Element rRoot = regions.createElement(AML_NS, "aml:region", "aml:annotation", "aml:id");

    Element aRoot = document.createElementNS(AML_NS, "aml:annotations");
    AnnotationModel.appendNSAttr(aRoot);

    for (int i = 0; i < annotations.length; i++) {
      Element a = document.createElementNS(AML_NS, "aml:annotation");
      a.setAttributeNS(AML_NS, "aml:id", annotations[i].getId());
      aRoot.appendChild(a);
      AnnotationModel.appendToNode(a, annotations[i]);
    }

    return assembleResultDoc(document, rRoot, aRoot);
  }

  private static Document assembleResultDoc(Document document, Element regions, Element annotations) {
    String  xmlns = "http://www.w3.org/2000/xmlns/";

    Element source = document.getDocumentElement();
    source.setAttributeNS(xmlns, "xmlns:aml", AML_NS);

    source.appendChild(regions);
    source.appendChild(annotations);

    return document;
  }

  /**
   * Parses the content into a DOM document.
   *
   * @param content the content to parse
   *
   * @return the dom document
   *
   * @throws SAXException on an error in parse
   * @throws ParserConfigurationException if a suitable parser could not be found
   * @throws IOException on an error in reading the content
   */
  public static Document parse(DataHandler content)
                        throws SAXException, ParserConfigurationException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder        builder = factory.newDocumentBuilder();

    return builder.parse(content.getInputStream());
  }

  /**
   * Serializes a DOM document.
   *
   * @param document the document to serialize
   *
   * @return the serialized
   *
   * @throws TransformerException on an error in serialize
   * @throws IOException on an error in writing
   */
  public static DataHandler serialize(Document document)
                               throws TransformerException, IOException {
    TransformerFactory xformFactory = TransformerFactory.newInstance();
    Transformer        idTransform = xformFactory.newTransformer();
    Source             input       = new DOMSource(document);
    final File         tmp         = File.createTempFile("annotated", ".xml");
    tmp.deleteOnExit();

    Result output = new StreamResult(tmp);
    idTransform.transform(input, output);

    return new DataHandler(new FileDataSource(tmp) {
        protected void finalize() throws Throwable {
          super.finalize();

          try {
            tmp.delete();
          } catch (Throwable t) {
            log.warn("Failed to delete temporary file " + tmp);
          }
        }
      });
  }

  private static LocationList[] evaluate(Document document, AnnotationInfo[] annotations)
                                  throws URISyntaxException, TransformerException {
    LocationList[] lists = new LocationList[annotations.length];

    for (int i = 0; i < annotations.length; i++) {
      URI    context    = new URI(annotations[i].getContext());
      String expression = context.getFragment();

      try {
        expression = URLDecoder.decode(expression, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new Error(e);
      }

      try {
        LocationList list = XPointerAPI.evalFullptr(document, expression);
        lists[i] = list;
      } catch (TransformerException e) {
        throw new TransformerException(expression, e);
      }
    }

    return lists;
  }

  private static class Regions extends SelectionRangeList {
    private Document document;

    public Regions(Document document) {
      this.document = document;
    }

    public void addRegion(LocationList list, AnnotationInfo annotation) {
      int length = list.getLength();

      for (int i = 0; i < length; i++)
        addRegion(list.item(i), annotation);
    }

    public void addRegion(Location location, AnnotationInfo annotation) {
      Range range;

      if (location.getType() == Location.RANGE)
        range = (Range) location.getLocation();
      else {
        range = ((DocumentRange) document).createRange();
        range.selectNode((Node) location.getLocation());
      }

      // Ignore it if this range is collapsed (ie. start == end)
      if (!range.getCollapsed())
        insert(new SelectionRange(range, annotation));
    }


    public Element createElement(String nsUri, String elemQName, String annotationsQName,
                                 String idAttrQName) {
      int     length = size();
      Element root = document.createElementNS(nsUri, elemQName + "s");

      for (int i = 0; i < length; i++) {
        Element rNode = document.createElementNS(nsUri, elemQName);
        rNode.setAttributeNS(nsUri, idAttrQName, "" + (i + 1));
        root.appendChild(rNode);

        List annotations = get(i).getUserDataList();

        int  c = annotations.size();

        for (int j = 0; j < c; j++) {
          AnnotationInfo a     = (AnnotationInfo) annotations.get(j);
          Element        aNode = document.createElementNS(nsUri, annotationsQName);
          aNode.setAttributeNS(nsUri, idAttrQName, a.getId());
          rNode.appendChild(aNode);
        }
      }

      return root;
    }
  }

}
