package org.nuclos.client.explorer.node.eventsupport;

import java.util.Date;

public class EventSupportTransferVO {
	private String sName;;
	private String sDescription;
	private String sClassname;
	private String sInterface;
	private String sPackage;
	private Date   dDateOfCompilation;
	
	
	public EventSupportTransferVO(String sName, String sDescription,
			String sClassname, String sInterface, String sPackage,
			Date dDateOfCompilation) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sClassname = sClassname;
		this.sInterface = sInterface;
		this.sPackage = sPackage;
		this.dDateOfCompilation = dDateOfCompilation;
	}
	
	public String getsName() {
		return sName;
	}
	public void setsName(String sName) {
		this.sName = sName;
	}
	public String getsDescription() {
		return sDescription;
	}
	public void setsDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	public String getsClassname() {
		return sClassname;
	}
	public void setsClassname(String sClassname) {
		this.sClassname = sClassname;
	}
	public String getsInterface() {
		return sInterface;
	}
	public void setsInterface(String sInterface) {
		this.sInterface = sInterface;
	}
	public String getsPackage() {
		return sPackage;
	}
	public void setsPackage(String sPackage) {
		this.sPackage = sPackage;
	}
	public Date getdDateOfCompilation() {
		return dDateOfCompilation;
	}
	public void setdDateOfCompilation(Date dDateOfCompilation) {
		this.dDateOfCompilation = dDateOfCompilation;
	}
	
	
}
