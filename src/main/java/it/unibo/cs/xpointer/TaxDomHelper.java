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

import it.unibo.cs.org.apache.xpath.DOMHelper;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;
import it.unibo.cs.xpointer.XPRange;

/**
 * The DOMHelper of the xpath package provides some useful operations
 * that act on nodes.
 * The purpose of this class is providing the same operations for
 * locations.
 * A better name of this class should be ExtDomHelper.
 */
public class TaxDomHelper {

    private DOMHelper domHelper;
    
    /** Creates new TaxDomHelper 
     * @param domHelper the standard DOMHelper
     */
    public TaxDomHelper(DOMHelper domHelper) {
        this.domHelper = domHelper;
    }

    /**
     * Figures out whether loc2 should be considered as being later 
     * in the document than loc1, in Document Order ad defined 
     * in XPointer CR.
     *
     * @param loc1 the location to perform position comparison on
     * @param loc2 the location to perform position comparison on
     * @return false if loc2 comes before loc1,otherwise returns true
     */
    public boolean isLocationAfter(Location loc1,Location loc2)
    {
        XPRange xpRange1,xpRange2;
        Range range1,range2;
        Node startNode1,startNode2,endNode1,endNode2;
        int startIndex1,startIndex2,endIndex1,endIndex2;
        
        xpRange1 = new XPRange();
        xpRange2 = new XPRange();
        range1 = xpRange1.getRange(loc1);
        range2 = xpRange2.getRange(loc2);
        startNode1 = range1.getStartContainer();
        startNode2 = range2.getStartContainer();
        endNode1 = range1.getEndContainer();
        endNode2 = range2.getEndContainer();
        startIndex1 = range1.getStartOffset();
        startIndex2 = range2.getStartOffset();
        endIndex1 = range1.getEndOffset();
        endIndex2 = range2.getEndOffset();   
     
        if(startNode1!=startNode2)
            return domHelper.isNodeAfter(startNode1,startNode2);
        else
        {
            if(startIndex1>startIndex2)
                return false;
            else
            {
                if(startIndex1<startIndex2)
                    return true;
                else
                {
                    if(endNode1!=endNode2)
                        return domHelper.isNodeAfter(endNode1,endNode2);
                    else
                        if(endIndex1>endIndex2)
                            return false;
                        else
                            return true;
                }
            }           
        }
    }
}
