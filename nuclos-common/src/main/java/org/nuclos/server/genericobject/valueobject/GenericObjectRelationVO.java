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
package org.nuclos.server.genericobject.valueobject;

import java.util.Date;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class GenericObjectRelationVO extends NuclosValueObject {

	private Integer sourceGOId;
	private Integer destinationGOId;
	private String relationType;
	private Date validFrom;
	private Date validUntil;
	private String description;

	public GenericObjectRelationVO(NuclosValueObject evo, Integer sourceGOId, Integer destinationGOId, String relationType,
			Date validFrom, Date validUntil, String description) {
		super(evo);
		this.sourceGOId = sourceGOId;
		this.destinationGOId = destinationGOId;
		this.relationType = relationType;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.description = description;
	}

	public Integer getSourceGOId() {
		return sourceGOId;
	}

	public void setSourceGOId(Integer sourceGOId) {
		this.sourceGOId = sourceGOId;
	}

	public Integer getDestinationGOId() {
		return destinationGOId;
	}

	public void setDestinationGOId(Integer destinationGOId) {
		this.destinationGOId = destinationGOId;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
