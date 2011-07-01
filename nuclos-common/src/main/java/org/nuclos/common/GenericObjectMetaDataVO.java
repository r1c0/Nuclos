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
package org.nuclos.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.LayoutUsageVO;

/**
 * Leased object meta data CVO to be transferred from server to clients.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @deprecated use EntityMetaDataVO
 */
@Deprecated
public class GenericObjectMetaDataVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final LayoutCache layoutcache;

	private final LayoutUsageCache layoutusagecache;
	
	private final Map<Integer, Set<String>> attributes;

	/**
	 * @param mpLayouts Map<Integer iLayoutId, String sLayoutML>
	 * @param colllayoutusagevo Collection<LayoutUsageVO>
	 */
	public GenericObjectMetaDataVO(Map<Integer, Set<String>> attributes, Map<Integer, String> mpLayouts, Collection<LayoutUsageVO> colllayoutusagevo) {
		// fill caches - information needed:

		// 1. attributes
		this.attributes = attributes;

		// 2. layouts
		this.layoutcache = new LayoutCache(mpLayouts);

		// 3. layoutusages
		this.layoutusagecache = new LayoutUsageCache(colllayoutusagevo);

		// 4. modules - do not need to be initialized
	}

	/**
	 * @param attrprovider
	 * @param iModuleId may be <code>null</code>
	 * @param bSearchable
	 * @return Collection<AttributeCVO>
	 * @postcondition result != null
	 */
	public Collection<AttributeCVO> getAttributeCVOsByModuleId(AttributeProvider attrprovider, Integer iModuleId, Boolean bSearchable) {
		return this.getAttributeCVOsFromNames(iModuleId, attrprovider, this.getAttributeNamesByModuleId(iModuleId, bSearchable));
	}

	/**
	 * @param iModuleId may be <code>null</code>
	 * @param bSearchable may be <code>null</code>.
	 * @return Set<String>
	 * @postcondition result != null
	 */
	public Set<String> getAttributeNamesByModuleId(Integer iModuleId, Boolean bSearchable) {
		return this.attributes.get(iModuleId);
	}

	
	/**
	 * @param iModuleId
	 * @param bSearchable
	 * @return Set<Integer>
	 */
	public Set<Integer> getLyoutIdsByModuleId(Integer iModuleId, Boolean bSearchable) {
		return getLayoutIdsByModuleIdAndSearchFlag(this.layoutusagecache, iModuleId, bSearchable);
	}
	
	/**
	 * @param attrprovider
	 * @param iLayoutId
	 * @return Collection<AttributeCVO>
	 * @postcondition result != null
	 */
