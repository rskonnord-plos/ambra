package com.opensymphony.webwork.interceptor;


import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class contains a couple of helper functions needed by both of the
 * Interceptors and Results.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>
 */
public final class AjaxInterceptorHelper {


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxInterceptorHelper.class);


  /**
   * Utility class, no instantiation allowed.
   */
  private AjaxInterceptorHelper() {
  } // End constructor().


  /**
   * This method will return the body content of an HTTP request as a String
   * (lifted from Java Web Parts: http://javawebparts.sourceforge.net).
   *
   * @param  request     A valid HTTPServletRequest object.
   * @return             A String containing the body content of the request.
   * @throws IOException Catch-all exception.
   */
  public static String getBodyContent(final HttpServletRequest request)
    throws IOException {

    BufferedReader br          = request.getReader();
    String         nextLine    = "";
    StringBuffer   bodyContent = new StringBuffer();
    nextLine = br.readLine();
    while (nextLine != null) {
      bodyContent.append(nextLine);
      nextLine = br.readLine();
    }
    return bodyContent.toString();

  } // End getBodyContent().


  /**
   * This method is called to add a new element to the given elements
   * collection during incoming message parsing.
   *
   * @param keyValue The value of the "key" attribute of the current element of
   *                 XML being parsed, if applicable, null otherwise.
   * @param elements The Map of elements currently being generated.
   * @param eName    The name of the element.
   * @param eValue   The value of the element.
   */
  public static void addElement(final String keyValue, final Map elements,
    final String eName, final String eValue) {

    // Adds a new element to the collection of elements.  If the element
    // did not have a "key" attribute, then we are creating (or adding to)
    // a List.  If the element from the XML document being added is the
    // only one of its kind, it is still stored in a List, it just happens
    // to be the only member.  If it is one of many, then obviously we
    // need to have a collection.  So, this method first tries to retrieve
    // an existing/ List, and creates a new one if not found, otherwise
    // adds to the existing one.  If the "key" attribute *is* found though,
    // then this element will be added to a Map, or a new Map is created.

    if (log.isDebugEnabled()) {
      log.debug("keyValue = " + keyValue + " -- eName = " + eName +
        " -- eValue = " + eValue);
    }

    if (keyValue == null) {

      List elementValues = (List)elements.get(eName);
      if (elementValues == null) {
        elementValues = new ArrayList();
      }
      elementValues.add(eValue);
      elements.put(eName, elementValues);

    } else {

      Map elementValues = (Map)elements.get(eName);
      if (elementValues == null) {
        elementValues = new HashMap();
      }
      elementValues.put(keyValue, eValue);
      elements.put(eName, elementValues);

    }

  } // End addElement().


  /**
   * This method populates the Action with the collection of elements
   * generated from parsing the incoming message in one of the Interceptors.
   *
   * @param inAction   The Action instance being populated.
   * @param inElements The elements parsed from the incoming request POST body.
   */
  public static void populateAction(final Object inAction,
    final Map inElements) {

    for (Iterator it = inElements.entrySet().iterator(); it.hasNext();) {

      // Get the next element in the collection, which is the next field of the
      // Action to set.
      Map.Entry entry       = (Map.Entry)it.next();
      String    fieldName   = (String)entry.getKey();
      Object    fieldValues = entry.getValue();
      if (log.isDebugEnabled()) {
        log.debug("fieldName = " + fieldName + ", " +
          "fieldValues = " + fieldValues);
      }

      try {

        // Determine its type: List, Map, array or simple scalar, and set it
        // as appropriate using Beanutils.
        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(
          inAction, fieldName);
        if (pd == null) {
          log.error("Field " + fieldName + " was parsed from request, but " +
            "was not found in Action");
          return;
        }
        Class propertyClass = pd.getPropertyType();

        // **********
        // ** List **
        // **********
        if (List.class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a List");
          }
          PropertyUtils.setProperty(inAction, fieldName, (List)fieldValues);
        // *********
        // ** Map **
        // *********
        } else if (Map.class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a Map");
          }
          PropertyUtils.setProperty(inAction, fieldName, (Map)fieldValues);
        // ***********
        // ** Array **
        // **********(
        } else if (Object[].class.isAssignableFrom(propertyClass)) {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is an array");
          }
          List   fv         = (List)fieldValues;
          Object theArray   = PropertyUtils.getProperty(inAction, fieldName);
          Class  arrayClass = null;
          try {
            arrayClass = theArray.getClass();
          } catch (NullPointerException npe) {
            log.error("Array '" + fieldName + "' in Action was either not " +
              "present, or not initialized.  Arrays *must* be " +
              "initialized (as an example, you can do: " +
              "String[] a = new String[0]; ... the array does *not* need " +
              "to be populated or even be allocated elements)");
            break;
          }
          Class componentClass = arrayClass.getComponentType();
          theArray = Array.newInstance(componentClass, fv.size());
          int i = 0;
          for (Iterator fvit = fv.iterator(); fvit.hasNext();) {
            Array.set(theArray, i, fvit.next());
            i++;
          }
          PropertyUtils.setProperty(inAction, fieldName, theArray);
        // *******************
        // ** Simple scalar **
        // *******************
        } else {
          if (log.isDebugEnabled()) {
            log.debug(fieldName + " is a simple scalar");
          }
          PropertyUtils.setSimpleProperty(inAction, fieldName,
            ((List)fieldValues).get(0));
        }

      } catch (IllegalAccessException iae) {
        iae.printStackTrace();
      } catch (InvocationTargetException ite) {
        ite.printStackTrace();
      } catch (NoSuchMethodException nsme) {
        nsme.printStackTrace();
      }

    } // End for.

  } // End populateAction().


} // End class.
