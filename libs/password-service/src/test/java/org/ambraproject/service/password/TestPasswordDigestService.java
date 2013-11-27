/* $HeadURL::                                                                            $
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

package org.ambraproject.service.password;

import junit.framework.TestCase;

import java.util.Random;

public class TestPasswordDigestService extends TestCase {

  public void testHashingShouldGiveDifferentReturnValue() {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random(0);

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++) {
        final char ch = (char) (64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword = passwordDigestService.generateDigest(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(digestPassword));
      assertTrue(passwordDigestService.verifyPassword(originalPassword, digestPassword));
    }
  }

  public void testVerifyPassportService() {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();
    //Quick check of the password service with a value from the database copied over
    final String expected = "6584abbf44d354572af470f6de0d48c11d595968636b75b38006e5a60043b6641aeba7";
    final String password = "fedoraAdmin";
  }

  public void testVerificationShouldFailForWrongPassword() {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random(0);

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++) {
        final char ch = (char) (64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword = passwordDigestService.generateDigest(originalPassword);
      assertFalse(originalPassword.equalsIgnoreCase(digestPassword));
      assertFalse(passwordDigestService.verifyPassword(originalPassword + "1", digestPassword));
    }
  }

  public void testHashingOfSameStringShouldGiveDifferentResult()
      throws InterruptedException {
    final PasswordDigestService passwordDigestService = getPasswordDigestService();

    final Random random = new Random(0);

    for (int count = 0; count < 100; count++) {
      final StringBuilder sb = new StringBuilder();
      for (int length = 1; length < random.nextInt(20); length++) {
        final char ch = (char) (64 + random.nextInt(60));
        sb.append(ch);
      }

      final String originalPassword = sb.toString();
      final String digestPassword1 = passwordDigestService.generateDigest(originalPassword);
      Thread.sleep(40);
      final String digestPassword2 = passwordDigestService.generateDigest(originalPassword);
      assertFalse(digestPassword1.equalsIgnoreCase(digestPassword2));

    }
  }

  private PasswordDigestService getPasswordDigestService() {
    return new PasswordDigestService(new Random(0));
  }
}
