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
package org.nuclos.client.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectableEntityFieldBasedTableModel;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;

/**
 * A Frame with system information for administrative and debugging purposes.
 * This frame can be invoked with STRG-SHIFT-F11
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class SwingDebugFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JScrollPane scrl;
	JEditorPane text;
	MainController ctrl;

	SwingDebugFrame(MainController ctrl) {
		super();
		this.ctrl = ctrl;
		text = new JEditorPane("text/html", "<html>No component</html>");
		text.setEditable(false);

		this.setSize(500, 300);
		scrl = new JScrollPane(text);

		/*Application Properties */
		final JEditorPane epAppProps = new JEditorPane();
		epAppProps.setContentType("text/html");
		epAppProps.setEditable(false);
		epAppProps.setText(ApplicationProperties.getInstance().toHtml());

		/* Client System Properties */
		final JEditorPane epClient = new JEditorPane();
		epClient.setContentType("text/html");
		epClient.setEditable(false);
		epClient.setText(getClientProperties());

		final JEditorPane epT_ad_parameters = new JEditorPane();
		epT_ad_parameters.setContentType("text/html");
		epT_ad_parameters.setEditable(false);
		epT_ad_parameters.setText(getSystemParameters());

		final JEditorPane epDbInfo = new JEditorPane();
		epDbInfo.setContentType("text/html");
		epDbInfo.setEditable(false);
		final JPanel pnlDbInfo = new JPanel(new BorderLayout());
		final JButton btnDbInfo = new JButton(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
				epDbInfo.setText(cmdDbInfo());
			}
		});
		btnDbInfo.setText("Load from DB Metainformation");
		btnDbInfo.setToolTipText("Get the Metainformation from a DB Connection");
		pnlDbInfo.add(btnDbInfo, BorderLayout.NORTH);
		pnlDbInfo.add(epDbInfo, BorderLayout.CENTER);

		final JEditorPane epServerProps = new JEditorPane();
		epServerProps.setContentType("text/html");
		epServerProps.setEditable(false);
		final JPanel pnlServerProps = new JPanel(new BorderLayout());
		final JButton btnServerProps = new JButton(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
				epServerProps.setText(cmdGetServerProps());
			}
		});
		btnServerProps.setText("Get From Server");
		btnServerProps.setToolTipText("Get the system properties from the server environment");
		pnlServerProps.add(btnServerProps, BorderLayout.NORTH);
		pnlServerProps.add(epServerProps, BorderLayout.CENTER);

		/* add the tabs */
		final JTabbedPane tp = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tp.addTab("Component Debugger", scrl);
		tp.addTab("Application Properties", new JScrollPane(epAppProps));
		tp.addTab("Client System Properties", new JScrollPane(epClient));
		tp.addTab("Server System Properties", new JScrollPane(pnlServerProps));
		tp.addTab("T_AD_PARAMETER", new JScrollPane(epT_ad_parameters));
		tp.addTab("Database Meta Data", new JScrollPane(pnlDbInfo));

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tp, BorderLayout.CENTER);
		this.setTitle("System Information");

		setAlwaysOnTop(true);
		setVisible(false);
	}

	private String cmdDbInfo() {
		try {
			return ServiceLocator.getInstance().getFacade(ConsoleFacadeRemote.class).getDatabaseInformationAsHtml();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private String cmdGetServerProps() {
		try {
			return ServiceLocator.getInstance().getFacade(ConsoleFacadeRemote.class).getSystemPropertiesAsHtml();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

 	private String getClientProperties() {
		final StringBuilder sbClient = new StringBuilder();
		sbClient.append("<html><b>Java System Properties (Client):</b>");
		sbClient.append("<table border=\"1\">");
		List<String> keys = new ArrayList<String>(System.getProperties().stringPropertyNames());
		Collections.sort(keys);
		for (final String key : keys) {
			sbClient.append("<tr><td><b>" + key + "</b></td><td>" + System.getProperty(key) + "</td></tr>");
		}
		sbClient.append("</table></html>\n");
		return sbClient.toString();
	}

	private String getSystemParameters() {
		final StringBuilder sbClient = new StringBuilder();
		sbClient.append("<html><b>T_AD_PARAMETER Settings (from Cache):</b>");
		sbClient.append("<table border=\"1\">");
		Map<String, String> mpParameters = ClientParameterProvider.getInstance().getAllParameters();
		List<String>lstSortedKeys = new ArrayList<String>(mpParameters.keySet());
		Collections.sort(lstSortedKeys);
		for (String sKey : lstSortedKeys) {
			sbClient.append("<tr><td><b>" + sKey + "</b></td><td>" + mpParameters.get(sKey) + "</td></tr>");
		}
		sbClient.append("</table></html>\n");
		return sbClient.toString();
	}

	void showComponentDetails(Component comp) {
		final StringBuilder sb = new StringBuilder(1000);

		sb.append("<html>\n");
		addGeneralInfo(sb, comp);

		if (comp instanceof JTree) {
			addTreeInfos(sb, (JTree) comp);
		}
		else if (comp instanceof SubForm.SubFormTable) {
			addSubFormInfo(sb, (SubForm.SubFormTable) comp);
		}
		else {
			addFieldInfos(sb, comp);
		}

		sb.append("</html>");

		text.setText(sb.toString());

		Rectangle rectComp = comp.getBounds();
		comp.getGraphics().setColor(Color.red);
		comp.getGraphics().drawRect(0, 0, rectComp.width-1, rectComp.height-1);

		if (!this.isVisible()) {
			this.setVisible(true);
		}
	}

	private void addGeneralInfo(StringBuilder sb, Component comp) {
		sb.append("<b>Default Info</b><br>");
		sb.append("  Name: ").append(comp.getName()).append("<br>");
		sb.append("  Class: ").append(comp.getClass().getName()).append("<br>");
		sb.append("  Identity Hash: ").append(Integer.toString(System.identityHashCode(comp), 16)).append("<br>");
		sb.append("  Enabled: ").append(comp.isEnabled()).append("<br>");
		sb.append("  Container: ").append(comp instanceof Container).append("<br>");
		sb.append("  Focusable: ").append(comp.isFocusable()).append("<br>");
		sb.append("  Current size: (width=").append(comp.getSize().width).append(", height=").append(comp.getSize().height).append(")<br>");
		sb.append("  Minimum size: (width=").append(comp.getMinimumSize().width).append(", height=").append(comp.getMinimumSize().height).append(")<br>");
		sb.append("  Preferred size: (width=").append(comp.getPreferredSize().width).append(", height=").append(comp.getPreferredSize().height).append(")<br>");
		sb.append("  Maximum size: (width=").append(comp.getMaximumSize().width).append(", height=").append(comp.getMaximumSize().height).append(")<br>");
		sb.append("  Font: ").append(comp.getGraphics().getFont().getFontName()).append(" ").append(comp.getGraphics().getFont().getSize()).append("<br>");
		sb.append("  Parent name: ").append(comp.getParent().getName()).append("<br>");
		sb.append("  Parent class: ").append(comp.getParent().getClass().getName()).append("<br>");
	}

	@SuppressWarnings("deprecation")
	private void addSubFormInfo(StringBuilder sb, SubForm.SubFormTable subFormTable) {
		final int iColumn = subFormTable.columnAtPoint(subFormTable.getMousePosition());
		final int iRow = subFormTable.rowAtPoint(subFormTable.getMousePosition());

		// SubFormTable -> JViewport -> JScrollPane -> SubForm
		final SubForm subform = (SubForm) SwingUtilities.getAncestorOfClass(SubForm.class, subFormTable); 

		sb.append("<b>Subform Info</b><br>");
		final String sEntity = subform.getEntityName();
		sb.append("  Subform entity: ").append(sEntity).append("<br>");
		//MasterDataMetaVO mdmcvo = MasterDataDelegate.getInstance().getMetaData(sEntity);

		sb.append("  Subform controller type: ").append(subform.getControllerType()).append("<br>");
		if (iColumn > -1) {
			final CollectableEntityFieldBasedTableModel tblmdl = (CollectableEntityFieldBasedTableModel) subFormTable.getModel();

			final int iModelColumn = subFormTable.convertColumnIndexToModel(iColumn);
			final String sColumnName = tblmdl.getColumnName(iModelColumn);
			sb.append("<b>Column/Field Info</b><br>");
			sb.append("  Column label: ").append(sColumnName).append("<br>");

			final CollectableEntityField clctef = tblmdl.getCollectableEntityField(iModelColumn);
			final String sFieldName = clctef.getName();
			sb.append("  Entity field name: ").append(sFieldName).append("<br>");
			sb.append("  Column class: ").append(clctef.getJavaClass().getName()).append("<br>");
			if (clctef.isReferencing()) {
				sb.append("  Referencing: ").append(clctef.getReferencedEntityName()).append(".").append(clctef.getReferencedEntityFieldName()).append("<br>");
			}

			if(iRow > -1) {
				final CollectableField cfEntry = tblmdl.getValueAsCollectableField(tblmdl.getValueAt(iRow, subFormTable.convertColumnIndexToModel(iColumn)));
				if(cfEntry != null) {
					sb.append("Contained value: ").append(cfEntry.getValue());
					if(cfEntry.isIdField()) {
						sb.append(", value id:").append(cfEntry.getValueId());
					}
					sb.append("<br>");
				}
			}

			final SubForm.Column column = subform.getColumn(sFieldName);
			if (column != null) {
				sb.append("  Enabled by initial: ").append(column.isEnabled()).append("<br>");
			}
		}
	}

	private void addTreeInfos(StringBuilder sb, JTree tree) {
		final Point p = tree.getMousePosition();
		final TreePath treepath = tree.getPathForLocation(p.x, p.y);
		if (treepath != null) {
			final TreeNode treeNode = (TreeNode) treepath.getLastPathComponent();
			sb.append("<b>TreeNode Info</b><br>");
			sb.append("  Node class: ").append(treeNode.getClass().getName()).append("<br>");
			sb.append("Child count: ").append(treeNode.getChildCount()).append("<br>");
			if(treeNode instanceof ExplorerNode<?>) {
				ExplorerNode<?> explorerNode = (ExplorerNode<?>) treeNode;
				sb.append("<b>ExplorerNode Info</b><br>");
				sb.append("  Node label: ").append(explorerNode.getLabel()).append("<br>");
				sb.append("  TreeNode class: ").append(explorerNode.getTreeNode().getClass().getName()).append("<br>");
			}

		}
	}

	@SuppressWarnings("deprecation")
	private void addFieldInfos(StringBuilder sb, Component comp) {
		CollectController<?> clctctrl = getController(comp);
		String sEntity = null;
		if(clctctrl != null) {
			sb.append("<b>CollectController Info</b><br>");
			sb.append("CollectController type: ").append(clctctrl.getClass().getName()).append("<br>");
			sEntity = clctctrl.getEntityName();
		}
		String sField = comp.getName();

		if(sEntity != null && sField != null) {
			int iDotPos = sField.indexOf(".");
			if(iDotPos != -1) {
				sField = sField.substring(0, iDotPos);
			}
			CollectableComponentModel ccm = clctctrl.getDetailsPanel().getEditModel().getCollectableComponentModelFor(sField);
			if(ccm != null) {
				CollectableField cf = ccm.getField();
				CollectableEntityField clctef = ccm.getEntityField();
				sb.append("<b>Entity field Info</b><br>");
				sb.append(" Field name: ").append(clctef.getName()).append("<br>");
				sb.append("Data type: ").append(clctef.getJavaClass().getName()).append("<br>");
				if(clctef.isReferencing()) {
					sb.append("Referenced foreign entity: ").append(clctef.getReferencedEntityName()).append("<br>");
					sb.append("Referenced field: ").append(clctef.getReferencedEntityFieldName()).append("<br>");
				}
				sb.append("Contained value: ").append(cf.getValue()).append("<br>");
				if(cf.isIdField()) {
					sb.append("Contained id: ").append(cf.getValueId()).append("<br>");
				}
			}
		}
	}

	private CollectController<?> getController(Component comp) {
		return ctrl.getControllerForInternalFrame(UIUtils.getInternalFrameForComponent(comp));
	}
}
