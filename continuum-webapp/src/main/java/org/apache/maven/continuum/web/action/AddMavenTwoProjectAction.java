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

import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Add a Maven 2 project to Continuum.
 *
 * @author Nick Gonzalez
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "addMavenTwoProject", instantiationStrategy = "per-lookup" )
public class AddMavenTwoProjectAction
    extends AddMavenProjectAction
{

    @Override
    public void prepare()
        throws Exception
    {
        super.prepare();
        setImportType( ImportType.SEPARATE_SCM );
    }

    public enum ImportType
    {
        SEPARATE_SCM( "add.m2.project.importType.projectPerModuleSeparateScm", true, false ),
        SINGLE_SCM( "add.m2.project.importType.projectPerModuleSingleScm", true, true ),
        SINGLE_MULTI_MODULE( "add.m2.project.importType.singleMultiModule", false, false );

        private String textKey;

        private boolean recursiveImport;

        private boolean singleDirCheckout;

        ImportType( String textKey, boolean recursiveImport, boolean singleCheckout )
        {
            this.textKey = textKey;
            this.recursiveImport = recursiveImport;
            this.singleDirCheckout = singleCheckout;
        }

        public String getTextKey()
        {
            return textKey;
        }

        public boolean isSingleCheckout()
        {
            return singleDirCheckout;
        }

        public boolean isRecursiveImport()
        {
            return recursiveImport;
        }
    }

    /**
     * Generates locale-sensitive import options.
     */
    public Map<ImportType, String> getImportOptions()
    {
        Map<ImportType, String> options = new LinkedHashMap<ImportType, String>();
        for ( ImportType type : ImportType.values() )
        {
            options.put( type, getText( type.getTextKey() ) );
        }
        return options;
    }

    // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
    public static final String ERROR_UPLOADING_M2_PROJECT_WITH_MODULES = "add.m2.project.upload.modules.error";

    public static final String ERROR_READING_POM_EXCEPTION_MESSAGE = "Error reading POM";

    public static final String FILE_SCHEME = "file:/";

    private ImportType importType;

    protected ContinuumProjectBuildingResult doExecute( String pomUrl, int selectedProjectGroup, boolean checkProtocol,
                                                        boolean scmUseCache )
        throws ContinuumException
    {
        ContinuumProjectBuildingResult result = null;

        ImportType importType = getImportType();
        boolean recursiveImport = importType.isRecursiveImport();
        boolean checkoutInSingleDirectory = importType.isSingleCheckout();

        // TODO: remove this part once uploading of an m2 project with modules is supported ( CONTINUUM-1098 )
        boolean preventModulesInFileUpload = !checkProtocol && recursiveImport;
        if ( preventModulesInFileUpload )
        {
            List modules = fileToModel( urlToFile( pomUrl ) ).getModules();
            if ( modules != null && modules.size() != 0 )
            {
                result = new ContinuumProjectBuildingResult();
                result.addError( ERROR_UPLOADING_M2_PROJECT_WITH_MODULES );
            }
        }

        if ( result == null )
        {
            result = getContinuum().addMavenTwoProject( pomUrl, selectedProjectGroup, checkProtocol, scmUseCache,
                                                        recursiveImport, this.getBuildDefinitionTemplateId(),
                                                        checkoutInSingleDirectory );
        }

        AuditLog event = new AuditLog( hidePasswordInUrl( pomUrl ), AuditLogConstants.ADD_M2_PROJECT );
        event.setCategory( AuditLogConstants.PROJECT );
        event.setCurrentUser( getPrincipal() );

        if ( result == null || result.hasErrors() )
        {
            event.setAction( AuditLogConstants.ADD_M2_PROJECT_FAILED );
        }

        event.log();
        return result;
    }

    private Model fileToModel( File pomFile )
        throws ContinuumException
    {
        MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
        try
        {
            return m2pomReader.read( ReaderFactory.newXmlReader( pomFile ) );
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

    private File urlToFile( String url )
    {
        if ( !url.startsWith( FILE_SCHEME + "/" ) && url.startsWith( FILE_SCHEME ) )
        {
            //Little hack for linux (CONTINUUM-1169)
            url = StringUtils.replace( url, FILE_SCHEME, FILE_SCHEME + "/" );
        }

        if ( url.startsWith( FILE_SCHEME ) )
        {
            url = url.substring( FILE_SCHEME.length() );
        }

        return new File( url );
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

    public ImportType getImportType()
    {
        return importType;
    }

    public void setImportType( ImportType importType )
    {
        this.importType = importType;
    }
}
