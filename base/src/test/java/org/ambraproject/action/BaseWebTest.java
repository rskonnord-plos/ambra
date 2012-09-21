/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.ambraproject.Constants;
import org.ambraproject.models.UserProfile;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Base class for tests of Action classes, interceptors, or other tests of the web infrastructure. Provides basic Http
 * and struts config objects.
 * <p/>
 * This class provides some housekeeping via before/after methods:
 * <ol>
 * <li>Before each method, it sets up a user context and sets the default request on an action.  Tests that wish to use an admin context
 * must explicitly call {@link #setupAdminContext()}</li>
 * <li>After each method, it clears all messages stored on the action</li>
 * </ol>
 * <p/>
 * Tests that extend this class will NOT be thread safe. The only way for a test to be thread safe would be to create an instance of the action within each test method
 *
 * @author alex 8/22/11
 */
public abstract class BaseWebTest extends BaseTest {

  /**
   * @return the action object that is being tested, so we can set the request/clear messages on it.  Subclasses are allowed to return null, in which case we won't set anything on it.
   */
  protected abstract BaseActionSupport getAction();

  /**
   * Optional hook for subclasses to override to set additional attributes on the request
   * @param request the request attributes which will be set on the action
   */
  protected void doModifyRequest(Map<String, Object> request){
    //do nothing by default
  }

  /**
   * Optional hook for sublcasses to override to perform actions after context has been setup
   */
  protected void afterContextSetup() {
    //do nothing by default
  }

  /**
   * Set up a default session and request. The current journal will be set to {@link BaseTest#defaultJournal}, and
   * the default user defined by {@link BaseTest#DEFAULT_USER_AUTHID} will be logged in
   */
  @BeforeMethod
  public void setDefaultRequest() {
    setupUserContext();
    if (getAction() != null) {
      Map<String, Object> request = getDefaultRequestAttributes();
      doModifyRequest(request);
      getAction().setRequest(request);
      if (getAction() instanceof SessionAware) {
        ((SessionAware) getAction()).setSession(ActionContext.getContext().getSession());
      }
    }
  }

  /**
   * Clear the session and any messages set on the action
   */
  @AfterMethod
  public void clearMessages() {
    if (getAction() != null) {
      getAction().setActionMessages(new HashSet<String>());
      getAction().setActionErrors(new HashSet<String>());
      getAction().setFieldErrors(new HashMap<String, List<String>>());
    }
    if (ActionContext.getContext() != null && ActionContext.getContext().getSession() != null) {
      ActionContext.getContext().getSession().clear();
    }
  }

  /**
   * Utility method to add items to the test session
   *
   * @param key   the key for the item to add to the session
   * @param value the value to add
   */
  protected final void putInSession(String key, Object value) {
    ActionContext.getContext().getSession().put(key, value);
  }

  /**
   * Utility method to remove objects from the test session
   *
   * @param key the key of the object to remove
   */
  protected final void removeFromSession(String key) {
    ActionContext.getContext().getSession().remove(key);
  }

  /**
   * Utility method to get an item from the session
   *
   * @param key the key of the item to get
   * @return the value of the item in the session, or null if it is not present
   */
  protected final Object getFromSession(String key) {
    return ActionContext.getContext().getSession().get(key);
  }

  /**
   * Insert all the session attributes that would indicate the given user is logged in
   *
   * @param user
   */
  protected final void login(UserProfile user) {
    putInSession(Constants.AMBRA_USER_KEY, user);
    putInSession(Constants.AUTH_KEY, user.getAuthId());
    putInSession(Constants.SINGLE_SIGNON_EMAIL_KEY, user.getEmail());
  }

  /**
   * Set up struts container / context with an admin request.  This is for unit tests that need to access attributes
   * from the session, set up mock action invocations, etc.
   */
  protected final void setupAdminContext() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put(Constants.AUTH_KEY, DEFAULT_ADMIN_AUTHID);

    setupContext(sessionAttributes);
  }

  /**
   * Set up struts container / context with an admin request.  This is for unit tests that need to access attributes
   * from the session, set up mock action invocations, etc.
   */
  protected final void setupUserContext() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put(Constants.AUTH_KEY, DEFAULT_USER_AUTHID);
    setupContext(sessionAttributes);
  }

  /**
   * Set up the {@link ActionContext} for the test.
   *
   * @param sessionAttributes the attributes to put in the session
   */
  protected final void setupContext(Map<String, Object> sessionAttributes) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    for (String attr : sessionAttributes.keySet()) {
      session.setAttribute(attr, sessionAttributes.get(attr));
    }
    request.setSession(session);

    ConfigurationManager configurationManager = new ConfigurationManager();
    configurationManager.addContainerProvider(new XWorkConfigurationProvider());
    Configuration config = configurationManager.getConfiguration();
    Container strutsContainer = config.getContainer();

    ValueStack stack = strutsContainer.getInstance(ValueStackFactory.class).createValueStack();
    stack.getContext().put(ServletActionContext.CONTAINER, strutsContainer);

    ActionContext.setContext(new ActionContext(stack.getContext()));
    ActionContext.getContext().setSession(sessionAttributes);
    ServletActionContext.setContext(ActionContext.getContext());
    ServletActionContext.setRequest(request);
    afterContextSetup();
  }

  /**
   * @return the default request attributes that are set up for action invocations
   */
  public static Map<String, Object> getDefaultRequestAttributes() {
    Map<String, Object> requestAttributes = new HashMap<String, Object>();
    requestAttributes.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT,
        new VirtualJournalContext("journal", "dfltJournal", "http", 80, "localhost",
            "", new ArrayList<String>()));
    return requestAttributes;
  }
}
