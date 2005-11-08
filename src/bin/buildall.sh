#!/bin/bash

#  Copyright 2005 The Apache Software Foundation
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# $Rev$ $Date$

#script to build geronimo, tranql, tranql-connector, and openejb in a reasonable order

echo "To deploy use the command line ./buildall.sh [clean] deploy <your apache/codehaus username>"

set -e

MAVEN_REPO=~/.maven/repository

OFFLINE=

TRANQL=tranql/tranql
TRANQLCONNECTOR=tranql/connector
OPENEJB=openejb

if [ "$1" = clean ]; then
  shift
  echo cleaning...
  (cd specs;maven $OFFLINE multiproject:clean) || exit 1
  (if [ -d $TRANQL ]; then cd $TRANQL ; maven $OFFLINE  clean || exit 1; fi)
  (if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven $OFFLINE  clean || exit 1; fi)
  (cd modules; maven $OFFLINE  multiproject:clean) || exit 1
  (cd plugins; maven $OFFLINE multiproject:clean) || exit 1
  (cd applications; maven $OFFLINE multiproject:clean) || exit 1
  (if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven $OFFLINE  multiproject:clean || exit 1; fi)
  (cd modules/assembly; maven $OFFLINE  clean || exit 1)
  (if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven $OFFLINE  clean || exit 1; fi)
  (if [ -d $OPENEJB ]; then cd openejb/modules/itests; maven $OFFLINE clean || exit 1; fi )
fi

echo updating
svn up || exit 1
(if [ -d $TRANQL ]; then cd $TRANQL ; cvs -q up -dP || exit 1; fi)
(if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; cvs -q up -dP || exit 1; fi)
(if [ -d $OPENEJB ]; then cd $OPENEJB; cvs -q up -dP || exit 1; fi)

echo cleaning local repo
rm -rf  $MAVEN_REPO/geronimo || exit 1
rm -rf  $MAVEN_REPO/geronimo-spec || exit 1
if [ -d $TRANQL ]; then rm -rf $MAVEN_REPO/tranql || exit 1; fi
if [ -d $OPENEJB ]; then rm -rf $MAVEN_REPO/openejb || exit 1; fi

OFFLINE=-o

echo building
(cd specs;maven $OFFLINE multiproject:install) || exit 1
(if [ -d $TRANQL ]; then cd $TRANQL ; maven $OFFLINE  jar:install || exit 1; fi)
(if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven $OFFLINE  rar:install || exit 1; fi)
(cd plugins/maven-xmlbeans-plugin; maven -o plugin:install) || exit 1
(cd modules; maven $OFFLINE  multiproject:install) || exit 1
(cd plugins; maven $OFFLINE multiproject:install) || exit 1
(cd applications; maven $OFFLINE multiproject:install) || exit 1
(if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven $OFFLINE  multiproject:install || exit 1; fi)
(cd modules/assembly; maven $OFFLINE  jar:install) || exit 1
(if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven $OFFLINE  jar:install || exit 1; fi)
#(if [ -d $OPENEJB ]; then cd openejb/modules/itests; maven $OFFLINE || exit 1; fi )

if [ "$1" = deploy ]; then
  echo deploying
  (cd specs;maven -o -Duser.name=$2 multiproject:deploy) || exit 1
  (if [ -d $TRANQL ]; then cd $TRANQL ; maven -o -Duser.name=$2 jar:deploy || exit 1; fi)
  (if [ -d $TRANQLCONNECTOR ]; then cd $TRANQLCONNECTOR; maven -o -Duser.name=$2  jar:deploy  rar:deploy || exit 1; fi)
  (cd modules; maven -o -Duser.name=$2  multiproject:deploy) || exit 1
  (if [ -d $OPENEJB ]; then cd $OPENEJB/modules; maven -o -Duser.name=$2   multiproject:deploy || exit 1; fi)
  (cd modules/assembly; maven -o -Duser.name=$2   jar:deploy) || exit 1
  (if [ -d $OPENEJB ]; then cd openejb/modules/assembly; maven -o -Duser.name=$2   jar:deploy || exit 1; fi)
fi
