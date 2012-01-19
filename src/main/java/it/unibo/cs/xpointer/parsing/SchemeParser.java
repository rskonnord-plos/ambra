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

import it.unibo.cs.xpointer.datatype.*;
import java.util.Vector;

/**
 * This class provides some methods for extracting the
 * schemes used in an XPointer expression.
 *  
 */
public class SchemeParser {

    private SchemeListImpl xmlnsList,schemeList;
    
    
    /** integers identifyng schemes correspond to positions in this array*/
    public static final String [] schemeNames = {"element","xmlns","xpointer"};
    
    
    /** Creates new SchemeParser 
     * @param str the XPointer expression
     */
    public SchemeParser(String str) {
        
        
        xmlnsList = new SchemeListImpl();
        schemeList = new SchemeListImpl();
       
        Lexer lexer = new Lexer(str);
        lexer.analyzeExpression();
        
        buildSchemes(lexer.getTokenQueue());
    }

    /**
     * Returns a list of ALL the schemes of any kind.
     * The ordering of the schemes in the list reflects the order of the schemes
     * in the full xpointer expression.
     */
    public SchemeList getSchemeList()
    {
        return schemeList;
    }
   
    /**
     * Returns xmlns schemes referring a given xpointer scheme
     */
    public SchemeList getXmlNSSchemeList(XPointerScheme xpointerScheme)
    {
        return xpointerScheme.getXmlNSSchemes();
    }
    
    /**
     * Builds the different lists of pointers and makes the association between
     * namespaces and xpointer.
     * @param token_queue 
     */
    private void buildSchemes(Object []token_queue)
    {
        
        for(int i=0;token_queue[i]!=null;i+=2)
        {
            int type = ((Integer)token_queue[i]).intValue();
            
            switch(type)
            {
                case Scheme.ELEMENT_SCHEME:
                {
                    schemeList.addScheme(new ElementScheme((String)token_queue[i+1]));
                    break;
                }
                case Scheme.XPOINTER_SCHEME:
                {
                    XPointerScheme xpointerScheme = new XPointerScheme((String)token_queue[i+1]);
                    for(int j=0;j<xmlnsList.getLength();j++)
                    {
                        XmlNSScheme tempXmlNSScheme = (XmlNSScheme)xmlnsList.item(j);
                        xpointerScheme.addXmlNSScheme(tempXmlNSScheme);
                    }    
                    
                    schemeList.addScheme(xpointerScheme);
                    break;
                }
                case Scheme.XMLNS_SCHEME:
                {
                    XmlNSScheme xmlNSScheme = new XmlNSScheme((String)token_queue[i+1]);
                    /*check to see if this prefix is already used*/
                    for(int j=0;j<xmlnsList.getLength();j++)
                    {
                        XmlNSScheme comparingScheme = (XmlNSScheme)xmlnsList.item(j);
                        if(xmlNSScheme.getPrefix().equals(comparingScheme.getPrefix()))
                        {
                            xmlnsList.removeScheme(comparingScheme);
                        }
                    }
                    xmlnsList.addScheme(xmlNSScheme);
                    schemeList.addScheme(xmlNSScheme);
                    break;
                }
                case Scheme.SHORTHAND:
                {
                    schemeList.addScheme(new ShortHand((String)token_queue[i+1]));
                    break;
                }
            }
        }
    }
    
}
