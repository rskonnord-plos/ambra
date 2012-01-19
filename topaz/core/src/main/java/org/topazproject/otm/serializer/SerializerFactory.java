/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.serializer;

import java.net.URI;
import java.net.URL;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Predicate;

/**
 * A factory for creating serializers for basic java types.
 *
 * @author Pradeep Krishnan
 */
public class SerializerFactory {
  private Map<Class, Map<String, Serializer>> serializers;
  private SessionFactory                      sf;
  private static Map<Class, String>           typeMap = new HashMap<Class, String>();
  private final Set<Class>                    supers  = new LinkedHashSet<Class>();

  static {
    typeMap.put(String.class, null);
    typeMap.put(Boolean.class, Rdf.xsd + "boolean");
    typeMap.put(Boolean.TYPE, Rdf.xsd + "boolean");
    typeMap.put(Integer.class, Rdf.xsd + "int");
    typeMap.put(Integer.TYPE, Rdf.xsd + "int");
    typeMap.put(Long.class, Rdf.xsd + "long");
    typeMap.put(Long.TYPE, Rdf.xsd + "long");
    typeMap.put(Short.class, Rdf.xsd + "short");
    typeMap.put(Short.TYPE, Rdf.xsd + "short");
    typeMap.put(Float.class, Rdf.xsd + "float");
    typeMap.put(Float.TYPE, Rdf.xsd + "float");
    typeMap.put(Double.class, Rdf.xsd + "double");
    typeMap.put(Double.TYPE, Rdf.xsd + "double");
    typeMap.put(Byte.class, Rdf.xsd + "byte");
    typeMap.put(Byte.TYPE, Rdf.xsd + "byte");
    typeMap.put(URI.class, Rdf.xsd + "anyURI");
    typeMap.put(URL.class, Rdf.xsd + "anyURI");
    typeMap.put(Date.class, Rdf.xsd + "dateTime");
    typeMap.put(Calendar.class, Rdf.xsd + "dateTime");
  }

  /**
   * Creates a new SerializerFactory object.
   *
   * @param sf the session factory
   */
  public SerializerFactory(SessionFactory sf) {
    this.sf       = sf;
    serializers   = new HashMap<Class, Map<String, Serializer>>();

    initDefaults();
  }

  private void initDefaults() {
    DateBuilder<Date>     dateDateBuilder     =
      new DateBuilder<Date>() {
        public Date toDate(Date o) {
          return o;
        }

        public Date fromDate(Date d) {
          return d;
        }
      };

    DateBuilder<Long>     longDateBuilder     =
      new DateBuilder<Long>() {
        public Date toDate(Long o) {
          return new Date(o.longValue());
        }

        public Long fromDate(Date d) {
          return new Long(d.getTime());
        }
      };

    DateBuilder<Calendar> calendarDateBuilder =
      new DateBuilder<Calendar>() {
        public Date toDate(Calendar o) {
          return o.getTime();
        }

        public Calendar fromDate(Date d) {
          Calendar c = Calendar.getInstance();
          c.setTime(d);

          return c;
        }
      };

    setSerializer(String.class, new SimpleSerializer<String>(String.class), false);
    setSerializer(String.class, Rdf.xsd + "string", new SimpleSerializer<String>(String.class),
                  false);
    setSerializer(String.class, Rdf.xsd + "anyURI", new SimpleSerializer<String>(String.class),
                  false);
    setSerializer(String.class, Rdf.rdf + "XMLLiteral", new SimpleSerializer<String>(String.class),
                  false);
    setSerializer(Boolean.class, new XsdBooleanSerializer(), false);
    setSerializer(Boolean.TYPE, new XsdBooleanSerializer(), false);
    setSerializer(Integer.class, new SimpleSerializer<Integer>(Integer.class), false);
    setSerializer(Integer.TYPE, new SimpleSerializer<Integer>(Integer.class), false);
    setSerializer(Integer.class, Rdf.xsd + "double", new IntegerSerializer<Integer>(Integer.class),
                  false);
    setSerializer(Integer.TYPE, Rdf.xsd + "double", new IntegerSerializer<Integer>(Integer.class),
                  false);
    setSerializer(Long.class, new SimpleSerializer<Long>(Long.class), false);
    setSerializer(Long.TYPE, new SimpleSerializer<Long>(Long.class), false);
    setSerializer(Short.class, new SimpleSerializer<Short>(Short.class), false);
    setSerializer(Short.TYPE, new SimpleSerializer<Short>(Short.class), false);
    setSerializer(Float.class, new SimpleSerializer<Float>(Float.class), false);
    setSerializer(Float.TYPE, new SimpleSerializer<Float>(Float.class), false);
    setSerializer(Double.class, new SimpleSerializer<Double>(Double.class), false);
    setSerializer(Double.TYPE, new SimpleSerializer<Double>(Double.class), false);
    setSerializer(Byte.class, new SimpleSerializer<Byte>(Byte.class), false);
    setSerializer(Byte.TYPE, new SimpleSerializer<Byte>(Byte.class), false);
    setSerializer(URI.class, new SimpleSerializer<URI>(URI.class), false);
    setSerializer(URL.class, new SimpleSerializer<URL>(URL.class), false);
    setSerializer(Date.class,
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"), false);
    setSerializer(Calendar.class,
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "dateTime"),
                  false);

    setSerializer(Date.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"), false);
    setSerializer(Date.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "date"), false);
    setSerializer(Date.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "time"), false);

