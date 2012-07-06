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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.editor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXPanel;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BORDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMMON_LABELS;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown.MovePanelUpAndDownControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * The Bordereditor for visually creating a Border.<br>
 * Contains:
 * <ul>
 * <li> {@link BorderEditorPreviewPanel} to see what was "clicked together" </li>
 * <li> {@link SingleBorder} for creating one Border </li>
 * </ul>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class BorderEditor extends JDialog implements SaveAndCancelButtonPanelControllable, ListDataListener {
	
	static class SingleBorder extends JPanel implements LayoutMLConstants {

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
						Map<String, String> res = TranslationEditor.showDialog(TitledBorderEditor.this, translations, title.getText());
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

	private static final Logger LOG = Logger.getLogger(BorderEditor.class);

	private int width = 850;
	private int height = 400;

	private JCheckBox checkBox = new JCheckBox();
	private JScrollPane scrollPane = null;
	private JXPanel borderPanel = new JXPanel();

	/** for storing the borders */
	private DefaultListModel model = new DefaultListModel();
	
	/** for making a preview */
	private BorderEditorPreviewPanel previewPanel = new BorderEditorPreviewPanel();
	
	private Border border = null;
	private boolean clearBorder;

	/**
	 * Constructor
	 * @param wysiwygComponent the {@link WYSIWYGComponent} to add the Border
	 * @param border the {@link Border} to be restored
	 * @param clearBorder the {@link LayoutMLConstants#ELEMENT_CLEARBORDER} Element
	 */
	public BorderEditor(WYSIWYGComponent wysiwygComponent, Border border, boolean clearBorder) {
		this.setIconImage(NuclosIcons.getInstance().getScaledDialogIcon(48).getImage());
		
		this.border = border;
		if (border != null) {
			this.setBorder(border);
		}
		else {
			this.setBorder(BorderFactory.createEmptyBorder());
		}
		
		this.clearBorder = clearBorder;
		
		model.addListDataListener(this);
		
		this.checkBox.setSelected(clearBorder);
		this.checkBox.setText(BORDER_EDITOR.LABEL_CLEAR_BORDER_CHECKBOX);
		
		this.setTitle(BORDER_EDITOR.TITLE_BORDER_EDITOR);
		double[][] borderEditorDialog = {
			{
				InterfaceGuidelines.MARGIN_LEFT, 
				TableLayout.FILL, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				BorderEditorPreviewPanel.PREVIEW_BORDER_SIZE, 
				InterfaceGuidelines.MARGIN_RIGHT
			}, 
			{
				InterfaceGuidelines.MARGIN_TOP,
				TableLayout.PREFERRED,
				InterfaceGuidelines.MARGIN_BETWEEN,
				TableLayout.FILL, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED,
				InterfaceGuidelines.MARGIN_BOTTOM
			}
		};
		this.setLayout(new TableLayout(borderEditorDialog));
		
		this.add(checkBox, new TableLayoutConstraints(1,1,3,1));

		borderPanel = new JXPanel();
		borderPanel.setScrollableTracksViewportWidth(true);
		scrollPane = new JScrollPane(borderPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		refresh();
		
		this.add(scrollPane, "1,3");
		
		ArrayList<AbstractButton> list = new ArrayList<AbstractButton>();
		JButton button = new JButton(BORDER_EDITOR.LABEL_REMOVE_ALL_BORDERS);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.clear();
				performSaveAction();
			}
		});
		list.add(button);
		
		this.add(new SaveAndCancelButtonPanel(borderPanel.getBackground(), this, list), new TableLayoutConstraints(1,5,3,5));
		
		
		TableLayoutConstraints constraint = new TableLayoutConstraints(3,3,3,3, TableLayout.FULL, TableLayout.FULL);
		this.add(previewPanel, constraint);
		
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - width) / 2;
		int y = (screenSize.height - height) / 2;
		this.setBounds(x, y, width, height);
		this.setModal(true);
		this.setVisible(true);
	}
	
	/**
	 * @return the {@link Border} Object
	 */
	public Border getBorder() {
		return border;
	}
	
	/**
	 * @return the Border as is shows up in the {@link BorderEditorPreviewPanel}
	 */
	private Border getBorderFromModel() {
		Border result = null;
		for (int i = model.getSize() - 1; i >= 0 ; i--) {
			if (result == null) {
				result = (Border)model.get(i);
			}
			else {
				result = BorderFactory.createCompoundBorder((Border)model.get(i), result);
			}
		}
		return result;
	}
	
	/**
	 * @param border the Border to be restored
	 */
	private void setBorder(Border border) {
		model = new DefaultListModel();
		
		while (border instanceof CompoundBorder) {
			model.addElement(((CompoundBorder)border).getOutsideBorder());
			border = ((CompoundBorder)border).getInsideBorder();
		}
		model.addElement(border);
	}
	
	/**
	 * @return the {@link LayoutMLConstants#ELEMENT_CLEARBORDER}
	 */
	public boolean getClearBorder() {
		return clearBorder;
	}

	/**
	 * This Method creates a new Border
	 * @param index
	 */
	public void performAddBorderAction(int index) {
		if (viewToModel()) {
			model.add(index + 1, BorderFactory.createEmptyBorder());
		}
	}

	/**
	 * This Method removes a Border
	 * @param index
	 */
	public void performRemoveBorderAction(int index) {
		if (viewToModel()) {
			try {
			model.remove(index);
			} catch (ArrayIndexOutOfBoundsException e) {
				LOG.warn("performRemoveBorderAction failed: " + e);
			}
		}
	}
	
	/**
	 * This Method moves a Panel one Position up
	 * @param index
	 */
	public void performMovePanelUpAction(int index) {
		if (viewToModel()) {
			Object o = model.get(index);
			model.remove(index);
			model.add(index - 1, o);
		}
	}

	/**
	 * This Method moves a Panel one Position down
	 * @param index
	 */
	public void performMovePanelDownAction(int index) {
		if (viewToModel()) {
			Object o = model.get(index);
			model.remove(index);
			model.add(index + 1, o);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		if (viewToModel()) {
			this.border = getBorderFromModel();
			this.clearBorder = checkBox.isSelected();
			this.dispose();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void contentsChanged(ListDataEvent e) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void intervalAdded(ListDataEvent e) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void intervalRemoved(ListDataEvent e) {
		refresh();
	}
	
	/**
	 * This Method removes all Borders and refeshes the {@link BorderEditorPreviewPanel}
	 */
	private void refresh() {
		borderPanel.removeAll();
		
		double[][] borderPanelLayout = new double[][]{
				{
					TableLayout.FILL
				}, 
				{}
			};
		borderPanel.setLayout(new TableLayout(borderPanelLayout));
			
		if (model.getSize() > 0) {
			for (int a = 0; a < model.getSize(); a++) {
				int i = expandLayout();
				JPanel singleBorderPanel = new CompleteSingleBorderPanel((Border)model.get(a), a);
				borderPanel.add(singleBorderPanel, "0," + i);
			}
		}
		else {
			int i = expandLayout();
			JPanel singleBorderPanel = new CompleteSingleBorderPanel(BorderFactory.createLineBorder(Color.WHITE), 0);
			borderPanel.add(singleBorderPanel, "0," + i);
		}
		previewPanel.setBordersForComponent(getBorderFromModel());
		borderPanel.updateUI();
		scrollPane.updateUI();
	}
	
	/**
	 * This Method expands the Layout on one Row
	 * @return the number of the Row
	 */
	public int expandLayout() {
		TableLayout tableLayout = (TableLayout) borderPanel.getLayout();
		int rows = tableLayout.getNumRow();
		//rows = rows - 1;
		if (rows < 0)
			rows = 0;

		tableLayout.insertRow(rows, TableLayout.PREFERRED);
		tableLayout.insertRow(rows, InterfaceGuidelines.MARGIN_BETWEEN);

		return rows + 1;
	}
	
	/**
	 * @return the Number of Borders
	 */
	public int getModelSize() {
		return model.getSize();
	}
	
	/**
	 * Transfers the Panels into the underlying {@link ListModel}.
	 * @return true if successful, false if Exception occours
	 */
	public boolean viewToModel() {
		HashMap<Integer, Border> map = new HashMap<Integer, Border>();
		
		try {
			for (int i = 0; i < model.getSize(); i++) {
				for (Component c : borderPanel.getComponents()) {
					if (c instanceof CompleteSingleBorderPanel) {
						map.put(((CompleteSingleBorderPanel)c).index, ((CompleteSingleBorderPanel)c).getEditedBorder());
					}
				}
			}

			for (Map.Entry<Integer, Border> e : map.entrySet()) {
				model.set(e.getKey(), e.getValue());
			}
			return true;
		}
		catch (CommonBusinessException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), COMMON_LABELS.ERROR, JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	/**
	 * This Class wraps:
	 * <ul>
	 * <li> {@link SingleBorder}</li>
	 * <li> {@link MovePanelUpAndDown} </li>
	 * <li> {@link AddRemoveRowsFromPanel}</li>
	 * <ul>
	 * This Panel controls everything needed do edit a {@link Border} Object. 
	 *
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class CompleteSingleBorderPanel extends JPanel implements AddRemoveButtonControllable, MovePanelUpAndDownControllable {

		private int index;

		private SingleBorder singleBorder = null;
		private MovePanelUpAndDown movePanelUpAndDown = null;
		private AddRemoveRowsFromPanel addRemoveRowsFromPanel = null;
		
		private double[][] layout = new double[][]{
				{
					5,
					TableLayout.PREFERRED, 
					TableLayout.FILL,
					TableLayout.PREFERRED,
					5
				},
				{
					TableLayout.PREFERRED
				}
		};
				
		/**
		 * 
		 * @param border the {@link Border} to edit
		 * @param index the Position
		 */
		public CompleteSingleBorderPanel(Border border, int index) {
			this.index = index;
			
			this.setLayout(new TableLayout(layout));
			this.setBorder(BorderFactory.createTitledBorder(WYSIWYGStringsAndLabels.partedString(BORDER_EDITOR.BORDER_PANEL_TITLE, (index + ""))));
			
			movePanelUpAndDown = new MovePanelUpAndDown(borderPanel.getBackground(), this, MovePanelUpAndDown.VERTICAL);
			this.add(movePanelUpAndDown, "1,0");
			
			if (index == 0) {
				movePanelUpAndDown.disableMoveUpButton();
			}
			if (index + 1 == BorderEditor.this.getModelSize()) {
				movePanelUpAndDown.disableMoveDownButton();
			}
			
			singleBorder = new SingleBorder(border, BorderEditor.this);
			this.add(singleBorder, "2,0");
			
			addRemoveRowsFromPanel = new AddRemoveRowsFromPanel(borderPanel.getBackground(), this, AddRemoveRowsFromPanel.VERTICAL);
			this.add(addRemoveRowsFromPanel, "3,0");
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
		 */
		@Override
		public void performAddAction() {
			if (viewToModel()) {
				performAddBorderAction(index);
			}
			scrollPane.getViewport().setViewPosition(this.getLocation());
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
		 */
		@Override
		public void performRemoveAction() {
			if (viewToModel()) {
				performRemoveBorderAction(index);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown.MovePanelUpAndDownControllable#performMoveDownAction()
		 */
		@Override
		public void performMoveDownAction() {
			if (viewToModel()) {
				performMovePanelDownAction(index);
			}
			scrollPane.getViewport().setViewPosition(this.getLocation());
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown.MovePanelUpAndDownControllable#performMoveUpAction()
		 */
		@Override
		public void performMoveUpAction() {
			if (viewToModel()) {
				performMovePanelUpAction(index);
			}
			scrollPane.getViewport().setViewPosition(this.getLocation());
		}

		/**
		 * @return the {@link Border} that is edited in this {@link CompleteSingleBorderPanel}
		 * @throws NuclosBusinessException if invalid Values were found (Parse Exception for Values etc)
		 */
		public Border getEditedBorder() throws NuclosBusinessException {
			return singleBorder.getEditedBorder();
		}
	}
}
