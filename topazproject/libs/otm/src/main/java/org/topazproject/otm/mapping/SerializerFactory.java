/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import java.lang.reflect.Constructor;

import java.net.URI;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * A factory for creating serializers for basic java types.
 *
 * @author Pradeep Krishnan
 */
public class SerializerFactory {
  private Map<Class, Map<String, Serializer>> serializers;
  private SessionFactory                      sf;
  private static Map<Class, String>           typeMap = new HashMap<Class, String>();

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

    setSerializer(String.class, new SimpleSerializer<String>(String.class));
    setSerializer(String.class, Rdf.xsd + "string", new SimpleSerializer<String>(String.class));
    setSerializer(String.class, Rdf.xsd + "anyURI", new SimpleSerializer<String>(String.class));
    setSerializer(String.class, Rdf.rdf + "XMLLiteral", new SimpleSerializer<String>(String.class));
    setSerializer(Boolean.class, new XsdBooleanSerializer());
    setSerializer(Boolean.TYPE, new XsdBooleanSerializer());
    setSerializer(Integer.class, new SimpleSerializer<Integer>(Integer.class));
    setSerializer(Integer.TYPE, new SimpleSerializer<Integer>(Integer.class));
    setSerializer(Integer.class, Rdf.xsd + "double", new IntegerSerializer<Integer>(Integer.class));
    setSerializer(Integer.TYPE, Rdf.xsd + "double", new IntegerSerializer<Integer>(Integer.class));
    setSerializer(Long.class, new SimpleSerializer<Long>(Long.class));
    setSerializer(Long.TYPE, new SimpleSerializer<Long>(Long.class));
    setSerializer(Short.class, new SimpleSerializer<Short>(Short.class));
    setSerializer(Short.TYPE, new SimpleSerializer<Short>(Short.class));
    setSerializer(Float.class, new SimpleSerializer<Float>(Float.class));
    setSerializer(Float.TYPE, new SimpleSerializer<Float>(Float.class));
    setSerializer(Double.class, new SimpleSerializer<Double>(Double.class));
    setSerializer(Double.TYPE, new SimpleSerializer<Double>(Double.class));
    setSerializer(Byte.class, new SimpleSerializer<Byte>(Byte.class));
    setSerializer(Byte.TYPE, new SimpleSerializer<Byte>(Byte.class));
    setSerializer(URI.class, new SimpleSerializer<URI>(URI.class));
    setSerializer(URL.class, new SimpleSerializer<URL>(URL.class));
    setSerializer(Date.class, new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Calendar.class,
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "dateTime"));

    setSerializer(Date.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Date.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "date"));
    setSerializer(Date.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Date>(dateDateBuilder, Rdf.xsd + "time"));

    setSerializer(Long.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Long.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"));
    setSerializer(Long.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"));

    setSerializer(Long.TYPE, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Long.TYPE, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "date"));
    setSerializer(Long.TYPE, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Long>(longDateBuilder, Rdf.xsd + "time"));

    setSerializer(Calendar.class, Rdf.xsd + "dateTime",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "dateTime"));
    setSerializer(Calendar.class, Rdf.xsd + "date",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "date"));
    setSerializer(Calendar.class, Rdf.xsd + "time",
                  new XsdDateTimeSerializer<Calendar>(calendarDateBuilder, Rdf.xsd + "time"));
  }

  /**
   * Tests if a class must be serialized. 
   *
   * @param clazz the class
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
   * @param clazz the class
   * @param dataType the data type
   *
   * @return a serializer or null
   */
  public <T> Serializer<T> getSerializer(Class<T> clazz, String dataType) {
    if (dataType == null)
      dataType = Predicate.UNTYPED;

    Map<String, Serializer> m = serializers.get(clazz);

    return (m != null) ? (Serializer<T>) m.get(dataType) : null;
  }

  /**
   * Sets the serializer for a class and data type.
   *
   * @param clazz the class
   * @param dataType the data type or null for un-typed
   * @param serializer the serializer to set
   *
   * @return previous serializer if any
   */
  public <T> Serializer<T> setSerializer(Class<T> clazz, String dataType, Serializer<T> serializer) {
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
   * @param clazz the class
   * @param serializer the serializer to set
   */
  public <T> void setSerializer(Class<T> clazz, Serializer<T> serializer) {
    String dataType = typeMap.get(clazz);

    if (dataType != null)
      setSerializer(clazz, dataType, serializer);

    setSerializer(clazz, null, serializer);
  }

  private class SimpleSerializer<T> implements Serializer<T> {
    private Constructor<T> constructor;

    public SimpleSerializer(Class<T> clazz) {
      try {
        constructor = clazz.getConstructor(String.class);
      } catch (NoSuchMethodException t) {
        throw new IllegalArgumentException("Must have a constructor that takes a String", t);
      }
    }

    public String serialize(T o) throws Exception {
      return (o == null) ? null : o.toString();
    }

    public T deserialize(String o) throws Exception {
      return constructor.newInstance(o);
    }

    public String toString() {
      return "SimpleSerializer[" + constructor.getDeclaringClass().getName() + "]";
    }
  }

  /**
   * When de-serializing an Integer that is stored as some non-integer, be sure to remove
   * the decimal point.
   */
  private class IntegerSerializer<T> extends SimpleSerializer<T> {
    public IntegerSerializer(Class<T> clazz) {
      super(clazz);
    }
    
    public T deserialize(String o) throws Exception {
      int decimal = o.indexOf(".");
      if (decimal != -1)
        o = o.substring(0, decimal); // TODO: Round-off properly?
      return super.deserialize(o);
    }
  }
  
  private static interface DateBuilder<T> {
    public Date toDate(T o);

    public T fromDate(Date d);
  }

  private static class XsdDateTimeSerializer<T> implements Serializer<T> {
    private SimpleDateFormat sparser;
    private SimpleDateFormat zparser;
    private SimpleDateFormat fmt;
    private DateBuilder<T>   dateBuilder;
    private boolean          hasTime = true;

    public XsdDateTimeSerializer(DateBuilder dateBuilder, String dataType) {
      this.dateBuilder = dateBuilder;

      if ((Rdf.xsd + "date").equals(dataType)) {
        zparser   = new SimpleDateFormat("yyyy-MM-ddZ");
        sparser   = new SimpleDateFormat("yyyy-MM-dd");
        fmt       = new SimpleDateFormat("yyyy-MM-dd'Z'");
        hasTime   = false;
      } else if ((Rdf.xsd + "dateTime").equals(dataType)) {
        zparser   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
        sparser   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS");
        fmt       = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

        if (mulgaraWorkAround()) {
          // assume UTC if no timezone specified
          sparser.setTimeZone(new SimpleTimeZone(0, "UTC"));
        }
      } else if ((Rdf.xsd + "time").equals(dataType)) {
        zparser   = new SimpleDateFormat("HH:mm:ss'.'SSSZ");
        sparser   = new SimpleDateFormat("HH:mm:ss'.'SSS");
        fmt       = new SimpleDateFormat("HH:mm:ss'.'SSS'Z'");
      } else {
        throw new IllegalArgumentException("Data type must be an xsd:date, xsd:time or xsd:dateTime");
      }

      fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
      sparser.setLenient(false);
      zparser.setLenient(false);
    }

    public String serialize(T o) throws Exception {
      synchronized (fmt) {
        return (o == null) ? null : fmt.format(dateBuilder.toDate(o));
      }
    }

    public T deserialize(String o) throws Exception {
      if (o == null)
        return null;

      if (o.endsWith("Z"))
        o = o.substring(0, o.length() - 1) + "+00:00";

      int     len         = o.length();
      boolean hasTimeZone =
        ((o.charAt(len - 3) == ':') && ((o.charAt(len - 6) == '-') || (o.charAt(len - 6) == '+')));

      if (hasTime) {
        int    pos    = o.indexOf('.');
        String mss;
        int    endPos;

        if (pos == -1) {
          mss         = ".000";
          pos         = hasTimeZone ? (len - 6) : len;
          endPos      = pos;
        } else {
          // convert fractional seconds to number of milliseconds
          endPos   = hasTimeZone ? (len - 6) : len;
          mss      = o.substring(pos, endPos);

          while (mss.length() < 4)
            mss += "0";

          if (mss.length() > 4)
            mss = mss.substring(0, 4);
        }

        o = o.substring(0, pos) + mss + o.substring(endPos, len);
      }

      if (hasTimeZone) {
        // convert hh:mm to hhmm in timezone
        len   = o.length();
        o     = o.substring(0, len - 3) + o.substring(len - 2, len);
      }

      SimpleDateFormat parser = hasTimeZone ? zparser : sparser;

      synchronized (parser) {
        return dateBuilder.fromDate(parser.parse(o));
      }
    }

    public String toString() {
      return "XsdDateTimeSerializer";
    }

    /**
     * Comment from Ronald. I give it  Tue Jan 12 11:42:34 PST 1999 what goes into mulgara
     * is 1999-01-12T19:42:34.000Z what comes out of mulgara is 1999-01-12T19:42:34 and the result
     * is Tue Jan 12 19:42:34 PST 1999 (i.e. 8 hours shifted). This problem doesn't occur for date
     * (no time) or time because curiously time comes back as '19:42:34.000Z' i.e. with the Z. I
     * think mulgara dropping the time zone is a bug.
     *
     * @return true
     */
    private boolean mulgaraWorkAround() {
      // xxx : detect if this bug exists in the version we are connected to
      // xxx : or pull this from the triple-store config
      return true;
    }
  }

  private static class XsdBooleanSerializer implements Serializer<Boolean> {
    public String serialize(Boolean o) throws Exception {
      return o.toString();
    }

    public Boolean deserialize(String o) throws Exception {
      if ("1".equals(o) || "true".equals(o))
        return Boolean.TRUE;

      if ("0".equals(o) || "false".equals(o))
        return Boolean.FALSE;

      throw new IllegalArgumentException("invalid xsd:boolean '" + o + "'");
    }
  }
}
