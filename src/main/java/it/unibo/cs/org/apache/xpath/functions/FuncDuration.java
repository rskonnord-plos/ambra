/*
 * FuncDuration.java
 *
 * Created on 4 marzo 2002, 14.18
 */

package it.unibo.cs.org.apache.xpath.functions;

import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xpath.objects.*;

/**
 *
 * @author  root
 * @version 
 */
public class FuncDuration extends FunctionMultiArgs {

    /** Creates new FuncDuration */
    public FuncDuration() {
    }

   
    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {
        
        XDuration duration = new XDuration();
        
        switch(calcArgNum())
        {
            case 6:
                duration.setMillisecond((int)m_args[2].execute(xctxt).num());
            case 5:
                duration.setSecond((int)m_args[1].execute(xctxt).num());
            case 4:
                duration.setHour((int)m_args[0].execute(xctxt).num());
            case 3:
                duration.setDay((int)getArg2().execute(xctxt).num());
            case 2:
                duration.setMonth((int)getArg1().execute(xctxt).num());
            case 1:
                duration.setYear((int)getArg0().execute(xctxt).num());
                break;
                
            default:
                throw new javax.xml.transform.TransformerException("Wrong Argument Number");
        }
        
        return duration;
    }
 
    private int calcArgNum()
    {
        int num = 0;
        
        if(getArg0()!=null)
            num++;
        if(getArg1()!=null)
            num++;
        if(getArg2()!=null)
            num++;
        
        if(m_args!=null)
            num += m_args.length;
        
        return num;
    }
}
