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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import fedora.common.Constants;
import fedora.common.PID;

/** 
 * This filter contains the best-effort rules for storing stuff in Fedora. Best-effort in this case
 * mean it will try to store the triples A) in a fedora-pid that matches the subject-uri, if
 * possible, and B) will try to avoid changing the subject-uri if possible. Basically the rules boil
 * down to:
 * <ol>
 *   <li>If the subject starts with <var>info:fedora/</var> then it is assumed that what follows
 *       that prefix is a valid fedora pid and the subject is used as is.
 *   <li>Else the subject is modified according to the configured prefix mappings (e.g.
 *       info:doi/10.1371/foo.bar -&gt; doi:10.1371/foo.bar) and url-encoded to make it a valid
 *       PID (-&gt; doi:10.1371%2Ffoo.bar). If the resulting PID is too long, then it is shortened
 *       to the form &lt;shortPidBase&gt;&lt;hash-of-pid&gt;.
 *   <li>If the datastream is not RELS-EXT then the stored RDF contains the original URI; otherwise
 *       it will contain <var>info:fedora/&lt;pid&gt;</var>.
 * </ol>
 *
 * <p>A typical config would look like this:
 * <pre>
 *   topaz.fr.bestEffortUpdateFilter.shortPidBase=doi:10.1371%2Fshort.uri.
 *
 *   topaz.fr.bestEffortUpdateFilter.prefixMap.doi\:=doi:
 *   topaz.fr.bestEffortUpdateFilter.prefixMap.info\:doi/=doi:
 *   topaz.fr.bestEffortUpdateFilter.prefixMap.*=uri:
 *
 *   topaz.fr.bestEffortUpdateFilter.ignore.0=info:doi/10.1371/journal.
 *   topaz.fr.bestEffortUpdateFilter.ignore.1=doi:10.1371/journal.
 * </pre>
 * The '*' entry in the prefix map indicates the default to use if no other entries match.
 * 
 * @author Ronald Tschalär
 */
public class BestEffortUpdateFilter implements UpdateFilter {
  private static final Logger logger = Logger.getLogger(BestEffortUpdateFilter.class);

  private static final int    MAX_PID_LEN = PID.MAX_LENGTH;
  private static final BitSet PID_CHARS;

  private final String shortPidBase;
  private final Map    prefixMap;
  private final Set    ignoreList;

  static {
    PID_CHARS = new BitSet(256);
    for (int idx = 'a'; idx <= 'z'; idx++)
        PID_CHARS.set(idx);
    for (int idx = 'A'; idx <= 'Z'; idx++)
        PID_CHARS.set(idx);
    for (int idx = '0'; idx <= '9'; idx++)
        PID_CHARS.set(idx);
    PID_CHARS.set('.');
    PID_CHARS.set('~');
    PID_CHARS.set('_');
    PID_CHARS.set('-');
  }

  public BestEffortUpdateFilter(Configuration config, String base) throws IOException {
    config = config.subset("bestEffortUpdateFilter");
    base  += ".bestEffortUpdateFilter";

    shortPidBase = config.getString("shortPidBase", null);

    prefixMap = new HashMap();
    for (Iterator iter = config.getKeys("prefixMap"); iter.hasNext(); ) {
      String n = (String) iter.next();
      prefixMap.put(n.substring(10), config.getString(n));
    }

    ignoreList = new HashSet();
    for (Iterator iter = config.getKeys("ignore"); iter.hasNext(); ) {
      String n = (String) iter.next();
      ignoreList.add(config.getString(n));
    }
  }

  /** 
   * If the URI is a fedora URI ({@link fedora.common.Constants.FEDORA#uri FEDORA.uri}) then
   * return the URI minus the fedora prefix. Else apply the prefix map; if the resulting PID
   * is too long to be a fedora pid, shorten it.
   */
  public String getFedoraPID(URI subject, String[] models, String datastream) {
    String subj = subject.toString();

    for (Iterator iter = ignoreList.iterator(); iter.hasNext(); ) {
      String ign = (String) iter.next();
      if (subj.startsWith(ign))
        return null;
    }

    if (subj.startsWith(Constants.FEDORA.uri))
      return subj.substring(Constants.FEDORA.uri.length());

    String pid = null;
    for (Iterator iter = prefixMap.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry e = (Map.Entry) iter.next();
      String pfx = (String) e.getKey();
      if (subj.startsWith(pfx)) {
        pid = e.getValue() + pidEscape(subj.substring(pfx.length()));
        break;
      }
    }

    if (pid == null)
      pid = prefixMap.get("*") + pidEscape(subj);

    if (pid.length() > MAX_PID_LEN)
      pid = shortenPid(pid);

    return pid;
  }

  /** 
   * If the datastream is not <var>RELS-EXT</var> then return <var>subject</var>; else return
   * <var>info:fedora/&lt;getFedoraPID()&gt;</var>.
   */
  public URI processSubject(URI subject, String[] models, String datastream) {
    if (!datastream.equals("RELS-EXT"))
      return subject;

    return URI.create(Constants.FEDORA.uri + getFedoraPID(subject, models, datastream));
  }

  protected String shortenPid(String id) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      String hash =
          new String(Base64.encodeBase64(md.digest(id.getBytes("ISO-8859-1"))), "ISO-8859-1");

      return shortPidBase + pidEscape(hash);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);  // can't happen
    } catch (NoSuchAlgorithmException nsae) {
      throw new RuntimeException(nsae); // can't happen
    }
  }

  protected static final String pidEscape(String in) {
    try {
      return new String(URLCodec.encodeUrl(PID_CHARS, in.getBytes("UTF-8")), "US-ASCII");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected exception", uee);  // can't really happen
    }
  }
}
