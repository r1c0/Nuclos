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
package org.nuclos.client.security;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.security.NuclosLoginException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.rcp.RemoteAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class NuclosRemoteServerSession {

	private static final Logger log = Logger.getLogger(NuclosRemoteServerSession.class);

	private static Integer sessionId;

	public static void login(String username, String password) throws LoginException {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, new String(password)));
		if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
			try {
				Authentication auth = am.authenticate(SecurityContextHolder.getContext().getAuthentication());
				SecurityContextHolder.getContext().setAuthentication(auth);
				sessionId = ServiceLocator.getInstance().getFacade(SecurityFacadeRemote.class).login();
				log.info("Logged in.");
			}
			catch (AccessDeniedException ex) {
				throw new NuclosLoginException(ex.getMessage(), NuclosLoginException.AUTHORISATION_ERROR);
			}
			catch (RemoteAuthenticationException ex) {
				throw new LoginException(ex.getMessage());
			}
			catch (RemoteAccessException ex) {
				throw new LoginException(ex.getMessage());
			}
		}
	}

	public static void logout() {
		try {
			ServiceLocator.getInstance().getFacade(SecurityFacadeRemote.class).logout(sessionId);
			SecurityContextHolder.getContext().setAuthentication(null);
			log.info("Logged out.");
		}
		catch(Exception e) {
			log.error(e);
		}
	}
}
