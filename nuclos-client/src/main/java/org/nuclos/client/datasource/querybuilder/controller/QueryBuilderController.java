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
package org.nuclos.client.datasource.querybuilder.controller;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry;
import org.nuclos.client.datasource.querybuilder.gui.ColumnSelectionTable;
import org.nuclos.client.datasource.querybuilder.gui.ColumnSelectionTableModel;
import org.nuclos.client.datasource.querybuilder.gui.JoinPanel;
import org.nuclos.client.datasource.querybuilder.gui.TableTransferable;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnectionPoint;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnector;
import org.nuclos.client.datasource.querybuilder.shapes.TableShape;
import org.nuclos.client.datasource.querybuilder.shapes.gui.ColumnTransferable;
import org.nuclos.client.datasource.querybuilder.shapes.gui.ConstraintColumn;
import org.nuclos.client.datasource.querybuilder.shapes.gui.TableList;
import org.nuclos.client.gef.AbstractComponentModel;
import org.nuclos.client.gef.AbstractController;
import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.ShapeModelListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Constraint;
import org.nuclos.common.database.query.definition.ReferentialContraint;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;

/**
 * Controller for datasource editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class QueryBuilderController extends AbstractController implements ShapeModelListener {
	
	private final Logger LOG = Logger.getLogger(this.getClass());
	
	private final Set<String> setQueryTypes;

	/**
	 * drop target listener implementation
	 */
	private class DropTargetListenerImpl implements DropTargetListener {
		/**
		 *
		 * @param dtde
		 */
		@Override
        public void dragEnter(DropTargetDragEvent dtde) {
			if (dtde.getDropTargetContext().getComponent() == viewer) {
				dragEnterViewer(dtde);
			}
			else if (dtde.getDropTargetContext().getComponent().getClass().equals(TableList.class)) {
				dragEnterTable(dtde);
			}
			else if (dtde.getDropTargetContext().getComponent().getClass().equals(ColumnSelectionTable.class)) {
				dragEnterColumnTable(dtde);
			}
		}

		/**
		 *
		 * @param dtde
		 */
		@Override
        public void dragOver(DropTargetDragEvent dtde) {
			if (dtde.getDropTargetContext().getComponent().getClass().equals(TableList.class)) {
				dragOverTable(dtde);
			}
			else if (dtde.getDropTargetContext().getComponent().getClass().equals(ColumnSelectionTable.class)) {
				dragOverColumnTable(dtde);
			}
		}

		/**
		 *
		 * @param dtde
		 */
		@Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		/**
		 *
		 * @param dte
		 */
		@Override
        public void dragExit(DropTargetEvent dte) {
			if (dte.getDropTargetContext().getComponent().getClass().equals(TableList.class)) {
				dragExitTable(dte);
			}
		}

		/**
		 *
		 * @param dtde
		 */
		@Override
        public void drop(DropTargetDropEvent dtde) {
			if (dtde.getDropTargetContext().getComponent() == viewer) {
				// drop a table
				dropViewer(dtde);
			}
			else if (dtde.getDropTargetContext().getComponent().getClass().equals(TableList.class)) {
				// drop a column on another column (create a relation)
				dropTable(dtde);
			}
			else if (dtde.getDropTargetContext().getComponent().getClass().equals(ColumnSelectionTable.class)) {
				// drop a column on the column selection table
				dropColumnTable(dtde);
			}
			else {
				dtde.rejectDrop();
			}
		}

	}

	/**
	 * drag source listener implementation
	 */
	private class DragSourceListenerImpl implements DragSourceListener {
		/**
		 *
		 * @param dsde
		 */
		@Override
        public void dragEnter(DragSourceDragEvent dsde) {
			if (dsde.getDragSourceContext().getComponent().getClass().equals(TableList.class)) {
				dragStartTable(dsde);
			}
		}

		/**
		 *
		 * @param dsde
		 */
		@Override
        public void dragOver(DragSourceDragEvent dsde) {
		}

		/**
		 *
		 * @param dsde
		 */
		@Override
        public void dropActionChanged(DragSourceDragEvent dsde) {
		}

		/**
		 *
		 * @param dse
		 */
		@Override
        public void dragExit(DragSourceEvent dse) {
		}

		/**
		 *
		 * @param dsde
		 */
		@Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
			if (dsde.getDragSourceContext().getComponent().getClass().equals(TableList.class)) {
				dragEndTable();
			}
		}

	}

	private class DeleteTableAction extends AbstractAction {

		DeleteTableAction() {
			super(SpringLocaleDelegate.getInstance().getMessage(
					"QueryBuilderController.1", "Ausgew\u00e4hlte Tabelle l\u00f6schen"));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			removeSelection();
		}
	}

	private class DeleteRelationAction extends AbstractAction {

		DeleteRelationAction() {
			super(SpringLocaleDelegate.getInstance().getMessage(
					"QueryBuilderController.2", "Relation l\u00f6schen"));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			removeSelection();
		}
	}

	private class EditRelationAction extends AbstractAction {

		private final RelationConnector connector;

		EditRelationAction(RelationConnector shape) {
			super(SpringLocaleDelegate.getInstance().getMessage(
					"QueryBuilderController.3", "Eigenschaften..."));
			connector = shape;
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			editProperties(connector);
		}
	}

	private static final String TABLE_PREFIX = "T";
	private long lAliasIndex = 1L;

	private final List<Table> lstTables = new ArrayList<Table>();
	private final List<PropertyChangeListener> lstPropertyChangeListener = new Vector<PropertyChangeListener>();

	protected QueryBuilderEditor editor;
	protected Schema currentSchema;
	protected DropTargetListenerImpl dropTargetListener;
	protected DragSourceListenerImpl dragSourceListener;
	protected TableList draggingList = null;
	protected RelationConnector selectedConnector = null;

	/**
	 *
	 * @param editor
	 */
	public QueryBuilderController(QueryBuilderEditor editor, Set<String> setQueryTypes) {
		super(editor.getViewer());
		this.editor = editor;
		this.setQueryTypes = setQueryTypes;
		this.viewer.getModel().addShapeModelListener(this);
		setPopupMenu(new JPopupMenu());

		refreshSchema();
		lAliasIndex = 1L;
	}

	public long getAliasIndex() {
		return lAliasIndex++;
	}

	/**
	 *
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		lstPropertyChangeListener.add(listener);
	}

	/**
	 *
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		lstPropertyChangeListener.remove(listener);
	}

	/**
	 *
	 * @param sPropertyName
	 * @param oldValue
	 * @param newValue
	 */
	protected void firePropertyChange(String sPropertyName, Object oldValue, Object newValue) {
		final PropertyChangeEvent ev = new PropertyChangeEvent(this, sPropertyName, oldValue, newValue);
		for (PropertyChangeListener propertyChangeListener : lstPropertyChangeListener) {
			propertyChangeListener.propertyChange(ev);
		}
	}

	/**
	 *
	 */
	public void removeTable() {
	}

	public void removeShape(Shape shape) {
		if (shape instanceof TableShape) {
			final TableShape tableshape = (TableShape) shape;
			lstTables.remove(tableshape.getTable());
		}
	}

	public void removeShapes(Collection<Shape> collShapes) {
		for (Shape shape : collShapes) {
			if (shape instanceof TableShape) {
				final TableShape tableshape = (TableShape) shape;
				lstTables.remove(tableshape.getTable());
				firePropertyChange("Tables", null, lstTables);

				// remove columns for this alias:
				final String sAlias = tableshape.getTable().getAlias();
				if (LOG.isDebugEnabled())
					LOG.debug("removeShaped: sAlias = " + sAlias);
				editor.getColumnSelectionPanel().getModel().removeColumnsForTable(sAlias);
			}
		}
	}

	/**
	 *
	 * @return current schema
	 */
	public Schema getCurrentSchema() {
		return currentSchema;
	}

	public void refreshSchema() {
		currentSchema = DatasourceDelegate.getInstance().getSchemaTables();
		editor.getTableSelectionPanel().setTables(getCurrentSchema().getTables(setQueryTypes));
	}

	/**
	 *
	 * @return drop target listener
	 */
	public DropTargetListenerImpl getDropTargetListener() {
		if (dropTargetListener == null) {
			dropTargetListener = new DropTargetListenerImpl();
		}
		return dropTargetListener;
	}

	/**
	 *
	 * @return drag source listener
	 */
	public DragSourceListenerImpl getDragSourceListener() {
		if (dragSourceListener == null) {
			dragSourceListener = new DragSourceListenerImpl();
		}
		return dragSourceListener;
	}

	/**
	 *
	 * @param dtde
	 */
	private void dragEnterViewer(DropTargetDragEvent dtde) {
		boolean bSupported = false;

		for (DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
			if (flavor.getDefaultRepresentationClass().equals(Table.class)) {
				;
			}
			bSupported = true;
		}
		if (bSupported) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else {
			dtde.rejectDrag();
		}
	}

	/**
	 *
	 * @param dtde
	 */
	private void dragEnterTable(DropTargetDragEvent dtde) {
		for (DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
			if (!dtde.getDropTargetContext().getComponent().equals(dtde.getSource())) {
				if (flavor.getRepresentationClass().equals(Column.class)) {
					dtde.acceptDrag(DnDConstants.ACTION_LINK);
					return;
				}
			}
		}
		dtde.rejectDrag();
	}

	/**
	 *
	 * @param dtde
	 */
	private void dragOverTable(DropTargetDragEvent dtde) {
		((TableList) dtde.getDropTargetContext().getComponent()).dragOver(dtde);
	}

	/**
	 *
	 * @param dtde
	 */
	private void dropTable(DropTargetDropEvent dtde) {
		try {
			for (DataFlavor flavor : dtde.getTransferable().getTransferDataFlavors()) {
				if (flavor.match(ColumnTransferable.ColumnDataFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_MOVE);
					createLink((TableList) dtde.getDropTargetContext().getComponent(), dtde.getTransferable());
					((TableList) dtde.getDropTargetContext().getComponent()).drop(dtde);
					dtde.dropComplete(true);
				}
			}
		}
		catch (Exception e) {
			LOG.warn("dropTable failed: " + e, e);
		}
	}

	/**
	 *
	 * @param dte
	 */
	private void dragExitTable(DropTargetEvent dte) {
		((TableList) dte.getDropTargetContext().getComponent()).dragExit(dte);
	}

	/**
	 *
	 * @param dtde
	 */
	private void dragEnterColumnTable(DropTargetDragEvent dtde) {
		for (DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
			if (flavor.getRepresentationClass().equals(Column.class)) {
				dtde.acceptDrag(DnDConstants.ACTION_LINK);
				return;
			}
		}
		dtde.rejectDrag();
	}

	/**
	 *
	 * @param dtde
	 */
	private void dragOverColumnTable(DropTargetDragEvent dtde) {
		((ColumnSelectionTable) dtde.getDropTargetContext().getComponent()).dragOver(dtde);
	}

	/**
	 *
	 * @param dtde
	 */
	private void dropColumnTable(DropTargetDropEvent dtde) {
		try {
			for (DataFlavor flavor : dtde.getTransferable().getTransferDataFlavors()) {
				if (flavor.match(ColumnTransferable.ColumnDataFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					fillColumn((ColumnSelectionTable) dtde.getDropTargetContext().getComponent(), dtde.getTransferable());
					((ColumnSelectionTable) dtde.getDropTargetContext().getComponent()).drop(dtde);
				}
			}
			dtde.dropComplete(true);
		}
		catch (Exception e) {
			LOG.warn("dropColumnTable failed: " + e, e);
			dtde.rejectDrop();
		}
	}

	/**
	 *
	 * @param targetList
	 * @param transferable
	 */
	private void createLink(TableList targetList, Transferable transferable) throws UnsupportedFlavorException, IOException {
		final ConstraintColumn sourceColumn = (ConstraintColumn) transferable.getTransferData(ColumnTransferable.ColumnDataFlavor);
		final ConstraintColumn targetColumn = targetList.getMarkedColumn();
		createLink(draggingList.getEmbeddingShape(), sourceColumn, targetList.getEmbeddingShape(), targetColumn);
		viewer.repaint();
	}

	/**
	 *
	 * @param sourceShape
	 * @param sourceColumn
	 * @param targetShape
	 * @param targetColumn
	 */
	private RelationConnector createLink(TableShape sourceShape, ConstraintColumn sourceColumn, TableShape targetShape,
			ConstraintColumn targetColumn) {
		final RelationConnectionPoint sourceCP = new RelationConnectionPoint(sourceShape, sourceColumn);
		final RelationConnectionPoint targetCP = new RelationConnectionPoint(targetShape, targetColumn);

		final RelationConnector result = new RelationConnector(sourceCP, targetCP);
		
		if (sourceCP.getTargetShape() == targetCP.getTargetShape()) {
			return null;
		}
		

		model.addShape(result);
		return result;
	}

	/**
	 *
	 * @param shape
	 */
	private void checkRelations(TableShape shape) {
		// Forward check
		for (ConstraintColumn cc : shape.getReferencingColumns()) {
			final ReferentialContraint c = (ReferentialContraint) cc.getForeignKeyConstraint();

			final Constraint fwdConstraint = currentSchema.getConstraint(c.getReferencedConstraint());
			if (fwdConstraint != null) {
				final TableShape targetShape = findShapeForTable(fwdConstraint.getTable());
				if (targetShape != null) {
					final ConstraintColumn targetColumn = targetShape.getConstraintColumn(c.getReferencedConstraint());
					final RelationConnector connector = createLink(shape, cc, targetShape, targetColumn);
					if (connector != null) {
						connector.setSourceCardinality(RelationConnector.CARDINALITY_N);
						connector.setTargetCardinality(RelationConnector.CARDINALITY_1);
					}
				}
			}
		}

		// Backward check
		for (Table table : lstTables) {
			final TableShape tableshape = findShapeForTable(table);
			if (!(tableshape == shape)) {
				for (ConstraintColumn cc : tableshape.getReferencingColumns()) {
					if (cc.getForeignKeyConstraint() != null) {
						final ReferentialContraint c = (ReferentialContraint) cc.getForeignKeyConstraint();
						final Constraint bwdConstraint = currentSchema.getConstraint(c.getReferencedConstraint());
						if (bwdConstraint != null) {
							if (bwdConstraint.getTable().getName().equals(shape.getTable().getName())) {
								final RelationConnector connector = createLink(tableshape, cc, shape, shape.getConstraintColumn(c.getReferencedConstraint()));
								connector.setSourceCardinality(RelationConnector.CARDINALITY_N);
								connector.setTargetCardinality(RelationConnector.CARDINALITY_1);
							}
						}
					}
				}
			}
		}
		viewer.repaint();
	}

	/**
	 *
	 * @param t
	 * @return table shape
	 */
	private TableShape findShapeForTable(Table t) {

		for (Iterator<Shape> i = viewer.getModel().getActiveLayer().getShapes().iterator(); i.hasNext();) {
			Shape shape = i.next();
			if (shape instanceof TableShape) {
				if (((TableShape) shape).getTable().equals(t)) {
					return (TableShape) shape;
				}
			}
		}

		return null;
	}

	/**
	 *
	 * @param sAlias
	 * @return table shape
	 */
	private TableShape findShapeByAlias(String sAlias) {

		for (Iterator<Shape> i = viewer.getModel().getActiveLayer().getShapes().iterator(); i.hasNext();) {
			Shape shape = i.next();
			if (shape instanceof TableShape) {
				if (((TableShape) shape).getTable().getAlias().equals(sAlias)) {
					return (TableShape) shape;
				}
			}
		}

		return null;
	}

	/**
	 *
	 * @param dsde
	 */
	private void dragStartTable(DragSourceDragEvent dsde) {
		draggingList = (TableList) dsde.getDragSourceContext().getComponent();
	}

	/**
	 *
	 */
	private void dragEndTable() {
		draggingList = null;
	}

	/**
	 *
	 * @param columnSelectionTable
	 * @param transferable
	 */
	private void fillColumn(ColumnSelectionTable columnSelectionTable,
			Transferable transferable) throws UnsupportedFlavorException, IOException {
		final ConstraintColumn sourceColumn = (ConstraintColumn) transferable.getTransferData(ColumnTransferable.ColumnDataFlavor);
		final ColumnSelectionTableModel model = (ColumnSelectionTableModel) columnSelectionTable.getModel();

		final ColumnEntry entry = new ColumnEntry();
		entry.setTable(sourceColumn.getTable());
		entry.setColumn(sourceColumn);
		entry.setVisible(true);
		entry.setAlias(entry.getColumn().getName().replaceAll("[%.,; -]", ""));
		model.addColumn(columnSelectionTable.getMarkedIndex(), entry);
		model.fireTableDataChanged();
	}

	/**
	 *
	 * @param e
	 */
	private void handleDoubleclick(MouseEvent e) {
		final TableList list = (TableList) e.getSource();
		final ConstraintColumn column = (ConstraintColumn) list.getSelectedValue();
		addColumnToSelectionPanel(column);
	}

	private void addColumnToSelectionPanel(ConstraintColumn column) {
		final ColumnSelectionTable table = editor.getColumnSelectionPanel().getTable();
		final ColumnSelectionTableModel model = (ColumnSelectionTableModel) table.getModel();
		model.fireTableDataChanged();
		final int iColumnIndex = table.getNextAvailableIndex();
		final ColumnEntry entry = model.getColumn(iColumnIndex);
		entry.setTable(column.getTable());
		entry.setColumn(column);
		entry.setAlias(entry.getColumn().getName().replaceAll("[%.,; -]", ""));
		entry.setVisible(true);
		table.scrollRectToVisible(table.getCellRect(0, iColumnIndex, true));
		model.fireTableDataChanged();
	}

	/**
	 *
	 * @param dtde
	 */
	private void dropViewer(DropTargetDropEvent dtde) {
		try {
			//final String sTable = (String) dtde.getTransferable().getTransferData(TableTransferable.TableDataFlavor);
			Transferable tr = dtde.getTransferable();
			for (DataFlavor flavor : tr.getTransferDataFlavors()) {
				if (flavor.match(TableTransferable.TableDataFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					final String sTable = (String) tr.getTransferData(TableTransferable.TableDataFlavor);
					final Table table = (Table) currentSchema.getTable(sTable).clone();
					table.setAlias(TABLE_PREFIX + getAliasIndex());

					if (table.getColumns().size() <= 0) {
						fetchColumns(table);
					}

					final TableShape shape = new TableShape(table);
					shape.setLocation(dtde.getLocation());
					lstTables.add(table);
					model.addShape(shape);
					firePropertyChange("Tables", null, lstTables);
					checkRelations(shape);
					addMouseListener(shape);
					dtde.dropComplete(true);
				}
			}
		}
		catch (Exception e) {
			LOG.warn("dropColumnTable failed: " + e, e);
			dtde.rejectDrop();
		}
	}

	/**
	 *
	 * @param sEntity
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param bCheckRelations
	 * @return table name
	 */
	public String addTable(String sAlias, String sEntity, double x, double y, double width, double height,
			boolean bCheckRelations) throws NuclosBusinessException {
		String iResult = null;
		final Table tableSchema = currentSchema.getTable(sEntity);
		if (tableSchema == null) {
			// @todo: throw exception
			LOG.warn("Could not find table of entity " + sEntity + " in schema.");
		}
		else {
			final Table table = (Table) tableSchema.clone();
			table.setAlias(TABLE_PREFIX + getAliasIndex());
			if (table.getColumns().size() <= 0) {
				fetchColumns(table);
			}

			final TableShape shape = new TableShape(table);
			shape.setLocation(x, y);
			shape.setDimension(new Rectangle2D.Double(x, y, width, height));
			lstTables.add(table);
			model.addShape(shape);
			firePropertyChange("Tables", null, lstTables);

			if (bCheckRelations) {
				checkRelations(shape);
			}
			addMouseListener(shape);
			iResult = table.getAlias();
		}
		return iResult;
	}

	/**
	 * Fetch columns and constraints when cache miss occured
	 * @param table
	 * @throws NuclosBusinessException
	 */
	private void fetchColumns(Table table) throws NuclosBusinessException {
		final Table t = DatasourceDelegate.getInstance().getSchemaColumns(table);
		table.getColumns().addAll(t.getColumns());
		table.getConstraints().addAll(t.getConstraints());
		getViewer().setCursor(Cursor.getDefaultCursor());
	}

	/**
	 *
	 * @param shape
	 */
	private void addMouseListener(TableShape shape) {
		shape.getTableList().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					handleDoubleclick(e);
				}
			}
		});
	}

	/**
	 *
	 * @param sSrcAlias
	 * @param sSrcColumn
	 * @param sSrcCardinality
	 * @param sDstAlias
	 * @param sDstColumn
	 * @param sDstCardinality
	 */
	public boolean addRelation(String sSrcAlias, String sSrcColumn, String sSrcCardinality, String sDstAlias,
			String sDstColumn, String sDstCardinality, String sJoinType) {
		final TableShape srcShape = findShapeByAlias(sSrcAlias);
		final TableShape dstShape = findShapeByAlias(sDstAlias);
		if (srcShape == null || dstShape == null) {
			return false;
		}
		
		ConstraintColumn srcColumn = srcShape.getColumn(sSrcColumn);
		ConstraintColumn dstColumn = dstShape.getColumn(sDstColumn);		
		if (srcColumn == null || dstColumn == null) {
			return false;
		}
			
		final RelationConnector connector = createLink(srcShape, srcColumn, dstShape, dstColumn);
		if (connector == null) {
			return false;
		}
		
		connector.setSourceCardinality(sSrcCardinality == null ? RelationConnector.CARDINALITY_NONE : sSrcCardinality);
		connector.setTargetCardinality(sDstCardinality == null ? RelationConnector.CARDINALITY_NONE : sDstCardinality);
		int joinType = RelationConnector.TYPE_JOIN;
		if (sJoinType != null) {
			if (sJoinType.equals("LeftOuterJoin")) {
				joinType = RelationConnector.TYPE_LEFTJOIN;
			}
			else if (sJoinType.equals("RightOuterJoin")) {
				joinType = RelationConnector.TYPE_RIGHTJOIN;
			}
		}
		connector.setJoinType(joinType);
		viewer.repaint();
		return true;
	}

	/**
	 * @param defaultColumns (could be null)
	 *
	 */
	public void newDocument(List<ColumnEntry> defaultColumns) {
		((AbstractComponentModel) viewer.getModel()).removeAllShapes();
		editor.getColumnSelectionPanel().getModel().removeAllColumns();
		if (defaultColumns != null) {
			for (int i = 0; i < defaultColumns.size(); i++) {
				ColumnEntry ce = defaultColumns.get(i);
				editor.getColumnSelectionPanel().getModel().addColumn(i, ce);
				editor.getColumnSelectionPanel().getTable().getColumnModel().getColumn(i).setPreferredWidth(ce.getDefaultWidth());
			}
		}
		editor.getTableSelectionPanel().getParameterPanel().getParameterModel().clear();
		lstTables.clear();
		viewer.repaint();
		lAliasIndex = 1L;
	}

	/**
	 *
	 * @param iPosition
	 * @param sTable
	 * @param sColumn
	 * @param bVisible
	 * @param sGroup
	 * @param sSort
	 * @return column entry
	 */
	public ColumnEntry addCondition(int iPosition, String sTable, String sColumn, String sAlias, boolean bVisible,
			String sGroup, String sSort) {
		final ColumnSelectionTableModel model = editor.getColumnSelectionPanel().getModel();
		final ColumnSelectionTable table = editor.getColumnSelectionPanel().getTable();
		ColumnEntry entry = null;

		final TableColumn column = table.getColumnModel().getColumn(iPosition);

		final TableShape shape = findShapeByAlias(sTable);
		if (shape != null) {
			Table t = shape.getTable();
			Column c = t.getColumn(sColumn);
			if (t != null && c != null) {
				entry = model.getColumn(column.getModelIndex());
				entry.setTable(t);
				entry.setColumn(new ConstraintColumn(c));
				if (sAlias == null || sAlias.length() == 0) {
					String sNewAlias = c.getName();
					entry.setAlias(sNewAlias.replaceAll("[%.,; -]", ""));
				}
				else {
					entry.setAlias(sAlias);
				}
				entry.setVisible(bVisible);
				entry.setGroupBy(sGroup);
				entry.setOrderBy(sSort);
				model.fireTableDataChanged();
			}
		}
		return entry;
	}

	@Override
    public void modelChanged() {
	}

	@Override
    public void selectionChanged(Shape shape) {
		final JPopupMenu menu = new JPopupMenu();
		if (shape != null) {
			if (shape instanceof RelationConnector) {
				menu.add(new DeleteRelationAction());
				menu.addSeparator();
				menu.add(new EditRelationAction((RelationConnector) shape));
			}
			else if (shape instanceof TableShape) {
				menu.add(new DeleteTableAction());
			}
		}
		setPopupMenu(menu);
	}

	@Override
    public void multiSelectionChanged(Collection<Shape> collShapes) {
		getPopupMenu().removeAll();
	}

	@Override
    public void shapeDeleted(Shape shape) {
	}

	@Override
    public void shapesDeleted(Collection<Shape> collShapes) {
	}

	private void removeSelection() {
		model.removeShapes(model.getSelection());
		viewer.repaint();
	}

	private void editProperties(RelationConnector connector) {
		final RelationConnectionPoint src = (RelationConnectionPoint) connector.getSourceConnection();
		final RelationConnectionPoint dst = (RelationConnectionPoint) connector.getDestinationConnection();

		final JoinPanel pnlJoin = new JoinPanel(src.getColumn().getTable().getName(),
				src.getColumn().getName(),
				dst.getColumn().getTable().getName(),
				dst.getColumn().getName());
		switch (connector.getJoinType()) {
			case RelationConnector.TYPE_JOIN:
				pnlJoin.rbJoinEqual.setSelected(true);
				break;
			case RelationConnector.TYPE_LEFTJOIN:
				pnlJoin.rbJoinLeft.setSelected(true);
				break;
			case RelationConnector.TYPE_RIGHTJOIN:
				pnlJoin.rbJoinRight.setSelected(true);
				break;
		}

		if (JOptionPane.showConfirmDialog(UIUtils.getFrameForComponent(editor), pnlJoin, SpringLocaleDelegate.getInstance().getMessage(
				"QueryBuilderController.4", "Eigenschaften von Relation"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {
			connector.setJoinType(pnlJoin.getJoinType());
			viewer.getModel().fireModelChanged();
		}
	}
	
}	// class QueryBuilderController
