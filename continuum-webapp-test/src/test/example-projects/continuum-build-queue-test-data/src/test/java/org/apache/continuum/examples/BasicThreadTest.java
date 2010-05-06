package org.apache.continuum.examples;

import org.testng.annotations.Test;

public class BasicThreadTest {
	
	@Test
	public void testThread() {
	    try {
	        long numMillisecondsToSleep = 60000; // 1 * 60 seconds
	        Thread.sleep(numMillisecondsToSleep);
	    } catch (InterruptedException e) {
	    }
	}

}
