package org.apache.maven.continuum.web.action.admin;

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

import org.apache.maven.continuum.web.action.component.AbstractFooterAction;
import org.apache.maven.continuum.web.appareance.AppareanceConfiguration;

import java.io.IOException;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="configureFooter"
 * @since 9 nov. 07
 */
public class ConfigureFooterAction
    extends AbstractFooterAction
{

    /**
     * @plexus.requirement
     */
    private AppareanceConfiguration appareanceConfiguration;

    public String saveFooter()
        throws IOException
    {
        appareanceConfiguration.saveFooter( getFooter() );
        return SUCCESS;
    }

}
