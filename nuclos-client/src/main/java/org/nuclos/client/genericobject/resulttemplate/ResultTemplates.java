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
package org.nuclos.client.genericobject.resulttemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosFatalException;

/**
 * A set of search result templates.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 01.00.00
 */
public class ResultTemplates {

	static final Logger log = Logger.getLogger(ResultTemplates.class);

	private static final String PREFS_NODE_MODULESEARCHRESULTTEMPLATES = "searchResultTemplates";

	/**
	 * @return the search result templates for the module with the given id.
	 */
	public static SearchResultTemplates templatesForModule(Integer iModuleId) {
		return new SearchResultTemplates(iModuleId);
	}

	/**
	 * Set of module specific search result templates (for a given module).
	 */
	public static class SearchResultTemplates {
		private final Integer iModuleId;
	
		/**
		 * creates a set of search result templates for the module with the given id.
		 * @param iModuleId
		 */
		public SearchResultTemplates(Integer iModuleId) {
			this.iModuleId = iModuleId;
		}
	
		/**
		 * @return the preferences node where module specific search result templates are stored.
		 * @postcondition result != null
		 */
		Preferences getPreferences() {
			return ClientPreferences.getUserPreferences().node(PREFS_NODE_MODULESEARCHRESULTTEMPLATES);
		}
	
		/**
		 * reads the search result template with the given name from the preferences.
		 * @param sTemplateName
		 * @return
		 * @throws NoSuchElementException if there is no result template with the given name.
		 * @postcondition result != null
		 */
		public SearchResultTemplate get(String sTemplateName) throws PreferencesException {
			return SearchResultTemplate.get(this.getPreferences(), sTemplateName);
		}
	
		/**
		 * @return the module specific search result templates for the currently logged-in user and the module specified in the constructor.
		 * @postcondition result != null
		 */
		public List<SearchResultTemplate> getAll() throws PreferencesException {
			final List<SearchResultTemplate> result = new ArrayList<SearchResultTemplate>();
	
			for (String sTemplateName : this.getTemplateNames()) {
				try {
					final SearchResultTemplate template = this.get(sTemplateName);
					if (LangUtils.equals(this.iModuleId, template.getModuleId())) {
						result.add(template);
					}
				}
				catch (NuclosAttributeNotFoundException ex) {
					// ignore "attribute not found", just log for now:
					log.error(ex.getMessage());
					/** @todo report this to the user: + parameter bThrowAttributeNotFound */
				}
			}
			assert result != null;
			return result;
		}
	
		/**
		 * @return the names of all search result templates.
		 * @throws PreferencesException
		 */
		List<String> getTemplateNames() throws PreferencesException {
			final List<String> result;
			try {
				final Transformer<String, String> decodeDecodeTemplateName = new Transformer<String, String>() {
					@Override
					public String transform(String sEncodedTemplateName) {
						return SearchResultTemplate.decoded(sEncodedTemplateName);
					}
				};
				result = CollectionUtils.transform(Arrays.asList(getPreferences().childrenNames()), decodeDecodeTemplateName);
			}
			catch (BackingStoreException ex) {
				throw new PreferencesException(ex);
			}
			return result;
		}
		
		/**
		 * @return a new default search result template for this set of search result templates.
		 * @postcondition result != null
		 */
		public SearchResultTemplate newDefaultTemplate() {
			final SearchResultTemplate result = SearchResultTemplate.newDefaultTemplate(iModuleId);
			return result;
		}
	
		/**
		 * @param sTemplateName
		 * @return Is the given template name already used for a personal search result template?
		 */
		public boolean contains(String sTemplateName) {
			try {
				return getPreferences().nodeExists(SearchResultTemplate.encoded(sTemplateName));
			}
			catch (BackingStoreException ex) {
				throw new NuclosFatalException(ex);
			}
		}
	
		/**
		 * stores the given search result template in the preferences.
		 * @param template
		 * @throws IllegalArgumentException if the template (name) is empty or invalid
		 */
		public void put(SearchResultTemplate template) throws PreferencesException {
			template.put(this.getPreferences());
			this.fireChangedEvent();
		}
	
		/**
		 * removes the template with the given name from the personal search result templates
		 * @param sTemplateName
		 * @todo refactor: SearchResultTemplate.remove()
		 */
		public void remove(String sTemplateName) {
			final Preferences prefs = this.getPreferences().node(SearchResultTemplate.encoded(sTemplateName));
			try {
				prefs.removeNode();
				/** @todo prefs.flush */
				this.fireChangedEvent();
			}
			catch (BackingStoreException ex) {
				throw new NuclosFatalException(ex);
			}
		}
	
		private final List<ChangeListener> lstChangeListeners = new LinkedList<ChangeListener>();
	
		/**
		 * adds the given change listener.
		 * @param cl is notified when the search result templates have changed.
		 */
		public synchronized void addChangeListener(ChangeListener cl) {
			this.lstChangeListeners.add(cl);
		}
	
		/**
		 * removes the given change listener.
		 * @param cl
		 */
		public synchronized void removeChangeListener(ChangeListener cl) {
			this.lstChangeListeners.remove(cl);
		}
	
		/**
		 * notifies the change listeners that the search result templates have changed.
		 */
		private synchronized void fireChangedEvent() {
			for (ChangeListener cl : this.lstChangeListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
		
	}	// inner class SearchResultTemplates
}
