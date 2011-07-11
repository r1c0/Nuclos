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
package org.nuclos.client.statemodel.panels;

import info.clearthought.layout.TableLayout;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.statemodel.models.StatePropertiesPanelModel;
import org.nuclos.client.statemodel.panels.rights.ExpandCollapseListener;
import org.nuclos.client.statemodel.panels.rights.MultiEditListener;
import org.nuclos.client.statemodel.panels.rights.RightAndMandatoryColumnHeader;
import org.nuclos.client.statemodel.panels.rights.RightAndMandatoryConstants;
import org.nuclos.client.statemodel.panels.rights.RightAndMandatoryRow;
import org.nuclos.client.statemodel.panels.rights.RightTransfer;
import org.nuclos.client.statemodel.panels.rights.SelectionListener;
import org.nuclos.client.statemodel.panels.rights.RightTransfer.RoleRight;
import org.nuclos.client.statemodel.panels.rights.RightTransfer.RoleRights;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.SelectObjectsPanel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.AttributegroupPermissionVO;
import org.nuclos.server.statemodel.valueobject.EntityFieldPermissionVO;
import org.nuclos.server.statemodel.valueobject.MandatoryColumnVO;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.SubformColumnPermissionVO;
import org.nuclos.server.statemodel.valueobject.SubformPermissionVO;

/**
 * Panel containing the properties of a state.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */

public class StatePropertiesPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class StateDependantRightsPanel extends JPanel implements RightAndMandatoryConstants{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final int LEFT_BORDER = 5;
		
		public final static String PREFS_NODE_SELECTEDROLES = "selectedRoles";
		
		private Set<Integer> selectedRoles = new HashSet<Integer>();
		
		private Set<Integer> collapsedGroups = new HashSet<Integer>();
		
		private boolean filterUnchangedAttributes = false;
		
		private boolean firstInstance = true;
		
		private JToolBar toolBar = UIUtils.createNonFloatableToolBar(JToolBar.HORIZONTAL);
		
		private JPanel main = new JPanel();
		
		private RightTransfer rightTransfer = null; 
		
		private ActionListener actionListenerForWidthChanged = null;
		
		private int scrollPosition = -1;
		
		private StateVO statevo = null;
		
		private List<ChangeListener> lstDetailsChangedListeners = new ArrayList<ChangeListener>();
		
		public void addDetailsChangedListener(ChangeListener cl) {
			lstDetailsChangedListeners.add(cl);
		}

		public void removeDetailsChangedListener(ChangeListener cl) {
			lstDetailsChangedListeners.remove(cl);
		}
		
		private void detailsChanged() {
			for (ChangeListener cl : lstDetailsChangedListeners) {
				cl.stateChanged(new ChangeEvent(this));
			}
		}
		
