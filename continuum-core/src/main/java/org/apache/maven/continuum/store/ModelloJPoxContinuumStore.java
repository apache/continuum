package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.ScmResult;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo All validation should happen at the project application but things like the state gaurd
 * should not be present in the store. We have to move toward being able to generate more of
 * these methods automatically so any app customizatoins would prevent this from working.
 *
 * @todo we could create the necessary ContinuumProject else where and use the addContinuumProject that is present in the store class generated by modello.
 * @todo how to build in some basic query methods so we can describe what we want and have it be generated
 */
public class ModelloJPoxContinuumStore
    extends AbstractContinuumStore
    implements ContinuumStore, Initializable
{
    /** @plexus.requirement */
    private JdoFactory jdoFactory;

    private ContinuumJPoxStore store;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        store = new ContinuumJPoxStore( jdoFactory.getPersistenceManagerFactory() );
    }

    // ----------------------------------------------------------------------
    // ContinuumStore Implementation
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // ContinuumProject
    // ----------------------------------------------------------------------

    public String addProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        try
        {
            Object id = store.addContinuumProject( project );

            project = store.getContinuumProjectByJdoId( id, true );

            return project.getId();
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while adding a project.", e );
        }
    }

    public void removeProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            // TODO: This whole section is dumb.
            PersistenceManager pm = store.getThreadState().getPersistenceManager();

            List builds = project.getBuilds();

            for ( Iterator it = builds.iterator(); it.hasNext(); )
            {
                ContinuumBuild build = (ContinuumBuild) it.next();

                pm.deletePersistent( build );
            }

            pm.deletePersistentAll( builds );

            store.deleteContinuumProject( projectId );

            store.commit();

            getLogger().info( "Removed project with id '" + projectId + "'." );
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while removing project with id '" + projectId + "'.", e );
        }
    }

    public void updateProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        try
        {
            project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );

            store.storeContinuumProject( project );

            project = store.getContinuumProject( project.getId(), true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while setting the working directory.", e );
        }
    }

    public Collection getAllProjects()
        throws ContinuumStoreException
    {
        try
        {
            Collection collection = store.getContinuumProjectCollection( true, null, "name ascending" );

            for ( Iterator it = collection.iterator(); it.hasNext(); )
            {
                setProjectState( (ContinuumProject) it.next() );
            }

            return collection;
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading project set", e );
        }
    }

    public ContinuumProject getProjectByName( String name )
        throws ContinuumStoreException
    {
        try
        {
            String filter = "this.name == \"" + name + "\"";

            String ordering = "";

            store.begin();

            Collection projects = store.getContinuumProjectCollection( true, filter, ordering );

            if ( projects.size() == 0 )
            {
                store.commit();

                return null;
            }

            ContinuumProject project = (ContinuumProject) projects.iterator().next();

            setProjectState( project );

            store.commit();

            return project;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while loading project set", e );
        }
    }

    public ContinuumProject getProjectByScmUrl( String scmUrl )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            String filter = "this.scmUrl == \"" + scmUrl + "\"";

            String ordering = "";

            Collection projects = store.getContinuumProjectCollection( true, filter, ordering );

            if ( projects.size() == 0 )
            {
                store.commit();

                return null;
            }

            ContinuumProject project = setProjectState( (ContinuumProject) projects.iterator().next() );

            store.commit();

            return project;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while finding projects.", e );
        }
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumStoreException
    {
        return getProject( projectId, true );
    }

    public ContinuumProject getProject( String projectId, boolean detach )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, detach );

            setProjectState( project );

            store.commit();

            return project;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while loading project.", e );
        }
    }

    public ScmResult getScmResultForProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            if ( project.getScmResult() == null )
            {
                store.commit();

                return null;
            }

            PersistenceManager pm = JDOHelper.getPersistenceManager( project );

            ScmResult result = project.getScmResult();

            pm.retrieve( result );

            pm.makeTransient( result );

            pm.retrieveAll( result.getFiles(), false );

            pm.makeTransientAll( result.getFiles() );

            store.commit();

            return result;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while getting build result.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Build
    // ----------------------------------------------------------------------

    public String addBuild( String projectId, ContinuumBuild build )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            build.setProject( project );

            Object id = store.addContinuumBuild( build );

            project.setLatestBuildId( build.getId() );

            project.setBuildNumber( project.getBuildNumber() + 1 );

            store.commit();

            build = store.getContinuumBuildByJdoId( id, true );

            return build.getId();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while creating continuum build for project: '" + projectId + "'.", e );
        }
    }

    public void updateBuild( ContinuumBuild build )
        throws ContinuumStoreException
    {
        try
        {
            store.storeContinuumBuild( build );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while updating continuum build: '" + build.getId() + "'.", e );
        }
    }

    public ContinuumBuild getBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            return store.getContinuumBuild( buildId, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading build id: '" + buildId + "'.", e );
        }
    }

    public ContinuumBuild getLatestBuildForProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject p = store.getContinuumProject( projectId, false );

            if ( p.getLatestBuildId() == null )
            {
                store.commit();

                return null;
            }

            ContinuumBuild b = store.getContinuumBuild( p.getLatestBuildId(), false );

            b = (ContinuumBuild) store.getThreadState().getPersistenceManager().detachCopy( b );

            store.commit();

            return b;

        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while loading last build for project id: '" + projectId + "'.", e );
        }
    }

    public Collection getBuildsForProject( String projectId, int start, int end )
        throws ContinuumStoreException
    {
        try
        {
            String filter = "this.project.id == \"" + projectId + "\"";

            String ordering = "startTime descending";

            return store.getContinuumBuildCollection( true, filter, ordering );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while getting builds for project id: '" + projectId + "'.", e );
        }
    }

    public List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            if ( build.getScmResult() == null )
            {
                return Collections.EMPTY_LIST;
            }

            // TODO: Having to copy the objects feels a /bit/ strange.

            List changedFiles = new ArrayList();

            for ( Iterator it = build.getScmResult().getFiles().iterator(); it.hasNext(); )
            {
                ScmFile scmFile = (ScmFile) it.next();

                ScmFile file = new ScmFile();

                file.setPath( scmFile.getPath() );

                changedFiles.add( file );
            }

            store.commit();

            return changedFiles;
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while getting build result.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ContinuumJPoxStore getStore()
    {
        return store;
    }

    private ContinuumProject setProjectState( ContinuumProject project )
        throws ContinuumStoreException
    {
        ContinuumBuild build = getLatestBuildForProject( project.getId() );

        if ( build == null )
        {
            project.setState( ContinuumProjectState.NEW );
        }
        else
        {
            project.setState( build.getState() );
        }

        return project;
    }

    private void rollback( ContinuumJPoxStore store )
    {
        try
        {
            getLogger().warn( "Rolling back transaction." );

            store.rollback();
        }
        catch ( Exception e )
        {
            getLogger().error( "Error while rolling back tx.", e );
        }
    }

    // ----------------------------------------------------------------------
    // Notifiers
    // ----------------------------------------------------------------------

    public void removeNotifier( Object notifier )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            PersistenceManager pm = store.getThreadState().getPersistenceManager();

            notifier = pm.attachCopy( notifier, false );

            pm.deletePersistent( notifier );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while removing a notifier.", e );
        }
    }

    public void storeNotifier( Object notifier )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            PersistenceManager pm = store.getThreadState().getPersistenceManager();

            notifier = pm.attachCopy( notifier, false );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while removing a notifier.", e );
        }
    }

}
