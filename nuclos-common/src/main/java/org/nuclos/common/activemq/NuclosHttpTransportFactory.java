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
package org.nuclos.common.activemq;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.http.HttpClientTransport;
import org.apache.activemq.transport.http.HttpTransportFactory;
import org.apache.activemq.wireformat.WireFormat;
import org.apache.http.client.HttpClient;
import org.nuclos.common.http.NuclosHttpClientFactory;

/**
 * A nuclos custom extension to ActiveMQ {@link HttpTransportFactory}.
 * <p>
 * This factory is bound to ActiveMQ scheme 'myhttp' in server and client (i.e.
 * in common). It uses the (apache httpcomponents) HttpClient instance in the 
 * Spring context in the client.
 * </p>
 * @author Thomas Pasch
 */
public class NuclosHttpTransportFactory extends HttpTransportFactory {

	private HttpClient httpClient;
	
	private NuclosHttpClientFactory factory;

	public NuclosHttpTransportFactory() {
	}
	
	final HttpClient getHttpClient() {
		if (httpClient == null) {
			// Use a separate HttpClient here because ActiveMQ will
			// always set connection timeout to 30000 (30 seconds). (tp)
			
			// httpClient = NuclosHttpClientFactory.getInstance().getObject();
			factory = new NuclosHttpClientFactory();
			httpClient = factory.getObject();
		}
		return httpClient;
	}
	
	@Override
    protected Transport createTransport(URI location, WireFormat wf) throws IOException {
		if (location.getScheme().equals("myhttp")) {
			try {
				location = new URI(location.toString().substring(2));
			}
			catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
        final HttpClientTransport result = (HttpClientTransport) super.createTransport(location, wf);
        final HttpClient httpClient = getHttpClient();
        result.setReceiveHttpClient(httpClient);
        result.setSendHttpClient(httpClient);
        return result;
    }	
	
	@Override
	public void finalize() {
		if (factory != null) {
			factory.close();
		}
		factory = null;
		httpClient = null;
	}
	
}
