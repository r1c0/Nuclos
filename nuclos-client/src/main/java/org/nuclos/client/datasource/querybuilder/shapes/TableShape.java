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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.nuclos.client.datasource.querybuilder.QueryBuilderViewer;
import org.nuclos.client.datasource.querybuilder.shapes.gui.ConstraintColumn;
import org.nuclos.client.datasource.querybuilder.shapes.gui.TableHeader;
import org.nuclos.client.datasource.querybuilder.shapes.gui.TableList;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.ComponentAdapter;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Constraint;
import org.nuclos.common.database.query.definition.Table;

/**
 * Table shape for datasource editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TableShape extends ComponentAdapter implements Comparable<TableShape> {

	private final TableHeader header;
	private Table table;
	private TableList list;
	private JScrollPane scrPane;
	private final Map<String, ConstraintColumn> columnMap = CollectionUtils.newHashMap();
	private final List<ConstraintColumn> columns = new ArrayList<ConstraintColumn>();

	/**
	 *
	 */
	public TableShape() {
		super();
		header = new TableHeader("");
		component = init();
		setDimension(new Rectangle(0, 0, 160, 240));
		setMinimumSize(128, 128);
		setResizeable(true);
		setConnectable(true);
	}

	/**
	 *
	 * @param table
	 */
	public TableShape(Table table) {
		super();
		this.table = table;
		header = new TableHeader("");
		component = init();
		setDimension(new Rectangle(0, 0, 160, 240));
		setResizeable(true);
	}

	/**
	 *
	 * @return table
	 */
	public Table getTable() {
		return table;
	}

	/**
	 *
	 * @param value
	 */
	@Override
	public void setSelection(boolean value) {
		super.setSelection(value);
		header.setSelection(value);
	}

	public int getScrollPaneOffset() {
		return scrPane.getVerticalScrollBar().getModel().getValue();
	}

	/**
	 *
	 * @return initialized panel
	 */
	public JComponent init() {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

		scrPane = new JScrollPane();
		scrPane.setBorder(BorderFactory.createEmptyBorder());
		scrPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
				adjustConnections(true);
			}
		});

		header.setText(table.toString());

		list = new TableList(this);
		list.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 11));
		list.setBackground(new Color(220, 235, 250));

		scrPane.getViewport().add(list);
		panel.add(header, BorderLayout.NORTH);
		panel.add(scrPane, BorderLayout.CENTER);
		//@todo allow multi selection
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		initColumns();
		return panel;
	}

	/**
	 *
	 * @param gfx
	 */
	@Override
	public void paint(Graphics2D gfx) {
		super.paint(gfx);
		header.repaint();
		scrPane.repaint();
	}

	/**
	 *
	 */
	protected void initColumns() {
		for (Column column : table.getColumns()) {
			final ConstraintColumn constraintcolumn = new ConstraintColumn(column.getTable(), column.getName(),
					column.getType(), column.getLength(), column.getPrecision(), column.getScale(), column.isNullable());
			columnMap.put(constraintcolumn.getName(), constraintcolumn);
			columns.add(constraintcolumn);
		}

		for (Constraint constraint : table.getConstraints()) {
			for (Column column : constraint.getColumns()) {
				final ConstraintColumn cc = columnMap.get(column.getName());
				if (cc != null) {
					cc.addConstraint(constraint);
				}
			}
		}

		//  columns are now ordered alphabatic . Like in Elisa (see ELISA-6405)
		 // if (!table.isQuery()) {
			Collections.sort(columns, new Comparator<ConstraintColumn>() {
				@Override
                public int compare(ConstraintColumn c1, ConstraintColumn c2) {
					if (c1.getName().toLowerCase().equals("intid")) {
						return -1;
					}
					else if (c2.getName().toLowerCase().equals("intid")) {
						return 1;
					}
					else return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
				}
			});
		// }
		list.setColumns(columns);
	}

	@Override
	public void beforeDelete() {
		if (connectors.size() > 0) {
			for (Object oConnector : connectors.toArray()) {
				final RelationConnector c = (RelationConnector) oConnector;
				view.getModel().removeShape(c);
			}
		}
	}

	/**
	 *
	 * @param column
	 * @return rect for column
	 */
	public Rectangle2D getConnectionPoint(ConstraintColumn column) {
		return list.getRectForColumn(column);
	}

	/**
	 *
	 * @return y table offset
	 */
	public double getYTableOffset() {
		return getY() + header.getHeight() + 2d;
	}

	/**
	 *
	 * @param dimension
	 */
	@Override
	public void setDimension(Rectangle2D dimension) {
		super.setDimension(dimension);
		adjustConnections(false);
	}

	/**
	 *
	 */
	public void adjustConnections(boolean repaint) {
		for (Iterator<AbstractConnector> i = connectors.iterator(); i.hasNext();) {
			RelationConnector connector = (RelationConnector) i.next();
			if (connector.getSourceConnection() != null && connector.getSourceConnection().getTargetShape().equals(this)) {
				connector.setStartRect(getConnectionPoint(((RelationConnectionPoint) connector.getSourceConnection()).getColumn()));
			}
			else
			if (connector.getDestinationConnection() != null && connector.getDestinationConnection().getTargetShape().equals(this))
			{
				connector.setEndRect(getConnectionPoint(((RelationConnectionPoint) connector.getDestinationConnection()).getColumn()));
			}
		}
		if (repaint) {
			((QueryBuilderViewer) view).repaint();
		}
	}

	/**
	 *
	 * @param t
	 * @return a list of ConstraintColumns
	 */
	public List<ConstraintColumn> getTableReferences(Table t) {
		return list.getTableReferences(t);
	}

	/**
	 *
	 * @return List of columns
	 */
	public List<Column> getColumns() {
		return CollectionUtils.typecheck(list.getRows(), Column.class);
	}

	/**
	 *
	 * @return List of referencing columns
	 */
	public List<ConstraintColumn> getReferencingColumns() {
		List<ConstraintColumn> result = new ArrayList<ConstraintColumn>();
		for (Iterator<Column> i = getColumns().iterator(); i.hasNext();) {
			ConstraintColumn cc = (ConstraintColumn) i.next();
			if (cc.hasForeignKeyConstraint()) {
				result.add(cc);
			}
		}
		return result;
	}

	/**
	 *
	 * @return List of all tables
	 */
	public TableList getTableList() {
		return list;
	}

	/**
	 *
	 * @param name
	 * @return column by name
	 */
	public ConstraintColumn getColumn(String name) {
		for (Iterator<Column> i = getColumns().iterator(); i.hasNext();) {
			ConstraintColumn cc = (ConstraintColumn) i.next();
			if (cc.getName().equalsIgnoreCase(name)) {
				return cc;
			}
		}
		return null;
	}

	/**
	 *
	 * @param referencedConstraint
	 * @return constraint column by name
	 */
	public ConstraintColumn getConstraintColumn(String referencedConstraint) {
		for (Iterator<Column> i = getColumns().iterator(); i.hasNext();) {
			ConstraintColumn cc = (ConstraintColumn) i.next();
			for (Iterator<Constraint> j = cc.getConstraints().iterator(); j.hasNext();) {
				Constraint c = j.next();
				if (c.getName().equalsIgnoreCase(referencedConstraint)) {
					return cc;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param o
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TableShape)) {
			return false;
		}

		final TableShape tableShape = (TableShape) o;

		if (!columnMap.equals(tableShape.columnMap)) {
			return false;
		}
		if (!columns.equals(tableShape.columns)) {
			return false;
		}
		if (!header.equals(tableShape.header)) {
			return false;
		}
		if (!list.equals(tableShape.list)) {
			return false;
		}
		if (!scrPane.equals(tableShape.scrPane)) {
			return false;
		}
		if (!table.equals(tableShape.table)) {
			return false;
		}

		return true;
	}

	/**
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		int result = header.hashCode();
		result = 29 * result + table.hashCode();
		result = 29 * result + list.hashCode();
		result = 29 * result + scrPane.hashCode();
		result = 29 * result + columnMap.hashCode();
		result = 29 * result + columns.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return table.getName();
	}

	@Override
    public int compareTo(TableShape shape) {
		return table.getName().compareTo(shape.getTable().getName());
	}

}	// class TableShape
