/*
 * FuncTextPoint.java
 *
 * Created on 22 aprile 2002, 8.34
 */

package it.unibo.cs.org.apache.xpath.functions;

import it.unibo.cs.xpointer.custom.TextPoint;
import it.unibo.cs.org.apache.xpath.LocationSet;
import it.unibo.cs.org.apache.xpath.objects.*;
import it.unibo.cs.org.apache.xpath.XPathContext;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.*;
import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.LocationIterator;


/**
 * Calling textpoint(//foo) is equivalent to string-range(//foo,''),
 * so points around characters in the string-value of a location are returned.
 */
public class FuncTextPoint extends FunctionOneArg {

    /** Creates new FuncTextPoint */
    public FuncTextPoint() {
    }

    /**
     * Execute an XPath function object.  The function must return
     * a valid object.
     * @param xctxt The execution current context.
     * @return A valid XObject.
     *
     * @throws javax.xml.transform.TransformerException
     */
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {
       
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        
        XObject xobj = getArg0().execute(xctxt);
        
        Range [] tempArray; 
        TextPoint textPoint;
        
        switch(xobj.getType())
        {
            case XObject.CLASS_NODESET:
                XNodeSet xNodeSet = (XNodeSet) xobj;
                NodeIterator nodeIterator = xNodeSet.nodeset();
                Node current;
                Location loc;
                while((current=nodeIterator.nextNode())!=null)
                {
                    Location tempLoc = new Location();
                    tempLoc.setType(Location.NODE);
                    tempLoc.setLocation(current);
                    textPoint = new TextPoint(tempLoc);
                    tempArray = textPoint.getTextPoint();
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
                    textPoint = new TextPoint(current2);
                    tempArray = textPoint.getTextPoint();
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
    
}
