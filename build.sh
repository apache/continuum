#!/bin/bash

set -e
set -x

includes=\
continuum-api/pom.xml,\
continuum-cc/pom.xml,\
continuum-core/pom.xml,\
continuum-model/pom.xml,\
continuum-notifiers/pom.xml,\
continuum-notifiers/continuum-notifier-irc/pom.xml,\
continuum-notifiers/continuum-notifier-jabber/pom.xml,\
continuum-notifiers/continuum-notifier-msn/pom.xml,\
continuum-web/pom.xml,\
continuum-xfire/pom.xml,\
continuum-xmlrpc/pom.xml

(
  m2 -N install "$@"
  (cd continuum-notifiers && m2 -N install "$@" )
  m2 -r -Dmaven.reactor.includes=*/pom.xml clean:clean "$@"
  m2 -r -Dmaven.reactor.includes="$includes" install "$@"
  (cd continuum-plexus-application && sh build.sh "$@")
) 2>&1 | tee result.log
