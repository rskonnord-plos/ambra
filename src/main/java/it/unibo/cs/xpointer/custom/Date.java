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


package it.unibo.cs.xpointer.custom;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 *
 * @author  root
 * @version 
 */
public class Date {

    protected GregorianCalendar gregorianCalendar;
    
    protected Date()
    {   
        gregorianCalendar = new GregorianCalendar();
    }
    
    /** Creates new Date */
    public Date(String s) throws java.text.ParseException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(sdf.parse(s));
       
    }

        
    private void init(int year,int month,int day,int hour,int minute,int second,int millisec,String timeZoneID)
    {
        gregorianCalendar = new GregorianCalendar(year,month,day,hour,minute,second);
        
        gregorianCalendar.set(Calendar.MILLISECOND,millisec);
        
        if(timeZoneID!=null)
        {
            TimeZone tz = TimeZone.getTimeZone(timeZoneID);
            gregorianCalendar.setTimeZone(tz);
        }
    }
    
    public Date(int year,int month,int day,int hour,int minute,int second,int millisec,String timeZoneID)
    {
        init(year,month,day,hour,minute,second,millisec,timeZoneID);
    }
    
    public Date(int year,int month,int day)
    {
        init(year,month,day,0,0,0,0,null);
    }
    
    public Date(int year,int month,int day,int hour,int minute,String timeZoneID)
    {
        init(year,month,day,hour,minute,0,0,timeZoneID);
    }
    
    public Date(int year,int month,int day,int hour,int minute,int second,String timeZoneID)
    {
        init(year,month,day,hour,minute,second,0,timeZoneID);
    }
    
    public String toString()
    {
        return gregorianCalendar.getTime().toString();
    }
    
    public void add(int field,int amount)
    {
        gregorianCalendar.add(field,amount);
    }
    
    public int subtractDate(Date subtDate)
    {
        GregorianCalendar subtGC = subtDate.gregorianCalendar;
        GregorianCalendar major,minor;
        int sign;
        int numdays = 0;
        
        if(subtGC.getTime().getTime()<gregorianCalendar.getTime().getTime())
        {
            major = gregorianCalendar;
            minor = subtGC;
            sign = 1;
        }
        else
        {
            major = subtGC;
            minor = gregorianCalendar;
            sign = -1;
        }
        
        while(equalDate(minor,major)==false)
        {
            minor.add(Calendar.DATE,1);
            numdays++;
        }
        
        return sign*numdays;
    }
    
    private boolean equalDate(GregorianCalendar gc1,GregorianCalendar gc2)
    {
        if(gc1.get(Calendar.DATE)!=gc2.get(Calendar.DATE))
            return false;
        
        if(gc1.get(Calendar.MONTH)!=gc2.get(Calendar.MONTH))
            return false;
        
        if(gc1.get(Calendar.YEAR)!=gc1.get(Calendar.YEAR))
            return false;
        
        return true;
    }
    
    public boolean greaterThan(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()>date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean lessThan(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()<date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean equal(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()==date2.gregorianCalendar.getTime().getTime());
    }
        
    public boolean greaterThanOrEqual(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()>=date2.gregorianCalendar.getTime().getTime());
    }
    
    public boolean lessThanOrEqual(Date date2)
    {
        return (gregorianCalendar.getTime().getTime()<=date2.gregorianCalendar.getTime().getTime());
    }
}
