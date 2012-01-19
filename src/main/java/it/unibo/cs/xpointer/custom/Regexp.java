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

import it.unibo.cs.xpointer.*;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import gnu.regexp.*;
import java.util.*;

public class Regexp {

    /** contains the text nodes of the document fragment selected by the first parameter of regexp()*/
    private TextTree textTree;
    
    /** contains text nodes of the entire document*/
    private TextTree entireTextTree;
    
    private int compilationFlags = 0;
    
    private Vector storeContainer = new Vector();
    
    private TextNodeFragment nodeFragment = null;
    
     /** 
     * Creates new Regexp 
     * @param location the location where the matching string is looked for
     */
    public Regexp(Location location) {
        
        if(location.getType()==Location.RANGE)
        {
            Range searchingRange = (Range)location.getLocation();
            
            if(isOtherType(searchingRange))
            {
                Node containerNode = searchingRange.getStartContainer();
                nodeFragment = new TextNodeFragment();
                nodeFragment.setNode(containerNode);
                nodeFragment.setStartIndex(searchingRange.getStartOffset());
                nodeFragment.setEndIndex(searchingRange.getEndOffset());
                return;
            }
            
            textTree = new TextTree(searchingRange);
            Node cac = searchingRange.getCommonAncestorContainer();
            
            if(cac.getNodeType()==Node.DOCUMENT_NODE)
                entireTextTree = new TextTree(((Document)cac).getDocumentElement());
            else
                entireTextTree = new TextTree(cac.getOwnerDocument().getDocumentElement());
        }    
        else
        {
            Node tempNode = (Node) location.getLocation();
            
            if(isOtherType(tempNode))
            {
                nodeFragment = new TextNodeFragment();
                nodeFragment.setNode(tempNode);
                nodeFragment.setStartIndex(0);
                nodeFragment.setEndIndex(tempNode.getNodeValue().length());
                return;
            }
            
            textTree = new TextTree((Node) location.getLocation());   
            
            if(tempNode.getNodeType()==Node.DOCUMENT_NODE)
                entireTextTree = new TextTree(tempNode);
            else
                entireTextTree = new TextTree(tempNode.getOwnerDocument().getDocumentElement());
        }
    }
    
    /** 
     * Creates new Regexp 
     * @param node the node where the matching string is looked for
     */
    public Regexp(Node node)
    {
        textTree = new TextTree(node);
        
        if(node.getNodeType()==Node.DOCUMENT_NODE)
            entireTextTree = new TextTree(node);
        else
            entireTextTree = new TextTree(node.getOwnerDocument().getDocumentElement());
    }
    
    /**
     * Returns an array of ranges containing the string-value selected by the regular expression.
     *
     * @paran pattern the regular expression pattern
     */
    public Range [] getRegexp(String pattern)
    {
        if(nodeFragment!=null)
            return handleGenericNodes(pattern);
        
        Range [] retval;
        String buffer = textTree.toString();
        UncheckedRE ucRE = new UncheckedRE(pattern,compilationFlags);
        
        REMatch [] matches = ucRE.getAllMatches(buffer);
        retval = new Range[matches.length];
        it.unibo.cs.xpointer.TextPoint startPoint,endPoint;
        
        for(int i=0;i<matches.length;i++)
        {
            int index = 0;
            Vector tempVector = new Vector();
            
            while(matches[i].getStartIndex(index)!=-1)
            {
                
                startPoint = entireTextTree.importTextPoint(textTree.retrievePointAfter(matches[i].getStartIndex(index),true));
                endPoint = entireTextTree.importTextPoint(textTree.retrievePointAfter(matches[i].getEndIndex(index),false));
               
                Range tempRange = ((DocumentRange)startPoint.getContainer().getOwnerDocument()).createRange();
                tempRange.setStart(startPoint.getContainer(),startPoint.getIndex());
                tempRange.setEnd(endPoint.getContainer(),endPoint.getIndex());
                tempVector.addElement(tempRange);
                
                index++;
            }
            
            retval[i] = (Range) tempVector.elementAt(0);
            storeContainer.addElement(tempVector);
        }
           
        
        return retval;
    }

    /**
     * Returns an array of ranges which contain the string-value selected by a 
     * sub-pattern of this regular expression enclosed by parenthesis.
     * 
     * @param index the index of the sub-pattern to be returned
     * @return the array of ranges corresponding to a sub-pattern 
     */
    public Range [] getGroups(int index)
    {
        if(storeContainer.size()<=index)
            return null;
        
        Vector temp = (Vector)storeContainer.elementAt(index);
        
        Range [] retval = new Range[temp.size()];
        
        for(int i=0;i<retval.length;i++)
        {
            retval[i] = (Range)temp.elementAt(i);
        }
        
        return retval;
    }
    
    /**
     * Set a compilation flag for the regular expression.
     * @param flag the compilation flag
     */
    public void setFlag(int flag)
    {
        compilationFlags |= flag;
    }
    
      /**
     * Returns true if the node type need to be handled
     * in a different mode from element nodes
     */
    private boolean isOtherType(Node node)
    {
        boolean retval = false;
        int nodeType = node.getNodeType();
        
        switch(nodeType)
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                retval = true;
                break;
            case Node.TEXT_NODE:
                if(node.getParentNode().getNodeType()==Node.ATTRIBUTE_NODE)
                    retval=true;
                break;
        }
        
        
        return retval;
    }
    
    /**
     * Returns true if start-point and end-point of a range have the same
     * container node,the type of which must be handled in a differnet mode from 
     * element nodes.
     */
    private boolean isOtherType(Range range)
    {
       boolean retval = false;
       
       if(range.getStartContainer()==range.getEndContainer())
       {
            Node node = range.getStartContainer();
            
            switch(node.getNodeType())
            {
                case Node.CDATA_SECTION_NODE:
                case Node.COMMENT_NODE:
                case Node.PROCESSING_INSTRUCTION_NODE:
                    retval = true;
                    break;
                case Node.TEXT_NODE:
                    if(node.getParentNode().getNodeType()==Node.ATTRIBUTE_NODE)
                        retval = true;
            }
       }
       
       return retval;
    }
    
    private Range []handleGenericNodes(String pattern)
    {
        UncheckedRE ucRE = new UncheckedRE(pattern,compilationFlags);
        String buffer = nodeFragment.getNode().getNodeValue().substring(nodeFragment.getStartIndex(),nodeFragment.getEndIndex());
        
        DocumentRange docRange = (DocumentRange)nodeFragment.getNode().getOwnerDocument();
        
        REMatch [] matches = ucRE.getAllMatches(buffer);
        Range [] retval = new Range[matches.length];
        
        Node node = nodeFragment.getNode();
        
        for(int i=0;i<matches.length;i++)
        {
            int index = 0;
            Vector tempVector = new Vector();
            
            while(matches[i].getStartIndex(index)!=-1)
            {
                Range tempRange = docRange.createRange();
                
                tempRange.setStart(node,matches[i].getStartIndex(index)+nodeFragment.getStartIndex());
                tempRange.setEnd(node,matches[i].getEndIndex(index)+nodeFragment.getStartIndex());
           
                tempVector.addElement(tempRange);
                index++;
            }
            
            retval[i] = (Range) tempVector.elementAt(0);
            storeContainer.addElement(tempVector);
        }
        
        return retval;
    }
}
