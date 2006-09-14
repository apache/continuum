package org.apache.maven.continuum.security;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.user.model.UserManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultContinuumSecurity
    implements ContinuumSecurity
{
    /**
     * @plexus.requirement
     */
    private UserManager userManager;

    public List getPermissions( ContinuumUser user )
        throws ContinuumSecurityException
    {
        ContinuumUser u = user;

        if ( u == null )
        {
            u = getGuestUser();
        }
        
        List groups = u.getGroups();
        Iterator it = groups.iterator();
        List permissions = new ArrayList();
        while ( it.hasNext() )
        {
            permissions.addAll( ( (org.apache.maven.user.model.UserGroup) it.next() ).getPermissions() );
        }

        return permissions;
    }

    public List getPermissions( UserGroup group )
    {
        return group.getPermissions();
    }

    public boolean isAuthorized( ContinuumUser user, String action )
        throws ContinuumSecurityException
    {
        List perms = getPermissions( user );

        if ( perms != null )
        {
            for ( Iterator i = perms.iterator(); i.hasNext(); )
            {
                Permission perm = (Permission) i.next();
                if ( perm.getName().equals( action ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAuthorized( UserGroup group, String action )
        throws ContinuumSecurityException
    {
        List perms = getPermissions( group );

        if ( perms != null )
        {
            for ( Iterator i = perms.iterator(); i.hasNext(); )
            {
                Permission perm = (Permission) i.next();
                if ( perm.getName().equals( action ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public ContinuumUser getGuestUser()
        throws ContinuumSecurityException
    {
        return ( ContinuumUser ) userManager.getGuestUser();
    }
}
