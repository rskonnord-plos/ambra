package it.unibo.cs.org.apache.xalan.templates;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.ranges.*;
import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.LocationIterator;
import it.unibo.cs.org.apache.xpath.LocationSet;
import it.unibo.cs.org.apache.xpath.objects.*;
import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xalan.transformer.TransformerImpl;
import javax.xml.transform.TransformerException;
import java.util.Vector;
import it.unibo.cs.org.apache.xml.utils.QName;
import it.unibo.cs.org.apache.xalan.transformer.ResultTreeHandler;
import it.unibo.cs.org.apache.xalan.transformer.StackGuard;
import it.unibo.cs.org.apache.xpath.XPath;
import javax.xml.transform.SourceLocator;
import org.xml.sax.SAXException;

/**
 *
 * @author  tax
 * @version 
 */
public class ElemApplyXPTRTemplates extends ElemApplyTemplates {

                
  public void execute(
          TransformerImpl transformer, Location sourceLocation, QName mode)
            throws TransformerException
  {
      transformer.pushCurrentTemplateRuleIsNull(false);

      try
      {
          if(null!=sourceLocation)
          {
              if (!m_isDefaultTemplate)
              {
                mode = getMode();
              }
              transformSelectedLocations(transformer,sourceLocation,null,mode);
          }
          else
          {
              transformer.getMsgMgr().error(this,
                "source location is null");  
          }
      }
      finally
      {
          transformer.popCurrentTemplateRuleIsNull();
      }
  }
  
  public void transformSelectedNodes(
          TransformerImpl transformer, Node sourceNode, ElemTemplateElement template, QName mode)
            throws TransformerException
  {
      try
      {
        XPathContext xctxt = transformer.getXPathContext();
        XLocationSet locset = getSelectOrDefaultLocation(xctxt,sourceNode);
      
        int savedSearchStart = pushParams(transformer, xctxt, sourceNode, mode);
        SourceLocator savedLocator = xctxt.getSAXLocator();
      
        //TODO: fare il push della context location list quando sarà implementata
      
        ResultTreeHandler rth = transformer.getResultTreeHandler();
        StylesheetRoot sroot = getStylesheetRoot();
        TemplateList tl = sroot.getTemplateListComposed();

        StackGuard guard = transformer.getStackGuard();
        boolean check = (guard.m_recursionLimit > -1);
        boolean quiet = transformer.getQuietConflictWarnings();
        boolean needToFindTemplate = (null == template);
      
        try
        {
          Location child;
          Location currentRange = null;
          LocationIterator ll = locset.locationSet();
          Range [] currentGroup = null;
          
          while((child=ll.nextLocation())!=null)
          {
              if(needToFindTemplate)
              {
                  TemplateAssociation ta = tl.getTemplate(xctxt,child,mode,-1,quiet);
                  if(ta==null)
                      template = null;
                  else
                  {
                    template = ta.template;
                    currentRange = ta.matchedLocation;
                    currentGroup = ta.group;
                  }
                  
                  if(template==null)
                  {
                      if(tl.isInsideTemplate(xctxt,child,mode))
                          continue;
                      
                      Range point = (Range)child.getLocation();
                      Node container = point.getStartContainer();
                      
                      switch(container.getNodeType())
                      {
                          case Node.TEXT_NODE:
                          case Node.ATTRIBUTE_NODE:
                              String data = container.getNodeValue();
                              int index = point.getStartOffset();
                              if(index<data.length())
                                  rth.characters(data.toCharArray(),index,1);
                          default:
                              continue;
                      }
                  }
              }
              
              ElemTemplateElement t = template.m_firstChild;
              
              try
              {
                  xctxt.pushCurrentLocation(currentRange);
                  xctxt.pushGroup(currentGroup);
                  
                  for(;t!=null;t = t.m_nextSibling)
                  {
                      xctxt.setSAXLocator(t);
                      transformer.setCurrentElement(t);
                      t.execute(transformer,child,mode);
                  }
                  reMarkParams(xctxt);
              }
              finally
              {
                  xctxt.popCurrentLocation();
                  xctxt.popGroup();
                  
                  if (check)
                    guard.pop();
              }
          }
        }
        finally
        {
            //TODO pop della context location list
            popParams(xctxt, savedSearchStart);
            xctxt.setSAXLocator(savedLocator);
        }
      }
      catch(SAXException se)
      {
        transformer.getErrorListener().fatalError(new TransformerException(se));
      }
  }       
            
  private XLocationSet getSelectOrDefaultLocation(XPathContext xctxt,Node sourceNode) throws javax.xml.transform.TransformerException
  {
     LocationSet locset = new LocationSet(); 
     XLocationSet retval;
     
     if(getSelect()==null)
     {
         calcLocations(sourceNode,locset);
         retval = new XLocationSet(locset);
     }
     else
     {
         XPath selectPattern = getSelect();
         XObject obj = selectPattern.execute(xctxt,sourceNode,this);
         
         if(obj instanceof XNodeSet)
         {
             NodeIterator nl = obj.nodeset();
             Node node;
             while((node=nl.nextNode())!=null)
             {
                 calcLocations(node,locset);
             }
             retval = new XLocationSet(locset);
         }
         else
             retval = (XLocationSet) obj;
     }
     
     return retval;
  }
  
