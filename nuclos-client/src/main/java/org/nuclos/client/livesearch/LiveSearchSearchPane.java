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
/*
 * Created on 05.08.2010
 */
package org.nuclos.client.livesearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nuclos.client.ui.Icons;
import org.nuclos.common.collection.CollectionUtils;

/*package*/ class LiveSearchSearchPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField	        searchtext;
	private List<LiveSearchSearchPaneListener>   listeners;
	private BusyComponent       busyComp;

	public LiveSearchSearchPane() {
		listeners = new ArrayList<LiveSearchSearchPaneListener>();
		
		searchtext = new JTextField(40);
		searchtext.getDocument().addDocumentListener(docChangeListener);
		
		busyComp = new BusyComponent(10, 40);
		
		Box header = Box.createHorizontalBox();
		header.add(Box.createGlue());
		header.add(Box.createHorizontalStrut(100));
		Box search = Box.createVerticalBox();
		search.add(Box.createVerticalStrut(20));
		search.add(new JLabel(Icons.getInstance().getLiveSearchLogo()));
		search.add(Box.createVerticalStrut(10));
		search.add(searchtext);
		search.add(Box.createVerticalStrut(20));
		header.add(search);
		header.add(busyComp);
		header.add(Box.createHorizontalStrut(100 - busyComp.getPreferredSize().width));
		header.add(Box.createGlue());
		
		setLayout(new GridLayout(1, 1));
		add(header);
		
		// Recursively color everything in snow-white
		List<Component> q = new LinkedList<Component>();
		q.add(this);
		while(!q.isEmpty()) {
			Component c = q.remove(0);
			c.setBackground(Color.WHITE);
			if(c instanceof Container)
				CollectionUtils.addAll(q, ((Container) c).getComponents());
		}
		
		validate();
	}

	// -- Interface called from LiveSearchActuion
	public void startStopLoadingAnimation(boolean start) {
		if(start)
			busyComp.start();
		else
			busyComp.stop();
	}
	// -- to here

	public void addLiveSearchSearchPaneListener(LiveSearchSearchPaneListener l) {
		listeners.add(l);
	}
	
	public void removeLiveSearchSearchPaneListener(LiveSearchSearchPaneListener l) {
		listeners.remove(l);
	}
	
	private void broadcastSearchTextChanged() {
		String newSearchText = searchtext.getText();
		for(LiveSearchSearchPaneListener l : new ArrayList<LiveSearchSearchPaneListener>(listeners))
	        l.searchTextUpdated(newSearchText);
	}

	private DocumentListener docChangeListener = new DocumentListener() {
		@Override
		public void removeUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			broadcastSearchTextChanged();
		}
	};

}
