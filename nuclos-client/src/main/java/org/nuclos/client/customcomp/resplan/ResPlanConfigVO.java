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

package org.nuclos.client.customcomp.resplan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.time.LocalTime;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;

@XmlType
@XmlRootElement(name="resplan")
public class ResPlanConfigVO implements Serializable {

	@XmlAttribute(name="version", required=true)
	static final String VERSION = "0.1";

	private String resourceEntity;
	private String resourceSortField;
	private String entryEntity;
	private String referenceField;

	private String dateFromField;
	private String dateUntilField;
	private String timePeriodsString;
	private String timeFromField;
	private String timeUntilField;

	private String resourceLabelText;
	private String resourceToolTipText;
	private String entryLabelText;
	private String entryToolTipText;
	private String cornerLabelText;
	private String cornerToolTipText;

	private boolean scriptingActivated;
	private String scriptingCode;
	private String backgroundPaintMethod;
	private String scriptingResourceCellMethod;
	private String scriptingEntryCellMethod;

	private List<ResPlanResourceVO> resources;
	private String defaultViewFrom;
	private String defaultViewUntil;

	public ResPlanConfigVO() {
	}

	@XmlElement(name="resourceEntity")
	public String getResourceEntity() {
		return resourceEntity;
	}

	public void setResourceEntity(String resourceEntity) {
		this.resourceEntity = resourceEntity;
	}

	@XmlElement(name="entryEntity")
	public String getEntryEntity() {
		return entryEntity;
	}

	public void setEntryEntity(String entryEntity) {
		this.entryEntity = entryEntity;
	}

	@XmlElement(name="referenceField")
	public String getReferenceField() {
		return referenceField;
	}

	public void setResourceSortField(String resourceSortField) {
		this.resourceSortField = resourceSortField;
	}

	public String getResourceSortField() {
		return resourceSortField;
	}

	public void setReferenceField(String referenceField) {
		this.referenceField = referenceField;
	}

	@XmlElement(name="dateFromField")
	public String getDateFromField() {
		return dateFromField;
	}

	public void setDateFromField(String dateFromField) {
		this.dateFromField = dateFromField;
	}

	@XmlElement(name="dateToField")
	public String getDateUntilField() {
		return dateUntilField;
	}

	public void setDateUntilField(String dateUntilField) {
		this.dateUntilField = dateUntilField;
	}

	@XmlElement(name="timeFromField")
	public String getTimeFromField() {
		return timeFromField;
	}

	public void setTimeFromField(String timeFromField) {
		this.timeFromField = timeFromField;
	}

	@XmlElement(name="timeToField")
	public String getTimeUntilField() {
		return timeUntilField;
	}

	public void setTimeUntilField(String timeUntilField) {
		this.timeUntilField = timeUntilField;
	}

	@XmlElement(name="timePerods")
	public String getTimePeriodsString() {
		return timePeriodsString;
	}

	public void setTimePeriodsString(String timePeriodsString) {
		this.timePeriodsString = timePeriodsString;
	}

	@XmlElement(name="resourceLabelText")
	public String getResourceLabelText() {
		return resourceLabelText;
	}

	public void setResourceLabelText(String resourceLabelText) {
		this.resourceLabelText = resourceLabelText;
	}

	@XmlElement(name="resourceToolTipText")
	public String getResourceToolTipText() {
		return resourceToolTipText;
	}

	public void setResourceToolTipText(String resourceToolTipText) {
		this.resourceToolTipText = resourceToolTipText;
	}

	@XmlElement(name="entryLabelText")
	public String getEntryLabelText() {
		return entryLabelText;
	}

	public void setEntryLabelText(String entryLabelText) {
		this.entryLabelText = entryLabelText;
	}

	@XmlElement(name="entryToolTipText")
	public String getEntryToolTipText() {
		return entryToolTipText;
	}

	public void setEntryToolTipText(String entryToolTipText) {
		this.entryToolTipText = entryToolTipText;
	}

	@XmlElement(name="cornerLabelText")
	public String getCornerLabelText() {
		return cornerLabelText;
	}

	public void setCornerLabelText(String cornerLabelText) {
		this.cornerLabelText = cornerLabelText;
	}

	@XmlElement(name="cornerToolTipText")
	public void setCornerToolTipText(String cornerToolTipText) {
		this.cornerToolTipText = cornerToolTipText;
	}

	public String getCornerToolTipText() {
		return cornerToolTipText;
	}

	@XmlElement(name="scripting")
	public boolean isScriptingActivated() {
		return scriptingActivated;
	}

