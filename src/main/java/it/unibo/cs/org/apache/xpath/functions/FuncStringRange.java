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
import it.unibo.cs.org.apache.xpath.objects.XObject;
import it.unibo.cs.xpointer.*;
import it.unibo.cs.org.apache.xpath.objects.XLocationSet;
import it.unibo.cs.org.apache.xpath.LocationSet;
import it.unibo.cs.org.apache.xpath.objects.XNodeSet;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncStringRange extends FunctionMultiArgs {

    
    private int numArgs = 2;
    private int offset,len;
    private String matchingString;
    
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        XObject xobj = getArg0().execute(xctxt);
        matchingString = getArg1().execute(xctxt).str();
        
        if(getArg2()!=null)
        {
            offset = (int) getArg2().execute(xctxt).num();
            numArgs++;
            if(m_args!=null)
            {
                len = (int) m_args[0].execute(xctxt).num();
                numArgs++;
            }
        }
        
        Range [] tempArray; 
        
        switch(xobj.getType())
        {
            case XObject.CLASS_NODESET:
                XNodeSet xNodeSet = (XNodeSet) xobj;
                NodeIterator nodeIterator = xNodeSet.nodeset();
                Node current;
                Location loc;
                while((current=nodeIterator.nextNode())!=null)
                {
                    tempArray = process(current);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc = new Location();
                        loc.setLocation(tempArray[i]);
                        loc.setType(Location.RANGE);
                        locset.addLocation(loc);
                    }
                }
                break;
         
            case XObject.CLASS_LOCATIONSET:
                XLocationSet xLocationSet = (XLocationSet) xobj;
                LocationIterator locIterator = xLocationSet.locationSet();
                Location current2,loc2;
                while((current2=locIterator.nextLocation())!=null)
                {
                    tempArray = process(current2);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc2 = new Location();
                        loc2.setLocation(tempArray[i]);
                        loc2.setType(Location.RANGE);
                        locset.addLocation(loc2);
                    }
                }
        }
        
        return retval;
    }
    
    
    private Range[] process(Object obj) throws javax.xml.transform.TransformerException
    {
        Location loc;
        Range []result = null;
        
        if(obj instanceof Node)
        {
            loc = new Location();
            loc.setType(Location.NODE);
            loc.setLocation(obj);
        }
        else
            loc = (Location) obj;
        
        StringRange stringRange = new StringRange(loc);    
        
        switch(numArgs)
        {
            case 2:
                result = stringRange.getStringRange(matchingString);
                break;
            case 3:
                result = stringRange.getStringRange(matchingString,offset);
                break;
            case 4:
                result = stringRange.getStringRange(matchingString,offset,len);
        }
        
        return result;
    }
}
