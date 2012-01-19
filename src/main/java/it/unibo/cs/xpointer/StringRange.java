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
import java.util.Vector;
import it.unibo.cs.xpointer.litmatch.*;
import it.unibo.cs.org.apache.xpath.XPathAPI;
import javax.xml.transform.TransformerException;

/**
 * This class represents the string-range() XPointer function.
 * 
 */

public class StringRange {

    /*contiene i nodi testo del frammento individuato dal parametro della string-range()*/
    /** contains the text nodes of the document fragment selected by the first parameter of string-range()*/
    private TextTree textTree = null;
    
    /*contiene i nodi testo dell'intero documento*/
    /** contains text nodes of the entire document*/
    private TextTree entireTextTree = null;
    
    private TextNodeFragment nodeFragment = null;
    
    
    /** Creates new StringRange 
     * @param location the location where the matching sting is looked for
     */
    public StringRange(Location location){
       
        
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
            
            Node cac = searchingRange.getCommonAncestorContainer();
                
            textTree = new TextTree(searchingRange);
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
            
            textTree = new TextTree(tempNode);   
           
            if(tempNode.getNodeType()==Node.DOCUMENT_NODE)
                entireTextTree = new TextTree(tempNode);
            else
                entireTextTree = new TextTree(tempNode.getOwnerDocument().getDocumentElement());
        }
    }

     /*
     * THIS IS NOT JAVADOC
     * Seleziona i punti da cui inizia la stringa da matchare.
     * Esamino i nodi testo in questo modo:
     * (i=1,2,3)
     * 1
     * 1 2
     * 1 2 3 
     * e considero le stringhe che fanno match e che iniziano nel nodo testo i-esimo.
     * In questo modo riesco a selezionare anche più match all'interno di uno stesso nodo testo e match a cavallo di più 
     * nodi testo.
     * Esempio (pattern = "ciao mondo"):
     *  <el>ciao mondo  ciao mondo </el> 
     *  <el>ciao mo</el><el>ndo  ciao mondo</el>
     *
     * @param param la stringa che deve essere ricercata
     * @return un vettore contenente i punti selezionati
     */
    
    /**
     * Creates a Vector containing points from which the matching string starts in the document.
     * A given string may appear multiple times in a document. For each occurrence, this method 
     * finds the point where that occurence starts from. 
     *
     * @param param the matching string
     * @return a Vector of TextPoint objects  
     */
    private Vector getMatchPoint(String param)
    {
        //NodeList nodiTesto = textTree.getTextNodes();
        TextNodeFragment [] nodiTesto = textTree.getTextNodeFragments();
        Vector totalMatch = new Vector();
        
        String buffer;
        LE litexp = new LE(param);
        boolean matchFound;
        LEMatch [] leMatches;
        Range tempRange;
        
        for(int i=0;i<nodiTesto.length;i++)
        {
            matchFound = false;
            buffer = "";
            for(int j=i;j<nodiTesto.length && matchFound==false ;j++)
            {
                buffer += nodiTesto[j].getNode().getNodeValue().substring(nodiTesto[j].getStartIndex(),nodiTesto[j].getEndIndex());
                leMatches = litexp.getAllMatches(buffer);
                if(leMatches.length>0)
                {
                    /*vedo se il match comincia nel i-esimo nodo testo*/
                    for(int k=0;k<leMatches.length;k++)
                    {
                        if(leMatches[k].getStartIndex()+nodiTesto[i].getStartIndex()<nodiTesto[i].getEndIndex())
                        {
                            TextPoint tempPoint = new TextPoint(entireTextTree); //il problema è qui
                            tempPoint.setContainer(nodiTesto[i].getNode());
                            tempPoint.setIndex(leMatches[k].getStartIndex()+nodiTesto[i].getStartIndex());
                            totalMatch.addElement(tempPoint);
                            matchFound = true;
                        }    
                    }
                }
            }
        }

        if (param.equals("")) {
          // special case: match beyond end as per spec
          int last = nodiTesto.length - 1;
          if (last >= 0) {
            TextPoint tempPoint = new TextPoint(entireTextTree); //il problema è qui
            tempPoint.setContainer(nodiTesto[last].getNode());
            tempPoint.setIndex(nodiTesto[last].getEndIndex());
            totalMatch.addElement(tempPoint);
          }
        }
       
        return totalMatch;
    }
    
    /**
     * Returns a range determined by searching the string-value of the location
     * for substrings the match the given string.
     * The empty string is defined to select the entire location.   
     *
     * @param match the matching string
     * @param offset position of the first character to be in the resulting range
     * @param length the number of characters in the resulting range
     * @return an array of ranges which contain the matchted string
     */
    public Range[] getStringRange(String match,int offset,int length) throws TransformerException
    {
        
        /*perchè la string-range comincia a contare da uno*/
        offset -= 1;
        
        if(nodeFragment!=null)
            return searchGenericNodes(match,offset,length);
        
        /*il quarto parametro non può essere negativo!!!*/
        if(length<0)
            throw new TransformerException("Fourth parameter is negative");
        
        Vector pl = getMatchPoint(match);
        
        int startChars,endChars; //numero di caratteri prima del punto iniziale e finale
        TextPoint textPoint,startPoint,endPoint;
        Vector vectorRanges = new Vector();
        
        for(int i=0; i<pl.size();i++)
        {
            textPoint = (TextPoint) pl.elementAt(i);
            startChars = textPoint.retrievePrecedingCharacters() + offset;
            startPoint = entireTextTree.retrievePointAfter(startChars,true); 
            if(length!=0)
            {
                endChars = startChars + length;
                endPoint = entireTextTree.retrievePointAfter(endChars,false);
            }
            /*caso in cui devo ritornare un range collassato*/
            else
            {
                endPoint = startPoint;
            }
            
            /*se il punto iniziale è prima dell'inizio del documento oppure dopo la fine,si ha errore*/
            if ((startPoint==null) && (endPoint == null)) {
              // skip locations that lie entirely outside the document
              continue;
            }

            if (startPoint == null) {
                // truncate start to start of doc
                startPoint = new TextPoint(entireTextTree);
                startPoint.setContainer(entireTextTree.getFirst());
                startPoint.setIndex(0);
            }
              
            if (endPoint==null) {
                // truncate end to end of doc
                endPoint = new TextPoint(entireTextTree);
                endPoint.setContainer(entireTextTree.getLast());
                endPoint.setIndex(entireTextTree.getLast().getNodeValue().length());
            }
            
            Range tempRange = ((DocumentRange)(textPoint.getContainer().getOwnerDocument())).createRange();
            tempRange.setStart(startPoint.getContainer(),startPoint.getIndex());
            tempRange.setEnd(endPoint.getContainer(),endPoint.getIndex());
            
            vectorRanges.addElement(tempRange);
        }
        
        Range [] rangeArray = new Range[vectorRanges.size()];
        
        for(int i=0;i<vectorRanges.size();i++)
            rangeArray[i] = (Range) vectorRanges.elementAt(i);
        
        return rangeArray;
    }
    
    /**
     * Returns a range determined by searching the string-value of the location
     * for substrings the match the given string.
     * The empty string is defined to select the entire location.   
     * The position of the first character in the range is 1, i.e. the range 
     * starts immediatly before the first character of the matched string.
     * The range extends to the end of the matched string. 
     *
     * @param match the matching string
     * @return an array of ranges which contain the matchted string
     */
    public Range[] getStringRange(String match) throws TransformerException
    {
        return getStringRange(match,1,match.length());
    }
    
    /*
     * Se offset &egrave; minore di 1, il range risultante parte da 
     * una posizione ottenuta contando all'indietro abs(offset)+1 posizioni a partire dall'inizio
     * della stringa matchata. 
     * Se offset &egrave; maggiore della lunghezza della stringa matchata,
     * viene ritornato un range lungo zero (un punto). 
     * @param match la stringa da ricercare per produrre il range
     * @param offset posizione del primo carattere contenuto nel range risultante; nota che un offset pari ad 1 punta
     * prima del primo carattere della stringa
     * @return un array di range contenenti la stringa passata come argomento 
     */
    
    /**
     * Returns a range determined by searching the string-value of the location
     * for substrings the match the given string.
     * The empty string is defined to select the entire location.   
     * The range extends to the end of the matched string. 
     * If the offset is less than 1, the resulting range starts from a position
     * obtained counting abs(offstet)+1 positions backwards from the beginning of
     * the matched string.
     * If the offset is greater than the length of the matched string, a collapsed range
     * is returned. 
     *
     * @param match the matching string
     * @param offset position of the first character to be in the resulting range
     * @return an array of ranges which contain the matchted string
     */
    public Range[] getStringRange(String match,int offset) throws TransformerException
    {
        int len;
        
        
        if(offset<=0)
            len = java.lang.Math.abs(offset) + match.length() + 1;
        else 
            if(offset>match.length())
                len = 0;
            else  //caso standard
                len = match.length() - offset + 1;
        
        return getStringRange(match,offset,len);
        
    }
    
    /**
     * Search the matching string in other node types (not elements), such as attributes,
     * processing-instructions, comments.
     * The string must be wholly enclosed in a node.
     *
     * @param match the matching string
     * @return the ranges containing the matched string
     */
    private Range [] searchGenericNodes(String match,int offset,int len) throws TransformerException
    {
        Vector result = new Vector();
        
        DocumentRange docRange = (DocumentRange) nodeFragment.getNode().getOwnerDocument();
        
        String buffer = nodeFragment.getNode().getNodeValue().substring(nodeFragment.getStartIndex(),nodeFragment.getEndIndex());
        
        int index = -1;
        
        while((index=buffer.indexOf(match,index+1))!=-1)
        {
            if(index+nodeFragment.getStartIndex()+offset+len > nodeFragment.getNode().getNodeValue().length())
                throw new TransformerException("Subresource Error");
            
            if(index+nodeFragment.getStartIndex()+offset < 0)
                throw new TransformerException("Subresource Error");
            
            Range range = docRange.createRange();
            
            Node targetNode;
            
            if(nodeFragment.getNode().getNodeType()==Node.ATTRIBUTE_NODE)
            {
                targetNode = nodeFragment.getNode().getFirstChild();
            }
            else
            {
                targetNode = nodeFragment.getNode();
            }
            
            range.setStart(targetNode,index+offset);
            range.setEnd(targetNode,index+offset+len);
            
            result.addElement(range);
        }
        
        Range []retval = new Range[result.size()];
        
        for(int i=0;i<result.size();i++)
        {
            retval[i] = (Range)result.elementAt(i);
        }
        
        return retval;
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
}
