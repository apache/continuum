package org.apache.maven.continuum.security.acegi.aspectj;

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
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.ContinuumSecurity;
import org.codehaus.plexus.util.dag.CycleDetectedException;

/**
 * Stub implementation of {@link Continuum} with empty implementations.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ContinuumStub
    implements Continuum
{

    public void addBuildDefinition( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
    }

    public void addBuildDefinitionFromParams( int projectId, Map configuration )
        throws ContinuumException
    {
    }

    public ContinuumProjectBuildingResult addMavenOneProject( String metadataUrl )
        throws ContinuumException
    {
        return null;
    }

    public ContinuumProjectBuildingResult addMavenTwoProject( String metadataUrl )
        throws ContinuumException
    {
        return null;
    }

    public void addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
    }

    public void addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
    }

    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        return 0;
    }

    public void addSchedule( Schedule schedule )
        throws ContinuumException
    {
    }

    public void addUser( ContinuumUser user )
        throws ContinuumException
    {
    }

    public void addUser( Map configuration )
        throws ContinuumException
    {
    }

    public void addUserGroup( UserGroup userGroup )
    {
    }

    public void addUserGroup( Map configuration )
        throws ContinuumException
    {
    }

    public void buildProject( int projectId )
        throws ContinuumException
    {
    }

    public void buildProject( int projectId, int trigger )
        throws ContinuumException
    {
    }

    public void buildProject( int projectId, int buildDefinitionId, int trigger )
        throws ContinuumException
    {
    }

    public void buildProjects()
        throws ContinuumException
    {
    }

    public void buildProjects( int trigger )
        throws ContinuumException
    {
    }

    public void buildProjects( Schedule schedule )
        throws ContinuumException
    {
    }

    public void checkoutProject( int projectId )
        throws ContinuumException
    {
    }

    public Collection getAllProjects( int start, int end )
        throws ContinuumException
    {
        return null;
    }

    public List getAllProjectsWithAllDetails( int start, int end )
    {
        return null;
    }

    public BuildDefinition getBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return null;
    }

    public List getBuildDefinitions( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public String getBuildOutput( int projectId, int buildId )
        throws ContinuumException
    {
        return null;
    }

    public BuildResult getBuildResult( int buildId )
        throws ContinuumException
    {
        return null;
    }

    public BuildResult getBuildResultByBuildNumber( int projectId, int buildNumber )
        throws ContinuumException
    {
        return null;
    }

    public Collection getBuildResultsForProject( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public Map getBuildResultsInSuccess()
    {
        return null;
    }

    public List getChangesSinceLastSuccess( int projectId, int buildResultId )
        throws ContinuumException
    {
        return null;
    }

    public ConfigurationService getConfiguration()
    {
        return null;
    }

    public BuildDefinition getDefaultBuildDefinition( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public String getFileContent( int projectId, String directory, String filename )
        throws ContinuumException
    {
        return null;
    }

    public List getFiles( int projectId, String currentDirectory )
        throws ContinuumException
    {
        return null;
    }

    public BuildResult getLatestBuildResultForProject( int projectId )
    {
        return null;
    }

    public Map getLatestBuildResults()
    {
        return null;
    }

    public ProjectNotifier getNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
        return null;
    }

    public Project getProject( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public Project getProjectWithAllDetails( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public Project getProjectWithBuilds( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public Project getProjectWithCheckoutResult( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public Collection getProjects()
        throws ContinuumException
    {
        return null;
    }

    public List getProjectsInBuildOrder()
        throws CycleDetectedException, ContinuumException
    {
        return null;
    }

    public Collection getProjectsWithDependencies()
        throws ContinuumException
    {
        return null;
    }

    public Schedule getSchedule( int id )
        throws ContinuumException
    {
        return null;
    }

    public Collection getSchedules()
        throws ContinuumException
    {
        return null;
    }

    public ContinuumSecurity getSecurity()
    {
        return null;
    }

    public ContinuumUser getUser( int userId )
        throws ContinuumException
    {
        return null;
    }

    public UserGroup getUserGroup( int userGroupId )
        throws ContinuumException
    {
        return null;
    }

    public List getUserGroups()
        throws ContinuumException
    {
        return null;
    }

    public List getUsers()
        throws ContinuumException
    {
        return null;
    }

    public File getWorkingDirectory( int projectId )
        throws ContinuumException
    {
        return null;
    }

    public boolean isInBuildingQueue( int projectId )
        throws ContinuumException
    {
        return false;
    }

    public boolean isInBuildingQueue( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
        return false;
    }

    public boolean isInCheckoutQueue( int projectId )
        throws ContinuumException
    {
        return false;
    }

    public void reloadConfiguration()
        throws ContinuumException
    {
    }

    public void removeBuildDefinition( int projectId, int buildDefinitionId )
        throws ContinuumException
    {
    }

    public void removeNotifier( int projectId, int notifierId )
        throws ContinuumException
    {
    }

    public void removeProject( int projectId )
        throws ContinuumException
    {
    }

    public void removeSchedule( int scheduleId )
        throws ContinuumException
    {
    }

    public void removeUser( int userId )
        throws ContinuumException
    {
    }

    public void removeUserGroup( int userGroupId )
        throws ContinuumException
    {
    }

    public void updateBuildDefinition( BuildDefinition buildDefinition, int projectId )
        throws ContinuumException
    {
    }

    public void updateBuildDefinition( int projectId, int buildDefinitionId, Map configuration )
        throws ContinuumException
    {
    }

    public void updateConfiguration( Map parameters )
        throws ContinuumException
    {
    }

    public void updateNotifier( int projectId, int notifierId, Map configuration )
        throws ContinuumException
    {
    }

    public void updateNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
    }

    public void updateProject( Project project )
        throws ContinuumException
    {
    }

    public void updateSchedule( Schedule schedule )
        throws ContinuumException
    {
    }

    public void updateSchedule( int scheduleId, Map configuration )
        throws ContinuumException
    {
    }

    public void updateUser( ContinuumUser user )
        throws ContinuumException
    {
    }

    public void updateUser( int userId, Map configuration )
        throws ContinuumException
    {
    }

    public void updateUserGroup( UserGroup userGroup )
        throws ContinuumException
    {
    }

    public void updateUserGroup( int userGroupId, Map configuration )
        throws ContinuumException
    {
    }

}
