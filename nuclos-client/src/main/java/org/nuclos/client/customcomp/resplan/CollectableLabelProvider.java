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

import javax.swing.JLabel;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.PainterAware;
import org.nuclos.client.scripting.GroovySupport;
import org.nuclos.client.ui.resplan.header.JHeaderGrid;
import org.nuclos.client.ui.util.PainterUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.StringUtils;

public class CollectableLabelProvider extends LabelProvider {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Class<?>[] SCRIPTING_SIGNATURE = { Collectable.class, LabelCell.class };
	
	private String labelTemplate;
	private String toolTipTemplate;
	private GroovySupport.InvocableMethod scriptMethod;
	
	public CollectableLabelProvider() {
	}

	public String getLabelTemplate() {
		return labelTemplate;
	}
	
	public void setLabelTemplate(String labelTemplate) {
		this.labelTemplate = labelTemplate;
	}
	
	public String getToolTipTemplate() {
		return toolTipTemplate;
	}
	
	public void setToolTipTemplate(String toolTipTemplate) {
		this.toolTipTemplate = toolTipTemplate;
	}
	
	public void setGroovyMethod(GroovySupport.InvocableMethod method) {
		this.scriptMethod = method;
	}
	
	@Override
	protected JLabel createRendererComponent() {
		JLabel label = super.createRendererComponent();
		label.setVerticalAlignment(JLabel.TOP);
		return label;
	}
	
	@Override
	protected void format(CellContext context) {
		super.format(context);
		
		Object value = context.getValue();
		if (value instanceof Collectable) {
			Collectable clct = (Collectable) value;
			String text = formatTemplateText(labelTemplate, clct);
			String toolTip = formatTemplateText(toolTipTemplate, clct);
			Color color = null;

			if (scriptMethod != null && !scriptMethod.hasErrors()) {
				LabelCell cell = new LabelCell();
				cell.setText(text);
				cell.setToolTip(toolTip);
				scriptMethod.invoke(clct, cell);
				text = cell.getText();
				toolTip = cell.getToolTip();
				color = cell.getColor();
			}

			rendererComponent.setText(StringUtils.defaultIfNull(text, clct.getIdentifierLabel()));
			rendererComponent.setToolTipText(StringUtils.nullIfEmpty(toolTip));
			Painter<?> painter = null;
			if (context.getComponent() instanceof JHeaderGrid<?>) {
				if (context.isSelected()) {
					color = new Color(0xccccff);
					painter = new PainterUtils.BorderPainter(new Color(0x8080ff), 3);
				} else {
					painter = new PainterUtils.HeaderPainter(color);
				}
			}
			
			if (color != null) {
				rendererComponent.setBackground(color);
			}
			if (rendererComponent instanceof PainterAware) {
				((PainterAware) rendererComponent).setPainter(painter);
			}
		}
	}

	private static String formatTemplateText(String templateText, final Collectable clct) {
		if (templateText == null)
			return null;
		final boolean html = templateText.startsWith("<html>");
		return StringUtils.replaceParameters(templateText, new Transformer<String, String>() {
			@Override public String transform(String param) {
				String value = null;
				try {
					CollectableField field = clct.getField(param);
					if (field != null) {
						value = field.toString();
					}
				} catch (Exception ex) {
				}
				if (value == null) {
					value = "${" + param + "}";
				}
				return html ? StringUtils.xmlEncode(value) : value;
			}
		});
	}
	
	public static class LabelCell {
		
		private String text;
		private Color color;
		private String toolTip;
		
		public void setText(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
		public void setColor(Object colorObject) {
			if (colorObject instanceof String) {
				this.color = Color.decode((String) colorObject);
			} else {
				this.color = (Color) colorObject;
			}
		}
		
		public Color getColor() {
			return color;
		}
		
		public void setToolTip(String toolTip) {
			this.toolTip = toolTip;
		}
		
		public String getToolTip() {
			return toolTip;
		}
	}
}