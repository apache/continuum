package org.apache.maven.continuum.xmlrpc.server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Map;

public class AddingResultUtil
{
    private static final Map<String, String> errorMap;

    static
    {
        errorMap = new HashMap<String, String>();
        errorMap.put( "add.project.unknown.host.error", "The specified host is either unknown or inaccessible." );
        errorMap.put( "add.project.connect.error", "Unable to connect to remote server." );
        errorMap.put( "add.project.malformed.url.error", "The URL provided is malformed." );
        errorMap.put( "add.project.field.required.error", "Either POM URL or Upload POM is required." );
        errorMap.put( "add.project.xml.parse.error", "The XML content of the POM can not be parsed." );
        errorMap.put( "add.project.extend.error", "Cannot use a POM with an ''extend'' element." );
        errorMap.put( "add.project.missing.pom.error",
                      "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
        errorMap.put( "add.project.missing.groupid.error", "Missing 'groupId' element in the POM." );
        errorMap.put( "add.project.missing.artifactid.error", "Missing 'artifactId' element in the POM." );
        errorMap.put( "add.project.missing.version.error", "Missing 'version' element in the POM." );
        errorMap.put( "add.project.missing.name.error", "Missing 'name' element in the POM." );
        errorMap.put( "add.project.missing.repository.error", "Missing 'repository' element in the POM." );
        errorMap.put( "add.project.missing.scm.error", "Missing 'scm' element in the POM project." );
        errorMap.put( "add.project.missing.scm.connection.error",
                      "Missing 'connection' sub-element in the 'scm' element in the POM." );
        errorMap.put( "add.project.missing.notifier.type.error",
                      "Missing 'type' sub-element in the 'notifier' element in the POM." );
        errorMap.put( "add.project.missing.notifier.configuration.error",
                      "Missing 'configuration' sub-element in the 'notifier' element in the POM." );
        errorMap.put( "add.project.metadata.transfer.error", "Transfer of Metadata has failed." );
        errorMap.put( "add.project.validation.protocol.not_allowed",
                      "The specified resource isn't a file or the protocol used isn't allowed." );
        errorMap.put( "add.project.unauthorized.error",
                      "You are not authorized to access the requested URL. Please verify that the correct username and password are provided." );
        errorMap.put( "add.project.artifact.not.found.error",
                      "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
        errorMap.put( "add.project.project.building.error", "Unknown error trying to build POM." );
        errorMap.put( "add.project.unknown.error",
                      "The specified resource cannot be accessed. Please try again later or contact your administrator." );
        errorMap.put( "add.project.nogroup.error", "No project group specified." );
        errorMap.put( "add.project.duplicate.error", "Trying to add duplicate projects in the same project group." );
    }

    public static String getErrorMessage( String error )
    {
        String message = errorMap.get( error );

        return message == null ? error : message;
    }
}
