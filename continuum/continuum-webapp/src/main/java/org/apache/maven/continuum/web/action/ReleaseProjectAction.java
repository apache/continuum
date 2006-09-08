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

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.release.ContinuumReleaseManager;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.plugins.release.versions.DefaultVersionInfo;
import org.apache.maven.plugins.release.versions.VersionInfo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin Punzalan
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="releaseProject"
 */
public class ReleaseProjectAction
    extends ContinuumActionSupport
{
    private int projectId;

    private String preparedReleaseName;

    private String goal;

    private Project project;

    private String scmUsername;

    private String scmPassword;

    private String scmTag;

    private String scmTagBase;

    private List projects = new ArrayList();

    public String promptReleaseGoal()
        throws Exception
    {
        project = getContinuum().getProjectWithAllDetails( projectId );

        String releaseId = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

        ContinuumReleaseManager releaseManager = getContinuum().getReleaseManager();

        Map preparedReleases = releaseManager.getPreparedReleases();
        if ( preparedReleases.containsKey( releaseId ) )
        {
            ReleaseDescriptor descriptor = (ReleaseDescriptor) preparedReleases.get( releaseId );

            preparedReleaseName = descriptor.getName();
        }

        return "prompt";
    }

    public String execute()
        throws Exception
    {
        if ( "prepare".equals( goal ) )
        {
            return doPrepare();
        }
        else if ( "perform".equals( goal ) )
        {
            return doPerform();
        }
        else
        {
            return "prompt";
        }
    }

    public String doPrepare()
        throws Exception
    {
        project = getContinuum().getProject( projectId );
        scmUsername = project.getScmUsername();
        scmPassword = project.getScmPassword();
        scmTag = project.getScmTag();
        scmTagBase = "";

        processProject( project.getWorkingDirectory(), "pom.xml" );

        return "prepareRelease";
    }

    public String doPerform()
        throws Exception
    {
        return "performRelease";
    }

    private void processProject( String workingDirectory, String pomFilename )
        throws Exception
    {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read( new FileReader( new File( workingDirectory, pomFilename ) ) );

        if ( model.getGroupId() == null )
        {
            model.setGroupId( model.getParent().getGroupId() );
        }

        if ( model.getVersion() == null )
        {
            model.setVersion( model.getParent().getVersion() );
        }

        setProperties( model );

        for( Iterator modules = model.getModules().iterator(); modules.hasNext(); )
        {
            processProject( workingDirectory + "/" + modules.next().toString(), "pom.xml" );
        }
    }

    private void setProperties( Model model )
        throws Exception
    {
        Map params = new HashMap();

        params.put( "key", model.getGroupId() + ":" + model.getArtifactId() );

        if ( model.getName() == null )
        {
            model.setName( model.getArtifactId() );
        }
        params.put( "name", model.getName() );

        VersionInfo version = new DefaultVersionInfo( project.getVersion() );

        params.put( "release", version.getReleaseVersionString() );
        params.put( "dev", version.getNextVersion().getSnapshotVersionString() );

        projects.add( params );
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getPreparedReleaseName()
    {
        return preparedReleaseName;
    }

    public void setPreparedReleaseName( String preparedReleaseName )
    {
        this.preparedReleaseName = preparedReleaseName;
    }

    public String getGoal()
    {
        return goal;
    }

    public void setGoal( String goal )
    {
        this.goal = goal;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public List getProjects()
    {
        return projects;
    }

    public void setProjects( List projects )
    {
        this.projects = projects;
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