		private ChangeListener detailsChangedListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				for (ChangeListener cl : lstDetailsChangedListeners) {
					cl.stateChanged(e);
				}
			}
		};

		public StateDependantRightsPanel() {
			super(new BorderLayout());
			
			toolBar.setBorderPainted(false);
			this.add(toolBar, BorderLayout.NORTH);
			
			main.setBorder(BorderFactory.createEmptyBorder(5, LEFT_BORDER, 0, 0));
			main.setBackground(COLOR_BACKGROUND);
			JScrollPane scroll = new JScrollPane(main);
			scroll.getHorizontalScrollBar().setUnitIncrement(20);
			scroll.getVerticalScrollBar().setUnitIncrement(20);
			this.add(scroll, BorderLayout.CENTER);
		}
		
		public RightTransfer getRightTransfer() {
			return rightTransfer;
		}
		
		/**
		 * uses a given statevo
		 * @param usages
		 */
		public void setup(List<CollectableEntityObject> usages) {
			this.setup(usages, statevo);
		}
		
		/**
		 * 
		 * @param usages
		 * @param statevo
		 */
		public void setup(List<CollectableEntityObject> usages, StateVO statevo) {
			this.statevo = statevo;
			toolBar.removeAll();
			main.removeAll();
			try {
				initMain(usages);
			}
			catch(CommonBusinessException e) {
				e.printStackTrace();
			}
			revalidate();
			repaint();
		}
		
		/**
		 * updates the statevo with rights and mandatory information from this properties panel
		 */
		public void updateStateVO() {
			if(rightTransfer != null && statevo != null) {
				RoleRights rr = rightTransfer.getAllRoleRights();
				statevo.getMandatoryFields().clear();
				statevo.getMandatoryColumns().clear();
				statevo.getUserRights().clear();
				statevo.getUserFieldRights().clear();
				statevo.getUserSubformRights().clear();
				for (Integer role : rr.rights.keySet()) {
					RoleRight roleRight = rr.rights.get(role);
					for (Integer group : roleRight.groupRights.keySet()) {
						if (roleRight.groupRights.get(group) != null) {
							if (!roleRight.groupIsSubform.contains(group)) {
								statevo.getUserRights().addValue(role, new AttributegroupPermissionVO(group, null, role, null, statevo.getId(), null, roleRight.groupRights.get(group)));
							} else {
								// store subform...
								Set<SubformColumnPermissionVO> columnPermissions = new HashSet<SubformColumnPermissionVO>();
								for (Pair<Integer, String> column : roleRight.subformColumns.getValues(group)) {
									if (rr.rightsEnabled.contains(column.getX())) {
										boolean writeable = roleRight.attributeRights.get(column.getX()) == null ? false : roleRight.attributeRights.get(column.getX());
										columnPermissions.add(new SubformColumnPermissionVO(null/*=roleSubformId would be set in server*/, column.getY(), writeable));
									}
								}
								statevo.getUserSubformRights().addValue(role, new SubformPermissionVO(roleRight.groupNames.get(group), role, null, statevo.getId(), null, roleRight.groupRights.get(group), columnPermissions));
							}
						}
					}
					for (Integer attribute : roleRight.attributeRights.keySet()) {
						if (!roleRight.subformColumns.getAllValues().contains(attribute)) { // no subform columns here!
							if (rr.rightsEnabled.contains(attribute)) {
								boolean readable = roleRight.attributeRights.get(attribute) != null;
								boolean writeable = roleRight.attributeRights.get(attribute) == null ? false : roleRight.attributeRights.get(attribute);
								statevo.getUserFieldRights().addValue(role, new EntityFieldPermissionVO(attribute, role, statevo.getId(), readable, writeable));
							}
						}
					}
				}
				for (Integer mandatory : rr.mandatoryFields) {
					statevo.getMandatoryFields().add(new MandatoryFieldVO(mandatory, statevo.getId()));
				}
				for (Pair<String, String> mandatoryColumn : rr.mandatoryColumns) {
					statevo.getMandatoryColumns().add(new MandatoryColumnVO(mandatoryColumn.getX(), mandatoryColumn.getY(), statevo.getId()));
				}
			}
		}
		
		/**
		 * 
		 * @param usages
		 * @param statevo
		 * @throws CommonBusinessException
		 */
		private void initMain(List<CollectableEntityObject> usages) throws CommonBusinessException {
			final Collection<MasterDataVO> roles = MasterDataCache.getInstance().get(NuclosEntity.ROLE.getEntityName());
			final SortedMap<String, Integer> rolesSorted = getRolesSorted(roles);
			final List<Integer> rolesSortOrder = getRolesSorted(rolesSorted);
			final Set<Integer> modules = getModules(usages);
			final Set<Long> metaMandatory = new HashSet<Long>();
			final Map<Long, SortedSet<Attribute>> attributes = getAttributes(modules, metaMandatory);
			final Map<Long, String> attributeGroups = getAttributeGroups(attributes.keySet());
			final SortedMap<String, Long> attributeGroupsSorted = getAttributeGroupsSorted(attributeGroups);
			final SortedMap<SubForm,SortedSet<Attribute>> subFormWithColumns = getSubForms(modules, metaMandatory);
			
			main.setLayout(new TableLayout(new double[][]{new double[]{TableLayout.FILL},new double[]{TableLayout.PREFERRED, TableLayout.FILL}}));
			
			final JPanel header = new JPanel();
			final JPanel rows = new JPanel();
			final JScrollPane scrollRows = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollRows.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			
			header.setBackground(COLOR_BACKGROUND);
			rows.setBackground(COLOR_BACKGROUND);
			
			main.add(header, "0,0");
			main.add(rows, "0,1");
			
			final double[] colSizes = getColumns(roles.size());
			final double[] rowSizes = getRows(attributeGroupsSorted, attributes, subFormWithColumns);
			
			final TableLayout layoutHeader = new TableLayout(new double[][]{colSizes, new double[]{TableLayout.PREFERRED, GAP_LINEBREAK}});
			final TableLayout layoutRows = new TableLayout(new double[][]{new double[]{colSizes[0], TableLayout.PREFERRED}, rowSizes});
			
			header.setLayout(layoutHeader);
			rows.setLayout(layoutRows);
			
			final JToggleButton filter = new JToggleButton(CommonLocaleDelegate.getMessage("StatePropertiesPanel.11", "Filter Attribute"), Icons.getInstance().getIconFilter16());
			filter.setSelected(filterUnchangedAttributes);
			final JButton roleSelection = new JButton(CommonLocaleDelegate.getMessage("StatePropertiesPanel.12", "Benutzergruppen"), Icons.getInstance().getIconLDAP());
			final JButton help = new JButton(Icons.getInstance().getIconHelp());
			
			
			/**
			 * Header in northwest
			 */
			JPanel nw = new JPanel();
			nw.setLayout(new TableLayout(new double[][]{
				new double[]{TableLayout.FILL, TableLayout.PREFERRED, GAP_ROWHEADER+5},
				new double[]{TableLayout.FILL}
			}));
			if (!modules.isEmpty()) {
				nw.add(new HorizontalLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.13", "Pflichtfeld")), "1,0,r,b");
			} else {
				nw.add(new JLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.14", "Keine Verwendung angegeben!")), "1,0");
			}
			nw.setBackground(COLOR_BACKGROUND);
			
			header.add(nw, "1,0");
			
			if (modules.isEmpty())
				return;
			
			if (statevo == null) {
				return;
			}
			
			
			/**
			 * init columns
			 */
			int iCol = 2;
			final Map<RightAndMandatoryColumnHeader, Integer> colsInLayout = new HashMap<RightAndMandatoryColumnHeader, Integer>();
			final Map<Integer, RightAndMandatoryColumnHeader> roleColumns = new HashMap<Integer, RightAndMandatoryColumnHeader>();
			if (firstInstance) {
				firstInstance = false;
				Preferences prefs = ClientPreferences.getUserPreferences().node("collect").node("entity").node(NuclosEntity.STATEMODEL.getEntityName());
				List<Integer> selectedRolesFromPreferences = PreferencesUtils.getIntegerList(prefs, PREFS_NODE_SELECTEDROLES);
				
				boolean showBubble = false;
				if (selectedRolesFromPreferences.size() > 0) {
					for (Integer role : rolesSortOrder) {
						if (selectedRolesFromPreferences.contains(role)) {
							selectedRoles.add(role);
						}
					}
					showBubble = selectedRoles.size() != rolesSortOrder.size();
				} else {
					if (rolesSortOrder.size() > 12) {
						for (int i = 0; i < 12; i++) {
							selectedRoles.add(rolesSortOrder.get(i));
						}
						showBubble = true;
					} else {
						selectedRoles.addAll(rolesSortOrder);
					}
				}
				
				if (showBubble) {
					final Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							boolean tryAgain = true;
							while(tryAgain) {
								try {
									 Thread.currentThread().sleep(2000);
								}
								catch(InterruptedException e1) {
									tryAgain = false;
								}
								try {
									roleSelection.getLocationOnScreen();
									tryAgain = false;
									(new Bubble(roleSelection, CommonLocaleDelegate.getMessage("StatePropertiesPanel.15", "Einige Benutzergruppen sind ausgeblendet."), 5, Bubble.Position.SE)).setVisible(true);
								} catch (IllegalComponentStateException e) {
									// do nothing. it is not shown
								}
							}
						}
					}, "StateModelProperties.showBubble()");
					t.start();
				}
			}
			for (String role : rolesSorted.keySet()) {
				RightAndMandatoryColumnHeader col = new RightAndMandatoryColumnHeader(rolesSorted.get(role), role, detailsChangedListener);
				roleColumns.put(rolesSorted.get(role), col);
				header.add(col.initView(100), iCol + ",0");
				colsInLayout.put(col, iCol);
				if (!selectedRoles.contains(rolesSorted.get(role))) {
					layoutHeader.setColumn(iCol, 0);
				}
				iCol++;
			}
			JPanel lastGridOnTheRight = new JPanel() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g;				
					g2d.setStroke(new BasicStroke(1.f));
					g2d.setColor(COLOR_MARKER_GRID);
					g2d.drawLine(0, 0, 0, CELL_HEIGHT-1);
					g2d.setColor(COLOR_GRID);
					g2d.drawLine(0, CELL_HEIGHT, 0, COLUMN_HEADER_HEIGHT_MAX);
				}
			};
			header.add(lastGridOnTheRight, iCol + ",0");
			
			class LastGridOfGroup extends JPanel {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2d = (Graphics2D) g;				
					g2d.setStroke(new BasicStroke(1.f));
					g2d.setColor(COLOR_GRID);
					int rowHeader = Double.valueOf(layoutHeader.getColumn(1)).intValue();
					g2d.drawLine(0, 0, rowHeader-GAP_ROWHEADER-1, 0);
					g2d.drawLine(rowHeader, 0, rowHeader+(selectedRoles.size()*(CELL_WIDTH-1)), 0);
				};
			}
			
			
			/**
			 * Attributes with groups
			 */
			int iRow = 0;
			final Map<RightAndMandatoryRow, Integer> rowsInLayout = new HashMap<RightAndMandatoryRow, Integer>(); 
			final Map<RightAndMandatoryRow, Set<RightAndMandatoryRow>> groupsWithArttributes = new HashMap<RightAndMandatoryRow, Set<RightAndMandatoryRow>>(); 
			
			class GroupExpandCollapseListener extends ExpandCollapseListener {
				
				private final RightAndMandatoryRow row;
				
				public GroupExpandCollapseListener(RightAndMandatoryRow row) {
					super();
					this.row = row;
				}
				
				@Override
				public void expand() {
					collapsedGroups.remove(row.getId());
					row.setCollapsed(false);
					for (RightAndMandatoryRow attrRow : groupsWithArttributes.get(row)) {
						attrRow.setCollapsed(false);
						if (filterUnchangedAttributes && !attrRow.isMandatory() && !attrRow.isRightsEnabled()) {
							continue;
						}
						int rowInLayout = rowsInLayout.get(attrRow);
						layoutRows.setRow(rowInLayout, rowSizes[rowInLayout]); // back to default
					}
					rows.revalidate();
					rows.repaint();
				}
				@Override
				public void collapse() {
					collapsedGroups.add(row.getId());
					row.setCollapsed(true);
					for (RightAndMandatoryRow attrRow : groupsWithArttributes.get(row)) {
						attrRow.setCollapsed(true);
						layoutRows.setRow(rowsInLayout.get(attrRow), 0);
					}
					rows.revalidate();
					rows.repaint();
				}
			};
			
			for (String attributeGroup : attributeGroupsSorted.keySet()) {
				if (attributes.keySet().contains(attributeGroupsSorted.get(attributeGroup))) {
					final RightAndMandatoryRow rowGroup = getRow(attributeGroupsSorted.get(attributeGroup).intValue(), false, null, attributeGroup, false, statevo, true, rolesSortOrder, null, detailsChangedListener);
					iRow = addGroupToLayout(iRow, rowGroup, rowsInLayout, groupsWithArttributes, rows);
					
					for (Attribute attribute : attributes.get(attributeGroupsSorted.get(attributeGroup))) {
						Long attributeId = attribute.getMetaData().getId();
						RightAndMandatoryRow rowAttribute = getRow(null, false, attributeId.intValue(), attribute.getMetaData().getField(), metaMandatory.contains(attributeId), statevo, true, rolesSortOrder, rowGroup, detailsChangedListener);
						iRow = addAttributeToLayout(iRow, rowGroup, rowAttribute, rowsInLayout, groupsWithArttributes, rows);
					}
					
					rows.add(new LastGridOfGroup(), "1," + iRow);
					iRow++;
					
					//set expand/collapse listener for this group
					rowGroup.setExpandCollapseListener(new GroupExpandCollapseListener(rowGroup));
				}
			}
			
			
			/**
			 * Subforms with columns
			 */
			iRow++; // GAP
			if (!subFormWithColumns.isEmpty()) {
				JLabel lab = new JLabel("<html><b>" + CommonLocaleDelegate.getMessage("StatePropertiesPanel.16", "Unterformulare") + ":</b></html>");
				lab.setForeground(COLOR_SELECTION_BACKGROUND);
				rows.add(lab, "1," + iRow);
			}
			iRow++; 
			iRow++; // GAP
			
			for (SubForm subform : subFormWithColumns.keySet()) {
				final RightAndMandatoryRow rowGroup = getRow(subform.getMetaData().getId().intValue(), true, null, subform.getMetaData().getEntity(), false, statevo, true, rolesSortOrder, null, detailsChangedListener);
				iRow = addGroupToLayout(iRow, rowGroup, rowsInLayout, groupsWithArttributes, rows);
				
				if (DEV_MODUS)
				for (Attribute attribute : subFormWithColumns.get(subform)) {
					Long attributeId = attribute.getMetaData().getId();
					RightAndMandatoryRow rowAttribute = getRow(null, true, attributeId.intValue(), attribute.getMetaData().getField(), metaMandatory.contains(attributeId), statevo, false, rolesSortOrder, rowGroup, detailsChangedListener);
					iRow = addAttributeToLayout(iRow, rowGroup, rowAttribute, rowsInLayout, groupsWithArttributes, rows);
				}
				
				rows.add(new LastGridOfGroup(), "1," + iRow);
				iRow++;
				
				//set expand/collapse listener for this group
				if (DEV_MODUS) rowGroup.setExpandCollapseListener(new GroupExpandCollapseListener(rowGroup));
			}
			
			
			/**
			 * hide not selected roles in rows
			 */
			for (Integer role : rolesSortOrder) {
				if (!selectedRoles.contains(role)) {
					for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
						row.hideRoleRight(role);
					}
				}
			}
			
			
			/**
			 * restore collapsed groups
			 */
			for(Integer group : collapsedGroups) {
				for (RightAndMandatoryRow rowGroup : groupsWithArttributes.keySet()) {
					if (group.equals(rowGroup.getId())) {
						if (rowGroup.getExpandCollapseListener() != null)
							rowGroup.getExpandCollapseListener().actionPerformed(new ActionEvent(rowGroup, rowGroup.getId(), ExpandCollapseListener.COMMAND_COLLAPSE));
					}
				}
			}
			
			
			/**
			 * hide unchanged attributes
			 */
			final ActionListener filterUnchangedAttributesListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					filterUnchangedAttributes = filter.isSelected();
					
					for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
						if (!row.isGroup()) {
							if (!row.isMandatory() && !row.isRightsEnabled()) {
								if (filterUnchangedAttributes) {
									layoutRows.setRow(rowsInLayout.get(row), 0);
								} else {
									if (!row.isCollapsed()) {
										int rowInLayout = rowsInLayout.get(row);
										layoutRows.setRow(rowInLayout, rowSizes[rowInLayout]); // back to default
									}
								}
							} else {
								// pasted rights in filtered attributes
								if (!row.isCollapsed()) {
									int rowInLayout = rowsInLayout.get(row);
									layoutRows.setRow(rowInLayout, rowSizes[rowInLayout]); // back to default
								}
							}
						}
					}
					
					rows.revalidate();
					rows.repaint();
				}
			};
			filterUnchangedAttributesListener.actionPerformed(new ActionEvent(filter, 0, "INIT_FILTER_UNCHANGED_ATTRIBUTES"));
			
			
			/**
			 * add selection listener
			 */
			SelectionListener sl = new SelectionListener() {
				@Override
				public void select(Integer role) {
					deselect();
					roleColumns.get(role).setSelected(true);
					for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
						row.setSelected(role, true);
					}
				}

				@Override
				public void deselect() {
					for (RightAndMandatoryColumnHeader col : roleColumns.values()) {
						col.setSelected(false);
					}
					for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
						row.removeAllSelections();
					}
				}
			};
			for (RightAndMandatoryColumnHeader col : roleColumns.values()) {
				col.setSelectionListener(sl);
			}
			for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
				row.setSelectionListener(sl);
			}
			
			
			/**
			 * add multiedit column listener
			 */
			MultiEditListener mel = new MultiEditListener() {
				@Override
				public void setRoleRight(Integer role, Boolean right) {
					for (RightAndMandatoryRow group : groupsWithArttributes.keySet()) {
						group.setRoleRight(role, right);
						for (RightAndMandatoryRow attr : groupsWithArttributes.get(group)) {
							attr.setRoleRight(role, right);
						}
					}
					detailsChanged();
				}
			};
			for (RightAndMandatoryColumnHeader col : roleColumns.values()) {
				col.setMultieditListener(mel);
			}
			
			
			/**
			 * init righttransfer for copy & paste between complete role rights from one state to other,
			 * and for rights of one role to other.
			 * 
			 * also used to fill statevo. see <code>updateStateVO</code>
			 */
			rightTransfer = new RightTransfer(){
				@Override
				public RoleRights getAllRoleRights() {
					RoleRights rr = new RoleRights();
					for (Integer role : rolesSortOrder) {
						rr.rights.put(role, getRoleRight(role));
					}
					for (RightAndMandatoryRow group : groupsWithArttributes.keySet()) {
						for (RightAndMandatoryRow attr : groupsWithArttributes.get(group)) {
							if (attr.isMandatory()) {
								if (attr.isSubForm()) {
									rr.mandatoryColumns.add(new Pair<String, String> (attr.getGroupName(), attr.getName()));
								} else {
									rr.mandatoryFields.add(attr.getId());
								}
							}
							if (attr.isRightsEnabled()) {
								rr.rightsEnabled.add(attr.getId());
							}
						}
					}
					return rr;
				}
				@Override
				public RoleRight getRoleRight(Integer role) {
					RoleRight rr = new RoleRight();
					for (RightAndMandatoryRow group : groupsWithArttributes.keySet()) {
						rr.groupRights.put(group.getId(), group.getRoleRights().get(role));
						rr.groupNames.put(group.getId(), group.getGroupName());
						if (group.isSubForm()) rr.groupIsSubform.add(group.getId());
						for (RightAndMandatoryRow attr : groupsWithArttributes.get(group)) {
							rr.attributeRights.put(attr.getId(), attr.isRightsEnabled()? 
								attr.getRoleRights().get(role) : group.getRoleRights().get(role));
							if (group.isSubForm()) rr.subformColumns.addValue(group.getId(), new Pair<Integer, String>(attr.getId(), attr.getName()));
						}
					}
					
					return rr;
				}
				@Override
				public void setAllRoleRights(RoleRights rr) {
					for (Integer role : rolesSortOrder) {
						for (RightAndMandatoryRow group : groupsWithArttributes.keySet()) {
							group.setRoleRight(role, rr.rights.get(role).groupRights.get(group.getId()));
							for (RightAndMandatoryRow attr : groupsWithArttributes.get(group)) {
								attr.setMandatory(rr.mandatoryFields.contains(attr.getId()) ||
									rr.mandatoryColumns.contains(new Pair<String, String>(group.getName(), attr.getName())));
								attr.setRightsEnabled(rr.rightsEnabled.contains(attr.getId()));
								attr.setRoleRight(role, rr.rights.get(role).attributeRights.get(attr.getId()));
							}
						}
					}
					filterUnchangedAttributesListener.actionPerformed(new ActionEvent(rr, 0, "PASTE_ALL_ROLE_RIGHTS"));
				}
				@Override
				public void setRoleRight(RoleRight rr) {
					for (Integer role : roleColumns.keySet()) {
						if (roleColumns.get(role).isSelected()) {
							for (RightAndMandatoryRow group : groupsWithArttributes.keySet()) {
								group.setRoleRight(role, rr.groupRights.get(group.getId()));
								for (RightAndMandatoryRow attr : groupsWithArttributes.get(group)) {
									Boolean newAttributeRight = rr.attributeRights.get(attr.getId());
									if (!attr.isRightsEnabled()) {
										// if group right differs from attribute right ... enable rights
										Boolean groupRight = group.getRoleRights().get(role);
										if (!LangUtils.equals(newAttributeRight, groupRight)) {
											attr.copyRightsFromGroup();
											attr.setRoleRight(role, newAttributeRight);
										}
									} else {
										attr.setRoleRight(role, newAttributeRight);
									}
								}
							}
						}
					}
					filterUnchangedAttributesListener.actionPerformed(new ActionEvent(rr, 0, "PASTE_ROLE_RIGHT"));
				}
			};
			for (RightAndMandatoryColumnHeader col : roleColumns.values()) {
				col.setRightTransfer(rightTransfer);
			}
			
			
			/**
			 * adjust row header width and column header height
			 */
			layoutHeader.setColumn(1, adjustRowHeaderWidth(rowsInLayout.keySet()) + GAP_ROWHEADER);
			layoutHeader.setRow(0, adjustColumnHeaderHeight(roleColumns.values()) + CELL_HEIGHT);
			
			
			/**
			 * set toolbar functions
			 */
			filter.addActionListener(filterUnchangedAttributesListener);
			toolBar.add(filter);
			roleSelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					RoleSelection roleSelectionCtrl = new RoleSelection(roleSelection);
					ChoiceList<RightAndMandatoryColumnHeader> ro = new ChoiceList<RightAndMandatoryColumnHeader>();
					ro.set(CollectionUtils.select(roleColumns.values(),
							new Predicate<RightAndMandatoryColumnHeader>() {
								@Override
								public boolean evaluate(
										RightAndMandatoryColumnHeader t) {
									return !selectedRoles.contains(t.getId());
								}
							}), CollectionUtils.sorted(CollectionUtils.select(
							roleColumns.values(),
							new Predicate<RightAndMandatoryColumnHeader>() {
								@Override
								public boolean evaluate(
										RightAndMandatoryColumnHeader t) {
									return selectedRoles.contains(t.getId());
								}
							}), new RightAndMandatoryColumnHeader.Comparator()),
							new RightAndMandatoryColumnHeader.Comparator());
					roleSelectionCtrl.setModel(ro);
					boolean changed = roleSelectionCtrl.run( 
							CommonLocaleDelegate.getMessage("StatePropertiesPanel.17", "Benutzergruppenauswahl"));
					
					if (changed) {
						@SuppressWarnings("unchecked")
						List<RightAndMandatoryColumnHeader> selectedColsChanged = (List<RightAndMandatoryColumnHeader>) roleSelectionCtrl.getSelectedObjects();
						Set<Integer> selectedRolesNew = new HashSet<Integer>();
						
						for (RightAndMandatoryColumnHeader col : colsInLayout.keySet()) {
							if (selectedColsChanged.contains(col)) {
								selectedRolesNew.add(col.getId());
								
								if (!selectedRoles.contains(col.getId())) {
									layoutHeader.setColumn(colsInLayout.get(col), colSizes[colsInLayout.get(col)]);
									for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
										row.showRoleRight(col.getId());
									}
								}
							} else {
								if (selectedRoles.contains(col.getId())) {
									layoutHeader.setColumn(colsInLayout.get(col), 0);
									for (RightAndMandatoryRow row : rowsInLayout.keySet()) {
										row.hideRoleRight(col.getId());
									}
								}
							}
						}
						selectedRoles.clear();
						selectedRoles.addAll(selectedRolesNew);
						
						header.revalidate();
						header.repaint();
						
						sendWidthChanged();
						
						// save to preferences
						Preferences prefs = ClientPreferences.getUserPreferences().node("collect").node("entity").node(NuclosEntity.STATEMODEL.getEntityName());
						try {
							PreferencesUtils.putIntegerList(prefs, PREFS_NODE_SELECTEDROLES, new ArrayList<Integer>(selectedRoles));
						}
						catch(PreferencesException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			toolBar.add(roleSelection);
			help.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					(new Bubble(help, CommonLocaleDelegate.getMessage("StatePropertiesPanel.18", "<html>grau=nicht sichtbar<br/>gelb=lesen<br/>gr�n=schreiben</html>"), 10, Bubble.Position.SE)).setVisible(true);
				}
			});
			toolBar.add(help);
			
			
			/**
			 * adjust splitpane
			 */
			sendWidthChanged();
			
			
			/**
			 * scroll to last position
			 */
			scrollRows.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					scrollPosition = e.getValue();
				}
			});
			scrollRows.getVerticalScrollBar().setValues(scrollPosition!=-1?scrollPosition:0, CELL_HEIGHT, 0, 0);
			scrollRows.getVerticalScrollBar().setUnitIncrement(CELL_HEIGHT);
		}
		
		/**
		 * send width change to <code>actionListenerForWidthChanged</code>
		 */
		private void sendWidthChanged() {
			if (actionListenerForWidthChanged != null) {
				actionListenerForWidthChanged.actionPerformed(new ActionEvent(StateDependantRightsPanel.this, 0, "WIDTH_CHANGED"));
			}
		}
		
		/**
		 * 
		 * select roles to show in columns
		 */
		class RoleSelection extends SelectObjectsController {

			public RoleSelection(Component parent) {
				super(parent, new DefaultSelectObjectsPanel());
			}

		}
		
		/**
		 * 
		 * @param roleCount
		 * @return
		 */
		private double[] getColumns(int roleCount) {
			double[] result = new double[roleCount + 5];
			result[0] = 10;
			result[1] = TableLayout.PREFERRED;
			for (int i = 2; i < result.length; i++) {
				result[i] = CELL_WIDTH-1;
			}
			result[result.length-3] = 1; //last grid line on the right
			result[result.length-2] = CELL_WIDTH; //delete button
			result[result.length-1] = CELL_WIDTH; //scrollbar width
			
			return result;
		}
		
		/**
		 * 
		 * @param attributeGroupsSorted
		 * @param attributes
		 * @param subFormWithColumns
		 * @return
		 */
		private double[] getRows(SortedMap<String, Long> attributeGroupsSorted, Map<Long, SortedSet<Attribute>> attributes, SortedMap<SubForm, SortedSet<Attribute>> subFormWithColumns) {
			int countGroups1 = 0;
			int countAttributes1 = 0;
			int countGroups2 = 0;
			int countAttributes2 = 0;
			
			for (String group : attributeGroupsSorted.keySet()) {
				if (attributes.containsKey(attributeGroupsSorted.get(group))) {
					countGroups1++;
					countAttributes1 = countAttributes1 + attributes.get(attributeGroupsSorted.get(group)).size();
				}
			}
			for (SubForm subform : subFormWithColumns.keySet()) {
				countGroups2++;
				if (DEV_MODUS) countAttributes2 = countAttributes2 + subFormWithColumns.get(subform).size();
			}
			
			double[] result = new double[(countGroups1*2) + countAttributes1 + 2/*=LINE_BREAK*/ + 1 /*=SubformLabel*/ + (countGroups2*2) + countAttributes2];
			int i = 0;
			for (String group : attributeGroupsSorted.keySet()) {
				if (attributes.containsKey(attributeGroupsSorted.get(group))) {
					result[i] = CELL_HEIGHT;
					i++;
					
					for (@SuppressWarnings("unused") Attribute efMeta : attributes.get(attributeGroupsSorted.get(group))) {
						result[i] = CELL_HEIGHT-1;
						i++;
					}
					
					result[i] = 1;
					i++;
				}
			}
			
			// Subforms
			
			result[i] = GAP_LINEBREAK;
			i++;
			result[i] = CELL_HEIGHT;
			i++;
			result[i] = GAP_LINEBREAK;
			i++;
			
			for (SubForm subform : subFormWithColumns.keySet()) {
				result[i] = CELL_HEIGHT;
				i++;
				
				if (DEV_MODUS) for (@SuppressWarnings("unused") Attribute efMeta : subFormWithColumns.get(subform)) {
					result[i] = CELL_HEIGHT-1;
					i++;
				}
				
				result[i] = 1;
				i++;
			}
			
			return result;
		}
		
		private int addGroupToLayout(int iRow, RightAndMandatoryRow group, Map<RightAndMandatoryRow, Integer> rowsInLayout, Map<RightAndMandatoryRow, Set<RightAndMandatoryRow>> groupsWithArttributes, JPanel rows) {
			groupsWithArttributes.put(group, new HashSet<RightAndMandatoryRow>());
			rows.add(group.initView(100), "1," + iRow);
			rowsInLayout.put(group, iRow);
			iRow++;
			return iRow;
		}
		
		private int addAttributeToLayout(int iRow, RightAndMandatoryRow group, RightAndMandatoryRow attribute, Map<RightAndMandatoryRow, Integer> rowsInLayout, Map<RightAndMandatoryRow, Set<RightAndMandatoryRow>> groupsWithArttributes, JPanel rows) {
			groupsWithArttributes.get(group).add(attribute);
			rows.add(attribute.initView(100), "1," + iRow);
			rowsInLayout.put(attribute, iRow);
			iRow++;
			return iRow;
		}
		
		private RightAndMandatoryRow getRow(Integer groupId, boolean subform, Integer attributeId, String name, boolean metaMandatory, StateVO statevo, boolean nullRightAllowed, List<Integer> sortOrder, RightAndMandatoryRow group, ChangeListener detailsChangedListener) {
			boolean mandatory = false;
			boolean rightsEnabled = false;
			
			if (!metaMandatory) {
				for (MandatoryFieldVO mandatoryField : statevo.getMandatoryFields()) {
					if (mandatoryField.getFieldId().equals(attributeId))
						mandatory = true;
				}
				if (group != null) {
					for (MandatoryColumnVO mandatoryColumn : statevo.getMandatoryColumns()) {
						if (mandatoryColumn.getEntity().equals(group.getName())) {
							if (mandatoryColumn.getColumn().equals(name)) {
								mandatory = true;
							}
						}
					}
				}
			}
			
			Map<Integer, Boolean> roleRights = new HashMap<Integer, Boolean>();
			for (Integer role : sortOrder) {
				roleRights.put(role, null); // default is no rights
			}
			
			if (groupId != null) {
				rightsEnabled = true;
				if (subform) {
					for (Integer roleId : statevo.getUserSubformRights().asMap().keySet()) {
						for (SubformPermissionVO sfPerm : statevo.getUserSubformRights().asMap().get(roleId)) {
							if (sfPerm.getSubform().equals(name)) {
								roleRights.put(roleId, new Boolean(sfPerm.isWriteable()));
							}
						}
					}
				} else {
					for (Integer roleId : statevo.getUserRights().asMap().keySet()) {
						for (AttributegroupPermissionVO agPerm : statevo.getUserRights().asMap().get(roleId)) {
							if (agPerm.getAttributegroupId().equals(groupId)) {
								roleRights.put(roleId, new Boolean(agPerm.isWritable()));
							}
						}
					}
				}
			}
			
			if (attributeId != null) {
				if (subform) {
					for (Integer roleId : statevo.getUserSubformRights().asMap().keySet()) {
						for (SubformPermissionVO sfPerm : statevo.getUserSubformRights().asMap().get(roleId)) {
							for (SubformColumnPermissionVO sfcPerm : sfPerm.getColumnPermissions()) {
								if (sfcPerm.getColumn().equals(name)) {
									rightsEnabled = true;
									roleRights.put(roleId, new Boolean(sfcPerm.isWriteable()));
								}
							}
						}
					}
				}
				for (Integer roleId : statevo.getUserFieldRights().asMap().keySet()) {
					for (EntityFieldPermissionVO efPerm : statevo.getUserFieldRights().asMap().get(roleId)) {
						if (efPerm.getFieldId().equals(attributeId)) {
							rightsEnabled = true;
							roleRights.put(roleId, !efPerm.isReadable()? null : new Boolean(efPerm.isWriteable()));
						}
					}
				}
			}
			
			RightAndMandatoryRow result = new RightAndMandatoryRow(groupId==null?attributeId:groupId, name, rightsEnabled, roleRights, nullRightAllowed, mandatory, metaMandatory, sortOrder, group, subform, detailsChangedListener);
			
			return result;
		}
		
		private int adjustRowHeaderWidth(Set<RightAndMandatoryRow> rows) {
			int max = 0;
			for (RightAndMandatoryRow row : rows) {
				if (max < row.getRowHeaderWidth())
					max = row.getRowHeaderWidth();
			}
			
			for (RightAndMandatoryRow row : rows) {
				row.updateView(max);
			}
			
			return max;
		}
		
		private int adjustColumnHeaderHeight(Collection<RightAndMandatoryColumnHeader> cols) {
			int max = 0;
			for (RightAndMandatoryColumnHeader col : cols) {
				if (max < col.getColumnNameHeight())
					max = col.getColumnNameHeight();
			}
			if (max > COLUMN_HEADER_HEIGHT_MAX) {
				max = COLUMN_HEADER_HEIGHT_MAX;
			}
			
			for (RightAndMandatoryColumnHeader col : cols) {
				col.updateView(max);
			}
			
			return max;
		}
		
		private Set<Integer> getModules(List<CollectableEntityObject> usages) {
			Set<Integer> result = new HashSet<Integer>();
			for (CollectableEntityObject usage : usages)
				if (usage.getValueId("nuclos_module") != null) {
					Long lId = (Long)usage.getValueId("nuclos_module");
					result.add(lId.intValue());
				}
			return result;
		}
		
		private SortedMap<String, Integer> getRolesSorted(Collection<MasterDataVO> roles) {
			SortedMap<String, Integer> result = new TreeMap<String, Integer>();
			for (MasterDataVO roleVO : roles) {
				result.put(roleVO.getField("name", String.class), roleVO.getIntId());
			}			
			return result;
		}
		
		private List<Integer> getRolesSorted(SortedMap<String, Integer> sortedRoles) {
			List<Integer> result = new ArrayList<Integer>();
			for (String role : sortedRoles.keySet()) {
				result.add(sortedRoles.get(role));
			}			
			return result;
		}
		
		private Map<Long, SortedSet<Attribute>> getAttributes(Set<Integer> modules, Set<Long> metaMandatory) {
			Map<Long, SortedSet<Attribute>> result = new HashMap<Long, SortedSet<Attribute>>();
			
			for (Integer module : modules) {
				String entity = MetaDataClientProvider.getInstance().getEntity(module.longValue()).getEntity();
				for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).values()) {
					Long groupId = efMeta.getFieldGroupId();
					if (groupId == null) groupId = 0l;
					
					if (!result.containsKey(groupId)) {
						result.put(groupId, new TreeSet<Attribute>());
					}
					
					result.get(groupId).add(new Attribute(efMeta));
					
					if (!efMeta.isNullable())
						metaMandatory.add(efMeta.getId());
				}
			}
			
			return result;
		}
		
		private SortedMap<SubForm, SortedSet<Attribute>> getSubForms(Set<Integer> modules, Set<Long> metaMandatory) {
			SortedMap<SubForm, SortedSet<Attribute>> result = new TreeMap<SubForm, SortedSet<Attribute>>();
			Map<String, SubForm> entitySubForm = new HashMap<String, SubForm>();
			
			for (Integer module : modules) {
				for (EntityAndFieldName eafn : GenericObjectLayoutCache.getInstance().getSubFormEntities(module)) {
					if (!entitySubForm.containsKey(eafn.getEntityName())) {
						entitySubForm.put(eafn.getEntityName(), new SubForm(MetaDataClientProvider.getInstance().getEntity(eafn.getEntityName())));
					}
					SubForm sf = entitySubForm.get(eafn.getEntityName());
					if (!result.containsKey(sf)) {
						result.put(sf, new TreeSet<Attribute>());
					}
					if (!eafn.getEntityName().startsWith("dyn_")) { // no dynamic entity columns here
						for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(eafn.getEntityName()).values()) {
							if (efMeta.isDynamic()) 
								continue; // no dynamic entity columns here
							
							if (efMeta.getFieldGroupId() != null && 
								(NuclosEOField.GROUP_ID_READ.equals(efMeta.getFieldGroupId()) || NuclosEOField.GROUP_ID_WRITE.equals(efMeta.getFieldGroupId())))
								continue;
							
							if (!efMeta.getField().equals(eafn.getFieldName())) {
								result.get(sf).add(new Attribute(efMeta));
							}
							
							if (!efMeta.isNullable()) {
								metaMandatory.add(efMeta.getId());
							}
						}
					}
				}
			}
			
			return result;
		}
		
		private Map<Long, String> getAttributeGroups(Collection<Long> groups) throws CommonBusinessException {
			Map<Long, String> result = new HashMap<Long, String>();
			
			for (MasterDataVO groupVO : MasterDataCache.getInstance().get(NuclosEntity.ENTITYFIELDGROUP.getEntityName())) {
				Long groupId = groupVO.getIntId().longValue();
				if (groups.contains(groupId)) {
					result.put(groupId, groupVO.getField("name", String.class));
				}
			}
			
			result.put(0l, "[" + CommonLocaleDelegate.getMessage("StatePropertiesPanel.19", "Ohne Gruppe") + "]");
			
			return result;
		}
		
		private SortedMap<String, Long> getAttributeGroupsSorted(Map<Long, String> attributeGroups) {
			SortedMap<String, Long> result = new TreeMap<String, Long>(); 
			for (Entry<Long, String> group : attributeGroups.entrySet()) {
				result.put(group.getValue(), group.getKey());
			}
			return result;
		}

