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
package org.nuclos.client.ui.collect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.ListOfValues;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.FixedColumnRowHeader.FixedRowIndicatorTableModel;
import org.nuclos.client.ui.collect.SubForm.RefreshValueListAction;
import org.nuclos.client.ui.collect.SubForm.SubFormTable;
import org.nuclos.client.ui.collect.SubForm.SubFormTableModel;
import org.nuclos.client.ui.collect.component.CollectableCheckBox;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.CollectableDateChooser;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.gc.IReferenceHolder;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * SubFormFilter that handles the collapsible filter panels for the
 * fixed and external tables of a SubForm and filters the corresponding table data.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 01.00.00
 */
public class SubFormFilter implements Closeable, IReferenceHolder {

	private static final Logger LOG = Logger.getLogger(SubFormFilter.class);

	private JToggleButton filterButton;
	private JCheckBoxMenuItem miFilter;
	
	private SubFormParameterProvider parameterProvider;

	private SubForm subform;
	private SubFormFilterPanel fixedSubFormFilter;
	private SubFormFilterPanel externalSubFormFilter;
	private CollectableFieldsProviderFactory collectableFieldsProviderFactory;
	
	private SubFormTable externalTable;

	private boolean filteringActive = false;

	private boolean closed = false;
	
	private final List<Object> ref = new LinkedList<Object>();

   public SubFormFilter(SubForm subform, JTable fixedTable, TableColumnModel fixedColumnModel, SubFormTable externalTable, TableColumnModel externalColumnModel, 
		   JToggleButton filterButton, JCheckBoxMenuItem miFilter, CollectableFieldsProviderFactory collectableFieldsProviderFactory) {
      this.subform = subform;
      this.externalTable = externalTable;
      this.filterButton = filterButton;
      this.miFilter = miFilter;
      this.collectableFieldsProviderFactory = collectableFieldsProviderFactory;

      setFixedSubFormFilter(fixedTable, fixedColumnModel);
      setExternalSubFormFilter(externalTable, externalColumnModel);

      addActionListener();
   }
   
	@Override
	public void close() {
		// Close is needed for avoiding memory leaks
		// If you want to change something here, please consult me (tp).
		if (!closed) {
			LOG.debug("close(): " + this);
			subform = null;
			fixedSubFormFilter = null;
			externalTable = null;
			
			if (externalSubFormFilter != null) {
				externalSubFormFilter.close();
			}
			externalSubFormFilter = null;
			if (fixedSubFormFilter != null) {
				fixedSubFormFilter.close();
			}
			fixedSubFormFilter = null;
			
			ref.clear();
			closed = true;
		}
   }

   /**
    * actionlistener to collapse or expand the searchfilter panels
    */
   private void addActionListener() {
	   ListenerUtil.registerSubFormToolListener(subform, this, new SubForm.SubFormToolListener() {
		@Override
		public void toolbarAction(String actionCommand) {
			if(SubForm.ToolbarFunction.fromCommandString(actionCommand) == SubForm.ToolbarFunction.FILTER) {
				// collapse removes the filter, expanding filters with the (maybe set) entered filters
	            if (!fixedSubFormFilter.isCollapsed() || !externalSubFormFilter.isCollapsed()) {
	               //NUCLEUSINT-789 f
	               clearFilter();
	            } else {
	               filter();
	            }

	            fixedSubFormFilter.setCollapsed(!fixedSubFormFilter.isCollapsed());
	            externalSubFormFilter.setCollapsed(!externalSubFormFilter.isCollapsed());
			}
		}
	});
   }

	@Override
	public void addRef(EventListener o) {
		ref.add(o);
	}
	
	public void setSubFormParameterProvider(SubFormParameterProvider parameterProvider) {
		this.parameterProvider = parameterProvider;
	}
	
