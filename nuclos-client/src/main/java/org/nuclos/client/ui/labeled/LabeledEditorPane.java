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
package org.nuclos.client.ui.labeled;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.CommonJScrollPane;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JEditorPane</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik.Stueker</a>
 * @version	01.00.00
 */

public class LabeledEditorPane extends LabeledTextComponent {
	
	private static final String TOOLBAR_SEPARATOR = "toolbarSeparator"; 
	private static final String[] toolbarActions = {"font-bold", 
												   "font-italic", 
												   "font-underline", 
												   TOOLBAR_SEPARATOR,
												   "InsertUnorderedListItem", 
												   "InsertOrderedListItem", 
												   TOOLBAR_SEPARATOR,
												   "InsertTable", 
												   "InsertTableRow", 
												   "InsertTableDataCell"};
	private static final Map<String, Pair<String, Icon>> actionPresentation = new HashMap<String, Pair<String,Icon>>();
	static {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		
		actionPresentation.put("font-bold", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.textBold", "Fett"), Icons.getInstance().getIconTextBold()));
		actionPresentation.put("font-italic", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.textItalic", "Kursiv"), Icons.getInstance().getIconTextItalic()));
		actionPresentation.put("font-underline", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.textUnderline", "Unterstrichen"), Icons.getInstance().getIconTextUnderline()));
		actionPresentation.put("InsertUnorderedListItem", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.listUnordered", "Unsortierte Liste"), Icons.getInstance().getIconListUnordered()));
		actionPresentation.put("InsertOrderedListItem", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.listOrdered", "Sortierte Liste"), Icons.getInstance().getIconListOrdered()));
		actionPresentation.put("InsertTable", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.table", "Tabelle einfügen"), Icons.getInstance().getIconInsertTable16()));
		actionPresentation.put("InsertTableRow", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.tableRow", "Zeile einfügen"), Icons.getInstance().getIconInsertRow16()));
		actionPresentation.put("InsertTableDataCell", new Pair<String, Icon>(cld.getMessage(
				"CollectableEditorPane.tableDataCell", "Zelle einfügen"), Icons.getInstance().getIconInsertCell16()));
	}
	
	private JPanel content = new JPanel(new BorderLayout());
	
	private JToolBar toolbar = new JToolBar();

	private JEditorPane ep = new JEditorPane() {

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = LabeledEditorPane.this.getToolTipTextProviderForControl();
			return StringUtils.concatHtml(provider != null ? provider.getDynamicToolTipText() : super.getToolTipText(ev), LabeledEditorPane.this.getValidationToolTip());
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = LabeledEditorPane.this.getBackgroundColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}
	};

	private JScrollPane scrlpn = new CommonJScrollPane(this.ep, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {

		@Override
		public boolean hasFocus() {
			return ep.hasFocus();
		}
	};

	public LabeledEditorPane() {
		this(true, String.class, null, false);
	}

	public LabeledEditorPane(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		initValidation(isNullable, javaClass, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.content);
		}
		this.getJLabel().setLabelFor(this.ep);
		
		if (bSearchable) {
			this.ep.setContentType("text/plain");
		} else {
			this.ep.setContentType("text/html");
			Action[] allActions = this.ep.getEditorKit().getActions();
			for (String action : toolbarActions) {
				if (TOOLBAR_SEPARATOR.equals(action)) {
					toolbar.addSeparator();
				} else {
					for (int i = 0; i < allActions.length; i++) {
						String name = allActions[i].getValue(Action.NAME).toString();
						if (LangUtils.equals(action, name)) {
							JButton btn = new JButton(allActions[i]);
							btn.setText("");
							btn.setToolTipText(actionPresentation.get(action).x);
							btn.setIcon(actionPresentation.get(action).y);
							toolbar.add(btn);
						}
					}
				}
			}
			
			this.toolbar.setFloatable(false);
			this.content.add(this.toolbar, BorderLayout.NORTH);
		}	
		
		this.content.add(this.scrlpn, BorderLayout.CENTER);
	}
	
	public void setToolbarEnabled(boolean enabled) {
		for (Component c : this.toolbar.getComponents()) {
			c.setEnabled(enabled);
		}
	}

	@Override
	protected JComponent getLayeredComponent(){
		return this.content;
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.ep;
	}
	
	public JScrollPane getJScrollPane() {
		return this.scrlpn;
	}

	public JEditorPane getJEditorPane() {
		return this.ep;
	}

	/**
	 * @return the editor pane
	 */
	@Override
	public JTextComponent getJTextComponent() {
		return this.ep;
	}

	@Override
	public JComponent getControlComponent() {
		return this.content;
	}
	
	@Override
	public boolean hasFocus() {
		return this.ep.hasFocus();
	}

	/**
	 * sets the static tooltip text for the label and the control component (not for the panel itself).
	 * The static tooltip is shown in the control component only if no tooltiptextprovider was set for the control.
	 * @param sToolTipText
	 * @postcondition LangUtils.equals(this.getToolTipText(), sToolTipText)
	 */
	@Override
	public void setToolTipText(String sToolTipText) {
		this.getJLabel().setToolTipText(sToolTipText);
		this.ep.setToolTipText(sToolTipText);

		assert LangUtils.equals(this.getToolTipText(), sToolTipText);
	}

	@Override
	public void setToolTipTextProviderForControl(ToolTipTextProvider tooltiptextprovider) {
		super.setToolTipTextProviderForControl(tooltiptextprovider);
		if (tooltiptextprovider != null) {
			// This is necessary to enable dynamic tooltips for the text area:
			ToolTipManager.sharedInstance().registerComponent(this.getJEditorPane());
		}
	}

	@Override
	protected GridBagConstraints getGridBagConstraintsForLabel() {
		final GridBagConstraints result = (GridBagConstraints) super.getGridBagConstraintsForLabel().clone();

		result.anchor = GridBagConstraints.NORTHWEST;

		return result;
	}

	@Override
	protected GridBagConstraints getGridBagConstraintsForControl(boolean bFill) {
		final GridBagConstraints result = (GridBagConstraints) super.getGridBagConstraintsForControl(bFill).clone();

		// always fill vertically:
		switch (result.fill) {
			case GridBagConstraints.NONE:
				result.fill = GridBagConstraints.VERTICAL;
				break;
			case GridBagConstraints.HORIZONTAL:
				result.fill = GridBagConstraints.BOTH;
				break;
			case GridBagConstraints.BOTH:
				result.fill = GridBagConstraints.BOTH;
				break;
			default:
				assert false;
		}

		result.weighty = 1.0;

		// no top/bottom insets:
		result.insets.top = 0;
		result.insets.bottom = 0;

		return result;
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.ep, sName, "ep");
	}

}  // class LabeledEditorPane
