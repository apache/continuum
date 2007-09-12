package org.apache.maven.continuum.web.action.component;

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
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.model.BuildDefinitionSummary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BuildDefinitionSummaryAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="buildDefinitionSummary"
 */
public class BuildDefinitionSummaryAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private String projectGroupName;

    private int projectId;

    private ProjectGroup projectGroup;

    private List<BuildDefinitionSummary> projectBuildDefinitionSummaries = new ArrayList<BuildDefinitionSummary>();

    private List<BuildDefinitionSummary> groupBuildDefinitionSummaries = new ArrayList<BuildDefinitionSummary>();

    private List<BuildDefinitionSummary> allBuildDefinitionSummaries = new ArrayList<BuildDefinitionSummary>();

    //profileName

    public String summarizeForProject()
    {
        try
        {
            projectGroup = getContinuum().getProjectGroupByProjectId( projectId );
            projectGroupId = projectGroup.getId();
            projectGroupName = projectGroup.getName();

            checkViewProjectGroupAuthorization( projectGroupName );

            groupBuildDefinitionSummaries = gatherGroupBuildDefinitionSummaries( projectGroupId );
            projectBuildDefinitionSummaries = gatherProjectBuildDefinitionSummaries( projectId );

            fixDefaultBuildDefinitions();

            allBuildDefinitionSummaries.addAll( groupBuildDefinitionSummaries );
            allBuildDefinitionSummaries.addAll( projectBuildDefinitionSummaries );
        }
        catch ( ContinuumException e )
        {
            getLogger().info( "unable to build summary" );
            return ERROR;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return SUCCESS;
    }

    public String summarizeForGroup()
    {
        try
        {
            groupBuildDefinitionSummaries = gatherGroupBuildDefinitionSummaries( projectGroupId );

            projectGroup = getContinuum().getProjectGroupWithProjects( projectGroupId );

            checkViewProjectGroupAuthorization( projectGroup.getName() );

            for ( Iterator i = projectGroup.getProjects().iterator(); i.hasNext(); )
            {
                Project project = (Project) i.next();
                projectBuildDefinitionSummaries.addAll( gatherProjectBuildDefinitionSummaries( project.getId() ) );

            }

            allBuildDefinitionSummaries.addAll( groupBuildDefinitionSummaries );
            allBuildDefinitionSummaries.addAll( projectBuildDefinitionSummaries );
        }
        catch ( ContinuumException e )
        {
            getLogger().info( "unable to build summary" );
            return ERROR;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        return SUCCESS;
    }

    private void fixDefaultBuildDefinitions()
    {
        boolean containsDefaultBDForProject = false;

        for ( BuildDefinitionSummary bds : projectBuildDefinitionSummaries )
        {
            if ( bds.isIsDefault() )
            {
                containsDefaultBDForProject = true;
            }
        }

        if ( containsDefaultBDForProject )
        {
            for ( BuildDefinitionSummary bds : groupBuildDefinitionSummaries )
            {
                bds.setIsDefault( false );
            }
        }
    }

    private List<BuildDefinitionSummary> gatherProjectBuildDefinitionSummaries( int projectId )
        throws ContinuumException
    {
        List<BuildDefinitionSummary> summaryList = new ArrayList<BuildDefinitionSummary>();

        Project project = getContinuum().getProjectWithAllDetails( projectId );
        for ( Iterator i = project.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinitionSummary bds = generateBuildDefinitionSummary( (BuildDefinition) i.next() );
            bds.setFrom( "PROJECT" );
            bds.setProjectId( project.getId() );
            bds.setProjectName( project.getName() );

            summaryList.add( bds );
        }

        return summaryList;
    }

    private List<BuildDefinitionSummary> gatherGroupBuildDefinitionSummaries( int projectGroupId )
        throws ContinuumException
    {
        List<BuildDefinitionSummary> summaryList = new ArrayList<BuildDefinitionSummary>();

        projectGroup = getContinuum().getProjectGroupWithBuildDetails( projectGroupId );

        for ( Iterator i = projectGroup.getBuildDefinitions().iterator(); i.hasNext(); )
        {
            BuildDefinitionSummary bds = generateBuildDefinitionSummary( (BuildDefinition) i.next() );
            bds.setFrom( "GROUP" );
            bds.setProjectGroupId( projectGroup.getId() );

            summaryList.add( bds );
        }

        return summaryList;
    }

    protected BuildDefinitionSummary generateBuildDefinitionSummary( BuildDefinition bd )
    {
        BuildDefinitionSummary bds = new BuildDefinitionSummary();

        bds.setGoals( bd.getGoals() );
        bds.setId( bd.getId() );
        bds.setArguments( bd.getArguments() );
        bds.setBuildFile( bd.getBuildFile() );
        bds.setScheduleId( bd.getSchedule().getId() );
        bds.setScheduleName( bd.getSchedule().getName() );
        bds.setIsDefault( bd.isDefaultForProject() );
        bds.setIsBuildFresh( bd.isBuildFresh() );
        if ( bd.getProfile() != null )
        {
            bds.setProfileName( bd.getProfile().getName() );
            bds.setProfileId( bd.getProfile().getId() );
        }
        bds.setDescription( bd.getDescription() );
        bds.setType( bd.getType() );
        return bds;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public List getProjectBuildDefinitionSummaries()
    {
        return projectBuildDefinitionSummaries;
    }

    public void setProjectBuildDefinitionSummaries( List<BuildDefinitionSummary> projectBuildDefinitionSummaries )
    {
        this.projectBuildDefinitionSummaries = projectBuildDefinitionSummaries;
    }

    public List<BuildDefinitionSummary> getGroupBuildDefinitionSummaries()
    {
        return groupBuildDefinitionSummaries;
    }

    public void setGroupBuildDefinitionSummaries( List<BuildDefinitionSummary> groupBuildDefinitionSummaries )
    {
        this.groupBuildDefinitionSummaries = groupBuildDefinitionSummaries;
    }

    public List<BuildDefinitionSummary> getAllBuildDefinitionSummaries()
    {
        return allBuildDefinitionSummaries;
    }

    public void setAllBuildDefinitionSummaries( List<BuildDefinitionSummary> allBuildDefinitionSummaries )
    {
        this.allBuildDefinitionSummaries = allBuildDefinitionSummaries;
    }

    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }
}
