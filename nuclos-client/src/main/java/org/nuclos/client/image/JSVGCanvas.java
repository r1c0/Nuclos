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
package org.nuclos.client.image;

import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.JComponent;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * An improved version of the batik component.
 * 
 * @author Thomas Passch
 */
public class JSVGCanvas extends org.apache.batik.swing.JSVGCanvas {
	
	private JComponent outer;
	
	private ViewMode viewMode;
	
	public JSVGCanvas() {
		viewMode = ViewMode.ORIGINAL_SIZE;
		setRecenterOnResize(false);
	}
	
	public void setOuter(JComponent outer) {
		this.outer = outer;
	}
	
	public JComponent getOuter() {
		return outer;
	}
	
	@Override
	public void setSVGDocument(SVGDocument doc) {
		viewMode = ViewMode.ORIGINAL_SIZE;
		super.setSVGDocument(doc);
	}
	
	public void setViewMode(ViewMode viewMode) {
		if (viewMode == this.viewMode) {
			return;
		}
		switch (viewMode) {
		case ORIGINAL_SIZE:
			new ResetTransformAction().actionPerformed(null);
			break;
		case FIT_TO_PAGE:
			final AffineTransform trans = toFitTransform();
			new AffineAction(trans).actionPerformed(null);
			final AffineTransform vbt = getViewBoxTransform();
			final AffineTransform trans2 = AffineTransform.getTranslateInstance(-vbt.getTranslateX(), 0);
			new AffineAction(trans2).actionPerformed(null);
			// why we can't do: trans.concatenate(trans2); here ??? (It does not work!) (tp)
			break;
		default:
			throw new IllegalArgumentException("Unknown mode: " + viewMode);
		}
		this.viewMode = viewMode;
		repaint();
	}
	
	public ViewMode getViewMode() {
		return viewMode;
	}
	
	private AffineTransform toFitTransform() {
		AffineTransform result = null;
		// fit to screen size
		final double screenWidth = outer.getSize().getWidth();
		final double svgWidth = getSVGDocumentSize().getWidth();
		if (screenWidth > 0 && svgWidth > 0) {
			final double s = screenWidth / svgWidth - 0.01;
			if (s < 1.0) {
				result = AffineTransform.getScaleInstance(s, 1.0);
			}
			else {
				result = AffineTransform.getScaleInstance(1.0, 1.0);				
			}
		}
		return result;
	}
	
	/*
	@Override
	protected AffineTransform calculateViewingTransform(String fragIdent, SVGSVGElement svgElt) {
		final AffineTransform trans = super.calculateViewingTransform(fragIdent, svgElt);
		original = new AffineTransform(trans);
		switch (viewMode) {
		case FIT_TO_PAGE:
			// fit to screen size
			final double screenWidth = outer.getSize().getWidth();
			final double svgWidth = getSVGDocumentSize().getWidth();
			if (screenWidth > 0 && svgWidth > 0) {
				final double s = screenWidth / svgWidth - 0.01;
				if (s < 1.0) {
					trans.concatenate(AffineTransform.getScaleInstance(s, 1.0));
				}
			}
			fit = new AffineTransform(trans);
			break;
		case ORIGINAL_SIZE:
			break;
		default:
			throw new IllegalArgumentException("Unknown mode: " + viewMode);
		}
		return trans;
	}
	*/

}
