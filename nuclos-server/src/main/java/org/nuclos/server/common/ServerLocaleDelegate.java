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
/*
 * Created on 25.05.2009
 */
package org.nuclos.server.common;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerLocaleDelegate implements SpringLocaleDelegate.LookupService {
	
	private static ServerLocaleDelegate INSTANCE;
	
	//

	private LocaleFacadeLocal localeFacade;
	
	ServerLocaleDelegate() {
		INSTANCE = this;
	}

	@Autowired
	final void setService(LocaleFacadeLocal service) {
		this.localeFacade = service;
	}

	public static ServerLocaleDelegate getInstance() {
		return INSTANCE;
	}

	@Override
	public Locale getLocale() {
		return getLocaleInfo().toLocale();
	}

	@Override
	public LocaleInfo getLocaleInfo() {
		return localeFacade.getUserLocale();
	}

	@Override
	public boolean isResource(String key) {
		LocaleInfo userLocale = getLocaleInfo();
		if(userLocale != null) {
			ResourceBundle bndl = getResourceBundle();
			return bndl.containsKey(key);
		}
		else {
			return false;
		}
	}

	@Override
	public String getResource(String key) {
		try {
			return getResourceBundle().getString(key);
		}
		catch (MissingResourceException ex) {
			return key;
		}
	}

	@Override
	public NumberFormat getNumberFormat() {
		if (getLocaleInfo() != null) {
			MasterDataVO lmd = localeFacade.getLocaleVO(getLocaleInfo());
			return new DecimalFormat(lmd.getField("numberformat", String.class), new DecimalFormatSymbols(getLocaleInfo().toLocale()));
		}
		else {
			return NumberFormat.getNumberInstance();
		}
	}

	@Override
	public DateFormat getDateFormat() {
		if (getLocaleInfo() != null) {
			MasterDataVO lmd = localeFacade.getLocaleVO(getLocaleInfo());
			return new SimpleDateFormat(lmd.getField("dateformat", String.class), getLocaleInfo().toLocale());
		}
		else {
			return SimpleDateFormat.getDateInstance();
		}
	}

	@Override
	public DateFormat getTimeFormat() {
		if (getLocaleInfo() != null) {
			MasterDataVO lmd = localeFacade.getLocaleVO(getLocaleInfo());
			return new SimpleDateFormat(lmd.getField("timeformat", String.class), getLocaleInfo().toLocale());
		}
		else {
			return SimpleDateFormat.getTimeInstance();
		}
	}

	@Override
	public DateFormat getDateTimeFormat() {
		if (getLocaleInfo() != null) {
			MasterDataVO lmd = localeFacade.getLocaleVO(getLocaleInfo());
			return new SimpleDateFormat(lmd.getField("dateformat", String.class) + " " + lmd.getField("timeformat", String.class), getLocaleInfo().toLocale());
		}
		else {
			return SimpleDateFormat.getDateTimeInstance();
		}
	}

	@Override
	public List<LocaleInfo> getParentChain() {
		return localeFacade.getParentChain(getLocaleInfo());
	}

	@Override
	public Date getLastChange() {
		return localeFacade.getLastChange();
	}

	private ResourceBundle getResourceBundle() {
		if (getLocaleInfo() != null) {
			return localeFacade.getResourceBundle(getLocaleInfo());
		}
		else {
			return localeFacade.getResourceBundle(localeFacade.getDefaultLocale());
		}
	}

	@Override
	public String getResourceById(LocaleInfo li, String key) {
		return localeFacade.getResourceById(li, key);
	}
}
