package org.nuclos.client.statemodel;

public class StateModelEventSupportVO 
{
	private String sName;
	private String sClassname;
	private String sClasstype;
	private String sDescription;
	
	public StateModelEventSupportVO(String sName, String sClassname, String sClassType,
			String sDescription) {
		super();
		this.sName = sName;
		this.sClassname = sClassname;
		this.sClasstype = sClassType;
		this.sDescription = sDescription;
	}
	
	public String getName() {
		return sName;
	}
	public void setName(String sName) {
		this.sName = sName;
	}
	public String getClassname() {
		return sClassname;
	}
	public void setClassname(String sClassname) {
		this.sClassname = sClassname;
	}
	public String getDescription() {
		return sDescription;
	}
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	public String getClasstype() {
		return sClasstype;
	}
	public void setClasstype(String sClasstype) {
		this.sClasstype = sClasstype;
	}
}
