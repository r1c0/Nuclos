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
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.QueryTable;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * <code>CollectController</code> for entity "valuelistProvider".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ValuelistProviderCollectController extends AbstractDatasourceCollectController implements DatasourceEditController {

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ValuelistProviderCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.VALUELISTPROVIDER)), tabIfAny);

		CollectableMasterDataEntity clctEntity = new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.VALUELISTPROVIDER));
		initializeDatasourceCollectController(
			new DatasourceEditPanel(this, 
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_NAME),
				null,
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_DESCRIPTION)));
	}

	@Override
	public void detailsChanged(Component compSource) {
		super.detailsChanged(compSource);
	}

	@Override
	protected boolean isNewAllowed() {
		return isSaveAllowed();
	}

	@Override
	protected boolean isSaveAllowed() {
		return SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed(){
		return SecurityCache.getInstance().isDeleteAllowedForMasterData(sEntity);
	}

	@Override
	public CollectableDataSource findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return new CollectableDataSource(datasourcedelegate.getValuelistProvider((Integer) oId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CollectableDataSource newCollectable() {
		return new CollectableDataSource(new ValuelistProviderVO(null, null, null, null, null));
	}

	/**
	 * @throws NuclosBusinessException
	 * @todo This method probably shouldn't popup an option pane. @see CollectModel for a discussion
	 */
	@Override
	protected void deleteCollectable(CollectableDataSource clct) throws CommonBusinessException {
		final List<DatasourceVO> lstUsages = DatasourceDelegate.getInstance().getUsagesForDatasource(clct.getDatasourceVO());
		if (!lstUsages.isEmpty()) {
			final int iBtn = JOptionPane.showConfirmDialog(this.getFrame(), CommonLocaleDelegate.getMessage("DatasourceCollectController.8","Diese Datenquelle wird in anderen Datenquellen verwendet.") + "\n" +
				CommonLocaleDelegate.getMessage("DatasourceCollectController.1","Das L\u00f6schen f\u00fchrt dazu, dass folgende Datenquellen nicht mehr ausf\u00fchrbar sind") + ":\n" + getUsagesAsString(lstUsages) +
					"\n" + CommonLocaleDelegate.getMessage("DatasourceCollectController.24","Wollen sie die Datenquelle dennoch l\u00f6schen?"), CommonLocaleDelegate.getMessage("DatasourceCollectController.20","Umbenennung best\u00e4tigen"), JOptionPane.YES_NO_OPTION);
			if (iBtn != JOptionPane.OK_OPTION) {
				throw new CommonBusinessException(CommonLocaleDelegate.getMessage("DatasourceCollectController.15","L\u00f6schen wurde durch den Benutzer abgebrochen."));
			}
			DatasourceDelegate.getInstance().setInvalid(lstUsages);
		}

		datasourcedelegate.removeValuelistProvider((ValuelistProviderVO) clct.getDatasourceVO());

		pnlEdit.pnlQueryEditor.getController().refreshSchema();
	}

	/**
	 * @param clctEdited
	 * @return
	 * @throws NuclosBusinessException
	 */
	@Override
	protected CollectableDataSource updateCurrentCollectable(CollectableDataSource clctEdited) throws CommonBusinessException {
		//validateParameters();

		final List<String> lstUsedDatasources = pnlEdit.pnlQueryEditor.getUsedDatasources();

		final ValuelistProviderVO valuelistProviderVO = (ValuelistProviderVO) clctEdited.getDatasourceVO();

		final boolean bDataSourceNameWasChanged = !datasourcedelegate.getValuelistProvider(clctEdited.getId()).getName().equals(valuelistProviderVO.getName());
		if (bDataSourceNameWasChanged) {
			final List<DatasourceVO> lstUsages = DatasourceDelegate.getInstance().getUsagesForDatasource(valuelistProviderVO);
			if (!lstUsages.isEmpty()) {
				final int iBtn = JOptionPane.showConfirmDialog(this.getFrame(), CommonLocaleDelegate.getMessage("DatasourceCollectController.9","Diese Datenquelle wird in anderen Datenquellen verwendet.") + "\n" +
					CommonLocaleDelegate.getMessage("DatasourceCollectController.11","Eine Umbenennung f\u00fchrt dazu, dass folgende Datenquellen nicht mehr ausf\u00fchrbar sind:") + "\n" +
						getUsagesAsString(lstUsages) + "\n" + CommonLocaleDelegate.getMessage("DatasourceCollectController.23","Wollen sie dennoch speichern?"), CommonLocaleDelegate.getMessage("DatasourceCollectController.21","Umbenennung best\u00e4tigen"), JOptionPane.YES_NO_OPTION);
				if (iBtn != JOptionPane.OK_OPTION) {
					throw new CommonBusinessException(CommonLocaleDelegate.getMessage("DatasourceCollectController.18","Speichern wurde durch den Benutzer abgebrochen."));
				}
				DatasourceDelegate.getInstance().setInvalid(lstUsages);
			}
		}
		final ValuelistProviderVO valuelistprovidervoUpdated = datasourcedelegate.modifyValuelistProvider(valuelistProviderVO, lstUsedDatasources);

		pnlEdit.pnlQueryEditor.getController().refreshSchema();

		return new CollectableDataSource(valuelistprovidervoUpdated);
	}

	@Override
	protected CollectableDataSource updateCollectable(CollectableDataSource clct, Object oAdditionalData) throws CommonBusinessException {
		/** @todo implement */
		throw new NuclosFatalException(CommonLocaleDelegate.getMessage("DatasourceCollectController.17","Sammelbearbeitung ist hier noch nicht m\u00f6glich."));
	}

	/**
	 * @param clctNew
	 * @return
	 * @throws NuclosBusinessException
	 */
	@Override
	protected CollectableDataSource insertCollectable(CollectableDataSource clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}
		
		final ValuelistProviderVO valuelistprovidervo = datasourcedelegate.createValuelistProvider((ValuelistProviderVO) clctNew.getDatasourceVO(),
				pnlEdit.pnlQueryEditor.getUsedDatasources());

		pnlEdit.pnlQueryEditor.getController().refreshSchema();

		return new CollectableDataSource(valuelistprovidervo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean stopEditingInDetails() {
		return pnlEdit.pnlQueryEditor.stopEditing();
	}

	/**
	 * @return the name of the current user.
	 */
	@Override
	public ResultVO cmdExecuteCurrentStatement(Integer iMaxRowCount) {
		ResultVO result = null;

		try {
			final String sDatasourceXML = this.pnlEdit.pnlQueryEditor.getXML(new DatasourceEntityOptions(false));
			final Map<String, Object> mpParams = CollectionUtils.newHashMap();

			if (createParamMap(sDatasourceXML, mpParams, ifrm)) {
				result = datasourcedelegate.executeQuery(sDatasourceXML, mpParams, iMaxRowCount);
			}
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
		return result;
	}

	/**
	 * generate the sql statement from the current definition of this datasource
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	public String generateSql() throws CommonBusinessException {
		return datasourcedelegate.createSQL(this.pnlEdit.pnlQueryEditor.getXML(new DatasourceEntityOptions(false)));
	}

	/**
	 * @param sXML
	 */
	@Override
	protected void importXML(String sXML) throws NuclosBusinessException {
		final String sWarnings = QueryBuilderEditor.getSkippedElements(pnlEdit.pnlQueryEditor.setXML(sXML));
		if (sWarnings.length() > 0) {
			JOptionPane.showMessageDialog(parent, CommonLocaleDelegate.getMessage("DatasourceCollectController.12","Folgende Elemente existieren nicht mehr in dem aktuellen Datenbankschema\n und wurden daher entfernt:") + "\n" + sWarnings);
		}
		detailsChanged(pnlEdit.pnlQueryEditor);
	}

	/**
	 * @param file
	 * @throws IOException
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void exportXML(File file) throws IOException, CommonBusinessException {
		final String sReportXML = pnlEdit.pnlQueryEditor.getXML(new DatasourceEntityOptions(false));
		IOUtils.writeToTextFile(file, sReportXML, "UTF-8");
	}

	@Override
	protected void validateSQL() {
		try {
			final DatasourceFacadeRemote dataSourceFacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
			dataSourceFacade.validateSqlFromXML(pnlEdit.pnlQueryEditor.getXML(new DatasourceEntityOptions(false)));
			JOptionPane.showMessageDialog(getFrame(), CommonLocaleDelegate.getMessage("DatasourceCollectController.10","Die SQL Abfrage ist syntaktisch korrekt."));
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	@Override
	public void execute(final CommonClientWorker worker) {
		CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<CollectableDataSource>(this) {

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

	@Override
	public Set<String> getQueryTypes() {
		HashSet<String> result = new HashSet<String>();
		result.add(QueryTable.QUERY_TYPE_VALUELIST_PROVIDER);
		return result;
	}

	@Override
	public boolean isWithParameterEditor() {
		return true;
	}

	@Override
	public boolean isParameterEditorWithValuelistProviderColumn() {
		return false;
	}

	@Override
	public boolean isParameterEditorWithLabelColumn() {
		return false;
	}

}