   /**
    * create collectablecomponents as search components and assign them to the corresponding column
    */
   private Map<String, CollectableComponent> getSearchFilterComponents() {
	   Map<String, CollectableComponent> column2component = new HashMap<String, CollectableComponent>();

   	   SubFormTableModel tableModel = (SubFormTableModel)externalTable.getModel();

       for (int index=0 ; index < tableModel.getColumnCount(); index++) {
         final CollectableEntityField cef = tableModel.getCollectableEntityField(index);
         final String columnName = tableModel.getColumnFieldName(index);
         final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(cef, subform.getCollectableComponentType(cef.getName(), true), true);

         if (clctcomp instanceof LabeledCollectableComponentWithVLP) {
         	// handle valuelistprovider
        	final LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP)clctcomp;
            CollectableFieldsProvider valuelistprovider = subform.getValueListProvider(columnName);
            if (valuelistprovider == null && cef.isReferencing()) {
               valuelistprovider = collectableFieldsProviderFactory.newDefaultCollectableFieldsProvider(subform.getEntityName(), clctWithVLP.getFieldName());
            }
            clctWithVLP.setValueListProvider(valuelistprovider);
            if (!subform.isLayout())
            	clctWithVLP.refreshValueList(true);

            final FocusAdapter refreshVLPAdapter = new FocusAdapter() {
            	@Override
            	public void focusGained(FocusEvent e) {
    				final Collection<RefreshValueListAction> collRefreshValueListActions = subform.getRefreshValueListActions(clctWithVLP.getFieldName());
    				if (!collRefreshValueListActions.isEmpty()) {
	    				// set the value list provider (dynamically):
	    				CollectableFieldsProvider valuelistprovider = subform.getValueListProvider(clctWithVLP.getFieldName());
	    				if (valuelistprovider != null) {
		    				clctWithVLP.setValueListProvider(valuelistprovider);
		
		    				// set parameters:
		    				for (RefreshValueListAction rvlact : collRefreshValueListActions) {
		    					SubForm.setParameterForRefreshValueListAction(rvlact, -1, clctWithVLP, (SubFormTableModel)subform.getSubformTable().getModel(), parameterProvider);					
		    				}

		    	            JTextComponent compText = null;
		    	            JComponent comp = clctcomp.getControlComponent();
		    	            if (comp instanceof ListOfValues) {
		    	            	compText = ((ListOfValues)comp).getJTextField();
		    	            } else if (comp instanceof JComboBox) {
		    	            	compText = (JTextComponent)((JComboBox)comp).getEditor().getEditorComponent();
		    	            }   

		    	            // remember old value here. 
		    				String clctfValue = compText.getText();
		    				// refresh value list:
							clctWithVLP.refreshValueList(false);
		    				compText.setText(clctfValue);
	    				}
    				}
            	}
			};
            JComponent comp = clctcomp.getControlComponent();
            if (comp instanceof ListOfValues) {
            	((ListOfValues)comp).getJTextField().addFocusListener(refreshVLPAdapter);
            } else if (comp instanceof JComboBox) {
            	for (Component c : ((JComboBox)comp).getComponents())
            	{
            		if (c instanceof JButton)
            			((JButton)c).addMouseListener(new MouseAdapter() {
            				@Override
            				public void mouseEntered(MouseEvent e) {
            					refreshVLPAdapter.focusGained(null);
            				}
						});
            	}
            	((JComboBox)comp).getEditor().getEditorComponent().addFocusListener(refreshVLPAdapter);
            }   
            
            // handle listener
            if (clctcomp instanceof CollectableComboBox) {
	            ((JComboBox)clctcomp.getControlComponent()).addItemListener(new ItemListener() {
	               //NUCLEUSINT-789 i
	               @Override
	               public void itemStateChanged(ItemEvent e) {
	            	   if (!closed) {
	            		   filter();
	            	   }
	               }
	            });
            }
            if (clctcomp instanceof CollectableListOfValues) {
            	((ListOfValues)clctcomp.getControlComponent()).getJTextField().addFocusListener(new FocusAdapter() {
	               //NUCLEUSINT-789 
	               @Override
	               public void focusLost(FocusEvent e) {
	                  saveSearchTerm(subform.getEntityName(), cef.getName(), clctcomp);
	                  if (!closed) {
	                	  filter();
	                  }
	               }
	            });
	            }
         }
         else if (clctcomp instanceof CollectableCheckBox) {
            ((JCheckBox)clctcomp.getControlComponent()).addChangeListener(new ChangeListener() {
               //NUCLEUSINT-789 i
               @Override
               public void stateChanged(ChangeEvent e) {
            	   if (!closed) {
            		   filter();
            	   }
               }
            });

            clctcomp.clear();
         }
         else if (clctcomp instanceof CollectableDateChooser) {
         	((CollectableDateChooser)clctcomp).getDateChooser().getJTextField().addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						if (!closed) {
							filter();
						}
					}
				});
         } else {
         	JComponent comp = clctcomp.getControlComponent();
            comp.addFocusListener(new FocusAdapter() {
               //NUCLEUSINT-789 
               @Override
               public void focusLost(FocusEvent e) {
                  saveSearchTerm(subform.getEntityName(), cef.getName(), clctcomp);
                  if (!closed) {
                	  filter();
                  }
               }
            });
         }

         if (clctcomp != null) {
            column2component.put(cef.getName()/*tableModel.getColumnFieldName(index)*/, clctcomp);
         }

         // autocompletion for text fields
         if (clctcomp instanceof CollectableTextField) {
         	CollectableTextField.addAutoComplete(cef, ((CollectableTextField)clctcomp).getJTextField(), getAutoCompletePreferences(cef.getCollectableEntity().getName()));
         }
      }
      return column2component;
   }

   // saves the search term for autocompletion functionality
   private void saveSearchTerm(String entityName, String fieldName, CollectableComponent clctcomp) {
      try {
      	if (clctcomp instanceof CollectableTextField) {
      		ArrayList<String> l = PreferencesUtils.getStringList(getAutoCompletePreferences(entityName), fieldName);

      		Object o = getFilterValue(clctcomp);

      		if(o != null) {
      			String s = o.toString();
      			if(l.contains(s))
      				l.remove(s);

      			l.add(0, s);
      		}
      		while(l.size() > 10)
      			l.remove(l.size()-1);

      		PreferencesUtils.putStringList(getAutoCompletePreferences(entityName), fieldName, l);

      		CollectableTextField.addAutoComplete(clctcomp.getEntityField(), ((CollectableTextField)clctcomp).getJTextField(), getAutoCompletePreferences(entityName));
      	}
      }
      catch (PreferencesException e) {
      	throw new CommonFatalException(e);
      }
   }

   private Preferences getAutoCompletePreferences(String entityName) {
      return ClientPreferences.getUserPreferences().node("collect").node("entity").node(entityName).node("subformfilter").node("autocompletion");
   }

   private Preferences getFilterPreferences(String entityName) {
      return ClientPreferences.getUserPreferences().node("collect").node("entity").node(entityName).node("subformfilter").node(subform.getEntityName());
   }

   private void setFixedSubFormFilter(JTable table, TableColumnModel columnModel) {
      this.fixedSubFormFilter = new SubFormFilterPanel(columnModel, getSearchFilterComponents(), true);
      setupResetFilterAction();
   }

   public SubFormFilterPanel getFixedSubFormFilter() {
      return this.fixedSubFormFilter;
   }

   private void setExternalSubFormFilter(JTable table, TableColumnModel columnModel) {
      this.externalSubFormFilter = new SubFormFilterPanel(columnModel, getSearchFilterComponents(), false);
   }

   public SubFormFilterPanel getExternalSubFormFilter() {
      return this.externalSubFormFilter;
   }

   /**
    * handles the action when the reset filter button was pressed
    */
   private void setupResetFilterAction() {
      this.fixedSubFormFilter.getResetFilterButton().addActionListener(new ActionListener() {
         @Override
		public void actionPerformed(ActionEvent ae) {
         	Map<String, CollectableComponent> columnFilters = getAllFilterComponents();

         	for (CollectableComponent comp : columnFilters.values()) {
         		if (comp != null) {
         			comp.clear();
         		}
         	}
         	getFixedSubFormFilter().repaint();
         	getExternalSubFormFilter().repaint();
         	filter();
         }
      });
   }

   public CollectableField getFilterValueForColumn(String columnName) {
      Map<String, CollectableComponent> columnFilters = externalSubFormFilter.getActiveFilterComponents();
      CollectableComponent clctcomp = columnFilters.get(columnName);
      if (clctcomp != null) {
         Object value = clctcomp.getModel().getField().getValue();
         Object valueid = null;
         try {
            valueid = clctcomp.getModel().getField().getValueId();
         } catch (UnsupportedOperationException e) {

         }
         if (valueid != null) {
            return new CollectableValueIdField(valueid, value);
         } else {
            return new CollectableValueField(value);
         }
      }

      return null;
   }

   /**
    * filters the tablemodel using the entered data in the filter components located above the columns
    */
   private void filter() {
	  // subformFilterPanel is invisible by default. @see NUCLOSINT-1630
	  fixedSubFormFilter.setVisible(true);
	  externalSubFormFilter.setVisible(true);
	  
	  Map<String, CollectableComponent> columnFilters = getAllFilterComponents();

      ArrayList<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel,Integer>>();
      SubFormTableModel subformtablemodel = externalTable.getSubFormModel();

      for (int index=0 ; index < subformtablemodel.getColumnCount(); index++) {
         CollectableEntityField cef = subformtablemodel.getCollectableEntityField(index);

         CollectableComponent clctcomp = columnFilters.get(cef.getName());

         try {
				if (clctcomp == null || clctcomp.getSearchCondition() == null) {//Field().isNull()) {
				   continue;
				}
			}
			catch(CollectableFieldFormatException e) {
				throw new CommonFatalException(e);
			}

         filters.add(new SubFormRowFilter(cef, clctcomp));
      }
      RowFilter<TableModel, Integer> filter = null;
      if (filters.size() > 0)
    	  filter = RowFilter.andFilter(filters);

      RowSorter<?> rowSorter = externalTable.getRowSorter();
      if (rowSorter instanceof TableRowSorter)
         ((TableRowSorter<?>) rowSorter).setRowFilter(filter);

      filteringActive = (filter != null);
   }

   // returns all CollectableComponents used in the filter panels
   public final Map<String, CollectableComponent> getAllFilterComponents() {
   	 // get all CollectableComponents used in the filter panels
      Map<String, CollectableComponent> columnFilters = fixedSubFormFilter.getActiveFilterComponents();
      columnFilters.putAll(externalSubFormFilter.getActiveFilterComponents());

      return columnFilters;
   }

   /**
    * Is filtering currently active?
    * @return true if filtering, false if not
    */
   public boolean isFilteringActive(){
      return filteringActive;
   }

   /**
    * Removes filtering
    * NUCLEUSINT-789 f
    */
	private void clearFilter() {
      Icon icon = Icons.getInstance().getIconFilter16();
      this.filterButton.setIcon(icon);
      this.miFilter.setIcon(icon);

      RowSorter<?> rowSorter = externalTable.getRowSorter();
      if (rowSorter instanceof TableRowSorter)
         ((TableRowSorter<?>) rowSorter).setRowFilter(null);

      filteringActive = false;
   }

   public void removeFiltering() {
	   // Why the heck is the filtering state handled so complicated?!?  There's a reason why Swing has
	   // Actions (with SELECTED_KEY) and Button/ItemModels etc...
	   clearFilter();
	   getFixedSubFormFilter().setCollapsed(true);
	   getExternalSubFormFilter().setCollapsed(true);
	   filterButton.setSelected(false);
	   miFilter.setSelected(false);
   }

   class SubFormRowFilter extends RowFilter<TableModel, Integer> {
      private CollectableEntityField cef;
      private CollectableComponent clctcomp;

      SubFormRowFilter(CollectableEntityField cef, CollectableComponent clctcomp) {
         this.clctcomp = clctcomp;
         this.cef = cef;
      }

      @Override
      public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
         TableModel model = entry.getModel();
         if (model instanceof FixedRowIndicatorTableModel) {
            model = ((FixedRowIndicatorTableModel) model).getExternalModel();
         }

         int colIndex = -1;
         if (model instanceof SubFormTableModel) {
            colIndex = ((SubFormTableModel) model).findColumnByFieldName(cef.getName());
         }
         if (colIndex == -1)
            return true;

         Object v = ((SubFormTableModel) model).getValueAt(entry.getIdentifier(), colIndex);
         if(!(v instanceof CollectableField))
            return true;

         CollectableField f = (CollectableField) v;

         Object o1 = f.getValue();
         // NUCLOSINT-823: If the field references an optional foreign key, it could be null.
         // In this case, we want to exclude it from the search, as a value for the foreign 
         // key was given in the search query. (Thomas Pasch)
         if (o1 == null)
        	 return false;

         final String cellValue = toFilterableString(o1).toLowerCase();

         final ComparisonOperator comparisonOperator;
         final String filterValue;

         try {
            CollectableSearchCondition cond = clctcomp.getSearchCondition();
            Object o = getFilterValue(clctcomp);

            // no search condition means no filtering, so return true
            if (cond == null || o == null) {
               return true;
            }

            if (cond instanceof AtomicCollectableSearchCondition) {
               comparisonOperator = ((AtomicCollectableSearchCondition)cond).getComparisonOperator();

               if (cond instanceof CollectableLikeCondition) {
                  filterValue = o.toString().toLowerCase();
               }
               else {
                  filterValue = toFilterableString(o).toLowerCase();
               }
            }
            else {
               return true;
            }

            switch (comparisonOperator) {
            case NONE:
               return true;
            case EQUAL:
               return this.isEqual(cellValue, filterValue);
            case LESS:
               return cellValue.compareTo(filterValue) < 0;
            case GREATER:
               return cellValue.compareTo(filterValue) > 0;
            case LESS_OR_EQUAL:
               return cellValue.compareTo(filterValue) <= 0;
            case GREATER_OR_EQUAL:
               return cellValue.compareTo(filterValue) >= 0;
            case NOT_EQUAL:
               return !this.isEqual(cellValue, filterValue);
            case LIKE:
               return cellValue.contains(filterValue.replace("*", ""));
            case NOT_LIKE:
               return !cellValue.contains(filterValue.replace("*", ""));
            case IS_NULL:
               return cellValue == null;
            case IS_NOT_NULL:
               return cellValue != null;
            default:
               return (cellValue.indexOf(filterValue) == -1 ? false : true); // do a like search per default this.isEqual(cellValue, filterValue);
            }
         }
          catch(CollectableFieldFormatException e) {
        	LOG.warn("include failed: " + e, e);
            JOptionPane.showConfirmDialog(null, SpringLocaleDelegate.getInstance().getMessage(
            		"subform.filter.exception", "Das Format der Suchkomponente '{0}' ist nicht korrekt.", cef.getLabel()), 
            		SpringLocaleDelegate.getInstance().getMessage("subform.filter.exception.title", "Formatfehler"), 
            		JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
            return true;
         }
      }



      /**
       * converts the given String into a comparable String which can be
       * interpreted by this RowFilter
       * @param value
       * @return a comparable string
       */
      private String toFilterableString(Object value) {
         if (value == null) {
            return null;
         }

         String result = "";

         if (cef.isReferencing() || cef.getJavaClass().equals(String.class)) {
            result = value.toString();
         }
         else if (cef.getJavaClass().equals(Date.class)) {
            result = String.format("%TF", value);
         }
         else if (cef.getJavaClass().equals(Integer.class)) {
            result = String.format("%0100d", value);
         }
         else if (cef.getJavaClass().equals(Double.class)) {
            result = String.format("%0111.010f", value);
         }
         else if (cef.getJavaClass().equals(Boolean.class)) {
            result = (value.equals(Boolean.TRUE)) ? "1" : "0";
         }
         else {
            result = value.toString();
         }

         return result;
      }

      /**
       * checks whether the given Strings are equal regarding NULL values
       * @param one
       * @param two
       * @return true of given Strings are equal, otherwise false
       */
      private boolean isEqual(String one, String two) {
         if (one == null && two == null) {
            return true;
         }
         else {
            if (one != null) {
               return one.equals(two);
            }
            else {
               return false;
            }
         }
      }
   }

   private Object getFilterValue(CollectableComponent clctcomp) {
   	try {
   		CollectableSearchCondition cond = clctcomp.getSearchCondition();

   		// no search condition means no filtering, so return null
   		if (cond == null) {
   			return null;
   		}

   		if (cond instanceof AtomicCollectableSearchCondition) {
   			if (cond instanceof CollectableLikeCondition) {
   				return ((CollectableLikeCondition)cond).getLikeComparand();
   			}
   			else {
   				return clctcomp.getField().getValue();
   			}
   		}
   	}
   	catch(CollectableFieldFormatException e) {
    	LOG.warn("getFilterValue failed: " + e, e);
   		JOptionPane.showConfirmDialog(null, SpringLocaleDelegate.getInstance().getMessage(
   			"subform.filter.exception", "Das Format der Suchkomponente '{0}' ist nicht korrekt.", clctcomp.getEntityField().getLabel()), 
   			SpringLocaleDelegate.getInstance().getMessage("subform.filter.exception.title", "Formatfehler"), 
   			JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
   	}
   	return null;
   }

   /**
    * stores the content of the table filter in the preferences
    * @param parentEntityName
    */
   public void storeTableFilter(String parentEntityName) {
   	try {
   		// 1. remove all filter entries
   		for (String key : getFilterPreferences(parentEntityName).keys()) {
   			getFilterPreferences(parentEntityName).remove(key);
   		}

   		// 2. store all filter entries
   		for (CollectableComponent comp : getAllFilterComponents().values()) {
   			try {
   				if (comp != null && !comp.getField().isNull()) {
   					PreferencesUtils.putSerializableObjectXML(getFilterPreferences(parentEntityName), comp.getFieldName(), comp.getField());
   				}
   			}
   			catch(CollectableFieldFormatException e) {
   				throw new CommonFatalException(e);
   			}
   			catch(PreferencesException e) {
   	        	LOG.info("storeTableFilter failed: " + e, e);
   			}
   		}
   	}
   	catch (BackingStoreException e) {
       	LOG.info("storeTableFilter failed: " + e, e);
   	}
   }

   /**
    * load the content of the table filter of the preferences
    * @param parentEntityName
    */
   public void loadTableFilter(String parentEntityName) {
   	Preferences prefs = getFilterPreferences(parentEntityName);

   	for (CollectableComponent comp : getAllFilterComponents().values()) {
   		try {
   			if (comp != null) {
   				CollectableField field = (CollectableField)PreferencesUtils.getSerializableObject(prefs, comp.getFieldName());
   				if (field != null) {
   					comp.setField(field);
   					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
		   					filterButton.doClick();
						}
					});
   				}
   			}
   		}
   		catch(PreferencesException e) {
	        LOG.info("loadTableFilter failed: " + e, e);
   		}
   	}
   }
   
   public void setupTableHeaderForScrollPane(JScrollPane scrollPane) {
	   if (UIUtils.findJComponent(scrollPane, "CornerPanel") == null) {
			// add subformfilter for fixed columns
			Component corner = scrollPane.getCorner(JScrollPane.UPPER_LEFT_CORNER );
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.setName("CornerPanel");
			panel2.add(corner, BorderLayout.CENTER);
			panel2.add(getFixedSubFormFilter(), BorderLayout.NORTH);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, panel2);
	   }
	   if (UIUtils.findJComponent(scrollPane, "ColumnHeader") == null) {
			// add subformfilter for external table
			Component origColumnHeader = scrollPane.getColumnHeader().getView();
			JPanel panel1 = new JPanel(new BorderLayout());
			panel1.setName("ColumnHeader");
			panel1.add(origColumnHeader, BorderLayout.CENTER);
			panel1.add(getExternalSubFormFilter(), BorderLayout.NORTH);
			scrollPane.setColumnHeaderView(panel1);
	   }
   }
}
