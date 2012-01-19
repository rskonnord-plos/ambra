/*
 * FuncSentence.java
 *
 * Created on 1 marzo 2002, 14.54
 */

package it.unibo.cs.org.apache.xpath.functions;

import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xpath.objects.*;
import java.util.Vector;
import it.unibo.cs.xpointer.custom.Sentence;
import org.w3c.dom.*;
import org.w3c.dom.ranges.*;
import it.unibo.cs.xpointer.Location;
import it.unibo.cs.org.apache.xpath.LocationSet;
import org.w3c.dom.traversal.NodeIterator;
import it.unibo.cs.xpointer.LocationIterator;

/**
 *
 * @author  root
 * @version 
 */
public class FuncSentence extends FunctionMultiArgs{

   
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        XLocationSet retval = new XLocationSet();
        LocationSet locset = retval.mutableLocationSet();
        XObject xobj = getArg0().execute(xctxt);
        
        boolean retBoundary = true;
        XString [] boundaries;
        int bsize;
        
        Vector v = analyzeArguments(xctxt);
        if(v.isEmpty()==false && v.lastElement() instanceof XBoolean)
        {
            retBoundary = ((XBoolean)v.lastElement()).bool();
            bsize = v.size()-1; 
        }
        else
        {
            bsize = v.size();
        }
        
        boundaries = new XString[bsize];
        for(int i=0;i<bsize;i++)
            boundaries[i] = (XString)v.elementAt(i);
         
        Sentence sentence;
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
                    sentence = new Sentence(current);
                    for(int i=0;i<boundaries.length;i++)
                        sentence.setBoundary(boundaries[i].str());
                    sentence.setBoundaryOutput(retBoundary);
                    tempArray = sentence.getSentences();
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
                    sentence = new Sentence(current2);
                    for(int i=0;i<boundaries.length;i++)
                        sentence.setBoundary(boundaries[i].str());
                    sentence.setBoundaryOutput(retBoundary);
                    tempArray = sentence.getSentences();
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
    
    private Vector analyzeArguments(XPathContext xctxt) throws javax.xml.transform.TransformerException
    {
        Vector xobjects = new Vector();
        
        if(getArg1()!=null)
        {
            xobjects.addElement(getArg1().execute(xctxt)); 
        }
        if(getArg2()!=null)
        {
            xobjects.addElement(getArg2().execute(xctxt));
        }
        
        if(m_args!=null)
            for(int i=0;i<m_args.length;i++)
                xobjects.addElement(m_args[i].execute(xctxt));
        
        return xobjects;
    }
}
