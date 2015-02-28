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

import org.apache.continuum.web.util.FixedBufferAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.codehaus.plexus.component.annotations.Component;

import java.util.Properties;

/**
 * AboutAction:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "about", instantiationStrategy = "per-lookup" )
public class AboutAction
    extends ContinuumActionSupport
{
    private Properties systemProperties;

    private String logOutput;

    public String execute()
        throws Exception
    {
        try
        {
            checkManageConfigurationAuthorization();
            systemProperties = System.getProperties();
            logOutput = constructOutput();
        }
        catch ( Exception e )
        {
            // Ignore, just hide additional system information
        }
        return SUCCESS;
    }

    private String constructOutput()
    {
        StringBuilder buf = new StringBuilder();
        Object async = Logger.getRootLogger().getAppender( "async" );
        if ( async != null && async instanceof AppenderAttachable )
        {
            Object webViewable = ( (AppenderAttachable) async ).getAppender( "webViewable" );
            if ( webViewable != null && webViewable instanceof FixedBufferAppender )
            {
                FixedBufferAppender appender = (FixedBufferAppender) webViewable;
                for ( String line : appender.getLines() )
                {
                    buf.append( line );
                }
            }
        }
        return buf.toString();
    }

    public Properties getSystemProperties()
    {
        return systemProperties;
    }

    public String getLogOutput()
    {
        return logOutput;
    }
}
