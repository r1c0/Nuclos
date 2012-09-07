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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.util.List;

import javax.swing.ImageIcon;

import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.gef.shapes.ArrowConnector;
import org.nuclos.client.gef.shapes.ConnectionPoint;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;

/**
 * Shape for displaying a state transition in the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateTransition extends ArrowConnector implements ImageObserver {
	
	private final StateTransitionVO transitionvo;
	private final float[] afDashes = {5f, 5f};
	private final Stroke strokeAutomatic = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, afDashes, 1f);
	private final Stroke strokeRegular = new BasicStroke(1f);

	public StateTransition() {
		super();
		this.transitionvo = new StateTransitionVO(-getId(), null, null, null, false, false);
	}

	/**
	 *
	 */
	public StateTransition(StateTransitionVO transitionvo) {
		super();
		this.transitionvo = transitionvo;
	}

	/**
	 *
	 * @param startPoint
	 * @param endPoint
	 */
	public StateTransition(StateTransitionVO transitionvo, Point2D startPoint, Point2D endPoint) {
		super(startPoint, endPoint);
		this.transitionvo = transitionvo;
	}

	/**
	 *
	 * @param src
	 * @param dst
	 */
	public StateTransition(StateTransitionVO statetransitionvo, ConnectionPoint src, ConnectionPoint dst) {
		super(src, dst);
		this.transitionvo = statetransitionvo;
	}

	@Override
	public void paint(Graphics2D gfx) {
		if (transitionvo != null && transitionvo.isAutomatic()) {
			setStroke(strokeAutomatic);
		}
		else {
			setStroke(strokeRegular);
		}
		if (transitionvo != null && transitionvo.isDefault()) {
			setPaint(Color.BLACK);
		}
		else {
			setPaint(Color.GRAY);
		}
		
		super.paint(gfx);
		if (transitionvo.getRuleIdsWithRunAfterwards().size() > 0) {
			ImageIcon icon = (ImageIcon) Icons.getInstance().getIconStateTransitionRules();
			gfx.drawImage(icon.getImage(), (int) (dimension.getX() + dimension.getWidth() / 2 - 8),
					(int) (dimension.getY() + dimension.getHeight() / 2 - 8), this);
		}		
		/*if (transitionvo != null && transitionvo.isDefault()) {
			//gfx.setFont(gfx.getFont().deriveFont(Font.BOLD));
			//gfx.drawString("d", (int) (dimension.getX() + dimension.getWidth() / 2 - 8),
				//	(int) (dimension.getY() + dimension.getHeight() / 2 - 8));
			ImageIcon icon = (ImageIcon) Icons.getInstance().getIconStateTransitionDefault();
			gfx.drawImage(icon.getImage(), (int) (dimension.getX() + dimension.getWidth() / 2),
					(int) (dimension.getY() + dimension.getHeight() / 2 - 16), this);
		}*/
	}

	public void addRule(Integer ruleId, Boolean bRunAfterwards) {
		transitionvo.getRuleIdsWithRunAfterwards().add(new Pair<Integer, Boolean>(ruleId, bRunAfterwards));
	}

	public void addRule(EventSupportTransitionVO evTrans, Boolean bRunAfterwards) {
		transitionvo.getEventSupportWithRunAfterwards().add(new Pair<EventSupportTransitionVO, Boolean>(evTrans, bRunAfterwards));
	}
	
	public void removeRule(Integer iRuleId) {
		transitionvo.removeRule(iRuleId);
	}

	public void removeEventSupport(EventSupportTransitionVO ruleClassname) {
		transitionvo.removeEventSupport(ruleClassname);
	}
	
	public void removeAllRules() {
		transitionvo.getRuleIdsWithRunAfterwards().clear();
		transitionvo.getEventSupportWithRunAfterwards().clear();
	}

	public java.util.List<Integer> getRules() {
		return transitionvo.getRuleIds();
	}
	
	public List<Pair<Integer, Boolean>> getRuleIdsWithRunAfterwards() {
		return transitionvo.getRuleIdsWithRunAfterwards();
	}
	
	
	public Pair<Integer, Boolean> getRule(Integer iRuleId) {
		for (Pair<Integer, Boolean> rule : transitionvo.getRuleIdsWithRunAfterwards()){
			if (rule.x.equals(iRuleId))
				return rule;
		}
		return null;
	}
	
	public java.util.List<EventSupportTransitionVO> getEventSupports() {
		return transitionvo.getEventSupports();
	}
	
	public List<Pair<EventSupportTransitionVO, Boolean>> getEventSupportsWithRunAfterwards() {
		return transitionvo.getEventSupportWithRunAfterwards();
	}
	
	
	public Pair<EventSupportTransitionVO, Boolean> getEventSupport(EventSupportTransitionVO classname) {
		for (Pair<EventSupportTransitionVO, Boolean> rule : transitionvo.getEventSupportWithRunAfterwards()){
			if (rule.getX().getEventSupportClass().equals(classname.getEventSupportClass()))
				return rule;
		}
		return null;
	}
	
	public void addRole(Integer iRoleId) {
		transitionvo.getRoleIds().add(iRoleId);
	}

	public void removeRole(Integer iRoleId) {
		transitionvo.getRoleIds().remove(iRoleId);
	}

	public void removeAllRoles() {
		transitionvo.getRoleIds().clear();
	}

	public java.util.List<Integer> getRoles() {
		return transitionvo.getRoleIds();
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags,
			int x, int y, int width, int height) {
		return false;
	}

	public StateTransitionVO getStateTransitionVO() {
		return transitionvo;
	}

	@Override
	public void afterCreate() {
		if (getSourceConnection() != null && getSourceConnection().getTargetShape() != null) {
			this.transitionvo.setStateSource(((StateShape) getSourceConnection().getTargetShape()).getStateVO().getClientId());
		}
		if (getDestinationConnection() != null && getDestinationConnection().getTargetShape() != null) {
			this.transitionvo.setStateTarget(((StateShape) getDestinationConnection().getTargetShape()).getStateVO().getClientId());
		}
	}

	@Override
	public void beforeDelete() {
		super.beforeDelete();
		this.transitionvo.remove();
	}

	@Override
	public void setSourceConnection(ConnectionPoint srcConnection) {
		super.setSourceConnection(srcConnection);
		if (srcConnection != null) {
			if (srcConnection.getTargetShape() instanceof StateShape) {
				this.transitionvo.setStateSource(((StateShape) srcConnection.getTargetShape()).getStateVO().getClientId());
				((StateShape) srcConnection.getTargetShape()).checkStatus();
			}
			else if (srcConnection.getTargetShape() instanceof StateModelStartShape) {
				this.transitionvo.setStateSource(null);
			}
		}
		else {
			this.transitionvo.setStateSource(-9999);
		}

		if (dstConnection != null && dstConnection.getTargetShape() != null && dstConnection.getTargetShape() instanceof StateShape)
		{
			((StateShape) dstConnection.getTargetShape()).checkStatus();
		}
	}

	@Override
	public void setDestinationConnection(ConnectionPoint dstConnection) {
		super.setDestinationConnection(dstConnection);
		if (dstConnection != null) {
			if (dstConnection.getTargetShape() instanceof StateShape) {
				this.transitionvo.setStateTarget(((StateShape) dstConnection.getTargetShape()).getStateVO().getClientId());
				((StateShape) dstConnection.getTargetShape()).checkStatus();
			}
			else if (dstConnection.getTargetShape() instanceof StateModelStartShape) {
				this.transitionvo.setStateSource(null);
			}
		}
		else {
			this.transitionvo.setStateTarget(-9999);
		}
	}

	@Override
	public boolean isConnectionAllowed(Shape shape, int connectorPoint, int connectionPoint) {
		boolean bResult = true;
		if (shape instanceof StateModelStartShape && connectorPoint != AbstractConnector.STARTPOINT &&
				connectionPoint != AbstractShape.CONNECTION_CENTER) {
			bResult = false;
		}
		return bResult && super.isConnectionAllowed(shape, connectorPoint, connectionPoint);
	}
}