//	public Collection<AttributeCVO> getAttributeCVOsByLayoutId(AttributeProvider attrprovider, int iLayoutId) {
//		return this.getAttributeCVOsFromNames(attrprovider, this.getAttributeNamesByLayoutId(iLayoutId));
//	}

	public Set<String> getAttributeNamesByLayoutId(int iLayoutId) {
		return this.layoutcache.getAttributeNames(iLayoutId);
	}

	/**
	 * @param iLayoutId
	 * @return
	 * @postcondition result != null
	 */
	public Set<String> getSubFormEntityNamesByLayoutId(int iLayoutId) {
		return this.layoutcache.getSubFormEntityNames(iLayoutId);
	}

	public Set<Integer> getAllLayoutIds() {
		return this.layoutcache.getAllLayoutIds();
	}

	/**
	 * @param iLayoutId
	 * @return
	 * @postcondition result != null
	 */
	public Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNamesByLayoutId(int iLayoutId) {
		return this.layoutcache.getSubFormEntityAndForeignKeyFieldNames(iLayoutId);
	}

	private Collection<AttributeCVO> getAttributeCVOsFromNames(Integer iEntityId, AttributeProvider attrprovider, Collection<String> collAttributeNames) {
		return CollectionUtils.transform(collAttributeNames, new AttributeProvider.GetAttributeByName(iEntityId, attrprovider));
	}

	/**
	 * @param usagecriteria
	 * @return
	 * @throws CommonFinderException
	 * @postcondition result != null
	 */
	public Set<String> getBestMatchingLayoutAttributeNames(UsageCriteria usagecriteria) throws CommonFinderException {
		final Set<String> result = this.getAttributeNamesByLayoutId(this.getBestMatchingLayoutId(usagecriteria, false));
		assert result != null;
		return result;
	}
	
	/**
	 * @param usagecriteria
	 * @return
	 * @throws CommonFinderException
	 */
	public Set<String> getBestMatchingLayoutSubformEntityNames(UsageCriteria usagecriteria) throws CommonFinderException {
		return this.getSubFormEntityNamesByLayoutId(this.getBestMatchingLayoutId(usagecriteria, false));
	}
	
	public String getBestMatchingLayoutML(UsageCriteria usagecriteria, boolean bSearchScreen) throws CommonFinderException {
		final String result = this.getLayoutML(this.getBestMatchingLayoutId(usagecriteria, bSearchScreen));
		assert result != null;
		return result;
	}

	/**
	 * @param iLayoutId
	 * @return
	 */
	public String getLayoutML(int iLayoutId) {
		return this.layoutcache.getLayoutML(iLayoutId);
	}

	public int getBestMatchingLayoutId(UsageCriteria usagecriteria, boolean bSearchScreen) throws CommonFinderException {
		final Map<UsageCriteria, Integer> mpLayoutUsages = this.layoutusagecache.getLayoutUsages(bSearchScreen);
		final UsageCriteria usagecriteriaBestMatching = UsageCriteria.getBestMatchingUsageCriteria(mpLayoutUsages.keySet(), usagecriteria);
		if (usagecriteriaBestMatching == null) {
			throw new CommonFinderException("No matching layout was found for usagecriteria " + usagecriteria + ".");
		}
		return mpLayoutUsages.get(usagecriteriaBestMatching);
	}

	/**
	 * @param iModuleId may be <code>null</code>.
	 * @return Set<String> the names of subform entities used in the module (details only) with the given id. If module id is <code>null</code>,
	 * the names of all subform entities used in any module (details only).
	 */
	public Set<String> getSubFormEntityNamesByModuleId(Integer iModuleId) {
		final Set<Integer> stLayoutIds = getLayoutIdsByModuleIdAndSearchFlag(this.layoutusagecache, iModuleId, false);
		final Set<String> result = new HashSet<String>();
		for (Integer iLayoutId : stLayoutIds) {
			result.addAll(this.getSubFormEntityNamesByLayoutId(iLayoutId));
		}
		return result;
	}

	/**
	 * Find layouts for Search or Details.
	 * @param layoutusagecache
	 * @param iModuleId may be <code>null</code>
	 * @param bSearchable
	 * @return Set<Integer iLayoutId>
	 */
	private static Set<Integer> getLayoutIdsByModuleIdAndSearchFlag(LayoutUsageCache layoutusagecache, Integer iModuleId, boolean bSearchable) {
		final Set<Integer> result = new HashSet<Integer>();
		final Map<UsageCriteria, Integer> mpLayoutUsages = layoutusagecache.getLayoutUsages(bSearchable);
		for (UsageCriteria usagecriteria : mpLayoutUsages.keySet()) {
			if (iModuleId == null || LangUtils.equals(usagecriteria.getModuleId(), iModuleId)) {
				result.add(mpLayoutUsages.get(usagecriteria));
			}
		}
		assert result != null;
		return result;
	}

	/**
	 * Layout cache.
	 */
	private static class LayoutCache implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Map<Integer iLayoutId, String sLayoutML>
		 */
		private final Map<Integer, String> mpLayoutML;

		/**
		 * Map<Integer iLayoutId, Set<String>>
		 */
		private transient Map<Integer, Set<String>> mpAttributeNames = CollectionUtils.newHashMap();

		/**
		 * Map<Integer iLayoutId, Collection<EntityAndFieldName>>
		 */
		private transient Map<Integer, Collection<EntityAndFieldName>> mpSubFormEntityAndForeignKeyFieldNames = CollectionUtils.newHashMap();

		LayoutCache(Map<Integer, String> mpLayouts) {
			this.mpLayoutML = new HashMap<Integer, String>(mpLayouts);
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			this.mpAttributeNames = new HashMap<Integer, Set<String>>();
			this.mpSubFormEntityAndForeignKeyFieldNames = new HashMap<Integer, Collection<EntityAndFieldName>>();
		}

		/**
		 * @param iLayoutId
		 * @return the LayoutML definition for the layout with the given id.
		 */
		public String getLayoutML(int iLayoutId) {
			return this.mpLayoutML.get(iLayoutId);
		}

		/**
		 * Get all layoutIDs
		 * @return Set of layout IDs
		 */
		public Set<Integer> getAllLayoutIds() {
			return this.mpLayoutML.keySet();
		}

		/**
		 * @param iLayoutId
		 * @return the names of the attributes contained in the layout with the given id.
		 * @postcondition result != null
		 */
		public synchronized Set<String> getAttributeNames(int iLayoutId) {
			Set<String> result = this.mpAttributeNames.get(iLayoutId);
			if (result == null) {
				final String sLayoutML = this.getLayoutML(iLayoutId);
				if (StringUtils.isNullOrEmpty(sLayoutML)) {
					result = new HashSet<String>();
				}
				else {
					try {
						result = new LayoutMLParser().getCollectableFieldNames(new InputSource(new StringReader(sLayoutML)));
					}
					catch (LayoutMLException ex) {
						throw new NuclosFatalException(ex);
					}
				}
				this.mpAttributeNames.put(iLayoutId, result);
			}
			assert result != null;
			return result;
		}

		/**
		 * @param iLayoutId
		 * @return
		 * @postcondition result != null
		 */
		public synchronized Set<String> getSubFormEntityNames(int iLayoutId) {
			return new HashSet<String>(CollectionUtils.transform(this.getSubFormEntityAndForeignKeyFieldNames(iLayoutId), new EntityAndFieldName.GetEntityName()));
		}

		/**
		 * @param iLayoutId
		 * @return
		 * @postcondition result != null
		 */
		public synchronized Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNames(int iLayoutId) {
			Collection<EntityAndFieldName> result = this.mpSubFormEntityAndForeignKeyFieldNames.get(iLayoutId);
			if (result == null) {
				final String sLayoutML = this.getLayoutML(iLayoutId);
				if (StringUtils.isNullOrEmpty(sLayoutML)) {
					result = new HashSet<EntityAndFieldName>();
				}
				else {
					try {
						result = new LayoutMLParser().getSubFormEntityAndForeignKeyFieldNames(new InputSource(new StringReader(sLayoutML)));
					}
					catch (LayoutMLException ex) {
						throw new NuclosFatalException(ex);
					}
				}
				this.mpSubFormEntityAndForeignKeyFieldNames.put(iLayoutId, result);
			}
			assert result != null;
			return result;
		}

	}	// inner class LayoutCache

	/**
	 * Layout usage cache.
	 */
	class LayoutUsageCache implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Map<UsageCriteria, Integer iLayoutId>
		 */
		private final Map<UsageCriteria, Integer> mpLayoutUsagesSearch = CollectionUtils.newHashMap();

		/**
		 * Map<UsageCriteria, Integer iLayoutId>
		 */
		private final Map<UsageCriteria, Integer> mpLayoutUsagesDetails = CollectionUtils.newHashMap();

		LayoutUsageCache(Collection<LayoutUsageVO> colllayoutusagevo) {
			for (LayoutUsageVO layoutusagevo : colllayoutusagevo) {
				this.getLayoutUsages(layoutusagevo.isSearchScreen()).put(layoutusagevo.getUsageCriteria(), layoutusagevo.getLayoutId());
			}
		}

		public Map<UsageCriteria, Integer> getLayoutUsages(boolean bSearchScreen) {
			return (bSearchScreen ? mpLayoutUsagesSearch : mpLayoutUsagesDetails);
		}
	}	// inner class LayoutUsageCache

}	// class GenericObjectMetaDataVO
