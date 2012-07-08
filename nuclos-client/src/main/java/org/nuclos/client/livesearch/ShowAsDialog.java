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

package org.nuclos.client.livesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * This dialog gets shown, when a live search result can be displayed in more
 * than one way.
 */
public class ShowAsDialog extends JDialog {

	private static final Logger LOG = Logger.getLogger(ShowAsDialog.class);

	private static final String META_VO = "META_VO";
	private List<JCheckBox>    openAsList;
	private EntityObjectVO     object;
	
	public ShowAsDialog(JFrame owner, EntityObjectVO object, List<EntityFieldMetaDataVO> possibleReferences) {
		super(owner, SpringLocaleDelegate.getInstance().getResource("livesearch.showasdiag.title", "Frage"), false);
		this.object = object;
		
		List<Pair<EntityFieldMetaDataVO, String>> withLabel
			= CollectionUtils.sorted(
				CollectionUtils.transform(
					possibleReferences,
					new Transformer<EntityFieldMetaDataVO, Pair<EntityFieldMetaDataVO, String>>() {
						@Override
	                    public Pair<EntityFieldMetaDataVO, String> transform(EntityFieldMetaDataVO i) {
		                    return new Pair<EntityFieldMetaDataVO, String>(
		                    	i,
		                    	SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(
		                    			MetaDataClientProvider.getInstance().getEntity(i.getForeignEntity())));
	                    }
					}),
				new Comparator<Pair<EntityFieldMetaDataVO, String>>() {
					@Override
                    public int compare(Pair<EntityFieldMetaDataVO, String> o1, Pair<EntityFieldMetaDataVO, String> o2) {
	                    return o1.y.compareToIgnoreCase(o2.y);
                    }});
				
		setLayout(new BorderLayout(5, 10));
		
		openAsList = new ArrayList<JCheckBox>();
		
		JPanel checkPane = new JPanel(new GridLayout(-1, 1));
		
		for(Pair<EntityFieldMetaDataVO, String> m : withLabel) {
			JCheckBox b = new JCheckBox(m.y);
			openAsList.add(b);
			checkPane.add(b);
			b.putClientProperty(META_VO, m.x);
		}
		checkPane.setBorder(new CompoundBorder(new EmptyBorder(10, 30, 10, 30), new LineBorder(Color.GRAY)));
		
		JLabel label = new JLabel(SpringLocaleDelegate.getInstance().getResource(
				"livesearch.showasdiag.text", "<html><br>Das Objekt repräsentiert einen Detaildatensatz, zu dem kein eindeutiges Layout bestimmt werden konnte.<br>In welchem Modul soll die Ansicht geöffnet werden?</html>"));
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		add(label, BorderLayout.NORTH);
		add(checkPane, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(new JButton(okAction));
		buttons.add(new JButton(cancelAction));
		add(buttons, BorderLayout.SOUTH);
		
		pack();
		
		// Recursively color everything in snow-white
		List<Component> q = new LinkedList<Component>();
		q.add(this);
		while(!q.isEmpty()) {
			Component c = q.remove(0);
			c.setBackground(Color.WHITE);
			if(c instanceof Container)
				CollectionUtils.addAll(q, ((Container) c).getComponents());
		}
		
		UIUtils.center(this, owner, true);
	}
	
	
	private Action okAction = new AbstractAction(SpringLocaleDelegate.getInstance().getResource("livesearch.showasdiag.ok", "OK")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			for(JCheckBox b : openAsList)
				if(b.isSelected()) {
					EntityFieldMetaDataVO fm = (EntityFieldMetaDataVO) b.getClientProperty(META_VO);
                    Main.getInstance().getMainController().showDetails(
                    	fm.getForeignEntity(),
                    	object.getFieldIds().get(fm.getField()));
				}
			dispose();
		}
	};

	private Action cancelAction = new AbstractAction(SpringLocaleDelegate.getInstance().getResource("livesearch.showasdiag.cancel", "Cancel")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};
}

