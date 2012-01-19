/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.finder.AttributeFinder;

/**
 * A factory class for creating PDPs configured in a config file. PDPs can be configured with the
 * varoius attribute,policy and resource finder modules. Also xacml extensions (new attribute
 * types and functions) can be defined in the config file. The xacml extensions will be shared
 * across all PDP instances.
 * 
 * <p>
 * Makes use of <code>com.sun.xacml.ConfigurationStore</code> for parsing the config file.
 * </p>
 *
 * @author Pradeep Krishnan
 */
public class PDPFactory {
  /**
   * Default PDP configuration File.
   */
  public static final String DEFAULT_PDP_CONFIG = "/WEB-INF/PDPConfig.xml";
  private ConfigurationStore store;
  private PDP                defaultPDP       = null;
  private PDPConfig          defaultPDPConfig = null;
  private Map                pdpMap           = new HashMap();

  /**
   * Returns a singleton instance of the factory for a wep-app. The instance is maintained in the
   * <code>ServletContext</code> itself.
   * 
   * <p>
   * Uses {@link #getPDPConfigFile} to locate the configuration file for the factory.
   * </p>
   * 
   * <p></p>
   *
   * @param context The web-app context
   *
   * @return Returns the PDPFactory instance
   *
   * @throws IOException on error in accessing the config file
   * @throws ParsingException on error in parsing the config file
   * @throws UnknownIdentifierException when an unknown identifier was used in a standard xacml
   *         factory
   */
  public static PDPFactory getInstance(ServletContext context)
                                throws IOException, ParsingException, UnknownIdentifierException {
    PDPFactory instance;

    synchronized (context) {
      instance = (PDPFactory) context.getAttribute(PDPFactory.class.getName());

      if (instance == null) {
        instance = new PDPFactory(getPDPConfigFile(context));
        context.setAttribute(PDPFactory.class.getName(), instance);
      }
    }

    return instance;
  }

  /**
   * Creates a PDPFactory from a configuration file.  See
   * <code>com.sun.xacml.ConfigurationStore</code> on how to configure the Factory.
   *
   * @param configFile A file containing various PDP configurations
   *
   * @throws ParsingException on error in parsing the config file
   * @throws UnknownIdentifierException when an unknown identifier was used in a standard xacml
   *         factory
   */
  public PDPFactory(File configFile) throws ParsingException, UnknownIdentifierException {
    store = new ConfigurationStore(configFile);

    // xxx: use defaults now; may be provide ability to choose factories later
    store.useDefaultFactories();
    defaultPDPConfig = store.getDefaultPDPConfig();
  }

  /**
   * Returns the PDP corresponding to the default configuration.
   *
   * @return Returns a PDP instance
   *
   * @throws UnknownIdentifierException when an unknown identifier was used in a standard xacml
   *         factory
   */
  public PDP getDefaultPDP() throws UnknownIdentifierException {
    synchronized (this) {
      if (defaultPDP == null)
        defaultPDP = new TopazPDP(defaultPDPConfig);

      return defaultPDP;
    }
  }

  /**
   * Returns a PDP corresponding to the named configuration.
   *
   * @param name Configuration name
   *
   * @return Returns a PDP instance.
   *
   * @throws UnknownIdentifierException when an unknown identifier was used in a standard xacml
   *         factory
   */
  public PDP getPDP(String name) throws UnknownIdentifierException {
    synchronized (this) {
      PDP pdp = (PDP) pdpMap.get(name);

      if (pdp == null) {
        PDPConfig config = store.getPDPConfig(name);

        if (config == defaultPDPConfig)
          return getDefaultPDP();

        pdp = new TopazPDP(config);
        pdpMap.put(name, pdp);
      }

      return pdp;
    }
  }

  /**
   * Returns a PDP configuration file. Looks for a name of the config file in the following order:
   * 
   * <ul>
   * <li>
   * Servlet Init Parameter <code>com.sun.xacml.PDPConfigFile</code>
   * </li>
   * <li>
   * System property <code>com.sun.xacml.PDPConfigFile</code>
   * </li>
   * </ul>
   * 
   * If a name is not found, a default name of {@link #DEFAULT_PDP_CONFIG} is used.
   * 
   * <p>
   * The name is first looked up relative to the web-app context and then in the local file system.
   * If found in the web-app context, it is copied to the temp directory for the web-app  (to
   * support containers that do not expand a war file).
   * </p>
   *
   * @param servletContext The web application context
   *
   * @return the PDP configuration File
   *
   * @throws IOException when there is an error in accessing the resource from the web-app context
   * @throws FileNotFoundException when a configuration file could not be located
   */
  public static File getPDPConfigFile(ServletContext servletContext)
                               throws IOException {
    String name = servletContext.getInitParameter(ConfigurationStore.PDP_CONFIG_PROPERTY);

    if (name == null)
      name = System.getProperty(ConfigurationStore.PDP_CONFIG_PROPERTY);

    if (name == null)
      name = DEFAULT_PDP_CONFIG;

    File f;

    // Try web-app first
    InputStream is = servletContext.getResourceAsStream(name);

    if (is == null) {
      // No. See if a file with that name exists
      f = new File(name);

      if (f.exists())
        return f;

      f = null;
    } else {
      File tmp = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
      f = new File(tmp, "PDPConfig.xml");

      OutputStream os = new FileOutputStream(f);

      int          b;

      while ((b = is.read()) != -1)
        os.write(b);

      is.close();
      os.close();
    }

    if (f != null)
      return f;

    // No configuration. Find out why and throw appropriate exception.
    if (servletContext.getInitParameter(ConfigurationStore.PDP_CONFIG_PROPERTY) != null)
      throw new FileNotFoundException("Can not find a file or resource named '" + name
                                      + "' specified for '"
                                      + ConfigurationStore.PDP_CONFIG_PROPERTY
                                      + "' in WEB-INF/web.xml");

    if (System.getProperty(ConfigurationStore.PDP_CONFIG_PROPERTY) != null)
      throw new FileNotFoundException("Can not find a file or resource named '" + name
                                      + "' specified for '"
                                      + ConfigurationStore.PDP_CONFIG_PROPERTY
                                      + "' in System property");

    throw new FileNotFoundException(ConfigurationStore.PDP_CONFIG_PROPERTY
                                    + " must be configured in the WEB-INF/web.xml OR"
                                    + " must be made available as a system property OR "
                                    + DEFAULT_PDP_CONFIG + " must exist.");
  }

  public static class TopazPDP extends PDP {
    private AttributeFinder attrFinder;

    public TopazPDP(PDPConfig config) {
      super(config);
      attrFinder = config.getAttributeFinder();
    }

    public AttributeFinder getAttributeFinder() {
      return attrFinder;
    }
  }
}
