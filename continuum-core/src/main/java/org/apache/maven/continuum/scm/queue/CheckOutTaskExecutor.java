package org.apache.maven.continuum.scm.queue;

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

import org.apache.continuum.dao.ProjectDao;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.action.ActionManager;
import org.codehaus.plexus.taskqueue.Task;
import org.codehaus.plexus.taskqueue.execution.TaskExecutionException;
import org.codehaus.plexus.taskqueue.execution.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.taskqueue.execution.TaskExecutor"
 * role-hint="check-out-project"
 */
public class CheckOutTaskExecutor
    implements TaskExecutor
{
    private Logger log = LoggerFactory.getLogger( CheckOutTaskExecutor.class );

    /**
     * @plexus.requirement
     */
    private ActionManager actionManager;

    /**
     * @plexus.requirement
     */
    private ProjectDao projectDao;

    // ----------------------------------------------------------------------
    // TaskExecutor Implementation
    // ----------------------------------------------------------------------

    public void executeTask( Task t )
        throws TaskExecutionException
    {
        CheckOutTask task = (CheckOutTask) t;

        int projectId = task.getProjectId();

        Project project;

        try
        {
            project = projectDao.getProjectWithBuildDetails( projectId );
        }
        catch ( ContinuumStoreException ex )
        {
            log.error( "Internal error while getting the project.", ex );

            return;
        }

        String workingDirectory = task.getWorkingDirectory().getAbsolutePath();

        Map<String, Object> context = new HashMap<String, Object>();

        context.put( AbstractContinuumAction.KEY_PROJECT_ID, new Integer( projectId ) );

        context.put( AbstractContinuumAction.KEY_PROJECT, project );

        context.put( AbstractContinuumAction.KEY_WORKING_DIRECTORY, workingDirectory );

        context.put( AbstractContinuumAction.KEY_SCM_USERNAME, task.getScmUserName() );

        context.put( AbstractContinuumAction.KEY_SCM_PASSWORD, task.getScmPassword() );

        try
        {
            actionManager.lookup( "checkout-project" ).execute( context );

            actionManager.lookup( "store-checkout-scm-result" ).execute( context );
        }
        catch ( Exception e )
        {
            throw new TaskExecutionException( "Error checking out project.", e );
        }
    }
}
