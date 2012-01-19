/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.article.service;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.ImplicitCollectionMapper;

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;

import org.topazproject.xml.transform.EntityResolvingSource;
import org.topazproject.xml.transform.cache.CachedSource;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.TextRepresentation;
import org.topazproject.ambra.permission.service.PermissionsService;

import org.topazproject.otm.AbstractBlob;
import org.topazproject.otm.Blob;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * The article ingestor.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class Ingester {
  private static final Log    log = LogFactory.getLog(Ingester.class);

  private final Zip zip;

  private final TransformerFactory tFactory;
  private Document objInfo;

  /**
   * Create a new ingester for the given zip.
   *
   * @param zip   the zip archive containing the article and it's related objects
   */
  public Ingester(Zip zip) {
    this.zip = zip;

    tFactory = new TransformerFactoryImpl();
    tFactory.setURIResolver(new URLResolver());
    tFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
    tFactory.setAttribute("http://saxon.sf.net/feature/strip-whitespace", "none");
    tFactory.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing a stylesheet", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing a stylesheet", te);
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing a stylesheet", te);
      }
    });
  }

  /**
   * Create a new ingester for the given DataSource.
   *
   * @param article the article data-source
   *
   * @throws IOException on an error
   */
  public Ingester(DataSource article) throws IOException {
    this(createZip(article));
  }

  private static Zip createZip(DataSource article) throws IOException {
    if ((article instanceof FileDataSource)
        && Zip.StreamZip.isZip(article.getContentType()))
      return new Zip.FileZip(((FileDataSource) article).getFile().toString());
    return new Zip.DataSourceZip(article);
  }

  /**
   * Prepare the ingester for ingesting. This can be done outside a transaction
   * scope. It creates the object descriptions ready for ingest.
   *
   * @param configuration Ambra configuration
   * @throws IngestException on an error in reading the zip file
   */
  public void prepare(Configuration configuration) throws IngestException {
    try {
      // get zip info
      String zipInfo = Zip2Xml.describeZip(zip);
      if (log.isDebugEnabled())
        log.debug("Extracted zip-description: " + zipInfo);

      // find ingest format handler
      String handler = findIngestHandler(zipInfo);
      if (log.isDebugEnabled())
        log.debug("Using ingest handler '" + handler + "'");

      // use handler to convert zip to object descriptions
      objInfo = zip2Obj(zip, zipInfo, handler,
                        configuration.getString("ambra.platform.doiUrlPrefix", null));
      if (log.isDebugEnabled())
        log.debug("Got object-info '" + dom2String(objInfo) + "'");
    } catch (IOException ioe) {
      throw new IngestException("Error reading zip", ioe);
    } catch (TransformerException te) {
      throw new IngestException("Zip format error", te);
    }
  }

  /**
   * Ingest a new article.
   *
   * @param configuration Ambra configuration
   * @param sess     the OTM session to use to add the objects
   * @param permSvc  the permissions-service to use to add the permissions
   * @param force if true then don't check whether this article already exists but just
   *              save this new article.
   * @return the new article
   * @throws DuplicateArticleIdException if an article exists with the same URI as the new article
   *                                     and <var>force</var> is false
   * @throws IngestException if there's any other problem ingesting the article
   */
  public Article ingest(Configuration configuration, Session sess, PermissionsService permSvc,
                        boolean force)
      throws DuplicateArticleIdException, IngestException {

    if (objInfo == null)
      prepare(configuration);

    try {
      // process the object descriptions
      Article art = processObjectInfo(zip, objInfo, force, sess, permSvc);

      log.info("Successfully ingested '" + art.getId() + "'");
      return art;
    } catch (OtmException oe) {
      throw new IngestException("Error talking to triple- or blob-store", oe);
    } catch (XStreamException xe) {
      throw new IngestException("Error unmarshalling object-descriptors", xe);
    }
  }

  /**
   * Look up the appropriate stylesheet to handle the zip. This stylesheet is responsible
   * for converting a zip-info doc (zip.dtd) to a fedora-objects doc (fedora.dtd).
   *
   * @param zipInfo the zip archive description (zip.dtd)
   * @return the URL of the stylesheet
   */
  private String findIngestHandler(String zipInfo) {
    // FIXME: make this configurable, and allow for some sort of lookup
    return getClass().getResource("pmc2obj.xslt").toString();
  }

  /**
   * Run the main ingest script.
   *
   * @param zip     the zip archive containing the items to ingest
   * @param zipInfo the document describing the zip archive (adheres to zip.dtd)
   * @param handler the stylesheet to run on <var>zipInfo</var>; this is the main script
   * @param doiUrlPrefix DOI URL prefix
   * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
   * @throws TransformerException if an error occurs during the processing
   */
  private Document zip2Obj(Zip zip, String zipInfo, String handler, String doiUrlPrefix)
      throws TransformerException {
    Transformer t = tFactory.newTransformer(new StreamSource(handler));
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setURIResolver(new ZipURIResolver(zip));

    // override the doi url prefix if one is specified in the config
    if (doiUrlPrefix != null)
      t.setParameter("doi-url-prefix", doiUrlPrefix);

    /*
     * Note: it would be preferable (and correct according to latest JAXP specs) to use
     * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
     * Hence we need to use Saxon's API's in order to get at those messages.
     */
    final StringWriter msgs = new StringWriter();
    ((Controller) t).makeMessageEmitter();
    ((Controller) t).getMessageEmitter().setWriter(msgs);
    t.setErrorListener(new ErrorListener() {
      public void warning(TransformerException te) {
        log.warn("Warning received while processing zip", te);
      }

      public void error(TransformerException te) {
        log.warn("Error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }

      public void fatalError(TransformerException te) {
        log.warn("Fatal error received while processing zip", te);
        msgs.write(te.getMessageAndLocation() + '\n');
      }
    });

    Source    inp = new StreamSource(new StringReader(zipInfo), "zip:/");
    DOMResult res = new DOMResult();

    try {
      t.transform(inp, res);
    } catch (TransformerException te) {
      if (msgs.getBuffer().length() > 0)
        throw new TransformerException(msgs.toString(), te);
      else
        throw te;
    }
    if (msgs.getBuffer().length() > 0)
      throw new TransformerException(msgs.toString());

    return (Document) res.getNode();
  }

  private Article processObjectInfo(final Zip zip, Document objInfo, boolean force,
                                    Session sess, PermissionsService permSvc)
      throws IngestException, DuplicateArticleIdException {
    Article art = null;
    final StringWriter msgs = new StringWriter();

    XStream unmarshaller = getUnmarshaller(zip);

    for (Element child : getChildren(objInfo.getDocumentElement(), null)) {
      String tag = child.getTagName();

      if (tag.equals("Article")) {
        art = (Article) unmarshaller.unmarshal(new DomReader(child));
        Article existing = sess.get(Article.class, art.getId().toString());

        if (!force && existing != null)
          throw new DuplicateArticleIdException(art.getId().toString());

        Map<Representation, Blob> blobs = gatherBlobs(art);

        if (existing != null) {
          sess.delete(existing);
          sess.flush();
        }

        art.setArchiveName(getArchiveName(zip));

        sess.saveOrUpdate(art);

        writeBlobs(blobs);

      } else if (tag.equals("propagatePermissions")) {
        String resource = child.getAttribute("resource");
        Set<String> to = new HashSet<String>();
        for (Element t : getChildren(child, "to"))
          to.add(t.getTextContent());
        permSvc.propagatePermissions(resource, to.toArray(new String[to.size()]));

      } else {
        throw new IngestException("Unrecognized element '" + tag + "' in object descriptions:\n" +
                                  dom2String(objInfo));
      }
    }

    if (msgs.getBuffer().length() > 0)
      throw new IngestException(msgs.toString());

    return art;
  }

  private String getArchiveName(Zip zip) {
    String archiveName = zip.getName();
    if (archiveName != null && archiveName.contains(File.separator)) {
      archiveName = archiveName.substring(archiveName.lastIndexOf(File.separatorChar)+1);
    }
    return archiveName;
  }

  private Map<Representation, Blob> gatherBlobs(Article art) {
    Map<Representation, Blob> map = new HashMap<Representation, Blob>();
    for (Representation rep : art.getRepresentations()) {
      if (rep.getBody() != null) {
        map.put(rep, rep.getBody());
        rep.setBody(null);  // let OTM assign one
      }
    }

    // xstream could set parts as null
    if (art.getParts() != null) {
      for (ObjectInfo part : art.getParts()) {
        for (Representation rep : part.getRepresentations()) {
          if (rep.getBody() != null) {
            map.put(rep, rep.getBody());
            rep.setBody(null);  // let OTM assign one
          }
        }
      }
    }
    return map;
  }

  private void writeBlobs(Map<Representation, Blob> map) throws OtmException {
    for (Map.Entry<Representation, Blob> entry : map.entrySet()) {
      log.info("Copying from zip to Blob. rep = '" + entry.getKey().getName() + "', id = "
          + entry.getKey().getId());
      InputStream in = null;
      OutputStream out = null;
      try {
        IOUtils.copy(in = entry.getValue().getInputStream(),
            out = entry.getKey().getBody().getOutputStream());
      } catch (IOException e) {
        throw new OtmException("Error copying from zip. rep = '" + entry.getKey().getName()
            + "', id = " + entry.getKey().getId(), e);
      } finally {
        for (Closeable c : new Closeable[] { in, out }) {
          try {
            if (c != null)
              c.close();
          } catch (IOException e) {
            log.warn("Error while closing a blob copy stream for " + entry.getKey().getId(), e);
          }
        }
      }
    }
  }

  private List<Element> getChildren(Element parent, String child) {
    List<Element> res = new ArrayList<Element>();

    NodeList items = parent.getChildNodes();
    for (int idx = 0; idx < items.getLength(); idx++) {
      if (items.item(idx) instanceof Element) {
        Element c = (Element) items.item(idx);
        if (child == null || c.getTagName().equals(child))
          res.add(c);
      }
    }

    return res;
  }

  private XStream getUnmarshaller(final Zip zip) {
    final XStream xstream = new XStream(null, null, getClass().getClassLoader(),
                                        new CollectionMapper(new XStream().getMapper()));

    xstream.setMode(XStream.ID_REFERENCES);

    xstream.registerConverter(new SingleValueConverter() {
      public boolean canConvert(Class type) {
        return type == URI.class;
      }

      public String toString(Object obj) {
        return obj.toString();
      }

      public Object fromString(String str) {
        try {
          return new URI(str);
        } catch (Exception e) {
          throw new XStreamException(str, e);
        }
      }
    });

    xstream.registerConverter(new SingleValueConverter() {
      public boolean canConvert(Class type) {
        return type == Blob.class;
      }

      public String toString(Object obj) {
        return "";      // not used
      }

      public Object fromString(String str) {
        if (log.isDebugEnabled())
          log.debug("Setting up ZipBlob for " + str);
        return new ZipBlob(str, zip);
      }
    });

    xstream.registerConverter(new Converter() {
      private final Converter orig =
                          xstream.getConverterLookup().lookupConverterForType(Representation.class);

      public boolean canConvert(Class type) {
        return Representation.class.isAssignableFrom(type);
      }

      public void marshal(Object value, HierarchicalStreamWriter writer,
                          MarshallingContext context) {
        // not used
      }

      public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Class rep =
            reader.getAttribute("isText") != null ? TextRepresentation.class : Representation.class;
        return context.convertAnother(context.currentObject(), rep, orig);
      }
    });

    xstream.alias("Article", Article.class);

    return xstream;
  }

  private String dom2String(Node dom) {
    try {
      StringWriter sw = new StringWriter(500);
      Transformer t = tFactory.newTransformer();
      t.transform(new DOMSource(dom), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException te) {
      log.error("Error converting dom to string", te);
      return "";
    }
  }

  /**
   * Saxon's default resolver uses URI.resolve() to resolve the relative URI's. However, that
   * doesn't understand 'jar' URL's and treats them as opaque (because of the "jar:file:"
   * prefix). That in turn prevents us from using relative URI's for things like xsl:include
   * and xsl:import . Hence this class here, which just uses URL to do the resolution.
   */
  private static class URLResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
      if (href.length() == 0)
        return null;  // URL doesn't handle this case properly, so let default resolver handle it

      try {
        URL url = new URL(new URL(base), href);
        return new StreamSource(url.toString());
      } catch (MalformedURLException mue) {
        log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
                 "default URIResolver", mue);
        return null;
      }
    }
  }

  /**
   * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
   */
  private static class ZipURIResolver extends URLResolver {
    private final Zip zip;

    /**
     * Create a new resolver that returns documents from the given zip.
     *
     * @param zip the zip archive to return documents from
     */
    public ZipURIResolver(Zip zip) {
      this.zip = zip;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      if (log.isDebugEnabled())
        log.debug("resolving: base='" + base + "', href='" + href + "'");

      if (!base.startsWith("zip:"))
        return super.resolve(href, base);

      try {
        InputSource src = resolveToIS(base, href);
        if (src == null)
          return null;

        return new EntityResolvingSource(src, new EntityResolver() {
          public InputSource resolveEntity(String publicId, String systemId)
              throws SAXException, IOException {
            if (systemId != null && systemId.startsWith("zip:"))
              return resolveToIS("zip:/", systemId);
            return CachedSource.getResolver().resolveEntity(publicId, systemId);
          }
        });
      } catch (IOException ioe) {
        throw new TransformerException(ioe);
      } catch (SAXException se) {
        throw new TransformerException(se);
      }
    }

    private InputSource resolveToIS(String base, String rel) throws IOException {
      URI uri = URI.create(base).resolve(rel);
      InputStream is = zip.getStream(uri.getPath().substring(1), new long[1]);
      if (is == null)         // hack to deal with broken AP zip's that contain absolute paths
        is = zip.getStream(uri.getPath(), new long[1]);

      if (log.isDebugEnabled())
        log.debug("resolved: uri='" + uri + "', found=" + (is != null));

      if (is == null)
        return null;

      InputSource src = new InputSource(is);
      src.setSystemId(uri.toString());

      return src;
    }
  }

  /**
   * Custom implicit-collection-mapper for XStream. The primary purpose of this is to reduce the
   * amount of direct dependencies on package names and on the exact definition of the classes.
   * For collections we have the following options:
   * <ol>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       &lt;org.topazproject.ambra.models.UserProfile&gt;
   *         ...
   *       &lt;/org.topazproject.ambra.models.UserProfile&gt;
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       &lt;UserProfile&gt;
   *         ...
   *       &lt;/UserProfile&gt;
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       ...
   *     &lt;/authors&gt;
   *     &lt;authors&gt;
   *       ...
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   * </ol>
   * The first one requires the description generator to know about both component types of
   * collections and their fully-qualified package names; the second requires us to provide
   * explicit aliases for almost all model classes; the last one requires us to provide
   * implicit-collection-mappings for almost all model classes.
   *
   * <p>This class automates the third option by dynamically looking up the component type
   * of collections (thanks to generics). Unfortunately there's also a bug in XStream 1.3
   * with regards to subclasses, so we need to do most of the mappings management and lookup
   * ourselves.
   */
  public static class CollectionMapper extends ImplicitCollectionMapper {
    private final Map<Class<?>, Set<ImplicitCollectionMapping>> mappings =
                                      new HashMap<Class<?>, Set<ImplicitCollectionMapping>>();

    public CollectionMapper(Mapper wrapped) {
      super(wrapped);
    }

    @Override
    public String getFieldNameForItemTypeAndName(Class definedIn, Class itemType,
                                                 String itemFieldName) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (icm.getItemType().isAssignableFrom(itemType) &&
              itemFieldName.equals(icm.getFieldName()))
            return icm.getFieldName();
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    @Override
    public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (itemFieldName.equals(icm.getFieldName()))
            return icm.getItemType();
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    @Override
    public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType,
                                                                          String fieldName) {
      ImplicitCollectionMapping icm = findMapping(itemType, fieldName);
      if (icm != null)
        return icm;

      // see if this is an array or collection, and if so get the component type
      Class<?> declType = null;
      Class<?> compType = null;
      try {
        // try this as a public field
        Field f = itemType.getField(fieldName);
        declType = f.getDeclaringClass();
        compType = getCompType(f.getType(), f.getGenericType());
      } catch (NoSuchFieldException nsfe) {
        if (Ingester.log.isTraceEnabled())
          Ingester.log.trace("class '" + itemType.getName() + "', field '" + fieldName + "'", nsfe);

        // not a public field, so look for getter
        String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
          Method m = itemType.getMethod(getter);
          declType = m.getDeclaringClass();
          compType = getCompType(m.getReturnType(), m.getGenericReturnType());
        } catch (NoSuchMethodException nsme) {
          if (Ingester.log.isTraceEnabled())
            Ingester.log.trace("class '" + itemType.getName() + "', method '" + getter + "'", nsme);
        }
      }

      if (compType != null) {
        super.add(declType, fieldName, compType);
        icm = super.getImplicitCollectionDefForFieldName(declType, fieldName);
        getOrCreateICM(declType).add(icm);

        if (Ingester.log.isTraceEnabled())
          Ingester.log.trace("created def: defIn=" + declType + ", fieldName=" +
                             icm.getFieldName() + ", fieldType=" + icm.getItemType() +
                             ", elemName=" + icm.getItemFieldName());
      }

      return icm;
    }

    private Set<ImplicitCollectionMapping> getOrCreateICM(Class<?> definedIn) {
      Set<ImplicitCollectionMapping> icmList = mappings.get(definedIn);
      if (icmList == null)
        mappings.put(definedIn, icmList = new HashSet<ImplicitCollectionMapping>());
      return icmList;
    }

    private ImplicitCollectionMapping findMapping(Class<?> definedIn, String field) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (icm.getFieldName().equals(field))
            return icm;
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    private static Class<?> getCompType(Class<?> clazz, Type type) {
      if (clazz.isArray() &&  clazz != byte[].class)
        return clazz.getComponentType();

      if (Collection.class.isAssignableFrom(clazz))
        return getCompType(type);

      return null;
    }

    private static Class<?> getCompType(Type collType) {
      return getClass(((ParameterizedType) collType).getActualTypeArguments()[0]);
    }

    private static Class<?> getClass(Type t) {
      if (t instanceof Class)
        return (Class<?>) t;

      if (t instanceof GenericArrayType)
        return Array.newInstance(getClass(((GenericArrayType) t).getGenericComponentType()), 0).
                     getClass();

      if (t instanceof ParameterizedType)
        return getClass(((ParameterizedType) t).getRawType());

      if (t instanceof WildcardType)
        return getClass(((WildcardType) t).getUpperBounds()[0]);

      return Object.class;
    }
  }

  private static class ZipBlob extends AbstractBlob {
    private final Zip zip;

    protected ZipBlob(String id, Zip zip) {
      super(id);
      this.zip = zip;
    }

    @Override
    protected InputStream doGetInputStream() throws OtmException {
      try {
        return zip.getStream(getId(), new long[1]);
      } catch (IOException e) {
        throw new OtmException("Failed to read zip entry: " + getId(), e);
      }
    }

    @Override
    protected OutputStream doGetOutputStream() throws OtmException {
      return null;
    }

    @Override
    protected void writing(OutputStream out) {
    }

    public boolean create() throws OtmException {
      return false;
    }

    public boolean delete() throws OtmException {
      return false;
    }

    public boolean exists() throws OtmException {
      return true;
    }

    public ChangeState getChangeState() {
      return ChangeState.NONE;
    }

    public ChangeState mark() {
      return ChangeState.NONE;
    }

    public byte[] readAll(boolean original) {
      return readAll();
    }
  }

  public Zip getZip() {
    return zip;
  }
}
