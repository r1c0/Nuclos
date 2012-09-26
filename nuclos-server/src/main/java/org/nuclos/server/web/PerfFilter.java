//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.server.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * An servlet filter that could be used for getting performance data for client-server
 * communication (remote calls, JMS calls, etc.).
 * 
 * @author Thomas Pasch
 */
public class PerfFilter implements Filter {
	
	private static final Logger LOG = Logger.getLogger(PerfFilter.class);
	
	private FilterConfig filterConfig;
	
	private String loggingPrefix = "<set 'loggingPrefix' with init parameter in web.xml>";
	
	public PerfFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		loggingPrefix = filterConfig.getInitParameter("loggingPrefix");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		final long start = System.currentTimeMillis();
		final HttpServletRequest req = (HttpServletRequest) request;
		// final HttpServletResponse resp = (HttpServletResponse) response;
		final MyServletResponseWrapper resp = new MyServletResponseWrapper((HttpServletResponse) response);
		chain.doFilter(request, resp);
		final long end = System.currentTimeMillis();
		final StringBuilder path = new StringBuilder();
		if (req.getServletPath() != null) {
			path.append(req.getServletPath());
		}
		if (req.getPathInfo() != null) {
			path.append(req.getPathInfo());
		}
		if (req.getQueryString() != null) {
			path.append("?");
			path.append(req.getQueryString());
		}
		LOG.info(loggingPrefix + " path: " + path.toString() + " request: " + req.getContentLength() + " response: " + resp.getContentLength()
				+ " time: " + (end -start));
		
		final Cookie[] cookies = req.getCookies();
		if (cookies != null && cookies.length > 0) {
			final StringBuilder c = new StringBuilder("cookies: ");
			for (int i = 0; i < cookies.length; ++i) {
				c.append(cookies[i].getName());
				c.append("=");
				c.append(cookies[i].getValue());
				c.append(" ");
			}
			final HttpSession session = req.getSession(false);
			if (session != null) {
				c.append(session);
			}
			LOG.info(c.toString());
		}
	}

	@Override
	public void destroy() {
	}
	
	private static class MyServletResponseWrapper extends HttpServletResponseWrapper {

		private int len = -1;
		
		private MyServletOutputStreamWrapper output;
		
		private PrintWriter writer;
		
		public MyServletResponseWrapper(HttpServletResponse response) throws IOException {
			super(response);
			output = new MyServletOutputStreamWrapper(getResponse().getOutputStream());
			writer = new PrintWriter(output);
		}
		
		@Override
		public void setContentLength(int len) {
			this.len = len;
			getResponse().setContentLength(len);
		}
		
		public int getContentLength() {
			if (len < 0) {
				return output.getLength();
			}
			return len;
		}
		
		@Override
	    public ServletOutputStream getOutputStream() throws IOException {
			return output;
		}
		
		@Override
	    public PrintWriter getWriter() throws IOException {
			return writer;
		}

	}
	
	private static class MyServletOutputStreamWrapper extends ServletOutputStream {
		
		private ServletOutputStream wrapped;
		
		private int len = 0;
		
		public MyServletOutputStreamWrapper(ServletOutputStream wrapped) {
			this.wrapped = wrapped;
		}
		
		public int getLength() {
			return len;
		}

		@Override
		public void write(int b) throws IOException {
			wrapped.write(b);
			++len;
		}
		
		@Override
	    public void write(byte b[], int off, int len) throws IOException {
	    	wrapped.write(b, off, len);
	    	this.len += len;
	    }
	    
		@Override
	    public void write(byte b[]) throws IOException {
			wrapped.write(b);
			if (b != null) {
				len += b.length;
			}
	    }
		
		@Override
		public void close() throws IOException {
			wrapped.close();
		}
		
		@Override
		public void flush() throws IOException {
			wrapped.flush();
		}
	}

}
