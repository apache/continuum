package org.apache.continuum.plugin.api;

import org.apache.continuum.plugin.api.context.ProjectInformation;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface PreUpdateSourcesPlugin
    extends Plugin
{
    void execute( ProjectInformation projectInformation );
}