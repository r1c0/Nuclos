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
package org.nuclos.client.genericobject;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.valuelistprovider.GenericObjectCollectableFieldsProviderFactory;
import org.nuclos.client.genericobject.valuelistprovider.ProcessCollectableFieldsProvider;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.CollectableComponentWithValueListProvider;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.GenericObjectMetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collection.BinaryPredicate;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.xml.sax.InputSource;

/**
 * Cache for generic object layout ids.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo check if this cache is still needed
 */
public class GenericObjectLayoutCache {
	
	private static final Logger LOG = Logger.getLogger(GenericObjectLayoutCache.class);

	private static GenericObjectLayoutCache singleton;

	private final GenericObjectMetaDataProvider gometa = GenericObjectMetaDataCache.getInstance();

	private static class Key {
		private final UsageCriteria usagecriteria;
		private final boolean bSearchScreen;

		private Key(UsageCriteria usagecriteria, boolean bSearchScreen) {
			this.usagecriteria = usagecriteria;
			this.bSearchScreen = bSearchScreen;
		}

		@Override
		public boolean equals(Object o) {
			final boolean result;
			if (this == o) {
				result = true;
			}
			else if (!(o instanceof Key)) {
				result = false;
			}
			else {
				final Key that = (Key) o;
				result = this.usagecriteria.equals(that.usagecriteria) && (this.bSearchScreen == that.bSearchScreen);
			}
			return result;
		}

		@Override
		public int hashCode() {
			return usagecriteria.hashCode() ^ Boolean.valueOf(bSearchScreen).hashCode();
		}

	}	// inner class Key

	/**
	 * Map<Key, Integer iLayoutId> caches the least recently used keys/layout ids
	 */
	private final Map<Key, Integer> mpLayoutIds;

	// This ReferenceMap does not work as expected. The soft references are freed far to early.
//		private final Map mpLayoutIds = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);

	private final LayoutMLParser parser = new LayoutMLParser();

	private GenericObjectLayoutCache() {
		 mpLayoutIds = new LRUMap(100);
	}

	public static synchronized GenericObjectLayoutCache getInstance() {
		if (singleton == null) {
			singleton = new GenericObjectLayoutCache();
		}
		return singleton;
	}
	
	/**
	 * 
	 * @param iModuleId
	 * @return
	 */
	public synchronized Collection<EntityAndFieldName> getSubFormEntities(Integer iModuleId) {
		Collection<EntityAndFieldName> result = new ArrayList<EntityAndFieldName>();
		for (Integer iLayoutId : this.gometa.getLayoutIdsByModuleId(iModuleId, false)) {
			final String sLayoutML = this.gometa.getLayoutML(iLayoutId);	
			if (sLayoutML != null) {
				try {
					result.addAll(this.parser.getSubFormEntityAndForeignKeyFieldNames(new InputSource(new StringReader(sLayoutML))));
				}
				catch(LayoutMLException e) {
					LOG.warn("getSubFormEntities failed: " + e, e);
				}
			}
		}
		return CollectionUtils.distinct(result, new BinaryPredicate<EntityAndFieldName, EntityAndFieldName>() {
			@Override
			public boolean evaluate(EntityAndFieldName t1, EntityAndFieldName t2) {
				return LangUtils.equals(t1.getEntityName(), t2.getEntityName()) &&
					LangUtils.equals(t1.getFieldName(), t2.getFieldName());
			}});
	}

