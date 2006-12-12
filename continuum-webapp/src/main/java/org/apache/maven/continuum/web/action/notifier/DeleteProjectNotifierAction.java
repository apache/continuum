package org.apache.maven.continuum.web.action.notifier;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
 */

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

/**
 * Action that deletes a {@link ProjectNotifier} of type 'IRC' from the specified {@link ProjectGroup}.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: DeleteNotifierAction.java 467122 2006-10-23 20:50:19Z jmcconnell $
 * 
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="deleteProjectNotifier"
 */
public class DeleteProjectNotifierAction extends ContinuumActionSupport
{

    private int projectId;

    /**
     * Identifier for the {@link ProjectGroup} that the current {@link Project} is a member of.
     */
    private int projectGroupId;

    private int notifierId;

    private String notifierType;

    public String execute() throws ContinuumException
    {
        getContinuum().removeNotifier( projectId, notifierId );
        return SUCCESS;
    }

    public String doDefault()
    {
        return "delete";
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public int getProjectId()
    {
        return projectId;
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

    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

}
