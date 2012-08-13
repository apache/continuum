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

import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildResults"
 */
public class BuildResultsListAction
    extends AbstractBuildAction
{
    private static final Logger logger = LoggerFactory.getLogger( BuildResultsListAction.class );

    private Project project;

    private Collection<BuildResult> buildResults;

    private Collection<String> selectedBuildResults;

    private int projectId;

    private int projectGroupId;

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
        if ( this.isConfirmed() )
        {
            if ( selectedBuildResults != null && !selectedBuildResults.isEmpty() )
            {
                for ( String id : selectedBuildResults )
                {
                    int buildId = Integer.parseInt( id );

                    try
                    {
                        logger.info( "Removing BuildResult with id=" + buildId );

                        getContinuum().removeBuildResult( buildId );

                        AuditLog event = new AuditLog( "Build Result id=" + buildId,
                                                       AuditLogConstants.REMOVE_BUILD_RESULT );
                        event.setCategory( AuditLogConstants.BUILD_RESULT );
                        event.setCurrentUser( getPrincipal() );
                        event.log();
                    }
                    catch ( ContinuumException e )
                    {
                        logger.error( "Error removing BuildResult with id=" + buildId );
                        addActionError( getText( "buildResult.delete.error", "Unable to delete build result",
                                                 new Integer( buildId ).toString() ) );
                    }
                }
            }
            return SUCCESS;
        }
        else
        {
            List<String> buildResultsRemovable = new ArrayList<String>();
            if ( selectedBuildResults != null && !selectedBuildResults.isEmpty() )
            {
                for ( String id : selectedBuildResults )
                {
                    int buildId = Integer.parseInt( id );

                    try
                    {
                        if ( canRemoveBuildResult( getContinuum().getBuildResult( buildId ) ) )
                        {
                            buildResultsRemovable.add( Integer.toString( buildId ) );
                        }
                        else
                        {
                            this.addActionMessage( getResourceBundle().getString( "buildResult.cannot.delete" ) );
                            return SUCCESS;
                        }
                    }
                    catch ( BuildManagerException e )
                    {
                        logger.error( e.getMessage() );
                        throw new ContinuumException( e.getMessage(), e );
                    }
                }
            }
            this.setSelectedBuildResults( buildResultsRemovable );
        }
        return CONFIRM;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public Collection<BuildResult> getBuildResults()
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

    public Collection<String> getSelectedBuildResults()
    {
        return selectedBuildResults;
    }

    public void setSelectedBuildResults( Collection<String> selectedBuildResults )
    {
        this.selectedBuildResults = selectedBuildResults;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }
}
