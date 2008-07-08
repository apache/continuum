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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that edits a {@link ProjectNotifier} of type 'Mail' from the
 * specified {@link Project}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: MailNotifierEditAction.java 465060 2006-10-17 21:24:38Z jmcconnell $
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="mailProjectNotifierEdit"
 * @since 1.1
 */
public class MailProjectNotifierEditAction
    extends AbstractProjectNotifierEditAction
{
    private String address;

    private boolean committers;

    protected void initConfiguration( Map configuration )
    {
        if ( StringUtils.isNotEmpty( (String) configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD ) ) )
        {
            address = (String) configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD );
        }

        if ( StringUtils.isNotEmpty( (String) configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
        {
            committers =
                Boolean.parseBoolean( (String) configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) );
        }
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap<String, Object> configuration = new HashMap<String, Object>();

        if ( StringUtils.isNotEmpty( address ) )
        {
            configuration.put( AbstractContinuumNotifier.ADDRESS_FIELD, address );
        }

        configuration.put( AbstractContinuumNotifier.COMMITTER_FIELD, String.valueOf( committers ) );

        notifier.setConfiguration( configuration );
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress( String address )
    {
        this.address = address;
    }

    public boolean isCommitters()
    {
        return committers;
    }

    public void setCommitters( boolean committers )
    {
        this.committers = committers;
    }
}
