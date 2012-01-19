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
package org.topazproject.otm.mapping.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.topazproject.otm.Blob;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.BinderFactory;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.PropertyBinder.ManagedStreamer;
import org.topazproject.otm.mapping.PropertyBinder.SerializingStreamer;
import org.topazproject.otm.mapping.PropertyBinder.Streamer;
import org.topazproject.otm.mapping.PropertyBinder.UnManagedStreamer;
import org.topazproject.otm.metadata.BlobDefinition;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.EmbeddedDefinition;
import org.topazproject.otm.metadata.RdfDefinition;
import org.topazproject.otm.metadata.VarDefinition;
import org.topazproject.otm.serializer.Serializer;

/**
 * A binder factory that can create a PropertyBinder for a given property.
 *
 * @author Pradeep Krishnan
 */
public class PropertyBinderFactory implements BinderFactory {
  private final String   propertyName;
  private final Property property;

  /**
   * Creates a new PropertyBinderFactory object.
   *
   * @param propertyName the property definition name
   * @param property the java bean property
   */
  public PropertyBinderFactory(String propertyName, Property property) {
    this.propertyName   = propertyName;
    this.property       = property;
  }

  /*
   * inherited javadoc
   */
  public String getPropertyName() {
    return propertyName;
  }

  /*
   * inherited javadoc
   */
  public EntityMode getEntityMode() {
    return EntityMode.POJO;
  }

  /*
   * inherited javadoc
   */
  public PropertyBinder createBinder(SessionFactory sf) throws OtmException {
    Definition pd = sf.getDefinition(propertyName);

    if (pd instanceof EmbeddedDefinition)
      return new EmbeddedClassFieldBinder(property,
                                     sf.getClassMetadata(((EmbeddedDefinition) pd).getEmbedded()));

    Serializer serializer;
    Streamer streamer  = null;
    Class<?>   type = property.getComponentType();

    if (pd instanceof RdfDefinition) {
      RdfDefinition rd = (RdfDefinition) pd;
      serializer = (rd.isAssociation()) ? null
                   : sf.getSerializerFactory().getSerializer(type, rd.getDataType());

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' with dataType '"
                               + rd.getDataType() + "' for " + property);
    } else if (pd instanceof VarDefinition) {
      VarDefinition vd = (VarDefinition) pd;
      serializer = (vd.getAssociatedEntity() == null) ? null
                   : sf.getSerializerFactory().getSerializer(type, null);
    } else
      serializer = sf.getSerializerFactory().getSerializer(type, null);

    if (pd instanceof BlobDefinition) {
      if (property.isCollection())
        throw new OtmException("Collections of Blobs not supported. Property: " + property);

      if ((property.isArray() && !type.equals(Byte.TYPE)))
        throw new OtmException("Arrays of Blobs not supported. Property: " + property);

      streamer = getStreamer(type);

      if (streamer == null) {
        if (serializer == null)
          throw new OtmException("Cannot create a Blob-Streamer for property : " + property);

        streamer = new SerializingStreamer();
      } else {
        /*
         * Remove serializer here otherwise serializer may create multiple
         * values. (eg. one value for each byte in a byte[] field.) This
         * will create issues for text-search etc.
         *
         * The only place where serializer is appropriate is when
         * using a serializing-streamer.
         */
        serializer = null;
      }
    }

    if (property.isArray())
      return new ArrayFieldBinder(property, serializer, streamer);

    if (property.isCollection())
      return new CollectionFieldBinder(property, serializer);

    return new ScalarFieldBinder(property, serializer, streamer);
  }

  private Streamer getStreamer(Class<?> type) throws OtmException {
    // really need a factory here
    if (type.equals(Byte.TYPE))
      return getByteArrayStreamer();

    if (type.equals(DataSource.class))
      return getDataSourceStreamer();

    if (type.equals(Blob.class))
      return getBlobStreamer();

    if (type.equals(InputStream.class))
      return getInputStreamStreamer();

    return null;
  }

  private static Streamer getBlobStreamer() {
    return new ManagedStreamer() {
      public void attach(PropertyBinder binder, Object instance, Blob blob) throws OtmException {
        binder.setRawValue(instance, blob);
      }
    };
  }

  private static Streamer getByteArrayStreamer() {
    return new UnManagedStreamer() {

      public byte[] getBytes(PropertyBinder binder, Object instance) throws OtmException {
        return (byte[]) binder.getRawValue(instance, false);
      }

      public void setBytes(PropertyBinder binder, Object instance, byte[] bytes)
          throws OtmException {
        binder.setRawValue(instance, bytes);
      }
    };
  }

  private static Streamer getDataSourceStreamer() {
    return new ManagedStreamer() {

      public void attach(PropertyBinder binder, Object instance, final Blob blob) throws OtmException {
        binder.setRawValue(instance, new DataSource() {

          public String getContentType() {
            return "application/octet-stream";
          }

          public InputStream getInputStream() throws IOException {
            return blob.getInputStream();
          }

          public String getName() {
            return blob.getId();
          }

          public OutputStream getOutputStream() throws IOException {
            return blob.getOutputStream();
          }
        });
      }
    };
  }

  private static Streamer getInputStreamStreamer() {
    return new ManagedStreamer() {
      public void attach(PropertyBinder binder, Object instance, final Blob blob) throws OtmException {
        binder.setRawValue(instance, new InputStream() {
          private InputStream in = null;

          private InputStream ensureOpen() throws IOException {
            if (in == null)
              in = blob.getInputStream();

            return in;
          }

          @Override
          public int available() throws IOException {
            return ensureOpen().available();
          }

          @Override
          public void close() throws IOException {
            if (in != null)
              in.close();

            in = null;
          }

          @Override
          public synchronized void mark(int readlimit) {
            try {
              ensureOpen().mark(readlimit);
            } catch (IOException e) {
              throw new OtmException("Lost connection to Blob", e);
            }
          }

          @Override
          public boolean markSupported() {
            try {
              return ensureOpen().markSupported();
            } catch (IOException e) {
              throw new OtmException("Lost connection to Blob", e);
            }
          }

          @Override
          public int read(byte[] b, int off, int len) throws IOException {
            return ensureOpen().read(b, off, len);
          }

          @Override
          public int read(byte[] b) throws IOException {
            return ensureOpen().read(b);
          }

          @Override
          public int read() throws IOException {
            return ensureOpen().read();
          }

          @Override
          public synchronized void reset() throws IOException {
            ensureOpen().reset();
          }

          @Override
          public long skip(long n) throws IOException {
            return ensureOpen().skip(n);
          }
        });
      }
    };
  }
}
