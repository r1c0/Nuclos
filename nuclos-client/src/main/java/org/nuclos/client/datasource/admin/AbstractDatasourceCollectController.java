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
package org.nuclos.client.datasource.admin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.console.NuclosConsole;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.querybuilder.QueryBuilderConstants;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;

public abstract class AbstractDatasourceCollectController<T extends DatasourceVO> extends NuclosCollectController<CollectableDataSource<T>>  {

	private static final Logger LOG = Logger.getLogger(AbstractDatasourceCollectController.class);

	protected static final String PREFS_KEY_LASTIMPORTEXPORTPATH = "lastImportExportPath";

	protected final DatasourceDelegate datasourcedelegate = DatasourceDelegate.getInstance();

	private boolean addTabToParent;
	protected DatasourceEditPanel pnlEdit;
	protected CollectPanel<CollectableDataSource<T>> pnlCollect = new DatasourceCollectPanel(false);

	protected JButton btnImport;
	protected JButton btnExport;
	protected JButton btnValidate;

	/**
	 * Don't make this public!
	 *
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected AbstractDatasourceCollectController(CollectableEntity clcte, MainFrameTab tabIfAny) {
		super(clcte, tabIfAny);
		addTabToParent = tabIfAny==null;
	}

	protected void initializeDatasourceCollectController(DatasourceEditPanel pnlEdit) {
		this.pnlEdit = pnlEdit;

		this.initialize(this.pnlCollect);

		getTab().setLayeredComponent(pnlCollect);

		this.setupShortcutsForTabs(getTab());

		this.setupDetailsToolBar();

		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.getHeader().newCollectableComponentsProvider()));

		btnImport.setEnabled(true);
		btnExport.setEnabled(true);
		btnValidate.setEnabled(true);

		this.getCollectStateModel().addCollectStateListener(new DatasourcesCollectStateListener());
	}
	
	/**
	 * @throws NuclosBusinessException
	 * @todo This method probably shouldn't popup an option pane. @see CollectModel for a discussion
	 */
	@Override
	protected final void deleteCollectable(CollectableDataSource<T> clct) throws CommonBusinessException {
		final List<DatasourceVO> lstUsages = DatasourceDelegate.getInstance().getUsagesForDatasource(clct.getDatasourceVO());
		if (!lstUsages.isEmpty()) {
			final int iBtn = JOptionPane.showConfirmDialog(this.getTab(), getSpringLocaleDelegate().getMessage(
					"DatasourceCollectController.8","Diese Datenquelle wird in anderen Datenquellen verwendet.") + "\n" +
					getSpringLocaleDelegate().getMessage(
							"DatasourceCollectController.1","Das L\u00f6schen f\u00fchrt dazu, dass folgende Datenquellen nicht mehr ausf\u00fchrbar sind") + ":\n" + getUsagesAsString(lstUsages) +
					"\n" + getSpringLocaleDelegate().getMessage(
							"DatasourceCollectController.24","Wollen sie die Datenquelle dennoch l\u00f6schen?"), 
							getSpringLocaleDelegate().getMessage("DatasourceCollectController.20","Umbenennung best\u00e4tigen"), 
							JOptionPane.YES_NO_OPTION);
			if (iBtn != JOptionPane.OK_OPTION) {
				throw new CommonBusinessException(getSpringLocaleDelegate().getMessage(
						"DatasourceCollectController.15","L\u00f6schen wurde durch den Benutzer abgebrochen."));
			}
			DatasourceDelegate.getInstance().setInvalid(lstUsages);
		}

		datasourcedelegate.remove(clct.getDatasourceVO());

		pnlEdit.getQueryEditor().getController().refreshSchema();
	}

