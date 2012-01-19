/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/** 
 * This filter just uses Fedora as a backup for the triples. All subject-uri's are hashed and
 * appended to a configured prefix in order to arrive at the Fedora PID to use. Furthermore it
 * is assumed that no model is being stored in the RELS-EXT datastream, so that the subject in
 * the RDF/XML need not be changed.
 * 
 * @author Ronald Tschalär
 */
public class BackupUpdateFilter implements UpdateFilter {
  private static final Logger logger = Logger.getLogger(BackupUpdateFilter.class);

  private final String pidBase;

  public BackupUpdateFilter(Configuration config, String base) throws IOException {
    pidBase = config.getString("backupUpdateFilter.pidBase", null);
    if (pidBase == null)
      throw new IOException("Missing configuration entry '" + base +
                            ".backupUpdateFilter.pidBase'");
  }

  /** 
   * Return a pid created from a configured base and a hash of the subject-uri.
   */
  public String getFedoraPID(URI subject, String[] models, String datastream) {
    return uriToPID(subject.toString());
  }

  /** 
   * Return the subject-uri.
   */
  public URI processSubject(URI subject, String[] models, String datastream) {
    return subject;
  }

  /** 
   * Use SHA-1 to hash up the id, base-64 encode the hash, remove the trailing '='s, and
   * append to the configured base.
   * 
   * @param id the id for which to create the Fedora PID
   * @return the Fedora PID
   */
  protected String uriToPID(String id) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      String hash =
          new String(Base64.encodeBase64(md.digest(id.getBytes("ISO-8859-1"))), "ISO-8859-1");

      return pidBase + hash.replaceAll("[+/=]", "~");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);  // can't happen
    } catch (NoSuchAlgorithmException nsae) {
      throw new RuntimeException(nsae); // can't happen
    }
  }
}
