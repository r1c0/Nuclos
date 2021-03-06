package org.nuclos.server.eventsupport.valueobject;

import java.util.Date;
import java.util.List;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportSourceVO extends NuclosValueObject {

	private String sName;
	private String sDescription;
	private String sClassname;
	private List<String> sInterface;
	private String sPackage;
	private Date   dDateOfCompilation;
	
	public EventSupportSourceVO(String sName, String sDescription, String sClassname, List<String> sInterface, String sPackage, Date dDateOfCompilation) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sClassname = sClassname;
		this.sInterface = sInterface;
		this.sPackage = sPackage;
		this.dDateOfCompilation = dDateOfCompilation;
	}
	
	public String getName() {
		return sName;
	}
	public void setName(String sName) {
		this.sName = sName;
	}
	public String getDescription() {
		return sDescription;
	}
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	public String getClassname() {
		return sClassname;
	}
	public void setClassname(String sClassname) {
		this.sClassname = sClassname;
	}
	public List<String> getInterface() {
		return sInterface;
	}
	public void setInterface(List<String> cInterface) {
		this.sInterface = cInterface;
	}

	private String getEventSupportVO()
	{
		return this.getClassname();
	}
	
	public String getPackage() {
		return sPackage;
	}

	public void setPackage(String sPackage) {
		this.sPackage = sPackage;
	}

	
	public Date getDateOfCompilation() {
		return dDateOfCompilation;
	}

	public void setDateOfCompilation(Date dDateOfCompilation) {
		this.dDateOfCompilation = dDateOfCompilation;
	}

	/**
	 * validity checker
	 */
	@Override
	public void validate() throws CommonValidationException {
		
		if (StringUtils.isNullOrEmpty(this.getName())) {
			throw new CommonValidationException("eventsupport.error.validation.eventsupport.name");
		}
		if (StringUtils.isNullOrEmpty(this.getDescription())) {
			throw new CommonValidationException("eventsupport.error.validation.eventsupport.description");
		}
		if (StringUtils.isNullOrEmpty(this.getClassname())) {
			throw new CommonValidationException("ruleengine.error.validation.eventsupport.classname");
		}
	}

	@Override
	public int hashCode() {
		return (getEventSupportVO() != null ? getEventSupportVO().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof EventSupportSourceVO) {
			final EventSupportSourceVO that = (EventSupportSourceVO) o;
			// eventsupport objects are equal if there names are equal
			return getEventSupportVO().equals(that.getEventSupportVO());
		}
		return false;
	}
	
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("EventSupportVO[");
		result.append("id=").append(getId());
		result.append("name=").append(getName());
		if (getClassname() != null) {
			result.append(",classname=").append(getClassname());
		}
		if (getDescription() != null) {
			result.append(",description=").append(getDescription() != null);
		}
	
		result.append("]");
		return result.toString();
	}
}
