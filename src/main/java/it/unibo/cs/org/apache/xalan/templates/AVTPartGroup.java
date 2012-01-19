/*
 * AVTPartGroup.java
 *
 * Created on 9 aprile 2002, 10.24
 */

package it.unibo.cs.org.apache.xalan.templates;

import org.w3c.dom.ranges.Range;
import org.w3c.dom.Node;
import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xml.utils.FastStringBuffer;

/**
 *
 * @author  tax
 * @version 
 */
public class AVTPartGroup extends AVTPart {

    private int m_groupNum;
    
    /** Creates new AVTPartGroup */
    public AVTPartGroup(int groupNum) {
        m_groupNum = groupNum;
    }

    /**
     * Get the AVT part as the original string.
     *
     * @return the AVT part as the original string.
     */
    public String getSimpleString() {
        return "{$"+m_groupNum+"}";
    }
    
    /**
     * Write the evaluated value into the given
     * string buffer.
     *
     * @param xctxt The XPath context to use to evaluate this AVT.
     * @param buf Buffer to write into.
     * @param context The current source tree context.
     * @param nsNode The current namespace context (stylesheet tree context).
     * @param NodeList The current Context Node List.
     *
     * @throws javax.xml.transform.TransformerException
     */
    public void evaluate(XPathContext xctxt, FastStringBuffer buf, Node context, it.unibo.cs.org.apache.xml.utils.PrefixResolver nsNode) throws javax.xml.transform.TransformerException 
    {
        Range [] group = xctxt.getGroup();
        
        if(group!=null && m_groupNum<group.length)
        {
            Range range = group[m_groupNum];
            
            buf.append(range.toString());
        }
    }
    
}
