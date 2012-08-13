package org.apache.continuum.web.action;

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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.jmock.MockObjectTestCase;

public abstract class AbstractActionTest
    extends MockObjectTestCase
{
    protected void setUp()
        throws Exception
    {
        if ( ActionContext.getContext() == null )
        {
            // This fix allow initialization of ActionContext.getContext() to avoid NPE

            ConfigurationManager configurationManager = new ConfigurationManager();
            configurationManager.addContainerProvider( new XWorkConfigurationProvider() );
            com.opensymphony.xwork2.config.Configuration config = configurationManager.getConfiguration();
            Container container = config.getContainer();

            ValueStack stack = container.getInstance( ValueStackFactory.class ).createValueStack();
            stack.getContext().put( ActionContext.CONTAINER, container );
            ActionContext.setContext( new ActionContext( stack.getContext() ) );

            assertNotNull( ActionContext.getContext() );
        }
    }
}
