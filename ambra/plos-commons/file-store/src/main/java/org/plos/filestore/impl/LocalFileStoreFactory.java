package org.plos.filestore.impl;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.io.File;
import java.util.Hashtable;

/**
 * Factory for {@link org.plos.filestore.impl.FileSystemImpl} objects, to interface with tomcat.  Configure this in tomcat's context.xml as follows:
 *
 * <code>
 *  &lt;Resource name="ambra/FileStore"
 *   type="org.plos.filestore.FileStoreService"
 *   factory="org.plos.filestore.impl.LocalFileStoreFactory"
 *   baseDir="path to directory"
 *   domain="ambra" /&gt;
 * </code>
 *
 * @author Alex Kudlick 10/14/11
 */
public class LocalFileStoreFactory implements ObjectFactory {


  public static final String DOMAIN_PARAM = "domain";
  public static final String BASE_DIR_PARAM = "baseDir";

  public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
    String domain = (String) ((Reference) o).get(DOMAIN_PARAM).getContent();
    File baseDir = new File((String) ((Reference) o).get(BASE_DIR_PARAM).getContent());
    return new FileSystemImpl(baseDir, domain);
  }
}
