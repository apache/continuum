#!/bin/sh

cd ..

rm -rf cache

java -classpath lib/continuum-plugin-manager-1.4-SNAPSHOT.jar:system/continuum-plugin-api-1.4-SNAPSHOT.jar:lib/org.apache.felix.main-1.2.2.jar org.apache.continuum.plugin.manager.PluginManager
