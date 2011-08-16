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

import javax.ejb.CreateException;

import org.nuclos.common.collection.Pair;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.UpdateJobs;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
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
 */

public class MakeAttributesConsistent implements UpdateJobs{

	public static final String sRelease = "Nucleus Release 2.1.2";
	private boolean isSuccessfulExecuted = true;

	private MasterDataFacadeLocal mdfacadelocal;
	private GenericObjectFacadeLocal gofacadelocal;
	private AttributeFacadeLocal attrfacadelocal;

	final AttributeCache attrprovider = AttributeCache.getInstance();

	@Override
	public boolean execute() {
		logger.debug("START executing MakeAttributesConsistent");

		makeAttributesConsistent();
		makeAttributeValuesConsistent();

		logger.debug("END executing MakeAttributesConsistent");

		return isSuccessfulExecuted;
	}

	private void makeAttributesConsistent() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_ATTRIBUTE").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.baseColumn("INTID_EXTERNAL", Integer.class));
		query.distinct(true);
		
		// get all masterdata entities
		for (final MasterDataMetaVO mdmvo : MasterDataMetaCache.getInstance().getAllMetaData()) {
			// get all attributes which have a reference to the current masterdata entity
			for (final AttributeCVO attrcvo : AttributeCache.getInstance().getReferencingAttributes(mdmvo.getEntityName())) {
				// check whether attribute is used in a genericobject
				try {
					query.where(builder.equal(t.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), attrcvo.getId()));
					List<Integer> intidExternals = DataBaseHelper.getDbAccess().executeQuery(query);
					
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

							// make attributes consistent
							getAttributeFacade().makeConsistent(attrcvo.getExternalEntity(), intid_external, new Pair<String, String>(sExternalEntityFieldName, sValue_new));
						}
						catch (CommonPermissionException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
						catch (CommonFinderException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
						catch (CreateException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
					}
				}
				catch (DbException ex) {
					isSuccessfulExecuted = false;
					logger.error(ex, ex.getCause());
				}
			}
		}

		// get all module entities
		for (final MasterDataVO mdvo : Modules.getInstance().getModules(false)) {
			// get all attributes which have a reference to the current module entity
			for (final AttributeCVO attrcvo : AttributeCache.getInstance().getReferencingAttributes(mdvo.getField("entity").toString())) {
				// check whether attribute is used in a genericobject
				try {
					query.where(builder.equal(t.baseColumn("INTID_T_MD_ATTRIBUTE", Integer.class), attrcvo.getId()));
					List<Integer> intidExternals = DataBaseHelper.getDbAccess().executeQuery(query);
					
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

							// make attributes consistent
							getAttributeFacade().makeConsistent(attrcvo.getExternalEntity(), intid_external, new Pair<String, String>(sExternalEntityFieldName, sValue_new));
						}
						catch (CommonPermissionException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
						catch (CommonFinderException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
						catch (CreateException ex) {
							isSuccessfulExecuted = false;
							logger.error(ex, ex.getCause());
						}
					}
				}
				catch (DbException ex) {
					isSuccessfulExecuted = false;
					logger.error(ex, ex.getCause());
				}
			}
		}
	}

	private void makeAttributeValuesConsistent() {
		// get all attributes
		for (AttributeCVO attrcvo : AttributeCache.getInstance().getAttributes()) {
			// gett all attribute values
			for (AttributeValueVO attrvaluevo : attrcvo.getValues()) {
				try {
					// make attributes consistent
					final String sValue_new = (attrcvo.isShowMnemonic()) ? attrvaluevo.getMnemonic() : attrvaluevo.getValue();
					getAttributeFacade().makeConsistent(attrcvo.getId(), attrvaluevo.getId(), sValue_new);
				}
				catch (CommonFinderException ex) {
					isSuccessfulExecuted = false;
					logger.error(ex, ex.getCause());
				}
				catch (CreateException ex) {
					isSuccessfulExecuted = false;
					logger.error(ex, ex.getCause());
				}
			}
		}
	}

	private MasterDataFacadeLocal getMasterDataFacade() throws CreateException {
		if (mdfacadelocal == null)
			mdfacadelocal = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		return mdfacadelocal;
	}

	private AttributeFacadeLocal getAttributeFacade() throws CreateException {
		if (attrfacadelocal == null)
			attrfacadelocal = ServiceLocator.getInstance().getFacade(AttributeFacadeLocal.class);
		return attrfacadelocal;
	}

	private GenericObjectFacadeLocal getGenericObjectFacade() throws CreateException {
		if (gofacadelocal == null)
			gofacadelocal = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
		return gofacadelocal;
	}
}
