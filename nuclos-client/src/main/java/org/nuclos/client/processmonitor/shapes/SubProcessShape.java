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
package org.nuclos.client.processmonitor.shapes;

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
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.gef.AbstractShapeViewer;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.ContainerShape;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.statemodel.shapes.StateModelStartShape;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Shape representing a subprocess (in a processmonitor model).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */

public class SubProcessShape extends ContainerShape implements ImageObserver {

	private static final Logger LOG = Logger.getLogger(SubProcessShape.class);
	
	public static final int STATE_START = 1;
	public static final int STATE_END = 2;
	public static final int STATE_INTERMEDIATE = 3;

	private SubProcessVO subprocessvo;
	private ImageIcon icon = (ImageIcon) Icons.getInstance().getIconStateIntermediate();
	private final Font textFont = new Font("Arial", Font.TRUETYPE_FONT, 10);
	private AttributedString attrString;

	public SubProcessShape() {
		this(null);
	}

	/**
	 * @param statevo
	 */
	public SubProcessShape(SubProcessVO subprocessvo) {
		this(subprocessvo, 0d, 0d, 130d, 48d);
	}

	/**
	 * @param statevo
	 * @param dX
	 * @param dY
	 * @param dWidth
	 * @param dHeight
	 */
	private SubProcessShape(SubProcessVO subprocessvo, double dX, double dY, double dWidth, double dHeight) {
		super(dX, dY, dWidth, dHeight);
		this.subprocessvo = subprocessvo;
		setMinimumSize(120d, 48d);
		this.bgColor = new Color(240, 240, 240);
		this.borderColor = new Color(90, 90, 90);
		this.borderSize = 2d;
		setConnectable(true);
		setResizeable(true);
		connectionPointColor = Color.RED;
		checkStatus();
		
	}

