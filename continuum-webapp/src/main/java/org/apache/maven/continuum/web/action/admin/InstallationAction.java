package org.apache.maven.continuum.web.action.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

import com.opensymphony.xwork.Preparable;

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
 * @since 14 juin 07
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="installation"
 */
public class InstallationAction
    extends ContinuumActionSupport
    implements Preparable
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private InstallationService installationService;

    private List<Installation> installations;

    private Installation installation;

    private Map typesLabels;

    private List types;

    private boolean varNameUpdatable = true;

    private boolean nameUpdatable = true;

    // -----------------------------------------------------
    // Webwork methods
    // -----------------------------------------------------

    public void prepare()
        throws Exception
    {
        super.prepare();
    }

    public String list()
        throws Exception
    {
        this.installations = installationService.getAllInstallations();
        return SUCCESS;
    }

    public String edit()
        throws Exception
    {
        this.installation = installationService.getInstallation( installation.getName() );

        if ( this.installation != null )
        {
            this.nameUpdatable = false;
            // we can update env var name only with env var type
            if ( !InstallationService.ENVVAR_TYPE.equals( this.installation.getType() ) )
            {
                this.varNameUpdatable = false;
            }
        }
        return SUCCESS;
    }

    public String input()
        throws Exception
    {
        return INPUT;
    }

    public String save()
        throws Exception
    {
        Installation installationToSave = installationService.getInstallation( this.installation.getName() );
        if ( installationToSave == null )
        {
            installationService.add( installation );
        }
        else
        {
            installationToSave.setName( installation.getName() );
            installationToSave.setVarName( installation.getVarName() );
            installationToSave.setVarValue( installation.getVarValue() );
            installationService.update( installationToSave );
        }
        this.installations = installationService.getAllInstallations();
        return SUCCESS;
    }

    public String delete()
        throws Exception
    {
        Installation installationToDelete = installationService.getInstallation( installation.getName() );
        installationService.delete( installationToDelete );
        this.installations = installationService.getAllInstallations();
        return SUCCESS;
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

    public Map getTypesLabels()
    {
        if ( this.typesLabels == null )
        {
            this.typesLabels = new LinkedHashMap<String, String>();
            ResourceBundle resourceBundle = getTexts( "localization/Continuum" );
            this.typesLabels.put( InstallationService.JDK_TYPE, resourceBundle
                .getString( "installation.jdk.type.label" ) );
            this.typesLabels.put( InstallationService.MAVEN2_TYPE, resourceBundle
                .getString( "installation.maven2.type.label" ) );
            this.typesLabels.put( InstallationService.MAVEN1_TYPE, resourceBundle
                .getString( "installation.maven1.type.label" ) );
            this.typesLabels.put( InstallationService.ANT_TYPE, resourceBundle
                .getString( "installation.ant.type.label" ) );
            this.typesLabels.put( InstallationService.ENVVAR_TYPE, resourceBundle
                .getString( "installation.envvar.type.label" ) );
        }
        return typesLabels;
    }

    public void setTypesLabels( Map typesLabels )
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

    public List getTypes()
    {
        if ( this.types == null )
        {
            this.types = new ArrayList();
            this.types.add( InstallationService.JDK_TYPE );
            this.types.add( InstallationService.MAVEN2_TYPE );
            this.types.add( InstallationService.MAVEN1_TYPE );
            this.types.add( InstallationService.ANT_TYPE );
            this.types.add( InstallationService.ENVVAR_TYPE );

        }
        return types;
    }

    public void setTypes( List types )
    {
        this.types = types;
    }

    public boolean isNameUpdatable()
    {
        return nameUpdatable;
    }

    public void setNameUpdatable( boolean nameUpdatable )
    {
        this.nameUpdatable = nameUpdatable;
    }

}
