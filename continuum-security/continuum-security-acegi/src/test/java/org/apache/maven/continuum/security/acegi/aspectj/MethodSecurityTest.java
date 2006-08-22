package org.apache.maven.continuum.security.acegi.aspectj;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.acegisecurity.MockAccessDecisionManager;
import org.acegisecurity.MockAuthenticationManager;
import org.acegisecurity.MockRunAsManager;
import org.acegisecurity.intercept.method.MethodDefinitionMap;
import org.acegisecurity.intercept.method.MethodDefinitionSourceMapping;
import org.acegisecurity.intercept.method.aspectj.AspectJSecurityInterceptor;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.acegi.intercept.method.aspectj.AspectJSecurityInterceptorHelper;

/**
 * Test for {@link ContinuumSecurityAspect}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class MethodSecurityTest
    extends AbstractMethodSecurityTest
{

    protected void setUp()
        throws Exception
    {
        super.setUp();
        MethodDefinitionSourceMapping mapping = new MethodDefinitionSourceMapping();
        mapping.setMethodName( Continuum.class.getName() + ".addProject" );
        mapping.setConfigAttributes( Arrays.asList( new String[] { "MOCK_ADMIN" } ) );

        List mappings = new ArrayList();
        mappings.add( mapping );

        MethodDefinitionMap methodDefinitionSource = new MethodDefinitionMap();
        methodDefinitionSource.setMappings( mappings );

        AspectJSecurityInterceptor si = new AspectJSecurityInterceptor();
        si.setObjectDefinitionSource( methodDefinitionSource );
        si.setAccessDecisionManager(new MockAccessDecisionManager());
        si.setAuthenticationManager(new MockAuthenticationManager());
        si.setRunAsManager(new MockRunAsManager());

        setContinuum( new ContinuumStub() );
        AspectJSecurityInterceptorHelper helper = new AspectJSecurityInterceptorHelper();
        helper.setAspectName( "org.apache.maven.continuum.security.acegi.aspectj.ContinuumSecurityAspect" );
        helper.setSecurityInterceptor( si );
        helper.initialize();
        //        aspect = ContinuumSecurityAspect.aspectOf();
    }
}
