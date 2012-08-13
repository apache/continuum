package org.apache.continuum.webdav;

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

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;

public class ContinuumBuildAgentDavResource
    implements DavResource
{
    private static final Logger log = LoggerFactory.getLogger( ContinuumBuildAgentDavResource.class );

    private final ContinuumBuildAgentDavResourceLocator locator;

    private final DavResourceFactory factory;

    private final File localResource;

    private final String logicalResource;

    private final DavSession session;

    private final MimetypesFileTypeMap mimeTypes;

    private DavPropertySet properties = null;

    public static final String COMPLIANCE_CLASS = "1, 2";

    public static final String HIDDEN_PATH_PREFIX = ".";

    public static final String SUPPORTED_METHODS = "OPTIONS, GET, HEAD, TRACE, PROPFIND";

    public ContinuumBuildAgentDavResource( String localResource, String logicalResource, DavSession session,
                                           ContinuumBuildAgentDavResourceLocator locator, DavResourceFactory factory,
                                           MimetypesFileTypeMap mimeTypes )
    {
        this.localResource = new File( localResource );
        this.logicalResource = logicalResource;
        this.locator = locator;
        this.factory = factory;
        this.session = session;
        this.mimeTypes = mimeTypes;
    }

    public void addLockManager( LockManager lockManager )
    {
    }

    public void addMember( DavResource davResource, InputContext inputContext )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public MultiStatusResponse alterProperties( List changeList )
        throws DavException
    {
        return null;
    }

    public MultiStatusResponse alterProperties( DavPropertySet setProperties, DavPropertyNameSet removePropertyNames )
        throws DavException
    {
        return null;
    }

    public void copy( DavResource destination, boolean shallow )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public boolean exists()
    {
        return localResource.exists();
    }

    public DavResource getCollection()
    {
        DavResource parent = null;
        if ( getResourcePath() != null && !"/".equals( getResourcePath() ) )
        {
            String parentPath = Text.getRelativeParent( getResourcePath(), 1 );
            if ( "".equals( parentPath ) )
            {
                parentPath = "/";
            }

            DavResourceLocator parentloc = locator.getFactory().createResourceLocator( locator.getPrefix(),
                                                                                       parentPath );

            try
            {
                parent = factory.createResource( parentloc, session );
            }
            catch ( DavException e )
            {
                // should not occur
            }
        }
        return parent;
    }

    public String getComplianceClass()
    {
        return COMPLIANCE_CLASS;
    }

    public String getDisplayName()
    {
        String resPath = getResourcePath();
        return ( resPath != null ) ? Text.getName( resPath ) : resPath;
    }

    public DavResourceFactory getFactory()
    {
        return factory;
    }

    public String getHref()
    {
        return locator.getHref( isCollection() );
    }

    public File getLocalResource()
    {
        return localResource;
    }

    public DavResourceLocator getLocator()
    {
        return locator;
    }

    public ActiveLock getLock( Type type, Scope scope )
    {
        return null;
    }

    public ActiveLock[] getLocks()
    {
        return null;
    }

    public String getLogicalResource()
    {
        return logicalResource;
    }

    public DavResourceIterator getMembers()
    {
        List<DavResource> list = new ArrayList<DavResource>();
        if ( exists() && isCollection() )
        {
            for ( String item : localResource.list() )
            {
                try
                {
                    if ( !item.startsWith( HIDDEN_PATH_PREFIX ) )
                    {
                        String path = locator.getResourcePath() + '/' + item;
                        DavResourceLocator resourceLocator = locator.getFactory().createResourceLocator(
                            locator.getPrefix(), path );
                        DavResource resource = factory.createResource( resourceLocator, session );

                        if ( resource != null )
                        {
                            log.debug( "Retrieved resource: " + resource.getResourcePath() );
                            list.add( resource );
                        }
                    }
                }
                catch ( DavException e )
                {
                    // should not occur
                }
            }
        }

        return new DavResourceIteratorImpl( list );
    }

    public MimetypesFileTypeMap getMimeTypes()
    {
        return mimeTypes;
    }

    public long getModificationTime()
    {
        return localResource.lastModified();
    }

    public DavPropertySet getProperties()
    {
        return initProperties();
    }

    public DavProperty getProperty( DavPropertyName propertyName )
    {
        return getProperties().get( propertyName );
    }

    public DavPropertyName[] getPropertyNames()
    {
        return getProperties().getPropertyNames();
    }

    public String getResourcePath()
    {
        return locator.getResourcePath();
    }

    public DavSession getSession()
    {
        return session;
    }

    public String getSupportedMethods()
    {
        return SUPPORTED_METHODS;
    }

    public boolean hasLock( Type type, Scope scope )
    {
        return false;
    }

    public boolean isCollection()
    {
        return localResource.isDirectory();
    }

    public boolean isLockable( Type type, Scope scope )
    {
        return false;
    }

    public ActiveLock lock( LockInfo lockInfo )
        throws DavException
    {
        return null;
    }

    public void move( DavResource destination )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public ActiveLock refreshLock( LockInfo lockInfo, String lockTocken )
        throws DavException
    {
        return null;
    }

    public void removeMember( DavResource member )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public void removeProperty( DavPropertyName propertyName )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public void setProperty( DavProperty property )
        throws DavException
    {
        throw new UnsupportedOperationException( "Not supported" );
    }

    public void spool( OutputContext outputContext )
        throws IOException
    {
        if ( !isCollection() )
        {
            outputContext.setContentLength( localResource.length() );
            outputContext.setContentType( mimeTypes.getContentType( localResource ) );
        }

        if ( !isCollection() && outputContext.hasStream() )
        {
            FileInputStream is = null;
            try
            {
                // Write content to stream
                is = new FileInputStream( localResource );
                IOUtils.copy( is, outputContext.getOutputStream() );
            }
            finally
            {
                IOUtils.closeQuietly( is );
            }
        }
    }

    public void unlock( String lockTocken )
        throws DavException
    {
    }

    /**
     * Fill the set of properties
     */
    protected DavPropertySet initProperties()
    {
        if ( !exists() )
        {
            properties = new DavPropertySet();
        }

        if ( properties != null )
        {
            return properties;
        }

        DavPropertySet properties = new DavPropertySet();

        // set (or reset) fundamental properties
        if ( getDisplayName() != null )
        {
            properties.add( new DefaultDavProperty( DavPropertyName.DISPLAYNAME, getDisplayName() ) );
        }
        if ( isCollection() )
        {
            properties.add( new ResourceType( ResourceType.COLLECTION ) );
            // Windows XP support
            properties.add( new DefaultDavProperty( DavPropertyName.ISCOLLECTION, "1" ) );
        }
        else
        {
            properties.add( new ResourceType( ResourceType.DEFAULT_RESOURCE ) );

            // Windows XP support
            properties.add( new DefaultDavProperty( DavPropertyName.ISCOLLECTION, "0" ) );
        }

        // Need to get the ISO8601 date for properties
        DateTime dt = new DateTime( localResource.lastModified() );
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String modifiedDate = fmt.print( dt );

        properties.add( new DefaultDavProperty( DavPropertyName.GETLASTMODIFIED, modifiedDate ) );

        properties.add( new DefaultDavProperty( DavPropertyName.CREATIONDATE, modifiedDate ) );

        properties.add( new DefaultDavProperty( DavPropertyName.GETCONTENTLENGTH, localResource.length() ) );

        this.properties = properties;

        return properties;
    }
}
