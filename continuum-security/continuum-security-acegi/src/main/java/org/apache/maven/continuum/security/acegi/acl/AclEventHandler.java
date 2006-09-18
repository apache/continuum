package org.apache.maven.continuum.security.acegi.acl;

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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.user.acegi.AclManager;
import org.apache.maven.user.model.InstancePermissions;
import org.apache.maven.user.model.User;

/**
 * Utility class to handle ACL manipulation on Continuum events, like adding or
 * removing projects, adding or removing project groups,...
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclEventHandler
    extends AclManager
{
    public static final String ROLE = AclEventHandler.class.getName();

    /**
     * Create ACLs for new {@link ProjectGroup} and {@link Project}s
     * 
     * @param result
     */
    public void afterAddProject( ContinuumProjectBuildingResult result )
    {
        List projectGroups = result.getProjectGroups();
        if ( projectGroups.size() > 0 )
        {
            createNewProjectGroupsACLs( projectGroups );

            if ( projectGroups.size() > 1 )
            {
                throw new RuntimeException( "Adding a project has returned more than one project group: "
                    + projectGroups );
            }
            ProjectGroup projectGroup = (ProjectGroup) projectGroups.iterator().next();
            createNewProjectsACLs( result.getProjects(), projectGroup.getId() );
        }
    }

    public void afterAddProjectBuildDefinition( BuildDefinition buildDefinition, int projectId )
    {
        afterAddProjectDependentObject( buildDefinition, buildDefinition.getId(), projectId );
    }

    public void afterAddProjectGroupBuildDefinition( BuildDefinition buildDefinition, int projectGroupId )
    {
        afterAddProjectGroupDependentObject( buildDefinition, buildDefinition.getId(), projectGroupId );
    }

    public void afterAddProjectNotifier( ProjectNotifier notifier, int projectId )
    {
        afterAddProjectGroupDependentObject( notifier, notifier.getId(), projectId );
    }

    public void afterAddProjectGroupNotifier( ProjectNotifier notifier, int projectGroupId )
    {
        afterAddProjectGroupDependentObject( notifier, notifier.getId(), projectGroupId );
    }

    /**
     * Delete {@link ProjectGroup} ACLs
     * 
     * @param projectGroupId
     */
    public void afterDeleteProjectGroup( int projectGroupId )
    {
        delete( ProjectGroup.class, new Integer( projectGroupId ) );
    }

    public void afterDeleteProject( int projectId )
    {
        delete( Project.class, new Integer( projectId ) );
    }

    /**
     * Set {@link ProjectGroup} permissions in all objects
     * 
     * @param projectGroups
     */
    public void afterReturningProjectGroup( Collection projectGroups )
    {
        Iterator it = projectGroups.iterator();
        while ( it.hasNext() )
        {
            ProjectGroup projectGroup = (ProjectGroup) it.next();
            //            projectGroup.s
        }
    }

    /**
     * Call this method when new {@link ProjectGroup}s are created.
     * 
     * @param projectGroups
     */
    protected void createNewProjectGroupsACLs( Collection projectGroups )
    {
        Iterator it = projectGroups.iterator();
        while ( it.hasNext() )
        {
            ProjectGroup projectGroup = (ProjectGroup) it.next();
            createNewProjectGroupACL( projectGroup );
        }
    }

    /**
     * Creator of {@link ProjectGroup} has Administration permissions.
     * 
     * @param projectGroup
     */
    protected void createNewProjectGroupACL( ProjectGroup projectGroup )
    {
        InstancePermissions permission = new InstancePermissions();
        User user = new User();
        user.setUsername( getCurrentUserName() );
        permission.setUser( user );
        permission.setAdminister( true );

        permission.setInstanceClass( ProjectGroup.class );
        permission.setId( new Integer( projectGroup.getId() ) );
        permission.setParentClass( ProjectGroup.class );
        permission.setParentId( new Integer( AclInitializer.PARENT_PROJECT_GROUP_ACL_ID ) );

        setUsersInstancePermission( permission );
    }

    /**
     * Call this method when new {@link Project}s are created.
     * 
     * @param projects
     */
    protected void createNewProjectsACLs( Collection projects, int projectGroupId )
    {
        Iterator it = projects.iterator();
        while ( it.hasNext() )
        {
            Project project = (Project) it.next();
            afterAddProject( project, projectGroupId );
        }
    }

    /**
     * Create ACL for new {@link Project}, it has same permissions as its project group.
     * 
     * @param project
     * @param projectGroupId group the projects belong to
     */
    public void afterAddProject( Project project, int projectGroupId )
    {
        afterAddProjectGroupDependentObject( project, project.getId(), projectGroupId );
    }

    /**
     * Create an ACL that inherits from a {@link ProjectGroup} ACL
     *
     * @param object object to protect
     * @param id identifier of the object to protect
     * @param projectGroupId id of the group that provides the ACLs for this object
     */
    private void afterAddProjectGroupDependentObject( Object object, int id, int projectGroupId )
    {
        afterAddDependentObject( object, id, ProjectGroup.class, projectGroupId );
    }

    /**
     * Create an ACL that inherits from a {@link Project} ACL
     *
     * @param object object to protect
     * @param id identifier of the object to protect
     * @param projectId id of the group that provides the ACLs for this object
     */
    private void afterAddProjectDependentObject( Object object, int id, int projectId )
    {
        afterAddDependentObject( object, id, Project.class, projectId );
    }

    private void afterAddDependentObject( Object object, int id, Class dependentClass, int dependentId )
    {
        InstancePermissions permission = new InstancePermissions();
        permission.setUser( null );
        permission.setInstanceClass( object.getClass() );
        permission.setId( new Integer( id ) );
        permission.setParentClass( dependentClass );
        permission.setParentId( new Integer( dependentId ) );

        setUsersInstancePermission( permission );
    }
}
