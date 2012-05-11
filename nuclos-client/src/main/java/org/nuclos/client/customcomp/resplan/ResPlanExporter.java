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
package org.nuclos.client.customcomp.resplan;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.nuclos.client.customcomp.resplan.ResPlanController.GranularityType;
import org.nuclos.client.customcomp.resplan.ResPlanController.TimeGranularity;
import org.nuclos.client.image.ImageType;
import org.nuclos.client.image.SVGDOMDocumentSupport;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.ResPlanModel;
import org.nuclos.client.ui.resplan.TimeModel;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.dblayer.CollectableNameProducer;
import org.nuclos.common2.DateUtils;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGTextElement;

public class ResPlanExporter {
	
	private static final Logger LOG = Logger.getLogger(ResPlanExporter.class);
	
	private static final String SVG_TEMPLATE = "templates/svg/resplan.svg";
	
	//
	
	private static final int XPIXEL_OFFSET = 200;
	private static final int XPIXEL_FOR_TIME_CAT = 50;
	private static final int YPIXEL_FOR_HEADER_CAT = 20;
	private static final int YPIXEL_HEADER_TXT_OFFSET = 15;
	private static final int YPIXEL_FOR_RESOURCE = 40;
	private static final int YPIXEL_RESOURCE_BORDER = 4;
	private static final int YPIXEL_BIGTXT_OFFSET = 28;
	
	//
	
	private final ImageType imageType;
	private final File save;
	private final CollectableNameProducer resourceNameProducer;
	private final CollectableNameProducer entryNameProducer;
	private final GranularityType granularity;
	private final Interval<Date> horizon; 
	private final ResPlanModel<Collectable, Date, Collectable> model;
	private final TimeModel<Date> time;
	
	//
	
	private SVGDOMDocumentSupport sdds;
	
	private int maxCategory = -1;
	
	/**
	 * Horizon quantized to scale.
	 */
	private Interval<Date> realHorizon;
	
	/**
	 * Maximum x-Position. (Minimal x-Position is 0).
	 */
	private float maxX;
	
	/**
	 * How many timeMillis are one pixel (1px).
	 */
	private long millisForPx;
	
	/**
	 * Current y-Position.
	 */
	private float currentY = 0.0f;
	
	public ResPlanExporter(ImageType imageType, File save, ResPlanResourceVO vo, GranularityType granularity, 
			Interval<Date> horizon, ResPlanModel<Collectable, Date, Collectable> model, TimeModel<Date> time) {
		this.imageType = imageType;
		this.save = save;
		this.resourceNameProducer = new CollectableNameProducer(vo.getResourceLabel());
		this.entryNameProducer = new CollectableNameProducer(vo.getBookingLabel());
		this.granularity = granularity;
		this.horizon = horizon;
		this.model = model;
		this.time = time;
	}
	
	public void run() throws IOException, XPathExpressionException {
		final String uri = Thread.currentThread().getContextClassLoader().getResource(SVG_TEMPLATE).toExternalForm();
		sdds = new SVGDOMDocumentSupport(uri);
		
		final Element svg =  sdds.getDocument().getDocumentElement();
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final SVGElement g = (SVGElement) xpath.evaluate("id('resplan')", svg, XPathConstants.NODE);
		if (g == null) {
			throw new XPathExpressionException("template include element not found");
		}
		makeTimeHeader(g);
		makeResPlanModel(g);
		
		svg.setAttribute("width", Float.toString(maxX + XPIXEL_OFFSET + 10));
		svg.setAttribute("height", Float.toString(currentY + 10));
		sdds.writeAs(save, imageType);
	}
	
