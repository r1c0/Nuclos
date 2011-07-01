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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.genericobject.MakeGenericObjectValueIdField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * generic valuelistprovider for all entities (genericobject and masterdata).
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class GenericCollectableFieldsProvider implements CollectableFieldsProvider {

	private String entity;
	private String fieldexpression;
	private boolean valid = false;

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.CollectableFieldsProvider#setParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameter(String name, Object value) {
		if ("entity".equals(name)) {
			entity = value.toString();
		}
		else if ("field".equals(name)) {
			fieldexpression = value.toString();
		}
		else if ("valid".equals(name)) {
			valid = Boolean.parseBoolean(value.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.CollectableFieldsProvider#getCollectableFields()
	 */
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		final List<CollectableField> result;
		
		if (Modules.getInstance().isModuleEntity(entity)) {
			result = getGenericObjectFields();
			Collections.sort(result);
		}
		else {
			CollectableFieldsProvider masterdataCollectableFieldsProvider = new MasterDataCollectableFieldsProvider(entity);
			masterdataCollectableFieldsProvider.setParameter("fieldName", fieldexpression);
			masterdataCollectableFieldsProvider.setParameter("_searchmode", !valid);
			result =  masterdataCollectableFieldsProvider.getCollectableFields();
		}
		
		return result;
	}

	private List<CollectableField> getGenericObjectFields() throws CommonBusinessException {
		try {
			int iModuleId = Modules.getInstance().getModuleIdByEntityName(entity);
			Set<Integer> stAttributeIds = new HashSet<Integer>();
			List<String> sAttributeNames = new ArrayList<String>();

			// case of multiple pattern attributes
			if (fieldexpression.contains("${")){
				Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w]+[}]");
			    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (fieldexpression);
			    new StringBuffer();
			      while (referencedEntityMatcher.find()) {
			    	  sAttributeNames.add(referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1));
			    	  stAttributeIds.add(AttributeCache.getInstance().getAttribute(entity, referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1)).getId());
			    }
			}
			else {
				stAttributeIds.add(AttributeCache.getInstance().getAttribute(entity, fieldexpression).getId());
			}

			List <GenericObjectWithDependantsVO> collgowdvo = GenericObjectDelegate.getInstance().getGenericObjectsWithDependants(iModuleId, new CollectableSearchExpression(), stAttributeIds, new HashSet<String>(), false, false);

			final Collection<GenericObjectWithDependantsVO> collgovoFiltered =selectValidAndActive(entity, sAttributeNames, collgowdvo);
			return CollectionUtils.transform(collgovoFiltered, new MakeGenericObjectValueIdField(AttributeCache.getInstance(), fieldexpression));
		}
		catch (RuntimeException e) {
			throw new CommonBusinessException(e);
		}
	}

	private Collection<GenericObjectWithDependantsVO> selectValidAndActive(String sEntity, Collection<String> sAttributeName, Collection<GenericObjectWithDependantsVO> collgowdvo) {
		final List<GenericObjectWithDependantsVO> result = new ArrayList<GenericObjectWithDependantsVO>();

		final Collection<String> collFieldNames = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntity).getFieldNames();
		final boolean bContainsAttribute = collFieldNames.containsAll(sAttributeName);

		for (GenericObjectWithDependantsVO gowdvo : collgowdvo) {
			// separate valid entries...
			boolean bAddToResult = true;
			if (!bContainsAttribute) {
				bAddToResult = false;
			}
			if (bAddToResult) {
				if (gowdvo.isDeleted() || gowdvo.isRemoved()) {
					bAddToResult = false;
				}
			}
			if (bAddToResult) {
				result.add(gowdvo);
			}
		}
		return result;
	}
}
