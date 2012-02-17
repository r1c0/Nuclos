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
package org.nuclos.server.updatejobs;

import java.util.List;

import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.UpdateJobs;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Make attributes consistent, which have a reference to another entity.
 *
 * The problem up to now was, if a field or attribute of an entity has been changed and
 * an attribute of a module had a reference to the changed field, the value of the
 * database table t_ud_go_attribute was not changed as well. This caused some inconsistent
 * data and made some problems while showing the data in the dialogs.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 * 
 * @deprecated Does nothing/unneeded? (Please re-check!) (tp)
 */
public class MakeAttributesConsistent implements UpdateJobs{

	public static final String sRelease = "Nucleus Release 2.1.2";
	
	private boolean isSuccessfulExecuted = true;

	private MasterDataFacadeLocal mdfacadelocal;
	private GenericObjectFacadeLocal gofacadelocal;
	private AttributeFacadeLocal attrfacadelocal;

	private final AttributeCache attrprovider = AttributeCache.getInstance();
	
	/**
	 * @deprecated Does nothing/unneeded? (Please re-check!) (tp)
	 */
	public MakeAttributesConsistent() {
	}

	@Override
	public boolean execute() {
		LOG.debug("START executing MakeAttributesConsistent");

		makeAttributesConsistent();
		makeAttributeValuesConsistent();

		LOG.debug("END executing MakeAttributesConsistent");

		return isSuccessfulExecuted;
	}

	/**
	 * @deprecated Does nothing/unneeded? (Please re-check!) (tp)
	 */
	private void makeAttributesConsistent() {
		final DbAccess dbAccess = SpringDataBaseHelper.getInstance().getDbAccess();
		DbQueryBuilder builder = dbAccess.getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_ATTRIBUTE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_EXTERNAL", Integer.class));
		query.distinct(true);
		
		// get all masterdata entities
		for (final MasterDataMetaVO mdmvo : MasterDataMetaCache.getInstance().getAllMetaData()) {
			// get all attributes which have a reference to the current masterdata entity
			for (final AttributeCVO attrcvo : AttributeCache.getInstance().getReferencingAttributes(mdmvo.getEntityName())) {
				// check whether attribute is used in a genericobject
				try {
					query.where(builder.equal(t.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), attrcvo.getId()));
					List<Integer> intidExternals = dbAccess.executeQuery(query);
					
					for (Integer intid_external : intidExternals) {
						try {
							// get current field value of the referenced masterdata record
							MasterDataVO mdVO = getMasterDataFacade().get(attrcvo.getExternalEntity(), intid_external);
							String sExternalEntityFieldName = (attrcvo.getExternalEntityFieldName() == null) ? "name" : attrcvo.getExternalEntityFieldName();
							Object oValue_new = mdVO.getField(sExternalEntityFieldName);
							if (oValue_new == null) {
								continue;
							}
							String sValue_new = oValue_new.toString();

							// not needed/available
							// make attributes consistent
							// getAttributeFacade().makeConsistent(attrcvo.getExternalEntity(), intid_external, new Pair<String, String>(sExternalEntityFieldName, sValue_new));
						}
						catch (CommonPermissionException ex) {
							isSuccessfulExecuted = false;
							LOG.error(ex, ex.getCause());
						}
						catch (CommonFinderException ex) {
							isSuccessfulExecuted = false;
							LOG.error(ex, ex.getCause());
						}
					}
				}
				catch (DbException ex) {
					isSuccessfulExecuted = false;
					LOG.error(ex, ex.getCause());
				}
			}
		}

		// get all module entities
		for (final MasterDataVO mdvo : Modules.getInstance().getModules()) {
			// get all attributes which have a reference to the current module entity
			for (final AttributeCVO attrcvo : AttributeCache.getInstance().getReferencingAttributes(mdvo.getField("entity").toString())) {
				// check whether attribute is used in a genericobject
				try {
					query.where(builder.equal(t.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), attrcvo.getId()));
					List<Integer> intidExternals = dbAccess.executeQuery(query);
					
					for (Integer intid_external : intidExternals) {
						try {
							// get current field value of the referenced genericobject record
							GenericObjectVO goVO = getGenericObjectFacade().get(intid_external);
							String sExternalEntityFieldName = (attrcvo.getExternalEntityFieldName() == null) ? "name" : attrcvo.getExternalEntityFieldName();
							Object oValue_new = goVO.getAttribute(sExternalEntityFieldName, attrprovider).getValue();
							if (oValue_new == null) {
								continue;
							}
							String sValue_new = oValue_new.toString();

							// not needed/available
							// make attributes consistent
							// getAttributeFacade().makeConsistent(attrcvo.getExternalEntity(), intid_external, new Pair<String, String>(sExternalEntityFieldName, sValue_new));
						}
						catch (CommonPermissionException ex) {
							isSuccessfulExecuted = false;
							LOG.error(ex, ex.getCause());
						}
						catch (CommonFinderException ex) {
							isSuccessfulExecuted = false;
							LOG.error(ex, ex.getCause());
						}
					}
				}
				catch (DbException ex) {
					isSuccessfulExecuted = false;
					LOG.error(ex, ex.getCause());
				}
			}
		}
	}

	/**
	 * @deprecated Does nothing/unneeded.
	 */
	private void makeAttributeValuesConsistent() {
		// get all attributes
		for (AttributeCVO attrcvo : AttributeCache.getInstance().getAttributes()) {
			// get all attribute values
			for (AttributeValueVO attrvaluevo : attrcvo.getValues()) {
				// make attributes consistent
				final String sValue_new = (attrcvo.isShowMnemonic()) ? attrvaluevo.getMnemonic() : attrvaluevo.getValue();
				// not needed/available
				// getAttributeFacade().makeConsistent(attrcvo.getId(), attrvaluevo.getId(), sValue_new);
			}
		}
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		if (mdfacadelocal == null)
			mdfacadelocal = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdfacadelocal;
	}

	private AttributeFacadeLocal getAttributeFacade() {
		if (attrfacadelocal == null)
			attrfacadelocal = ServerServiceLocator.getInstance().getFacade(AttributeFacadeLocal.class);
		return attrfacadelocal;
	}

	private GenericObjectFacadeLocal getGenericObjectFacade() {
		if (gofacadelocal == null)
			gofacadelocal = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
		return gofacadelocal;
	}
}
