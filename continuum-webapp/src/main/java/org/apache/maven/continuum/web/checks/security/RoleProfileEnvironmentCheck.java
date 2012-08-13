package org.apache.maven.continuum.web.checks.security;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * RoleProfileEnvironmentCheck:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $Id$
 * @plexus.component role="org.codehaus.plexus.redback.system.check.EnvironmentCheck"
 * role-hint="continuum-role-profile-check"
 */
public class RoleProfileEnvironmentCheck
    implements EnvironmentCheck
{
    private static final Logger log = LoggerFactory.getLogger( RoleProfileEnvironmentCheck.class );

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager roleManager;

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void validateEnvironment( List list )
    {
        try
        {
            log.info( "Checking roles list." );

            Collection<ProjectGroup> projectGroups = continuum.getAllProjectGroups();

            for ( ProjectGroup group : projectGroups )
            {
                // gets the role, making it if it doesn't exist
                //TODO: use continuum.executeAction( "add-assignable-roles", context ); or something like that to avoid code duplication
                if ( !roleManager.templatedRoleExists( "project-administrator", group.getName() ) )
                {
                    roleManager.createTemplatedRole( "project-administrator", group.getName() );
                }
                if ( !roleManager.templatedRoleExists( "project-developer", group.getName() ) )
                {
                    roleManager.createTemplatedRole( "project-developer", group.getName() );
                }

                if ( !roleManager.templatedRoleExists( "project-user", group.getName() ) )
                {
                    roleManager.createTemplatedRole( "project-user", group.getName() );
                }
            }

        }
        catch ( RoleManagerException rpe )
        {
            rpe.printStackTrace();
            list.add( "error checking existence of roles for groups" );
        }
    }
}
