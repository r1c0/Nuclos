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
package org.nuclos.client;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecuredBasicAuthHttpInvokerRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(SecuredBasicAuthHttpInvokerRequestExecutor.class);

	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;

	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;

	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);
	
	//

	private HttpRequestRetryHandler retryHandler;
	
	private ApplicationProperties applicationProperties;
	
	static {
		LOG.info("Register CustomSecureProtocolSocketFactoryOld for HTTPS (java rt and apache commons httpclient)");
		// ActiveMQ still uses Apache Commons HttpClient
		Protocol.registerProtocol("https", new Protocol("https", new CustomSecureProtocolSocketFactoryOld(), 443));		
	}

	public SecuredBasicAuthHttpInvokerRequestExecutor() {
		super(getMyHttpClient());
		setReadTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
	}
	
	private final static HttpClient getMyHttpClient() {
		LOG.info("Register CustomSecureProtocolSocketFactory for HTTPS (modern apache http component)");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		// schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, new CustomSecureProtocolSocketFactory()));

		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
		connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

		final HttpClient result = new DefaultHttpClient(connectionManager);
		return result;
	}
	
	// @Autowired
	public final void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	// @PostConstruct
	public final void afterPropertiesSet() {
		// timeout 30 minutes (requested for entity transfer)
		super.setReadTimeout(applicationProperties.isFunctionBlockDev() ? 0 : 1000 * 60 * 30);
	}

	@Override
	protected HttpPost createHttpPost(HttpInvokerClientConfiguration config) throws IOException {
		HttpPost postMethod = super.createHttpPost(config);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if ((auth != null) && (auth.getName() != null)) {
			String base64 = auth.getName() + ":" + LangUtils.defaultIfNull(auth.getCredentials(), "");
			postMethod.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(base64.getBytes())));
		}
		return postMethod;
	}

	@Override
	public void setHttpClient(HttpClient httpClient) {
		if (this.retryHandler != null) {
			((DefaultHttpClient) httpClient).setHttpRequestRetryHandler(retryHandler);
		}
		super.setHttpClient(httpClient);
	}

	public void setRetryHandler(HttpRequestRetryHandler retryHandler) {
		this.retryHandler = retryHandler;
		if (getHttpClient() != null) {
			((DefaultHttpClient) getHttpClient()).setHttpRequestRetryHandler(retryHandler);
		}
	}
}
