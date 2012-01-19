package com.opensymphony.webwork.interceptor;


import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This Interceptor auto-populates an Action from an incoming XML message.
 *
 */
public class AjaxXMLInterceptor implements Interceptor {


  /**
   * Serializable ID.
   */
  public static final long serialVersionUID = 1;


  /**
   * Log instance.
   */
  private static Log log = LogFactory.getLog(AjaxXMLInterceptor.class);


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

    // Only process requests with a content-type of "text/xml".
    if (contentType.equalsIgnoreCase("text/xml")) {

      // Get the Action instance.
      Object action = inInvocation.getAction();

      // Only procede if Action implements the appropriate marker interface.
      if (action instanceof AjaxXMLAware) {

        // Get the body content of the request, our XML.
        String xml = AjaxInterceptorHelper.getBodyContent(request);
        if (log.isDebugEnabled()) {
          log.debug("Incoming XML = " + xml);
        }

        // Parse the XML.
        DefaultHandler   handler = new SAXHandler();
        SAXParserFactory spf     = SAXParserFactory.newInstance();
        spf.setValidating(false);
        SAXParser sp = spf.newSAXParser();
        sp.parse(new ByteArrayInputStream(xml.getBytes()), handler);
        if (log.isDebugEnabled()) {
          log.debug("elements = " + ((SAXHandler)handler).getElements());
        }

        // Populate the Action with the parsed elements.
        AjaxInterceptorHelper.populateAction(
          action, ((SAXHandler)handler).getElements());

      } else {
        log.debug("Action does not implement AjaxXMLAware, nothing to do");
      } // End (action instanceof AjaxXMLAware).

    } else {
      log.debug("Request content-type is not text/xml, nothing to do");
    } // End if (contentType.equalsIgnoreCase("text/xml")).

    return Action.SUCCESS;

  } // End before().


  /**
   * destroy(), does nothing.
   */
  public void destroy() {
  } // End destroy().


  /**
   * SAX parser handler.
   */
  private static class SAXHandler extends DefaultHandler {


    /**
     * Serializable ID.
     */
    public static final long serialVersionUID = 1;


    /**
     * Name of the next element encountered in the XML during parsing.
     */
    private String eName;


    /**
     * Name of the root element encountered in the XML during parsing.
     */
    private String root;


    /**
     * StringBuffer used to capture the text of the next element.
     */
    private StringBuffer eValue = new StringBuffer(2048);


    /**
     * Value of the "key" attribute, if present, for the current element.
     */
    private String keyValue;


    /**
     * This is the collection of parsed elements from the incoming XML.
     */
    private Map elements = new HashMap();


    /**
     * Mutator for elements.
     *
     * @param inElements New value for elements.
     */
    public void setElements(final Map inElements) {

      elements = inElements;

    } // End setElements().


    /**
     * Accessor for elements.
     *
     * @return Value of elements.
     */
    public Map getElements() {

      return elements;

    } // End getElements().


    /**
     * Event handler for the opening of an element.
     *
     * @param inURI        URI.
     * @param inLocalName  LocalName.
     * @param inQName      QName.
     * @param inAttributes Attribute.
     */
    public void startElement(final String inURI, final String inLocalName,
      final String inQName, final Attributes inAttributes) {

      eValue.setLength(0);
      keyValue = inAttributes.getValue("key");
      if (root == null) {
        root = inQName;
      } else {
        eName = inQName;
      }

    } // End startElement().


    /**
     * Event handler called when text of an element is encountered.
     *
     * @param inBuffer Character array of the parsed text.
     * @param inStart  Start.
     * @param inLength Length of the text.
     */
    public void characters(final char[] inBuffer, final int inStart,
      final int inLength) {

      eValue.append(inBuffer, inStart, inLength);

    } // End characters().


    /**
     * Event handler called for the closing of an element.
     *
     * @param inURI       URI.
     * @param inLocalName LocalName.
     * @param inQName     QName.
     */
    public void endElement(final String inURI, final String inLocalName,
      final String inQName) {

      // Ignore the root element to avoid double-setting the final element.
      if (!inQName.equals(root)) {
        // Add element to collection.
        AjaxInterceptorHelper.addElement(keyValue, elements, eName,
          eValue.toString());
      } // End if (!inQName.equals(root)).

    } // End endElement().


  } // End class.


} // End class.
