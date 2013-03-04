package org.ambraproject.service.article;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
  },
  checkForSupplementaryDOI {
    void check(Document html) {
      //Methodology:
      //get all <p> tags
      //make sure at least one tag has a doi in it and nothing more

      //Rationale:
      //article xml varies from article to article, so the desired tag could be in any <section>
      //and within the section, it could appear in a variety of infeasible-to-predict locations
      //Instead of getting each section and searching for the desired tag, just get all paragraphs
      //and make sure at least one satisfies the test condition

      Matcher m = Pattern.compile("doi/10.1371/journal\\.[a-z]{4}\\.[0-9]{7}").matcher("");

      boolean hasDOI = true;
      for (Element element : html.getElementsByTag("p")) {
        m.reset(element.text());
        hasDOI = m.find();
        if (hasDOI) {
          break;
        }
      }
      assertTrue(hasDOI, "Failed insert doi into supplementary information section");
    }
  };

  abstract void check(Document html);
}
