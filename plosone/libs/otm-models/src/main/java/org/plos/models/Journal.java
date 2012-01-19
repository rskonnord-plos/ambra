/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Marker class to mark an Aggregation as a "Journal".
 *
 * @author Pradeep Krishnan
 */
@Entity(type = PLoS.plos + "Journal", model = "ri")
public class Journal extends Aggregation {
  @Predicate(uri = PLoS.plos + "key")
  private String  key;

  @Predicate(uri = "http://prismstandard.org/namespaces/1.2/basic/eIssn")
  private String  eIssn;

  /**
   * Get the internal key used to identify this journal.
   *
   * @return the key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Set the internal key used to identify this journal.
   *
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Get the e-issn of this journal.
   *
   * @return the e-issn.
   */
  public String getEIssn() {
    return eIssn;
  }

  /**
   * Set the e-issn of this journal.
   *
   * @param eIssn the e-issn.
   */
  public void setEIssn(String eIssn) {
    this.eIssn = eIssn;
  }
}
