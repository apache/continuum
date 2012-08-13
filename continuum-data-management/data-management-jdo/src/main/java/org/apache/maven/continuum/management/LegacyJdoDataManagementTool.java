package org.apache.maven.continuum.management;

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

import org.apache.maven.continuum.model.project.v1_0_9.BuildDefinition;
import org.apache.maven.continuum.model.project.v1_0_9.BuildResult;
import org.apache.maven.continuum.model.project.v1_0_9.ContinuumDatabase;
import org.apache.maven.continuum.model.project.v1_0_9.Project;
import org.apache.maven.continuum.model.project.v1_0_9.ProjectDependency;
import org.apache.maven.continuum.model.project.v1_0_9.ProjectDeveloper;
import org.apache.maven.continuum.model.project.v1_0_9.ProjectGroup;
import org.apache.maven.continuum.model.project.v1_0_9.ProjectNotifier;
import org.apache.maven.continuum.model.project.v1_0_9.Schedule;
import org.apache.maven.continuum.model.project.v1_0_9.io.stax.ContinuumStaxReader;
import org.apache.maven.continuum.model.project.v1_0_9.io.stax.ContinuumStaxWriter;
import org.apache.maven.continuum.model.scm.v1_0_9.ChangeFile;
import org.apache.maven.continuum.model.scm.v1_0_9.ChangeSet;
import org.apache.maven.continuum.model.scm.v1_0_9.ScmResult;
import org.apache.maven.continuum.model.scm.v1_0_9.SuiteResult;
import org.apache.maven.continuum.model.scm.v1_0_9.TestCaseFailure;
import org.apache.maven.continuum.model.scm.v1_0_9.TestResult;
import org.apache.maven.continuum.model.system.v1_0_9.SystemConfiguration;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.PlexusJdoUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.xml.stream.XMLStreamException;

/**
 * JDO implementation the database management tool API.
 *
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.management.DataManagementTool" role-hint="legacy-continuum-jdo"
 */
