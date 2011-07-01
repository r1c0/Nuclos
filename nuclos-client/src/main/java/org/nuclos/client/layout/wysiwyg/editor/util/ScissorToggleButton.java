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
package org.nuclos.client.layout.wysiwyg.editor.util;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ENABLE_DISABLE_SLICING;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable;

/**
 * This class adds the "Slicing" Button into the {@link WYSIWYGEditorsToolbar}.<br>
 * With this {@link JToggleButton} the slicing can be enabled or disabled.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ScissorToggleButton implements WYSIWYGToolbarAttachable{

	private final static String path = "org/nuclos/client/layout/wysiwyg/editor/util/images/";
	
	private final static Icon iconEnabled = new ImageIcon(ScissorToggleButton.class.getClassLoader().getResource(path + "edit-cut-enabled.png"));
	private final static Icon iconDisabled = new ImageIcon(ScissorToggleButton.class.getClassLoader().getResource(path + "edit-cut-disabled.png"));
	
	//private boolean justAdd = false;
	
	private JToggleButton scissorButton = null;
	private AbstractActionExt scissorAction = null;
	
	private WYSIWYGLayoutEditorPanel panel;
	
	private static final Logger log = Logger.getLogger(ScissorToggleButton.class);
	
	public ScissorToggleButton(WYSIWYGLayoutEditorPanel panel) {
		this.panel = panel;
	}
	
//	private final void writeToPreferences(){
//		try {
//			panel.getController().getPreferences().putBoolean(WYSIWYGLayoutControllingPanel.PREFERENCES_SLICING, isJustAddEnabled());
//		} catch (NuclosBusinessException e) {
//			log.error(e);
//		}
//	}
	
	public boolean isScissorEnabled(){
		return scissorAction != null? !(Boolean)scissorAction.getValue(AbstractActionExt.SELECTED_KEY) : false;
	}
	
	public void setScissorEnabled(boolean value) {
		this.enableScissorAction(value);
	}

	private void enableScissorAction(boolean newSelectedState) {
		this.scissorAction.setSmallIcon(newSelectedState? iconEnabled : iconDisabled);
		this.scissorAction.setShortDescription(newSelectedState? ENABLE_DISABLE_SLICING.TOOLTIP_ENABLED_SLICING : ENABLE_DISABLE_SLICING.TOOLTIP_DISABLED_SLICING);
		//writeToPreferences();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable#getToolbarItems()
	 */
	@Override
	public JComponent[] getToolbarItems() {
		this.scissorAction = new AbstractActionExt(){
			{init();}

			private void init() {
				putValue(SELECTED_KEY, false); //revert - strange in this Look and Feel
				this.setSmallIcon(iconDisabled);
				this.setShortDescription(ENABLE_DISABLE_SLICING.TOOLTIP_DISABLED_SLICING);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean newSelectedState = ((Boolean)getValue(SELECTED_KEY));
				enableScissorAction(newSelectedState);
			};
		};
		this.scissorButton = new JToggleButton(this.scissorAction);
		//button.setRolloverEnabled(false);
		return new JToggleButton[]{this.scissorButton};
	}
}
