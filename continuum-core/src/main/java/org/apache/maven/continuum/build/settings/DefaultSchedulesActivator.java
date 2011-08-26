package org.apache.maven.continuum.build.settings;

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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.continuum.dao.BuildDefinitionDao;
import org.apache.continuum.dao.DirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.DistributedDirectoryPurgeConfigurationDao;
import org.apache.continuum.dao.RepositoryPurgeConfigurationDao;
import org.apache.continuum.dao.ScheduleDao;
import org.apache.continuum.model.repository.DirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.DistributedDirectoryPurgeConfiguration;
import org.apache.continuum.model.repository.RepositoryPurgeConfiguration;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.scheduler.ContinuumBuildJob;
import org.apache.maven.continuum.scheduler.ContinuumPurgeJob;
import org.apache.maven.continuum.scheduler.ContinuumSchedulerConstants;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.scheduler.AbstractJob;
import org.codehaus.plexus.scheduler.Scheduler;
import org.codehaus.plexus.util.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.build.settings.SchedulesActivator"
 */
public class DefaultSchedulesActivator
    implements SchedulesActivator
{
    private static final Logger log = LoggerFactory.getLogger( DefaultSchedulesActivator.class );

    /**
     * @plexus.requirement
     */
    private DirectoryPurgeConfigurationDao directoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private RepositoryPurgeConfigurationDao repositoryPurgeConfigurationDao;

    /**
     * @plexus.requirement
     */
    private DistributedDirectoryPurgeConfigurationDao distributedDirectoryPurgeConfigurationDao;


    /**
     * @plexus.requirement
     */
    private BuildDefinitionDao buildDefinitionDao;

    /**
     * @plexus.requirement
     */
    private ScheduleDao scheduleDao;

    /**
     * @plexus.requirement role-hint="default"
     */
    private Scheduler scheduler;

    // private int delay = 3600;
    private static final int delay = 1;

    public void activateSchedules( Continuum continuum )
        throws SchedulesActivationException
    {
        log.info( "Activating schedules ..." );

        Collection<Schedule> schedules = scheduleDao.getAllSchedulesByName();

        for ( Schedule schedule : schedules )
        {
            if ( schedule.isActive() )
            {
                try
                {
                    activateSchedule( schedule, continuum );
                }
                catch ( SchedulesActivationException e )
                {
                    log.error( "Can't activate schedule '" + schedule.getName() + "'", e );

                    schedule.setActive( false );

                    try
                    {
                        scheduleDao.storeSchedule( schedule );
                    }
                    catch ( ContinuumStoreException e1 )
                    {
                        throw new SchedulesActivationException( "Can't desactivate schedule '" + schedule.getName()
                            + "'", e );
                    }
                }
            }
        }
    }

    public void activateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        if ( schedule != null )
        {
            log.info( "Activating schedule " + schedule.getName() );

            activateBuildSchedule( schedule, continuum );

            activatePurgeSchedule( schedule, continuum );
        }
    }

    public void activateBuildSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        if ( schedule != null && schedule.isActive() && isScheduleFromBuildJob( schedule ) )
        {
            schedule( schedule, continuum, ContinuumBuildJob.class, ContinuumBuildJob.BUILD_GROUP );
        }
    }

    public void activatePurgeSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        if ( schedule != null && schedule.isActive() && isScheduleFromPurgeJob( schedule ) )
        {
            schedule( schedule, continuum, ContinuumPurgeJob.class, ContinuumPurgeJob.PURGE_GROUP );
        }
    }

    public void unactivateSchedule( Schedule schedule, Continuum continuum )
        throws SchedulesActivationException
    {
        log.info( "Deactivating schedule " + schedule.getName() );

        unactivateBuildSchedule( schedule );
        unactivatePurgeSchedule( schedule );
    }

    public void unactivateOrphanBuildSchedule( Schedule schedule )
        throws SchedulesActivationException
    {
        if ( schedule != null && !isScheduleFromBuildJob( schedule ) )
        {
            unactivateBuildSchedule( schedule );
        }
    }

    public void unactivateOrphanPurgeSchedule( Schedule schedule )
        throws SchedulesActivationException
    {
        if ( schedule != null && !isScheduleFromPurgeJob( schedule ) )
        {
            unactivatePurgeSchedule( schedule );
        }
    }

    private void unactivateBuildSchedule( Schedule schedule )
        throws SchedulesActivationException
    {
        log.debug( "Deactivating schedule " + schedule.getName() + " for Build Process" );

        unschedule( schedule, ContinuumBuildJob.BUILD_GROUP );
    }

    private void unactivatePurgeSchedule( Schedule schedule )
        throws SchedulesActivationException
    {
        log.debug( "Deactivating schedule " + schedule.getName() + " for Purge Process" );

        unschedule( schedule, ContinuumPurgeJob.PURGE_GROUP );
    }

    protected void schedule( Schedule schedule, Continuum continuum, Class jobClass, String group )
        throws SchedulesActivationException
    {
        if ( StringUtils.isEmpty( schedule.getCronExpression() ) )
        {
            log.info( "Not scheduling " + schedule.getName() );
            return;
        }

        JobDataMap dataMap = new JobDataMap();

        dataMap.put( "continuum", continuum );

        dataMap.put( AbstractJob.LOGGER, log );

        dataMap.put( ContinuumSchedulerConstants.SCHEDULE, schedule );

        // the name + group makes the job unique

        JobDetail jobDetail = new JobDetail( schedule.getName(), group, jobClass );

        jobDetail.setJobDataMap( dataMap );

        jobDetail.setDescription( schedule.getDescription() );

        CronTrigger trigger = new CronTrigger();

        trigger.setName( schedule.getName() );

        trigger.setGroup( group );

        Date startTime = new Date( System.currentTimeMillis() + delay * 1000 );

        trigger.setStartTime( startTime );

        trigger.setNextFireTime( startTime );

        try
        {
            trigger.setCronExpression( schedule.getCronExpression() );
        }
        catch ( ParseException e )
        {
            throw new SchedulesActivationException( "Error parsing cron expression.", e );
        }

        try
        {
            scheduler.scheduleJob( jobDetail, trigger );

            log.info( trigger.getName() + ": next fire time ->" + trigger.getNextFireTime() );
        }
        catch ( SchedulerException e )
        {
            throw new SchedulesActivationException( "Cannot schedule job ->" + jobClass.getName(), e );
        }
    }

    private void unschedule( Schedule schedule, String group )
        throws SchedulesActivationException
    {
        try
        {
            if ( schedule.isActive() )
            {
                log.info( "Stopping active schedule \"" + schedule.getName() + "\"." );

                scheduler.interruptSchedule( schedule.getName(), group );
            }

            scheduler.unscheduleJob( schedule.getName(), group );
        }
        catch ( SchedulerException e )
        {
            throw new SchedulesActivationException( "Cannot unschedule build job \"" + schedule.getName() + "\".", e );
        }
    }

    private boolean isScheduleFromBuildJob( Schedule schedule )
    {
        List<BuildDefinition> buildDef = buildDefinitionDao.getBuildDefinitionsBySchedule( schedule.getId() );
        // Take account templateBuildDefinition too.
        // A improvement will be add schedule only for active buildDefinition, but it would need activate
        // schedule job in add project and add group process
        return buildDef.size() > 0;

    }

    private boolean isScheduleFromPurgeJob( Schedule schedule )
    {
        List<RepositoryPurgeConfiguration> repoPurgeConfigs =
            repositoryPurgeConfigurationDao.getEnableRepositoryPurgeConfigurationsBySchedule( schedule.getId() );
        List<DirectoryPurgeConfiguration> dirPurgeConfigs =
            directoryPurgeConfigurationDao.getEnableDirectoryPurgeConfigurationsBySchedule( schedule.getId() );
        List<DistributedDirectoryPurgeConfiguration> distriDirPurgeConfigs = 
            distributedDirectoryPurgeConfigurationDao.getEnableDistributedDirectoryPurgeConfigurationsBySchedule( schedule.getId() );

        return repoPurgeConfigs.size() > 0 || dirPurgeConfigs.size() > 0 || distriDirPurgeConfigs.size() > 0;

    }
}
