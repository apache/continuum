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

import org.acegisecurity.acl.basic.BasicAclEntry;
import org.acegisecurity.acl.basic.NamedEntityObjectIdentity;
import org.acegisecurity.acl.basic.SimpleAclEntry;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.User;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.user.acegi.AclManager;

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
            createNewProjectsACLs( result.getProjects(), projectGroup );
        }
    }

    /**
     * Delete {@link ProjectGroup} ACLs
     * 
     * @param projectGroupId
     */
    public void afterDeleteProjectGroup( int projectGroupId )
    {
        delete( ProjectGroup.class, projectGroupId );
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
     * Creator of {@link ProjectGroup} has {@link SimpleAclEntry#ADMINISTRATION} permissions.
     * 
     * @param projectGroup
     */
    protected void createNewProjectGroupACL( ProjectGroup projectGroup )
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SimpleAclEntry aclEntry = new SimpleAclEntry();
        aclEntry.setAclObjectIdentity( createProjectGroupObjectIdentity( projectGroup.getId() ) );
        aclEntry.setRecipient( user.getUsername() );
        aclEntry.setAclObjectParentIdentity( AclInitializer.PARENT_PROJECT_GROUP_ACL_ID );
        aclEntry.addPermission( SimpleAclEntry.ADMINISTRATION );
        create( aclEntry );
    }

    /**
     * Call this method when new {@link Project}s are created.
     * 
     * @param projects
     */
    protected void createNewProjectsACLs( Collection projects, ProjectGroup projectGroup )
    {
        Iterator it = projects.iterator();
        while ( it.hasNext() )
        {
            Project project = (Project) it.next();
            createNewProjectACL( project, projectGroup );
        }
    }

    /**
     * Project has same permissions as its project group.
     * 
     * @param project
     * @param projectGroup group the projects belong to
     */
    protected void createNewProjectACL( Project project, ProjectGroup projectGroup )
    {
        NamedEntityObjectIdentity projectGroupIdentity = createProjectGroupObjectIdentity( projectGroup.getId() );
        SimpleAclEntry aclEntry = new SimpleAclEntry();
        aclEntry.setAclObjectIdentity( createProjectObjectIdentity( project.getId() ) );
        aclEntry.setAclObjectParentIdentity( projectGroupIdentity );
        create( aclEntry );
    }

    public void setProjectGroupPermissions( int projectGroupId, String userName, int permissions )
    {
        super.setPermissions( ProjectGroup.class, projectGroupId, userName, permissions,
                              AclInitializer.PARENT_PROJECT_GROUP_ACL_ID );
    }

    public BasicAclEntry getProjectGroupAcl( int projectGroupId, String userName )
    {
        return getAcl( ProjectGroup.class, projectGroupId, userName );
    }

    private NamedEntityObjectIdentity createProjectObjectIdentity( int projectId )
    {
        return createObjectIdentity( Project.class, projectId );
    }

    private NamedEntityObjectIdentity createProjectGroupObjectIdentity( int projectGroupId )
    {
        return createObjectIdentity( ProjectGroup.class, projectGroupId );
    }

    public BasicAclEntry[] getProjectGroupAcls( int projectGroupId )
    {
        return getAcls( ProjectGroup.class, projectGroupId );
    }
}
