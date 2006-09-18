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

import java.util.Collection;
import java.util.Map;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.security.acegi.acl.AclEventHandler;

/**
 * Continuum implementation that just delegates to an actual implementation.
 * Used to weave in the Acegi required aspects.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AcegiContinuum
    extends ContinuumDelegate
{

    private AclEventHandler aclEventHandler;

    public void setAclEventHandler( AclEventHandler eventHandler )
    {
        this.aclEventHandler = eventHandler;
    }

    public AclEventHandler getAclEventHandler()
    {
        return aclEventHandler;
    }

    public BuildDefinition addBuildDefinitionToProject( int projectId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        BuildDefinition definition = getContinuum().addBuildDefinitionToProject( projectId, buildDefinition );
        //getAclEventHandler().afterAddProjectBuildDefinition( definition, projectId );
        return definition;
    }

    public BuildDefinition addBuildDefinitionToProjectGroup( int projectGroupId, BuildDefinition buildDefinition )
        throws ContinuumException
    {
        BuildDefinition definition = getContinuum().addBuildDefinitionToProjectGroup( projectGroupId, buildDefinition );
        //getAclEventHandler().afterAddProjectGroupBuildDefinition( definition, projectGroupId );
        return definition;
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

    public ProjectNotifier addNotifier( int projectId, ProjectNotifier notifier )
        throws ContinuumException
    {
        ProjectNotifier projectNotifier = getContinuum().addNotifier( projectId, notifier );
        //getAclEventHandler().afterAddProjectNotifier( projectNotifier, projectId );
        return projectNotifier;
    }

    public ProjectNotifier addNotifier( int projectId, String notifierType, Map configuration )
        throws ContinuumException
    {
        ProjectNotifier projectNotifier = getContinuum().addNotifier( projectId, notifierType, configuration );
        //getAclEventHandler().afterAddProjectNotifier( projectNotifier, projectId );
        return projectNotifier;
    }

    public int addProject( Project project, String executorId )
        throws ContinuumException
    {
        int projectId = getContinuum().addProject( project, executorId );
        Project addedProject = getContinuum().getProject( projectId );
        getAclEventHandler().afterAddProject( addedProject, addedProject.getProjectGroup().getId() );
        return projectId;
    }

    public Collection getAllProjectGroupsWithProjects()
    {
        Collection groups = getContinuum().getAllProjectGroupsWithProjects();
        //getAclEventHandler().afterReturningProjectGroup( groups );
        return groups;
    }

    public ProjectGroup getProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        ProjectGroup projectGroup = getContinuum().getProjectGroup( projectGroupId );
        return getAclEventHandler().setPermissions( projectGroup );
    }

    public void removeProject( int projectId )
        throws ContinuumException
    {
        removeProject( getProject( projectId ) );
    }

    /**
     * Required for Acegi ACL
     * 
     * @param project
     * @throws ContinuumException
     */
    public void removeProject( Project project )
        throws ContinuumException
    {
        getContinuum().removeProject( project.getId() );
        getAclEventHandler().afterDeleteProject( project.getId() );
    }

    public void removeProjectGroup( int projectGroupId )
        throws ContinuumException
    {
        removeProjectGroup( getProjectGroup( projectGroupId ) );
    }

    /**
     * Required for Acegi ACL
     * 
     * @param projectGroup
     * @throws ContinuumException
     */
    public void removeProjectGroup( ProjectGroup projectGroup )
        throws ContinuumException
    {
        getContinuum().removeProjectGroup( projectGroup.getId() );
        getAclEventHandler().afterDeleteProjectGroup( projectGroup.getId() );
    }

}
