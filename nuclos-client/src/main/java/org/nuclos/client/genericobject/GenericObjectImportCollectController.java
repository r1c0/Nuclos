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
package org.nuclos.client.genericobject;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.fileimport.ImportDelegate;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ProgressNotification;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common.fileimport.ImportResult;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Special masterdata collect controller for generic object file import.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Configurable(preConstruction=true)
public class GenericObjectImportCollectController extends MasterDataCollectController implements MessageListener {

	private static final Logger LOG = Logger.getLogger(GenericObjectImportCollectController.class);

	public static final String COMPONENTNAME_PROGRESSPANEL = "progressPanel";

	private ImportDelegate delegate = ImportDelegate.getInstance();

	private final Action actImport = new CommonAbstractAction(Icons.getInstance().getIconPlay16(), getSpringLocaleDelegate().getMessage(
			"GenericObjectImportCollectController.import", "Importieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdImport();
		}
	};

	private final Action actStop = new CommonAbstractAction(Icons.getInstance().getIconStop16(), getSpringLocaleDelegate().getMessage(
			"GenericObjectImportCollectController.stopimport", "Import abbrechen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdCancelImport();
		}
	};

	private JButton btnStart;
	private JButton btnStop;

	private JProgressBar progressBar;

	private ProgressNotification lastnotification;
	
