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

import java.util.ArrayList;
import java.util.Collection;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.rcp.RemoteAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Implementation for Spring's <code>RemoteAuthenticationManager</code> that
 * does not convert all exceptions to RemoteAuthenticationException.<br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(propagation=Propagation.NOT_SUPPORTED)
public class RemoteAuthenticationManager implements org.nuclos.common.security.RemoteAuthenticationManager, InitializingBean {

	private AuthenticationManager authenticationManager;

	private UserDetailsService userDetailsService;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.authenticationManager, "authenticationManager is required");
	}

	@Override
	public Collection<? extends GrantedAuthority> attemptAuthentication(String username, String password) throws RemoteAuthenticationException {
		UsernamePasswordAuthenticationToken request = new UsernamePasswordAuthenticationToken(username, password);

		try {
			Authentication auth = authenticationManager.authenticate(request);
			userDetailsService.logAttempt(username, auth.isAuthenticated());
			final Collection<? extends GrantedAuthority> result = auth.getAuthorities();
			return result;
		}
		catch (UsernameNotFoundException ex) {
			throw ex;
		}
		catch (CredentialsExpiredException ex) {
			userDetailsService.logAttempt(username, true);
			throw ex;
		}
		catch (AuthenticationException ex) {
			userDetailsService.logAttempt(username, false);
			throw ex;
		}
	}

	@Override
	public void changePassword(final String username, String oldpassword, final String newpassword) throws AuthenticationException, CommonBusinessException {
		UserDetails ud = userDetailsService.loadUserByUsername(username);

		boolean authenticated = false;
		if (StringUtils.isNullOrEmpty(ud.getPassword()) && StringUtils.isNullOrEmpty(oldpassword)) {
			authenticated = true;
		}

		final String sPasswordFromUser = StringUtils.encryptBase64(username + ((oldpassword == null) ? "" : new String(oldpassword)));
		if(sPasswordFromUser.equals(ud.getPassword())) {
			authenticated = true;
		}

		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new GrantedAuthorityImpl("Login"));
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, ud.getPassword(), authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);

		if (authenticated) {
			ServiceLocator.getInstance().getFacade(UserFacadeLocal.class).setPassword(username, newpassword);
		}
		else {
			userDetailsService.logAttempt(username, authenticated);
			throw new BadCredentialsException("invalid.login.exception");
		}
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}
