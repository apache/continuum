package org.apache.continuum.web.test;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

import java.util.Properties;

import junit.framework.TestCase;

public class AboutTest extends TestCase {

	private String baseUrl;
    public String maxWaitTimeInMs;
    private Selenium selenium;
    private String adminUsername;
    private String adminFullname;
    private String adminEmail;
    private String adminPassword;

    public void setUp() throws Exception {

        Properties p = new Properties();
        p.load ( this.getClass().getClassLoader().getResourceAsStream( "it.properties" ) );

        baseUrl = p.getProperty( "BASE_URL" );
        maxWaitTimeInMs = p.getProperty( "MAX_WAIT_TIME_IN_MS" );

        String seleniumHost = p.getProperty( "SELENIUM_HOST" );
        int seleniumPort = Integer.parseInt( (p.getProperty( "SELENIUM_PORT" ) ) );
        String seleniumBrowser = p.getProperty( "SELENIUM_BROWSER" );

        adminUsername = p.getProperty( "ADMIN_USERNAME" );
        adminFullname = p.getProperty( "ADMIN_FULLNAME" );
        adminEmail = p.getProperty( "ADMIN_EMAIL" );
        adminPassword = p.getProperty( "ADMIN_PASSWORD" );

        selenium = new DefaultSelenium( seleniumHost, seleniumPort, seleniumBrowser, baseUrl );
        selenium.start();

        //make sure the initial config has been done
        createAdminUser();
        adminLogin();
        generalConfiguration();

    }

    public void tearDown() throws Exception {
	    selenium.stop();
    }

    public void testAboutDisplay() {

    	selenium.open( baseUrl + "/about.action" );
    	selenium.waitForPageToLoad( maxWaitTimeInMs );

    	assertEquals("Continuum - About", selenium.getTitle());
    }

    private void createAdminUser() {

        selenium.open( baseUrl );

        if( selenium.getTitle().endsWith( "Create Admin User") ) {
            selenium.type( "user.fullName", adminFullname );
            selenium.type( "user.email", adminEmail );
            selenium.type( "user.password", adminPassword );
            selenium.type( "user.confirmPassword", adminPassword );
            selenium.click( "adminCreateForm_0" );
            selenium.waitForPageToLoad( maxWaitTimeInMs );
        }
    }

    private void adminLogin() {

        selenium.open( baseUrl );

        if ( selenium.getTitle().endsWith( "Login Page") ) {
            selenium.type( "username", adminUsername );
            selenium.type( "password", adminPassword );
            selenium.click( "loginForm__login");
            selenium.waitForPageToLoad( maxWaitTimeInMs );
        }
    }

    private void generalConfiguration() {

        selenium.open( baseUrl );

        if ( selenium.getTitle().endsWith( "Configuration" ) ) {
            selenium.click( "configuration_" );
            selenium.waitForPageToLoad( maxWaitTimeInMs );
        }
    }
}