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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version 
 */
public class DefaultWorkingDirectoryServiceTest
    extends PlexusInSpringTestCase
{
    private DefaultWorkingDirectoryService workingDirectoryService;
    
    private Mockery context;
    
    private ConfigurationService configurationService;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        context = new JUnit3Mockery();
        
        configurationService = context.mock( ConfigurationService.class );
        
        workingDirectoryService = ( DefaultWorkingDirectoryService ) lookup( WorkingDirectoryService.class );
        
        workingDirectoryService.setConfigurationService( configurationService );
    }
    
    public void testGetWorkingDirectoryOfSingleCheckoutMultiModules()
        throws Exception
    {
        ProjectGroup pGroup = new ProjectGroup();
        pGroup.setId( 1 );
        
        List<Project> projects = new ArrayList<Project>();
        
        Project project = new Project();        
        project.setId( 7 );
        project.setGroupId( "org.apache.continuum" );
        project.setArtifactId( "module-a" );
        project.setVersion( "1.0-SNAPSHOT" );
        project.setScmUrl( "scm:local:src/test-projects:flat-multi-module/module-a" );
        
        projects.add( project );
        
        Project otherProject = new Project();        
        otherProject.setId( 8 );
        otherProject.setGroupId( "org.apache.continuum" );
        otherProject.setArtifactId( "module-b" );
        otherProject.setVersion( "1.0-SNAPSHOT" );
        otherProject.setScmUrl( "scm:local:src/test-projects:flat-multi-module/module-b" );
        
        projects.add( otherProject );
        
        otherProject = new Project();        
        otherProject.setId( 6 );
        otherProject.setGroupId( "org.apache.continuum" );
        otherProject.setArtifactId( "parent-project" );
        otherProject.setVersion( "1.0-SNAPSHOT" );
        otherProject.setScmUrl( "scm:local:src/test-projects:flat-multi-module/parent-project" );
        
        projects.add( otherProject );
                
        final File unixBaseWorkingDirectory = new File( "/target/working-directory" );        
     
        final File windowsBaseWorkingDirectory = new File( "c:\\target\\working-directory" );
        
        context.checking( new Expectations()
        {
            {
                one( configurationService ).getWorkingDirectory();
                will( returnValue( unixBaseWorkingDirectory ) );
                
                one( configurationService ).getWorkingDirectory();
                will( returnValue( windowsBaseWorkingDirectory ) );
            }} );
        
     // test if unix path
        File projectWorkingDirectory =
            workingDirectoryService.getWorkingDirectory( project, "scm:local:src/test-projects:flat-multi-module",
                                                         projects );
        
        assertEquals( "Incorrect working directory for multi-module project", "/target/working-directory/6/module-a",
                      projectWorkingDirectory.getPath() );
            
        project.setWorkingDirectory( null );
        
     // test if windows path
        projectWorkingDirectory =
            workingDirectoryService.getWorkingDirectory( project, "scm:local:src/test-projects:flat-multi-module",
                                                         projects );
        
        assertEquals( "Incorrect working directory for multi-module project", "c:\\target\\working-directory\\6\\module-a",
                      projectWorkingDirectory.getPath() );

    }
    
    
}
