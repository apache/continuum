package org.apache.continuum.web.test.parent;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public abstract class AbstractReleaseTest
    extends AbstractContinuumTest
{
    public void releasePrepareProject( String username, String password, String tagBase, String tag, String releaseVersion, String developmentVersion, boolean success )
        throws Exception
    {
        goToReleasePreparePage();
        setFieldValue( "scmUsername", username );
        setFieldValue( "scmPassword", password );
        setFieldValue( "scmTag", tag );
        setFieldValue( "scmTagBase", tagBase );
        setFieldValue( "prepareGoals", "clean" );
        setFieldValue( "relVersions", releaseVersion );
        setFieldValue( "devVersions", developmentVersion );
        submit();

        while ( !isButtonWithValuePresent( "Done" ) )
        {
            Thread.sleep( 10000 );
        }

        assertButtonWithValuePresent( "Rollback changes" );

        if ( success )
        {
            assertImgWithAltNotPresent( "Error" );
        }
        else
        {
            assertImgWithAlt( "Error" );
        }
    }

    public void goToReleasePreparePage()
    {
        clickLinkWithLocator( "goal", false );
        submit();
        assertReleasePreparePage();
    }

    public void assertReleasePreparePage()
    {
        assertPage( "Continuum - Release Project" );
        assertTextPresent( "Prepare Project for Release" );
        assertTextPresent( "Release Prepare Parameters" );
        assertTextPresent( "SCM Username" );
        assertTextPresent( "SCM Password" );
        assertTextPresent( "SCM Tag" );
        assertTextPresent( "SCM Tag Base" );
        assertTextPresent( "SCM Comment Prefix" );
        assertTextPresent( "Preparation Goals" );
        assertTextPresent( "Arguments" );
        assertTextPresent( "Build Environment" );
        assertTextPresent( "Release Version" );
        assertTextPresent( "Next Development Version" );
        assertButtonWithValuePresent( "Submit" );
    }
}
