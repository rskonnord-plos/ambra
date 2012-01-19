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
import javax.xml.transform.TransformerException;

/**
 * This class represents the endpoint() XPointer function.
 */
public class EndPoint {

    /** Creates new EndPoint */
    public EndPoint() {
    }

    /**
     * Creates the end-point of the given generic location.
     *
     * @param location a generic location
     * @return a collapsed range corresponding to the end point of the given location  
     */
    public Range getEndPoint(Location location) throws TransformerException
    {
        if(location.getType()==Location.NODE)
            
            return getEndPoint((Node)location.getLocation());
        
        else
            
            return getEndPoint((Range)location.getLocation());
    }
    
    /**
     * Creates the end-point of the given range.
     * 
     * 
     * @param range a range
     * @return the collapsed range corresponding to the end point of the given range
     */
    public Range getEndPoint(Range range)
    {
        Node node = range.getStartContainer();
        DocumentRange docRange;
        Range retval;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            docRange = (DocumentRange) node;
        else
            docRange = (DocumentRange) node.getOwnerDocument();
        
        retval = docRange.createRange();
     
        retval.setStart(range.getEndContainer(),range.getEndOffset());
        retval.setEnd(range.getEndContainer(),range.getEndOffset());
        
        return retval;
    }
    
    /**
     * Creates the end-point of the given node.
     * 
     * If the given node is of type root or element, the container node of the resulting point is the given node and
     * the index is the number of children of the given node.
     * If the given node is of type text, comment or processing istruction, the container node of the resulting 
     * point is the given node and the index is the length of the string-value of the given node.
     * If the given node is of type attribute or namespace, the method returns null.
     * 
     * @param node a node
     * @return the collapsed range corresponding to the end point of the given node, null if
     * the given node is of type attribute
     */
    public Range getEndPoint(Node node) throws TransformerException
    {
        Range range;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else        
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        switch(node.getNodeType())
        {
            case Node.ELEMENT_NODE:
            case Node.DOCUMENT_NODE:
                range.setStart(node,node.getChildNodes().getLength());
                range.setEnd(node,node.getChildNodes().getLength());
                break;
                
            case Node.ATTRIBUTE_NODE:
                throw new TransformerException("Subresource Error: endpoint argument is of type attribute or namespace");
                
                
            default:
                range.setStart(node,node.getNodeValue().length());
                range.setEnd(node,node.getNodeValue().length());
        }
        
        return range;
    }
}
