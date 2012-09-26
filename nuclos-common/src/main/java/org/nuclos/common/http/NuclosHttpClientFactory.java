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
package org.nuclos.common.http;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.Logger;
import org.nuclos.common.tls.CustomSecureProtocolSocketFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * A Spring BeanFactory that creates a customized (apache httpcomponents) HttpClient instance.
 * <p>
 * The created instance uses the {@link CustomSecureProtocolSocketFactory} for SSL/TLS connections.
 * In addition the http(s) pool settings could be tweaked.
 * </p><p>
 * This instance is used for both remote and JMS calls. 
 * </p>
 * @author Thomas Pasch
 */
public class NuclosHttpClientFactory implements FactoryBean<HttpClient> {
	
	private static final Logger LOG = Logger.getLogger(NuclosHttpClientFactory.class);
	
	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;

	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;
	
	private static NuclosHttpClientFactory INSTANCE;

	private HttpClient httpClient;
	
	public NuclosHttpClientFactory() {
		INSTANCE = this;
		LOG.info("Register CustomSecureProtocolSocketFactory for HTTPS (modern apache http component)");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		// schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, new CustomSecureProtocolSocketFactory()));

		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
		connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
		connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

		httpClient = new DefaultHttpClient(connectionManager);		
	}
	
	@Deprecated
	public static NuclosHttpClientFactory getInstance() {
		return INSTANCE;
	}
	
	@Override
	public HttpClient getObject() {
		return httpClient;
	}

	@Override
	public Class<?> getObjectType() {
		return HttpClient.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
