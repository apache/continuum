package org.apache.maven.continuum.execution;

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

import org.apache.continuum.utils.shell.ExecutionResult;
import org.apache.continuum.utils.shell.ShellCommandHelper;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.configuration.DefaultConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.scm.ScmResult;
import org.apache.maven.continuum.utils.ChrootJailWorkingDirectoryService;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ContinuumBuildExecutorTest
{
    protected AbstractBuildExecutor executor = new BuildExecutorStub();

    private String toSystemPath( String path )
    {
        if ( File.separator.equals( "\\" ) )
        {
            String newPath = path.replaceAll( "/", "\\" + File.separator );
            return newPath.replaceAll( "\\\\bin\\\\sh", "/bin/sh" );
        }
        return path;
    }

    @Test
    public void testExecuteShellCommand()
        throws Exception
    {
        File chrootJailFile = new File( toSystemPath( "/home" ) );
        File workingDirectory = new File( toSystemPath( "/dir1/dir2/workingdir" ) );
        ShellCommandHelper helper = mock( ShellCommandHelper.class );
        ConfigurationService configurationService = mock( DefaultConfigurationService.class );
        when( configurationService.getWorkingDirectory() ).thenReturn( workingDirectory );

        ChrootJailWorkingDirectoryService directoryService = new ChrootJailWorkingDirectoryService();
        directoryService.setConfigurationService( configurationService );
        directoryService.setChrootJailDirectory( chrootJailFile );

        executor.setChrootJailDirectory( chrootJailFile );
        executor.setShellCommandHelper( helper );
        executor.setWorkingDirectoryService( directoryService );
        //executor.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );

        Project project = new Project();
        project.setId( 7 );
        project.setGroupId( "xx" );
        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setGroupId( project.getGroupId() );
        project.setProjectGroup( projectGroup );

        assertEquals( toSystemPath(
            chrootJailFile.getPath() + "/" + project.getGroupId() + workingDirectory.getPath() + "/" +
                project.getId() ), directoryService.getWorkingDirectory( project ).getPath() );

        String executable = "mvn";
        String arguments = "-o clean install";
        File output = new File( "target/tmp" );
        Map<String, String> environments = new HashMap<String, String>();

        String cmd =
            "chroot /home/xx " + " /bin/sh -c 'cd /dir1/dir2/workingdir/" + project.getId() + " && " + executable +
                " " + arguments + "'";

        ExecutionResult expectedResult = new ExecutionResult( 0 );

        when( helper.executeShellCommand( chrootJailFile, "sudo", toSystemPath( cmd ), output, project.getId(),
                                          environments ) ).thenReturn( expectedResult );

        ContinuumBuildExecutionResult result =
            executor.executeShellCommand( project, executable, arguments, output, environments,
                                          null, null );

        assertEquals( 0, result.getExitCode() );
        verify( helper ).executeShellCommand( chrootJailFile, "sudo", toSystemPath( cmd ), output, project.getId(),
                                              environments );
    }

    class BuildExecutorStub
        extends AbstractBuildExecutor
    {

        protected BuildExecutorStub()
        {
            super( "stub", true );
        }

        protected String findExecutable( String executable, String defaultExecutable, boolean resolveExecutable,
                                         File workingDirectory )
        {
            return executable;
        }

        @Override
        protected Map<String, String> getEnvironments( BuildDefinition buildDefinition )
        {
            // TODO Auto-generated method stub
            return null;
        }

        public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput,
                                                    List<Project> projectsWithCommonScmRoot, String projectScmRootUrl )
            throws ContinuumBuildExecutorException
        {
            // TODO Auto-generated method stub
            return null;
        }

        public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition,
                                               ScmResult scmResult )
            throws ContinuumBuildExecutorException
        {
            // TODO Auto-generated method stub

        }
    }
}
