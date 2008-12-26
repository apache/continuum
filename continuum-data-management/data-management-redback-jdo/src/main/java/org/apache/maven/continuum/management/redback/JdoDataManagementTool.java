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
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.users.UserManager;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * JDO implementation the database management tool API.
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.management.DataManagementTool" role-hint="redback-jdo"
 */
public class JdoDataManagementTool
    implements DataManagementTool
{
    /**
     * @plexus.requirement role-hint="jdo"
     */
    private org.codehaus.plexus.redback.management.DataManagementTool toolDelegate;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private RBACManager rbacManager;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private UserManager userManager;

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private KeyManager keyManager;

    public void backupDatabase( File backupDirectory )
        throws IOException
    {
        try
        {
            toolDelegate.backupKeyDatabase( keyManager, backupDirectory );
            toolDelegate.backupRBACDatabase( rbacManager, backupDirectory );
            toolDelegate.backupUserDatabase( userManager, backupDirectory );
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

    public void eraseDatabase()
    {
        toolDelegate.eraseKeysDatabase( keyManager );
        toolDelegate.eraseRBACDatabase( rbacManager );
        toolDelegate.eraseUsersDatabase( userManager );
    }

    public void restoreDatabase( File backupDirectory )
        throws IOException
    {
        try
        {
            toolDelegate.restoreKeysDatabase( keyManager, backupDirectory );
            toolDelegate.restoreRBACDatabase( rbacManager, backupDirectory );
            toolDelegate.restoreUsersDatabase( userManager, backupDirectory );
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
}
