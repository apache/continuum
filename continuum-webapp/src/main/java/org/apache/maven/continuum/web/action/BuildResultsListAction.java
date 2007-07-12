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

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="buildResults"
 */
public class BuildResultsListAction
    extends ContinuumActionSupport
{
    private Project project;

    private Collection buildResults;
    
    private Collection selectedBuildResults;

    private int projectId;

    private String projectName;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        project = getContinuum().getProject( projectId );

        buildResults = getContinuum().getBuildResultsForProject( projectId );

        return SUCCESS;
    }
    
    public String remove()
        throws ContinuumException
    {
        try
        {
            checkModifyProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }
        
        if ( selectedBuildResults != null && !selectedBuildResults.isEmpty() )
        {
            for ( Iterator i = selectedBuildResults.iterator(); i.hasNext(); )
            {
                int buildId = Integer.parseInt( (String) i.next() );
                
                try
                {
                    getLogger().info( "Removing BuildResult with id=" + buildId );
                    
                    getContinuum().removeBuildResult( buildId );
                }
                catch ( ContinuumException e )
                {
                    getLogger().error( "Error removing BuildResult with id=" + buildId );
                    addActionError( "Unable to remove BuildResult with id=" + buildId );
                }
            }
        }
        
        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public Collection getBuildResults()
    {
        return buildResults;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public Project getProject()
    {
        return project;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProject( projectId ).getProjectGroup().getName();
        }

        return projectGroupName;
    }

    public Collection getSelectedBuildResults()
    {
        return selectedBuildResults;
    }

    public void setSelectedBuildResults( Collection selectedBuildResults )
    {
        this.selectedBuildResults = selectedBuildResults;
    }
}
