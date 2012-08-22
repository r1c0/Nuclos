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
package org.nuclos.client.statemodel.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.nuclos.common.NuclosImage;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.client.common.Utils;
import org.nuclos.client.gef.AbstractShapeViewer;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.ContainerShape;
import org.nuclos.client.ui.Icons;
import org.nuclos.server.common.InstanceConstants;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Shape representing a state (in a state model).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

public class StateShape extends ContainerShape implements ImageObserver {
	public static final int STATE_START = 1;
	public static final int STATE_END = 2;
	public static final int STATE_INTERMEDIATE = 3;

	private StateVO statevo;
	private Icon icon = Icons.getInstance().getIconStateIntermediate();
	private final Font textFont = new Font("Arial", Font.TRUETYPE_FONT, 10);
	private AttributedString attrString;

	public StateShape() {
		this(null);
	}

	/**
	 * @param statevo
	 */
	public StateShape(StateVO statevo) {
		this(statevo, 0d, 0d, 120d, 48d);
	}

	/**
	 * @param statevo
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 */
	private StateShape(StateVO statevo, double dX, double dY, double dWidth, double dHeight) {
		super(dX, dY, dWidth, dHeight);
		this.statevo = statevo;
		setMinimumSize(120d, 48d);
		this.bgColor = new Color(240, 240, 240);
		this.borderColor = new Color(90, 90, 90);
		this.borderSize = 2d;
		setConnectable(true);
		setResizeable(true);
		connectionPointColor = Color.RED;
		checkStatus();
	}

	public StateVO getStateVO() {
		return statevo;
	}

	@Override
	protected Color getBackground() {
		String sColor = statevo.getColor();
		if (sColor != null) {
			return Color.decode(sColor);
		}
		return bgColor;
	}

	@Override
	public void paint(Graphics2D gfx) {
		final Shape oldClip = gfx.getClip();

		super.paint(gfx);

		gfx.setClip(dimension);

		final Line2D line = new Line2D.Double(dimension.getX(), dimension.getY() + 20d, dimension.getX() + dimension.getWidth(),
				dimension.getY() + 20d);
		gfx.setPaint(new Color(160, 160, 160));
		gfx.setStroke(new BasicStroke(1f));
		gfx.draw(line);
		if (getIcon() == null || getIcon().getContent() == null) {
			gfx.drawImage(((ImageIcon) icon).getImage(), (int) dimension.getX() + 2, (int) dimension.getY() + 3, this);			
		} else {
			gfx.drawImage(new ImageIcon(getIcon().getContent()).getImage(), (int) dimension.getX() + 2, (int) dimension.getY() + 3, this);
		}
		
		gfx.setColor(Utils.getBestForegroundColor(getBackground()));
		if (statevo.getStatename() != null) {
			gfx.setFont(new Font("Arial", Font.BOLD, 12));
			gfx.drawString((statevo.getNumeral() == null ? statevo.getStatename() : statevo.getNumeral() + " " + statevo.getStatename()), (float) dimension.getX() + 19, (float) dimension.getY() + 14);
		}
		if (statevo.getDescription() != null) {
			paintDescription(gfx, statevo.getDescription(), new Rectangle2D.Double(dimension.getX() + 3f, dimension.getY() + 17f,
					dimension.getWidth() - 3f, dimension.getHeight() - 17f));
/*			f = new Font("Arial", Font.PLAIN | Font.TRUETYPE_FONT, 10);
			gfx.setFont(f);
			gfx.drawString(statevo.getDescription(), (float) dimension.getX() + 3, (float) dimension.getY() + 30); */
		}
		gfx.setClip(oldClip);
	}

	/**
	 * @param gfx
	 * @param sText
	 */
	public void paintDescription(Graphics2D gfx, String sText, Rectangle2D extents) {
		double fX = 0d, fY = extents.getY() + 4d;

		if (sText.length() == 0) {
			return;
		}
		gfx.setClip(dimension);
		gfx.setFont(textFont);
		attrString = new AttributedString(sText);
		if (sText.length() > 0) {
			attrString.addAttribute(TextAttribute.FONT, textFont);
		}

		FontRenderContext frc = gfx.getFontRenderContext();
		AttributedCharacterIterator paragraph = attrString.getIterator();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);

