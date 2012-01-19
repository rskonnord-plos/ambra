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

package it.unibo.cs.xpointer.parsing;

import java.util.*;

/**
 * This class parses the schemedata of the element() scheme.
 * The expression must conform to the following syntax:
 *
 * elementschemedata ::= (Name, childseq) | childseq
 * childseq ::= ('/' [1-9] [0-9]* )+
 *
 * A Token queue is created; the first token can be a String object (corresponding 
 * to the Name token) and the following tokens are Integer objects.
 */
public class ElementSchemeParser {

    private String globalExpr;
    private Object [] token_queue;
    
    /** Creates new ElementSchemeParser 
     * @param schemedata the schemedata of element scheme 
     */
    public ElementSchemeParser(String schemedata) {
        this.globalExpr = schemedata;
        
        if(schemedata.equals(""))
            throw new RuntimeException("Syntax Error");
        
        tokenize();
    }

    /**
     * Builds the token queue.
     */
    private void tokenize()
    {
        StringTokenizer st = new StringTokenizer(globalExpr,"/",true);
        String Name = null;
        /*true if the previous token is '/' */
        boolean previousSeparator = false;
        /* true only at the first iteration of the while loop*/
        boolean expectName = true;
        String token = null;
        Vector numbers = new Vector();
        
        while(st.hasMoreTokens())
        {
            token = st.nextToken();
            
            if(expectName && token.equals("/")==false)
            {
                Name = token;
            }   
            else
                /*this avoids two '/' in a row*/
                if(token.equals("/"))
                    if(previousSeparator==false)
                    {
                        previousSeparator = true;
                        expectName = false;
                        continue;
                    }
                    else
                        throw new RuntimeException("Syntax Error");
                else
                    if(isNum(token))
                    {
                        numbers.addElement(new Integer(token));
                        previousSeparator = false;
                    }
                    else
                        throw new RuntimeException("Syntax Error");
            
            expectName = false;
        }
        
        if(token.equals("/"))
            throw new RuntimeException("Syntax Error");
        
        if(Name!=null)
        {
            token_queue = new Object[1+numbers.size()];
            token_queue[0] = Name;
            
            for(int i=1;i<token_queue.length;i++)
                token_queue[i] = numbers.elementAt(i-1);

        }
        else
        {
            token_queue = new Object[numbers.size()];
            for(int i=0;i<token_queue.length;i++)
                token_queue[i] = numbers.elementAt(i);

        }
        
            }
    
            
    /**
     * Verifies if a string is numerical and the first digit is not zero.  
     *
     * @return true if the string passed as an argument has the form [1-9][0-9]*,false otherwise
     * @param num the string to be analysed
     */        
    private boolean isNum(String num)
    {
        if(num.charAt(0)=='0')
            return false;
        
        for(int i=0;i<num.length();i++)
            if(num.charAt(i)<'0' || num.charAt(i)>'9')
                return false;
        
        return true;
    }
    
    /**
     * Returns the token queue created after the parsing of the schemedata of the element() scheme.
     *
     * @return the token queue
     */
    public Object[] getTokenQueue()
    {
        return token_queue;
    }
    
    
}
