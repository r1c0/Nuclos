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
package org.nuclos.client.genericobject.logbook;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;

/**
 * Controller for viewing the logbook.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class LogbookController extends Controller {
	private final String PREFS_KEY_LOGBOOK = "logbook";

	private final JComponent parentMdi;
	private final int iModuleId;
	private final int iGenericObjectId;
	private MainFrameTab ifrm;

	private final LogbookPanel pnlLogbook = new LogbookPanel(null);
	private final JButton btnRefresh = new JButton();
	private final Action actRefresh = new CommonAbstractAction(CommonLocaleDelegate.getMessage("LogbookController.1", "Aktualisieren"),
			Icons.getInstance().getIconRefresh16(), CommonLocaleDelegate.getMessage("LogbookController.1", "Aktualisieren")) {
		/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdRefresh();
		}
	};
	
	private JPanel pnlFilter = new JPanel();
	
	private JTextField tfCOLUMN_CHANGEDAT = new JTextField();
	private JTextField tfCOLUMN_CHANGEDBY = new JTextField();
	private JTextField tfCOLUMN_LABEL = new JTextField();
	private JTextField tfCOLUMN_OLDVALUE = new JTextField();
	private JTextField tfCOLUMN_NEWVALUE = new JTextField();
	private JTextField tfCOLUMN_ID = new JTextField();
	
	
	private final JButton btnFilter = new JButton();
	private final JButton btnClearFilter = new JButton();
	
	private final Action actFilter = new CommonAbstractAction(CommonLocaleDelegate.getMessage("LogbookController.2", "Filter"),
			Icons.getInstance().getIconFilter16(), CommonLocaleDelegate.getMessage("LogbookController.2", "Filter")) {
		/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdFilter();
		}
	};
	
	private final Action actClearFilter = new CommonAbstractAction(CommonLocaleDelegate.getMessage("LogbookController.3", "Filter leeren"),
			Icons.getInstance().getIconClearSearch16(), CommonLocaleDelegate.getMessage("LogbookController.3", "Filter leeren")) {
		/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdClearFilter();
		}
	};
	
	private Preferences prefs;

	public LogbookController(Component parent, JComponent parentMdi, int iModuleId, int iGenericObjectId, Preferences prefs) {
		super(parent);
		this.parentMdi = parentMdi;
		this.iModuleId = iModuleId;
		this.iGenericObjectId = iGenericObjectId;
		this.prefs = prefs.node(PREFS_KEY_LOGBOOK);
	}

	public void run(String sGenericObjectIdentifier) throws CommonFinderException, CommonPermissionException {
		final Collection<LogbookVO> collLogbook = GenericObjectDelegate.getInstance().getLogbook(iGenericObjectId);

		setupToolbar(pnlLogbook);
		
		pnlLogbook.refreshTableModel(collLogbook);
		
		final String sTitle = getTitle(sGenericObjectIdentifier);

		ifrm = MainController.newMainFrameTab(null, sTitle);
		//ifrm.setContentPane(pnlLogbook);
		ifrm.setLayeredComponent(pnlLogbook);
		parentMdi.add(ifrm);
		this.setupInternalFrame();

		setupEscapeKey(ifrm, pnlLogbook);

		setupDoubleClickListener(pnlLogbook);

		this.restoreFromPreferences();

//		ifrm.pack();
		ifrm.setVisible(true);
	}

	private void setupToolbar(final LogbookPanel pnlLogbook) {
		this.btnRefresh.setName("btnRefresh");
		this.btnRefresh.setAction(actRefresh);
		this.btnRefresh.setText(null);
		pnlLogbook.getToolbar().add(this.btnRefresh);
		
		pnlFilter.setBorder(BorderFactory.createTitledBorder(CommonLocaleDelegate.getMessage("LogbookController.4", "Filter nach")));
		pnlFilter.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.right = 5;
		gbc.fill = GridBagConstraints.NONE;
		
		
		gbc.gridx = 0;
		gbc.gridy = 0;		
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.5", "Ge\u00e4ndert am")), gbc);
		
		gbc.gridx = 1;
		gbc.insets.right = 10;
		this.pnlFilter.add(this.tfCOLUMN_CHANGEDAT, gbc);
		this.tfCOLUMN_CHANGEDAT.setColumns(15);
		this.tfCOLUMN_CHANGEDAT.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" + 
			CommonLocaleDelegate.getMessage("LogbookController.5", "Ge\u00e4ndert am") + "'");
		
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.insets.right = 5;		
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.7", "Ge\u00e4ndert von")), gbc);
		
		gbc.gridx = 1;
		gbc.insets.right = 10;		
		this.pnlFilter.add(this.tfCOLUMN_CHANGEDBY, gbc);
		this.tfCOLUMN_CHANGEDBY.setColumns(15);
		this.tfCOLUMN_CHANGEDBY.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" + 
			CommonLocaleDelegate.getMessage("LogbookController.7", "Ge\u00e4ndert von")+ "'");
		
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.insets.right = 5;		
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.8", "Feld")), gbc);
		
		gbc.gridx = 1;
		gbc.insets.right = 10;		
		this.pnlFilter.add(this.tfCOLUMN_LABEL, gbc);
		this.tfCOLUMN_LABEL.setColumns(15);
		this.tfCOLUMN_LABEL.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" + 
			CommonLocaleDelegate.getMessage("LogbookController.8", "Feld")+ "'");
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets.right = 5;
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.9", "Alter Wert")), gbc);
		
		gbc.gridx = 3;		
		this.pnlFilter.add(this.tfCOLUMN_OLDVALUE, gbc);
		this.tfCOLUMN_OLDVALUE.setColumns(15);
		this.tfCOLUMN_OLDVALUE.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" +
			CommonLocaleDelegate.getMessage("LogbookController.9", "Alter Wert")+ "'");
		
		++gbc.gridy;
		gbc.gridx = 2;		
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.10", "Neuer Wert")), gbc);
		
		gbc.gridx = 3;	
		this.pnlFilter.add(this.tfCOLUMN_NEWVALUE, gbc);
		this.tfCOLUMN_NEWVALUE.setColumns(15);
		this.tfCOLUMN_NEWVALUE.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" +
			CommonLocaleDelegate.getMessage("LogbookController.10", "Neuer Wert")+ "'");		
		
		++gbc.gridy;
		gbc.gridx = 2;		
		pnlFilter.add(new JLabel(CommonLocaleDelegate.getMessage("LogbookController.11", "ID")), gbc);
		
		gbc.gridx = 3;		
		this.pnlFilter.add(this.tfCOLUMN_ID, gbc);
		this.tfCOLUMN_ID.setColumns(15);
		this.tfCOLUMN_ID.setToolTipText(CommonLocaleDelegate.getMessage("LogbookController.6", "Filter nach Spalte") + " '" +
			CommonLocaleDelegate.getMessage("LogbookController.11", "ID")+ "'");
				
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		this.btnFilter.setAction(actFilter);
		this.btnFilter.setText(null);
		this.pnlFilter.add(this.btnFilter, gbc);
		
		gbc.gridx = 4;
		gbc.gridy = 1;		
		this.btnClearFilter.setAction(actClearFilter);
		this.btnClearFilter.setText(null);
		this.pnlFilter.add(this.btnClearFilter, gbc);		
		
		pnlLogbook.getToolbar().add(this.pnlFilter);
		pnlLogbook.getToolbar().add(Box.createHorizontalGlue());
		

		/**
		 * action: Refresh
		 */
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.REFRESH, actRefresh, pnlLogbook);
	}
	
	private void cmdClearFilter() {
		tfCOLUMN_CHANGEDAT.setText("");
		tfCOLUMN_CHANGEDBY.setText("");
		tfCOLUMN_LABEL.setText("");
		tfCOLUMN_OLDVALUE.setText("");
		tfCOLUMN_NEWVALUE.setText("");
		tfCOLUMN_ID.setText("");
	}
	
	private void cmdFilter() {
		final String sFilterCHANGEDAT = tfCOLUMN_CHANGEDAT.getText();
		final String sFilterCHANGEDBY = tfCOLUMN_CHANGEDBY.getText();
		final String sFilterLABEL = tfCOLUMN_LABEL.getText();
		final String sFilterOLDVALUE = tfCOLUMN_OLDVALUE.getText();
		final String sFilterNEWVALUE = tfCOLUMN_NEWVALUE.getText();
		final String sFilterID = tfCOLUMN_ID.getText();
		
		ArrayList<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel,Integer>>();
		if (!sFilterCHANGEDAT.equals("")) {
			filters.add(newFilter(sFilterCHANGEDAT, 0));
		}
		if (!sFilterCHANGEDBY.equals("")) {
			filters.add(newFilter(sFilterCHANGEDBY, 1));
		}
		if (!sFilterLABEL.equals("")) {
			filters.add(newFilter(sFilterLABEL, 2));
		}
		if (!sFilterOLDVALUE.equals("")) {
			filters.add(newFilter(sFilterOLDVALUE, 3));
		}
		if (!sFilterNEWVALUE.equals("")) {
			filters.add(newFilter(sFilterNEWVALUE, 4));
		}
		if (!sFilterID.equals("")) {
			filters.add(newFilter(sFilterID, 5));
		}		
		
		TableRowSorter<? extends TableModel> rowSorter = (TableRowSorter<? extends TableModel>) pnlLogbook.tbl.getRowSorter();
		rowSorter.setRowFilter(!filters.isEmpty() ? RowFilter.andFilter(filters) : null);
	}
	
	private RowFilter<TableModel, Integer> newFilter(String sFilterText, int iColumn) {
		String[] split = sFilterText.split(" ");
		ArrayList<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel,Integer>>(split.length);
		for (String filterStrg : split) {
			try {
				String regex = "^" + StringUtils.wildcardToRegex(filterStrg) + "$";
				filters.add(RowFilter.<TableModel, Integer>regexFilter(regex, iColumn));
			}
			catch (java.util.regex.PatternSyntaxException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		
		return RowFilter.andFilter(filters);
	}
	
	private void restoreFromPreferences() {
		boolean bCenterWindow = false;
		try {
			if (prefs.childrenNames().length == 0) {
				// If this window is opened for the first time, center it...
				// This check has to be done before readWindowState, as this method does create a node
				bCenterWindow = true;
			}
		}
		catch (BackingStoreException ex) {
			throw new NuclosFatalException(ex);
		}
//		PreferencesUtils.readWindowState(prefs, ifrm, 900, 300);
		// There may be a conflict between saved coordinates of the logbook window and the visible part of the desktop pane.
		// So if the logbook is not completely visible, it is centered.
		if (parentMdi instanceof JDesktopPane) {
			JDesktopPane desktop = (JDesktopPane) parentMdi;
			if (!desktop.getVisibleRect().contains(ifrm.getBounds())) {
				bCenterWindow = true;
			}
		}
		if (bCenterWindow) {
			UIUtils.centerOnDesktop(ifrm, getParent(), true);
		}
	}

	private void writeToPreferences() {
//		PreferencesUtils.writeWindowState(prefs, ifrm);
	}

	private JComponent getFrame() {
		return ifrm;
	}

	private void setupDoubleClickListener(final LogbookPanel pnlLogbook) {
		pnlLogbook.tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					final int iSelectedRow = pnlLogbook.tbl.getSelectedRow();
					if (iSelectedRow >= 0) {
						final int modelRow = pnlLogbook.tbl.convertRowIndexToModel(iSelectedRow);
						final LogbookVO logbookvoSelected = pnlLogbook.getLogbookTableModel().getRow(modelRow);
						final Date dateHistoric = logbookvoSelected.getChangedAt();
						cmdShowHistoricalGenericObject(getFrame(), dateHistoric);
					}
				}
			}
		});
	}

	private void cmdRefresh() {
		try {
			pnlLogbook.refreshTableModel(GenericObjectDelegate.getInstance().getLogbook(iGenericObjectId));
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	/**
	 * @param parent
	 * @param dateHistorical
	 * @precondition dateHistorical != null
	 */
	private void cmdShowHistoricalGenericObject(final Component parent, final Date dateHistorical) {
		if (dateHistorical == null) {
			throw new NullArgumentException("dateHistorical");
		}
		UIUtils.runCommand(parent, new Runnable() {
			@Override
			public void run() {
				try {
					final GenericObjectWithDependantsVO lowdcvo = GenericObjectDelegate.getInstance().getHistorical(iGenericObjectId,
							dateHistorical);

					final CollectableGenericObjectWithDependants clct = new CollectableGenericObjectWithDependants(lowdcvo);
					NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(parentMdi, iModuleId, null).
							runViewSingleHistoricalCollectable(clct, dateHistorical);
					}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(parent, ex);
				}
			}
		});
	}

	private String getTitle(String sGenericObjectIdentifier) {
		final StringBuffer sbTitle = new StringBuffer(CommonLocaleDelegate.getMessage("LogbookController.12", "Logbuch f\u00fcr")+" ");

		if (sGenericObjectIdentifier == null) {
			sbTitle.append(CommonLocaleDelegate.getMessage("LogbookController.13", "das Objekt mit der Id") + " ");
			sbTitle.append(Integer.toString(iGenericObjectId));
		}
		else {
			sbTitle.append(Modules.getInstance().getEntityLabelByModuleId(iModuleId)).append(" ");
			sbTitle.append("\"").append(sGenericObjectIdentifier).append("\"");
		}

		return sbTitle.toString();
	}

	private static void setupEscapeKey(final MainFrameTab ifrm, LogbookPanel pnlLogbook) {
		// Escape key is to close the window:
		final Action actClose = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				ifrm.dispose();
			}
		};

		final String KEY_CLOSE = "Close";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY_CLOSE);
		ifrm.getRootPane().getActionMap().put(KEY_CLOSE, actClose);

		pnlLogbook.tbl.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY_CLOSE);
		pnlLogbook.tbl.getActionMap().put(KEY_CLOSE, actClose);
	}

	private void setupInternalFrame() {
		ifrm.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				writeToPreferences();
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});
	}

}	// class LogbookController
