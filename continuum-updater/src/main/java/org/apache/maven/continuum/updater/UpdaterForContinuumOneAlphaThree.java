package org.apache.maven.continuum.updater;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.updater.exception.UpdaterException;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.updater.Updater"
 *   role-hint="updateTo1.0-alpha-4"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class UpdaterForContinuumOneAlphaThree
    implements Updater
{
    public String getReleaseUrl()
    {
        return "http://www.apache.org/dist/maven/binaries/continuum-1.0-alpha-4-bin.zip";
    }

    public void updateDatabase()
        throws UpdaterException
    {
        //throw new UpdaterException( "Not implemented." );
    }
}