//		public int getSelectedSubform() {
//			return tbdpnRoleRights.getSelectedIndex();
//		}
//
//		public SubForm getSubformRoles() {
//			return subformRoles;
//		}
//
//		public SubForm getSubformAttributeGroups() {
//			return subformAttributeGroups;
//		}
//
//		public SubForm getSubformSubForms() {
//			return subformSubForms;
//		}
		
		public ActionListener getActionListenerForWidthChanged() {
			return actionListenerForWidthChanged;
		}

		public void setActionListenerForWidthChanged(ActionListener actionListenerForWidthChanged) {
			/*
			 * Temporary deactivated...
			 * TODO remove if not in user any more
			 */
			//this.actionListenerForWidthChanged = actionListenerForWidthChanged;
		}
		
	}	// inner class StateDependantRightsPanel
	
	/**
	 * 
	 * for sorting...
	 */
	private static class Attribute implements Comparable<Attribute> {

		final EntityFieldMetaDataVO efMeta;
		
		public Attribute(EntityFieldMetaDataVO efMeta) {
			super();
			this.efMeta = efMeta;
		}

		public EntityFieldMetaDataVO getMetaData() {
			return efMeta;
		}

		@Override
		public int compareTo(Attribute o) {
			return efMeta.getField().compareToIgnoreCase(o.getMetaData().getField());
		}
		
	}
	
	/**
	 * 
	 * for sorting...
	 */
	private static class SubForm implements Comparable<SubForm> {
		
		final EntityMetaDataVO eMeta;

		public SubForm(EntityMetaDataVO eMeta) {
			super();
			this.eMeta = eMeta;
		}

		public EntityMetaDataVO getMetaData() {
			return eMeta;
		}

		@Override
		public int compareTo(SubForm o) {
			return eMeta.getEntity().compareToIgnoreCase(o.getMetaData().getEntity());
		}		
		
	}
	
	public static class HorizontalLabel extends JLabel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean needsRotate;
		
		public HorizontalLabel(String text) {
			super(text);
		}
		
		@Override
		public Dimension getPreferredSize() {
			/*if (!needsRotate) {
			    return super.getPreferredSize();
			}*/
			
			Dimension size = super.getPreferredSize();
			return new Dimension(size.height, size.width);
		}

		@Override
		public Dimension getSize() {
			if (!needsRotate) {
				return super.getSize();
			}
			
			Dimension size = super.getSize();
			return new Dimension(size.height, size.width);
		}

		@Override
		public int getHeight() {
			return getSize().height;
		}

		@Override
		public int getWidth() {
			return getSize().width;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D gr = (Graphics2D) g.create();

			gr.transform(AffineTransform.getQuadrantRotateInstance(1));
			gr.translate(0, -getSize().getWidth());

			needsRotate = true;
			super.paintComponent(gr);
			needsRotate = false;
		}
		
	}

	private final StatePropertiesPanelModel model = new StatePropertiesPanelModel();

	private final StateDependantRightsPanel pnlStateDependantRights = new StateDependantRightsPanel();

	public StatePropertiesPanel() {
		super(new BorderLayout());
		final JTabbedPane tabpn = new JTabbedPane();
		this.add(tabpn, BorderLayout.CENTER);
		tabpn.addTab(CommonLocaleDelegate.getMessage("StatePropertiesPanel.6","Eigenschaften"), newStateBasicPropertiesPanel());
		tabpn.addTab(CommonLocaleDelegate.getMessage("StatePropertiesPanel.9","Berechtigungen"), pnlStateDependantRights);
	}

	/**
	 * @return a new panel containing the basic properties for a state.
	 */
	private JPanel newStateBasicPropertiesPanel() {
		final JPanel pnlStateProperties = new JPanel(new GridBagLayout());
		pnlStateProperties.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel labName = new JLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.7","Name"));
		final JTextField tfName = new JTextField();
		labName.setAlignmentY((float) 0.0);
		labName.setHorizontalAlignment(SwingConstants.LEADING);
		labName.setHorizontalTextPosition(SwingConstants.TRAILING);
		labName.setLabelFor(tfName);
		labName.setVerticalAlignment(SwingConstants.CENTER);
		labName.setVerticalTextPosition(SwingConstants.CENTER);

		tfName.setAlignmentX((float) 0.0);
		tfName.setAlignmentY((float) 0.0);
		tfName.setPreferredSize(new Dimension(100, 21));
		tfName.setDocument(model.docName);
		tfName.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		final JLabel labMnemonic = new JLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.8","Numeral"));
		final JTextField tfMnemonic = new JTextField();
		labMnemonic.setAlignmentY((float) 0.0);
		labMnemonic.setHorizontalAlignment(SwingConstants.LEADING);
		labMnemonic.setHorizontalTextPosition(SwingConstants.TRAILING);
		labMnemonic.setLabelFor(tfMnemonic);
		labMnemonic.setVerticalAlignment(SwingConstants.CENTER);
		labMnemonic.setVerticalTextPosition(SwingConstants.CENTER);

		tfMnemonic.setAlignmentX((float) 0.0);
		tfMnemonic.setAlignmentY((float) 0.0);
		tfMnemonic.setPreferredSize(new Dimension(100, 21));
		tfMnemonic.setText("");
		tfMnemonic.setDocument(model.docMnemonic);
		tfMnemonic.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		final JLabel labDescription = new JLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.5","Beschreibung"));
		final JTextArea taDescription = new JTextArea();
		labDescription.setAlignmentY((float) 0.0);
		labDescription.setHorizontalAlignment(SwingConstants.LEADING);
		labDescription.setHorizontalTextPosition(SwingConstants.TRAILING);
		labDescription.setIconTextGap(4);
		labDescription.setLabelFor(taDescription);
		labDescription.setVerticalAlignment(SwingConstants.TOP);
		labDescription.setVerticalTextPosition(SwingConstants.TOP);

		taDescription.setAlignmentX((float) 0.0);
		taDescription.setAlignmentY((float) 0.0);
		taDescription.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		taDescription.setText("");
		taDescription.setDocument(model.docDescription);
		taDescription.setFont(tfName.getFont());
		taDescription.setLineWrap(true);
		taDescription.setEditable(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));
		
		final JLabel labTabbedPaneName = new JLabel(CommonLocaleDelegate.getMessage("StatePropertiesPanel.1","Aktive Tablasche"));
		final JComboBox cmbbxTabbedPaneName = new JComboBox();
		labTabbedPaneName.setHorizontalAlignment(SwingConstants.LEADING);
		labTabbedPaneName.setHorizontalTextPosition(SwingConstants.TRAILING);
		labTabbedPaneName.setIconTextGap(4);
		labTabbedPaneName.setLabelFor(cmbbxTabbedPaneName);
		labTabbedPaneName.setVerticalAlignment(SwingConstants.CENTER);

		cmbbxTabbedPaneName.setModel(model.modelTab);
		cmbbxTabbedPaneName.setPreferredSize(new Dimension(100, 21));
		cmbbxTabbedPaneName.setAlignmentY((float) 0.0);
		cmbbxTabbedPaneName.setAlignmentX((float) 0.0);

		pnlStateProperties.setMaximumSize(new Dimension(2147483647, 2147483647));

		final JScrollPane scrlpn = new JScrollPane(taDescription);
		scrlpn.setAutoscrolls(true);
		scrlpn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.setAlignmentX((float) 0.0);
		this.setAlignmentY((float) 0.0);

		pnlStateProperties.add(labName,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfName,
				new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labMnemonic,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(tfMnemonic,
				new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labDescription,
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(scrlpn,
				new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						new Insets(2, 5, 0, 0), 0, 0));
		pnlStateProperties.add(labTabbedPaneName,
			new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(2, 0, 0, 5), 0, 0));
		pnlStateProperties.add(cmbbxTabbedPaneName,
			new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					new Insets(2, 5, 0, 0), 0, 0));
		return pnlStateProperties;
	}

	public StateDependantRightsPanel getStateDependantRightsPanel() {
		return this.pnlStateDependantRights;
	}

	public StatePropertiesPanelModel getModel() {
		return model;
	}

}	// class StatePropertiesPanel