  public void transformSelectedLocations(
          TransformerImpl transformer, Location loc, ElemTemplateElement template, QName mode)
            throws TransformerException
  {
      try
      {
        XPathContext xctxt = transformer.getXPathContext();
        XLocationSet locset = getSelectOrDefaultLocation(xctxt,loc);
        
        if(locset==null)
            return;
        
        SourceLocator savedLocator = xctxt.getSAXLocator();
      
        //TODO: fare il push della context location list quando sarà implementata
      
        ResultTreeHandler rth = transformer.getResultTreeHandler();
        StylesheetRoot sroot = getStylesheetRoot();
        TemplateList tl = sroot.getTemplateListComposed();

        StackGuard guard = transformer.getStackGuard();
        boolean check = (guard.m_recursionLimit > -1);
        boolean quiet = transformer.getQuietConflictWarnings();
        boolean needToFindTemplate = (null == template);
      
        try
        {
          Location child;
          LocationIterator ll = locset.locationSet();
          Location currentRange = null;
          Range []currentGroup = null;
          
          while((child=ll.nextLocation())!=null)
          {
              if(needToFindTemplate)
              {
                  TemplateAssociation ta = tl.getTemplate(xctxt,child,mode,-1,quiet);
                  if(ta==null)
                  {
                      template=null;
                  }
                  else
                  {
                    template = ta.template;
                    currentRange = ta.matchedLocation;
                    currentGroup = ta.group;
                  }
                  
                  if(template==null)
                  {
                      if(tl.isInsideTemplate(xctxt,child,mode))
                          continue;
                      
                      Range point = (Range)child.getLocation();
                      Node container = point.getStartContainer();
                      
                      switch(container.getNodeType())
                      {
                          case Node.TEXT_NODE:
                          case Node.ATTRIBUTE_NODE:
                              String data = container.getNodeValue();
                              int index = point.getStartOffset();
                              if(index<data.length()-1)
                                  rth.characters(data.toCharArray(),index,1);
                          default:
                              continue;
                      }
                  }
              }
              
              ElemTemplateElement t = template.m_firstChild;
              
              try
              {
                  xctxt.pushCurrentLocation(currentRange);
                  xctxt.pushGroup(currentGroup);
                  
                  for(;t!=null;t = t.m_nextSibling)
                  {
                      xctxt.setSAXLocator(t);
                      transformer.setCurrentElement(t);
                      t.execute(transformer,child,mode);
                  }
              }
              finally
              {
                  xctxt.popCurrentLocation();
                  xctxt.popGroup();
                  
                  if (check)
                    guard.pop();
              }
          }
        }
        finally
        {
            //TODO pop della context location list
            xctxt.setSAXLocator(savedLocator);
        }
      }
      catch(SAXException se)
      {
          transformer.getErrorListener().fatalError(new TransformerException(se));
      }
      
  }
  
  private XLocationSet getSelectOrDefaultLocation(XPathContext xctxt,Location sourceLocation) throws javax.xml.transform.TransformerException
  {
  
      if(getSelect()==null)
      {
          //che senso ha andare ad applicare un template a me stesso? andrei in loop
          //per esempio:
          //
          //<xsl:XPTRtemplate match="regexp(/,'.')">
          //    <xsl:apply-XPTRtemplates/>
          //</xsl:XPTRtemplate/>
          // mi ci vuole per forza l'attributo select
          throw new RuntimeException("Not implemented branch");
      }
      else
      {
          LocationSet locset = new LocationSet();
          try
          {
            XPath selectPattern = getSelect();
            xctxt.pushCurrentLocation(sourceLocation);
            XObject obj = selectPattern.execute(xctxt,null,this);
            
            if(obj instanceof XNodeSet)
            {
                NodeIterator nl = obj.nodeset();
                Node node;
                while((node=nl.nextNode())!=null)
                    calcLocations(node,locset);
                
                return new XLocationSet(locset);
            }
            else
                return (XLocationSet)obj;
            
          }
          finally
          {
            xctxt.popCurrentLocation();  
          }
      }
  }
  
   
  private void calcLocations(Node node,LocationSet locset)
  {
      Location loc;
      int len;
      DocumentRange docRange;
      
      if(node.getNodeType()==Node.DOCUMENT_NODE)
        docRange = (DocumentRange) node;
      else
        docRange = (DocumentRange) node.getOwnerDocument();
      
      switch(node.getNodeType())
      {
        case Node.ATTRIBUTE_NODE:
            node = node.getFirstChild();
        case Node.TEXT_NODE:
            String value = node.getNodeValue();
            len = value.length();                      
            break;
        default:
            len = node.getChildNodes().getLength();
       }
       
       if(len>0)
       for(int i=0;i<=len;i++)
       {
        Range point = docRange.createRange();
        point.setStart(node,i);
        point.setEnd(node,i);
        loc = new Location();
        loc.setType(Location.RANGE);
        loc.setLocation(point);
        locset.addLocation(loc);
       }
              
  }
}
