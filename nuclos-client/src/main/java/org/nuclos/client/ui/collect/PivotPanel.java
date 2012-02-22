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
import javax.swing.JComponent;
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
import org.nuclos.common2.SpringLocaleDelegate;


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

			/**
			 * Index is always of the first line for this subform.
			 */
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
				final EntityFieldMetaDataVO keyItem = keyMds.get(index);
				
				final int size = valueCombos.size();
				for (int i = 0; i < size; ++i) {
					// value could be null as we don't change the _model_ index
					final JComboBox value = valueCombos.get(i);
					// only enable/disable a certain subform
					if (value == null || !subform.equals(value.getClientProperty(SUBFORM_KEY))) {
						continue;
					}
					final JLabel key = keyLabels.get(i);
					final JButton add = subformAddOrDelete.get(i);
					key.setEnabled(selected);
					value.setEnabled(selected);
					if (index != i) {
						subformCbs.get(i).setSelected(selected);
						add.setEnabled(true);
					}
					else {
						add.setEnabled(selected);
					}
					
					if (selected) {
						// Re-enable all lines from this subform
						final JComboBox cb = valueCombos.get(i);
						if (cb.getClientProperty(SUBFORM_KEY).equals(subform)) {
							setState(selected, subform, keyItem, (EntityFieldMetaDataVO) cb.getSelectedItem());
						}
					}
				}
				
				final int s = subformNames.get(subform);
				subformAddOrDelete.get(index).setEnabled(selected && s < valueCombos.get(index).getModel().getSize());
				
				final JComboBox value = valueCombos.get(index);
				if (!selected) {
					// remove all lines of this subform from state
					deleteState(subform);
					fireItemEvent(value, null, ItemEvent.DESELECTED);
				}
				else {
					fireItemEvent(value, keyItem, ItemEvent.SELECTED);
				}
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
					
					// set state
					// setState(true, subform, keyItem, valueItem);
					final PivotInfo pinfo = getPivotInfo(subform, keyItem, valueItem);
					final int i = getPivotListIndexOf(subform, index);
					final  List<PivotInfo> plist = state.get(subform);
					plist.set(i, pinfo);
					
					fireItemEvent(value, keyItem, ItemEvent.SELECTED);
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
				final PivotInfo pinfo = getDefaultPivotInfo(Header.this, subform);
				final int modelIndex = keyLabels.size();
				if (addLine(Header.this, baseEntity, pinfo, modelIndex, viewIndex, fields, false, true)) {
					pivotLines.map(modelIndex, viewIndex);
					
					// disable more lines for this subform if there are no more values
					final int size = incSubformNames(subform);
					if (size >= valueCombos.get(index).getModel().getSize()) {
						subformAddOrDelete.get(index).setEnabled(false);
					}
					
					updateLayout();
				}
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
				// delete state
				final List<PivotInfo> plist = state.get(subform);
				if (plist != null) {
					plist.remove(getPivotListIndexOf(subform, index));
				}

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
				final JComboBox old = valueCombos.set(index, null);
				CollectionUtils.trimTail(valueCombos);
								
				// enable more lines for this subform if there are more values 
				// (and the line/subform is selected)
				final int size = decSubformNames(subform);
				final int i = getIndexOfFirst(subform);
				if (subformCbs.get(i).isSelected() && size < valueCombos.get(i).getModel().getSize()) {
					subformAddOrDelete.get(i).setEnabled(true);
				}
				
				updateLayout();	
				fireItemEvent(old, null, ItemEvent.DESELECTED);				
			}	
			
		}
		
		private final String baseEntity;
		
		private final ViewIndex pivotLines;
		
		private final Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields;
		
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

		private Header(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,List<PivotInfo>> state) {
			super(new GridBagLayout());
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			
			this.baseEntity = baseEntity;
			this.pivotLines = new ViewIndex();
			this.subFormFields = subFormFields;
			
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
					localeDelegate.getMessageFromResource("pivot.panel.pivot.entity"));
			c.gridy = 1;
			c.gridx = 0;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					localeDelegate.getMessageFromResource("pivot.panel.key.field"));
			c.gridx = 1;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					localeDelegate.getMessageFromResource("pivot.panel.value.field"));
			c.gridx = 3;
			c.weightx = 0.2;
			add(label, c);

			int index = 0;
			for (String subform: subFormFields.keySet()) {
				final Map<String, EntityFieldMetaDataVO> fields = subFormFields.get(subform);

				// copy state
				List<PivotInfo> plist = state.get(subform);
				if (plist == null) plist = Collections.emptyList();
				if (plist.size() == 0) {
					// no pivot from this subform is in state
					final PivotInfo pinfo = getDefaultPivotInfo(this, subform);
					if (pinfo != null && addLine(this, baseEntity, pinfo, index, index, fields, true, false)) {
						pivotLines.map(index, index);
						incSubformNames(subform);
						++index;
					}
				}
				boolean first = true;
				for (PivotInfo pinfo: plist) {
					if (addLine(this, baseEntity, pinfo, index, index, fields, first, true)) {
						pivotLines.map(index, index);
						incSubformNames(subform);
						++index;
						first = false;
					}
				}
				
				// disable add button if there are equal or more lines than value
				final int firstIndex = getIndexOfFirst(subform);
				if (firstIndex >= 0 && valueCombos.get(firstIndex).getModel().getSize() <= plist.size()) {
					subformAddOrDelete.get(firstIndex).setEnabled(false);
				}
			}
		}
		
		private static PivotInfo getDefaultPivotInfo(Header me, String subform) {
			// try to get it from state
			final List<PivotInfo> plist = me.state.get(subform);
			if (plist != null && !plist.isEmpty()) {
				return plist.iterator().next();
			}
			// no pivot from this subform is in state
			final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
			// final EntityFieldMetaDataVO field = me.subFormFields.get(subform).values().iterator().next();
			final EntityFieldMetaDataVO keyField = mdProv.getPivotKeyField(me.baseEntity, subform);
			if (keyField == null) return null;
			return getPivotInfo(subform, keyField, keyField);
		}
		
		private static boolean addLine(Header me, String baseEntity, PivotInfo pinfo, int index, int viewIndex, 
				Map<String, EntityFieldMetaDataVO> fields, boolean first, boolean enabled) {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
			final Collator collator = Collator.getInstance(localeDelegate.getLocale());
			final String subform = pinfo.getSubform();
			final EntityMetaDataVO mdSubform = mdProv.getEntity(subform);

			final EntityFieldMetaDataVO keyField = mdProv.getPivotKeyField(baseEntity, subform);
			final JComboBox combo = mkCombo(baseEntity, subform, keyField.getField(), collator, fields);
			// nothing to select -> no line to add
			if (combo == null) return false;
			
			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.weightx = 0.2;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 1;
			c.ipadx = 3;
			c.ipady = 1;

			final JCheckBox cb = new JCheckBox(localeDelegate.getLabelFromMetaDataVO(mdSubform));
			cb.setSelected(enabled);
			if (first) {
				cb.addItemListener(me.new Enabler(subform, index));
			}
			me.subformCbs.add(cb);
			c.gridy = viewIndex + 2;
			c.gridx = 0;
			me.add(cb, c);
			cb.setVisible(true);
			cb.setEnabled(first);
			
			final Changer changer = me.new Changer(pinfo.getSubform(), index);
			final JLabel l = new JLabel(localeDelegate.getLabelFromMetaFieldDataVO(keyField));
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
				// figure dash, see http://en.wikipedia.org/wiki/Dash
				// minus sign, see http://en.wikipedia.org/wiki/Plus_and_minus_signs
				add = new JButton("\u2212");
				add.addActionListener(me.new Deleter(subform, index));
			}
			add.setEnabled(enabled);
			me.subformAddOrDelete.add(add);
			c.gridx = 2;
			c.fill = GridBagConstraints.NONE;
			me.add(add, c);
			c.fill = GridBagConstraints.BOTH;

			combo.setEnabled(enabled);
			final EntityFieldMetaDataVO valueItem = fields.get(pinfo.getValueField()); 
			combo.setSelectedItem(valueItem);
			combo.addItemListener(changer);
			me.valueCombos.add(combo);
			c.gridx = 3;
			me.add(combo, c);
			
			if (enabled) {
				// copy state
				me.setState(enabled, subform, keyField, valueItem);
				me.fireItemEvent(combo, keyField, ItemEvent.SELECTED);
				// disable add button if there is one value
				if (first && combo.getModel().getSize() <= 1) {
					add.setEnabled(false);
				}
			}
			return true;
		}

		private static JComboBox mkCombo(final String baseEntity, String subform, String keyField, final Collator col, Map<String, EntityFieldMetaDataVO> fields) {
			/*
			final TreeSet<EntityFieldMetaDataVO> sorted = new TreeSet<EntityFieldMetaDataVO>(
					new Comparator<EntityFieldMetaDataVO>() {
						@Override
						public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
							return col.compare(SpringLocaleDelegate.getLabelFromMetaFieldDataVO(o1),
									SpringLocaleDelegate.getLabelFromMetaFieldDataVO(o2));
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
			
			// return null if there is nothing to select...
			if (fieldList.isEmpty()) return null;
			
			Collections.sort(fieldList, new Comparator<EntityFieldMetaDataVO>() {
				@Override
				public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
					return col.compare(SpringLocaleDelegate.getInstance().getLabelFromMetaFieldDataVO(o1),
							SpringLocaleDelegate.getInstance().getLabelFromMetaFieldDataVO(o2));
				}
			});
			final ComboBoxModel model = new SimpleCollectionComboBoxModel<EntityFieldMetaDataVO>(fieldList);
			final JComboBox result = new JComboBox(model);
			result.setRenderer(EntityFieldMetaDataListCellRenderer.getInstance());
			result.setVisible(true);
			if (!fieldList.isEmpty()) {
				result.setSelectedIndex(0);
			}
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
					
					ch.revalidate();
					k.revalidate();
					a.revalidate();
					v.revalidate();
				}
			}
			// force repaint
			validate();
		}

		private static PivotInfo getPivotInfo(String subform, EntityFieldMetaDataVO keyItem, EntityFieldMetaDataVO valueItem) {
			final Class<?> type;
			try {
				type = Class.forName(valueItem.getDataType());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
			return new PivotInfo(subform, keyItem.getField(), valueItem.getField(), type);
		}
		
		private void setState(boolean selected, String subform, EntityFieldMetaDataVO keyItem, EntityFieldMetaDataVO valueItem) {
			List<PivotInfo> plist = state.get(subform);
			final PivotInfo pinfo = getPivotInfo(subform, keyItem, valueItem);
			// set state
			if (selected) {
				if (plist == null) {
					plist = new ArrayList<PivotInfo>();
					state.put(subform, plist);
				}
				plist.add(pinfo);
			}
			else {
				if (plist != null) {
					plist.remove(pinfo);
				}
			}
		}
		
		private void deleteState(String subform) {
			state.put(subform, null);
		}

		private void fireItemEvent(ItemSelectable source, EntityFieldMetaDataVO item, int state) {
			final List<ItemListener> l;
			synchronized (listener) {
				if (listener.isEmpty()) return;
				l = (List<ItemListener>) CloneUtils.cloneCollection(listener);
			}
			final ItemEvent event = new ItemEvent(source, state, item, state);
			for (ItemListener i: l) {
				i.itemStateChanged(event);
			}
		}
		
		/**
		 * Return the new value.
		 */
		private int incSubformNames(String subform) {
			Integer old = subformNames.get(subform);
			if (old == null) {
				old = Integer.valueOf(0);
			}
			final int newValue = old.intValue() + 1;
			subformNames.put(subform, Integer.valueOf(newValue));
			return newValue;
		}

		/**
		 * Return the new value.
		 */
		private int decSubformNames(String subform) {
			final int newValue = subformNames.get(subform).intValue() - 1;
			subformNames.put(subform, Integer.valueOf(newValue));
			return newValue;
		}

		private int getIndexOfFirst(String subform) {
			final int size = valueCombos.size();
			for (int i = 0; i < size; ++i) {
				final JComboBox cb = valueCombos.get(i);
				final String s = (String) cb.getClientProperty(SUBFORM_KEY);
				if (subform.equals(s)) {
					return i;
				}
			}
			return -1;
		}
		
		private int getPivotListIndexOf(String subform, int modelIndex) {
			final int size = valueCombos.size();
			int result = -1;
			for (int i = 0; i < size && i <= modelIndex; ++i) {
				final JComboBox cb = valueCombos.get(i);
				final String s = (String) cb.getClientProperty(SUBFORM_KEY);
				if (subform.equals(s)) {
					++result; 
				}
			}
			return result;
		}
		
	}
	
	public PivotPanel(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,List<PivotInfo>> state) {
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

	public String getSubformName(ItemSelectable src) {
		return (String) ((JComponent) src).getClientProperty(SUBFORM_KEY);
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
