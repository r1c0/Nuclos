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
package org.nuclos.client.main;

import javax.jnlp.SingleInstanceListener;

import org.apache.log4j.Logger;

/**
 * Implementation of SingleInstanceListener.
 * This listener is registered if client is started with webstart in single instance mode.
 * All beans of type <code>LaunchListener</code> are notified.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class JWSSingleInstanceListener implements SingleInstanceListener {

	private static final Logger log = Logger.getLogger(JWSSingleInstanceListener.class);

	@Override
	public void newActivation(String[] args) {
		Main.notifyListeners(args);
	}
}
