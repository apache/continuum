package org.apache.continuum.plugin.api.builder;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class Phase
{
    public static final Phase PRE_UPDATE_SOURCES = new Phase( "pre-update-sources" );

    public static final Phase UPDATE_SOURCES = new Phase( "update-sources" );

    public static final Phase POST_UPDATE_SOURCES = new Phase( "post-update-sources" );

    public static final Phase PRE_BUILD_PROJECT = new Phase( "pre-build-project" );

    public static final Phase BUILD_PROJECT = new Phase( "build-project" );

    public static final Phase POST_BUILD_PROJECT = new Phase( "post-build-project" );

    public static final Phase PRE_DEPLOY_ARTIFACTS = new Phase( "pre-deploy-artifacts" );

    public static final Phase DEPLOY_ARTIFACTS = new Phase( "deploy-artifacts" );

    public static final Phase POST_DEPLOY_ARTIFACTS = new Phase( "post-deploy-artifacts" );

    public static final Phase PRE_BUILD_REPORTS = new Phase( "pre-build-reports" );

    public static final Phase BUILD_REPORTS = new Phase( "build-reports" );

    public static final Phase POST_BUILD_REPORTS = new Phase( "post-build-reports" );

    public static final Phase PRE_SEND_NOTIFICATIONS = new Phase( "pre-send-notifications" );

    public static final Phase SEND_NOTIFICATIONS = new Phase( "send-notifications" );

    public static final Phase POST_SEND_NOTIFICATIONS = new Phase( "post-send-notifications" );

    public static final Phase END_BUILD = new Phase( "end-build" );

    private String name;

    private Phase( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
