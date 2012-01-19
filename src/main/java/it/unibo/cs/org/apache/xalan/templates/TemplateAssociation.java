/*
 * TemplateAssociation.java
 *
 * Created on 29 marzo 2002, 11.26
 */

package it.unibo.cs.org.apache.xalan.templates;

import it.unibo.cs.xpointer.Location;
import org.w3c.dom.ranges.Range;

/**
 * This class contains a couple made of a template and the location (tipically a range)
 * effectively matched by that range.
 * 
 */
public class TemplateAssociation {

    public ElemTemplateElement template;
    
    public Location matchedLocation;
    
    public Range [] group;
    
    /** Creates new TemplateAssociation */
    public TemplateAssociation() {
    }

    public TemplateAssociation(ElemTemplateElement template,Location matchedLocation) {
        this.matchedLocation = matchedLocation;
        this.template = template;
    }
}
