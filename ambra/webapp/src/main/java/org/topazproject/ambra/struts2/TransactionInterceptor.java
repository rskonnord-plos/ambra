/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.topazproject.ambra.struts2;

import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionProxy;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.beans.factory.annotation.Required;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * Struts interceptor that will wrap a Spring managed transaction around action and result.
 * Transaction parameters are specified in nested annotation.
 * <p/>
 * Use when you want transaction to span beyond your action method into result.
 *
 * @author Dragisa Krsmanovic
 */
public class TransactionInterceptor extends AbstractInterceptor {
  private static final Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

  private PlatformTransactionManager txManager;

  public String intercept(final ActionInvocation actionInvocation) throws Exception {

    Action action = (Action) actionInvocation.getAction();
    ActionProxy actionProxy = actionInvocation.getProxy();
    String methodName = actionProxy.getMethod();

    Span span = getAnnotation(action.getClass(), methodName, Span.class);
    if (span == null) {
      return actionInvocation.invoke();
    }

    if (log.isDebugEnabled())
      log.debug("Interceped " + action.getClass().getName() + "." + methodName + "(...)");

    final Transactional transactional = span.value();

    TransactionTemplate txTemplate = new TransactionTemplate(txManager);
    txTemplate.setReadOnly(transactional.readOnly());
    txTemplate.setTimeout(transactional.timeout());
    txTemplate.setIsolationLevel(transactional.isolation().value());
    txTemplate.setPropagationBehavior(transactional.propagation().value());

    CallbackResult callbackResult = (CallbackResult) txTemplate.execute(new TransactionCallback() {
      public CallbackResult doInTransaction(TransactionStatus transactionStatus) {
        CallbackResult result = new CallbackResult();
        try {
          result.setResult(actionInvocation.invoke());
        } catch (Exception e) {
          /* 
           * Callback does not throw exception. We need to pass Exception object in the return
           * parameter so we can throw it in the calling method.
           */
          boolean noRollback = false;

          if (transactional.noRollbackFor() != null) {
            for (Class<? extends Throwable> exception : transactional.noRollbackFor()) {
              if (exception.isInstance(e)) {
                noRollback = true;
                break;
              }
            }
          }

          if (!noRollback && transactional.rollbackFor() != null) {
            for (Class<? extends Throwable> exception : transactional.rollbackFor()) {
              if (exception.isInstance(e)) {
                transactionStatus.setRollbackOnly();
                break;
              }
            }
          }
          result.setException(e);
        }
        return result;
      }
    });

    if (callbackResult.getException() != null)
      throw callbackResult.getException();

    return callbackResult.getResult();
  }

  private <A extends Annotation> A getAnnotation(Class<? extends Action> actionClass,
    String methodName, Class<A> annotationType) throws Exception {
    A annotation = actionClass.getAnnotation(annotationType);
    if (annotation == null) {
      annotation = getMethodAnnotation(actionClass, methodName, annotationType);
    }

    return annotation;
  }

  private <A extends Annotation> A getMethodAnnotation(Class<? extends Action> actionClass,
    String methodName, Class<A> annotationType) {
    try {
      Method method = actionClass.getDeclaredMethod(methodName);
      A annotation = method.getAnnotation(annotationType);
      if (annotation == null) {
        Class parent = actionClass.getSuperclass();
        if (Action.class.isAssignableFrom(parent)) {
          annotation = getMethodAnnotation((Class<? extends Action>) parent,
              methodName, annotationType);
        }
      }
      return annotation;
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Spring setter method. Sets Spring transaction manager
   *
   * @param txManager Transaction manager
   */
  @Required
  public void setTxManager(PlatformTransactionManager txManager) {
    this.txManager = txManager;
  }

  /**
   * Return value from TransactionTemplate callback. Encapsulates possible Exception.
   */
  private static class CallbackResult {

    private String result;
    private Exception exception;

    public String getResult() {
      return result;
    }

    public void setResult(String result) {
      this.result = result;
    }

    public Exception getException() {
      return exception;
    }

    public void setException(Exception exception) {
      this.exception = exception;
    }
  }
}
