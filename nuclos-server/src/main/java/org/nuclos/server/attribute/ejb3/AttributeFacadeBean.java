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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.attribute.NuclosSystemAttributeNotModifiableException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

/**
 * Attribute facade bean encapsulating access functions for dynamic attributes.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(AttributeFacadeLocal.class)
@Remote(AttributeFacadeRemote.class)
@Transactional
public class AttributeFacadeBean extends NuclosFacadeBean implements AttributeFacadeLocal, AttributeFacadeRemote {

//	private DboFacadeLocal dboFacade;
//	
//	private final static String ATTRIBUTE_TABLE = "t_md_attribute";
//
//	private DboFacadeLocal getDboFacade() {
//		if (dboFacade == null)
//			dboFacade = ServiceLocator.getInstance().getFacade(DboFacadeLocal.class);
//		return dboFacade;
//	}

	/**
	 * @return a collection containing all dynamic attributes
	 */
	@Override
	@RolesAllowed("Login")
	public Collection<AttributeCVO> getAttributes(Integer iGroupId) {
		final Collection<AttributeCVO> result = new HashSet<AttributeCVO>();

		final SecurityCache securitycache = SecurityCache.getInstance();

		for (AttributeCVO attrcvo : AttributeCache.getInstance().getAttributes()) {
			if (iGroupId == null || iGroupId.equals(attrcvo.getAttributegroupId())) {
				final AttributeCVO attrcvoClone = attrcvo.clone();
				attrcvoClone.setPermissions(securitycache.getAttributeGroup(this.getCurrentUserName(), attrcvoClone.getAttributegroupId()));
				result.add(attrcvoClone);
			}
		}
		return result;
	}

	/**
	 * @param iAttributeId id of attribute
	 * @return the attribute value object for the attribute with the given id
	 * @throws CommonPermissionException
	 * @precondition iAttributeId != null
	 */
	@Override
	public AttributeCVO get(Integer iAttributeId) throws CommonFinderException, CommonPermissionException {
//		this.checkReadAllowed(NuclosEntity.ATTRIBUTE);
		if (iAttributeId == null) {
			throw new NullArgumentException("iAttributeId");
		}
		final AttributeCVO result;
		try {
			result = AttributeCache.getInstance().getAttribute(iAttributeId);
		}
		catch (NuclosAttributeNotFoundException ex) {
			throw new CommonFinderException(ex);
		}
		return result;
	}

	/**
	 * use is unnecessary
	 */
	@Override
	@Deprecated
	@RolesAllowed("Login")
	public AttributeCVO create(AttributeCVO attrcvo, DependantMasterDataMap mpDependants)
	throws CommonCreateException, CommonFinderException, CommonRemoveException,
	CommonStaleVersionException, CommonValidationException, CommonPermissionException {

		AttributeCVO result = null;
//
//		this.checkWriteAllowed("attribute");
//		attrcvo.validate();
//
//		final Attribute attribute = buildDbo(attrcvo,true);
//		try {
//			LocaleFacadeLocal locale = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
//			attribute.setResourceSIdForLabel(locale.setDefaultResource(null, attrcvo.getLabel()));
//			attribute.setResourceSIdForDescription(locale.setDefaultResource(null, attrcvo.getDescription()));
//
//			getDboFacade().save(attribute);
//			if (mpDependants != null) {
//				new MasterDataFacadeHelper().createDependants(mpDependants, "attribute", attribute.getId(), this.getCurrentUserName(), true, null);
//			}
//
//			final Map<Integer, Permission> mpPermissions = SecurityCache.getInstance().getAttributeGroup(getCurrentUserName(), attribute.getAttributegroupId());
//			final Map<Integer,List<AttributeValue>> values = getDboFacade().getAttributeValues();
//
//			result = VOFactory.FACTORY.createAttributeCVO(attribute,mpPermissions,values.get(attribute.getId()));
//
//		}
//		catch (SQLException ex) {
//			try{
//				throw SQLExceptionHandler.handleException(ex, new CommonCreateException(ex));
//			} catch (Exception newEx) {
//				throw new CommonCreateException(newEx.getMessage());
//			}
//		}
//		catch (CommonFatalException ex) {
//			if(ex.getCause() != null && ex.getCause() instanceof SQLException){
//				try{
//					throw SQLExceptionHandler.handleException((SQLException)ex.getCause(), new CommonCreateException(ex));
//				} catch (Exception newEx) {
//					throw new CommonFatalException(newEx.getMessage());
//				}
//			}
//			throw ex;
//		}
//
//		this.invalidateCache(attribute.getId());
//
//		this.refreshViews();
//
//		assert result != null;
		return result;
	}

	/**
	 * use is unnecessary
	 */
	@Override
	@Deprecated
	@RolesAllowed("Login")
	public Integer modify(AttributeCVO attrcvo, DependantMasterDataMap mpDependants) throws CommonCreateException,
			CommonFinderException, CommonRemoveException,
			CommonStaleVersionException, CommonValidationException,
			NuclosSystemAttributeNotModifiableException, CommonPermissionException, CreateException {
//		this.checkWriteAllowed("attribute");
//		attrcvo.validate();
//
//		Attribute attribute;
//
//		try {
//			validateUniqueConstraint(attrcvo);
//
//			Attribute a = (Attribute)getDboFacade().findById(new Attribute(), attrcvo.getId());
//			if (a.getVersion() != attrcvo.getVersion()) {
//				throw new CommonStaleVersionException();
//			}
//			if (a.isSystemAttribute()) {
//				// we only allow the user to change the label, description and
//				// group for system attributes:
//				this.debug("System attribute " + a.getName() + " changed by user.");
//				a.setLabel(attrcvo.getLabel());
//				a.setDescription(attrcvo.getDescription());
//				a.setAttributegroupId(attrcvo.getAttributegroupId());
//				a.setLogbookTracking(attrcvo.isLogbookTracking());
//			}
//			else{
//				// The datatype for an existing attribute may only changed to java.lang.String:
//				final String sJavaClassName = attrcvo.getDataType().getJavaClass().getName();
//				if (!sJavaClassName.equals("java.lang.String") && !sJavaClassName.equals(a.getDataType())) {
//					throw new CommonValidationException("attribute.update.exception.1");
//						//"Der Datentyp eines Attributs kann nachtr\u00e4glich nur in \"Text\" ge\u00e4ndert werden.");
//				}
//
//				// The "calculated" property may not be changed for an existing attribute:
//				if (attrcvo.isCalculated() != a.isCalculated()) {
//					throw new CommonValidationException("attribute.update.exception.2");
//						//"Die Eigenschaft \"berechnet\" eines Attributs kann nicht nachtr\u00e4glich ge\u00e4ndert werden.");
//				}
//				if(GenericObjectMetaDataCache.getInstance().getAttributeNamesByModuleId(null,null).contains(a.getName())){
//					if(!a.getName().equals(attrcvo.getName()))
//						throw new CommonValidationException("attribute.update.exception.3");
//							//"Der Name kann nicht nachtr\u00e4glich ge\u00e4ndert werden. Das Attribut wird in einem Layout verwendet.");
//				}
//
//
//				attribute = buildDbo(attrcvo,false);
//				getDboFacade().update(attribute);
//
//				LocaleFacadeLocal locale = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
//				if (attribute.getResourceSIdForLabel() != null) {
//					locale.updateResource(attribute.getResourceSIdForLabel(), attrcvo.getLabel());
//				}
//				if (attribute.getResourceSIdForDescription() != null) {
//					if (!StringUtils.isNullOrEmpty(attribute.getDescription())) {
//						locale.updateResource(attribute.getResourceSIdForDescription(), attrcvo.getDescription());
//					}
//					else {
//						LocaleUtils.setResourceIdForField(ATTRIBUTE_TABLE, attribute.getId(), LocaleUtils.FIELD_DESCRIPTION, null);
//						locale.deleteResource(attribute.getResourceSIdForDescription());
//					}
//				}
//				else {
//					String sRid = locale.setDefaultResource(null, attrcvo.getDescription()); 
//					LocaleUtils.setResourceIdForField(ATTRIBUTE_TABLE, attribute.getId(), LocaleUtils.FIELD_DESCRIPTION, sRid);
//				}				
//				
//			}
//
//		}
//		catch (SQLException ex) {
//			throw new CommonCreateException(ex);
//		}
//		catch (CommonFatalException ex) {
//			throw new NuclosFatalException(ex);
//		}
//
//		if (mpDependants != null) {
//			getMasterDataFacade().modifyDependants("attribute",attrcvo.getId(),attrcvo.isRemoved(),mpDependants);
//
//			for (MasterDataVO mdvoDependant : mpDependants.getValues("attributevalue")) {
//				if(mdvoDependant.isChanged()) {
//					String sValue = (attrcvo.isShowMnemonic()) ? mdvoDependant.getField("mnemonic").toString() : mdvoDependant.getField("name").toString();
//					makeConsistent(attrcvo.getId(), mdvoDependant.getIntId(), sValue);
//				}
//			}
//		}
//
//		this.invalidateCache(attrcvo.getId());
//		this.refreshViews();
		return attrcvo.getId();
	}

	@Deprecated
	private void buildDbo(AttributeCVO attrcvo, boolean createNew) {
//		Attribute a = new Attribute();
//		if (createNew) {
//			a.setId(DataBaseHelper.getNextIdAsInteger("idfactory"));
//			a.setChangedBy(getCurrentUserName());
//			a.setCreatedBy(getCurrentUserName());
//		}
//		else {
//			a.setId(attrcvo.getId());
//			a.setChangedBy(attrcvo.getChangedBy());
//			a.setCreatedBy(attrcvo.getCreatedBy());
//			a.setVersion(attrcvo.getVersion());
//		}
//		a.setAttributegroupId(attrcvo.getAttributegroupId());
//		a.setName(attrcvo.getName());
//		a.setLabel(attrcvo.getLabel());
//		a.setDescription(attrcvo.getDescription());
//		a.setCalcFunction(attrcvo.getCalcFunction());
//		a.setExternalEntityName(attrcvo.getExternalEntity());
//		a.setExternalEntityFieldName(attrcvo.getExternalEntityFieldName());
//		a.setDataType(attrcvo.getJavaClass().getName());
//		a.setDataScale(attrcvo.getDataScale());
//		a.setDataPrecision(attrcvo.getDataPrecision());
//		a.setInputFormat(attrcvo.getInputFormat());
//		a.setOutputFormat(attrcvo.getOutputFormat());
//		a.setNullable(attrcvo.isNullable());
//		a.setSearchable(attrcvo.isSearchable());
//		a.setModifiable(attrcvo.isModifiable());
//		a.setInsertable(attrcvo.isInsertable());
//		a.setLogbookTracking(attrcvo.isLogbookTracking());
//		a.setShowMnemonic(attrcvo.isShowMnemonic());
//		a.setSystemAttribute(false);
//
//		if (attrcvo.getExternalEntity() == null) {
//			a.setDefaultValueId(attrcvo.getDefaultValueId());
//			a.setDefaultValueExternalId(null);
//		}
//		else {
//			a.setDefaultValueId(null);
//			a.setDefaultValueExternalId(attrcvo.getDefaultValueId());
//		}
//
//		if (attrcvo.getDefaultValue() == RelativeDate.today()) {
//			a.setDefaultValue(RelativeDate.today().toString());
//		}
//		else {
//			a.setDefaultValue(DynamicAttributeVO.getCanonicalFormat(attrcvo).format(attrcvo.getDefaultValue()));
//		}
//		a.setSortationAsc(attrcvo.getSortationAsc());
//		a.setSortationDesc(attrcvo.getSortationDesc());
//		a.setResourceSIdForLabel(attrcvo.getResourceSIdForLabel());
//		a.setResourceSIdForDescription(attrcvo.getResourceSIdForDescription());
//
//		return a;
	}

	@Deprecated
   private void validateUniqueConstraint(AttributeCVO attrcvo) throws NuclosFatalException {
//   	try {
//			final Attribute attribute = (Attribute)getDboFacade().findByName(new Attribute(),attrcvo.getName());
//			if(attrcvo.getId().intValue() != attribute.getId().intValue()){
//				try{
//					throw SQLExceptionHandler.createException("default.UNIQUE", Arrays.asList("'Name'"), "'Attribute'");
//				} catch (Exception newEx) {
//					throw new NuclosFatalException(newEx.getMessage());
//				}
//			}
//		}  catch (CommonFinderException e) {
//			// No element found -> validation O.K.
//		}
//
//		try {
//			List<String> values = new ArrayList<String>();
//			Map<Integer,List<AttributeValue>> valueMap = getDboFacade().getAttributeValues();
//
//			for (AttributeValueVO attrvaluevo : attrcvo.getValues()) {
//				if(!attrvaluevo.isRemoved()){
//					if(values.contains(attrvaluevo.getValue())){
//						try{
//							throw SQLExceptionHandler.createException("default.UNIQUE", Arrays.asList("'Value' of list of values"));
//						} catch (Exception newEx) {
//							throw new NuclosFatalException(newEx.getMessage());
//						}
//					}
//					values.add(attrvaluevo.getValue());
//
//					for (AttributeValue attrvalue : valueMap.get(attrcvo.getId())) {
//						if (attrvaluevo.getValue().equals(attrvalue.getValue())) {
//							if(attrvaluevo.getId() == null || (attrvaluevo.getId().intValue() != attrvalue.getId().intValue())){
//								try{
//									throw SQLExceptionHandler.createException("default.UNIQUE", Arrays.asList("'Value' of list of values"));
//								} catch (Exception newEx) {
//									throw new NuclosFatalException(newEx.getMessage());
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (SQLException e) {
//			throw new NuclosFatalException(e.getMessage());
//		}
	}

	/**
	 * use is unnecessary
	 */
   @Override
@Deprecated
   @RolesAllowed("Login")
	public void remove(AttributeCVO attrcvo) throws CommonFinderException,
			CommonRemoveException, CommonStaleVersionException,
			NuclosSystemAttributeNotModifiableException, CommonPermissionException {
//		this.checkDeleteAllowed("attribute");
//		try {
//			final Attribute attribute = (Attribute)getDboFacade().findById(new Attribute(), attrcvo.getId());
//
//			if (attribute.getVersion() != attrcvo.getVersion()) {
//				throw new CommonStaleVersionException();
//			}
//
//			if (attribute.isSystemAttribute()) {
//				throw new NuclosSystemAttributeNotModifiableException();
//			}
//
//			// check if the attribute to remove is used in a layout:
//			final Set<String> sUsedAttributeNames = GenericObjectMetaDataCache.getInstance().getAttributeNamesByModuleId(null, null);
//			final String sAttributeName = attrcvo.getName();
//			if (sUsedAttributeNames.contains(sAttributeName)) {
//				throw new CommonRemoveException(StringUtils.getParameterizedExceptionMessage("attribute.remove.exception.1", sAttributeName));
//					//"Das Attribut \"" + sAttributeName + "\" kann nicht gel\u00f6scht werden, da es in einem Modul-Layout verwendet wird.");
//			}
//
//			// everything seems to be okay: remove the attribute:
//			getDboFacade().delete(attribute);
//
//			LocaleFacadeLocal locale = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
//			locale.deleteResource(attribute.getResourceSIdForLabel());
//			locale.deleteResource(attribute.getResourceSIdForDescription());
//
//		}
//		catch (Exception ex) {
//			throw new NuclosFatalException(ex);
//		}
//		// AttributeCache.getInstance().remove(attrcvo);
//		this.invalidateCache(attrcvo.getId());
//		this.refreshViews();
	}

	/**
	 * invalidates the attribute cache (console function)
	 */
	@Override
	@RolesAllowed("Login")
	public void invalidateCache() {
		this.invalidateCache(null);
	}

	/**
	 * invalidates the attribute cache
	 */
	@Override
	public void invalidateCache(Integer iAttributeId) {
		GenericObjectMetaDataCache.getInstance().attributeChanged(iAttributeId);
	}

	/**
	 * @return the available calculation functions for calculated attributes
	 */
	@Override
	@RolesAllowed("Login")
	public Collection<String> getCalculationFunctions() {
		return CollectionUtils.applyFilter(DataBaseHelper.getDbAccess().getCallableNames(), new Predicate<String>() {
			@Override public boolean evaluate(String name) {
				return StringUtils.toUpperCase(name).startsWith("CA");
			};
		});
	}

	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@Override
	@RolesAllowed("Login")
	public Set<String> getAttributeLayouts(String sAttributeName){
		Set<String> sLayoutsName = new HashSet<String>();
		LayoutMLParser parser = new LayoutMLParser();
		Map<Integer, String> mLayoutMap = GenericObjectMetaDataCache.getLayoutMap();
		Iterator<Integer> iLayoutsIds = mLayoutMap.keySet().iterator();
		while(iLayoutsIds.hasNext()){
			Integer iId = iLayoutsIds.next();
			try{
				if (mLayoutMap.get(iId) != null) {
					Set<String> set = parser.getCollectableFieldNames(new InputSource(new StringReader(mLayoutMap.get(iId))));
					if(set.contains(sAttributeName))
						sLayoutsName.add(GenericObjectMetaDataCache.getLayoutName(iId));
				}
			}
			catch(LayoutMLException e){
				throw new CommonFatalException(e);
			}
		}
		return sLayoutsName;
	}
	
	@Override
	@RolesAllowed("Login")
	public Set<String> getAttributeForModule(String sModuleId) {
		
		Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(sModuleId);
		
		Set<String> setAttributes = new HashSet<String>();
		Map<Integer, String> mLayoutMap = GenericObjectMetaDataCache.getLayoutMap();
		List<Integer> lstLayoutIds = GenericObjectMetaDataCache.getLayoutIdsForModule(iModuleId);
		for(Integer iLayoutId : lstLayoutIds) {
			LayoutMLParser parser = new LayoutMLParser();
			try{
				Set<String> set = parser.getCollectableFieldNames(new InputSource(new StringReader(mLayoutMap.get(iLayoutId))));
				setAttributes.addAll(set);
			}
			catch(LayoutMLException e){
				throw new CommonFatalException(e);
			}
		}
		
		return setAttributes;
	}

	/**
	 * use is unnecessary
	 */
	@Override
	@Deprecated
	@RolesAllowed("Login")
	public void makeConsistent(String sEntityName, Integer iCollectableId, Map<String, Object> mpChangedFields) throws CreateException, CommonFinderException {
//		for (String sKey : mpChangedFields.keySet()) {
//			String sValue = (mpChangedFields.get(sKey) == null) ? "" : mpChangedFields.get(sKey).toString();
//			makeConsistent(sEntityName, iCollectableId, new Pair<String, String>(sKey, sValue));
//		}
	}

	/**
	 * use is unnecessary
	 */
	@Override
	@Deprecated
	@RolesAllowed("Login")
	public void makeConsistent(String sEntityName, Integer iCollectableId, Pair<String, String> changedField) throws CreateException, CommonFinderException {
//		for(AttributeCVO attrcvo : AttributeCache.getInstance().getReferencingAttributes(sEntityName)) {
//			String externalEntityFieldName = attrcvo.getExternalEntityFieldName();
//			String changedFieldName = changedField.getX();
//			if ((externalEntityFieldName == null && "name".equals(changedFieldName)) ||
//					(externalEntityFieldName != null && changedFieldName.equals(externalEntityFieldName))) {
//				Integer iAttributeId = attrcvo.getId();
//				String sValue = changedField.getY();
//
//				// get all genericObjects which are involved by the attribute change
//				String sSqlSelect = "select intid_t_ud_genericobject from t_ud_go_attribute "+
//				"where intid_t_md_attribute = "+iAttributeId+
//				" and intid_external = "+iCollectableId;
//
//				List<Integer> lstgointid = DataBaseHelper.runSQLSelect(NuclosDataSources.getDefaultDS(), sSqlSelect, new NovabitDataBaseRunnable<List<Integer>>() {
//					public List<Integer> run(ResultSet rs) {
//						try {
//							List<Integer> lstgointid = new ArrayList<Integer>();
//							while (rs.next()) {
//								lstgointid.add(rs.getInt("intid_t_ud_genericobject"));
//							}
//							return lstgointid;
//						}
//						catch (SQLException e) {
//							throw new CommonFatalException(e);
//						}
//					}
//				});
//
//				String sDBCompatibleValue = sValue.replaceAll("'", "''");
//
//				// update all genericObjects which are involved by the attribute change
//				String sSqlUpdate = "update t_ud_go_attribute set strvalue = '"+sDBCompatibleValue+"' "+
//				"where intid_t_md_attribute = "+iAttributeId+
//				" and intid_external = "+iCollectableId;
//
//				DataBaseHelper.executeSQL(NuclosDataSources.getDefaultDS(), sSqlUpdate);
//
//				// recursive call to make all genericObjects consistent which are involved by the attribute change
//				for (Integer igointid : lstgointid) {
//					Integer iModuleId = getGenericObjectFacade().getModuleContainingGenericObject(igointid);
//					String sModuleEntityName = (String)Modules.getInstance().getModuleById(iModuleId).getField("entity");
//
//					makeConsistent(sModuleEntityName, igointid, new Pair<String, String>(attrcvo.getName(), sValue));
//				}
//			}
//		}
	}

	/**
	 * use is unnecessary
	 */
	@Override
	@Deprecated
	@RolesAllowed("Login")
	public void makeConsistent(Integer iAttributeId, Integer iAttributeValueId, String sAttributeValue) throws CreateException, CommonFinderException {
//		// get all genericObjects which are involved by the attributevalue change
//		String sSqlSelect = "select intid_t_ud_genericobject from t_ud_go_attribute "+
//		"where intid_t_md_attribute = "+iAttributeId+
//		" and intid_t_dp_value = "+iAttributeValueId;
//
//		List<Integer> lstgointid = DataBaseHelper.runSQLSelect(NuclosDataSources.getDefaultDS(), sSqlSelect, new NovabitDataBaseRunnable<List<Integer>>() {
//			public List<Integer> run(ResultSet rs) {
//				try {
//					List<Integer> lstgointid = new ArrayList<Integer>();
//					while (rs.next()) {
//						lstgointid.add(rs.getInt("intid_t_ud_genericobject"));
//					}
//					return lstgointid;
//				}
//				catch (SQLException e) {
//					throw new CommonFatalException(e);
//				}
//
//			}
//		});
//
//		// update all genericObjects which are involved by the attributevalue change
//		String sSql = "update t_ud_go_attribute set strvalue = '"+sAttributeValue+"'"+
//		"where intid_t_md_attribute = "+iAttributeId+
//		" and intid_t_dp_value = "+iAttributeValueId;
//
//		DataBaseHelper.executeSQL(NuclosDataSources.getDefaultDS(), sSql);
//
//		// recursive call to make all genericObjects consistent which are involved by the attributevalue change
//		for (Integer igointid : lstgointid) {
//			Integer iModuleId = getGenericObjectFacade().getModuleContainingGenericObject(igointid);
//			String sModuleEntityName = (String)Modules.getInstance().getModuleById(iModuleId).getField("entity");
//			String sAttributeName = AttributeCache.getInstance().getAttribute(iAttributeId).getName();
//
//			makeConsistent(sModuleEntityName, igointid, new Pair<String, String>(sAttributeName, sAttributeValue));
//		}
	}
}
