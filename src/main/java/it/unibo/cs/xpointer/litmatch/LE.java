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

import java.util.Vector;

/**
 * This class represents a Literal Expression.
 * It is a much simpler implementation of Regular Expressions, in fact
 * strings are matched literally without special characters.
 * It is useful for string-range() function because it matches string literally.
 */
public class LE {

    private String match;
    
    /** Creates new LE 
     * @param match the string to be matched
     */
    public LE(String match) {
        this.match = match;
    }

    /**
     * Searches in the input string all the occurences which
     * match literally this expression.
     * 
     * @return an array of matches
     * @param input the string where matches are looked for
     */
    public LEMatch [] getAllMatches(String input) 
    {
        int index = 0;
        int searchIndex = 0;
        Vector matches = new Vector();
        
        while( (index=indexOf(input, searchIndex))!=-1)
        {
            LEMatch leMatch = new LEMatch(index,index+match.length());
            matches.addElement(leMatch);
            searchIndex = index + match.length() + 1;
        }
        
        LEMatch [] retval = new LEMatch[matches.size()];
        
        for(int i=0;i<retval.length;i++)
            retval[i] = (LEMatch)matches.elementAt(i);
        
        return retval;
    }
    
    private int indexOf(String input, int searchIndex) {
      
      if (match.equals("")) 
        return (searchIndex < input.length()) ? searchIndex : -1;
      
      return input.indexOf(match, searchIndex);
    }
  
}
