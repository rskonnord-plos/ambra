/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.ambraproject;

import org.ambraproject.migrationtests.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to manually run the migration tests outside of the Maven Surefire plugin.  The reason we need to do this is
 * maven tends to run out of memory when building the html reports for the migration tests, so here we just log test
 * failures and keep going.  Test Failures are logged with the info level, and start with the string &quot;TEST
 * FAILURE&quot;  Only errors in running the tests are logged with error level.
 *
 * @author Alex Kudlick Date: 6/24/11
 *         <p/>
 *         org.ambraproject
 */
public class RunMigrationTests {
  private static final Logger log = LoggerFactory.getLogger(RunMigrationTests.class);
  private static final Map<String, ClassMetadata> hibernateMetadata = new Configuration().configure()
      .buildSessionFactory().getAllClassMetadata();

  private static final Class[] TEST_CLASSES = new Class[]{
      AggregationMigrationTest.class, AnnotationMigrationTest.class, ArticleMigrationTest.class,
      CitationMigrationTest.class, ReplyMigrationTest.class, RepresentationMigrationTest.class,
      UserAccountMigrationTest.class, UserProfileMigrationTest.class};
  private static final String MIGRATION_TEST_PACKAGE = "org.ambraproject.migrationtests.";


  public static void main(String[] args) {
    if (args.length == 0) {
      log.info("Running all test classes");
      for (Class clazz : TEST_CLASSES) {
        try {
          testClass(clazz);
        } catch (Exception e) {
          log.error("Failure running test class: " + clazz.getSimpleName(), e);
        }
      }
    } else {
      for (String className : args) {
        try {
          Class<?> clazz;
          try {
            clazz = Class.forName(className);
          } catch (ClassNotFoundException e) {
            clazz = Class.forName(MIGRATION_TEST_PACKAGE + className);
          }
          testClass(clazz);
        } catch (Exception e) {
          log.error("Failure running test class: " + className, e);
        }
      }
    }
  }

  /**
   * Return a simple string description of a hibernate object
   *
   * @param object - the object to describe
   * @return - a string describing the hibernate object: class + " " + id
   * @throws Exception - from reflection calls
   */
  private static String getDescription(Object object) throws Exception {
    String idProperty = hibernateMetadata.get(object.getClass().getName()).getIdentifierPropertyName();
    Method getter = object.getClass().getMethod(
        "get" + idProperty.substring(0, 1).toUpperCase() + idProperty.substring(1));
    return object.getClass().getSimpleName() + " " + getter.invoke(object);
  }

  /**
   * Run the test methods in a class.  This methods makes assumptions about the test methods in the class that only work
   * because we know our migration tests (for instance, that every test method has a data provider, that every
   * dataprovider returns an iterator)
   *
   * @param clazz - the class to test
   * @throws Exception - if there's a problem running the test methods - NOT if there is a test failure.  Those will
   *                   simply be logged
   */
  @SuppressWarnings("unchecked")
  private static void testClass(Class clazz) throws Exception {
    Object testInstance = clazz.newInstance();
    ((BaseMigrationTest) testInstance).initServices();
    log.info("Running test class: " + clazz.getSimpleName());
    long totalRun = 0;
    long failures = 0;
    long skipped = 0;

    for (Method testMethod : getTestMethods(clazz)) {
      log.info("Running test method " + testMethod.getName());
      Method dataProvider = getDataProvider(clazz, testMethod);

      Iterator<Object[]> iterator;
      if (dataProvider.getParameterTypes() == null
          || dataProvider.getParameterTypes().length == 0) {
        iterator = (Iterator<Object[]>) dataProvider.invoke(testInstance);
      } else {
        //Some of the data providers take the test method as argument
        iterator = (Iterator<Object[]>) dataProvider.invoke(testInstance, testMethod);
      }
      long invocationCount = 0;
      while (iterator.hasNext()) {
        invocationCount++;
        Object[] row;
        try {
          row = iterator.next();
        } catch (Exception e) {
          log.error("Error invoking dataprovider for method: " + testMethod.getName() +
              " on invocation number: " + invocationCount);
          skipped++;
          continue;
        }
        try {
          testMethod.invoke(testInstance, row);
        } catch (Exception e) {
          failures++;
          logTestFailure(row[0], e, testMethod);
        }
      }
      totalRun += invocationCount;
      log.info("Finished Running method " + testMethod.getName());
    }
    ((BaseMigrationTest) testInstance).closeServices();
    log.info("Finished " + clazz.getSimpleName() +
        ": Tests Passed: " + (totalRun - failures - skipped) +
        " Tests Failed: " + failures + " Tests Skipped: " + skipped);
  }

  /**
   * Log a test failure
   *
   * @param object     - the hibernate object for the test that failed
   * @param e          - the Exception from the test
   * @param testMethod - the test method that failed
   * @throws Exception - if there's a problem logging the message
   */
  private static void logTestFailure(Object object, Exception e, Method testMethod) throws Exception {
    String message = "TEST FAILURE for method " + testMethod.getName();
    if (object != null) {
      message += " on " + getDescription(object);
    }
    log.info(message + "; " + createLogMessage(e));
  }

  /**
   * Create a string to log from an exception that caused a test failure
   *
   * @param e - the exception from the test failure (almost always should be InvocationTargetException wrapped around
   *          the true problem)
   * @return - a message to log
   */
  private static String createLogMessage(Throwable e) {
    String assertionErrorMessage = getAssertionErrorMessage(e);
    if (!assertionErrorMessage.isEmpty()) {
      return assertionErrorMessage;
    }
    Throwable cause = e;
    //get rid of the InvocationTargetExceptions, they just wrap the real failure
    while ((cause != null && cause instanceof InvocationTargetException)) {
      cause = cause.getCause();
    }
    if (cause != null) {
      return cause.getClass().getName() + ": " + cause.getMessage();
    }
    return "";
  }

  /**
   * Get the message from an Exception that was caused by an assertion error.
   *
   * @param e - the exception to look through
   * @return the message from the root assertion error if there is one, else an empty string
   */
  private static String getAssertionErrorMessage(Throwable e) {
    if (e instanceof AssertionError) {
      return e.getMessage();
    } else if (e.getCause() != null) {
      return getAssertionErrorMessage(e.getCause());
    } else {
      return "";
    }
  }

  /**
   * Get all the methods annotated with org.testng.annotations.Test from the class
   *
   * @param clazz - the test class
   * @return - a list of all the test methods in the class
   */
  private static List<Method> getTestMethods(Class clazz) {
    List<Method> testMethods = new LinkedList<Method>();
    for (Method method : clazz.getMethods()) {
      if (method.getAnnotation(Test.class) != null) {
        testMethods.add(method);
      }
    }
    return testMethods;
  }

  /**
   * Get the dataprovider for a given test method
   *
   * @param clazz      - the test class
   * @param testMethod - the test method
   * @return - the dataprovider specified in the test methods @Test annotation
   */
  private static Method getDataProvider(Class clazz, Method testMethod) {
    String dataProviderName = testMethod.getAnnotation(Test.class).dataProvider();
    for (Method method : clazz.getMethods()) {
      if (method.getAnnotation(DataProvider.class) != null
          && method.getAnnotation(DataProvider.class).name().equals(dataProviderName)) {
        return method;
      }
    }
    return null;
  }

}
