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

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.nuclos.common.NuclosImage;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.resource.valueobject.ResourceVO;

public class StateWrapper implements Comparable<StateWrapper> {
	private final Integer iId;
	private final Integer iNumeral;
	private final String sName;
	private final String sDescription;
	private final NuclosImage icon;
	private final boolean bFromAutomatic;
	private final List<Integer> lstReachable;
	private Color color;
	private final ResourceVO resButtonIcon;
	
	public StateWrapper(Integer iId, Integer iNumeral, String sName, NuclosImage icon, String sDescription, String color, ResourceVO resButtonIcon) {
		this(iId, iNumeral, sName, icon, sDescription, color, resButtonIcon, false, new LinkedList<Integer>());
	}
	public StateWrapper(Integer iId, Integer iNumeral, String sName, NuclosImage icon, String sDescription, String color, ResourceVO resButtonIcon, boolean bFromAutomatic, List<Integer> lstReachable) {
		this.iId = iId;
		this.iNumeral = iNumeral;
		this.sName = sName;
		this.icon = icon;
		this.sDescription = sDescription;
		this.bFromAutomatic = bFromAutomatic;
		this.lstReachable = lstReachable;
		this.resButtonIcon = resButtonIcon;
		if (color != null) {
			try {
				this.color = Color.decode(color);
			} catch (Exception ex) {
				// ignore
			}
		}
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
	
	public Color getColor() {
		return color;
	}

	public ResourceVO getResButtonIcon() {
		return resButtonIcon;
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
		if (that == null)
			return -1;
		return LangUtils.compare(iNumeral, that.iNumeral);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(iId) ^ LangUtils.hashCode(iNumeral) ^ LangUtils.hashCode(sName);
	}

	@Override
	public String toString() {
		return getNumeralText() + " " + (sName == null ? "N/A" : sName);
	}

	public String getStatusText() {
		return (sName == null ? "N/A" : sName);
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
