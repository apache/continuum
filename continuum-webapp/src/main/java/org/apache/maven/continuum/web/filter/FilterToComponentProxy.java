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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.xwork.PlexusLifecycleListener;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Emmanuel Venisse (evenisse at apache dot org)
 */
public class FilterToComponentProxy
    implements Filter
{
    private static final Log log = LogFactory.getLog( FilterToComponentProxy.class );

    private ServletContext ctx;

    private String componentName;

    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        ctx = filterConfig.getServletContext();

        if (filterConfig != null)
        {
            String param = filterConfig.getInitParameter( "component" );

            if ( param != null)
            {
                componentName = param;
            }
            else
            {
                throw new ServletException( this.getClass().getName() + " require a \"component\" init-param." );
            }
        }
    }

    public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
        throws IOException, ServletException
    {
        PlexusContainer container = (PlexusContainer) ctx.getAttribute( PlexusLifecycleListener.KEY );

        try
        {
            Object component = container.lookup( componentName );
        }
        catch ( Exception e )
        {
            //DO SOMETHING
        }

        chain.doFilter( req, res );
    }

    public void destroy()
    {
    }
}
