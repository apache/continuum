package org.apache.maven.continuum.web.action;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.continuum.web.action.stub.AddProjectActionStub;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Test for {@link AddProjectAction}
 *
 * @author <a href="mailto:jzurbano@apache.org">jzurbano</a>
 */
public class AddProjectActionTest
    extends MockObjectTestCase
{
    private final AddProjectActionStub action;

    private final Mock continuumMock;

    public AddProjectActionTest()
    {
        action = new AddProjectActionStub();
        continuumMock = new Mock( Continuum.class );
        action.setContinuum( (Continuum) continuumMock.proxy() );
    }

    public void testAddProjectNullValues()
        throws Exception
    {
        action.setProjectName( null );
        action.setProjectVersion( null );
        action.setProjectScmUrl( null );
        
        action.validate();
    }
    
    /**
     * Test add of Ant project
     *
     * @throws Exception
     */
    public void testAddAntProject()
        throws Exception
    {
        String scmUrl = "scm:svn:http://project/scm/url/test/build.xml";
        
        List<Project> projects = createProjectList();
        continuumMock.expects( once() ).method( "getProjects" ).will( returnValue( projects ) );
        continuumMock.expects( once() ).method( "addProject" ).will( returnValue( 3 ) );
        
        action.setProjectName( "Ant Test Project" );
        action.setProjectVersion( "1.0-SNAPSHOT" );
        action.setProjectScmUrl( scmUrl );
        action.setProjectType( "ant" );
        action.setSelectedProjectGroup( 1 );
        action.setBuildDefintionTemplateId( 1 );
        
        action.validate();
        action.add();
        continuumMock.verify();
        
    }

    /**
     * Test add of Shell project
     *
     * @throws Exception
     */
    public void testAddShellProject()
        throws Exception
    {
        String scmUrl = "scm:svn:http://project/scm/url/test/run.sh";
        
        List<Project> projects = createProjectList();
        continuumMock.expects( once() ).method( "getProjects" ).will( returnValue( projects ) );
        continuumMock.expects( once() ).method( "addProject" ).will( returnValue( 3 ) );
        
        action.setProjectName( "Shell Test Project" );
        action.setProjectVersion( "1.0-SNAPSHOT" );
        action.setProjectScmUrl( scmUrl );
        action.setProjectType( "shell" );
        action.setSelectedProjectGroup( 1 );
        action.setBuildDefintionTemplateId( 1 );
        
        action.validate();
        action.add();
        continuumMock.verify();
    }
    
    private List<Project> createProjectList()
    {
        List<Project> projects = new ArrayList<Project>();
        
        Project project1 = createProject( "scm:svn:http://project/scm/url/test-1/run.sh", "Shell Test Project 1", "1.0-SNAPSHOT", 1 );
        Project project2 = createProject( "scm:svn:http://project/scm/url/test-2/build.xml", "Ant Test Project 1", "1.0-SNAPSHOT", 2 );
        
        projects.add( project1 );
        projects.add( project2 );
        
        return projects;
    }
    
    private Project createProject( String scmUrl, String name, String version, int id )
    {
        Project project = new Project();
        project.setId( id );
        project.setName( name );
        project.setVersion( version );
        project.setScmUrl( scmUrl );
        
        return project;
    }
}
