package org.apache.continuum.scm.queue;

import java.util.Map;

import org.codehaus.plexus.taskqueue.Task;

public class PrepareBuildProjectsTask
    implements Task
{
    private Map<Integer, Integer> projectsBuildDefinitionsMap;

    private int trigger;

    public PrepareBuildProjectsTask( Map<Integer, Integer> projectsBuildDefinitionsMap, int trigger )
    {
        this.projectsBuildDefinitionsMap = projectsBuildDefinitionsMap;
        this.trigger = trigger;
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

    public int getTrigger()
    {
        return trigger;
    }

    public void setTrigger( int trigger )
    {
        this.trigger = trigger;
    }
    
    public int getHashCode()
    {
        return this.hashCode();
    }    
}
