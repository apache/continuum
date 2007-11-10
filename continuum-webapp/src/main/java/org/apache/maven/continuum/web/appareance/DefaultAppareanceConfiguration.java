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

import java.util.Calendar;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.registry.Registry;
import org.codehaus.plexus.registry.RegistryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 10 nov. 07
 * @version $Id$
 * @plexus.component role="org.apache.maven.continuum.web.appareance.AppareanceConfiguration" role-hint="default"
 */
public class DefaultAppareanceConfiguration
    implements AppareanceConfiguration, Initializable
{

    private String FOOTER_REGISTRY_KEY = "footer";
    
    private String REGISTRY_SECTION_KEY = "org.apache.maven.continuum.user";
    
    private String footer;
    
    /**
     * @plexus.requirement role-hint="commons-configuration"
     */
    private Registry registry;
    
    // ------------------------------------------------
    //  Plexus Lifecycle
    // ------------------------------------------------
    
    public void initialize()
        throws InitializationException
    {
        Registry continuumRegistry = getContinuumRegistry();
        if (continuumRegistry != null)
        {
            this.footer = continuumRegistry.getString( FOOTER_REGISTRY_KEY );
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
        throws RegistryException
    {
        Registry continuumRegistry = getContinuumRegistry();
        
        continuumRegistry.setString( FOOTER_REGISTRY_KEY, footerHtmlContent );
        continuumRegistry.save();
        this.footer = footerHtmlContent;
    }

    // ------------------------------------------------
    //  Internal stuff
    // ------------------------------------------------

    private Registry getContinuumRegistry()
    {
        return registry.getSection( REGISTRY_SECTION_KEY );
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
    
}
