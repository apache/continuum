package org.apache.maven.continuum.utils;

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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DefaultWorkingDirectoryServiceTest
    extends PlexusInSpringTestCase
{
    private DefaultWorkingDirectoryService workingDirectoryService;

    private ConfigurationService configurationService;

    private File baseWorkingDirectory;

    public void setUp()
        throws Exception
    {
        super.setUp();

        configurationService = mock( ConfigurationService.class );

        baseWorkingDirectory = new File( getBasedir(), "target" + File.separator + "working-directory" );
        when( configurationService.getWorkingDirectory() ).thenReturn( baseWorkingDirectory );

        workingDirectoryService = (DefaultWorkingDirectoryService) lookup( WorkingDirectoryService.class );
        workingDirectoryService.setConfigurationService( configurationService );
    }

    private Project createProject( int id, String groupId, String artifactId, String version, String scmUrl,
                                   boolean checkedOutInSingleDirectory )
    {
        Project project = new Project();
        project.setId( id );
        project.setGroupId( groupId );
        project.setArtifactId( artifactId );
        project.setVersion( version );
        project.setScmUrl( scmUrl );
        project.setCheckedOutInSingleDirectory( checkedOutInSingleDirectory );

        return project;
    }

    public void testGetWorkingDirectoryOfSingleCheckoutFlatMultiModules()
        throws Exception
    {
        List<Project> projects = new ArrayList<Project>();
        Project project = createProject( 7, "org.apache.continuum", "module-a", "1.0-SNAPSHOT",
                                         "scm:local:src/test-projects:flat-multi-module/module-a", true );
        projects.add( project );
        projects.add( createProject( 8, "org.apache.continuum", "module-b", "1.0-SNAPSHOT",
                                     "scm:local:src/test-projects:flat-multi-module/module-b", true ) );
        projects.add( createProject( 6, "org.apache.continuum", "parent-project", "1.0-SNAPSHOT",
                                     "scm:local:src/test-projects:flat-multi-module/parent-project", true ) );

        File projectWorkingDirectory = workingDirectoryService.getWorkingDirectory( project,
                                                                                    "scm:local:src/test-projects:flat-multi-module",
                                                                                    projects );

        assertEquals( "Incorrect working directory for flat multi-module project", baseWorkingDirectory +
            File.separator + "6" + File.separator + "module-a", projectWorkingDirectory.getPath() );

        // test if separator is appended at the end of the scm root url
        projectWorkingDirectory = workingDirectoryService.getWorkingDirectory( project,
                                                                               "scm:local:src/test-projects:flat-multi-module/",
                                                                               projects );

        assertEquals( "Incorrect working directory for flat multi-module project", baseWorkingDirectory +
            File.separator + "6" + File.separator + "module-a", projectWorkingDirectory.getPath() );
    }

    public void testGetWorkingDirectoryOfSingleCheckoutRegularMultiModules()
        throws Exception
    {
        List<Project> projects = new ArrayList<Project>();
        Project project = createProject( 10, "org.apache.continuum", "module-a", "1.0-SNAPSHOT",
                                         "scm:local:src/test-projects:regular-multi-module/module-a", true );
        projects.add( project );
        projects.add( createProject( 11, "org.apache.continuum", "module-b", "1.0-SNAPSHOT",
                                     "scm:local:src/test-projects:regular-multi-module/module-b", true ) );
        projects.add( createProject( 9, "org.apache.continuum", "parent-project", "1.0-SNAPSHOT",
                                     "scm:local:src/test-projects:regular-multi-module/", true ) );

        File projectWorkingDirectory = workingDirectoryService.getWorkingDirectory( project,
                                                                                    "scm:local:src/test-projects:regular-multi-module",
                                                                                    projects );

        assertEquals( "Incorrect working directory for regular multi-module project",
                      baseWorkingDirectory + File.separator +
                          "9" + File.separator + "module-a", projectWorkingDirectory.getPath() );

        // test if separator is appended at the end of the scm root url
        projectWorkingDirectory = workingDirectoryService.getWorkingDirectory( project,
                                                                               "scm:local:src/test-projects:regular-multi-module/",
                                                                               projects );

        assertEquals( "Incorrect working directory for regular multi-module project",
                      baseWorkingDirectory + File.separator +
                          "9" + File.separator + "module-a", projectWorkingDirectory.getPath() );

        // test generated path of parent project
        project = createProject( 9, "org.apache.continuum", "parent-project", "1.0-SNAPSHOT",
                                 "scm:local:src/test-projects:regular-multi-module", true );

        projectWorkingDirectory = workingDirectoryService.getWorkingDirectory( project,
                                                                               "scm:local:src/test-projects:regular-multi-module",
                                                                               projects );

        assertEquals( "Incorrect working directory for regular multi-module project", baseWorkingDirectory +
            File.separator + "9", projectWorkingDirectory.getPath() );
    }
}