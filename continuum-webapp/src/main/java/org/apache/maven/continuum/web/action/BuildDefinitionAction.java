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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionService;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.profile.ProfileException;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.exception.ContinuumActionException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.codehaus.plexus.util.StringUtils;

/**
 * BuildDefinitionAction:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildDefinition"
 */
public class BuildDefinitionAction
    extends ContinuumConfirmAction
{
    private int buildDefinitionId;

    private int projectId;

    private int projectGroupId;

    private int scheduleId;

    private boolean defaultBuildDefinition;

    private boolean confirmed = false;

    private String executor;

    private String goals;

    private String arguments;

    private String buildFile;

    private boolean buildFresh;

    private Map<Integer, String> schedules;

    private List<Profile> profiles;

    private boolean groupBuildDefinition = false;

    private boolean groupBuildView = false;

    private String projectGroupName = "";

    private int profileId;

    private String description;

    private List<String> buildDefinitionTypes;

    private String buildDefinitionType;

    private boolean alwaysBuild;

    /**
     * @plexus.requirement
     */
    private BuildDefinitionService buildDefinitionService;

    @Override
    public void prepare()
        throws Exception
    {
        super.prepare();

        if ( schedules == null )
        {
            schedules = new HashMap<Integer, String>();

            Collection<Schedule> allSchedules = getContinuum().getSchedules();

            for ( Schedule schedule : allSchedules )
            {
                schedules.put( schedule.getId(), schedule.getName() );
            }
        }

        // todo: missing from continuum, investigate
        if ( profiles == null )
        {
            profiles = this.getContinuum().getProfileService().getAllProfiles();
        }

        buildDefinitionTypes = new ArrayList<String>();
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR );
        buildDefinitionTypes.add( ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR );
    }

    /**
     * if there is a build definition id set, then retrieve it..either way set us to up to work with build definition
     *
     * @return action result
     */
    @Override
    public String input()
        throws ContinuumException, ContinuumStoreException, BuildDefinitionServiceException
    {
        try
        {
            if ( executor == null )
            {
                if ( projectId != 0 )
                {
                    executor = getContinuum().getProject( projectId ).getExecutorId();
                }
                else
                {
                    List<Project> projects = getContinuum().getProjectGroupWithProjects( projectGroupId ).getProjects();

                    if ( projects.size() > 0 )
                    {
                        Project project = projects.get( 0 );
                        executor = project.getExecutorId();
                    }
                }
            }

            if ( buildDefinitionId != 0 )
            {
                if ( projectId != 0 )
                {
                    checkModifyProjectBuildDefinitionAuthorization( getProjectGroupName() );
                }
                else
                {
                    checkModifyGroupBuildDefinitionAuthorization( getProjectGroupName() );
                }

                BuildDefinition buildDefinition = getContinuum().getBuildDefinition( buildDefinitionId );
                goals = buildDefinition.getGoals();
                arguments = buildDefinition.getArguments();
                buildFile = buildDefinition.getBuildFile();
                buildFresh = buildDefinition.isBuildFresh();
                scheduleId = buildDefinition.getSchedule().getId();
                defaultBuildDefinition = buildDefinition.isDefaultForProject();
                Profile profile = buildDefinition.getProfile();
                if ( profile != null )
                {
                    profileId = profile.getId();
                }
                description = buildDefinition.getDescription();
                buildDefinitionType = buildDefinition.getType();
                alwaysBuild = buildDefinition.isAlwaysBuild();
            }
            else
            {
                String preDefinedBuildFile = "";

                if ( projectId != 0 )
                {
                    checkAddProjectBuildDefinitionAuthorization( getProjectGroupName() );
                    BuildDefinition bd = getContinuum().getDefaultBuildDefinition( projectId );
                    if ( bd != null )
                    {
                        preDefinedBuildFile = bd.getBuildFile();
                    }
                }
                else
                {
                    checkAddGroupBuildDefinitionAuthorization( getProjectGroupName() );
                    List<BuildDefinition> bds = getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId );
                    if ( bds != null && !bds.isEmpty() )
                    {
                        preDefinedBuildFile = bds.get( 0 ).getBuildFile();
                    }
                }

                if ( StringUtils.isEmpty( preDefinedBuildFile ) )
                {
                    if ( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( executor ) )
                    {
                        buildFile =
                            ( (BuildDefinition) buildDefinitionService.getDefaultMavenTwoBuildDefinitionTemplate().getBuildDefinitions().get(
                                0 ) ).getBuildFile();
                        buildDefinitionType = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;
                    }
                    else if ( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR.equals( executor ) )
                    {
                        buildFile =
                            ( (BuildDefinition) buildDefinitionService.getDefaultMavenOneBuildDefinitionTemplate().getBuildDefinitions().get(
                                0 ) ).getBuildFile();
                        buildDefinitionType = ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR;
                    }
                    else if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( executor ) )
                    {
                        buildFile =
                            ( (BuildDefinition) buildDefinitionService.getDefaultAntBuildDefinitionTemplate().getBuildDefinitions().get(
                                0 ) ).getBuildFile();
                        buildDefinitionType = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;
                    }
                    else
                    {
                        buildDefinitionType = ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR;
                    }
                }
                else
                {
                    buildFile = preDefinedBuildFile;
                }
            }

            // if buildDefinitionType is null it will find with the executor
            if ( StringUtils.isEmpty( buildDefinitionType ) )
            {
                if ( ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR.equals( executor ) )
                {
                    buildDefinitionType = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;
                }
                else if ( ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR.equals( executor ) )
                {
                    buildDefinitionType = ContinuumBuildExecutorConstants.MAVEN_ONE_BUILD_EXECUTOR;
                }
                else if ( ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR.equals( executor ) )
                {
                    buildDefinitionType = ContinuumBuildExecutorConstants.ANT_BUILD_EXECUTOR;
                }
                else
                {
                    buildDefinitionType = ContinuumBuildExecutorConstants.SHELL_BUILD_EXECUTOR;
                }
            }

        }
        catch ( AuthorizationRequiredException authzE )
        {
            return REQUIRES_AUTHORIZATION;
        }

        return SUCCESS;
    }

    public String saveBuildDefinition()
        throws ContinuumException, ProfileException
    {
        if ( projectId != 0 && !groupBuildDefinition )
        {
            return saveToProject();
        }
        else
        {
            return saveToGroup();
        }
    }

    public String saveToProject()
        throws ContinuumException, ProfileException
    {

        try
        {
            if ( buildDefinitionId == 0 )
            {
                checkAddProjectBuildDefinitionAuthorization( getProjectGroupName() );

                getContinuum().addBuildDefinitionToProject( projectId, getBuildDefinitionFromInput() );
            }
            else
            {
                checkModifyProjectBuildDefinitionAuthorization( getProjectGroupName() );

                getContinuum().updateBuildDefinitionForProject( projectId, getBuildDefinitionFromInput() );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        
        String resource = "Project id=" + projectId + ":" +  goals + " " + arguments;
        AuditLog event = new AuditLog( resource, AuditLogConstants.ADD_GOAL );
        event.setCategory( AuditLogConstants.BUILD_DEFINITION );
        event.setCurrentUser( getPrincipal() );
        event.log();

        if ( groupBuildView )
        {
            return "success_group";
        }

        return SUCCESS;
    }

    public String saveToGroup()
        throws ContinuumException, ProfileException
    {
        try
        {
            BuildDefinition newBuildDef = getBuildDefinitionFromInput();

            if ( getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId ).size() == 0 )
            {
                newBuildDef.setDefaultForProject( true );
            }

            if ( buildDefinitionId == 0 )
            {
                checkAddGroupBuildDefinitionAuthorization( getProjectGroupName() );

                getContinuum().addBuildDefinitionToProjectGroup( projectGroupId, newBuildDef );
            }
            else
            {
                checkModifyGroupBuildDefinitionAuthorization( getProjectGroupName() );

                getContinuum().updateBuildDefinitionForProjectGroup( projectGroupId, newBuildDef );
            }
        }
        catch ( ContinuumActionException cae )
        {
            addActionError( cae.getMessage() );
            return INPUT;
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        if ( projectId != 0 )
        {
            String resource = "Project id=" + projectId + ":" +  goals + " " + arguments;
            AuditLog event = new AuditLog( resource, AuditLogConstants.ADD_GOAL );
            event.setCategory( AuditLogConstants.BUILD_DEFINITION );
            event.setCurrentUser( getPrincipal() );
            event.log();
            
            return SUCCESS;
        }
        else
        {
            String resource = "Project Group id=" + projectGroupId + ":" + goals + " " + arguments;
            AuditLog event = new AuditLog( resource, AuditLogConstants.ADD_GOAL );
            event.setCategory( AuditLogConstants.BUILD_DEFINITION );
            event.setCurrentUser( getPrincipal() );
            event.log();
            return "success_group";
        }
    }

    public String removeFromProject()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectBuildDefinitionAuthorization( getProjectGroupName() );

            if ( confirmed )
            {
                getContinuum().removeBuildDefinitionFromProject( projectId, buildDefinitionId );
                
                String resource = "Project id=" + projectId + ":" +  goals + " " + arguments;
                AuditLog event = new AuditLog( resource, AuditLogConstants.REMOVE_GOAL );
                event.setCategory( AuditLogConstants.BUILD_DEFINITION );
                event.setCurrentUser( getPrincipal() );
                event.log();

                return SUCCESS;
            }
            else
            {
                BuildDefinition buildDefinition = getContinuum().getBuildDefinition( buildDefinitionId );
                this.description = buildDefinition.getDescription();
                this.goals = buildDefinition.getGoals();
                return CONFIRM;
            }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
    }

    public String removeFromProjectGroup()
        throws ContinuumException
    {
        try
        {
            checkRemoveGroupBuildDefinitionAuthorization( getProjectGroupName() );

            if ( confirmed )
            {
                getContinuum().removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );
                
                String resource = "Project Group id=" + projectGroupId + ":" +  goals + " " + arguments;
                AuditLog event = new AuditLog( resource, AuditLogConstants.REMOVE_GOAL );
                event.setCategory( AuditLogConstants.BUILD_DEFINITION );
                event.setCurrentUser( getPrincipal() );
                event.log();

                return SUCCESS;
            }
            else
            {
                BuildDefinition buildDefinition = getContinuum().getBuildDefinition( buildDefinitionId );
                this.description = buildDefinition.getDescription();
                this.goals = buildDefinition.getGoals();
                return CONFIRM;
            }
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
    }

    private BuildDefinition getBuildDefinitionFromInput()
        throws ContinuumActionException, ProfileException
    {

        Schedule schedule;

        try
        {
            schedule = getContinuum().getSchedule( scheduleId );
        }
        catch ( ContinuumException e )
        {
            addActionError( getText( "unable to get schedule" ) );
            throw new ContinuumActionException( "unable to get schedule" );
        }

        BuildDefinition buildDefinition = new BuildDefinition();

        if ( buildDefinitionId != 0 )
        {
            buildDefinition.setId( buildDefinitionId );
        }
        buildDefinition.setGoals( goals );
        buildDefinition.setArguments( arguments );
        buildDefinition.setBuildFile( buildFile );
        buildDefinition.setBuildFresh( buildFresh );
        buildDefinition.setDefaultForProject( defaultBuildDefinition );
        buildDefinition.setSchedule( schedule );
        if ( profileId != -1 )
        {
            Profile profile = getContinuum().getProfileService().getProfile( profileId );
            if ( profile != null )
            {
                buildDefinition.setProfile( profile );
            }
        }
        buildDefinition.setDescription( StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeXml( description ) ) );
        buildDefinition.setType( buildDefinitionType );
        buildDefinition.setAlwaysBuild( alwaysBuild );
        return buildDefinition;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public void setBuildDefinitionId( final int buildDefinitionId )
    {
        this.buildDefinitionId = buildDefinitionId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( final int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( final int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getScheduleId()
    {
        return scheduleId;
    }

    public void setScheduleId( final int scheduleId )
    {
        this.scheduleId = scheduleId;
    }

    public boolean isDefaultBuildDefinition()
    {
        return defaultBuildDefinition;
    }

    public void setDefaultBuildDefinition( final boolean defaultBuildDefinition )
    {
        this.defaultBuildDefinition = defaultBuildDefinition;
    }

    @Override
    public boolean isConfirmed()
    {
        return confirmed;
    }

    @Override
    public void setConfirmed( final boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor( final String executor )
    {
        this.executor = executor;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals( final String goals )
    {
        this.goals = goals;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments( final String arguments )
    {
        this.arguments = arguments;
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile( final String buildFile )
    {
        this.buildFile = buildFile;
    }

    public boolean isBuildFresh()
    {
        return buildFresh;
    }

    public void setBuildFresh( final boolean buildFresh )
    {
        this.buildFresh = buildFresh;
    }

    public Map<Integer, String> getSchedules()
    {
        return schedules;
    }

    public void setSchedules( final Map<Integer, String> schedules )
    {
        this.schedules = schedules;
    }

    public List<Profile> getProfiles()
    {
        return profiles;
    }

    public void setProfiles( final List<Profile> profiles )
    {
        this.profiles = profiles;
    }

    public boolean isGroupBuildDefinition()
    {
        return groupBuildDefinition;
    }

    public void setGroupBuildDefinition( final boolean groupBuildDefinition )
    {
        this.groupBuildDefinition = groupBuildDefinition;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( projectGroupName == null || "".equals( projectGroupName ) )
        {
            if ( projectGroupId != 0 )
            {
                projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
            }
            else
            {
                projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
            }
        }

        return projectGroupName;
    }

    public int getProfileId()
    {
        return profileId;
    }

    public void setProfileId( final int profileId )
    {
        this.profileId = profileId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    public String getBuildDefinitionType()
    {
        return buildDefinitionType;
    }

    public void setBuildDefinitionType( final String buildDefinitionType )
    {
        this.buildDefinitionType = buildDefinitionType;
    }

    public List<String> getBuildDefinitionTypes()
    {
        return buildDefinitionTypes;
    }

    public boolean isAlwaysBuild()
    {
        return alwaysBuild;
    }

    public void setAlwaysBuild( final boolean alwaysBuild )
    {
        this.alwaysBuild = alwaysBuild;
    }

    public boolean isGroupBuildView()
    {
        return groupBuildView;
    }

    public void setGroupBuildView( final boolean groupBuildView )
    {
        this.groupBuildView = groupBuildView;
    }

}
