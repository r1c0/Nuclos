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

package org.nuclos.client.customcomp;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.TranslationVO;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.customcomp.ejb3.CustomComponentFacadeRemote;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

public class CustomComponentDelegate {

	private static final Logger LOG = Logger.getLogger(CustomComponentDelegate.class);

	private static CustomComponentDelegate singleton;

	public static synchronized CustomComponentDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new CustomComponentDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}

	private final CustomComponentFacadeRemote facade;

	private CustomComponentDelegate() {
		this.facade = ServiceLocator.getInstance().getFacade(CustomComponentFacadeRemote.class);
	}

	public List<CustomComponentVO> getAll() {
		return facade.getAll();
	}

	public void remove(CustomComponentVO vo) throws CommonBusinessException {
		facade.remove(vo);
	}

	public void create(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException {
		facade.create(vo, translations);
	}

	public void modify(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException {
		facade.modify(vo, translations);
	}

	public List<TranslationVO> getTranslations(Integer ccid) throws CommonBusinessException {
		return facade.getTranslations(ccid);
	}

	private static void storeAll(List<CustomComponentVO> all) {
		try {
			PreferencesUtils.putSerializableListXML(getPreferences(), "_all2", all);
			getPreferences().flush();
		} catch (PreferencesException ex) {
			throw new NuclosFatalException(ex);
		} catch (BackingStoreException e) {
			LOG.warn("storeAll failed: " + e, e);
		}
	}

	private static Preferences getPreferences() {
		return Preferences.userRoot().node("customcomponent3");
	}
}
