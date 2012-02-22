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
 * Created on 20.08.2010
 */
package org.nuclos.client.livesearch;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.model.CommonDefaultListModel;
import org.nuclos.client.ui.model.MutableListModel;
import org.nuclos.client.ui.model.SortedListModel;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.PreferencesException;

public class LiveSearchSettingsPanel extends JPanel {

	private DefaultSelectObjectsPanel<?> selPanel;

	public LiveSearchSettingsPanel() {
		super(new BorderLayout());

		ArrayList<String> savedSelected = new ArrayList<String>();
		ArrayList<String> savedDeselected = new ArrayList<String>();
		try {
			String[] savedSelectedArray = PreferencesUtils.getStringArray(ClientPreferences.getUserPreferences().node("livesearch"), "selected");
			CollectionUtils.addAll(savedSelected, savedSelectedArray);
			String[] savedDeselectedArray = PreferencesUtils.getStringArray(ClientPreferences.getUserPreferences().node("livesearch"), "deselected");
			CollectionUtils.addAll(savedDeselected, savedDeselectedArray);
		} catch (PreferencesException e) {
			Errors.getInstance().showExceptionDialog(this, "Exception while reading the preferences.", e);
		}

		JLabel label = new JLabel(SpringLocaleDelegate.getInstance().getResource(
				"livesearch.settings.text", ""));
		label.setBorder(new EmptyBorder(20, 20, 20, 20));
		add(label, BorderLayout.NORTH);

		selPanel = new DefaultSelectObjectsPanel<Object>();
		selPanel.btnUp.setVisible(true);
		selPanel.btnDown.setVisible(true);
		add(selPanel, BorderLayout.CENTER);

		List<String> allEntities = new ArrayList<String>();
		Set<String> systemEntities = CollectionUtils.transformIntoSet(EnumSet.<NuclosEntity> allOf(NuclosEntity.class), new Transformer<NuclosEntity, String>() {
			@Override
			public String transform(NuclosEntity i) {
				return i.getEntityName();
			}
		});
		for (EntityMetaDataVO md : MetaDataClientProvider.getInstance().getAllEntities())
			if (!systemEntities.contains(md.getEntity()) && SecurityCache.getInstance().isReadAllowedForEntity(md.getEntity()))
				allEntities.add(md.getEntity());
		Collections.sort(allEntities);

		ArrayList<String> unselected = new ArrayList<String>();
		ArrayList<String> selected = new ArrayList<String>();
		for (String s : savedSelected)
			if (allEntities.contains(s)) { // still existant?
				selected.add(s);
				allEntities.remove(s);
			}
		for (String s : allEntities)
			// Remaining: either deselected or new -> select
			if (savedDeselected.contains(s))
				unselected.add(s);
			else
				selected.add(s);

		MutableListModel<String> availModel = new SortedListModel<String>(unselected);
		MutableListModel<String> selectModel = new CommonDefaultListModel<String>(selected);

		selPanel.getJListAvailableObjects().setModel(availModel);
		selPanel.getJListSelectedObjects().setModel(selectModel);

		selPanel.getJListAvailableObjects().addListSelectionListener(listSelListener);
		selPanel.getJListSelectedObjects().addListSelectionListener(listSelListener);
		selPanel.btnUp.addActionListener(up);
		selPanel.btnDown.addActionListener(down);
		selPanel.btnLeft.addActionListener(left);
		selPanel.btnRight.addActionListener(right);
	}

	private ListSelectionListener listSelListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean leftSel = selPanel.getJListAvailableObjects().getSelectedIndices().length > 0;
			selPanel.btnRight.setEnabled(leftSel);

			int[] si = selPanel.getJListSelectedObjects().getSelectedIndices();
			boolean rightSel = si.length > 0;
			selPanel.btnLeft.setEnabled(rightSel);

			selPanel.btnUp.setEnabled(rightSel && si[0] > 0);
			selPanel.btnDown.setEnabled(rightSel && si[si.length - 1] < selPanel.getJListSelectedObjects().getModel().getSize() - 1);
		}
	};

	private ActionListener up = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			moveUpDown(-1);
		}
	};

	private ActionListener down = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			moveUpDown(1);
		}
	};

	private ActionListener left = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JList list = selPanel.getJListSelectedObjects();
			moveLeftRight((MutableListModel<String>) list.getModel(), (MutableListModel<String>) selPanel.getJListAvailableObjects().getModel(), list.getSelectedIndices());
		}
	};

	private ActionListener right = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JList list = selPanel.getJListAvailableObjects();
			moveLeftRight((MutableListModel<String>) list.getModel(), (MutableListModel<String>) selPanel.getJListSelectedObjects().getModel(), list.getSelectedIndices());
		}
	};

	private void moveLeftRight(MutableListModel<String> srcModel, MutableListModel<String> dstModel, int[] si) {
		ArrayList<String> toTransport = new ArrayList<String>();
		for (int i = si.length - 1; i >= 0; i--)
			toTransport.add(srcModel.remove(si[i]));
		for (String s : toTransport)
			dstModel.add(s);
	}

	private void moveUpDown(int offset) {
		JList list = selPanel.getJListSelectedObjects();
		CommonDefaultListModel<String> model = (CommonDefaultListModel<String>) list.getModel();
		int start = offset < 0 ? 0 : model.getSize() - 1;
		int end = offset < 0 ? model.getSize() : -1;
		int[] si = list.getSelectedIndices();
		for (int i = start; i != end; i -= offset) {
			if (Arrays.binarySearch(si, i) >= 0) {
				Object thing = model.getElementAt(i);
				model.remove(i);
				model.add(i + offset, thing);
			}
		}
		for (int i = 0; i < si.length; i++)
			si[i] += offset;
		list.setSelectedIndices(si);
	}

	private String[] toArray(JList l) {
		ArrayList<String> a = new ArrayList<String>();
		ListModel m = l.getModel();
		for (int i = 0; i < m.getSize(); i++)
			a.add((String) m.getElementAt(i));
		return a.toArray(new String[a.size()]);
	}

	public void save() throws PreferencesException {
		PreferencesUtils.putStringArray(ClientPreferences.getUserPreferences().node("livesearch"), "selected", toArray(selPanel.getJListSelectedObjects()));
		PreferencesUtils.putStringArray(ClientPreferences.getUserPreferences().node("livesearch"), "deselected", toArray(selPanel.getJListAvailableObjects()));
	}
}
