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
package org.nuclos.server.common.ejb3;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.TranslationVO;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Remote
public interface LocaleFacadeRemote {
	
	void flushInternalCaches();

	/**
	 * Return the complete resource bundle for a given locale
	 * @param localeInfo  the locale info
	 * @return the resulting resource bundle
	 * @throws CommonFatalException
	 */
	HashResourceBundle getResourceBundle(LocaleInfo localeInfo)
		throws CommonFatalException;

	/**
	 * Determine the default locale
	 * @return the default locale
	 * @throws CommonFatalException
	 */
	LocaleInfo getDefaultLocale() throws CommonFatalException;

	/**
	 * Return an overview of all defined locales
	 * @param includeNull  true, to include the null-locale, false to filter
	 * @return the locales
	 * @throws CommonFatalException
	 */
	Collection<LocaleInfo> getAllLocales(boolean includeNull)
		throws CommonFatalException;

	String getResourceById(LocaleInfo localeInfo, String sresourceId);

	Map<String, String> getAllResourcesById(String resourceId);

	Object modify(MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws NuclosBusinessRuleException,
		CommonCreateException, CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException;

	void deleteResource(String resourceId);

	void deleteResourceFromLocale(String resourceId, LocaleInfo localeInfo);

	/**
	 * get resource by the given id
	 */
	String getResource(String resId);

	/**
	 * get resources by the given id
	 */
	Collection<MasterDataVO> getResourcesAsVO(
		Collection<String> coll, LocaleInfo locale);

	String setResourceForLocale(String sResourceId, LocaleInfo localeInfo, String sText);

	String createResource(String sText);

	void updateResource(String resourceId, String text);

	/**
	 * Return a specific locale
	 * @return the locale
	 * @throws CommonFatalException
	 */
	MasterDataVO getLocaleVO(LocaleInfo localeInfo);

	Collection<MasterDataVO> getLocaleResourcesForParent(LocaleInfo localeInfo);

	LocaleInfo getBestLocale(LocaleInfo localeInfo);

	List<LocaleInfo> getParentChain(LocaleInfo localeInfo);

	Date getLastChange();
	
	List<TranslationVO> getResources(String entity, Integer id) throws CommonBusinessException;
}
