package org.apache.maven.continuum.web.action.admin;

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.continuum.installation.AlreadyExistsInstallationException;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.profile.AlreadyExistsProfileException;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumConfirmAction;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="installation"
 * @since 14 juin 07
 */
public class InstallationAction
    extends ContinuumConfirmAction
    implements Preparable, SecureAction
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private InstallationService installationService;

    private List<Installation> installations;

    private Installation installation;

    private Map<String, String> typesLabels;

    private List<String> types;

    private boolean varNameUpdatable = false;

    private boolean automaticProfile;

    private boolean varNameDisplayable = false;

    private boolean displayTypes = true;

    private String installationType;

    private Map<String, String> installationTypes;

    private static final String TOOL_TYPE_KEY = "tool";

    private boolean automaticProfileDisplayable = true;

    private boolean confirmed;

    // -----------------------------------------------------
    // Webwork methods
    // -----------------------------------------------------

    public String list()
        throws Exception
    {
        this.installations = installationService.getAllInstallations();
        return SUCCESS;
    }

    public String edit()
        throws Exception
    {
        this.installation = installationService.getInstallation( installation.getInstallationId() );

        if ( this.installation != null )
        {
            this.configureUiFlags();
        }
        this.automaticProfileDisplayable = false;
        return SUCCESS;
    }

    public String input()
        throws Exception
    {
        if ( InstallationService.ENVVAR_TYPE.equalsIgnoreCase( this.getInstallationType() ) )
        {
            this.installation = new Installation();
            this.installation.setType( InstallationService.ENVVAR_TYPE );
            this.setDisplayTypes( false );
            this.setVarNameUpdatable( true );
            this.setVarNameDisplayable( true );
        }
        else
        {
            this.setVarNameUpdatable( false );
            this.setVarNameDisplayable( false );
        }
        return INPUT;
    }

    public String save()
        throws Exception
    {
        if ( InstallationService.ENVVAR_TYPE.equalsIgnoreCase( this.getInstallationType() ) )
        {
            this.installation.setType( InstallationService.ENVVAR_TYPE );
            if ( StringUtils.isEmpty( installation.getVarName() ) )
            {
                addFieldError( "installation.varName", getResourceBundle().getString(
                    "installation.varName.required" ) );
                return INPUT;
            }

        }
        if ( installation.getInstallationId() == 0 )
        {
            try
            {
                installationService.add( installation, this.automaticProfile );
            }
            catch ( AlreadyExistsInstallationException e )
            {
                this.addActionError( getResourceBundle().getString( "installation.name.duplicate" ) );
                return INPUT;
            }
            catch ( AlreadyExistsProfileException e )
            {
                this.addActionError( getResourceBundle().getString( "profile.name.already.exists" ) );
                return INPUT;
            }
        }
        else
        {
            this.configureUiFlags();
            try
            {
                installationService.update( installation );
            }
            catch ( AlreadyExistsInstallationException e )
            {
                this.addActionError( getResourceBundle().getString( "installation.name.duplicate" ) );
                return INPUT;
            }
        }
        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        if ( confirmed )
        {
            Installation installationToDelete = installationService.getInstallation( installation.getInstallationId() );
            installationService.delete( installationToDelete );
            this.installations = installationService.getAllInstallations();
        }
        else
        {
            return CONFIRM;
        }
        return SUCCESS;
    }

    public String listTypes()
    {
        this.installationTypes = new LinkedHashMap<String, String>();
        ResourceBundle resourceBundle = getResourceBundle();
        this.installationTypes.put( TOOL_TYPE_KEY, resourceBundle.getString( "installationTypeChoice.tool.label" ) );
        this.installationTypes.put( InstallationService.ENVVAR_TYPE, resourceBundle.getString(
            "installationTypeChoice.envar.label" ) );

        return SUCCESS;
    }

    // -----------------------------------------------------
    // security
    // -----------------------------------------------------

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_MANAGE_INSTALLATIONS, Resource.GLOBAL );

        return bundle;
    }

    // -----------------------------------------------------
    // utils
    // -----------------------------------------------------
    private void configureUiFlags()
    {
        // we can update env var name only with env var type
        if ( !InstallationService.ENVVAR_TYPE.equals( this.installation.getType() ) )
        {
            this.setDisplayTypes( true );
            this.setVarNameUpdatable( false );
        }
        else
        {
            this.setDisplayTypes( false );
            this.setVarNameUpdatable( true );
            this.setVarNameDisplayable( true );
        }
        this.setInstallationType( this.getInstallation().getType() );
    }

    // -----------------------------------------------------
    // getter/setters
    // -----------------------------------------------------

    public List<Installation> getInstallations()
    {
        return installations;
    }

    public void setInstallations( List<Installation> installations )
    {
        this.installations = installations;
    }

    public Installation getInstallation()
    {
        return installation;
    }

    public void setInstallation( Installation installation )
    {
        this.installation = installation;
    }

    public Map<String, String> getTypesLabels()
    {
        if ( this.typesLabels == null )
        {
            this.typesLabels = new LinkedHashMap<String, String>();
            ResourceBundle resourceBundle = getResourceBundle();
            this.typesLabels.put( InstallationService.JDK_TYPE, resourceBundle.getString(
                "installation.jdk.type.label" ) );
            this.typesLabels.put( InstallationService.MAVEN2_TYPE, resourceBundle.getString(
                "installation.maven2.type.label" ) );
            this.typesLabels.put( InstallationService.MAVEN1_TYPE, resourceBundle.getString(
                "installation.maven1.type.label" ) );
            this.typesLabels.put( InstallationService.ANT_TYPE, resourceBundle.getString(
                "installation.ant.type.label" ) );
            // CONTINUUM-1430
            //this.typesLabels.put( InstallationService.ENVVAR_TYPE, resourceBundle
            //    .getString( "installation.envvar.type.label" ) );
        }
        return typesLabels;
    }

    public void setTypesLabels( Map<String, String> typesLabels )
    {
        this.typesLabels = typesLabels;
    }

    public boolean isVarNameUpdatable()
    {
        return varNameUpdatable;
    }

    public void setVarNameUpdatable( boolean varNameUpdatable )
    {
        this.varNameUpdatable = varNameUpdatable;
    }

    public List<String> getTypes()
    {
        if ( this.types == null )
        {
            this.types = new ArrayList<String>( 5 );
            this.types.add( InstallationService.JDK_TYPE );
            this.types.add( InstallationService.MAVEN2_TYPE );
            this.types.add( InstallationService.MAVEN1_TYPE );
            this.types.add( InstallationService.ANT_TYPE );
            // CONTINUUM-1430
            //this.types.add( InstallationService.ENVVAR_TYPE );

        }
        return types;
    }

    public void setTypes( List<String> types )
    {
        this.types = types;
    }

    public boolean isAutomaticProfile()
    {
        return automaticProfile;
    }

    public void setAutomaticProfile( boolean automaticProfile )
    {
        this.automaticProfile = automaticProfile;
    }

    public Map<String, String> getInstallationTypes()
    {
        return installationTypes;
    }

    public void setInstallationTypes( Map<String, String> installationTypes )
    {
        this.installationTypes = installationTypes;
    }

    public boolean isVarNameDisplayable()
    {
        return varNameDisplayable;
    }

    public void setVarNameDisplayable( boolean varNameDisplayable )
    {
        this.varNameDisplayable = varNameDisplayable;
    }

    public boolean isDisplayTypes()
    {
        return displayTypes;
    }

    public void setDisplayTypes( boolean displayTypes )
    {
        this.displayTypes = displayTypes;
    }

    public String getInstallationType()
    {
        return installationType;
    }

    public void setInstallationType( String installationType )
    {
        this.installationType = installationType;
    }

    public boolean isAutomaticProfileDisplayable()
    {
        return automaticProfileDisplayable;
    }

    public void setAutomaticProfileDisplayable( boolean automaticProfileDisplayable )
    {
        this.automaticProfileDisplayable = automaticProfileDisplayable;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }
}
