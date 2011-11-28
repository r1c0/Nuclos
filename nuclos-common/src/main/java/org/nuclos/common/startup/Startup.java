package org.nuclos.common.startup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Startup {
	
	private static final Logger LOG = Logger.getLogger(Startup.class);
	
	private static final String ENCODING = "UTF-8";
	
	public Startup() {
		try {
			final String version = IOUtils.toString(getClasspathResource("nuclos-version.properties"), ENCODING);
			LOG.info("version info\n:" + version);
			final String info = IOUtils.toString(getClasspathResource("info.xml"), ENCODING);
			LOG.info("SVN info\n:" + info);
			final String status = IOUtils.toString(getClasspathResource("status.xml"), ENCODING);
			LOG.info("SVN status\n:" + status);
		}
		catch (Exception e) {
			LOG.warn("Startup constructor failed: " + e, e);
		}
	}
	
	public static InputStream getClasspathResource(String path) throws IOException {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Enumeration<URL> en = cl.getResources(path);
		if (!en.hasMoreElements()) {
			throw new IllegalArgumentException("classpath resource " + path + " not found");
		}
		final URL url = en.nextElement();
		if (en.hasMoreElements()) {
			throw new IllegalArgumentException("duplicated classpath resource " + path 
					+ " at " + url + " and " + en.nextElement());
		}
		return url.openStream();
	}

}
