package org.apache.continuum.scm.manager;

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

import org.apache.maven.scm.log.ScmLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SLF4J logger for Maven SCM.
 *
 * @version $Id$
 * @todo move to maven-scm?
 */
@Service( "scmLogger" )
public class Slf4jScmLogger
    implements ScmLogger
{
    private static final Logger logger = LoggerFactory.getLogger( Slf4jScmLogger.class );

    public void debug( String arg0 )
    {
        logger.debug( arg0 );
    }

    public void debug( Throwable arg0 )
    {
        logger.debug( "Exception", arg0 );
    }

    public void debug( String arg0, Throwable arg1 )
    {
        logger.debug( arg0, arg1 );
    }

    public void error( String arg0 )
    {
        logger.error( arg0 );
    }

    public void error( Throwable arg0 )
    {
        logger.error( "Exception", arg0 );
    }

    public void error( String arg0, Throwable arg1 )
    {
        logger.error( arg0, arg1 );
    }

    public void info( String arg0 )
    {
        logger.info( arg0 );
    }

    public void info( Throwable arg0 )
    {
        logger.info( "Exception", arg0 );
    }

    public void info( String arg0, Throwable arg1 )
    {
        logger.info( arg0, arg1 );
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }

    public void warn( String arg0 )
    {
        logger.warn( arg0 );
    }

    public void warn( Throwable arg0 )
    {
        logger.warn( "Exception", arg0 );
    }

    public void warn( String arg0, Throwable arg1 )
    {
        logger.warn( arg0, arg1 );
    }
}
