/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.struts2;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import org.plos.configuration.ConfigurationStore;
import org.plos.web.VirtualJournalContext;
import org.plos.web.VirtualJournalMappingFilter;

/**
 * Custom WebWork ActionMapper.
 *
 * Map friendly URIs, e.g. "/article/feed" to WebWork actions w/o WebWorks URIs, "/article/articleFeed.action?parms"
 *
 * @author Jeff Suttor
 *
 */
public class PlosOneActionMapper extends DefaultActionMapper {

  /** Pub Configuration */
  private static final org.apache.commons.configuration.Configuration PUB_CONFIG =
    ConfigurationStore.getInstance().getConfiguration();
  private final String PUB_APP_CONTEXT = PUB_CONFIG.getString("pub.app-context", "");

  private static final Log log = LogFactory.getLog(PlosOneActionMapper.class);

  /**
   * @see DefaultActionMapper#getMapping(HttpServletRequest, ConfigurationManager).
   */
  public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {

    final String origUri = request.getRequestURI();
    ActionMapping actionMapping = null;

    // do not care about "null"
    if (origUri == null) {
      return super.getMapping(request, configManager);
    }

    // does a virtual journal context exist?
    final VirtualJournalContext virtualJournalContext = (VirtualJournalContext) request
      .getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    final String mappingPrefix;
    if (virtualJournalContext != null) {
      mappingPrefix = virtualJournalContext.getMappingPrefix();
    } else {
      mappingPrefix = null;
    }
    if (mappingPrefix == null || mappingPrefix.length() == 0) {
      // no override in effect, use default
      actionMapping = super.getMapping(request, configManager);
      if (log.isDebugEnabled()) {
        log.debug("no mappingPrefix, using default action: " + debugActionMapping(actionMapping));
      }
    } else {
      // look for an override Action in the mappingPrefix namespace
      // will need to examine Request Path Elements, get virtualized URI values
      final String[] virtualizedValues = virtualJournalContext.virtualizeUri(
        request.getContextPath(), request.getServletPath(), request.getPathInfo());
      final String virtualContextPath = virtualizedValues[0];
      final String virtualServletPath = virtualizedValues[1];
      final String virtualPathInfo    = virtualizedValues[2];
      final String virtualRequestUri  = virtualizedValues[3];

      // get an ActionMapping for the re-written URI
      actionMapping = super.getMapping(VirtualJournalMappingFilter.wrapRequest(request,
        virtualContextPath, virtualServletPath, virtualPathInfo, virtualRequestUri), configManager);

      // does created ActionMapping actually exist in config?
      if (actionMapping != null) {
        boolean actionMappingExists = false;

        final Configuration configuration = configManager.getConfiguration();
        final Map<String, PackageConfig> packageConfigs = configuration.getPackageConfigs();
        for (final PackageConfig packageConfig : packageConfigs.values()) {
          // is package in our namespace?
          if (!packageConfig.getNamespace().equals(actionMapping.getNamespace())) {
            continue;
          }

          // does package contain this action?
          final Map<String, ActionConfig> actionConfigs = packageConfig.getAllActionConfigs();
          if (actionConfigs.containsKey(actionMapping.getName())) {
            actionMappingExists = true;
            break;
          }
        }

        if (!actionMappingExists) {
          actionMapping = null;
        }
      }

      // if no override, use default
      if (actionMapping == null) {
        // get defaulted URI values
        final String[] defaultedValues = virtualJournalContext.defaultUri(
          request.getContextPath(), request.getServletPath(), request.getPathInfo());
        final String defaultContextPath = defaultedValues[0];
        final String defaultServletPath = defaultedValues[1];
        final String defaultPathInfo    = defaultedValues[2];
        final String defaultRequestUri  = defaultedValues[3];

        actionMapping = super.getMapping(VirtualJournalMappingFilter.wrapRequest(request,
          defaultContextPath, defaultServletPath, defaultPathInfo, defaultRequestUri), configManager);
        if (log.isDebugEnabled()) {
          log.debug("using default action for mappingPrefix: " + mappingPrefix
            + ", action: " + debugActionMapping(actionMapping));
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("override action for mappingPrefix: " + mappingPrefix
            + ", action: " + debugActionMapping(actionMapping));
        }
      }
    }

    // ATOM feed hook: only care about "/article/feed"
    // will factor out with comprehensive REST URI mapping
    if (origUri.startsWith(PUB_APP_CONTEXT + "/article/feed")) {
      return mapUriToAction();
    }

    return actionMapping;
  }

  /**
   * @see DefaultActionMapper#getUriFromActionMapping(ActionMapping).
   */
  public String getUriFromActionMapping(ActionMapping mapping) {

    // only care about /article/feed
    if ("getFeed".equals(mapping.getName())
      && "/article/feed".equals(mapping.getNamespace())
      && "execute".equals(mapping.getMethod())) {
      return(PUB_APP_CONTEXT + "/article/feed");
    }

    // use default
    return super.getUriFromActionMapping(mapping);
 }

  /**
   * Map URIs that start with /article/feed to the getFeed action.
   *
   * @return ActionMapping for getFeed.
   */
 private ActionMapping mapUriToAction() {

   // placeholder for real REST URIs

   // TODO: possible to use common config?
   HashMap<String, String> parms = new HashMap();
   parms.put("feedName", "wireFeed");  // parms passed as null, for now

   return new ActionMapping(
     "getFeed",                              // name
     "/article/feed",                        // namespace
     "execute",                              // method
     null);                                  // parms
 }

 /**
  * Create a debug String from  an ActionMapping.
  */
 private String debugActionMapping(final ActionMapping actionMapping) {

   if (actionMapping == null) {
     return null;
   }

   return actionMapping.getName() + "#" + actionMapping.getMethod()
    + "(" + actionMapping.getParams() + ") in namespace " + actionMapping.getNamespace();
 }
}
