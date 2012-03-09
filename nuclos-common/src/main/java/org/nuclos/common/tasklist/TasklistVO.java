package org.nuclos.common.tasklist;

import org.nuclos.server.common.valueobject.NuclosValueObject;

public class TasklistVO extends NuclosValueObject {

	private String name;
	private String description;
	private String labelResourceId;
	private String descriptionResourceId;
	private String menupathResourceId;
	private Integer dynamicTasklistId;
	private Integer dynamicTasklistIdFieldname;
	private Integer dynamicTasklistEntityFieldname;

	public TasklistVO(NuclosValueObject that) {
		super(that);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabelResourceId() {
		return labelResourceId;
	}

	public void setLabelResourceId(String labelResourceId) {
		this.labelResourceId = labelResourceId;
	}

	public String getDescriptionResourceId() {
		return descriptionResourceId;
	}

	public void setDescriptionResourceId(String descriptionResourceId) {
		this.descriptionResourceId = descriptionResourceId;
	}

	public String getMenupathResourceId() {
		return menupathResourceId;
	}

	public void setMenupathResourceId(String menupathResourceId) {
		this.menupathResourceId = menupathResourceId;
	}

	public Integer getDynamicTasklistId() {
		return dynamicTasklistId;
	}

	public void setDynamicTasklistId(Integer dynamicTasklistId) {
		this.dynamicTasklistId = dynamicTasklistId;
	}

	public Integer getDynamicTasklistIdFieldname() {
		return dynamicTasklistIdFieldname;
	}

	public void setDynamicTasklistIdFieldname(Integer dynamicTasklistIdFieldname) {
		this.dynamicTasklistIdFieldname = dynamicTasklistIdFieldname;
	}

	public Integer getDynamicTasklistEntityFieldname() {
		return dynamicTasklistEntityFieldname;
	}

	public void setDynamicTasklistEntityFieldname(Integer dynamicTasklistEntityFieldname) {
		this.dynamicTasklistEntityFieldname = dynamicTasklistEntityFieldname;
	}

}
