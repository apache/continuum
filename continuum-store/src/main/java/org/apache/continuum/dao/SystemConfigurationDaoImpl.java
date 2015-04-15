package org.apache.continuum.dao;

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

import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.component.annotations.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Repository( "systemConfigurationDao" )
@Component( role = org.apache.continuum.dao.SystemConfigurationDao.class )
public class SystemConfigurationDaoImpl
    extends AbstractDao
    implements SystemConfigurationDao
{
    public SystemConfiguration addSystemConfiguration( SystemConfiguration systemConf )
    {
        return addObject( systemConf );
    }

    public void updateSystemConfiguration( SystemConfiguration systemConf )
        throws ContinuumStoreException
    {
        updateObject( systemConf );
    }

    public SystemConfiguration getSystemConfiguration()
        throws ContinuumStoreException
    {
        List systemConfs = getAllObjectsDetached( SystemConfiguration.class );

        if ( systemConfs == null || systemConfs.isEmpty() )
        {
            return null;
        }
        else if ( systemConfs.size() > 1 )
        {
            throw new ContinuumStoreException(
                "Database is corrupted. There are more than one systemConfiguration object." );
        }
        else
        {
            return (SystemConfiguration) systemConfs.get( 0 );
        }
    }
}
