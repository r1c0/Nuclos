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
package org.nuclos.server.web;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.TransportConnection;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportAcceptListener;
import org.apache.activemq.transport.http.HttpEmbeddedTunnelServlet;
import org.apache.activemq.transport.http.HttpTransportServer;
import org.apache.activemq.util.ServiceSupport;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;

public class NuclosJMSBrokerTunnelServlet extends HttpEmbeddedTunnelServlet {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected BrokerService createBroker() throws Exception {
		XBeanBrokerService obj = (XBeanBrokerService) SpringApplicationContextHolder.getBean("broker");
		return obj;
	}

	@Override
	public synchronized void init() throws ServletException {
		try {
			if(broker == null)
				broker = createBroker();
			
			String url = getConnectorURL();
			transportConnector = new HttpTransportServer(new URI(url));
			
			TransportAcceptListener acceptListener = new TransportAcceptListener() {
				
				@Override
				public void onAcceptError(Exception arg0) {
					System.out.println("onerror");
				}
				
				@Override
				public void onAccept(final Transport transport) {
					try {
	                    // Starting the connection could block due to
	                    // wireformat negotiation, so start it in an async thread.
	                    Thread startThread = new Thread("ActiveMQ Transport Initiator: " + transport.getRemoteAddress()) {
	                        @Override
							public void run() {
	                            try {
	                                Connection connection = createConnection(transport);
	                                connection.start();
	                            } catch (Exception e) {
	                                ServiceSupport.dispose(transport);
	                                onAcceptError(e);
	                            }
	                        }
	                    };
	                    startThread.start();
	                } catch (Exception e) {
	                    ServiceSupport.dispose(transport);
	                    onAcceptError(e);
	                }
				}
			}; 
			transportConnector.setAcceptListener(acceptListener);
			
			
			TransportAcceptListener listener = transportConnector.getAcceptListener();			
			getServletContext().setAttribute("transportChannelListener", listener);
			getServletContext().setAttribute("acceptListener", acceptListener);
			
			super.init();
			
		}
		catch (Exception e) {
			throw new ServletException  ("Failed to start embedded broker: " + e, e);
		}
		
	}
	
	protected Connection createConnection(Transport transport) throws IOException {
        TransportConnection answer = null;
		try {
			TransportConnector con = new TransportConnector();
			con.setBrokerService(broker);
			
			answer = new TransportConnection(con, transport, broker.getBroker(), null);
			answer.getStatistics().setEnabled(true);
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}
       
        return answer;
    }

	 @Override
	 protected String getConnectorURL() {
	    return "http://localhost/" + "nuclos";
	 }


}