	public void setScriptingActivated(boolean scriptingActivated) {
		this.scriptingActivated = scriptingActivated;
	}

	@XmlElement(name="code")
	public String getScriptingCode() {
		return scriptingCode;
	}

	public void setScriptingCode(String scriptingCode) {
		this.scriptingCode = scriptingCode;
	}

	@XmlElement(name="backgroundPaintMethod")
	public String getScriptingBackgroundPaintMethod() {
		return backgroundPaintMethod;
	}

	public void setScriptingBackgroundPaintMethod(String backgroundPaintCellMethod) {
		this.backgroundPaintMethod = backgroundPaintCellMethod;
	}

	@XmlElement(name="resourceCellMethod")
	public String getScriptingResourceCellMethod() {
		return scriptingResourceCellMethod;
	}

	public void setScriptingResourceCellMethod(String scriptingResourceCellMethod) {
		this.scriptingResourceCellMethod = scriptingResourceCellMethod;
	}

	@XmlElement(name="entryCellMethod")
	public String getScriptingEntryCellMethod() {
		return scriptingEntryCellMethod;
	}

	public void setScriptingEntryCellMethod(String scriptingEntryCellMethod) {
		this.scriptingEntryCellMethod = scriptingEntryCellMethod;
	}

	@XmlElement(name="resources")
	public List<ResPlanResourceVO> getResources() {
		return resources;
	}

	public void setResources(List<ResPlanResourceVO> resources) {
		this.resources = resources;
	}
	
	@XmlElement(name="defaultViewFrom")
	public String getDefaultViewFrom() {
		return defaultViewFrom;
	}

	public void setDefaultViewFrom(String defaultViewFrom) {
		this.defaultViewFrom = defaultViewFrom;
	}

	@XmlElement(name="defaultViewUntil")
	public String getDefaultViewUntil() {
		return defaultViewUntil;
	}

	public void setDefaultViewUntil(String defaultViewUntil) {
		this.defaultViewUntil = defaultViewUntil;
	}

	public ResPlanResourceVO getResources(LocaleInfo li) {
		for (LocaleInfo locale : LocaleDelegate.getInstance().getParentChain()) {
			for (ResPlanResourceVO result : resources) {
				if (locale.localeId.equals(result.getLocaleId())) {
					return result;
				}
			}
		}
		// if no resources can be determined, return an empty resource set
		return new ResPlanResourceVO();
	}

	public List<Pair<LocalTime, LocalTime>> getParsedTimePeriods() {
		if (StringUtils.looksEmpty(getTimePeriodsString()))
			return null;
		return parseTimePeriodsString(getTimePeriodsString());
	}

	public byte[] toBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JAXB.marshal(this, out);
		return out.toByteArray();
	}

	public static List<Pair<LocalTime, LocalTime>> parseTimePeriodsString(String period) {
		List<Pair<LocalTime, LocalTime>> list = new ArrayList<Pair<LocalTime, LocalTime>>();
		if (period == null || period.trim().isEmpty())
			return list;
		for (String s : period.split("\\s*[ ,;]\\s*")) {
			String[] s2 = s.split("\\s*-\\s*", 2);
			if (s2.length != 2) {
				throw new IllegalArgumentException("Invalid time period string " + s);
			}
			list.add(Pair.makePair(LocalTime.parse(s2[0]), LocalTime.parse(s2[1])));
		}
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Empty time period string");
		}
		return list;
	}

	public static ResPlanConfigVO fromBytes(byte[] b) {
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		ResPlanConfigVO result = JAXB.unmarshal(in, ResPlanConfigVO.class);

		// transfer resources from template
		if (result.getResources() == null || result.getResources().size() == 0) {
			List<ResPlanResourceVO> resources = new ArrayList<ResPlanResourceVO>();
			Collection<LocaleInfo> locales = LocaleDelegate.getInstance().getAllLocales(false);
			for (LocaleInfo li : locales) {
				ResPlanResourceVO vo = new ResPlanResourceVO();
				vo.setLocaleId(li.localeId);
				vo.setResourceLabel(result.getResourceLabelText());
				vo.setResourceTooltip(result.getResourceToolTipText());
				vo.setBookingLabel(result.getEntryLabelText());
				vo.setBookingTooltip(result.getEntryToolTipText());
				vo.setLegendLabel(result.getCornerLabelText());
				vo.setLegendTooltip(result.getCornerToolTipText());
				resources.add(vo);
			}
			result.setResources(resources);
		}
		return result;
	}
}

