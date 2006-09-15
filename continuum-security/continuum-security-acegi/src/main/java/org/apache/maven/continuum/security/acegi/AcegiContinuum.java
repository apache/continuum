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
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.apache.maven.continuum.security.acegi.acl.AclEventHandler;
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

    public void addBuildDefinitionToProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().addBuildDefinitionToProject( projectId, buildDefinition );
    }

    public void addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().addBuildDefinitionToProjectGroup( projectGroupId, buildDefinition );
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = getContinuum().addMavenOneProject( metadataUrl );
        getAclEventHandler().afterAddProject( result );
        return result;
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = getContinuum().addMavenTwoProject( metadataUrl );
        getAclEventHandler().afterAddProject( result );
        return result;
    }

    public void addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        getContinuum().addNotifier( projectId, notifier );
    }

    public void addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        getContinuum().addNotifier( projectId, notifierType, configuration );
    }

    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        int projectId = getContinuum().addProject( project, executorId );
        Project addedProject = getContinuum().getProject( projectId );
        getAclEventHandler().afterAddProject( addedProject, addedProject.getProjectGroup().getId() );
        return projectId;
    }

    public void addSchedule( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().addSchedule( schedule );
    }

    public void addUser( ContinuumUser user )
        throws ContinuumException
    {
        getContinuum().addUser( user );
    }

    public void addUser( Map configuration )
        throws ContinuumException
    {
        getContinuum().addUser( configuration );
    }

    public void addUserGroup( Map configuration )
        throws ContinuumException
    {
        getContinuum().addUserGroup( configuration );
    }

    public void addUserGroup( UserGroup userGroup )
    {
        getContinuum().addUserGroup( userGroup );
    }

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId, buildDefinitionId, trigger );
    }

    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId, trigger );
    }

    public void buildProject( int projectId )
        throws ContinuumException
    {
        getContinuum().buildProject( projectId );
    }

    public void buildProjects()
        throws ContinuumException
    {
        getContinuum().buildProjects();
    }

    public void buildProjects( int trigger )
        throws ContinuumException
    {
        getContinuum().buildProjects( trigger );
    }

    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().buildProjects( schedule );
    }

    public void checkoutProject( int projectId )
        throws ContinuumException
    {
        getContinuum().checkoutProject( projectId );
    }

    public Collection getAllProjectGroupsWithProjects()
    {
        return getContinuum().getAllProjectGroupsWithProjects();
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return getContinuum().getAllProjects( start, end );
    }

    public List getAllProjectsWithAllDetails( int start, int end )
    {
        return getContinuum().getAllProjectsWithAllDetails( start, end );
    }

    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinition( projectId, buildDefinitionId );
    }

    public BuildDefinition getBuildDefinition( int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinition( buildDefinitionId );
    }

    public List getBuildDefinitions( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitions( projectId );
    }

    public List getBuildDefinitionsForProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitionsForProject( projectId );
    }

    public List getBuildDefinitionsForProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getBuildDefinitionsForProjectGroup( projectGroupId );
    }

    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        return getContinuum().getBuildOutput( projectId, buildId );
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumException
    {
        return getContinuum().getBuildResult( buildId );
    }

    public BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException
    {
        return getContinuum().getBuildResultByBuildNumber( projectId, buildNumber );
    }

    public Collection getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getBuildResultsForProject( projectId );
    }

    public Map getBuildResultsInSuccess()
    {
        return getContinuum().getBuildResultsInSuccess();
    }

    public List getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        return getContinuum().getChangesSinceLastSuccess( projectId, buildResultId );
    }

    public ConfigurationService getConfiguration()
    {
        return getContinuum().getConfiguration();
    }

    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException
    {
        return getContinuum().getDefaultBuildDefinition( projectId );
    }

    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        return getContinuum().getFileContent( projectId, directory, filename );
    }

    public List getFiles( int projectId, String currentDirectory )
        throws ContinuumException
    {
        return getContinuum().getFiles( projectId, currentDirectory );
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        return getContinuum().getLatestBuildResultForProject( projectId );
    }

    public Map getLatestBuildResults()
    {
        return getContinuum().getLatestBuildResults();
    }

    public ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        return getContinuum().getNotifier( projectId, notifierId );
    }

    public Project getProject( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProject( projectId );
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getProjectGroup( projectGroupId );
    }

    public ProjectGroup getProjectGroupByProjectId( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectGroupByProjectId( projectId );
    }

    public Collection getProjects()
        throws ContinuumException
    {
        return getContinuum().getProjects();
    }

    public List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException
    {
        return getContinuum().getProjectsInBuildOrder();
    }

    public Collection getProjectsInGroup( int projectGroupId )
        throws ContinuumException
    {
        return getContinuum().getProjectsInGroup( projectGroupId );
    }

    public Collection getProjectsWithDependencies()
        throws ContinuumException
    {
        return getContinuum().getProjectsWithDependencies();
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithAllDetails( projectId );
    }

    public Project getProjectWithBuilds( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithBuilds( projectId );
    }

    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException
    {
        return getContinuum().getProjectWithCheckoutResult( projectId );
    }

    public Schedule getSchedule( int id )
        throws ContinuumException
    {
        return getContinuum().getSchedule( id );
    }

    public Collection getSchedules()
        throws ContinuumException
    {
        return getContinuum().getSchedules();
    }

    public ContinuumSecurity getSecurity()
    {
        return getContinuum().getSecurity();
    }

    public ContinuumUser getUser( int userId )
        throws ContinuumException
    {
        return getContinuum().getUser( userId );
    }

    public UserGroup getUserGroup( int userGroupId )
        throws ContinuumException
    {
        return getContinuum().getUserGroup( userGroupId );
    }

    public List getUserGroups()
        throws ContinuumException
    {
        return getContinuum().getUserGroups();
    }

    public List getUsers()
        throws ContinuumException
    {
        return getContinuum().getUsers();
    }

    public File getWorkingDirectory( int projectId )
        throws ContinuumException
    {
        return getContinuum().getWorkingDirectory( projectId );
    }

    public boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return getContinuum().isInBuildingQueue( projectId, buildDefinitionId );
    }

    public boolean isInBuildingQueue( int projectId )
        throws ContinuumException
    {
        return getContinuum().isInBuildingQueue( projectId );
    }

    public boolean isInCheckoutQueue( int projectId )
        throws ContinuumException
    {
        return getContinuum().isInCheckoutQueue( projectId );
    }

    public void reloadConfiguration()
        throws ContinuumException
    {
        getContinuum().reloadConfiguration();
    }

    public void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinition( projectId, buildDefinitionId );
    }

    public void removeBuildDefinitionFromProject( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinitionFromProject( projectId, buildDefinitionId );
    }

    public void removeBuildDefinitionFromProjectGroup( int projectGroupId, int buildDefinitionId )
        throws ContinuumException
    {
        getContinuum().removeBuildDefinitionFromProjectGroup( projectGroupId, buildDefinitionId );
    }

    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        getContinuum().removeNotifier( projectId, notifierId );
    }

    public void removeProject( int projectId )
        throws ContinuumException
    {
        getContinuum().removeProject( projectId );
    }

    public void removeSchedule( int scheduleId )
        throws ContinuumException
    {
        getContinuum().removeSchedule( scheduleId );
    }

    public void removeUser( int userId )
        throws ContinuumException
    {
        getContinuum().removeUser( userId );
    }

    public void removeUserGroup( int userGroupId )
        throws ContinuumException
    {
        getContinuum().removeUserGroup( userGroupId );
    }

    public void updateBuildDefinitionForProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().updateBuildDefinitionForProject( projectId, buildDefinition );
    }

    public void updateBuildDefinitionForProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        getContinuum().updateBuildDefinitionForProjectGroup( projectGroupId, buildDefinition );
    }

    public void updateConfiguration( Map parameters )
        throws ContinuumException
    {
        getContinuum().updateConfiguration( parameters );
    }

    public void updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateNotifier( projectId, notifierId, configuration );
    }

    public void updateNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        getContinuum().updateNotifier( projectId, notifier );
    }

    public void updateProject( Project project )
        throws ContinuumException
    {
        getContinuum().updateProject( project );
    }

    public void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateSchedule( scheduleId, configuration );
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumException
    {
        getContinuum().updateSchedule( schedule );
    }

    public void updateUser( ContinuumUser user )
        throws ContinuumException
    {
        getContinuum().updateUser( user );
    }

    public void updateUser( int userId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateUser( userId, configuration );
    }

    public void updateUserGroup( int userGroupId, Map configuration )
        throws ContinuumException
    {
        getContinuum().updateUserGroup( userGroupId, configuration );
    }

    public void updateUserGroup( UserGroup userGroup )
        throws ContinuumException
    {
        getContinuum().updateUserGroup( userGroup );
    }

    public void buildProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        getContinuum().buildProjectGroup( projectGroupId );
    }

    public void removeProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        getContinuum().removeProjectGroup( projectGroupId );
        getAclEventHandler().afterDeleteProjectGroup( projectGroupId );
    }

    public ContinuumReleaseManager getReleaseManager()
    {
        return continuum.getReleaseManager();
    }

}
