package org.apache.continuum.web.action;

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

import org.apache.continuum.model.project.ProjectScmRoot;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.struts2.ServletActionContext;

/**
 * @author <a href="mailto:ctan@apache.org">Maria Catherine Tan</a>
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="scmResult"
 */
public class ScmResultAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private int projectScmRootId;

    private String projectGroupName;

    private String state;

    private ProjectScmRoot projectScmRoot;

    public String execute()
        throws Exception
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        projectScmRoot = getContinuum().getProjectScmRoot( projectScmRootId );

        state = StateGenerator.generate( projectScmRoot.getState(),
                                         ServletActionContext.getRequest().getContextPath() );

        return SUCCESS;
    }

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectScmRootId()
    {
        return projectScmRootId;
    }

    public void setProjectScmRootId( int projectScmRootId )
    {
        this.projectScmRootId = projectScmRootId;
    }

    public ProjectScmRoot getProjectScmRoot()
    {
        return projectScmRoot;
    }

    public void setProjectScmRoot( ProjectScmRoot projectScmRoot )
    {
        this.projectScmRoot = projectScmRoot;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        projectGroupName = getContinuum().getProjectGroup( getProjectGroupId() ).getName();

        return projectGroupName;
    }

    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    public String getState()
    {
        return state;
    }

    public void setState( String state )
    {
        this.state = state;
    }
}
