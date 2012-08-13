package org.apache.maven.continuum;

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

import junit.framework.Assert;
import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.core.action.CheckoutProjectContinuumAction;

import java.util.Map;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class AddProjectToCheckoutQueueStub
    extends AbstractContinuumAction
{
    @SuppressWarnings( "unchecked" )
    public void execute( Map context )
        throws Exception
    {
        getLogger().info( "Executing add-project-to-checkout-queue (stub for testing) action." );

        // check if scm credentials were set in context (CONTINUUM-2466)
        Assert.assertEquals( AddProjectTest.SCM_USERNAME, CheckoutProjectContinuumAction.getScmUsername( context,
                                                                                                         null ) );
        Assert.assertEquals( AddProjectTest.SCM_PASSWORD, CheckoutProjectContinuumAction.getScmPassword( context,
                                                                                                         null ) );
    }
}
