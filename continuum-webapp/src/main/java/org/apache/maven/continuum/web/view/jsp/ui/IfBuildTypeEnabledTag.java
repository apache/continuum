package org.apache.maven.continuum.web.view.jsp.ui;

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

import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

/**
 * ifBuildTypeEnabledTag:
 *
 * @author Jan Ancajas <jansquared@gmail.com>
 */
public class IfBuildTypeEnabledTag
    extends ConditionalTagSupport
{
    private Continuum continuum;

    private String buildType;

    public static final String DISTRIBUTED = "distributed";

    protected boolean condition()
        throws JspTagException
    {

        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
            pageContext.getServletContext() );
        this.setContinuum( (Continuum) applicationContext.getBean( PlexusToSpringUtils.buildSpringId( Continuum.ROLE,
                                                                                                      "default" ) ) );

        if ( continuum == null )
        {
            throw new JspTagException( "cannot lookup component:  " + Continuum.ROLE );
        }

        if ( DISTRIBUTED.equals( buildType ) )
        {
            return continuum.getConfiguration().isDistributedBuildEnabled();
        }

        // left out 'parallel' buildType checking for cyclomatic complexity's sake :)
        return !continuum.getConfiguration().isDistributedBuildEnabled();

    }

    public String getBuildType()
    {
        return buildType;
    }

    public void setBuildType( String buildType )
    {
        this.buildType = buildType;
    }

    public Continuum getContinuum()
    {
        return continuum;
    }

    public void setContinuum( Continuum continuum )
    {
        this.continuum = continuum;
    }
}
