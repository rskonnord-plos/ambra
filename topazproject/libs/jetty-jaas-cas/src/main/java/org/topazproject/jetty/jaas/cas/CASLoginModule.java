/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
/*
 *  Copyright (c) 2000-2003 Yale University. All rights reserved.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 *  DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 *  DAMAGE.
 *
 *  Redistribution and use of this software in source or binary forms,
 *  with or without modification, are permitted, provided that the
 *  following conditions are met:
 *
 *  1. Any redistribution must include the above copyright notice and
 *  disclaimer and this list of conditions in any related documentation
 *  and, if feasible, in the redistributed software.
 *
 *  2. Any redistribution must include the acknowledgment, "This product
 *  includes software developed by Yale University," in any related
 *  documentation and, if feasible, in the redistributed software.
 *
 *  3. The names "Yale" and "Yale University" must not be used to endorse
 *  or promote products derived from this software.
 */

package org.topazproject.jetty.jaas.cas;

import edu.yale.its.tp.cas.client.ProxyTicketValidator;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.mortbay.jaas.callback.RequestParameterCallback;
import org.mortbay.jaas.JAASPrincipal;
import org.mortbay.jaas.JAASRole;


/**
 * This class implements a JAAS <code>LoginModule</code> that defers authentication
 * to CAS. See the 
 * <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jaas/JAASRefGuide.html">
 * JAAS documentation</a> for details about configuration and architecture.
 * <p>
 * The calling application's <code>CallbackHandler</code> MUST return the <strong>ticket</strong> for a 
 * <code>TextInputCallback</code> whose prompt is "ticket".
 * <p>
 * The CAS <strong>service</strong> MAY be hard-coded into the configuration; if it is not,
 * the calling application's <code>CallbackHandler</code> MAY return the <strong>service</strong>
 * in a <code>TextInputCallback</code> whose prompt is "service".
 * <p>
 * The <strong>cas_validate_url</strong> MUST be hard-coded in the configuration 
 * <p>
 * Sample configuration:
 * 
 * <pre>
 * Application
 * {
 *      org.topazproject.jetty.jaas.cas.CASLoginModule sufficient    
 *          cas_validate_url="https://cas.server.edu/cas/serviceValidate"
 *          service="https://my.application.edu/login";				  
 * }
 * 
 * </pre>
 *
 */
public class CASLoginModule implements LoginModule{
    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected String casValidateUrl;
    protected String service;
    protected Principal principal;
    
    /**
     * Initialize the CASLoginModule.
     * @param subject
     * @param callbackHandler
     * @param sharedState
     * @param options can contain <ul>
     *      <li><strong>cas_validate_url</strong> (required)</li>
     *      <li><strong>service</strong> (optional)</li>
     * </ul>
     * 
     */ 
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options){
        this.subject=subject;
        this.callbackHandler=callbackHandler;
        this.casValidateUrl=(String)options.get("cas_validate_url");
        this.service = (String)options.get("service");
    }

    public boolean login() throws LoginException{
        Callback[] callbacks;
        // Jetty's request parameter callback to get "ticket" and "service"
        RequestParameterCallback ticketCallback = new RequestParameterCallback();
        ticketCallback.setParameterName("ticket");
        RequestParameterCallback serviceCallback=null;
        if(service==null || "".equals(service.trim())){
            //the service has not been hardcoded, so give the application
            // a callback which can be used to specify it
            serviceCallback = new RequestParameterCallback();
            serviceCallback.setParameterName("service"); 
            callbacks = new Callback[] {ticketCallback,serviceCallback};
        }
        else{
            callbacks = new Callback[] {ticketCallback};
        }
         
        try{
            callbackHandler.handle(callbacks);
        }
        catch(IOException e){
            throw new LoginException(e.getMessage());
        }
        catch(UnsupportedCallbackException e){
            throw new LoginException(e.getMessage());
        }
        String ticket = getParameterValue(ticketCallback);
        if(ticket!=null && !ticket.trim().equals("")){
            if(serviceCallback!=null){
                service = getParameterValue(serviceCallback); 
            }
            ProxyTicketValidator pv = new ProxyTicketValidator();
            pv.setCasValidateUrl(casValidateUrl);
            if(service!=null &&  !("".equals(service.trim()))){
                pv.setService(service);
            }
            pv.setServiceTicket(ticket);
            try{
                pv.validate();
            }
            catch(IOException e){
                throw new LoginException(e.getMessage());
            }
            catch(SAXException e){
                throw new LoginException(e.getMessage());
            }
            catch(ParserConfigurationException e){
                throw new LoginException(e.getMessage());
            }
            if(pv.isAuthenticationSuccesful()){
                final String name = pv.getUser();
                principal = new Principal(){
                    public String getName(){
                        return name;
                    }
                };
                //authentication successful
                return true;
            }
        }
        //authentication failed
        throw new FailedLoginException("Login failed.");
    }

    public boolean commit() throws LoginException{
        if(principal!=null){
            subject.getPrincipals().add(principal);
            return true;
        }
        return false;
    }

    public boolean abort() throws LoginException{
        if(principal!=null){
            principal = null;
            return true;
        }
        return false;
    }

    public boolean logout() throws LoginException{
        if(principal!=null){
            subject.getPrincipals().remove(principal);
            return true;
        }
        return false;
    }

    private String getParameterValue(RequestParameterCallback callback){
        List values = callback.getParameterValues();
        return (values.size() == 0) ? null : (String)values.get(0);
    }
}

