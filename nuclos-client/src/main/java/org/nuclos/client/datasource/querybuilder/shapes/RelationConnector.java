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
package org.nuclos.client.datasource.querybuilder.shapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.nuclos.client.datasource.querybuilder.QueryBuilderViewer;
import org.nuclos.client.gef.AbstractViewer;
import org.nuclos.client.gef.shapes.AbstractConnector;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RelationConnector extends AbstractConnector {
	protected static final int CONTROLPOINT_WIDTH = 24;

	public static final int TYPE_JOIN = 1;
	public static final int TYPE_LEFTJOIN = 2;
	public static final int TYPE_RIGHTJOIN = 3;

	public static final String CARDINALITY_NONE = "";
	public static final String CARDINALITY_1 = "1";
	public static final String CARDINALITY_N = "n";
	public static final String CARDINALITY_M = "m";

	protected Rectangle2D startRect = new Rectangle2D.Double();
	protected Rectangle2D endRect = new Rectangle2D.Double();
	protected Point2D startControlPoint = new Point2D.Double();
	protected Point2D endControlPoint = new Point2D.Double();
	protected String sSourceCardinality = "";
	protected String sTargetCardinality = "";
	protected int joinType = TYPE_JOIN;
	protected int arrowSize = 5;
	protected Font font = new Font("Dialog", Font.BOLD, 11);

	public RelationConnector() {
		super();
	}

	public RelationConnector(RelationConnectionPoint src, RelationConnectionPoint dst) {
		srcConnection = src;
		dstConnection = dst;
		Rectangle2D srcRect = null, dstRect = null;

		TableShape srcShape = (TableShape) src.getTargetShape();
		srcRect = srcShape.getConnectionPoint(src.getColumn());
		srcShape.addConnector(this);
		TableShape dstShape = (TableShape) dst.getTargetShape();
		dstRect = dstShape.getConnectionPoint(dst.getColumn());
		dstShape.addConnector(this);
		setStartRect(srcRect);
		setEndRect(dstRect);
		setMoveable(false);
	}

	public void setStartRect(Rectangle2D r) {
		startRect.setRect(r);
		computeDimension();
	}

	public void setEndRect(Rectangle2D r) {
		endRect.setRect(r);
		computeDimension();
	}

	@Override
	protected void computeDimension() {
		Point2D ps, pe;

		if (startRect == null || endRect == null) {
			return;
		}

		if (startRect.getX() < endRect.getX()) {
			ps = new Point2D.Double(startRect.getX() + startRect.getWidth(), startRect.getY() + startRect.getHeight() / 2d);
			startControlPoint.setLocation(ps.getX() + CONTROLPOINT_WIDTH, ps.getY());
			pe = new Point2D.Double(endRect.getX(), endRect.getY() + endRect.getHeight() / 2d);
			endControlPoint.setLocation(pe.getX() - CONTROLPOINT_WIDTH, pe.getY());
		}
		else {
			ps = new Point2D.Double(startRect.getX(), startRect.getY() + startRect.getHeight() / 2d);
			startControlPoint.setLocation(ps.getX() - CONTROLPOINT_WIDTH, ps.getY());
			pe = new Point2D.Double(endRect.getX() + endRect.getWidth(), endRect.getY() + endRect.getHeight() / 2d);
			endControlPoint.setLocation(pe.getX() + CONTROLPOINT_WIDTH, pe.getY());
		}
		startPoint.setLocation(ps);
		endPoint.setLocation(pe);
		super.computeDimension();
	}

	@Override
	public void paint(Graphics2D gfx) {
		Shape path = getPath();
		if (bSelected) {
			gfx.setPaint(Color.BLUE);
		}
		else {
			gfx.setPaint(Color.BLACK);
		}
		gfx.setStroke(stroke);
		gfx.draw(path);

		FontMetrics fm = ((QueryBuilderViewer) view).getFontMetrics(font);
		Rectangle2D rSource = fm.getStringBounds(sSourceCardinality, gfx);
		Rectangle2D rTarget = fm.getStringBounds(sTargetCardinality, gfx);

		gfx.setFont(font);
		gfx.drawString(sSourceCardinality,
				(startPoint.getX() < startControlPoint.getX() ?
						(float) (startPoint.getX() + ((startControlPoint.getX() - startPoint.getX()) / 2f) - (float) rSource.getWidth() / 2f) :
						(float) (startPoint.getX() - (startPoint.getX() - startControlPoint.getX()) / 2f) - (float) rSource.getWidth() / 2f),
				(float) startPoint.getY() - 3f);
		gfx.drawString(sTargetCardinality,
				(endPoint.getX() > endControlPoint.getX() ?
						(float) (endPoint.getX() - ((endPoint.getX() - endControlPoint.getX()) / 2f) - (float) rTarget.getWidth() / 2f) :
						(float) (endPoint.getX() + (endControlPoint.getX() - endPoint.getX()) / 2f) - (float) rTarget.getWidth() / 2f),
				(float) endPoint.getY() - 3f);

		if (joinType != TYPE_JOIN) {
			GeneralPath p = new GeneralPath();
			switch (joinType) {
				case TYPE_RIGHTJOIN:
					p.moveTo((float) startPoint.getX(), (float) startPoint.getY());
					p.lineTo((float) startPoint.getX() + arrowSize * (startPoint.getX() < startControlPoint.getX() ? 1 : -1), (float) startPoint.getY() - arrowSize);
					p.lineTo((float) startPoint.getX() + arrowSize * (startPoint.getX() < startControlPoint.getX() ? 1 : -1), (float) startPoint.getY() + arrowSize);
					p.closePath();
					break;
				case TYPE_LEFTJOIN:
					p.moveTo((float) endPoint.getX(), (float) endPoint.getY());
					p.lineTo((float) endPoint.getX() + arrowSize * (endPoint.getX() > endControlPoint.getX() ? -1 : 1), (float) endPoint.getY() - arrowSize);
					p.lineTo((float) endPoint.getX() + arrowSize * (endPoint.getX() > endControlPoint.getX() ? -1 : 1), (float) endPoint.getY() + arrowSize);
					p.closePath();
					break;
			}
			gfx.fill(p);
		}
	}

	private Shape getPath() {
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		path.moveTo((float) startPoint.getX(), (float) startPoint.getY());
		path.lineTo((float) startControlPoint.getX(), (float) startControlPoint.getY());
		path.lineTo((float) endControlPoint.getX(), (float) endControlPoint.getY());
		path.lineTo((float) endPoint.getX(), (float) endPoint.getY());
		return path;
	}

	@Override
	public boolean isPointInside(Point2D p) {
		return hitTest(p) != HIT_NONE;
	}

	@Override
	public int hitTest(Point2D p) {
		int iWhich = HIT_NONE;
		Shape shape = getPath();

		PathIterator i = shape.getPathIterator(null);
		double[] coords = new double[6];
		double lastX = 0d, lastY = 0d;

		while (!i.isDone()) {
			int type = i.currentSegment(coords);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					lastX = coords[0];
					lastY = coords[1];
					break;
				case PathIterator.SEG_LINETO:
					Line2D line = new Line2D.Double(lastX, lastY, coords[0], coords[1]);
					lastX = coords[0];
					lastY = coords[1];
					if (line.ptSegDist(p) <= 9) {
						return HIT_LINE;
					}
					break;
			}
			i.next();
		}
		return iWhich;
	}

	public void setSourceCardinality(String sCardinality) {
		sSourceCardinality = sCardinality;
	}

	public void setTargetCardinality(String sCardinality) {
		sTargetCardinality = sCardinality;
	}

	public String getSourceCardinality() {
		return sSourceCardinality;
	}

	public String getTargetCardinality() {
		return sTargetCardinality;
	}

	public int getJoinType() {
		return joinType;
	}

	public void setJoinType(int joinType) {
		this.joinType = joinType;
		((AbstractViewer) view).repaint();
	}

}	// class RelationConnector
