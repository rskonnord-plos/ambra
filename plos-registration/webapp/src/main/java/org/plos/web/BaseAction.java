/**
 * 
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import org.plos.OtherConstants;



/**
 * @author stevec
 *
 */
public class BaseAction extends ActionSupport {
  private OtherConstants otherConstants;

  /**
   * @return Returns the otherConstants.
   */
  public OtherConstants getOtherConstants() {
    return otherConstants;
  }

  /**
   * @param otherConstants The otherConstants to set.
   */
  public void setOtherConstants(OtherConstants otherConstants) {
    otherConstants = otherConstants;
  }
  
}
