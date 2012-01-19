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

import gnu.regexp.UncheckedRE;

/**
 * Given an expression, the lexer builds 
 * an array of tokens.
 * The array is made up of couples: the first element identifies the scheme type,
 * the second element contains the schemedata.
 */
public class Lexer {

    private String globalExpr;
    private Object [] token_queue;
    private final int TOKEN_QUEUE_LEN = 255;
    private int nextTokenPos = 0;
    
    /*true if a shorthand can be found*/
    private boolean possibleShortHand = true; 
    
    /** 
     * Creates new Lexer 
     * @param  the string to be analayzed
     */
    public Lexer(String expr) {
        globalExpr = expr;
        token_queue = new Object[TOKEN_QUEUE_LEN];
    }

    /**
     * Tokenize an expression.
     */
    public void analyzeExpression()
    {
        int index = 0;
        
        /*remove starting white spaces*/
        while(globalExpr.charAt(index)==' ')
            index++;
        
        while(index<globalExpr.length())
        {
            index = analyzeSinglePointer(index);
            
            if(index<globalExpr.length() &&  globalExpr.charAt(index)==' ')
                index++;
        }
    }
    
    /**
     * @param offset the starting index of a pointer
     * @return the position after the analyzed pointer 
     */
    private int analyzeSinglePointer(int offset)
    {
        int index = offset;
        String currentExpr = "";
        int startSchemedata,endSchemedata;
        int numpar = 1;
        
        while(globalExpr.charAt(index)!='(')
        {
            currentExpr = currentExpr + globalExpr.charAt(index);
            index++;
            
            if(index >= globalExpr.length())
                if(possibleShortHand)
                {
                    handleShortHand();
                    return globalExpr.length();
                }
                else
                    throw new RuntimeException("Syntax Error");
        }
        
        int i;
        
        for(i=0;i<SchemeParser.schemeNames.length;i++)
        {
            if(currentExpr.equals(SchemeParser.schemeNames[i]))
            {
                token_queue[nextTokenPos] = new Integer(i);
                
                possibleShortHand = false;
                
                nextTokenPos++;
                
                break;
            }
        }
        
        if(i==SchemeParser.schemeNames.length)
        {
            if(possibleShortHand==false)
                throw new RuntimeException("Syntax Error");
            
            handleShortHand();
            return globalExpr.length();
        }
        
        startSchemedata = index + 1;
        endSchemedata = startSchemedata;
        boolean escaping = false;
        
        
        while(numpar!=0 && endSchemedata<globalExpr.length())
        {
            if(globalExpr.charAt(endSchemedata)=='^')
            {
                if(escaping==false)
                    escaping = true;
                else  //we have ^^
                    escaping = false;
                          
            }
            else if(globalExpr.charAt(endSchemedata)=='(')
            {
                if(escaping==false)
                    numpar++;
                else
                    escaping=false;
            }
            else
            {
                if(globalExpr.charAt(endSchemedata)==')')
                {
                    if(escaping==false)
                        numpar--;
                    else
                        escaping = false;
                }
                else
                {
                    if(escaping)
                        throw new RuntimeException("Unescaped circumflex");
                }
            }
            
            endSchemedata++;
        }
        
        if(numpar!=0)
            throw new RuntimeException("Syntax Error: parenthesis unbalanced");
        
        token_queue[nextTokenPos] = new String(escapeCircumflex(globalExpr.substring(startSchemedata,endSchemedata-1)));
        nextTokenPos++;
        
        return endSchemedata;
    }
    
    Object [] getTokenQueue()
    {
        return token_queue;
    }
    
    /**
     * Makes escaping of unbalanced parenthesis and of double occurences of circumflex.
     * @param param the string where the substitution takes place
     *
     * @return a string where each occurrence of a circumflex used for escaping is erased
     */
    private String escapeCircumflex(String param)
    {
        UncheckedRE regexp = new UncheckedRE("\\^\\^");
        
        String retval = regexp.substituteAll(param,"^");
        
        regexp = new UncheckedRE("\\^\\(");
        retval = regexp.substituteAll(retval,"(");
        
        regexp = new UncheckedRE("\\^\\)");
        retval = regexp.substituteAll(retval,")");
        
        return retval;
    }
    
    private void handleShortHand()
    {
        token_queue[nextTokenPos++] = new Integer(it.unibo.cs.xpointer.datatype.Scheme.SHORTHAND);
        token_queue[nextTokenPos++] = globalExpr;
    }
}
