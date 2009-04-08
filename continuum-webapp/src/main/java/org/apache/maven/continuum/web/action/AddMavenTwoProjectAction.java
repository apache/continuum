package org.apache.maven.continuum.web.action;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.builddefinition.BuildDefinitionServiceException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Add a Maven 2 project to Continuum.
 *
 * @author Nick Gonzalez
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="addMavenTwoProject"
 */
public class AddMavenTwoProjectAction
    extends AddMavenProjectAction
{
    // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
    public static final String ERROR_UPLOADING_M2_PROJECT_WITH_MODULES = "add.m2.project.upload.modules.error";

    public static final String ERROR_READING_POM_EXCEPTION_MESSAGE = "Error reading POM";

    public static final String FILE_SCHEME = "file:/";

    private boolean nonRecursiveProject;

    protected ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol,
                                                        boolean scmUseCache )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = null;
        
        String groupId = "";
        
        String artifactId = "";
        
        String resource = "";

        // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
        if ( ( checkProtocol == false ) || ( ( checkProtocol == true ) && ( pomUrl.startsWith( FILE_SCHEME ) ) ) )
        {
            MavenXpp3Reader m2pomReader = new MavenXpp3Reader();

            try
            {
                String filePath = pomUrl;

                if ( !filePath.startsWith( FILE_SCHEME + "/" ) && filePath.startsWith( FILE_SCHEME ) )
                {
                    //Little hack for linux (CONTINUUM-1169)
                    filePath = StringUtils.replace( filePath, FILE_SCHEME, FILE_SCHEME + "/" );
                }

                if ( filePath.startsWith( FILE_SCHEME ) )
                {
                    filePath = filePath.substring( FILE_SCHEME.length() );
                }

                Model model = m2pomReader.read( new FileReader( filePath ) );
                
                groupId = model.getGroupId();
                artifactId = model.getArtifactId();
                resource =  groupId + ":" + artifactId;

                List modules = model.getModules();

                if ( ( checkProtocol == false ) && ( modules != null && modules.size() != 0 ) )
                {
                    result = new ContinuumProjectBuildingResult();
                    result.addError( ERROR_UPLOADING_M2_PROJECT_WITH_MODULES );
                }
            }
            catch ( FileNotFoundException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
            }
            catch ( IOException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
            }
            catch ( XmlPullParserException e )
            {
                throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
            }
        }
        else
        {
            if ( ( pomUrl.startsWith( "http" ) ) && ( pomUrl.endsWith( "pom.xml" ) ) )
            {
                try
                {
                    URL url = new URL( pomUrl );
                    BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
                    StringBuilder content = new StringBuilder();
                    String line = in.readLine();
                    
                    while ( line != null )
                    {
                        content.append( line );
                        line = in.readLine();
                    }
                    in.close();
                   
                    if ( content.length() > 0 )
                    {
                        groupId = getSubString( content.toString(), "<groupId>", "</groupId>" );
                        artifactId = getSubString( content.toString(), "<artifactId>", "</artifactId>" );
                        resource = groupId + ":" + artifactId;
                    }
                }
                catch ( MalformedURLException e )
                {
                    addActionError( ERROR_READING_POM_EXCEPTION_MESSAGE );
                }
                catch ( IOException e )
                {
                    throw new ContinuumException( ERROR_READING_POM_EXCEPTION_MESSAGE, e );
                }
            }
        }

        if ( result == null )
        {
            result = getContinuum().addMavenTwoProject( pomUrl, selectedProjectGroup, checkProtocol, scmUseCache,
                                                        !this.isNonRecursiveProject(), this.getBuildDefinitionTemplateId() );
        }
        
        triggerAuditEvent( getPrincipal(), AuditLogConstants.PROJECT, resource, AuditLogConstants.ADD_M2_PROJECT );

        return result;
    }
    
    public String doDefault()
        throws BuildDefinitionServiceException
    {
        return super.doDefault();
    }

    /**
     * @deprecated Use {@link #getPomFile()} instead
     */
    public File getM2PomFile()
    {
        return getPomFile();
    }

    /**
     * @deprecated Use {@link #setPomFile(File)} instead
     */
    public void setM2PomFile( File pomFile )
    {
        setPomFile( pomFile );
    }

    /**
     * @deprecated Use {@link #getPomUrl()} instead
     */
    public String getM2PomUrl()
    {
        return getPomUrl();
    }

    /**
     * @deprecated Use {@link #setPomUrl(String)} instead
     */
    public void setM2PomUrl( String pomUrl )
    {
        setPomUrl( pomUrl );
    }

    public boolean isNonRecursiveProject()
    {
        return nonRecursiveProject;
    }

    public void setNonRecursiveProject( boolean nonRecursiveProject )
    {
        this.nonRecursiveProject = nonRecursiveProject;
    }
    
    private String getSubString( String content, String tagStart, String tagEnd )
    {
        String subString = "";
        
        int start = content.indexOf( tagStart ) + tagStart.length();
        int end = content.indexOf( tagEnd );
        subString = content.substring( start, end );
        
        return subString;
    }

}
