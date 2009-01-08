#!/usr/bin/sh
## $Id$ ##
#$1 version
#$2 staging repo path on p.a.o

set -x
if test $# -ne 2 
then
  echo 'usage release-script.sh version stagingRepoPath on p.a.o'
  exit
fi
export VER=$1
export STAGE_REPO=$2

cd /www/people.apache.org/builds/continuum/$1

find $STAGE_REPO -name "*.zip*"    -exec cp {} . \;
find $STAGE_REPO -name "*.tar.gz*" -exec cp {} . \;
find $STAGE_REPO -name "*.war*"    -exec cp {} . \;
find . -name "*.asc.*" -exec rm {} \;

mv continuum-$VER.zip apache-continuum-$VER-src.zip
mv continuum-$VER.zip.md5 apache-continuum-$VER-src.zip.md5
mv continuum-$VER.zip.sha1 apache-continuum-$VER-src.zip.sha1
mv continuum-$VER.zip.asc apache-continuum-$VER-src.zip.asc

mv continuum-jetty-$VER-bin.tar.gz apache-continuum-$VER-bin.tar.gz
mv continuum-jetty-$VER-bin.tar.gz.md5 apache-continuum-$VER-bin.tar.gz.md5
mv continuum-jetty-$VER-bin.tar.gz.sha1 apache-continuum-$VER-bin.tar.gz.sha1
mv continuum-jetty-$VER-bin.tar.gz.asc apache-continuum-$VER-bin.tar.gz.asc

mv continuum-jetty-$VER-bin.zip apache-continuum-$VER-bin.zip
mv continuum-jetty-$VER-bin.zip.md5 apache-continuum-$VER-bin.zip.md5
mv continuum-jetty-$VER-bin.zip.sha1 apache-continuum-$VER-bin.zip.sha1
mv continuum-jetty-$VER-bin.zip.asc apache-continuum-$VER-bin.zip.asc

mv continuum-webapp-$VER.war apache-continuum-$VER.war
mv continuum-webapp-$VER.war.asc apache-continuum-$VER.war.asc
mv continuum-webapp-$VER.war.md5 apache-continuum-$VER.war.md5
mv continuum-webapp-$VER.war.sha1 apache-continuum-$VER.war.sha1

echo 'GREAT :-) '
