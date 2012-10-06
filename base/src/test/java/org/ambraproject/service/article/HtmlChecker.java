package org.ambraproject.service.article;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * This is an abstract class for testing xsl transform of the article xml.
 *
 * Override the check function to test a specific logic of the xsl.
 *
 */
public abstract class HtmlChecker {

  public abstract void check(Document html);

  private static List<HtmlChecker> allCheckers = new ArrayList<HtmlChecker>();

  // keep track of all the xsl transform tests
  public HtmlChecker() {
    allCheckers.add(this);
  }

  public static List<HtmlChecker> allCheckers() {
    return Collections.unmodifiableList(allCheckers);
  }


  public static HtmlChecker CheckForAnchorTag = new HtmlChecker() {
    @Override
    public void check(Document html) {
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

}