public class LegacyJdoDataManagementTool
    implements DataManagementTool
{
    protected static final String BUILDS_XML = "builds.xml";

    /**
     * @plexus.requirement role="org.codehaus.plexus.jdo.JdoFactory" role-hint="continuum"
     */
    protected DefaultConfigurableJdoFactory factory;

    public void backupDatabase( File backupDirectory )
        throws IOException
    {
        PersistenceManagerFactory pmf = getPersistenceManagerFactory( "jdo109" );

        ContinuumDatabase database = new ContinuumDatabase();
        try
        {
            database.setSystemConfiguration( retrieveSystemConfiguration( pmf ) );
        }
        catch ( ContinuumStoreException e )
        {
            throw new DataManagementException( e );
        }

        Collection<ProjectGroup> projectGroups = retrieveAllProjectGroups( pmf );
        database.setProjectGroups( new ArrayList<ProjectGroup>( projectGroups ) );

        database.setSchedules( retrieveAllSchedules( pmf ) );

        ContinuumStaxWriter writer = new ContinuumStaxWriter();

        backupDirectory.mkdirs();
        OutputStream out = new FileOutputStream( new File( backupDirectory, BUILDS_XML ) );
        Writer fileWriter = new OutputStreamWriter( out, Charset.forName( database.getModelEncoding() ) );

        try
        {
            writer.write( fileWriter, database );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( "Modello failure: unable to write data to StAX writer", e );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    private List retrieveAllSchedules( PersistenceManagerFactory pmf )
    {
        return PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager( pmf ), Schedule.class, "name ascending",
                                                     (String) null );
    }

    private Collection<ProjectGroup> retrieveAllProjectGroups( PersistenceManagerFactory pmf )
    {
        List<String> fetchGroups = Arrays.asList( "project-with-builds", "projectgroup-projects",
                                                  "build-result-with-details", "project-with-checkout-result",
                                                  "project-all-details", "project-build-details" );
        return PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager( pmf ), ProjectGroup.class, "name ascending",
                                                     fetchGroups );
    }

    private SystemConfiguration retrieveSystemConfiguration( PersistenceManagerFactory pmf )
        throws ContinuumStoreException
    {
        SystemConfiguration result;
        List systemConfs = PlexusJdoUtils.getAllObjectsDetached( getPersistenceManager( pmf ),
                                                                 SystemConfiguration.class, null, (String) null );

        if ( systemConfs == null || systemConfs.isEmpty() )
        {
            result = null;
        }
        else if ( systemConfs.size() > 1 )
        {
            throw new ContinuumStoreException(
                "Database is corrupted. There are more than one systemConfiguration object." );
        }
        else
        {
            result = (SystemConfiguration) systemConfs.get( 0 );
        }
        return result;
    }

    @SuppressWarnings( {"OverlyCoupledMethod"} )
    public void eraseDatabase()
    {
        PersistenceManagerFactory pmf = getPersistenceManagerFactory( "jdo109" );
        PersistenceManager persistenceManager = getPersistenceManager( pmf );
        PlexusJdoUtils.removeAll( persistenceManager, ProjectGroup.class );
        PlexusJdoUtils.removeAll( persistenceManager, Project.class );
        PlexusJdoUtils.removeAll( persistenceManager, Schedule.class );
        PlexusJdoUtils.removeAll( persistenceManager, ScmResult.class );
        PlexusJdoUtils.removeAll( persistenceManager, BuildResult.class );
        PlexusJdoUtils.removeAll( persistenceManager, TestResult.class );
        PlexusJdoUtils.removeAll( persistenceManager, SuiteResult.class );
        PlexusJdoUtils.removeAll( persistenceManager, TestCaseFailure.class );
        PlexusJdoUtils.removeAll( persistenceManager, SystemConfiguration.class );
        PlexusJdoUtils.removeAll( persistenceManager, ProjectNotifier.class );
        PlexusJdoUtils.removeAll( persistenceManager, ProjectDeveloper.class );
        PlexusJdoUtils.removeAll( persistenceManager, ProjectDependency.class );
        PlexusJdoUtils.removeAll( persistenceManager, ChangeSet.class );
        PlexusJdoUtils.removeAll( persistenceManager, ChangeFile.class );
        PlexusJdoUtils.removeAll( persistenceManager, BuildDefinition.class );
    }

    private PersistenceManager getPersistenceManager( PersistenceManagerFactory pmf )
    {
        PersistenceManager pm = pmf.getPersistenceManager();

        pm.getFetchPlan().setMaxFetchDepth( -1 );
        pm.getFetchPlan().setDetachmentOptions( FetchPlan.DETACH_LOAD_FIELDS );

        return pm;
    }

    public void restoreDatabase( File backupDirectory, boolean strict )
        throws IOException
    {
        ContinuumStaxReader reader = new ContinuumStaxReader();

        FileReader fileReader = new FileReader( new File( backupDirectory, BUILDS_XML ) );

        ContinuumDatabase database;
        try
        {
            database = reader.read( fileReader, strict );
        }
        catch ( XMLStreamException e )
        {
            throw new DataManagementException( e );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        PersistenceManagerFactory pmf = getPersistenceManagerFactory( "jdorepl109" );

        PlexusJdoUtils.addObject( pmf.getPersistenceManager(), database.getSystemConfiguration() );

        Map<Integer, Schedule> schedules = new HashMap<Integer, Schedule>();
        for ( Iterator i = database.getSchedules().iterator(); i.hasNext(); )
        {
            Schedule schedule = (Schedule) i.next();

            schedule = (Schedule) PlexusJdoUtils.addObject( pmf.getPersistenceManager(), schedule );
            schedules.put( Integer.valueOf( schedule.getId() ), schedule );
        }

        for ( Iterator i = database.getProjectGroups().iterator(); i.hasNext(); )
        {
            ProjectGroup projectGroup = (ProjectGroup) i.next();

            // first, we must map up any schedules, etc.
            processBuildDefinitions( projectGroup.getBuildDefinitions(), schedules );

            for ( Iterator j = projectGroup.getProjects().iterator(); j.hasNext(); )
            {
                Project project = (Project) j.next();

                processBuildDefinitions( project.getBuildDefinitions(), schedules );
            }

            PlexusJdoUtils.addObject( pmf.getPersistenceManager(), projectGroup );
        }
        pmf.close();
    }

    private PersistenceManagerFactory getPersistenceManagerFactory( String ext )
    {
        // Take control of the JDO instead of using the store, and configure a new persistence factory
        // that won't generate new object IDs.
        Properties properties = new Properties();
        //noinspection UseOfPropertiesAsHashtable
        properties.putAll( factory.getProperties() );
        properties.setProperty( "org.jpox.metadata.jdoFileExtension", ext );
        return JDOHelper.getPersistenceManagerFactory( properties );
    }

    private static void processBuildDefinitions( List buildDefinitions, Map<Integer, Schedule> schedules )
    {
        for ( Iterator i = buildDefinitions.iterator(); i.hasNext(); )
        {
            BuildDefinition def = (BuildDefinition) i.next();

            if ( def.getSchedule() != null )
            {
                def.setSchedule( schedules.get( Integer.valueOf( def.getSchedule().getId() ) ) );
            }
        }
    }
}
