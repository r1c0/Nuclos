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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A List of Values (LOV), as in Oracle Forms.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class ListOfValues extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int QUICKSEARCH_DELAY_TIME = 756;
	
	private static final int QUICKSEARCH_POPUP_ROWS = 16;
	
	private ToolTipTextProvider tooltiptextprovider;
	
	private QuickSearchResulting quickSearchResulting;
	
	private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
	
	private static final KeyStroke UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true);
	
	private static final KeyStroke DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true);
	
	private static final KeyStroke ESC = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
	
	private static final KeyStroke DELETE = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true);
	
	private static final KeyStroke BACK_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, true);
	
	private static final String QUICK_SEARCH = "quickSearch";
	
	private static final String QUICK_SEARCH_NAV_UP = "quickSearchNavUp";
	
	private static final String QUICK_SEARCH_NAV_DOWN = "quickSearchNavDown";
	
	private static final String QUICK_SEARCH_CANCEL = "quickSearchCancel";
	
	private QuickSearchSelectedListener quickSearchSelectedListener;
	
	private ActionListener quickSearchCanceledListener;
	
	private boolean searchOnLostFocus = true;
	
	private boolean transferQuickSearchValue = true;
	
	private boolean quickSearchEnabled = true;
	
	private boolean alertRunning = false;
	
	private boolean searchRunning = false;
	
	private boolean changesPending = false;
	
	private boolean clearOnEmptyInput = false;
	
	private final InputChanged inputChanged = new InputChanged();
	
	private final LovDocumentListener lovDocumentListener = new LovDocumentListener();
	
	private final QuickSearchAction actionSearch = new QuickSearchAction();
	
	private SearchingWorker lastSearchingWorker = null;
	
	private Timer lastTimer = null;

	private final TextFieldWithButton tf = new TextFieldWithButton(Icons.getInstance().getIconTextFieldButtonLOV()) {		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = ListOfValues.this.tooltiptextprovider;
			return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
		}

		@Override
		public boolean isButtonEnabled() {
			return ListOfValues.this.btnBrowse.isEnabled();
		}

		@Override
		public void buttonClicked() {
			ListOfValues.this.btnBrowse.doClick();
		}

		@Override
		public boolean isEditable() {
			return super.isEditable() && quickSearchEnabled;
		}
		
		@Override
		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed) {
			
			//System.out.println(ks);
			
			if (e.getKeyCode() == ENTER.getKeyCode()) {
				if (ks.getModifiers() == 0) {
					if (!pressed) {
						if (ListOfValues.this.cbxQuickChooser.isPopupVisible()) {
							actionPerformedQuickSearchSelected();
						} else {
							SwingUtilities.notifyAction(new QuickSearchAction(), ks, e, ListOfValues.this.tf,
								   e.getModifiers());
						}
					} 
					return true;
				} else {
					return super.processKeyBinding(ks, e, condition, pressed);
				}
			} else if (e.getKeyCode() == UP.getKeyCode()) {
				if (ListOfValues.this.cbxQuickChooser.isPopupVisible()) {
					if (!pressed) {
						SwingUtilities.notifyAction(new QuickSearchNavigationAction(true), ks, e, ListOfValues.this.tf,
							   e.getModifiers());
					}
					return true;
				} 
			} else if (e.getKeyCode() == DOWN.getKeyCode()) {
				if (ListOfValues.this.cbxQuickChooser.isPopupVisible()) {
					if (!pressed) {
						SwingUtilities.notifyAction(new QuickSearchNavigationAction(false), ks, e, ListOfValues.this.tf,
							   e.getModifiers());
					}
					return true;
				} 
			} else if (e.getKeyCode() == ESC.getKeyCode()) {
				if (!pressed) {
					SwingUtilities.notifyAction(new QuickSearchCancelAction(), ks, e, ListOfValues.this.tf,
						   e.getModifiers());
				}
				return true;
			} else if (e.getKeyCode() == DELETE.getKeyCode() || e.getKeyCode() == BACK_SPACE.getKeyCode()) {
				boolean result = super.processKeyBinding(ks, e, condition, pressed);
				clearOnEmptyInput = true;
				if (ListOfValues.this.cbxQuickChooser.isPopupVisible()) {
					inputChanged.handleUpdate();
				}
				return result;
			} else if (e.getKeyCode() != 0) {
				boolean result = super.processKeyBinding(ks, e, condition, pressed);
				inputChanged.enable();
				return result;
			}
			return super.processKeyBinding(ks, e, condition, pressed);
		}
		
		
	};

	private final JButton btnBrowse = new JButton();
	
	private final JComboBox cbxQuickChooser = new JComboBox() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Dimension getSize() {
			return ListOfValues.this.tf.getSize();
		}

		@Override
		public Rectangle getBounds() {
			return ListOfValues.this.tf.getBounds();
		}
		
		@Override
		 public Point getLocationOnScreen() {
			return ListOfValues.this.tf.getLocationOnScreen();
		}

		@Override
		public boolean isShowing() {
			return ListOfValues.this.tf.isShowing();
		}
		
	};

	public ListOfValues() {
		super(new BorderLayout(2, 0));

		this.setOpaque(false);
		super.addFocusListener(new LovFocusListener());

		this.add(this.tf, BorderLayout.CENTER);
		this.tf.getDocument().addDocumentListener(lovDocumentListener);
		this.tf.addFocusListener(new LovTfFocusListener());
		this.tf.addKeyListener(new LovKeyListener());
		
		this.add(this.cbxQuickChooser, BorderLayout.SOUTH);
		this.cbxQuickChooser.setVisible(false);
		this.cbxQuickChooser.setMaximumRowCount(QUICKSEARCH_POPUP_ROWS);
		this.cbxQuickChooser.addActionListener(new QuickSearchActionListener());
		this.cbxQuickChooser.setRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (value instanceof String) {
					return new JLabel((String) value);
				}
				
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});
		
		this.tf.getActionMap().put(QUICK_SEARCH, new QuickSearchAction());
		this.tf.getActionMap().put(QUICK_SEARCH_NAV_UP, new QuickSearchNavigationAction(true));
		this.tf.getActionMap().put(QUICK_SEARCH_NAV_DOWN, new QuickSearchNavigationAction(false));
		this.tf.getActionMap().put(QUICK_SEARCH_CANCEL, new QuickSearchCancelAction());
	}
	
	/**
	 * 
	 * @param enabled
	 */
	public void setQuickSearchEnabled(boolean enabled) {
		this.quickSearchEnabled = enabled;
	}

	public JTextField getJTextField() {
		return this.tf;
	}

	public JButton getBrowseButton() {
		return btnBrowse;
	}

	/**
	 * sets the number of columns of the textfield
	 * @param iColumns
	 */
	public void setColumns(int iColumns) {
		this.tf.setColumns(iColumns);
	}

	@Override
	public void setToolTipText(String sText) {
		this.tf.setToolTipText(sText);
	}

	public void setToolTipTextProvider(ToolTipTextProvider tooltiptextprovider) {
		this.tooltiptextprovider = tooltiptextprovider;
		if (tooltiptextprovider != null) {
			ToolTipManager.sharedInstance().registerComponent(this.getJTextField());
		}
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);

		this.tf.setEditable(bEnabled);
		this.btnBrowse.setEnabled(bEnabled);
	}

	public void setBackgroundColorProviderForTextField(ColorProvider colorproviderBackground) {
		this.tf.setBackgroundColorProviderForTextField(colorproviderBackground);
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.tf, sName, "tf");
		UIUtils.setCombinedName(this.btnBrowse, sName, "btnBrowse");
	}
	
	@Override
	public boolean hasFocus() {
		return tf.hasFocus();
	}
	
	@Override
	public synchronized void addFocusListener(FocusListener l) {
		tf.addFocusListener(l);
	}

	@Override
	public synchronized void removeFocusListener(FocusListener l) {
		tf.removeFocusListener(l);
	}
	
	class LovFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			ListOfValues.this.tf.requestFocus();
		}
		@Override
		public void focusLost(FocusEvent e) {
		}
	}
	
	class LovTfFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			tf.selectAll();
		}
		@Override
		public void focusLost(FocusEvent e) {
			if (cbxQuickChooser.isPopupVisible()) {
				actionPerformedQuickSearchSelected();
			} else if (clearOnEmptyInput && StringUtils.looksEmpty(tf.getText())) {
				actionPerformedQuickSearchSelected(true);
			} else if (changesPending) {
				if (searchOnLostFocus) {
					actionSearch.actionPerformed(true);
				} else {
					if (lastTimer != null) lastTimer.cancel();
					if (lastSearchingWorker != null) lastSearchingWorker.setStopped(true);
				}
			}
			clearOnEmptyInput = false;
			changesPending = false;
		}
	}
	
	class LovKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if ((e.isMetaDown() || e.isControlDown()) && e.getKeyCode() == KeyEvent.VK_V) {
				inputChanged.enable();
			}
		}
	}
	
	class LovDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			inputChanged.handleUpdate();}
		@Override
		public void removeUpdate(DocumentEvent e) {
			if (ListOfValues.this.cbxQuickChooser.isPopupVisible()) {
				inputChanged.enable();
			}
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			inputChanged.handleUpdate();
		}
	}
	
	class InputChanged extends Object {
		private long enabledAt = 0l;
		
		private void handleUpdate() {
			if (enabledAt+25 >= System.currentTimeMillis()) {
				// verarbeite nur Events die 25 ms nach einer Tastatureingabe getätigt wurden.		
				
				changesPending = true;
				
				if (lastTimer != null) {
					lastTimer.cancel();
				}
				
				lastTimer = new Timer(this.getClass().getName() + " quick-search-delay");
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						UIUtils.invokeOnDispatchThread(new Runnable() {
							@Override
							public void run() {
								if (tf.getText().length() >= 2) {
									actionSearch.actionPerformed(false);
								}
							}
						});
					}
				};
				lastTimer.schedule(task, QUICKSEARCH_DELAY_TIME);
			}
		}
		
		public void enable() {
			enabledAt = System.currentTimeMillis();
		}
	}
	
	/**
	 * 
	 */
	private void hideQuickSearch() {
		ListOfValues.this.cbxQuickChooser.hidePopup();
	}
	
	/**
	 * 
	 *
	 */
	class QuickSearchActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (transferQuickSearchValue)
				actionPerformedQuickSearchSelected();
		}
	}
	
	/**
	 * 
	 *
	 */
	class QuickSearchNavigationAction extends AbstractAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final boolean navUp;

		public QuickSearchNavigationAction(boolean navUp) {
			this.navUp = navUp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				ListOfValues.this.transferQuickSearchValue = false;
				JComboBox cbx = ListOfValues.this.cbxQuickChooser;
				if (cbx.isPopupVisible()) {
					
					InputMap map = cbx.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
					ActionMap am = cbx.getActionMap();
	
			        if	(map != null && am != null && isEnabled()) {
			        	KeyStroke ks = KeyStroke.getKeyStroke((navUp ? KeyEvent.VK_UP : KeyEvent.VK_DOWN), 0);
			        	KeyEvent ke = new KeyEvent(
							ListOfValues.this.cbxQuickChooser, 
							KeyEvent.KEY_RELEASED, 
							e.getWhen(), 
							e.getModifiers(), 
							(navUp ? KeyEvent.VK_UP : KeyEvent.VK_DOWN), 
							KeyEvent.CHAR_UNDEFINED);
							
			        	Object binding = map.get(ks);
			        	Action action = (binding == null) ? null : am.get(binding);
			        	if (action != null) {
			        		SwingUtilities.notifyAction(action, ks, ke, cbx, ke.getModifiers());
			        	}
					}
				}
			} finally {
				ListOfValues.this.transferQuickSearchValue = true;
			}
		}		
	}

	/**
	 * 
	 *
	 */
	class QuickSearchAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {		
			actionPerformed(false);
		}
		
		public void actionPerformed(boolean forceSelect) {
			final String inputString = ListOfValues.this.tf.getText();
			if (StringUtils.looksEmpty(inputString) || !ListOfValues.this.tf.isEditable() || alertRunning)
				return;
			
			if (lastSearchingWorker != null) {
				lastSearchingWorker.setStopped(true);
			}
			lastSearchingWorker = new SearchingWorker(inputString, forceSelect);
			CommonMultiThreader.getInstance().executeInterruptible(lastSearchingWorker);
			changesPending = false;
		}
	}
	
	class SearchingWorker implements CommonClientWorker {
		
		private final String inputString;
		private final boolean forceSelect;
		
		private boolean stopped = false;
		List<CollectableValueIdField> qsResult;

		public SearchingWorker(String inputString, boolean forceSelect) {
			super();
			this.inputString = inputString;
			this.forceSelect = forceSelect;
		}

		@Override
		public void work() throws CommonBusinessException {
			qsResult = (quickSearchResulting != null ? quickSearchResulting.getQuickSearchResult(inputString) : new ArrayList<CollectableValueIdField>());
			
			if (!searchRunning) 
				return;
			searchRunning = false;
		}
		
		@Override
		public void paint() throws CommonBusinessException {
			if (isStopped())
				return;
			
			ListOfValues.this.transferQuickSearchValue = false;		
			ListOfValues.this.cbxQuickChooser.removeAllItems();
			switch (qsResult.size()) {
				case 0:
					ListOfValues.this.alertQuickSearch(ListOfValues.Alert.NO_RESULT_FOUND);
					if (forceSelect) {
						ListOfValues.this.actionPerformedQuickSearchSelected();
					}
					break;
				case 1:
					ListOfValues.this.cbxQuickChooser.addItem(qsResult.get(0));
					ListOfValues.this.actionPerformedQuickSearchSelected();
//					ListOfValues.this.alertQuickSearch(ListOfValues.Alert.ONE_RESULT_SELECTION);
					break;
				default:
					if (forceSelect) {
						ListOfValues.this.alertQuickSearch(ListOfValues.Alert.NO_RESULT_FOUND);
						ListOfValues.this.actionPerformedQuickSearchSelected();
						break;
					}
					
					for (Object item : qsResult) {
						ListOfValues.this.cbxQuickChooser.addItem(item);
					}
					
					if (qsResult.size() == ICollectableListOfValues.QUICKSEARCH_MAX) {
						ListOfValues.this.cbxQuickChooser.addItem("...");
					}
					
					if (qsResult.size() < QUICKSEARCH_POPUP_ROWS) {
						for (int i = 1; i < QUICKSEARCH_POPUP_ROWS - qsResult.size(); i++) {
							ListOfValues.this.cbxQuickChooser.addItem(" ");
						}
					}
					
					if (!ListOfValues.this.cbxQuickChooser.isPopupVisible())
						ListOfValues.this.cbxQuickChooser.showPopup();
			}
			
			ListOfValues.this.transferQuickSearchValue = true;
		}
		
		@Override
		public void init() throws CommonBusinessException {
			ListOfValues.this.searchRunning = true;
		}
		
		@Override
		public void handleError(Exception ex) {
			if (ex instanceof IllegalComponentStateException) {
				// do nothing. popup could not be shown. maybe the frame is closed.
			} else {
				Errors.getInstance().showExceptionDialog(getResultsComponent(), ex);
			}
		}
		
		@Override
		public JComponent getResultsComponent() {
			return ListOfValues.this.tf;
		}

		public boolean isStopped() {
			return stopped;
		}

		public void setStopped(boolean stopped) {
			this.stopped = stopped;
		}
	};
	
	/**
	 * 
	 *
	 */
	class QuickSearchCancelAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (searchRunning) {
				searchRunning = false;
				ListOfValues.this.setEnabled(true);
			}
			if (ListOfValues.this.quickSearchCanceledListener != null) {
				ListOfValues.this.quickSearchCanceledListener.actionPerformed(new ActionEvent(ListOfValues.this, 0, ListOfValues.QUICK_SEARCH_CANCEL));
			}
			changesPending = false;
			hideQuickSearch();
		}
	}
	
	/**
	 * 
	 */
	private void actionPerformedQuickSearchSelected() {
		actionPerformedQuickSearchSelected(false);
	}
	
	/**
	 * 
	 * @param forceClear
	 */
	private void actionPerformedQuickSearchSelected(boolean forceClear) {
		if (this.quickSearchSelectedListener != null) {
			if (forceClear) {
				this.quickSearchSelectedListener.actionPerformed(null);
			} else {
				if (ListOfValues.this.cbxQuickChooser.getSelectedIndex() >= 0 &&
					ListOfValues.this.cbxQuickChooser.getSelectedIndex() < CollectableListOfValues.QUICKSEARCH_MAX && 
					ListOfValues.this.cbxQuickChooser.getSelectedItem() != null &&
					ListOfValues.this.cbxQuickChooser.getSelectedItem() instanceof CollectableValueIdField) {
					this.quickSearchSelectedListener.actionPerformed(((CollectableValueIdField) ListOfValues.this.cbxQuickChooser.getSelectedItem()));
				} else {
					this.quickSearchSelectedListener.actionPerformed(null);
				}
			}
		}
		if (this.cbxQuickChooser.isPopupVisible())
			this.cbxQuickChooser.hidePopup();
	}
	
	/**
	 * 
	 * @param qssl
	 */
	public void setQuickSearchSelectedListener(final QuickSearchSelectedListener qssl) {
		this.quickSearchSelectedListener = qssl;
	}
	
	/**
	 * 
	 * @param al
	 */
	public void setQuickSearchCanceledListener(final ActionListener al) {
		this.quickSearchCanceledListener = al;
	}
	
	/**
	 * 	
	 * @return
	 */
	public boolean isSearchOnLostFocus() {
		return searchOnLostFocus;
	}

	/**
	 * 
	 * @param searchOnLostFocus
	 */
	public void setSearchOnLostFocus(boolean searchOnLostFocus) {
		this.searchOnLostFocus = searchOnLostFocus;
	}
	
	public static abstract class QuickSearchResulting {
		/**
		 * 
		 * @param inputString
		 * @return
		 */
		protected abstract List<CollectableValueIdField> getQuickSearchResult(String inputString);	
	}
	
	/**
	 * 
	 *
	 */
	public static abstract class QuickSearchSelectedListener {
		public abstract void actionPerformed(CollectableValueIdField itemSelected);
	}
	
	/**
	 * 
	 *
	 */
	private enum Alert {
		ONE_RESULT_SELECTION,
		NO_RESULT_FOUND;
	}
	
	/**
	 * 
	 * @param alert
	 */
	private void alertQuickSearch(Alert alert) {
		alertRunning = true;
		final Color color;
		switch (alert) {
			case ONE_RESULT_SELECTION:
				color = new Color(180, 250, 170);
				break;
			case NO_RESULT_FOUND:
				color = new Color(250, 140, 140);
				break;
			default:
				color = Color.YELLOW;
		}
		
		final Color defaultColor = this.tf.getBackground();
		final ColorProvider colorProvider = this.tf.getBackgroundColorProviderForTextField();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().sleep(200);
					ListOfValues.this.tf.setBackgroundColorProviderForTextField(null);
					ListOfValues.this.tf.setBackground(color);
					ListOfValues.this.tf.repaint();
					
					Thread.currentThread().sleep(500);
					ListOfValues.this.tf.setBackground(defaultColor);
					ListOfValues.this.tf.repaint();
					ListOfValues.this.tf.setBackgroundColorProviderForTextField(colorProvider);
					
					alertRunning = false;
				} catch (Exception ex) {
					// do nothing...
				} 
			}
		}, "ListOfValues.AlertQuickSearch");
		
		t.start();
	}

	/**
	 * 
	 * @return
	 */
	public QuickSearchResulting getQuickSearchResulting() {
		return quickSearchResulting;
	}

	/**
	 * 
	 * @param quickSearchResulting
	 */
	public void setQuickSearchResulting(QuickSearchResulting quickSearchResulting) {
		this.quickSearchResulting = quickSearchResulting;
	}

}  // class ListOfValues
