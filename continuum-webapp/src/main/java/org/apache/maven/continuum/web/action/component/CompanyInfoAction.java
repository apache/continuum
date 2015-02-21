package org.apache.maven.continuum.web.action.component;

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

import com.opensymphony.xwork2.ActionSupport;
import org.apache.continuum.utils.m2.LocalRepositoryHelper;
import org.apache.maven.model.Model;
import org.apache.maven.shared.app.company.CompanyPomHandler;
import org.apache.maven.shared.app.configuration.MavenAppConfiguration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Stores the company information for displaying on the page.
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "companyInfo", instantiationStrategy = "per-lookup" )
public class CompanyInfoAction
    extends ActionSupport
{
    private String companyLogo;

    private String companyUrl;

    private String companyName;

    @Requirement
    private CompanyPomHandler handler;

    @Requirement
    private MavenAppConfiguration appConfiguration;

    @Requirement
    private LocalRepositoryHelper helper;

    public String execute()
        throws Exception
    {
        Model model = handler.getCompanyPomModel( appConfiguration.getConfiguration().getCompanyPom(),
                                                  helper.getLocalRepository() );

        if ( model != null )
        {
            if ( model.getOrganization() != null )
            {
                companyName = model.getOrganization().getName();
                companyUrl = model.getOrganization().getUrl();
            }

            companyLogo = model.getProperties().getProperty( "organization.logo" );
        }

        return SUCCESS;
    }

    public String getCompanyLogo()
    {
        return companyLogo;
    }

    public String getCompanyUrl()
    {
        return companyUrl;
    }

    public String getCompanyName()
    {
        return companyName;
    }
}
