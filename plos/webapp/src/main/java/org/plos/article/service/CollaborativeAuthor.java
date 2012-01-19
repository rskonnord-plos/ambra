package org.plos.article.service;

/**
 * CollaborativeAuthor - Simple class to represent the collaborative author(s) for citation
 * purposes.
 * 
 * @author jkirton
 */
public class CollaborativeAuthor {

  /**
   * The collective single name given for the collaborative author(s). Ref:
   * {@link http://dtd.nlm.nih.gov/publishing/tag-library/2.0/n-x630.html}
   */
  private String nameRef;

  /**
   * @return the nameRef
   */
  public String getNameRef() {
    return nameRef;
  }

  /**
   * @param nameRef the nameRef to set
   */
  public void setNameRef(String nameRef) {
    this.nameRef = nameRef;
  }
}