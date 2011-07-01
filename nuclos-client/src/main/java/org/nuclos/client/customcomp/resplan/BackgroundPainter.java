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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jdesktop.swingx.painter.Painter;
import org.nuclos.client.scripting.GroovySupport;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.JResPlanComponent.Area;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;

public class BackgroundPainter implements Painter<Area<Collectable, Date>> {

	public static Class<?>[] SCRIPTING_SIGNATURE = { Collectable.class, Interval.class, BackgroundPainter.class };

	private final Date today = DateUtils.getPureDate(new Date());
	private GregorianCalendar gcToday = new GregorianCalendar();
	private GregorianCalendar gcStart = new GregorianCalendar();
	private GregorianCalendar gcEnd = new GregorianCalendar();

	public BackgroundPainter() {
		gcToday.setTime(today);
	}

	private Color color;
	private GroovySupport.InvocableMethod groovyMethod;
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Object colorObject) {
		if (colorObject instanceof String) {
			this.color = Color.decode((String) colorObject);
		} else {
			this.color = (Color) colorObject;
		}
	}
	
	void setGroovyMethod(GroovySupport.InvocableMethod groovyMethod) {
		this.groovyMethod = groovyMethod;
	}
	
	@Override
	public void paint(Graphics2D g, Area<Collectable, Date> area, int width, int height) {
		color = null;
		Date pureDateStart = DateUtils.getPureDate(area.getInterval().getStart());
		Date pureDateEnd = DateUtils.getPureDate(area.getInterval().getEnd());
		
		boolean dayView = pureDateStart.equals(pureDateEnd) || DateUtils.addDays(pureDateStart, 1).equals(pureDateEnd);
		
		if (dayView) {
			if (pureDateStart.equals(today)) {
				color = new Color(0xccffcc);
			} else {
				gcStart.setTime(pureDateStart);
				int dayOfWeek = gcStart.get(GregorianCalendar.DAY_OF_WEEK);
				if (dayOfWeek == GregorianCalendar.SATURDAY || dayOfWeek == GregorianCalendar.SUNDAY) {
					color = new Color(0xffcccc);
				}
			}
		} else {
			gcStart.setTime(pureDateStart);
			gcEnd.setTime(pureDateEnd);
			
			if (LangUtils.equals(gcStart.get(Calendar.DAY_OF_MONTH), gcEnd.get(Calendar.DAY_OF_MONTH))) {
				// is MonthView...
				
				if (LangUtils.equals(gcStart.get(Calendar.MONTH), gcToday.get(Calendar.MONTH)) && 
					LangUtils.equals(gcStart.get(Calendar.YEAR), gcToday.get(Calendar.YEAR))) {
					color = new Color(0xccffcc);
				}
			}
		}
		
		if (groovyMethod != null && !groovyMethod.hasErrors()) {
			groovyMethod.invoke(area.getResource(), area.getInterval(), this);
		}
		if (color != null) {
			g.setColor(color);
			g.fillRect(0, 0, width, height);
		}
	}
}
