package org.apache.maven.continuum.installation;

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

/**
 * @author <a href="mailto:olamy@codehaus.org">olamy</a>
 * @since 13 juin 07
 */
public class InstallationException
    extends Exception
{
    private static final long serialVersionUID = 5055136446336281496L;

    public InstallationException( String message )
    {
        super( message );
    }

    public InstallationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
