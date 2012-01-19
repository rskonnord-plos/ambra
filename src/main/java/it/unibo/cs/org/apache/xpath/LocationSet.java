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

package it.unibo.cs.org.apache.xpath;

import java.util.Vector;
import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.LocationIterator;
import it.unibo.cs.org.apache.xpath.axes.ContextLocationList;

/**
 * This class acts as a LocationIterator.
 *
 */
public class LocationSet implements LocationIterator,ContextLocationList,Cloneable{

    private Vector v;
    private int iterator;
    
    /** Creates new LocationSet */
    public LocationSet() {
        v = new Vector();
        iterator = 0;
    }

    public void addLocation(Location location)
    {
        v.addElement(location);
    }
    
    public int getLength()
    {
        return v.size();
    }
    
    public Location elem(int i)
    {
        return (Location)v.elementAt(i);
    }
    
    public Location nextLocation() {
        
        if(iterator<v.size())
            return (Location) v.elementAt(iterator++);
        else 
            return null;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        LocationSet clone = (LocationSet) super.clone();
        
        clone.v = new Vector();
        
        for(int i=0;i<v.size();i++)
            clone.v.addElement(((Location)v.elementAt(i)).clone());
        
        return clone;
    }
    
    public void reset()
    {
        iterator = 0;
    }
    
    public void setCurrentPos(int i) {
        iterator = i;
    }
    
    public int size() {
        return v.size();
    }
    
    public int getCurrentPos() {
        return iterator;
    }
    
    public Location getCurrentLocation() {
        
        return (Location)v.elementAt(iterator-1);
    }
    
}
