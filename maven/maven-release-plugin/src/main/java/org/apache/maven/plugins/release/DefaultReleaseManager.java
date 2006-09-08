package org.apache.maven.plugins.release;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import org.apache.maven.plugins.release.config.ReleaseDescriptor;
import org.apache.maven.plugins.release.config.ReleaseDescriptorStore;
import org.apache.maven.plugins.release.config.ReleaseDescriptorStoreException;
import org.apache.maven.plugins.release.exec.MavenExecutor;
import org.apache.maven.plugins.release.exec.MavenExecutorException;
import org.apache.maven.plugins.release.phase.ReleasePhase;
import org.apache.maven.plugins.release.scm.ReleaseScmCommandException;
import org.apache.maven.plugins.release.scm.ReleaseScmRepositoryException;
import org.apache.maven.plugins.release.scm.ScmRepositoryConfigurator;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Implementation of the release manager.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DefaultReleaseManager
    extends AbstractLogEnabled
    implements ReleaseManager
{
    /**
     * The phases of release to run, and in what order.
     */
    private List preparePhases;

    //todo implement
    private List performPhases;

    /**
     * The available phases.
     */
    private Map releasePhases;

    /**
     * The configuration storage.
     */
    private ReleaseDescriptorStore configStore;

    /**
     * Tool for configuring SCM repositories from release configuration.
     */
    private ScmRepositoryConfigurator scmRepositoryConfigurator;

    /**
     * Tool to execute Maven.
     */
    private MavenExecutor mavenExecutor;

    //todo tests
    private List listenerList = new ArrayList();

    private final int phaseSkip = 0, phaseStart = 1, phaseEnd = 2, goalStart = 11, goalEnd = 12, error = 99;

    public void prepare( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        prepare( releaseDescriptor, settings, reactorProjects, true, false );
    }

    public void prepare( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects, boolean resume,
                         boolean dryRun )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        reportToListeners( "prepare", goalStart );

        ReleaseDescriptor config;
        if ( resume )
        {
            try
            {
                config = configStore.read( releaseDescriptor );
            }
            catch ( ReleaseDescriptorStoreException e )
            {
                reportToListeners( e.getMessage(), error );

                throw new ReleaseExecutionException( "Error reading stored configuration: " + e.getMessage(), e );
            }
        }
        else
        {
            config = releaseDescriptor;
        }

        // Later, it would be a good idea to introduce a proper workflow tool so that the release can be made up of a
        // more flexible set of steps.

        String completedPhase = config.getCompletedPhase();
        int index = preparePhases.indexOf( completedPhase );

        for ( int idx = 0; idx < preparePhases.size(); idx++ )
        {
            reportToListeners( preparePhases.get( idx ).toString(), phaseSkip );
        }

        if ( index == preparePhases.size() - 1 )
        {
            getLogger().info(
                "Release preparation already completed. You can now continue with release:perform, or start again using the -Dresume=false flag" );
        }
        else if ( index >= 0 )
        {
            getLogger().info( "Resuming release from phase '" + preparePhases.get( index + 1 ) + "'" );
        }

        // start from next phase
        for ( int i = index + 1; i < preparePhases.size(); i++ )
        {
            String name = (String) preparePhases.get( i );

            ReleasePhase phase = (ReleasePhase) releasePhases.get( name );

            if ( phase == null )
            {
                String message = "Unable to find phase '" + name + "' to execute";

                reportToListeners( message, error );

                throw new ReleaseExecutionException( message );
            }

            reportToListeners( name, phaseStart );

            if ( dryRun )
            {
                phase.simulate( config, settings, reactorProjects );
            }
            else
            {
                phase.execute( config, settings, reactorProjects );
            }

            config.setCompletedPhase( name );
            try
            {
                configStore.write( config );
            }
            catch ( ReleaseDescriptorStoreException e )
            {
                reportToListeners( e.getMessage(), error );

                // TODO: rollback?
                throw new ReleaseExecutionException( "Error writing release properties after completing phase", e );
            }

            reportToListeners( name, phaseEnd );

            reportToListeners( "prepare", goalEnd );
        }
    }

    public void perform( ReleaseDescriptor releaseDescriptor, Settings settings, List reactorProjects,
                         File checkoutDirectory, String goals, boolean useReleaseProfile )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        reportToListeners( "perform", goalStart );

        getLogger().info( "Checking out the project to perform the release ..." );

        reportToListeners( "verify-release-configuration", phaseStart );

        ReleaseDescriptor config;
        try
        {
            config = configStore.read( releaseDescriptor );
        }
        catch ( ReleaseDescriptorStoreException e )
        {
            reportToListeners( e.getMessage(), error );

            throw new ReleaseExecutionException( "Error reading stored configuration: " + e.getMessage(), e );
        }

        reportToListeners( "verify-release-configuration", phaseEnd );
        reportToListeners( "verify-completed-prepare-phases", phaseStart );

        // if we stopped mid-way through preparation - don't perform
        if ( config.getCompletedPhase() != null && !"end-release".equals( config.getCompletedPhase() ) )
        {
            String message = "Cannot perform release - the preparation step was stopped mid-way. Please re-run " +
                "release:prepare to continue, or perform the release from an SCM tag.";

            reportToListeners( message, error );

            throw new ReleaseFailureException( message );
        }

        if ( config.getScmSourceUrl() == null )
        {
            String message = "No SCM URL was provided to perform the release from";

            reportToListeners( message, error );

            throw new ReleaseFailureException( message );
        }

        reportToListeners( "verify-completed-prepare-phases", phaseEnd );
        reportToListeners( "configure-repositories", phaseStart );

        ScmRepository repository;
        ScmProvider provider;
        try
        {
            repository = scmRepositoryConfigurator.getConfiguredRepository( config, settings );

            provider = scmRepositoryConfigurator.getRepositoryProvider( repository );
        }
        catch ( ScmRepositoryException e )
        {
            reportToListeners( e.getMessage(), error );

            throw new ReleaseScmRepositoryException( e.getMessage(), e.getValidationMessages() );
        }
        catch ( NoSuchScmProviderException e )
        {
            reportToListeners( e.getMessage(), error );

            throw new ReleaseExecutionException( "Unable to configure SCM repository: " + e.getMessage(), e );
        }

        // TODO: sanity check that it is not . or .. or lower

        reportToListeners( "configure-repositories", phaseEnd );
        reportToListeners( "checkout-project-from-scm", phaseStart );

        if ( checkoutDirectory.exists() )
        {
            try
            {
                FileUtils.deleteDirectory( checkoutDirectory );
            }
            catch ( IOException e )
            {
                reportToListeners( e.getMessage(), error );

                throw new ReleaseExecutionException( "Unable to remove old checkout directory: " + e.getMessage(), e );
            }
        }
        checkoutDirectory.mkdirs();

        CheckOutScmResult result;
        try
        {
            result = provider.checkOut( repository, new ScmFileSet( checkoutDirectory ), config.getScmReleaseLabel() );
        }
        catch ( ScmException e )
        {
            reportToListeners( e.getMessage(), error );

            throw new ReleaseExecutionException( "An error is occurred in the checkout process: " + e.getMessage(), e );
        }
        if ( !result.isSuccess() )
        {
            reportToListeners( result.getProviderMessage(), error );

            throw new ReleaseScmCommandException( "Unable to checkout from SCM", result );
        }

        reportToListeners( "checkout-project-from-scm", phaseEnd );
        reportToListeners( "build-project", phaseStart );

        String additionalArguments = config.getAdditionalArguments();

        if ( useReleaseProfile )
        {
            if ( !StringUtils.isEmpty( additionalArguments ) )
            {
                additionalArguments = additionalArguments + " -DperformRelease=true";
            }
            else
            {
                additionalArguments = "-DperformRelease=true";
            }
        }

        try
        {
            mavenExecutor.executeGoals( checkoutDirectory, goals, config.isInteractive(), additionalArguments,
                                        config.getPomFileName() );
        }
        catch ( MavenExecutorException e )
        {
            reportToListeners( e.getMessage(), error );

            throw new ReleaseExecutionException( "Error executing Maven: " + e.getMessage(), e );
        }

        reportToListeners( "build-project", phaseEnd );
        reportToListeners( "cleanup", phaseStart );

        clean( config, reactorProjects );

        reportToListeners( "cleanup", phaseEnd );
        reportToListeners( "perform", goalEnd );
    }

    public void clean( ReleaseDescriptor releaseDescriptor, List reactorProjects )
    {
        getLogger().info( "Cleaning up after release..." );

        configStore.delete( releaseDescriptor );

        for ( Iterator i = preparePhases.iterator(); i.hasNext(); )
        {
            String name = (String) i.next();

            ReleasePhase phase = (ReleasePhase) releasePhases.get( name );

            phase.clean( reactorProjects );
        }
    }

    void setConfigStore( ReleaseDescriptorStore configStore )
    {
        this.configStore = configStore;
    }

    void setMavenExecutor( MavenExecutor mavenExecutor )
    {
        this.mavenExecutor = mavenExecutor;
    }

    void reportToListeners( String name, int state )
    {
        for( Iterator listeners = listenerList.iterator(); listeners.hasNext(); )
        {
            ReleaseManagerListener listener = (ReleaseManagerListener) listeners.next();

            switch( state )
            {
                case goalStart:
                    listener.goalStart( name, getGoalPhases( name ) );
                    break;
                case goalEnd:
                    listener.goalEnd();
                    break;
                case phaseSkip:
                    listener.phaseSkip( name );
                    break;
                case phaseStart:
                    listener.phaseStart( name );
                    break;
                case phaseEnd:
                    listener.phaseEnd();
                    break;
                default:
                    listener.error( name );
            }
        }
    }

    private List getGoalPhases( String name )
    {
        List phases = new ArrayList();

        if ( "prepare".equals( name ) )
        {
            phases.addAll( this.preparePhases );
        }
        else if ( "perform".equals( name ) )
        {
            phases.addAll( this.performPhases );
        }

        return phases;
    }

    public void addListener( ReleaseManagerListener listener )
    {
        listenerList.add( listener );
    }

    public void removeListener( ReleaseManagerListener listener )
    {
        if ( listenerList.contains( listener ) )
        {
            listenerList.remove( listener );
        }
    }

    List getListeners()
    {
        return listenerList;
    }
}
