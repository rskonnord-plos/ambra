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
 *  The XPRange class represents the XPointer function range().
 *  Note that this is not the range() location test.
 */

import org.w3c.dom.ranges.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import it.unibo.cs.org.apache.xml.utils.UnImplNode;
import javax.xml.transform.dom.*;

public class XPRange {

    /** Creates new XPRange */
    public XPRange() {
    }

    /**
     * Creates a range from the given generic location.
     *
     * @param location the location passed as argument of the range function  
     * @return the range corresponding to the given location
     */
    public Range getRange(Location location)
    {
        if(location.getType()==Location.NODE)
            
            return getRange((Node)location.getLocation());
        
        else
            
            return getRange((Range)location.getLocation());
    }
    
     /**
     * Creates a range from the given node.
     *
     * The range covers the given node.
     * For the root node, the containter of the start point and end point of the returned range
     * is the root node; the index of the start point of the range is zero; and the index of the end 
     * point of the range is the number of children of the root node.
     * For an attribute node, the container node of the start point and end point of the returned range is
     * the attribute node; the index of the start-point of the range is zero; and the index of the end point
     * of the range is the length of the string-value of the attribute node.
     * For any other kind of node, the container node to the start point and end point of the returned range
     * is the parent of the node; the index of the start point of the range is the number of preceding sibling nodes of 
     * the given node; and the index of the end point is one greater than the index of the start point.
     *
     * @param node the node 
     * @return the range corresponding to the given node
     */
    public Range getRange(Node node)
    {
        Range range;
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            range = ((DocumentRange)node).createRange();
        else
            range = ((DocumentRange)node.getOwnerDocument()).createRange();
        
        
        switch(node.getNodeType())
        {
            case Node.ATTRIBUTE_NODE: 
                /*The first child of an attribute or namespace node 
                 is the text node containing the string-value.
                 This text node is the container of start point and end point of the range.*/
                range.setStart(node.getFirstChild(),0);
                range.setEnd(node.getFirstChild(),node.getNodeValue().length());
                break;
                
            case Node.DOCUMENT_NODE:
                range.setStart(node,0);
                range.setEnd(node,node.getChildNodes().getLength());
                break;
                
            default:
                Node parent = node.getParentNode();
                int startIndex=0;
                int endIndex;
                  
                Node temp = parent.getFirstChild();
                while(temp!=node)
                {
                    startIndex++;
                    temp = temp.getNextSibling();
                }
                    
                endIndex=startIndex+1;
                    
                range.setStart(parent,startIndex);
                range.setEnd(parent,endIndex);
                
        }
        
        return range;
    }
    
    /**
     * Creates a range from a given range.
     *
     * The covering range is identical to the given range.
     * @param range the given range
     * @return the same range
     */
     
    public Range getRange(Range range)
    {   // nel caso sia un range oppure un range collassato (un punto) 
        return range;
    }
    
    private Node convert(UnImplNode unImplNode)
    {
        Document result = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        TransformerFactory tf = TransformerFactory.newInstance();
        
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            result = db.newDocument();
            
            Transformer tr = tf.newTransformer();
            tr.transform(new DOMSource(unImplNode),new DOMResult(result));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return result;
    }
}
