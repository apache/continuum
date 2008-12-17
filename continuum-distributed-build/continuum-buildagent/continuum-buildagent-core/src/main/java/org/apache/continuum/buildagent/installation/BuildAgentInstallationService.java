package org.apache.continuum.buildagent.installation;

import org.apache.maven.continuum.execution.ExecutorConfigurator;

public interface BuildAgentInstallationService
{
    String ROLE = BuildAgentInstallationService.class.getName();

    String JDK_TYPE = "jdk";

    String MAVEN2_TYPE = "maven2";

    String MAVEN1_TYPE = "maven1";

    String ANT_TYPE = "ant";

    String ENVVAR_TYPE = "envvar";

    /**
     * @param type
     * @return ExecutorConfigurator or null if unknown type
     */
    public ExecutorConfigurator getExecutorConfigurator( String type );
}
