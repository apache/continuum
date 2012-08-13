package org.apache.maven.continuum.xmlrpc.server;

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
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.xmlrpc.ContinuumService;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumSecureService
    implements ContinuumService, ContinuumXmlRpcComponent
{
    /**
     * @plexus.requirement role-hint="default"
     */
    private SecuritySystem securitySystem;

    private ContinuumXmlRpcConfig config;

    public void setConfig( ContinuumXmlRpcConfig config )
    {
        this.config = config;
    }

    public SecuritySystem getSecuritySystem()
    {
        return securitySystem;
    }

    public SecuritySession getSecuritySession()
    {
        return config.getSecuritySession();
    }

    /**
     * Check if the current user is already authenticated
     *
     * @return true if the user is authenticated
     */
    public boolean isAuthenticated()
    {
        return !( getSecuritySession() == null || !getSecuritySession().isAuthenticated() );

    }

    /**
     * Check if the current user is authorized to do the action
     *
     * @param role the role
     * @throws ContinuumException if the user isn't authorized
     */
    protected void checkAuthorization( String role )
        throws ContinuumException
    {
        checkAuthorization( role, null, false );
    }

    /**
     * Check if the current user is authorized to do the action
     *
     * @param role     the role
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized
     */
    protected void checkAuthorization( String role, String resource )
        throws ContinuumException
    {
        checkAuthorization( role, resource, true );
    }

    /**
     * Verify if the current user is authorized to do the action
     *
     * @param role     the role
     * @param resource the operation resource
     * @return true if the user is authorized
     * @throws AuthorizationException if the authorizing request generate an error
     */
    protected boolean isAuthorized( String role, String resource )
        throws AuthorizationException
    {
        return isAuthorized( role, resource, true );
    }

    /**
     * Verify if the current user is authorized to do the action
     *
     * @param role             the role
     * @param resource         the operation resource
     * @param requiredResource true if resource can't be null
     * @return true if the user is authorized
     * @throws AuthorizationException if the authorizing request generate an error
     */
    protected boolean isAuthorized( String role, String resource, boolean requiredResource )
        throws AuthorizationException
    {
        if ( resource != null && StringUtils.isNotEmpty( resource.trim() ) )
        {
            if ( !getSecuritySystem().isAuthorized( config.getSecuritySession(), role, resource ) )
            {
                return false;
            }
        }
        else
        {
            if ( requiredResource || !getSecuritySystem().isAuthorized( config.getSecuritySession(), role ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the current user is authorized to do the action
     *
     * @param role             the role
     * @param resource         the operation resource
     * @param requiredResource true if resource can't be null
     * @throws ContinuumException if the user isn't authorized
     */
    protected void checkAuthorization( String role, String resource, boolean requiredResource )
        throws ContinuumException
    {
        try
        {
            if ( !isAuthorized( role, resource, requiredResource ) )
            {
                throw new ContinuumException( "You're not authorized to execute this action." );
            }
        }
        catch ( AuthorizationException ae )
        {
            throw new ContinuumException( "error authorizing request." );
        }
    }

    /**
     * Check if the current user is authorized to view the specified project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkViewProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a project group
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddProjectGroupAuthorization()
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_OPERATION );
    }

    /**
     * Check if the current user is authorized to delete the specified project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to build the specified project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkBuildProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_BUILD_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify the specified project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyProjectGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a project to a specific project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddProjectToGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to delete a project from a specified group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveProjectFromGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_FROM_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify a project in the specified group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyProjectInGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_IN_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to build a project in the specified group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkBuildProjectInGroupAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a build definition for the specified
     * project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddGroupBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_BUILD_DEFINTION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to delete a build definition in the specified
     * project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveGroupBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_BUILD_DEFINITION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify a build definition in the specified
     * project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyGroupBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_BUILD_DEFINITION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a group build definition to a specific
     * project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddProjectBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_BUILD_DEFINTION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify a build definition of a specific project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyProjectBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_BUILD_DEFINITION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to delete a build definition of a specific
     * project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveProjectBuildDefinitionAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_BUILD_DEFINITION_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a notifier to the specified
     * project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddProjectGroupNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_GROUP_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to delete a notifier in the specified
     * project group
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveProjectGroupNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_GROUP_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify a notifier in the specified
     * project group
     *
     * @param resource the operartion resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyProjectGroupNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_GROUP_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to add a notifier to a specific project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkAddProjectNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_ADD_PROJECT_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to delete a notifier in a specific project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkRemoveProjectNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_REMOVE_PROJECT_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to modify a notifier in a specific project
     *
     * @param resource the operation resource
     * @throws ContinuumException if the user isn't authorized if the user isn't authorized
     */
    protected void checkModifyProjectNotifierAuthorization( String resource )
        throws ContinuumException
    {
        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MODIFY_PROJECT_NOTIFIER_OPERATION, resource );
    }

    /**
     * Check if the current user is authorized to manage the application's configuration
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authenticated
     */
    protected void checkManageConfigurationAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_CONFIGURATION );
    }

    /**
     * Check if the current user is authorized to manage the project build schedules
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authenticated
     */
    protected void checkManageSchedulesAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_SCHEDULES );
    }

    /**
     * Check if the current user is authorized to manage the installations
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authenticated
     */
    protected void checkManageInstallationsAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_INSTALLATIONS );
    }

    /**
     * Check if the current user is authorized to manage the profiles
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authenticated
     */
    protected void checkManageProfilesAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PROFILES );
    }

    /**
     * Check if the current user is authorized to manage the build definitions templates
     *
     * @throws ContinuumException if the user isn't authorized if the user isn't authenticated
     */
    protected void checkManageBuildDefinitionTemplatesAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_BUILD_TEMPLATES );
    }

    protected void checkManageQueuesAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_QUEUES );
    }

    protected void checkManagePurgingAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        try
        {
            checkAuthorization( ContinuumRoleConstants.SYSTEM_ADMINISTRATOR_ROLE );
        }
        catch ( ContinuumException e )
        {
            checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PURGING );
        }
    }

    protected void checkManageRepositoriesAuthorization()
        throws ContinuumException
    {
        if ( !isAuthenticated() )
        {
            throw new ContinuumException( "Authentication required." );
        }

        try
        {
            checkAuthorization( ContinuumRoleConstants.SYSTEM_ADMINISTRATOR_ROLE );
        }
        catch ( ContinuumException e )
        {
            checkAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_REPOSITORIES );
        }
    }
}
