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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.project.ContinuumBuild;
import org.apache.maven.continuum.project.ContinuumJPoxStore;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.scm.CheckOutScmResult;
import org.apache.maven.continuum.scm.ScmFile;
import org.apache.maven.continuum.scm.UpdateScmResult;

import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo All validation should happen at the project application but things like the state gaurd
 * should not be present in the store. We have to move toward being able to generate more of
 * these methods automatically so any app customizatoins would prevent this from working.
 *
 * @todo remove the state guard code
 * @todo we could create the necessary ContinuumProject else where and use the addContinuumProject that is present in the store class generated by modello.
 * @todo how to build in some basic query methods so we can describe what we want and have it be generated
 * @todo cascading delete
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
            project.setState( ContinuumProjectState.CHECKING_OUT );

            Object id = store.addContinuumProject( project );

            project = store.getContinuumProjectByJdoId( id, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while adding a project.", e );
        }

        return project.getId();
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

//            for ( Iterator it = builds.iterator(); it.hasNext(); )
//            {
//                ContinuumBuild build = (ContinuumBuild) it.next();
//
//                ContinuumBuildResult result = build.getBuildResult();
//
//                if ( result == null )
//                {
//                    continue;
//                }
//
//                result.setBuild( null );
//
//                pm.deletePersistent( result );
//            }

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

    public void setWorkingDirectory( String projectId, String workingDirectory )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setWorkingDirectory( workingDirectory );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting the working directory.", e );
        }
    }

    public void updateProject( ContinuumProject project )
        throws ContinuumStoreException
    {
        try
        {
            project.setCommandLineArguments( StringUtils.clean( project.getCommandLineArguments() ) );

            store.storeContinuumProject( project );

//            pm.attachCopyAll( project.getDevelopers(), true );
//
//            Collection notifiers = project.getNotifiers();

//            System.err.println( "updating " + notifiers.size() );
//
//            for ( Iterator it = notifiers.iterator(); it.hasNext(); )
//            {
//                ContinuumNotifier notifier = (ContinuumNotifier) it.next();
//
//                System.err.println( "type: " + notifier.getType() );
//                System.err.println( "config:" + notifier.getConfiguration() );
//                System.err.println( "id: " + JDOHelper.getObjectId( notifier ) );
//
//                pm.attachCopy( notifier, true );
//            }

//            pm.attachCopyAll( notifiers, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while setting the working directory.", e );
        }
    }

