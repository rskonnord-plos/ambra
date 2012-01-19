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

# Don't exit if we get a meaningless error
set +e

SVNVERSION=`svnversion`
MVNARGS=-Dsvnversion=${SVNVERSION}

java -version
echo "pwd: "`pwd`
echo "svnversion: $SVNVERSION"
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

# Return build result
exit ${N}
