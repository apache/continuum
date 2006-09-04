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

import java.util.Date;
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
import org.apache.maven.user.model.UserManager;
import org.springframework.dao.DataAccessException;

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
    static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * @plexus.requirement
     */
    private UserManager userManager;

    /**
     * @plexus.configuration default-value="60"
     */
    private int daysBeforeExpiration;

    public ContinuumUserDetailsService()
    {
    }

    public UserDetails loadUserByUsername( String username )
        throws UsernameNotFoundException, DataAccessException
    {
        ContinuumUser user;

        user = (ContinuumUser) userManager.getUser( username );

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
            grantedAuthorities[i] = new GrantedAuthorityImpl( "ROLE_" + permission.getName() );
            i++;
        }
        String username = user.getUsername();
        String password = user.getEncodedPassword();
        boolean enabled = true;
        boolean accountNonExpired = true;

        if ( user.getLastPasswordChange() != null && daysBeforeExpiration > 0 )
        {
            long lastPasswordChange = user.getLastPasswordChange().getTime();
            long currentTime = new Date().getTime();
            accountNonExpired = lastPasswordChange + daysBeforeExpiration * MILLISECONDS_PER_DAY > currentTime;
        }

        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        UserDetails userDetails = new User( username, password, enabled, accountNonExpired, credentialsNonExpired,
                                            accountNonLocked, grantedAuthorities );

        return userDetails;
    }

    protected void setDaysBeforeExpiration( int daysBeforeExpiration )
    {
        this.daysBeforeExpiration = daysBeforeExpiration;
    }

    /**
     * TODO: convert Acegi user into Continuum user?
     */
}
