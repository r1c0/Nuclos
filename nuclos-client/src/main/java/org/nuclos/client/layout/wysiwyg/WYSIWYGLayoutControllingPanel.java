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
package org.nuclos.client.layout.wysiwyg;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections.map.ListOrderedMap;
import org.xml.sax.SAXException;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.client.layout.admin.LayoutCollectController;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.WYSIWYGLAYOUT_EDITOR_PANEL;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGInitialFocusComponentEditor;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable;
import org.nuclos.client.layout.wysiwyg.palette.PaletteController;

/**
 * This Class controls the WYSIWYG Editor. It does setup the Panels, shows the
 * Editor {@link WYSIWYGLayoutControllingPanel#showPaletteAndEditorPanel()} or
 * the "Waiting for Metainformation" Panel
 * {@link WYSIWYGLayoutControllingPanel#showWaitingPanel()} It also contains the
 * ChangeDescriptor for notifiying the Main Controllerpanel that something
 * changed.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIWYGLayoutControllingPanel extends JPanel implements WYSIWYGToolbarAttachable, WYSIWYGEditorModes {

	private PaletteController paletteCtrl;

	private LayoutMLGenerator mlGenerator;

	private LayoutMLLoader mlLoader;

	private WYSIWYGLayoutEditorPanel jpnEditor;

	private JPanel jpnPaletteAndEditor;

	private JPanel jpnWaitingForMeta;

	/** the main JSplitPane (left Palette and right the EditorPanel) */
	private JSplitPane jpnSplit;
	
	private JTabbedPane paletteAndProperties;

	private final List<ChangeListener> lstChangeListener = new Vector<ChangeListener>();

	private WYSIWYGLayoutEditorChangeDescriptor chgDescriptor;

	private WYSIWYGMetaInformation metaInf;

	private LayoutCollectController layoutCollectController;
	
	private PropertiesPanel container;

	private int mode = STANDARD_MODE;

	private Preferences preferences;

	/** the property node for the mode the WYSIWYG Editor did work in */
	public static final String PREFERENCES_MODE = "mode";
	// NUCLEUSINT-362
	public static final String PREFERENCES_SPLITPANE_DIVIDERLOCATION = "divider-location";

	/** the wyiswyg propertynode used to store the state of slicing */
	public static final String PREFERENCES_SLICING = "slicing";
	
	/**
	 * setting the modes that are choosable for the editor, used to filter
	 * Properties and Components
	 */
	private Map<String, Integer> modes = new ListOrderedMap();
	{
		modes.put(WYSIWYGStringsAndLabels.STANDARD_MODE, STANDARD_MODE);
		modes.put(WYSIWYGStringsAndLabels.EXPERT_MODE, EXPERT_MODE);
	}

	public WYSIWYGLayoutControllingPanel(WYSIWYGMetaInformation metaInf) {
		this.metaInf = metaInf;
		this.setLayout(new BorderLayout());
		setupPanel();
	}

	public WYSIWYGMetaInformation getMetaInformation() {
		return metaInf;
	}

	/**
	 * This method starts the generation of the layoutML. The WYSIWYG Editor
	 * panel passed to the LayoutMLGenerator.
	 * 
	 * @return
	 * @throws CommonValidationException
	 */
	public String getLayoutML() throws CommonValidationException {
		//NUCLEUSINT-554
		storeSplitpaneDividerInPreferences();
		return mlGenerator.getLayoutML(jpnEditor);
	}

	/**
	 * This Method loads the LayoutML. The LayoutML XML is put in the
	 * LayoutMLLoader and creates the WYSIWYG Editor with the components
	 * described in the LayoutML
	 * 
	 * @param layoutML
	 * @throws CommonBusinessException
	 * @see CollectableWYSIWYGLayoutEditor#updateView(org.nuclos.common.collect.collectable.CollectableField)
	 */
	public void setLayoutML(String layoutML) throws CommonBusinessException, SAXException {
		setupPanel();
		mlLoader.setLayoutML(jpnEditor, layoutML);
	}
	
	public List<WYSIWYGComponent> getAllComponents() {
		return this.jpnEditor.getAllComponents();
	}

	/**
	 * This Method creates the WYSIWYG Editor
	 */
	private void setupPanel() {
		this.removeAll();

		/*
		 * EDITOR
		 */
		jpnPaletteAndEditor = new JPanel(new BorderLayout());

		paletteCtrl = new PaletteController(mode);
		mlGenerator = new LayoutMLGenerator();
		mlLoader = new LayoutMLLoader();

		final JPanel jpnSplitLeftPanel = paletteCtrl.getPalettePanel();
		
		paletteAndProperties = new JTabbedPane();
		paletteAndProperties.insertTab(WYSIWYGStringsAndLabels.PALETTE_CONTROLLER.TITLE_PALETTE, null, jpnSplitLeftPanel, "", 0);

		double[][] layout = {{TableLayout.FILL}, {25, TableLayout.FILL}};
		final JPanel jpnSplitRightPanel = new JPanel(new TableLayout(layout));

		/**
		 * the main JSplitPane with the Editor Panel on the Right and the
		 * Palette on the Left
		 */
		jpnSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paletteAndProperties, jpnSplitRightPanel);
		jpnSplit.setVisible(true);
		jpnSplit.setOneTouchExpandable(true);
		jpnSplit.setDividerSize(10);
		
		// NUCLEUSINT-362 / NUCLEUSINT-554
		if(getPreferences() != null) {
			int dividerPosition = getPreferences().getInt(PREFERENCES_SPLITPANE_DIVIDERLOCATION, 250);
			jpnSplit.setDividerLocation(dividerPosition);
		}
		
		jpnPaletteAndEditor.add(jpnSplit, BorderLayout.CENTER);

		/**
		 * The Toolbar on the top of the EditorPanel, this is where UndoRedo and
		 * the Modeselector are placed
		 */
		WYSIWYGEditorsToolbar wysiwygEditorsToolbar = new WYSIWYGEditorsToolbar();

		jpnEditor = new WYSIWYGLayoutEditorPanel(getMetaInformation(), wysiwygEditorsToolbar);
