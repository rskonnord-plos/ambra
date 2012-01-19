/*
 * XDuration.java
 *
 * Created on 4 marzo 2002, 14.27
 */

package it.unibo.cs.org.apache.xpath.objects;

/**
 *
 * @author  root
 * @version 
 */
public class XDuration extends XObject {

    private int year,month,day,hour,minute,second,millisecond;
    
    /** Creates new XDuration */
    public XDuration() {
        year = month = day = hour = minute = second = millisecond = 0;
    }

    /**
     * Tell what kind of class this is.
     *
     * @return CLASS_UNKNOWN
     */
    public int getType() {
       return CLASS_DURATION;
    }
    
    public String getTypeString() {
        return "#DURATION";
    }
    
    /** Getter for property day.
     * @return Value of property day.
     */
    public int getDay() {
        return day;
    }
    
    /** Setter for property day.
     * @param day New value of property day.
     */
    public void setDay(int day) {
        this.day = day;
    }
    
    /** Getter for property hour.
     * @return Value of property hour.
     */
    public int getHour() {
        return hour;
    }
    
    /** Setter for property hour.
     * @param hour New value of property hour.
     */
    public void setHour(int hour) {
        this.hour = hour;
    }
    
    /** Getter for property millisecond.
     * @return Value of property millisecond.
     */
    public int getMillisecond() {
        return millisecond;
    }
    
    /** Setter for property millisecond.
     * @param millisecond New value of property millisecond.
     */
    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }
    
    /** Getter for property minute.
     * @return Value of property minute.
     */
    public int getMinute() {
        return minute;
    }
    
    /** Setter for property minute.
     * @param minute New value of property minute.
     */
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    /** Getter for property month.
     * @return Value of property month.
     */
    public int getMonth() {
        return month;
    }
    
    /** Setter for property month.
     * @param month New value of property month.
     */
    public void setMonth(int month) {
        this.month = month;
    }
    
    /** Getter for property second.
     * @return Value of property second.
     */
    public int getSecond() {
        return second;
    }
    
    /** Setter for property second.
     * @param second New value of property second.
     */
    public void setSecond(int second) {
        this.second = second;
    }
    
    /** Getter for property year.
     * @return Value of property year.
     */
    public int getYear() {
        return year;
    }
    
    /** Setter for property year.
     * @param year New value of property year.
     */
    public void setYear(int year) {
        this.year = year;
    }
    
}
