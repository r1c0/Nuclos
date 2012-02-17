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

package org.nuclos.server.livesearch.ejb3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.caching.GenCache;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common.transport.GzipList;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.SessionUtils;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Live search server bean.
 * Extension point: if - for any business reason - additional filtering is
 * is required for the search results, a pluggable module architecture exists
 * via the server-side configuration and implementations of LiveSearchAddFilter. 
 */
@Transactional
public class LiveSearchFacadeBean extends NuclosFacadeBean implements LiveSearchFacadeRemote {
	
	private SessionUtils utils;
	
	public LiveSearchFacadeBean() {
	}
	
	@Autowired
	void setSessionUtils(SessionUtils utils) {
		this.utils = utils;
	}

	/**
	 * Perform live-search for one entity and search string.
	 * Returns a list of matching entity vos along with a set of attribute
	 * names to hide from the enduser. This is convenience, as we have to check
	 * the attribute permissions anyway when searching, and thus, can simply
	 * include this data in the search result.
	 * 
	 * The "hide attributes"-set may be null
	 */
	public List<Pair<EntityObjectVO, Set<String>>> search(final String entity, String searchString) {
		JdbcEntityObjectProcessor proc = NucletDalProvider.getInstance().getEntityObjectProcessor(entity);

		MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		CollectableSearchCondition condition = null;
		Map<String, EntityFieldMetaDataVO> fields = mdProv.getAllEntityFieldsByEntity(entity);
		HashSet<String> fieldsUsedForSearch = new HashSet<String>();
		
		for(String fieldName : fields.keySet()) {
			if(canUseFieldForSearch(fields.get(fieldName))) {
				fieldsUsedForSearch.add(fieldName);
				
				CollectableSearchCondition condeq
					= SearchConditionUtils.newEOLikeComparison(
						entity, fieldName,
						"*" + searchString + "*", mdProv);
				if(condition == null)
					condition = condeq;
				else
					condition = SearchConditionUtils.or(condeq, condition);
			}
		}

		List<EntityObjectVO> dbResult;
		if(condition != null)
			dbResult = proc.getBySearchExpression(appendRecordGrants(new CollectableSearchExpression(condition), entity), true);
		else
			dbResult = Collections.emptyList();

		List<Pair<EntityObjectVO, Set<String>>> res = attributeRightsFilter(
            dbResult, entity, searchString, fieldsUsedForSearch);
		
		String addFilters = ServerParameterProvider.getInstance().getValue("livesearch.server.addfilter");
		res = applyAddFilters(res, addFilters);
		
		return new GzipList<Pair<EntityObjectVO,Set<String>>>(res);
	}


	/**
	 * Performs the post-filtering regarding state-based attribute rights,
	 * converting each EntityObjectVO into a pair of EntityObjectVO and hidden
	 * fields.
	 * 
	 * If the EntityObjectVO does not have a state, it is simply added to the
	 * result as is.
	 * 
	 * Otherwise, we fetch the state-dependent field rights, and check, whether
	 * there is a match concerning the readable fields only. If so, the entity
	 * gets added to the result along with a set of non-readable fields. If no
	 * match is found the entity gets discarded.
	 */
	private ArrayList<Pair<EntityObjectVO, Set<String>>> attributeRightsFilter(List<EntityObjectVO> dbResult, final String entity, String searchString, HashSet<String> fieldsUsedForSearch) {
	    ArrayList<Pair<EntityObjectVO, Set<String>>> res = new ArrayList<Pair<EntityObjectVO, Set<String>>>();
	    String upperSearchString = searchString.toUpperCase();
		
		GenCache<Long, Map<String, Permission>> permissionCache
		= new GenCache<Long, Map<String,Permission>>(
			new GenCache.LookupProvider<Long, Map<String,Permission>>() {
				private SecurityFacadeLocal secFacade;
				
				private SecurityFacadeLocal getFacade() {
					if(secFacade == null)
						secFacade = ServerServiceLocator.getInstance().getFacade(SecurityFacadeLocal.class);
					return secFacade;
				}
				
				@Override
				public Map<String, Permission> lookup(Long stateId) {
					return getFacade().getAttributePermissionsByEntity(entity, new Integer(stateId.intValue()));
				}});

		for(EntityObjectVO obj : dbResult) {
			Long stateId = obj.getFieldId("nuclosState");
			if(stateId != null) {
				Map<String, Permission> attributeRights = permissionCache.get(stateId);
				HashSet<String> hideAttributes = new HashSet<String>();
				boolean foundMatch = false;
				for(String fieldName : fieldsUsedForSearch) {
					Permission p = attributeRights.get(fieldName); // null = no rights at all!
					if(p != null && p.includesReading()) {
						String value = StringUtils.emptyIfNull(obj.getField(fieldName, String.class)).toUpperCase();
						if(value.indexOf(upperSearchString) >= 0)
							foundMatch = true;
					}
					else
						hideAttributes.add(fieldName);
				}
				if(foundMatch)
					res.add(new Pair<EntityObjectVO, Set<String>>(obj, hideAttributes));
			}
			else
				res.add(new Pair<EntityObjectVO, Set<String>>(obj, null));
		}
	    return res;
    }


	/**
	 * Decides, whether we use a field for searching. True for String-fields,
	 * which are not part of the versioning data.
	 */
	private boolean canUseFieldForSearch(EntityFieldMetaDataVO fieldMeta) {
		NuclosEOField eoField = NuclosEOField.getByField(fieldMeta.getField());
		if(eoField != null && !eoField.isForceValueSearch())
			return false;

		if(!fieldMeta.getDataType().equals(String.class.getName()))
			return false;

		// References sometimes come with a string-class declaration, but
		// but without a textual (and thus searchable) representation of the
		// referenced entry. Filter these.
		if(fieldMeta.getForeignEntity() != null) {
			if(fieldMeta.getDbColumn().startsWith("INTID"))
				return false;
			if(fieldMeta.getForeignEntityField() == null)
				return false;
		}
		return true;
	}
	
	
	private List<Pair<EntityObjectVO, Set<String>>> applyAddFilters(List<Pair<EntityObjectVO, Set<String>>> in, String filters) {
		if(StringUtils.nullIfEmpty(filters) == null)
			return in;
		
		String[] filterList = filters.split(", ?");
		for(String filter : filterList) {
			try {
	            Class<?> cl = Class.forName(filter.trim());
	            LiveSearchAddFilter filterImpl = (LiveSearchAddFilter) cl.newInstance();
	            in = filterImpl.applyFilter(utils.getCurrentUserName(), in);
            }
            catch(ClassNotFoundException e) {
            	throw new CommonFatalException(e);
            }
            catch(InstantiationException e) {
            	throw new CommonFatalException(e);
            }
            catch(IllegalAccessException e) {
            	throw new CommonFatalException(e);
            }
		}	
		return in;
	}
}
