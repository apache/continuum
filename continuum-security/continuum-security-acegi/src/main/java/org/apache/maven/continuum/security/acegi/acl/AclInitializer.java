package org.apache.maven.continuum.security.acegi.acl;

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

import org.acegisecurity.acl.basic.NamedEntityObjectIdentity;
import org.acegisecurity.acl.basic.SimpleAclEntry;
import org.acegisecurity.acl.basic.jdbc.JdbcExtendedDaoImpl;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.sql.SqlExecMojo;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Initialize the ACL system with some default values.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclInitializer
    extends AbstractLogEnabled
    implements Initializable
{

    private JdbcExtendedDaoImpl dao;

    private SqlExecMojo sqlMojo;

    public void setDao( JdbcExtendedDaoImpl dao )
    {
        this.dao = dao;
    }

    public JdbcExtendedDaoImpl getDao()
    {
        return dao;
    }

    public void setSqlMojo( SqlExecMojo sqlMojo )
    {
        this.sqlMojo = sqlMojo;
    }

    public SqlExecMojo getSqlMojo()
    {
        return sqlMojo;
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            getSqlMojo().execute();
        }
        catch ( MojoExecutionException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        /* poor check to see if this is the first time initializing the database */
        if ( getSqlMojo().getSuccessfulStatements() >= 2 )
        {
            /* tables were created, insert default values */
            getLogger().info( "Initializing ACL database" );

            /* admin can do anything with project number 1 */
            SimpleAclEntry aclEntry = new SimpleAclEntry();
            aclEntry.setAclObjectIdentity( new NamedEntityObjectIdentity( Project.class.getName(), "1" ) );
            aclEntry.setRecipient( "ROLE_ADMIN" );
            aclEntry.addPermission( SimpleAclEntry.ADMINISTRATION );
            getDao().create( aclEntry );
        }

    }
}
