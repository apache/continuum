package org.apache.continuum.taskqueue;

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

import java.io.Serializable;

import org.apache.maven.continuum.model.scm.ScmResult;
import org.codehaus.plexus.taskqueue.Task;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class BuildProjectTask
    implements Task, Serializable
{    
	private static final long serialVersionUID = 3647734422022017812L;

	private int projectId;

    private int buildDefinitionId;

    private long timestamp;

    private int trigger;

    private long maxExecutionTime;
    
    private String projectName;
    
    private String buildDefinitionLabel;

    private ScmResult scmResult;

    public BuildProjectTask( int projectId, int buildDefinitionId, int trigger, String projectName,
                             String buildDefinitionLabel, ScmResult scmResult )
    {
        this.projectId = projectId;

        this.buildDefinitionId = buildDefinitionId;

        this.timestamp = System.currentTimeMillis();

        this.trigger = trigger;

        this.projectName = projectName;
        
        this.buildDefinitionLabel = buildDefinitionLabel;

        this.scmResult = scmResult;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public int getBuildDefinitionId()
    {
        return buildDefinitionId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getTrigger()
    {
        return trigger;
    }

    public void setMaxExecutionTime( long maxExecutionTime )
    {
        this.maxExecutionTime = maxExecutionTime;
    }

    public long getMaxExecutionTime()
    {
        return maxExecutionTime;
    }

    public String getProjectName()
    {
        return projectName;
    }    
    
    public String getBuildDefinitionLabel()
    {
        return buildDefinitionLabel;
    }    

    public ScmResult getScmResult()
    {
        return scmResult;
    }

    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( obj == this )
        {
            return true;
        }
        if ( !( obj instanceof BuildProjectTask ) )
        {
            return false;
        }
        BuildProjectTask buildProjectTask = (BuildProjectTask) obj;
        return buildProjectTask.getBuildDefinitionId() == this.getBuildDefinitionId()
            && buildProjectTask.getProjectId() == this.getProjectId()
            && buildProjectTask.getTrigger() == this.getTrigger();
    }

    public int hashCode()
    {
        return this.getBuildDefinitionId() + this.getProjectId() + this.getTrigger();
    }
    
    public int getHashCode()
    {
        return this.hashCode();
    }    
}
