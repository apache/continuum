package org.apache.continuum.purge.repository.content;

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

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( role = RepositoryManagedContentFactory.class )
public class RepositoryManagedContentFactoryImpl
    implements RepositoryManagedContentFactory, Contextualizable
{
    private static final Logger log = LoggerFactory.getLogger( RepositoryManagedContentFactoryImpl.class );

    private PlexusContainer container;

    @Configuration( "default" )
    private String defaultLayout;

    public RepositoryManagedContent create( String layout )
        throws ComponentLookupException
    {
        RepositoryManagedContent repoContent;
        try
        {
            log.debug( "attempting to find direct match for layout {}", layout );
            repoContent = (RepositoryManagedContent) container.lookup( RepositoryManagedContent.ROLE, layout );
            log.debug( "found direct match for layout '{}'", layout );
        }
        catch ( ComponentLookupException e )
        {
            log.warn( "layout '{}' not found, falling back to '{}'", layout, defaultLayout );
            repoContent = (RepositoryManagedContent) container.lookup( RepositoryManagedContent.ROLE, defaultLayout );
        }
        return repoContent;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
