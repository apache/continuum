package org.apache.maven.continuum.plugin;

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

import org.apache.maven.continuum.xmlrpc.project.AddingResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which add a Maven2 project.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal add-maven-two-project
 */
public class AddMavenTwoProject
    extends AbstractContinuumMojo
{
    /**
     * POM Url.
     *
     * @parameter expression="${projectUrl}" default-value="${project.scm.url}"
     * @required
     */
    private String projectUrl;

    /**
     * Project Group Id.
     *
     * @parameter expression="${projectGroupId}"
     */
    private String projectGroupId;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        AddingResult addingResult = null;
        try
        {

            if ( projectGroupId != null && projectGroupId.length() > 0 )
            {
                addingResult = getClient().addMavenTwoProject( projectUrl, Integer.parseInt( projectGroupId ) );
            }
            else
            {
                addingResult = getClient().addMavenTwoProject( projectUrl );
            }
            if ( addingResult.getErrorsAsString() != null )
            {
                getLog().error( "fail to add mavenTwo project " + addingResult.getErrorsAsString() );
                throw new MojoExecutionException( "fail to add mavenTwo project " + addingResult.getErrorsAsString() );
            }
        }
        catch ( MojoExecutionException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Can't add the Maven2 project from '" + projectUrl + "'.", e );
        }
    }
}
