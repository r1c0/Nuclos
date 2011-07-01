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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.resplan.AbstractResPlanModel;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.time.LocalTime;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.exception.CommonBusinessException;


public class CollectableResPlanModel extends AbstractResPlanModel<Collectable, Date, Collectable> {

	protected static final Logger log = Logger.getLogger(CollectableResPlanModel.class);

	private boolean readOnly = false;
	private ResPlanController controller;
	private ResPlanConfigVO configVO;
	private CollectableHelper<?> resEntity;
	private CollectableHelper<?> entryEntity;
	private boolean hasTime;
	private List<Collectable> resources = new ArrayList<Collectable>();
	private Map<Object, List<Collectable>> entryMap = new HashMap<Object, List<Collectable>>();

	public CollectableResPlanModel(ResPlanController controller) {
		this.controller = controller;
		this.configVO = controller.getConfigVO();
		this.resEntity = controller.getResEntity();
		this.entryEntity = controller.getEntryEntity();
		this.hasTime = configVO.getTimeFromField() != null && configVO.getTimeUntilField() != null;
	}

	@Override
	public Class<Collectable> getEntryType() {
		return Collectable.class;
	}
	
	public CollectableHelper<?> getResourceEntity() {
		return resEntity;
	}
	
	public CollectableHelper<?> getEntryEntity() {
		return entryEntity;
	}
	
	public CollectableEntityField getReferenceEntityField() {
		return entryEntity.getCollectableEntity().getEntityField(configVO.getReferenceField());
	}
	
	public void setData(Collection<? extends Collectable> resources, Collection<? extends Collectable> entries) {
		this.resources = new ArrayList<Collectable>(resources);
		this.entryMap = new HashMap<Object, List<Collectable>>();
		for (Collectable clct : entries) {
			addEntryToMap(clct);
		}
		fireResourcesChanged();
	}

	private void addEntryToMap(Collectable clct) {
		Object parentId = clct.getValueId(configVO.getReferenceField());
		List<Collectable> parentEntries = entryMap.get(parentId);
		if (parentEntries == null) {
			parentEntries = new ArrayList<Collectable>();
			entryMap.put(parentId, parentEntries);
		}
		parentEntries.add(clct);
	}

	private void removeEntryFromMap(Collectable entry) {
		List<Collectable> list = entryMap.get(entry.getValueId(configVO.getReferenceField()));
		if (list != null) {
			list.remove(entry);
		}
	}

	@Override
	public List<? extends Collectable> getResources() {
		return resources;
	}

	@Override
	public List<? extends Collectable> getEntries(Collectable resource) {
		List<Collectable> list = entryMap.get(resource.getId());
		return (list != null) ? list : Collections.<Collectable>emptyList();
	}

	@Override
	public Interval<Date> getInterval(Collectable entry) {
		Date start = getDateTime(entry, configVO.getDateFromField(), configVO.getTimeFromField(), false);
		Date end = getDateTime(entry, configVO.getDateUntilField(), configVO.getTimeUntilField(), true);
		return new Interval<Date>(start, end, true);
	}

