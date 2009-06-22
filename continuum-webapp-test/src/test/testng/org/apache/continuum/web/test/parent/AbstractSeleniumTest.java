package org.apache.continuum.web.test.parent;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.lang.Object;
import java.lang.Throwable;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.testng.Assert;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Based on AbstractSeleniumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractSeleniumTest
{
    public static String baseUrl;

    public static String maxWaitTimeInMs;

    private static ThreadLocal<Selenium> selenium;

    private static Properties p;

    private final static String PROPERTIES_SEPARATOR = "=";

    /**
     * Initialize selenium an others properties. This method is called from BeforeSuite method of sub-class.
     */
    public void open()
        throws Exception
    {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream( "testng.properties" );
        p = new Properties();
        p.load( input );

        baseUrl = getProperty( "BASE_URL" );
        maxWaitTimeInMs = getProperty( "MAX_WAIT_TIME_IN_MS" );

        String seleniumHost = getProperty( "SELENIUM_HOST" );
        int seleniumPort = Integer.parseInt( ( getProperty( "SELENIUM_PORT" ) ) );

        String seleniumBrowser = System.getProperty( "browser" );
        if ( StringUtils.isEmpty( seleniumBrowser ) )
        {
            seleniumBrowser = getProperty( "SELENIUM_BROWSER" );
        }

        final Selenium s = new DefaultSelenium( seleniumHost, seleniumPort, seleniumBrowser, baseUrl );
        selenium = new ThreadLocal<Selenium>()
        {
            @Override
            protected Selenium initialValue()
            {
                return s;
            }
        };
        getSelenium().start();
    }

    protected static Selenium getSelenium()
    {
        return selenium.get();
    }

    protected String getProperty( String key )
    {
        return p.getProperty( key );
    }

    protected String getEscapeProperty( String key )
    {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream( "testng.properties" );
        String value = null;
        List<String> lines;
        try
        {
            lines = IOUtils.readLines( input );
        }
        catch ( IOException e )
        {
            lines = new ArrayList<String>();
        }
        for ( String l : lines )
        {
            if ( l != null && l.startsWith( key ) )
            {
                int indexSeparator = l.indexOf( PROPERTIES_SEPARATOR );
                value = l.substring( indexSeparator + 1 ).trim();
                break;
            }
        }
        return value;
    }

    /**
     * Close selenium session. Called from AfterSuite method of sub-class
     */
    public void close()
        throws Exception
    {
        getSelenium().stop();
    }

    // *******************************************************
    // Auxiliar methods. This method help us and simplify test.
    // *******************************************************

    public void assertFieldValue( String fieldValue, String fieldName )
    {
        assertElementPresent( fieldName );
        try
        {
            Assert.assertEquals( fieldValue, getSelenium().getValue( fieldName ) );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertPage( String title )
    {
        try
        {
            Assert.assertEquals( getTitle(), title );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public String getTitle()
    {
        return getSelenium().getTitle();
    }

    public String getHtmlContent()
    {
        return getSelenium().getHtmlSource();
    }

    public void assertTextPresent( String text )
    {
        try
        {
            Assert.assertTrue( getSelenium().isTextPresent( text ), "'" + text + "' isn't present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertTextNotPresent( String text )
    {
        try
        {
            Assert.assertFalse( getSelenium().isTextPresent( text ), "'" + text + "' is present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertElementPresent( String elementLocator )
    {
        try
        {
            Assert.assertTrue( isElementPresent( elementLocator ), "'" + elementLocator + "' isn't present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertElementNotPresent( String elementLocator )
    {
        try
        {
            Assert.assertFalse( isElementPresent( elementLocator ), "'" + elementLocator + "' is present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertLinkPresent( String text )
    {
        try
        {
            Assert.assertTrue( isElementPresent( "link=" + text ), "The link '" + text + "' isn't present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertLinkNotPresent( String text )
    {
        try
        {
            Assert.assertFalse( isElementPresent( "link=" + text ), "The link '" + text + "' is present." );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertImgWithAlt( String alt )
    {
        assertElementPresent( "//img[@alt='" + alt + "']" );
    }

    public void assertImgWithAltAtRowCol( boolean isALink, String alt, int row, int column )
    {
        String locator = "//tr[" + row + "]/td[" + column + "]/";
        locator += isALink ? "a/" : "";
        locator += "img[@alt='" + alt + "']";

        assertElementPresent( locator );
    }

    public void assertCellValueFromTable( String expected, String tableElement, int row, int column )
    {
        try
        {
            Assert.assertEquals( expected, getCellValueFromTable( tableElement, row, column ) );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public boolean isTextPresent( String text )
    {
        return getSelenium().isTextPresent( text );
    }

    public boolean isLinkPresent( String text )
    {
        return isElementPresent( "link=" + text );
    }

    public boolean isElementPresent( String locator )
    {
        return getSelenium().isElementPresent( locator );
    }

    public void waitPage()
    {
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    public String getFieldValue( String fieldName )
    {
        return getSelenium().getValue( fieldName );
    }

    public String getCellValueFromTable( String tableElement, int row, int column )
    {
        return getSelenium().getTable( tableElement + "." + row + "." + column );
    }

    public void selectValue( String locator, String value )
    {
        getSelenium().select( locator, "label=" + value );
    }

    public void assertOptionPresent( String selectField, String[] options )
    {
        assertElementPresent( selectField );
        String[] optionsPresent = getSelenium().getSelectOptions( selectField );
        List<String> expected = Arrays.asList( options );
        List<String> present = Arrays.asList( optionsPresent );
        try
        {
            Assert.assertTrue( present.containsAll( expected ), "Options expected are not included in present options" );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertSelectedValue( String value, String fieldName )
    {
        assertElementPresent( fieldName );
        String optionsPresent = getSelenium().getSelectedLabel( value );
        try
        {
            Assert.assertEquals( optionsPresent, value );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void submit()
    {
        clickLinkWithXPath( "//input[@type='submit']" );
    }

    public void assertButtonWithValuePresent( String text )
    {
        try
        {
            Assert.assertTrue( isButtonWithValuePresent( text ), "'" + text + "' button isn't present" );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertButtonWithIdPresent( String id )
    {
        try
        {
            Assert.assertTrue( isButtonWithIdPresent( id ), "'Button with id =" + id + "' isn't present" );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertButtonWithValueNotPresent( String text )
    {
        try
        {
            Assert.assertFalse( isButtonWithValuePresent( text ), "'" + text + "' button is present" );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public boolean isButtonWithValuePresent( String text )
    {
        return isElementPresent( "//button[@value='" + text + "']" )
            || isElementPresent( "//input[@value='" + text + "']" );
    }

    public boolean isButtonWithIdPresent( String text )
    {
        return isElementPresent( "//button[@id='" + text + "']" ) || isElementPresent( "//input[@id='" + text + "']" );
    }

    public void clickButtonWithValue( String text )
    {
        clickButtonWithValue( text, true );
    }

    public void clickButtonWithValue( String text, boolean wait )
    {
        assertButtonWithValuePresent( text );

        if ( isElementPresent( "//button[@value='" + text + "']" ) )
        {
            clickLinkWithXPath( "//button[@value='" + text + "']", wait );
        }
        else
        {
            clickLinkWithXPath( "//input[@value='" + text + "']", wait );
        }
    }

    public void clickSubmitWithLocator( String locator )
    {
        clickLinkWithLocator( locator );
    }

    public void clickSubmitWithLocator( String locator, boolean wait )
    {
        clickLinkWithLocator( locator, wait );
    }

    public void clickImgWithAlt( String alt )
    {
        clickLinkWithLocator( "//img[@alt='" + alt + "']" );
    }

    public void clickLinkWithText( String text )
    {
        clickLinkWithText( text, true );
    }

    public void clickLinkWithText( String text, boolean wait )
    {
        clickLinkWithLocator( "link=" + text, wait );
    }

    public void clickLinkWithXPath( String xpath )
    {
        clickLinkWithXPath( xpath, true );
    }

    public void clickLinkWithXPath( String xpath, boolean wait )
    {
        clickLinkWithLocator( "xpath=" + xpath, wait );
    }

    public void clickLinkWithLocator( String locator )
    {
        clickLinkWithLocator( locator, true );
    }

    public void clickLinkWithLocator( String locator, boolean wait )
    {
        assertElementPresent( locator );
        getSelenium().click( locator );
        if ( wait )
        {
            waitPage();
        }
    }

    public void setFieldValues( Map<String, String> fieldMap )
    {
        Map.Entry<String, String> entry;

        for ( Iterator<Entry<String, String>> entries = fieldMap.entrySet().iterator(); entries.hasNext(); )
        {
            entry = entries.next();

            getSelenium().type( entry.getKey(), entry.getValue() );
        }
    }

    public void setFieldValue( String fieldName, String value )
    {
        getSelenium().type( fieldName, value );
    }

    public void checkField( String locator )
    {
        getSelenium().check( locator );
    }

    public void uncheckField( String locator )
    {
        getSelenium().uncheck( locator );
    }

    public boolean isChecked( String locator )
    {
        return getSelenium().isChecked( locator );
    }

    public void assertIsChecked( String locator )
    {
        try
        {
            Assert.assertTrue( getSelenium().isChecked( locator ) );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void assertIsNotChecked( String locator )
    {
        try
        {
            Assert.assertFalse( getSelenium().isChecked( locator ) );
        }
        catch ( java.lang.AssertionError e )
        {
            captureAssertionError( e );
        }
    }

    public void clickAndWait( String locator )
    {
        getSelenium().click( locator );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    public void waitForElementPresent( String locator )
        throws InterruptedException
    {
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        for ( int second = 0;; second++ )
        {
            if ( second >= 60 )
                Assert.fail( "timeout" );
            try
            {
                if ( isElementPresent( locator ) )
                    break;
            }
            catch ( Exception e )
            {
            }
            Thread.sleep( 1000 );
        }
    }

    public void waitForTextPresent( String text )
        throws InterruptedException
    {
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
        for ( int second = 0;; second++ )
        {
            if ( second >= 60 )
                Assert.fail( "Timeout" );
            try
            {
                if ( isTextPresent( text ) )
                    break;
            }
            catch ( Exception e )
            {
            }
            Thread.sleep( 1000 );
        }
    }

    // captureAssertionError() creates a 'target/screenshots' directory and saves '.png' page screenshot of the
    // encountered error
    public void captureError()
    {
        File f = new File( "" );
        String filePath = f.getAbsolutePath();
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd-HH_mm_ss" );
        String time = sdf.format( d );
        String fs = File.separator;
        File targetPath = new File( filePath + fs + "target" + fs + "screenshots" );
        targetPath.mkdir();
        String cName = getClass().getName();
        StackTraceElement stackTrace[] = new Throwable().fillInStackTrace().getStackTrace();
        int index = getStackTraceIndexOfCallingClass( cName, stackTrace );
        String methodName = stackTrace[index].getMethodName();
        int lNumber = stackTrace[index].getLineNumber();
        String lineNumber = Integer.toString( lNumber );
        String className = cName.substring( cName.lastIndexOf( '.' ) + 1 );
        String fileName =
            targetPath.toString() + fs + methodName + "(" + className + ".java_" + lineNumber + ")-" + time + ".png";
        getSelenium().windowMaximize();
        getSelenium().captureEntirePageScreenshot( fileName, "" );
    }

    public int getStackTraceIndexOfCallingClass( String nameOfClass, StackTraceElement stackTrace[] )
    {
        boolean match = false;
        int i = 0;
        do
        {
            String className = stackTrace[i].getClassName();
            match = Pattern.matches( nameOfClass, className );
            i++;
        }
        while ( match == false );
        i--;
        return i;
    }

    // captureError calls the captureAssertionError method to capture screenshot and
    // throw an assertion error for the errors to be displayed in the build results summary
    public void captureAssertionError( Object e )
    {
        captureError();
        throw new java.lang.AssertionError( e );
    }
}
