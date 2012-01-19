/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.ambra.models.support.fedora;

import java.net.URI;
import java.util.BitSet;

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Representation;
import org.topazproject.fedora.otm.FedoraBlob;
import org.topazproject.fedora.otm.FedoraBlobFactory;
import org.topazproject.fedora.otm.FedoraConnection;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;

import org.apache.commons.codec.net.URLCodec;

/**
 * A factory to create fedora blobs for article representations.
 *
 * @author Pradeep Krishnan
 */
public class RepresentationFedoraBlobFactory implements FedoraBlobFactory {
  private static final BitSet DOI_PID_CHARS = new BitSet(128);

  static {
    // Allowed chars in Fedora-pid: [A-Za-z0-9.-]+:([A-Za-z0-9.~_-]|%[0-9A-F]{2})+
    for (int ch = '0'; ch <= '9'; ch++)  DOI_PID_CHARS.set(ch);
    for (int ch = 'A'; ch <= 'Z'; ch++)  DOI_PID_CHARS.set(ch);
    for (int ch = 'a'; ch <= 'z'; ch++)  DOI_PID_CHARS.set(ch);
    DOI_PID_CHARS.set('-');
    DOI_PID_CHARS.set('_');
    DOI_PID_CHARS.set('.');
    DOI_PID_CHARS.set('~');
  }

  private final String pidNs;
  private final String uriPrefix;

  /**
   * Creates a new RepresentationFedoraBlobFactory object.
   *
   * @param pidNs the Fedora PID namespace for representations. (See retainPIDs in fedora.fcfg)
   * @param uriPrefix the uri prefix to strip to get to the Fedora PID (eg. 'info:doi/')
   */
  public RepresentationFedoraBlobFactory(String pidNs, String uriPrefix) {
    this.pidNs = pidNs;
    this.uriPrefix = uriPrefix;
  }

  /*
   * inherited javadoc
   */
  public String[] getSupportedUriPrefixes() {
    return new String[] { uriPrefix };
  }

  /*
   * inherited javadoc
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id, Object instance, FedoraConnection con)
                        throws OtmException {
    if (EntityMode.POJO != con.getSession().getEntityMode())
      throw new OtmException(con.getSession().getEntityMode() + " is not supported here. Fix me");

    if (instance == null)
      return null;

    if (!(instance instanceof Representation))
      throw new OtmException("Expecting entity " + Representation.class + " instead found " +
                             instance.getClass() + " (" + cm + ")");

    Representation r      = (Representation) instance;
    String         cModel = (r.getObject() instanceof Article) ? "AmbraArticle" : "AmbraArticleSecObj";
    String         pid    = toPid(r.getObject().getId());

    return new RepresentationFedoraBlob(cm, pid, r.getName(), r.getContentType(), cModel);
  }

  private String toPid(URI uri) throws OtmException {
    if (!uri.toString().startsWith(uriPrefix))
      throw new OtmException("Unknown uri type '" + uri + "' - can only map " + uriPrefix + "... uri's");

    try {
      byte[] dec = URLCodec.decodeUrl(uri.toString().substring(uriPrefix.length()).getBytes("UTF-8"));
      return pidNs + ":" + new String(URLCodec.encodeUrl(DOI_PID_CHARS, dec), "ISO-8859-1");
    } catch (Exception e) {
      throw new OtmException("Error converting '" + uri + "' to fedora pid", e);
    }
  }

  /*
   * inherited javadoc
   */
  public String generateId(ClassMetadata cm, FedoraConnection con) throws OtmException {
    throw new OtmException("Unsupported (for now)");
  }
}
