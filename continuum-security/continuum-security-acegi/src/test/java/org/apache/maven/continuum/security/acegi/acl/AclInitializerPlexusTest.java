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

import java.io.File;

import javax.sql.DataSource;

import org.acegisecurity.acl.basic.jdbc.JdbcExtendedDaoImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.mojo.sql.SqlExecMojo;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * Test for {@link AclInitializer} using Plexus to inject dependencies
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class AclInitializerPlexusTest
    extends PlexusTestCase
{

    private AclInitializer initializer;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        initializer = (AclInitializer) super.lookup( AclInitializer.ROLE );
    }

    public void testInitialize()
        throws Exception
    {
        initializer.initialize();
        initializer.initialize();
    }
}
