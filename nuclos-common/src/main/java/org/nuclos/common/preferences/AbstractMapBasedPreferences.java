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
package org.nuclos.common.preferences;

import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;

/**
 * Abstract base class for Map-based implementations of the preferences API.
 * For safety reasons, everything is synchronized across all instances of
 * <code>MapBasedPreferences</code>. That way nobody can change something in
 * the middle of writing to or reading from the file.
 * 
 * An implementator should must subclass {@link Root} and provide its own
 * implementation for {@link Root#flushSpi()}.
 * 
 * <br>Based on Code found in Sun's Bug Database, Bug #4788410.
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class AbstractMapBasedPreferences extends AbstractPreferences {
	
	protected static final Logger log = Logger.getLogger(AbstractMapBasedPreferences.class);

	/**
	 * the parent of this preferences node, if any
	 */
	private final AbstractMapBasedPreferences parent;

	/**
	 * the entries in this preferences node
	 */
	private final Map<String, String> mpEntries = CollectionUtils.newHashMap();

	/**
	 * the children of this preferences node
	 */
	private final Map<String, AbstractMapBasedPreferences> mpChildren = CollectionUtils.newHashMap();

	/**
	 * inner class <code>NuclosPreferences.Root</code>. root node for NuclosPreferences
	 */
	public abstract static class Root extends AbstractMapBasedPreferences {

		/**
		 * the dirty flag in case this node is a root node
		 */
		private boolean bDirty;

		protected Root() {
			super(null, "");
		}

		/**
		 * @return bDirty Has the tree changed since the last sync/flush?
		 */
		@Override
		public boolean isDirty() {
			return this.bDirty;
		}

		/**
		 * @param bDirty Has the tree changed since the last sync/flush?
		 */
		@Override
		protected void setDirty(boolean bDirty) {
			this.bDirty = bDirty;
		}

		/**
		 * flushes the whole tree
		 */
		@Override
		protected abstract void flushSpi() throws BackingStoreException;

	}	// inner class Root

	/**
	 * creates a preferences node.
	 * @param parent the parent of this node. <code>null</code> for the root node.
	 * @param sName
	 */
	private AbstractMapBasedPreferences(AbstractMapBasedPreferences parent, String sName) {
		super(parent, sName);
		this.parent = parent;
	}

	private AbstractMapBasedPreferences.Root getRoot() {
		synchronized (lock) {
			AbstractMapBasedPreferences root = this;
			while (root.parent != null) {
				root = root.parent;
			}
			return (AbstractMapBasedPreferences.Root) root;
		}
	}

	@Override
	protected void putSpi(String sKey, String sValue) {
		log.debug("putting (" + sKey + "/" + sValue + ") in preferences node " + name());

		final String sOldValue = mpEntries.get(sKey);
		if (!sValue.equals(sOldValue)) {
			mpEntries.put(sKey, sValue);
			this.setDirty(true);
		}
	}

	@Override
	protected String getSpi(String key) {
		return mpEntries.get(key);
	}

	@Override
	protected void removeSpi(String key) {
		synchronized (lock) {
			log.debug("removing the key " + key + " from preferences node" + name());
			if (mpEntries.get(key) != null) {
				mpEntries.remove(key);
				this.setDirty(true);
			}
		}
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		synchronized (lock) {
			log.debug("removing the node " + name());
			if (parent != null) {
				parent.mpChildren.remove(name());
				parent.setDirty(true);
			}
		}
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		synchronized (lock) {
			final String[] asKeys = new String[mpEntries.size()];
			return mpEntries.keySet().toArray(asKeys);
		}
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		synchronized (lock) {
			final String[] asChildrenNames = new String[mpChildren.size()];
			return mpChildren.keySet().toArray(asChildrenNames);
		}
	}

	@Override
	protected AbstractPreferences childSpi(String name) {
		synchronized (lock) {
			AbstractMapBasedPreferences result = mpChildren.get(name);
			if (result == null) {
				result = new AbstractMapBasedPreferences(this, name);
				mpChildren.put(name, result);
				setDirty(true);
			}
			return result;
		}
	}

	/**
	 * syncs the whole tree
	 */
	@Override
	public void sync() throws BackingStoreException {
		synchronized (lock) {
			this.syncSpi();
		}
	}

	/**
	 * syncs the whole tree
	 */
	@Override
	protected void syncSpi() throws BackingStoreException {
		this.flush();
	}

	/**
	 * flushes the whole tree
	 * @throws BackingStoreException
	 */
	@Override
	public void flush() throws BackingStoreException {
		synchronized (lock) {
			this.flushSpi();
		}
	}

	/**
	 * flushes the whole tree
	 */
	@Override
	protected void flushSpi() throws BackingStoreException {
		this.getRoot().flushSpi();
	}

	protected void setDirty(boolean bDirty) {
		synchronized (lock) {
			this.getRoot().setDirty(bDirty);
		}
	}

	protected boolean isDirty() {
		synchronized (lock) {
			return this.getRoot().isDirty();
		}
	}

}	// class MapBasedPreferences
