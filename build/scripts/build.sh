#!/usr/bin/env bash
# $HeadURL::                                                                                      $
# $Id$
#
# Copyright (c) 2006 by Topaz, Inc.
# http://topazproject.org
#
# Licensed under the Educational Community License version 1.0
# http://opensource.org/licenses/ecl1.php

# WARNING:
#  ant must be in path and configured properly (ANT_HOME)
#  mvn must be in path and configured properly
#  JAVA_HOME must be set appropriately

# TODO: Subroutines to stop servers. Check to see if running. If so and fail to stop, show errors.
# TODO: Consider starting env ourselves for integrationtests instead of -Pit-startenv
# TODO: Prefix all logs w/something easy to search on
# TODO: Copy failed surefire reports to stdout/err
# TODO: Command line args to select what part of build to do? (for testing)

[ -z "$MVN" ] && MVN=mvn

[ -z "$TOPAZ_INSTALL_DIR" ] && \
  TOPAZ_INSTALL_DIR=`$MVN help:effective-pom | grep 'topazproject.install.dir' | head -1 | \
                     sed 's/.*>\(.*\)<.*/\1/'`

MVN_REPOSITORY=$HOME/.m2/repository
MVN_REPOSITORY_TOPAZ=${MVN_REPOSITORY}/org/topazproject

MVN_FIRST_FAILURE=$HOME/.m2/first_failure
MVN_LAST_SUCCESS=$HOME/.m2/last_success
MVN_LAST_BUILD=$HOME/.m2/last_build
FIRST_FAILURE=`cat ${MVN_FIRST_FAILURE} 2>/dev/null`
LAST_SUCCESS=`cat ${MVN_LAST_SUCCESS} 2>/dev/null`
LAST_BUILD=`cat ${MVN_LAST_BUILD} 2>/dev/null`

# Don't exit if we get a meaningless error
set +e

SVNVERSION=`svnversion`
MVNARGS=-Dsvnversion=${SVNVERSION}

java -version
echo "pwd: "`pwd`
[ -e ${MVN_FIRST_FAILURE} ] && echo "Initial failure: ${FIRST_FAILURE}"
echo "svnversion: r$SVNVERSION (last success: r$LAST_SUCCESS / last build: r$LAST_BUILD)"
echo "svn info and recent changes"
svn info
svn log -rBASE:{`date "+%Y-%m-%d"`}

echo "Removing potentially stale directory: ${MVN_REPOSITORY_TOPAZ}/{esup*,fedora*}"
rm -rf ${MVN_REPOSITORY_TOPAZ}/{esup*,fedora*}

# Build our ant-tasks first
echo "Building ant-tasks-plugin first"
(cd build/ant-tasks-plugin; ${MVN} ${MVNARGS} install)

echo "Making sure ecqs is stopped: mvn ant-tasks:ecqs-stop"
${MVN} ${MVNARGS} ant-tasks:ecqs-stop   > /dev/null
echo "Making sure fedora is stopped: mvn ant-tasks:fedora-stop"
${MVN} ${MVNARGS} ant-tasks:fedora-stop > /dev/null 2>&1
echo "Making sure mulgara is stopped: mvn ant-tasks:mulgara-stop"
${MVN} ${MVNARGS} ant-tasks:mulgara-stop > /dev/null 2>&1
echo "Making sure search is stopped: mvn ant-tasks:search-stop"
${MVN} ${MVNARGS} ant-tasks:search-stop > /dev/null 2>&1

# Do a build, if it fails, just exit
echo "Running our build: mvn clean install --batch-mode"
set -e
${MVN} ${MVNARGS} clean install --batch-mode
N=$?

echo "Removing potentially stale directory: ${TOPAZ_INSTALL_DIR}"
rm -rf ${TOPAZ_INSTALL_DIR}

# The rest of these things we do whether they succeed or not
# (Actually we care if integration tests fail, but need docs run even if they do)
set +e

# Run integration tests and generate documentation
if [ ${N} -eq 0 ]; then
  echo "Running integration tests: mvn clean -Pit-startenv install --batch-mode"
  export MAVEN_OPTS=-XX:MaxPermSize=128m
  (cd topazproject/integrationtests; ${MVN} ${MVNARGS} -Pit-startenv clean install --batch-mode)
  N=$?

  echo "Creating documentation: cd integrationtests; mvn site-deploy"
  mkdir -p topazproject/integrationtests/target
  RESULTS=`pwd`/topazproject/integrationtests/target/site.build.out.$$
  #rm -rf ${TOPAZ_INSTALL_DIR}/topazdocs
  (cd topazproject/integrationtests; ${MVN} ${MVNARGS} site-deploy --batch-mode 2>&1 >$RESULTS)
  if [ $? -ne 0 ]; then
    echo "Site Build Failed"
    cat $RESULTS
  fi
fi

echo "Stopping ecqs"
${MVN} ${MVNARGS} ant-tasks:ecqs-stop   > /dev/null
echo "Stopping fedora"
${MVN} ${MVNARGS} ant-tasks:fedora-stop > /dev/null 2>&1
echo "Stopping mulgara"
${MVN} ${MVNARGS} ant-tasks:mulgara-stop > /dev/null 2>&1
echo "Making sure search is stopped: mvn ant-tasks:search-stop"
${MVN} ${MVNARGS} ant-tasks:search-stop > /dev/null 2>&1

# Build RPMs if integration tests succeeded
if [ ${N} -eq 0 -a -x /usr/bin/rpmbuild ]; then
  echo "Build RPMs"
  (cd packages; ${MVN} ${MVNARGS} -Prpm clean install --batch-mode)
  N=$?
fi

# Update last build #
echo ${SVNVERSION} > ${MVN_LAST_BUILD}

# Update the last success
if [ ${N} -eq 0 ]; then
  echo "BUILD SUCCEED for r${SVNVERSION}"
  [ -n "${FIRST_FAILURE}" ] && echo "Fixes failure in ${FIRST_FAILURE}"
  echo ${SVNVERSION} > ${MVN_LAST_SUCCESS}
  rm -f ${MVN_FIRST_FAILURE}
else
  echo "BUILD FAILED for r$SVNVERSION (last success: r$LAST_SUCCESS first fail: $FIRST_FAILURE)"
  [ ! -e ${MVN_FIRST_FAILURE} ] && echo "r${SVNVERSION}:${LAST_BUILD}" > ${MVN_FIRST_FAILURE}
fi
echo "See http://gandalf.topazproject.org/trac/log/head?rev=${SVNVERSION}&verbose=on"

# Return build result
exit ${N}
