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
 * This class represents the start-point() XPointer function.
 */

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import javax.xml.transform.TransformerException;

public class StartPoint {

    /** Creates new StartPoint */
    public StartPoint() {
    }

     /**
     * Creates the start-point of the given generic location.
     *
     * @param location a generic location
     * @return a collapsed range corresponding to the start point of the given location  
     */
    public Range getStartPoint(Location location) throws TransformerException
    {
        if(location.getType()==Location.NODE)
            
            return getStartPoint((Node)location.getLocation());
        
        else
            
            return getStartPoint((Range)location.getLocation());
    }
    
     /**
     * Creates the start-point of the given range.
     * 
     * 
     * @param range a range
     * @return the collapsed range corresponding to the start point of the given range
     */
    private Range getStartPoint(Range range)
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
        retval.setEnd(range.getStartContainer(),range.getStartOffset());
        
        return retval;
    }
    
    /**
     * Creates the start point of the given node.
     *
     * If the given node is of type attribute, the method returns null.
     * For any other kind of node, the container node of resulting point is the given node and the index
     * is zero.
     *    
     * @param node a node
     * @return the collapsed range corresponding to the start point of the given node, null if
     * the given node is of type attribute
     */
    public Range getStartPoint(Node node) throws TransformerException
    {
        Range range;
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else    
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        switch(node.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
                throw new TransformerException("Subresource Error: start-point argument is attribute or namespace");
                
            default:    
                range.setStart(node,0);
                range.setEnd(node,0);
        }
        
        return range;
    }
}
