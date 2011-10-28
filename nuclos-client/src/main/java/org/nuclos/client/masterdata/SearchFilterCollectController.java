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
package org.nuclos.client.masterdata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.searchfilter.SearchFilterDelegate;
import org.nuclos.client.searchfilter.SearchFilterMode;
import org.nuclos.client.searchfilter.SearchFilterResourceTableModel;
import org.nuclos.client.searchfilter.SearchFilterResultController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.result.NuclosSearchResultStrategy;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Controller for searchfilters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 01.00.00
 */
public class SearchFilterCollectController extends MasterDataCollectController {

	private Integer iSearchDeleted = null;

	public final static String FIELD_SEARCHFILTER = "clbsearchfilter";
	public final static String FIELD_LABELRES = "labelres";
	public final static String FIELD_DESCRIPTIONRES = "descriptionres";

	private SearchFilterResourceTableModel tablemodel = new SearchFilterResourceTableModel();
	private JTable table = new JTable(tablemodel);

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public SearchFilterCollectController(JComponent parent, MainFrameTab tabIfAny){
		super(parent, NuclosEntity.SEARCHFILTER, tabIfAny, 
				new SearchFilterResultController<CollectableMasterDataWithDependants>(NuclosEntity.SEARCHFILTER.getEntityName(),
						new NuclosSearchResultStrategy<CollectableMasterDataWithDependants>()));

		// a searchfilter can only be created via a searchpanel of a collectcontroller
		getNewAction().setEnabled(false);

		setSearchDeletedRendererInResultTable();
        getResultTable().getModel().addTableModelListener( new TableModelListener() {
 			@Override
            public void tableChanged(TableModelEvent e) {
 				setSearchDeletedRendererInResultTable();
			}
        });
	}

	@Override
   protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
      final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oAdditionalData;

      stopEditing();
      final Object oId = SearchFilterDelegate.getInstance().update(this.getEntityName(), clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap(), tablemodel.getRows());

      final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);

      return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
   }

	// a searchfilter can only be created via a searchpanel of a collectcontroller
	@Override
	protected boolean isCloneAllowed() {
		return false;
	}

	private void setSearchDeletedRendererInResultTable() {
		SwingUtilities.invokeLater( new Runnable() {
			@Override
            public void run() {
				TableColumn column = null;
				final int idx_active = getResultTableModel().findColumnByFieldName("searchDeleted");
				if( idx_active >= 0 ) {
					column = getResultTable().getColumnModel().getColumn(idx_active);
					column.setCellRenderer(new SearchDeletedCellRenderer());
				}
				getResultTable().validate();
			}
		});
	}

	private	class SearchDeletedCellRenderer extends DefaultTableCellRenderer {

		public SearchDeletedCellRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
            final JLabel jLabel = (JLabel)super.getTableCellRendererComponent(table, oValue, bSelected, bHasFocus, iRow, iColumn);

            if (oValue != null && oValue instanceof CollectableField) {
            	CollectableField cf = (CollectableField)oValue;
            	if (cf.getValue() != null) {
            		SearchFilterMode mode = KeyEnum.Utils.findEnum(SearchFilterMode.class, (Integer)((CollectableField)oValue).getValue());
                    jLabel.setText(CommonLocaleDelegate.getText(mode.getResourceId()));
                    jLabel.setHorizontalAlignment(SwingConstants.LEFT);
            	}
            	else {
            		jLabel.setText("");
            	}
            }
            return jLabel;
		}
	}  // inner class SearchDeletedCellRenderer

	// remember the origin value if field 'searchDeleted'
	@Override
	public CollectableMasterDataWithDependants findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		CollectableMasterDataWithDependants clctmdwd =  super.findCollectableById(sEntity, oId);

		iSearchDeleted = (Integer)clctmdwd.getField("searchDeleted").getValue();

		return clctmdwd;
	}

	// transform the numeric value of field 'searchDeleted' into a string representation
	@Override
	protected void unsafeFillDetailsPanel(CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		super.unsafeFillDetailsPanel(clct);

		if (clct != null && clct.getId() != null) {
			try {
				tablemodel.setRows(SearchFilterDelegate.getInstance().getResources(clct.getMasterDataCVO().getIntId()));
			} catch (CommonBusinessException e1) {
				throw new NuclosBusinessException(e1);
			}
		}

		JTabbedPane tabbedpane = getTabbedPane(getDetailsPanel());
		Component c = tabbedpane.getComponentAt(2);

		JTextField txtField = new JTextField();
		txtField.getDocument().addDocumentListener(new ResourceDocumentListener());

		txtField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		DefaultCellEditor editor = new DefaultCellEditor(txtField);
		editor.setClickCountToStart(1);

		for(TableColumn col : CollectionUtils.iterableEnum(table.getColumnModel().getColumns())) {
			col.setCellEditor(editor);
		}

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stopEditing();
			}
		});

		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		if (c instanceof JPanel) {
			JPanel tab = (JPanel) c;
			tab.removeAll();
			tab.setLayout(new BorderLayout());
			tab.add(new JScrollPane(table), BorderLayout.CENTER);
		}
	}

	private static JTabbedPane getTabbedPane(Component component) {
		if (component instanceof JTabbedPane) {
			return (JTabbedPane) component;
		}

		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				JTabbedPane result = getTabbedPane(c);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void stopEditing() {
		if (table.getCellEditor() != null) {
			table.getCellEditor().stopCellEditing();
		}
	}

	// transform the numeric value of field 'searchDeleted' into a string representation
	@Override
	protected void readValuesFromEditPanel(CollectableMasterDataWithDependants clct, boolean bSearchTab) throws CollectableValidationException {
		final Collection<CollectableComponent> collclctcomp = this.getDetailsPanel().getLayoutRoot().getCollectableComponentsFor("searchDeleted");

		Iterator<CollectableComponent> iClctComp = collclctcomp.iterator();
		if (iClctComp.hasNext()) {
			JComponent comp = iClctComp.next().getJComponent();

			if (comp instanceof LabeledTextField) {
				if (iSearchDeleted == null) {
					((LabeledTextField)comp).getTextField().setText(null);
				}
				else {
					((LabeledTextField)comp).getTextField().setText(""+iSearchDeleted);
				}
			}
		}

		super.readValuesFromEditPanel(clct, bSearchTab);
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		String name = (String) clct.getMasterDataWithDependantsCVO().getField("name");
		mddelegate.remove(getEntityName(), clct.getMasterDataCVO());
		if(!StringUtils.isNullOrEmpty(name))
			SearchFilterCache.getInstance().removeFilter(name, null);
		fireApplicationObserverEvent();
	}

	public void enterEditMode() {
		if (getCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW) {
			try {
				this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_EDIT);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(this.getFrame(), ex);
			}
		}
	}

	private class ResourceDocumentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			enterEditMode();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			enterEditMode();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			enterEditMode();
		}
	}
}
