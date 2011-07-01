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
package org.nuclos.client.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * @author marcel.billen
 */
public class AutoCompleterFilter extends DocumentFilter {
  private FilterWindowListener fwl;
  private JWindow win;
  private ListSelectionListener lsl;
  private MouseAdapter lml;
  private JList lst;
  private JScrollPane sp;
  private FilterListModel lm;
  private JTextField textField;
  private List<String> itemList = new ArrayList<String>();
  private String preText;
  private int firstSelectedIndex = -1;
  private boolean isAdjusting = false;

	public AutoCompleterFilter(JTextField tf) {
		textField = tf;
		fwl = new FilterWindowListener();
		lm = new FilterListModel(itemList);
		textField.addKeyListener(new TextFieldKeyListener());
		textField.registerKeyboardAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isFilterWindowVisible())
					setFilterWindowVisible(false);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	public void setAutoCompleteItems(List<String> items) {
		if(isFilterWindowVisible())
			setFilterWindowVisible(false);

		itemList = items;
		firstSelectedIndex = -1;
		lm.setAutoCompleteItems(items);
	}

	@Override
	public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException {
		setFilterWindowVisible(false);
		super.insertString(filterBypass, offset, string, attributeSet);
	}

	@Override
	public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
		setFilterWindowVisible(false);
		super.remove(filterBypass, offset, length);
	}

	@Override
	public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
		if(isAdjusting || "".equals(string) || !textField.isShowing()) {
			filterBypass.replace(offset, length, string, attributeSet);
			return;
		}

		super.replace(filterBypass, offset, length, string, attributeSet);
		Document doc = filterBypass.getDocument();
		preText = doc.getText(0, doc.getLength());
		firstSelectedIndex = -1;

		for(int i = 0; i < itemList.size(); i++) {
			String itemString = itemList.get(i).toString();

			if(itemString.equalsIgnoreCase(preText)) {
				firstSelectedIndex = i;
				break;
			}

			if(itemString.length() <= preText.length())
				continue;

			if(itemString.substring(0, preText.length()).equalsIgnoreCase(preText)) {
				/* do not auto select first NUCLOSINT-974
				String objStringEnd = itemString.substring(preText.length());
				filterBypass.insertString(preText.length(), objStringEnd, attributeSet);
				textField.select(preText.length(), doc.getLength());
				*/
				firstSelectedIndex = i;
				break;
			}
		}

		if(firstSelectedIndex == -1) {
			if(isFilterWindowVisible())
				setFilterWindowVisible(false);

			return;
		}

		lm.setFilter(preText);

		if(!isFilterWindowVisible())
			setFilterWindowVisible(true);
		else
			setWindowDimension();
		
		/* do not auto select first NUCLOSINT-974
		lst.setSelectedValue(textField.getText(), true);
		 */
	}

	private boolean isFilterWindowVisible() {
		return ((win != null) && (win.isVisible()));
	}
	
	private void setWindowDimension() {
		int height = lst.getFixedCellHeight() * Math.min(5, lm.getSize());
		int width = textField.getWidth();
		
		height += lst.getInsets().top + lst.getInsets().bottom;
		height += sp.getInsets().top + sp.getInsets().bottom;
		
		win.setSize(width, height);
		sp.setSize(width, height);
	}

	private void setFilterWindowVisible(boolean visible) {
		if(visible) {
			Window ancestor = SwingUtilities.getWindowAncestor(textField);
			win = new JWindow(ancestor);
			win.addWindowFocusListener(fwl);
			textField.addAncestorListener(fwl);
			ancestor.addMouseListener(fwl);
			lsl = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					isAdjusting = true;
					textField.setText(lst.getSelectedValue().toString());
					isAdjusting = false;
					textField.select(preText.length(), textField.getText().length());
				}
			};
			lml = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2)
						setFilterWindowVisible(false);
				}
			};
			lst = new JList(lm);
			lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lst.setFocusable(false);
			lst.setPrototypeCellValue("XXXXXXXXXX");
			lst.addListSelectionListener(lsl);
			lst.addMouseListener(lml);
			sp = new JScrollPane(lst, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setFocusable(false);
			sp.getVerticalScrollBar().setFocusable(false);
			setWindowDimension();
			win.setLocation(textField.getLocationOnScreen().x, textField.getLocationOnScreen().y + textField.getHeight());
			win.getContentPane().add(sp);
			lst.setModel(lm);
			win.setVisible(true);
			textField.requestFocus();
			textField.addFocusListener(fwl);
		} else {
			if(win == null)
				return;
			win.setVisible(false);
			win.removeFocusListener(fwl);
			Window ancestor = SwingUtilities.getWindowAncestor(textField);
			ancestor.removeMouseListener(fwl);
			textField.removeFocusListener(fwl);
			textField.removeAncestorListener(fwl);
			lst.removeMouseListener(lml);
			lst.removeListSelectionListener(lsl);
			lsl = null;
			lml = null;
			win.dispose();
			win = null;
			lst = null;
		}
	}

	private class FilterWindowListener extends MouseAdapter implements AncestorListener, FocusListener, WindowFocusListener {
		@Override
		public void ancestorMoved(AncestorEvent event) {
			setFilterWindowVisible(false);
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
			setFilterWindowVisible(false);
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			setFilterWindowVisible(false);
		}

		@Override
		public void focusLost(FocusEvent e) {
			if(e.getOppositeComponent() != win)
				setFilterWindowVisible(false);
		}

		@Override
		public void focusGained(FocusEvent e) {}

		@Override
		public void windowLostFocus(WindowEvent e) {
			if(e.getOppositeWindow().getFocusOwner() != textField)
				setFilterWindowVisible(false);
		}

		@Override
		public void windowGainedFocus(WindowEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			setFilterWindowVisible(false);
		}
	}

	private class TextFieldKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if(!((e.getKeyCode() == KeyEvent.VK_DOWN)
				|| (e.getKeyCode() == KeyEvent.VK_UP)
				|| ((e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) && (isFilterWindowVisible()))
				|| ((e.getKeyCode() == KeyEvent.VK_PAGE_UP) && (isFilterWindowVisible()))
				|| (e.getKeyCode() == KeyEvent.VK_ENTER)
				|| (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)))
				return;

			if((e.getKeyCode() == KeyEvent.VK_DOWN) && !isFilterWindowVisible()) {
				preText = textField.getText();
				lm.setFilter(preText);

				if(lm.getSize() > 0)
					setFilterWindowVisible(true);
				else
					return;
			}

			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				if(isFilterWindowVisible())
					setFilterWindowVisible(false);

				textField.setCaretPosition(textField.getText().length());
				return;
			}

			int index = -1;

			if(e.getKeyCode() == KeyEvent.VK_DOWN)
				index = Math.min(lst.getSelectedIndex() + 1, lst.getModel().getSize() - 1);
			else if(e.getKeyCode() == KeyEvent.VK_UP)
				index = Math.max(lst.getSelectedIndex() - 1, 0);
			else if(e.getKeyCode() == KeyEvent.VK_PAGE_UP)
				index = Math.max(lst.getSelectedIndex() - 5, 0);
			else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				index = Math.min(lst.getSelectedIndex() + 5, lst.getModel().getSize() - 1);

			if(index == -1)
				return;

			lst.setSelectedIndex(index);
			lst.scrollRectToVisible(lst.getCellBounds(index, index));
		}
	}

	private class FilterListModel extends AbstractListModel {
		private List<String> fullList;
		private List<String> filteredList;

		public FilterListModel(List<String> unfilteredList) {
			fullList = unfilteredList;
			filteredList = new ArrayList<String>(unfilteredList);
		}

		@Override
		public int getSize() {
			return filteredList.size();
		}

		@Override
		public Object getElementAt(int index) {
			return filteredList.get(index);
		}

		public void setFilter(String filter) {
			filteredList.clear();
			for (String s : fullList) {
				if (s.toString().length() < filter.length())
					continue;

				if (s.toString().substring(0, filter.length()).compareToIgnoreCase(filter) == 0)
					filteredList.add(s);
			}
			fireContentsChanged(this, 0, filteredList.size());
		}

		public void clearFilter() {
			filteredList = new ArrayList<String>(fullList);
		}

		public void setAutoCompleteItems(List<String> autoCompleteItems) {
			fullList = autoCompleteItems;
			clearFilter();
		}
	}
}
