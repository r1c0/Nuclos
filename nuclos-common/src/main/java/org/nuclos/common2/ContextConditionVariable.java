//Copyright (C) 2010  Novabit Informationssysteme GmbH
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
package org.nuclos.common2;

import org.apache.log4j.Logger;

public class ContextConditionVariable {
	
	private static final Logger LOG = Logger.getLogger(ContextConditionVariable.class);
	
	private final String name;
	
	private boolean refreshed = false;
	
	public ContextConditionVariable(String name) {
		this.name = name;
	}
	
	public boolean isRefreshed() {
		return refreshed;
	}
	
	public void waitFor() {
		LOG.info("Starting waitFor() on " + name);
		try {
			for(int i = 0; !refreshed && i < 1000; ++i) {
				wait(100);
			}
		}
		catch (InterruptedException e) {
			// ignore
		}
		if (!refreshed) {
			throw new IllegalStateException("Can't create MainController: Spring context not initialized!");
		}
		LOG.info("Finished waitFor() on " + name);
	}
	
	public void refreshed() {
		LOG.info("refreshed() on " + name);
		refreshed = true;
	}
	
}