	@Override
	public void updateEntry(final Collectable entry, final Collectable resource, final Interval<Date> interval) {
		controller.runCommandWithSpecialHandler(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				Collectable clone = entryEntity.copyCollectable(entry);
				prepareCollectableEntry(clone, resource, interval);
				Collectable modified = entryEntity.modify(clone);
				removeEntryFromMap(entry);
				addEntryToMap(modified);
				fireResourcesChanged();
			}
		});
	}

	@Override
	public void removeEntry(final Collectable entry) {
		controller.runCommandWithSpecialHandler(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				entryEntity.remove(entry);
				removeEntryFromMap(entry);
				fireResourcesChanged();
			}
		});
	}

	@Override
	public void createEntry(final Collectable resource, final Interval<Date> interval, Object value) {
		if (value instanceof Collectable) {
			final Collectable clct = entryEntity.check((Collectable) value);
			if (clct != null) {
				controller.runCommandWithSpecialHandler(new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						Collectable clone = entryEntity.cloneCollectable(clct);
						prepareCollectableEntry(clone, resource, interval);
						clone = entryEntity.create(clone);
						addEntryToMap(clone);
						fireResourcesChanged();
					}
				});
			}
		}
	}
	
	@Override
	public boolean isCreateAllowed() {
		if (readOnly)
			return false;
		return entryEntity.isNewAllowed();
	}
	
	@Override
	public boolean isRemoveAllowed(Collectable entry) {
		if (readOnly)
			return false;
		entry = entryEntity.check(entry);
		return entry != null && entryEntity.isRemoveAllowed(entry);
	}
	
	@Override
	public boolean isUpdateAllowed(Collectable entry) {
		if (readOnly)
			return false;
		entry = entryEntity.check(entry);
		return entry != null && entryEntity.isModifyAllowed(entry);
	}
	
	public Collectable createCollectableEntry(Collectable resource, Interval<Date> interval) {
		Collectable clct = entryEntity.newCollectable(true);
		prepareCollectableEntry(clct, resource, interval);
		return clct;
	}
	
	public boolean isCollectableEntryValid(Collectable entry) {
		Object dateFrom = entry.getValue(configVO.getDateFromField());
		Object dateUntil = entry.getValue(configVO.getDateUntilField());
		if (!(dateFrom instanceof Date) || !(dateUntil instanceof Date))
			return false;
		if (hasTime) {
			Object timeFrom = entry.getValue(configVO.getTimeFromField());
			Object timeUntil = entry.getValue(configVO.getTimeUntilField());
			return isValidTimeValue(timeFrom) && isValidTimeValue(timeUntil);
		}
		return true;
	}

	private static boolean isValidTimeValue(Object value) {
		// null is valid (see getDateTime and means start resp. end of the whole day)
		if (value == null)
			return true;
		if (value instanceof String) {
			try {
				// Test if time string is parseable
				LocalTime.parse((String) value);
				return true;
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}
		return false;
	}

	private Collectable prepareCollectableEntry(Collectable entry, Collectable resource, Interval<Date> interval) {
		setDateTime(entry, configVO.getDateFromField(), configVO.getTimeFromField(), interval.getStart(), false);
		setDateTime(entry, configVO.getDateUntilField(), configVO.getTimeUntilField(), interval.getEnd(), true);
		if (resource != null) {
			EntityFieldMetaDataVO entityField = MetaDataClientProvider.getInstance().getEntityField(entryEntity.getEntityName(), configVO.getReferenceField());
			String resourceLabel = CollectableUtils.formatFieldExpression(entityField.getForeignEntityField(), resource);
			entry.setField(configVO.getReferenceField(), new CollectableValueIdField(resource.getId(), resourceLabel));
		}
		return entry;
	}
	
	private Date getDateTime(Collectable clct, String dateFieldName, String timeFieldName, boolean end) {
		Date date = (Date) clct.getValue(dateFieldName);
		date = DateUtils.getPureDate(date);
		boolean add1Day = false;
		if (hasTime) {
			String timeString = (String) clct.getValue(timeFieldName);
			long secondOfDay = 0;
			if (timeString != null) {
				secondOfDay = LocalTime.parse(timeString).toSecondOfDay();
				date = new Date(date.getTime() + secondOfDay * 1000);
			} else {
				add1Day = end;
			}
		} else {
			add1Day = end;
		}
		if (add1Day)
			date = DateUtils.addDays(date, 1);
		return date;
	}
	
	private void setDateTime(Collectable clct, String dateFieldName, String timeFieldName, Date date, boolean end) {
		Date datePart = DateUtils.getPureDate(date);
		if (hasTime) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(date);
			LocalTime time = LocalTime.ofSecondOfDay((date.getTime() - datePart.getTime()) / 1000);
			String timeString = time.toString();
			clct.setField(timeFieldName, new CollectableValueField(timeString));
		} else {
			if (end) {
				datePart = DateUtils.addDays(datePart, -1);
			}
		}
		clct.setField(dateFieldName, new CollectableValueField(datePart));
	}
}
