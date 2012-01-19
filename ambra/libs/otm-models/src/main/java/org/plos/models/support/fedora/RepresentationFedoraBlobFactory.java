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
package org.plos.models.support.fedora;

import java.net.URI;
import java.util.BitSet;

import org.plos.models.Article;
import org.plos.models.Representation;

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

  /*
   * inherited javadoc
   */
  public String[] getSupportedUriPrefixes() {
    return new String[] { "info:doi/" };
  }

  /*
   * inherited javadoc
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id, Object instance, FedoraConnection con)
                        throws OtmException {
    if (EntityMode.POJO != con.getSession().getEntityMode())
      throw new OtmException(con.getSession().getEntityMode() + " is not supported here. Fix me");

    if (!(instance instanceof Representation))
      throw new OtmException("Expecting entity " + Representation.class + " instead found " + cm);

    Representation r      = (Representation) instance;

    String         cModel =
      (r.getObject() instanceof Article) ? "PlosArticle" : "PlosArticleSecObj";

    String pid = toPid(r.getObject().getId());

    return new RepresentationFedoraBlob(cm, id, pid, r.getName(), r.getContentType(), cModel);
  }

  private static String toPid(URI uri) throws OtmException {
    // info:doi/ -> doi:
    if (!uri.toString().startsWith("info:doi/"))
      throw new OtmException("Unknown uri type '" + uri + "' - can only map info:doi/... uri's");

    try {
      byte[] dec = URLCodec.decodeUrl(uri.toString().substring(9).getBytes("UTF-8"));
      return "doi:" + new String(URLCodec.encodeUrl(DOI_PID_CHARS, dec), "ISO-8859-1");
    } catch (Exception e) {
      throw new OtmException("Error converting '" + uri + "' to fedora pid", e);
    }
  }

  /*
   * inherited javadoc
   */
  public String generateId(ClassMetadata cm, FedoraConnection con)
                    throws OtmException {
    throw new OtmException("Unsupported (for now)");
  }
}
