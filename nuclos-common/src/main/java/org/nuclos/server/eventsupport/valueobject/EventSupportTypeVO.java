package org.nuclos.server.eventsupport.valueobject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class EventSupportTypeVO extends NuclosValueObject {
	
	private String sName;
	private String sDescription;
	private String sClassname;
	private List<String> lstMethods;
	private List<String> lstImports;
	private String sPackage;
	private Date   dDateOfCompilation;
	
	
	
	public EventSupportTypeVO(String sName, String sDescription,
			String sClassname, List<String> lstMethods, List<String> lstImports, String sPackage,
			Date dDateOfCompilation) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;
		this.sClassname = sClassname;
		this.lstMethods = lstMethods;
		this.lstImports = lstImports;
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
	public List<String> getMethods() {
		return lstMethods;
	}
	public void setMethods(List<String> lstMethods) {
		this.lstMethods = lstMethods;
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
	public List<String> getImports() {
		return lstImports;
	}
	public void setImports(List<String> lstImports) {
		this.lstImports = lstImports;
	}
	
}
