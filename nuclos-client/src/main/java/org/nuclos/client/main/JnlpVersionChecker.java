//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadService2;
import javax.jnlp.DownloadServiceListener;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.6
 */
public class JnlpVersionChecker extends TimerTask {
	
	private static final Logger LOG = Logger.getLogger(JnlpVersionChecker.class);
	
	private static final int MAX_CHECKS = 5;
	
	//
		
	private final Timer timer = new Timer("JnlpVersionChecker");
	
	private DownloadService2.ResourceSpec[] updates;
	
	private int checks = 0;

	public JnlpVersionChecker() {
		timer.schedule(this, 0, 10000);
	}
	
	@Override
	public void run() {
		try {
			String url = System.getProperty("url.remoting");
			url = url.substring(0, url.lastIndexOf("/remoting"));
			LOG.info("Nuclos web start client up-to-date check started, url=" + url);
			final boolean updateNeeded = _run(url);
			if (updateNeeded) {
				final Error err = new Error("This Nuclos web start client is not up-to-date");
				LOG.fatal("Not up-to-date: " + err);
				final StringBuilder details = new StringBuilder();
				final int size = updates.length;
				for (int i = 0; i < size; ++i) {
					final DownloadService2.ResourceSpec upd = updates[i];
					details.append(toString(upd)).append("\n");
					if (i > 10) {
						details.append("...\n");
						break;
					}
				}
				/*
				final ErrorInfo ei = new ErrorInfo(
						"Fatal Error", err.toString(), details.toString(), "FATAL", err, Level.SEVERE, null);
				JXErrorPane.showDialog(null, ei);
				 */
				final int ans = JOptionPane.showOptionDialog(null, details, err.toString(), 
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,  
						null, new Object[] { "Exit Nuclos client", "Try anyway"}, 0);
				if (ans == JOptionPane.YES_OPTION) {
					System.exit(-1);
				}
				throw err;
			}
			else {
				LOG.info("Nuclos web start client up-to-date check succeeded");
			}
		}
		catch (UnavailableServiceException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
			checks = MAX_CHECKS;
		}
		catch (IOException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
			checks = MAX_CHECKS;
		}
		catch (RuntimeException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
			checks = MAX_CHECKS;
		}
		if (checks >= MAX_CHECKS) {
			LOG.info("Timer cancelled - no more update checks");
			timer.cancel();
		}
	}
	
	private boolean _run(String url) throws UnavailableServiceException, IOException {
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/examples.html
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/jweb/customizeRIALoadingExperience.html
		// see http://docs.oracle.com/javase/6/docs/jre/api/javaws/jnlp/index.html
		
		boolean updateNeeded = false;
		
		final DownloadService service = (DownloadService) ServiceManager.lookup("javax.jnlp.DownloadService");
		final DownloadServiceListener listener = service.getDefaultProgressWindow();
		final DownloadService2 service2 = (DownloadService2) ServiceManager.lookup("javax.jnlp.DownloadService2");
		
		// As we don't use the version protocol, version must be null.
		// DownloadService2.JAR seems not to work, hence be prudent if you change.
		// (tp)
		final DownloadService2.ResourceSpec all = new DownloadService2.ResourceSpec(url + "/app/.*", null, DownloadService2.ALL);
		LOG.info("Looking for Spec " + toString(all));

		try {
			final DownloadService2.ResourceSpec[] cached = service2.getCachedResources(all);
			if (cached.length > 0) {
				++checks;
			}
			final Priority prio;
			if (checks > 1) {
				prio = Priority.DEBUG;
			}
			else {
				prio = Priority.INFO;
			}
			for (DownloadService2.ResourceSpec c: cached) {
				LOG.log(prio, "Webstart cached resource: " + toString(c));
			}
		}
		catch (NullPointerException e) {
			LOG.warn("NPE in getCachedResources", e);
		}

		try {
			updates = service2.getUpdateAvailableResources(all);
			if (updates.length > 0) {
				++checks;
			}
			long total = 0L;
			for (DownloadService2.ResourceSpec upd: updates) {
				updateNeeded = true;
				LOG.info("Webstart update needed for: " + toString(upd));
				total += upd.getSize();
			}
			final DownloadServiceListenerWrapper wrapper = new DownloadServiceListenerWrapper(listener, total);
			final int size = updates.length;
			for (int i = 0; i < size; ++i) {
				final DownloadService2.ResourceSpec upd = updates[i];
				LOG.info("Updating: " + toString(upd));
				final URL resUrl = new URL(upd.getUrl());
				service.removeResource(resUrl, null);
				// service.loadResource(resUrl, null, wrapper);
				wrapper.addToReadSoFar(upd.getSize());
				if (i + 1 == size) {
					wrapper.progress(resUrl, null, total, total, 100);
					Thread.sleep(500);
					wrapper.disposeWindow();
				}
			}
		}
		catch (NullPointerException e) {
			LOG.warn("NPE in getUpdateAvailableResources", e);
		}
		catch (InterruptedException e) {
			LOG.warn("Interrupted in getUpdateAvailableResources", e);
		}
		
		return updateNeeded;
	}
	
	private static String toString(DownloadService2.ResourceSpec res) {
		final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final StringBuilder result = new StringBuilder();
		result.append(res.getUrl());
		result.append(" ver=").append(res.getVersion());
		result.append(" type=").append(res.getType());
		result.append(" mod=").append(format.format(new Date(res.getLastModified())));
		result.append(" exp=").append(format.format(new Date(res.getExpirationDate())));
		result.append(" size=").append(res.getSize());
		return result.toString();
	}
	
	private static class DownloadServiceListenerWrapper implements DownloadServiceListener {
		
		private final DownloadServiceListener wrapped;
		
		private final long realTotal;
		
		private long realReadSoFar = 0;
		
		private DownloadServiceListenerWrapper(DownloadServiceListener wrapped, long total) {
			this.wrapped = wrapped;
			this.realTotal = total;
		}
		
		private void addToReadSoFar(long read) {
			realReadSoFar += read;
		}

		public void progress(URL url, String version, long readSoFar, long total, int overallPercent) {
			if (realTotal < 0) {
				overallPercent = -1;
			}
			else {
				overallPercent = (int) (realReadSoFar * 100 / realTotal);
			}
			wrapped.progress(url, version, realReadSoFar, realTotal, overallPercent);
		}

		public void validating(URL url, String version, long entry, long total, int overallPercent) {
			wrapped.validating(url, version, total, total, overallPercent);
		}

		public void upgradingArchive(URL url, String version, int patchedPercent, int overallPercent) {
			wrapped.upgradingArchive(url, version, patchedPercent, overallPercent);
		}

		public void downloadFailed(URL url, String version) {
			wrapped.downloadFailed(url, version);
		}
		
		public void disposeWindow() {
			final Class<?> clazz = wrapped.getClass();
			try {
				final Method disposeWindow = clazz.getDeclaredMethod("done");
				disposeWindow.invoke(this);
			}
			catch (NoSuchMethodException e) {
				LOG.warn("disposeWindow failed: " + e);
			}
			catch (SecurityException e) {
				LOG.warn("disposeWindow failed: " + e);
			}
			catch (IllegalAccessException e) {
				LOG.warn("disposeWindow failed: " + e);
			}
			catch (InvocationTargetException e) {
				LOG.warn("disposeWindow failed: " + e.getCause());
			}
		}
	}
}
