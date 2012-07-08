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
package org.nuclos.client.genericobject;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.statemodel.StateWrapper;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.LineBreakLabel;
import org.nuclos.common2.StringUtils;

public class ChangeStatePanel extends JPanel {
	
	private final StateWrapper stateSource;
	private final StateWrapper stateTarget;
	
	public ChangeStatePanel(StateWrapper stateSource, StateWrapper stateTarget) {
		super();
		this.stateSource = stateSource;
		this.stateTarget = stateTarget;
		
		boolean isSourceIcon = stateSource==null?false:stateSource.getResButtonIcon()!=null;
		boolean isTargetIcon = stateTarget.getResButtonIcon()!=null;
		
		JLabel lbSource = new JLabel(stateSource==null?"":stateSource.getName(), isSourceIcon?ResourceCache.getInstance().getIconResource(stateSource.getResButtonIcon().getId()):null, SwingConstants.LEFT);
		lbSource.setVerticalTextPosition(JLabel.BOTTOM);
		lbSource.setHorizontalTextPosition(JLabel.CENTER);
		
		JLabel lbArrow = new JLabel(Icons.getInstance().getIconStateChangeArrow());
		
		JLabel lbTarget = new JLabel(stateTarget.getName(), isTargetIcon?ResourceCache.getInstance().getIconResource(stateTarget.getResButtonIcon().getId()):null, SwingConstants.RIGHT);
		lbTarget.setVerticalTextPosition(JLabel.BOTTOM);
		lbTarget.setHorizontalTextPosition(JLabel.CENTER);
		
		int gap = 20;
		int widhtNumSource = 0;
		
		TableLayout tbllay = new TableLayout();
		tbllay.setColumn(new double[] {30d, TableLayout.PREFERRED, gap, TableLayout.FILL, gap, TableLayout.PREFERRED, 30d});
		tbllay.setRow(new double[] {10d, 30d, TableLayout.FILL, TableLayout.PREFERRED, 0d, TableLayout.PREFERRED, 20d});
		setLayout(tbllay);
		
		add(new StateColorLine(), 											new TableLayoutConstraints(0, 0, 6, 0));
		if (!isSourceIcon) {
			JLabel lbNumSource = getNumeralLabel(stateSource);
			widhtNumSource = lbNumSource.getPreferredSize().width;
			add(lbNumSource,				 								new TableLayoutConstraints(1, 2, 1, 2, TableLayout.FULL, TableLayout.BOTTOM));
		}
		add(lbSource, 														new TableLayoutConstraints(1, isSourceIcon?2:3, 1, 3, TableLayout.FULL, TableLayout.BOTTOM));
		add(lbArrow,												 		new TableLayoutConstraints(3, 2, 3, 3, TableLayout.FULL, TableLayout.CENTER));
		if (!isTargetIcon) {
			add(getNumeralLabel(stateTarget), 								new TableLayoutConstraints(5, 2, 5, 2, TableLayout.FULL, TableLayout.BOTTOM));
		}
		add(lbTarget, 														new TableLayoutConstraints(5, isTargetIcon?2:3, 5, 3, TableLayout.FULL, TableLayout.BOTTOM));
		if (!StringUtils.looksEmpty(stateTarget.getDescription())) {
			tbllay.setRow(4, 20d);
			int width = Math.max(widhtNumSource, lbSource.getPreferredSize().width) + gap + lbArrow.getPreferredSize().width + gap + lbTarget.getPreferredSize().width;
			
			JLabel lbDesc = new LineBreakLabel(stateTarget.getDescription(), width);
			lbDesc.setForeground(NuclosThemeSettings.BACKGROUND_COLOR3);
			add(lbDesc,													 	new TableLayoutConstraints(1, 5, 5, 5, TableLayout.LEFT, TableLayout.FULL));
		}
	}
	
		
	private JLabel getNumeralLabel(StateWrapper state) {
		JLabel lbNum = new JLabel(state==null?"*******":state.getNumeral().toString(), SwingConstants.CENTER);
		lbNum.setVerticalTextPosition(JLabel.BOTTOM);
		lbNum.setHorizontalTextPosition(JLabel.CENTER);
		
		Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>(lbNum.getFont().getAttributes());
		fontAttributes.put(TextAttribute.SIZE, new Float(((Float)fontAttributes.get(TextAttribute.SIZE)).intValue()   +8  ));
		fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		lbNum.setFont(new Font(fontAttributes));
		return lbNum;
	}
	
	private class StateColorLine extends JComponent {

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			final Rectangle bounds = getBounds();
			
			g2.setPaint(new GradientPaint(0, 0, (stateSource==null||stateSource.getColor()==null)?NuclosThemeSettings.BACKGROUND_ROOTPANE:stateSource.getColor(), 
					bounds.width, 0, stateTarget.getColor()==null?NuclosThemeSettings.ICON_BLUE_LIGHTER:stateTarget.getColor()));
			g2.fillRect(0, 0, bounds.width, bounds.height/2);
			
			g2.setPaint(new GradientPaint(0, 0, NuclosThemeSettings.BACKGROUND_ROOTPANE, 
					0, bounds.height, NuclosThemeSettings.BACKGROUND_PANEL));
			g2.fillRect(0, bounds.height/2, bounds.width, bounds.height/2);
		}
		
	}
	
}
