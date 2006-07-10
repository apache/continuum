package org.apache.maven.continuum.security;

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
import java.util.Map;

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
import org.codehaus.plexus.security.Authentication;
import org.codehaus.plexus.security.Authenticator;
import org.codehaus.plexus.security.exception.AuthenticationException;
import org.codehaus.plexus.security.exception.UnauthorizedException;
import org.codehaus.plexus.security.exception.UnknownEntityException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * TODO: Move this to o.a.m.c.security once plexus-security doesn't depend on plexus-summit.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumAuthenticator
    implements Authenticator, UserDetailsService
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    // ----------------------------------------------------------------------
    // Authenticator Implementation
    // ----------------------------------------------------------------------

    public Authentication authenticate( Map tokens )
        throws UnknownEntityException, AuthenticationException, UnauthorizedException
    {
        String username = (String) tokens.get( "username" );
        String password = (String) tokens.get( "password" );

        ContinuumUser user = getUser( username );

        if ( user == null )
        {
            throw new UnknownEntityException();
        }

        System.err.println( "username: " + username );
        //System.err.println( "password: " + password );
        //System.err.println( "user.password: " + user.getPassword() );

        if ( !user.equalsPassword( password ) )
        {
            throw new AuthenticationException( "Invalid password." );
        }

        return null;
    }

    public UserDetails loadUserByUsername( String username )
        throws UsernameNotFoundException, DataAccessException
    {
        ContinuumUser user;
        try
        {
            user = store.getUserByUsername( username );
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

    public Authentication getAnonymousEntity()
    {
        throw new RuntimeException( "Not implemented" );
    }

    // ----------------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------------

    private ContinuumUser getUser( String username )
        throws AuthenticationException
    {
        try
        {
            return store.getUserByUsername( username );
        }
        catch ( ContinuumStoreException e )
        {
            throw new AuthenticationException( "Error while retreiving user.", e );
        }
    }
    
    /**
     * Convert a Continuum user into a Acegi user
     * 
     * @param user the continuum user loaded from DB
     * @return the Acegi user
     */
    private UserDetails getUserDetails( ContinuumUser user )
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
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        UserDetails userDetails = new User( user.getUsername(), user.getPassword(), enabled, accountNonExpired,
                                            credentialsNonExpired, accountNonLocked, grantedAuthorities );

        return userDetails;
    }
}
