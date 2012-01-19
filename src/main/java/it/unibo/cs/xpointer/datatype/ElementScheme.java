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

package it.unibo.cs.xpointer.datatype;

import it.unibo.cs.xpointer.parsing.ElementSchemeParser;

/**
 *
 * @author  root
 * @version 
 */
public class ElementScheme extends Scheme {

    /** Creates new ElementScheme */
    public ElementScheme(String schemedata) {
        super(schemedata,Scheme.ELEMENT_SCHEME);
    }

    /**
     * Converts an XPointer expression (conforming to the element() scheme) to 
     * the equivalent XPath expression.
     * @return an XPath expression
     */
    public String getXPathExpression() 
    {    
        ElementSchemeParser esp = new ElementSchemeParser(getValue());
        
        Object [] token_queue = esp.getTokenQueue();
        
        String retval="";
        int i=0;
        
        if(token_queue[0] instanceof String)
        {
            retval = "id('"+token_queue[0]+"')";
            i = 1;
        }
        
        for(;i<token_queue.length;i++)
        {
            retval = retval + "/*[" + token_queue[i]+"]";
        }
        
        return retval;
    }
    
    
    
}
