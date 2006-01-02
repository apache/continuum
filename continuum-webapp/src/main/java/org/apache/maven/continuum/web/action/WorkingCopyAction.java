package org.apache.maven.continuum.web.action;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.web.util.WorkingCopyContentGenerator;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;
import com.opensymphony.xwork.ActionSupport;

import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class WorkingCopyAction
    extends ActionSupport
{
    private Continuum continuum;

    private WorkingCopyContentGenerator generator;

    private int projectId;

    private String projectName;

    private String userDirectory;

    private List files;

    private String output;

    public String execute()
    {
        try
        {
            files = continuum.getFiles( projectId, userDirectory );

            HashMap params = new HashMap();

            params.put( "projectId", new Integer( projectId ) );

            params.put( "projectName", new Integer( projectName ) );

            String baseUrl = UrlHelper.buildUrl( "/workingCopy.action", ServletActionContext.getRequest(), ServletActionContext.getResponse(), params );

            output = generator.generate( files, baseUrl, continuum.getWorkingDirectory( projectId ) );
        }
        catch ( ContinuumException e )
        {
            addActionError( "Can't get file list for project (id=" + projectId + ") : " + e.getMessage() );

            e.printStackTrace();

            return ERROR;
        }
        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public String getUserDirectory()
    {
        return userDirectory;
    }

    public void setUserDirectory( String userDirectory )
    {
        this.userDirectory = userDirectory;
    }

    public List getFiles()
    {
        return files;
    }

    public String getOutput()
    {
        return output;
    }
}