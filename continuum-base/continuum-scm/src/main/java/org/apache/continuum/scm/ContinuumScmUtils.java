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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version
 */
public class ContinuumScmUtils
{
    public static final String GIT_SCM_PROVIDERTYPE = "git";

    // CONTINUUM-2628
    public static ContinuumScmConfiguration setSCMCredentialsforSSH( ContinuumScmConfiguration config,
                             String scmUrl, String scmUsername, String scmPassword )
    {
        String sshScmUsername = "";
        String sshScmPassword = "";
        String providerType = ScmUrlUtils.getProvider( scmUrl );

        String scmSpecificUrl = scmUrl.substring( providerType.length() + 5 );

        if( providerType.contains( GIT_SCM_PROVIDERTYPE ) && scmSpecificUrl.startsWith( GitScmProviderRepository.PROTOCOL_SSH ) )
        {
            scmSpecificUrl = scmSpecificUrl.substring( GitScmProviderRepository.PROTOCOL_SSH.length() + 3 );

            // extract user information
            int indexAt = scmSpecificUrl.indexOf( "@" );
            if ( indexAt >= 0 )
            {
                String userInfo = scmSpecificUrl.substring( 0, indexAt );
                sshScmUsername = userInfo;
                int indexPwdSep = userInfo.indexOf( ":" );
                // password is specified in the url
                if ( indexPwdSep < 0 )
                {
                    sshScmUsername = userInfo.substring( indexPwdSep + 1);
                }
                else
                {
                    sshScmUsername = userInfo.substring( 0, indexPwdSep );
                    sshScmPassword = userInfo.substring( indexPwdSep + 1 );
                }
            }
        }

        if( StringUtils.isBlank( sshScmUsername ) )
        {
            config.setUsername( scmUsername );
            config.setPassword( scmPassword );
        }
        else
        {
            config.setUsername( sshScmUsername );
            if( !StringUtils.isBlank( sshScmPassword ) )
            {
                config.setPassword( sshScmPassword );
            }
        }

        return config;
    }
}
