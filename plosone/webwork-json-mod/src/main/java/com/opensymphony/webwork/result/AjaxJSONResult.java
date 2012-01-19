package com.opensymphony.webwork.result;


import com.metaparadigm.jsonrpc.JSONSerializer;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Result to generate a JSON response from an Action.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>
 */
public class AjaxJSONResult implements Result {


  /**
   * Serializable ID.
   */
  public static final long serialVersionUID = 1;


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxJSONResult.class);
  private String contentType;
  private boolean noCache = false;


  /**
   * Generate a JSON response from the Action associated with this request
   * using the fields of the Action to populate the message.
   *
   * @param inInvocation The execution state of the action.
   */
  public void execute(final ActionInvocation inInvocation)  {

    ActionContext actionContext = inInvocation.getInvocationContext();
    HttpServletResponse response =
      (HttpServletResponse)actionContext.get(
        ServletActionContext.HTTP_RESPONSE);

    // Generate JSON.
    JSONSerializer jsonSerializer = new JSONSerializer();
    jsonSerializer.setDebug(false);
    jsonSerializer.setMarshallClassHints(false);
    jsonSerializer.setMarshallNullAttributes(true);
    String json = null;
    try {
      jsonSerializer.registerDefaultSerializers();
      json = jsonSerializer.toJSON(inInvocation.getAction());
      if (log.isDebugEnabled()) {
        log.debug("\n\nJSON!! = " + json);
      }
    } catch (Exception e) {
      log.error("Error while json serializing:", e);
    }

    if (noCache) {
      // HTTP 1.1 browsers should defeat caching on this header
      response.setHeader("Cache-Control", "no-cache");
      // HTTP 1.0 browsers should defeat caching on this header
      response.setHeader("Pragma", "no-cache");
      // Last resort for those that ignore all of the above
      response.setHeader("Expires", "-1");
    }


    // Write JSON to response.
    try {
      response.setContentLength(json.length());
      //Note: The content type is text so that it can be rendered directly on the browser.
      response.setContentType(contentType);
//      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(json);
    } catch (IOException ioe) {
      log.error("Error while writing out the jso result", ioe);
    }
  } // End execute().

  /**
   * Set the content type for the json result
   * @param contentType contentType
   */
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  /**
   * Set the content type for the json result
   * @param contentType contentType
   */
  public void setNoCache(final boolean inNoCache) {
    this.noCache = inNoCache;
  }

} // End class.
