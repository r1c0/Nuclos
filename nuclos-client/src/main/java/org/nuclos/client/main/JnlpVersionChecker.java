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

import java.awt.Component;
import java.io.IOException;
import javax.jnlp.DownloadService2;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import org.apache.log4j.Logger;
import org.nuclos.client.login.LoginPanel;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.5
 */
public class JnlpVersionChecker implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(JnlpVersionChecker.class);
	
	//
	
	private final NuclosCriticalErrorHandler criticalErrorHandler;
	
	public JnlpVersionChecker(NuclosCriticalErrorHandler criticalErrorHandler) {
		this.criticalErrorHandler = criticalErrorHandler;
	}
	
	@Override
	public void run() {
		try {
			LOG.info("Nuclos web start client up-to-date check started...");
			final boolean updateNeeded = _run();
			if (updateNeeded) {
				final Error err = new Error("This Nuclos web start client is not up-to-date");
				LOG.fatal("Not up-to-date: " + err);
				final Component c = LoginPanel.getInstance();
				criticalErrorHandler.handleCriticalError(c, err);
			}
			else {
				LOG.info("Nuclos web start client up-to-date check succeeded");
			}
		}
		catch (UnavailableServiceException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
		}
		catch (IOException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
		}
		catch (RuntimeException e) {
			LOG.error("JnlpVersionChecker failed: " + e, e);
		}
	}
	
	private boolean _run() throws UnavailableServiceException, IOException {
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/examples.html
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
		// see http://docs.oracle.com/javase/6/docs/technotes/guides/jweb/customizeRIALoadingExperience.html
		// see http://docs.oracle.com/javase/6/docs/jre/api/javaws/jnlp/index.html
		
		boolean updateNeeded = false;
		
		final DownloadService2 service = (DownloadService2) ServiceManager.lookup("javax.jnlp.DownloadService2");
		final DownloadService2.ResourceSpec all = new DownloadService2.ResourceSpec(".*", ".*", DownloadService2.ALL);
		final DownloadService2.ResourceSpec[] updates = service.getUpdateAvailableResources(all);
		for (DownloadService2.ResourceSpec upd: updates) {
			updateNeeded = true;
			LOG.info("Webstart update needed for: " + upd);
		}
		
		return updateNeeded;
	}
	
}
