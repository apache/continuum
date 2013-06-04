package org.apache.maven.continuum.web.util;

import org.apache.struts2.views.util.DefaultUrlHelper;
import org.apache.struts2.views.util.UrlHelper;

public class UrlHelperFactory {
	
	private UrlHelperFactory() {}
	
	public static UrlHelper getInstance() {
		return new DefaultUrlHelper();
	}

}
