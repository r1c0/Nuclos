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
package org.nuclos.server.report;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.nuclos.common.PDFHelper;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;

/**
 * Jasper Reports Data Source for Table Models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars.Rueckemann</a>
 * @version 01.00.00
 */
public class TableModelDataSource implements JRDataSource {

	private TableModel model;
	private int iCurrentRow;
	private Map<String, Integer> mapFields;

	public TableModelDataSource(TableModel model) {
		this.model = model;
		this.iCurrentRow = -1;
		mapFields = new HashMap<String, Integer>();
		for (int i = 0; i < model.getColumnCount(); i++) {
			mapFields.put(PDFHelper.getFieldName(model.getColumnName(i)), i);
		}
	}

	/**
	 * switch to the next row in the table model
	 * @return
	 * @throws JRException
	 */
	@Override
	public boolean next() throws JRException {
		if (++iCurrentRow < model.getRowCount()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		Object value = model.getValueAt(iCurrentRow, this.mapFields.get(jrField.getName()));
		return CollectableFieldFormat.getInstance(value != null ? value.getClass() : String.class).format(null, value);
	}

}	// class TableModelDataSource
