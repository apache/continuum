package org.apache.continuum.buildagent.installation;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.execution.ExecutorConfigurator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @plexus.component role="org.apache.continuum.buildagent.installation.BuildAgentInstallationService"
 */
public class DefaultBuildAgentInstallationService
    implements BuildAgentInstallationService, Initializable
{
    private Map<String, ExecutorConfigurator> typesValues;

    public ExecutorConfigurator getExecutorConfigurator( String type )
    {
        return this.typesValues.get( type );
    }

    public void initialize()
        throws InitializationException
    {
        this.typesValues = new HashMap<String, ExecutorConfigurator>();
        this.typesValues.put( BuildAgentInstallationService.ANT_TYPE,
                              new ExecutorConfigurator( "ant", "bin", "ANT_HOME", "-version" ) );

        this.typesValues.put( BuildAgentInstallationService.ENVVAR_TYPE, null );
        this.typesValues.put( BuildAgentInstallationService.JDK_TYPE,
                              new ExecutorConfigurator( "java", "bin", "JAVA_HOME", "-version" ) );
        this.typesValues.put( BuildAgentInstallationService.MAVEN1_TYPE,
                              new ExecutorConfigurator( "maven", "bin", "MAVEN_HOME", "-v" ) );
        this.typesValues
            .put( BuildAgentInstallationService.MAVEN2_TYPE, new ExecutorConfigurator( "mvn", "bin", "M2_HOME", "-v" ) );
    }

    public String getEnvVar( String type )
    {
        ExecutorConfigurator executorConfigurator = this.typesValues.get( type );
        return executorConfigurator == null ? null : executorConfigurator.getEnvVar();
    }
}
