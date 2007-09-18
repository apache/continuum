package org.apache.maven.continuum.web.util;

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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class WorkingCopyContentGenerator
    extends AbstractLogEnabled
{
    private File basedir;

    private String urlParamSeparator;

    private static DecimalFormat decFormatter = new DecimalFormat( "###.##" );

    private static final long KILO = 1024;

    private static final long MEGA = 1024 * KILO;

    private static final long GIGA = 1024 * MEGA;

    private boolean odd = false;

    public String generate( Object item, String baseUrl, String imagesBaseUrl, File basedir )
    {
        this.basedir = basedir;
        if ( baseUrl.indexOf( "?" ) > 0 )
        {
            urlParamSeparator = "&";
        }
        else
        {
            urlParamSeparator = "?";
        }

        List directoryEntries = (List) item;

        StringBuffer buf = new StringBuffer();

        buf.append( "<div class=\"eXtremeTable\" >" );
        buf.append( "<table class=\"tableRegion\" width=\"100%\">\n" );

        buf.append( "<tr class=\"odd\"><td><img src=\"" + imagesBaseUrl +
            "icon_arrowfolder1_sml.gif\">&nbsp;<a href=\"" + baseUrl + urlParamSeparator +
            "userDirectory=/\">/</a><br /></td><td>&nbsp;</td><td>&nbsp;</td>" );

        print( directoryEntries, "&nbsp;&nbsp;", baseUrl, imagesBaseUrl, buf );

        buf.append( "</table>\n" );
        buf.append( "</div>\n" );

        return buf.toString();
    }

    private void print( List dirs, String indent, String baseUrl, String imagesBaseUrl, StringBuffer buf )
    {
        for ( Iterator i = dirs.iterator(); i.hasNext(); )
        {
            Object obj = i.next();

            print( obj, indent, baseUrl, imagesBaseUrl, buf );
        }
    }

    private void print( Object obj, String indent, String baseUrl, String imagesBaseUrl, StringBuffer buf )
    {
        if ( obj instanceof File )
        {
            String cssClass = odd ? "odd" : "even";

            File f = (File) obj;

            if ( !f.isDirectory() )
            {
                String fileName = f.getName();

                if ( !".cvsignore".equals( fileName ) && !"vssver.scc".equals( fileName ) &&
                    !".DS_Store".equals( fileName ) )
                {
                    String userDirectory = null;

                    if ( f.getParentFile().getAbsolutePath().equals( basedir.getAbsolutePath() ) )
                    {
                        userDirectory = "/";
                    }
                    else
                    {
                        userDirectory =
                            f.getParentFile().getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );
                    }

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( "<tr class=\"" + cssClass + "\">" );

                    buf.append( "<td width=\"98%\">" + indent + "&nbsp;&nbsp;<img src=\"" + imagesBaseUrl +
                        "file.gif\">&nbsp;<a href=\"" + baseUrl + urlParamSeparator + "userDirectory=" + userDirectory +
                        "&file=" + fileName + "\">" + fileName + "</a></td><td width=\"1%\">" +
                        getReadableFileSize( f.length() ) + "</td><td width=\"1%\">" +
                        getFormattedDate( f.lastModified() ) + "</td>\n" );
                    buf.append( "</tr>\n" );

                    odd = !odd;
                }
            }
            else
            {
                String directoryName = f.getName();

                if ( !"CVS".equals( directoryName ) && !".svn".equals( directoryName ) &&
                    !"SCCS".equals( directoryName ) )
                {
                    String userDirectory = f.getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( "<tr class=\"" + cssClass + "\">" );

                    buf.append( "<td width=\"98%\">" + indent + " <img src=\"" + imagesBaseUrl +
                        "icon_arrowfolder1_sml.gif\"> &nbsp;<a href =\"" + baseUrl + urlParamSeparator +
                        "userDirectory=" + userDirectory + "\">" + directoryName + "</a></td><td width=\"1%\">" +
                        "&nbsp;" + "</td><td width=\"1%\">" + getFormattedDate( f.lastModified() ) + "</td>" );
                    buf.append( "</tr>\n" );

                    odd = !odd;
                }
            }
        }
        else
        {
            print( (List) obj, indent + "&nbsp;&nbsp;", baseUrl, imagesBaseUrl, buf );
        }
    }

    private String getFormattedDate( long timestamp )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( timestamp );
        Date date = cal.getTime();
        String res = new SimpleDateFormat( "dd-MMM-yyyy, K:mm a, z" ).format( date );
        return StringUtils.replace( res, " ", "&nbsp;" );
    }

    private static String getReadableFileSize( long fileSizeInBytes )
    {
        if ( fileSizeInBytes >= GIGA )
        {
            return decFormatter.format( fileSizeInBytes / GIGA ) + "&nbsp;Gb";
        }
        else if ( fileSizeInBytes >= MEGA )
        {
            return decFormatter.format( fileSizeInBytes / MEGA ) + "&nbsp;Mb";
        }
        else if ( fileSizeInBytes >= KILO )
        {
            return decFormatter.format( fileSizeInBytes / KILO ) + "&nbsp;Kb";
        }
        else if ( fileSizeInBytes > 0 && fileSizeInBytes < KILO )
        {
            return decFormatter.format( fileSizeInBytes ) + " b";
        }

        return "0 B";
    }
}