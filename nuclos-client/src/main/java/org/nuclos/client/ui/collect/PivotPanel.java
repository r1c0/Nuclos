//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.model.SimpleCollectionComboBoxModel;
import org.nuclos.client.ui.renderer.EntityFieldMetaDataListCellRenderer;
import org.nuclos.client.ui.util.ViewIndex;
import org.nuclos.common.CloneUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.CommonLocaleDelegate;


/**
 * A specialization of SelectFixedColumnsPanel for also choosing 'columns'
 * from a pivot (key -> value) table.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotPanel extends SelectFixedColumnsPanel {

	private static final Logger LOG = Logger.getLogger(PivotPanel.class);
	
	private static final String SUBFORM_KEY = "SUBFORM_KEY";

	private static class Header extends JPanel {

		private class Enabler implements ItemListener {
			
			private final String subform;

			private final int index;

			public Enabler(String subform, int index) {
				this.subform = subform;
				this.index = index;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				LOG.info("Enabler: item event " + e);
				final JCheckBox src = (JCheckBox) e.getSource();
				final boolean selected = src.isSelected();
				final JLabel key = keyLabels.get(index);
				final JButton add = subformAddOrDelete.get(index);
				final JComboBox value = valueCombos.get(index);
				key.setEnabled(selected);
				add.setEnabled(selected);
				value.setEnabled(selected);
				final EntityFieldMetaDataVO keyItem = keyMds.get(index);
				final EntityFieldMetaDataVO valueItem = (EntityFieldMetaDataVO) value.getSelectedItem();
				setState(selected, subform, keyItem, valueItem);
				fireItemEvent(value, keyItem);
			}

		}

		private class Changer implements ItemListener {

			private final String subform;
			
			private final int index;

			public Changer(String subform, int index) {
				this.subform = subform;
				this.index = index;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				LOG.info("Changer: item event " + e);
				final JComboBox src = (JComboBox) e.getSource();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final JLabel key = keyLabels.get(index);
					final JComboBox value = valueCombos.get(index);
					final EntityFieldMetaDataVO keyItem = keyMds.get(index);
					final EntityFieldMetaDataVO valueItem = (EntityFieldMetaDataVO) value.getSelectedItem();
					setState(true, subform, keyItem, valueItem);
					fireItemEvent(value, keyItem);
				}
			}

		}
		
		private class Adder implements ActionListener {
			
			private String subform;
			
			private final int index;
			
			private final Map<String, EntityFieldMetaDataVO> fields;
			
			public Adder(String subform, int index, Map<String, EntityFieldMetaDataVO> fields) {
				this.subform = subform;
				this.index = index;
				this.fields = fields;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				final int viewIndex = pivotLines.getViewIndex(index) + 1;
				final PivotInfo pinfo = state.get(subform).iterator().next();
				addLine(Header.this, baseEntity, pinfo, viewIndex, fields, false, true);
				final int modelIndex = subformAddOrDelete.size() - 1;
				pivotLines.map(modelIndex, viewIndex);
				
				subformNames.put(subform, Integer.valueOf(subformNames.get(subform).intValue() + 1));
				
				updateLayout();
			}	
			
		}
		
		private class Deleter implements ActionListener {
			
			private final String subform;
						
			private final int index;
			
			public Deleter(String subform, int index) {
				this.subform = subform;
				this.index = index;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				pivotLines.removeFromViewByMi(index);
				
				remove(subformCbs.get(index));
				subformCbs.set(index, null);
				CollectionUtils.trimTail(subformCbs);
				
				remove(subformAddOrDelete.get(index));
				subformAddOrDelete.set(index, null);
				CollectionUtils.trimTail(subformAddOrDelete);
				
				remove(keyLabels.get(index));
				keyLabels.set(index, null);
				CollectionUtils.trimTail(keyLabels);
				
				remove(valueCombos.get(index));
				valueCombos.set(index, null);
				CollectionUtils.trimTail(valueCombos);
								
				subformNames.put(subform, Integer.valueOf(subformNames.get(subform).intValue() - 1));
				
				updateLayout();
			}	
			
		}
		
		private final String baseEntity;
		
		private final ViewIndex pivotLines;
		
		/**
		 * Only for the first subform line
		 */
		private final List<JCheckBox> subformCbs = new ArrayList<JCheckBox>();
		
		/**
		 * add:		Only for the first subform line.
		 * delete:	Only for the subform lines after the first.
		 */
		private final List<JButton> subformAddOrDelete = new ArrayList<JButton>();

		private final List<JLabel> keyLabels = new ArrayList<JLabel>();

		private final List<EntityFieldMetaDataVO> keyMds = new ArrayList<EntityFieldMetaDataVO>();

		private final List<JComboBox> valueCombos = new ArrayList<JComboBox>();

		private final LinkedHashMap<String, List<PivotInfo>> state;

		// subform -> (number of gui lines) mapping 
		private final SortedMap<String,Integer> subformNames = new TreeMap<String,Integer>();

		private final List<ItemListener> listener = new LinkedList<ItemListener>();

		private Header(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,List<PivotInfo>> state) throws ClassNotFoundException {
			super(new GridBagLayout());
			this.baseEntity = baseEntity;
			this.pivotLines = new ViewIndex(subFormFields.size());
			
			// copy state: see below
			this.state = new LinkedHashMap<String, List<PivotInfo>>();

			// setPreferredSize(new Dimension(400, 200));
			final GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 4;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.BOTH;
			setVisible(true);

			// label
			JLabel label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.pivot.entity"));
			c.gridy = 1;
			c.gridx = 0;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.key.field"));
			c.gridx = 1;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.value.field"));
			c.gridx = 3;
			c.weightx = 0.2;
			add(label, c);

			int index = 0;
			for (String subform: subFormFields.keySet()) {
				final Map<String, EntityFieldMetaDataVO> fields = subFormFields.get(subform);

				// copy state
				List<PivotInfo> plist = state.get(subform);
				if (plist == null) plist = Collections.emptyList();
				if (plist.size() > 0) {
					this.state.put(subform, plist);
				}
				else {
					// not pivot from this subform is in state
					final EntityFieldMetaDataVO field = fields.values().iterator().next();
					final PivotInfo pinfo = new PivotInfo(subform, null, field.getField(), Class.forName(field.getDataType()));
					addLine(this, baseEntity, pinfo, index, fields, true, false);
				}
				boolean first = true;
				for (PivotInfo pinfo: plist) {
					addLine(this, baseEntity, pinfo, index, fields, first, true);
					first = false;
				}
				
				subformNames.put(subform, Integer.valueOf(1));
				++index;
			}
		}
		
		private static void addLine(Header me, String baseEntity, PivotInfo pinfo, int viewIndex, 
				Map<String, EntityFieldMetaDataVO> fields, boolean first, boolean enabled) {
			final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
			final Collator collator = Collator.getInstance(CommonLocaleDelegate.getLocale());
			final String subform = pinfo.getSubform();
			final EntityMetaDataVO mdSubform = mdProv.getEntity(subform);
			final int index = me.keyLabels.size();
			
			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.weightx = 0.2;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 1;
			c.ipadx = 3;
			c.ipady = 1;

			final JCheckBox cb = new JCheckBox(CommonLocaleDelegate.getLabelFromMetaDataVO(mdSubform));
			cb.setSelected(enabled);
			cb.addItemListener(me.new Enabler(subform, index));
			me.subformCbs.add(cb);
			c.gridy = viewIndex + 2;
			c.gridx = 0;
			me.add(cb, c);
			cb.setVisible(true);
			cb.setEnabled(first);
			
			final Changer changer = me.new Changer(pinfo.getSubform(), index);
			final EntityFieldMetaDataVO keyField = mdProv.getPivotKeyField(baseEntity, subform);
			final JLabel l = new JLabel(CommonLocaleDelegate.getLabelFromMetaFieldDataVO(keyField));
			l.setEnabled(enabled);
			me.keyLabels.add(l);
			me.keyMds.add(keyField);
			c.gridx = 1;
			me.add(l, c);

			final JButton add;
			if (first) {
				add = new JButton("+");
				add.addActionListener(me.new Adder(subform, index, fields));
			}
			else {
				add = new JButton("-");
				add.addActionListener(me.new Deleter(subform, index));
			}
			add.setEnabled(enabled);
			me.subformAddOrDelete.add(add);
			c.gridx = 2;
			c.fill = GridBagConstraints.NONE;
			me.add(add, c);
			c.fill = GridBagConstraints.BOTH;

			final JComboBox combo = mkCombo(baseEntity, subform, keyField.getField(), collator, fields);
			combo.setEnabled(enabled);
			if (pinfo != null) {
				combo.setSelectedItem(fields.get(pinfo.getValueField()));
			}
			combo.addItemListener(changer);
			me.valueCombos.add(combo);
			c.gridx = 3;
			me.add(combo, c);
		}

		private static JComboBox mkCombo(final String baseEntity, String subform, String keyField, final Collator col, Map<String, EntityFieldMetaDataVO> fields) {
			/*
			final TreeSet<EntityFieldMetaDataVO> sorted = new TreeSet<EntityFieldMetaDataVO>(
					new Comparator<EntityFieldMetaDataVO>() {
						@Override
						public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
							return col.compare(CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o1),
									CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o2));
						}
					});
			sorted.addAll(fields.values());
			final ComboBoxModel model = new SimpleCollectionComboBoxModel<EntityFieldMetaDataVO>(sorted);
			 */
			final List<EntityFieldMetaDataVO> fieldList = new ArrayList<EntityFieldMetaDataVO>();
			for (EntityFieldMetaDataVO ef: fields.values()) {
				// don't allow system fields (bug in result table display)
				if (SystemFields.FIELDS2TYPES_MAP.containsKey(ef.getField())) continue;
				// don't allow refs to base entity
				if (baseEntity.equals(ef.getForeignEntity())) continue;
				// don't allow key as value
				if (ef.getField().equals(keyField)) continue;
				fieldList.add(ef);
			}
			Collections.sort(fieldList, new Comparator<EntityFieldMetaDataVO>() {
				@Override
				public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
					return col.compare(CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o1),
							CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o2));
				}
			});
			final ComboBoxModel model = new SimpleCollectionComboBoxModel<EntityFieldMetaDataVO>(fieldList);
			final JComboBox result = new JComboBox(model);
			result.setRenderer(EntityFieldMetaDataListCellRenderer.getInstance());
			result.setVisible(true);
			result.setSelectedIndex(0);
			result.putClientProperty(SUBFORM_KEY, subform);
			return result;
		}
		
		private void updateLayout() {
			final GridBagLayout lm = (GridBagLayout) getLayout();
			final int size = pivotLines.size();
			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.weightx = 0.2;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 1;
			c.ipadx = 3;
			c.ipady = 1;
			
			final Iterator<Integer> itLines = pivotLines.iterator();
			final Iterator<JCheckBox> itCheck = subformCbs.iterator();
			final Iterator<JButton> itAdd = subformAddOrDelete.iterator();
			final Iterator<JLabel> itKey = keyLabels.iterator();
			final Iterator<JComboBox> itValue = valueCombos.iterator();
			for (int i = 0; i < size; ++i) {
				final Integer viewIndex = itLines.next();
				if (!itCheck.hasNext()) break;
				
				final JCheckBox ch = itCheck.next();
				final JButton a = itAdd.next();
				final JLabel k = itKey.next();
				final JComboBox v = itValue.next();
				
				if (viewIndex == null) {
					continue;
				}
				
				final int vi = viewIndex.intValue();
				c.gridy = vi + 2;
				
				if (ch != null) {
					c.gridx = 0;
					lm.setConstraints(ch, c);
					c.gridx = 1;
					lm.setConstraints(k, c);
					c.fill = GridBagConstraints.NONE;
					c.gridx = 2;
					lm.setConstraints(a, c);
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 3;
					lm.setConstraints(v, c);
				}
			}
			// force repaint
			invalidate();
		}

		private void setState(boolean selected, String subform, EntityFieldMetaDataVO keyItem, EntityFieldMetaDataVO valueItem) {
			// set state
			if (selected) {
				final Class<?> type;
				try {
					type = Class.forName(valueItem.getDataType());
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
				List<PivotInfo> plist = state.get(subform);
				if (plist == null) {
					plist = new ArrayList<PivotInfo>();
					state.put(subform, plist);
				}
				plist.add(new PivotInfo(subform, keyItem.getField(), valueItem.getField(), type));
			}
			else {
				state.put(subform, null);
			}
		}

		private void fireItemEvent(ItemSelectable source, EntityFieldMetaDataVO item) {
			final List<ItemListener> l;
			synchronized (listener) {
				if (listener.isEmpty()) return;
				l = (List<ItemListener>) CloneUtils.cloneCollection(listener);
			}
			final ItemEvent event = new ItemEvent(source, ItemEvent.SELECTED, item, ItemEvent.SELECTED);
			for (ItemListener i: l) {
				i.itemStateChanged(event);
			}
		}

	}
	
	public PivotPanel(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,List<PivotInfo>> state) throws ClassNotFoundException {
		super(subFormFields.isEmpty() ? null : new Header(baseEntity, subFormFields, state));
	}

	public void addPivotItemListener(ItemListener l) {
		List<ItemListener> listener = getHeader().listener;
		synchronized (listener) {
			listener.add(l);
		}
	}

	public void removePivotItemListener(ItemListener l) {
		List<ItemListener> listener = getHeader().listener;
		synchronized (listener) {
			listener.remove(l);
		}
	}

	public List<PivotInfo> getState(String subformName) {
		return getHeader().state.get(subformName);
	}

	public String getSubformName(int index) {
		return (String) getHeader().valueCombos.get(index).getClientProperty(SUBFORM_KEY);
	}

	public int indexFromValueComponent(ItemSelectable key) {
		return getHeader().valueCombos.indexOf(key);
	}

	public Map<String,List<PivotInfo>> getState() {
		return getHeader().state;
	}

	public Header getHeader() {
		return (Header) getHeaderComponent();
	}

}
