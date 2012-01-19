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

package it.unibo.cs.xpointer.litmatch;

/**
 * An instance of this class represents a match completed by a LE matching function.
 * It can be used to obtaion relevant information about the location of a match. 
 * 
 */
public class LEMatch {

    private int startIndex;
    private int endIndex;
    
    /** Creates new LEMatch */
    LEMatch() {
    }

    /**
     * Creates new LEMatch
     * @param startIndex the start index of the matched string in the original expression
     * @param endIndex the end index of the matched string in the original expression
     */
    LEMatch(int startIndex,int endIndex)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
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
    void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
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
    void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
}
