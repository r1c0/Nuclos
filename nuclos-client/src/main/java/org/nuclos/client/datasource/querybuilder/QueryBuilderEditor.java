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
package org.nuclos.client.datasource.querybuilder;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.datasource.admin.DatasourceEditPanel;
import org.nuclos.client.datasource.admin.DatasourceEntityOptions;
import org.nuclos.client.datasource.querybuilder.controller.QueryBuilderController;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry;
import org.nuclos.client.datasource.querybuilder.gui.ColumnSelectionPanel;
import org.nuclos.client.datasource.querybuilder.gui.ColumnSelectionTable;
import org.nuclos.client.datasource.querybuilder.gui.ColumnSelectionTableModel;
import org.nuclos.client.datasource.querybuilder.gui.ParameterModel;
import org.nuclos.client.datasource.querybuilder.gui.TableSelectionPanel;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry.ConditionEntry;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnectionPoint;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnector;
import org.nuclos.client.datasource.querybuilder.shapes.TableShape;
import org.nuclos.client.gef.AbstractShapeModel;
import org.nuclos.client.gef.DefaultComponentModel;
import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.ShapeModelListener;
import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class QueryBuilderEditor extends JPanel {

	public static final String ALIAS_INTID = "_internal_lo_intid";

	public static final String KEY_TABLES = "SkippedTables";
	public static final String KEY_COLUMNS = "SkippedColumns";
	public static final String KEY_RELATIONS = "SkippedRelations";

	private final DatasourceEditPanel parent;

	private final JSplitPane splitpnTab = new JSplitPane();
	private final JSplitPane splitpnViewer = new JSplitPane();
	private final TableSelectionPanel pnlTableSelection;
	private final ColumnSelectionPanel pnlColumnSelection;
	private final QueryBuilderViewer viewer = new QueryBuilderViewer();
	private final JScrollPane scrlpn = new JScrollPane();
	private final QueryBuilderController ctl;
	private final List<ChangeListener> lstChangeListener = new Vector<ChangeListener>();
	private final Map<String, String> mpTables = CollectionUtils.newHashMap();
	private final List<String> lstSkippedTables = new ArrayList<String>();
	private final List<String> lstSkippedColumns = new ArrayList<String>();
	private final List<String> lstSkippedRelations = new ArrayList<String>();

	/**
	 *
	 */
	public QueryBuilderEditor(DatasourceEditPanel parent, 
			Set<String> setQueryTypes, 
			boolean blnWithParameters, 
			boolean blnWithValuelistProviderColumn,
			boolean blnWithParameterLabelColumn) {
		this.parent = parent;
		this.pnlTableSelection = new TableSelectionPanel(blnWithParameters, 
			blnWithValuelistProviderColumn, 
			blnWithParameterLabelColumn);
		setLayout(new BorderLayout());
		ctl = new QueryBuilderController(this, setQueryTypes);

		viewer.setExtents(new Extents2D(1600, 1024));
		scrlpn.setViewportView(viewer);

		pnlColumnSelection = new ColumnSelectionPanel(ctl);

		splitpnViewer.setResizeWeight(0.75);
		splitpnViewer.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitpnViewer.setTopComponent(scrlpn);
		splitpnViewer.setBottomComponent(pnlColumnSelection);
		splitpnViewer.setOneTouchExpandable(true);

		splitpnTab.setResizeWeight(0.2);
		splitpnTab.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitpnTab.setLeftComponent(pnlTableSelection);
		splitpnTab.setRightComponent(splitpnViewer);
		splitpnTab.setOneTouchExpandable(true);

		UIUtils.clearKeymaps(splitpnViewer);
		UIUtils.clearKeymaps(splitpnTab);

		pnlTableSelection.setTables(ctl.getCurrentSchema().getTables(setQueryTypes));
		add(splitpnTab, BorderLayout.CENTER);

		viewer.getModel().addShapeModelListener(new ShapeModelListener() {
			@Override
            public void modelChanged() {
				fireChange();
			}

			@Override
            public void multiSelectionChanged(Collection<Shape> collShapes) {
			}

			@Override
            public void selectionChanged(Shape shape) {
			}

			@Override
            public void shapeDeleted(Shape shape) {
				fireChange();
			}

			@Override
            public void shapesDeleted(Collection<Shape> collShapes) {
				fireChange();
			}
		});

		pnlColumnSelection.getModel().addTableModelListener(new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent e) {
				fireChange();
			}
		});

		pnlColumnSelection.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
            public void columnAdded(TableColumnModelEvent e) {
				fireChange();
			}

			@Override
            public void columnMarginChanged(ChangeEvent e) {
			}

			@Override
            public void columnMoved(TableColumnModelEvent e) {
				fireChange();
			}

			@Override
            public void columnRemoved(TableColumnModelEvent e) {
				fireChange();
			}

			@Override
            public void columnSelectionChanged(ListSelectionEvent e) {
			}
		});

		pnlTableSelection.getParameterPanel().getParameterTable().getModel().addTableModelListener(new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent e) {
				fireChange();
			}
		});
	}

	public QueryBuilderController getController() {
		return this.ctl;
	}

	/**
	 * @return column selection panel
	 */
	public ColumnSelectionPanel getColumnSelectionPanel() {
		return pnlColumnSelection;
	}

	/**
	 * @return table selection panel
	 */
	public TableSelectionPanel getTableSelectionPanel() {
		return pnlTableSelection;
	}

	/**
	 * @return scroll pane
	 */
	public JScrollPane getScrPane() {
		return scrlpn;
	}

	/**
	 * @return tab split pane
	 */
	public JSplitPane getTabSplitPane() {
		return splitpnTab;
	}

	/**
	 * @return viewer
	 */
	public QueryBuilderViewer getViewer() {
		return viewer;
	}

	/**
	 * @return viewer split pane
	 */
	public JSplitPane getViewerSplitPane() {
		return splitpnViewer;
	}

	/**
	 * @param changelistenerDetailsChanged
	 */
	public void addChangeListener(ChangeListener changelistenerDetailsChanged) {
		lstChangeListener.add(changelistenerDetailsChanged);
	}

	/**
	 * @param changelistenerDetailsChanged
	 */
	public void removeChangeListener(ChangeListener changelistenerDetailsChanged) {
		lstChangeListener.remove(changelistenerDetailsChanged);
	}

	/**
	 * @param defaultColumns 
	 *
	 */
	public void newDatasource(List<ColumnEntry> defaultColumns) {
		ctl.newDocument(defaultColumns);
	}

	/**
	 * @param reportXML
	 */
	public Map<String, List<String>> setXML(String reportXML) throws NuclosBusinessException {
		final Map<String, List<String>> result = new HashMap<String, List<String>>();

		lstSkippedTables.clear();
		lstSkippedColumns.clear();
		lstSkippedRelations.clear();

		pnlColumnSelection.cancelEditing();
		try {
			result.put(KEY_TABLES, lstSkippedTables);
			result.put(KEY_COLUMNS, lstSkippedColumns);
			result.put(KEY_RELATIONS, lstSkippedRelations);

			ctl.newDocument(null);

			final XMLReader parser = XMLUtils.newSAXParser();
			final XMLContentHandler xmlContentHandler = new XMLContentHandler();
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", xmlContentHandler);
			parser.setContentHandler(xmlContentHandler);
			parser.setEntityResolver(new EntityResolver() {
				@Override
                public InputSource resolveEntity(String publicId, String systemId) throws IOException {
					InputSource result = null;
					if (systemId.equals(QueryBuilderConstants.SYSTEMID)) {
						final URL url = this.getClass().getClassLoader().getResource(QueryBuilderConstants.RESOURCE_PATH);
						if (url == null) {
							throw new NuclosFatalException("DTD f\u00fcr SystemID " + QueryBuilderConstants.SYSTEMID + "kann nicht gefunden werden");
						}
						result = new InputSource(new BufferedInputStream(url.openStream()));
					}
					return result;
				}
			});
			parent.setIsModelUsed(true);
			parser.parse(new InputSource(new StringReader(reportXML)));
		}
		catch (IOException e) {
			throw new NuclosFatalException(e);
		}
		catch (SAXException e) {
			throw new NuclosFatalException(e);
		}
		return result;
	}

	/**
	 * @return
	 * @throws NuclosBusinessException
	 */
	public String getXML(DatasourceEntityOptions eOptions) throws CommonBusinessException {
		String result = "";
		try {
			final Document doc = DocumentHelper.createDocument();
			doc.addDocType(QueryBuilderConstants.DOCTYPE, null, QueryBuilderConstants.SYSTEMID);

			Element root = doc.addElement(QueryBuilderConstants.DOCTYPE);
			Element header = root.addElement(QueryBuilderConstants.TAG_HEADER);
			Element tables = root.addElement(QueryBuilderConstants.TAG_TABLES);
			Element connectors = root.addElement(QueryBuilderConstants.TAG_CONNECTORS);
			Element columns = root.addElement(QueryBuilderConstants.TAG_COLUMNS);
			Element parameters = root.addElement(QueryBuilderConstants.TAG_PARAMETERS);
			Element sql = root.addElement(QueryBuilderConstants.TAG_SQL);

			serializeHeader(header);
			serializeTables(tables);
			serializeConnectors(connectors);
			serializeColumns(columns);
			serializeParameters(parameters);
			
			if (eOptions != null) {
				Element entityoptions = root.addElement(QueryBuilderConstants.TAG_ENTITYOPTIONS);
				entityoptions.addAttribute("dynamic", eOptions.isDynamic()? "yes" : "no" );
			}

			final boolean bModelUsed = parent.isModelUsed();
			sql.addAttribute("isModelUsed", bModelUsed ? "true" : "false");
			if (!bModelUsed) {
				sql.addCDATA(StringUtils.emptyIfNull(parent.getSql()));
			}

			final OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			//format.setEncoding("ISO-8859-1");
			final StringWriter sw = new StringWriter();
			final XMLWriter xmlw = new XMLWriter(sw, format);
			xmlw.write(doc);
			xmlw.close();
			result = sw.toString();
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
		return result;
	}

	/**
	 * @param el
	 */
	private void serializeParameters(Element el) {
		final ParameterModel model = pnlTableSelection.getParameterPanel().getParameterModel();

		for (DatasourceParameterVO paramvo : (model.getParameters())) {
			final Element entry = el.addElement(QueryBuilderConstants.TAG_PARAMETER);
			entry.addAttribute("name", paramvo.getParameter());
			entry.addAttribute("type", paramvo.getDatatype());
			entry.addAttribute("description", paramvo.getDescription());
			// entry.addAttribute("id", paramvo.getDatasourceId().toString());
			if (paramvo.getValueListProvider() != null) {
				final Element vlp = entry.addElement(QueryBuilderConstants.TAG_VALUELISTPROVIDER);
				vlp.addAttribute("type", paramvo.getValueListProvider().getType());
				for (Map.Entry<String, String> parameter : paramvo.getValueListProvider().getParameters().entrySet()) {
					final Element param = vlp.addElement(QueryBuilderConstants.TAG_VALUELISTPROVIDER_PARAMETER);
					param.addAttribute("name", parameter.getKey());
					param.addAttribute("value", parameter.getValue());
				}
			}
		}
	}

	/**
	 * @param el
	 */
	protected void serializeHeader(Element el) {
		el.addAttribute("name", parent.getDatasourceName());
		if (parent.getEntity() != null)
			el.addAttribute("entity", parent.getEntity());
		el.addAttribute("description", parent.getDatasourceDescription());
	}

	/**
	 * @param el
	 */
	protected void serializeTables(Element el) {
		final DefaultComponentModel model = (DefaultComponentModel) viewer.getModel();
		for (Iterator<AbstractShapeModel.Layer> iterLayer = model.getVisibleLayers().iterator(); iterLayer.hasNext();) {
			final AbstractShapeModel.Layer layer = iterLayer.next();

			for (Iterator<Shape> iterShapes = layer.getShapes().iterator(); iterShapes.hasNext();) {
				final Shape shape = iterShapes.next();
				if (shape instanceof TableShape) {
					final TableShape tableshape = (TableShape) shape;
					final Element table = el.addElement(QueryBuilderConstants.TAG_TABLE);
					table.addAttribute("id", tableshape.getTable().getAlias());
					table.addAttribute("entity", tableshape.getTable().getName());
					table.addAttribute("x", Double.toString(tableshape.getX()));
					table.addAttribute("y", Double.toString(tableshape.getY()));
					table.addAttribute("w", Double.toString(tableshape.getWidth()));
					table.addAttribute("h", Double.toString(tableshape.getHeight()));
				}
			}
		}
	}

	/**
	 * return a list of datasource names which are used by this model
	 * @return List<String> of all used datasources
	 */
	public List<String> getUsedDatasources() {
		final List<String> lstResult = new ArrayList<String>();
		final DefaultComponentModel model = (DefaultComponentModel) viewer.getModel();
		for (Iterator<AbstractShapeModel.Layer> iterLayer = model.getVisibleLayers().iterator(); iterLayer.hasNext();) {
			final AbstractShapeModel.Layer layer = iterLayer.next();

			for (Iterator<Shape> iterShapes = layer.getShapes().iterator(); iterShapes.hasNext();) {
				final Shape shape = iterShapes.next();
				if (shape instanceof TableShape) {
					final TableShape tableshape = (TableShape) shape;
					if (tableshape.getTable().isQuery()) {
						lstResult.add(tableshape.getTable().getName());
					}
				}
			}
		}
		return lstResult;
	}

	/**
	 * @param el
	 */
	protected void serializeConnectors(Element el) {
		final DefaultComponentModel model = (DefaultComponentModel) viewer.getModel();
		for (Iterator<AbstractShapeModel.Layer> iterLayer = model.getVisibleLayers().iterator(); iterLayer.hasNext();) {
			final AbstractShapeModel.Layer layer = iterLayer.next();

			for (Iterator<Shape> iterShapes = layer.getShapes().iterator(); iterShapes.hasNext();) {
				final Shape shape = iterShapes.next();
				if (shape instanceof RelationConnector) {
					final RelationConnector relationconnector = (RelationConnector) shape;
					final TableShape tableshapeSource = (TableShape) relationconnector.getSourceConnection().getTargetShape();
					final TableShape tableshapeDest = (TableShape) relationconnector.getDestinationConnection().getTargetShape();
					final Element connector = el.addElement(QueryBuilderConstants.TAG_CONNECTOR);
					connector.addAttribute("srctableid", tableshapeSource.getTable().getAlias());
					connector.addAttribute("srccolumn", ((RelationConnectionPoint) relationconnector.getSourceConnection()).getColumn().getName());
					connector.addAttribute("srccardinality", relationconnector.getSourceCardinality());
					connector.addAttribute("dsttableid", tableshapeDest.getTable().getAlias());
					connector.addAttribute("dstcolumn", ((RelationConnectionPoint) relationconnector.getDestinationConnection()).getColumn().getName());
					connector.addAttribute("dstcardinality", relationconnector.getTargetCardinality());
					connector.addAttribute("jointype", relationconnector.getJoinType() == RelationConnector.TYPE_JOIN ? "Join" :
							(relationconnector.getJoinType() == RelationConnector.TYPE_LEFTJOIN ? "LeftOuterJoin" : "RightOuterJoin"));
				}
			}
		}
	}

	/**
	 * @param el
	 * @throws NuclosBusinessException
	 */
	protected void serializeColumns(Element el) throws CommonBusinessException {
		final ColumnSelectionTableModel model = pnlColumnSelection.getModel();
		final ColumnSelectionTable table = pnlColumnSelection.getTable();

		final Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn tablecolumn = enumeration.nextElement();
			final ColumnEntry entry = model.getColumn(tablecolumn.getModelIndex());
			if (entry.getTable() == null && entry.getColumn() != null) {
				throw new CommonBusinessException("Die Spalte " + entry.getColumn().getName() + " enth\u00e4lt keinen Tabellennamen.");
			}
			if (entry.getTable() != null && entry.getColumn() != null) {
				final Element column = el.addElement(QueryBuilderConstants.TAG_COLUMN);
				column.addAttribute("table", entry.getTable().getAlias());
				column.addAttribute("column", entry.getColumn().getName());
				column.addAttribute("alias", entry.getAlias());
				column.addAttribute("visible", entry.isVisible() ? "yes" : "no");
				column.addAttribute("group", entry.getGroupBy());
				column.addAttribute("sort", entry.getOrderBy());
				column.addAttribute("type", entry.getColumn().getType().getTypeName());
				column.addAttribute("length", String.valueOf(entry.getColumn().getLength()));
				column.addAttribute("nullable", entry.getColumn().isNullable() ? "yes" : "no");
				column.addAttribute("precision", String.valueOf(entry.getColumn().getPrecision()));
				column.addAttribute("scale", String.valueOf(entry.getColumn().getScale()));

				for (ConditionEntry ce : entry.getConditions()) {
					if (ce.getCondition() != null && ce.getCondition().length() > 0) {
						final Element condition = column.addElement(QueryBuilderConstants.TAG_CONDITION);
						condition.addAttribute("text", ce.getCondition());
					}
				}
				entry.getConditions();
			}
		}
	}

	/**
	 *
	 */
	protected void fireChange() {
		for (ChangeListener cl : lstChangeListener) {
			cl.stateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * @param lstParams
	 */
	public void setParameter(List<DatasourceParameterVO> lstParams) {
		final ParameterModel model = pnlTableSelection.getParameterPanel().getParameterModel();
		model.clear();
		for (DatasourceParameterVO paramvo : lstParams) {
			model.addEntry(paramvo);
		}
	}

	/**
	 * @return used parameters
	 */
	public List<DatasourceParameterVO> getParameters() {
		final List<DatasourceParameterVO> result = new ArrayList<DatasourceParameterVO>();

		for (DatasourceParameterVO paramvo : pnlTableSelection.getParameterPanel().getParameterModel().getParameters()) {
			result.add(paramvo);
		}
		return result;
	}

	/**
	 *
	 */
	public void cancelEditing() {
		getColumnSelectionPanel().cancelEditing();
		getTableSelectionPanel().getParameterPanel().cancelEditing();
	}

	/**
	 *
	 * @return true if editing could be stopped
	 */
	public boolean stopEditing() {
		return getColumnSelectionPanel().stopEditing() & getTableSelectionPanel().getParameterPanel().stopEditing();
	}

	/**
	 *
	 * @param mp
	 */
	public static String getSkippedElements(Map<String, List<String>> mp) {
		final List<String> lstTableNames = mp.get(KEY_TABLES);
		final List<String> lstColumnNames = mp.get(KEY_COLUMNS);
		final List<String> lstRelationNames = mp.get(KEY_RELATIONS);

		final StringBuffer sb = new StringBuffer();

		for (String sTableName : lstTableNames) {
			sb.append("Tabelle ").append(sTableName).append("\n");
		}
		for (String sColumnName : lstColumnNames) {
			sb.append("Spalte ").append(sColumnName).append("\n");
		}
		for (String sRelationName : lstRelationNames) {
			sb.append("Relation ").append(sRelationName).append("\n");
		}
		return sb.toString();
	}

	class XMLContentHandler implements ContentHandler, LexicalHandler {
		ColumnEntry currEntry = null;
		int iPosition = 0;
		boolean isCDATA = false;
		StringBuffer sbCDATA;

		@Override
        public void characters(char[] ac, int start, int length) {
			if (isCDATA) {
				sbCDATA.append(ac, start, length);
			}
		}

		@Override
        public void endDocument() {
		}

		@Override
        public void endElement(String namespaceURI, String localName, String qName) {
			if (qName.equals(QueryBuilderConstants.TAG_SQL)) {
				parent.setSql(sbCDATA == null ? "" : sbCDATA.toString().trim());
			}
			sbCDATA = null;
		}

		@Override
        public void endPrefixMapping(String prefix) {
		}

		@Override
        public void ignorableWhitespace(char[] ac, int start, int length) {
		}

		@Override
        public void processingInstruction(String target, String data) {
		}

		@Override
        public void skippedEntity(String name) {
		}

		@Override
        public void startDocument() {
		}

		@Override
        public void startPrefixMapping(String prefix, String uri) {
		}

		@Override
        public void setDocumentLocator(Locator loc) {
		}

		@Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
			if (qName.equals(QueryBuilderConstants.TAG_HEADER)) {
				parent.setDatasourceName(atts.getValue("name"));
				if (atts.getValue("entity") != null)
					parent.setEntity(atts.getValue("entity"));
				parent.setDatasourceDescription(atts.getValue("description"));
			}

			// process tables
			else if (qName.equals(QueryBuilderConstants.TAG_TABLE)) {
				final String sOldTableId = atts.getValue("id");
				try {
					final String sEntity = atts.getValue("entity");
					final String sNewTableId = QueryBuilderEditor.this.ctl.addTable(sOldTableId, sEntity,
							Double.parseDouble(atts.getValue("x")), Double.parseDouble(atts.getValue("y")),
							Double.parseDouble(atts.getValue("w")), Double.parseDouble(atts.getValue("h")), false);
					if (sNewTableId != null) {
						mpTables.put(sOldTableId, sNewTableId);
					}
					else {
						lstSkippedTables.add(sEntity);
					}

				}
				catch (NuclosBusinessException ex) {
					throw new NuclosFatalException(ex);
				}
				catch (NumberFormatException ex) {
					throw new NuclosFatalException(ex);
				}
			}

			// process connectors
			else if (qName.equals(QueryBuilderConstants.TAG_CONNECTOR)) {
				final String sSrcTableId = mpTables.get(atts.getValue("srctableid"));
				final String sDstTableId = mpTables.get(atts.getValue("dsttableid"));
				final String sSrcColumn = atts.getValue("srccolumn");
				final String sDstColumn = atts.getValue("dstcolumn");
				final String sSrcCardinality = atts.getValue("srccardinality");
				final String sDstCardinality = atts.getValue("dstcardinality");
				final String sJoinType = atts.getValue("jointype");
				boolean success =QueryBuilderEditor.this.ctl.addRelation(
						sSrcTableId, sSrcColumn, sSrcCardinality,
						sDstTableId, sDstColumn, sDstCardinality, sJoinType);
				if (!success) {
					lstSkippedRelations.add(sSrcTableId + "." + sSrcColumn
							+ "-" + sDstTableId + "." + sDstColumn);

				}
			}

			// process columns
			else if (qName.equals(QueryBuilderConstants.TAG_COLUMN)) {
				final String sTable = mpTables.get(atts.getValue("table"));
				final String sColumn = atts.getValue("column");
				final String sAlias = atts.getValue("alias");
				final boolean bVisible = atts.getValue("visible").equals("yes");
				final String sGroup = atts.getValue("group");
				final String sSort = atts.getValue("sort");

				currEntry = QueryBuilderEditor.this.ctl.addCondition(iPosition++, sTable, sColumn, sAlias, bVisible,
						sGroup, sSort);
				if (currEntry == null) {
					lstSkippedColumns.add(sTable + "." + sColumn);
				}
			}

			// process conditions
			else if (qName.equals(QueryBuilderConstants.TAG_CONDITION)) {
				if (currEntry != null) {
					currEntry.addCondition(atts.getValue("text"));
				}
			}
			// process parameters
			else if (qName.equals(QueryBuilderConstants.TAG_PARAMETER)) {
				final String sName = atts.getValue("name");
				final String sType = atts.getValue("type");
				final String sDescription = atts.getValue("description");
				pnlTableSelection.getParameterPanel().addParameter(null, sName, sType, sDescription);
			}
			// process sql
			else if (qName.equals(QueryBuilderConstants.TAG_SQL)) {
				final String strIsModelUsed = atts.getValue("isModelUsed");
				parent.setIsModelUsed(strIsModelUsed == null || strIsModelUsed.equals("true"));
			}
		}

		@Override
        public void endCDATA() throws SAXException {
			isCDATA = false;
		}

		@Override
        public void endDTD() throws SAXException {
		}

		@Override
        public void startCDATA() throws SAXException {
			sbCDATA = new StringBuffer();
			isCDATA = true;
		}

		@Override
        public void comment(char[] ch, int start, int length) throws SAXException {
		}

		@Override
        public void endEntity(String name) throws SAXException {
		}

		@Override
        public void startEntity(String name) throws SAXException {
		}

		@Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
		}
	}

}	// class QueryBuilderEditor
