//Copyright (C) 2011  Novabit Informationssysteme GmbH
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.awt.Image;

/**
 * An replacement for {@link java.awt.Image#getScaledInstance(int, int, int)}.
 * <p>
 * Because of a java bug (see below), it is <em>not</em> save to scale an image with this method.
 * In addition, the method is relatively slow (see below). Hence we use the method
 * described in 'Performance of getScaledInstance'. 
 * </p>
 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6887286">
 * 		The Java Bug</a>
 * @see <a href="http://frickelblog.wordpress.com/2009/06/08/fast-image-scaling-in-java/">
 * 		Performance of getScaledInstance</a>
 * @since Nuclos 3.3
 * @author Thomas Pasch
 */
public class ImageScaler {
	
	private ImageScaler() {
		// Never invoked.
	}

	public static Image scaleImage(Image img, Dimension d) {
		if (d.width <= 0 && d.height <= 0) {
			throw new IllegalArgumentException();
		}
		
		final int w = (int) img.getWidth(null);
		final int h = (int) img.getHeight(null);
		final float factor = getBinFactor(w, h, d);
		img = scaleByHalf(img, w, h, factor);
		
		img = scaleExact(img, w, h, d);
		return img;
	}

	public static Image scaleImage(Image img, int dw, int dh) {
		if (dw <= 0 && dh <= 0) {
			throw new IllegalArgumentException();
		}
		
		final int w = (int) img.getWidth(null);
		final int h = (int) img.getHeight(null);
		if (dw < 0) {
			dw = (int) (((float) w) / dh * h);
		}
		else if (dh < 0) {
			dh = (int) (((float) h) / dw * w);
		}
		final Dimension d = new Dimension(dw, dh);
		
		final float factor = getBinFactor(w, h, d);
		img = scaleByHalf(img, w, h, factor);
		
		img = scaleExact(img, w, h, d);
		return img;
	}

	private static Image scaleByHalf(Image img, int ww, int hh, float factor) {
		// make new size
		final int w = (int) (ww * factor);
		final int h = (int) (hh * factor);
		if (ww == w && hh == h) {
			return img;
		}
		
		final BufferedImage scaled = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION ,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
		
		g.drawImage(img, 0, 0, w, h, null);
		g.dispose();
		return (Image) scaled;
	}

	private static Image scaleExact(Image img, int ww, int hh, Dimension d) {
		final float factor = getFactor(ww, hh, d);

		// create the image
		final int w = (int) (ww * factor);
		final int h = (int) (hh * factor);
		if (ww == w && hh == h) {
			return img;
		}
		
		final BufferedImage scaled = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = scaled.createGraphics();	
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION ,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
		
		g.drawImage(img, 0, 0, w, h, null);
		g.dispose();
		return scaled;
	}

	private static float getBinFactor(int width, int height, Dimension dim) {
		float factor = 1;
		final float target = getFactor(width, height, dim);
		if (target <= 1) {
			while (factor / 2 > target) {
				factor /= 2;
			}
		}
		else {
			while (factor * 2 < target) {
				factor *= 2;
			}
		}
		return factor;
	}

	private static float getFactor(int width, int height, Dimension dim) {
		final float sx = dim.width / (float) width;
		final float sy = dim.height / (float) height;
		if (sx <= 0 || sy <= 0) {
			throw new IllegalArgumentException("width=" + width + " height=" + height + " dim=" + dim 
					+ " sx=" + sx + " sy=" + sy);
		}
		final float result = Math.min(sx, sy);
		return result;
	}
	
}
