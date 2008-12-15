package org.apache.maven.continuum.web.action.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.dao.BuildQueueDao;
import org.apache.continuum.taskqueue.DefaultOverallBuildQueue;
import org.apache.continuum.taskqueue.OverallBuildQueue;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.store.ContinuumStoreException;

import com.opensymphony.xwork2.Preparable;

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

/**
 *
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="buildQueueAction"
 */
public class BuildQueueAction 
    extends AbstractBuildQueueAction
    implements Preparable
{
	private String name;
	
	private int size;
	
    private List<BuildQueue> buildQueueList;
    
    /**
     * @plexus.requirement
     */
    private BuildQueueDao buildQueueDao;
    
    private BuildQueue buildQueue;

    public void prepare()
        throws ContinuumStoreException
    {
	    this.buildQueueList = buildQueueDao.getAllBuildQueues();
    }
    
    public String input()
    {	
        return INPUT;
    }
    
    public String list()
        throws Exception
    {
        this.buildQueueList = buildQueueDao.getAllBuildQueues();
        return SUCCESS;
    }
    
    public String save()
        throws Exception
    {
    	
    	int allowedBuilds = getContinuum().getConfiguration().getNumberOfBuildsInParallel();
    	if ( allowedBuilds <= this.buildQueueList.size() )
    	{
    		addActionError(" You are only allowed " + allowedBuilds );
    		return ERROR;
    	}
    	else
    	{
    		BuildQueue buildQueue = new BuildQueue();	
        	buildQueue.setId( buildQueueList.size() + 1 );
        	buildQueue.setName( name );
        	buildQueueDao.addBuildQueue( buildQueue );
        	
        	return SUCCESS;
    	}
    	
    }
    
    public String edit()
        throws Exception
    {
        BuildQueue buildQueueToBeEdited = buildQueueDao.getBuildQueue( this.buildQueue.getId() );
        return SUCCESS;
    }
    
    public String delete()
        throws Exception
    {
    	BuildQueue buildQueueToBeDeleted = buildQueueDao.getBuildQueue( this.buildQueue.getId() );
    	buildQueueDao.removeBuildQueue( buildQueueToBeDeleted );

    	this.buildQueueList = buildQueueDao.getAllBuildQueues();
    	
        return SUCCESS;
    }

	public String getName() 
	{
	    return name;
	}

	public void setName(String name) 
	{
	    this.name = name;
	}

	public List<BuildQueue> getBuildQueueList() 
	{
	    return buildQueueList;
	}

	public void setBuildQueueList(List<BuildQueue> buildQueueList) 
	{
	    this.buildQueueList = buildQueueList;
	}

	public BuildQueueDao getBuildQueueDao() 
	{
	    return buildQueueDao;
	}

	public void setBuildQueueDao(BuildQueueDao buildQueueDao) 
	{
	    this.buildQueueDao = buildQueueDao;
	}

	public int getSize() 
	{
		return size;
	}

	public void setSize(int size) 
	{
		this.size = size;
	}

	public BuildQueue getBuildQueue() 
	{
	    return buildQueue;
	}

	public void setBuildQueue(BuildQueue buildQueue) 
	{
	    this.buildQueue = buildQueue;
	}
}
