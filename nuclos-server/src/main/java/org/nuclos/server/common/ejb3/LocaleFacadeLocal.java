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

import javax.ejb.Local;

import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.HashResourceBundle;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

@Local
public interface LocaleFacadeLocal {

	public abstract void flushInternalCaches();

	/**
	 * Fetch the user locale (server-internal)
	 *
	 * @return the user locale (or -1 if not set)
	 */
	public abstract LocaleInfo getUserLocale();

	/**
	 * Return the complete resource bundle for a given localeId
	 * @param localeId  the locale id
	 * @return the resulting resource bundle
	 * @throws CommonFatalException
	 */
	public abstract HashResourceBundle getResourceBundle(LocaleInfo localeInfo) throws CommonFatalException;

	public abstract Collection<LocaleInfo> getAllLocales(boolean includeNull)
	throws CommonFatalException;

	public abstract DateFormat getDateFormat();

	public abstract String getResourceById(LocaleInfo localeInfo, String sresourceId);

	public abstract void deleteResource(String resourceId);

	/**
	 * get resource by the given id
	 */
	public abstract String getResource(String iId);

	public abstract String setResourceForLocale(String sResourceId, LocaleInfo localeInfo, String sText);

	public abstract String setDefaultResource(String sResourceId, String stext);

	public abstract String createResource(String sText);

	public abstract void updateResource(String resourceId, String text);

	/**
	 * Determine the default locale
	 * @return the default locale
	 * @throws CommonFatalException
	 */
	public abstract LocaleInfo getDefaultLocale() throws CommonFatalException;
	
	public Date getLastChange();

	public abstract MasterDataVO getLocaleVO(LocaleInfo localeInfo);

	public abstract List<LocaleInfo> getParentChain(LocaleInfo localeInfo);
	
	public void update(String resourceId, LocaleInfo localeInfo, String text);
	
	public String insert(String sResourceId, LocaleInfo localeInfo, String sText);
}
