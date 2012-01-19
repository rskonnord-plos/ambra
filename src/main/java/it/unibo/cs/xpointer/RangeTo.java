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

/**
 * This class represents the range-to() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;

public class RangeTo {

    /** Creates new RangeTo */
    public RangeTo() {
    }

    /**
     * Creates a range starting from the beginning of the context location 
     * to the end of the given location.
     * The start point of the the range is the start point of the context location and
     * the end point of the range is the end point of the given location, which is evaluated
     * with respect to the context location.  
     * 
     * @param contextLocation the context location
     * @param location the given location
     * @return a range starting from the beginning of the context location 
     * to the end of the given location.
     */
    public Range getRangeTo(Location contextLocation,Location location) throws TransformerException
    {
        StartPoint sp = new StartPoint();
        EndPoint ep = new EndPoint();
        
        Range startPoint = sp.getStartPoint(contextLocation);
        Range endPoint = ep.getEndPoint(location);
        
        Range range;
        Node temp;
        
        if(location.getType()==Location.NODE)
            temp = (Node) location.getLocation();
        else
            temp = ((Range)location.getLocation()).getStartContainer();
        
        if(temp.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)temp).createRange();
        else
            range = ((DocumentRange)temp.getOwnerDocument()).createRange();
        
        range.setStart(startPoint.getStartContainer(),startPoint.getStartOffset());
        range.setEnd(endPoint.getEndContainer(),endPoint.getEndOffset());
        
        TaxDomHelper taxDomHelper = new TaxDomHelper(new it.unibo.cs.org.apache.xpath.DOMHelper());
        Location startLoc = new Location();
        startLoc.setType(Location.RANGE);
        startLoc.setLocation(startPoint);
        Location endLoc = new Location();
        endLoc.setType(Location.RANGE);
        endLoc.setLocation(endPoint);
        if (isRangeAfter(startPoint,endPoint)==false)
            throw new TransformerException("Subresource Error: range start-point is after end-point");
        
        return range;
    }
    
    private boolean isRangeAfter(Range r1,Range r2)
    {
        switch(r1.compareBoundaryPoints(Range.START_TO_START,r2))
        {
            case -1: return true;
                
            case 1 : return false;
            
            default: //0
                switch(r1.compareBoundaryPoints(Range.END_TO_END,r2))
                {
                    case -1: 
                    case 0:    return true;
                    default: return false; //1
                }
        }
            
    }
}
