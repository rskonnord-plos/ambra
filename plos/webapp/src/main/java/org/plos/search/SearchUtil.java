/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search;

import org.plos.search.service.SearchHit;
import static org.plos.search.service.SearchHit.MULTIPLE_VALUE_DELIMITER;
import org.plos.util.DateParser;
import org.plos.util.InvalidDateException;
import org.plos.util.TextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Util functions to be used for Flag related tasks like created and extracting flag attributes.
 *
 * @author Viru
 * @author Eric Brown
 */
public class SearchUtil {
  private static final String                 charsetEncoding         = "UTF-8";
  private static final DocumentBuilderFactory documentBuilderFactory  =
                                                DocumentBuilderFactory.newInstance();
  private static final Pattern                patternToMatchFieldNode =
                                                Pattern.compile("<field\\s[^>]*>(.*)</field>");

  /**
   * Return a collection of SearchHit's
   * @param searchResultXml searchResultXml
   * @return An object representing a result page
   * @throws IOException IOException
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws org.plos.util.InvalidDateException InvalidDateException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public static SearchResultPage convertSearchResultXml(final String searchResultXml)
      throws IOException, ParserConfigurationException, SAXException, InvalidDateException,
             TransformerException {
    final String nodeName = "hit";
    final Element rootNode = getRootNode(searchResultXml);

    final NodeList hitList = getNodeList(rootNode, nodeName);
    final int noOfHits = hitList.getLength();

    final Collection<SearchHit> hits = new ArrayList<SearchHit>();
    for (int i = 0; i < noOfHits; i++) {
      final Node hitNode = hitList.item(i);
      final SearchHit searchHit = convertToSearchHit((Element) hitNode);
      hits.add(searchHit);
    }

    final NamedNodeMap rootAttributes = rootNode.getAttributes();
    final int totalNoOfHits = Integer.parseInt(getAttributeTextValue(rootAttributes, "hitTotal"));
    final int pageSize = Integer.parseInt(getAttributeTextValue(rootAttributes, "hitPageSize"));
    return new SearchResultPage(totalNoOfHits, pageSize, hits);
  }

  private static Element getRootNode(final String xmlDocument)
      throws SAXException, IOException, ParserConfigurationException {
    final Document doc = documentBuilderFactory.newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xmlDocument.getBytes(charsetEncoding)));
    return doc.getDocumentElement();
  }

  private static NodeList getNodeList(final Element element, final String nodeName)
      throws SAXException, IOException, ParserConfigurationException {
    return element.getElementsByTagName(nodeName);
  }

  private static SearchHit convertToSearchHit(final Element hitNode)
      throws InvalidDateException, TransformerException {
    final NamedNodeMap hitNodeMap = hitNode.getAttributes();
    final String hitNumber = getAttributeTextValue(hitNodeMap, "no");
    final String hitScore = getAttributeTextValue(hitNodeMap, "score");

    final Map<String, String> map = getFieldNodeNameAttributeValueMap(hitNode);

    final String pid = map.get("identifier");
    final String type = map.get("property.type");
    final String state = map.get("property.state");
    final Date createdDate = DateParser.parse(map.get("property.createdDate"));
    final Date lastModifiedDate = DateParser.parse(map.get("property.lastModifiedDate"));
    final String contentModel = map.get("property.contentModel");
    final String description = map.get("description");
    final String creator = map.get("creator");
    final String publisher = map.get("publisher");
    final String repositoryName = map.get("repositoryName");
    final Date date = DateParser.parse(map.get("date"));
    final String title = map.get("title");
    final String highlight = map.get("body");
    return new SearchHit(hitNumber, hitScore, pid, title, highlight, type, state, creator, date,
                         createdDate, lastModifiedDate, contentModel, description, publisher,
                         repositoryName);
  }

  private static String getAttributeTextValue(final NamedNodeMap hitNodeMap,
                                              final String attributeName) {
    return hitNodeMap.getNamedItem(attributeName).getTextContent();
  }

  private static Map<String, String> getFieldNodeNameAttributeValueMap(final Element hitNode)
      throws TransformerException {
    final NodeList fieldNodes = hitNode.getElementsByTagName("field");

    final int noOfFields = fieldNodes.getLength();
    final Map<String, String> map = new HashMap<String, String>(noOfFields);
    for (int i = 0; i < noOfFields; i++) {
      final Node node = fieldNodes.item(i);
      final NamedNodeMap attributes = node.getAttributes();
      final String key = getAttributeTextValue(attributes, "name");
//      String value = node.getTextContent();
      String asXmlString = TextUtils.getAsXMLString(node);
      String singleLine = asXmlString.replace('\n', ' ');
      String value = getContentWithinFieldTags(singleLine);
      final String existingValue = map.get(key);
      if (null != existingValue) {
        value = existingValue + MULTIPLE_VALUE_DELIMITER + value;
      }
      map.put(key, value);
    }
    return map;
  }

  private static String getContentWithinFieldTags(final String singleLine) {
    final Matcher matcher = patternToMatchFieldNode.matcher(singleLine);
    if (matcher.find()) {
      //return the first group other than the entire pattern
      return matcher.group(1);
    }
    return null;
  }
}
