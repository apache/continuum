package org.apache.maven.continuum.web.action.admin;

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

import com.opensymphony.xwork2.Preparable;
import org.apache.continuum.buildmanager.BuildManagerException;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.List;

@Component( role = com.opensymphony.xwork2.Action.class, hint = "buildQueueAction", instantiationStrategy = "per-lookup" )
public class BuildQueueAction
    extends ContinuumConfirmAction
    implements Preparable, SecureAction
{
    private String name;

    private int size;

    private List<BuildQueue> buildQueueList;

    private BuildQueue buildQueue;

    private String message;

    private boolean confirmed;

    public void prepare()
        throws ContinuumException
    {
        this.buildQueueList = getContinuum().getAllBuildQueues();
    }

    public String input()
    {
        return INPUT;
    }

    public String list()
        throws Exception
    {
        try
        {
            this.buildQueueList = getContinuum().getAllBuildQueues();
        }
        catch ( ContinuumException e )
        {
            addActionError( "Cannot get build queues from the database : " + e.getMessage() );
            return ERROR;
        }
        return SUCCESS;
    }

    public String save()
        throws Exception
    {
        int allowedBuilds = getContinuum().getConfiguration().getNumberOfBuildsInParallel();
        if ( allowedBuilds < ( this.buildQueueList.size() + 1 ) )
        {
            addActionError( "You are only allowed " + allowedBuilds + " number of builds in parallel." );
            return ERROR;
        }
        else
        {
            try
            {
                if ( !isDuplicate( name ) )
                {
                    BuildQueue buildQueue = new BuildQueue();
                    buildQueue.setName( name );
                    BuildQueue addedBuildQueue = getContinuum().addBuildQueue( buildQueue );

                    getContinuum().getBuildsManager().addOverallBuildQueue( addedBuildQueue );

                    AuditLog event = new AuditLog( "Build Queue id=" + addedBuildQueue.getId(),
                                                   AuditLogConstants.ADD_BUILD_QUEUE );
                    event.setCategory( AuditLogConstants.BUILD_QUEUE );
                    event.setCurrentUser( getPrincipal() );
                    event.log();
                }
                else
                {
                    addActionError( "Build queue name already exists." );
                    return ERROR;
                }
            }
            catch ( ContinuumException e )
            {
                addActionError( "Error adding build queue to database: " + e.getMessage() );
                return ERROR;
            }
            catch ( BuildManagerException e )
            {
                addActionError( "Error creating overall build queue: " + e.getMessage() );
                return ERROR;
            }

            return SUCCESS;
        }
    }

    public String edit()
        throws Exception
    {
        try
        {
            BuildQueue buildQueueToBeEdited = getContinuum().getBuildQueue( this.buildQueue.getId() );
        }
        catch ( ContinuumException e )
        {
            addActionError( "Error retrieving build queue from the database : " + e.getMessage() );
            return ERROR;
        }
        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        if ( confirmed )
        {
            BuildQueue buildQueueToBeDeleted = getContinuum().getBuildQueue( this.buildQueue.getId() );
            getContinuum().getBuildsManager().removeOverallBuildQueue( buildQueueToBeDeleted.getId() );
            getContinuum().removeBuildQueue( buildQueueToBeDeleted );

            this.buildQueueList = getContinuum().getAllBuildQueues();

            AuditLog event = new AuditLog( "Build Queue id=" + buildQueue.getId(),
                                           AuditLogConstants.REMOVE_BUILD_QUEUE );
            event.setCategory( AuditLogConstants.BUILD_QUEUE );
            event.setCurrentUser( getPrincipal() );
            event.log();
        }
        else
        {
            return CONFIRM;
        }

        return SUCCESS;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_PARALLEL_BUILDS, Resource.GLOBAL );

        return bundle;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<BuildQueue> getBuildQueueList()
    {
        return buildQueueList;
    }

    public void setBuildQueueList( List<BuildQueue> buildQueueList )
    {
        this.buildQueueList = buildQueueList;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize( int size )
    {
        this.size = size;
    }

    public BuildQueue getBuildQueue()
    {
        return buildQueue;
    }

    public void setBuildQueue( BuildQueue buildQueue )
    {
        this.buildQueue = buildQueue;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    private boolean isDuplicate( String queueName )
        throws ContinuumException
    {
        boolean isExisting = false;

        List<BuildQueue> buildQueues = getContinuum().getAllBuildQueues();

        for ( BuildQueue bq : buildQueues )
        {
            if ( queueName.equals( bq.getName() ) )
            {
                isExisting = true;
                break;
            }
        }

        return isExisting;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
}