	private void makeTimeHeader(SVGElement g) {
		final TimeGranularity tg = new TimeGranularity(granularity, time);
		final int quantizer = granularity.getCalendarQuantizer();
		maxCategory = tg.getCategoryCount() - 1;
		realHorizon = new Interval<Date>(DateUtils.floorBound(horizon.getStart(), quantizer), 
				DateUtils.ceilBound(horizon.getEnd(), quantizer));
		millisForPx = DateUtils.getMillis(realHorizon.getStart(), quantizer) / XPIXEL_FOR_TIME_CAT;
		maxX = getX(realHorizon.getEnd());
		
		for (int cat = 0; cat <= maxCategory; ++cat) {
			final GranularityType gt = GranularityType.getGranularityForLevel(cat);
			// final int width = (int) (gt.getApproxMillis() / millisForPx);
			final Calendar cal = Calendar.getInstance();
			final int q = gt.getCalendarQuantizer();	
			for (cal.setTime(DateUtils.floorBound(realHorizon.getStart(), q)); 
					cal.getTime().before(realHorizon.getEnd()); 
					DateUtils.montoneAdd(cal, q, 1)) {
				
				final int width = (int) (DateUtils.getMillis(cal.getTime(), q) / millisForPx);
				// here x might be negative
				float x = getX(cal.getTime());
				float realWidth;
				if (x < 0) {
					realWidth = width + x;
				}
				else if (x + width > maxX) {
					realWidth = maxX - x;
				}
				else {
					realWidth = width;
				}
				assert realWidth >= 0 && realWidth <= maxX : "realWidth: " + realWidth;
				x = Math.max(x, 0);
				g.appendChild(sdds.createRect(x + XPIXEL_OFFSET, currentY, 
						realWidth, YPIXEL_FOR_HEADER_CAT, 
						"header"));
				if (realWidth + 3 > XPIXEL_FOR_TIME_CAT) {
					final String text = tg.getCategoryValue(cat, cal.getTime());
					g.appendChild(sdds.createText(x + XPIXEL_OFFSET + realWidth/2, currentY + YPIXEL_HEADER_TXT_OFFSET, 
						text, "headerTxt"));
				}
			}
			currentY += YPIXEL_FOR_HEADER_CAT;
		}
	}
	
	private void makeResPlanModel(SVGElement g) {
		int nor = 0;
		for (Collectable r: model.getResources()) {
			final float resourceStartY = currentY;
			Interval<Date> lastInterval = null;
			
			final String resourceName = resourceNameProducer.makeName(r);
			final SVGTextElement resourceText = sdds.createText(0, currentY + YPIXEL_BIGTXT_OFFSET, 
					resourceName, "bigTxt"); 
			g.appendChild(resourceText);
			
			final List<? extends Collectable> entries = model.getEntries(r);
			for (Collectable e: entries) {
				final Interval<Date> i = model.getInterval(e);
				if (lastInterval != null && i.intersects(lastInterval)) {
					currentY += YPIXEL_FOR_RESOURCE;
				}

				final String entryName = entryNameProducer.makeName(e);
				final float x = getX(i.getStart());
				final float width = getX(i.getEnd()) - x;
				final SVGRectElement rect = sdds.createRect(x + XPIXEL_OFFSET, currentY + YPIXEL_RESOURCE_BORDER, width, 
						YPIXEL_FOR_RESOURCE - 2 * YPIXEL_RESOURCE_BORDER, "lane-grey");
				final SVGTextElement text = sdds.createText(x + XPIXEL_OFFSET, currentY + YPIXEL_BIGTXT_OFFSET, entryName, "bigTxt");
				g.appendChild(rect);
				g.appendChild(text);
				lastInterval = i;
			}
			currentY += YPIXEL_FOR_RESOURCE;
			if (nor % 2 == 0) {
				final SVGRectElement oddLane = sdds.createRect(0, resourceStartY, maxX + XPIXEL_OFFSET, currentY - resourceStartY, "oddRow"); 
				g.insertBefore(oddLane, resourceText.getNextSibling());
			}
			lastInterval = null;
			++nor;
		}
	}
	
	private float getX(Date d) {
		final long millis = d.getTime() - realHorizon.getStart().getTime();
		return millis / millisForPx;
	}
	
	private float getBoundedX(Date d) {
		final float result;
		if (d.before(realHorizon.getStart())) {
			result = 0.0f;
		}
		else if (d.after(realHorizon.getEnd())) {
			result = getX(realHorizon.getEnd());
		}
		else {
			result = getX(d);
		}
		return result;
	}
	
}
