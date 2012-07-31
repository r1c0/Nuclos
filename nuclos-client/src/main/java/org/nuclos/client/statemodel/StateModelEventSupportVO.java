package org.nuclos.client.statemodel;

public class StateModelEventSupportVO 
{
	private String sName;
	private String sClassname;
	private String sDescription;
	
	public StateModelEventSupportVO(String sName, String sClassname,
			String sDescription) {
		super();
		this.sName = sName;
		this.sClassname = sClassname;
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
	
	
}
