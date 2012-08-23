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

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ServerServiceLocator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.rcp.RemoteAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
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
@Transactional(propagation=Propagation.NOT_SUPPORTED, noRollbackFor= {Exception.class})
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
	public void changePassword(String username, String oldpassword, final String newpassword) throws AuthenticationException, CommonBusinessException {
		final UserDetails ud = userDetailsService.loadUserByUsername(username);

		boolean authenticated = false;
		if (StringUtils.isNullOrEmpty(ud.getPassword()) && StringUtils.isNullOrEmpty(oldpassword)) {
			authenticated = true;
		}

		final String sPasswordFromUser = StringUtils.encryptPw(username, oldpassword);
		if (!authenticated && sPasswordFromUser.equals(ud.getPassword())) {
			authenticated = true;
		}

		// http://support.novabit.de/browse/ACC-228
		final SecurityContext context = SecurityContextHolder.getContext();
		final Authentication preAuth = context.getAuthentication();
		if (!authenticated && preAuth != null) {
			final Collection<? extends GrantedAuthority> granted = preAuth.getAuthorities();
			if (granted != null && !granted.isEmpty()) {
				final Collection<GrantedAuthority> required = new ArrayList<GrantedAuthority>();
				required.add(new SimpleGrantedAuthority("Login"));
				// required.add(new SimpleGrantedAuthority("ChangeOwnPassword"));
				
				required.removeAll(granted);
				if (required.isEmpty() && ud.getUsername().equalsIgnoreCase(username) && sPasswordFromUser.equals(ud.getPassword())) {
					authenticated = true;
				}
			}
		}
		
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("Login"));
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud.getUsername(), ud.getPassword(), authorities);
		context.setAuthentication(auth);

		if (authenticated) {
			ServerServiceLocator.getInstance().getFacade(UserFacadeLocal.class).setPassword(ud.getUsername(), newpassword);
		}
		else {
			userDetailsService.logAttempt(ud.getUsername(), authenticated);
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
