package org.nuclos.client.customcomp.resplan;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.nuclos.client.image.ImageType;
import org.nuclos.client.image.SVGDOMDocumentSupport;
import org.nuclos.client.ui.resplan.JResPlanComponent;
import org.nuclos.client.ui.resplan.header.JHeaderGrid;
import org.nuclos.common.collect.collectable.Collectable;
import org.w3c.dom.Element;

/**
 * A SVG (and more) exporter for {@link JResPlanComponent}s.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.6
 */
public class ResPlanExporter2 implements IResPlanExporter<Collectable, Collectable, Collectable> {
	
	private final JResPlanComponent<Collectable, Date, Collectable, Collectable> jrpc;
	
	private SVGDOMDocumentSupport sdds;
	
	ResPlanExporter2(JResPlanComponent<Collectable, Date, Collectable, Collectable> jrpc) {
		this.jrpc = jrpc;
	}

	@Override
	public SVGDOMDocumentSupport getSVGDOMDocumentSupport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(String template, int startCategory) throws IOException, XPathExpressionException {
		/*
		final String uri = Thread.currentThread().getContextClassLoader().getResource(template).toExternalForm();
		sdds = new SVGDOMDocumentSupport(uri);
		 */
		
		// ignore uri and startCategory
		sdds = new SVGDOMDocumentSupport();
		final SVGGraphics2D g2d = sdds.asGraphics2D();
		
		final JHeaderGrid<?> columnHeader, rowHeader;
		switch (jrpc.getOrientation()) {
		case VERTICAL:
			columnHeader = jrpc.getResourceHeader();
			rowHeader = jrpc.getTimelineHeader();
			break;
		case HORIZONTAL:
			columnHeader = jrpc.getTimelineHeader();
			rowHeader = jrpc.getResourceHeader();
			break;
		default:
			throw new IllegalStateException();
		}
		final Dimension columnDim = new Dimension(columnHeader.getWidth(), columnHeader.getHeight());
		final Dimension rowDim = new Dimension(rowHeader.getWidth(), rowHeader.getHeight());
		final Dimension mainDim = new Dimension(jrpc.getWidth(), jrpc.getHeight());
		
		g2d.translate(rowDim.width, 0);
		columnHeader.paint(g2d);
		g2d.translate(-rowDim.width, columnDim.height);
		rowHeader.paint(g2d);
		g2d.translate(rowDim.width, 0);
		jrpc.paint(g2d);
		g2d.translate(-rowDim.width, -columnDim.height);
		
		// g2d.setSVGCanvasSize(dim);
		sdds.fromGraphics2D(g2d, false);

		final Element svg = sdds.getDocument().getDocumentElement();
		svg.setAttribute("width", Integer.toString(rowDim.width + mainDim.width + 20));
		svg.setAttribute("height", Integer.toString(columnDim.height + mainDim.height + 20));
	}

	@Override
	public void save(ImageType imageType, File save) throws IOException {
		sdds.writeAs(save, imageType);
	}

}
