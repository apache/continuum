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

/**
 * A portable program that sleeps. Useful for cross platform shell tests.
 */
public class Sleep
{

    public static void main( String[] args )
        throws InterruptedException
    {
        if ( args.length != 1 )
        {
            System.err.printf( "usage: java %s <time-to-sleep-in-seconds>\n", Sleep.class.getName() );
            System.exit( 1 );
        }
        int sleepTimeInMillis = Integer.valueOf( args[0] ) * 1000;
        Thread.sleep( sleepTimeInMillis );
    }
}
