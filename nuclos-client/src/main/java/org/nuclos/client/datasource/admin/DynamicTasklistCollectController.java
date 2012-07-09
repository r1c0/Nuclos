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
import org.nuclos.client.datasource.querybuilder.QueryBuilderConstants;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.datasource.querybuilder.gui.ColumnEntry;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.QueryTable;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <code>CollectController</code> for dynamic task lists.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class DynamicTasklistCollectController extends AbstractDatasourceCollectController<DynamicTasklistVO> implements DatasourceEditController {
	
	// Spring injection
	
	private DatasourceFacadeRemote datasourceFacadeRemote;
	
	// end of Spring injection

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public DynamicTasklistCollectController(MainFrameTab tabIfAny) {
		super(new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICTASKLIST)), tabIfAny);

		CollectableMasterDataEntity clctEntity = new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICTASKLIST));
		initializeDatasourceCollectController(
			new DatasourceEditPanel(this, 
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_NAME),
				null,
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_DESCRIPTION)));
	}
	
	@Autowired
	final void setDatasourceFacadeRemote(DatasourceFacadeRemote datasourceFacadeRemote) {
		this.datasourceFacadeRemote = datasourceFacadeRemote;
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
		return new CollectableDataSource(datasourcedelegate.getDynamicTasklist((Integer)oId));
	}

	@Override
	public CollectableDataSource newCollectable() {
		return new CollectableDataSource(new DynamicTasklistVO(null, null, null, null, null, null));
	}
	
	@Override
	protected void readValuesFromEditPanel(CollectableDataSource clct, boolean bSearchTab) throws CollectableValidationException {
		//NUCLOSINT-1015
		pnlEdit.getQueryEditor().setParameter(QueryBuilderConstants.RECORDGRANT_SYSTEMPARAMETERS);
		super.readValuesFromEditPanel(clct, bSearchTab);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean stopEditingInDetails() {
		return pnlEdit.stopEditing();
	}

	/**
	 * @return the name of the current user.
	 */
	@Override
	public ResultVO cmdExecuteCurrentStatement(Integer iMaxRowCount) {
		ResultVO result = null;

		try {
			final String sDatasourceXML = this.pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false));
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
		return datasourcedelegate.createSQL(this.pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false)));
	}

	/**
	 * @param sXML
	 */
	@Override
	protected void importXML(String sXML) throws NuclosBusinessException {
		final String sWarnings = QueryBuilderEditor.getSkippedElements(pnlEdit.getQueryEditor().setXML(sXML));
		if (sWarnings.length() > 0) {
			JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
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
		final String sReportXML = pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false));
		IOUtils.writeToTextFile(file, sReportXML, "UTF-8");
	}

	@Override
	protected void validateSQL() {
		try {
			datasourceFacadeRemote.validateSqlFromXML(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false)));
			JOptionPane.showMessageDialog(getTab(), getSpringLocaleDelegate().getMessage(
					"DatasourceCollectController.10","Die SQL Abfrage ist syntaktisch korrekt."));
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getTab(), ex);
		}
	}

	@Override
	public Set<String> getQueryTypes() {
		HashSet<String> result = new HashSet<String>();
		result.add(QueryTable.QUERY_TYPE_DYNAMIC_TASK);
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
	
	@Override
	protected List<ColumnEntry> getDefaultColumns() {
		List<ColumnEntry> result = new ArrayList<ColumnEntry>();
		
		final ColumnEntry INTID_T_UD_GENERICOBJECT = new ColumnEntry();
		INTID_T_UD_GENERICOBJECT.setAlias("INTID");
		INTID_T_UD_GENERICOBJECT.setVisible(true);
		INTID_T_UD_GENERICOBJECT.setDefaultWidth(180);
		result.add(INTID_T_UD_GENERICOBJECT);
		
		final ColumnEntry USER = new ColumnEntry();
		USER.setCondition(0, "='$" + QueryBuilderConstants.PARAMETER_USERNAME_NAME + "'");
		USER.setVisible(false);
		USER.setDefaultWidth(180);
		result.add(USER);
		
		return result;
	}

}
