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
package org.nuclos.client.ui.collect.component;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.labeled.LabeledTextField;
import org.nuclos.client.ui.message.MessageExchange;
import org.nuclos.client.ui.message.MessageExchange.MessageExchangeListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JTextField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableTextField extends CollectableTextComponent implements MessageExchangeListener {
	
	private static final Logger LOG = Logger.getLogger(CollectableTextField.class);

   private CollectableEntityField clctef;
   
   /**
    * @param clctef
    * @postcondition this.isDetailsComponent()
    */
   public CollectableTextField(CollectableEntityField clctef) {
      this(clctef, false);
      assert this.isDetailsComponent();
   }

   public CollectableTextField(CollectableEntityField clctef, boolean bSearchable) {
      super(clctef, new LabeledTextField(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);
      this.clctef = clctef;
      setupTextField(clctef, this.getJTextField(), bSearchable);
      MessageExchange.addListener(this);
   }

   // @todo return JTextField
   public CommonJTextField getJTextField() {
      return (CommonJTextField) this.getJTextComponent();
   }

   @Override
   public void setColumns(int iColumns) {
      this.getJTextField().setColumns(iColumns);
   }

   @Override
   public void setComparisonOperator(ComparisonOperator compop) {
      super.setComparisonOperator(compop);

      if (compop.getOperandCount() < 2) {
         this.runLocked(new Runnable() {
            @Override
            public void run() {
            	try {
            		getJTextComponent().setText(null);
				}
				catch (Exception e) {
					LOG.error("CollectableTextField.setComparisionOperator: " + e, e);
				}            		
            }
         });
      }
   }

   /**
    * sets up a contained textfield according to the type of this component.
    * Takes special care for dates and numbers.
    * @param ntf
    */
   private static void setupTextField(CollectableEntityField clctef, CommonJTextField ntf, boolean bSearchable) {
      // set the preferred width for numbers and dates:
      final Class<?> cls = clctef.getJavaClass();
      if (Number.class.isAssignableFrom(cls)) {
         ntf.setColumnWidthChar('0');
         // numbers are right aligned:
         ntf.setHorizontalAlignment(JTextField.TRAILING);
         ntf.setFormat(CollectableFieldFormat.getInstance(cls));
         ntf.setPattern(clctef.getFormatOutput());
      }
      else if (Date.class.isAssignableFrom(cls)) {
         /** @todo this could be enhanced by calculating the width of "01.01.2000" */
         ntf.setColumnWidthChar('0');
         ntf.setFormat(CollectableFieldFormat.getInstance(cls));
         ntf.setPattern(clctef.getFormatOutput());
      }
      else if (Boolean.class.isAssignableFrom(cls)) {
    	  ntf.setColumnWidthChar('0');
          ntf.setFormat(CollectableFieldFormat.getInstance(cls));
      }

      if (bSearchable)
      	addAutoComplete(clctef, ntf, getAutoCompletePreferences(clctef));
   }

   public static void addAutoComplete(CollectableEntityField clctef, CommonJTextField ntf, Preferences prefs) {
   	if (prefs != null) {
   		List<String> items = new ArrayList<String>();
   		try {
   			items = PreferencesUtils.getStringList(prefs, clctef.getName());
   		}
   		catch(PreferencesException e) {
   			LOG.warn("addAutoComplete: " + e);
   		}
   		ntf.addAutoCompleteItems(items);
   	}
   }
   
	private static class CollectableTextFieldCellRenderer implements TableCellRenderer {
		
		private final TableCellRenderer parentRenderer;
		
		// Don't use, this triggers a memory leak! (tp)
		// private final CommonJTextField ntf;

		private final int horizontalAlignment;
		
		private CollectableTextFieldCellRenderer(TableCellRenderer parentRenderer, CommonJTextField ntf) {
			this.parentRenderer = parentRenderer;
			this.horizontalAlignment = ntf.getHorizontalAlignment();
		}

		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus,
				int iRow, int iColumn) {
			final Component comp = parentRenderer.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow,
					iColumn);
			if (comp instanceof JLabel) {
				final JLabel lb = (JLabel) comp;
				lb.setHorizontalAlignment(horizontalAlignment);
			}
			return comp;
		}
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		final TableCellRenderer parentRenderer = super.getTableCellRenderer(subform);
		final CommonJTextField ntf = getJTextField();
		return new CollectableTextFieldCellRenderer(parentRenderer, ntf);
	}

   @Override
   public void receive(Object id, ObjectType type, MessageType msg) {
      if(clctef != null && clctef.getCollectableEntity() != null) {
         Pair<String, String> idPair = new Pair<String, String>(clctef.getCollectableEntity().getName(), clctef.getName());
         if(idPair.equals(id))
            if (type == MessageExchangeListener.ObjectType.TEXTFIELD)
               if (msg == MessageExchangeListener.MessageType.REFRESH)
            	   if (isSearchComponent())
            		   addAutoComplete(clctef, getJTextField(), getAutoCompletePreferences(clctef));
      }
   }

   private static Preferences getAutoCompletePreferences(CollectableEntityField clctef) {
      if(clctef.getCollectableEntity() != null) {
         return ClientPreferences.getUserPreferences().node("collect").node("entity").node(clctef.getCollectableEntity().getName()).node("fields");
      }
      return null;
   }
}	// class CollectableTextField
