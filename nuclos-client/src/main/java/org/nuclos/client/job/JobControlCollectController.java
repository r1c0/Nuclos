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
package org.nuclos.client.job;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.SubFormController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.job.IntervalUnit;
import org.nuclos.common.job.JobType;
import org.nuclos.common.job.JobUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Controller for nucleus quartz jobs.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
@Configurable(preConstruction=true)
public class JobControlCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(JobControlCollectController.class);
	
	private static final Object JOB_RESULT_SUCCESSFUL = "INFO";
	private static final Object JOB_RESULT_WITH_ERROR = "ERROR";
	private static final Object JOB_RESULT_WITH_WARNINGS = "WARNING";
	
	// Spring injection
	
	private SchedulerControlFacadeRemote schedulerControlFacadeRemote;
	
	// end of Spring injection

	private final JobControlDelegate delegate = JobControlDelegate.getInstance();

	private final Action actSchedule = new CommonAbstractAction(Icons.getInstance().getIconPlay16(), getSpringLocaleDelegate().getMessage(
			"JobControlCollectController.1","Aktivieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			try {
				cmdScheduleJob();
			}
			catch (CommonBusinessException e) {
				JOptionPane.showMessageDialog(JobControlCollectController.this.getDetailsPanel(), getSpringLocaleDelegate().getMessageFromResource(
						e.getMessage()), "", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	private final Action actUnschedule = new CommonAbstractAction(Icons.getInstance().getIconStop16(), getSpringLocaleDelegate().getMessage(
			"JobControlCollectController.3","Deaktivieren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			try {
				cmdUnscheduleJob();
			}
			catch (CommonBusinessException e) {
				JOptionPane.showMessageDialog(JobControlCollectController.this.getDetailsPanel(), getSpringLocaleDelegate().getMessageFromResource(
						e.getMessage()), "", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	private final Action actStartImmediately = new CommonAbstractAction(Icons.getInstance().getIconNext16(), getSpringLocaleDelegate().getMessage(
			"JobControlCollectController.2","Ausf\u00fchren")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdStartJobImmediately();
		}
	};

	CollectableComponentModelListener ccml_jobtype = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			String sType = (String)ev.getCollectableComponentModel().getField().getValue();
			sType = (String)ev.getNewValue().getValue();
			enableParameterSubForm(sType);
		}
	};

	CollectableComponentModelListener ccml_cronexpression = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			CollectableComponentModel usecron = getDetailsEditView().getModel().getCollectableComponentModelFor("usecronexpression");
			if (usecron.getField().isNull() || !(Boolean)usecron.getField().getValue()) {
				Date startdate;
				String starttime;
				Integer interval;
				IntervalUnit unit;

				if (ev.getCollectableComponentModel().getEntityField().getName().equals("startdate")) {
					startdate = (Date) ev.getNewValue().getValue();
				}
				else {
					startdate = (Date) getDetailsEditView().getModel().getCollectableComponentModelFor("startdate").getField().getValue();
				}

				if (ev.getCollectableComponentModel().getEntityField().getName().equals("starttime")) {
					starttime = (String) ev.getNewValue().getValue();
				}
				else {
					starttime = (String) getDetailsEditView().getModel().getCollectableComponentModelFor("starttime").getField().getValue();
				}

				if (ev.getCollectableComponentModel().getEntityField().getName().equals("interval")) {
					interval = (Integer) ev.getNewValue().getValue();
				}
				else {
					interval = (Integer) getDetailsEditView().getModel().getCollectableComponentModelFor("interval").getField().getValue();
				}

				if (ev.getCollectableComponentModel().getEntityField().getName().equals("unit")) {
					unit = org.nuclos.common2.KeyEnum.Utils.findEnum(IntervalUnit.class, (String) ev.getNewValue().getValue());
				}
				else {
					unit = org.nuclos.common2.KeyEnum.Utils.findEnum(IntervalUnit.class,
						(String) getDetailsEditView().getModel().getCollectableComponentModelFor("unit").getField().getValue());
				}

				if (startdate != null && starttime != null && interval != null && unit != null) {
	                try {
	                	Calendar c = Calendar.getInstance(getSpringLocaleDelegate().getUserLocaleInfo().toLocale());
	    				c.setTime(startdate);
	    				int iHour = Integer.parseInt(starttime.split(":")[0]);
	    				int iMinute = Integer.parseInt(starttime.split(":")[1]);
	    				c.set(Calendar.HOUR_OF_DAY, iHour);
	    				c.set(Calendar.MINUTE, iMinute);
	    				String cronexpression = JobUtils.getCronExpressionFromInterval(unit, interval, c);
		                getDetailsEditView().getModel().getCollectableComponentModelFor("cronexpression").setField(new CollectableValueField(cronexpression));
	                }
	                catch(Exception e) {
						LOG.warn("collectableFieldChangedInModel failed: " + e, e);
	                }
				}
			}
		}
	};

	CollectableComponentModelListener ccml_usecronexpression = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			boolean cronexpression = ev.getNewValue().isNull() ? false : (Boolean)ev.getNewValue().getValue();
			setInputFieldsEnabled(cronexpression);
		}
	};

	private final ActionListener rbAcionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean cronexpression = false;
			if ("cronexpression".equals(e.getActionCommand())) {
				cronexpression = true;
			}
			else {
				cronexpression = false;
			}

			getDetailsComponentModel("usecronexpression").setField(new CollectableValueField(Boolean.valueOf(cronexpression)));
		}
	};

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public JobControlCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.JOBCONTROLLER, tabIfAny, null);

		this.setupDetailsToolBar();

		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				int iDetailsMode = ev.getNewCollectState().getInnerState();
				if (iDetailsMode == CollectState.DETAILSMODE_NEW || iDetailsMode == CollectState.DETAILSMODE_NEW_CHANGED ||
						iDetailsMode == CollectState.DETAILSMODE_EDIT || iDetailsMode == CollectState.DETAILSMODE_MULTIEDIT) {
					actSchedule.setEnabled(false);
					actUnschedule.setEnabled(false);
					actStartImmediately.setEnabled(false);
				}
				else {
					actSchedule.setEnabled(true);
					actUnschedule.setEnabled(true);
					actStartImmediately.setEnabled(true);
				}

				setResultLastRunInDetails();
				setupRadioButtons();

				for (CollectableComponent clctcomp : getDetailCollectableComponentsFor("type")) {
					enableParameterSubForm((String)clctcomp.getField().getValue());
				}


				if (getDetailsEditView().getModel().getCollectableComponentModelFor("type") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("type").addCollectableComponentModelListener(ccml_jobtype);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("startdate") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("startdate").addCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("starttime") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("starttime").addCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("interval") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("interval").addCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("unit") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("unit").addCollectableComponentModelListener(ccml_cronexpression);
				}

				CollectableComponentModel model = getDetailsEditView().getModel().getCollectableComponentModelFor("usecronexpression");
				if (model != null) {
					model.addCollectableComponentModelListener(ccml_usecronexpression);
				}

				boolean cronexpression = model.getField().isNull() ? false : (Boolean)model.getField().getValue();
				setInputFieldsEnabled(cronexpression);
			}

			@Override
			public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("type") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("type").removeCollectableComponentModelListener(ccml_jobtype);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("startdate") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("startdate").removeCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("starttime") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("starttime").removeCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("interval") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("interval").removeCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("unit") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("unit").removeCollectableComponentModelListener(ccml_cronexpression);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("usecronexpression") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("usecronexpression").removeCollectableComponentModelListener(ccml_usecronexpression);
				}
			}
		});

		setResultLastRunRendererInResultTable();
        getResultTable().getModel().addTableModelListener( new TableModelListener() {
 			@Override
			public void tableChanged(TableModelEvent e) {
				setResultLastRunRendererInResultTable();
			}
        });
	}
	
	@Autowired
	final void setSchedulerControlFacadeRemote(SchedulerControlFacadeRemote schedulerControlFacadeRemote) {
		this.schedulerControlFacadeRemote = schedulerControlFacadeRemote;
	}

	protected void setInputFieldsEnabled(boolean useCronExpression) {
		for (CollectableComponent c : getDetailCollectableComponentsFor("interval")) {
			c.setEnabled(!useCronExpression);
		}
		for (CollectableComponent c : getDetailCollectableComponentsFor("unit")) {
			c.setEnabled(!useCronExpression);
		}
		for (CollectableComponent c : getDetailCollectableComponentsFor("cronexpression")) {
			c.setEnabled(useCronExpression);
		}
	}

	protected void setupRadioButtons() {
		ButtonGroup group = new ButtonGroup();
		for (CollectableComponent label : getDetailsEditView().getCollectableLabels()) {
			JRadioButton radiobutton = null;
			if (label.getFieldName().equals("interval")) {
				radiobutton = new JRadioButton(label.getEntityField().getLabel());
				radiobutton.setActionCommand(label.getFieldName());
				if (getDetailsComponentModel("usecronexpression").getField().getValue() == null
					|| !(Boolean)getDetailsComponentModel("usecronexpression").getField().getValue() ) {
					radiobutton.setSelected(true);
				}
			}

			if (label.getFieldName().equals("cronexpression")) {
				radiobutton = new JRadioButton(label.getEntityField().getLabel());
				radiobutton.setActionCommand(label.getFieldName());
				if (getDetailsComponentModel("usecronexpression").getField().getValue() != null
					&& (Boolean)getDetailsComponentModel("usecronexpression").getField().getValue() ) {
					radiobutton.setSelected(true);
				}
			}

			if (radiobutton != null) {
				radiobutton.setOpaque(false);
				radiobutton.addActionListener(rbAcionListener);
				group.add(radiobutton);

				Component placeHolder = label.getJComponent();
				if (placeHolder.getParent() != null) {
					Container container = placeHolder.getParent();
					TableLayout layoutManager = (TableLayout)container.getLayout();
					TableLayoutConstraints constraints = layoutManager.getConstraints(placeHolder);

					container.remove(placeHolder);
					container.add(radiobutton, constraints);
				}
			}
		}
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

		final MasterDataVO mdvoInserted = delegate.create(new JobVO(clctNew.getMasterDataCVO(), mpmdvoDependants));

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap)oAdditionalData;

		final Object oId = this.delegate.modify(new JobVO(clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap()));

		final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);
		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		this.delegate.remove(new JobVO(clct.getMasterDataCVO()));
	}

	private void checkNameWithAlreadyScheduledJobs(CollectableMasterDataWithDependants clct) throws CommonValidationException {
		if (schedulerControlFacadeRemote.isScheduled((String)clct.getValue("name"))) {
			throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage(
					"jobcontroller.error.validation.name", clct.getValue("name")));
		}
	}

	private void setupDetailsToolBar() {
		// additional functionality in Details panel:
		//final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		final JButton btnStart = new JButton(this.actSchedule);
		btnStart.setName("btnStart");
		btnStart.setText(getSpringLocaleDelegate().getMessage("JobControlCollectController.1", "Aktivieren"));
		//toolbar.add(btnStart);
		this.getDetailsPanel().addToolBarComponent(btnStart);

		final JButton btnStop = new JButton(this.actUnschedule);
		btnStop.setName("btnStop");
		btnStop.setText(getSpringLocaleDelegate().getMessage("JobControlCollectController.3", "Deaktivieren"));
		//toolbar.add(btnStop);
		this.getDetailsPanel().addToolBarComponent(btnStop);

		final JButton btnStartNow = new JButton(this.actStartImmediately);
		btnStartNow.setName("btnExecuteNow");
		btnStartNow.setText(getSpringLocaleDelegate().getMessage("JobControlCollectController.2", "Ausf\u00fchren"));
		//toolbar.add(btnStartNow);
		this.getDetailsPanel().addToolBarComponent(btnStartNow);

		//this.getDetailsPanel().setCustomToolBarArea(toolbar);
	}

	private void enableParameterSubForm(String sType) {
		if (sType != null) {
			JobType type = org.nuclos.common2.KeyEnum.Utils.findEnum(JobType.class, sType);
			if (JobType.TIMELEIMIT.equals(type)) {
				this.setSubFormsVisibility(true, false);
			}
			else if (JobType.HEALTHCHECK.equals(type)) {
				this.setSubFormsVisibility(false, true);
			}
		}
		else {
			this.setSubFormsVisibility(true, true);
		}
	}

	private void setSubFormsVisibility(boolean bJobRuleSubForm, boolean bJobDBObjectSubForm) {
		if (getSubForm(NuclosEntity.JOBRULE) != null) {
			getSubForm(NuclosEntity.JOBRULE).setVisible(bJobRuleSubForm);
		}
		if (getSubForm(NuclosEntity.JOBDBOBJECT) != null) {
			getSubForm(NuclosEntity.JOBDBOBJECT).setVisible(bJobDBObjectSubForm);
		}
	}

	private SubForm getSubForm(NuclosEntity entity) {
		for (SubFormController subformctl : getSubFormControllersInDetails()) {
			if(entity.checkEntityName(subformctl.getCollectableEntity().getName()))
				return subformctl.getSubForm();
		}

		return null;
	}

	private void cmdScheduleJob() throws CommonBusinessException{
		if (CollectState.isDetailsModeChangesPending(getCollectStateModel().getDetailsMode())) {
			throw new CommonBusinessException(getSpringLocaleDelegate().getMessage(
					"JobControlCollectController.4", "Bitte zuerst speichern"));
		}
		else {
			checkNameWithAlreadyScheduledJobs(this.getSelectedCollectable());
			delegate.scheduleJob(this.getSelectedCollectable().getMasterDataCVO().getId());
			UIUtils.runCommand(this.getTab(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					refreshCurrentCollectable();
				}
			});
		}
	}

	private void cmdUnscheduleJob() throws CommonBusinessException {
		delegate.unscheduleJob(this.getSelectedCollectable().getMasterDataCVO().getId());
		UIUtils.runCommand(this.getTab(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				refreshCurrentCollectable();
			}
		});
	}

	private void cmdStartJobImmediately() {
		CommonMultiThreader.getInstance().execute(new CommonClientWorker() {

			@Override
			public void work() throws CommonBusinessException {
				delegate.startJobImmediately(getSelectedCollectable().getMasterDataCVO().getId());
			}

			@Override
			public void paint() throws CommonBusinessException {
				UIUtils.runCommand(getTab(), new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						try {
							Thread.sleep(3 * 1000); //wait for quartz to start job
						} catch (InterruptedException e) { }
						refreshCurrentCollectable();
					}
				});
			}

			@Override
			public void init() throws CommonBusinessException {
			}

			@Override
			public void handleError(Exception ex) {
				JOptionPane.showMessageDialog(JobControlCollectController.this.getDetailsPanel(), getSpringLocaleDelegate().getMessageFromResource(
						ex.getMessage()), "", JOptionPane.INFORMATION_MESSAGE);
			}

			@Override
			public JComponent getResultsComponent() {
				return getTab();
			}
		});
	}

	/**
	 *
	 */
	public void setResultLastRunInDetails() {
		final String resultLastRun = this.getSelectedCollectable() != null ?
				(this.getSelectedCollectable().getValue("result") != null ? (String)this.getSelectedCollectable().getValue("result") : null) : null;
		for (CollectableComponent clct : this.getDetailCollectableComponentsFor("result")) {
			if (clct instanceof CollectableTextField) {
				((CollectableTextField)clct).getJTextField().setVisible(false);
				((CollectableTextField)clct).getJLabel().setText("");
				((CollectableTextField)clct).getJLabel().setIcon(getIconForJobResult(resultLastRun));
				clct.setToolTipText(resultLastRun);
			}
		}
	}

	private void setResultLastRunRendererInResultTable() {
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				setRendereInResultTable("result", createResultLastRunRenderer());
			}
		});
	}

	private void setRendereInResultTable( String columnName, TableCellRenderer tableRenderer) {
		TableColumn column = null;
		final int idx_active = getResultTableModel().findColumnByFieldName(columnName);
		if( idx_active >= 0 ) {
			column = getResultTable().getColumnModel().getColumn(idx_active);
			column.setCellRenderer(tableRenderer);
		}
	}

	private Icon getIconForJobResult(String resultLastRun ) {
		Icon resultIcon = null;
		if( resultLastRun == null) {
			resultIcon = Icons.getInstance().getIconJobUnknown();
		} else if ( resultLastRun.equals(JOB_RESULT_SUCCESSFUL)) {
			resultIcon = Icons.getInstance().getIconJobSuccessful();
		} else if ( resultLastRun.equals(JOB_RESULT_WITH_ERROR)) {
			resultIcon = Icons.getInstance().getIconJobError();
		} else if ( resultLastRun.equals(JOB_RESULT_WITH_WARNINGS)) {
			resultIcon = Icons.getInstance().getIconJobWarning();
		}
		return resultIcon;
	}

	private TableCellRenderer createResultLastRunRenderer() {

		class TrafficLightCellRenderer extends DefaultTableCellRenderer {

			public TrafficLightCellRenderer() {
				super();
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object oValue, boolean bSelected, boolean bHasFocus,
					int iRow, int iColumn) {
                final JLabel jLabel = (JLabel)super.getTableCellRendererComponent(table,
                        oValue, bSelected, bHasFocus, iRow, iColumn);

                jLabel.setText("");
                jLabel.setHorizontalAlignment(SwingConstants.CENTER);

                final String sValue = (String)((CollectableField)oValue).getValue();
				jLabel.setIcon(getIconForJobResult(sValue));
                return jLabel;
			}
		}  // inner class CollectableBooleanTableCellRenderer

		return new TrafficLightCellRenderer();
	}

}
