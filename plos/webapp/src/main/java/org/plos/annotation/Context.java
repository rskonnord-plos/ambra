/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation;

/**
 * Simple encapsulation of properties that define an annotation context.
 * 
 * @author jkirton
 */
public class Context {
  private String startPath;
  private int startOffset;
  private String endPath;
  private int endOffset;
  private String target;

  /**
   * Constructor
   */
  public Context() {
    super();
  }

  /**
   * Constructor
   * 
   * @param startPath
   * @param startOffset
   * @param endPath
   * @param endOffset
   * @param target
   */
  public Context(String startPath, int startOffset, String endPath, int endOffset, String target) {
    super();
    this.startPath = startPath;
    this.startOffset = startOffset;
    this.endPath = endPath;
    this.endOffset = endOffset;
    this.target = target;
  }

  public String getStartPath() {
    return startPath;
  }

  public void setStartPath(String startPath) {
    this.startPath = startPath;
  }

  public int getStartOffset() {
    return startOffset;
  }

  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }

  public String getEndPath() {
    return endPath;
  }

  public void setEndPath(String endPath) {
    this.endPath = endPath;
  }

  public int getEndOffset() {
    return endOffset;
  }

  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(512);
    sb.append("START[path:");
    sb.append(startPath);
    sb.append(", offset:");
    sb.append(startOffset);
    sb.append("] END[");
    sb.append(endPath);
    sb.append(", offset:");
    sb.append(endOffset);
    sb.append("] (target:");
    sb.append(target);
    sb.append(')');
    return sb.toString();
  }
}
