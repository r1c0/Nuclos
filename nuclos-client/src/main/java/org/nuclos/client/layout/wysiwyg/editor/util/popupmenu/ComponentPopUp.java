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
package org.nuclos.client.layout.wysiwyg.editor.util.popupmenu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.IllegalComponentStateException;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMPONENT_POPUP;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.layoutmlrule.LayoutMLRuleEditorDialog;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;

/**
 * This class provides the ContextMenu shown for every {@link WYSIWYGComponent}.<br>
 * There are some Standardactions nearly every {@link WYSIWYGComponent} has like:
 * <ul>
 * <li> Move </li>
 * <li> Change the Alignment </li>
 * <li> Delete </li>
 * </ul>
 * It does load additional {@link MenuItem} from {@link WYSIWYGComponent#getAdditionalContextMenuItems(int)}
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ComponentPopUp {

	private static final Logger LOG = Logger.getLogger(ComponentPopUp.class);

	private JPopupMenu contextMenu = new JPopupMenu();
	private JMenuItem deleteComponent;
	private TableLayoutPanel contentPane = null;
	private WYSIWYGComponent wysiwygcomponent = null;
	private int xClick;
	private TableLayoutUtil tableLayoutUtil;

	/**
	 * 
	 * @param tableLayoutUtil
	 * @param component
	 */
	public ComponentPopUp(TableLayoutUtil tableLayoutUtil, Component component) {
		this(tableLayoutUtil, component, 0);
	}

	/**
	 * 
	 * @param tableLayoutUtil
	 * @param component
	 * @param x
	 */
	public ComponentPopUp(final TableLayoutUtil tableLayoutUtil, Component component, int x) {
		this.tableLayoutUtil = tableLayoutUtil;
		if (component instanceof WYSIWYGTabbedPane) {
			Component comp = ((WYSIWYGTabbedPane) component).getSelectedComponent();
			if (comp instanceof WYSIWYGLayoutEditorPanel) {
				this.tableLayoutUtil = ((WYSIWYGLayoutEditorPanel)comp).getTableLayoutUtil();
			}
		}
		this.contentPane = this.tableLayoutUtil.getContainer();
		
		/** find the fitting component */
		if (component instanceof WYSIWYGLayoutEditorPanel) {
			if (((WYSIWYGLayoutEditorPanel) component).getParentWrappingComponent() == null)
				wysiwygcomponent = (WYSIWYGLayoutEditorPanel) component;
			else
				wysiwygcomponent = ((WYSIWYGLayoutEditorPanel) component).getParentWrappingComponent();
		} else {
			while (!(component instanceof WYSIWYGComponent) && component != null) {
				component = component.getParent();
			}
			if (component != null) {
				wysiwygcomponent = (WYSIWYGComponent) component;
			}
		}

		this.xClick = x;

		/**
		 * Enable Movement of Component
		 */
		JMenuItem moveComponent = new JMenuItem(COMPONENT_POPUP.LABEL_MOVE_COMPONENT);
		moveComponent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				moveComponent();
				contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		if (((WYSIWYGComponent)component).getParentEditor() != null)
			contextMenu.add(moveComponent);
		
		/**
		 * If the Component is a WYSIWYGLayoutEditorPanel the Standardborder can be hidden
		 */
		if (component instanceof WYSIWYGLayoutEditorPanel || component instanceof WYSIWYGTabbedPane) {
			LayoutCell layoutCell = this.tableLayoutUtil.getLayoutCellByPosition(0, 0);
			
			boolean borderIsShown = true;
			if (layoutCell.getCellHeight() == 0 && layoutCell.getCellWidth() == 0){
				borderIsShown = false;
			}

			
			JMenuItem hideStandardBorder;
			if (borderIsShown)
				hideStandardBorder = new JMenuItem(COMPONENT_POPUP.LABEL_HIDE_STANDARD_BORDER);
			else
				hideStandardBorder = new JMenuItem(COMPONENT_POPUP.LABEL_SHOW_STANDARD_BORDER);

			hideStandardBorder.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleStandardBorderVisible();
				}
			});
			
			contextMenu.add(hideStandardBorder);
		}

		/**
		 * Add Rules if Component can be Target of LayoutMLRules 
		 */
		if (wysiwygcomponent.getLayoutMLRulesIfCapable() != null && (!(wysiwygcomponent instanceof WYSIWYGSubForm)) && (!(wysiwygcomponent instanceof WYSIWYGChart))) {
			/** if the component can be target of layoutml rules this item is added */
			JMenuItem addLayoutMLRule = new JMenuItem(COMPONENT_POPUP.LABEL_EDIT_RULES_FOR_COMPONENT);
			addLayoutMLRule.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					addLayoutMLRuleToComponent(wysiwygcomponent.getParentEditor());
				}
			});

			contextMenu.add(addLayoutMLRule);
		}

		/**
		 * Delete the Component
		 */
		deleteComponent = new JMenuItem(COMPONENT_POPUP.LABEL_DELETE_COMPONENT);
		deleteComponent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteComponent();
			}
		});

		contextMenu.add(deleteComponent);

		/**
		 * Add a Separator (after this Component specific Contextmenu Items are added
		 */
		if (component instanceof WYSIWYGLayoutEditorPanel) {
			if (((WYSIWYGLayoutEditorPanel) component).getParentEditor() == null) {
				// if it is not usable, just remove it
				contextMenu.remove(deleteComponent);
			}
		}
		
		/**
		 * there is no need to display move and delete for a subform column
		 */
		if((wysiwygcomponent instanceof WYSIWYGSubForm)) {
			if(((WYSIWYGSubForm) wysiwygcomponent).getColumnAtX(xClick) != null) {
				contextMenu.remove(moveComponent);
				contextMenu.remove(deleteComponent);
			}
		}

		/**
		 * Loading additional Menuitems from each WYSIWYG Component.
		 */
		if (wysiwygcomponent != null) {
			List<JMenuItem> additionalItems = wysiwygcomponent.getAdditionalContextMenuItems(xClick);
			if (additionalItems != null && !additionalItems.isEmpty()) {

				for (Iterator<JMenuItem> it = additionalItems.iterator(); it.hasNext();) {
					JMenuItem mi = it.next();
					if ("-".equals(mi.getLabel())) {
						contextMenu.addSeparator();
					} else {
						contextMenu.add(mi);
					}
				}
			}
		}
	}
	
	/**
	 * This method toogles the StandardBorder.<b>
	 * It does set the outer Border to a size of 0.<b>
	 * This is the best Method, deleting would mess up quite a lot.<b>
	 */
	private void toggleStandardBorderVisible(){
		LayoutCell upperLeftCorner = tableLayoutUtil.getLayoutCellByPosition(0, 0);
		
		boolean borderIsShown = true;
		if (upperLeftCorner.getCellHeight() == 0 && upperLeftCorner.getCellWidth() == 0){
			borderIsShown = false;
		}
		
		if (borderIsShown){
			tableLayoutUtil.modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, true, upperLeftCorner, false);
			tableLayoutUtil.modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, false, upperLeftCorner, false);
		} else {
			tableLayoutUtil.modifyTableLayoutSizes(InterfaceGuidelines.MARGIN_TOP, false, upperLeftCorner, false);
			tableLayoutUtil.modifyTableLayoutSizes(InterfaceGuidelines.MARGIN_LEFT, true, upperLeftCorner, false);
		}
	}

	/**
	 * This Method is opening the {@link LayoutMLRuleEditorDialog} and adds {@link LayoutMLRules} to this {@link WYSIWYGComponent}
	 * @param editorPanel
	 */
	protected void addLayoutMLRuleToComponent(WYSIWYGLayoutEditorPanel editorPanel) {
		LayoutMLRuleEditorDialog ruleDialog = new LayoutMLRuleEditorDialog(wysiwygcomponent, editorPanel);
		if (ruleDialog.getExitStatus() == LayoutMLRuleEditorDialog.EXIT_SAVE)
			tableLayoutUtil.notifyThatSomethingChanged();
	}

	/**
	 * Shows the Contextmenu for the Component
	 * 
	 * @param mouseLoc the position of the Mouse (needed for SubformColumns)
	 * 
	 */
	public void showComponentPropertiesPopup(Point mouseLoc) {
		try {
			contextMenu.show(contentPane, mouseLoc.x, mouseLoc.y);			
		} catch (IllegalComponentStateException e) {
			LOG.warn("showComponentPropertiesPopup", e);
		}
	}

	/**
	 * The Action to perform to delete a {@link WYSIWYGComponent} from the {@link TableLayoutPanel}
	 */
	private final void deleteComponent() {
		try {
			if (tableLayoutUtil.getContainer().getParentEditorPanel().getController().preferencesForThisComponentShown(wysiwygcomponent))
				tableLayoutUtil.getContainer().getParentEditorPanel().getController().hidePreferencesPanel();
		}
		catch(NuclosBusinessException e) {
			Errors.getInstance().showExceptionDialog(null, e);
		}
		tableLayoutUtil.removeComponentFromLayout(wysiwygcomponent, true);
	}

	/**
	 * This Method handles Moving a {@link WYSIWYGComponent}
	 */
	private final void moveComponent() {
		tableLayoutUtil.getContainer().setComponentToMove(wysiwygcomponent);
		((Component) wysiwygcomponent).setEnabled(false);
	}
}
