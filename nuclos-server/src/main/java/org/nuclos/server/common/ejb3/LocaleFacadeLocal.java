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

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.nuclos.common.HashResourceBundle;
import org.nuclos.common.TranslationVO;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

// @Local
public interface LocaleFacadeLocal {

	void flushInternalCaches();

	/**
	 * Fetch the user locale (server-internal)
	 *
	 * @return the user locale (or -1 if not set)
	 */
	LocaleInfo getUserLocale();

	/**
	 * Return the complete resource bundle for a given localeId
	 * @param localeId  the locale id
	 * @return the resulting resource bundle
	 * @throws CommonFatalException
	 */
	HashResourceBundle getResourceBundle(LocaleInfo localeInfo) throws CommonFatalException;

	Collection<LocaleInfo> getAllLocales(boolean includeNull) throws CommonFatalException;

	DateFormat getDateFormat();

	String getResourceById(LocaleInfo localeInfo, String sresourceId);

	void deleteResource(String resourceId);

	void deleteResourceFromLocale(String resourceId, LocaleInfo localeInfo);

	/**
	 * get resource by the given id
	 */
	String getResource(String iId);

	String setResourceForLocale(String sResourceId, LocaleInfo localeInfo, String sText);

	String setDefaultResource(String sResourceId, String stext);

	String createResource(String sText);

	void updateResource(String resourceId, String text);

	/**
	 * Determine the default locale
	 * @return the default locale
	 * @throws CommonFatalException
	 */
	LocaleInfo getDefaultLocale() throws CommonFatalException;

	Date getLastChange();

	MasterDataVO getLocaleVO(LocaleInfo localeInfo);

	List<LocaleInfo> getParentChain(LocaleInfo localeInfo);

	void update(String resourceId, LocaleInfo localeInfo, String text);

	String insert(String sResourceId, LocaleInfo localeInfo, String sText);

	boolean isResourceId(String s);
	
	void setResources(String entity, MasterDataVO md);
}
