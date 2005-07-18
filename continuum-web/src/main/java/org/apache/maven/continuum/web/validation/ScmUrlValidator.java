package org.apache.maven.continuum.web.validation;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.util.List;

import org.codehaus.plexus.formica.FormicaException;
import org.codehaus.plexus.formica.validation.AbstractValidator;
import org.codehaus.plexus.scm.ScmManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmUrlValidator
    extends AbstractValidator
{
    /**
     * @plexus.requirement
     */
    private ScmManager scmManager;

    public boolean validate( String scmUrl )
        throws FormicaException
    {
        List messages = scmManager.validateScmRepository( scmUrl );

        if (messages.size() != 0)
        {
            return false;
        }

        return true;
    }
}
