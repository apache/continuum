package org.apache.maven.continuum.management.redback;

import org.apache.maven.continuum.management.DatabaseFactoryConfigurator;
import org.apache.maven.continuum.management.DatabaseParams;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;

import java.util.Iterator;
import java.util.Properties;

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

@Component( role = org.apache.maven.continuum.management.DatabaseFactoryConfigurator.class, hint = "redback" )
public class DefaultDatabaseFactoryConfigurator
    implements DatabaseFactoryConfigurator
{

    @Requirement( role = org.codehaus.plexus.jdo.JdoFactory.class, hint = "users" )
    protected DefaultConfigurableJdoFactory factory;

    public void configure( DatabaseParams params )
    {
        // Must occur before store is looked up
        factory.setDriverName( params.getDriverClass() );
        factory.setUserName( params.getUsername() );
        factory.setPassword( params.getPassword() );
        factory.setUrl( params.getUrl() );

        Properties properties = params.getProperties();
        for ( Iterator i = properties.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();
            factory.setProperty( key, properties.getProperty( key ) );
        }
    }
}