		lineMeasurer.setPosition(0);
		while (lineMeasurer.getPosition() < paragraph.getEndIndex()) {
			TextLayout layout = lineMeasurer.nextLayout((int) extents.getWidth());
			// Move y-coordinate by the ascent of the layout.
			fY += layout.getAscent();

			// Compute pen x position.  If the paragraph is
			// right-to-left, we want to align the TextLayouts
			// to the right edge of the panel.
			if (layout.isLeftToRight()) {
				fX = getX() + 4;
			}
			else {
				fX = getX() + getWidth() - layout.getAdvance() + 4;
			}

			// Draw the TextLayout at (drawPosX, drawPosY).
			layout.draw(gfx, (float) fX, (float) fY);

			// Move y-coordinate in preparation for next layout.
			fY += layout.getDescent() + layout.getLeading();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return statevo.getStatename();
	}

	/**
	 * @param sName
	 */
	public void setName(String sName) {
		statevo.setStatename(sName);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumeral() {
		return statevo.getNumeral();
	}

	/**
	 * @param iNumeral
	 */
	public void setNumeral(Integer iNumeral) {
		statevo.setNumeral(iNumeral);
	}

	/**
	 * {@inheritDoc}
	 */
	public NuclosImage getIcon() {
		return statevo.getIcon();
	}

	/**
	 * @param bIcon
	 */
	public void setIcon(NuclosImage bIcon) {
		statevo.setIcon(bIcon);
	}
	
	public void setStateColor(String color) {
		statevo.setColor(color);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return statevo.getDescription();
	}

	/**
	 * @param sDescription
	 */
	public void setDescription(String sDescription) {
		statevo.setDescription(sDescription);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getButtonLabel() {
		return statevo.getButtonLabel();
	}

	/**
	 * @param sName
	 */
	public void setButtonLabel(String sButtonLabel) {
		statevo.setButtonLabel(sButtonLabel);
	}
	
	public void setTabbedPaneName(String tab) {
		statevo.setTabbedPaneName(tab);
	}

	@Override
	public void afterCreate() {
		statevo = new StateVO(new Integer(-getId()), null, SpringLocaleDelegate.getInstance().getMessage(
				"StateShape.1", "Neuer Status"), "", null, null);
		super.afterCreate();
	}

	@Override
	public void beforeDelete() {
		statevo.remove();
		super.beforeDelete();
	}

	@Override
	public void addConnector(AbstractConnector connector) {
		super.addConnector(connector);
		checkStatus();
	}

	@Override
	public void removeConnector(AbstractConnector connector) {
		super.removeConnector(connector);
		checkStatus();
	}

	/**
	 * @todo what is checked here exactly?
	 */
	public void checkStatus() {
		boolean isStart = false;
		boolean isOutgoing = false;
		Icon newIcon = null;

		for (Iterator<AbstractConnector> i = connectors.iterator(); i.hasNext();) {
			AbstractConnector c = i.next();
			if (c.getSourceConnection() != null &&
					c.getSourceConnection().getTargetShape() != null &&
					c.getSourceConnection().getTargetShape().equals(this)) {
				isOutgoing = true;
			}
			if (c.getDestinationConnection() != null &&
					c.getDestinationConnection().getTargetShape() != null &&
					c.getDestinationConnection().getTargetShape().equals(this) &&
					c.getSourceConnection() != null &&
					c.getSourceConnection().getTargetShape() != null &&
					c.getSourceConnection().getTargetShape() instanceof StateModelStartShape) {
				isStart = true;
			}
		}

		if (isStart) {
			newIcon = Icons.getInstance().getIconStateStart();
			bgColor = new Color(230, 230, 230); //new Color(220, 255, 220);
		}
		else if (isOutgoing) {
			newIcon = Icons.getInstance().getIconStateIntermediate();
			bgColor = new Color(230, 230, 230); //new Color(220, 235, 250);
		}
		else {
			newIcon = Icons.getInstance().getIconStateEnd();
			bgColor = new Color(230, 230, 230); //new Color(255, 255, 190);
		}

		if (icon != newIcon) {
			icon = newIcon;
			if (getView() != null) {
				((AbstractShapeViewer) getView()).repaint();
			}
		}
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags,
			int x, int y, int width, int height) {
		return false;
	}

	@Override
	public void doubleClicked(JComponent parent) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @param status
	 */
	public void markState(int status){
		switch (status){
		case InstanceConstants.STATE_IS_CURRENT:
			bgColor = new Color(168, 177, 242);
			break;
		case InstanceConstants.STATE_IS_NOT_CURRENT:
			bgColor = new Color(192, 192, 192);
			break;
		}
		
		if (getView() != null) {
			((AbstractShapeViewer) getView()).repaint();
		}
	}

}	// class StateShape
