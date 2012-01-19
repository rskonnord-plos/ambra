package com.opensymphony.webwork.result;


import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Result to generate a XML response from an Action.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>
 */
public class AjaxXMLResult implements Result {


  /**
   * Serializable ID.
   */
  public static final long serialVersionUID = 1;


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxXMLResult.class);


  /**
   * Generate a XML response from the Action associated with this request
   * using the fields of the Action to populate the message.
   *
   * @param inInvocation The execution state of the action.
   */
  public void execute(final ActionInvocation inInvocation)  {

    // Get the response and Action objects.
    ActionContext actionContext = inInvocation.getInvocationContext();
    HttpServletResponse response =
      (HttpServletResponse)actionContext.get(
        ServletActionContext.HTTP_RESPONSE);
    Object action = inInvocation.getAction();

    // Generate XML.  Do this by iterating over all the fields of the Action,
    // and for each, based on what type it is (scalar, List, Map or array).
    // generate the appropriate XML.  First, start it off with the root node
    // being the name of the Action class (probably should get this from some
    // specially-defined field in the Action, but this is good enough for now).
    StringBuffer xml = new StringBuffer(2048);
    xml.append("<" + action.getClass().getName() + ">\n");
    Map props = null;

    try {

      // Get the list of fields that expose an accessor.
      props = PropertyUtils.describe(action);

      for (Iterator it = props.keySet().iterator(); it.hasNext();) {

        String fieldName = (String)it.next();

        // Not sure what the "class" field is, but it needs to be ignored, until
        // I find the right answer about it!
        if (fieldName.equalsIgnoreCase("class")) {
          continue;
        }

        // Get type of field.
        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(
          action, fieldName);
        Class propertyClass = pd.getPropertyType();
        if (log.isDebugEnabled()) {
          log.debug("Field to set = " + pd.getName());
        }

        // **********
        // ** List **
        // **********
        if (List.class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a List");
          }
          List fVal = (List)PropertyUtils.getSimpleProperty(action, fieldName);
          for (Iterator lit = fVal.iterator(); lit.hasNext();) {
            String nextVal = (String)lit.next();
            xml.append("  <" + fieldName + ">" + nextVal +
              "</" + fieldName + ">\n");
          }
        // *********
        // ** Map **
        // *********
        } else if (Map.class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a Map");
          }
          Map fVal = (Map)PropertyUtils.getSimpleProperty(action, fieldName);
          for (Iterator mit = fVal.entrySet().iterator(); mit.hasNext();) {
            Map.Entry entry      = (Map.Entry)mit.next();
            String    entryName  = (String)entry.getKey();
            Object    entryValue = entry.getValue();
            xml.append("  <" + fieldName + " key=\"" + entryName + "\">" +
              entryValue + "</" + fieldName + ">\n");
          }
        // ***********
        // ** Array **
        // **********(
        } else if (Object[].class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is an array");
          }
          Object theArray = PropertyUtils.getProperty(action, fieldName);
          int len = Array.getLength(theArray);
          for (int i = 0; i < len; i++) {
            Object nextVal = Array.get(theArray, i);
            xml.append("  <" + fieldName + ">" + nextVal +
              "</" + fieldName + ">\n");
          }
        // *******************
        // ** Simple scalar **
        // *******************
        } else {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a simple scalar");
          }
          Object fVal = PropertyUtils.getSimpleProperty(action, fieldName);
          xml.append("  <" + fieldName + ">" + fVal + "</" + fieldName + ">\n");
        }

      } // End iterator.

      // Finish up the XML.
      xml.append("</" + action.getClass().getName() + ">");

    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
    } catch (NoSuchMethodException nsme) {
      nsme.printStackTrace();
    } catch (InvocationTargetException ite) {
      ite.printStackTrace();
    }

    // Write XML to response.
    try {
      response.setContentLength(xml.toString().length());
      response.setContentType("text/xml");
      PrintWriter out = response.getWriter();
      out.print(xml.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

  } // End execute().


} // End class.
