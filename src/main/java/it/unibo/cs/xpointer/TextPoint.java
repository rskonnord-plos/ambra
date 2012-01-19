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
/*
 * TextPoint.java
 *
 * Created on 16 gennaio 2002, 9.08
 */

import org.w3c.dom.*;

/**
 * This class represents a character point according to the XPointer CR.
 * It has a container text node and an index.
 */
public class TextPoint {

    private Node container;
    
    private int index;
    
    private TextTree textTree;
    
      
    /** Creates new TextPoint
     * @param the tree which the point belongs to.  
     */
    public TextPoint(TextTree textTree) {
        this.textTree = textTree;
    }

    /** Getter for property container.
     * @return Value of property container.
     */
    public Node getContainer() {
        return container;
    }
    
    /** Setter for property container.
     * @param container New value of property container.
     */
    public void setContainer(Node container) {
        this.container = container;
    }
    
    /** Getter for property index.
     * @return Value of property index.
     */
    public int getIndex() {
        return index;
    }
    
    /** Setter for property index.
     * @param index New value of property index.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
  
    /**
     * Returns the number of characters preceding the point 
     * inside its tree.
     */
    public int retrievePrecedingCharacters()
    {
        TextNodeFragment [] nodiTesto = textTree.getTextNodeFragments();
        int numcar=0; /*i caratteri che stanno prima del punto*/
        int i;
        
        for(i=0;i<nodiTesto.length;i++)
        {
            if(nodiTesto[i].getNode()==container)
                break;
            else
                numcar += nodiTesto[i].getNode().getNodeValue().length()-nodiTesto[i].getStartIndex();
        }
        
        numcar += index - nodiTesto[i].getStartIndex();
         
        return numcar;
    }
    
    /**
     * Returns the tree which the point belongs to.
     */
    protected TextTree getTextTree()
    {
        return textTree; 
    }
   
}
