#!/usr/local/bin/bash
## $Id$ ##
#$1 version
#$2 staging repo path on p.a.o

set -x
if test $# -ne 2 
then
  echo 'usage release-script.sh version stagingRepoUrl'
  exit
fi
export version=$1
export repo=$2

mkdir /www/people.apache.org/builds/continuum/$1
cd /www/people.apache.org/builds/continuum/$1

mkdir binaries
(
cd binaries
for i in tar.gz tar.gz.asc tar.gz.md5 tar.gz.sha1 zip zip.asc zip.md5 zip.sha1
do
  wget -O apache-continuum-$version-bin.$i $repo/org/apache/continuum/continuum-jetty/$version/continuum-jetty-$version-bin.$i
  if [ ! -f apache-continuum-$version-bin.$i ]; then
    echo Unable to find apache-continuum-$version-bin.$i
    exit 1
  fi
done

for i in tar.gz tar.gz.asc tar.gz.md5 tar.gz.sha1 zip zip.asc zip.md5 zip.sha1
do
  wget -O apache-continuum-buildagent-$version-bin.$i $repo/org/apache/continuum/continuum-buildagent-jetty/$version/continuum-buildagent-jetty-$version-bin.$i
  if [ ! -f apache-continuum-buildagent-$version-bin.$i ]; then
    echo Unable to find apache-continuum-buildagent-$version-bin.$i
    exit 1
  fi
done

for i in war war.asc war.md5 war.sha1
do
  wget -O apache-continuum-$version.$i $repo/org/apache/continuum/continuum-webapp/$version/continuum-webapp-$version.$i
  if [ ! -f apache-continuum-$version.$i ]; then
    echo Unable to find apache-continuum-$version.$i
    exit 1
  fi
done

for i in war war.asc war.md5 war.sha1
do
  wget -O apache-continuum-buildagent-$version.$i $repo/org/apache/continuum/continuum-webapp/$version/continuum-buildagent-webapp-$version.$i
  if [ ! -f apache-continuum-buildagent-$version.$i ]; then
    echo Unable to find apache-continuum-buildagent-$version.$i
    exit 1
  fi
done

for i in zip zip.asc zip.md5 zip.sha1
do
  wget -O apache-continuum-$version-docs.$i $repo/org/apache/continuum/continuum-docs/$version/continuum-docs-$version-docs.$i
  if [ ! -f apache-continuum-$version-docs.$i ]; then
    echo Unable to find apache-continuum-$version-docs.$i
    exit 1
  fi
done
)

mkdir source
(
cd source
for i in zip zip.asc zip.md5 zip.sha1
do
  wget -O apache-continuum-$version-src.$i $repo/org/apache/continuum/continuum/$version/continuum-$version-src.$i
  if [ ! -f apache-continuum-$version-src.$i ]; then
    echo Unable to find apache-continuum-$version-src.$i
    exit 1
  fi
done
)

echo 'GREAT :-) '
