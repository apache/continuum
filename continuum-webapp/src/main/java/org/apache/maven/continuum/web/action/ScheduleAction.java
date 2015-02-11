package org.apache.maven.continuum.web.action;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.continuum.web.util.AuditLog;
import org.apache.continuum.web.util.AuditLogConstants;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.BuildQueue;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.web.exception.AuthenticationRequiredException;
import org.apache.maven.continuum.web.exception.AuthorizationRequiredException;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Nik Gonzalez
 */
@Component( role = com.opensymphony.xwork2.Action.class, hint = "schedule", instantiationStrategy = "per-lookup"  )
public class ScheduleAction
    extends ContinuumConfirmAction
    implements Preparable
{
    private static final Logger logger = LoggerFactory.getLogger( ScheduleAction.class );

    private int id;

    private boolean active = false;

    private int delay;

    private String description;

    private String name;

    private Collection schedules;

    private Schedule schedule;

    private boolean confirmed;

    private int maxJobExecutionTime;

    private String second = "0";

    private String minute = "0";

    private String hour = "*";

    private String dayOfMonth = "*";

    private String month = "*";

    private String dayOfWeek = "?";

    private String year;

    private List<BuildQueue> availableBuildQueues;

    private List<BuildQueue> selectedBuildQueues = new ArrayList<BuildQueue>();

    private List<String> selectedBuildQueuesIds = new ArrayList<String>();

    public void prepare()
        throws Exception
    {
        super.prepare();

        populateBuildQueues();
    }

    private void populateBuildQueues()
        throws ContinuumException
    {
        if ( schedule != null )
        {
            selectedBuildQueues = schedule.getBuildQueues();
            for ( BuildQueue bq : selectedBuildQueues )
            {
                this.selectedBuildQueuesIds.add( Integer.toString( bq.getId() ) );
            }
        }

        availableBuildQueues = getContinuum().getAllBuildQueues();

        // remove selected build queues from available build queues
        for ( BuildQueue buildQueue : selectedBuildQueues )
        {
            if ( availableBuildQueues.contains( buildQueue ) )
            {
                availableBuildQueues.remove( buildQueue );
            }
        }
    }

    public String summary()
        throws ContinuumException
    {
        try
        {
            checkManageSchedulesAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;

        }

        schedules = getContinuum().getSchedules();

        return SUCCESS;
    }

    public String input()
        throws ContinuumException
    {

        try
        {
            checkManageSchedulesAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        if ( id != 0 )
        {
            schedule = getContinuum().getSchedule( id );
            active = schedule.isActive();

            String[] cronEx = schedule.getCronExpression().split( " " );
            second = cronEx[0];
            minute = cronEx[1];
            hour = cronEx[2];
            dayOfMonth = cronEx[3];
            month = cronEx[4];
            dayOfWeek = cronEx[5];
            if ( cronEx.length > 6 )
            {
                year = cronEx[6];
            }

            description = schedule.getDescription();
            name = schedule.getName();
            delay = schedule.getDelay();
            maxJobExecutionTime = schedule.getMaxJobExecutionTime();

            populateBuildQueues();
        }
        else
        {
            // all new schedules should be active
            active = true;
        }

        return SUCCESS;
    }

    public String save()
        throws ContinuumException
    {

        try
        {
            checkManageSchedulesAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        if ( StringUtils.isBlank( name ) )
        {
            logger.error( "Can't create schedule. No schedule name was supplied." );
            addActionError( getText( "buildDefinition.noname.save.error.message" ) );
        }
        if ( !getContinuum().getConfiguration().isDistributedBuildEnabled() &&
            ( selectedBuildQueuesIds == null || selectedBuildQueuesIds.isEmpty() ) )
        {
            addActionError( getText( "schedule.buildqueues.empty.error" ) );
        }
        if ( hasErrors() )
        {
            return ERROR;
        }

        try
        {
            Schedule s = getContinuum().getScheduleByName( name );
            if ( s != null && id != s.getId() )
            {
                addActionError( getText( "schedule.name.already.exists" ) );
                return ERROR;
            }
        }
        catch ( ContinuumException e )
        {
            logger.debug( "Unexpected error getting schedule" );
        }

        AuditLog event = new AuditLog( getName(), AuditLogConstants.ADD_SCHEDULE );
        event.setCategory( AuditLogConstants.SCHEDULE );
        event.setCurrentUser( getPrincipal() );

        if ( id == 0 )
        {
            try
            {
                getContinuum().addSchedule( setFields( new Schedule() ) );
                event.log();
            }
            catch ( ContinuumException e )
            {
                addActionError( getText( "schedule.buildqueues.add.error" ) );
                return ERROR;
            }
            return SUCCESS;
        }
        else
        {
            try
            {
                getContinuum().updateSchedule( setFields( getContinuum().getSchedule( id ) ) );
                event.setAction( AuditLogConstants.MODIFY_SCHEDULE );
                event.log();
            }
            catch ( ContinuumException e )
            {
                addActionError( getText( "schedule.buildqueues.add.error" ) );
                return ERROR;
            }
            return SUCCESS;
        }
    }

    private Schedule setFields( Schedule schedule )
        throws ContinuumException
    {
        schedule.setActive( active );
        schedule.setCronExpression( getCronExpression() );
        schedule.setDelay( delay );
        schedule.setDescription( StringEscapeUtils.escapeXml( StringEscapeUtils.unescapeXml( description ) ) );
        schedule.setName( name );
        schedule.setMaxJobExecutionTime( maxJobExecutionTime );
        if ( !getContinuum().getConfiguration().isDistributedBuildEnabled() )
        {
            // if distributed build don't update schedules
            schedule.setBuildQueues( null );
            for ( String id : selectedBuildQueuesIds )
            {
                BuildQueue buildQueue = getContinuum().getBuildQueue( Integer.parseInt( id ) );
                schedule.addBuildQueue( buildQueue );
            }
        }

        return schedule;
    }

    public String confirm()
        throws ContinuumException
    {
        try
        {
            checkManageSchedulesAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        schedule = getContinuum().getSchedule( id );

        return SUCCESS;
    }

    public String remove()
        throws ContinuumException
    {
        try
        {
            checkManageSchedulesAuthorization();
        }
        catch ( AuthorizationRequiredException authzE )
        {
            addActionError( authzE.getMessage() );
            return REQUIRES_AUTHORIZATION;
        }
        catch ( AuthenticationRequiredException e )
        {
            addActionError( e.getMessage() );
            return REQUIRES_AUTHENTICATION;
        }

        if ( confirmed )
        {
            try
            {
                getContinuum().removeSchedule( id );
            }
            catch ( ContinuumException e )
            {
                addActionError( getText( "schedule.remove.error" ) );
                return ERROR;
            }
        }
        else
        {
            setConfirmationInfo( "Schedule Removal", "removeSchedule", name, "id", "" + id );

            name = getContinuum().getSchedule( id ).getName();

            return CONFIRM;
        }

        AuditLog event = new AuditLog( name, AuditLogConstants.REMOVE_SCHEDULE );
        event.setCategory( AuditLogConstants.SCHEDULE );
        event.setCurrentUser( getPrincipal() );
        event.log();

        return SUCCESS;
    }

    public Collection getSchedules()
    {
        return schedules;
    }

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public int getDelay()
    {
        return delay;
    }

    public void setDelay( int delay )
    {
        this.delay = delay;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule( Schedule schedule )
    {
        this.schedule = schedule;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed( boolean confirmed )
    {
        this.confirmed = confirmed;
    }

    public int getMaxJobExecutionTime()
    {
        return maxJobExecutionTime;
    }

    public void setMaxJobExecutionTime( int maxJobExecutionTime )
    {
        this.maxJobExecutionTime = maxJobExecutionTime;
    }

    public String getSecond()
    {
        return second;
    }

    public void setSecond( String second )
    {
        this.second = second;
    }

    public String getMinute()
    {
        return minute;
    }

    public void setMinute( String minute )
    {
        this.minute = minute;
    }

    public String getHour()
    {
        return hour;
    }

    public void setHour( String hour )
    {
        this.hour = hour;
    }

    public String getDayOfMonth()
    {
        return dayOfMonth;
    }

    public void setDayOfMonth( String dayOfMonth )
    {
        this.dayOfMonth = dayOfMonth;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear( String year )
    {
        this.year = year;
    }

    public String getMonth()
    {
        return month;
    }

    public void setMonth( String month )
    {
        this.month = month;
    }

    public String getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek( String dayOfWeek )
    {
        this.dayOfWeek = dayOfWeek;
    }

    private String getCronExpression()
    {
        return ( second + " " + minute + " " + hour + " " + dayOfMonth + " " + month + " " + dayOfWeek + " " +
            year ).trim();
    }

    public List<BuildQueue> getAvailableBuildQueues()
    {
        return availableBuildQueues;
    }

    public void setAvailableBuildQueues( List<BuildQueue> availableBuildQueues )
    {
        this.availableBuildQueues = availableBuildQueues;
    }

    public List<BuildQueue> getSelectedBuildQueues()
    {
        return selectedBuildQueues;
    }

    public void setSelectedBuildQueues( List<BuildQueue> selectedBuildQueues )
    {
        this.selectedBuildQueues = selectedBuildQueues;
    }

    public List<String> getSelectedBuildQueuesIds()
    {
        return selectedBuildQueuesIds;
    }

    public void setSelectedBuildQueuesIds( List<String> selectedBuildQueuesIds )
    {
        this.selectedBuildQueuesIds = selectedBuildQueuesIds;
    }
}
