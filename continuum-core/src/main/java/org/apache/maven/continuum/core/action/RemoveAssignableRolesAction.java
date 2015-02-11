package org.apache.maven.continuum.core.action;

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

import org.apache.continuum.dao.ProjectGroupDao;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;

import java.util.Map;

/**
 * AddAssignableRolesAction:
 *
 * @author: Emmanuel Venisse <evenisse@apache.org>
 */
@Component( role = org.codehaus.plexus.action.Action.class, hint = "remove-assignable-roles" )
public class RemoveAssignableRolesAction
    extends AbstractContinuumAction
{

    @Requirement
    private ProjectGroupDao projectGroupDao;

    @Requirement( hint = "default" )
    private RoleManager roleManager;

    public void execute( Map context )
        throws ContinuumException, ContinuumStoreException
    {
        int projectGroupId = getProjectGroupId( context );

        ProjectGroup projectGroup = projectGroupDao.getProjectGroup( projectGroupId );

        try
        {
            if ( !roleManager.templatedRoleExists( "project-administrator", projectGroup.getName() ) )
            {
                roleManager.removeTemplatedRole( "project-administrator", projectGroup.getName() );
            }
            if ( !roleManager.templatedRoleExists( "project-developer", projectGroup.getName() ) )
            {
                roleManager.removeTemplatedRole( "project-developer", projectGroup.getName() );
            }

            if ( !roleManager.templatedRoleExists( "project-user", projectGroup.getName() ) )
            {
                roleManager.removeTemplatedRole( "project-user", projectGroup.getName() );
            }
        }
        catch ( RoleManagerException e )
        {
            e.printStackTrace();
            throw new ContinuumException( "error removing templated role for project " + projectGroup.getName(), e );
        }
    }
}
