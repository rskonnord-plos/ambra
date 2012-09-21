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

import freemarker.template.TemplateException;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Tests for VersionedFileDirectiveTest
 */
public class VersionedFileDirectiveTest {

  private static class VersionedFileDirectiveForTest extends VersionedFileDirective {

    @Override
    public String getLink(String filename, String fingerprint, Map params) throws TemplateException {
      return null;
    }
  }

  @Test
  public void testGetFingerprint() throws Exception {
    File f = File.createTempFile("testdata", ".txt");
    f.deleteOnExit();
    BufferedWriter out = new BufferedWriter(new FileWriter(f));
    out.write("This is a test\n");
    out.close();

    VersionedFileDirective vfd = new VersionedFileDirectiveForTest();
    assertEquals(vfd.getFingerprint(f.getAbsolutePath()), "PBuwzV1n3dwC+uUL9W06Oky8cgQ");

    // Check again since the value should be in the cache.
    assertEquals(vfd.getFingerprint(f.getAbsolutePath()), "PBuwzV1n3dwC+uUL9W06Oky8cgQ");

    // Append to the file; fingerprint should change (have to create a new VersionedFileDirective because of caching).
    out = new BufferedWriter(new FileWriter(f, true));  // true == append
    out.write("foo\n");
    out.close();
    vfd = new VersionedFileDirectiveForTest();
    assertEquals(vfd.getFingerprint(f.getAbsolutePath()), "0Eg9JYTa0RKSr3ypJplKU9DrCuA");
  }
}
