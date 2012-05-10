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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Jasper Reports Data Source for Table Models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars.Rueckemann</a>
 * @version 01.00.00
 */
public class ResultVODataSource implements JRDataSource {

	private ResultVO resultVO;
	private int iCurrentRow;
	private Map<String, Integer> mapFields;
	private Map<String, Class<?>> mapTypes;
	private List<ReportFieldDefinition> fields;
 
	public ResultVODataSource(ResultVO resultVO, List<ReportFieldDefinition> fields) {
		this.resultVO = resultVO;
		this.iCurrentRow = -1;
		mapFields = new HashMap<String, Integer>();
		mapTypes = new HashMap<String, Class<?>>();
		this.fields = fields;
		for (int i = 0; i < resultVO.getColumnCount(); i++) {
			String fieldname = fields.get(i).getName();
			mapFields.put(fieldname, i);
			mapTypes.put(fieldname, resultVO.getColumns().get(i).getColumnClass());
		}
	}

	/**
	 * switch to the next row in the table model
	 * @return
	 * @throws JRException
	 */
	@Override
	public boolean next() throws JRException {
		if (++iCurrentRow < resultVO.getRowCount()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		Object value = resultVO.getRows().get(iCurrentRow)[this.mapFields.get(jrField.getName())];
		Class<?> type = mapTypes.get(jrField.getName());
		String format = fields.get(this.mapFields.get(jrField.getName())).getOutputformat();
		if (value instanceof List) {
			final StringBuilder result = new StringBuilder();
			for (Iterator<?> it = ((List<?>) value).iterator(); it.hasNext();) {
				final Object v = it.next();
				result.append(CollectableFieldFormat.getInstance(type).format(format, v));
				if (it.hasNext()) {
					result.append(", ");
				}
			}
			return result.toString();
		}
		else if (NuclosImage.class.equals(mapTypes.get(jrField.getName()))) {
			if (((NuclosImage) value).getContent() == null)
				return null;
			
			// remove transparency for pdf output.
			try {
				BufferedImage in = ImageIO.read(new ByteArrayInputStream(((NuclosImage)value).getContent()));
				BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = out.createGraphics();
				g2d.setBackground(Color.white);
				g2d.clearRect(0, 0, out.getWidth(), out.getHeight());
				g2d.fillRect(0, 0, out.getWidth(), out.getHeight());				
				g2d.drawImage(in, 0, 0, out.getWidth(), out.getHeight(), null);
				g2d.dispose(); 
				return out;
			} catch (Exception e) {
    			// do nothing
			}
			return new ImageIcon(((NuclosImage) value).getContent()).getImage();
		}
		else {
			if (String.class.equals(mapTypes.get(jrField.getName()))) {
				//@todo: this is a workaround (seems that Jasper can not handle \n in String values) (LR)
				value = (value == null) ? null : ((String) value).replace('\n', ' ').trim();
			}
			return CollectableFieldFormat.getInstance(type).format(format, value);
		}
	}

}	// class TableModelDataSource
