/*
 * NodeListImpl.java
 *
 * Created on 12 marzo 2002, 17.14
 */

package it.unibo.cs.org.apache.xalan.stree;

import org.w3c.dom.*;
import java.util.Vector;

/**
 *
 * @author  root
 * @version 
 */
public class NodeListImpl implements NodeList {

    private Vector v;
    
    /** Creates new NodeListImpl */
    public NodeListImpl() {
        v = new Vector();
    }

    public int getLength() {
        return v.size();
    }
    
    public org.w3c.dom.Node item(int param) {
        return (Node) v.elementAt(param);
    }
 
    void addNode(Node node)
    {
        v.addElement(node);
    }
}
