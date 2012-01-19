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

import java.text.SimpleDateFormat;

import java.util.SimpleTimeZone;

import org.topazproject.otm.Rdf;

/**
 * A serializer for xsd:date, xsd:time and xsd:dateTime.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> The java type to serialize/deserialize to
 */
public class XsdDateTimeSerializer<T> implements Serializer<T> {
  private SimpleDateFormat sparser;
  private SimpleDateFormat zparser;
  private SimpleDateFormat fmt;
  private DateBuilder<T>   dateBuilder;
  private boolean          hasTime = true;

  /**
   * Creates a new XsdDateTimeSerializer object.
   *
   * @param dateBuilder the date builder to use
   * @param dataType the xsd type
   */
  public XsdDateTimeSerializer(DateBuilder<T> dateBuilder, String dataType) {
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

  /*
   * inherited javadoc
   */
  public String serialize(T o) throws Exception {
    synchronized (fmt) {
      return (o == null) ? null : fmt.format(dateBuilder.toDate(o));
    }
  }

  /*
   * inherited javadoc
   */
  public T deserialize(String o, Class<T> c) throws Exception {
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

  /*
   * inherited javadoc
   */
  public String toString() {
    return "XsdDateTimeSerializer";
  }

  /**
   * Comment from Ronald. I give it  Tue Jan 12 11:42:34 PST 1999 what goes into mulgara is
   * 1999-01-12T19:42:34.000Z what comes out of mulgara is 1999-01-12T19:42:34 and the result is
   * Tue Jan 12 19:42:34 PST 1999 (i.e. 8 hours shifted). This problem doesn't occur for date (no
   * time) or time because curiously time comes back as '19:42:34.000Z' i.e. with the Z. I think
   * mulgara dropping the time zone is a bug.
   *
   * @return true
   */
  private boolean mulgaraWorkAround() {
    // xxx : detect if this bug exists in the version we are connected to
    // xxx : or pull this from the triple-store config
    return true;
  }
}
