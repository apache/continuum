#!/bin/bash

ENV=test
#ENV=production

mvn -Denv=$ENV clean:clean plexus:app plexus:bundle-application assembly:assembly "$@"