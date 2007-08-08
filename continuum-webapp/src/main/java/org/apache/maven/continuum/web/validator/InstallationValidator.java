package org.apache.maven.continuum.web.validator;

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

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.ValidatorSupport;
import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.apache.maven.continuum.installation.InstallationException;
import org.apache.maven.continuum.installation.InstallationService;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;


/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.validator.Validator" role-hint="org.apache.maven.continuum.web.validator.InstallationValidator"
 * @since 19 juin 07
 */
public class InstallationValidator
    extends ValidatorSupport
    implements LogEnabled
{
    private String fieldName;

    private Logger logger;

    /**
     * @plexus.requirement role-hint="default"
     */
    private InstallationService installationService;

    /**
     * @see com.opensymphony.xwork.validator.Validator#validate(java.lang.Object)
     */
    public void validate( Object object )
        throws ValidationException
    {
        String name = (String) this.getFieldValue( "installation.name", object );
        if ( StringUtils.isEmpty( name ) )
        {
            // nothing empty name
            return;
        }

        String varValue = (String) this.getFieldValue( "installation.varValue", object );

        // TODO validating varValue != null depending on type (not null for envVar)

        String type = (String) this.getFieldValue( "installation.type", object );

        ExecutorConfigurator executorConfigurator = installationService.getExecutorConfigurator( type );
        try
        {
            if ( executorConfigurator != null )
            {
                if ( executorConfigurator.getVersionArgument() != null )
                {
                    // just try to get version infos to validate path is valid
                    List<String> versionInfos = installationService
                        .getExecutorConfiguratorVersion( varValue, executorConfigurator, null );
                }
            }
        }
        catch ( InstallationException e )
        {
            String message = getMessage( getMessageKey() ) + e.getMessage();
            logger.error( message );
            addFieldError( "installation.varValue", message );
        }
    }


    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName( String fieldName )
    {
        this.fieldName = fieldName;
    }

    /**
     * @see org.codehaus.plexus.logging.LogEnabled#enableLogging(org.codehaus.plexus.logging.Logger)
     */
    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
}
