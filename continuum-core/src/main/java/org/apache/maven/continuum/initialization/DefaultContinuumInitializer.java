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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.jpox.SchemaTool;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @todo use this, reintroduce default project group
 * @plexus.component role="org.apache.maven.continuum.initialization.ContinuumInitializer"
 * role-hint="default"
 */
public class DefaultContinuumInitializer
    extends AbstractLogEnabled
    implements ContinuumInitializer
{
    // ----------------------------------------------------------------------
    // Default values for the default schedule
    // ----------------------------------------------------------------------

    private SystemConfiguration systemConf;

    // ----------------------------------------------------------------------
    //  Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="jdo"
     */
    private ContinuumStore store;

    /**
     * @plexus.requirement role-hint="default"
     */
    private ConfigurationService configurationService;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void initialize()
        throws ContinuumInitializationException
    {
        getLogger().info( "Continuum initializer running ..." );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Dumping JPOX/JDO Schema Details ..." );
            try
            {
                SchemaTool.outputDBInfo( null, true );
                SchemaTool.outputSchemaInfo( null, true );
            }
            catch ( Exception e )
            {
                getLogger().debug( "Error while dumping the database schema", e );
            }
        }

        try
        {
            // System Configuration
            systemConf = store.getSystemConfiguration();

            if ( systemConf == null )
            {
                systemConf = new SystemConfiguration();

                systemConf = store.addSystemConfiguration( systemConf );
            }

            // Schedule
            Schedule s = store.getScheduleByName( DEFAULT_SCHEDULE_NAME );

            if ( s == null )
            {
                Schedule defaultSchedule = createDefaultSchedule();

                store.addSchedule( defaultSchedule );
            }

            createDefaultProjectGroup();
        }
        catch ( ContinuumStoreException e )
        {
            throw new ContinuumInitializationException( "Can't initialize default schedule.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Schedule createDefaultSchedule()
    {
        Schedule schedule = new Schedule();

        schedule.setName( DEFAULT_SCHEDULE_NAME );

        schedule.setDescription( systemConf.getDefaultScheduleDescription() );

        schedule.setCronExpression( systemConf.getDefaultScheduleCronExpression() );

        schedule.setActive( true );

        return schedule;
    }

    private void createDefaultProjectGroup()
        throws ContinuumStoreException
    {
        ProjectGroup group;
        try
        {
            group = store.getProjectGroupByGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );
        }
        catch ( ContinuumObjectNotFoundException e )
        {
            group = new ProjectGroup();

            group.setName( "Default Project Group" );

            group.setGroupId( Continuum.DEFAULT_PROJECT_GROUP_GROUP_ID );

            group.setDescription( "Contains all projects that do not have a group of their own" );

            group.getBuildDefinitions().add( configurationService.getDefaultMavenTwoBuildDefinition() );

            group = store.addProjectGroup( group );
        }
    }
}
