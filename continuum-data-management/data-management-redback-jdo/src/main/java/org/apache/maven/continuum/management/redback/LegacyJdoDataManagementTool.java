package org.apache.maven.continuum.management.redback;

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

import org.apache.maven.continuum.management.DataManagementException;
import org.apache.maven.continuum.management.DataManagementTool;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.jdo.PlexusStoreException;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.JdoOperation;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.JdoPermission;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.JdoResource;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.JdoRole;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.JdoUserAssignment;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.RbacDatabase;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.RbacJdoModelModelloMetadata;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.io.stax.RbacJdoModelStaxReader;
import org.codehaus.plexus.security.authorization.rbac.jdo.v0_9_0.io.stax.RbacJdoModelStaxWriter;
import org.codehaus.plexus.security.keys.jdo.v0_9_0.AuthenticationKeyDatabase;
import org.codehaus.plexus.security.keys.jdo.v0_9_0.JdoAuthenticationKey;
import org.codehaus.plexus.security.keys.jdo.v0_9_0.PlexusSecurityKeyManagementJdoModelloMetadata;
import org.codehaus.plexus.security.keys.jdo.v0_9_0.io.stax.PlexusSecurityKeyManagementJdoStaxReader;
import org.codehaus.plexus.security.keys.jdo.v0_9_0.io.stax.PlexusSecurityKeyManagementJdoStaxWriter;
import org.codehaus.plexus.security.rbac.RBACObjectAssertions;
import org.codehaus.plexus.security.rbac.RbacManagerException;
import org.codehaus.plexus.security.user.Messages;
import org.codehaus.plexus.security.user.UserManagerException;
import org.codehaus.plexus.security.user.jdo.v0_9_0.JdoUser;
import org.codehaus.plexus.security.user.jdo.v0_9_0.UserDatabase;
import org.codehaus.plexus.security.user.jdo.v0_9_0.UserManagementModelloMetadata;
import org.codehaus.plexus.security.user.jdo.v0_9_0.io.stax.UserManagementStaxReader;
import org.codehaus.plexus.security.user.jdo.v0_9_0.io.stax.UserManagementStaxWriter;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.xml.stream.XMLStreamException;

/**
 * JDO implementation the database management tool API.
 *
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.management.DataManagementTool" role-hint="legacy-redback-jdo"
 */
