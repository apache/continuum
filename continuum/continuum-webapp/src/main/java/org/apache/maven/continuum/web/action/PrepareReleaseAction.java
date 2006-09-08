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

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="prepareRelease"
 */
public class PrepareReleaseAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private List projectKeys;

    private List devVersions;

    private List relVersions;

    public String execute()
        throws Exception
    {
        Project project = getContinuum().getProject( projectId );

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        releaseManager.prepare( project, getReleaseProperties(), getRelVersionMap(), getDevVersionMap() );

        return SUCCESS;
    }

    private Map getDevVersionMap()
    {
        return getVersionMap( projectKeys, devVersions );
    }

    private Map getRelVersionMap()
    {
        return getVersionMap( projectKeys, relVersions );
    }

    private Map getVersionMap( List keys, List versions )
    {
        Map versionMap = new HashMap();

        for ( int idx = 0; idx < keys.size(); idx++ )
        {
            String key = keys.get( idx ).toString();
            String version = versions.get( idx ).toString();

            versionMap.put( key, version );
        }

        return versionMap;
    }

    private Properties getReleaseProperties()
    {
        Properties p = new Properties();

        p.setProperty( "tag", scmTag );
        p.setProperty( "tagBase", scmTagBase );

        return p;
    }

    public List getProjectKeys()
    {
        return projectKeys;
    }

    public void setProjectKeys( List projectKeys )
    {
        this.projectKeys = projectKeys;
    }

    public List getDevVersions()
    {
        return devVersions;
    }

    public void setDevVersions( List devVersions )
    {
        this.devVersions = devVersions;
    }

    public List getRelVersions()
    {
        return relVersions;
    }

    public void setRelVersions( List relVersions )
    {
        this.relVersions = relVersions;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getScmUsername()
    {
        return scmUsername;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmPassword()
    {
        return scmPassword;
    }

    public void setScmPassword( String scmPassword )
    {
        this.scmPassword = scmPassword;
    }

    public String getScmTag()
    {
        return scmTag;
    }

    public void setScmTag( String scmTag )
    {
        this.scmTag = scmTag;
    }

    public String getScmTagBase()
    {
        return scmTagBase;
    }

    public void setScmTagBase( String scmTagBase )
    {
        this.scmTagBase = scmTagBase;
    }
}
