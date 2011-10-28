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
package org.nuclos.server.attribute.ejb3;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.GenericObjectMetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.attribute.valueobject.LayoutVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.transfer.XmlExportImportHelper;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

/**
 * Layout facade encapsulating generic object screen layout management.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Stateless
// @Local(LayoutFacadeLocal.class)
// @Remote(LayoutFacadeRemote.class)
@Transactional
public class LayoutFacadeBean extends MasterDataFacadeBean implements LayoutFacadeLocal, LayoutFacadeRemote {

	/**
	 * imports the given layouts, adding new and overwriting existing layouts. The other existing layouts are untouched.
	 * Currently, only the layoutml description is imported, not the usages.
	 * @param colllayoutvo
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void importLayouts(Collection<LayoutVO> colllayoutvo) throws CommonBusinessException {
		for (LayoutVO layoutvo : colllayoutvo) {
			importLayout(NuclosEntity.LAYOUT, layoutvo);
		}
		GenericObjectMetaDataCache.getInstance().layoutChanged(null);
		notifyClients(NuclosEntity.LAYOUT);
	}

	private void importLayout(NuclosEntity entity, LayoutVO layoutvo)
			throws CommonCreateException, CommonStaleVersionException, CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {

		final String sLayoutName = layoutvo.getName();
		final MasterDataMetaVO mdmetavo = getMetaData(entity);
		final CollectableSearchCondition cond = SearchConditionUtils.newMDComparison(mdmetavo, "name", ComparisonOperator.EQUAL, sLayoutName);
		final TruncatableCollection<MasterDataVO> collmdvo = getMasterData(entity.getEntityName(), cond, true);
		if (collmdvo.isEmpty()) {
			final MasterDataVO mdvoNew = new MasterDataVO(mdmetavo, true);
			mdvoNew.setField("name", sLayoutName);
			mdvoNew.setField("description", layoutvo.getDescription());
			mdvoNew.setField("layoutML", layoutvo.getLayoutML());
			create(entity.getEntityName(), mdvoNew, null);
		}
		else {
			final MasterDataVO mdvo = collmdvo.iterator().next();
			mdvo.setField("layoutML", layoutvo.getLayoutML());
			try {
				modify(entity.getEntityName(), mdvo, null);
			}
			catch (CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}
			catch (CommonRemoveException ex) {
				throw new CommonFatalException(ex);
			}
		}
	}

	/**
	 * refreshes the module attribute relation table and all generic object views (console function)
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void refreshAll() {
		GenericObjectMetaDataCache.getInstance().layoutChanged(null);
	}


	/**
	 * @param sEntity
	 * @return true, if detail layout is available for the given entity name, otherwise false
	 */
	@Override
	@RolesAllowed("Login")
	public boolean isMasterDataLayoutAvailable(String sEntity) {
		return getMasterDataLayout(sEntity) == null ? false : true;
	}

	/**
	 * @param sEntity
	 * @return the detail layout for the given entity name if any, otherwise null
	 */
	@Override
	@RolesAllowed("Login")
	public String getMasterDataLayout(String sEntity) {
		return getMasterDataLayout(sEntity, false);
	}
	/**
	 * @param sEntity
	 * @return the layout for the given entity name if any, otherwise null
	 */
	@Override
	@RolesAllowed("Login")
	public String getMasterDataLayout(String sEntity, boolean bSearchMode) {
	String sLayoutML = null;
		final List<MasterDataVO> lstMDLayoutUsage = new ArrayList<MasterDataVO>(getMasterDataFacade().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), null, true));
		for (MasterDataVO mdvoUsage : lstMDLayoutUsage) {
//			final String sEntityName = mdvoUsage.getField("entity", String.class);
			final String sEntityName = mdvoUsage.getField("entity", String.class);
			final boolean bSearch = mdvoUsage.getField("searchScreen", Boolean.class);
			final Integer iLayoutId = mdvoUsage.getField("layoutId", Integer.class);

			if (sEntityName.equals(sEntity) && bSearch == bSearchMode) {
				try {
					MasterDataVO mdvoLayout = MasterDataFacadeHelper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LAYOUT), iLayoutId);
					sLayoutML = mdvoLayout.getField("layoutML", String.class);
					break;
				}
				catch (CommonFinderException e) {
					throw new NuclosFatalException(e);
				}
			}
		}
		return sLayoutML;
	}

	/**
	 * returns the entity names of the subform entities along with their foreignkey field
	 * and the referenced parent entity name used in the given layout
	 * Note that this works only for genericobject entities
	 * @param iLayoutId
	 */
	@Override
	@RolesAllowed("Login")
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesByLayoutId(Integer iLayoutId) {
		String sLayoutML = null;

		try {
			MasterDataVO mdvoLayout = MasterDataFacadeHelper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LAYOUT), iLayoutId);
			sLayoutML = mdvoLayout.getField("layoutML", String.class);
		}
		catch (CommonFinderException e) {
			throw new NuclosFatalException(e);
		}

		if (sLayoutML == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("layout.facade.exception.1", iLayoutId));
				//"Die Eingabemaske mit der Id \"" + iLayoutId + "\" wurde nicht gefunden.");
		}
		try {
			return new LayoutMLParser().getSubFormEntityAndParentSubFormEntityNames(new InputSource(new StringReader(sLayoutML)));
		} catch (LayoutMLException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * returns the names of the subform entities along with their foreignkey field
	 * and the referenced parent entity name used in the given entity
	 * @param entityName
	 * @param id, id of MasterDataVO or GenericObjectVO
	 * @param forImportOrExport, true if it is used for import- or export-routines
	 */
	@Override
	// Caveat: Das Methodenformat und der Kommentar sind ein wenig verwirrend und verschleiern
	// die internen Zusammenhaenge. Fuer Stammdaten ist eine Objekt-ID ueberhaupt nicht notwendig,
	// sie wird hier ignoriert. Bei generischen Objekten werden die UsageCritera (Modul- +
	// Prozess-ID) verwendet, um das "am besten passende" Layout zu ermitteln (s.
	// GenericObjectMetaDataCache.getBestMatchingLayoutId().
	@RolesAllowed("Login")
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(String entityName, Integer id, boolean forImportOrExport) {

		Map<EntityAndFieldName, String> result = new HashMap<EntityAndFieldName, String>();

		if (Modules.getInstance().isModuleEntity(entityName)) {
			try {
				result = getSubFormEntityAndParentSubFormEntityNamesByGO(getGenericObjectFacade().get(id));
			}
			catch (CommonFinderException e) {
				throw new CommonFatalException(e);
			}
			catch (CommonPermissionException e) {
				throw new CommonFatalException(e);
			}
		}
		else {
			result = getSubFormEntityAndParentSubFormEntityNamesMD(entityName,forImportOrExport);
		}

		return result;
	}

	private Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesMD(String entityName, boolean forImportOrExport) {
		String sLayoutML = getMasterDataLayout(entityName);

		Map<EntityAndFieldName, String> result = new HashMap<EntityAndFieldName, String>();

		if (sLayoutML == null) {
			// special handling for entities with manually build layouts which are not saved in the database
			NuclosEntity entity = NuclosEntity.getByName(entityName);
			if (entity != null) {
				switch (entity) {
				case RULE:
					result.put(new EntityAndFieldName(NuclosEntity.RULEUSAGE, "rule"), null);
					break;
				case STATEMODEL:
					result.put(new EntityAndFieldName(NuclosEntity.STATEMODELUSAGE, "statemodel"), null);
					result.put(new EntityAndFieldName(NuclosEntity.STATE, "model"), null);
					break;
				case STATE:
					result.put(new EntityAndFieldName(NuclosEntity.STATETRANSITION, "state2"), null);
					result.put(new EntityAndFieldName(NuclosEntity.ROLEATTRIBUTEGROUP, "state"), null);
					result.put(new EntityAndFieldName(NuclosEntity.ROLESUBFORM, "state"), null);
					break;
				case STATETRANSITION:
					result.put(new EntityAndFieldName(NuclosEntity.ROLETRANSITION, "transition"), null);
					result.put(new EntityAndFieldName(NuclosEntity.RULETRANSITION, "transition"), null);
					break;
				case DATASOURCE:
					result.put(new EntityAndFieldName(NuclosEntity.DATASOURCEUSAGE, "datasource"), null);
					break;
				case GENERATION:
					result.put(new EntityAndFieldName(NuclosEntity.RULEGENERATION, "generation"), null);
					break;
				case TASKLIST:
					result.put(new EntityAndFieldName(NuclosEntity.TASKOBJECT, "tasklist"), null);
					result.put(new EntityAndFieldName(NuclosEntity.TASKOWNER, "tasklist"), null);					
					break;
				case REPORT:
					result.put(new EntityAndFieldName(NuclosEntity.REPORTOUTPUT, "report"), null);
					break;
				}
			}
/* Der "tiefe" XML-Export tut's ueberhaupt nicht mehr mit dem "Original"-Code: */
//			else {
//				throw new NuclosFatalException("Die Eingabemaske f\u00fcr die Entit\u00e4t \"" + entityName + "\" fehlt.");
//			}
/* tentativer Fix (10/2009)- TODO: ueberpruefen!!! */
			else {
				MasterDataMetaVO metaVO =
					MasterDataMetaCache.getInstance().getMetaData(entityName);
				if (!metaVO.isSystemEntity()) {
					throw new NuclosFatalException(
						StringUtils.getParameterizedExceptionMessage("layout.facade.exception.2", entityName));
						//"Die Eingabemaske f\u00fcr die Entit\u00e4t \"" + entityName + "\" fehlt.");
				}
			}
		}
		else {
			try {
				final InputSource inSrc = new InputSource(new StringReader(sLayoutML));
				inSrc.setSystemId("entity=" + entityName + ":forImportOrExport=" + forImportOrExport);
				if (forImportOrExport) {
					Map<EntityAndFieldName, String> subformtree = new LayoutMLParser().getSubFormEntityAndParentSubFormEntityNames(inSrc);

					for (EntityAndFieldName eafn : subformtree.keySet()) {
						//String sSubform = eafn.getEntityName();
						//String sForeignKeyFieldName = XmlExportImportHelper.getForeignKeyFieldName(eafn.getEntityName(), eafn.getFieldName(), sSubform);
						//result.put(new EntityAndFieldName(sSubform, sForeignKeyFieldName), subformtree.get(eafn));
						/* Das kann so wohl kaum stimmen, s. XmlExportFacadeBean.exportGOEntity(); tentativer Fix (10/2009): */
						String sSubform = eafn.getEntityName();
						String sParentEntityName = subformtree.get(eafn);
						String sForeignKeyFieldName = XmlExportImportHelper.getForeignKeyFieldName(
							(sParentEntityName == null ? entityName : sParentEntityName), eafn.getFieldName(), sSubform);
						result.put(new EntityAndFieldName(sSubform, sForeignKeyFieldName), sParentEntityName/*?!*/);
					}
				}
				else {
					return new LayoutMLParser().getSubFormEntityAndParentSubFormEntityNames(inSrc);
				}
			} catch (LayoutMLException e) {
				throw new NuclosFatalException(e);
			}
		}

		return result;
	}

	private Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesByGO(GenericObjectVO govo) throws CommonFinderException {
		final AttributeProvider attrprovider = AttributeCache.getInstance();
		final GenericObjectMetaDataProvider lometadataprovider = GenericObjectMetaDataCache.getInstance();
		final int iBestMatchingLayoutId = lometadataprovider.getBestMatchingLayoutId(govo.getUsageCriteria(attrprovider), false);
		return getSubFormEntityAndParentSubFormEntityNamesByLayoutId(iBestMatchingLayoutId);
	}



	/**
	 * returns the entity names of the subform entities along with their foreignkey field
	 * and the referenced parent entity name used in the given layout
	 * Note that this works only for genericobject entities
	 * @param iLayoutId
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission role-name="Login"
	 */
	@Override
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNamesById(Integer iLayoutId) {
		String sLayoutML = null;

		try {
			MasterDataVO mdvoLayout = MasterDataFacadeHelper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LAYOUT), iLayoutId);
			sLayoutML = mdvoLayout.getField("layoutML", String.class);
		}
		catch (CommonFinderException e) {
			throw new NuclosFatalException(e);
		}

		if (sLayoutML == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("layout.facade.exception.1", iLayoutId));
				//"Die Eingabemaske mit der Id \"" + iLayoutId + "\" wurde nicht gefunden.");
		}
		try {
			return new LayoutMLParser().getSubFormEntityAndParentSubFormEntityNames(new InputSource(new StringReader(sLayoutML)));
		} catch (LayoutMLException e) {
			throw new NuclosFatalException(e);
		}
	}


}
