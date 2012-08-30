package org.ambraproject.action.article;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 5/15/12
 */
public class FetchPowerPointActionTest extends AmbraWebTest {

  @Autowired
  protected FetchPowerPointAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void test() throws IOException, NoSuchArticleIdException {
    setUpArticleForImageFromFilestore();
    action.setUri(IMAGE_DOI_IN_FILESTORE);

    try {
      String result = action.execute();
      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertEquals(action.getActionMessages().size(), 0,
          "Action returned messages: " + StringUtils.join(action.getActionMessages(), ";"));
      assertEquals(action.getActionErrors().size(), 0,
          "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

      assertEquals(action.getContentType(), "application/vnd.ms-powerpoint", "action had incorrect content type");
      assertEquals(action.getContentDisposition(),
          "filename=\"" + IMAGE_DOI_IN_FILESTORE.replaceFirst("info:doi/", "") + ".ppt\"",
          "Action had incorrect filename");

      assertNotNull(action.getInputStream(), "Action had null input stream");
      Tika tika = new Tika();

      assertEquals(tika.detect(action.getInputStream()), "application/x-tika-msoffice", "input stream was incorrect type");
    } finally {
      if (action.getInputStream() != null) {
        action.getInputStream().close();
      }
    }
  }
}
