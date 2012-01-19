/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.otm;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * An id generator that allocates ids from Fedora.
 *
 * @author Pradeep Krishnan
 */
public class FedoraIdGenerator implements IdentifierGenerator {
  private String prefix;

  /*
   * inherited javadoc
   */
  public String generate(ClassMetadata cm, Session sess) throws OtmException {
    FedoraConnection con = FedoraConnection.getCon(sess);
    if (con == null) {
      /* FIXME: using sf.getBlobStore() is a hack and will fail when a multi-blob store is used
       * instead!
       */
      con = (FedoraConnection) sess.getSessionFactory().getBlobStore().openConnection(sess, false);
    }

    return con.getBlobStore().generateId(cm, prefix, con);
  }

  /*
   * inherited javadoc
   */
  public void setUriPrefix(String prefix) {
    this.prefix = prefix;
  }
}
