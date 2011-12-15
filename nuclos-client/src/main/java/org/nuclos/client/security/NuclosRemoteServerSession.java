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

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class NuclosRemoteServerSession {

	private static final Logger LOG = Logger.getLogger(NuclosRemoteServerSession.class);

	private static Integer sessionId;

	public static void login(String username, String password) throws AuthenticationException {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
		try {
			AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
			SecurityContextHolder.getContext().setAuthentication(am.authenticate(new UsernamePasswordAuthenticationToken(username, new String(password))));
			sessionId = ServiceLocator.getInstance().getFacade(SecurityFacadeRemote.class).login();
			LOG.info("User " + username + " logged in, session=" + sessionId);
		}
		catch (AuthenticationException ex) {
			SecurityContextHolder.getContext().setAuthentication(null);
			throw ex;
		}
	}

	public static void relogin(String username, String password) throws AuthenticationException {
		try {
			AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
			SecurityContextHolder.getContext().setAuthentication(am.authenticate(new UsernamePasswordAuthenticationToken(username, new String(password))));
			LOG.info("Validated login.");
			SecurityCache.getInstance().revalidate();
		}
		catch (AuthenticationException ex) {
			SecurityContextHolder.getContext().setAuthentication(null);
			throw ex;
		}
	}

	public static Authentication authenticate() throws AuthenticationException, RemoteAccessException {
		AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
		return am.authenticate(SecurityContextHolder.getContext().getAuthentication());
	}

	public static void logout() {
		try {
			ServiceLocator.getInstance().getFacade(SecurityFacadeRemote.class).logout(sessionId);
			SecurityContextHolder.getContext().setAuthentication(null);
			LOG.info("Logged out.");
		}
		catch(Exception e) {
			LOG.error("logout failed: " + e, e);
		}
	}
}