//		if (preferences != null) {
//			jpnEditor.getJustAddToggleButton().setJustAddEnabled(preferences.getBoolean(PREFERENCES_SLICING, false));
//		}

		if (chgDescriptor != null) {
			jpnEditor.setWYSIWYGLayoutEditorChangeDescriptor(chgDescriptor);
		}

		jpnSplitRightPanel.add(wysiwygEditorsToolbar, "0,0");

		JScrollPane scrollpane = new JScrollPane(jpnEditor);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getHorizontalScrollBar().setUnitIncrement(20);
		jpnSplitRightPanel.add(scrollpane, "0,1");

		/**
		 * WAITING This Panel is shown if the Metainformations are not correctly
		 * set. It is removed and replaced by the panel with the Palette and the
		 * Editor if Metainformation was set
		 */
		jpnWaitingForMeta = new JPanel(new BorderLayout());
		jpnWaitingForMeta.add(new JLabel(WYSIWYGLAYOUT_EDITOR_PANEL.WAITING_FOR_METAINFORMATION), BorderLayout.CENTER);

		/**
		 * layoutML Dependencies are not included, the LayoutMLParser is missing
		 * the implementation of this LayoutML Part
		 */
		// wysiwygEditorsToolbar.addComponentToToolbar(jpnEditor.
		// getLayoutMLDependencies());
		wysiwygEditorsToolbar.addComponentToToolbar(new WYSIWYGInitialFocusComponentEditor(jpnEditor));

		wysiwygEditorsToolbar.addComponentToToolbar(this);

		this.add(jpnWaitingForMeta);
	}

	/**
	 * Helpermethod, clearing the whole panel
	 */
	public void clearEditorPanel() {
		jpnEditor.getTableLayoutUtil().clearTableLayout();
	}

	/**
	 * This method creates a StandardLayout
	 */
	public void createStandartLayout() {
		jpnEditor.getTableLayoutUtil().createStandardLayout();
	}

	/**
	 * This Method clears the UndoRedoStack
	 */
	public void clearUndoRedoStack() {
		jpnEditor.getTableLayoutUtil().getUndoRedoFunction().clearUndoRedoStack();
	}

	/**
	 * 
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
		this.lstChangeListener.add(listener);
	}

	/**
	 * 
	 * @param listener
	 */
	public void removeChangeListener(ChangeListener listener) {
		this.lstChangeListener.remove(listener);
	}

	/**
	 * @todo fireChange should be called every time when the contents of the
	 *       MyXerlinPanel changes, not only when it gets dirty (on the first
	 *       change).
	 */
	public void fireChange() {
		for (ChangeListener listener : this.lstChangeListener) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * 
	 * @param chgDescriptor
	 */
	public void setEditorChangeDescriptor(WYSIWYGLayoutEditorChangeDescriptor chgDescriptor) {
		this.chgDescriptor = chgDescriptor;
		jpnEditor.setWYSIWYGLayoutEditorChangeDescriptor(chgDescriptor);
	}

	/**
	 * Displays the "Waiting for Metadata" Panel
	 */
	public void showWaitingPanel() {
		showComponent(jpnWaitingForMeta);
		//NUCLEUSINT-976
		((JTabbedPane)this.getParent().getParent()).setSelectedIndex(1);
	}

	/**
	 * Displays the WYISWYG Editor
	 */
	public void showPaletteAndEditorPanel() {
		showComponent(jpnPaletteAndEditor);
		//NUCLEUSINT-976
		((JTabbedPane)this.getParent().getParent()).setSelectedIndex(0);
	}

	/**
	 * Method for switching the Layoutpanels
	 * 
	 * @param componentToShow
	 * @see #showPaletteAndEditorPanel()
	 * @see #showWaitingPanel()
	 */
	// NUCLEUSINT-314 refractored to be able to pass over the xerlin editor
	public void showComponent(JComponent componentToShow) {
		this.removeAll();
		this.add(componentToShow, BorderLayout.CENTER);
	}

	/**
	 * 
	 * @return LayoutCollectController
	 */
	public LayoutCollectController getLayoutCollectController() {
		return layoutCollectController;
	}

	/**
	 * @see CollectableWYSIWYGLayoutEditor#setLayoutCollectController(LayoutCollectController)
	 * @param layoutCollectController
	 */
	public void setLayoutCollectController(LayoutCollectController layoutCollectController) {
		this.layoutCollectController = layoutCollectController;
	}

	/**
	 * 
	 * @return
	 */
	public Preferences getPreferences() {
		return preferences;
	}

	/**
	 * 
	 * @param preferences
	 */
	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
		setMode(preferences.getInt(PREFERENCES_MODE, STANDARD_MODE));
		//jpnEditor.setJustAddEnabled(preferences.getBoolean(PREFERENCES_SLICING, false));
	}
	
	/**
	 * Stores Position of the Palette Splitpane in UserPrefs
	 */
	private void storeSplitpaneDividerInPreferences() {
		if (getPreferences() != null)
			getPreferences().putInt(PREFERENCES_SPLITPANE_DIVIDERLOCATION, jpnSplit.getDividerLocation());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable#getToolbarItems()
	 */
	@Override
	public JComponent[] getToolbarItems() {
		JComboBox comboBox = new JComboBox();
		comboBox.setSize(200, 23);
		for (Map.Entry<String, Integer> e : modes.entrySet()) {
			comboBox.addItem(e.getKey());
			if (e.getValue() == this.mode) {
				comboBox.setSelectedItem(e.getKey());
			}
		}

		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setMode(modes.get(e.getItem()));
				}
			}
		});

		return new JComponent[]{comboBox};
	}

	/**
	 * @return the Mode the Editor is currently running in
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Setting the Mode the Editor is running in Called from
	 * 
	 * @see #getToolbarItems()
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
		this.paletteCtrl.setMode(mode);
		this.preferences.putInt(PREFERENCES_MODE, mode);
		
		if (this.container != null) {
			// preferences are shown and must be refreshed
			PropertiesPanel.showPropertiesForComponent(container.getWYSIWYGComponent(), container.getTableLayoutUtil());	
		}
	}
	
	/**
	 * Shows the properties for the component
	 * 
	 * @param container the complete properties panel
	 * @param componentName the name to display in the tab
	 */
	public synchronized void setPreferencesPanel(PropertiesPanel container, String componentName) {
		safePendingPropertyChanges();
		
		this.container = container;
		if (arePreferencesShown()) {
			// saving changes when
//			PropertiesPanel before = (PropertiesPanel)paletteAndProperties.getComponentAt(1);
//			before.performSaveAction();
			paletteAndProperties.remove(1);
		}
		paletteAndProperties.insertTab(componentName, null, container, "", 1);
		paletteAndProperties.invalidate();
		paletteAndProperties.setSelectedIndex(1);
	}
	
	/**
	 * Helpermethod for checking if the properties are shown
	 * @return true if preferences are shown
	 */
	public boolean arePreferencesShown() {
		if (paletteAndProperties.getTabCount() > 1)
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param component the {@link WYSIWYGComponent} to check
	 * @return true if the Properties for this Component are shown
	 */
	public boolean preferencesForThisComponentShown(WYSIWYGComponent component) {
		if (arePreferencesShown()) {
			if (container.getWYSIWYGComponent().equals(component))
				return true;
		}
		
		return false;
	}
	
	public synchronized void safePendingPropertyChanges() {
		if (arePreferencesShown() && this.container != null) {
			this.container.performSaveAction();
		}
	}
	
	/**
	 * Hides the preferences for the component
	 */
	public synchronized void hidePreferencesPanel() {
		this.container = null;
		paletteAndProperties.remove(1);
		paletteAndProperties.invalidate();
		paletteAndProperties.setSelectedIndex(0);
	}
	
	public WYSIWYGLayoutEditorPanel getEditorPanel() {
		return this.jpnEditor;
	}
}
