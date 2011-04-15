package org.apache.continuum.web.interceptor;

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

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.TokenInterceptor;
import org.apache.struts2.interceptor.TokenSessionStoreInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * ContinuumTokenSessionInterceptor allows the action to pass through if <i>explicitCSRFCheck</i> is
 * enabled even if the TokenSessionStoreInterceptor returned invalid token error. If the <i>explicitCSRFCheck</i>
 * parameter is <i>true</i>, it means that the CSRF check will be done in the action itself. Otherwise,
 * whatever  result is returned by the TokenSessionStoreInterceptor will just be propagated.
 *
 * This is to handle cases in Continuum where an <s:action> tag is also present in the page (example is projectGroupSummary.jsp)
 * causing a double submit and the TokenSessionStoreInterceptor to fail even if the request was actually valid.
 *
 * @author: Maria Odea Ching <oching@apache.org>
 * @version:
 * @plexus.component role="com.opensymphony.xwork2.interceptor.Interceptor" role-hint="continuumTokenSessionInterceptor"
 */
public class ContinuumTokenSessionInterceptor
    extends TokenSessionStoreInterceptor
{
    private static final String EXPLICIT_CSRF_CHECK_PARAM = "explicitCSRFCheck";

    private static final Logger logger = LoggerFactory.getLogger( ContinuumTokenSessionInterceptor.class );

    @Override
    protected String doIntercept( ActionInvocation invocation )
        throws Exception
    {
        String returnedString = super.doIntercept( invocation );

        logger.debug( "TokenSessionStoreInterceptor returned '" + returnedString + "'." );

        if ( TokenInterceptor.INVALID_TOKEN_CODE.equalsIgnoreCase( returnedString ) )
        {
            HttpServletRequest request =
                ( HttpServletRequest ) invocation.getInvocationContext().get( ServletActionContext.HTTP_REQUEST );

            String explicitCSRFCheck = request.getParameter( EXPLICIT_CSRF_CHECK_PARAM );

            if ( explicitCSRFCheck != null && Boolean.parseBoolean( explicitCSRFCheck ) )
            {
                logger.debug( "Explicit CSRF check flag set to true. Passing on the invocation.." );
                return invocation.invoke();
            }
        }

        return returnedString;
    }

}
