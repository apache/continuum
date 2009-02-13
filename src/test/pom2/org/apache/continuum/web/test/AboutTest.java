package org.apache.continuum.web.test;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AboutTest extends TestCase {

	private String baseUrl;
    public String maxWaitTimeInMs;
    private Selenium selenium;

    public void setUp() throws Exception {

        Properties p = new Properties();
        p.load ( this.getClass().getClassLoader().getResourceAsStream( "it.properties" ) );

        baseUrl = p.getProperty( "BASE_URL" );
        maxWaitTimeInMs = p.getProperty( "MAX_WAIT_TIME_IN_MS" );

        String seleniumHost = p.getProperty( "SELENIUM_HOST" );
        int seleniumPort = Integer.parseInt( (p.getProperty( "SELENIUM_PORT" ) ) );
        String seleniumBrowser = p.getProperty( "SELENIUM_BROWSER" );

        selenium = new DefaultSelenium( seleniumHost, seleniumPort, seleniumBrowser, baseUrl );
        selenium.start();
    }

    public void tearDown() throws Exception {
	    selenium.stop();
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return new CargoTestSetup(new TestSuite(AboutTest.class));
    }

    public void testAboutDisplay() {
    	selenium.open( baseUrl + "/about.action" );
    	selenium.waitForPageToLoad( maxWaitTimeInMs );

    	assertEquals("Continuum - About", selenium.getTitle());
    }
}
