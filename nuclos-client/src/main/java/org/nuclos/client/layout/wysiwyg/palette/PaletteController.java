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
package org.nuclos.client.layout.wysiwyg.palette;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_CHART;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_CHECKBOX;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_COMBOBOX;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_DATECHOOSER;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_EMAIL;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_HYPERLINK;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABEL;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_CHECKBOX;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_COMBOBOX;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_DATECHOOSER;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_EMAIL;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_HYPERLINK;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_IMAGE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_LISTOFVALUES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_OPTIONGROUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_PASSWORDFIELD;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_TEXTAREA;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LABELED_TEXTFIELD;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_LISTOFVALUES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_OPTIONGROUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_PASSWORDFIELD;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_SUBFORM;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_TEXTAREA;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COLLECTABLE_TEXTFIELD;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.EDITORPANEL;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSCROLLPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JSPLITPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JTABBEDPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PALETTE_CONTROLLER;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_COMBOBOX;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_LABEL;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_SEPERATOR;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_TEXTAREA;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_TEXTFIELD;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_TITLED_SEPARATOR;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableElement;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.client.ui.Errors;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * PaletteController builds palette view and handles drag&drop
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
@Configurable
public class PaletteController implements DragGestureListener, LayoutMLConstants, WYSIWYGEditorModes {

	private NucletComponentRepository nucletComponentRepository;
	
	private JPanel palettePanel = new JPanel();
	private JList paletteList;

	/**
	 * the model containing the palette items
	 */
	private DefaultListModel model;

	/**
	 * dragsource to register for draggestures
	 */
	private final DragSource dragsource = DragSource.getDefaultDragSource();

	/**
	 * ctor
	 */
	public PaletteController() {
	}
	
	@Autowired
	void setNucletComponentRepository(NucletComponentRepository nucletComponentRepository) {
		this.nucletComponentRepository = nucletComponentRepository;
	}
	
