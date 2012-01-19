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

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import it.unibo.cs.xpointer.custom.Regexp;
import it.unibo.cs.org.apache.xpath.*;
import it.unibo.cs.org.apache.xpath.objects.*;
import org.w3c.dom.traversal.NodeIterator;
import it.unibo.cs.xpointer.*;
import it.unibo.cs.org.apache.xpath.LocationSet;
import java.util.Hashtable;

/**
 *
 * @author  root
 * @version 
 */
public class FuncRegexp extends Function2Args {

        
    private Hashtable groups = null;
    
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        groups = new Hashtable();
        
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        XObject xobj = getArg0().execute(xctxt);
        String matchingString = getArg1().execute(xctxt).str();
        
        Regexp regexp;
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
                    regexp = new Regexp(current);
                    tempArray = regexp.getRegexp(matchingString);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc = new Location();
                        loc.setLocation(tempArray[i]);
                        loc.setType(Location.RANGE);
                        locset.addLocation(loc);
                        
                        groups.put(loc,regexp.getGroups(i));
                    }
                }
                break;
        
            case XObject.CLASS_LOCATIONSET:
                XLocationSet xLocationSet = (XLocationSet) xobj;
                LocationIterator locIterator = xLocationSet.locationSet();
                Location current2,loc2;
                while((current2=locIterator.nextLocation())!=null)
                {
                    regexp = new Regexp(current2);
                    tempArray = regexp.getRegexp(matchingString);
                    for(int i=0;i<tempArray.length;i++)
                    {
                        loc2 = new Location();
                        loc2.setLocation(tempArray[i]);
                        loc2.setType(Location.RANGE);
                        locset.addLocation(loc2);
                        
                        groups.put(loc2,regexp.getGroups(i));
                    }
                }
        }
        
        return retval;
    }
    
    public Hashtable getGroupMap()
    {
        return groups;
    }
}
