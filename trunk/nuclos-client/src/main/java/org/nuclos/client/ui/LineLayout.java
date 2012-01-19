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
package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Custom LayoutManager that lays out its component as a horizontal or vertical line.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LineLayout implements LayoutManager {

	/* possible extensions (only if really needed, don't make this class too complicated,
	 *   use GridBagLayout instead!):
	 *   align=left/center/right (default: left) bzw. top/center/bottom (default: top)
	 *      Alternative: 0.0 bis 1.0
	 *   anchor (wie in GridBagConstraints) - default: northwest
	 */

	/**
	 * layout the components horizontally
	 */
	public static final int HORIZONTAL = 1;

	/**
	 * layout the components vertically
	 */
	public static final int VERTICAL = 2;

	/**
	 * the orientation specifies how the components are laid out
	 */
	private final int iOrientation;

	/**
	 * the gap between each two components
	 */
	private final int iGap;

	/**
	 * Fill each component horizontally (if orientation is vertical) or vice versa?
	 */
	private final boolean bFill;

	/**
	 * @param iOrientation specifies if the components are laid out horizontally or vertically
	 * Fills each component horizontally (if orientation is vertical) and vice versa?
	 */
	public LineLayout(int iOrientation) {
		this(iOrientation, 0, true);
	}

	/**
	 * @param iOrientation specifies if the components are laid out horizontally or vertically
	 * @param iGap the gap between each two components
	 * @param bFill	Fill each component horizontally (if orientation is vertical) and vice versa?
	 */
	public LineLayout(int iOrientation, int iGap, boolean bFill) {
		this.iOrientation = iOrientation;
		this.iGap = iGap;
		this.bFill = bFill;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// do nothing
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// do nothing
	}

	@Override
	public void layoutContainer(Container parent) {
		final Component[] acomp = parent.getComponents();
		final Insets insets = parent.getInsets();
		final int iInsetsHeight = insets.top + insets.bottom;
		final int iInsetsWidth = insets.left + insets.right;
		int x = insets.left;
		int y = insets.top;

		/* The algorithm for the distribution of available space is as follows (for the HORIZONTAL case):
		   Each component gets its minimum width at least, plus an amount of distributable space,
		   distributed among the components depending on how much they can take:

		   width_i = min_i + distributionFactor * distributableSpace,

			 where
			   distributionFactor = (pref_i - min_i) / (pref_total - min_total)
			   distributableSpace = width_total - min_total
		*/

		switch (iOrientation) {
			case HORIZONTAL: {
				final int iMaxHeight = parent.getHeight() - iInsetsHeight;
				final int iContainerWidth = parent.getWidth();
				final int iContainerMinWidth = parent.getMinimumSize().width;
				final int iContainerPrefWidth = parent.getPreferredSize().width;
				final int iTotalDelta = (iContainerPrefWidth - iContainerMinWidth);
				final int iDistributableWidth = iContainerWidth - iContainerMinWidth;
				for (Component comp : acomp) {
					comp.setLocation(x, y);
					final Dimension dimPrefSize = comp.getPreferredSize();
					final int iMinWidth = comp.getMinimumSize().width;
					final int iDistributionFactor = (iTotalDelta == 0) ? 0 : (dimPrefSize.width - iMinWidth) / iTotalDelta;
					final int iWidth = Math.min(iMinWidth + iDistributionFactor * iDistributableWidth, dimPrefSize.width);
					final int iHeight = bFill ? iMaxHeight : dimPrefSize.height;
					comp.setSize(iWidth, iHeight);
					x += iWidth;
					x += iGap;
				}
				break;
			}
			case VERTICAL: {
				final int iMaxWidth = parent.getWidth() - iInsetsWidth;
				final int iContainerHeight = parent.getHeight();
				final int iContainerMinHeight = parent.getMinimumSize().height;
				final int iContainerPrefHeight = parent.getPreferredSize().height;
				final int iTotalDelta = (iContainerPrefHeight - iContainerMinHeight);
				final int iDistributableHeight = iContainerHeight - iContainerMinHeight;
				for (Component comp : acomp) {
					comp.setLocation(x, y);
					final Dimension dimPrefSize = comp.getPreferredSize();
					final int iMinHeight = comp.getMinimumSize().height;
					final int iDistributionFactor = (iTotalDelta == 0) ? 0 : (dimPrefSize.height - iMinHeight) / iTotalDelta;
					final int iHeight = Math.min(iMinHeight + iDistributionFactor * iDistributableHeight, dimPrefSize.height);
					final int iWidth = bFill ? iMaxWidth : dimPrefSize.width;
					comp.setSize(iWidth, iHeight);
					y += iHeight;
					y += iGap;
				}
				break;
			}
			default:
				assert false;
		}	// switch
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int width = 0;
		int height = 0;

		final Component[] acomp = parent.getComponents();

		switch (iOrientation) {
			case HORIZONTAL: {
				// width: summed minimum width of components + gaps
				// height: maximum minimum height of components
				for (Component comp : acomp) {
					final Dimension size = comp.getMinimumSize();
					height = Math.max(height, size.height);
					width += size.width;
				}
				width += (acomp.length - 1) * iGap;
				break;
			}
			case VERTICAL: {
				// width: maximum minimum width of components
				// height: summed minimum height of components + gaps
				for (Component comp : acomp) {
					final Dimension size = comp.getMinimumSize();
					width = Math.max(width, size.width);
					height += size.height;
				}
				height += (acomp.length - 1) * iGap;
				break;
			}

			default:
				assert false;
		}	// switch

		final Dimension result = new Dimension(width, height);

		addInsetsToDimension(parent.getInsets(), result);

		return result;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int width = 0;
		int height = 0;

		final Component[] acomp = parent.getComponents();

		switch (iOrientation) {
			case HORIZONTAL: {
				// width: summed preferred width of components + gaps
				// height: maximum preferred height of components
				for (Component comp : acomp) {
					final Dimension size = comp.getPreferredSize();
					height = Math.max(height, size.height);
					width += size.width;
				}
				width += (acomp.length - 1) * iGap;
				break;
			}
			case VERTICAL: {
				// width: maximum preferred width of components
				// height: summed preferred height of components + gaps
				for (Component comp : acomp) {
					final Dimension size = comp.getPreferredSize();
					width = Math.max(width, size.width);
					height += size.height;
				}
				height += (acomp.length - 1) * iGap;
				break;
			}
			default:
				assert false;
		}	// switch

		final Dimension result = new Dimension(width, height);

		addInsetsToDimension(parent.getInsets(), result);

		return result;
	}

	private void addInsetsToDimension(Insets insets, Dimension dim) {
		dim.width = (int) Math.min((long) dim.width + (long) insets.left + insets.right, Integer.MAX_VALUE);
		dim.height = (int) Math.min((long) dim.height + (long) insets.top + insets.bottom, Integer.MAX_VALUE);
	}

}	// class LineLayout
