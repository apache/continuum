package org.apache.continuum.builder.impl;

import org.apache.continuum.model.BuildResult;
import org.apache.continuum.model.ScmResult;
import org.apache.continuum.plugin.api.BuildProjectPlugin;
import org.apache.continuum.plugin.api.BuildReportsPlugin;
import org.apache.continuum.plugin.api.EndBuildPlugin;
import org.apache.continuum.plugin.api.Plugin;
import org.apache.continuum.plugin.api.PostBuildProjectPlugin;
import org.apache.continuum.plugin.api.PostBuildReportsPlugin;
import org.apache.continuum.plugin.api.PostSendNotificationPlugin;
import org.apache.continuum.plugin.api.PostUpdateSourcesPlugin;
import org.apache.continuum.plugin.api.PreBuildProjectPlugin;
import org.apache.continuum.plugin.api.PreBuildReportsPlugin;
import org.apache.continuum.plugin.api.PreSendNotificationPlugin;
import org.apache.continuum.plugin.api.PreUpdateSourcesPlugin;
import org.apache.continuum.plugin.api.SendNotificationPlugin;
import org.apache.continuum.plugin.api.UpdateSourcesPlugin;
import org.apache.continuum.plugin.api.builder.Builder;
import org.apache.continuum.plugin.api.builder.Phase;
import org.apache.continuum.plugin.api.context.ProjectInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuilderImpl
    implements Builder
{
    private static final Logger log = LoggerFactory.getLogger( BuilderImpl.class );

    private Map<String, BuildPluginTracker> trackers;

    public BuilderImpl( Map<String, BuildPluginTracker> trackers )
    {
        this.trackers = trackers;
    }

    public void execute( int projectId, int buildDef )
    {
        log.info( "Executing build for project '" + projectId + "' with build def '" + buildDef + "'" );

        BuildContext context = new BuildContext();

        execute( Phase.PRE_UPDATE_SOURCES, context );
        execute( Phase.UPDATE_SOURCES, context );
        execute( Phase.POST_UPDATE_SOURCES, context );

        execute( Phase.PRE_BUILD_PROJECT, context );
        execute( Phase.BUILD_PROJECT, context );
        execute( Phase.POST_BUILD_PROJECT, context );

        execute( Phase.PRE_DEPLOY_ARTIFACTS, context );
        execute( Phase.DEPLOY_ARTIFACTS, context );
        execute( Phase.POST_DEPLOY_ARTIFACTS, context );

        execute( Phase.PRE_BUILD_REPORTS, context );
        execute( Phase.BUILD_REPORTS, context );
        execute( Phase.POST_BUILD_REPORTS, context );

        execute( Phase.PRE_SEND_NOTIFICATIONS, context );
        execute( Phase.SEND_NOTIFICATIONS, context );
        execute( Phase.POST_SEND_NOTIFICATIONS, context );

        execute( Phase.END_BUILD, context );
    }

    private void execute( Phase phase, BuildContext context )
    {
        log.info( "Running " + phase.getName() + " phase" );
        Object[] plugins = trackers.get( phase.getName() ).getServices();
        if ( plugins != null && plugins.length > 0 )
        {
            for ( Object obj : plugins )
            {
                Plugin p = (Plugin) obj;

                if ( p instanceof PreUpdateSourcesPlugin )
                {
                    ( (PreUpdateSourcesPlugin) p ).execute( context.getProjectInformation() );
                }
                else if ( p instanceof UpdateSourcesPlugin )
                {
                    ( (UpdateSourcesPlugin) p ).execute( context.getProjectInformation(), context.getScmResult() );
                }
                else if ( p instanceof PostUpdateSourcesPlugin )
                {
                    ( (PostUpdateSourcesPlugin) p ).execute( context.getProjectInformation(), context.getScmResult() );
                }
                else if ( p instanceof PreBuildProjectPlugin )
                {
                    ( (PreBuildProjectPlugin) p ).execute( context.getProjectInformation(), context.getScmResult() );
                }
                else if ( p instanceof BuildProjectPlugin )
                {
                    ( (BuildProjectPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                        context.getBuildResult() );
                }
                else if ( p instanceof PostBuildProjectPlugin )
                {
                    ( (PostBuildProjectPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                            context.getBuildResult() );
                }
                else if ( p instanceof PreBuildReportsPlugin )
                {
                    ( (PreBuildReportsPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                           context.getBuildResult() );
                }
                else if ( p instanceof BuildReportsPlugin )
                {
                    ( (BuildReportsPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                        context.getBuildResult() );
                }
                else if ( p instanceof PostBuildReportsPlugin )
                {
                    ( (PostBuildReportsPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                            context.getBuildResult() );
                }
                else if ( p instanceof PreSendNotificationPlugin )
                {
                    ( (PreSendNotificationPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                               context.getBuildResult() );
                }
                else if ( p instanceof SendNotificationPlugin )
                {
                    ( (SendNotificationPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                            context.getBuildResult() );
                }
                else if ( p instanceof PostSendNotificationPlugin )
                {
                    ( (PostSendNotificationPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                                context.getBuildResult() );
                }
                else if ( p instanceof EndBuildPlugin )
                {
                    ( (EndBuildPlugin) p ).execute( context.getProjectInformation(), context.getScmResult(),
                                                    context.getBuildResult() );
                }
            }
        }
    }

    private class BuildContext
    {
        private ProjectInformation projectInfo = new ProjectInformation();

        private ScmResult scmResult = new ScmResult();

        private BuildResult buildResult = new BuildResult();

        public ProjectInformation getProjectInformation()
        {
            return projectInfo;
        }

        public void setProjectInformation( ProjectInformation projectInfo )
        {
            this.projectInfo = projectInfo;
        }

        public ScmResult getScmResult()
        {
            return scmResult;
        }

        public void setScmResult( ScmResult scmResult )
        {
            this.scmResult = scmResult;
        }

        public BuildResult getBuildResult()
        {
            return buildResult;
        }

        public void setBuildResult( BuildResult buildResult )
        {
            this.buildResult = buildResult;
        }
    }
}
