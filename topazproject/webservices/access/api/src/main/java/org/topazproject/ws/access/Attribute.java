/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.access;

/**
 * Attributes to be used in access evaluation
 *
 * @author Pradeep Krishnan
 */
public class Attribute {
  /**
   * Defined access types. Same as standard XACML attributes.
   */
  public interface Type {
    public static final String ANY_URI       = "http://www.w3.org/2001/XMLSchema#anyURI";
    public static final String BASE64_BINARY = "http://www.w3.org/2001/XMLSchema#base64Binary";
    public static final String BOOLEAN       = "http://www.w3.org/2001/XMLSchema#boolean";
    public static final String DATE          = "http://www.w3.org/2001/XMLSchema#date";
    public static final String DATE_TIME     = "http://www.w3.org/2001/XMLSchema#dateTime";
    public static final String DOUBLE        = "http://www.w3.org/2001/XMLSchema#double";
    public static final String HEX_BINARY    = "http://www.w3.org/2001/XMLSchema#hexBinary";
    public static final String INTEGER       = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String RFC822_NAME   = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";
    public static final String STRING        = "http://www.w3.org/2001/XMLSchema#string";
    public static final String TIME          = "http://www.w3.org/2001/XMLSchema#time";
    public static final String X500_NAME     = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
  }

  private String name;
  private String type;
  private String value;

  /**
   * Creates a new Attribute object.
   *
   * @param name name of this attribute
   * @param type type of this attribute
   * @param value text encoded value of this attribute
   */
  public Attribute(String name, String type, String value) {
    this.name    = name;
    this.type    = type;
    this.value   = value;
  }

  /**
   * Creates a new Attribute object.
   */
  public Attribute() {
    this(null, null, null);
  }

  /**
   * Get the name of this attribute.
   *
   * @return name as String.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of this attribute.
   *
   * @param name the value to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the type of this attribute.
   *
   * @return type as String.
   */
  public String getType() {
    return type;
  }

  /**
   * Set the type of this attribute. One of the defined types from above or any additional types
   * that the access checker understands.
   *
   * @param type the value to set.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get the value for this attribute.
   *
   * @return the text encoded representation of an attribute's value.
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the value for this attribute.
   *
   * @param value the text-encoded representation of an attribute's value
   */
  public void setValue(String value) {
    this.value = value;
  }
}
