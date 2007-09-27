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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectDependency;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sort projects by dependencies.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class ProjectSorter
{
    private ProjectSorter()
    {
        // no touchy...
    }

    /**
     * Sort a list of projects.
     * <ul>
     * <li>collect all the vertices for the projects that we want to build.</li>
     * <li>iterate through the deps of each project and if that dep is within
     * the set of projects we want to build then add an edge, otherwise throw
     * the edge away because that dependency is not within the set of projects
     * we are trying to build. we assume a closed set.</li>
     * <li>do a topo sort on the graph that remains.</li>
     * </ul>
     */
    public static List<Project> getSortedProjects( Collection<Project> projects )
        throws CycleDetectedException
    {
        DAG dag = new DAG();

        Map<String, Project> projectMap = new HashMap<String, Project>();

        for ( Project project : projects )
        {
            String id = getProjectId( project );

            dag.addVertex( id );

            projectMap.put( id, project );
        }

        for ( Project project : projects )
        {
            String id = getProjectId( project );

            // Parent
            ProjectDependency parent = project.getParent();

            if ( parent != null )
            {
                String dependencyId = getDependencyId( parent );

                if ( dag.getVertex( dependencyId ) != null )
                {
                    dag.addEdge( id, dependencyId );
                }
            }

            // Dependencies
            for ( Object o : project.getDependencies() )
            {
                ProjectDependency dependency = (ProjectDependency) o;

                String dependencyId = getDependencyId( dependency );

                if ( dag.getVertex( dependencyId ) != null )
                {
                    dag.addEdge( id, dependencyId );
                }
            }
        }

        List<Project> sortedProjects = new ArrayList<Project>();

        for ( Object o : TopologicalSorter.sort( dag ) )
        {
            String id = (String) o;

            sortedProjects.add( projectMap.get( id ) );
        }

        return sortedProjects;
    }

    private static String getProjectId( Project project )
    {
        String groupId;

        String artifactId;

        if ( project.getGroupId() == null )
        {
            groupId = project.getName();
        }
        else
        {
            groupId = project.getGroupId();
        }

        if ( project.getArtifactId() == null )
        {
            artifactId = project.getName();
        }
        else
        {
            artifactId = project.getArtifactId();
        }

        return groupId + ":" + artifactId + ":" + project.getVersion();
    }

    private static String getDependencyId( ProjectDependency project )
    {
        return project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
    }
}
