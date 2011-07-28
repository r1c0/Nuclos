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

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class SessionInvalidationFilter implements Filter {

	 static final String FILTER_APPLIED = "__nuclos_session_invalidation_filter_applied";

	private static final Logger LOG = Logger.getLogger(SessionInvalidationFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;

		if (request.getAttribute(FILTER_APPLIED) != null) {
            chain.doFilter(request, response);
            return;
        }

		request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

		HttpSession session = request.getSession(false);
		if (session != null) {
			try {
				DateFormat format = DateFormat.getDateTimeInstance();
				// invalidate session every 10 minutes, to force a re-authentication
				System.out.println(format.format(new Date(session.getCreationTime())));
				if (session.getCreationTime() + (1000 * 60 * 5) < Calendar.getInstance().getTimeInMillis()) {
					LOG.debug(MessageFormat.format("Invalidating HttpSession (CreationTime: {0})", format.format(new Date(session.getCreationTime()))));
					session.invalidate();
					session = request.getSession(true);
					assert session.isNew();
				}
			}
			catch (IllegalStateException ex) {
				LOG.debug("Session already invalidated.");
			}
		}

		if (chain != null) {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}
}
