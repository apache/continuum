package org.apache.maven.continuum.web.action;

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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.UserGroup;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;

import com.opensymphony.webwork.interceptor.ServletRequestAware;

/**
 * @author Henry Isidro
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="editUser"
 */
public class EditUserAction
    extends ContinuumActionSupport
    implements ServletRequestAware
{

    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    private ContinuumUser user;

    private UserGroup userGroup;

    private List staticPermissions;

    private List availablePermissions;

    private Permission staticPermission;

    private Permission permission;

    private String permissionName;

    private boolean addMode = false;

    private int accountId;

    private String username;

    private String password;

    private String email;

    private List permissions;

    private HttpServletRequest request;

    public String execute()
        throws Exception
    {
        permissions = (List) request.getSession().getAttribute( "permissions" );
        try
        {
            username = username.substring( 0, username.indexOf( "," ) );
            password = password.substring( 0, password.indexOf( "," ) );
            email = email.substring( 0, email.indexOf( "," ) );
        }
        catch ( StringIndexOutOfBoundsException e )
        {
        }
        if ( addMode )
        {
            try
            {
                userGroup = new UserGroup();
                userGroup.setName( username );
                userGroup.setPermissions( permissions );

                user = new ContinuumUser();
                user.setUsername( username );
                user.setPassword( password );
                user.setEmail( email );
                user.setGroup( userGroup );
                getContinuum().addUser( user );
            }
            catch ( ContinuumException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();

                return ERROR;
            }
        }
        else
        {
            try
            {
                user = getContinuum().getUser( accountId );
                user.setUsername( username );
                user.setPassword( password );
                user.setEmail( email );
                user.getGroup().setPermissions( permissions );
            }
            catch ( ContinuumException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();

                return ERROR;
            }

            try
            {
                getContinuum().updateUser( user );
            }
            catch ( ContinuumException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();

                return ERROR;
            }
        }

        request.getSession().removeAttribute( "addMode" );
        request.getSession().removeAttribute( "accountId" );
        request.getSession().removeAttribute( "username" );
        request.getSession().removeAttribute( "password" );
        request.getSession().removeAttribute( "email" );
        request.getSession().removeAttribute( "permissions" );

        return SUCCESS;
    }

    public String doAdd()
        throws Exception
    {
        addMode = true;
        return INPUT;
    }

    public String doEdit()
        throws Exception
    {
        try
        {
            addMode = false;
            user = getContinuum().getUser( accountId );
            username = user.getUsername();
            password = user.getPassword();
            email = user.getEmail();
            permissions = user.getGroup().getPermissions();
            if ( permissions.size() == 1 )
            {
                permissionName = ( (Permission) permissions.get( 0 ) ).getName();
            }
        }
        catch ( ContinuumException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return ERROR;
        }

        return INPUT;
    }

    public String doGetAvailablePermissions()
        throws Exception
    {
        try
        {
            int i, j;
            availablePermissions = new ArrayList();
            staticPermissions = store.getPermissions();
            permissions = (List) request.getSession().getAttribute( "permissions" );
            if ( permissions == null || permissions.size() == 0 )
            {
                availablePermissions.addAll( staticPermissions );
            }
            else
            {
                for ( i = 0; i < staticPermissions.size(); i++ )
                {
                    staticPermission = (Permission) staticPermissions.get( i );
                    for ( j = 0; j < permissions.size(); j++ )
                    {
                        permission = (Permission) permissions.get( j );
                        if ( permission.getName().equalsIgnoreCase( staticPermission.getName() ) )
                        {
                            break;
                        }
                    }
                    if ( j >= permissions.size() )
                    {
                        availablePermissions.add( staticPermission );
                    }
                }
            }
        }
        catch ( ContinuumStoreException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return ERROR;
        }

        request.getSession().setAttribute( "addMode", Boolean.valueOf( addMode ) );
        request.getSession().setAttribute( "accountId", new Integer( accountId ) );
        request.getSession().setAttribute( "username", username );
        request.getSession().setAttribute( "password", password );
        request.getSession().setAttribute( "email", email );

        return "permissions";
    }

    public String doAddPermission()
        throws Exception
    {
        try
        {
            staticPermissions = store.getPermissions();
            int i, j;
            for ( i = 0; i < staticPermissions.size(); i++ )
            {
                permission = (Permission) staticPermissions.get( i );
                if ( permission.getName().equalsIgnoreCase( permissionName ) )
                {
                    permissions = (List) request.getSession().getAttribute( "permissions" );
                    if ( permissions == null )
                    {
                        permissions = new ArrayList();
                        permissions.add( permission );
                    }
                    else
                    {
                        for ( j = 0; j < permissions.size(); j++ )
                        {
                            Permission permission = (Permission) permissions.get( j );
                            if ( permission.getName().equalsIgnoreCase( permissionName ) )
                            {
                                break;
                            }
                        }
                        if ( j >= permissions.size() )
                        {
                            permissions.add( permission );
                        }
                    }
                    if ( permissions.size() == 1 )
                    {
                        permissionName = ( (Permission) permissions.get( 0 ) ).getName();
                    }
                    break;
                }
            }
        }
        catch ( ContinuumStoreException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return ERROR;
        }

        addMode = ( (Boolean) request.getSession().getAttribute( "addMode" ) ).booleanValue();
        accountId = ( (Integer) request.getSession().getAttribute( "accountId" ) ).intValue();
        username = (String) request.getSession().getAttribute( "username" );
        password = (String) request.getSession().getAttribute( "password" );
        email = (String) request.getSession().getAttribute( "email" );

        return INPUT;
    }

    public String doDeletePermission()
        throws Exception
    {
        int i = 0;
        permissions = (List) request.getSession().getAttribute( "permissions" );
        for ( ; i < permissions.size(); i++ )
        {
            permission = (Permission) permissions.get( i );
            if ( permission.getName().equalsIgnoreCase( permissionName ) )
            {
                permissions.remove( i );
                break;
            }
            if ( permissions.size() == 1 )
            {
                permissionName = ( (Permission) permissions.get( 0 ) ).getName();
            }
        }

        return INPUT;
    }

    public List getAvailablePermissions()
    {
        return availablePermissions;
    }

    public String getPermissionName()
    {
        return permissionName;
    }

    public void setPermissionName( String permissionName )
    {
        this.permissionName = permissionName;
    }

    public boolean isAddMode()
    {
        return addMode;
    }

    public void setAddMode( boolean addMode )
    {
        this.addMode = addMode;
    }

    public int getAccountId()
    {
        return accountId;
    }

    public void setAccountId( int accountId )
    {
        this.accountId = accountId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public List getPermissions()
    {
        return this.permissions;
    }

    public void setServletRequest( HttpServletRequest request )
    {
        this.request = request;
    }

}
