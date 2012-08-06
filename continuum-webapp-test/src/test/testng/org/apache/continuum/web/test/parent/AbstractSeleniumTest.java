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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Based on AbstractSeleniumTestCase of Emmanuel Venisse test.
 *
 * @author José Morales Martínez
 * @version $Id$
 */
public abstract class AbstractSeleniumTest
{
    protected static String baseUrl;

    static String browser;

    protected static String maxWaitTimeInMs;

    private static final ThreadLocal<Selenium> selenium = new ThreadLocal<Selenium>();

    private static Properties p;

    private final static String PROPERTIES_SEPARATOR = "=";

    private static String maxProjectWaitTimeInMs;

    /**
     * Initialize selenium
     */
    void open( String baseUrl, String browser, String seleniumHost, int seleniumPort )
        throws Exception
    {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream( "testng.properties" );
        p = new Properties();
        p.load( input );

        String svnBaseUrl = "file://localhost/" + new File( "target/example-svn" ).getAbsolutePath();
        for ( String key : p.stringPropertyNames() ) {
            String value = p.getProperty( key ).replace( "${svn.base.url}", svnBaseUrl );
            p.setProperty( key, value );
        }

        maxWaitTimeInMs = getProperty( "MAX_WAIT_TIME_IN_MS" );
        maxProjectWaitTimeInMs = getProperty( "MAX_PROJECT_WAIT_TIME_IN_MS" );

        AbstractSeleniumTest.baseUrl = baseUrl;

        AbstractSeleniumTest.browser = browser;

        if ( getSelenium() == null )
        {
            DefaultSelenium s = new DefaultSelenium( seleniumHost, seleniumPort, browser, baseUrl );
            s.start();
            s.setTimeout( maxWaitTimeInMs );
            selenium.set( s );
        }
    }

    public static Selenium getSelenium()
    {
        return selenium == null ? null : selenium.get();
    }

    protected String getProperty( String key )
    {
        return p.getProperty( key );
    }

    // TODO: look into removing this, as the issue should be fixed by upgrading the resources plugin to v2.4+
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
    @AfterSuite(alwaysRun = true)
    public void close()
        throws Exception
    {
        if ( getSelenium() != null )
        {
            getSelenium().stop();
            selenium.set( null );
        }
    }

    // *******************************************************
    // Auxiliar methods. This method help us and simplify test.
    // *******************************************************

    protected void assertFieldValue( String fieldValue, String fieldName )
    {
        assertElementPresent( fieldName );
        Assert.assertEquals( fieldValue, getSelenium().getValue( fieldName ) );
    }

    protected void assertPage( String title )
    {
        Assert.assertEquals( getTitle(), title );
    }

    protected String getTitle()
    {
        return getSelenium().getTitle();
    }

    protected void assertTextPresent( String text )
    {
        Assert.assertTrue( getSelenium().isTextPresent( text ), "'" + text + "' isn't present." );
    }

    protected void assertTextNotPresent( String text )
    {
        Assert.assertFalse( getSelenium().isTextPresent( text ), "'" + text + "' is present." );
    }

    protected void assertElementPresent( String elementLocator )
    {
        Assert.assertTrue( isElementPresent( elementLocator ), "'" + elementLocator + "' isn't present." );
    }

    void assertElementNotPresent( String elementLocator )
    {
        Assert.assertFalse( isElementPresent( elementLocator ), "'" + elementLocator + "' is present." );
    }

    protected void assertLinkPresent( String text )
    {
        Assert.assertTrue( isElementPresent( "link=" + text ), "The link '" + text + "' isn't present." );
    }

    protected void assertLinkNotPresent( String text )
    {
            Assert.assertFalse( isElementPresent( "link=" + text ), "The link '" + text + "' is present." );
    }

    protected void assertImgWithAlt( String alt )
    {
        assertElementPresent( "//img[@alt='" + alt + "']" );
    }

    protected void assertCellValueFromTable( String expected, String tableElement, int row, int column )
    {
        Assert.assertEquals( expected, getCellValueFromTable( tableElement, row, column ) );
    }

    protected boolean isTextPresent( String text )
    {
        return getSelenium().isTextPresent( text );
    }

    protected boolean isLinkPresent( String text )
    {
        return isElementPresent( "link=" + text );
    }

    protected boolean isElementPresent( String locator )
    {
        return getSelenium().isElementPresent( locator );
    }

