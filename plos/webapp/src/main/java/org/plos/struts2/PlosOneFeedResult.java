/*
 * $HeadURL::                                                                            $ $Id:
 * PlosStreamResult.java 946 2006-11-03 22:23:42Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc. http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import org.apache.struts2.ServletActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.io.WireFeedOutput;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A WebWorks Result for Feeds.
 *
 * @author jsuttor
 */
public class PlosOneFeedResult implements Result {

  private String feedName;

  private static final Log log = LogFactory.getLog(PlosOneFeedResult.class);

  /**
   * @see com.opensymphony.xwork2.Result#execute(com.opensymphony.xwork2.ActionInvocation)
   */
  public void execute(ActionInvocation ai) throws Exception {

    // feedName must be available to find feed on stack
    if (feedName == null) {
      throw new RuntimeException("Internal logic error, no feedName provided.");
    }

    if (log.isDebugEnabled()) {
      log.debug("Creating a PlosOneFeedResult with feedName=" + feedName);
    }

    // get the feed from the stack that can be found by the feedName
    WireFeed feed = (WireFeed) ai.getStack().findValue(feedName);
    // feed must be available on stack
    if (feed == null) {
      throw new RuntimeException("Internal logic error, feedName, " + feedName + ", resolves to null.");
    }

    // work w/HTTP directly, avoid WebWorks interactions
    HttpServletRequest  request  = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    // TODO: any optimization of HTTP headers?
    response.setContentType("application/atom+xml");
    response.setCharacterEncoding(feed.getEncoding());

    // use Writer for Rome (no OutputStream) -> response
    WireFeedOutput feedOutput = new WireFeedOutput();
    Writer out = null;
    try {
      out = response.getWriter();
      feedOutput.output(feed, out);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      // close the output writer (will flush automatically)
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Allow WebWorks to set the feedName.
   *
   * @param feedName Name of the feed in WebWorks, must provide a get${feedName} in the action.
   */
  public void setFeedName(String feedName) {
          this.feedName = feedName;
  }
}
