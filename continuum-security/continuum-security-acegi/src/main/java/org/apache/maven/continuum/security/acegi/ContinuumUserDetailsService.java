package org.apache.maven.continuum.security.acegi;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
 *
 */

import java.util.Iterator;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Acegi {@link UserDetailsService} that loads user info from Continuum database.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @author Henry Isidro
 * @version $Id$
 */
public class ContinuumUserDetailsService
    implements UserDetailsService
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public ContinuumUserDetailsService()
    {
    }

    public void setStore( ContinuumStore store )
    {
        this.store = store;
    }

    /**
     * {@link ContinuumStore} to load the user from.
     * 
     * @return the store
     */
    public ContinuumStore getStore()
    {
        return store;
    }

    public UserDetails loadUserByUsername( String username )
        throws UsernameNotFoundException, DataAccessException
    {
        ContinuumUser user;
        try
        {
            user = getStore().getUserByUsername( username );
        }
        catch ( ContinuumStoreException e )
        {
            throw new DataAccessResourceFailureException( e.getMessage(), e );
        }
        if ( user == null )
        {
            throw new UsernameNotFoundException( "Could not find user: " + username );
        }
        return getUserDetails( user );
    }

    /**
     * Convert a Continuum user into a Acegi user
     * 
     * @param user the continuum user loaded from DB
     * @return the Acegi user
     */
    UserDetails getUserDetails( ContinuumUser user )
    {
        List permissions = user.getGroup().getPermissions();

        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[permissions.size()];
        int i = 0;
        Iterator it = permissions.iterator();
        while ( it.hasNext() )
        {
            Permission permission = (Permission) it.next();
            grantedAuthorities[i] = new GrantedAuthorityImpl( permission.getName() );
            i++;
        }
        String username = user.getUsername();
        String password = user.getHashedPassword();
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        UserDetails userDetails = new User( username, password, enabled, accountNonExpired, credentialsNonExpired,
                                            accountNonLocked, grantedAuthorities );

        return userDetails;
    }

    /**
     * TODO: convert Acegi user into Continuum user?
     */
}
