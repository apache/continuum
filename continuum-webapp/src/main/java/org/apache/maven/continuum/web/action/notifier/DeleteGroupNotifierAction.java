package org.apache.maven.continuum.web.action.notifier;

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

import org.apache.continuum.web.util.GenerateRecipentNotifier;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Action to delete a {@link ProjectNotifier} instance from a
 * specified {@link ProjectGroup}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="deleteGroupNotifier"
 * @since 1.1
 */
public class DeleteGroupNotifierAction
    extends ContinuumActionSupport
{
    private int projectGroupId;

    private int notifierId;

    private String notifierType;

    private String recipient;

    private String projectGroupName = "";

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectGroupNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        getContinuum().removeGroupNotifier( projectGroupId, notifierId );

        return SUCCESS;
    }

    public String doDefault()
        throws ContinuumException
    {
        try
        {
            checkRemoveProjectGroupNotifierAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }

        ProjectNotifier notifier = getContinuum().getGroupNotifier( projectGroupId, notifierId );

        notifierType = notifier.getType();

        recipient = GenerateRecipentNotifier.generate( notifier );

        return "delete";
    }

    public void setNotifierId( int notifierId )
    {
        this.notifierId = notifierId;
    }

    public int getNotifierId()
    {
        return notifierId;
    }

    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    public String getNotifierType()
    {
        return notifierType;
    }

    /**
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    public int getProjectId()
    {
        //flags that this is a group notifier
        return -1;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void setRecipient( String recipient )
    {
        this.recipient = recipient;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroup( projectGroupId ).getName();
        }

        return projectGroupName;
    }
}