	/**
	 * @param clctEdited
	 * @return
	 * @throws NuclosBusinessException
	 */
	@Override
	protected final CollectableDataSource<T> updateCurrentCollectable(CollectableDataSource<T> clctEdited) throws CommonBusinessException {
		//validateParameters();

		final List<String> lstUsedDatasources = pnlEdit.getQueryEditor().getUsedDatasources();

		final DatasourceVO datasourceVO = clctEdited.getDatasourceVO();

		final boolean bDataSourceNameWasChanged = !datasourcedelegate.getDatasource(clctEdited.getId()).getName().equals(datasourceVO.getName());
		if (bDataSourceNameWasChanged) {
			final List<DatasourceVO> lstUsages = DatasourceDelegate.getInstance().getUsagesForDatasource(clctEdited.getId());
			if (!lstUsages.isEmpty()) {
				final int iBtn = JOptionPane.showConfirmDialog(this.getTab(), getSpringLocaleDelegate().getMessage(
						"DatasourceCollectController.9","Diese Datenquelle wird in anderen Datenquellen verwendet.") + "\n" +
						getSpringLocaleDelegate().getMessage("DatasourceCollectController.11","Eine Umbenennung f\u00fchrt dazu, dass folgende Datenquellen nicht mehr ausf\u00fchrbar sind:") + "\n" +
						getUsagesAsString(lstUsages) + "\n" + getSpringLocaleDelegate().getMessage(
								"DatasourceCollectController.23","Wollen sie dennoch speichern?"), 
								getSpringLocaleDelegate().getMessage("DatasourceCollectController.21","Umbenennung best\u00e4tigen"), 
								JOptionPane.YES_NO_OPTION);
				if (iBtn != JOptionPane.OK_OPTION) {
					throw new CommonBusinessException(getSpringLocaleDelegate().getMessage(
							"DatasourceCollectController.18","Speichern wurde durch den Benutzer abgebrochen."));
				}
				DatasourceDelegate.getInstance().setInvalid(lstUsages);
			}
		}
		final DatasourceVO datasourceVOUpdated = datasourcedelegate.modify(datasourceVO, null, lstUsedDatasources);

		pnlEdit.getQueryEditor().getController().refreshSchema();

		return new CollectableDataSource(datasourceVOUpdated);
	}

