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
package org.nuclos.installer.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.nuclos.installer.InstallException;
import org.nuclos.installer.util.FileUtils;
import org.nuclos.installer.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Catalina {

	private static final String XPATH_SERVER_HTTP_CONNECTOR = "/Server/Service[@name='Catalina']/Connector[@protocol='HTTP/1.1']";
	private static final String XPATH_SERVER_HTTP_CONNECTOR_PORT = XPATH_SERVER_HTTP_CONNECTOR + "/@port";

	private static final String XPATH_SERVER = "/Server";
	private static final String XPATH_SERVER_PORT = XPATH_SERVER + "/@port";

	private static final String XPATH_SERVER_AJP_CONNECTOR = "/Server/Service[@name='Catalina']/Connector[@protocol='AJP/1.3']";

	private static final String XPATH_SERVER_SERVICE = "/Server/Service[@name='Catalina']";

	private final File catalinaHome;
	private final File bootstrapJar;
	private final File confUserFile;
	private final File confServerFile;
	private final File catalinaScript;
	private Manifest manifest;

	public Catalina(String catalinaHome, boolean isWindows) {
		this(new File(catalinaHome), isWindows);
	}

	public Catalina(File catalinaHome, boolean isWindows) {
		this.catalinaHome = catalinaHome.getAbsoluteFile();
		this.confServerFile = new File(catalinaHome, "conf/server.xml");
		this.bootstrapJar = new File(catalinaHome, "bin/bootstrap.jar");
		this.confUserFile = new File(catalinaHome, "conf/tomcat-users.xml");
		this.catalinaScript =  new File(catalinaHome, isWindows ? "bin/catalina.bat" : "bin/catalina.sh");
	}

	public Manifest checkTomcat() throws InstallException {
		if (manifest == null) {
			if (!catalinaHome.exists())
				throw new InstallException("Tomcat home directory does not exist " + catalinaHome);
			for (File f : new File[] {bootstrapJar, confServerFile, confUserFile, catalinaScript}) {
				if (!f.exists()) {
					throw new InstallException("Invalid tomcat installion, missing " + f);
				}
			}
			try {
				manifest = FileUtils.extractManifest(bootstrapJar);
			} catch (IOException ex) {
				throw new InstallException("Error accessing " + bootstrapJar, ex);
			}
		}
		return manifest;
	}

	/**
	 * Get the currently configured port of the HTTP connector.
	 */
	public String getServerHttpPort() throws IOException, SAXException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_HTTP_CONNECTOR_PORT);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		return (node != null) ? node.getNodeValue() : null;
	}

	/**
	 * Configures the port of the HTTP connector.
	 */
	public void configureServerHttpPort(String port) throws IOException, SAXException, InstallException, TransformerException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_HTTP_CONNECTOR_PORT);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		if (node != null) {
			node.setNodeValue(port);
			XmlUtils.writeDocument(xml, confServerFile);
		} else {
			throw new InstallException("Cannot configure HTTP port");
		}
	}

	/**
	 * Diable the HTTP connector.
	 */
	public void disableHttpConnector() throws IOException, SAXException, InstallException, TransformerException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_HTTP_CONNECTOR);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		if (node != null) {
			node.getParentNode().removeChild(node);
			XmlUtils.writeDocument(xml, confServerFile);
		} else {
			throw new InstallException("Cannot disable AJP connector.");
		}
	}

	/**
	 * Configures the shutdown port.
	 */
	public void configureServerShutdownPort(String port) throws IOException, SAXException, InstallException, TransformerException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_PORT);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		if (node != null) {
			node.setNodeValue(port);
			XmlUtils.writeDocument(xml, confServerFile);
		} else {
			throw new InstallException("Cannot configure shutdown port");
		}
	}

	/**
	 * Diable the AJP connector.
	 */
	public void disableAjpConnector() throws IOException, SAXException, InstallException, TransformerException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_AJP_CONNECTOR);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		if (node != null) {
			node.getParentNode().removeChild(node);
			XmlUtils.writeDocument(xml, confServerFile);
		} else {
			throw new InstallException("Cannot disable AJP connector.");
		}
	}

	/**
	 * Enable HTTPS connector.
	 * <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
     *  maxThreads="150" scheme="https" secure="true"
     *  clientAuth="false" sslProtocol="TLS" />
	 */
	public void configureHttpsConnector(String port, String keystorePath, String keystorePass) throws IOException, SAXException, InstallException, TransformerException {
		Document xml = XmlUtils.readDocument(confServerFile);
		Node node;
		try {
			node = XmlUtils.getXPathNode(xml, XPATH_SERVER_SERVICE);
		} catch (XPathExpressionException ex) {
			throw new SAXException(ex);
		}
		if (node != null) {
			Element httpsConnector = xml.createElement("Connector");
			httpsConnector.setAttribute("port", port);
			httpsConnector.setAttribute("keystoreFile", keystorePath);
			httpsConnector.setAttribute("keystorePass", keystorePass);
			httpsConnector.setAttribute("protocol", "HTTP/1.1");
			httpsConnector.setAttribute("SSLEnabled", "true");
			httpsConnector.setAttribute("maxThreads", "150");
			httpsConnector.setAttribute("scheme", "https");
			httpsConnector.setAttribute("secure", "true");
			httpsConnector.setAttribute("clientAuth", "false");
			httpsConnector.setAttribute("sslProtocol", "TLS");
			node.appendChild(httpsConnector);
			XmlUtils.writeDocument(xml, confServerFile);
		} else {
			throw new InstallException("Cannot disable AJP connector.");
		}
	}

	public ProcessBuilder buildStartProcess() throws InstallException {
		return buildCatalinaProcess("run");
	}

	public ProcessBuilder buildStopProcess() throws InstallException {
		return buildCatalinaProcess("stop");
	}

	public ProcessBuilder buildCatalinaProcess(String...args) throws InstallException {
		List<String> command = new ArrayList<String>();
		command.add(catalinaScript.getAbsolutePath());
		command.addAll(Arrays.asList(args));

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.environment().put("CATALINA_HOME", catalinaHome.getAbsolutePath());
		return pb;
	}

	public File checkAppExists(String appName) {
		File descriptor = checkDescriptor(appName);
		return (descriptor != null) ? descriptor : checkWebappsDir(appName);
	}

	public File checkDescriptor(String appName) {
		File contextXml = new File(catalinaHome, "conf/Catalina/localhost/" + appName + ".xml");
		return contextXml.exists() ? contextXml.getAbsoluteFile() : null;
	}

	public File checkWebappsDir(String appName) {
		File webApp = new File(catalinaHome, "webapps/" + appName);
		return webApp.exists() ? webApp.getAbsoluteFile() : null;
	}

	public void undeploy(String appName) throws IOException {
		File contextXml = new File(catalinaHome, "conf/Catalina/localhost/" + appName + ".xml");
		File webApp = new File(catalinaHome, "webapps/" + appName);
		if (contextXml.exists())
			FileUtils.delete(contextXml, false);
		if (contextXml.exists())
			FileUtils.delete(webApp, true);
	}
}
