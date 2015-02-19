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

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class WorkingCopyContentGenerator
{
    private File basedir;

    private String urlParamSeparator;

    private static final DecimalFormat decFormatter = new DecimalFormat( "###.##" );

    private static final long KILO = 1024;

    private static final long MEGA = 1024 * KILO;

    private static final long GIGA = 1024 * MEGA;

    private boolean odd = false;

    public String generate( List<File> files, String baseUrl, String imagesBaseUrl, File basedir )
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

        StringBuffer buf = new StringBuffer();

        buf.append( "<div class=\"eXtremeTable\" >" );
        buf.append( "<table class=\"tableRegion\" width=\"100%\">\n" );

        buf.append( "<tr class=\"odd\"><td><img src=\"" ).append( imagesBaseUrl ).append(
            "icon_arrowfolder1_sml.gif\">&nbsp;<a href=\"" ).append( baseUrl ).append( urlParamSeparator ).append(
            "userDirectory=/\">/</a><br /></td><td>&nbsp;</td><td>&nbsp;</td>" );

        print( basedir, files, baseUrl, imagesBaseUrl, buf );

        buf.append( "</table>\n" );
        buf.append( "</div>\n" );

        return buf.toString();
    }

    private void print( File basedir, List<File> files, String baseUrl, String imagesBaseUrl, StringBuffer buf )
    {
        for ( File f : files )
        {
            print( f, getIndent( basedir, f ), baseUrl, imagesBaseUrl, buf );
        }
    }

    private void print( File f, String indent, String baseUrl, String imagesBaseUrl, StringBuffer buf )
    {
        String cssClass = odd ? "odd" : "even";

        if ( !f.isDirectory() )
        {
            String fileName = f.getName();

            if ( !".cvsignore".equals( fileName ) && !"vssver.scc".equals( fileName ) &&
                !".DS_Store".equals( fileName ) && !"release.properties".equals( fileName ) )
            {
                String userDirectory;

                if ( f.getParentFile().getAbsolutePath().equals( basedir.getAbsolutePath() ) )
                {
                    userDirectory = "/";
                }
                else
                {
                    userDirectory = f.getParentFile().getAbsolutePath().substring(
                        basedir.getAbsolutePath().length() + 1 );
                }

                userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                buf.append( "<tr class=\"" ).append( cssClass ).append( "\">" );

                buf.append( "<td width=\"98%\">" ).append( indent ).append( "&nbsp;&nbsp;<img src=\"" ).append(
                    imagesBaseUrl ).append( "file.gif\">&nbsp;<a href=\"" ).append( baseUrl ).append(
                    urlParamSeparator ).append( "userDirectory=" ).append( userDirectory ).append( "&file=" ).append(
                    fileName ).append( "\">" ).append( fileName ).append( "</a></td><td width=\"1%\">" ).append(
                    getReadableFileSize( f.length() ) ).append( "</td><td width=\"1%\">" ).append( getFormattedDate(
                    f.lastModified() ) ).append( "</td>\n" );
                buf.append( "</tr>\n" );

                odd = !odd;
            }
        }
        else
        {
            String directoryName = f.getName();

            if ( !"CVS".equals( directoryName ) && !".svn".equals( directoryName ) && !"SCCS".equals( directoryName ) )
            {
                String userDirectory = f.getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );

                userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                buf.append( "<tr class=\"" ).append( cssClass ).append( "\">" );

                buf.append( "<td width=\"98%\">" ).append( indent ).append( "<img src=\"" ).append(
                    imagesBaseUrl ).append( "icon_arrowfolder1_sml.gif\">&nbsp;<a href =\"" ).append( baseUrl ).append(
                    urlParamSeparator ).append( "userDirectory=" ).append( userDirectory ).append( "\">" ).append(
                    directoryName ).append( "</a></td><td width=\"1%\">" + "&nbsp;" + "</td><td width=\"1%\">" ).append(
                    getFormattedDate( f.lastModified() ) ).append( "</td>" );
                buf.append( "</tr>\n" );

                odd = !odd;
            }
        }
    }

    private String getFormattedDate( long timestamp )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( timestamp );
        Date date = cal.getTime();
        String res = new SimpleDateFormat( "MMM dd, yyyy hh:mm:ss aaa z" ).format( date );
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
            return decFormatter.format( fileSizeInBytes ) + "&nbsp;b";
        }

        return "0&nbsp;b";
    }

    private String getIndent( File basedir, File userFile )
    {
        String root = basedir.getAbsolutePath();
        String userdir;
        if ( userFile.isDirectory() )
        {
            userdir = userFile.getAbsolutePath();
        }
        else
        {
            userdir = userFile.getParentFile().getAbsolutePath();
        }

        userdir = userdir.substring( root.length() );

        StringBuffer indent = new StringBuffer();
        while ( userdir.indexOf( File.separator ) >= 0 )
        {
            indent.append( "&nbsp;&nbsp;" );
            userdir = userdir.substring( userdir.indexOf( File.separator ) + 1 );
        }
        return indent.toString();
    }
}