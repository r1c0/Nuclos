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

package org.nuclos.client.masterdata.user;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.TreeCellContext;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProvider;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.preferences.PreferencesConverter;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;

public class CopyPreferencesPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JComboBox userCollectableCbx;
	private final JXTree tree;

	public CopyPreferencesPanel(String defaultUser) {
		userCollectableCbx = new JComboBox(getUserNames().toArray());
		AutoCompleteDecorator.decorate(userCollectableCbx);

		JLabel label = new JLabel(CommonLocaleDelegate.getMessage("nuclos.preferences.transfer.sourceuser", null));
		label.setLabelFor(userCollectableCbx);

		Box sourceUserBox = UIUtils.createHorizontalBox(label, userCollectableCbx, 5, null);
		//		
		JPanel prefsPanel = new JPanel(new BorderLayout());
		prefsPanel.setBorder(new TitledBorder(CommonLocaleDelegate.getMessage("nuclos.user.preferences", null)));

		tree = new JXTree(new PreferencesTreeModel());
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new DefaultTreeRenderer(new PreferencesNodeValueProvider()));
		tree.setCellEditor(new PreferencesNodeValueProvider().createEditor());
		tree.setEditable(true);

		prefsPanel.add(new JScrollPane(tree));

		userCollectableCbx.setSelectedItem(defaultUser);
		initPreferencesTree(defaultUser);
		userCollectableCbx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					initPreferencesTree((String) e.getItem());
				}
			}
		});		
		setLayout(new BorderLayout());
		add(sourceUserBox, BorderLayout.NORTH);
		add(prefsPanel, BorderLayout.CENTER);
	}

	private static List<String> getUserNames() {
		try {
			return CollectionUtils.sorted(CollectionUtils.transform(new MasterDataCollectableFieldsProvider(NuclosEntity.USER.getEntityName()).getCollectableFields(),
				new Transformer<CollectableField, String>() {
					@Override public String transform(CollectableField cf) { return (String) cf.getValue(); }
				}));
		} catch (CommonBusinessException e) {
			throw new NuclosFatalException(e);
		}
	}

	private void initPreferencesTree(String userName) {
		PreferencesTreeModel model = new PreferencesTreeModel();
		if (userName != null) {
			PreferencesFacadeRemote facade = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
			try {
				PreferencesVO prefs = facade.getPreferencesForUser(userName);
				NavigableMap<String, Map<String, String>> prefsMap;
				try {
					prefsMap = PreferencesConverter.loadPreferences(new ByteArrayInputStream(prefs.getPreferencesBytes()));
				} catch(IOException ex) {
					throw new NuclosFatalException(ex);
				}
				model = makePreferencesTreeModel(prefsMap);
			} catch(CommonFinderException ex) {
				Errors.getInstance().showExceptionDialog(this, ex);
			}
		}
		tree.setModel(model);
		tree.expandAll();
	}

	public Map<String, Map<String, String>> getSelectedPreferences() {
		Map<String, Map<String, String>> prefsToMerge = new HashMap<String, Map<String, String>>();
		PreferencesExportNode root = (PreferencesExportNode) tree.getModel().getRoot();
		root.fillSelectedPrefs(prefsToMerge);
		return prefsToMerge;
	}

	private static PreferencesTreeModel makePreferencesTreeModel(SortedMap<String, Map<String, String>> prefs) {
		PreferencesTreeModel model = new PreferencesTreeModel();
		PreferencesExportNode rootTreeNode = model.getRoot();

		PreferencesExportNode entitiesTreeNode = new PreferencesExportNode(CommonLocaleDelegate.getMessage("nuclos.preferences.entity.layoutconfig", null));
		for (String nodeKey : prefs.keySet()) {
			Matcher matcher = ENTITY_KEY_PATTERN.matcher(nodeKey);
			if (matcher.matches()) {
				Map<String, Map<String, String>> mergePrefs = new HashMap<String, Map<String, String>>();

				SortedMap<String, Map<String, String>> entityPrefs = StringUtils.submapWithPrefix(prefs, nodeKey);
				// Copy all relevant entity preferences (note that non-existing preferences are stored
				// with a null key in order to make this set consistent).
				for (String mergeKey : ENTITY_MERGEPREFS) {
					String key = nodeKey + "/" + mergeKey;
					mergePrefs.put(key, entityPrefs.get(key));
				}

				for (String subKey : entityPrefs.keySet()) {
					if (SUBENTITY_KEY_PATTERN.matcher(subKey).matches()) {
						for (String mergeKey : SUBENTITY_MERGEPREFS) {
							String key = subKey + "/" + mergeKey;
							mergePrefs.put(key, entityPrefs.get(key));
						}
					}
				}

				PreferencesExportNode entityTreeNode = new PreferencesExportNode(matcher.group(1), mergePrefs);
				entitiesTreeNode.add(entityTreeNode);
			}
		}

		if (entitiesTreeNode.getChildCount() > 0)
			rootTreeNode.add(entitiesTreeNode);
		return model;
	}

	private static Pattern ENTITY_KEY_PATTERN = Pattern.compile("/org/nuclos/client/collect/entity/([^/]+)");

	private static Pattern SUBENTITY_KEY_PATTERN = Pattern.compile("/org/nuclos/client/collect/entity/[^/]+/subentity/[^/]+");

	private static String[] ENTITY_MERGEPREFS = {
		"fixedFields",
		"fixedFieldWidths",
		"orderAscending",
		"orderBySelectedField",
		"selectedFieldEntities",
		"selectedFieldWidths",
		"selectedFields",
	};

	// scheinen dieselben zu sein
	private static String[] SUBENTITY_MERGEPREFS = ENTITY_MERGEPREFS;

	private static class PreferencesTreeModel extends DefaultTreeModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private PreferencesTreeModel() {
			super(new PreferencesExportNode(""));
		}

		@Override
		public PreferencesExportNode getRoot() {
			return (PreferencesExportNode) super.getRoot();
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			super.valueForPathChanged(path, newValue);
			nodeChanged(root);
		}
	}

	private static class PreferencesExportNode extends DefaultMutableTreeNode {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Map<String, Map<String, String>> prefs;
		private final String name;

		public PreferencesExportNode(String name) {
			this(name, null);
		}

		public PreferencesExportNode(String name, Map<String, Map<String, String>> prefs) {
			this.name = name;
			this.prefs = prefs;
		}

		@Override
		public PreferencesExportNode getParent() {
			return (PreferencesExportNode) super.getParent();
		}

		@Override
		public PreferencesExportNode getChildAt(int index) {
			return (PreferencesExportNode) super.getChildAt(index);
		}

		@Override
		public Object getUserObject() {
			if (userObject == null) {
				if (isLeaf()) {
					userObject = false;
				} else {
					boolean b = true;
					for (int i = 0; i < getChildCount() && b; i++) {
						b &= Boolean.TRUE.equals(getChildAt(i).getUserObject());
					}
					userObject = b;
				}
			}
			return userObject;
		}

		@Override
		public void setUserObject(Object value) {
			if (value != null) {
				for (int i = 0; i < getChildCount(); i++) {
					getChildAt(i).userObject = value;
				}
			}
			userObject = value;
			PreferencesExportNode parent = getParent();
			if (parent != null)
				parent.userObject = null;
		}

		public void fillSelectedPrefs(Map<String, Map<String, String>> map) {
			if (prefs != null && Boolean.TRUE.equals(getUserObject())) {
				map.putAll(prefs);
			}
			for (int i = 0; i < getChildCount(); i++) {
				PreferencesExportNode child = getChildAt(i);
				child.fillSelectedPrefs(map);
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static class PreferencesNodeValueProvider extends CheckBoxProvider {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected void format(CellContext context) {
			PreferencesExportNode node = (PreferencesExportNode) context.getValue();
			Boolean selected = (Boolean) node.getUserObject();
			rendererComponent.setSelected(Boolean.TRUE.equals(selected));
			rendererComponent.setText(node.name);
		}

		public DefaultCellEditor createEditor() {
			return new DefaultCellEditor((JCheckBox) rendererComponent) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
					TreeCellContext cellContext = new TreeCellContext();
					cellContext.installContext(tree, value, row, 0, isSelected, true, expanded, leaf);
					configureVisuals(cellContext);
					configureContent(cellContext);
					return rendererComponent;
				}
			};
		}
	}	
}
