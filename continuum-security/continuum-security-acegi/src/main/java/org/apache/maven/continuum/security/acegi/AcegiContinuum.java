package org.apache.maven.continuum.security.acegi;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.codehaus.plexus.util.dag.CycleDetectedException;

/**
 * Continuum implementation that just delegates to an actual implementation.
 * Used to weave in the Acegi required aspects.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AcegiContinuum
    implements Continuum
{

    private Continuum continuum;

    private AclEventHandler aclEventHandler;

    /**
     * Set the object to delegate to
     * 
     * @param continuum
     */
    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }

    /**
     * Get the object to delegate to
     * 
     * @return the delegated object
     */
    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setAclEventHandler( AclEventHandler eventHandler )
    {
        this.aclEventHandler = eventHandler;
    }

    public AclEventHandler getAclEventHandler()
    {
        return aclEventHandler;
    }

    /**
     * @param projectId
     * @param buildDefinition
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addBuildDefinitionToProject(int, org.apache.maven.continuum.model.project.BuildDefinition)
     */
    public void addBuildDefinitionToProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().addBuildDefinitionToProject( projectId, buildDefinition );
    }

    /**
     * @param projectGroupId
     * @param buildDefinition
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addBuildDefinitionToProjectGroup(int, org.apache.maven.continuum.model.project.BuildDefinition)
     */
    public void addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().addBuildDefinitionToProjectGroup( projectGroupId, buildDefinition );
    }

    /**
     * @param metadataUrl
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addMavenOneProject(java.lang.String)
     */
    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = getContinuum().addMavenOneProject( metadataUrl );
        getAclEventHandler().afterAddProject( result );
        return result;
    }

    /**
     * @param metadataUrl
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addMavenTwoProject(java.lang.String)
     */
    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject( metadataUrl );
        getAclEventHandler().afterAddProject( result );
        return result;
    }

    /**
     * @param projectId
     * @param notifier
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addNotifier(int, org.apache.maven.continuum.model.project.ProjectNotifier)
     */
    public void addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        getContinuum().addNotifier( projectId, notifier );
    }

    /**
     * @param projectId
     * @param notifierType
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addNotifier(int, java.lang.String, java.util.Map)
     */
    public void addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        getContinuum().addNotifier( projectId, notifierType, configuration );
    }

    /**
     * @param project
     * @param executorId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addProject(org.apache.maven.continuum.model.project.Project, java.lang.String)
     */
    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        return getContinuum().addProject( project, executorId );
    }

    /**
     * @param schedule
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public void addSchedule( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().addSchedule( schedule );
    }

    /**
     * @param user
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addUser(org.apache.maven.continuum.model.system.ContinuumUser)
     */
    public void addUser( ContinuumUser user )
        throws ContinuumException
    {
        getContinuum().addUser( user );
    }

    /**
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addUser(java.util.Map)
     */
    public void addUser( Map configuration )
        throws ContinuumException
    {
        getContinuum().addUser( configuration );
    }

    /**
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#addUserGroup(java.util.Map)
     */
    public void addUserGroup( Map configuration )
        throws ContinuumException
    {
        getContinuum().addUserGroup( configuration );
    }

    /**
     * @param userGroup
     * @see org.apache.maven.continuum.Continuum#addUserGroup(org.apache.maven.continuum.model.system.UserGroup)
     */
    public void addUserGroup( UserGroup userGroup )
    {
        getContinuum().addUserGroup( userGroup );
    }

    /**
     * @param projectId
     * @param buildDefinitionId
     * @param trigger
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProject(int, int, int)
     */
    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId, buildDefinitionId, trigger );
    }

    /**
     * @param projectId
     * @param trigger
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProject(int, int)
     */
    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId, trigger );
    }

    /**
     * @param projectId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProject(int)
     */
    public void buildProject( int projectId )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId );
    }

    /**
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProjects()
     */
    public void buildProjects()
        throws ContinuumException
    {
        getContinuum().buildProjects();
    }

    /**
     * @param trigger
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProjects(int)
     */
    public void buildProjects( int trigger )
        throws ContinuumException
    {
        getContinuum().buildProjects( trigger );
    }

    /**
     * @param schedule
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProjects(org.apache.maven.continuum.model.project.Schedule)
     */
    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().buildProjects( schedule );
    }

    /**
     * @param projectId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#checkoutProject(int)
     */
    public void checkoutProject( int projectId )
        throws ContinuumException
    {
        getContinuum().checkoutProject( projectId );
    }

    /**
     * @return
     * @see org.apache.maven.continuum.Continuum#getAllProjectGroupsWithProjects()
     */
    public Collection getAllProjectGroupsWithProjects()
    {
        return getContinuum().getAllProjectGroupsWithProjects();
    }

    /**
     * @param start
     * @param end
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getAllProjects(int, int)
     */
    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return getContinuum().getAllProjects( start, end );
    }

    /**
     * @param start
     * @param end
     * @return
     * @see org.apache.maven.continuum.Continuum#getAllProjectsWithAllDetails(int, int)
     */
    public List getAllProjectsWithAllDetails( int start, int end )
    {
        return getContinuum().getAllProjectsWithAllDetails( start, end );
    }

    /**
     * @param projectId
     * @param buildDefinitionId
     * @return
     * @throws ContinuumException
     * @deprecated
     * @see org.apache.maven.continuum.Continuum#getBuildDefinition(int, int)
     */
    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinition( projectId, buildDefinitionId );
    }

    /**
     * @param buildDefinitionId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildDefinition(int)
     */
    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinition( buildDefinitionId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @deprecated
     * @see org.apache.maven.continuum.Continuum#getBuildDefinitions(int)
     */
    public List getBuildDefinitions( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitions( projectId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildDefinitionsForProject(int)
     */
    public List getBuildDefinitionsForProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitionsForProject( projectId );
    }

    /**
     * @param projectGroupId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildDefinitionsForProjectGroup(int)
     */
    public List getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId );
    }

    /**
     * @param projectId
     * @param buildId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildOutput(int, int)
     */
    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        return getContinuum().getBuildOutput( projectId, buildId );
    }

    /**
     * @param buildId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildResult(int)
     */
    public BuildResult getBuildResult( int buildId )
        throws ContinuumException
    {
        return getContinuum().getBuildResult( buildId );
    }

    /**
     * @param projectId
     * @param buildNumber
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildResultByBuildNumber(int, int)
     */
    public BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException
    {
        return getContinuum().getBuildResultByBuildNumber( projectId, buildNumber );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getBuildResultsForProject(int)
     */
    public Collection getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildResultsForProject( projectId );
    }

    /**
     * @return
     * @see org.apache.maven.continuum.Continuum#getBuildResultsInSuccess()
     */
    public Map getBuildResultsInSuccess()
    {
        return getContinuum().getBuildResultsInSuccess();
    }

    /**
     * @param projectId
     * @param buildResultId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getChangesSinceLastSuccess(int, int)
     */
    public List getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        return getContinuum().getChangesSinceLastSuccess( projectId, buildResultId );
    }

    /**
     * @return
     * @see org.apache.maven.continuum.Continuum#getConfiguration()
     */
    public ConfigurationService getConfiguration()
    {
        return getContinuum().getConfiguration();
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getDefaultBuildDefinition(int)
     */
    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException
    {
        return getContinuum().getDefaultBuildDefinition( projectId );
    }

    /**
     * @param projectId
     * @param directory
     * @param filename
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getFileContent(int, java.lang.String, java.lang.String)
     */
    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        return getContinuum().getFileContent( projectId, directory, filename );
    }

    /**
     * @param projectId
     * @param currentDirectory
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getFiles(int, java.lang.String)
     */
    public List getFiles( int projectId, String currentDirectory )
        throws ContinuumException
    {
        return getContinuum().getFiles( projectId, currentDirectory );
    }

    /**
     * @param projectId
     * @return
     * @see org.apache.maven.continuum.Continuum#getLatestBuildResultForProject(int)
     */
    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        return getContinuum().getLatestBuildResultForProject( projectId );
    }

    /**
     * @return
     * @see org.apache.maven.continuum.Continuum#getLatestBuildResults()
     */
    public Map getLatestBuildResults()
    {
        return getContinuum().getLatestBuildResults();
    }

    /**
     * @param projectId
     * @param notifierId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getNotifier(int, int)
     */
    public ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        return getContinuum().getNotifier( projectId, notifierId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProject(int)
     */
    public Project getProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProject( projectId );
    }

    /**
     * @param projectGroupId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectGroup(int)
     */
    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getProjectGroup( projectGroupId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectGroupByProjectId(int)
     */
    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectGroupByProjectId( projectId );
    }

    /**
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjects()
     */
    public Collection getProjects()
        throws ContinuumException
    {
        return getContinuum().getProjects();
    }

    /**
     * @return
     * @throws CycleDetectedException
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectsInBuildOrder()
     */
    public List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException
    {
        return getContinuum().getProjectsInBuildOrder();
    }

    /**
     * @param projectGroupId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectsInGroup(int)
     */
    public Collection getProjectsInGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getProjectsInGroup( projectGroupId );
    }

    /**
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectsWithDependencies()
     */
    public Collection getProjectsWithDependencies()
        throws ContinuumException
    {
        return getContinuum().getProjectsWithDependencies();
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectWithAllDetails(int)
     */
    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithAllDetails( projectId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectWithBuilds(int)
     */
    public Project getProjectWithBuilds( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithBuilds( projectId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getProjectWithCheckoutResult(int)
     */
    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithCheckoutResult( projectId );
    }

    /**
     * @param id
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getSchedule(int)
     */
    public Schedule getSchedule( int id )
        throws ContinuumException
    {
        return getContinuum().getSchedule( id );
    }

    /**
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getSchedules()
     */
    public Collection getSchedules()
        throws ContinuumException
    {
        return getContinuum().getSchedules();
    }

    /**
     * @return
     * @see org.apache.maven.continuum.Continuum#getSecurity()
     */
    public ContinuumSecurity getSecurity()
    {
        return getContinuum().getSecurity();
    }

    /**
     * @param userId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getUser(int)
     */
    public ContinuumUser getUser( int userId )
        throws ContinuumException
    {
        return getContinuum().getUser( userId );
    }

    /**
     * @param userGroupId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getUserGroup(int)
     */
    public UserGroup getUserGroup( int userGroupId )
        throws ContinuumException
    {
        return getContinuum().getUserGroup( userGroupId );
    }

    /**
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getUserGroups()
     */
    public List getUserGroups()
        throws ContinuumException
    {
        return getContinuum().getUserGroups();
    }

    /**
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getUsers()
     */
    public List getUsers()
        throws ContinuumException
    {
        return getContinuum().getUsers();
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#getWorkingDirectory(int)
     */
    public File getWorkingDirectory( int projectId )
        throws ContinuumException
    {
        return getContinuum().getWorkingDirectory( projectId );
    }

    /**
     * @param projectId
     * @param buildDefinitionId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#isInBuildingQueue(int, int)
     */
    public boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().isInBuildingQueue( projectId, buildDefinitionId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#isInBuildingQueue(int)
     */
    public boolean isInBuildingQueue( int projectId )
        throws ContinuumException
    {
        return getContinuum().isInBuildingQueue( projectId );
    }

    /**
     * @param projectId
     * @return
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#isInCheckoutQueue(int)
     */
    public boolean isInCheckoutQueue( int projectId )
        throws ContinuumException
    {
        return getContinuum().isInCheckoutQueue( projectId );
    }

    /**
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#reloadConfiguration()
     */
    public void reloadConfiguration()
        throws ContinuumException
    {
        getContinuum().reloadConfiguration();
    }

    /**
     * @param projectId
     * @param buildDefinitionId
     * @throws ContinuumException
     * @deprecated
     * @see org.apache.maven.continuum.Continuum#removeBuildDefinition(int, int)
     */
    public void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinition( projectId, buildDefinitionId );
    }

    /**
     * @param projectId
     * @param buildDefinitionId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeBuildDefinitionFromProject(int, int)
     */
    public void removeBuildDefinitionFromProject( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinitionFromProject( projectId, buildDefinitionId );
    }

    /**
     * @param projectGroupId
     * @param buildDefinitionId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeBuildDefinitionFromProjectGroup(int, int)
     */
    public void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );
    }

    /**
     * @param projectId
     * @param notifierId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeNotifier(int, int)
     */
    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        getContinuum().removeNotifier( projectId, notifierId );
    }

    /**
     * @param projectId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeProject(int)
     */
    public void removeProject( int projectId )
        throws ContinuumException
    {
        getContinuum().removeProject( projectId );
    }

    /**
     * @param scheduleId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeSchedule(int)
     */
    public void removeSchedule( int scheduleId )
        throws ContinuumException
    {
        getContinuum().removeSchedule( scheduleId );
    }

    /**
     * @param userId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeUser(int)
     */
    public void removeUser( int userId )
        throws ContinuumException
    {
        getContinuum().removeUser( userId );
    }

    /**
     * @param userGroupId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeUserGroup(int)
     */
    public void removeUserGroup( int userGroupId )
        throws ContinuumException
    {
        getContinuum().removeUserGroup( userGroupId );
    }

    /**
     * @param projectId
     * @param buildDefinition
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateBuildDefinitionForProject(int, org.apache.maven.continuum.model.project.BuildDefinition)
     */
    public void updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().updateBuildDefinitionForProject( projectId, buildDefinition );
    }

    /**
     * @param projectGroupId
     * @param buildDefinition
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateBuildDefinitionForProjectGroup(int, org.apache.maven.continuum.model.project.BuildDefinition)
     */
    public void updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().updateBuildDefinitionForProjectGroup( projectGroupId, buildDefinition );
    }

    /**
     * @param parameters
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateConfiguration(java.util.Map)
     */
    public void updateConfiguration( Map parameters )
        throws ContinuumException
    {
        getContinuum().updateConfiguration( parameters );
    }

    /**
     * @param projectId
     * @param notifierId
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateNotifier(int, int, java.util.Map)
     */
    public void updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateNotifier( projectId, notifierId, configuration );
    }

    /**
     * @param projectId
     * @param notifier
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateNotifier(int, org.apache.maven.continuum.model.project.ProjectNotifier)
     */
    public void updateNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        getContinuum().updateNotifier( projectId, notifier );
    }

    /**
     * @param project
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateProject(org.apache.maven.continuum.model.project.Project)
     */
    public void updateProject( Project project )
        throws ContinuumException
    {
        getContinuum().updateProject( project );
    }

    /**
     * @param scheduleId
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateSchedule(int, java.util.Map)
     */
    public void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateSchedule( scheduleId, configuration );
    }

    /**
     * @param schedule
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateSchedule(org.apache.maven.continuum.model.project.Schedule)
     */
    public void updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().updateSchedule( schedule );
    }

    /**
     * @param user
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateUser(org.apache.maven.continuum.model.system.ContinuumUser)
     */
    public void updateUser( ContinuumUser user )
        throws ContinuumException
    {
        getContinuum().updateUser( user );
    }

    /**
     * @param userId
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateUser(int, java.util.Map)
     */
    public void updateUser( int userId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateUser( userId, configuration );
    }

    /**
     * @param userGroupId
     * @param configuration
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateUserGroup(int, java.util.Map)
     */
    public void updateUserGroup( int userGroupId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateUserGroup( userGroupId, configuration );
    }

    /**
     * @param userGroup
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#updateUserGroup(org.apache.maven.continuum.model.system.UserGroup)
     */
    public void updateUserGroup( UserGroup userGroup )
        throws ContinuumException
    {
        getContinuum().updateUserGroup( userGroup );
    }

    /**
     * @param projectGroupId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#buildProjectGroup(int)
     */
    public void buildProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        getContinuum().buildProjectGroup( projectGroupId );
    }

    /**
     * @param projectGroupId
     * @throws ContinuumException
     * @see org.apache.maven.continuum.Continuum#removeProjectGroup(int)
     */
    public void removeProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        getContinuum().removeProjectGroup( projectGroupId );
    }

}
