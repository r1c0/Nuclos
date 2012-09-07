//Copyright (C) 2011  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common.startup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Log version and SVN information during startup of client and server.
 * <p>
 * This class is instantiated in the spring context of Nuclos client <em>and</em> server.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class Startup implements InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(Startup.class);
	
	private static final String ENCODING = "UTF-8";
	
	private Timer timer;
	
	public Startup() {
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		final TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				try {
					final RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
					final List<String> arguments = RuntimemxBean.getInputArguments();
					LOG.info("server started with " + arguments);
					
					final String version = IOUtils.toString(getClasspathResource("nuclos-version.properties"), ENCODING);
					LOG.info("version info\n:" + version);
					final String info = IOUtils.toString(getClasspathResource("info.txt"), ENCODING);
					LOG.info("SVN info\n:" + info);
					final String status = IOUtils.toString(getClasspathResource("status.txt"), ENCODING);
					LOG.info("SVN status\n:" + status);
				}
				catch (Exception e) {
					LOG.info("Startup timer task failed: " + e);
				}
			}
		};
		timer.schedule(task, 500);
	}

	@Autowired
	final void setTimer(Timer timer) {
		this.timer = timer;
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
