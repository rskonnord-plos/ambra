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

import it.unibo.cs.org.apache.xpath.objects.*;
import org.w3c.dom.traversal.*;
import it.unibo.cs.org.apache.xpath.*;
import it.unibo.cs.xpointer.datatype.*;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import it.unibo.cs.xpointer.parsing.*;
import it.unibo.cs.xpointer.xmlns.*;
import javax.xml.transform.TransformerException;

public class XPointerAPI {

    /** Creates new XPointerAPI */
    public XPointerAPI() {
    }

    /**
     * Evaluates an XPointer expression.
     * If no locations are selected, an empty list is returned.
     *
     * @param contextLocation the location to start searching from
     * @param str the XPointer expression
     * @return a location list containing the selected locations 
     */
    public static LocationList selectLocationList(Node context,String str) throws javax.xml.transform.TransformerException
    {
        LocationListImpl locationListImpl = new LocationListImpl();
        
        
        XObject xobj = XPathAPI.eval(context,str);
        Location loc;
        
        if(xobj instanceof XLocationSet)
        {
            XLocationSet xls = (XLocationSet) xobj;
            it.unibo.cs.xpointer.LocationIterator li = xls.locationSet();
            
            while((loc = li.nextLocation())!=null)
            {
                locationListImpl.addLocation(loc); 
            }
        }
        if(xobj instanceof XNodeSet)
        {
            XNodeSet xns = (XNodeSet)xobj;
            NodeIterator ni = xns.nodeset();
            Node node;
            while((node = ni.nextNode())!=null)
            {
                loc = new Location();
                loc.setType(Location.NODE);
                loc.setLocation(node);
                locationListImpl.addLocation(loc);
            }
        }
        
        return locationListImpl;
    }
    
    /**
     * Evaluates an XPointer expression and returns the first selected location.
     * If the expression does not select any locations, returns null.
     *
     * @param contextLocation the location to start searching from
     * @param str the XPointer expression
     * @return the selected location, null if no locations are selected
     */
    public static Location selectSinlgeLocation(Node context,String str) throws javax.xml.transform.TransformerException
    {
        Location loc = null;
        
        LocationList locationList = selectLocationList(context,str);
        
        if(locationList.getLength()>0)
            loc = locationList.item(0);
        
        return loc;
    }
   
    /**
     * Evaluates a full pointer which consists of one or more pointer parts.
     * Multiple pointers parts are evaluated from left to right, if the scheme identifies no resource 
     * the next is evaluated. The result of the first pointer part whose evaluation succeedes is reported as the 
     * subresource identified by the pointer as a whole.
     *
     * @param context the context node for the pointer evaluation
     * @param fullptr a full pointer
     * @return the list of location selected by the first pointer which succeedes
     */ 
   public static LocationList evalFullptr(Node context,String fullptr) throws javax.xml.transform.TransformerException
   {
       return evalFullptr(context,fullptr,null,null);
   }
    
    /**
     * Evaluates a full pointer which consists of one or more pointer parts.
     * Multiple pointers parts are evaluated from left to right, if the scheme identifies no resource 
     * the next is evaluated. The result of the first pointer part whose evaluation succeedes is reported as the 
     * subresource identified by the pointer as a whole.
     * An application may provide values to be used for here() and origin functions.
     *
     * @param context the context node for the pointer evaluation
     * @param fullptr a full pointer
     * @param here the location returned by the here() function,may be null
     * @param origin the location returned by the origin() function,may be null
     * @return the list of location selected by the first pointer which succeedes
     */
    public static LocationList evalFullptr(Node context,String fullptr,Location here,Location origin) throws javax.xml.transform.TransformerException
    {
        
        LocationList retLocation = null;
        
        SchemeParser sp = new SchemeParser(fullptr);
        
        SchemeList schemeList = sp.getSchemeList();
        
       
        
        for(int i=0;i<schemeList.getLength();i++)
        {           
            Scheme ptrpart = schemeList.item(i);
            
            XObject xobj = null;
            
            switch(ptrpart.getType())
            {
                case Scheme.XPOINTER_SCHEME:
                {
                    /*xmlns scheme handling*/
                    PrefixResolverImpl prefixResolverImpl = new PrefixResolverImpl();
                    SchemeList namespaceList = sp.getXmlNSSchemeList((XPointerScheme)ptrpart);           
                    for(int j=0; j<namespaceList.getLength();j++)
                    {
                        XmlNSScheme xmlNSScheme = (XmlNSScheme) namespaceList.item(j);
                        prefixResolverImpl.setNamespace(xmlNSScheme.getPrefix(),xmlNSScheme.getNamespaceURI());
                    }
                    
                    String schemedata = ptrpart.getValue();
                    xobj = XPathAPI.eval(context,schemedata,prefixResolverImpl,here,origin);
                    break;
                }
                case Scheme.ELEMENT_SCHEME:
                {
                    ElementScheme es = (ElementScheme)ptrpart;
                    xobj = XPathAPI.eval(context,es.getXPathExpression(),here,origin);
                    break;
                }
                case Scheme.SHORTHAND:
                {
                    ShortHand sh = (ShortHand)ptrpart;
                    xobj = XPathAPI.eval(context,sh.getXPathExpression());
                    break;
                }
            }
            
            if(xobj!=null)
            {
                retLocation = buildLocationList(xobj);
                if(retLocation.getLength()>0)
                    break;
            }
        }
        
        //if(retLocation.getLength()==0)
        //    throw new TransformerException("Subresource Error: empty location-set");
        
        return retLocation;
    }
    
    private static LocationList buildLocationList(XObject xobj)
    {
        LocationListImpl locationListImpl = new LocationListImpl();
        Location loc;
        
        if(xobj instanceof XLocationSet)
        {
            XLocationSet xls = (XLocationSet) xobj;
            it.unibo.cs.xpointer.LocationIterator li = xls.locationSet();
            
            while((loc = li.nextLocation())!=null)
            {
                locationListImpl.addLocation(loc); 
            }
        }
        if(xobj instanceof XNodeSet)
        {
            XNodeSet xns = (XNodeSet)xobj;
            NodeIterator ni = xns.nodeset();
            Node node;
            while((node = ni.nextNode())!=null)
            {
                loc = new Location();
                loc.setType(Location.NODE);
                loc.setLocation(node);
                locationListImpl.addLocation(loc);
            }
        }
        
        return locationListImpl;
    }
}
