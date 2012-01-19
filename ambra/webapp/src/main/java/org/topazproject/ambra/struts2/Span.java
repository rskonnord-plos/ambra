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

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Target;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Struts action that are marked with this annotation signal to TransactionInterceptor that it
 * needs to wrap transaction around action invocation.
 * <p/>
 * Use when you want transaction to span beyond action and into results. Transaction parameters are
 * specified in the nested Transactional annotation. Can be applied to whole Action class or
 * to individual methods.
 *
 * @see TransactionInterceptor
 * @author Dragisa Krsmanovic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Span {

  Transactional value();
  
}
