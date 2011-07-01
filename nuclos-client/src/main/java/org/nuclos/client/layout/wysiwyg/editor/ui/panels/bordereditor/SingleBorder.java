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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.bordereditor;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BORDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.TranslationPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This class represents one {@link Border} Object.<br>
 * Has Editors for:
 * <ul>
 * <li> {@link EmptyBorderEditor}</li>
 * <li> {@link EtchedBorderEditor}</li>
 * <li> {@link BevelBorderEditor}</li>
 * <li> {@link LineBorderEditor}</li>
 * <li> {@link TitledBorderEditor}</li>
 * </ul>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class SingleBorder extends JPanel implements LayoutMLConstants {

	/** The bordertype that can be set */
	private HashMap<String, String> borderTypes = new HashMap<String, String>();
	{
		borderTypes.put(ELEMENT_EMPTYBORDER, BORDER_EDITOR.NAME_EMPTYBORDER);
		borderTypes.put(ELEMENT_ETCHEDBORDER, BORDER_EDITOR.NAME_ETCHEDBORDER);
		borderTypes.put(ELEMENT_BEVELBORDER, BORDER_EDITOR.NAME_BEVELBORDER);
		borderTypes.put(ELEMENT_LINEBORDER, BORDER_EDITOR.NAME_LINEBORDER);
		borderTypes.put(ELEMENT_TITLEDBORDER, BORDER_EDITOR.NAME_TITLEDBORDER);
	}

	private Border border;
	
	private JComboBox combobox = new JComboBox();
	private SingleBorderEditor editor;
	private BorderEditor borderEditor;
	
	/**
	 * Constructor
	 * @param b the {@link Border} that is edited
	 * @param borderEditor the {@link BorderEditor}
	 */
	public SingleBorder(Border b, BorderEditor borderEditor) {
		this.borderEditor = borderEditor;
		this.border = b;
		double[][] singleBorderLayout = {
				{
					InterfaceGuidelines.MARGIN_LEFT, 
					TableLayout.PREFERRED, 
					InterfaceGuidelines.MARGIN_BETWEEN, 
					TableLayout.FILL,
					InterfaceGuidelines.MARGIN_RIGHT
				}, 
				{
					3,
					TableLayout.PREFERRED,
					InterfaceGuidelines.MARGIN_BETWEEN,
					TableLayout.PREFERRED
				}
			};
		this.setLayout(new TableLayout(singleBorderLayout));

		for (Entry<String, String> type : borderTypes.entrySet()) {
			combobox.addItem(type.getValue());
		}
		
		/** restoring a existing Border */
		if (border instanceof EmptyBorder) {
			combobox.setSelectedItem(borderTypes.get(ELEMENT_EMPTYBORDER));
		} else if (border instanceof EtchedBorder) {
			combobox.setSelectedItem(borderTypes.get(ELEMENT_ETCHEDBORDER));
		} else if (border instanceof BevelBorder) {
			combobox.setSelectedItem(borderTypes.get(ELEMENT_BEVELBORDER));
		} else if (border instanceof LineBorder) {
			combobox.setSelectedItem(borderTypes.get(ELEMENT_LINEBORDER));
		} else if (border instanceof TitledBorder) {
			combobox.setSelectedItem(borderTypes.get(ELEMENT_TITLEDBORDER));
		} 
		
		setPanel();
		
		/** handling the change event */
		combobox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String selection = "";
				for (Entry<String, String> type : borderTypes.entrySet()) {
					if (combobox.getSelectedItem().equals(type.getValue())) {
						selection = type.getKey();
					}
				}
				
				if (selection.equals(ELEMENT_EMPTYBORDER)) {
					border = BorderFactory.createEmptyBorder();
				} else if (selection.equals(ELEMENT_ETCHEDBORDER)) {
					border = BorderFactory.createEtchedBorder();
				} else if (selection.equals(ELEMENT_BEVELBORDER)) {
					border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
				} else if (selection.equals(ELEMENT_LINEBORDER)) {
					border = BorderFactory.createLineBorder(Color.BLACK);
				} else if (selection.equals(ELEMENT_TITLEDBORDER)) {
					border = new TitledBorderWithTranslations(BORDER_EDITOR.DEFAULT_TITLE_FOR_NEW_TITLED_BORDER);
				}
				setPanel();
				refreshModel();
			}
		});

		this.add(new JLabel(BORDER_EDITOR.LABEL_BORDERTYPE), "1,1");
		this.add(combobox, "3,1");
		this.add(new JLabel(BORDER_EDITOR.LABEL_BORDERPROPERTIES), "1,3");
	}
	
	/**
	 * This Method applies the Changes to the "Dummy Object"
	 */
	private final void refreshModel(){
		borderEditor.viewToModel();
	}
	
	private void setPanel() {
		
		if (editor != null && ((JPanel)editor).getParent() != null) {
			((JPanel)editor).getParent().remove((JPanel)editor);
		}
		
		if (border instanceof EmptyBorder) {
			editor = new EmptyBorderEditor((EmptyBorder)border);
		} else if (border instanceof EtchedBorder) {
			editor = new EtchedBorderEditor((EtchedBorder)border);
		} else if (border instanceof BevelBorder) {
			editor = new BevelBorderEditor((BevelBorder)border);
		} else if (border instanceof LineBorder) {
			editor = new LineBorderEditor((LineBorder)border);
		} else if (border instanceof TitledBorder) {
			editor = new TitledBorderEditor((TitledBorder)border);
		} 
		
		this.add((JPanel)editor, "3,3");
		this.validate();
	}
	
	public Border getEditedBorder() throws NuclosBusinessException {
		return editor.getEditedBorder();
	}
	
	private interface SingleBorderEditor {
		public Border getEditedBorder() throws NuclosBusinessException;
	}

	/**
	 * This JPanel provides everything to edit a {@link EmptyBorder}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class EmptyBorderEditor extends JPanel implements SingleBorderEditor {
		
		private CollectableFieldFormat format = CollectableFieldFormat.getInstance(Integer.class);
		
		private JTextField top = new JTextField();
		private JTextField left = new JTextField();
		private JTextField bottom = new JTextField();
		private JTextField right = new JTextField();
		
		/**
		 * @param the {@link EmptyBorder} to restore
		 */
		public EmptyBorderEditor(EmptyBorder b) {
			top.setText(String.valueOf(b.getBorderInsets().top));
			left.setText(String.valueOf(b.getBorderInsets().left));
			bottom.setText(String.valueOf(b.getBorderInsets().bottom));
			right.setText(String.valueOf(b.getBorderInsets().right));
			
			top.setColumns(3);
			left.setColumns(3);
			bottom.setColumns(3);
			right.setColumns(3);
			
			this.setLayout(new TableLayout(new double[][]{
					{
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED
					},
					{
						TableLayout.PREFERRED
					}
				}));
			
			this.add(new JLabel(BORDER_EDITOR.TOP_BORDERLOCATION), "0,0");
			this.add(top, "2,0");
			this.add(new JLabel(BORDER_EDITOR.LEFT_BORDERLOCATION), "4,0");
			this.add(left, "6,0");
			this.add(new JLabel(BORDER_EDITOR.BOTTOM_BORDERLOCATION), "8,0");
			this.add(bottom, "10,0");
			this.add(new JLabel(BORDER_EDITOR.RIGHT_BORDERLOCATION), "12,0");
			this.add(right, "14,0");
		}
		
		/**
		 * @return new {@link EmptyBorder} with the Values set from {@link EmptyBorderEditor}
		 */
		@Override
		public Border getEditedBorder() throws NuclosBusinessException {
			try {
				int t = (Integer) format.parse(null, top.getText());
				int l = (Integer) format.parse(null, left.getText());
				int b = (Integer) format.parse(null, bottom.getText());
				int r = (Integer) format.parse(null, right.getText());
				
				return BorderFactory.createEmptyBorder(t, l, b, r);
			}
			catch (CollectableFieldFormatException ex) {
				throw new NuclosBusinessException(ex.getMessage());
			}
		}
	}
	
	/**
	 * This class is for creating a new {@link EtchedBorder}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class EtchedBorderEditor extends JPanel implements SingleBorderEditor {
		
		private JComboBox type = new JComboBox();
		
		private HashMap<String, String> types = new HashMap<String, String>();
		{
			types.put(ATTRIBUTEVALUE_LOWERED, BORDER_EDITOR.LOWERED_PROPERTY_BEVEL_BORDER);
			types.put(ATTRIBUTEVALUE_RAISED, BORDER_EDITOR.RAISED_PROPERTY_BEVEL_BORDER);
		}
		
		/**
		 * Constructor
		 * @param border the Border to be restored
		 */
		public EtchedBorderEditor(EtchedBorder border) {
			for (Map.Entry<String, String> entry : types.entrySet()) {
				type.addItem(entry.getValue());
				if (entry.getKey().equals(ATTRIBUTEVALUE_LOWERED) && border.getEtchType() == EtchedBorder.LOWERED) {
					type.setSelectedItem(entry.getValue());
				}
				else if (entry.getKey().equals(ATTRIBUTEVALUE_RAISED) && border.getEtchType() == EtchedBorder.RAISED) {
					type.setSelectedItem(entry.getValue());
				}
			}
			
			this.setLayout(new TableLayout(new double[][]{
					{
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.FILL
					},{
						TableLayout.PREFERRED
					}
				}));
						
			this.add(new JLabel(BORDER_EDITOR.LABEL_ETCHEDBORDER_TYPE), "0,0");
			this.add(type, "2,0");
		}

		/**
		 * @return new {@link EtchedBorder} with all the Values set from the {@link EtchedBorderEditor}
		 */
		@Override
		public Border getEditedBorder() throws NuclosBusinessException {
			for (Map.Entry<String, String> entry : types.entrySet()) {
				if (entry.getValue().equals(type.getSelectedItem())) {
					if (entry.getKey().equals(ATTRIBUTEVALUE_LOWERED)) {
						return BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
					} else if (entry.getKey().equals(ATTRIBUTEVALUE_RAISED)) {
						return BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * This class is for Editing a {@link BevelBorder}.
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class BevelBorderEditor extends JPanel implements SingleBorderEditor {

		private JComboBox type = new JComboBox();
		
		private HashMap<String, String> types = new HashMap<String, String>();
		{
			types.put(ATTRIBUTEVALUE_LOWERED, BORDER_EDITOR.LOWERED_PROPERTY_BEVEL_BORDER);
			types.put(ATTRIBUTEVALUE_RAISED, BORDER_EDITOR.RAISED_PROPERTY_BEVEL_BORDER);
		}
		
		/**
		 * @param border the {@link BevelBorder} to be restored
		 */
		public BevelBorderEditor(BevelBorder border) {
			for (Map.Entry<String, String> entry : types.entrySet()) {
				type.addItem(entry.getValue());
				if (entry.getKey().equals(ATTRIBUTEVALUE_LOWERED) && border.getBevelType() == BevelBorder.LOWERED) {
					type.setSelectedItem(entry.getValue());
				}
				else if (entry.getKey().equals(ATTRIBUTEVALUE_RAISED) && border.getBevelType() == BevelBorder.RAISED) {
					type.setSelectedItem(entry.getValue());
				}
			}
			
			this.setLayout(new TableLayout(new double[][]{
					{
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.FILL
					},{
						TableLayout.PREFERRED
					}
				}));
			
			this.add(new JLabel(BORDER_EDITOR.LABEL_BEVELBORDER_TYPE), "0,0");
			this.add(type, "2,0");
		}

		/**
		 * @return new created {@link BevelBorder} with the Values set from the {@link BevelBorderEditor}
		 */
		@Override
		public Border getEditedBorder() throws NuclosBusinessException {
			for (Map.Entry<String, String> entry : types.entrySet()) {
				if (entry.getValue().equals(type.getSelectedItem())) {
					if (entry.getKey().equals(ATTRIBUTEVALUE_LOWERED)) {
						return BorderFactory.createBevelBorder(BevelBorder.LOWERED);
					} else if (entry.getKey().equals(ATTRIBUTEVALUE_RAISED)) {
						return BorderFactory.createBevelBorder(BevelBorder.RAISED);
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * This class creates a new {@link LineBorder}.
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class LineBorderEditor extends JPanel implements SingleBorderEditor {
		private CollectableFieldFormat format = CollectableFieldFormat.getInstance(Integer.class);
		
		private JTextField thickness = new JTextField();
		private JButton button = new JButton(BUTTON_LABELS.LABEL_EDIT);
		
		private Color color;
		
		/**
		 * @param b the {@link LineBorder} to be restored
		 */
		public LineBorderEditor(LineBorder b) {
			thickness.setText(String.valueOf(b.getThickness()));
			thickness.setColumns(3);
			color = b.getLineColor();
			
			this.setLayout(new TableLayout(new double[][]{
					{
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED,
						InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
						TableLayout.PREFERRED
					},
					{
						TableLayout.PREFERRED
					}
				}));
			
			this.add(new JLabel(BORDER_EDITOR.LABEL_THICKNESS_PROPERTY), "0,0");
			this.add(thickness, "2,0");
			this.add(new JLabel(BORDER_EDITOR.LABEL_COLOR_PROPERTY), "4,0");
			
			
			button.setBackground(color);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Color c = JColorChooser.showDialog(LineBorderEditor.this, BORDER_EDITOR.COLORPICKER_LINECOLOR_TITLE, LineBorderEditor.this.color);
					if (c != null) {
						LineBorderEditor.this.color = c;
						LineBorderEditor.this.button.setBackground(c);
					}
					refreshModel();
				}
			});
			this.add(button, "6,0");
		}
		
		/** 
		 * @return a new {@link LineBorder} created with the values from {@link LineBorderEditor}
		 */
		@Override
		public Border getEditedBorder() throws NuclosBusinessException {
			try {
				int t = (Integer) format.parse(null, thickness.getText());
				
				return BorderFactory.createLineBorder(color, t);
			}
			catch (CollectableFieldFormatException ex) {
				throw new NuclosBusinessException(ex.getMessage());
			}
		}
	}
	
	/**
	 * This class is for creating a new {@link TitledBorder}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class TitledBorderEditor extends JPanel implements SingleBorderEditor {

		private JTextField title = new JTextField();
		private TranslationMap translations = null;
		private JButton button;

		/**
		 * @param b the {@link TitledBorder} to be restored
		 */
		public TitledBorderEditor(TitledBorder b) {
			title.setText(b.getTitle());
			if (b instanceof TitledBorderWithTranslations) {
				translations = ((TitledBorderWithTranslations) b).getTranslations();
			}
			
			this.setLayout(new TableLayout(new double[][]{
				{
					TableLayout.PREFERRED,
					InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
					TableLayout.FILL,
					InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
					TableLayout.PREFERRED,
					InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS,
					TableLayout.PREFERRED
				},
				{
					TableLayout.PREFERRED
				}
			}));
			
			this.add(new JLabel(BORDER_EDITOR.LABEL_TITLE_PROPERTY), "0,0");
			this.add(title, "2,0");
			
			this.add(new JLabel(BORDER_EDITOR.LABEL_TRANSLATIONS_PROPERTY), "4,0");
			button = new JButton(BUTTON_LABELS.LABEL_EDIT);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (translations == null) {
						translations = new TranslationMap();
					}
					Map<String, String> res = TranslationPanel.showDialog(TitledBorderEditor.this, translations, title.getText());
					if (res != null) {
						translations.merge(res);
					}
					refreshModel();
				}
			});
			this.add(button, "6,0");
		}
		
		/**
		 * @return a new {@link TitledBorderWithTranslations} created with the values from {@link TitledBorderEditor}
		 */
		@Override
		public Border getEditedBorder() throws NuclosBusinessException {
			return new TitledBorderWithTranslations(title.getText(), translations);
		}
	}
}