public class LegacyJdoDataManagementTool
    implements DataManagementTool
{
    private static final String USERS_XML_NAME = "users.xml";

    private static final String KEYS_XML_NAME = "keys.xml";

    private static final String RBAC_XML_NAME = "rbac.xml";

    /**
     * @plexus.requirement role-hint="users"
     */
    private JdoFactory jdoFactory;

    public void backupDatabase( File backupDirectory )
        throws IOException
    {
        try
        {
            backupKeyDatabase( backupDirectory );
            backupRBACDatabase( backupDirectory );
            backupUserDatabase( backupDirectory );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( e );
        }
        catch ( RbacManagerException e )
        {
            throw new DataManagementException( e );
        }
    }

    public void restoreDatabase( File backupDirectory, boolean strict )
        throws IOException, DataManagementException
    {
        try
        {
            restoreKeysDatabase( backupDirectory );
            restoreRBACDatabase( backupDirectory );
            restoreUsersDatabase( backupDirectory );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( e );
        }
        catch ( PlexusStoreException e )
        {
            throw new DataManagementException( e );
        }
        catch ( RbacManagerException e )
        {
            throw new DataManagementException( e );
        }
    }

    public void eraseDatabase()
    {
        eraseKeysDatabase();
        eraseRBACDatabase();
        eraseUsersDatabase();
    }

    public void backupRBACDatabase( File backupDirectory )
        throws RbacManagerException, IOException, XMLStreamException
    {
        RbacDatabase database = new RbacDatabase();
        database.setRoles( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoRole.class ) );
        database.setUserAssignments( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(),
                                                                           JdoUserAssignment.class ) );
        database.setPermissions( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoPermission.class ) );
        database.setOperations( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoOperation.class ) );
        database.setResources( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoResource.class ) );

        RbacJdoModelStaxWriter writer = new RbacJdoModelStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, RBAC_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void backupUserDatabase( File backupDirectory )
        throws IOException, XMLStreamException
    {
        UserDatabase database = new UserDatabase();
        database.setUsers( PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoUser.class ) );

        UserManagementStaxWriter writer = new UserManagementStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, USERS_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public void backupKeyDatabase( File backupDirectory )
        throws IOException, XMLStreamException
    {
        AuthenticationKeyDatabase database = new AuthenticationKeyDatabase();
        List keys = PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager(), JdoAuthenticationKey.class );

        database.setKeys( keys );

        PlexusSecurityKeyManagementJdoStaxWriter writer = new PlexusSecurityKeyManagementJdoStaxWriter();
        FileWriter fileWriter = new FileWriter( new File( backupDirectory, KEYS_XML_NAME ) );
        try
        {
            writer.write( fileWriter, database );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    private PersistenceManager getPersistenceManager()
    {
        PersistenceManager pm = jdoFactory.getPersistenceManagerFactory().getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( 5 );

        return pm;
    }

    public void restoreRBACDatabase( File backupDirectory )
        throws IOException, XMLStreamException, RbacManagerException, PlexusStoreException
    {
        RbacJdoModelStaxReader reader = new RbacJdoModelStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, RBAC_XML_NAME ) );

        RbacDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        Map<String, JdoPermission> permissionMap = new HashMap<String, JdoPermission>();
        Map<String, JdoResource> resources = new HashMap<String, JdoResource>();
        Map<String, JdoOperation> operations = new HashMap<String, JdoOperation>();
        for ( Iterator i = database.getRoles().iterator(); i.hasNext(); )
        {
            JdoRole role = (JdoRole) i.next();

            // TODO: this could be generally useful and put into saveRole itself as long as the performance penalty isn't too harsh.
            //   Currently it always saves everything where it could pull pack the existing permissions, etc if they exist
            List<JdoPermission> permissions = new ArrayList<JdoPermission>();
            for ( Iterator j = role.getPermissions().iterator(); j.hasNext(); )
            {
                JdoPermission permission = (JdoPermission) j.next();

                if ( permissionMap.containsKey( permission.getName() ) )
                {
                    permission = permissionMap.get( permission.getName() );
                }
                else if ( objectExists( permission ) )
                {
                    permission = (JdoPermission) PlexusJdoUtils.getObjectById( getPersistenceManager(),
                                                                               JdoPermission.class,
                                                                               permission.getName() );
                    permissionMap.put( permission.getName(), permission );
                }
                else
                {
                    JdoOperation operation = (JdoOperation) permission.getOperation();
                    if ( operations.containsKey( operation.getName() ) )
                    {
                        operation = operations.get( operation.getName() );
                    }
                    else if ( objectExists( operation ) )
                    {
                        operation = (JdoOperation) PlexusJdoUtils.getObjectById( getPersistenceManager(),
                                                                                 JdoOperation.class,
                                                                                 operation.getName() );
                        operations.put( operation.getName(), operation );
                    }
                    else
                    {
                        RBACObjectAssertions.assertValid( operation );
                        operation = (JdoOperation) PlexusJdoUtils.saveObject( getPersistenceManager(), operation,
                                                                              null );
                        operations.put( operation.getName(), operation );
                    }
                    permission.setOperation( operation );

                    JdoResource resource = (JdoResource) permission.getResource();
                    if ( resources.containsKey( resource.getIdentifier() ) )
                    {
                        resource = resources.get( resource.getIdentifier() );
                    }
                    else if ( objectExists( resource ) )
                    {
                        resource = (JdoResource) PlexusJdoUtils.getObjectById( getPersistenceManager(),
                                                                               JdoResource.class,
                                                                               resource.getIdentifier() );
                        resources.put( resource.getIdentifier(), resource );
                    }
                    else
                    {
                        RBACObjectAssertions.assertValid( resource );
                        resource = (JdoResource) PlexusJdoUtils.saveObject( getPersistenceManager(), resource, null );
                        resources.put( resource.getIdentifier(), resource );
                    }
                    permission.setResource( resource );

                    RBACObjectAssertions.assertValid( permission );
                    permission = (JdoPermission) PlexusJdoUtils.saveObject( getPersistenceManager(), permission, null );
                    permissionMap.put( permission.getName(), permission );
                }
                permissions.add( permission );
            }
            role.setPermissions( permissions );

            RBACObjectAssertions.assertValid( role );

            PlexusJdoUtils.saveObject( getPersistenceManager(), role, new String[]{null} );
        }

        for ( Iterator i = database.getUserAssignments().iterator(); i.hasNext(); )
        {
            JdoUserAssignment userAssignment = (JdoUserAssignment) i.next();

            RBACObjectAssertions.assertValid( "Save User Assignment", userAssignment );

            PlexusJdoUtils.saveObject( getPersistenceManager(), userAssignment, new String[]{null} );
        }
    }

    private boolean objectExists( Object object )
    {
        return JDOHelper.getObjectId( object ) != null;
    }

    public void restoreUsersDatabase( File backupDirectory )
        throws IOException, XMLStreamException
    {
        UserManagementStaxReader reader = new UserManagementStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, USERS_XML_NAME ) );

        UserDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        for ( Iterator i = database.getUsers().iterator(); i.hasNext(); )
        {
            JdoUser user = (JdoUser) i.next();

            if ( !( user instanceof JdoUser ) )
            {
                throw new UserManagerException( "Unable to Add User. User object " + user.getClass().getName() +
                                                    " is not an instance of " + JdoUser.class.getName() );
            }

            if ( StringUtils.isEmpty( user.getUsername() ) )
            {
                throw new IllegalStateException( Messages.getString(
                    "user.manager.cannot.add.user.without.username" ) ); //$NON-NLS-1$
            }

            PlexusJdoUtils.addObject( getPersistenceManager(), user );

        }
    }

    public void restoreKeysDatabase( File backupDirectory )
        throws IOException, XMLStreamException
    {
        PlexusSecurityKeyManagementJdoStaxReader reader = new PlexusSecurityKeyManagementJdoStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, KEYS_XML_NAME ) );

        AuthenticationKeyDatabase database;
        try
        {
            database = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        for ( Iterator i = database.getKeys().iterator(); i.hasNext(); )
        {
            JdoAuthenticationKey key = (JdoAuthenticationKey) i.next();

            PlexusJdoUtils.addObject( getPersistenceManager(), key );
        }
    }

    public void eraseRBACDatabase()
    {
        // Must delete in order so that FK constraints don't get violated
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoRole.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoPermission.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoOperation.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoResource.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoUserAssignment.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), RbacJdoModelModelloMetadata.class );
    }

    public void eraseUsersDatabase()
    {
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoUser.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), UserManagementModelloMetadata.class );
    }

    public void eraseKeysDatabase()
    {
        PlexusJdoUtils.removeAll( getPersistenceManager(), JdoAuthenticationKey.class );
        PlexusJdoUtils.removeAll( getPersistenceManager(), PlexusSecurityKeyManagementJdoModelloMetadata.class );
    }
}
