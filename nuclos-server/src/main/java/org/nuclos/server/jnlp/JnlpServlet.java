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
package org.nuclos.server.jnlp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Servlet for dynamic jnlp generation.
 * The codebase for the webstart client is /app.
 *
 * The servlet uses /WEB-INF/jnlp/jnlp.xsl as generation template and /WEB-INF/jnlp/jnlp.properties as a source for parameters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class JnlpServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(JnlpServlet.class);

	private boolean enabled = false;
	private boolean singleinstance = false;
	private File appDir = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		
		try {
			Properties props = new Properties();
			props.load(config.getServletContext().getResourceAsStream("/WEB-INF/jnlp/jnlp.properties"));
			enabled = Boolean.parseBoolean(props.getProperty("webstart.enabled"));
			singleinstance = Boolean.parseBoolean(props.getProperty("singleinstance"));
			appDir = new File(config.getServletContext().getRealPath(""), "app");
		}
		catch(IOException e) {
			log.error("Failed to initialize JnlpServlet.", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!enabled) {
			response.sendError(response.SC_NOT_FOUND, "Webstart is disabled.");
			return;
		}
		
		String urlPrefix = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

		Properties props = new Properties();
		props.put("codebase", urlPrefix + "/app");
		props.put("url.remoting", urlPrefix + "/remoting");
		props.put("url.jms", urlPrefix + "/jmsbroker");
		props.put("singleinstance", Boolean.toString(singleinstance));
		
		String attachment = "inline; filename=\"nuclos.jnlp\"";
		response.setContentType("application/x-java-jnlp-file");
		response.setHeader("Cache-Control", "max-age=30");
		response.setHeader("Content-disposition", attachment);
		
		InputStream is = getServletContext().getResourceAsStream("/WEB-INF/jnlp/jnlp.xsl");

		try {
			TransformerFactory transformFactory = TransformerFactory.newInstance();
			Transformer transformer = transformFactory.newTransformer(new StreamSource(is));
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			for (Map.Entry<Object, Object> e : props.entrySet()) {
				transformer.setParameter((String)e.getKey(), e.getValue());
			}

			Result output = new StreamResult(response.getOutputStream());
			transformer.setErrorListener(new ErrorListener() {
				@Override
				public void error(TransformerException exception) throws TransformerException {
					System.err.println("error: " + exception);
				}

				@Override
				public void fatalError(TransformerException exception) throws TransformerException {
					System.err.println("fatalError: " + exception);
				}

				@Override
				public void warning(TransformerException exception) throws TransformerException {
					System.err.println("warning: " + exception);
				}
			});

			transformer.transform(new DOMSource(getTransformationSource(request.getParameterMap())), output);
		}
		catch (Exception ex) {
			throw new ServletException(ex);
		}

		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	private Document getTransformationSource(Map<?, ?> parameters) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Node jnlp = document.appendChild(document.createElement("jnlp"));
        Node jars = jnlp.appendChild(document.createElement("jars"));
        
        if(appDir.isDirectory()) {
        	String sFiles [] = appDir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if(name.endsWith(".jar") && !(name.equals("nuclos-client.jar") || name.equals("nuclos-native.jar")))
						return true;
					else
						return false;
				}
			});
        	
        	for(String sFile : sFiles) {
        		Node jar = jars.appendChild(document.createElement("jar"));
    			jar.setTextContent(sFile);
        	}
        }

        Node arguments = jnlp.appendChild(document.createElement("arguments"));

        // convert map-like request parameters to program-arguments
        for (Map.Entry<?, ?> e : parameters.entrySet()) {
        	Node argument = document.createElement("argument");
        	argument.setTextContent(e.getKey() + (e.getValue() != null ? "=" + ((String[]) e.getValue())[0] : ""));
        	arguments.appendChild(argument);
        }

        return document;
	}

}
