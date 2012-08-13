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

import java.util.HashMap;
import java.util.Map;

/**
 * Action that edits a {@link ProjectNotifier} of type 'Wagon' from the
 * specified {@link Project}.
 *
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="wagonProjectNotifierEdit"
 */

public class WagonProjectNotifierEditAction
    extends AbstractProjectNotifierEditAction
{
    private String url;

    private String id;

    protected void initConfiguration( Map<String, String> configuration )
    {
        url = configuration.get( "url" );
        id = configuration.get( "id" );
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap<String, String> configuration = new HashMap<String, String>();

        configuration.put( "url", url );

        configuration.put( "id", id );

        notifier.setConfiguration( configuration );
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }
}