//    public void updateProject( String projectId,
//                               String name,
//                               String scmUrl,
//                               List notifiers,
//                               String version,
//                               String commandLineArguments )
//        throws ContinuumStoreException
//    {
//        try
//        {
//            store.begin();
//
//            ContinuumProject project = store.getContinuumProject( projectId, false );
//
//            project.setName( name );
//            project.setScmUrl( scmUrl );
//            project.setNotifiers( notifiers );
//            project.setVersion( version );
//            project.setCommandLineArguments( commandLineArguments );
//
//            store.commit();
//        }
//        catch ( Exception e )
//        {
//            rollback( store );
//
//            throw new ContinuumStoreException( "Error while updating project.", e );
//        }
//    }

    public Collection getAllProjects()
        throws ContinuumStoreException
    {
        try
        {
            return store.getContinuumProjectCollection( true, null, "name ascending" );
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

            Collection projects = store.getContinuumProjectCollection( true, filter, ordering );

            if ( projects.size() == 0 )
            {
                return null;
            }

            return (ContinuumProject) projects.iterator().next();
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading project set", e );
        }
    }

    public ContinuumProject getProjectByScmUrl( String scmUrl )
        throws ContinuumStoreException
    {
        try
        {
            String filter = "this.scmUrl == \"" + scmUrl + "\"";

            String ordering = "";

            Collection projects = store.getContinuumProjectCollection( true, filter, ordering );

            if ( projects.size() == 0 )
            {
                return null;
            }

            return (ContinuumProject) projects.iterator().next();
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while finding projects.", e );
        }
    }

    public ContinuumProject getProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            return store.getContinuumProject( projectId, true );
        }
        catch ( Exception e )
        {
            throw new ContinuumStoreException( "Error while loading project.", e );
        }
    }

    public CheckOutScmResult getCheckOutScmResultForProject( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            if ( project.getCheckOutScmResult() == null )
            {
                store.commit();

                return null;
            }

            PersistenceManager pm = JDOHelper.getPersistenceManager( project );

            CheckOutScmResult result = project.getCheckOutScmResult();

            pm.retrieve( result );

            pm.makeTransient( result );

            pm.retrieveAll( result.getCheckedOutFiles(), false );

            pm.makeTransientAll( result.getCheckedOutFiles() );

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

    public String createBuild( String projectId, boolean forced )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            ContinuumBuild build = new ContinuumBuild();

            build.setStartTime( System.currentTimeMillis() );

            build.setState( ContinuumProjectState.BUILDING );

            build.setProject( project );

            build.setForced( forced );

            Object id = store.addContinuumBuild( build );

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

    public void setBuildResult( String buildId,
                                int state,
                                ContinuumBuildExecutionResult result,
                                UpdateScmResult scmResult,
                                Throwable error )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            // TODO: as build.getProject() should be removed replace this with
            //       a search for the project
            ContinuumProject project = build.getProject();

            project.setState( state );

            build.setState( state );

            build.setEndTime( new Date().getTime() );

            build.setError( throwableToString( error ) );

            build.setUpdateScmResult( scmResult );

            // ----------------------------------------------------------------------
            // Copy over the build result
            // ----------------------------------------------------------------------

            build.setSuccess( result.isSuccess() );

            build.setStandardOutput( result.getStandardOutput() );

            build.setStandardError( result.getStandardError() );

            build.setExitCode( result.getExitCode() );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting build result for build: '" + buildId + "'.", e );
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
        // TODO: Find a better way to query for the latest build.

        try
        {
            store.begin();

            PersistenceManager pm = store.getThreadState().getPersistenceManager();

            Query q = pm.newQuery( ContinuumBuild.class );
            q.declareParameters( "String projectId" );
            q.setFilter( "this.project.id == projectId" );
            q.setOrdering( "id asc" );
            Collection builds = (Collection) q.execute( projectId );

            if ( builds.size() == 0 )
            {
                return null;
            }

            ContinuumBuild build = (ContinuumBuild) builds.iterator().next();

            build = (ContinuumBuild) store.getThreadState().getPersistenceManager().detachCopy( build );

            store.commit();

            return build;
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

//    public ContinuumBuildResult getBuildResultForBuild( String buildId )
//        throws ContinuumStoreException
//    {
//        try
//        {
//            store.begin();
//
//            ContinuumBuild build = store.getContinuumBuild( buildId, false );
//
//            if ( build.getBuildResult() == null )
//            {
//                store.commit();
//
//                return null;
//            }
//
//            Object id = JDOHelper.getObjectId( build.getBuildResult() );
//
//            store.commit();
//
//            ContinuumBuildResult result = store.getContinuumBuildResultByJdoId( id, true );
//
//            return result;
//        }
//        catch ( Exception e )
//        {
//            rollback( store );
//
//            throw new ContinuumStoreException( "Error while getting build result.", e );
//        }
//    }

    public List getChangedFilesForBuild( String buildId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

//            if ( build.getBuildResult() == null )
//            {
//                store.commit();
//
//                return null;
//            }

            // TODO: Having to copy the objects feels a /bit/ strange.

            List changedFiles = new ArrayList();

            for ( Iterator it = build.getUpdateScmResult().getUpdatedFiles().iterator(); it.hasNext(); )
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

    public void setCheckoutDone( String projectId,
                                 CheckOutScmResult scmResult,
                                 String errorMessage,
                                 Throwable exception )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            int state;

            if ( scmResult != null &&
                 scmResult.isSuccess() &&
                 StringUtils.isEmpty( errorMessage ) &&
                 exception == null )
            {
                state = ContinuumProjectState.NEW;
            }
            else
            {
                state = ContinuumProjectState.ERROR;
            }

            project.setState( state );

            project.setCheckOutScmResult( scmResult );

            project.setCheckOutErrorMessage( errorMessage );

            project.setCheckOutErrorException( throwableToString( exception ) );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting check out scm result.", e );
        }
    }

    public void setIsUpdating( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setState( ContinuumProjectState.UPDATING );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting build state.", e );
        }
    }

    public void setUpdateDone( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            project.setState( ContinuumProjectState.BUILDING );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting update scm result.", e );
        }
    }

    public void setBuildNotExecuted( String projectId )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            int state;

            ContinuumBuild latestBuild = getLatestBuildForProject( projectId );

            if ( latestBuild == null )
            {
                state = ContinuumProjectState.NEW;
            }
            else
            {
                state = latestBuild.getState();
            }

            project.setState( state );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting update scm result.", e );
        }
    }

    public String buildingProject( String projectId,
                                   boolean forced,
                                   UpdateScmResult scmResult )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            ContinuumProject project = store.getContinuumProject( projectId, false );

            ContinuumBuild build = new ContinuumBuild();

            build.setStartTime( new Date().getTime() );

            build.setState( ContinuumProjectState.BUILDING );

            project.setState( ContinuumProjectState.BUILDING );

            build.setProject( project );

            build.setForced( forced );

            build.setUpdateScmResult( scmResult );

            Object id = store.addContinuumBuild( build );

            build = store.getContinuumBuildByJdoId( id, false );

            store.commit();

            return build.getId();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while creating continuum build for project: '" + projectId + "'.", e );
        }

    }

    public void setBuildComplete( String buildId,
                                  UpdateScmResult scmResult,
                                  ContinuumBuildExecutionResult result )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            int state = result.isSuccess() ?
                        ContinuumProjectState.OK : ContinuumProjectState.FAILED;

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            build.setState( state );

            ContinuumProject project = build.getProject();

            project.setState( state );

            // ----------------------------------------------------------------------
            // Copy over the build result
            // ----------------------------------------------------------------------

            build.setSuccess( result.isSuccess() );

            build.setStandardOutput( result.getStandardOutput() );

            build.setStandardError( result.getStandardError() );

            build.setExitCode( result.getExitCode() );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting build result for build: '" + buildId + "'.", e );
        }
    }

    public void setBuildError( String buildId,
                               UpdateScmResult scmResult,
                               Throwable throwable )
        throws ContinuumStoreException
    {
        try
        {
            store.begin();

            int state = ContinuumProjectState.ERROR;

            ContinuumBuild build = store.getContinuumBuild( buildId, false );

            build.setState( state );

            ContinuumProject project = build.getProject();

            project.setState( state );

            build.setError( throwableToString( throwable ) );

            store.commit();
        }
        catch ( Exception e )
        {
            rollback( store );

            throw new ContinuumStoreException( "Error while setting build result for build: '" + buildId + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ContinuumJPoxStore getStore()
    {
        return store;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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
}
