package org.apache.maven.continuum.core.workflow;

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
 *
 */

import com.opensymphony.workflow.WorkflowException;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.core.action.CreateProjectsFromMetadata;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.osworkflow.PlexusOSWorkflow;
import org.codehaus.plexus.osworkflow.PropertySetMap;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultContinuumWorkflowEngine
    extends AbstractLogEnabled
    implements Initializable, ContinuumWorkflowEngine
{
    /**
     * @plexus.requirement
     */
    private PlexusOSWorkflow workflow;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws InitializationException
    {
    }

    // ----------------------------------------------------------------------
    // ContinuumWorkflowEngine Implementation
    // ----------------------------------------------------------------------

    public long addProjectsFromMetadata( String username, String builderId, String metadataUrl, String workingDirectory,
                                         boolean userInteractive )
        throws ContinuumException
    {
        try
        {
            Map context = new HashMap();
            context.put( CreateProjectsFromMetadata.KEY_PROJECT_BUILDER_ID, builderId );
            context.put( CreateProjectsFromMetadata.KEY_URL, metadataUrl );
            context.put( CreateProjectsFromMetadata.KEY_WORKING_DIRECTORY, workingDirectory );

            return workflow.startWorkflow( "add-projects-from-metadata", username, context );
        }
        catch ( WorkflowException e )
        {
            throw new ContinuumException( "Error while starting workflow.", e );
        }
    }

    public Map getContext( long workflowId )
        throws ContinuumException
    {
        try
        {
            return new PropertySetMap( workflow.getContext( workflowId ) );
        }
        catch ( WorkflowException e )
        {
            throw new ContinuumException( "Error while getting the workflow context.", e );
        }
    }

    public void waitForWorkflow( long workflowId )
        throws ContinuumException
    {
        try
        {
            while( !workflow.isWorkflowDone( workflowId ) )
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e )
                {
                    // continue
                }
            }
        }
        catch ( WorkflowException e )
        {
            throw new ContinuumException( "Error while waiting for workflow to complete", e );
        }
    }

    public List getCurrentSteps( long workflowId )
        throws ContinuumException
    {
        try
        {
            return workflow.getCurrentSteps( workflowId );
        }
        catch ( WorkflowException e )
        {
            throw new ContinuumException( "Error while getting the current steps for the workflow.", e );
        }
    }

    public void executeAction( long workflowId, int actionId, Map context )
        throws ContinuumException
    {
        try
        {
            workflow.doAction( workflowId, actionId, context );
        }
        catch ( WorkflowException e )
        {
            throw new ContinuumException( "Error while calling action.", e );
        }
    }
}
