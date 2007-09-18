package org.apache.maven.continuum.execution.maven.m2;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.continuum.configuration.ConfigurationException;
import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.execution.AbstractBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutionResult;
import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorConstants;
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.scm.SuiteResult;
import org.apache.maven.continuum.model.scm.TestCaseFailure;
import org.apache.maven.continuum.model.scm.TestResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenTwoBuildExecutor
    extends AbstractBuildExecutor
    implements ContinuumBuildExecutor
{
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static final String CONFIGURATION_GOALS = "goals";

    public static final String ID = ContinuumBuildExecutorConstants.MAVEN_TWO_BUILD_EXECUTOR;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private MavenBuilderHelper builderHelper;

    /**
     * @plexus.requirement
     */
    private MavenProjectHelper projectHelper;

    /**
     * @plexus.requirement
     */
    private ConfigurationService configurationService;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public MavenTwoBuildExecutor()
    {
        super( ID, true );
    }

    // ----------------------------------------------------------------------
    // ContinuumBuilder Implementation
    // ----------------------------------------------------------------------

    public ContinuumBuildExecutionResult build( Project project, BuildDefinition buildDefinition, File buildOutput )
        throws ContinuumBuildExecutorException
    {
        String executable = getInstallationService().getExecutorConfigurator( InstallationService.MAVEN2_TYPE )
            .getExecutable();

        StringBuffer arguments = new StringBuffer();

        String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

        if ( !StringUtils.isEmpty( buildFile ) && !"pom.xml".equals( buildFile ) )
        {
            arguments.append( "-f " ).append( buildFile ).append( " " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getArguments() ) ).append( " " );

        Properties props = getContinuumSystemProperties( project );
        for ( Enumeration itr = props.propertyNames(); itr.hasMoreElements(); )
        {
            String name = (String) itr.nextElement();
            String value = props.getProperty( name );
            arguments.append( "\"-D" ).append( name ).append( "=" ).append( value ).append( "\" " );
        }

        arguments.append( StringUtils.clean( buildDefinition.getGoals() ) );

        Map<String, String> environments = getEnvironments( buildDefinition );
        String m2Home = environments.get( getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ) );
        if ( StringUtils.isNotEmpty( m2Home ) )
        {
            executable = m2Home + File.separator + "bin" + File.separator + executable;
            setResolveExecutable( false );
        }

        return executeShellCommand( project, executable, arguments.toString(), buildOutput, environments );
    }

    public void updateProjectFromCheckOut( File workingDirectory, Project project, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File f = getPomFile( buildDefinition, workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor." );
        }

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        builderHelper.mapMetadataToProject( result, f, project );

        if ( result.hasErrors() )
        {
            throw new ContinuumBuildExecutorException( "Error while mapping metadata:" + result.getErrorsAsString() );
        }
    }

    private static File getPomFile( BuildDefinition buildDefinition, File workingDirectory )
    {
        File f = null;

        if ( buildDefinition != null )
        {
            String buildFile = StringUtils.clean( buildDefinition.getBuildFile() );

            if ( !StringUtils.isEmpty( buildFile ) )
            {
                f = new File( workingDirectory, buildFile );
            }
        }

        if ( f == null )
        {
            f = new File( workingDirectory, "pom.xml" );
        }

        return f;
    }

    public List getDeployableArtifacts( File workingDirectory, BuildDefinition buildDefinition )
        throws ContinuumBuildExecutorException
    {
        File f = getPomFile( buildDefinition, workingDirectory );

        if ( !f.exists() )
        {
            throw new ContinuumBuildExecutorException( "Could not find Maven project descriptor '" + f + "'." );
        }

        MavenProject project;

        ContinuumProjectBuildingResult result = new ContinuumProjectBuildingResult();

        project = builderHelper.getMavenProject( result, f );

        if ( result.hasErrors() )
        {
            throw new ContinuumBuildExecutorException(
                "Unable to read the Maven project descriptor '" + f + "': " + result.getErrorsAsString() );
        }

        // Maven could help us out a lot more here by knowing how to get the deployment artifacts from a project.
        // TODO: this is currently quite lame

        Artifact artifact = project.getArtifact();

        String projectPackaging = project.getPackaging();

        boolean isPomArtifact = "pom".equals( projectPackaging );

        if ( isPomArtifact )
        {
            artifact.setFile( project.getFile() );
        }
        else
        {
            // Attach pom
            ArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, project.getFile() );

            artifact.addMetadata( metadata );

            String finalName = project.getBuild().getFinalName();

            String filename = finalName + "." + artifact.getArtifactHandler().getExtension();

            String buildDirectory = project.getBuild().getDirectory();

            File artifactFile = new File( buildDirectory, filename );

            artifact.setFile( artifactFile );

            // sources jar
            File sourcesFile = new File( buildDirectory, finalName + "-sources.jar" );

            if ( sourcesFile.exists() )
            {
                projectHelper.attachArtifact( project, "java-source", "sources", sourcesFile );
            }

            // tests sources jar
            File testsSourcesFile = new File( buildDirectory, finalName + "-test-sources.jar" );

            if ( testsSourcesFile.exists() )
            {
                projectHelper.attachArtifact( project, "java-source", "test-sources", testsSourcesFile );
            }

            // javadoc jar
            File javadocFile = new File( buildDirectory, finalName + "-javadoc.jar" );

            if ( javadocFile.exists() )
            {
                projectHelper.attachArtifact( project, "javadoc", "javadoc", javadocFile );
            }

            // client jar
            File clientFile = new File( buildDirectory, finalName + "-client.jar" );

            if ( clientFile.exists() )
            {
                projectHelper.attachArtifact( project, projectPackaging + "-client", "client", clientFile );
            }

            // Tests jar
            File testsFile = new File( buildDirectory, finalName + "-tests.jar" );

            if ( testsFile.exists() )
            {
                projectHelper.attachArtifact( project, "jar", "tests", testsFile );
            }
        }

        List attachedArtifacts = project.getAttachedArtifacts();

        List artifacts = new ArrayList( attachedArtifacts.size() + 1 );

        if ( artifact.getFile().exists() )
        {
            artifacts.add( artifact );
        }

        for ( Iterator iterator = attachedArtifacts.iterator(); iterator.hasNext(); )
        {
            Artifact attachedArtifact = (Artifact) iterator.next();
            artifacts.add( attachedArtifact );
        }

        return artifacts;
    }

    public TestResult getTestResults( Project project, int buildId )
        throws ContinuumBuildExecutorException
    {
        File backupDirectory = null;
        try
        {
            backupDirectory = configurationService.getTestReportsDirectory( buildId, project.getId() );
            if ( !backupDirectory.exists() )
            {
                backupDirectory.mkdirs();
            }
        }
        catch ( ConfigurationException e )
        {
            getLogger().info( "error on surefire backup directory creation skip backup " + e.getMessage(), e );
        }
        return getTestResults( getWorkingDirectory( project ), backupDirectory );
    }

    private TestResult getTestResults( File workingDir, File backupDirectory )
        throws ContinuumBuildExecutorException
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( workingDir );
        scanner.setIncludes(
            new String[]{"**/target/surefire-reports/TEST-*.xml", "**/target/surefire-it-reports/TEST-*.xml"} );
        scanner.scan();

        TestResult testResult = new TestResult();
        int testCount = 0;
        int failureCount = 0;
        int totalTime = 0;
        String[] testResultFiles = scanner.getIncludedFiles();
        for ( int i = 0; i < testResultFiles.length; i++ )
        {
            File xmlFile = new File( workingDir, testResultFiles[i] );
            try
            {
                if ( backupDirectory != null )
                {
                    FileUtils.copyFileToDirectory( xmlFile, backupDirectory );
                }
            }
            catch ( IOException e )
            {
                getLogger().info( "failed to backup unit report file " + xmlFile.getPath() );
            }
            SuiteResult suite = new SuiteResult();
            try
            {
                XmlPullParser parser = new MXParser();
                parser.setInput( new FileReader( xmlFile ) );
                if ( parser.next() != XmlPullParser.START_TAG || !"testsuite".equals( parser.getName() ) )
                {
                    continue;
                }

                suite.setName( parser.getAttributeValue( null, "name" ) );

                int suiteFailureCount = Integer.parseInt( parser.getAttributeValue( null, "errors" ) ) +
                    Integer.parseInt( parser.getAttributeValue( null, "failures" ) );

                String time = parser.getAttributeValue( null, "time" );
                NumberFormat nf = NumberFormat.getInstance( Locale.ENGLISH );
                double dTime = 0;

                try
                {
                    dTime = nf.parse( time ).doubleValue();
                }
                catch ( ParseException nfe )
                {
                    getLogger().warn( "Can't parse time value (" + time + ") in " + xmlFile.getAbsolutePath() );
                }

                long suiteTotalTime = (long) ( 1000 * dTime );

                // TODO: add tests attribute to testsuite element so we only
                // have to parse the rest of the file if there are failures
                int suiteTestCount = 0;
                while ( !( parser.next() == XmlPullParser.END_TAG && "testsuite".equals( parser.getName() ) ) )
                {
                    if ( parser.getEventType() == XmlPullParser.START_TAG && "testcase".equals( parser.getName() ) )
                    {
                        suiteTestCount++;
                        String name = parser.getAttributeValue( null, "name" );
                        do
                        {
                            parser.next();
                        }
                        while ( parser.getEventType() != XmlPullParser.START_TAG &&
                            parser.getEventType() != XmlPullParser.END_TAG );
                        if ( parser.getEventType() == XmlPullParser.START_TAG &&
                            ( "error".equals( parser.getName() ) || "failure".equals( parser.getName() ) ) )
                        {
                            TestCaseFailure failure = new TestCaseFailure();
                            failure.setName( name );
                            if ( parser.next() == XmlPullParser.TEXT )
                            {
                                String exception = parser.getText();
                                //TODO: review this part as it include an hardcoded value of the columnsize
                                if ( exception.length() >= 8192 )
                                {
                                    exception = exception.substring( 0, 8185 ) + "[...]";
                                }
                                failure.setException( exception );
                            }
                            suite.addFailure( failure );
                        }
                    }
                }

                testCount += suiteTestCount;
                failureCount += suiteFailureCount;
                totalTime += suiteTotalTime;

                suite.setTestCount( suiteTestCount );
                suite.setFailureCount( suiteFailureCount );
                suite.setTotalTime( suiteTotalTime );
            }
            catch ( XmlPullParserException xppex )
            {
                throw new ContinuumBuildExecutorException( "Error parsing file: " + xmlFile, xppex );
            }
            catch ( FileNotFoundException fnfex )
            {
                throw new ContinuumBuildExecutorException( "Test file not found", fnfex );
            }
            catch ( IOException ioex )
            {
                throw new ContinuumBuildExecutorException( "Parsing error for file: " + xmlFile, ioex );
            }
            testResult.addSuiteResult( suite );
        }

        testResult.setTestCount( testCount );
        testResult.setFailureCount( failureCount );
        testResult.setTotalTime( totalTime );

        return testResult;
    }

    protected Map<String, String> getEnvironments( BuildDefinition buildDefinition )
    {
        Profile profile = buildDefinition.getProfile();
        if ( profile == null )
        {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> envVars = new HashMap<String, String>();
        String javaHome = getJavaHomeValue( buildDefinition );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }
        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ), builder.getVarValue() );
        }
        envVars.putAll( getEnvironmentVariables( buildDefinition ) );
        return envVars;

    }
}
