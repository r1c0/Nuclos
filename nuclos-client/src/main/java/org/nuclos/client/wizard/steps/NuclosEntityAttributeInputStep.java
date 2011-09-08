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
package org.nuclos.client.wizard.steps;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.wizard.NuclosEntityAttributeWizard;
import org.nuclos.client.wizard.NuclosEntityAttributeWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityAttributeInputStep extends NuclosEntityAbstractStep {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	JScrollPane scrolPane;
	JTable tblAttributes;

	JButton btnNewAttribute;
	JButton btnDropAttribute;
	JButton btnEditAttribute;
	TableColumn colGroup;

	static String[] sEditFields = {"STRCREATED","DATCREATED","STRCHANGED","DATCHANGED"};


	EntityAttributeTableModel entityModel;


	public NuclosEntityAttributeInputStep() {
		initComponents();
	}

	public NuclosEntityAttributeInputStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityAttributeInputStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}



	@Override
	protected void initComponents() {

		double size [][] = {{160,160,160,160,TableLayout.FILL}, {TableLayout.FILL, 25,10}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);

		entityModel = new EntityAttributeTableModel();

		tblAttributes = new JTable(entityModel);
		tblAttributes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblAttributes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tblAttributes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				btnDropAttribute.setEnabled(e.getFirstIndex() >= 0 && !isVirtual());
				btnEditAttribute.setEnabled(e.getFirstIndex() >= 0);
			}
		});


		tblAttributes.setDefaultRenderer(Boolean.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JCheckBox cb = new JCheckBox();

				switch (column) {
				case 5:
					cb.setSelected(((Boolean)value).booleanValue());
					break;
				case 6:
					cb.setSelected(((Boolean)value).booleanValue());
					break;
				case 7:
					cb.setSelected(((Boolean)value).booleanValue());
					break;

				default:
					break;
				}
				cb.setHorizontalAlignment(SwingConstants.CENTER);
				cb.setBackground(table.getBackground());

				return cb;
			}
		});

		tblAttributes.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int selected = tblAttributes.getSelectedRow();
					EntityAttributeTableModel model = (EntityAttributeTableModel)tblAttributes.getModel();
					Attribute attr = model.getObject(selected);
					showNuclosEntityAttributeWizard(attr, true, selected);
				}
			}
		});


		scrolPane = new JScrollPane();
		scrolPane.getViewport().add(tblAttributes);

		btnNewAttribute = new JButton(getMessage("wizard.step.inputattribute.1", "Attribut hinzuf\u00fcgen"));
		btnNewAttribute.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showNuclosEntityAttributeWizard(null,false,-1);
			}


		});

		btnDropAttribute = new JButton(getMessage("wizard.step.inputattribute.2", "Attribut entfernen"));
		btnDropAttribute.setEnabled(false);
		btnDropAttribute.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = tblAttributes.getSelectedRow();

				Attribute attr = entityModel.getObject(selected);

				StringBuffer sMessage = new StringBuffer();
				if(hasAttributeReferenz(attr, sMessage)) {
					JOptionPane.showMessageDialog(NuclosEntityAttributeInputStep.this, sMessage,
						getMessage("wizard.step.inputattribute.12", "Entfernen nicht möglich!"), JOptionPane.OK_OPTION);
					return;
				}

				Collection<MasterDataVO> colImportStructure = MetaDataDelegate.getInstance().hasEntityFieldInImportStructure(NuclosEntityAttributeInputStep.this.getModel().getEntityName(), attr.getInternalName());
				if(colImportStructure.size() > 0) {
					Collection<String> colImportStructureNames = CollectionUtils.transform(colImportStructure, new Transformer<MasterDataVO, String>() {
						@Override
						public String transform(MasterDataVO vo) {
							return (String)vo.getField("name");
						}
					});

					String sMessageText = getMessage("wizard.step.inputattribute.15", "Das Attribut wird in der Import Strukturdefinition ");

					for(String sImport : colImportStructureNames){
						sMessageText += sImport + " ";
					}

					sMessageText += getMessage("wizard.step.inputattribute.16", "\nDie Referenz wird entfernt, wenn das Attribut gelöscht wird!\nSoll das Attribut trotzdem gelöscht werden?");
					int option = JOptionPane.showConfirmDialog(NuclosEntityAttributeInputStep.this, sMessageText,getMessage("wizard.step.inputattribute.17", "Achtung"), JOptionPane.YES_NO_OPTION);
					if(option != JOptionPane.YES_OPTION) {
						return;
					}
				}

				if(attr.getMetaVO() != null) {
					if(isSubformEntity(attr)){
						if(NuclosEntityAttributeInputStep.this.getModel().hasRows()) {
							String sText = getMessage("wizard.step.inputattribute.14",
								"Die Entität enthält bereits Daten. Wenn Sie dieses Attribut löschen,\n" +
									"verliert die Hauptentität, die Datensätze dieser Entität.\n" +
									"Das Unterformular wird ebenfalls aus der Maske herausgenommen.");
							final JOptionPane pane = new JOptionPane(sText, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
							final JDialog dia = new JDialog(Main.getMainFrame(), true);
							pane.addPropertyChangeListener(new PropertyChangeListener() {
							        @Override
									public void propertyChange(PropertyChangeEvent e) {
							            String prop = e.getPropertyName();
							            if (dia.isVisible() && (e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
							                dia.setVisible(false);
							            }
							        }
							    });

							dia.setContentPane(pane);
							dia.setTitle(getMessage("wizard.step.inputattribute.13", "Attribut entfernen?"));
							dia.setLocationRelativeTo(NuclosEntityAttributeInputStep.this.getModel().getParentFrame());
							dia.pack();
							dia.setVisible(true);

							if(!(pane.getValue() instanceof Integer))
								return;

							int value = ((Integer)pane.getValue()).intValue();

							if(value != JOptionPane.YES_OPTION) {
								return;
							}
						}
					}
				}

				if(selected < 0)
					return;
				entityModel.removeRow(selected, true);
				NuclosEntityAttributeInputStep.this.model.setAttributeModel(entityModel);
			}
		});

		btnEditAttribute = new JButton(getMessage("wizard.step.inputattribute.3", "Attribut bearbeiten"));
		btnEditAttribute.setEnabled(false);
		btnEditAttribute.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = tblAttributes.getSelectedRow();
				if(selected < 0) {
					return;
				}
				EntityAttributeTableModel model = (EntityAttributeTableModel)tblAttributes.getModel();
				Attribute attr = model.getObject(selected);
				showNuclosEntityAttributeWizard(attr, true, selected);
			}
		});

		this.add(scrolPane, new TableLayoutConstraints(0, 0, 4, 0));
		this.add(btnNewAttribute, "0,1");
		this.add(btnDropAttribute, "1,1");
		this.add(btnEditAttribute, "2,1");

	}

	private boolean isVirtual() {
		return model != null ? model.isVirtual() : false;
	}

	private boolean isSubformEntity(Attribute attribute) {
		final Set<String> set = NuclosWizardUtils.searchParentEntity(model.getEntityName());
		if(set.size() > 0) {
			if(set.contains(attribute.getMetaVO().getEntity()))
				return true;
		}
		return false;
	}



	@Override
	public void prepare() {
		super.prepare();
		if(this.model.isEditMode() || (this.model.getAttributeModel() != null && this.model.getAttributeModel().getAttributes().size() > 0)) {
			EntityAttributeTableModel tableModel = (EntityAttributeTableModel)tblAttributes.getModel();
			List<Attribute> lstAttribute = this.model.getAttributeModel().getAttributes();
			for(Attribute attr : lstAttribute) {
				boolean hasAlready = false;
				for(Attribute attrModel : tableModel.getAttributes()) {
					if(attr.getInternalName().equals(attrModel.getInternalName())) {
						hasAlready = true;
						break;
					}
				}
				if(!hasAlready) {
					tableModel.addAttribute(attr);
					tableModel.addTranslation(attr, this.model.getAttributeModel().getTranslation().get(attr));
				}
			}
			this.setComplete(true);
			this.tblAttributes.repaint();
		}

		btnNewAttribute.setEnabled(!this.model.isImportTable());

		initTableSorter();


		if(!this.model.isStateModel()) {
			if(tblAttributes.getColumnCount() > 9) {
				colGroup = tblAttributes.getColumnModel().getColumn(9);
				tblAttributes.getColumnModel().removeColumn(colGroup);
			}
		}
		else {
			if(tblAttributes.getColumnCount() == 9) {
				if(colGroup != null) {
					tblAttributes.getColumnModel().addColumn(colGroup);
				}
			}
		}

		TableUtils.setOptimalColumnWidths(tblAttributes);

		if (model.isVirtual()) {
			btnNewAttribute.setEnabled(false);
		}
	}

	@Override
	public void applyState() throws InvalidStateException {
		super.applyState();
		EntityAttributeTableModel tableModel = (EntityAttributeTableModel)tblAttributes.getModel();
		List<String> missingreferences = new ArrayList<String>();
		for (Attribute a : tableModel.getAttributes()) {
			if (a.getDatatyp().isRefenceTyp() && a.getMetaVO() == null) {
				missingreferences.add(a.getInternalName());
			}
		}
		if (missingreferences.size() > 0) {
			String message = getMessage("wizard.step.inputattribute.validation.reference", "Please select a foreign entity for each reference field ({0}).", StringUtils.join(", ", missingreferences));
			JOptionPane.showMessageDialog(this, message, getMessage("wizard.step.entitycommonproperties.19", "Achtung!"), JOptionPane.OK_OPTION);
	        throw new InvalidStateException();
		}
	}

	private void initTableSorter() {
	    final MyRowSorter sorter = new MyRowSorter(tblAttributes.getModel());
		sorter.setMaxSortKeys(1);

		sorter.addRowSorterListener(new RowSorterListener() {

			@Override
			public void sorterChanged(RowSorterEvent e) {
				sorter.sortModel();

			}
		});

		tblAttributes.setRowSorter(sorter);
		sorter.setComparator(0, new StringComparator());
		sorter.setComparator(1, new StringComparator());
		sorter.setComparator(2, new DataTypComparator());
		sorter.setComparator(3, new IntegerComparator());
		sorter.setComparator(4, new IntegerComparator());
		sorter.setComparator(5, new BooleanComparator());
		sorter.setComparator(6, new BooleanComparator());
		sorter.setComparator(7, new BooleanComparator());
		sorter.setComparator(8, new StringComparator());
		if(model.isStateModel())
			sorter.setComparator(9, new StringComparator());
    }

	protected void showNuclosEntityAttributeWizard(Attribute attr, final boolean editMode, final int row) {

		final MainFrameTab tabAttribute = new MainFrameTab(getMessage("wizard.step.inputattribute.8", "Attribut Wizard f\u00fcr Entit\u00e4t"+" " + NuclosEntityAttributeInputStep.this.model.getEntityName()));

		try {
			final NuclosEntityAttributeWizardStaticModel model = new NuclosEntityAttributeWizardStaticModel(tblAttributes.getModel().getRowCount());
			model.setEditMode(editMode);
			model.setLastVisible(false);
			if(attr != null) {
				model.setAttribute((Attribute)attr.clone());
				if(attr.isValueList())
					model.setValueListTyp(true);
				else
					model.setValueListTyp(false);
			}
			model.setTranslation(entityModel.getTranslation().get(attr));

			NuclosEntityAttributePropertiesStep step1 = new NuclosEntityAttributePropertiesStep(getMessage("wizard.step.inputattribute.4", "Eigenschaften"), getMessage("wizard.step.inputattribute.4", "Eigenschaften"));
			step1.setParentWizardModel(this.model);
			step1.setParent(tabAttribute);
			step1.setColumnTypeChangeAllowed(!this.hasEntityValues());
			NuclosEntityAttributeValueListShipStep step2a = new NuclosEntityAttributeValueListShipStep(getMessage("wizard.step.inputattribute.5", "Werteliste"), getMessage("wizard.step.inputattribute.5", "Werteliste"));
			NuclosEntityAttributeRelationShipStep step2b = new NuclosEntityAttributeRelationShipStep(getMessage("wizard.step.inputattribute.10", "Verkn\u00fcpfung zu anderen Entit\u00e4ten"), getMessage("wizard.step.inputattribute.10", "Verkn\u00fcpfung zu anderen Entit\u00e4ten"));
			step2b.setAttributeList(this.getModel().getAttributeModel().getAttributes());
			step2b.setParentWizardModel(this.model);
			NuclosEntityAttributeTranslationStep step3 = new NuclosEntityAttributeTranslationStep(getMessage("wizard.step.inputattribute.6", "\u00dcbersetzungen"), getMessage("wizard.step.inputattribute.6", "\u00dcbersetzungen"));
			step3.setParentWizardModel(this.model);
			step3.setComplete(true);
			NuclosEntityAttributeCommonPropertiesStep step4 = new NuclosEntityAttributeCommonPropertiesStep(getMessage("wizard.step.inputattribute.7", "Allgemeine Eigenschaften"), getMessage("wizard.step.inputattribute.7", "Allgemeine Eigenschaften"));
			step4.setParentWizardModel(this.model);
			model.add(step1);
			model.add(step2a);
			model.add(step2b);
			model.add(step3);
			model.add(step4);


			NuclosEntityAttributeWizard wizard = new NuclosEntityAttributeWizard(model);

			wizard.addWizardListener(new WizardListener() {
				@Override
				public void wizardClosed(WizardEvent e) {
					Attribute attribute = model.getAttribute();
					if(!editMode || attribute.getInternalId() == null) {
						attribute.setInternalId(getHighestInternalId());
					}

					if(row >= 0 && editMode) {
						entityModel.removeRow(row, false);
					}

					entityModel.addAttribute(attribute);
					entityModel.addTranslation(attribute, model.getTranslation());

					NuclosEntityAttributeInputStep.this.setComplete(true);
					NuclosEntityAttributeInputStep.this.model.setAttributeModel(entityModel);

					tabAttribute.dispose();
					TableUtils.setOptimalColumnWidths(tblAttributes);
				}
				@Override
				public void wizardCancelled(WizardEvent e) {
					tabAttribute.dispose();
				}
			});

			tabAttribute.setLayeredComponent(wizard);
			parent.add(tabAttribute);
		}
		catch(CommonFinderException ex) {
			Errors.getInstance().showExceptionDialog(NuclosEntityAttributeInputStep.this, ex);
		}
		catch(CommonPermissionException e) {
			Errors.getInstance().showExceptionDialog(NuclosEntityAttributeInputStep.this, e);
		}

	}

	private Long getHighestInternalId() {
		Long l = new Long(0);
		for(Attribute attr : model.getAttributeModel().getAttributes()) {
			if(attr.getInternalId() != null) {
				if(l.longValue() < attr.getInternalId()) {
					l = attr.getInternalId();
					l++;
				}
			}
		}
		return l;
	}


	private boolean hasEntityValues() {
		boolean yes = false;

		if(this.model.isEditMode()) {
			return this.model.hasRows();
		}

		return yes;
	}


	public void resetStep() {
		entityModel = new EntityAttributeTableModel();
		tblAttributes.setModel(entityModel);
	}

	private boolean hasAttributeReferenz(Attribute attr, StringBuffer message) {
		boolean blnRef = false;

		for(EntityMetaDataVO vo : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(vo.getEntity().equals(this.model.getEntityName()))
				continue;
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(vo.getEntity()).values()) {
				if(voField.getForeignEntity() == null)
					continue;
				if(voField.getForeignEntityField() == null)
					continue;
				final String sForeign = voField.getForeignEntity();
				final String sForeignField = voField.getForeignEntityField();
				if(sForeign.equals(this.model.getEntityName())) {
					if(sForeignField.indexOf(attr.getInternalName()) >= 0) {
						String sMessage = getMessage("wizard.step.inputattribute.11", "Die Entität " + vo.getEntity() + " verweist auf das Feld " + attr.getLabel() + "\n" +
							"Bitte entfernen Sie vorher das Feld dort!", vo.getEntity(), attr.getLabel());
						message.append(sMessage);
						return true;
					}

				}
			}
		}

		return blnRef;
	}


	class MyRowSorter extends TableRowSorter<EntityAttributeTableModel> {

		final EntityAttributeTableModel model;

		public MyRowSorter(TableModel model) {
			this.model = (EntityAttributeTableModel)model;
			this.setModelWrapper(new ModelWrapper<EntityAttributeTableModel, Integer>() {

				@Override
                public int getColumnCount() {
	                return MyRowSorter.this.model.getColumnCount();
                }

				@Override
                public Integer getIdentifier(int row) {
	                return row;
                }

				@Override
                public EntityAttributeTableModel getModel() {
	               return MyRowSorter.this.model;
                }

				@Override
                public int getRowCount() {
	               return MyRowSorter.this.model.getRowCount();
                }

				@Override
                public Object getValueAt(int row, int column) {
	                return MyRowSorter.this.model.getValueAt(row, column);
                }

			});

		}

		@Override
        public int getModelRowCount() {
	        return model.getRowCount();
        }

		@Override
		public void sort() {
			super.sort();
			sortModel();
		}

        @SuppressWarnings("unchecked")
        public void sortModel() {
	        List<Attribute> lstAttributes = model.getAttributes();
	        List<SortKey> lst = (List<javax.swing.RowSorter.SortKey>) this.getSortKeys();
	        for(SortKey key : lst) {
	        	final int column = key.getColumn();

        		Collections.sort(lstAttributes, new Comparator<Attribute>() {

					@Override
                    public int compare(Attribute o1, Attribute o2) {
                        switch(column) {
                        case 0:
	                        return o1.getLabel().compareTo(o2.getLabel());
                        case 1:
	                        return o1.getDescription().compareTo(o2.getDescription());
                        case 2:
	                        return o1.getDatatyp().getName().compareTo(o2.getDatatyp().getName());
                        case 3:
                        	Integer s1 = o1.getDatatyp().getScale() != null ? o1.getDatatyp().getScale() : new Integer(0);
                        	Integer s2 = o2.getDatatyp().getScale() != null ? o2.getDatatyp().getScale() : new Integer(0);
                        	return s1.compareTo(s2);
                        case 4:
                        	Integer i1 = o1.getDatatyp().getPrecision() != null ? o1.getDatatyp().getPrecision() : new Integer(0);
                        	Integer i2 = o2.getDatatyp().getPrecision() != null ? o2.getDatatyp().getPrecision() : new Integer(0);
	                        return i1.compareTo(i2);
                        case 5:
	                        return new Boolean(o1.isDistinct()).compareTo(new Boolean(o2.isDistinct()));
                        case 6:
	                        return new Boolean(o1.isLogBook()).compareTo(new Boolean(o2.isLogBook()));
                        case 7:
	                        return new Boolean(o1.isMandatory()).compareTo(new Boolean(o2.isMandatory()));
                        case 8:
	                        return o1.getInternalName().compareTo(o2.getInternalName());
                        case 9:
                        	String group1 = StringUtils.emptyIfNull(o1.getAttributeGroup());
                        	String group2 = StringUtils.emptyIfNull(o2.getAttributeGroup());
	                        return group1.compareTo(group2);
                        default:
	                        break;
                        }
                        return 0;
                    }


        		});

        		if(key.getSortOrder().equals(SortOrder.DESCENDING)){
	        		Collections.reverse(lstAttributes);
	        	}
	        }
	        if(lst.size() == 0) {
	        	Collections.sort(lstAttributes, new Comparator<Attribute>() {

					@Override
					public int compare(Attribute o1, Attribute o2) {
						Long l1 = o1.getInternalId() == null ? 0L : o1.getInternalId().longValue();
					    Long l2 = o2.getInternalId() == null ? 0L : o2.getInternalId().longValue();
						return l1.compareTo(l2);
					}

				});
	        }

	       }

	}

	class StringComparator implements Comparator<String> {

		@Override
        public int compare(String o1, String o2) {
	        o1 = org.nuclos.common2.StringUtils.emptyIfNull(o1);
	        o2 = org.nuclos.common2.StringUtils.emptyIfNull(o2);
			return o1.compareTo(o2);
        }
	}


	class BooleanComparator implements Comparator<Boolean> {

		@Override
        public int compare(Boolean o1, Boolean o2) {
	        return o1.compareTo(o2);
        }
	}

	class IntegerComparator implements Comparator<Integer> {

		@Override
        public int compare(Integer o1, Integer o2) {
	        return o1.compareTo(o2);
        }
	}

	class DataTypComparator implements Comparator<DataTyp> {

		@Override
        public int compare(DataTyp o1, DataTyp o2) {
	        return o1.getName().compareTo(o2.getName());
        }
	}

}
