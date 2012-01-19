/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.annotation;

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
