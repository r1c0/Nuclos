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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JTABBEDPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.TranslationPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.PropertiesSorter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.JInfoTabbedPane;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 *
 *
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIWYGTabbedPane extends DnDTabbedPane implements WYSIWYGComponent, WYSIWYGEditorModes {

   public static String PROPERTY_NAME = PROPERTY_LABELS.NAME;
   public static String PROPERTY_PREFFEREDSIZE = PROPERTY_LABELS.PREFFEREDSIZE;
   public static String PROPERTY_TABLAYOUTPOLICY = PROPERTY_LABELS.TABLAYOUTPOLICY;
   public static String PROPERTY_TABPLACEMENT = PROPERTY_LABELS.TABPLACEMENT;

   public Map<Integer, TranslationMap> mpTabTranslations = new HashMap<Integer, TranslationMap>();
   public Map<Integer, String> mpTabTitles = new HashMap<Integer, String>();

   public static final String[][] PROPERTIES_TO_LAYOUTML_ATTRIBUTES = new String[][]{
      {PROPERTY_NAME, ATTRIBUTE_NAME},
      {PROPERTY_TABPLACEMENT, ATTRIBUTE_TABPLACEMENT},
      {PROPERTY_TABLAYOUTPOLICY, ATTRIBUTE_TABLAYOUTPOLICY}
   };

   private static String[] PROPERTY_NAMES = new String[]{
      PROPERTY_NAME,
      PROPERTY_PREFFEREDSIZE,
      PROPERTY_TABPLACEMENT,
      PROPERTY_TABLAYOUTPOLICY,
      PROPERTY_BORDER
   };

   private static PropertyClass[] PROPERTY_CLASSES = new PropertyClass[]{
      new PropertyClass(PROPERTY_NAME, String.class),
      new PropertyClass(PROPERTY_PREFFEREDSIZE, Dimension.class),
      new PropertyClass(PROPERTY_TABPLACEMENT, String.class),
      new PropertyClass(PROPERTY_TABLAYOUTPOLICY, String.class),
      new PropertyClass(PROPERTY_BORDER, Border.class)
   };

   private static PropertySetMethod[] PROPERTY_SETMETHODS = new PropertySetMethod[]{
      new PropertySetMethod(PROPERTY_NAME, "setName"),
      new PropertySetMethod(PROPERTY_PREFFEREDSIZE, "setPreferredSize"),
      new PropertySetMethod(PROPERTY_TABLAYOUTPOLICY, "setTabLayoutPolicy"),
      new PropertySetMethod(PROPERTY_BORDER, "setBorder")
   };

   private static PropertyFilter[] PROPERTY_FILTERS = new PropertyFilter[] {
      new PropertyFilter(PROPERTY_NAME, STANDARD_MODE | EXPERT_MODE),
      new PropertyFilter(PROPERTY_PREFFEREDSIZE, STANDARD_MODE | EXPERT_MODE),
      new PropertyFilter(PROPERTY_TABPLACEMENT, STANDARD_MODE | EXPERT_MODE),
      new PropertyFilter(PROPERTY_TABLAYOUTPOLICY, STANDARD_MODE | EXPERT_MODE),
      new PropertyFilter(PROPERTY_BORDER, STANDARD_MODE | EXPERT_MODE)
   };

   public static final String[][] PROTERTY_VALUES_STATIC = new String[][] {
      {PROPERTY_TABPLACEMENT, ATTRIBUTEVALUE_TOP, ATTRIBUTEVALUE_BOTTOM, ATTRIBUTEVALUE_LEFT, ATTRIBUTEVALUE_RIGHT},
      // wrong value caused nullpointer exception NUCLEUSINT-453
      {PROPERTY_TABLAYOUTPOLICY, JTABBEDPANE.SCROLL_TAB_LAYOUT, JTABBEDPANE.WRAP_TAB_LAYOUT}
      };

   private ComponentProperties properties;

   private WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor;

   private final Map<String, Integer> mpTabLayoutPolicies = new HashMap<String, Integer>(2);
   private final Map<String, Integer> mpTabPlacementConstants = new HashMap<String, Integer>(4);
   public WYSIWYGTabbedPane() {
      // tab layoutml policies:
      this.mpTabLayoutPolicies.put(JTABBEDPANE.SCROLL_TAB_LAYOUT, JTabbedPane.SCROLL_TAB_LAYOUT);
      this.mpTabLayoutPolicies.put(JTABBEDPANE.WRAP_TAB_LAYOUT, JTabbedPane.WRAP_TAB_LAYOUT);
      //NUCLEUSINT-453
      this.mpTabLayoutPolicies.put(ATTRIBUTEVALUE_SCROLL, JTabbedPane.SCROLL_TAB_LAYOUT);
      this.mpTabLayoutPolicies.put(ATTRIBUTEVALUE_WRAP, JTabbedPane.WRAP_TAB_LAYOUT);

      // tab placement constants:
      this.mpTabPlacementConstants.put(ATTRIBUTEVALUE_TOP, JTabbedPane.TOP);
      this.mpTabPlacementConstants.put(ATTRIBUTEVALUE_BOTTOM, JTabbedPane.BOTTOM);
      this.mpTabPlacementConstants.put(ATTRIBUTEVALUE_LEFT, JTabbedPane.LEFT);
      this.mpTabPlacementConstants.put(ATTRIBUTEVALUE_RIGHT, JTabbedPane.RIGHT);

      // rename Tab with doubleclick
      super.addMouseListener(new MouseAdapter() {
			@Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    renameTab();
                }
            }
        });

		//DropTarget dropTarget = new DropTarget(this, this);
		//dropTarget.setActive(true);
		//dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
      
        DragGestureListener dgListener = new DragGestureListener() {
    	   	     public void dragGestureRecognized(DragGestureEvent e) {
    	   	    	Object o = e.getComponent();
    	   	 		if (o instanceof WYSIWYGComponent) {
    	   	 			WYSIWYGComponent item = (WYSIWYGComponent) o;
    	   	 			
    	   	 			if (item == WYSIWYGTabbedPane.this) {
    	   	 				WYSIWYGTabbedPane tabbedPane = WYSIWYGTabbedPane.this;
    	   	 				final int index = tabbedPane.indexAtLocation(e.getDragOrigin().x, e.getDragOrigin().y);
    	   	 				if (index == -1) {
    	    	   	 			final WYSIWYGLayoutEditorPanel parent = getParentEditor();
    	    	   	 			if (parent != null && !parent.getTableLayoutPanel().isResizeDragPerformed()) {
    	    	   	 				parent.setComponentToMove(item);
    	    	   	 				try {
	    		    	 			    // NUCLEUSINT-496
	    		    	 				e.startDrag(null, new TransferableComponent(item), new DragSourceListener() {
	    									@Override
	    									public void dropActionChanged(DragSourceDragEvent dsde) {
	    									}
	    									@Override
	    									public void dragOver(DragSourceDragEvent dsde) {
	    									}
	    									@Override
	    									public void dragExit(DragSourceEvent dse) {
	    									}
	    									@Override
	    									public void dragEnter(DragSourceDragEvent dsde) {
	    									}
	    									@Override
	    									public void dragDropEnd(DragSourceDropEvent dsde) {
	    					   	 				parent.getTableLayoutPanel().hideGlassPane();
	    									}
	    								});
	    		    	 			} catch (InvalidDnDOperationException ex) {
	    		    	 				// do nothing.
	    		    	 			}
    	    	   	 			}
    	   	 				}
    	   	 			}
    	   	 		}
    	   	     } 
    	   	  };

    	   // component, action, listener
    	 DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
    	     this, DnDConstants.ACTION_COPY_OR_MOVE, dgListener );
   }

   /**
    * Externalized Method for renaming Tabs
    * Triggered by Contextual Menu and with Doubleclick on Tab.
    */
   private void renameTab() {
        int iSelected = getSelectedIndex();

        final JTextField titleTextField = new JTextField(getTitleAt(iSelected));
        final TranslationMap translations = new TranslationMap();
        if (getTabTranslations().get(iSelected) != null) {
        	// Make a copy
        	translations.putAll(getTabTranslations().get(iSelected));
        }

        Box box = Box.createHorizontalBox();
        box.add(new JLabel(PROPERTY_LABELS.TRANSLATIONS));
        box.add(Box.createHorizontalStrut(InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS));
        box.add(new JButton(new AbstractAction(BUTTON_LABELS.LABEL_EDIT) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String, String> res = TranslationPanel.showDialog(
					WYSIWYGTabbedPane.this, translations, titleTextField.getText());
				if (res != null) {
					translations.merge(res);
				}
			}
        }));
        int opt = JOptionPane.showConfirmDialog(
        	WYSIWYGTabbedPane.this,
        	new Object[]{ JTABBEDPANE.INPUTDIALOG_RENAME_ACTION_TITLE, titleTextField, box },
        	JTABBEDPANE.INPUTDIALOG_RENAME_ACTION_BUTTON,
        	JOptionPane.OK_CANCEL_OPTION,
        	JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
        	setTitleAt(iSelected, titleTextField.getText().trim());
        	getTabTranslations().put(iSelected, translations);
        	getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
        }
   }

   /**
    * Externalized Method for renaming Tabs
    * Triggered by Contextual Menu and with Doubleclick on Tab.
    */
   private void setMnemonicForTab() {
        int iSelected = getSelectedIndex();

        JTextField titleTextField;
        String s = String.valueOf((char)getMnemonicAt(iSelected));
        if(!(s != null && s.length() > 0)) {
        	titleTextField = new JTextField();
        }
        else {
        	int keycode = getMnemonicAt(iSelected);
        	keycode += 32;
        	byte b[] = {(byte)keycode};
			String sKey = new String(b);
        	titleTextField = new JTextField(sKey);
        }

        int opt = JOptionPane.showConfirmDialog(
        	WYSIWYGTabbedPane.this,
        	new Object[]{ JTABBEDPANE.INPUTDIALOG_SETMNEMONIC_ACTION, titleTextField},
        	JTABBEDPANE.INPUTDIALOG_SETMNEMONIC_ACTION_TEXT,
        	JOptionPane.OK_CANCEL_OPTION,
        	JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
        	int keycode = titleTextField.getText().trim().charAt(0);
        	if(keycode > 90)
				keycode -= 32;
        	KeyStroke stroke = KeyStroke.getKeyStroke(keycode, InputEvent.ALT_MASK);
        	if(stroke.getKeyCode() > 0)
        		setMnemonicAt(iSelected, stroke.getKeyCode());
        	getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
        }
   }

   private WYSIWYGLayoutEditorChangeDescriptor getWYSIWYGLayoutEditorChangeDescriptor() {
      return this.wysiwygLayoutEditorChangeDescriptor;
   }

   public void setWYSIWYGLayoutEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor wysiwygLayoutEditorChangeDescriptor) {
      this.wysiwygLayoutEditorChangeDescriptor = wysiwygLayoutEditorChangeDescriptor;
   }

   @Override
   public void addTab(String title, Component component) {
      super.addTab(title, component);
      ((WYSIWYGLayoutEditorPanel) component).setWYSIWYGLayoutEditorChangeDescriptor(this.getWYSIWYGLayoutEditorChangeDescriptor());
   }

   @Override
   public void addTab(String title, Icon icon, Component component, String tip) {
      super.addTab(title, icon, component, tip);
      ((WYSIWYGLayoutEditorPanel) component).setWYSIWYGLayoutEditorChangeDescriptor(this.getWYSIWYGLayoutEditorChangeDescriptor());
   }

   @Override
   public void addTab(String title, Icon icon, Component component) {
      super.addTab(title, icon, component);
      ((WYSIWYGLayoutEditorPanel) component).setWYSIWYGLayoutEditorChangeDescriptor(this.getWYSIWYGLayoutEditorChangeDescriptor());
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getProperties()
    */
   @Override
public ComponentProperties getProperties() {
      return properties;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperties(org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties)
    */
   @Override
public void setProperties(ComponentProperties properties) {
      this.properties = properties;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#setProperty(java.lang.String, org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue, java.lang.Class)
    */
   @Override
   public void setProperty(String property, PropertyValue<?> value, Class<?> valueClass) throws CommonBusinessException {
      properties.setProperty(property, value, valueClass);
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyNames()
    */
   @Override
public String[] getPropertyNames() {
      return PropertiesSorter.sortPropertyNames(PROPERTY_NAMES);
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertySetMethods()
    */
   @Override
public PropertySetMethod[] getPropertySetMethods() {
      return PROPERTY_SETMETHODS;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyClasses()
    */
   @Override
public PropertyClass[] getPropertyClasses() {
      return PROPERTY_CLASSES;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getParentEditor()
    */
   @Override
public WYSIWYGLayoutEditorPanel getParentEditor() {
      if (getScrollPane() != null) {
         return getScrollPane().getParentEditor();
      } else {
         if (super.getParent() instanceof TableLayoutPanel) {
            return (WYSIWYGLayoutEditorPanel) super.getParent().getParent();
         }
      }

      throw new CommonFatalException(ERROR_MESSAGES.PARENT_NO_WYSIWYG);
   }

   /**
    *
    * @return If Editor is placed in a WYSIWYGScrollPane the WYSIWYGScrollPane
    *         would be returned. Otherwise return null.
    */
   public WYSIWYGScrollPane getScrollPane() {
      if (super.getParent() instanceof JViewport) {
         if (super.getParent().getParent() instanceof WYSIWYGScrollPane) {
            return (WYSIWYGScrollPane) super.getParent().getParent();
         }
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyAttributeLink()
    */
   @Override
public String[][] getPropertyAttributeLink() {
      return PROPERTIES_TO_LAYOUTML_ATTRIBUTES;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getAdditionalContextMenuItems(int)
    */
   @Override
public List<JMenuItem> getAdditionalContextMenuItems(int xClick) {
      List<JMenuItem> list = new ArrayList<JMenuItem>();

      JMenuItem miAddTab = new JMenuItem(JTABBEDPANE.MENUITEM_ADD_TAB);
      miAddTab.addActionListener(new ActionListener() {

         @Override
        public void actionPerformed(ActionEvent e) {
            WYSIWYGLayoutEditorPanel editor = new WYSIWYGLayoutEditorPanel(getParentEditor().getMetaInformation());
            editor.getTableLayoutUtil().createStandardLayout();
            //NUCLEUSINT-484
            String title = JOptionPane.showInputDialog(JTABBEDPANE.INPUTDIALOG_RENAME_ACTION_TITLE);
            if (title == null)
                title = JTABBEDPANE.DEFAULT_TABNAME + (getTabCount() + 1);
            addTab(title, editor);
            getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
         }

      });
      list.add(miAddTab);

      JMenuItem miRenameTab = new JMenuItem(JTABBEDPANE.MENUITEM_RENAME_TAB);
      miRenameTab.addActionListener(new ActionListener() {

         @Override
        public void actionPerformed(ActionEvent e) {
           renameTab();
         }

      });
      list.add(miRenameTab);

      JMenuItem miSetMnemonic = new JMenuItem(JTABBEDPANE.MENUITEM_SET_MNEMONIC);
      miSetMnemonic.addActionListener(new ActionListener() {

         @Override
        public void actionPerformed(ActionEvent e) {
           setMnemonicForTab();
         }

      });
      list.add(miSetMnemonic);


      JMenuItem miRemoveTab = new JMenuItem(JTABBEDPANE.MENUITEM_REMOVE_TAB);
      miRemoveTab.addActionListener(new ActionListener() {

         @Override
        public void actionPerformed(ActionEvent e) {
            int iSelected = getSelectedIndex();

            String[] tabList = new String[getTabCount()];
            for (int i = 0; i < getTabCount(); i++) {
               tabList[i] = (i + 1) + ":" + getTitleAt(i);
            }

            String rawInput = (String) JOptionPane.showInputDialog(WYSIWYGTabbedPane.this, JTABBEDPANE.INPUTDIALOG_REMOVE_ACTION_TITLE, JTABBEDPANE.INPUTDIALOG_REMOVE_ACTION_BUTTON, JOptionPane.PLAIN_MESSAGE, null, tabList, tabList[iSelected]);
            if (rawInput != null) {

               String[] splittedInput = rawInput.split(":");
               if (splittedInput.length >= 2) {
                  int index = 0;
                  try {
                     index = new Integer(splittedInput[0]).intValue() - 1;
                     removeTabAt(index);
                     getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
                     mpTabTranslations.remove(index);
                     mpTabTitles.remove(index);
                    for (int i = index + 1; i < getTabCount() + 1; i++) {
                        mpTabTranslations.put(i - 1, mpTabTranslations.remove(i));
                        mpTabTitles.put(i-1, mpTabTitles.remove(i));
                    }
                  } catch (Exception ex) {
                     JOptionPane.showMessageDialog(WYSIWYGTabbedPane.this, JTABBEDPANE.ERRORMESSAGE_VALIDATION_REMOVE);
                  }
               }
            }
         }

      });
      list.add(miRemoveTab);

      JMenuItem miOrderTab = new JMenuItem(JTABBEDPANE.MENUITEM_ORDER_TABS);
      miOrderTab.addActionListener(new ActionListener() {

         @Override
        public void actionPerformed(ActionEvent e) {
            int iSelected = getSelectedIndex();

            String[] tabList = new String[getTabCount()];
            for (int i = 0; i < getTabCount(); i++) {
               tabList[i] = (i + 1) + ":" + getTitleAt(i);
            }

            String movingTab = (String) JOptionPane.showInputDialog(WYSIWYGTabbedPane.this, JTABBEDPANE.INPUTDIALOG_MOVE_ACTION_TITLE, JTABBEDPANE.INPUTDIALOG_MOVE_ACTION_BUTTON, JOptionPane.PLAIN_MESSAGE, null, tabList, tabList[iSelected]);
            if (movingTab != null) {

               String[] splittedInput = movingTab.split(":");
               if (splittedInput.length >= 2) {
                  int moveIndex = 0;
                  try {
                     moveIndex = new Integer(splittedInput[0]).intValue() - 1;
                     TranslationMap translationMapAtMoveIndex = mpTabTranslations.remove(moveIndex);
                     String titleAtMoveIndex = mpTabTitles.remove(moveIndex);

                     String placeBeforeTab = (String) JOptionPane.showInputDialog(WYSIWYGTabbedPane.this, WYSIWYGStringsAndLabels.partedString(JTABBEDPANE.INPUTDIALOG_INSERT_MOVED_TAB_ACTION_TITLE, getTitleAt(moveIndex)), JTABBEDPANE.INPUTDIALOG_INSERT_MOVED_TAB_ACTION_BUTTON, JOptionPane.PLAIN_MESSAGE, null, tabList, null);

                     if (placeBeforeTab != null) {

                        String[] splittedPlaceBeforeTab = placeBeforeTab.split(":");
                        if (splittedPlaceBeforeTab.length >= 2) {
                           int insertIndex = 0;
                           insertIndex = new Integer(splittedPlaceBeforeTab[0]).intValue() - 1;
                           if (moveIndex < insertIndex) {
                            for (int i = moveIndex + 1; i < insertIndex; i++) {
                                mpTabTranslations.put(i - 1, mpTabTranslations.remove(i));
                                mpTabTitles.put(i - 1, mpTabTitles.remove(i));
                            }
                            mpTabTranslations.put(insertIndex - 1, translationMapAtMoveIndex);
                            mpTabTitles.put(insertIndex - 1, titleAtMoveIndex);
                           } else {
                            for (int i = moveIndex - 1; i >= insertIndex; i-- ) {
                                mpTabTranslations.put(i + 1, mpTabTranslations.remove(i));
                                mpTabTitles.put(i + 1, mpTabTitles.remove(i));
                            }
                            mpTabTranslations.put(insertIndex, translationMapAtMoveIndex);
                            mpTabTitles.put(insertIndex, titleAtMoveIndex);
                           }
                           insertTab(getTitleAt(moveIndex), getIconAt(moveIndex), getComponentAt(moveIndex), getToolTipTextAt(moveIndex), insertIndex);
                           getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
                        }
                     }
                  } catch (Exception ex) {
                     JOptionPane.showMessageDialog(WYSIWYGTabbedPane.this, JTABBEDPANE.ERRORMESSAGE_VALIDATION_MOVE);
                  }
               }
            }
         }

      });
      list.add(miOrderTab);

      JMenu enableMenu = new JMenu(JTABBEDPANE.MENUITEM_ENABLE_OR_DISABLE_TABS);
      for (int i = 0; i < getTabCount(); i++) {
    	 JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(getTitleAt(i), isEnabledAt(i));
         checkBoxMenuItem.addItemListener(new EnableContextMenuListener(checkBoxMenuItem, i));
         enableMenu.add(checkBoxMenuItem);
      }
      list.add(enableMenu);

      return list;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesFromMetaInformation()
    */
   @Override
public String[][] getPropertyValuesFromMetaInformation() {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyValuesStatic()
    */
   @Override
public String[][] getPropertyValuesStatic() {
      return PROTERTY_VALUES_STATIC;
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getLayoutMLRulesIfCapable()
    */
   @Override
public LayoutMLRules getLayoutMLRulesIfCapable() {
      return null;
   }

   private class EnableContextMenuListener implements ItemListener {

      private final JCheckBoxMenuItem item;
      private final int index;

      public EnableContextMenuListener(JCheckBoxMenuItem item, int index) {
         this.item = item;
         this.index = index;
      }

      @Override
    public void itemStateChanged(ItemEvent e) {
         setEnabledAt(index, (item.getSelectedObjects()!=null));
         getWYSIWYGLayoutEditorChangeDescriptor().setContentChanged();
      }
   }

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#validateProperties(java.util.Map)
    */
   @Override
public void validateProperties(Map<String, PropertyValue<Object>> values) throws NuclosBusinessException {};

   /*
    * (non-Javadoc)
    * @see org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent#getPropertyFilters()
    */
   @Override
public PropertyFilter[] getPropertyFilters() {
      return PROPERTY_FILTERS;
   }

   public void setTabLayoutPolicy(String value) {
      if (value != null) {
         this.setTabLayoutPolicy(this.mpTabLayoutPolicies.get(value));
      }
      else {
         this.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
      }
   }

   public void setTabPlacement(String value) {
      if (value != null) {
         this.setTabPlacement(this.mpTabPlacementConstants.get(value));
      }
      else {
         this.setTabPlacement(JTabbedPane.TOP);
      }
   }

   public Map<Integer, TranslationMap> getTabTranslations() {
      return this.mpTabTranslations;
   }

   public Map<Integer, String> getTabTitles() {
	  return this.mpTabTitles;
   }

}

class DnDTabbedPane extends JInfoTabbedPane {
    private static final int LINEWIDTH = 3;
    private static final String NAME = "DnDTabbedPane";
    private final GhostGlassPane glassPane = new GhostGlassPane();
    private final Rectangle lineRect  = new Rectangle();
    private final Color     lineColor = new Color(0, 100, 255);
    private int dragTabIndex = -1;

    private void clickArrowButton(String actionKey) {
        ActionMap map = getActionMap();
        if(map != null) {
            Action action = map.get(actionKey);
            if (action != null && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
            }
        }
    }
    
    private static Rectangle rBackward = new Rectangle();
    private static Rectangle rForward  = new Rectangle();
    private static int rwh = 20;
    private static int buttonsize = 30; //scroll button size
    
    private void autoScroll(Point glassPt) {
        Rectangle r = getTabAreaBounds();
        int tabPlacement = getTabPlacement();
        if(tabPlacement==TOP || tabPlacement==BOTTOM) {
            rBackward.setBounds(r.x, r.y, rwh, r.height);
            rForward.setBounds(r.x+r.width-rwh-buttonsize, r.y, rwh+buttonsize, r.height);
        }else if(tabPlacement==LEFT || tabPlacement==RIGHT) {
            rBackward.setBounds(r.x, r.y, r.width, rwh);
            rForward.setBounds(r.x, r.y+r.height-rwh-buttonsize, r.width, rwh+buttonsize);
        }
        rBackward = SwingUtilities.convertRectangle(getParent(), rBackward, glassPane);
        rForward  = SwingUtilities.convertRectangle(getParent(), rForward,  glassPane);
        if(rBackward.contains(glassPt)) {
            clickArrowButton("scrollTabsBackwardAction");
        } else if(rForward.contains(glassPt)) {
            clickArrowButton("scrollTabsForwardAction");
        }
    }
    
    public DnDTabbedPane() {
        super();
        
        final DragSourceListener dragSourceListener = new DragSourceListener() {
            @Override 
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }
            @Override
            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                lineRect.setRect(0,0,0,0);
                glassPane.setPoint(new Point(-1000,-1000));
                glassPane.repaint();
            }
            @Override 
            public void dragOver(DragSourceDragEvent e) {
                Point glassPt = e.getLocation();
                SwingUtilities.convertPointFromScreen(glassPt, glassPane);
                
                int targetIdx = getTargetTabIndex(glassPt);
                if(targetIdx>=0 && targetIdx!=dragTabIndex && targetIdx!=dragTabIndex+1) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                    glassPane.setCursor(DragSource.DefaultMoveDrop);
                } else {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                    glassPane.setCursor(DragSource.DefaultMoveNoDrop);
                }
            }
            @Override 
            public void dragDropEnd(DragSourceDropEvent e) {
                lineRect.setRect(0,0,0,0);
                dragTabIndex = -1;
                glassPane.setVisible(false);
                glassPane.setImage(null);
                
                
            }
            @Override 
            public void dropActionChanged(DragSourceDragEvent e) {
            	
            }
        };
        
        final Transferable transferable = new Transferable() {
            private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
            @Override 
            public Object getTransferData(DataFlavor flavor) {
                return DnDTabbedPane.this;
            }
            @Override 
            public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] f = new DataFlavor[1];
                f[0] = this.FLAVOR;
                return f;
            }
            @Override 
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.getHumanPresentableName().equals(NAME);
            }
        };
        
        final DragGestureListener dragGestureListener = new DragGestureListener() {
            @Override 
            public void dragGestureRecognized(DragGestureEvent e) {
                if(getTabCount()<=1) 
                	return;
                
                Point tabPt = e.getDragOrigin();
                dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
                
                if(dragTabIndex<0 || !isEnabledAt(dragTabIndex))
                	return;
                
                initGlassPane(e.getComponent(), e.getDragOrigin());
                
                try {
                    e.startDrag(DragSource.DefaultMoveDrop, transferable, dragSourceListener);
                } catch (InvalidDnDOperationException ex) {
                    // do nothing-
                }
            }
        };
        
        new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new TabbedPaneDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
    }

    class TabbedPaneDropTargetListener implements DropTargetListener{
        @Override 
        public void dragEnter(DropTargetDragEvent e) {
            if(isDragAllowed(e))
            	e.acceptDrag(e.getDropAction());
            else
            	e.rejectDrag();
        }
        @Override 
        public void dragExit(DropTargetEvent e) {
        	
        }
        @Override 
        public void dropActionChanged(DropTargetDragEvent e) {
        	
        }

        private Point _glassPt = new Point();
        @Override 
        public void dragOver(final DropTargetDragEvent e) {
            Point glassPt = e.getLocation();
            if (getTabPlacement()==JTabbedPane.TOP || getTabPlacement()==JTabbedPane.BOTTOM) {
                initTargetLeftRightLine(getTargetTabIndex(glassPt));
            } else {
                initTargetTopBottomLine(getTargetTabIndex(glassPt));
            }
            glassPane.setPoint(glassPt);
           
            if(!_glassPt.equals(glassPt))
            	glassPane.repaint();
            _glassPt = glassPt;
            autoScroll(glassPt);
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (isDropAllowed(e)) {
                convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
                e.dropComplete(true);
            } else{
                e.dropComplete(false);
            }
            repaint();
        }
        
        private boolean isDragAllowed(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if(t==null) return false;
            DataFlavor[] f = e.getCurrentDataFlavors();
            if(t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
                if(dragTabIndex>=0) {
                	return true;
                }
            }
            return false;
        }
        
        private boolean isDropAllowed(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if(t==null) return false;
            DataFlavor[] f = t.getTransferDataFlavors();
            if(dragTabIndex>=0) {
            	return true;
            }
            return false;
        }
    }

    private int getTargetTabIndex(Point glassPt) {
        Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, DnDTabbedPane.this);
        boolean isTB = getTabPlacement()==JTabbedPane.TOP || getTabPlacement()==JTabbedPane.BOTTOM;
        for (int i=0;i<getTabCount();i++) {
            Rectangle r = getBoundsAt(i);
            if (isTB)
            	r.setRect(r.x-r.width/2, r.y,  r.width, r.height);
            else
            	r.setRect(r.x, r.y-r.height/2, r.width, r.height);
            
            if (r.contains(tabPt))
            	return i;
        }
        
        Rectangle r = getBoundsAt(getTabCount()-1);
        if (isTB)
        	r.setRect(r.x+r.width/2, r.y,  r.width, r.height);
        else
        	r.setRect(r.x, r.y+r.height/2, r.width, r.height);
        
        return r.contains(tabPt) ? getTabCount() :-1;
    }
    
    private void convertTab(int prev, int next) {
        if (next<0 || prev==next) {
            return;
        }

        Component cmp = getComponentAt(prev);
        Component tab = getTabComponentAt(prev);
        String str    = getTitleAt(prev);
        Icon icon     = getIconAt(prev);
        String tip    = getToolTipTextAt(prev);
        boolean flg   = isEnabledAt(prev);
        int tgtindex  = prev>next ? next : next-1;
        
        remove(prev);
        insertTab(str, icon, cmp, tip, tgtindex);
        
        setEnabledAt(tgtindex, flg);

        if (flg)
        	setSelectedIndex(tgtindex);

        setTabComponentAt(tgtindex, tab);
    }
    
    private void initTargetLeftRightLine(int next) {
        if (next<0 || dragTabIndex==next || next-dragTabIndex==1) {
            lineRect.setRect(0,0,0,0);
        } else if (next==0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            lineRect.setRect(r.x-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
        } else { 
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
            lineRect.setRect(r.x+r.width-LINEWIDTH/2,r.y,LINEWIDTH,r.height);
        }
    }
    
    private void initTargetTopBottomLine(int next) {
        if (next<0 || dragTabIndex==next || next-dragTabIndex==1) {
            lineRect.setRect(0,0,0,0);
        } else if(next==0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            lineRect.setRect(r.x,r.y-LINEWIDTH/2,r.width,LINEWIDTH);
        } else {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
            lineRect.setRect(r.x,r.y+r.height-LINEWIDTH/2,r.width,LINEWIDTH);
        }
    }

    private void initGlassPane(Component c, Point tabPt) {
        getRootPane().setGlassPane(glassPane);

        //paint ghost
        Rectangle rect = getBoundsAt(dragTabIndex);
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        c.paint(g);
        
        rect.x = rect.x < 0 ? 0 : rect.x;
        rect.y = rect.y < 0 ? 0 : rect.y;
        image = image.getSubimage(rect.x,rect.y,rect.width,rect.height);
        glassPane.setImage(image);
        
        Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
        glassPane.setPoint(glassPt);
        glassPane.setVisible(true);
    }

    private Rectangle getTabAreaBounds() {
        Rectangle tabbedRect = getBounds();

        Component comp = getSelectedComponent();

        int idx = 0;
        while(comp==null && idx<getTabCount()) comp = getComponentAt(idx++);
        Rectangle compRect = (comp==null)?new Rectangle():comp.getBounds();
        int tabPlacement = getTabPlacement();
        if (tabPlacement==TOP) {
            tabbedRect.height = tabbedRect.height - compRect.height;
        } else if(tabPlacement==BOTTOM) {
            tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
            tabbedRect.height = tabbedRect.height - compRect.height;
        } else if(tabPlacement==LEFT) {
            tabbedRect.width = tabbedRect.width - compRect.width;
        } else if(tabPlacement==RIGHT) {
            tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
            tabbedRect.width = tabbedRect.width - compRect.width;
        }
        tabbedRect.grow(2, 2);
        return tabbedRect;
    }
    
    class GhostGlassPane extends JPanel {
        private final AlphaComposite composite;
        private Point location = new Point(0, 0);
        private BufferedImage draggingGhost = null;
        public GhostGlassPane() {
            setOpaque(false);
            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        }
        public void setImage(BufferedImage draggingGhost) {
            this.draggingGhost = draggingGhost;
        }
        public void setPoint(Point location) {
            this.location = location;
        }
        @Override 
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(composite);
            
            if (draggingGhost != null) {
                double xx = location.getX() - (draggingGhost.getWidth(this) /2d);
                double yy = location.getY() - (draggingGhost.getHeight(this)/2d);
                g2.drawImage(draggingGhost, (int)xx, (int)yy , null);
            }
            if (dragTabIndex>=0) {
                g2.setPaint(lineColor);
                g2.fill(lineRect);
            }
        }
    }
}

