package org.ambraproject.service.article;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static org.testng.Assert.assertEquals;

/**
 * This is an abstract class for testing xsl transform of the article xml.
 *
 * Override the check function to test a specific logic of the xsl.
 *
 */

public enum HtmlChecker {
  CheckForAnchorTag {
    void check(Document html) {
      Elements elements = html.getElementsByAttribute("xpathlocation");

      for (Element element : elements) {
        if (element.nodeName().equalsIgnoreCase("p")) {
          String xpathLocation = element.attr("xpathlocation");

          String anchorId = xpathLocation.replaceAll("\\[", "");
          anchorId = anchorId.replaceAll("\\]", "");
          anchorId = anchorId.replaceAll("/", ".");
          anchorId = anchorId.substring(1);

          if (!xpathLocation.equalsIgnoreCase("noSelect")) {
            Elements anchorElementsViaId = html.getElementsByAttributeValue("id", anchorId);
            assertEquals(anchorElementsViaId.size(), 1, "Did not find the anchor tag for the given paragraph");

            Elements anchorElementsViaName = html.getElementsByAttributeValue("name", anchorId);
            assertEquals(anchorElementsViaName.size(), 1, "Did not find the anchor tag for the given paragraph");
          }
        }
      }
    }
  };

  abstract void check(Document html);
}
