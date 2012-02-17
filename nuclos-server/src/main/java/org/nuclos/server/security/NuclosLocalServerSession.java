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
package org.nuclos.server.security;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utilitiy class for setting up a new local server session context, for instance in job execution or server startup.
 *
 * TODO: set users locale or default locale to <code>LocaleContextHolder</code>
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Component
public class NuclosLocalServerSession {

	public static final String STATIC_SUPERUSER = "(superuser)";

	private static final Logger LOG = Logger.getLogger(NuclosLocalServerSession.class);
	
	private static NuclosLocalServerSession INSTANCE;
	
	//

	private Integer sessionId;
	
	private SecurityFacadeLocal securityFacadeLocal;
	
	private NuclosAuthenticationProvider nuclosAuthenticationProvider;
	
	private NuclosUserDetailsService nuclosUserDetailsService;
	
	NuclosLocalServerSession() {
		INSTANCE = this;
	}
	
	public static NuclosLocalServerSession getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setSecurityFacadeLocal(SecurityFacadeLocal securityFacadeLocal) {
		this.securityFacadeLocal = securityFacadeLocal;
	}
	
	@Autowired
	void setNuclosAuthenticationProvider(NuclosAuthenticationProvider nuclosAuthenticationProvider) {
		this.nuclosAuthenticationProvider = nuclosAuthenticationProvider;
	}
	
	@Autowired
	void setNuclosUserDetailsService(NuclosUserDetailsService nuclosUserDetailsService) {
		this.nuclosUserDetailsService = nuclosUserDetailsService;
	}

	public void login(String username, String password) {
		Authentication auth = new NuclosLocalServerAuthenticationToken(username, password);
		SecurityContextHolder.getContext().setAuthentication(nuclosAuthenticationProvider.authenticate(auth));
		sessionId = securityFacadeLocal.login();
	}

	public void loginAsUser(String username) {
		UserDetails userDetails = nuclosUserDetailsService.loadUserByUsername(username);
		loginAs(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
	}

	public String getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			return auth.getName();
		}
		return null;
	}

	public void runUnrestricted(Runnable run) {
		Authentication token = SecurityContextHolder.getContext().getAuthentication();
		try {
			RunAsUserToken runastoken = new RunAsUserToken("", STATIC_SUPERUSER, "", getSuperUserAuthorities(), token.getClass());
			SecurityContextHolder.getContext().setAuthentication(runastoken);
			run.run();
		}
		finally {
			SecurityContextHolder.getContext().setAuthentication(token);
		}
	}

	/** Login as dummy user with superuser rights. Used during start-up (cache initialization). */
	public void loginAsSuperUser() {
		loginAs(STATIC_SUPERUSER, "", getSuperUserAuthorities());
	}

	private void loginAs(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		Authentication auth = new NuclosLocalServerAuthenticationToken(username, password, authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
		sessionId = securityFacadeLocal.login();
	}

	public void logout() {
		try {
			securityFacadeLocal.logout(sessionId);
			SecurityContextHolder.getContext().setAuthentication(null);
		}
		catch(Exception e) {
			LOG.error(e);
		}
	}

	private List<GrantedAuthority> getSuperUserAuthorities() {
		return CollectionUtils.transform(NuclosUserDetailsService.getSuperUserActions(), new Transformer<String, GrantedAuthority>() {
			@Override
			public GrantedAuthority transform(String i) {
				return new GrantedAuthorityImpl(i);
			}
		});
	}
}
