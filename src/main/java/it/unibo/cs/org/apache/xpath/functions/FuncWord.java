/*
 * FuncWord.java
 *
 * Created on 1 marzo 2002, 14.53
 */

package it.unibo.cs.org.apache.xpath.functions;

import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import it.unibo.cs.xpointer.custom.Word;
import it.unibo.cs.org.apache.xpath.*;
import it.unibo.cs.org.apache.xpath.objects.*;
import org.w3c.dom.traversal.NodeIterator;
import it.unibo.cs.xpointer.*;
import it.unibo.cs.org.apache.xpath.LocationSet;

/**
 *
 * @author  root
 * @version 
 */
public class FuncWord extends FunctionOneArg {

    /** Creates new FuncWord */
    public FuncWord() {
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XObject xobj = getArg0().execute(xctxt);
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        Word word;
        
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
                    word = new Word(current);
                    tempArray = word.getWords();
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
                    word = new Word(current2);
                    tempArray = word.getWords();
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
