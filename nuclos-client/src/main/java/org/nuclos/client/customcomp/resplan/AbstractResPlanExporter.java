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
import org.nuclos.common.dblayer.INameProducer;
import org.nuclos.common2.DateUtils;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGPolygonElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGTextElement;

public abstract class AbstractResPlanExporter<R,E> implements IResPlanExporter<R, E> {
	
	private static final Logger LOG = Logger.getLogger(AbstractResPlanExporter.class);
	
	//
	
	private static final Date LOW = new Date(-99999999L);
	
	protected static final int XPIXEL_OFFSET = 200;
	// protected static final int XPIXEL_FOR_TIME_CAT = 50;
	protected static final int YPIXEL_FOR_HEADER_CAT = 20;
	protected static final int YPIXEL_HEADER_TXT_OFFSET = 15;
	protected static final int YPIXEL_FOR_RESOURCE = 40;
	protected static final int YPIXEL_RESOURCE_BORDER = 4;
	protected static final int YPIXEL_BIGTXT_OFFSET = 28;
	
	//
	
	private final GranularityType granularity;
	private final Interval<Date> horizon; 
	private final ResPlanModel<R, Date, E> model;
	private final TimeModel<Date> time;
	
	//
	
	private INameProducer<R> resourceNameProducer;
	
	private INameProducer<E> entryNameProducer;
	
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
	
	/**
	 * current number of entries drawn.
	 */
	private int entryNumber = 0;
	
	protected AbstractResPlanExporter(GranularityType granularity, 
			Interval<Date> horizon, ResPlanModel<R, Date, E> model, TimeModel<Date> time) {
		this.granularity = granularity;
		this.horizon = horizon;
		this.model = model;
		this.time = time;
	}
	
	protected abstract int getXPixelForTimeCat();
	
	protected abstract String entryRectClass();
	
	protected abstract String entryTxtClass();
	
	protected abstract boolean entryTxtCenter();
	
	@Override
	public void setResourceNameProducer(INameProducer<R> rnp) {
		this.resourceNameProducer = rnp;
	}
	
	@Override
	public INameProducer<R> getResourceNameProducer() {
		return resourceNameProducer;
	}
	
	@Override
	public void setEntryNameProducer(INameProducer<E> enp) {
		this.entryNameProducer = enp;
	}
	
	@Override
	public INameProducer<E> getEntryNameProducer() {
		return entryNameProducer;
	}
	
	@Override
	public SVGDOMDocumentSupport getSVGDOMDocumentSupport() {
		return sdds;
	}
	
	protected final float getCurrentY() {
		return currentY;
	}
	
	protected final void setCurrentY(float currentY) {
		this.currentY = currentY;
	}
	
	protected final float addToCurrentY(float add) {
		this.currentY += add;
		return currentY;
	}
	
	protected final int getEntryNumber() {
		return entryNumber;
	}
	
	protected final SVGDOMDocumentSupport getDocumentSupport() {
		return sdds;
	}
	
	protected final ResPlanModel<R, Date, E> getModel() {
		return model;
	}
	
	@Override
	public void run(String template, int startCategory) throws IOException, XPathExpressionException {
		final String uri = Thread.currentThread().getContextClassLoader().getResource(template).toExternalForm();
		sdds = new SVGDOMDocumentSupport(uri);
		
		final Element svg =  sdds.getDocument().getDocumentElement();
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final SVGElement g = (SVGElement) xpath.evaluate("id('resplan')", svg, XPathConstants.NODE);
		if (g == null) {
			throw new XPathExpressionException("template include element not found");
		}
		makeTimeHeader(g, startCategory);
		makeResPlanModel(g);
		makeFooter(g);
		
		svg.setAttribute("width", Float.toString(maxX + XPIXEL_OFFSET + 10));
		svg.setAttribute("height", Float.toString(currentY + 10));
	}
	
	@Override
	public void save(ImageType imageType, File save) throws IOException {
		sdds.writeAs(save, imageType);
	}
	
	protected void makeTimeHeader(SVGElement g, int startCategory) {
		final TimeGranularity tg = new TimeGranularity(granularity, time);
		final int quantizer = granularity.getCalendarQuantizer();
		maxCategory = tg.getCategoryCount() - 1;
		realHorizon = new Interval<Date>(DateUtils.floorBound(horizon.getStart(), quantizer), 
				DateUtils.ceilBound(horizon.getEnd(), quantizer));
		millisForPx = DateUtils.getMillis(realHorizon.getStart(), quantizer) / getXPixelForTimeCat();
		maxX = getX(realHorizon.getEnd());
		
		for (int cat = startCategory; cat <= maxCategory; ++cat) {
			final GranularityType gt = GranularityType.getGranularityForLevel(cat);
			// final int width = (int) (gt.getApproxMillis() / millisForPx);
			final Calendar cal = Calendar.getInstance();
			final int q = gt.getCalendarQuantizer();	
			for (cal.setTime(DateUtils.floorBound(realHorizon.getStart(), q)); 
					cal.getTime().before(realHorizon.getEnd()); 
					DateUtils.montoneAdd(cal, q, 1)) {
				
				final int width = (int) (DateUtils.getMillis(cal.getTime(), q) / millisForPx);
				float x = getX(cal.getTime());
				final XCoord xc = clip(x, width);
				if (xc != null) {
					final SVGGElement group = sdds.createGroup();
					g.appendChild(group);
					
					group.appendChild(sdds.createRect(xc.x + XPIXEL_OFFSET, currentY, 
							xc.width, YPIXEL_FOR_HEADER_CAT, 
							"header"));
					// + 7: Test case: Granularity month -> Feb YY must be shown.
					if (xc.width + 7 > getXPixelForTimeCat()) {
						final String text = tg.getCategoryValue(cat, cal.getTime());
						group.appendChild(sdds.createText(xc.x + XPIXEL_OFFSET + xc.width/2, currentY + YPIXEL_HEADER_TXT_OFFSET, 
							text, "headerTxt"));
					}
				}
			}
			currentY += YPIXEL_FOR_HEADER_CAT;
		}
	}
	
