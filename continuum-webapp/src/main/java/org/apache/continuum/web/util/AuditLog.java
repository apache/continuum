package org.apache.continuum.web.util;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author Jevica Arianne B. Zurbano
 * @since 17 apr 09
 */
public class AuditLog
{
    private Logger logger = LoggerFactory.getLogger( AuditLog.class.getName() );

    private String action;

    private String category;

    private String resource;

    private String currentUser;

    public AuditLog( String action )
    {
        this.action = action;
    }

    public AuditLog( String resource, String action )
    {
        this.action = action;
        this.resource = resource;
    }

    public void setCurrentUser( String currentUser )
    {
        this.currentUser = currentUser;
    }

    public String getCurrentUser()
    {
        return currentUser;
    }

    public void setResource( String resource )
    {
        this.resource = resource;
    }

    public String getResource()
    {
        return resource;
    }

    public void setCategory( String category )
    {
        this.category = category;
    }

    public String getCategory()
    {
        return category;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

    public void log()
    {
        if ( currentUser != null )
        {
            MDC.put( "security.currentUser", currentUser );
        }

        if ( resource != null )
        {
            if ( category != null )
            {
                logger.info( category + " " + resource + " - " + action );
            }
            else
            {
                logger.info( resource + " - " + action );
            }
        }
    }
}

