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

import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Base class to add ANT/Shell projects.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractAddProject
    extends AbstractContinuumMojo
{
    /**
     * The project name.
     *
     * @parameter expression="${projectName}"
     * @required
     */
    private String projectName;

    /**
     * The project verion.
     *
     * @parameter expression="${projectVersion}"
     * @required
     */
    private String projectVersion;

    /**
     * The SCM Url. Must be a Maven-SCM url.
     *
     * @parameter expression="${scmUrl}"
     * @required
     */
    private String scmUrl;

    /**
     * The SCM username.
     *
     * @parameter expression="${scm.username}"
     */
    private String scmUsernme;

    /**
     * The SCM password.
     *
     * @parameter expression="${scm.password}"
     */
    private String scmPassword;

    /**
     * The SCM branch/tag name.
     *
     * @parameter expression="${scm.tag}"
     */
    private String scmTag;

    /**
     * Use SCM credentials Cache, if available.
     *
     * @parameter expression="${scm.useCredentialsCache}" default-value="false"
     */
    private boolean scmUseCredentialsCache;

    /**
     * The project Group Id.
     *
     * @parameter expression="${projectGroupId}"
     */
    private String projectGroupId;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectSummary project = new ProjectSummary();
        project.setName( projectName );
        project.setVersion( projectVersion );
        project.setScmUrl( scmUrl );
        project.setScmUsername( scmUsernme );
        project.setScmPassword( scmPassword );
        project.setScmTag( scmTag );
        project.setScmUseCache( scmUseCredentialsCache );

        try
        {
            if ( projectGroupId != null && projectGroupId.length() > 0 )
            {
                if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( getProjectType() ) )
                {
                    getClient().addAntProject( project, Integer.parseInt( projectGroupId ) );
                }
                else
                {
                    getClient().addShellProject( project, Integer.parseInt( projectGroupId ) );
                }
            }
            else
            {
                if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( getProjectType() ) )
                {
                    getClient().addAntProject( project );
                }
                else
                {
                    getClient().addShellProject( project );
                }
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Can't add the " + getProjectType() + " project.", e );
        }
    }

    protected abstract String getProjectType();
}
