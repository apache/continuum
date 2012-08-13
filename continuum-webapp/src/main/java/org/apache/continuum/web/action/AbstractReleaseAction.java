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

import org.apache.continuum.configuration.BuildAgentConfiguration;
import org.apache.continuum.configuration.BuildAgentGroupConfiguration;
import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.maven.continuum.installation.InstallationService;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.Profile;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractReleaseAction
    extends ContinuumActionSupport
{
    protected Map<String, String> getEnvironments( Profile profile, String defaultBuildagent )
    {
        if ( profile == null )
        {
            if ( defaultBuildagent != null )
            {
                return Collections.singletonMap( DistributedReleaseUtil.KEY_BUILD_AGENT_URL, defaultBuildagent );
            }
            else
            {
                return Collections.emptyMap();
            }
        }

        Map<String, String> envVars = new HashMap<String, String>();

        if ( defaultBuildagent != null && defaultBuildagent.length() > 0 )
        {
            // get buildagent to be used from the buildagent group for distributed builds setup
            BuildAgentGroupConfiguration group =
                getContinuum().getConfiguration().getBuildAgentGroup( profile.getBuildAgentGroup() );

            if ( group != null )
            {
                List<BuildAgentConfiguration> agents = group.getBuildAgents();
                if ( agents != null )
                {
                    if ( isDefaultBuildAgentEnabledInGroup( defaultBuildagent, agents ) )
                    {
                        envVars.put( DistributedReleaseUtil.KEY_BUILD_AGENT_URL, defaultBuildagent );
                    }
                    else
                    {
                        for ( BuildAgentConfiguration agent : agents )
                        {
                            if ( agent.isEnabled() == true )
                            {
                                envVars.put( DistributedReleaseUtil.KEY_BUILD_AGENT_URL, agent.getUrl() );
                                break;
                            }
                        }
                    }
                }
            }
        }

        String javaHome = getJavaHomeValue( profile );
        if ( !StringUtils.isEmpty( javaHome ) )
        {
            envVars.put( getContinuum().getInstallationService().getEnvVar( InstallationService.JDK_TYPE ), javaHome );
        }

        Installation builder = profile.getBuilder();
        if ( builder != null )
        {
            envVars.put( getContinuum().getInstallationService().getEnvVar( InstallationService.MAVEN2_TYPE ),
                         builder.getVarValue() );
        }

        List<Installation> installations = profile.getEnvironmentVariables();
        for ( Installation installation : installations )
        {
            envVars.put( installation.getVarName(), installation.getVarValue() );
        }
        return envVars;
    }

    private boolean isDefaultBuildAgentEnabledInGroup( String defaultBuildagent, List<BuildAgentConfiguration> agents )
    {
        boolean isInGroup = false;

        for ( BuildAgentConfiguration agent : agents )
        {
            if ( agent.isEnabled() == true )
            {
                if ( defaultBuildagent.equals( agent.getUrl() ) )
                {
                    isInGroup = true;
                    break;
                }
            }
        }

        return isInGroup;
    }

    private String getJavaHomeValue( Profile profile )
    {
        Installation jdk = profile.getJdk();
        if ( jdk == null )
        {
            return null;
        }
        return jdk.getVarValue();
    }
}
