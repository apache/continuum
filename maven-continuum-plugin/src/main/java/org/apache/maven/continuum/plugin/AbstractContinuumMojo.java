package org.apache.maven.continuum.plugin;

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

import org.apache.maven.continuum.xmlrpc.client.ContinuumXmlRpcClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumMojo
    extends AbstractMojo
{
    /**
     * The Continuum XML-RPC server URL.
     *
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    /**
     * The Continuum username.
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The Continuum password.
     *
     * @parameter expression="${password}"
     */
    private String password;

    private ContinuumXmlRpcClient client;

    protected void createClient()
        throws MojoExecutionException
    {
        URL continuumUrl;

        try
        {
            continuumUrl = new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "The URL '" + url + "' isn't valid." );
        }

        client = new ContinuumXmlRpcClient( continuumUrl, username, password );
    }

    protected ContinuumXmlRpcClient getClient()
        throws MojoExecutionException
    {
        if ( client == null )
        {
            createClient();
        }
        return client;
    }
}
