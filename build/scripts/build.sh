#!/usr/bin/env bash
# $HeadURL::                                                                                      $
# $Id$
#
# Copyright (c) 2006-2008 by Topaz, Inc.
# http://topazproject.org
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# WARNING:
#  mvn must be in path and configured properly
#  JAVA_HOME must be set appropriately

# TODO: Subroutines to stop servers. Check to see if running. If so and fail to stop, show errors.
# TODO: Consider starting env ourselves for integrationtests instead of -Pit-startenv
# TODO: Prefix all logs w/something easy to search on
# TODO: Copy failed surefire reports to stdout/err
# TODO: Command line args to select what part of build to do? (for testing)

[ -z "$MVN" ] && MVN=mvn
[ -z "$JDK" ] && JDK=/home/tools/jdk

MVN_FIRST_FAILURE=$HOME/.m2/first_failure
MVN_LAST_SUCCESS=$HOME/.m2/last_success
MVN_LAST_BUILD=$HOME/.m2/last_build
FIRST_FAILURE=`cat ${MVN_FIRST_FAILURE} 2>/dev/null`
LAST_SUCCESS=`cat ${MVN_LAST_SUCCESS} 2>/dev/null`
LAST_BUILD=`cat ${MVN_LAST_BUILD} 2>/dev/null`

# Don't exit if we get a meaningless error
set +e

SVNVERSION=`svnversion`
MVNARGS="-Dsvnversion=${SVNVERSION} -U"

mvn --version
java -version
"$JDK" 1.6 java -version
echo "pwd: "`pwd`
[ -e ${MVN_FIRST_FAILURE} ] && echo "Initial failure: ${FIRST_FAILURE}"
echo "svnversion: r$SVNVERSION (last success: r$LAST_SUCCESS / last build: r$LAST_BUILD)"
echo "svn info and recent changes"
svn info
svn log -rBASE:{`date "+%Y-%m-%d"`}

# Remove all old rpm packages to not eat up disk space
find $HOME/.m2/repository/org/topazproject/packages -name "*.rpm" -exec rm {} \; -print

rm -f plos/it/install/*/installed
set -e

# compile under 1.5 and 1.6, and build packages under 1.5
#${MVN} ${MVNARGS} -Pit clean install --batch-mode

"$JDK" 1.6 env MAVEN_OPTS=-XX:MaxPermSize=128m ${MVN} ${MVNARGS} clean install --batch-mode
N6=$?

${MVN} ${MVNARGS} clean install --batch-mode -Ppackages,rpm,distribution
N5=$?

[[ $N5 != 0 || $N6 != 0 ]] && N=1 || N=0

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
