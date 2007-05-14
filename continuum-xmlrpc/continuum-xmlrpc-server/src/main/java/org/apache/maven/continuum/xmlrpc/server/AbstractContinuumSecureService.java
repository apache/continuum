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
            if ( resource != null && StringUtils.isNotEmpty( resource.trim() ) )
            {
                if ( !getSecuritySystem().isAuthorized( config.getSecuritySession(), role, resource ) )
                {
                    throw new ContinuumException( "You're not authorized to execute this action." );
                }
            }
            else
            {
                if ( requiredResource || !getSecuritySystem().isAuthorized( config.getSecuritySession(), role ) )
                {
                    throw new ContinuumException( "You're not authorized to execute this action." );
                }
            }
        }
        catch ( AuthorizationException ae )
        {
            throw new ContinuumException( "error authorizing request." );
        }
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
}
