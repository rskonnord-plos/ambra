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
import org.w3c.dom.traversal.*;
import java.util.Vector;

/**
 * This class represents the text nodes which are descendant of a given node.
 * It provides some useful operations on text nodes.
 * 
 */
public class TextTree {

    private Node rootNode;
    
    private Range rootRange;
    
    /** Creates new TextTree 
     * @param rootNode the root of the tree
     */
    public TextTree(Node rootNode) {
        this.rootNode = rootNode;
    }

    public TextTree(Range range)
    {
        this.rootRange = range;
    }
    
    
      
    /**
     * Returns all the text nodes of this tree.
     *
     * @return a nodelist containing all the text nodes of the tree
     */
    private NodeList getTextNodes()
    {
        DocumentTraversal docTraversal;
        
        if(rootNode.getNodeType()==Node.DOCUMENT_NODE)
        {
            docTraversal = (DocumentTraversal)rootNode;
        }
        else
        {
            docTraversal = (DocumentTraversal)rootNode.getOwnerDocument();
        }
        
        NodeIterator ni = docTraversal.createNodeIterator(rootNode,NodeFilter.SHOW_TEXT,null,true);
        
        Node node;
        NodeListImpl nli = new NodeListImpl();
        
        while((node=ni.nextNode())!=null)
        {
            nli.addNode(node);
        }
        
        return nli;
    }
    
    public TextNodeFragment[] getTextNodeFragments()
    {
        if(rootNode!=null)
        {
            Node node;
            NodeList nl = getTextNodes();
            
            TextNodeFragment [] retval = new TextNodeFragment[nl.getLength()];
            
            for(int i=0;i<nl.getLength();i++)
            {
                retval[i] = new TextNodeFragment();
                retval[i].setNode(nl.item(i));
                retval[i].setStartIndex(0);
                retval[i].setEndIndex(nl.item(i).getNodeValue().length());
            }
            
            return retval;
        }
        else
        {
            Node cac = rootRange.getCommonAncestorContainer();
            DocumentTraversal docTrav;
            DocumentRange docRange;
            
            if(cac.getNodeType()==Node.DOCUMENT_NODE)
            {
                docTrav = (DocumentTraversal) cac;
                docRange = (DocumentRange) cac;
            }
            else
            {
                docTrav = (DocumentTraversal) cac.getOwnerDocument();
                docRange = (DocumentRange) cac.getOwnerDocument();
            }
            
            NodeIterator ni = docTrav.createNodeIterator(cac,NodeFilter.SHOW_TEXT,null,true);
            
            Node node;
            Vector result = new Vector();
            TextNodeFragment tnf;
            
            while((node=ni.nextNode())!=null)
            {
                Range nodeRange = docRange.createRange();
                nodeRange.setStart(node,0);
                nodeRange.setEnd(node,node.getNodeValue().length());
                
                if(rootRange.compareBoundaryPoints(Range.START_TO_START,nodeRange)>0) //il nodo testo inizia prima
                {
                    if(rootRange.compareBoundaryPoints(Range.END_TO_START,nodeRange)<=0) //il nodo testo finisce dentro al range
                    {
                        tnf = new TextNodeFragment();
                        tnf.setNode(node);
                        tnf.setStartIndex(rootRange.getStartOffset());
                        if (rootRange.getEndContainer().equals(node))
                          tnf.setEndIndex(rootRange.getEndOffset());
                        else
                          tnf.setEndIndex(node.getNodeValue().length());
                        result.addElement(tnf);
                    }
                }
                else
                {   //il nodo testo inizia dopo l'inizio del range
                
                    if(rootRange.compareBoundaryPoints(Range.START_TO_END,nodeRange)>0) //ma inizia prima della fine del range
                    {
                        if(rootRange.compareBoundaryPoints(Range.END_TO_END,nodeRange)>0) //se finisce prima della fine del range,allora vi è completamente contenuto
                        {
                            tnf = new TextNodeFragment();
                            tnf.setNode(node);
                            tnf.setStartIndex(0);
                            tnf.setEndIndex(node.getNodeValue().length());
                            result.addElement(tnf);
                        }
                        else
                        { //il nodo è a cavallo della fine del range
                            tnf = new TextNodeFragment();
                            tnf.setNode(node);
                            tnf.setStartIndex(0);
                            tnf.setEndIndex(rootRange.getEndOffset());
                            result.addElement(tnf);
                        }
                    }
                }
            }
            
            TextNodeFragment [] resultArray = new TextNodeFragment[result.size()];
            
            for(int i=0;i<result.size();i++)
            {
                resultArray[i] = (TextNodeFragment) result.elementAt(i);
            }
            
            return resultArray;
        }
    }
   
    
    /*
     * Ritorna il punto che sta dopo un certo numero di caratteri all'interno del
     * documento.
     * @return il punto che sta dopo il numero di caratteri specificato, null se l'argomento &egrave; 
     * maggiore del numero di caratteri del documento
     * @param numcar il numero di caratteri dopo cui si vuole ottenere il punto, valore POSITIVO
     * @param beginning se vero, quando il punto sta tra due nodi testo,voglio che stia prima dell'inizio del secondo piuttosto
         che dopo la fine del primo.
     */
    
    /**
     * Returns the object TextPoint which points after the specified number of charactes 
     * in the document fragment.
     * If the specified number is greater than the number of characters contained in the fragment, null
     * is returned.
     * 
     * @param numcar the number of charatres,it must be non-negative
     * @param beginning if true, when the point is between two text nodes, it is positioned at the beginnig
     * of the second one; if false, it is positioned at the end of the first one. 
     * 
     */
     public TextPoint retrievePointAfter(int numcar,boolean beginning)
     {
        if(rootNode!=null)
            return retrievePointAfterForNode(numcar,beginning);
        else
            return retrievePointAfterForRange(numcar,beginning);
     }
    
