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

import javax.sql.DataSource;

import org.acegisecurity.acl.basic.jdbc.JdbcExtendedDaoImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.store.ContinuumStore;
import org.codehaus.mojo.sql.SqlExecMojo;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Test for {@link AclInitializer}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclInitializerTest
    extends MockObjectTestCase
{

    private AclInitializer initializer;

    private SqlExecMojo sqlMojo;

    private Mock store;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        initializer = new AclInitializer();
        initializer.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );
        initializer.setSqlClasspathResource( "org/apache/maven/user/acegi/acl/acegi-acl-derby.sql" );

        sqlMojo = new SqlExecMojo();
        sqlMojo.setUsername( "sa" );
        sqlMojo.setPassword( "" );
        sqlMojo.setDriver( "org.apache.derby.jdbc.EmbeddedDriver" );
        sqlMojo.setUrl( "jdbc:derby:target/acl-initializer-database;create=true" );
        sqlMojo.setOnError( SqlExecMojo.ON_ERROR_CONTINUE );
        initializer.setSqlMojo( sqlMojo );

        JdbcExtendedDaoImpl dao = new JdbcExtendedDaoImpl();
        dao.setDataSource( getDataSource() );
        initializer.setDao( dao );

        store = mock( ContinuumStore.class );
        initializer.setStore( (ContinuumStore) store.proxy() );
    }

    public void testInitialize()
        throws Exception
    {
        ProjectGroup parentGroup = new ProjectGroup();
        parentGroup.setId( 1 );
        store.expects( once() ).method( "getProjectGroupByGroupId" ).will( returnValue( parentGroup ) );
        initializer.initialize();
        initializer.initialize();
    }

    private DataSource getDataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName( sqlMojo.getDriver() );
        dataSource.setUrl( sqlMojo.getUrl() );
        dataSource.setUsername( sqlMojo.getUsername() );
        return dataSource;
    }
}