	protected void makeResPlanModel(SVGElement g) {
		int nor = 0;
		for (R r: model.getResources()) {
			final float resourceStartY = currentY;
			Interval<Date> lastInterval = new Interval<Date>(LOW, LOW);
			
			SVGElement resourceText = mkResPlanResource(g, r, lastInterval);
			currentY += YPIXEL_FOR_RESOURCE;
			if (nor % 2 == 0) {
				final SVGRectElement oddLane = sdds.createRect(0, resourceStartY, maxX + XPIXEL_OFFSET, currentY - resourceStartY, "oddRow"); 
				g.insertBefore(oddLane, resourceText.getNextSibling());
			}
			lastInterval = new Interval<Date>(LOW, LOW);
			++nor;
		}
	}
	
	protected SVGTextElement mkResPlanResource(SVGElement g, R r, Interval<Date> lastInterval) {
		final String resourceName = resourceNameProducer.makeName(r);
		final SVGTextElement resourceText = sdds.createText(0, currentY + YPIXEL_BIGTXT_OFFSET, 
				resourceName, "bigTxt"); 
		g.appendChild(resourceText);
		
		final List<? extends E> entries = model.getEntries(r);
		for (E e: entries) {
			mkResPlanEntry(g, e, lastInterval);
		}
		return resourceText;
	}
	
	protected void mkResPlanEntry(SVGElement g, E e, Interval<Date> lastInterval) {
		final Interval<Date> i = model.getInterval(e);
		if (lastInterval != null && i.intersects(lastInterval)) {
			beforeNextLineInSameResource(g, lastInterval);
			currentY += YPIXEL_FOR_RESOURCE;
		}

		final String entryName = entryNameProducer.makeName(e);
		final XCoord xc = clip(i);
		if (xc != null) {
			final SVGGElement group = sdds.createGroup();
			g.appendChild(group);
			
			final SVGRectElement rect = sdds.createRect(xc.x + XPIXEL_OFFSET, currentY + YPIXEL_RESOURCE_BORDER, xc.width, 
					YPIXEL_FOR_RESOURCE - 2 * YPIXEL_RESOURCE_BORDER, entryRectClass());
			final SVGTextElement text;
			if (entryTxtCenter()) {
				text = sdds.createText(xc.x + XPIXEL_OFFSET + xc.width/2, currentY + YPIXEL_BIGTXT_OFFSET, entryName, entryTxtClass());				
			}
			else {
				text = sdds.createText(xc.x + XPIXEL_OFFSET, currentY + YPIXEL_BIGTXT_OFFSET, entryName, entryTxtClass());
			}
			group.appendChild(rect);
			group.appendChild(text);
		}
		lastInterval.set(i);
		++entryNumber;
	}
	
	protected void beforeNextLineInSameResource(SVGElement g, Interval<Date> lastInterval) {
	}
	
	protected void makeFooter(SVGElement g) {
	}
	
	protected void mkRhomb(SVGElement g, float x, float y, float size, String clazz) {
		final XCoord xc = clip(x, 1);
		if (xc != null) {
			final SVGPolygonElement result = sdds.createPolygon(clazz, x - size, y, x, y - size, x + size, y, x, y + size);
			g.appendChild(result);
		}
	}
	
	protected float getX(Date d) {
		final long millis = d.getTime() - realHorizon.getStart().getTime();
		return millis / millisForPx;
	}
	
	protected XCoord clip(Interval<Date> i) {
		// here x might be negative
		float x = getX(i.getStart());
		final float width = getX(i.getEnd()) - x;
		return clip(x, width);
	}
	
	protected XCoord clip(float x, float width) {
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
		// assert realWidth >= 0 && realWidth <= maxX : "realWidth: " + realWidth;
		if (realWidth < 0 && realWidth > maxX) {
			return null;
		}
		x = Math.max(x, 0);
		
		final XCoord result = new XCoord();
		result.x = x;
		result.width = realWidth;
		return result;
	}
	
	protected static class XCoord {
		protected float x;
		protected float width;
	}
	
}