    private TextPoint retrievePointAfterForRange(int numcar,boolean beginning)
    {
        Node node = null;
        int counter = 0;
        int index = 0;
        Node cac = rootRange.getCommonAncestorContainer();
        Document ownerDoc;
        
        if(cac.getNodeType()==Node.DOCUMENT_NODE)
        {
            ownerDoc = (Document)cac;
        }
        else
        {
            ownerDoc = cac.getOwnerDocument();
        }
       
        NodeIterator nodeIterator = ((DocumentTraversal)ownerDoc).createNodeIterator(cac,NodeFilter.SHOW_TEXT,null,true);
        
       
        
        while((node=nodeIterator.nextNode())!=null)
        {
            index = numcar - counter;
            Range nodeRange = ((DocumentRange)ownerDoc).createRange();
            nodeRange.setStart(node,0);
            nodeRange.setEnd(node,node.getNodeValue().length());
            boolean inside = false;
            
            if(rootRange.compareBoundaryPoints(Range.START_TO_START,nodeRange)>0) //il nodo testo inizia prima
            {
                if(rootRange.compareBoundaryPoints(Range.END_TO_START,nodeRange)<=0) //il nodo testo finisce dentro al range
                {
                    counter += node.getNodeValue().length() - rootRange.getStartOffset();
                    inside = true;
                }
            }
            else
            {   //il nodo testo inizia dopo l'inizio del range
                
                if(rootRange.compareBoundaryPoints(Range.START_TO_END,nodeRange)>0) //ma inizia prima della fine del range
                {
                    inside = true;
                    if(rootRange.compareBoundaryPoints(Range.END_TO_END,nodeRange)>0) //se finisce prima della fine del range,allora vi è completamente contenuto
                    {
                        counter += node.getNodeValue().length();
                    }
                    else
                    { //il nodo è a cavallo della fine del range
                        counter += rootRange.getEndOffset();
                    }
                }
            }
            
            if(inside && counter>=numcar)
                break;
        }
        
        if(node==null)
            return null;
        
        
        Node previous = nodeIterator.previousNode();
        /*se vale null,significa che siamo arrivati alla fine del documento*/
        nodeIterator.nextNode();
        Node next = nodeIterator.nextNode();
        
        if(counter==numcar && next!=null && beginning)
        {
            node = next;
            index = 0;
        }
        
        TextPoint retval = new TextPoint(this);
        retval.setContainer(node);
        retval.setIndex(index);
        
        return retval;
    }
     
    private TextPoint retrievePointAfterForNode(int numcar,boolean beginning)
    {
        int counter = 0;
        
        NodeList nodiTesto = getTextNodes();
        Node container;
        int index=0;
        
        int i;
        
        if(numcar<0)
            return null;
        
        for(i=0;i<nodiTesto.getLength();i++)
        {
            index = numcar - counter;
            counter += nodiTesto.item(i).getNodeValue().length();
            
            if(counter>=numcar)
                break;
            
        }
        
        /*significa che numcar è maggiore della lunghezza del documento*/
        if(i==nodiTesto.getLength() && counter<numcar)
            return null;
        
        /*se il punto sta tra due nodi testo,voglio che stia prima dell'inizio del secondo piuttosto
         che dopo la fine del primo. Fa eccezzione il caso in cui sono arrivato alla fine del documento*/
        if(counter==numcar && i<(nodiTesto.getLength()-1) && beginning)
        {
            container = nodiTesto.item(i+1);
            index = 0;
        }
        else
            container = nodiTesto.item(i);
        
        TextPoint retval = new TextPoint(this);
        retval.setContainer(container);
        retval.setIndex(index);
        
        return retval;
    }
    
      
    /**
     * Returns the content of all the text nodes of the tree.
     */
    private String toStringForNode()
    {
        String buffer="";
        NodeList nodiTesto = getTextNodes();
        
        for(int i=0;i<nodiTesto.getLength();i++)
            buffer += nodiTesto.item(i).getNodeValue();
        
        return buffer;
    }
    
    private String toStringForRange()
    {
        return rootRange.toString();
    }
    
    public String toString()
    {
        if(rootNode!=null)
            return toStringForNode();
        else
            return toStringForRange();
    }
    
  
    /**
     * Imports a TextPoint object which belongs to another tree. 
     *
     * @param textPoint the textPoint to be imported
     * @return the imported TextPoint
     */
    public TextPoint importTextPoint(TextPoint textPoint)
    {
        TextPoint importedTextPoint = new TextPoint(this);
        
        importedTextPoint.setContainer(textPoint.getContainer());
        importedTextPoint.setIndex(textPoint.getIndex());
        
        return importedTextPoint;
    }
    
    /**
     * Returns the last text node in the tree.
     * If the rooto of the tree is a range, the returned node may not be completely contained in
     * the range. 
     */
    public Node getLast()
    {
        TextNodeFragment [] nodiTesto = getTextNodeFragments();
        
        if(nodiTesto.length>0)
        {
            return nodiTesto[nodiTesto.length-1].getNode();
        }
        else 
            return null;
    }
    
    /**
     * Returns the first text node in the tree.
     * If the rooto of the tree is a range, the returned node may not be completely contained in
     * the range. 
     */
    public Node getFirst()
    {
        TextNodeFragment [] nodiTesto = getTextNodeFragments();
        
        if(nodiTesto.length>0)
        {
            return nodiTesto[0].getNode();
        }
        else 
            return null;
    }
    
    
}
