#!/bin/bash
mvn clean:clean plexus:app plexus:bundle-application assembly:assembly "$@"