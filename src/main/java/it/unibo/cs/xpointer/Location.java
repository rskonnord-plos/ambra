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

package it.unibo.cs.xpointer;

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;

/**
 * This class represents the concept of location according to XPointer CR.
 * A location can be a node,a range or a point. We assume that a point is identical
 * to a collapsed range.
 */

public class Location implements Cloneable{

    public static final int RANGE = 0;
    
    public static final int NODE = 1;
    
    private int type;
    
    private Object location;
    
    public int getType()
    {
            return type;
    }
    
    public void setType(int param)
    {
        type=param;
    }
    
    public void setLocation(Object param)
    {
        location=param;
    }
    
    public Object getLocation()
    {
        return location;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        Location clone = (Location) super.clone();
        
        clone.type = type;
        
        if(type==NODE)
            clone.location = ((Node)location).cloneNode(true);
        else
            clone.location = ((Range)location).cloneRange();
        
        return clone;
    }
    
    /** Creates new Location */
    public Location() {
    }

    /**
     * Compares this location with another one.
     * 
     * @param loc the location to be compared
     * @return true if this location is equal to the location passed as an argument, false otherwise.
     */
    public boolean equals(Location loc)
    {
        if(type!=loc.getType())
            return false;
        
        if(loc.getType()==NODE)
        {
            Node node1 = (Node) loc.getLocation();
            Node node2 = (Node) location;
            return node1.equals(node2);
        }
        else
        {
            Range r1 = (Range) loc.getLocation();
            Range r2 = (Range) location;
            
            if(r1.getStartContainer()!=r2.getStartContainer())
                return false;
            else
                if(r1.getStartOffset()!=r2.getStartOffset())
                    return false;
                else
                    if(r1.getEndContainer()!=r2.getEndContainer())
                        return false;
                    else
                        if(r1.getEndOffset()!=r2.getEndOffset())
                            return false;
                        else
                            return true;
        }
    }
}
