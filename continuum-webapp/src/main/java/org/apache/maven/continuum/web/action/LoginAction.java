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

import com.opensymphony.xwork.ActionSupport;
import org.codehaus.plexus.util.StringUtils;

public class LoginAction
    extends ActionSupport
{
    private String username;

    private String password;

    public String execute()
        throws Exception
    {
        //TODO
        if ( StringUtils.isEmpty( username ) || StringUtils.isEmpty( password ) || !"testuser".equals( username ) )
        {
            //TODO : i18n
            addFieldError( "username", getText( "login.bad_login_password" ) );

            return INPUT;
        }

        return SUCCESS;
    }

    public String doDefault()
    {
        return INPUT;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }
}