	private TopicNotificationReceiver tnr;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GenericObjectImportCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.IMPORTFILE.getEntityName(), tabIfAny);
	}
	
	@Override
	public void init() {
		super.init();
		getCollectStateModel().addCollectStateListener(new CollectStateAdapter(){
			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				progressBar = new JProgressBar();
				progressBar.setStringPainted(true);

				Component placeHolder = getPlaceHolder(getDetailsPanel(), "lblPlaceholder1");
				// You only can replace the place holder once. (tp)
				if (placeHolder != null) {
					Container container = placeHolder.getParent();
					TableLayout layoutManager = (TableLayout)container.getLayout();
					TableLayoutConstraints constraints = layoutManager.getConstraints(placeHolder);

					container.remove(placeHolder);
					container.add(progressBar, constraints);
				}
				
				
	            Integer importfileId = (Integer) getSelectedCollectableId();

	            if (!SecurityCache.getInstance().isSuperUser()) {
		            for (CollectableComponent c : getDetailCollectableComponentsFor("mode")) {
		            	if (c instanceof CollectableComboBox) {
		            		c.setEnabled(false);
		            	}
		            }
	            }
	            resetProgressBars();

	            if (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW) {
	            	try {
		            	String correlationId = ImportDelegate.getInstance().getImportCorrelationId(importfileId);
		            	if (!StringUtils.isNullOrEmpty(correlationId)) {
		            		GenericObjectImportCollectController.this.progressBar.setString("Warte auf Status");
		            		setupDetailsToolBar(true, false);
		            		tnr.subscribe(JMSConstants.TOPICNAME_PROGRESSNOTIFICATION, correlationId, GenericObjectImportCollectController.this);
		            	}
		            	else {
		            		setupDetailsToolBar(false, false);
		            	}
		            }
		            catch(NuclosFileImportException e) {
			            throw new NuclosFatalException(e.getMessage());
		            }
		        }
		        else {
		        	setupDetailsToolBar(false, true);
		        }

	            setResultLastRunInDetails();
            }

			@Override
            public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
				tnr.unsubscribe(GenericObjectImportCollectController.this);
            }
		});

		setCellRendererInResultTable();
        getResultTable().getModel().addTableModelListener( new TableModelListener() {
 			@Override
			public void tableChanged(TableModelEvent e) {
 				setCellRendererInResultTable();
			}
        });
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}

	@Override
    protected CollectableMasterDataWithDependants newCollectableWithDefaultValues() {
		final CollectableMasterDataWithDependants result = super.newCollectableWithDefaultValues();
		result.setField("mode", new CollectableValueField(ImportMode.NUCLOSIMPORT.getValue()));
		return result;
    }

	@Override
	public CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if(clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		// We have to clear the ids for cloned objects:
		/**
		 * @todo eliminate this workaround - this is the wrong place. The right
		 *       place is the Clone action!
		 */
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		final MasterDataVO mdvoInserted = delegate.createFileImport(new MasterDataWithDependantsVO(clctNew.getMasterDataCVO(), mpmdvoDependants));

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap)oAdditionalData;

		final Object oId = this.delegate.modifyFileImport(new MasterDataWithDependantsVO(clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap()));

		final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);
		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		this.delegate.removeFileImport(clct.getMasterDataCVO());
	}

	private void resetProgressBars() {
		try {
			if (lastnotification != null) {
				GenericObjectImportCollectController.this.progressBar.setString(getSpringLocaleDelegate().getMessageFromResource(
						lastnotification.getMessage()));
	    		GenericObjectImportCollectController.this.progressBar.setMinimum(lastnotification.getProgressMinimum());
	    		GenericObjectImportCollectController.this.progressBar.setMaximum(lastnotification.getProgressMaximum());
	    		GenericObjectImportCollectController.this.progressBar.setValue(lastnotification.getValue());
			}
			else {
				GenericObjectImportCollectController.this.progressBar.setString(getSpringLocaleDelegate().getMessageFromResource(
						"GenericObjectImportCollectController.18"));
				GenericObjectImportCollectController.this.progressBar.setMinimum(0);
				GenericObjectImportCollectController.this.progressBar.setMaximum(0);
				GenericObjectImportCollectController.this.progressBar.setValue(0);
			}
		}
		finally {
			lastnotification = null;
		}

	}

	private static Component getPlaceHolder(Component component, String name) {
		if (name.equals(component.getName())) {
			return component;
		}

		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				Component result = getPlaceHolder(c, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private void setupDetailsToolBar(boolean running, boolean editmode) {
		//final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		if (btnStart != null) {
			this.getDetailsPanel().removeToolBarComponent(btnStart);
		}
		btnStart = new JButton(this.actImport);
		btnStart.setName("btnImport");
		btnStart.setText(getSpringLocaleDelegate().getMessage("GenericObjectImportCollectController.import", "Importieren"));
		btnStart.setEnabled(!running && !editmode);
		//toolbar.add(btnStart);
		this.getDetailsPanel().addToolBarComponent(btnStart);

		if (btnStop != null) {
			this.getDetailsPanel().removeToolBarComponent(btnStop);
		}
		btnStop = new JButton(this.actStop);
		btnStop.setName("btnStop");
		btnStop.setText(getSpringLocaleDelegate().getMessage("GenericObjectImportCollectController.stopimport", "Import abbrechen"));
		btnStop.setEnabled(running && !editmode);
		//toolbar.add(btnStop);
		this.getDetailsPanel().addToolBarComponent(btnStop);

		//this.getDetailsPanel().setCustomToolBarArea(toolbar);
	}

	public void cmdImport() {
		Integer importfileId = (Integer)getSelectedCollectableId();
		try {
	        ImportDelegate.getInstance().doImport(importfileId);
	        refreshCurrentCollectable();
        }
        catch (CommonBusinessException e) {
        	JOptionPane.showMessageDialog(GenericObjectImportCollectController.this.getDetailsPanel(), 
        			getSpringLocaleDelegate().getMessageFromResource(e.getMessage()), "", JOptionPane.ERROR_MESSAGE);
        }
	}

	public void cmdCancelImport() {
		Integer importfileId = (Integer)getSelectedCollectableId();
		try {
			ImportDelegate.getInstance().stopImport(importfileId);
        }
        catch(NuclosFileImportException e) {
			LOG.error("cmdCancelImport failed: " + e, e);
        	JOptionPane.showMessageDialog(GenericObjectImportCollectController.this.getDetailsPanel(), 
        			getSpringLocaleDelegate().getMessageFromResource(e.getMessage()), "", JOptionPane.ERROR_MESSAGE);
        }
	}

	public void setResultLastRunInDetails() {
		final String resultLastRun = this.getSelectedCollectable() != null ? (this.getSelectedCollectable().getValue("laststate") != null ? (String)this.getSelectedCollectable().getValue("laststate") : null) : null;
		for (CollectableComponent clct : this.getDetailCollectableComponentsFor("laststate")) {
			if (clct instanceof CollectableTextField) {
				((CollectableTextField)clct).getJTextField().setVisible(false);
				((CollectableTextField)clct).getJLabel().setVisible(true);
				((CollectableTextField)clct).getJLabel().setText("");
				((CollectableTextField)clct).getJLabel().setIcon(getIconForImportResult(resultLastRun));
				clct.setToolTipText(resultLastRun);
			}
		}
	}

	private void setCellRendererInResultTable() {
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				TableColumn column = null;
				final int idx_active = getResultTableModel().findColumnByFieldName("laststate");
				if(idx_active >= 0 ) {
					column = getResultTable().getColumnModel().getColumn(idx_active);
					column.setCellRenderer(new TrafficLightCellRenderer());
				}
			}
		});
	}

	private Icon getIconForImportResult(String resultLastRun) {
		ImportResult ir = KeyEnum.Utils.findEnum(ImportResult.class, resultLastRun);
		if (ImportResult.OK.equals(ir)) {
			return Icons.getInstance().getIconJobSuccessful();
		}
		else if (ImportResult.ERROR.equals(ir)) {
			return Icons.getInstance().getIconJobError();
		}
		else if (ImportResult.INCOMPLETE.equals(ir)) {
			return Icons.getInstance().getIconJobWarning();
		}
		else {
			return Icons.getInstance().getIconJobUnknown();
		}
	}

	@Override
    public void onMessage(Message message) {
		try {
		    if (message instanceof ObjectMessage) {
		    	ObjectMessage objectMessage = (ObjectMessage) message;
		    	if (objectMessage.getObject() instanceof ProgressNotification) {
		    		ProgressNotification notification = (ProgressNotification) objectMessage.getObject();

		    		if (notification.getState() == ProgressNotification.RUNNING) {
		    			LOG.info("onMessage " + this + " progressing...");

		    			this.btnStart.setEnabled(false);
		    			this.btnStop.setEnabled(true);

		    			GenericObjectImportCollectController.this.progressBar.setString(getSpringLocaleDelegate().getMessageFromResource(
		    					notification.getMessage()));
	            		GenericObjectImportCollectController.this.progressBar.setMinimum(notification.getProgressMinimum());
	            		GenericObjectImportCollectController.this.progressBar.setMaximum(notification.getProgressMaximum());
	            		GenericObjectImportCollectController.this.progressBar.setValue(notification.getValue());
		    		}
		    		else {
		    			try {
		    				LOG.info("onMessage " + this + " refreshCurrentCollectable...");
		    				this.lastnotification = notification;
							refreshCurrentCollectable(false);
						}
						catch(CommonBusinessException e) {
							LOG.error("onMessage failed: " + e, e);
						}
		    		}
		    	}
		    }
		}
		catch (JMSException ex) {
			LOG.error(ex);
		}
    }

	private class TrafficLightCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object oValue, boolean bSelected, boolean bHasFocus,int iRow, int iColumn) {
            final JLabel jLabel = (JLabel)super.getTableCellRendererComponent(table, oValue, bSelected, bHasFocus, iRow, iColumn);

            jLabel.setText("");
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);

            final String sValue = (String)((CollectableField)oValue).getValue();
			jLabel.setIcon(getIconForImportResult(sValue));
            return jLabel;
		}
	}
}	// class GenericObjectImportCollectController
