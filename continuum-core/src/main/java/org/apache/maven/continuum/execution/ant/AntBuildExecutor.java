package org.apache.maven.continuum.execution.ant;

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

import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.execution.shared.JUnitReportArchiver;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class AntBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_EXECUTABLE = "executable";

    public static final String CONFIGURATION_TARGETS = "targets";

    public static final String ID = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;

    private ConfigurationService configurationService;

    private JUnitReportArchiver testReportArchiver;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public AntBuildExecutor()
    {
        super( ID, true );
    }

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    public void setTestReportArchiver( JUnitReportArchiver testReportArchiver )
    {
        this.testReportArchiver = testReportArchiver;
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                List<Project> projectsWithCommonScmRoot, String projectScmRootUrl )
        throws ContinuumBuildExecutorException
    {
        String executable = getInstallationService().getExecutorConfigurator(
            InstallationService.ANT_TYPE ).getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = getBuildFileForProject( project, buildDefinition );

        if ( !StringUtils.isEmpty( buildFile ) )
        {
            arguments.append( "-f " ).append( buildFile ).append( " " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );

        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        Map<String, String> environments = getEnvironments( buildDefinition );
        String antHome = environments.get( getInstallationService().getEnvVar( InstallationService.ANT_TYPE ) );
        if ( StringUtils.isNotEmpty( antHome ) )
        {
            executable = antHome + File.separator + "bin" + File.separator + executable;
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
            envVars.put( getInstallationService().getEnvVar( InstallationService.ANT_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;

    }

    public void updateProjectFromCheckOut( File workingDirectory, Project p, BuildDefinition buildDefinition,
                                           ScmResult scmResult )
        throws ContinuumBuildExecutorException
    {
    }

    @Override
    public void backupTestFiles( Project project, int buildId, String projectScmRootUrl,
                                 List<Project> projectsWithCommonScmRoot )
    {
        try
        {
            File backupDirectory = configurationService.getTestReportsDirectory( buildId, project.getId() );
            if ( !backupDirectory.exists() )
            {
                backupDirectory.mkdirs();
            }
            testReportArchiver.archiveReports(
                getWorkingDirectory( project, projectScmRootUrl, projectsWithCommonScmRoot ),
                backupDirectory );
        }
        catch ( ConfigurationException e )
        {
            log.error( "failed to get backup directory", e );
        }
        catch ( IOException e )
        {
            log.warn( "failed to copy test results to backup directory", e );
        }
    }

}

