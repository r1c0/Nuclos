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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="resource")
public class ResPlanResourceVO implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String LOCALE = "locale";
	public static final String RESOURCE_L = "resource.label";
	public static final String RESOURCE_TT = "resource.looltip";
	public static final String BOOKING_L = "booking.label";
	public static final String BOOKING_TT = "booking.tooltip";
	public static final String LEGEND_L = "legend.label";
	public static final String LEGEND_TT = "legend.tooltip";

	@XmlAttribute(name = "version", required = true)
	static final String VERSION = "0.1";

	private Integer localeId;
	private String localeLabel;

	private String resourceLabel;
	private String resourceTooltip;

	private String bookingLabel;
	private String bookingTooltip;

	private String legendLabel;
	private String legendTooltip;

	@XmlElement(name=LOCALE)
	public Integer getLocaleId() {
		return localeId;
	}

	public void setLocaleId(Integer localeId) {
		this.localeId = localeId;
	}

	public String getLocaleLabel() {
		return localeLabel;
	}

	public void setLocaleLabel(String localeLabel) {
		this.localeLabel = localeLabel;
	}

	@XmlElement(name=RESOURCE_L)
	public String getResourceLabel() {
		return resourceLabel;
	}

	public void setResourceLabel(String resoureLabel) {
		this.resourceLabel = resoureLabel;
	}

	@XmlElement(name=RESOURCE_TT)
	public String getResourceTooltip() {
		return resourceTooltip;
	}

	public void setResourceTooltip(String resourceTooltip) {
		this.resourceTooltip = resourceTooltip;
	}

	@XmlElement(name=BOOKING_L)
	public String getBookingLabel() {
		return bookingLabel;
	}

	public void setBookingLabel(String bookingLabel) {
		this.bookingLabel = bookingLabel;
	}

	@XmlElement(name=BOOKING_TT)
	public String getBookingTooltip() {
		return bookingTooltip;
	}

	public void setBookingTooltip(String bookingTooltip) {
		this.bookingTooltip = bookingTooltip;
	}

	@XmlElement(name=LEGEND_L)
	public String getLegendLabel() {
		return legendLabel;
	}

	public void setLegendLabel(String legendLabel) {
		this.legendLabel = legendLabel;
	}

	@XmlElement(name=LEGEND_TT)
	public String getLegendTooltip() {
		return legendTooltip;
	}

	public void setLegendTooltip(String legendTooltip) {
		this.legendTooltip = legendTooltip;
	}
}
