package org.apache.continuum.builder.distributed.stubs;

import org.apache.continuum.builder.distributed.manager.DefaultDistributedBuildManager;
import org.apache.continuum.distributed.transport.slave.SlaveBuildAgentTransportService;

public class DefaultDistributedBuildManagerStub
    extends DefaultDistributedBuildManager
{
    @Override
    public SlaveBuildAgentTransportService createSlaveBuildAgentTransportClientConnection( String buildAgentUrl )
    {
        return new SlaveBuildAgentTransportClientStub();
    }
}
