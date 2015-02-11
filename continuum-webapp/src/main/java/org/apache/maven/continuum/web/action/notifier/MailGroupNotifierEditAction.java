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

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.notification.AbstractContinuumNotifier;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that edits a {@link ProjectNotifier} of type 'Mail' from the
 * specified {@link ProjectGroup}.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @since 1.1
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "mailGroupNotifierEdit", instantiationStrategy = "per-lookup" )
public class MailGroupNotifierEditAction
    extends AbstractGroupNotifierEditAction
{
    private String address;

    private boolean committers;

    private boolean developers;

    protected void initConfiguration( Map<String, String> configuration )
    {
        if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD ) ) )
        {
            address = configuration.get( AbstractContinuumNotifier.ADDRESS_FIELD );
        }

        if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) ) )
        {
            committers = Boolean.parseBoolean( configuration.get( AbstractContinuumNotifier.COMMITTER_FIELD ) );
        }

        if ( StringUtils.isNotEmpty( configuration.get( AbstractContinuumNotifier.DEVELOPER_FIELD ) ) )
        {
            developers = Boolean.parseBoolean( configuration.get( AbstractContinuumNotifier.DEVELOPER_FIELD ) );
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

        configuration.put( AbstractContinuumNotifier.DEVELOPER_FIELD, String.valueOf( developers ) );

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

    public boolean isDevelopers()
    {
        return developers;
    }

    public void setDevelopers( boolean developers )
    {
        this.developers = developers;
    }
}
