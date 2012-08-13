package org.apache.maven.continuum.initialization;

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

import org.apache.continuum.dao.DaoUtils;
import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.Schedule;

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @since 4 juin 07
 */
public class DefaultContinuumInitializerTest
    extends AbstractContinuumTest
{
    protected void setUp()
        throws Exception
    {
        super.setUp();
        DaoUtils daoUtils = (DaoUtils) lookup( DaoUtils.class.getName() );
        daoUtils.eraseDatabase();
        ContinuumInitializer continuumInitializer = (ContinuumInitializer) lookup( ContinuumInitializer.ROLE,
                                                                                   "default" );
        continuumInitializer.initialize();
    }

    public void testDefaultSchedule()
        throws Exception
    {
        Schedule schedule = getScheduleDao().getScheduleByName( ConfigurationService.DEFAULT_SCHEDULE_NAME );
        assertNotNull( schedule );
    }

}