    setSerializer(Long.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"), false);
    setSerializer(Long.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"), false);
    setSerializer(Long.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"), false);

    setSerializer(Long.TYPE, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"), false);
    setSerializer(Long.TYPE, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"), false);
    setSerializer(Long.TYPE, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"), false);

    setSerializer(Calendar.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "dateTime"),
                  false);
    setSerializer(Calendar.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "date"), false);
    setSerializer(Calendar.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "time"), false);

    setSerializer(Enum.class, new EnumSerializer(), true);
    setSerializer(Enum.class, Rdf.xsd + "string", new EnumSerializer(), true);
    setSerializer(Enum.class, Rdf.xsd + "anyURI", new EnumSerializer(), true);
    setSerializer(Enum.class, Rdf.xsd + "XMLLiteral", new EnumSerializer(), true);
  }

  /**
   * Tests if a class must be serialized.
   *
   * @param clazz the class
   *
   * @return true if this class must be serialized
   */
  public boolean mustSerialize(Class clazz) {
    // XXX: may be the apps would like to control this. 
    // XXX: for now piggy-back on the typeMap
    return typeMap.containsKey(clazz);
  }

  /**
   * Gets the default data type for a class (usually for basic java types).
   *
   * @param clazz the class
   *
   * @return data type or null
   */
  public static String getDefaultDataType(Class clazz) {
    return typeMap.get(clazz);
  }

  /**
   * Gets the serializer for a class.
   *
   * @param <T> the java type of the class
   * @param clazz the class
   * @param dataType the data type
   *
   * @return a serializer or null
   */
  public <T> Serializer<T> getSerializer(Class<T> clazz, String dataType) {
    if (dataType == null)
      dataType = Predicate.UNTYPED;

    Map<String, Serializer> m = serializers.get(clazz);

    Serializer<T>           s = (m != null) ? (Serializer<T>) m.get(dataType) : null;

    if (s != null)
      return s;

    for (Class c : supers)
      if (c.isAssignableFrom(clazz))
        return getSerializer(c, dataType);

    return null;
  }

  /**
   * Sets the serializer for a class and data type.
   *
   * @param <T> the java type of the class
   * @param clazz the class
   * @param dataType the data type or null for un-typed
   * @param serializer the serializer to set
   * @param sub indicates that the serializer can be used for sub-classes
   *
   * @return previous serializer if any
   */
  public <T> Serializer<T> setSerializer(Class<T> clazz, String dataType, Serializer<T> serializer,
                                         boolean sub) {
    if (sub)
      supers.add(clazz);

    if (dataType == null)
      dataType = Predicate.UNTYPED;

    Map<String, Serializer> m = serializers.get(clazz);

    if (m == null)
      serializers.put(clazz, m = new HashMap<String, Serializer>());

    return (Serializer<T>) m.put(dataType, serializer);
  }

  /**
   * Sets the default and un-typed serializer for a class.
   *
   * @param <T> the java type of the class
   * @param clazz the class
   * @param serializer the serializer to set
   * @param sub indicates that the serializer can be used for sub-classes
   */
  public <T> void setSerializer(Class<T> clazz, Serializer<T> serializer, boolean sub) {
    String dataType = typeMap.get(clazz);

    if (dataType != null)
      setSerializer(clazz, dataType, serializer, sub);

    setSerializer(clazz, null, serializer, sub);
  }
}
