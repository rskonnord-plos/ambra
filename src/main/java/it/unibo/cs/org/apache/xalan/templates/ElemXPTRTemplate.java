/*
 * ElemXPTRTemplate.java
 *
 * Created on 26 marzo 2002, 16.00
 */

package it.unibo.cs.org.apache.xalan.templates;

import it.unibo.cs.org.apache.xalan.transformer.TransformerImpl;
import it.unibo.cs.org.apache.xml.utils.QName;
import javax.xml.transform.TransformerException;

/**
 *
 * @author  tax
 * @version 
 */
public class ElemXPTRTemplate extends ElemTemplate 
{
    public void execute(
          TransformerImpl transformer, it.unibo.cs.xpointer.Location sourceLocation, QName mode)
            throws TransformerException
    {
        if (null != sourceLocation)
        {
            transformer.executeChildTemplates(this, sourceLocation, mode, true);
        }
        else  
        {
            throw new RuntimeException("source location is null");
        }
    }
    
}