	public SubProcessVO getStateVO() {
		return subprocessvo;
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
		gfx.drawImage(icon.getImage(), (int) dimension.getX() + 2, (int) dimension.getY() + 2, this);
		gfx.setColor(Color.BLACK);
//		if (subprocessvo.getStatename() != null) {
//			gfx.setFont(new Font("Arial", Font.BOLD, 12));
//			gfx.drawString((subprocessvo.getStatename()), (float) dimension.getX() + 19, (float) dimension.getY() + 14);
//		}
//		if (subprocessvo.getDescription() != null) {
//			paintDescription(gfx, subprocessvo.getDescription(), new Rectangle2D.Double(dimension.getX() + 3f, dimension.getY() + 17f,
//					dimension.getWidth() - 3f, dimension.getHeight() - 17f));
//		}
		if (subprocessvo.getGuarantor() != null) {
			gfx.setFont(new Font("Arial", Font.BOLD, 12));
			gfx.drawString((subprocessvo.getGuarantor()), (float) dimension.getX() + 19, (float) dimension.getY() + 14);
		}
		if (subprocessvo.getSecondGuarator() != null) {
			paintDescription(gfx, subprocessvo.getSecondGuarator(), new Rectangle2D.Double(dimension.getX() + 3f, dimension.getY() + 17f,
					dimension.getWidth() - 3f, dimension.getHeight() - 17f));
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
	
	public Integer getSubProcessStateModel() {
		if (subprocessvo.getStateModelVO() == null){
			return null;
		}
		return subprocessvo.getStateModelVO().getId();
	}
	
	public void setSubProcessStateModel(Integer stateModelId){
		if (stateModelId != null){
			for (StateModelVO model : StateDelegate.getInstance().getAllStateModels()){
				if (model.getId().equals(stateModelId)){
					subprocessvo.setStateModelVO(model);
				}
			}
		}
	}

	public String getName() {
		return subprocessvo.getStatename();
	}

	/**
	 * @param sName
	 */
	public void setName(String sName) {
		subprocessvo.setStatename(sName);
	}
	
	public void setSubProcessUsageCriteria(Integer ucId) {
		subprocessvo.setStateModelUsageId(ucId);
	}
	
	public Integer getSubProcessUsageCriteria() {
		return subprocessvo.getStateModelUsageId();
	}
	
	public Integer getRuntime() {
		return subprocessvo.getRuntime();
	}

	/**
	 * @param iNumeral
	 */
	public void setRuntime(Integer iRuntime) {
		subprocessvo.setRuntime(iRuntime);
	}
	
	public Integer getRuntimeFormat() {
		return subprocessvo.getRuntimeFormat();
	}
	
	public void setRuntimeFormat(Integer iFormat) {
		subprocessvo.setRuntimeFormat(iFormat);
	}
	
	public void setGuarantor(String strGuarantor) {
		subprocessvo.setGuarantor(strGuarantor);
	}
	
	public void setSecondGuarantor(String strSecondGuarantor) {
		subprocessvo.setSecondGuarator(strSecondGuarantor);
	}
	
	public String getPlanStartSeries() {
		return subprocessvo.getPlanStartSeries();
	}
	
	public void setPlanStartSeries(String series){
		subprocessvo.setPlanStartSeries(series);
	}
	
	public String getPlanEndSeries() {
		return subprocessvo.getPlanEndSeries();
	}
	
	public void setPlanEndSeries(String series){
		subprocessvo.setPlanEndSeries(series);
	}
	
	public void setSupervisor(String strSupervisor) {
		subprocessvo.setSupervisor(strSupervisor);
	}
	
	public String getGuarantor() {
		return subprocessvo.getGuarantor();
	}
	
	public String getSecondGuarantor() {
		return subprocessvo.getSecondGuarator();
	}
	
	public String getSupervisor() {
		return subprocessvo.getSupervisor();
	}
	
	public String getOriginalSystem() {
		return subprocessvo.getOriginalSystem();
	}
	
	public void setOriginalSystem(String sOriginalSystem) {
		subprocessvo.setOriginalSystem(sOriginalSystem);
	}

	public String getDescription() {
		return subprocessvo.getDescription();
	}

	/**
	 * @param sDescription
	 */
	public void setDescription(String sDescription) {
		subprocessvo.setDescription(sDescription);
	}

	@Override
	public void afterCreate() {
		subprocessvo = new SubProcessVO(new Integer(-getId()), null, null, null, "", "", "", "", "", null, null, null, null, null);
		super.afterCreate();
	}

	@Override
	public void beforeDelete() {
		subprocessvo.remove();
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
	 * 
	 */
	public void checkStatus() {
		boolean isStart = false;
		boolean isOutgoing = false;
		ImageIcon newIcon = null;

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
			newIcon = (ImageIcon) Icons.getInstance().getIconStateStart();
			bgColor = new Color(220, 255, 220);
		}
		else if (isOutgoing) {
			newIcon = (ImageIcon) Icons.getInstance().getIconStateIntermediate();
			bgColor = new Color(240, 240, 240);
		}
		else {
			newIcon = (ImageIcon) Icons.getInstance().getIconStateEnd();
			bgColor = new Color(255, 255, 190);
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
	
	/*
	 * opens the statemodel window with the double clicked subprocess 
	 */
	public void openStateModel() {			
		NuclosCollectController<CollectableStateModel> cont;
		try {		
			
			cont = (NuclosCollectController<CollectableStateModel>) NuclosCollectControllerFactory.getInstance().
				newCollectController(NuclosEntity.STATEMODEL.getEntityName(), null);
		
			Collection<StateModelVO> col = StateDelegate.getInstance().getAllStateModels();
			
			Integer id = new Integer(0);
			
			for(Iterator<StateModelVO> it = col.iterator(); it.hasNext(); ) {
				StateModelVO vi = it.next();
				if(vi.getName().equals(this.getName())) {
					id = vi.getId();
				}				
			}			
			
			try {
				if(id > 0) {
					cont.runViewSingleCollectableWithId(id);
				}
				LOG.info("openStateModel id=" + id);
			} catch (Exception e) {
				LOG.error("openStateModel failed: " + e, e);
			}
		
		} catch (NuclosBusinessException e1) {
			LOG.error("openStateModel failed: " + e1, e1);
		} catch (CommonPermissionException e1) {
			LOG.error("openStateModel failed: " + e1, e1);
		} catch (CommonFatalException e1) {
			LOG.error("openStateModel failed: " + e1, e1);
		}	
	}

	@Override
	public void doubleClicked(JComponent parent) {
		openStateModel();
	}

}	// class StateShape
