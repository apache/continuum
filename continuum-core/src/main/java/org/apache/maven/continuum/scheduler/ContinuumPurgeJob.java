package org.apache.maven.continuum.scheduler;

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

import org.apache.continuum.purge.ContinuumPurgeManager;
import org.apache.continuum.purge.ContinuumPurgeManagerException;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Schedule;
import org.codehaus.plexus.scheduler.AbstractJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;

/**
 * @author Maria Catherine Tan
 * @version $Id$
 * @since 25 jul 07
 */
public class ContinuumPurgeJob
    extends AbstractJob
{
    public static final String PURGE_GROUP = "PURGE_GROUP";

    public void execute( JobExecutionContext context )
    {
        if ( isInterrupted() )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Get the job detail
        // ----------------------------------------------------------------------

        JobDetail jobDetail = context.getJobDetail();

        // ----------------------------------------------------------------------
        // Get data map out of the job detail
        // ----------------------------------------------------------------------

        Logger logger = (Logger) jobDetail.getJobDataMap().get( AbstractJob.LOGGER );

        String jobName = jobDetail.getName();

        logger.info( ">>>>>>>>>>>>>>>>>>>>> Executing purge job (" + jobName + ")..." );

        Continuum continuum = (Continuum) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.CONTINUUM );

        ContinuumPurgeManager purgeManager = continuum.getPurgeManager();

        Schedule schedule = (Schedule) jobDetail.getJobDataMap().get( ContinuumSchedulerConstants.SCHEDULE );

        try
        {
            purgeManager.purge( schedule );
        }
        catch ( ContinuumPurgeManagerException e )
        {
            logger.error( "Error purging for job" + jobName + ".", e );
        }

        try
        {
            if ( schedule.getDelay() > 0 )
            {
                Thread.sleep( schedule.getDelay() * 1000 );
            }
        }
        catch ( InterruptedException e )
        {
        }
    }
}