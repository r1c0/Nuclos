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
package org.nuclos.client.report.admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.model.CollectableTableModel;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.SystemUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.OutputType;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;

/**
 * <code>MasterDataCollectController</code> for reports.
 * todo: entity 'template' should get an own collect controller, as it is more or less a dummy entity (no fields, just one record)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ReportCollectController extends MasterDataCollectController {

	//private final JButton btnShowResultInExplorer = new JButton();
	private final JButton btnPreview = new JButton();
	private final JButton btnMakeTreeRoot = new JButton();
	private final JButton btnExportFile = new JButton();

	private final NuclosEntity outputEntity;
	private final NuclosEntity subreportEntity;
	private final ReportDelegate reportdelegate = new ReportDelegate();

	private ReportVO.OutputType outputtype = ReportVO.OutputType.SINGLE;
	// Controls which are shown or hidden, depending on the output type of the report
	private CollectableComboBox clctcmbbxDataSource = null;
	private JPanel pnlFileChoosers = null;

	// Backups from table columns, which are shown or hidden, depending on the output type of the report
	private TableColumn tablecolumnDataSource = null;
	private TableColumn tablecolumnSourceFile = null;
	private TableColumn tablecolumnParameter = null;
	private TableColumn tablecolumnFormat = null;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ReportCollectController(NuclosEntity entity, MainFrameTab tabIfAny) {
		super(entity, tabIfAny);

		if (entity.equals(NuclosEntity.REPORT)) {
			outputEntity = NuclosEntity.REPORTOUTPUT;
			subreportEntity = NuclosEntity.SUBREPORT;
		}
		else if (entity.equals(NuclosEntity.FORM)) {
			outputEntity = NuclosEntity.FORMOUTPUT;
			subreportEntity = NuclosEntity.SUBFORM;
		}
		else
			throw new IllegalArgumentException();
		
		setupDetailsToolBar();

		clctcmbbxDataSource = (CollectableComboBox) getFirstComponent("datasource");
		// todo: search panel?!
		pnlFileChoosers = (JPanel) UIUtils.findJComponent(getDetailsPanel(), "filechoosers");

		/*final CollectableComboBox cmbbxReportType = (CollectableComboBox) getFirstComponent("type");
		cmbbxReportType.getModel().addCollectableComponentModelListener(new CollectableComponentModelAdapter() {
			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				final CollectableField clctf = ev.getNewValue();
				ReportType reportType = KeyEnum.Utils.findEnum(ReportType.class, (Integer) clctf.getValue());
				setReportType(reportType != null ? reportType : (getEntity().equals(NuclosEntity.REPORT.getEntityName()) ? ReportType.REPORT : ReportType.FORM));
			}
		});*/
		
		setReportType(getEntity().equals(NuclosEntity.REPORT.getEntityName()) ? ReportType.REPORT : ReportType.FORM);
	
		final CollectableComboBox cmbbxOutputType = (CollectableComboBox) getFirstComponent("outputtype");
		cmbbxOutputType.getModel().addCollectableComponentModelListener(new CollectableComponentModelAdapter() {
			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				final CollectableField clctf = ev.getNewValue();
				OutputType outputType = KeyEnum.Utils.findEnum(OutputType.class, (String) clctf.getValue());
				setOutputType(outputType != null ? outputType : OutputType.SINGLE);
			}
		});

		// getFirstComponent(sFieldName)

		getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) {
				final boolean bViewingExistingRecord = (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW);
				btnMakeTreeRoot.setEnabled(bViewingExistingRecord);
				btnPreview.setEnabled(bViewingExistingRecord);
			}
		});
	}

	private ReportVO.OutputType getOutputType() {
		return outputtype;
	}

	/**
	 * Adapts the (details) layout to one of the select report type.
	 */
	private void setReportType(ReportType reportType) {
		boolean formUsageEnabled = reportType == ReportType.FORM;
		DetailsSubFormController<CollectableEntityObject> formUsageController = getSubFormController(NuclosEntity.FORMUSAGE.getEntityName());
		if (formUsageController != null && formUsageController.getSubForm() != null) {
			SubForm subForm = formUsageController.getSubForm();
			if (subForm.getParent().getParent().getParent() instanceof JTabbedPane) {				
				JTabbedPane tabbedPane = (JTabbedPane) subForm.getParent().getParent().getParent();
				for (int tabIndex = 0; tabIndex < tabbedPane.getTabCount(); tabIndex++) {					
					JComponent comp = UIUtils.findFirstJComponent((JComponent) tabbedPane.getComponentAt(tabIndex), subForm.getClass() );
					if (comp == subForm) {
						tabbedPane.setEnabledAt(tabIndex, formUsageEnabled);
						if (tabbedPane.getSelectedIndex() == tabIndex && !formUsageEnabled)
							tabbedPane.setSelectedIndex(-1);
						break;
					}
				}
			}
		}
	}
	
	
	/**
	 * sets the (details) layout to one of the possible report output states.
	 * @param iOutputType
	 */
	private void setOutputType(OutputType outputType) {
		outputtype = outputType;

		// todo: Is there a better way to connect the columns to the fieldnames?
		final JTable tbl = getSubFormController(outputEntity.getEntityName()).getSubForm().getJTable();
		final TableColumnModel columnmodel = tbl.getColumnModel();
		switch (outputtype) {
		case SINGLE:
			if (tablecolumnDataSource == null) {
				tablecolumnDataSource = tbl.getColumn("datasource");
				columnmodel.removeColumn(tablecolumnDataSource);
			}
			if (tablecolumnParameter != null) {
				columnmodel.addColumn(tablecolumnParameter);
				tablecolumnParameter = null;
			}
			if (tablecolumnSourceFile != null) {
				columnmodel.addColumn(tablecolumnSourceFile);
				tablecolumnSourceFile = null;
			}
			if (tablecolumnFormat != null) {
				columnmodel.addColumn(tablecolumnFormat);
				tablecolumnFormat = null;
			}

			clctcmbbxDataSource.setVisible(true);
			pnlFileChoosers.setVisible(false);
			break;

		case COLLECTIVE:
			if (tablecolumnDataSource != null) {
				columnmodel.addColumn(tablecolumnDataSource);
				tablecolumnDataSource = null;
			}
			if (tablecolumnParameter != null) {
				columnmodel.addColumn(tablecolumnParameter);
				tablecolumnParameter = null;
			}
			if (tablecolumnSourceFile != null) {
				columnmodel.addColumn(tablecolumnSourceFile);
				tablecolumnSourceFile = null;
			}
			if (tablecolumnFormat != null) {
				columnmodel.addColumn(tablecolumnFormat);
				tablecolumnFormat = null;
			}

			clctcmbbxDataSource.setVisible(false);
			pnlFileChoosers.setVisible(false);
			break;

		case EXCEL:
			if (tablecolumnDataSource != null) {
				columnmodel.addColumn(tablecolumnDataSource);
				tablecolumnDataSource = null;
			}
			if (tablecolumnParameter == null) {
				tablecolumnParameter = tbl.getColumn("parameter");
				columnmodel.removeColumn(tablecolumnParameter);
			}
			if (tablecolumnSourceFile == null) {
				tablecolumnSourceFile = tbl.getColumn("sourceFile");
				columnmodel.removeColumn(tablecolumnSourceFile);
			}
			if (tablecolumnFormat == null) {
				tablecolumnFormat = tbl.getColumn("format");
				columnmodel.removeColumn(tablecolumnFormat);
			}

			clctcmbbxDataSource.setVisible(false);
			pnlFileChoosers.setVisible(true);
			break;

		default:
			throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
					"ReportExecutionCollectController.5","Unbekannter Reporttyp: {0}", outputType));
		}
	}

	/**
	 * Show all removed columns again, so they can be saved with their width, and are restored when opened again.
	 */
	@Override
	public void close() {
		final JTable tbl = getSubFormController(outputEntity.getEntityName()).getSubForm().getJTable();
		final TableColumnModel columnmodel = tbl.getColumnModel();
		if (tablecolumnDataSource != null) {
			columnmodel.addColumn(tablecolumnDataSource);
			tablecolumnDataSource = null;
		}
		if (tablecolumnParameter != null) {
			columnmodel.addColumn(tablecolumnParameter);
			tablecolumnParameter = null;
		}
		if (tablecolumnSourceFile != null) {
			columnmodel.addColumn(tablecolumnSourceFile);
			tablecolumnSourceFile = null;
		}
		if (tablecolumnFormat != null) {
			columnmodel.addColumn(tablecolumnFormat);
			tablecolumnFormat = null;
		}

		super.close();
	}

	private CollectableComponent getFirstComponent(String sFieldName) {
		final Collection<CollectableComponent> collclctcomp = getDetailsPanel().getEditView().getCollectableComponentsFor(sFieldName);
		return (collclctcomp.size() == 0) ? null : collclctcomp.iterator().next();
	}

	/**
	 *
	 */
	private void setupDetailsToolBar() {
		// additional functionality in Result panel:
		//final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		btnExportFile.setIcon(Icons.getInstance().getIconExport16());
		btnExportFile.setToolTipText(getSpringLocaleDelegate().getMessage(
				"ReportCollectController.1", "Reportvorlage exportieren"));
		btnExportFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdExportFile();
			}
		});

		btnPreview.setIcon(Icons.getInstance().getIconPlay16());
		btnPreview.setToolTipText(getSpringLocaleDelegate().getMessage(
				"ReportCollectController.2", "Layoutansicht"));
		btnPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdPreview();
			}
		});
		
		this.getDetailsPanel().addToolBarComponent(btnExportFile);
		this.getDetailsPanel().addToolBarComponent(btnPreview);
		/*
		toolbar.add(btnExportFile);
		toolbar.add(btnPreview);
		toolbar.addSeparator();

		getDetailsPanel().setCustomToolBarArea(toolbar);*/
	}

	/**
	 *
	 * @param clct
	 */
	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		reportdelegate.removeReport(clct.getMasterDataCVO());
	}

	/**
	 *
	 * @param clctEdited
	 * @return the current collectable.
	 */
	@Override
	protected CollectableMasterDataWithDependants updateCurrentCollectable(CollectableMasterDataWithDependants clctEdited) throws CommonBusinessException {
		getSubFormController(outputEntity.getEntityName()).stopEditing();

		validateReport(clctEdited);

		adjustValuesToTypeAndOutputFormat(clctEdited);

		return updateCollectable(clctEdited, getAllSubFormData(clctEdited.getId()));
		//		return super.updateCurrentCollectable(clctEdited);
	}

	/**
	 * Updates the currently edited Collectable in the database. PDF templates will be compiled by the report facade.
	 * @param clct
	 * @param oDependantData
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oDependantData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oDependantData;

		final Object oId = reportdelegate.modify(clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap());

		final MasterDataVO mdvoUpdated = mddelegate.get(getEntityName(), oId);

		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(),
			new MasterDataWithDependantsVO(mdvoUpdated, readDependants(mdvoUpdated.getId())));
	}
	
	@Override
	public CollectableMasterDataWithDependants newCollectable() {
		CollectableMasterDataWithDependants result = super.newCollectable();
		result.setField("type", getEntity().equals(NuclosEntity.REPORT.getEntityName())
				? new CollectableValueField(ReportType.REPORT.getValue()) : new CollectableValueField(ReportType.FORM.getValue()));
		return result;
	}
	@Override
	protected CollectableMasterDataWithDependants newCollectableWithDefaultValues() {
		CollectableMasterDataWithDependants result = super.newCollectableWithDefaultValues();
		result.setField("type", getEntity().equals(NuclosEntity.REPORT.getEntityName())
				? new CollectableValueField(ReportType.REPORT.getValue()) : new CollectableValueField(ReportType.FORM.getValue()));
		return result;
	}

	/**
	 * Inserts the currently edited Collectable in the database. PDF template files are compiled by the report facade.
	 * @param clctNew
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null)
			throw new IllegalArgumentException("clctNew");

		validateReport(clctNew);

		adjustValuesToTypeAndOutputFormat(clctNew);

		//		// We have to clear the ids for cloned objects:
		//		@todo eliminate this workaround
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(getAllSubFormData(null).toDependantMasterDataMap());
		
		final MasterDataVO mdvoInserted = reportdelegate.create(clctNew.getMasterDataCVO(), mpmdvoDependants);

		//return CollectableMasterDataWithDependants.newInstance(mdclctNew.getCollectableEntity(), mdvoInserted);
		return new CollectableMasterDataWithDependants(getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, readDependants(mdvoInserted.getId())));
	}

	private void validateReport(CollectableMasterDataWithDependants clctmdwd) throws CommonValidationException {
		for(Character ch : clctmdwd.getField("name").toString().toCharArray())
			if (!Character.isLetterOrDigit(ch) && !ch.equals('_') && !Character.isWhitespace(ch))
				throw new CollectableFieldValidationException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.3", ch));
		boolean existsAnyOutputFormat = false;
		for (CollectableMasterData md : getAllSubFormData(clctmdwd.getId()).getValues(outputEntity.getEntityName()))
			if (!md.isMarkedRemoved()) {
				existsAnyOutputFormat = true;
				boolean subreportsallowed = false;
				if (md.getField("destination").getValue() == null)
					throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
							"ReportCollectController.4", "Bitte geben Sie f\u00fcr jedes Ausgabeformat ein Ausgabemedium an."));
				if (md.getField("format").getValue() == null)
					throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
							"ReportCollectController.5", "Bitte geben Sie f\u00fcr jedes Ausgabeformat ein Ausgabeformat an."));
				final File fileSource = (md.getField("sourceFile").getValue() != null ? new File((String)md.getField("sourceFile").getValue()) : new File(""));
				final File fileDestination  = (md.getField("parameter").getValue() != null ? new File((String)md.getField("parameter").getValue()) : new File(""));
				final File fileSourceCollect = (clctmdwd.getField("sourceFile").getValue() != null ? new File((String)clctmdwd.getField("sourceFile").getValue()) : new File(""));
				final File fileDestinationCollect  = (clctmdwd.getField("parameter").getValue() != null ? new File((String)clctmdwd.getField("parameter").getValue()) : new File(""));
				switch (getOutputType()) {
					case EXCEL: {
						if ((fileSourceCollect.getName().equals("")) || (!fileSourceCollect.getName().toLowerCase().endsWith(".xls")))
							throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
									"ReportCollectController.8", "Bitte geben Sie f\u00fcr das Ausgabeformat XLS eine MS Excel Vorlage (.xls) an."));
						if ((!fileDestinationCollect.getName().equals("")) && (!fileDestinationCollect.isDirectory()) && (!fileDestinationCollect.getName().toLowerCase().endsWith(".xls")))
							throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
									"ReportCollectController.9", "Bitte geben Sie f\u00fcr das Ausgabeformat XLS eine MS Excel Zieldatei (.xls) an."));
						if ((md.getField("destination").getValue().toString().endsWith("Server")))
							throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
									"ReportCollectController.31", "Ausgabemedium \u00fcber Server ist momentan nur f\u00fcr das Ausgabeformat PDF erlaubt."));
						break;
					}
					default: {
						if (md.getField("format").getValue().equals("DOC")) {
							if ((fileSource.getName().equals("")) || (!fileSource.getName().toLowerCase().endsWith(".doc")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.6", "Bitte geben Sie f\u00fcr das Ausgabeformat DOC eine MS Word Vorlage (.doc) an."));
							if ((!fileDestination.getName().equals("")) && (!fileDestination.isDirectory()) && (!fileDestination.getName().toLowerCase().endsWith(".doc")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.7", "Bitte geben Sie f\u00fcr das Ausgabeformat DOC eine MS Word Zieldatei (.doc) an."));
							if ((md.getField("destination").getValue().toString().endsWith("Server")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.31", "Ausgabemedium \u00fcber Server ist momentan nur f\u00fcr das Ausgabeformat PDF erlaubt."));
						}
						else if (md.getField("format").getValue().equals("XLS")) {
							if ((fileSource.getName().equals("")) || (!fileSource.getName().toLowerCase().endsWith(".xls")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.8", "Bitte geben Sie f\u00fcr das Ausgabeformat XLS eine MS Excel Vorlage (.xls) an."));
							if ((!fileDestination.getName().equals("")) && (!fileDestination.isDirectory()) && (!fileDestination.getName().toLowerCase().endsWith(".xls")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.9", "Bitte geben Sie f\u00fcr das Ausgabeformat XLS eine MS Excel Zieldatei (.xls) an."));
							if ((md.getField("destination").getValue().toString().endsWith("Server")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.31", "Ausgabemedium \u00fcber Server ist momentan nur f\u00fcr das Ausgabeformat PDF erlaubt."));
						}
						else if (md.getField("format").getValue().equals("CSV")) {
							if ((md.getField("destination").getValue().toString().endsWith("Server")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.31", "Ausgabemedium \u00fcber Server ist momentan nur f\u00fcr das Ausgabeformat PDF erlaubt."));
						}
						else if (md.getField("format").getValue().equals("TSV")) {
							if ((md.getField("destination").getValue().toString().endsWith("Server")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.31", "Ausgabemedium \u00fcber Server ist momentan nur f\u00fcr das Ausgabeformat PDF erlaubt."));
						}
						else if (md.getField("format").getValue().equals("PDF")) {
							String fileName = fileSource.getName().toLowerCase();
							if (fileName.isEmpty() || !(fileName.endsWith(".xml") || fileName.endsWith(".jrxml")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.10", "Bitte geben Sie f\u00fcr das Ausgabeformat PDF eine Jasper Reports Vorlage (.xml, .jrxml) an."));
							if ((!fileDestination.getName().equals("")) && (!fileDestination.isDirectory()) && (!fileDestination.getName().toLowerCase().endsWith(".pdf")))
								throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
										"ReportCollectController.11", "Bitte geben Sie f\u00fcr das Ausgabeformat PDF eine PDF Zieldatei (.pdf) an."));
							subreportsallowed = true;
						}
					}
				}

				for (CollectableMasterData subreport : md.getDependantCollectableMasterDataMap().getValues(NuclosEntity.SUBREPORT.getEntityName())) {
					if (subreport.isMarkedRemoved())
						continue;

					if (!subreportsallowed)
						throw new CollectableFieldValidationException("ReportCollectController.validation.nosubreport");

					final String subreportsourcefilename = (String)subreport.getField("sourcefilename").getValue();
					if (StringUtils.isNullOrEmpty(subreportsourcefilename) || !(subreportsourcefilename.endsWith(".xml") || subreportsourcefilename.endsWith(".jrxml")))
						throw new CollectableFieldValidationException("ReportCollectController.validation.subreportmissingsource");
				}
			}
		if (!existsAnyOutputFormat)
			throw new CollectableFieldValidationException(getSpringLocaleDelegate().getMessage(
					"ReportCollectController.12", "Bitte geben Sie mindestens ein Ausgabeformat an."));
	}

	/**
	 * Adjust the entries from the output subform according to the chosen output format:
	 * - if no collective report, copy main datasource into all output dependants
	 * - in case of excel report set all output format values to xls
	 * @param clctmd
	 */
	private void adjustValuesToTypeAndOutputFormat(CollectableMasterData clctmd) throws CommonBusinessException {
		switch (getOutputType()) {
		case SINGLE: {
			if (clctcmbbxDataSource != null && clctcmbbxDataSource.getJComboBox().getSelectedItem() != null) {
				final CollectableField clctf = (CollectableField) clctcmbbxDataSource.getJComboBox().getSelectedItem();
				final CollectableTableModel<CollectableEntityObject> tblmodel = getSubFormController(outputEntity.getEntityName()).getCollectableTableModel();
				for (int i = 0; i < tblmodel.getRowCount(); i++)
					tblmodel.getCollectable(i).setField("datasource", clctf);
			}
			break;
		}
		case EXCEL: {
			final String sDestination = (String) clctmd.getMasterDataCVO().getField("parameter");
			final String sSourceFile = (String) clctmd.getMasterDataCVO().getField("sourceFile");
			final CollectableTableModel<CollectableEntityObject> tblmodel = getSubFormController(outputEntity.getEntityName()).getCollectableTableModel();
			for (int i = 0; i < tblmodel.getRowCount(); i++) {
				final Collectable clct = tblmodel.getCollectable(i);
				clct.setField("format", new CollectableValueField(ReportOutputVO.Format.XLS.getValue()));
				clct.setField("parameter", new CollectableValueField(sDestination));
				clct.setField("sourceFile", new CollectableValueField(sSourceFile));
			}
			break;
		}
		}

		importFiles(clctmd);
	}

	/**
	 * Read the specified files and set the content into the respective fields for outputs with template files
	 * We use the existence of a path in the filename as the sign, that the file template has changed, as the path will not be stored.
	 * @throws CommonBusinessException
	 */
	private void importFiles(CollectableMasterData clctmd) throws CommonBusinessException {
		final CollectableTableModel<CollectableEntityObject> tblmodel = getSubFormController(outputEntity.getEntityName()).getCollectableTableModel();
		for (int i = 0; i < tblmodel.getRowCount(); i++) {
			final CollectableEntityObject clct = tblmodel.getCollectable(i);
			final CollectableField clctfSource = clct.getField("sourceFile");
			if (clctfSource != null) {
				final String sSource = (String) clctfSource.getValue();
				if (sSource != null) {
					final File fileSource = new File(sSource);
					if (!fileSource.getPath().equals(fileSource.getName()))
						try {
							final byte[] abSourceFile = IOUtils.readFromBinaryFile(fileSource);
							clct.setField("sourceFileContent", new CollectableValueField(new org.nuclos.server.report.ByteArrayCarrier(abSourceFile)));
							clct.setField("sourceFile", new CollectableValueField(fileSource.getName()));
						}
					catch (IOException ex) {
						throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.13", sSource), ex);
					}
				}
			}

			for (CollectableMasterData subreportclct : clct.getDependantCollectableMasterDataMap().getValues(subreportEntity.getEntityName())) {
				final CollectableField subreportclctfSource = subreportclct.getField("sourcefilename");
				if (subreportclctfSource != null) {
					final String sourcefilename = (String) subreportclctfSource.getValue();
					if (sourcefilename != null) {
						final File subreportSource = new File(sourcefilename);
						if (!subreportSource.getPath().equals(subreportSource.getName()))
							try {
								final byte[] subreportByte = IOUtils.readFromBinaryFile(subreportSource);
								subreportclct.setField("sourcefileContent", new CollectableValueField(new org.nuclos.server.report.ByteArrayCarrier(subreportByte)));
								subreportclct.setField("sourcefilename", new CollectableValueField(subreportSource.getName()));
							}
						catch (IOException ex) {
							throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.13", subreportSource), ex);
						}
					}
				}
			}
		}

		if (getOutputType() == ReportVO.OutputType.EXCEL && tblmodel.getRowCount() > 0) {
			final String sSourceFile = (String) clctmd.getMasterDataCVO().getField("sourceFile");
			if (sSourceFile != null) {
				final File fileSource = new File(sSourceFile);
				if (!fileSource.getPath().equals(fileSource.getName()))
					clctmd.getMasterDataCVO().setField("sourceFile", fileSource.getName());
			}
		}
	}

	private void cmdExportFile() {
		String sFileName = null;
		CollectableEntityObject clct = null;
		if (getOutputType() == ReportVO.OutputType.EXCEL) {
			sFileName = (String) getSelectedCollectable().getMasterDataCVO().getField("sourceFile");

			final CollectableTableModel<CollectableEntityObject> tblmodel = getSubFormController(outputEntity.getEntityName()).getCollectableTableModel();
			for (int i = 0; i < tblmodel.getRowCount(); i++) {
				clct = tblmodel.getCollectable(i);
				final CollectableField clctfSource = clct.getField("sourceFileContent");
				if (clctfSource != null && clctfSource.getValue() != null)
					break;
			}

			if (sFileName == null || clct == null) {
				JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
						"ReportCollectController.14", "Dieses Objekt enth\u00e4lt keine Vorlagedatei."));
				return;
			}
		}
		else {
			clct = getSubFormController(outputEntity.getEntityName()).getSelectedCollectable();
			if (clct == null) {
				JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
						"ReportCollectController.15", "Bitte w\u00e4hlen Sie das Ausgabeformat aus, dessen Vorlagedatei exportiert werden soll."));
				return;
			}

			sFileName = (String) clct.getValue("sourceFile");

			if (sFileName == null) {
				JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
						"ReportCollectController.16", "Das ausgew\u00e4hlte Ausgabeformat enth\u00e4lt keine Vorlagedatei."));
				return;
			}
		}

		final org.nuclos.server.report.ByteArrayCarrier bacFileContent = (org.nuclos.server.report.ByteArrayCarrier) clct.getValue("sourceFileContent");
		if (bacFileContent == null) {
			JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
					"ReportCollectController.17", "Die Vorlagedatei kann nicht exportiert werden, da sie noch nicht importiert wurde."));
			return;
		}

		boolean includeSubreports = false;
		if (clct.getDependantCollectableMasterDataMap().getValues(subreportEntity.getEntityName()).size() > 0)
			includeSubreports = JOptionPane.showOptionDialog(getTab(), getSpringLocaleDelegate().getMessage(
					"ReportCollectController.question.exportsubreports", "Sollen Subreports exportiert werden?"), "Subreports", 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION;

		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (fileChooser.showOpenDialog(getTab()) == JFileChooser.APPROVE_OPTION) {
			final File targetDirectory = fileChooser.getSelectedFile();
			final File targetFile = new File(targetDirectory, sFileName);
			if (targetFile.exists())
				if (JOptionPane.showConfirmDialog(getTab(), getSpringLocaleDelegate().getMessage(
						"ReportCollectController.18", "Die Datei \"{0}\" existiert bereits. Wollen Sie sie \u00fcberschreiben?", 
						targetFile.getPath()), getSpringLocaleDelegate().getMessage(
								"ReportCollectController.19", "Vorlage exportieren"), 
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

			try {
				IOUtils.writeToBinaryFile(targetFile, bacFileContent.getData());
			}
			catch (IOException ex) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.19", targetFile.getPath()), ex);
			}

			if (includeSubreports)
				for (CollectableMasterData subreport : clct.getDependantCollectableMasterDataMap().getValues(getSubReportEntity())) {
					String subreportfilename = sFileName = (String) subreport.getValue("sourcefilename");
					if (subreportfilename != null) {
						final org.nuclos.server.report.ByteArrayCarrier bacFileSubreportContent = (org.nuclos.server.report.ByteArrayCarrier) subreport.getValue("sourcefileContent");
						if (bacFileSubreportContent == null) {
							JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
									"ReportCollectController.17", "Die Vorlagedatei kann nicht exportiert werden, da sie noch nicht importiert wurde."));
							continue;
						}

						File subreportFile = new File(targetDirectory, subreportfilename);
						if (subreportFile.exists())
							if (JOptionPane.showConfirmDialog(getTab(), getSpringLocaleDelegate().getMessage(
									"ReportCollectController.18", "Die Datei \"{0}\" existiert bereits. Wollen Sie sie \u00fcberschreiben?", subreportFile.getPath()), 
									getSpringLocaleDelegate().getMessage("ReportCollectController.19", "Vorlage exportieren"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
								continue;

						try {
							IOUtils.writeToBinaryFile(subreportFile, bacFileSubreportContent.getData());
						}
						catch (IOException ex) {
							throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.19", targetFile.getPath()), ex);
						}
					}
				}
		}
	}

	/**
	 *
	 */
	private void cmdPreview() {
		UIUtils.runCommand(getTab(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				ReportOutputVO outputvoFormat = null;
				final CollectableMasterData clctSelected = ReportCollectController.this.getSelectedCollectable();
				if (clctSelected != null)
					try {
						final ReportFacadeRemote facade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);

						final Collection<ReportOutputVO> collReportOutputVO = facade.getReportOutputs((Integer) clctSelected.getId());
						ReportOutputVO outputvo = null;

						if (collReportOutputVO.size() == 0) {
							JOptionPane.showMessageDialog(ReportCollectController.this.getTab(),
									getSpringLocaleDelegate().getMessage("ReportCollectController.20", "Diesem Report/Formular wurde keine Vorlage zugewiesen, dessen Layout als Vorschau angezeigt werden k\u00f6nnte."));
							return;
						}
						else if (collReportOutputVO.size() == 1)
							outputvo = collReportOutputVO.iterator().next();
						else if (collReportOutputVO.size() > 1) {
							Collectable clctReportOutput = ReportCollectController.this.getSubFormController(outputEntity.getEntityName()).getSelectedCollectable();
							if(clctReportOutput == null) {
								JOptionPane.showMessageDialog(ReportCollectController.this.getTab(),
										getSpringLocaleDelegate().getMessage("ReportCollectController.21", "Bitte w\u00e4hlen Sie die Vorlage aus, dessen Layout als Vorschau angezeigt werden soll."));
								return;
							}
							outputvo = facade.getReportOutput((Integer)clctReportOutput.getId());
						}

						if (outputvo == null || outputvo.getSourceFile() == null) {
							JOptionPane.showMessageDialog(ReportCollectController.this.getTab(),
									getSpringLocaleDelegate().getMessage("ReportCollectController.22", "Ein Fehler bei der Anzeige des Layouts der Vorlage ist aufgetreten. M\u00f6glicherweise wurde keine Vorlage zugewiesen."));
							return;
						}

						if ("PDF".equals(outputvo.getFormat().getValue()))
							outputvoFormat = outputvo;
						else if ("XLS".equals(outputvo.getFormat().getValue()))
							outputvoFormat = outputvo;
						else if ("XLSX".equals(outputvo.getFormat().getValue()))
							outputvoFormat = outputvo;
						else if ("DOC".equals(outputvo.getFormat().getValue()))
							outputvoFormat = outputvo;
						else if ("CSV".equals(outputvo.getFormat().getValue()))
							outputvoFormat = outputvo;

						if (outputvoFormat == null)
							// todo: some error handling here?
							return;

						previewReportOutputVO(outputvoFormat);
					}
				catch (RuntimeException ex) {
					throw new CommonBusinessException(ex.getMessage(), ex);
				}
			}
		});
	}

	/**
	 * Externalised Preview Body for
	 * ELISA-6574
	 * @param outputvo
	 * @throws CommonBusinessException
	 */
	private void previewReportOutputVO(ReportOutputVO outputvo) throws CommonBusinessException{
		String filename = outputvo.getSourceFile();
		if (filename == null) {
			filename = getSpringLocaleDelegate().getMessage("ReportCollectController.23", "Vorschau");
		}
		else {
			filename = filename.substring(0, filename.lastIndexOf('.'));
		}
		final String tempDir = System.getProperty("java.io.tmpdir");
		final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
		final File targetFile = new File(tempDir, filename + "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime()).replaceAll("[/+*%?#!.:]", "-") + outputvo.getFormat().getExtension());
		
		try {
			NuclosFile preview = reportdelegate.testReport(outputvo.getId());

			IOUtils.writeToBinaryFile(targetFile, preview.getFileContents());
			SystemUtils.open(targetFile);
		}
		catch (NuclosReportException e) {
			Errors.getInstance().showExceptionDialog(getParent(), e);
		}
		catch (IOException ex) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("ReportCollectController.19", targetFile.getPath()), ex);
		}
	}

	@Override
	protected boolean isSaveAllowed() {
		if (!SecurityCache.getInstance().isWriteAllowedForMasterData(getEntityName()))
			return false;

		if (getSelectedCollectableId() != null)
			return reportdelegate.isSaveAllowed((Integer) getSelectedCollectableId());
		else
			// new reports/forms may always be saved:
			return true;
	}

	@Override
	protected boolean isNewAllowed() {
		return SecurityCache.getInstance().isWriteAllowedForMasterData(getEntityName());
	}

	/**
	 * @return Is the "Delete" action for the given Collectable allowed? Default: true. May be overridden by subclasses.
	 */
	@Override
	protected boolean isDeleteAllowed(CollectableMasterDataWithDependants clct) {
		if (!SecurityCache.getInstance().isDeleteAllowedForMasterData(getEntityName()))
			return false;

		return reportdelegate.isSaveAllowed((Integer) clct.getId());
	}

	/**
	 * @deprecated Move to SearchController and make protected again. 
	 */
	@Override
	public CollectableSearchCondition getCollectableSearchConditionToDisplay() throws CollectableFieldFormatException {
		return getSearchStrategy().getCollectableSearchCondition();
	}

	public String getSubReportEntity() {
		if (NuclosEntity.FORM.getEntityName().equals(getEntityName())) {
			return NuclosEntity.SUBFORM.getEntityName();
		}
		else {
			return NuclosEntity.SUBREPORT.getEntityName();
		}
	}
}	// class ReportCollectController