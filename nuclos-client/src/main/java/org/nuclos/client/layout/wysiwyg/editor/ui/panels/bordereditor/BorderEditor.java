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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.jdesktop.swingx.JXPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BORDER_EDITOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMMON_LABELS;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.AddRemoveRowsFromPanel.AddRemoveButtonControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.MovePanelUpAndDown.MovePanelUpAndDownControllable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * The Bordereditor for visually creating a Border.<br>
 * Contains:
 * <ul>
 * <li> {@link BorderPreviewPanel} to see what was "clicked together" </li>
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
@SuppressWarnings("serial")
public class BorderEditor extends JDialog implements SaveAndCancelButtonPanelControllable, ListDataListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int width = 850;
	int height = 400;

	private JCheckBox checkBox = new JCheckBox();
	private JScrollPane scrollPane = null;
	private JXPanel borderPanel = new JXPanel();

	/** for storing the borders */
	private DefaultListModel model = new DefaultListModel();
	
	/** for making a preview */
	private BorderPreviewPanel previewPanel = new BorderPreviewPanel();
	
	private Border border = null;
	private boolean clearBorder;

	/**
	 * Constructor
	 * @param wysiwygComponent the {@link WYSIWYGComponent} to add the Border
	 * @param border the {@link Border} to be restored
	 * @param clearBorder the {@link LayoutMLConstants#ELEMENT_CLEARBORDER} Element
	 */
	public BorderEditor(WYSIWYGComponent wysiwygComponent, Border border, boolean clearBorder) {
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
				BorderPreviewPanel.PREVIEW_BORDER_SIZE, 
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
	 * @return the Border as is shows up in the {@link BorderPreviewPanel}
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
			} catch (ArrayIndexOutOfBoundsException e){}
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
	 * This Method removes all Borders and refeshes the {@link BorderPreviewPanel}
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

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
