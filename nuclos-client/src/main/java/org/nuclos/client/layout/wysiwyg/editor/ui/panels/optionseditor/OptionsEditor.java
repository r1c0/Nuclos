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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.optionseditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BORDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.OPTIONS_EDITOR;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.TranslationPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOption;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

/**
 * This Editor is used to edit a {@link WYSIWYGOptions} Object.<br>
 * Used for {@link WYSIWYGCollectableOptionGroup}<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class OptionsEditor extends JDialog implements SaveAndCancelButtonPanelControllable {

	private int height = 500;
	private int width = 470;

	private JLabel lblName = new JLabel(OPTIONS_EDITOR.LABEL_OPTIONGROUP_NAME);
	private JTextField txtName = new JTextField(15);

	private JLabel lblDefault = new JLabel(OPTIONS_EDITOR.LABEL_OPTIONGROUP_DEFAULT_VALUE);
	private JComboBox cbxDefault = new JComboBox();

	private JLabel lblOrientation = new JLabel(OPTIONS_EDITOR.LABEL_OPTIONGROUP_ORIENTATION);
	private JComboBox cbxOrientation = new JComboBox();

	private JPanel optionContainer = null;

	private WYSIWYGOptions options;

	private WYSIWYGOptions backupOptions;

	public static WYSIWYGOptions returnOptions;

	private final String HORIZONTAL_LABEL = OPTIONS_EDITOR.LABEL_ORIENTATION_HORIZONTAL;
	private final String VERTICAL_LABEL = OPTIONS_EDITOR.LABEL_ORIENTATION_VERTICAL;

	/**
	 * @param options the {@link WYSIWYGOptions} to be edited by this Editor
	 */
	private OptionsEditor(WYSIWYGOptions options) {
		this.setIconImage(NuclosIcons.getInstance().getScaledDialogIcon(48).getImage());
	
		//TODO align relative to parent Component
		this.options = options;
		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, TableLayout.PREFERRED}};
		this.setLayout(new TableLayout(layout));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		txtName.setText(this.options.getName());
		txtName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				changeValueForName();
			}
		});

		for (WYSIWYGOption option : options.getAllOptionValues()) {
			cbxDefault.addItem(option.getValue());
		}
		cbxDefault.setSelectedItem(options.getDefaultValue());

		cbxDefault.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeValueForDefaultItem(e);
			}
		});

		cbxOrientation.addItem(HORIZONTAL_LABEL);
		cbxOrientation.addItem(VERTICAL_LABEL);
		//NUCLEUSINT-228 restore does now map the value to be shown to the value to be set
		if (LayoutMLConstants.ATTRIBUTEVALUE_VERTICAL.equals(options.getOrientation()))
			cbxOrientation.setSelectedItem(VERTICAL_LABEL);
		else if (LayoutMLConstants.ATTRIBUTEVALUE_HORIZONTAL.equals(options.getOrientation()))
			cbxOrientation.setSelectedItem(HORIZONTAL_LABEL);

		cbxOrientation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeValueForOrientation(e);
			}
		});

		/**
		 * the valuelist provider type panel
		 */
		JPanel valuecontainer = new JPanel();
		valuecontainer.setLayout(new TableLayout(new double[][]{{
			TableLayout.PREFERRED,
			InterfaceGuidelines.MARGIN_BETWEEN,
			TableLayout.PREFERRED, 
			TableLayout.FILL, 
			TableLayout.PREFERRED}, {
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED}}));
		valuecontainer.add(lblName, "0,0");
		TableLayoutConstraints constraint = new TableLayoutConstraints(2, 0, 2, 0, TableLayout.FULL, TableLayout.CENTER);
		valuecontainer.add(txtName, constraint);
		valuecontainer.add(lblDefault, "0,2");
		constraint = new TableLayoutConstraints(2, 2, 2, 2, TableLayout.FULL, TableLayout.CENTER);
		valuecontainer.add(cbxDefault, constraint);
		valuecontainer.add(lblOrientation, "0,4");
		constraint = new TableLayoutConstraints(2, 4, 2, 4, TableLayout.FULL, TableLayout.CENTER);
		valuecontainer.add(cbxOrientation, constraint);

		JButton addOption = new JButton(OPTIONS_EDITOR.BUTTON_ADD_OPTION);
		addOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addOptionPanelIntoPanel(null);
			}
		});

		valuecontainer.add(addOption, "4,0");

		this.add(valuecontainer, "1,1");

		/**
		 * the options
		 */
		optionContainer = new JPanel();
		optionContainer.setLayout(new TableLayout(new double[][]{{TableLayout.FILL}, {}}));

		JScrollPane scrollbar = new JScrollPane(optionContainer);
		scrollbar.getVerticalScrollBar().setVisible(true);
		scrollbar.getVerticalScrollBar().setUnitIncrement(20);
		this.add(scrollbar, "1,3");

		/**
		 * save and cancel
		 */

		constraint = new TableLayoutConstraints(0, 4, 2, 4);
		
		this.add(new SaveAndCancelButtonPanel(optionContainer.getBackground(), this, null), constraint);

		try {
			backupOptions = (WYSIWYGOptions) options.clone();
		} catch (CloneNotSupportedException e1) {
			/** nothing to do, does support clone() */
		}

		for (WYSIWYGOption option : this.options.getAllOptionValues()) {
			addOptionPanelIntoPanel(option);
		}

		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - width) / 2;
		int y = (screenSize.height - height) / 2;
		this.setBounds(x, y, width, height);
		this.setModal(true);
		this.setVisible(true);
	}

	/**
	 * This Method changes the Orientation of the {@link WYSIWYGCollectableOptionGroup}
	 * @param e
	 */
	protected void changeValueForOrientation(ItemEvent e) {
		if (VERTICAL_LABEL.equals(e.getItem().toString()))
			options.setOrientation(WYSIWYGOptions.ORIENTATION_VERTICAL);
		else if (HORIZONTAL_LABEL.equals(e.getItem().toString()))
			options.setOrientation(WYSIWYGOptions.ORIENTATION_HORIZONTAL);
	}
	
	/**
	 * This Method sets the DefaultItem to be selected as Defaultvalue
	 * @param e
	 */
	protected void changeValueForDefaultItem(ItemEvent e) {
		options.setDefaultValue(e.getItem().toString());
	}

	/**
	 * This Method changes the Name of this {@link WYSIWYGOptions} Object
	 */
	protected void changeValueForName() {
		options.setName(txtName.getText());
	}

	/**
	 * This Method is to be called to open a new Editor.<br>
	 * Works like {@link JOptionPane#showInputDialog(Object)}
	 * @param options the {@link WYSIWYGOptions} to be edited
	 * @return the edited {@link WYSIWYGOptions} Object
	 */
	public static WYSIWYGOptions showEditor(WYSIWYGOptions options) {
		new OptionsEditor(options);

		return returnOptions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		options = null;
		options = backupOptions;
		returnOptions = backupOptions;
		this.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		for (WYSIWYGOption option : options.getAllOptionValues()) {
			if (StringUtils.isNullOrEmpty(option.getValue()) || StringUtils.isNullOrEmpty(option.getLabel())) {
				JOptionPane.showMessageDialog(this, OPTIONS_EDITOR.ERROR_INCOMPLETE_OPTION);
				return;
			}
		}
		
		if (StringUtils.isNullOrEmpty(options.getDefaultValue())) {
			JOptionPane.showMessageDialog(this, OPTIONS_EDITOR.ERROR_DEFAULTVALUE_OPTIONS);
			return;
		}
		
		returnOptions = options;
		this.dispose();
	}

	/**
	 * @param optionPanel the {@link OptionPanel} to be removed
	 */
	public void removeOptionFromPanel(OptionPanel optionPanel) {
		this.options.removeOptionFromOptionsGroup(optionPanel.getOption());
		TableLayout tablelayout = (TableLayout) optionContainer.getLayout();
		TableLayoutConstraints constraint = tablelayout.getConstraints(optionPanel);
		int row = constraint.row1;
		if (row - 1 < 0)
			row = 0;
		else
			row = row - 1;

		optionContainer.remove(optionPanel);
		removeOneRow(row);

		cbxDefault.removeAllItems();
		for (WYSIWYGOption option : options.getAllOptionValues()) {
			cbxDefault.addItem(option.getValue());
		}
		cbxDefault.setSelectedItem(options.getDefaultValue());
		
		if (options.getAllOptionValues().size() == 1) {
			for (Component c : optionContainer.getComponents()) {
				if (c instanceof OptionPanel) {
					((OptionPanel)c).addRemoveRowsFromPanel.disableDeleteButton();
				}
			}
		}
		
		optionContainer.updateUI();
	}

	/**
	 * @param option the {@link WYSIWYGOption} to be added
	 */
	public void addOptionPanelIntoPanel(WYSIWYGOption option) {
		WYSIWYGOption newOption = option;
		if (newOption == null) {
			newOption = new WYSIWYGOption();
			options.addOptionToOptionsGroup(newOption);
			
			if (options.getAllOptionValues().size() == 2) {
				for (Component c : optionContainer.getComponents()) {
					if (c instanceof OptionPanel) {
						((OptionPanel)c).addRemoveRowsFromPanel.enableDeleteButton();
					}
				}
			}
		}
		OptionPanel newPanel = new OptionPanel(newOption);

		expandLayout();
		optionContainer.add(newPanel, "0,0");
		optionContainer.updateUI();
	}

	/**
	 * Expanding the Layout on one Row
	 */
	private void expandLayout() {
		TableLayout tablelayout = (TableLayout) optionContainer.getLayout();
		tablelayout.insertRow(0, InterfaceGuidelines.MARGIN_BETWEEN);
		tablelayout.insertRow(0, TableLayout.PREFERRED);
	}

	/**
	 * @param row the Row to remove from the Layout
	 */
	private void removeOneRow(int row) {
		TableLayout tablelayout = (TableLayout) optionContainer.getLayout();
		tablelayout.deleteRow(row);
		tablelayout.deleteRow(row);
	}

	/**
	 * This class wraps a {@link WYSIWYGOption} in a JPanel to make it visual editable.<br>
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	class OptionPanel extends JPanel implements AddRemoveButtonControllable {

		WYSIWYGOption option = null;
		private JTextField txtName = null;
		private JTextField txtValue = null;
		private JTextField txtLabel = null;
		private JTextField txtMnemonic = null;
		
		private AddRemoveRowsFromPanel addRemoveRowsFromPanel;

		/**
		 * Default Constructor
		 * @param option to be attached to this {@link OptionPanel}
		 */
		public OptionPanel(WYSIWYGOption option) {
			this.option = option;
			double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, 
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN,
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED, 
				InterfaceGuidelines.MARGIN_BETWEEN, 
				TableLayout.PREFERRED}, 
				
				{InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED,
				InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED,
				InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}};

			this.setLayout(new TableLayout(layout));
			JLabel lblName = new JLabel(OPTIONS_EDITOR.LABEL_OPTION_NAME);
			JLabel lblValue = new JLabel(OPTIONS_EDITOR.LABEL_OPTION_VALUE);
			JLabel lblLabel = new JLabel(OPTIONS_EDITOR.LABEL_OPTION_LABEL);
			JLabel lblMnemonic = new JLabel(OPTIONS_EDITOR.LABEL_OPTION_MNEMONIC);

			txtName = new JTextField(15);
			txtName.setText(option.getName());
			txtName.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForName();
				}
			});

			txtValue = new JTextField(15);
			txtValue.setText(option.getValue());
			txtValue.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForValue();
				}
			});

			txtLabel = new JTextField(15);
			txtLabel.setText(option.getLabel());
			txtLabel.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForLabel();
				}
			});

			txtMnemonic = new JTextField(15);
			txtMnemonic.setText(option.getMnemonic());
			txtMnemonic.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					changeValueForMnemonic();
				}
			});

			JButton button = new JButton(BUTTON_LABELS.LABEL_EDIT);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WYSIWYGOption option = OptionPanel.this.option;
					TranslationMap translations = option.getTranslations();
					if (translations == null) {
						translations = new TranslationMap();
					}
					Map<String, String> res = TranslationPanel.showDialog(OptionPanel.this, translations, option.getLabel());
					if (res != null) {
						translations.merge(res);
						option.setTranslations(translations);
					}
				}
			});

			this.add(lblName, "1,1");
			TableLayoutConstraints constraint = new TableLayoutConstraints(3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtName, constraint);
			this.add(lblValue, "5,1");
			constraint = new TableLayoutConstraints(7, 1, 7, 1, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtValue, constraint);
			this.add(lblLabel, "1,3");
			constraint = new TableLayoutConstraints(3, 3, 3, 3, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtLabel, constraint);
			this.add(lblMnemonic, "5,3");
			constraint = new TableLayoutConstraints(7, 3, 7, 3, TableLayout.FULL, TableLayout.CENTER);
			this.add(txtMnemonic, constraint);
			this.add(new JLabel(BORDER_EDITOR.LABEL_TRANSLATIONS_PROPERTY), "1,5");
			this.add(button, "3,5");

			addRemoveRowsFromPanel = new AddRemoveRowsFromPanel(this.getBackground(), this);
			this.add(addRemoveRowsFromPanel, new TableLayoutConstraints(9,0,9,3));
		}

		/**
		 * This Method changes the Mnemonic for this {@link WYSIWYGOption}
		 */
		protected void changeValueForMnemonic() {
			this.option.setMnemonic(this.txtMnemonic.getText());
		}

		/**
		 * This Method changes the Label for this {@link WYSIWYGOption}
		 */
		protected void changeValueForLabel() {
			this.option.setLabel(this.txtLabel.getText());
		}

		/**
		 * This Method changes the Value for this {@link WYSIWYGOption}
		 */
		protected void changeValueForValue() {
			this.option.setValue(this.txtValue.getText());
			
			cbxDefault.removeAllItems();
			for (WYSIWYGOption option : options.getAllOptionValues()) {
				cbxDefault.addItem(option.getValue());
			}
			cbxDefault.setSelectedItem(options.getDefaultValue());
		}

		/**
		 * This Method changes the Name for this {@link WYSIWYGOption}
		 */
		protected void changeValueForName() {
			this.option.setName(this.txtName.getText());
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performAddAction()
		 */
		@Override
		public void performAddAction() {
			OptionsEditor.this.addOptionPanelIntoPanel(null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable#performRemoveAction()
		 */
		@Override
		public void performRemoveAction() {
			OptionsEditor.this.removeOptionFromPanel(this);
		}

		/**
		 * @return the {@link WYSIWYGOption} attached to this {@link OptionPanel}
		 */
		public WYSIWYGOption getOption() {
			return this.option;
		}
	}
}
