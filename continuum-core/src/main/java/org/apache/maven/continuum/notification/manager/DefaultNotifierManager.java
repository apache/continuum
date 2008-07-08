package org.apache.maven.continuum.notification.manager;

import org.apache.maven.continuum.notification.Notifier;

import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultNotifierManager
    implements NotifierManager
{
    private Map<String, Notifier> notifiers;

    public Notifier getNotifier( String notifierId )
    {
        return notifiers.get( notifierId );
    }

    public Map getNotifiers()
    {
        return notifiers;
    }

    public void setNotifiers( Map notifiers )
    {
        this.notifiers = notifiers;
    }
}
