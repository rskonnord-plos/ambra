/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Utility class for generating random tokens. Code copied from org.apache.struts2.util.TokenHelper
 *
 * @author Alex Kudlick 9/24/12
 */
public class TokenGenerator {
  private static final SecureRandom RANDOM = new SecureRandom();


  private TokenGenerator() {
    //not instantiable
  }

  public static String getUniqueToken() {
    return new BigInteger(165, RANDOM).toString(36).toUpperCase();
  }

}
