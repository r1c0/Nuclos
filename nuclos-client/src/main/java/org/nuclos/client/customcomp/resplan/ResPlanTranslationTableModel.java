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
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LocaleInfo;

public class ResPlanTranslationTableModel extends AbstractTableModel {

	static String[] columns = {ResPlanResourceVO.LOCALE,
		ResPlanResourceVO.RESOURCE_L,
		ResPlanResourceVO.RESOURCE_TT,
		ResPlanResourceVO.BOOKING_L,
		ResPlanResourceVO.BOOKING_TT,
		ResPlanResourceVO.LEGEND_L,
		ResPlanResourceVO.LEGEND_TT};

	List<ResPlanResourceVO> lstRows;

	Map<Integer, LocaleInfo> localeLabels;

	public ResPlanTranslationTableModel(Collection<LocaleInfo> locales) {
		lstRows = new ArrayList<ResPlanResourceVO>();
		localeLabels = CollectionUtils.transformIntoMap(locales, new Transformer<LocaleInfo, Integer>() {
			@Override
			public Integer transform(LocaleInfo i) {
				return i.localeId;
			}
		});
	}

	public void setRows(List<ResPlanResourceVO> rows) {
		lstRows = rows;
		this.fireTableDataChanged();
	}

	public List<ResPlanResourceVO> getRows() {
		return lstRows;
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public int getRowCount() {
		return lstRows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return localeLabels.get(lstRows.get(rowIndex).getLocaleId());
			case 1:
				return lstRows.get(rowIndex).getResourceLabel();
			case 2:
				return lstRows.get(rowIndex).getResourceTooltip();
			case 3:
				return lstRows.get(rowIndex).getBookingLabel();
			case 4:
				return lstRows.get(rowIndex).getBookingTooltip();
			case 5:
				return lstRows.get(rowIndex).getLegendLabel();
			case 6:
				return lstRows.get(rowIndex).getLegendTooltip();
			default:
				break;
		}
		return "";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue != null && !(aValue instanceof String)) {
			throw new IllegalArgumentException("aValue is not a java.lang.String");
		}
		String value = (String) aValue;

		switch(columnIndex) {
			case 1:
				lstRows.get(rowIndex).setResourceLabel(value);
				break;
			case 2:
				lstRows.get(rowIndex).setResourceTooltip(value);
				break;
			case 3:
				lstRows.get(rowIndex).setBookingLabel(value);
				break;
			case 4:
				lstRows.get(rowIndex).setBookingTooltip(value);
				break;
			case 5:
				lstRows.get(rowIndex).setLegendLabel(value);
				break;
			case 6:
				lstRows.get(rowIndex).setLegendTooltip(value);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex != 0;
	}

	@Override
	public String getColumnName(int column) {
		return SpringLocaleDelegate.getInstance().getText(
				"nuclos.resplan.l10n.labels." + columns[column]);
	}
}
