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
package org.apache.maven.continuum.web.appareance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.apache.continuum.web.appearance.ContinuumAppearance;
import org.apache.continuum.web.appearance.io.xpp3.ContinuumAppearanceModelsXpp3Reader;
import org.apache.continuum.web.appearance.io.xpp3.ContinuumAppearanceModelsXpp3Writer;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 10 nov. 07
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.web.appareance.AppareanceConfiguration" role-hint="default"
 */
public class DefaultAppareanceConfiguration
    implements AppareanceConfiguration, Initializable
{
    private Logger log = LoggerFactory.getLogger( getClass() );
    
    private String footer;
    
    public static final String APPEARANCE_FILE_NAME = "continuum-appearance.xml";
    
    private ContinuumAppearance continuumAppearance = new ContinuumAppearance(); 
   
    // ------------------------------------------------
    //  Plexus Lifecycle
    // ------------------------------------------------
    
    public void initialize()
        throws InitializationException
    {
        
        File appearanceConfFile = getAppearanceConfigurationFile();

        if ( appearanceConfFile.exists() )
        {
            try
            {
                ContinuumAppearanceModelsXpp3Reader appearanceReader = new ContinuumAppearanceModelsXpp3Reader();
                this.continuumAppearance = appearanceReader.read( ReaderFactory
                    .newXmlReader( appearanceConfFile ) );
                if ( continuumAppearance != null )
                {
                    this.footer = continuumAppearance.getFooter();
                }
            }
            catch ( IOException e )
            {
                log.warn( "skip IOException reading appearance file " + APPEARANCE_FILE_NAME + ", msg "
                    + e.getMessage() );
            }
            catch ( XmlPullParserException e )
            {
                log.warn( "skip XmlPullParserException reading appearance file " + APPEARANCE_FILE_NAME + ", msg "
                    + e.getMessage() );
            }
        }
        if ( StringUtils.isEmpty( this.footer ) )
        {
            // initiate with default footer (save in registry ?)
            this.footer = getDefaultFooter();
        }
    }
    
    /** 
     * @see org.apache.maven.continuum.web.appareance.AppareanceConfiguration#getFooter()
     */
    public String getFooter()
    {
        return this.footer;
    }

    /** 
     * @see org.apache.maven.continuum.web.appareance.AppareanceConfiguration#saveFooter(java.lang.String)
     */
    public void saveFooter( String footerHtmlContent )
        throws IOException
    {
        continuumAppearance.setFooter( footerHtmlContent );
        ContinuumAppearanceModelsXpp3Writer writer = new ContinuumAppearanceModelsXpp3Writer();
        File confFile = getAppearanceConfigurationFile();
        if (!confFile.exists())
        {
            confFile.getParentFile().mkdirs();
        }
        writer.write( new FileWriter( confFile ), continuumAppearance );
        this.footer = footerHtmlContent;
    }


    private String getDefaultFooter()
    {
        int inceptionYear = 2005;
        int currentYear = Calendar.getInstance().get( Calendar.YEAR );
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "<div class=\"xright\">" );
        stringBuilder.append( "Copyright &copy; " );
        stringBuilder.append( String.valueOf( inceptionYear ) + "-" + String.valueOf( currentYear ) );
        stringBuilder.append( "&nbsp;The Apache Software Foundation" );
        stringBuilder.append( "</div> <div class=\"clear\"><hr/></div>" );
        return stringBuilder.toString();
    }    
    
    
    private File getAppearanceConfigurationFile()
    {
        return new File( System.getProperty( "appserver.base" ) + File.separator + "conf" + File.separator
            + APPEARANCE_FILE_NAME );
    }
}
