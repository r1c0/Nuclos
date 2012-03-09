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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.QueryTable;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * <code>CollectController</code> for entity "dynamicEntity".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class DynamicEntityCollectController extends AbstractDatasourceCollectController<DynamicEntityVO> implements DatasourceEditController {

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public DynamicEntityCollectController(MainFrameTab tabIfAny) {
		super(new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICENTITY)), tabIfAny);

		CollectableMasterDataEntity clctEntity = new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICENTITY));
		initializeDatasourceCollectController(
			new DatasourceEditPanel(this, 
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_NAME),
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_ENTITY),
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
	public CollectableDataSource<DynamicEntityVO> findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return new CollectableDataSource<DynamicEntityVO>(datasourcedelegate.getDynamicEntity((Integer) oId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CollectableDataSource<DynamicEntityVO> newCollectable() {
		return new CollectableDataSource<DynamicEntityVO>(new DynamicEntityVO(null, null, null, null, null, null));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean stopEditingInDetails() {
		return pnlEdit.getQueryEditor().stopEditing();
	}

	/**
	 * @return the name of the current user.
	 */
	@Override
	public ResultVO cmdExecuteCurrentStatement(Integer iMaxRowCount) {
		ResultVO result = null;

		try {
			final String sDatasourceXML = pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(true));
			final Map<String, Object> mpParams = CollectionUtils.newHashMap();

			if (createParamMap(sDatasourceXML, mpParams, getTab())) {
				result = datasourcedelegate.executeQuery(sDatasourceXML, mpParams, iMaxRowCount);
			}
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(getTab(), ex);
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
		return datasourcedelegate.createSQL(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(true)));
	}

	/**
	 * @param sXML
	 */
	@Override
	protected void importXML(String sXML) throws NuclosBusinessException {
		final String sWarnings = QueryBuilderEditor.getSkippedElements(pnlEdit.getQueryEditor().setXML(sXML));
		if (sWarnings.length() > 0) {
			JOptionPane.showMessageDialog(getTab(), SpringLocaleDelegate.getInstance().getMessage(
					"DatasourceCollectController.12","Folgende Elemente existieren nicht mehr in dem aktuellen Datenbankschema\n und wurden daher entfernt:") + "\n" + sWarnings);
		}
		detailsChanged(pnlEdit.getQueryEditor());
	}

	/**
	 * @param file
	 * @throws IOException
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void exportXML(File file) throws IOException, CommonBusinessException {
		final String sReportXML = pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(true));
		IOUtils.writeToTextFile(file, sReportXML, "UTF-8");
	}

	@Override
	protected void validateSQL() {
		try {
			final DatasourceFacadeRemote dataSourceFacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
			dataSourceFacade.validateSqlFromXML(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(true)));
			JOptionPane.showMessageDialog(getTab(), SpringLocaleDelegate.getInstance().getMessage(
					"DatasourceCollectController.10","Die SQL Abfrage ist syntaktisch korrekt."));
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getTab(), ex);
		}
	}

	@Override
	protected void readValuesFromEditPanel(CollectableDataSource clct,
		boolean bSearchTab) throws CollectableValidationException {
		// if we have a search tab here, this method must be extended.
		assert !bSearchTab;

		super.readValuesFromEditPanel(clct, bSearchTab);

		try {
			clct.getDatasourceVO().setSource(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(true)));
		}
		catch (CommonBusinessException ex) {
			throw new CollectableValidationException(this.getCollectableEntity().getEntityField("datasourceXML"), ex);
		}
	}
	
	

	@Override
	protected List<ColumnEntry> getDefaultColumns() {
		List<ColumnEntry> result = new ArrayList<ColumnEntry>();
		
		final ColumnEntry INTID = new ColumnEntry();
		INTID.setAlias("INTID");
		INTID.setVisible(true);
		INTID.setDefaultWidth(120);
		result.add(INTID);
		
		final ColumnEntry INTID_T_UD_GENERICOBJECT = new ColumnEntry();
		INTID_T_UD_GENERICOBJECT.setAlias("INTID_T_UD_GENERICOBJECT");
		INTID_T_UD_GENERICOBJECT.setVisible(true);
		INTID_T_UD_GENERICOBJECT.setDefaultWidth(180);
		result.add(INTID_T_UD_GENERICOBJECT);
		
		return result;
	}

	@Override
	public Set<String> getQueryTypes() {
		HashSet<String> result = new HashSet<String>();
		result.add(QueryTable.QUERY_TYPE_DYNAMIC_ENTITY);
		return result;
	}

	@Override
	public boolean isWithParameterEditor() {
		return false;
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
