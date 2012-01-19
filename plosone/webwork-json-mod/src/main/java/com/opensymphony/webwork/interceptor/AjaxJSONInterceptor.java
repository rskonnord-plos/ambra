package com.opensymphony.webwork.interceptor;


import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * This Interceptor auto-populates an Action from an incoming JSON message.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>
 */
public class AjaxJSONInterceptor implements Interceptor {


  /**
   * Serializable ID.
   */
  public static final long serialVersionUID = 1;


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxJSONInterceptor.class);


  /**
   * init(), does nothing.
   */
  public void init() {
  } // End init().


  /**
   * intercept().
   *
   * @param  inInvocation inInvocation.
   * @throws Exception    Exception.
   * @return              Action.SUCCESS.
   */
  public String intercept(final ActionInvocation inInvocation)
    throws Exception {

    // Get incoming request and content type.
    HttpServletRequest request     = ServletActionContext.getRequest();
    String             contentType = request.getHeader("content-type");
    if (log.isDebugEnabled()) {
      log.debug("contentType = " + contentType);
    }

    // Only process requests with a content-type of "application/json".
    if (contentType.equalsIgnoreCase("application/json")) {

      // Get the Action instance.
      Object action = inInvocation.getAction();

      // Only procede if Action implements the appropriate marker interface.
      if (action instanceof AjaxJSONAware) {

        // Get the body content of the request, our JSON.
        String json = AjaxInterceptorHelper.getBodyContent(request);
        if (log.isDebugEnabled()) {
          log.debug("Incoming JSON = " + json);
        }

        // Parse the JSON, producing a JSONObject instance.
        JSONObject jsonObject = new JSONObject(json);
        if (log.isDebugEnabled()) {
          log.debug("jsonObject = " + jsonObject);
        }

        // Now we iterate over the collection of keys parsed and put them in
        // our elements collection.
        Map elements = new HashMap();
        for (Iterator it = jsonObject.keys(); it.hasNext();) {
          String nextKey = (String)it.next();
          Object joValue = jsonObject.get(nextKey);
          // For simple scalar elements in the JSON, they show up as String, in
          // which case it's an easy add.
          if (joValue instanceof String) {
            AjaxInterceptorHelper.addElement(null, elements, nextKey,
              (String)joValue);
          }
          // If the element type is a JSONArray, it can either be a straight
          // array, which means we'd be building up a List in our elements
          // collection, or it could be a JSONObject, which would correspond to
          // a Map.
          if (joValue instanceof JSONArray) {
            JSONArray ja = (JSONArray)joValue;
            for (int i = 0; i < ja.length(); i++) {
              Object aVal = ja.get(i);
              // If the next element is a String, then this is building up a
              // simple List.
              if (aVal instanceof String) {
                AjaxInterceptorHelper.addElement(null, elements, nextKey,
                  (String)aVal);
              }
              // If its a JSONObject, then we are building up a Map, which means
              // we need to iterate over the keys of the JSONObject, and add
              // each to a Map in elements, where the value of nextKey is the
              // key the Map will be stored in elements under, and we then
              // grab the key (mitKey) of the new Map element, and its value
              // (mitVal).
              if (aVal instanceof JSONObject) {
                for (Iterator mit = ((JSONObject)aVal).keys(); mit.hasNext();) {
                  String mitKey = (String)mit.next();
                  Object mitVal = ((JSONObject)aVal).get(mitKey);
                  AjaxInterceptorHelper.addElement(mitKey, elements, nextKey,
                    (String)mitVal);
                }
              }
            }
          }
        }

        // Populate the Action with the parsed elements.
        AjaxInterceptorHelper.populateAction(action, elements);

      } else {
        log.debug("Action does not implement AjaxJSONAware, nothing to do");
      } // End (action instanceof AjaxJSONAware).

    } else {
      log.debug("Request content-type is not application/json, nothing to do");
    } // End if (contentType.equalsIgnoreCase("application/json")).

    return Action.SUCCESS;

  } // End intercept().


  /**
   * destroy(), does nothing.
   */
  public void destroy() {
  } // End destroy().


} // End class.
