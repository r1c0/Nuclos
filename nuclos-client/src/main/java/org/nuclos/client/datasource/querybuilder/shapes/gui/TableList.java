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
package org.nuclos.client.datasource.querybuilder.shapes.gui;

import org.nuclos.client.datasource.querybuilder.QueryBuilderIcons;
import org.nuclos.client.datasource.querybuilder.controller.QueryBuilderController;
import org.nuclos.client.datasource.querybuilder.shapes.TableShape;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.database.query.definition.Table;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TableList extends JList implements DragGestureListener, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Color background = new Color(220, 235, 250);

	private class TableListModel extends AbstractListModel implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final List<Object> lstRows = new ArrayList<Object>();

		/**
		 *
		 * @return size
		 */
		@Override
        public int getSize() {
			return lstRows.size();
		}

		/**
		 *
		 * @param index
		 * @return element at
		 */
		@Override
        public Object getElementAt(int index) {
			return lstRows.get(index);
		}

		/**
		 *
		 * @param obj
		 */
		public void addRows(List<?> obj) {
			lstRows.addAll(obj);
			fireContentsChanged(this, 0, lstRows.size());
		}

		/**
		 *
		 * @param index
		 * @param bValue
		 */
		public void markElementAt(int index, boolean bValue) {
			final ConstraintColumn col = (ConstraintColumn) model.getElementAt(index);
			col.setMark(bValue);
			fireContentsChanged(this, index, index);
		}
	}

	class TableListCellRenderer implements ListCellRenderer, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JLabel label = new JLabel();
		private Font regularFont = new Font("Dialog", Font.PLAIN, 10);
		private Font boldFont = new Font("Dialog", Font.BOLD, 10);
		private Font italicFont = new Font("Dialog", Font.ITALIC, 10);

		/**
		 *
		 * @param list
		 * @param value
		 * @param index
		 * @param isSelected
		 * @param cellHasFocus
		 * @return list cell renderer component
		 */
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final ConstraintColumn column = (ConstraintColumn) value;
			label.setText(column.getName());
			label.setOpaque(true);
			label.setIconTextGap(2);
			label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

			if (column.hasPrimaryKeyConstraint()) {
				label.setIcon(QueryBuilderIcons.iconPrimaryKey16);
				label.setFont(boldFont);
			}
			else if (column.hasForeignKeyConstraint()) {
				label.setIcon(QueryBuilderIcons.iconForeignKey16);
				label.setFont(italicFont);
			}
			else {
				label.setIcon(QueryBuilderIcons.iconEmpty16);
				label.setFont(regularFont);
			}

			if (isSelected) {
				label.setBackground(Color.GRAY);
				label.setForeground(Color.WHITE);
			}
			else if (column.isMarked()) {
				label.setBackground(Color.LIGHT_GRAY);
				label.setForeground(Color.BLUE);
			}
			else {
				label.setForeground(Color.BLACK);				
				label.setBackground(background);
			}
			
			return label;
		}

	}

	protected TableShape embeddingShape;
	protected DragSource dragSource;
	protected TableListModel model = new TableListModel();
	protected int currentMark = -1;
	protected boolean bDragSource = false;

	/**
	 *
	 */
	public TableList(TableShape embeddingShape) {
		super();
		this.embeddingShape = embeddingShape;
		init();
	}

	/**
	 *
	 * @param listData
	 */
	public TableList(TableShape embeddingShape, final Object[] listData) {
		super(listData);
		this.embeddingShape = embeddingShape;
		init();
	}

	/**
	 *
	 */
	protected void init() {
		setModel(model);
		dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_LINK, this);
		setCellRenderer(new TableListCellRenderer());
	}

	/**
	 *
	 * @return embedding shape
	 */
	public TableShape getEmbeddingShape() {
		return embeddingShape;
	}

	/**
	 * @todo use Swing DND mechanism rather than the AWT mechanism, if possible
	 * @param dge
	 */
	@Override
    public void dragGestureRecognized(DragGestureEvent dge) {
		try {
			dge.startDrag(null, new ColumnTransferable((ConstraintColumn) ((TableList) dge.getComponent()).getSelectedValue()),
					((QueryBuilderController) embeddingShape.getView().getController()).getDragSourceListener());
		}
		catch (final InvalidDnDOperationException ex) {
			//@todo find the right way to handle this case
			ex.printStackTrace();
			dge.getSourceAsDragGestureRecognizer().resetRecognizer();
		}
		catch (final Exception ex) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
                public void run() {
					System.out.println(ex.getMessage());
					Errors.getInstance().showExceptionDialog(TableList.this, ex);
				}
			});
		}
	}

	/**
	 *
	 * @return drop target
	 */
	@Override
	public synchronized DropTarget getDropTarget() {
		DropTarget result = null;

		if (embeddingShape != null) {
			result = new DropTarget(this, DnDConstants.ACTION_LINK,
					((QueryBuilderController) embeddingShape.getView().getController()).getDropTargetListener());
		}
		return result;
	}

	/**
	 *
	 * @param columns
	 */
	public void setColumns(List<ConstraintColumn> columns) {
		Collections.sort(columns);
		model.addRows(columns);
	}

	/**
	 *
	 * @param dtde
	 */
	public void dragOver(DropTargetDragEvent dtde) {
		final int index = locationToIndex(dtde.getLocation());
		if (index >= 0 && currentMark != index) {
			resetMark();
			setMark(index);
		}
	}

	/**
	 *
	 * @param dte
	 */
	public void dragExit(DropTargetEvent dte) {
		resetMark();
	}

	/**
	 *
	 * @param dtde
	 */
	public void drop(DropTargetDropEvent dtde) {
		resetMark();
	}

	/**
	 *
	 */
	protected void resetMark() {
		if (currentMark >= 0) {
			model.markElementAt(currentMark, false);
			currentMark = -1;
		}
	}

	/**
	 *
	 * @param index
	 */
	protected void setMark(int index) {
		if (currentMark != index) {
			model.markElementAt(index, true);
			currentMark = index;
		}
	}

	public int getMark() {
		return currentMark;
	}

	/**
	 *
	 * @param column
	 * @return indef of constraint column
	 */
	public int getIndexOf(ConstraintColumn column) {
		int index = 0;
		// Since dropping a column delivers a new deserialized instance of ConstraintColumn,
		// we need to traverse the whole list to check equivalence
		for (Iterator<Object> i = model.lstRows.iterator(); i.hasNext(); index++) {
			ConstraintColumn cc = (ConstraintColumn) i.next();
			if (cc.equals(column)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 *
	 * @param index
	 * @return rect for index
	 */
	public Rectangle2D getRectForIndex(int index) {
		Rectangle2D r = getCellBounds(index, index), result = new Rectangle2D.Double();

		int iOffset = embeddingShape.getScrollPaneOffset();

		double dX = embeddingShape.getX();
		double dY = embeddingShape.getYTableOffset() + r.getY() - iOffset;
		if (dY < embeddingShape.getYTableOffset()) {
			dY = embeddingShape.getYTableOffset();
		}
		else if (dY > (embeddingShape.getY() + embeddingShape.getHeight() - r.getHeight())) {
			dY = embeddingShape.getY() + embeddingShape.getHeight() - r.getHeight(); // maximum y
//         dY = embeddingShape.getY() + embeddingShape.getHeight() - r.getHeight() / 2d; // maximum y
		}
		double dW = embeddingShape.getWidth();
		double dH = r.getHeight();

		result.setRect(dX, dY, dW, dH);
		return result;
	}

	/**
	 *
	 * @param column
	 * @return rect for column
	 */
	public Rectangle2D getRectForColumn(ConstraintColumn column) {
		return getRectForIndex(getIndexOf(column));
	}

	/**
	 *
	 * @return marked column
	 */
	public ConstraintColumn getMarkedColumn() {
		return (ConstraintColumn) model.getElementAt(currentMark);
	}

	/**
	 *
	 * @param t
	 * @return table references
	 */
	public List<ConstraintColumn> getTableReferences(Table t) {
		final List<ConstraintColumn> result = new ArrayList<ConstraintColumn>();
		for (Iterator<Object> i = model.lstRows.iterator(); i.hasNext();) {
			ConstraintColumn column = (ConstraintColumn) i.next();
			if (column.hasReferenceTo(t)) {
				result.add(column);
			}
		}
		return result;
	}

	/**
	 *
	 * @return list of rows
	 */
	public List<Object> getRows() {
		return model.lstRows;
	}

}	// class TableList
