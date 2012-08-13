package org.apache.continuum.buildagent.manager;


public interface BuildAgentPurgeManager
{
    String ROLE = BuildAgentPurgeManager.class.getName();

    void executeDirectoryPurge( String directoryType, int daysOlder, int retentionCount, boolean deleteAll )
        throws Exception;
}
