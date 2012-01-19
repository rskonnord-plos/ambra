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

mvn --version
java -version
echo "pwd: "`pwd`
[ -e ${MVN_FIRST_FAILURE} ] && echo "Initial failure: ${FIRST_FAILURE}"
echo "svnversion: r$SVNVERSION (last success: r$LAST_SUCCESS / last build: r$LAST_BUILD)"
echo "svn info and recent changes"
svn info
svn log -rBASE:{`date "+%Y-%m-%d"`}

rm -f plos/it/install/*/installed
set -e
#${MVN} ${MVNARGS} -Pit clean install --batch-mode
${MVN} ${MVNARGS} clean install --batch-mode -Ppackages,rpm,distribution
N=$?

# Build site info
if [ ${N} -eq 0 ]; then
  echo "Build site"
  ${MVN} ${MVNARGS} -Preports site-deploy
  N=$?
fi

# Update last build #
echo ${SVNVERSION} > ${MVN_LAST_BUILD}

# Deploy the OTM jars
if [ ${N} -eq 0 ]; then
  echo "Deploying jars"
  for pkg in "." "mulgara" "mulgara/string-compare-resolver" "mulgara/mulgara-client" "topaz" "topaz/core" ; do
    ${MVN} ${MVNARGS} -N -f "$pkg/pom.xml" -Dmaven.test.skip=true deploy
  done
fi

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
