package org.nuclos.server.eventsupport.valueobject;

import java.util.Date;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class ProcessVO extends NuclosValueObject {
	
	private Integer iNucletId;
	private Integer iModuleId;
	private Date	dValidUntil;
	private Date	dValidFrom;
	private String	sDescription;
	private String  sName;
	
	public ProcessVO(NuclosValueObject nvo, Integer pNucletId, Integer pModuleId, Date pValidUntil, Date pValidFrom, String pDescription, String pName) {
		super(nvo);
		
		this.iNucletId = pNucletId;
		this.iModuleId = pModuleId;
		
		this.dValidFrom = pValidFrom;
		this.dValidUntil = pValidUntil;
		this.sDescription = pDescription;
		this.sName = pName;	
	}

	
	public String getName() {
		return sName;
	}

	public Integer getNucletId() {
		return iNucletId;
	}

	public Integer getModuleId() {
		return iModuleId;
	}

	public Date getValidUntil() {
		return dValidUntil;
	}

	public Date getValidFrom() {
		return dValidFrom;
	}

	public String getDescription() {
		return sDescription;
	}
}
