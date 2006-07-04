package org.apache.maven.continuum.web.filter;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.xwork.PlexusLifecycleListener;

/**
 * <p>Delegates <code>Filter</code> requests to a Plexus component.</p>
 * 
 * <p>This class acts as a proxy on behalf of a
 * target {@link Filter} that is instantiated by the Plexus container. It is necessary to specify which target
 * {@link Filter} should be proxied as a filter initialization parameter.</p>
 * 
 * <p>To use this filter, it is necessary to specify the following filter initialization parameter:
 *  <ul>
 *      <li><code>component</code> indicates the name of the target <code>Filter</code> defined in the container.
 *      The only requirements are that this component implements the <code>javax.servlet.Filter</code>
 *      interface and is available in the <code>Container</code> under that name.</li>
 *  </ul>
 * </p>
 * 
 * <p>A final optional initialization parameter, <code>lifecycle</code>, determines whether the servlet container
 * or the IoC container manages the lifecycle of the proxied filter. When possible you should write your filters to be
 * managed via the IoC container interfaces. If you cannot control the filters you wish to proxy (eg
 * you do not have their source code) you might need to allow the servlet container to manage lifecycle via the {@link
 * javax.servlet.Filter#init(javax.servlet.FilterConfig)} and {@link javax.servlet.Filter#destroy()} methods. If this
 * case, set the <code>lifecycle</code> initialization parameter to <code>servlet-container-managed</code>. If the
 * parameter is any other value, servlet container lifecycle methods will not be delegated through to the proxy.</p>
 * 
 * @author Ben Alex
 * @author Emmanuel Venisse (evenisse at apache dot org)
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class FilterToComponentProxy
    implements Filter
{
    private static final Log log = LogFactory.getLog( FilterToComponentProxy.class );

    private ServletContext ctx;

    private Filter delegate;

    private FilterConfig filterConfig;

    private boolean initialized = false;

    private boolean servletContainerManaged = false;

    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        this.filterConfig = filterConfig;

        doInit();
    }

    /**
     * Lookup the delegate {@link Filter} in Plexus container
     * 
     * @throws ServletException if this Filter or the delegate Filter are not correctly configured
     */
    private synchronized void doInit()
        throws ServletException
    {
        if ( initialized )
        {
            // already initialized, so don't re-initialize
            return;
        }

        String componentName = null;

        String param = filterConfig.getInitParameter( "component" );

        if ( ( param != null ) && !param.equals( "" ) )
        {
            componentName = param;
        }

        if ( componentName == null )
        {
            throw new ServletException( this.getClass().getName() + " require a \"component\" init-param." );
        }

        String lifecycle = filterConfig.getInitParameter( "lifecycle" );

        if ( "servlet-container-managed".equals( lifecycle ) )
        {
            servletContainerManaged = true;
        }

        PlexusContainer container = getContainer( filterConfig );

        Object object = null;
        try
        {
            object = container.lookup( componentName );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Component '" + componentName + "' not found in container" );
        }

        if ( object == null )
        {
            throw new ServletException( "Component '" + componentName + "' not found in container" );
        }

        if ( !( object instanceof Filter ) )
        {
            throw new ServletException( "Component '" + componentName + "' does not implement javax.servlet.Filter" );
        }

        delegate = (Filter) object;

        if ( servletContainerManaged )
        {
            delegate.init( filterConfig );
        }

        // Set initialized to true at the end of the synchronized method, so
        // that invocations of doFilter() before this method has completed will not
        // cause NullPointerException
        initialized = true;
    }

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        if ( !initialized )
        {
            doInit();
        }

        delegate.doFilter( request, response, chain );
    }

    public void destroy()
    {
        if ( ( delegate != null ) && servletContainerManaged )
        {
            delegate.destroy();
        }
    }

    /**
     * Allows test cases to override where container is obtained from.
     *
     * @param filterConfig which can be used to find the <code>ServletContext</code>
     *
     * @return the Plexus container
     */
    protected PlexusContainer getContainer( FilterConfig filterConfig )
    {
        ctx = filterConfig.getServletContext();

        return (PlexusContainer) ctx.getAttribute( PlexusLifecycleListener.KEY );
    }
}
