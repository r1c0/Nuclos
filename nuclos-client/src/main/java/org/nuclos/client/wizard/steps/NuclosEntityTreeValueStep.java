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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.dnd.IReorderable;
import org.nuclos.client.ui.dnd.TableRowTransferHandler;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.EntityTreeViewTableModel;
import org.nuclos.client.wizard.util.MoreOptionPanel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.pietschy.wizard.InvalidStateException;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Entity wizard, entity tree representation, step 5.
 * <br/>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <br/>
 * @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */
@Configurable
public class NuclosEntityTreeValueStep extends NuclosEntityAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityTreeValueStep.class);

	private JScrollPane scrollAttribute;
	private JList lAttribute;

	private JLabel lbValue;
	private JButton btnAddToValueField;
	private JTextField tfValue;

	private JLabel lbTooltip;
	private JTextField tfTooltip;
	private JButton btnAddToTooltipField;

	private JLabel lbDirectory;
	private JTextField tfDirectory;
	private JButton btnDirectory;

	private JLabel lbReportName;
	private JTextField tfReportName;
	private JButton btnReportName;

	private JLabel lbMultiEditEquation;
	private JTextField tfMultiEditEquation;
	private JButton btnMultiEditEquation;

	private JScrollPane paneTreeView;
	private JTable tblTreeView;

	private JLabel lbTreeViewSubform;
	private List<String> subForms;
	private EntityTreeViewTableModel tableModel;

	private JComboBox cbxSubformRefField;

	private JPanel pnlMoreOptions;

	private TreeValueTableCellEditor refNameCellEditor;

	public NuclosEntityTreeValueStep() {
		// initComponents();
	}

	public NuclosEntityTreeValueStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityTreeValueStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}

	@PostConstruct
	@Override
	protected void initComponents() {

		subForms = new ArrayList<String>();

		double size [][] = {{TableLayout.PREFERRED, 50, TableLayout.FILL}, {20,20,5,20,20,5,20,20,5,20,20,5,20,20,7,150, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);

		lAttribute = new JList();
		lAttribute.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollAttribute = new JScrollPane();
		scrollAttribute.setPreferredSize(new Dimension(150, 100));
		scrollAttribute.getViewport().add(lAttribute);
		btnAddToValueField = new JButton(">>");
		btnAddToTooltipField = new JButton(">>");
		btnMultiEditEquation = new JButton(">>");
		btnDirectory = new JButton(">>");
		btnReportName = new JButton(">>");
		lbValue = new JLabel(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.1", "Anzeige Knotendarstellung & dynamischer Fenster-Titel *"));
		tfValue = new JTextField();
		tfValue.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfValue.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.tooltip.1", "Anzeige Knotendarstellung & dynamischer Fenster-Titel *"));

		lbTooltip = new JLabel(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.2", "Bezeichnung des Knoten Tooltips"));
		tfTooltip = new JTextField();
		tfTooltip.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.tooltip.2", "Bezeichnung des Knoten Tooltips"));
		tfTooltip.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbDirectory = new JLabel(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.4", "Verzeichnispfad"));
		lbDirectory.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.tooltip.4", "Bezeichnung des Knoten Tooltips"));
		tfDirectory = new JTextField();

		lbReportName = new JLabel(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.5", "Report Dateiname"));
		tfReportName = new JTextField();
		tfReportName.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.tooltip.5", "Report Dateiname"));

		lbMultiEditEquation = new JLabel(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.3", "Felder f\u00fcr Vergleich in der Sammelbearbeitung"));
		tfMultiEditEquation = new JTextField();
		tfMultiEditEquation.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entitytreevalue.tooltip.3", "Felder f\u00fcr Vergleich in der Sammelbearbeitung"));
		tfMultiEditEquation.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbTreeViewSubform = new JLabel("Baumdarstellung f\u00fcr Unterformulare");
		cbxSubformRefField = new JComboBox();
		refNameCellEditor = new TreeValueTableCellEditor(cbxSubformRefField);

		tableModel = new EntityTreeViewTableModel(refNameCellEditor);
		tblTreeView = new JTable(tableModel);
		tblTreeView.setDragEnabled(true);
		tblTreeView.setDropMode(DropMode.INSERT_ROWS);
		tblTreeView.setTransferHandler(new TableRowTransferHandler(tblTreeView));
		tblTreeView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneTreeView = new JScrollPane(tblTreeView);

		TableColumn colSubform = tblTreeView.getColumnModel().getColumn(1);
		colSubform.setCellEditor(refNameCellEditor);

		btnAddToValueField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 Attribute attr = (Attribute)lAttribute.getSelectedValue();
				 if(attr == null) {
					 return;
				 }
				 String strText = tfValue.getText();
				 strText += NuclosWizardUtils.getStapledString(attr.getInternalName());
				 tfValue.setText(strText);
				 tfValue.setCaretPosition(strText.length());
				 tfValue.requestFocus();
			}
		});

		btnDirectory.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 Attribute attr = (Attribute)lAttribute.getSelectedValue();
				 if(attr == null) {
					 return;
				 }
				 String strText = tfDirectory.getText();
				 strText += NuclosWizardUtils.getStapledString(attr.getInternalName());
				 tfDirectory.setText(strText);
				 tfDirectory.setCaretPosition(strText.length());
				 tfDirectory.requestFocus();
			}
		});

		btnReportName.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 Attribute attr = (Attribute)lAttribute.getSelectedValue();
				 if(attr == null) {
					 return;
				 }
				 String strText = tfReportName.getText();
				 strText += NuclosWizardUtils.getStapledString(attr.getInternalName());
				 tfReportName.setText(strText);
				 tfReportName.setCaretPosition(strText.length());
				 tfReportName.requestFocus();
			}
		});

		tfValue.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityTreeValueStep.this.model.setNodeLabel(e.getDocument().getText(0, e.getDocument().getLength()));
					NuclosEntityTreeValueStep.this.setComplete(e.getDocument().getLength() > 0);
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityTreeValueStep.this, ex);
				}
			}
		});

		tfDirectory.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityTreeValueStep.this.model.setDocumentPath(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityTreeValueStep.this, ex);
				}
			}
		});

		tfReportName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityTreeValueStep.this.model.setReportFilename(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityTreeValueStep.this, ex);
				}
			}
		});


		btnAddToTooltipField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 Attribute attr = (Attribute)lAttribute.getSelectedValue();
				 if(attr == null) {
					 return;
				 }
				 String strText = tfTooltip.getText();
				 strText += NuclosWizardUtils.getStapledString(attr.getInternalName());
				 tfTooltip.setText(strText);
				 tfTooltip.setCaretPosition(strText.length());
				 tfTooltip.requestFocus();
			}
		});

		tfTooltip.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityTreeValueStep.this.model.setNodeTooltip(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityTreeValueStep.this, ex);
				}
			}
		});

		btnMultiEditEquation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 Attribute attr = (Attribute)lAttribute.getSelectedValue();
				 if(attr == null) {
					 return;
				 }
				 String strText = tfMultiEditEquation.getText();
				 if(strText.length() > 0) {
					 strText += "," + attr.getInternalName();
				 }
				 else {
					 strText += attr.getInternalName();
				 }
				 tfMultiEditEquation.setText(strText);
				 tfMultiEditEquation.setCaretPosition(strText.length());
				 tfMultiEditEquation.requestFocus();
			}
		});

		tfMultiEditEquation.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityTreeValueStep.this.model.setMultiEditEquation(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityTreeValueStep.this, ex);
				}
			}
		});

		double sizeMoreOptions [][] = {{50,5, TableLayout.FILL,5}, {20,20,5,20,20,5,20,20, TableLayout.FILL}};

		pnlMoreOptions = new JPanel();

		TableLayout tblLayout = new TableLayout(sizeMoreOptions);

		layout.setVGap(3);
		layout.setHGap(5);

		pnlMoreOptions.setLayout(tblLayout);
		pnlMoreOptions.add(lbDirectory, "0,0,2,0");
		pnlMoreOptions.add(btnDirectory, "0,1");
		pnlMoreOptions.add(tfDirectory, "2,1");
		pnlMoreOptions.add(lbReportName, "0,3,2,3");
		pnlMoreOptions.add(btnReportName, "0,4");
		pnlMoreOptions.add(tfReportName, "2,4");
		pnlMoreOptions.add(lbMultiEditEquation, "0,6,2,6");
		pnlMoreOptions.add(btnMultiEditEquation, "0,7");
		pnlMoreOptions.add(tfMultiEditEquation, "2,7");

		MoreOptionPanel optionPanel = new MoreOptionPanel(pnlMoreOptions);

		this.add(scrollAttribute, "0,0,0,14");
		this.add(lbValue, "1,0,2,0");
		this.add(btnAddToValueField, "1,1");
		this.add(tfValue, "2,1");

		this.add(lbTooltip, "1,3,2,3");
		this.add(btnAddToTooltipField, "1,4");
		this.add(tfTooltip, "2,4");

		this.add(optionPanel, "1,6, 2,14");

		this.add(lbTreeViewSubform, "0,14");
		this.add(paneTreeView, "0,15,2,15");

	}

	@Override
	public void prepare() {
		super.prepare();
		List<Attribute> lstAttribute = new ArrayList<Attribute>(model.getAttributeModel().getAttributes());

        try {
	        lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.CHANGEDAT.getMetaData()));
	        lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.CHANGEDBY.getMetaData()));
	        lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.CREATEDAT.getMetaData()));
	        lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.CREATEDBY.getMetaData()));
	        if(this.model.isStateModel()) {
	        	lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.STATENUMBER.getMetaData()));
	        	lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.STATEICON.getMetaData()));
	        	lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.STATE.getMetaData()));
	        	lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.PROCESS.getMetaData()));
	        	lstAttribute.add(NuclosEntityNameStep.wrapEntityMetaFieldVO(NuclosEOField.SYSTEMIDENTIFIER.getMetaData()));
	        }
        }
        catch(CommonFinderException e) {
	        throw new CommonFatalException(e);
        }
        catch(CommonPermissionException e) {
        	throw new CommonFatalException(e);
        }

        Collections.sort(lstAttribute, new Comparator<Attribute>() {

			@Override
            public int compare(Attribute o1,Attribute o2) {
	            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }

        });

		lAttribute.setListData(lstAttribute.toArray());


		if(this.model.isEditMode()) {
			String sLabel = this.model.getNodeLabel();
			sLabel = removeAttributesFromString(sLabel);
			sLabel = changeAttributesInString(sLabel);
			tfValue.setText(sLabel);

			sLabel = this.model.getNodeTooltip();
			sLabel = removeAttributesFromString(sLabel);
			sLabel = changeAttributesInString(sLabel);
			tfTooltip.setText(sLabel);

			sLabel = this.model.getMultiEditEquation();
			sLabel = removeAttributesFromMultiEditEquation(sLabel);
			sLabel = changeAttributesInMultiEditEquation(sLabel);
			tfMultiEditEquation.setText(sLabel);

			sLabel = this.model.getDocumentPath();
			sLabel = removeAttributesFromString(sLabel);
			sLabel = changeAttributesInString(sLabel);
			tfDirectory.setText(sLabel);

			sLabel = this.model.getReportFilename();
			sLabel = removeAttributesFromString(sLabel);
			sLabel = changeAttributesInString(sLabel);
			tfReportName.setText(sLabel);

			loadSubforms();
			/*if(model.isStateModel())
				loadSubforms();
			else {
				paneTreeView.setVisible(false);
				lbTreeViewSubform.setVisible(false);
			}*/
		}
		else {
			paneTreeView.setVisible(false);
			lbTreeViewSubform.setVisible(false);
		}

		tfDirectory.setEnabled(model.isStateModel());
		tfReportName.setEnabled(model.isStateModel());
		btnDirectory.setEnabled(model.isStateModel());
		btnReportName.setEnabled(model.isStateModel());

	}

	private String removeAttributesFromString(String sNodeLabel) {
		if(sNodeLabel == null)
			return null;
		String sField = sNodeLabel;
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    StringBuffer sb = new StringBuffer();

		while (referencedEntityMatcher.find()) {
		  Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getRemoveAttributes()) {
			  if(attr.getInternalName().equals(sName)){
				  referencedEntityMatcher.appendReplacement (sb, "");
				  break;
			  }
		  }

		}

      // complete the transfer to the StringBuffer
      referencedEntityMatcher.appendTail (sb);
      sField = sb.toString();
      return sField;
	}

	private String removeAttributesFromMultiEditEquation(String sNodeLabel) {
		if(sNodeLabel == null)
			return null;
		String sField = sNodeLabel +",";

		Pattern referencedEntityPattern = Pattern.compile ("[\\w\\[\\]]+[,]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    StringBuffer sb = new StringBuffer();

		while (referencedEntityMatcher.find()) {
		  String value = referencedEntityMatcher.group();
		  if(value.endsWith(","))
			  value = value.substring(0, value.length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getRemoveAttributes()) {
			  if(attr.getInternalName().equals(sName)){
				  referencedEntityMatcher.appendReplacement (sb, "");
				  break;
			  }
		  }
		}

      // complete the transfer to the StringBuffer
      referencedEntityMatcher.appendTail (sb);
      if(sb.length() > 0) {
    	  sField = sb.toString().substring(0,sb.toString().length()-1);
      }
      else {
    	  sField = sb.toString();
      }

      return sField;
	}


	private String changeAttributesInMultiEditEquation(String sNodeLabel) {
		if(sNodeLabel == null)
			return null;
		String sField = sNodeLabel +",";
		Pattern referencedEntityPattern = Pattern.compile ("[\\w\\[\\]]+[,]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    StringBuffer sb = new StringBuffer();

		while (referencedEntityMatcher.find()) {
			 String value = referencedEntityMatcher.group();
			  if(value.endsWith(","))
				  value = value.substring(0, value.length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getAttributes()) {
			  if(!attr.hasInternalNameChanged())
				  continue;
			  if(attr.getOldInternalName().equals(sName)){
				  referencedEntityMatcher.appendReplacement (sb, attr.getInternalName() + ",");
				  break;
			  }
		  }

		}

      // complete the transfer to the StringBuffer
      referencedEntityMatcher.appendTail (sb);
      sField = sb.toString().substring(0,sb.toString().length()-1);
      return sField;
	}


	private String changeAttributesInString(String sNodeLabel) {
		if(sNodeLabel == null)
			return null;
		String sField = sNodeLabel;
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    StringBuffer sb = new StringBuffer();

		while (referencedEntityMatcher.find()) {
		  Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getAttributes()) {
			  if(!attr.hasInternalNameChanged())
				  continue;
			  if(attr.getOldInternalName().equals(sName)){
				  // ${*} has to be escaped
				  referencedEntityMatcher.appendReplacement (sb, "\\" +NuclosWizardUtils.getStapledString(attr.getInternalName()));
				  break;
			  }
		  }

		}

      // complete the transfer to the StringBuffer
      referencedEntityMatcher.appendTail (sb);
      sField = sb.toString();
      return sField;
	}

	private void loadSubforms() {
		final GenericObjectMetaDataCache goCache = GenericObjectMetaDataCache.getInstance();
		subForms = new ArrayList<String>();
		Long id = MetaDataDelegate.getInstance().getEntityIdByName(model.getEntityName());
		SortedSet<EntityTreeViewVO> lst = new TreeSet<EntityTreeViewVO>();
		refNameCellEditor.clear();

		// For all subforms in the entity layout...
		for (String subform : goCache.getSubFormEntityNamesByModuleId(IdUtils.unsafeToId(id))) {
			subForms.add(subform);
			EntityTreeViewVO voTmp = null;
			// find if there are already tree view preferences for this subform in the model
			for(EntityTreeViewVO voTreeView : model.getTreeView()) {
				if(voTreeView.getEntity().equals(subform)) {
					voTmp = voTreeView;
					break;
				}
			}
			if (voTmp == null) {
				// create a new tree view preferences table line if a (former) preference have not been found
				lst.add(new EntityTreeViewVO(null, id, subform, getRefFieldTo(subform).iterator().next(), null, Boolean.FALSE, 0));
			}
			else {
				lst.add(voTmp);
			}
		}
		paneTreeView.setVisible(subForms.size() > 0);
		lbTreeViewSubform.setVisible(subForms.size() > 0);

		// The sequence of lst could be another as the sequence of the for-loop.
		// Hence, we must set the cell editors in its own loop.
		for (EntityTreeViewVO vo: lst) {
			// Treat the cells in field name column special:
			refNameCellEditor.initCellEditors(vo.getEntity());
		}
		tableModel.setRows(lst);
		
		// NUCLOSINT-1089:
		// Delete all object from table t_md_entity_subnodes that are not present in lst.
		final EntityObjectDelegate eod = EntityObjectDelegate.getInstance();
		final Set<EntityTreeViewVO> toDelete = new HashSet<EntityTreeViewVO>(model.getTreeView());
		toDelete.removeAll(lst);
		for (EntityTreeViewVO tv: toDelete) {
			try {
				eod.removeEntity(NuclosEntity.ENTITYSUBNODES.getEntityName(), tv.getId());
			}
			catch (CommonPermissionException e) {
				LOG.error("Unable to remove (obsolete) " + tv, e); 
			}
		}
	}

	@Override
	public void applyState() throws InvalidStateException {
		super.applyState();
		if(tblTreeView.getCellEditor() != null)
			tblTreeView.getCellEditor().stopCellEditing();
		model.setTreeView(tableModel.getRows());
	}

	private SortedSet<String> getRefFieldTo(String subformEntityName) {
		final SortedSet<String> result = new TreeSet<String>();
		final Set<EntityFieldMetaDataVO> strange = new HashSet<EntityFieldMetaDataVO>();
		for(EntityFieldMetaDataVO voField : MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(subformEntityName).values()) {
			final String fEntity = voField.getForeignEntity();
			if(model.getEntityName().equals(fEntity) || NuclosEntity.GENERICOBJECT.getEntityName().equals(fEntity)) {
				result.add(voField.getField());
			}
			// TODO: sometimes we don't find the 'right' base entity reference with the if clause above,
			// thus the result set is empty. See http://support.novabit.de/browse/NUCLOSINT-1192 for details.
			// We log this case now, to get an (better) idea what is happening here.
			else if(fEntity != null) {
				strange.add(voField);
			}
		}
		if (result.isEmpty()) {
			LOG.warn("NuclosEntityTreeValueStep.getRefFieldTo: unable to find a ref to " + model.getEntityName() + ", using fallback...");
			for (EntityFieldMetaDataVO md: strange) {
				result.add(md.getField());
			}
		}
		if (!strange.isEmpty()) {
			final StringBuilder msg = new StringBuilder();
			msg.append("NuclosEntityTreeValueStep.getRefFieldTo: fallback ref to " + model.getEntityName() + " contains: [\n");
			for (EntityFieldMetaDataVO md: strange) {
				msg.append("\t(field: ").append(md.getField());
				msg.append(", entity: ").append(md.getEntityIdAsString());
				msg.append(", foreign entity:").append(md.getForeignEntity());
				msg.append(", foreign field:").append(md.getForeignEntityField());
				msg.append(")\n");
			}
			msg.append("]\n");
			LOG.warn(msg.toString());
		}
		return result;
	}

	private class TreeValueTableCellEditor extends DefaultCellEditor implements IReorderable {

		/**
		 * CellEditor with ComboBox for the reference field in the subform entity.
		 */
		private List<DefaultCellEditor> refToBaseEntityEditor = new ArrayList<DefaultCellEditor>();

		public TreeValueTableCellEditor(JComboBox comboBox) {
			super(comboBox);
		}

		public void clear() {
			refToBaseEntityEditor.clear();
		}

		public void initCellEditors(String subformEntityName) {
			// Treat the cells in field name column special:
			JComboBox editBox = new JComboBox();
			DefaultCellEditor edit = new DefaultCellEditor(editBox);
			refToBaseEntityEditor.add(edit);
			// editBox.addItem("");
			for(String voField : getRefFieldTo(subformEntityName)) {
				editBox.addItem(voField);
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
			if(column == 1)
				return refToBaseEntityEditor.get(row).getComponent();

			return super.getTableCellEditorComponent(table, value, isSelected,
				row, column);
		}

		@Override
		public Object getCellEditorValue() {
			final int row = tblTreeView.getSelectedRow();
			// final int column = tblTreeView.getSelectedColumn();

			DefaultCellEditor celleditor = refToBaseEntityEditor.get(row);
			JComboBox box = (JComboBox)celleditor.getComponent();
			return box.getSelectedItem();
		}

		@Override
		public void reorder(int fromModel, int toModel) {
			// swap editors
			DefaultCellEditor from = refToBaseEntityEditor.get(fromModel);
			refToBaseEntityEditor.set(fromModel, refToBaseEntityEditor.get(toModel));
			refToBaseEntityEditor.set(toModel, from);
		}

	}

}

