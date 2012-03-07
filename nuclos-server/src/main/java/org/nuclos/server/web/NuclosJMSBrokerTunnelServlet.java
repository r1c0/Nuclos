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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.TransportConnection;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportAcceptListener;
import org.apache.activemq.transport.http.HttpSpringEmbeddedTunnelServlet;
import org.apache.activemq.transport.http.HttpTransportFactory;
import org.apache.activemq.transport.http.HttpTransportServer;
import org.apache.activemq.transport.http.HttpTunnelServlet;
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
	
	/*
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
	 */
	
	public NuclosJMSBrokerTunnelServlet() {
	}
	
	/**
	 * Get the broker from our (existing) Spring context.
	 */
	@Override
	protected BrokerService createBroker() throws Exception {
		if (broker == null) {
			broker = (XBeanBrokerService) SpringApplicationContextHolder.getBean("broker");
		}
		return broker;
	}
	
	@Override
	public void init() throws ServletException {
		// -------------------------------------------------------------------------		
		// copied from org.apache.activemq.transport.http.HttpEmbeddedTunnelServlet (5.5.0)
        // lets initialize the ActiveMQ broker
        try {
            if (broker == null) {
                broker = createBroker();

                // Add the servlet connector
                String url = getConnectorURL();
                HttpTransportFactory factory = new HttpTransportFactory();
                transportConnector = (HttpTransportServer) factory.doBind(new URI(url));
                broker.addConnector(transportConnector);

                String brokerURL = getServletContext().getInitParameter("org.apache.activemq.brokerURL");
                if (brokerURL != null) {
                    log("Listening for internal communication on: " + brokerURL);
                }
            }
            broker.start();
        } catch (Exception e) {
            throw new ServletException("Failed to start embedded broker: " + e, e);
        }
        // now lets register the listener
        TransportAcceptListener listener = transportConnector.getAcceptListener();
        getServletContext().setAttribute("transportChannelListener", listener);
		// end of copied from org.apache.activemq.transport.http.HttpEmbeddedTunnelServlet (5.5.0)
		// -------------------------------------------------------------------------		
        
		// -------------------------------------------------------------------------
        // fix to set required parameter of HttpTunnelServlet
        // listener is always null here...
        if (listener == null) {
        	listener = new TransportAcceptListener() {
				
				@Override
				public void onAcceptError(Exception error) {
					LOG.error("accept failed: " + error, error);
				}
				
				@Override
				public void onAccept(final Transport transport) {
					try {
						// Starting the connection could block due to
						// wireformat negotiation, so start it in an async thread.
						Thread startThread = new Thread("NuclosJMSBrokerTunnelServlet: ActiveMQ Transport Initiator: " + transport.getRemoteAddress()) {
							@Override
							public void run() {
								try {
									Connection connection = createConnection(transport);
									connection.start();
								} 
								catch (Exception e) {
									ServiceSupport.dispose(transport);
									onAcceptError(e);
								}
							}
						};
						startThread.start();
					}
					catch (Exception error) {
						ServiceSupport.dispose(transport);
						onAcceptError(error);
					}
				}
        	};
            getServletContext().setAttribute("transportChannelListener", listener);
        }
        getServletContext().setAttribute("acceptListener", listener);
		// only with activemq 5.5.0
        // final HttpTransportFactory htf = new HttpTransportFactory();
        // getServletContext().setAttribute("transportFactory", htf);
        
		// -------------------------------------------------------------------------
        
		// -------------------------------------------------------------------------
        // copied from org.apache.activemq.transport.http.HttpTunnelServlet (5.5.0)
        //
        // The following is looked up in the servlet context:
        // context_name					type						variable				optional?
        // -------------------------------------------------------------------------------------------
        // acceptListener				TransportAcceptListener		listener				no
        // transportFactory				HttpTransportFactory		transportFactory		no
        // transportOptions				HashMap						transportOptions		yes
        // wireFormat					TextWireFormat				wireFormat				yes
        //
        /*
        this.listener = (TransportAcceptListener)getServletContext().getAttribute("acceptListener");
        if (this.listener == null) {
            throw new ServletException("No such attribute 'acceptListener' available in the ServletContext");
        }
        transportFactory = (HttpTransportFactory)getServletContext().getAttribute("transportFactory");
        if (transportFactory == null) {
            throw new ServletException("No such attribute 'transportFactory' available in the ServletContext");    
        }
        transportOptions = (HashMap)getServletContext().getAttribute("transportOptions");
        wireFormat = (TextWireFormat)getServletContext().getAttribute("wireFormat");
        if (wireFormat == null) {
            wireFormat = createWireFormat();
        }
         */
        // end of copied from org.apache.activemq.transport.http.HttpTunnelServlet (5.5.0)
		// -------------------------------------------------------------------------
        
        // set stuff from org.apache.activemq.transport.http.HttpTunnelServlet 
        // using reflection (to access private fields)
        setField("listener", (TransportAcceptListener)getServletContext().getAttribute("acceptListener"));
        
		// only with activemq 5.5.0
        // setField("transportFactory", (HttpTransportFactory)getServletContext().getAttribute("transportFactory"));
        // setField("transportOptions", (HashMap)getServletContext().getAttribute("transportOptions"));
        
        // setField("wireFormat", (TextWireFormat)getServletContext().getAttribute("wireFormat"));
        setField("wireFormat", createWireFormat());
	}
	
	private Connection createConnection(Transport transport) {
		final Broker b;
		try {
			b = broker.getBroker();
		}
		catch (Exception e) {
			throw new NuclosFatalException(e);
		}
		final TransportConnector con = new TransportConnector(transportConnector);
		con.setBrokerService(broker);
		final TransportConnection result = new TransportConnection(con, transport, b, null);
		result.getStatistics().setEnabled(true);
		return result;
	}
	
	private void setField(String name, Object value) throws ServletException {
		try {
			final Field field = HttpTunnelServlet.class.getDeclaredField(name);
			field.setAccessible(true);
			field.set(this, value);
		} 
		catch (SecurityException e) {
			throw new ServletException(e);
		} 
		catch (NoSuchFieldException e) {
			throw new ServletException(e);
		} 
		catch (IllegalArgumentException e) {
			throw new ServletException(e);
		} 
		catch (IllegalAccessException e) {
			throw new ServletException(e);
		}
	}
	
	/**
	 * Tidy up.
	 */
	@Override
	public void destroy() {
		super.destroy();
		try {
			transportConnector.stop();
		}
		catch (Exception e) {
			// ignore
		}
		transportConnector = null;
		
		if (broker != null) {
			try {
				broker.stop();
				broker.waitUntilStopped();
				((XBeanBrokerService) broker).destroy();
			}
			catch (Exception e) {
				// ignore
			}
		}
		broker = null;
		broker = null;
	}

	@Override
	protected String getConnectorURL() {
		return "http://localhost/" + "nuclos";
	}

}
