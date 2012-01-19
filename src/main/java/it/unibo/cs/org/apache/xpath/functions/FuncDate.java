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

package it.unibo.cs.org.apache.xpath.functions;

import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xpath.objects.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncDate extends FunctionMultiArgs {

    private XPathContext xctxt = null;
    
    /** Creates new FuncDate */
    public FuncDate() {
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XDate retval = null;
        this.xctxt = xctxt;
        
        switch(calcArgNum())
        {
            case 1:
                try
                {
                    retval = new XDate(getArg0().execute(xctxt).str());
                }
                catch(java.text.ParseException pe)
                {
                    throw new javax.xml.transform.TransformerException(pe.getMessage());
                }
                break;
                
            case 3:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num());
                break;
                
            case 6:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),m_args[2].execute(xctxt).str());
                break;                    
                                    
            case 7:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),(int)m_args[2].execute(xctxt).num(),m_args[3].execute(xctxt).str());
                break;
            
            case 8:
                retval = new XDate((int)getArg0().execute(xctxt).num(),(int)getArg1().execute(xctxt).num()-1,(int)getArg2().execute(xctxt).num(),
                                    (int)m_args[0].execute(xctxt).num(),(int)m_args[1].execute(xctxt).num(),(int)m_args[2].execute(xctxt).num(),(int)m_args[3].execute(xctxt).num(),m_args[4].execute(xctxt).str());
                break;
                
        }
        
        return retval;
    }
    
    private int calcArgNum()
    {
        int numArgs = 0;
        
        if(getArg0()!=null)
            numArgs++;
        if(getArg1()!=null)
            numArgs++;
        if(getArg2()!=null)
            numArgs++;
        
        if(m_args!=null)
        {
            numArgs += m_args.length;
        }
        
        return numArgs;
    }
    
    
}
