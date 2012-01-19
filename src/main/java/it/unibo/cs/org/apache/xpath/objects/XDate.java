/*
 * Copyright (c) 2006, University Of Bologna, Italy
 *
 * Contributor: Topaz, Inc. (http://www.topazproject.org)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of University Of Bologna, Italy and Topz, Inc., nor 
 *       the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY AND TOPAZ AND CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package it.unibo.cs.org.apache.xpath.objects;

import it.unibo.cs.xpointer.custom.Date;
import java.util.Calendar;

/**
 *
 * @author  root
 * @version 
 */
public class XDate extends XObject {

    protected Date date;
    
    public XDate(Date date)
    {
        this.date = date;
    }
    
    /** Creates new XDate */
    public XDate(int year,int month,int day) {
        date = new Date(year,month,day);
    }

    public XDate(int year,int month,int day,int hour,int minute,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,timeZoneID);
    }
    
    public XDate(int year,int month,int day,int hour,int minute,int second,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,second,timeZoneID);
    }
    
    public XDate(int year,int month,int day,int hour,int minute,int second,int millisecond,String timeZoneID)
    {
        date = new Date(year,month,day,hour,minute,second,millisecond,timeZoneID);
    }
    
    
    public XDate(String s) throws java.text.ParseException
    {
        date = new Date(s);
    }
    
    public void addDays(int n)
    {
        date.add(java.util.Calendar.DATE,n);
    }
    
    public int subtractDate(XDate xDate)
    {
        return date.subtractDate(xDate.date);
    }
    
    public String str()
    {
        return date.toString();
    }
    
   /**
   * Given a request type, return the equivalent string.
   * For diagnostic purposes.
   *
   * @return type string "#DATE"
   */
    public String getTypeString()
    {
        return "#DATE";
    }
    
    public int getType()
    {
        return CLASS_DATE;
    }

    public boolean greaterThan(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.greaterThan(((XDate)obj2).date);
    }
    
    public boolean equals(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.equal(((XDate)obj2).date);
    }
    
    public boolean lessThan(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.lessThan(((XDate)obj2).date);
 
    }
    
    public boolean greaterThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.greaterThanOrEqual(((XDate)obj2).date);

    }
    
    public boolean lessThanOrEqual(XObject obj2) throws javax.xml.transform.TransformerException
    {
        if((obj2 instanceof XDate)==false)
            throw new javax.xml.transform.TransformerException("XObject not comparable to XDate.");
        
        return date.lessThanOrEqual(((XDate)obj2).date);
 
    }
    
    public void addDuration(XDuration duration)
    {
        date.add(Calendar.YEAR,duration.getYear());
        date.add(Calendar.MONTH,duration.getMonth());
        date.add(Calendar.DATE,duration.getDay());
        date.add(Calendar.HOUR,duration.getHour());
        date.add(Calendar.MINUTE,duration.getMinute());
        date.add(Calendar.SECOND,duration.getSecond());
        date.add(Calendar.MILLISECOND,duration.getMillisecond());
    }
    
    public void subtractDuration(XDuration duration)
    {
        date.add(Calendar.YEAR,-1*duration.getYear());
        date.add(Calendar.MONTH,-1*duration.getMonth());
        date.add(Calendar.DATE,-1*duration.getDay());
        date.add(Calendar.HOUR,-1*duration.getHour());
        date.add(Calendar.MINUTE,-1*duration.getMinute());
        date.add(Calendar.SECOND,-1*duration.getSecond());
        date.add(Calendar.MILLISECOND,-1*duration.getMillisecond());
    }
}