	/**
	 * parses the LayoutML definition and gets the layout information
	 * @param clcte
	 * @param usagecriteria
	 * @param bSearchScreen
	 * @param actionlistener
	 * @return the LayoutRoot containing the layout information
	 * @precondition clcte != null
	 * @precondition usagecriteria != null
	 * @precondition clcte.getName().equals(Modules.getInstance().getEntityNameByModuleId(usagecriteria.getModuleId()))
	 */
	public synchronized LayoutRoot getLayout(CollectableEntity clcte, UsageCriteria usagecriteria, boolean bSearchScreen, ActionListener actionlistener, CollectableFieldsProviderCache valueListProviderCache) {
		if (clcte == null) {
			throw new NullArgumentException("clcte");
		}
		if (usagecriteria == null) {
			throw new NullArgumentException("usagecriteria");
		}
		final String sEntityName = clcte.getName();
		final Integer iModuleId = usagecriteria.getModuleId();
		
		if (!sEntityName.equals(Modules.getInstance().getEntityNameByModuleId(iModuleId))) {
			throw new IllegalArgumentException("The entity (\"" + sEntityName + "\") doesn't match the module id (" + iModuleId + ").");
		}
		
		final String sLayoutML = this.gometa.getLayoutML(this.getLayoutId(usagecriteria, bSearchScreen));	
		if(sLayoutML == null) {
			throw new NuclosFatalException(CommonLocaleDelegate.getInstance().getMessage(
					"GenericObjectLayoutCache.1", "Die Maske f\u00fcr das Modul {0} konte nicht geladen werden.\nEs wurde noch kein Layout zugewiesen.", Modules.getInstance().getEntityLabelByModuleId(iModuleId)));
		}
			
		final LayoutRoot result;
		try {
			result = this.parser.getResult(new InputSource(new StringReader(sLayoutML)), clcte,
					bSearchScreen, actionlistener, GenericObjectCollectableFieldsProviderFactory.newFactory(sEntityName, valueListProviderCache),
					CollectableComponentFactory.getInstance());
			
			for (CollectableComponent c : result.getCollectableComponentsFor(NuclosEOField.PROCESS.getName())) {
				if (c instanceof CollectableComponentWithValueListProvider) {
					CollectableComponentWithValueListProvider cvlp = (CollectableComponentWithValueListProvider)c;
					if (cvlp.getValueListProvider() instanceof ProcessCollectableFieldsProvider) {
						cvlp.getValueListProvider().setParameter(ProcessCollectableFieldsProvider.PARAM_MODULE_ID, iModuleId);
						cvlp.refreshValueList();
					}
				}
			}
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (LayoutMLException ex) {
			throw new NuclosFatalException(ex.getMessage(), ex);
			/** @todo It would be better if new NuclosFatalException(ex) would do the same. */
		}
		
		return result;
	}

	/**
	 * invalidates the cache
	 */
	public synchronized void invalidate() {
		mpLayoutIds.clear();
	}

	/**
	 * @param usagecriteria
	 * @param bSearchScreen
	 * @return database id of the best matching layout
	 */
	private synchronized int getLayoutId(UsageCriteria usagecriteria, boolean bSearchScreen) {
		final Key key = new Key(usagecriteria, bSearchScreen);
		Integer result = this.get(key);

		if (result != null) {
			LOG.debug("layout cache HIT");
		}
		else {
			LOG.debug("layout cache MISS");
			try {
				result = gometa.getBestMatchingLayoutId(usagecriteria, bSearchScreen);

				// For memory profiling, activate/deactivate the cache here:
				this.put(key, result);
			}
			catch (CommonFinderException ex) {
				final String sMessage;
				if (bSearchScreen)
					sMessage = CommonLocaleDelegate.getInstance().getMessage(
							"GenericObjectLayoutCache.3", "Ein passendes Such-Layout f\u00fcr {0} konnte nicht gefunden werden.\nEs wurde noch kein Layout zugewiesen.", usagecriteria);
				else
					sMessage = CommonLocaleDelegate.getInstance().getMessage(
							"GenericObjectLayoutCache.2", "Ein passendes Layout f\u00fcr {0} konnte nicht gefunden werden.\nEs wurde noch kein Layout zugewiesen.", usagecriteria);
				throw new NuclosFatalException(sMessage, ex);
			}
		}
		assert result != null;
		return result;
	}

	private Integer get(Key key) {
		return this.mpLayoutIds.get(key);
	}

	private void put(Key key, Integer iLayoutId) {
		this.mpLayoutIds.put(key, iLayoutId);
	}

}	// class GenericObjectLayoutCache
