package org.nuclos.client.image;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

public class SVGTest {
	
	// private static final String SVG_TEMPLATE = "templates/svg/resplan.svg";
	private static final String SVG_TEMPLATE = "templates/svg/resplan.svg";
		
	private static final File EMF_OUT = new File("/home/tpasch/test.emf");
	
	private static final File SVG_OUT = new File("/home/tpasch/test.svg");
	
	private static void convert() throws Exception {
		final String uri = Thread.currentThread().getContextClassLoader().getResource(SVG_TEMPLATE).toExternalForm();
		final SVGDOMDocumentSupport sdds = new SVGDOMDocumentSupport(uri);
		final SVG2EMF converter = new SVG2EMF();
		final FileOutputStream out = new FileOutputStream(EMF_OUT);
		try {
			converter.convert(sdds.getDocument(), out);
			// converter.convert(uri, OUT);
			// SVG2EMF.convert2("file:///home/tpasch/apache-tomcat-7.0.27/webapps/docs/images/tomcat.svg", OUT);
		}
		finally {
			out.close();
		}
	}
	
	private static void alterTemplate() throws Exception {
		final String uri = Thread.currentThread().getContextClassLoader().getResource(SVG_TEMPLATE).toExternalForm();
		final SVGDOMDocumentSupport sdds = new SVGDOMDocumentSupport(uri);
		
		final Element svg =  sdds.getDocument().getDocumentElement();
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final Element g = (Element) xpath.evaluate("id('resplan')", svg, XPathConstants.NODE);
		if (g == null) {
			throw new NullPointerException("template include element not found");
		}
		
		g.appendChild(sdds.createRect(10, 10, 100, 100, "lane-grey"));
		
		final FileOutputStream out = new FileOutputStream(SVG_OUT);
		try {
			sdds.writeAsSvg(out);
		}
		finally {
			out.close();
		}
	}

	public static void main(String[] args) throws Exception {
		alterTemplate();
	}
}
