package org.apache.continuum.web.action;

import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.DistributedReleaseSummary;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.redback.integration.interceptor.SecureAction;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="distributedRelease"
 */
public class DistributedReleasesAction
    extends ContinuumActionSupport
    implements SecureAction
{
    private List<DistributedReleaseSummary> releasesSummary;

    public String list()
        throws Exception
    {
        DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

        List<Map<String, Object>> releases = releaseManager.getAllReleasesInProgress();

        releasesSummary = new ArrayList<DistributedReleaseSummary>();

        for ( Map<String, Object> release : releases )
        {
            DistributedReleaseSummary summary = new DistributedReleaseSummary();
            summary.setReleaseId( DistributedReleaseUtil.getReleaseId( release ) );
            summary.setReleaseGoal( DistributedReleaseUtil.getReleaseGoal( release ) );
            summary.setBuildAgentUrl( DistributedReleaseUtil.getBuildAgentUrl( release ) );
            summary.setProjectId( DistributedReleaseUtil.getProjectId( release ) );

            releasesSummary.add( summary );
        }

        return SUCCESS;
    }

    public List<DistributedReleaseSummary> getReleasesSummary()
    {
        return releasesSummary;
    }

    public void setReleasesSummary( List<DistributedReleaseSummary> releasesSummary )
    {
        this.releasesSummary = releasesSummary;
    }

    public SecureActionBundle getSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( ContinuumRoleConstants.CONTINUUM_VIEW_RELEASE, Resource.GLOBAL );

        return bundle;
    }
}