    protected void waitPage()
    {
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    protected String getFieldValue( String fieldName )
    {
        return getSelenium().getValue( fieldName );
    }

    String getCellValueFromTable( String tableElement, int row, int column )
    {
        return getSelenium().getTable( tableElement + "." + row + "." + column );
    }

    protected void selectValue( String locator, String value )
    {
        getSelenium().select( locator, "label=" + value );
    }

    void assertOptionPresent( String selectField, String[] options )
    {
        assertElementPresent( selectField );
        String[] optionsPresent = getSelenium().getSelectOptions( selectField );
        List<String> expected = Arrays.asList( options );
        List<String> present = Arrays.asList( optionsPresent );

        Assert.assertTrue( present.containsAll( expected ), "Options expected are not included in present options" );
    }

    protected void submit()
    {
        clickLinkWithXPath( "//input[@type='submit']" );
    }

    protected void assertButtonWithValuePresent( String text )
    {
        Assert.assertTrue( isButtonWithValuePresent( text ), "'" + text + "' button isn't present" );
    }

    void assertButtonWithIdPresent( String id )
    {
        Assert.assertTrue( isButtonWithIdPresent( id ), "'Button with id =" + id + "' isn't present" );
    }

    boolean isButtonWithValuePresent( String text )
    {
        return isElementPresent( "//button[@value='" + text + "']" )
            || isElementPresent( "//input[@value='" + text + "']" );
    }

    boolean isButtonWithIdPresent( String text )
    {
        return isElementPresent( "//button[@id='" + text + "']" ) || isElementPresent( "//input[@id='" + text + "']" );
    }

    protected void clickButtonWithValue( String text )
    {
        clickButtonWithValue( text, true );
    }

    void clickButtonWithValue( String text, boolean wait )
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

    void clickSubmitWithLocator( String locator )
    {
        clickLinkWithLocator( locator );
    }

    protected void clickImgWithAlt( String alt )
    {
        clickLinkWithLocator( "//img[@alt='" + alt + "']" );
    }

    protected void clickLinkWithText( String text )
    {
        clickLinkWithLocator( "link=" + text, true );
    }

    protected void clickLinkWithXPath( String xpath )
    {
        clickLinkWithXPath( xpath, true );
    }

    protected void clickLinkWithXPath( String xpath, boolean wait )
    {
        clickLinkWithLocator( "xpath=" + xpath, wait );
    }

    void clickLinkWithLocator( String locator )
    {
        clickLinkWithLocator( locator, true );
    }

    protected void clickLinkWithLocator( String locator, boolean wait )
    {
        getSelenium().click( locator );
        if ( wait )
        {
            waitPage();
        }
    }

    protected void setFieldValue( String fieldName, String value )
    {
        getSelenium().type( fieldName, value );
    }

    protected void checkField( String locator )
    {
        getSelenium().check( locator );
    }

    void uncheckField( String locator )
    {
        getSelenium().uncheck( locator );
    }

    boolean isChecked( String locator )
    {
        return getSelenium().isChecked( locator );
    }

    void assertIsChecked()
    {
        Assert.assertTrue( getSelenium().isChecked( "saveBuildAgent_buildAgent_enabled" ) );
    }

    void click( String locator )
    {
        getSelenium().click( locator );
    }

    protected void clickAndWait( String locator )
    {
        getSelenium().click( locator );
        getSelenium().waitForPageToLoad( maxWaitTimeInMs );
    }

    protected void waitForElementPresent( String locator )
    {
        waitForElementPresent( locator, true );
    }

    /*
     * This will wait for the condition to be met.
     *   * shouldBePresent - if the locator is expected or not (true or false respectively)
     */
    void waitForElementPresent( String locator, boolean shouldBePresent )
    {
        waitForOneOfElementsPresent( Collections.singletonList( locator ), shouldBePresent );
    }

    void waitForOneOfElementsPresent( List<String> locators, boolean shouldBePresent )
    {
        if ( browser.equals( "*iexplore" ) )
        {
            int currentIt = 0;
            int maxIt = Integer.valueOf( getProperty( "WAIT_TRIES" ) );
            String pageLoadTimeInMs = maxWaitTimeInMs;

            while ( currentIt < maxIt )
            {
                for ( String locator : locators )
                {
                    if ( isElementPresent( locator ) == shouldBePresent )
                    {
                        return;
                    }
                }

                getSelenium().waitForPageToLoad( pageLoadTimeInMs );
                currentIt++;
            }
        }
        else
        {
            StringBuilder condition = new StringBuilder();
            String operator = "";
            for ( String locator : locators ) {
                condition.append( operator );
                condition.append( "(selenium.isElementPresent(\"" ).append( locator ).append( "\")" );
                condition.append( " == " );
                condition.append( shouldBePresent ).append( ")" );
                operator = "||";
            }
            waitForCondition( "(" + condition + ")" );
        }
    }

    void waitForCondition( String condition )
    {
        getSelenium().waitForCondition( condition, maxProjectWaitTimeInMs );
    }

    void assertEnabled()
    {
        Assert.assertTrue( getSelenium().isEditable( "alwaysBuild" ), "'" + "alwaysBuild" + "' is disabled" );
    }
}
