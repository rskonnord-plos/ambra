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

import org.w3c.dom.Node;

/**
 * This class is used when  we need something that is a range with the same start and end container.
 */
public class TextNodeFragment {

    private int startIndex;
    
    private int endIndex;
    
    private Node node;
    
    /** Creates new TextNodeFragment */
    public TextNodeFragment() {
    }

    /** Getter for property endIndex.
     * @return Value of property endIndex.
     */
    public int getEndIndex() {
        return endIndex;
    }
    
    /** Setter for property endIndex.
     * @param endIndex New value of property endIndex.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
    
    /** Getter for property node.
     * @return Value of property node.
     */
    public org.w3c.dom.Node getNode() {
        return node;
    }
    
    /** Setter for property node.
     * @param node New value of property node.
     */
    public void setNode(org.w3c.dom.Node node) {
        this.node = node;
    }
    
    /** Getter for property startIndex.
     * @return Value of property startIndex.
     */
    public int getStartIndex() {
        return startIndex;
    }
    
    /** Setter for property startIndex.
     * @param startIndex New value of property startIndex.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
}
