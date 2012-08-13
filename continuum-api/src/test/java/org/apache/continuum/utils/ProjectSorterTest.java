package org.apache.continuum.utils;

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

import junit.framework.TestCase;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jmcconnell@apache.org">Jesse McConnell</a>
 * @version $Id:$
 */
public class ProjectSorterTest
    extends TestCase
{

    /**
     * test basic three project tree (really a line in this case)
     */
    public void testBasicNestedProjectStructure()
        throws Exception
    {
        List<Project> list = new ArrayList<Project>();

        Project top = getNewProject( "top" );
        list.add( top );

        Project c1 = getNewProject( "c1" );
        c1.setParent( generateProjectDependency( top ) );
        list.add( c1 );

        Project c2 = getNewProject( "c2" );
        c2.setParent( generateProjectDependency( top ) );
        c2.setDependencies( Collections.singletonList( generateProjectDependency( c1 ) ) );
        list.add( c2 );

        List<Project> sortedList = ProjectSorter.getSortedProjects( list, null );

        assertNotNull( sortedList );

        Project p1 = sortedList.get( 0 );
        assertEquals( top.getArtifactId(), p1.getArtifactId() );
        Project p2 = sortedList.get( 1 );
        assertEquals( c1.getArtifactId(), p2.getArtifactId() );
        Project p3 = sortedList.get( 2 );
        assertEquals( c2.getArtifactId(), p3.getArtifactId() );
    }

    public void testNestedProjectStructureWithoutModulesDefinedInParentPom()
        throws Exception
    {
        Project top = getNewProject( "top" );

        Project war1 = getNewProject( "war1" );
        war1.setParent( generateProjectDependency( top ) );

        Project war2 = getNewProject( "war2" );
        war2.setParent( generateProjectDependency( top ) );

        Project ear1 = getNewProject( "ear1" );
        ear1.setParent( generateProjectDependency( top ) );
        List<ProjectDependency> deps = new ArrayList<ProjectDependency>();
        deps.add( generateProjectDependency( war1 ) );
        deps.add( generateProjectDependency( war2 ) );
        ear1.setDependencies( deps );

        List<Project> list = new ArrayList<Project>();

        // We add projects in a random order to really check the project orter
        list.add( top );
        list.add( ear1 );
        list.add( war1 );
        list.add( war2 );

        List<Project> sortedList = ProjectSorter.getSortedProjects( list, null );

        assertNotNull( sortedList );

        Project p1 = sortedList.get( 0 ); //top project must be the first
        assertEquals( top.getArtifactId(), p1.getArtifactId() );
        Project p4 = sortedList.get( 3 ); //ear1 project must be the latest
        assertEquals( ear1.getArtifactId(), p4.getArtifactId() );
    }

    /**
     * test project build order
     * build order: B -> A -> D -> C -> E
     *
     * @throws Exception
     */
    public void testProjectBuildOrder()
        throws Exception
    {
        List<Project> list = new ArrayList<Project>();

        Project projectA = getNewProject( "A" );
        Project projectB = getNewProject( "B" );
        Project projectC = getNewProject( "C" );
        Project projectD = getNewProject( "D" );
        Project projectE = getNewProject( "E" );

        projectA.setParent( generateProjectDependency( projectB ) );
        projectE.setParent( generateProjectDependency( projectB ) );
        projectC.setParent( generateProjectDependency( projectA ) );
        projectC.setDependencies( Collections.singletonList( generateProjectDependency( projectD ) ) );
        projectD.setParent( generateProjectDependency( projectA ) );

        list.add( projectA );
        list.add( projectB );
        list.add( projectC );
        list.add( projectD );
        list.add( projectE );

        List<Project> sortedList = ProjectSorter.getSortedProjects( list, null );
        assertNotNull( sortedList );

        List<Project> expectedList = new ArrayList<Project>();

        expectedList.add( projectB );
        expectedList.add( projectA );
        expectedList.add( projectD );
        expectedList.add( projectC );
        expectedList.add( projectE );

        for ( int i = 0; i < sortedList.size(); i++ )
        {
            Project sorted = sortedList.get( i );
            Project expected = expectedList.get( i );
            assertEquals( sorted.getArtifactId(), expected.getArtifactId() );
        }
    }

    /**
     * test one of the child projects not having the artifactId or groupId empty and working off the
     * name instead
     */
    public void testIncompleteNestedProjectStructure()
        throws Exception
    {
        List<Project> list = new ArrayList<Project>();

        Project top = getNewProject( "top" );
        list.add( top );

        Project c1 = getIncompleteProject( "c1" );
        c1.setParent( generateProjectDependency( top ) );
        list.add( c1 );

        Project c2 = getNewProject( "c2" );
        c2.setParent( generateProjectDependency( top ) );
        c2.setDependencies( Collections.singletonList( generateProjectDependency( c1 ) ) );
        list.add( c2 );

        List<Project> sortedList = ProjectSorter.getSortedProjects( list, null );

        assertNotNull( sortedList );

        Project p1 = sortedList.get( 0 );
        assertEquals( top.getArtifactId(), p1.getArtifactId() );
        Project p2 = sortedList.get( 1 );
        assertEquals( c1.getArtifactId(), p2.getArtifactId() );
        Project p3 = sortedList.get( 2 );
        assertEquals( c2.getArtifactId(), p3.getArtifactId() );
    }

    /**
     * project sorter can work with name replacing the artifactid and groupId
     *
     * @param projectId The project id
     * @return The generated Project
     */
    private Project getIncompleteProject( String projectId )
    {
        Project project = new Project();
        project.setName( "foo" + projectId );
        project.setVersion( "v" + projectId );
        project.setProjectGroup( new ProjectGroup() );

        return project;
    }

    private Project getNewProject( String projectId )
    {
        Project project = new Project();
        project.setArtifactId( "a" + projectId );
        project.setGroupId( "g" + projectId );
        project.setVersion( "v" + projectId );
        project.setName( "n" + projectId );
        project.setProjectGroup( new ProjectGroup() );

        return project;
    }

    private ProjectDependency generateProjectDependency( Project project )
    {
        ProjectDependency dep = new ProjectDependency();
        dep.setArtifactId( project.getArtifactId() );
        dep.setGroupId( project.getGroupId() );
        dep.setVersion( project.getVersion() );

        return dep;
    }

}
