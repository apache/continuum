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

import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.webwork.ServletActionContext;

import java.util.Collection;

public class SummaryAction
    extends ActionSupport
{
    private Continuum continuum;
    
    private Collection projects;

    public String execute()
        throws Exception
    {
        try
        {
            projects = continuum.getProjects();
            ServletActionContext.getRequest().setAttribute( "projects", projects );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public Collection getProjects()
    {
        return projects;
    }
}
