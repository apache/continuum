#!/usr/local/bin/bash
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

# HOW TO USE THIS SCRIPT 
#
#  Executing the script like the following will run the build and, in
#  the event of a failure, send an email message to
#  scm@geronimo.apache.org with a link to the build output and a small
#  snippit of maven output.
#
#   $ ./build_and_complain.sh 
#
#  Executing the script like the following will do the same as above, 
#  but send the build output to foobar@apache.org
#
#   $ ./build_and_complain.sh foobar@apache.org
#
# DETAILS
#
#  On failure, the script will scp build data to your public_html 
#  directory on people.apache.org.  If your keys aren't setup, this 
#  won't work so well.
#  

############################################################
#
#  Parameters
#
############################################################

function usage {
    echo "$(basename $0) [-u user] [-s svn_url] [email_address]"
}

# The default svn url 
SCM_URL="http://svn.apache.org/repos/asf/geronimo/trunk"

while getopts "s:u:" option; do
    case $option in 
	u ) USER=$OPTARG;;
	s ) SCM_URL=$OPTARG;;
	\? ) usage; exit 1;;
	* ) usage; exit 1;;
    esac
done
shift $(($OPTIND -1))

# The email address to complain to
SCM_LIST=${1:-scm@geronimo.apache.org}

echo "build:  $SCM_URL"
echo "notify: $SCM_LIST"

############################################################
#
#  Functions
#
############################################################

#----
# Don't kill the build on failing tests.  Run it all the 
# way through and report all the failing tests at the end.
#
function xmaven {
    LOG=/tmp/$USER-$(date +%s)
    maven -Dmaven.test.failure.ignore=true 2>&1 "$@" | tee $LOG

    if [ -n "$(grep 'FAILED$' $LOG)" ] ; then
	rm $LOG
	return 1
    fi
    rm $LOG
}

#----
# Grep the maven output and generate failure information
# if the build or tests failed.  Don't send a big email,
# just scp the files to people.apache.org/~you  and put
# a link in the email
#
function monitorlog {
    BUILD_ID=${1?You must specify a build id}
    LOG=${2?You must specify a log name}
    TO=${3:-scm@geronimo.apache.org}
    
    # If the build didn't fail, just exit
    if [ -z "$(grep 'FAILED$' $LOG)" ] ; then 
	return 1
    fi

    mkdir /tmp/$BUILD_ID
    cp $LOG /tmp/$BUILD_ID/build-output.txt

    for n in $(grep "junit.*FAILED" $LOG | perl -pe 's/.*TEST (.*) FAILED/$1/'); do
	JUNIT_REPORT=$(find . -name "TEST-$name.txt" -exec ls $PWD/{} \;)
	cp $JUNIT_REPORT /tmp/$BUILD_ID/
    done
    
    maven --info > /tmp/$BUILD_ID/maven-info.txt
    uname -a >  /tmp/$BUILD_ID/system-info.txt

    echo "http://people.apache.org/~$USER/$BUILD_ID"
    scp -C -r /tmp/$BUILD_ID people.apache.org:public_html/

    # Everything from EOF ...(many lines)... EOF is the email,
    # so if you want to change the output, this is where.

### begin email output ###
ssh people.apache.org /usr/sbin/sendmail -it <<EOF
From: $USER@apache.org
To: $TO
Reply-To: dev@geronimo.apache.org
Subject: build failed $(date)"

BUILD FAILED

  $SCM_URL
  $(grep 'Checked out revision' $LOG | sed 's/Checked out r/R/' )

See details at:

  http://people.apache.org/~$USER/$BUILD_ID

Simple grep of build output:

$(grep -C 10 FAILED $LOG | perl -pe 's/^--$/\n[SNIP]\n/; s/^/  /')

-- 
Feel welcome to set this script [1] up on your machine and direct the
messages to scm AT geronimo.apache.org.  Also feel free to send
patches to our jira [2] if you would like different/better output or
command line options.

 1. http://svn.apache.org/repos/asf/geronimo/scripts/build_and_complain.sh
 2. http://issues.apache.org/jira/browse/GERONIMO

EOF
### end email output ###

    rm -r /tmp/$BUILD_ID
}

############################################################
#
#  M A I N
#
############################################################

# scrape 'trunk' off the url and return the last 
# directory name in the url.
DIR=$(basename ${SCM_URL//\/trunk/})

# make up a somewhat unique build id
BUILD_ID=$DIR-$(date +%Y%m%d-%H%M)

# run the build
LOG=$BUILD_ID-output.txt
{
    echo "\$ svn co $SCM_URL $DIR"
    svn co $SCM_URL $DIR  &&
    echo "\$ maven m:checkout clean default" &&
    (cd $DIR && xmaven m:checkout clean default)
} 2>&1 | tee $LOG

# check the log file and complain if needed
monitorlog $BUILD_ID $LOG $SCM_LIST

# kill our log file
rm $LOG
