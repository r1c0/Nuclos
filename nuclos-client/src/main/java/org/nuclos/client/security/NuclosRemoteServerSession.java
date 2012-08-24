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

import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class NuclosRemoteServerSession {

	private static final Logger LOG = Logger.getLogger(NuclosRemoteServerSession.class);
	
	private static NuclosRemoteServerSession INSTANCE;
	
	//
	
	// Spring injection
	
	private SecurityFacadeRemote securityFacadeRemote;
	
	// end of Spring injection

	private volatile Integer sessionId;

	NuclosRemoteServerSession() {
		INSTANCE = this;
	}
	
	public static NuclosRemoteServerSession getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setSecurityFacadeRemote(SecurityFacadeRemote securityFacadeRemote) {
		this.securityFacadeRemote = securityFacadeRemote;
	}

	public String login(String username, String password) throws AuthenticationException {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
		try {
			final AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
			final UsernamePasswordAuthenticationToken auth1 = (UsernamePasswordAuthenticationToken) am.authenticate(new UsernamePasswordAuthenticationToken(username, new String(password)));
			final UsernamePasswordAuthenticationToken auth2 = new UsernamePasswordAuthenticationToken(auth1.getPrincipal(), password, auth1.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth2);
			sessionId = securityFacadeRemote.login();
			LOG.info("User " + username + " logged in as " + auth2.getPrincipal() + ", session=" + sessionId);
			return auth2.getPrincipal().toString();
		}
		catch (AuthenticationException ex) {
			SecurityContextHolder.getContext().setAuthentication(null);
			throw ex;
		}
	}

	public String relogin(String username, String password) throws AuthenticationException {
		try {
			final AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
			final UsernamePasswordAuthenticationToken auth1 = (UsernamePasswordAuthenticationToken) am.authenticate(new UsernamePasswordAuthenticationToken(username, new String(password)));
			final UsernamePasswordAuthenticationToken auth2 = new UsernamePasswordAuthenticationToken(auth1.getPrincipal(), password, auth1.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth2);
			LOG.info("Validated login of " + username + " as " + auth2.getPrincipal() + ", session=" + sessionId);
			SecurityCache.getInstance().revalidate();
			return auth2.getPrincipal().toString();
		}
		catch (AuthenticationException ex) {
			SecurityContextHolder.getContext().setAuthentication(null);
			throw ex;
		}
	}

	public Authentication authenticate() throws AuthenticationException, RemoteAccessException {
		AuthenticationManager am = (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
		Object c = SecurityContextHolder.getContext().getAuthentication().getCredentials();
		Authentication auth = am.authenticate(SecurityContextHolder.getContext().getAuthentication());
		auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), c, auth.getAuthorities());
		return auth;
	}

	@PreDestroy
	public void logout() {
		try {
			if (sessionId != null) {
				securityFacadeRemote.logout(sessionId);
				SecurityContextHolder.getContext().setAuthentication(null);
				LOG.info("Logged out, session terminated " + sessionId);
				sessionId = null;
			}
		}
		catch(Exception e) {
			LOG.error("logout failed: " + e, e);
		}
	}
}
