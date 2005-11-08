#!/bin/bash
#
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

# --------------------------------------------------------------------
# $Rev$ $Date$
# --------------------------------------------------------------------

#  If we need to SSH up to the server where the builds are published
#  These three varaibles will be used to find the host 
REMOTE_HOST=${1:-apache.org}

#  This is where we should place our unstable, non-release builds
#  We aren't labeling the releases by date, rather by svn revision, so
#  we put them in a directory called 'unstable' rather than 'nightly'
#  It's fine as we really don't even need to run this script nightly and
#  can instead opt to run it just when we think we think things are in a 
#  relatively good state.
RELEASE_DIR=${2:-/www/cvs.apache.org/dist/geronimo/unstable}

PROJ=geronimo
BIN_DIR=modules/assembly/target
DIST=$PWD/dist

#  Make the DIST directory if it isn't present
test -d $DIST || mkdir $DIST

#  The public repo url for geronimo
SVN_URL="http://svn.apache.org/repos/asf/$PROJ"

#  Get the current svn revision number for geronimo
#  Example: 123456
SVN_VERSION=$(wget -q -O - $SVN_URL/trunk | grep title | sed 's/[^0-9]//g')

#  The version of geronimo we are building.  In the future we may have nightly builds
#  of 1.0 and 2.0 going at the same time, so it's nice to include that in the build name
VN='1.0'

#  Example value: 1.0-123456
VERSION="$VN-$SVN_VERSION"

#  Example value: geronimo-1.0-123456
RELEASE_ID="$PROJ-$VERSION"

#  Example value: /www/cvs.apache.org/dist/geronimo/unstable/1.0-123456
VERSION_DIR="$RELEASE_DIR/$VERSION"

echo "$RELEASE_ID"
    
### Utility functions ########
function shash { openssl $1 < $2 > $2.$1 ;}
function fail () { echo $1 >&2; exit 1;}
function package () { 
    DEST=$1; SOURCE=$2
    tar czf $DEST.tar.gz $SOURCE
    zip -9rq $DEST.zip $SOURCE
}
function publish_build_archives {

    #  We want to checkout Geronimo into a directory that will be named
    #  just right to be the source directory, then we can just zip and tar
    #  it up before we build it.
    #
    #  The directory will be named geronimo-1.0-SVN_REVISION_NUMBER
    svn export --revision $SVN_VERSION $SVN_URL/trunk $RELEASE_ID
    
    #  Now let's create the source zip and tar before we build while we
    #  still have a completely clean checkout with no target directories,
    #  velocity.log files and other junk created during a build.
    package $DIST/${RELEASE_ID}-src $RELEASE_ID || fail "Unable to create source binaries"

    #  Let's go ahead and run the build to create the geronimo-foo-1.0-SVN_REVISION.jar files
    #  We don't run the tests as this is not a script for testing and reporting those test results.
    #  If the build fails to compile, the 'fail' function is called and this script will exit
    #  and nothing will be published.
    ( cd $RELEASE_ID && maven -o -Dgeronimo_version=$VERSION -Dmaven.{itest,test}.skip=true m:checkout m:build-all ) || fail "Build failed"

    #  During the assembly module a directory called geronimo-1.0-SVN_REVISION was created.  Let's 
    #  move in to that directory and create a geronimo-1.0-SVN_REVISION.zip and a tar.gz of the same name.
    #  When unpacked by users, these archives will extract into a directory called geronimo-1.0-SVN_REVISION/
    ( cd $RELEASE_ID/$BIN_DIR && package $DIST/${RELEASE_ID} $RELEASE_ID ) || fail "Unable to make binary archives"

    #  Let's create checksums for our source and binary tars and zips.
    for archive in $DIST/*.{zip,tar.gz}; do
	echo $archive
	shash md5 $archive
        shash sha $archive
    done || fail "Unable to sign or hash release archives"

    #  Now we want to create a directory where we will put the archives and checksums up for download.
    #  Here we setup some variables for use.  The VERSION_DIR will typically look like:
    #     /www/cvs.apache.org/dist/geronimo/unstable/1.0-SVN_REVISION/
    #  This is the directory on apache.org where non-release builds a placed.
    VERSION_DIR=$RELEASE_DIR/$VERSION

    #  At this point we are mostly done, we just need to make our release directory and copy the files.
    #
    #  We look to see if that release directory (/www/cvs.apache.org/dist/geronimo) is on this machine.
    #  If it is, then we know we are running locally and we can just move things over using mkdir and cp.
    #  If not, then we have to do this remotely over ssh.
    [ -d $RELEASE_DIR ] && RUNNING_LOCALLY=true

    echo "Making dir $VERSION_DIR"

    if [ $RUNNING_LOCALLY ]; then
	mkdir $VERSION_DIR || fail "Unable to create the release dir $VERSION_DIR"
	cp -r $DIST/${RELEASE_ID}* $VERSION_DIR || fail "Unable to upload the binaries to release dir $VERSION_DIR"
    else 
	SSH_URL=$REMOTE_HOST
	(ssh $SSH_URL  mkdir $VERSION_DIR ) || fail "Unable to create the release dir $VERSION_DIR"
	(scp $DIST/${RELEASE_ID}* $SSH_URL:$VERSION_DIR) || fail "Unable to upload the binaries to release dir $VERSION_DIR"
    fi

    #  Now we should be responsible and delete old nightly builds
    #  Again, we have take into account that this script might be running
    #  on another machine than where builds are published, so we do it
    #  over ssh if that is the case.
    #
    #  The commands are basically the same either way, so we'll just put the
    #  ssh part of the command in a variable SSH which will just not be set
    #  if we are running locally.

    [ ! $RUNNING_LOCALLY ] && SSH="ssh $REMOTE_HOST"

    #  The extra parenthesis in this statement make the result of the
    #  ls command into an array.  Many people aren't familiar with a 
    #  bash array.  This is a good reference:
    #     http://www.tldp.org/LDP/abs/html/arrays.html
    #  The -t option to ls gives us a list of files sorted by order created, newest first.
    UNSTABLE_BUILDS=( $( $SSH ls -t $RELEASE_DIR ) )

    #  Set this to the number of builds you want to keep
    KEEP=14
   
    #  Now we create a new list skipping over the newest entries.
    DELETE_LIST=${UNSTABLE_BUILDS[@]:$KEEP}
    
    #  If there is anything in the DELETE_LIST, delete them.  The command
    #  is in quotes so it will work as one command if done over ssh.
    [ "$DELETE_LIST" ] && ( $SSH "cd $RELEASE_DIR && rm -r $DELETE_LIST" )

    #  clean up locally
    echo rm -r $DIST/* $RELEASE_ID
}

publish_build_archives
