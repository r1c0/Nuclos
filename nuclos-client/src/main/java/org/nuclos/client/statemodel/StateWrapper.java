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
package org.nuclos.client.statemodel;

import java.util.LinkedList;
import java.util.List;

import org.nuclos.common.NuclosImage;
import org.nuclos.common2.LangUtils;

public class StateWrapper implements Comparable<StateWrapper> {
	private final Integer iId;
	private final Integer iNumeral;
	private final String sName;
	private final String sDescription;
	private final NuclosImage icon;
	private final boolean bFromAutomatic;
	private final List<Integer> lstReachable;
	
	public StateWrapper(Integer iId, Integer iNumeral, String sName, NuclosImage icon, String sDescription) {
		this(iId, iNumeral, sName, icon, sDescription, false, new LinkedList<Integer>());
	}
	public StateWrapper(Integer iId, Integer iNumeral, String sName, NuclosImage icon, String sDescription, boolean bFromAutomatic, List<Integer> lstReachable) {
		this.iId = iId;
		this.iNumeral = iNumeral;
		this.sName = sName;
		this.icon = icon;
		this.sDescription = sDescription;
		this.bFromAutomatic = bFromAutomatic;
		this.lstReachable = lstReachable;
	}

	public Integer getId() {
		return iId;
	}

	public Integer getNumeral() {
		return iNumeral;
	}

	public String getName() {
		return sName;
	}

	public String getDescription() {
		return sDescription;
	}

	public NuclosImage getIcon() {
		return icon;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		final StateWrapper that = (StateWrapper) o;
		return LangUtils.equals(iId, that.iId) && LangUtils.equals(iNumeral, that.iNumeral) && LangUtils.equals(sName, that.sName);
	}

	@Override
	public int compareTo(StateWrapper that) {
		return iNumeral.compareTo(that.iNumeral);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(iId) ^ LangUtils.hashCode(iNumeral) ^ LangUtils.hashCode(sName);
	}

	@Override
	public String toString() {
		return getNumeralText() + " " + (sName == null ? "N/A" : sName);
	}

	public String getCombinedStatusText() {
		return getNumeralText() + " (" + (sName == null ? "N/A" : sName) + ")";
	}

	private String getNumeralText() {
		return iNumeral == null ? "N/A" : iNumeral.toString();
	}
	
	public boolean isFromAutomatic() {
		return bFromAutomatic;
	}
	
	public boolean isReachable() {
		return lstReachable.size() != 0;
	}
	
	public List<Integer> getStatesBefore() {
		return lstReachable;
	}

}	// class StateWrapper
