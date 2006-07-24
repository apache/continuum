package org.apache.maven.continuum.web.action;

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
 */

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;

import java.util.Collection;

import org.codehaus.plexus.xwork.action.PlexusActionSupport;

/**
 * @author Henry Isidro
 * @version $Id$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="users"
 */
public class UsersAction
    extends PlexusActionSupport
{
    private Continuum continuum;

    private Collection users;

    public String execute()
    {
        try
        {
            users = continuum.getUsers();
        }
        catch ( ContinuumException ce )
        {
            addActionError( "Can't get continuum users: " + ce.getMessage() );

            ce.printStackTrace();

            return ERROR;
        }

        return SUCCESS;
    }

    public Collection getUsers()
    {
        return users;
    }

}
