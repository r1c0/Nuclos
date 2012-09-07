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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.database.query.definition.QueryTable;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * <code>CollectController</code> for entity "datasource".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class DatasourceCollectController extends AbstractDatasourceCollectController<DatasourceVO> implements DatasourceEditController {
	
	// former Spring injection
	
	private DatasourceFacadeRemote datasourceFacadeRemote;
	
	// end of former Spring injection

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public DatasourceCollectController(MainFrameTab tabIfAny) {
		super(new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DATASOURCE)), tabIfAny);
		setDatasourceFacadeRemote(SpringApplicationContextHolder.getBean(DatasourceFacadeRemote.class));
		
		CollectableMasterDataEntity clctEntity = new CollectableMasterDataEntity(
			MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICENTITY));
		initializeDatasourceCollectController(
			new DatasourceEditPanel(this, 
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_NAME),
				null,
				new CollectableEntityFieldWithEntity(clctEntity, CollectableDataSource.FIELDNAME_DESCRIPTION)));
		
		setupResultToolBar();
	}
	
	final void setDatasourceFacadeRemote(DatasourceFacadeRemote datasourceFacadeRemote) {
		this.datasourceFacadeRemote = datasourceFacadeRemote;
	}
	
	final DatasourceFacadeRemote getDatasourceFacadeRemote() {
		return datasourceFacadeRemote;
	}
	
	private void setupResultToolBar() {
		this.getResultPanel().addToolBarComponent(new JButton(new AbstractAction("", Icons.getInstance().getIconTree16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				getMainController().getExplorerController().cmdShowDatasources(null);
			}
		}));
	}
	
	@Override
	protected List<JButton> getAdditionalToolbarButtons() {
		List<JButton> lstButtons = new ArrayList<JButton>();
		
		final JButton btnMakeTreeRoot = new JButton();
		btnMakeTreeRoot.setIcon(Icons.getInstance().getIconMakeTreeRoot16());
		btnMakeTreeRoot.setToolTipText(getSpringLocaleDelegate().getMessage("DatasourceCollectController.14","In Explorer anzeigen"));
		btnMakeTreeRoot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdJumpToTree();
			}
		});
		lstButtons.add(btnMakeTreeRoot);
		
		return lstButtons;
	}

	/**
	 * show datasource in exlporer tree
	 *
	 */
	protected void cmdJumpToTree() {
		UIUtils.runCommand(this.getTab(), new Runnable() {
			@Override
			public void run() {
				final Integer iDatasourceId = (Integer) getSelectedCollectableId();
				getMainController().getExplorerController().cmdShowDatasources(iDatasourceId);
			}
		});
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
	public CollectableDataSource<DatasourceVO> findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return new CollectableDataSource<DatasourceVO>(datasourcedelegate.getDatasource((Integer) oId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CollectableDataSource<DatasourceVO> newCollectable() {
		return new CollectableDataSource<DatasourceVO>(new DatasourceVO(null, null, null, null, null));
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
			final String sDatasourceXML = pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false));
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
		return datasourcedelegate.createSQL(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false)));
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
			getDatasourceFacadeRemote().validateSqlFromXML(pnlEdit.getQueryEditor().getXML(new DatasourceEntityOptions(false)));
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
		result.add(QueryTable.QUERY_TYPE_REPORT);
		return result;
	}

	@Override
	public boolean isWithParameterEditor() {
		return true;
	}

	@Override
	public boolean isParameterEditorWithValuelistProviderColumn() {
		return true;
	}

	@Override
	public boolean isParameterEditorWithLabelColumn() {
		return true;
	}
}
