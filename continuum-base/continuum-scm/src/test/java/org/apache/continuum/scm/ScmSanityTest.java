package org.apache.continuum.scm;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.svn.svnexe.command.changelog.SvnChangeLogConsumer;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests to verify assumptions and avoid regressions in SCM dependencies.
 */
public class ScmSanityTest
{

    private InputStream getTestInput( String path )
    {
        return ScmSanityTest.class.getResourceAsStream( path );
    }

    /**
     * Tests that CONTINUUM-1640 is fixed by updated maven-scm
     */
    @Test
    public void testSvnLogWithSpaceInAuthorWorks()
        throws Exception
    {
        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer( new DefaultLog(), null );
        InputStream input = getTestInput( "svnlog-with-space-in-author.txt" );
        BufferedReader r = new BufferedReader( new InputStreamReader( input ) );
        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();
        assertEquals( 2, modifications.size() );

        ChangeSet firstEntry = (ChangeSet) modifications.get( 0 );
        assertEquals( "Immanuel Scheerer", firstEntry.getAuthor() );

        ChangeSet secondEntry = (ChangeSet) modifications.get( 1 );
        assertEquals( "Immanuel Scheerer", secondEntry.getAuthor() );
    }
}
