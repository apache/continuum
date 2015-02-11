package org.apache.maven.continuum.execution.maven.m1;

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

import org.apache.continuum.model.repository.LocalRepository;
import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenOneBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    public final static String CONFIGURATION_GOALS = "goals";

    public final static String ID = ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR;

    @Requirement
    private MavenOneMetadataHelper metadataHelper;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public MavenOneBuildExecutor()
    {
        super( ID, true );
    }

    public MavenOneMetadataHelper getMetadataHelper()
    {
        return metadataHelper;
    }

    public void setMetadataHelper( MavenOneMetadataHelper metadataHelper )
    {
        this.metadataHelper = metadataHelper;
    }

    // ----------------------------------------------------------------------
    // Builder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                List<Project> projectsWithCommonScmRoot, String projectScmRootUrl )
        throws ContinuumBuildExecutorException
    {
        String executable = getInstallationService().getExecutorConfigurator(
            InstallationService.MAVEN1_TYPE ).getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( project, buildDefinition );

        if ( !StringUtils.isEmpty( buildFile ) && !"project.xml".equals( buildFile ) )
        {
            arguments.append( "-p " ).append( buildFile ).append( " " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );

        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }

        // append -Dmaven.repo.local if project group has a local repository
        LocalRepository repository = project.getProjectGroup().getLocalRepository();
        if ( repository != null )
        {
            arguments.append( "\"-Dmaven.repo.local=" ).append( StringUtils.clean( repository.getLocation() ) ).append(
                "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        Map<String, String> environments = getEnvironments( buildDefinition );
        String m1Home = environments.get( getInstallationService().getEnvVar( InstallationService.MAVEN1_TYPE ) );
        if ( StringUtils.isNotEmpty( m1Home ) )
        {
            executable = m1Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments, null, null );
    }

    protected Map<String, String> getEnvironments( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> envVars = new HashMap<String, String>();
        String javaHome = getJavaHomeValue( buildDefinition );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }
        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.MAVEN1_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;

    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition,
                                           ScmResult scmResult )
        throws ContinuumBuildExecutorException
    {
        File projectXmlFile = null;

        if ( buildDefinition != null )
        {
            String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

            if ( !StringUtils.isEmpty( buildFile ) )
            {
                projectXmlFile = new File( workingDirectory, buildFile );
            }
        }

        if ( projectXmlFile == null )
        {
            projectXmlFile = new File( workingDirectory, "project.xml" );
        }

        if ( !projectXmlFile.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor." );
        }

        try
        {
            boolean update = isDescriptionUpdated( buildDefinition, scmResult, project );
            metadataHelper.mapMetadata( new ContinuumProjectBuildingResult(), projectXmlFile, project, update );
        }
        catch ( MavenOneMetadataHelperException e )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata.", e );
        }
    }
}