	@Override
	protected final CollectableDataSource<T> updateCollectable(CollectableDataSource<T> clct, Object oAdditionalData) throws CommonBusinessException {
		/** @todo implement */
		throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
				"DatasourceCollectController.17","Sammelbearbeitung ist hier noch nicht m\u00f6glich."));
	}

	/**
	 * @param clctNew
	 * @return
	 * @throws NuclosBusinessException
	 */
	@Override
	protected final CollectableDataSource<T> insertCollectable(CollectableDataSource<T> clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}
		
		final DatasourceVO datasourceVO = datasourcedelegate.create(clctNew.getDatasourceVO(), null,
				pnlEdit.getQueryEditor().getUsedDatasources());

		pnlEdit.getQueryEditor().getController().refreshSchema();

		return new CollectableDataSource(datasourceVO);
	}


	@Override
	protected String getEntityLabel() {
		return getSpringLocaleDelegate().getLabelFromMetaDataVO(MetaDataCache.getInstance().getMetaData(sEntity));
	}

	/**
	 * @param clct
	 * @param bSearchTab
	 * @throws CollectableValidationException
	 */
	@Override
	protected void readValuesFromEditPanel(CollectableDataSource clct, boolean bSearchTab) throws CollectableValidationException {
		// if we have a search tab here, this method must be extended.
		assert !bSearchTab;

		super.readValuesFromEditPanel(clct, bSearchTab);

		try {
			clct.getDatasourceVO().setSource(this.pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false)));
		}
		catch (CommonBusinessException ex) {
			throw new CollectableValidationException(this.getCollectableEntity().getEntityField("source"), ex);
		}
	}

	/**
	 * @param clct
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void validate(CollectableDataSource clct) throws CommonBusinessException {
		if (!pnlEdit.isModelUsed() && !pnlEdit.getSql().trim().toUpperCase().startsWith("SELECT")) {
			throw new CommonValidationException(
					getSpringLocaleDelegate().getMessage(
							"DatasourceCollectController.22","Ung\u00fcltiges SQL Statement. Es d\u00fcrfen nur SELECT Anweisungen ausgef\u00fchrt werden."));
		}
		validateParameters();
	}

	/** validate datasource parameters
	 *
	 */
	private void validateParameters() throws CommonValidationException {
		for (final DatasourceParameterVO paramvo : pnlEdit.getQueryEditor().getParameters()) {
			paramvo.validate();
		}
	}

	/**
	 *
	 */
	private void setupDetailsToolBar() {
		//final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();

		btnImport = new JButton(Icons.getInstance().getIconImport16());
		btnImport.setToolTipText(getSpringLocaleDelegate().getMessage("DatasourceCollectController.3","Datenquelle importieren"));
		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdImport();
			}
		});
		//toolbarCustomDetails.add(btnImport);
		this.getDetailsPanel().addToolBarComponent(btnImport);

		btnExport = new JButton(Icons.getInstance().getIconExport16());
		btnExport.setToolTipText(getSpringLocaleDelegate().getMessage("DatasourceCollectController.2","Datenquelle exportieren"));
		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdExport();
			}
		});
		//toolbarCustomDetails.add(btnExport);
		this.getDetailsPanel().addToolBarComponent(btnExport);

		btnValidate = new JButton(Icons.getInstance().getIconValidate16());
		btnValidate.setToolTipText(getSpringLocaleDelegate().getMessage(
				"DatasourceCollectController.19","Syntax der SQL Abfrage pr\u00fcfen"));
		btnValidate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdValidateSql();
			}
		});
		//toolbarCustomDetails.add(btnValidate);
		this.getDetailsPanel().addToolBarComponent(btnValidate);

		//toolbarCustomDetails.addSeparator();

		for(JButton jb : getAdditionalToolbarButtons()){
			//toolbarCustomDetails.add(jb);
			this.getDetailsPanel().addToolBarComponent(jb);
		}

		//this.getDetailsPanel().setCustomToolBarArea(toolbarCustomDetails);
	}

	protected List<JButton> getAdditionalToolbarButtons() {
		return Collections.<JButton>emptyList();
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		pnlEdit.getQueryEditor().addChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @deprecated Move to DetailsController and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		pnlEdit.getQueryEditor().removeChangeListener(this.changelistenerDetailsChanged);
	}

	protected static String getUsagesAsString(List<DatasourceVO> lstUsages) {
		/** @todo Use	CollectionUtils.getSeparatedList() */

		final StringBuffer sb = new StringBuffer();
		for (DatasourceVO datasourcevo : lstUsages) {
			sb.append(datasourcevo.getName());
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length() - 2);

		return sb.toString();
	}

	@Override
	protected CollectableDataSource findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}

	/**
	 * import a datasource from XML
	 */
	protected void cmdImport() {
		final String sCurrentDirectory = this.getPreferences().get(PREFS_KEY_LASTIMPORTEXPORTPATH, null);
		final JFileChooser filechooser = new JFileChooser(sCurrentDirectory);
		filechooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith("xml");
			}

			@Override
			public String getDescription() {
				return "*.xml";
			}
		});
		final int iBtn = filechooser.showOpenDialog(this.getTab());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				this.getPreferences().put(PREFS_KEY_LASTIMPORTEXPORTPATH, file.getParent());
				UIUtils.runCommand(this.getTab(), new Runnable() {
					@Override
					public void run() {
						try {
							importXML(IOUtils.readFromTextFile(file, "UTF-8"));
						}
						catch (FileNotFoundException e) {
							Errors.getInstance().showExceptionDialog(getTab(), e);
						}
						catch (IOException e) {
							Errors.getInstance().showExceptionDialog(getTab(), e);
						}
						catch (NuclosBusinessException e) {
							Errors.getInstance().showExceptionDialog(getTab(), e);
						}
					}
				});
			}
		}
	}

	protected abstract void importXML(String sXML) throws NuclosBusinessException;

	/**
	 * export datasource to XML
	 */
	protected void cmdExport() {
		final String sCurrentDirectory = this.getPreferences().get(PREFS_KEY_LASTIMPORTEXPORTPATH, null);
		JFileChooser filechooser;
		try {
			filechooser = new JFileChooser(sCurrentDirectory);
		}
		catch (Exception e) {
			filechooser = new JFileChooser();
		}
		filechooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith("xml");
			}

			@Override
			public String getDescription() {
				return "*.xml";
			}
		});
		final int iBtn = filechooser.showSaveDialog(this.getTab());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				this.getPreferences().put(PREFS_KEY_LASTIMPORTEXPORTPATH, file.getParent());
				String sPathName = filechooser.getSelectedFile().getAbsolutePath();
				if (!sPathName.toLowerCase().endsWith(".xml")) {
					sPathName += ".xml";
				}
				final File xmlFile = new File(sPathName);
				UIUtils.runCommand(this.getTab(), new Runnable() {
					@Override
					public void run() {
						try {
							exportXML(xmlFile);
						}
						catch (IOException ex) {
							Errors.getInstance().showExceptionDialog(getTab(), ex);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getTab(), ex);
						}
					}
				});
			}
		}
	}

	protected abstract void exportXML(File file) throws IOException, CommonBusinessException ;

	protected void cmdValidateSql() {
		UIUtils.runCommand(this.getTab(), new Runnable() {
			@Override
			public void run() {
				validateSQL();
			}
		});
	}


	protected abstract void validateSQL();

	protected List<ColumnEntry> getDefaultColumns() {
		return Collections.emptyList();
	}

	/**
	 * @param clct
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void unsafeFillDetailsPanel(CollectableDataSource clct) throws CommonBusinessException {
		// fill the textfields:
		super.unsafeFillDetailsPanel(clct);

		final DatasourceVO datasourceVO = clct.getDatasourceVO();

		if (datasourceVO.getId() == null) {
			pnlEdit.getQueryEditor().newDatasource(getDefaultColumns());
		}
		else {
			if (datasourceVO.getSource() != null) {
				final Map<String, List<String>> mpWarnings = pnlEdit.getQueryEditor().setXML(datasourceVO.getSource());
				final String sWarnings = QueryBuilderEditor.getSkippedElements(mpWarnings);
				if (sWarnings.length() > 0) {
					JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
							"DatasourceCollectController.13","Folgende Elemente existieren nicht mehr in dem aktuellen Datenbankschema und wurden daher entfernt") + ":\n" + sWarnings);
				}
				final List<DatasourceParameterVO> lstParams = datasourcedelegate.getParametersFromXML(datasourceVO.getSource());
				pnlEdit.getQueryEditor().setParameter(lstParams);

				final Set<String> stColumnParameters = new HashSet<String>(DatasourceUtils.getParametersFromString(datasourceVO.getSource()));
				final Set<String> stDefinedParameters = new HashSet<String>();

				for (DatasourceParameterVO paramvo : lstParams) {
					final String sParameter = paramvo.getParameter();
					stDefinedParameters.add(sParameter);
					if (paramvo.getValueListProvider() != null && !StringUtils.isNullOrEmpty(paramvo.getValueListProvider().getType())) {
						stDefinedParameters.add(sParameter + "Id");
					}

					if (pnlEdit.isModelUsed() && !(stColumnParameters.contains(sParameter) || stColumnParameters.contains(sParameter + "Id")) ) {
						if (datasourceVO instanceof RecordGrantVO &&
							QueryBuilderConstants.RECORDGRANT_SYSTEMPARAMETER_NAMES.contains(sParameter)) {
							continue;
						}

						JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
								"DatasourceCollectController.5","Der Parameter \"{0}\" ist definiert, wird aber nicht verwendet.", sParameter));
					}
				}
				for (String sParameter : stColumnParameters) {
					// username should always be available
					if (sParameter.equals("username")) {
						continue;
					}
					if (pnlEdit.isModelUsed() && !stDefinedParameters.contains(sParameter)) {
						JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
								"DatasourceCollectController.6","Der Parameter \"{0}\" ist nicht definiert.", sParameter));
					}
				}
			}
		}
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				pnlEdit.refreshView();
			}
		});
	}


	/** ask the user for parameter values
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return
	 * @throws CommonBusinessException
	 */
	public static boolean createParamMap(String sDatasourceXML, Map<String, Object> mpParams, Component parent) throws CommonBusinessException {
		final List<DatasourceParameterVO> lstParams = DatasourceDelegate.getInstance().getParametersFromXML(sDatasourceXML);

		return createParamMap(lstParams, mpParams, parent);
	}

	public static boolean createParamMap(List<DatasourceParameterVO> lstParams, Map<String, Object> mpParams, Component parent) throws CommonBusinessException {
		boolean result = true;
		final ParameterPanel panel = new ParameterPanel(lstParams);

		if (lstParams.iterator().hasNext()) {
			result = (JOptionPane.showOptionDialog(parent, panel, 
					SpringLocaleDelegate.getInstance().getMessage("DatasourceCollectController.16","Parameter"), 
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION);
			if (result) {
				panel.fillParameterMap(lstParams, mpParams);
			}
		}
		return result;
	}
	
	public void execute(final CommonClientWorker worker) {
		CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<CollectableDataSource<T>>(this) {

			@Override
			public void init() throws CommonBusinessException {
				super.init();
				worker.init();
			}

			@Override
			public void work() throws CommonBusinessException {
				worker.work();
			}

			@Override
			public void paint() throws CommonBusinessException {
				worker.paint();
				super.paint();
			}
		});
	}

	protected class DatasourcesCollectStateListener extends CollectStateAdapter {
		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws NuclosBusinessException {
			final int iDetailsMode = ev.getNewCollectState().getInnerState();
			if (iDetailsMode == CollectState.DETAILSMODE_NEW) {
				pnlEdit.setIsModelUsed(true);
				pnlEdit.refreshView();
			}
			AbstractDatasourceCollectController dscc = AbstractDatasourceCollectController.this;
			final boolean bWriteAllowed = SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);

			final QueryBuilderEditor editor = dscc.pnlEdit.getQueryEditor();
			if (iDetailsMode == CollectState.DETAILSMODE_EDIT || iDetailsMode == CollectState.DETAILSMODE_MULTIEDIT) {
				editor.getTableSelectionPanel().getParameterPanel().getDeleteParameterAction().setEnabled(bWriteAllowed);
			}
			dscc.btnImport.setEnabled(bWriteAllowed);
			dscc.btnValidate.setEnabled(bWriteAllowed);
			final DatasourceHeaderPanel header = dscc.pnlEdit.getHeader();
			header.getDescriptionField().setEnabled(bWriteAllowed);
			header.getNameField().setEnabled(bWriteAllowed);
			if (header.getEntityComboBox() != null) {
				header.getEntityComboBox().setEnabled(bWriteAllowed);
			}

			editor.getTableSelectionPanel().getParameterPanel().getNewParameterAction().setEnabled(bWriteAllowed);
			editor.getTableSelectionPanel().getParameterPanel().getParameterTable().setEnabled(bWriteAllowed);
			editor.getColumnSelectionPanel().getTable().setEnabled(bWriteAllowed);
			dscc.pnlEdit.sqlPanel.getBtnGenerateSql().setEnabled(bWriteAllowed);
		}
	}

	private class DatasourceCollectPanel<T> extends CollectPanel {

		DatasourceCollectPanel(boolean bSearchPanelAvailable) {
			super(bSearchPanelAvailable);
		}

		@Override
		public ResultPanel<CollectableDataSource> newResultPanel() {
			return new NuclosResultPanel<CollectableDataSource>() {
				@Override
				protected void postXMLImport(final CollectController<CollectableDataSource> clctctl) {
					// initialize attribute cache on server side
					try {
						NuclosConsole.getInstance().parseAndInvoke(new String[]{NuclosConsole.getInstance().CMD_INVALIDATEALLCACHES}, false);
					}
					catch(Exception e) {
						throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
								"DatasourceCollectController.7","Der serverseitige DatasourceCache konnte nicht invalidiert werden!"), e);
					}

					super.postXMLImport(clctctl);
				}
			};
		}

		@Override
		public DetailsPanel newDetailsPanel() {
			return new DetailsPanel(false);
		}


	}
}
