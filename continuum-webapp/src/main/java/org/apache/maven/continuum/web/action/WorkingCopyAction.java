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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.continuum.builder.distributed.manager.DistributedBuildManager;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.apache.maven.continuum.web.util.WorkingCopyContentGenerator;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.util.UrlHelper;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="workingCopy"
 */
public class WorkingCopyAction
    extends ContinuumActionSupport
{
    /**
     * @plexus.requirement
     */
    private WorkingCopyContentGenerator generator;

    /**
     * @plexus.requirement
     */
    private DistributedBuildManager distributedBuildManager;

    private Project project;

    private int projectId;

    private String userDirectory;

    private String currentFile;

    private String currentFileContent;

    private String output;

    private String projectName;

    private File downloadFile;

    private String mimeType = "application/octet-stream";

    private static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

    private String projectGroupName = "";

    private String downloadFileName = "";

    private String downloadFileLength = "";

    private InputStream downloadFileInputStream;

    public String execute()
        throws ContinuumException
    {
        try
        {
            checkViewProjectGroupAuthorization( getProjectGroupName() );
        }
        catch ( AuthorizationRequiredException e )
        {
            return REQUIRES_AUTHORIZATION;
        }

        if ( "release.properties".equals( currentFile ) )
        {
            throw new ContinuumException( "release.properties is not accessible." );
        }

        project = getContinuum().getProject( projectId );

        projectName = project.getName();

        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put( "projectId", projectId );

        params.put( "projectName", projectName );

        String baseUrl = UrlHelper.buildUrl( "/workingCopy.action", ServletActionContext.getRequest(),
                                             ServletActionContext.getResponse(), params );

        String imagesBaseUrl =
            UrlHelper.buildUrl( "/images/", ServletActionContext.getRequest(), ServletActionContext.getResponse(),
                                params );

        imagesBaseUrl = imagesBaseUrl.substring( 0, imagesBaseUrl.indexOf( "/images/" ) + "/images/".length() );

        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            output =
                distributedBuildManager.generateWorkingCopyContent( projectId, userDirectory, baseUrl, imagesBaseUrl );

            if ( currentFile != null && !currentFile.equals( "" ) )
            {
                Map<String, Object> projectFile = distributedBuildManager.getFileContent( projectId, userDirectory, currentFile );

                if ( projectFile == null )
                {
                    currentFileContent = "";
                }
                else
                {
                    downloadFileInputStream =  new ByteArrayInputStream( (byte[]) projectFile.get( "downloadFile" ) );
                    downloadFileLength = (String) projectFile.get( "downloadFileLength" );
                    downloadFileName = (String) projectFile.get( "downloadFileName" );
                    currentFileContent = (String) projectFile.get( "fileContent" );
                    mimeType = (String) projectFile.get( "mimeType" );

                    if ( (Boolean) projectFile.get( "isStream" ) ) 
                    {
                        return "stream";
                    }
                }
            }
            else
            {
                currentFileContent = "";
            }
        }
        else
        {
            List<File> files = getContinuum().getFiles( projectId, userDirectory );

            output =
                generator.generate( files, baseUrl, imagesBaseUrl, getContinuum().getWorkingDirectory( projectId ) );

            if ( currentFile != null && !currentFile.equals( "" ) )
            {
                String dir;

                //TODO: maybe create a plexus component for this so that additional mimetypes can be easily added
                MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
                mimeTypesMap.addMimeTypes( "application/java-archive jar war ear" );
                mimeTypesMap.addMimeTypes( "application/java-class class" );
                mimeTypesMap.addMimeTypes( "image/png png" );

                if ( FILE_SEPARATOR.equals( userDirectory ) )
                {
                    dir = userDirectory;
                }
                else
                {
                    dir = FILE_SEPARATOR + userDirectory + FILE_SEPARATOR;
                }

                downloadFile = new File( getContinuum().getWorkingDirectory( projectId ) + dir + currentFile );
                mimeType = mimeTypesMap.getContentType( downloadFile );
                downloadFileLength = Long.toString( downloadFile.length() );
                downloadFileName = downloadFile.getName();

                if ( ( mimeType.indexOf( "image" ) >= 0 ) || ( mimeType.indexOf( "java-archive" ) >= 0 ) ||
                    ( mimeType.indexOf( "java-class" ) >= 0 ) || ( downloadFile.length() > 100000 ) )
                {
                    return "stream";
                }

                currentFileContent = getContinuum().getFileContent( projectId, userDirectory, currentFile );
            }
            else
            {
                currentFileContent = "";
            }
        }

        return SUCCESS;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getUserDirectory()
    {
        return userDirectory;
    }

    public void setUserDirectory( String userDirectory )
    {
        this.userDirectory = userDirectory;
    }

    public void setFile( String currentFile )
    {
        this.currentFile = currentFile;
    }

    public String getFile()
    {
        return currentFile;
    }

    public String getOutput()
    {
        return output;
    }

    public String getFileContent()
    {
        return currentFileContent;
    }


    public InputStream getInputStream()
        throws ContinuumException
    {
        if ( getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            return downloadFileInputStream;
        }
        else
        {
            FileInputStream fis;
            try
            {
                fis = new FileInputStream( downloadFile );
            }
            catch ( FileNotFoundException fne )
            {
                throw new ContinuumException( "Error accessing file.", fne );
            }
    
            return fis;
        }
    }

    public String getFileLength()
    {
        return downloadFileLength;
    }

    public String getDownloadFilename()
    {
        return downloadFileName;
    }

    public String getMimeType()
    {
        return this.mimeType;
    }

    public Project getProject()
    {
        return project;
    }

    public String getProjectGroupName()
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( projectGroupName ) )
        {
            projectGroupName = getContinuum().getProjectGroupByProjectId( projectId ).getName();
        }

        return projectGroupName;
    }
}