	@PostConstruct
	void init() {
		palettePanel.setLayout(new GridBagLayout());

		JLabel paletteLabel = new JLabel(PALETTE_CONTROLLER.TITLE_PALETTE);

		paletteLabel.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), new EmptyBorder(2, 2, 2, 2)));
		palettePanel.add(paletteLabel, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));

		paletteList = new JList(setupModel());
		paletteList.setCellRenderer(new PaletteItemListCellRenderer());

		dragsource.createDefaultDragGestureRecognizer(paletteList, DnDConstants.ACTION_COPY, this);
		palettePanel.add(new JScrollPane(paletteList), new GridBagConstraints(0, 2, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

	}

	/**
	 * getter for view
	 * 
	 * @return view
	 */
	public JPanel getPalettePanel() {
		return palettePanel;
	}

	/**
	 * start drag
	 */
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		Object o = paletteList.getSelectedValue();
		if (o instanceof PaletteItem) {
			PaletteItem item = (PaletteItem) o;
			try {
			    // NUCLEUSINT-496
				dge.startDrag(null, new TransferableElement(item.getElement(), item.getControltype(), item.isLabeledComponent()));
			} catch (InvalidDnDOperationException e) {
				Errors.getInstance().showExceptionDialog(null, e);
			}
		}
	}

	/**
	 * Fills the ListModel with palette items.
	 * Filterting is done utilizing {@link WYSIWYGEditorModes}
	 * 
	 * @return
	 */
	private ListModel setupModel() {
		model = new DefaultListModel();
		// NUCLEUSINT-496
		model.addElement(PALETTE_CONTROLLER.SECTION_COLLECTABLE_LABELED_COMPONENTS);
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_TEXTFIELD, new COLLECTABLE_LABELED_TEXTFIELD()));
		//NUCLEUSINT-1142
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_PASSWORDFIELD, new COLLECTABLE_LABELED_PASSWORDFIELD()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_TEXTAREA, new COLLECTABLE_LABELED_TEXTAREA()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_COMBOBOX, new COLLECTABLE_LABELED_COMBOBOX()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_LISTOFVALUES, new COLLECTABLE_LABELED_LISTOFVALUES()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_DATECHOOSER, new COLLECTABLE_LABELED_DATECHOOSER()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_HYPERLINK, new COLLECTABLE_LABELED_HYPERLINK()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_EMAIL, new COLLECTABLE_LABELED_EMAIL()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_CHECKBOX, new COLLECTABLE_LABELED_CHECKBOX()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_OPTIONGROUP, new COLLECTABLE_LABELED_OPTIONGROUP()));
		//model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_IMAGE, new COLLECTABLE_LABELED_IMAGE()));
		model.addElement(PALETTE_CONTROLLER.SECTION_COLLECTABLE_COMPONENTS);
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_LABEL, new COLLECTABLE_LABEL()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_TEXTFIELD, new COLLECTABLE_TEXTFIELD()));
		//NUCLEUSINT-1142
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_PASSWORD, new COLLECTABLE_PASSWORDFIELD()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_TEXTAREA, new COLLECTABLE_TEXTAREA()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_COMBOBOX, new COLLECTABLE_COMBOBOX()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_LISTOFVALUES, new COLLECTABLE_LISTOFVALUES()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_DATECHOOSER, new COLLECTABLE_DATECHOOSER()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_HYPERLINK, new COLLECTABLE_HYPERLINK()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_EMAIL, new COLLECTABLE_EMAIL()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_CHECKBOX, new COLLECTABLE_CHECKBOX()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, ELEMENT_OPTIONGROUP, new COLLECTABLE_OPTIONGROUP()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, CONTROLTYPE_IMAGE, new COLLECTABLE_LABELED_IMAGE()));
		model.addElement(new PaletteItem(ELEMENT_SUBFORM, new COLLECTABLE_SUBFORM()));
		model.addElement(new PaletteItem(ELEMENT_CHART, new COLLECTABLE_CHART()));
		model.addElement(new PaletteItem(ELEMENT_COLLECTABLECOMPONENT, new WYSIWYGStringsAndLabels.COLLECTABLE_COMPONENT()));
		model.addElement(PALETTE_CONTROLLER.SECTION_STATIC_FIELDS);
		model.addElement(new PaletteItem(ELEMENT_LABEL, new STATIC_LABEL()));
		model.addElement(new PaletteItem(ELEMENT_BUTTON, new STATIC_BUTTON()));
		model.addElement(new PaletteItem(ELEMENT_TEXTFIELD, new STATIC_TEXTFIELD()));
		model.addElement(new PaletteItem(ELEMENT_TEXTAREA, new STATIC_TEXTAREA()));
		model.addElement(new PaletteItem(ELEMENT_COMBOBOX, new STATIC_COMBOBOX()));
		model.addElement(PALETTE_CONTROLLER.SECTION_COMPONENTS);
		//NUCLEUSINT-650
		model.addElement(new PaletteItem(ELEMENT_PANEL, new EDITORPANEL()));
		model.addElement(new PaletteItem(ELEMENT_TABBEDPANE, new JTABBEDPANE()));
		model.addElement(new PaletteItem(ELEMENT_SCROLLPANE, new JSCROLLPANE()));
		model.addElement(new PaletteItem(ELEMENT_SPLITPANE, new JSPLITPANE()));		
		model.addElement(PALETTE_CONTROLLER.SECTION_SEPARATOR);
		model.addElement(new PaletteItem(ELEMENT_SEPARATOR, new STATIC_SEPERATOR()));
		model.addElement(new PaletteItem(ELEMENT_TITLEDSEPARATOR, new STATIC_TITLED_SEPARATOR()));

		List<LayoutComponentFactory> layoutComponentFactories = nucletComponentRepository.getLayoutComponentFactories();
		if (!layoutComponentFactories.isEmpty()) {
			model.addElement(PALETTE_CONTROLLER.SECTION_NUCLET);
			Collections.sort(layoutComponentFactories, new FactoryComparator());
			for (LayoutComponentFactory lcf : layoutComponentFactories) {
				model.addElement(new PaletteItem(ELEMENT_LAYOUTCOMPONENT, lcf.getClass().getName(), new LayoutComponentPaletteItem(lcf)));
			}
		}
		
		return model;
	}

	/**
	 */
	public void initialize() {
		setupModel();
		paletteList.setModel(model);
	}

	/**
	 * checks if contents of dragevent support rewuired dataflavor
	 * 
	 * @param dtde
	 * @return
	 */
	private boolean isDropAcceptable(DropTargetDragEvent dtde) {
		return dtde.isDataFlavorSupported(TransferableElement.flavor);
	}

	/**
	 * reject drag by default
	 */
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.rejectDrag();
	}

	public void dragExit(DropTargetEvent dte) {
	}

	/**
	 * check if drop is acceptable
	 */
	public void dragOver(DropTargetDragEvent dtde) {
		if (!isDropAcceptable(dtde)) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
	}

	/**
	 * renders item in list with icon, label and tooltip
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a
	 *         href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
	 * @version 01.00.00
	 */
	private class PaletteItemListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof PaletteItem) {
				PaletteItem b = (PaletteItem) value;
				if (b != null) {
					this.setIcon(b.getIcon());
				}
				this.setBorder(new EmptyBorder(4, 4, 4, 0));
				this.setText(b.getLabel());

				this.setToolTipText(b.getTooltip());
			} else if (value instanceof String) {
				String s = (String) value;
				this.setText(s);
				this.setBackground(UIManager.getColor("control"));
			}

			return result;
		}
	}
	
	public static class FactoryComparator implements Comparator<LayoutComponentFactory> {
		@Override
		public int compare(LayoutComponentFactory o1, LayoutComponentFactory o2) {
			return StringUtils.compareIgnoreCase(getFactoryPresentation(o1), getFactoryPresentation(o2));
		}
	}

	private static String getFactoryPresentation(Object o) {
		if (o instanceof LayoutComponentFactory) {
			return String.format("%s (%s)", ((LayoutComponentFactory) o).getName(), o.getClass().getName());
		} else {
			return o==null? "": o.toString();
		}
	}
}
