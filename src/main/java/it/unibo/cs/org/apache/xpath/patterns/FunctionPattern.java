/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package it.unibo.cs.org.apache.xpath.patterns;

import it.unibo.cs.org.apache.xpath.XPath;
import it.unibo.cs.org.apache.xpath.DOMHelper;
import it.unibo.cs.org.apache.xpath.Expression;
import it.unibo.cs.org.apache.xpath.XPathContext;
import it.unibo.cs.org.apache.xpath.objects.XNumber;
import it.unibo.cs.org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import it.unibo.cs.org.apache.xpath.objects.XLocationSet;
import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.LocationIterator;
import org.w3c.dom.ranges.Range;
import it.unibo.cs.org.apache.xalan.stree.Parent;
import it.unibo.cs.org.apache.xalan.templates.ElemTemplateElement;
import it.unibo.cs.org.apache.xpath.functions.FuncRegexp;

/**
 * <meta name="usage" content="advanced"/>
 * Match pattern step that contains a function.
 */
public class FunctionPattern extends StepPattern
{

   private Location m_lastMatchedLocation; 
    
  /**
   * Construct a FunctionPattern from a 
   * {@link it.unibo.cs.org.apache.xpath.functions.Function expression}.
   *
   *
   * @param a should be a {@link it.unibo.cs.org.apache.xpath.functions.Function expression}.
   */
  public FunctionPattern(Expression expr)
  {

    super(0, null, null);

    m_functionExpr = expr;
  }
  
  public Expression getFunctionExpr()
  {
      return m_functionExpr;
  }

  /**
   * Static calc of match score.
   */
  protected final void calcScore()
  {

    m_score = SCORE_OTHER;

    if (null == m_targetString)
      calcTargetString();
  }

  /** Should be a {@link it.unibo.cs.org.apache.xpath.functions.Function expression}.
   *  @serial   */
  Expression m_functionExpr;

  /**
   * Test a node to see if it matches the given node test.
   *
   * @param xctxt XPath runtime context.
   *
   * @return {@link it.unibo.cs.org.apache.xpath.patterns.NodeTest#SCORE_NODETEST}, 
   *         {@link it.unibo.cs.org.apache.xpath.patterns.NodeTest#SCORE_NONE}, 
   *         {@link it.unibo.cs.org.apache.xpath.patterns.NodeTest#SCORE_NSWILD}, 
   *         {@link it.unibo.cs.org.apache.xpath.patterns.NodeTest#SCORE_QNAME}, or
   *         {@link it.unibo.cs.org.apache.xpath.patterns.NodeTest#SCORE_OTHER}.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {

    Location contextLoc = xctxt.getCurrentLocation();  
    Node context = xctxt.getCurrentNode();
    XObject obj = m_functionExpr.execute(xctxt);
    
    if(m_functionExpr instanceof FuncRegexp)
    {
        m_groupMap = ((FuncRegexp)m_functionExpr).getGroupMap();
    }
    
    if(obj instanceof XLocationSet)
    {
        LocationIterator ll = ((XLocationSet)obj).locationSet();
        XNumber score = SCORE_NONE;
        
        if(null != ll && contextLoc != null)
        {
            Location loc;
            Range contextRange = (Range)contextLoc.getLocation();
            
            while(null != (loc = ll.nextLocation()))
            {
                Range range = (Range) loc.getLocation();
                
                if(range.compareBoundaryPoints(Range.START_TO_START,contextRange)==0)
                {
                    m_lastMatchedLocation = loc;
                    score = SCORE_OTHER;
                    break;
                }
                else
                    score = SCORE_NONE;
            }
        }
        return score;
    }
    else
    {
        NodeIterator nl = obj.nodeset();
        XNumber score = SCORE_NONE;

        if (null != nl)
        {
            Node n;

            while (null != (n = nl.nextNode()))
            {
                score = (n.equals(context)) ? SCORE_OTHER : SCORE_NONE;

                if (score == SCORE_OTHER)
                {
                    context = n;

                    break;
                }
            }
      
        }

        return score;
    }
  }
  
  public boolean isInside(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
      Location contextLoc = xctxt.getCurrentLocation();
      
      if(contextLoc!=null && contextLoc.getType()==Location.RANGE)
      {
          Range contextRange = (Range) contextLoc.getLocation();
          
          //assertion
          if(contextRange.getStartContainer()!=contextRange.getEndContainer() || 
            contextRange.getStartOffset()!=contextRange.getEndOffset())
              throw new RuntimeException("Not a collapsed range");
          
          XObject obj = m_functionExpr.execute(xctxt);
          
          if(obj instanceof XLocationSet)
          {
              LocationIterator li = ((XLocationSet)obj).locationSet();
              Location loc;
              
              while((loc=li.nextLocation())!=null)
              {
                  if(loc.getType()==Location.RANGE)
                  {
                      Range range = (Range)loc.getLocation();
                      
                      if(range.compareBoundaryPoints(Range.START_TO_START,contextRange)<=0
                        && range.compareBoundaryPoints(Range.START_TO_END,contextRange)>0)
                          return true;
                  }
              }
          }
      }
      
      return false;
  }
  
 /* public Location getMatchedLocation(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
      Location contextLoc = xctxt.getCurrentLocation();  
      XObject obj = m_functionExpr.execute(xctxt);
      
      if(obj instanceof XLocationSet)
      {
          LocationIterator ll = ((XLocationSet)obj).locationSet();
          
          if(null != ll && contextLoc != null)
          {
            Location loc;
            Range contextRange = (Range)contextLoc.getLocation();
            
            while((loc=ll.nextLocation())!=null)
            {
                Range range = (Range) loc.getLocation();
                
                if(range.compareBoundaryPoints(Range.START_TO_START,contextRange)==0)
                    return loc;
            }
          }
      }
      
      return null;
  }*/
  
  public Location getMatchedLocation(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
      return m_lastMatchedLocation;
  }
  
  private java.util.Hashtable m_groupMap = null;
  
  public java.util.Hashtable getGroupMap()
  {
      return m_groupMap;
  }
}
