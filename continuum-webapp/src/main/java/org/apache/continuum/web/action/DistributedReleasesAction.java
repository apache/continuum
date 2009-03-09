package org.apache.continuum.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.continuum.release.distributed.DistributedReleaseUtil;
import org.apache.continuum.release.distributed.manager.DistributedReleaseManager;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.DistributedReleaseSummary;
/**
 * @plexus.component role="com.opensymphony.xwork2.Action" role-hint="distributedRelease"
 */
public class DistributedReleasesAction
    extends ContinuumActionSupport
{
    private List<DistributedReleaseSummary> releasesSummary;

    public String list()
        throws Exception
    {
        DistributedReleaseManager releaseManager = getContinuum().getDistributedReleaseManager();

        List<Map> releases = releaseManager.getAllReleasesInProgress();

        releasesSummary = new ArrayList<DistributedReleaseSummary>();

        for ( Map release : releases )
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
}
