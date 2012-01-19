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
 * This class represents the range-inside() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;


public class RangeInside {

    /** Creates new RangeInside */
    public RangeInside() {
    }

    /**
     * Creates a range that covers the contents of a generic location.
     *
     * @param location a location
     * @return a range covering the contents of the location
     */
    public Range getRangeInside(Location location) throws TransformerException
    {
        if(location.getType()==Location.RANGE)
            
            return getRangeInside((Range)location.getLocation());
        
        else
            
            return getRangeInside((Node)location.getLocation());
            
    }
    
    /**
     * Creates a range tha covers the contents of a given range.
     * @param range a range
     * @return the same range
     */
    public Range getRangeInside(Range range)
    {
        Node node = range.getStartContainer();
        DocumentRange docRange;
        Range retval;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            docRange = (DocumentRange) node;
        else
            docRange = (DocumentRange) node.getOwnerDocument();
        
        retval = docRange.createRange();
        retval.setStart(range.getStartContainer(),range.getStartOffset());
        retval.setEnd(range.getEndContainer(),range.getEndOffset());
        
        return retval;
    }
    
    /**
     * Creates a range tha covers the contents of a given node.
     *
     * The container node of the start point and of the end point of the resulting range 
     * is the node passed as an argument.
     * The index of the start point is zero. 
     * If the end point is a character point then its index is the length if the string-value of the node passed as 
     * an argument; otherwise its index is is the number of children of the given node. 
     *
     * @paran node a node
     * @return a range covering the contents of the node
     */
    public Range getRangeInside(Node node)
    {
        Range range;
        
        if(node.getNodeType()==node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        if(node.getNodeType()==Node.ATTRIBUTE_NODE)
            range.setStart(node.getFirstChild(),0);
        else
            range.setStart(node,0);
        
        switch(node.getNodeType())
        {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                range.setEnd(node,node.getNodeValue().length());
                break;
            
            case Node.ATTRIBUTE_NODE:    
                range.setEnd(node.getFirstChild(),node.getNodeValue().length());
                break;
                    
            default:
                range.setEnd(node,node.getChildNodes().getLength());
        }
        
        return range;
    }
}
