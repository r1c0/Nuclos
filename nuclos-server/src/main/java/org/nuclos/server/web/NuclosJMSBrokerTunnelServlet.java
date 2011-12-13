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
import org.apache.activemq.transport.http.HttpSpringEmbeddedTunnelServlet;
import org.apache.activemq.transport.http.HttpTransportFactory;
import org.apache.activemq.transport.http.HttpTransportServer;
import org.apache.activemq.util.ServiceSupport;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;

/**
 * TODO: There must be a extremely more convenient way to do this... (tp)
 */
public class NuclosJMSBrokerTunnelServlet extends HttpSpringEmbeddedTunnelServlet {
	
	private static final Logger LOG = Logger.getLogger(NuclosJMSBrokerTunnelServlet.class);
	
	/**
	 * <p>
	 * ActiveMQ HTTP seems to always need an jetty. See http://activemq.apache.org/http-and-https-transports-reference.html
	 * for details. Alternative transport are available as well, see http://activemq.apache.org/configuring-transports.html.
	 * Websocket http://activemq.apache.org/websockets.html would be fine, but can we use that with tomcat?
	 * </p>
	 * <p>
	 * Here are some links for using Servlets with Spring:
	 * <ul>
	 *   <li>http://andykayley.blogspot.com/2007/11/how-to-inject-spring-beans-into.html</li>
	 *   <li>http://javageek.org/2005/09/23/accessing_a_spring_bean_from_a_servlet.html</li>
	 *   <li>http://static.springsource.org/spring/docs/2.5.x/reference/mvc.html</li>
	 * </ul>
	 * </p>
	 * @deprecated There is a httpTransportFactory within HttpEmbeddedTunnelServlet, hence
	 * 		there must be away to use it (and HttpTransportServer as well). (tp)
	 */
	private HttpTransportFactory myTransportFactory;
	
	private XBeanBrokerService brokerService;
	
	private HttpTransportServer htServer;
	
	@Override
	protected BrokerService createBroker() throws Exception {
		if (brokerService == null) {
			brokerService = (XBeanBrokerService) SpringApplicationContextHolder.getBean("broker");
		}
		return brokerService;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		myTransportFactory = null;
		if (htServer != null) {
			try {
				htServer.stop();
			}
			catch (Exception e) {
				// ignore
			}
		}
		htServer = null;
		transportConnector = null;
		
		
		if (brokerService != null) {
			try {
				brokerService.stop();
				brokerService.waitUntilStopped();
				brokerService.destroy();
			}
			catch (Exception e) {
				// ignore
			}
		}
		brokerService = null;
		broker = null;
	}

	@Override
	public synchronized void init() throws ServletException {
		try {
			if(broker == null)
				broker = createBroker();
			
			String url = getConnectorURL();
			myTransportFactory = new HttpTransportFactory();
			htServer = new HttpTransportServer(new URI(url), myTransportFactory);
			transportConnector = htServer;
			
			TransportAcceptListener acceptListener = new TransportAcceptListener() {
				
				@Override
				public void onAcceptError(Exception e) {
					LOG.error("init failed: " + e, e);
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
			getServletContext().setAttribute("transportFactory", myTransportFactory);
			getServletContext().setAttribute("transportChannelListener", listener);
			getServletContext().setAttribute("acceptListener", acceptListener);
			
			super.init();
		}
		catch (Exception e) {
			throw new ServletException("Failed to start embedded broker: " + e, e);
		}
	}
	
	protected Connection createConnection(Transport transport) throws IOException {
        TransportConnection answer = null;
		try {
			TransportConnector con = new TransportConnector();
			con.setBrokerService(broker);
			con.setServer(htServer);
			
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
