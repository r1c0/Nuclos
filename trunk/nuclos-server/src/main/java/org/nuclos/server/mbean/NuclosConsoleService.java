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
package org.nuclos.server.mbean;

import org.apache.log4j.Logger;
import org.nuclos.common.Actions;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;
import org.nuclos.server.security.NuclosLocalServerSession;

public class NuclosConsoleService implements NuclosConsoleServiceMBean {

	protected static final Logger log = Logger.getLogger(NuclosConsoleService.class);
	
	/**
	 * 
	 * @param sUser
	 * @param sPassword
	 * @param sCommand (see documentation)
	 * @param sArgs (not in use)
	 * @throws Exception
	 */
	@Override
	public String executeCommand(final String sUser, final String sPassword, final String sCommand) throws Exception {
		final ServerCall sc = new ServerCall(sUser, sPassword) {
			@Override
			public String run() throws Exception {
				ServiceLocator.getInstance().getFacade(ConsoleFacadeRemote.class).executeCommand(sCommand);
				return "command successfully executed";
			}
		};
		
		return sc.start();
	}
	
	
	/**
	 * 
	 */
	private abstract class ServerCall {
		
		private String nuclosUser;
		
		private String nuclosPassword;
		
		public ServerCall(String nuclosUser, String nuclosPassword) {
			super();
			this.nuclosUser = nuclosUser;
			this.nuclosPassword = nuclosPassword;
		}

		public abstract String run() throws Exception;
		
		public String start() throws Exception {
			try {
				log.info("login(" + this.nuclosUser + ")");
				NuclosLocalServerSession.login(this.nuclosUser, this.nuclosPassword);
				if(!SecurityCache.getInstance().hasUserRight(Actions.ACTION_MANAGEMENT_CONSOLE)) {
					log.warn("Try to use management console from ServiceMBean, but user has no rights!");
					return "You need the right \"" + Actions.ACTION_MANAGEMENT_CONSOLE + "\" to use this ServiceMBean.";
				}
				
				return run();
			}
			finally {
				NuclosLocalServerSession.logout();
				log.info("logout(" + this.nuclosUser + ")");
			}
		}
	}
	
}
