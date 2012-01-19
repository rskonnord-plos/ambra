/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.dom.ranges;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.traversal.NodeFilter;

/**
 * A NodeFilter for filtering nodes in a Range. The filter accepts only the nodes that are either
 * fully inside the range or the containing node of the start or end of the range.
 *
 * @author Pradeep Krishnan
 */
public class RangeNodeFilter implements NodeFilter {
  private Range    range;
  private Document document;
  private Node     cac;
  private boolean  filterDescendants;
  private List     acceptedNodes = new ArrayList();

  /**
   * Creates a new RangeNodeFilter object.
   *
   * @param range the range
   * @param filterDescendants a flag to indicate skipping of child nodes
   */
  public RangeNodeFilter(Range range, boolean filterDescendants) {
    this.range               = range;
    this.filterDescendants   = filterDescendants;
    cac                      = range.getCommonAncestorContainer();
    document                 = cac.getOwnerDocument();
  }

  /*
   * @see org.w3c.dom.traversal.NodeFilter#acceptNode
   */
  public short acceptNode(Node n) {
    if (!inside(n))
      return FILTER_SKIP;

    if (filterDescendants && descendantOfAccepted(n))
      return FILTER_SKIP;

    acceptedNodes.add(n);

    return FILTER_ACCEPT;
  }

  private boolean inside(Node n) {
    Range   r     = ((DocumentRange) document).createRange();
    boolean start = (n == range.getStartContainer());
    boolean end   = (n == range.getEndContainer());

    if (start || end)
      r.selectNodeContents(n);
    else
      r.selectNode(n);

    if (start)
      r.setStart(n, range.getStartOffset());

    if (end)
      r.setEnd(n, range.getEndOffset());

    //compare start of other to start of this
    int ss = range.compareBoundaryPoints(Range.START_TO_START, r);

    // -1 if this point before other, 0 if equal, 1 if this after other
    if (ss > 0)
      return false;

    //compare end of other to end of this
    int ee = range.compareBoundaryPoints(Range.END_TO_END, r);

    // -1 if this point before other, 0 if equal, 1 if this after other
    return (ee >= 0); // if this ends on or after other
  }

  private boolean descendantOfAccepted(Node n) {
    if (n == cac)
      return false;

    if (acceptedNodes.contains(n))
      return true;

    return descendantOfAccepted(n.getParentNode());
  }
}
