package org.apache.continuum.utils.shell;

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

/**
 * Collects output to a logger. Unlike the list-based consumer, this should be safe to use when the output size is
 * expected to be large. It logs the output at level INFO.
 */
public class LogOutputConsumer
    implements OutputConsumer
{
    private Logger log;

    public LogOutputConsumer( Logger log )
    {
        this.log = log;
    }

    public void consume( String line )
    {
        if ( log != null )
            log.info( line );
    }
}
