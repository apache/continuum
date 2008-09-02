package org.apache.maven.continuum.scm.queue;

import java.util.Map;

import org.codehaus.plexus.taskqueue.Task;

public class PrepareBuildProjectsTask
    implements Task
{
    private Map<Integer, Integer> projectsBuildDefinitionsMap;

    public PrepareBuildProjectsTask( Map<Integer, Integer> projectsBuildDefinitionsMap )
    {
        this.projectsBuildDefinitionsMap = projectsBuildDefinitionsMap;
    }
    
    public long getMaxExecutionTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public Map<Integer, Integer> getProjectsBuildDefinitionsMap()
    {
        return projectsBuildDefinitionsMap;
    }
    
    public void setProjectsBuildDefinitionsMap( Map<Integer, Integer> projectsBuildDefinitionsMap )
    {
        this.projectsBuildDefinitionsMap = projectsBuildDefinitionsMap;
    }
    
    public int getHashCode()
    {
        return this.hashCode();
    }    
}
