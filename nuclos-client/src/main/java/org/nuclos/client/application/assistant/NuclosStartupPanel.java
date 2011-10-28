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
package org.nuclos.client.application.assistant;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.explorer.ProzessWizardPanel;
import org.nuclos.client.explorer.WorkflowWizardPanel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;


/**
 * @author marc.finke
 * first named Nuclos Class
 *
 */
public class NuclosStartupPanel extends JPanel implements ApplicationAssistantListener {
	
	private static final Logger LOG = Logger.getLogger(NuclosStartupPanel.class);
	
	private JPanel pnlOverview;
	private JLabel lbLink;
	private JLabel lbSteep;
	
	private JPanel pnlDescription;
	private StartupJLabel lbDescription;
	private JLabel lbDescriptionState;
	
	private JPanel pnlRelation;
	private StartupJLabel lbRelation;
	private JLabel lbRelationState;
	
	private JPanel pnlEntities;
	private StartupJLabel lbEntities;
	private JLabel lbEntitiesState;
	
	private JPanel pnlLayouts;
	private StartupJLabel lbLayouts;
	private JLabel lbLayoutsState;
	
	private JPanel pnlDataImport;
	private StartupJLabel lbDataImport;
	private JLabel lbDataImportState;
	
	private JPanel pnlTotalProcess;
	private StartupJLabel lbTotalProcess;
	private JLabel lbTotalProcessState;
	
	private JPanel pnlSubProcess;
	private StartupJLabel lbSubProcess;
	private JLabel lbSubProcessState;
	
	private JPanel pnlWorkflow;
	private StartupJLabel lbWorkflow;
	private JLabel lbWorkflowState;
	
	private JPanel pnlExplorerTree;
	private StartupJLabel lbExplorerTree;
	private JLabel lbExplorerTreeState;
	
	private JPanel pnlUserRights;
	private StartupJLabel lbUserRights;
	private JLabel lbUserRightsState;
	
	private JPanel pnlRest;
	private StartupJLabel lbRest;
	private JLabel lbRestState;
	
	private JPanel pnlNucletOnline;
	private StartupJLabel lbNucletOnline;
	private JLabel lbNucletOnlineState;
	
	private JCheckBox cbShowOnStartup;
	private JButton btClose;
	
	private String sNuclet;
	private String sUsername;
	
	private URL urlNotOkay;
	private URL urlOkay;
	
	private MainFrameTab iFrame;
	
	private List<StartupJLabel> lstLabel;
	
	private final String strOverviewText = "<html><body><font color=\"#0048BF\"><b>" + 
				getMessage("nuclos.startuppanel.20", "Herzlich willkommen beim Nuclet Wizard") +
				"</b></font></body></html>";
	
	private final String htmlStart = "<html><body><font color=\"#0048BF\"><b>";
	private final String htmlStartOverview = "<html><body><font color=\"#0048BF\"><b><u>";
	private final String htmlMarkedStart = "<html><body><font color=\"#FFFFFF\"><b>";
	private final String htmlEnd = "</b></font></body></html>";
	
	private MainFrame mainFrame;
	
	private ApplicationObserver observer = ApplicationObserver.getInstance();
	
	private URL url01 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/01_Neues_Nuclet.jpg");
	private URL url02 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/02_Entitaeten_Wizard.jpg");
	private URL url03 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/03.Relationen_Wizard.jpg");
	private URL url04 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/04_Layout_Editor.jpg");
	private URL url05 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/05_Daten_Import.jpg");
	private URL url06 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/06_Geschaeftsprozesse.jpg");
	private URL url07 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/07_Teilprozesse.jpg");
	private URL url08 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/08_Workflows.jpg");
	private URL url09 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/09_Baumansichten.jpg");
	private URL url10 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/10_Benutzerrechte.jpg");
	private URL url11 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/11_Nuclets_online.jpg");
	private URL url12 = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/12_Weitere.jpg");
	
	private URL urlNucLogo = this.getClass().getClassLoader().getResource("org/nuclos/client/application/assistant/images/nuclogo.jpg");
	

	public NuclosStartupPanel(MainFrame mf, String sUsername) {
		super();
		mainFrame = mf;
		this.sUsername = sUsername;
		init();		
		loadApplicationState();
		observer.addApplicationAssistantListener(ApplicationObserver.STARTUPPANEL, this);		
		fillMatrix();
		this.grabFocus();
		
	}
	
	public void setParent(MainFrameTab iFrame) {
		this.iFrame = iFrame;
		addInternalFrameListener();
	}
	
	protected void init() {
		StartupKeyAdapter adapter = new StartupKeyAdapter();
		
		double size [][] = {{10,TableLayout.PREFERRED,10, TableLayout.PREFERRED,10, TableLayout.PREFERRED, 10}, {10,TableLayout.PREFERRED, 10, TableLayout.PREFERRED,10,TableLayout.PREFERRED,10,TableLayout.PREFERRED,10, TableLayout.PREFERRED, 10, 25, 5}};
		urlNotOkay = this.getClass().getClassLoader().getResource("org/nuclos/client/ui/images/gtk-no.png");
		urlOkay = this.getClass().getClassLoader().getResource("org/nuclos/client/ui/images/dialog-apply.png");
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		lbLink = new JLabel();
		lbLink.addMouseListener(new OverviewMouseAdapter());
		
		lbSteep = new JLabel(strOverviewText);
		lbSteep.setIcon(new ImageIcon(urlNucLogo));		
		
		double sizeOverview [][] = {{10,TableLayout.PREFERRED,50, 200}, {100}};
		
		pnlOverview = new JPanel();
		pnlOverview.setLayout(new TableLayout(sizeOverview));
		pnlOverview.setBorder(new LineBorder(new Color(0,72,191), 1));
		pnlOverview.add(lbSteep, "1,0");
		pnlOverview.add(lbLink, "3,0");
		pnlOverview.setBackground(Color.WHITE);
		
		lbDescription = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.1", "Erstellen Sie ein <p> neues Nuclet ...") + htmlEnd, new Pair<Integer, Integer>(1,1), "Nuclet");
		lbDescription.setName(getMessage("nuclos.startuppanel.1", "Erstellen Sie ein <p> neues Nuclet ..."));
		lbDescription.addMouseListener(createDescriptionMouseAdapter());
		lbDescription.setIcon(new ImageIcon(url01));
		lbDescription.setBorder(new LineBorder(Color.WHITE));
		lbDescription.setFocusable(true);	
		lbDescription.addKeyListener(adapter);
		
		lbDescriptionState = new JLabel();		
		lbDescriptionState.setIcon(new ImageIcon(urlNotOkay));
		lbDescriptionState.setBorder(new LineBorder(Color.WHITE));
		
		pnlDescription = new JPanel();
		pnlDescription.setLayout(new BorderLayout());
		pnlDescription.add(lbDescription, BorderLayout.CENTER);
		pnlDescription.add(lbDescriptionState, BorderLayout.EAST);
		pnlDescription.setBackground(Color.WHITE);
				
		
		lbRelation = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.2", "Setzen Sie Entit\u00e4ten<p> in Verbindung ...") + htmlEnd, new Pair<Integer, Integer>(3,1), "Relation");
		lbRelation.setName(getMessage("nuclos.startuppanel.2", "Setzen Sie Entit\u00e4ten<p> in Verbindung ..."));
		lbRelation.addMouseListener(createRelationMouseAdapter());
		lbRelation.setIcon(new ImageIcon(url03));
		lbRelation.setBorder(new LineBorder(Color.WHITE));
		lbRelation.addKeyListener(adapter);
		lbRelation.setFocusable(true);
		
		lbRelationState = new JLabel();		
		lbRelationState.setIcon(new ImageIcon(urlNotOkay));
		lbRelationState.setBorder(new LineBorder(Color.WHITE));
		
		pnlRelation = new JPanel();
		pnlRelation.setLayout(new BorderLayout());
		pnlRelation.add(lbRelation, BorderLayout.CENTER);
		pnlRelation.add(lbRelationState, BorderLayout.EAST);
		pnlRelation.setBackground(Color.WHITE);
		
		
		lbEntities = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.3", "Erstellen Sie Entit\u00e4ten<p> mit Hilfe eines Wizard ...") + htmlEnd, new Pair<Integer, Integer>(2,1), "EntityWizard");
		lbEntities.setName(getMessage("nuclos.startuppanel.3", "Erstellen Sie Entit\u00e4ten<p> mit Hilfe eines Wizard ..."));
		lbEntities.addMouseListener(createEntityMouseAdapter());
		lbEntities.setIcon(new ImageIcon(url02));
		lbEntities.setBorder(new LineBorder(Color.WHITE));
		lbEntities.addKeyListener(adapter);
		lbEntities.setFocusable(true);
		
		lbEntitiesState = new JLabel();		
		lbEntitiesState.setIcon(new ImageIcon(urlNotOkay));
		lbEntitiesState.setBorder(new LineBorder(Color.WHITE));
		
		pnlEntities = new JPanel();
		pnlEntities.setLayout(new BorderLayout());
		pnlEntities.add(lbEntities, BorderLayout.CENTER);
		pnlEntities.add(lbEntitiesState, BorderLayout.EAST);
		pnlEntities.setBackground(Color.WHITE);
				
		lbLayouts = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.4", "Erstellen Sie Masken mit <p>Hilfe eines WYSIWYG-Editors ...") + htmlEnd, new Pair<Integer, Integer>(1, 2), "Layout");
		lbLayouts.setName(getMessage("nuclos.startuppanel.4", "Erstellen Sie Masken mit <p>Hilfe eines WYSIWYG-Editors ..."));
		lbLayouts.addMouseListener(createLayoutMouseAdapter());
		lbLayouts.setIcon(new ImageIcon(url04));
		lbLayouts.setBorder(new LineBorder(Color.WHITE));
		lbLayouts.setFocusable(true);
		lbLayouts.addKeyListener(adapter);
		
		lbLayoutsState = new JLabel();		
		lbLayoutsState.setIcon(new ImageIcon(urlNotOkay));
		lbLayoutsState.setBorder(new LineBorder(Color.WHITE));
		
		pnlLayouts = new JPanel();
		pnlLayouts.setLayout(new BorderLayout());
		pnlLayouts.add(lbLayouts, BorderLayout.CENTER);
		pnlLayouts.add(lbLayoutsState, BorderLayout.EAST);
		pnlLayouts.setBackground(Color.WHITE);
		
		lbDataImport = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.5", "Importieren Sie Daten <p>in Ihre Nuclos Applikation ...") + htmlEnd, new Pair<Integer, Integer>(1, 3), "DataImport");
		lbDataImport.setName(getMessage("nuclos.startuppanel.5", "Importieren Sie Daten <p>in Ihre Nuclos Applikation ..."));
		lbDataImport.addMouseListener(createDataImportMouseAdapter());
		lbDataImport.setIcon(new ImageIcon(url05));
		lbDataImport.setBorder(new LineBorder(Color.WHITE));
		lbDataImport.setFocusable(true);
		lbDataImport.addKeyListener(adapter);
		
		lbDataImportState = new JLabel();		
		lbDataImportState.setIcon(new ImageIcon(urlNotOkay));
		lbDataImportState.setBorder(new LineBorder(Color.WHITE));
	
		
		pnlDataImport = new JPanel();
		pnlDataImport.setLayout(new BorderLayout());
		pnlDataImport.add(lbDataImport, BorderLayout.CENTER);
		pnlDataImport.add(lbDataImportState, BorderLayout.EAST);
		pnlDataImport.setBackground(Color.WHITE);
		
		lbTotalProcess = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.6", "Konzipieren Sie Ihren<p> Gesch\u00e4ftsprozess ...") + htmlEnd, new Pair<Integer, Integer>(0, 0), "TotalProcess");
		lbTotalProcess.setName(getMessage("nuclos.startuppanel.6", "Konzipieren Sie Ihren<p> Gesch\u00e4ftsprozess ..."));
		lbTotalProcess.addMouseListener(createTotalProcessMouseAdapter());
		lbTotalProcess.setIcon(new ImageIcon(url06));
		lbTotalProcess.setBorder(new LineBorder(Color.WHITE));
		lbTotalProcess.setFocusable(true);
		lbTotalProcess.addKeyListener(adapter);
		
		lbTotalProcessState = new JLabel();		
		lbTotalProcessState.setIcon(new ImageIcon(urlNotOkay));
		lbTotalProcessState.setBorder(new LineBorder(Color.WHITE));
		
		pnlTotalProcess = new JPanel();
		pnlTotalProcess.setLayout(new BorderLayout());
		pnlTotalProcess.add(lbTotalProcess, BorderLayout.CENTER);
		pnlTotalProcess.add(lbTotalProcessState, BorderLayout.EAST);
		pnlTotalProcess.setBackground(Color.WHITE);
		
		lbSubProcess = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.7", "Konzipieren Sie Teilprozesse  ...") + htmlEnd, new Pair<Integer, Integer>(3, 2), "SubProcess");
		lbSubProcess.setName(getMessage("nuclos.startuppanel.7", "Konzipieren Sie Teilprozesse  ..."));
		lbSubProcess.addMouseListener(createSubProcessMouseAdapter());
		lbSubProcess.setIcon(new ImageIcon(url07));
		lbSubProcess.setBorder(new LineBorder(Color.WHITE));
		lbSubProcess.setFocusable(true);
		lbSubProcess.addKeyListener(adapter);
		
		lbSubProcessState = new JLabel();		
		lbSubProcessState.setIcon(new ImageIcon(urlNotOkay));
		lbSubProcessState.setBorder(new LineBorder(Color.WHITE));
		
		pnlSubProcess = new JPanel();
		pnlSubProcess.setLayout(new BorderLayout());
		pnlSubProcess.add(lbSubProcess, BorderLayout.CENTER);
		pnlSubProcess.add(lbSubProcessState, BorderLayout.EAST);
		pnlSubProcess.setBackground(Color.WHITE);
		
		lbWorkflow = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.8", "Erstellen Sie Ihren Workflow ...") + htmlEnd, new Pair<Integer, Integer>(3, 3), "Workflow");
		lbWorkflow.setName(getMessage("nuclos.startuppanel.8", "Erstellen Sie Ihren Workflow ..."));
		lbWorkflow.addMouseListener(createWorkflowProcessMouseAdapter());
		lbWorkflow.setIcon(new ImageIcon(url08));
		lbWorkflow.setBorder(new LineBorder(Color.WHITE));
		lbWorkflow.setFocusable(true);
		lbWorkflow.addKeyListener(adapter);
		
		lbWorkflowState = new JLabel();		
		lbWorkflowState.setIcon(new ImageIcon(urlNotOkay));
		lbWorkflowState.setBorder(new LineBorder(Color.WHITE));
		
		pnlWorkflow = new JPanel();
		pnlWorkflow.setLayout(new BorderLayout());
		pnlWorkflow.add(lbWorkflow, BorderLayout.CENTER);
		pnlWorkflow.add(lbWorkflowState, BorderLayout.EAST);
		pnlWorkflow.setBackground(Color.WHITE);
		
		lbExplorerTree = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.9", "Erstellen Sie Abl\u00e4ufe <p>in Form von Baumansichten ...") + htmlEnd, new Pair<Integer, Integer>(0, 0), "ExplorerTree");
		lbExplorerTree.setName(getMessage("nuclos.startuppanel.9", "Erstellen Sie Abl\u00e4ufe <p>in Form von Baumansichten ..."));
		lbExplorerTree.addMouseListener(createExplorerTreeProcessMouseAdapter());
		lbExplorerTree.setIcon(new ImageIcon(url09));
		lbExplorerTree.setBorder(new LineBorder(Color.WHITE));
		lbExplorerTree.setFocusable(true);
		lbExplorerTree.addKeyListener(adapter);
		
		lbExplorerTreeState = new JLabel();		
		lbExplorerTreeState.setIcon(new ImageIcon(urlNotOkay));
		lbExplorerTreeState.setBorder(new LineBorder(Color.WHITE));
		
		pnlExplorerTree = new JPanel();
		pnlExplorerTree.setLayout(new BorderLayout());
		pnlExplorerTree.add(lbExplorerTree, BorderLayout.CENTER);
		pnlExplorerTree.add(lbExplorerTreeState, BorderLayout.EAST);
		pnlExplorerTree.setBackground(Color.WHITE);
		
		lbUserRights = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.10", "Vergeben Sie Benutzerrechte ..."), new Pair<Integer, Integer>(2, 2), "UserRights");
		lbUserRights.setName(getMessage("nuclos.startuppanel.10", "Vergeben Sie Benutzerrechte ..."));
		lbUserRights.addMouseListener(createUserRightsMouseAdapter());
		lbUserRights.setIcon(new ImageIcon(url10));
		lbUserRights.setBorder(new LineBorder(Color.WHITE));
		lbUserRights.setFocusable(true);
		lbUserRights.addKeyListener(adapter);
		
		lbUserRightsState = new JLabel();		
		lbUserRightsState.setIcon(new ImageIcon(urlNotOkay));
		lbUserRightsState.setBorder(new LineBorder(Color.WHITE));
		
		pnlUserRights = new JPanel();
		pnlUserRights.setLayout(new BorderLayout());
		pnlUserRights.add(lbUserRights, BorderLayout.CENTER);
		pnlUserRights.add(lbUserRightsState, BorderLayout.EAST);
		pnlUserRights.setBackground(Color.WHITE);
		
		lbRest = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.11", "Weitere M\u00f6glichkeiten ...") + htmlEnd, new Pair<Integer, Integer>(0, 0), "");
		lbRest.setName(getMessage("nuclos.startuppanel.11", "Weitere M\u00f6glichkeiten ..."));
		lbRest.addMouseListener(createRestMouseAdapter());
		lbRest.setIcon(new ImageIcon(url12));
		lbRest.setBorder(new LineBorder(Color.WHITE));
		lbRest.setFocusable(true);
		lbRest.addKeyListener(adapter);
		
		lbRestState = new JLabel();		
		lbRestState.setIcon(new ImageIcon(urlNotOkay));
		lbRestState.setBorder(new LineBorder(Color.WHITE));
		
		pnlRest = new JPanel();
		pnlRest.setLayout(new BorderLayout());
		pnlRest.add(lbRest, BorderLayout.CENTER);
		pnlRest.add(lbRestState, BorderLayout.EAST);
		pnlRest.setBackground(Color.WHITE);
		
		lbNucletOnline = new StartupJLabel(htmlStart + getMessage("nuclos.startuppanel.12", "Nuclets online suchen ...") + htmlEnd, new Pair<Integer, Integer>(2, 3), "");
		lbNucletOnline.setName(getMessage("nuclos.startuppanel.12", "Nuclets online suchen ..."));
		lbNucletOnline.addMouseListener(createNucletOnlineMouseAdapter());
		lbNucletOnline.setIcon(new ImageIcon(url11));
		lbNucletOnline.setBorder(new LineBorder(Color.WHITE));
		lbNucletOnline.setFocusable(true);
		lbNucletOnline.addKeyListener(adapter);
		
		lbNucletOnlineState = new JLabel();		
		lbNucletOnlineState.setIcon(new ImageIcon(urlNotOkay));
		lbNucletOnlineState.setBorder(new LineBorder(Color.WHITE));
		
		pnlNucletOnline = new JPanel();
		pnlNucletOnline.setLayout(new BorderLayout());
		pnlNucletOnline.add(lbNucletOnline, BorderLayout.CENTER);
		pnlNucletOnline.add(lbNucletOnlineState, BorderLayout.EAST);
		pnlNucletOnline.setBackground(Color.WHITE);
		
		cbShowOnStartup = new JCheckBox(getMessage("nuclos.startuppanel.13", "Nuclet Wizard beim Start wieder laden?"));
		cbShowOnStartup.setSelected(true);
		cbShowOnStartup.setBackground(Color.WHITE);
		cbShowOnStartup.setFocusable(false);
		btClose = new JButton(createCloseAction());
		this.setBackground(Color.WHITE);
		
		this.add(pnlDescription, "1,3");	
		this.add(pnlEntities, "3,3");
		this.add(pnlRelation, "5,3");	
		
		this.add(pnlLayouts, "1,5");
		this.add(pnlUserRights, "3,5");
		this.add(pnlSubProcess, "5,5");
		
		this.add(pnlDataImport, "1,7");		
		this.add(pnlNucletOnline, "3,7");
		this.add(pnlWorkflow, "5,7");
		
		this.add(pnlOverview, "1,1, 5,1");
		
		
//		this.add(pnlDescription, "1,3");
//		this.add(pnlEntities, "1,5");
//		this.add(pnlRelation, "1,7");
//		this.add(pnlLayouts, "3,3");
//		this.add(pnlDataImport, "3,5");
//		this.add(pnlTotalProcess, "3,7");
//		this.add(pnlSubProcess, "5,3");
//		this.add(pnlWorkflow, "5,5");
//		this.add(pnlExplorerTree, "5,7");
//		this.add(pnlUserRights, "1,9");
//		this.add(pnlRest, "3,9");
//		this.add(pnlNucletOnline, "5,9");
		
		this.add(cbShowOnStartup, "1,11");
		this.add(btClose, "5,11");
		selectFirstEntry();
		
				
		
	}
	
	protected void fillMatrix() {
		lstLabel = new ArrayList<StartupJLabel>();
		lstLabel.add(lbDescription);
		lstLabel.add(lbEntities);
		lstLabel.add(lbRelation);
		lstLabel.add(lbLayouts);
		lstLabel.add(lbUserRights);
		lstLabel.add(lbSubProcess);
		lstLabel.add(lbDataImport);
		lstLabel.add(lbNucletOnline);
		lstLabel.add(lbWorkflow);
	}
	
	protected void selectFirstEntry()  {
		clearSymbols();
		
		lbDescription.setOpaque(true);
		lbDescription.setBorder(new LineBorder(new Color(0,72,191)));
		lbDescription.setBackground(new Color(0,72,191));
		lbDescription.setText(htmlMarkedStart + lbDescription.getName() + htmlEnd);
		
		lbLink.setText(htmlStartOverview + lbDescription.getName() + htmlEnd);
		lbLink.setName("Nuclet");
	}
	
	protected void clearSymbols() {
		lbDescription.setText(htmlStart + lbDescription.getName() + htmlEnd);
		lbDescription.setBorder(new LineBorder(Color.WHITE));
		lbDescription.setBackground(Color.WHITE);
		
		lbRelation.setText(htmlStart + lbRelation.getName() + htmlEnd);
		lbRelation.setBorder(new LineBorder(Color.WHITE));
		lbRelation.setBackground(Color.WHITE);
		
		lbEntities.setText(htmlStart + lbEntities.getName() + htmlEnd);
		lbEntities.setBorder(new LineBorder(Color.WHITE));
		lbEntities.setBackground(Color.WHITE);
		
		lbLayouts.setText(htmlStart + lbLayouts.getName() + htmlEnd);
		lbLayouts.setBorder(new LineBorder(Color.WHITE));
		lbLayouts.setBackground(Color.WHITE);
		
		lbTotalProcess.setText(htmlStart + lbTotalProcess.getName() + htmlEnd);
		lbTotalProcess.setBorder(new LineBorder(Color.WHITE));
		lbTotalProcess.setBackground(Color.WHITE);
		
		lbSubProcess.setText(htmlStart + lbSubProcess.getName() + htmlEnd);
		lbSubProcess.setBorder(new LineBorder(Color.WHITE));
		lbSubProcess.setBackground(Color.WHITE);
		
		lbWorkflow.setText(htmlStart + lbWorkflow.getName() + htmlEnd);
		lbWorkflow.setBorder(new LineBorder(Color.WHITE));
		lbWorkflow.setBackground(Color.WHITE);
		
		lbExplorerTree.setText(htmlStart + lbExplorerTree.getName() + htmlEnd);
		lbExplorerTree.setBorder(new LineBorder(Color.WHITE));
		lbExplorerTree.setBackground(Color.WHITE);
		
		lbUserRights.setText(htmlStart + lbUserRights.getName() + htmlEnd);
		lbUserRights.setBorder(new LineBorder(Color.WHITE));
		lbUserRights.setBackground(Color.WHITE);
		
		lbRest.setText(htmlStart + lbRest.getName() + htmlEnd);
		lbRest.setBorder(new LineBorder(Color.WHITE));
		lbRest.setBackground(Color.WHITE);
		
		lbNucletOnline.setText(htmlStart + lbNucletOnline.getName() + htmlEnd);
		lbNucletOnline.setBorder(new LineBorder(Color.WHITE));
		lbNucletOnline.setBackground(Color.WHITE);
		
		lbDataImport.setText(htmlStart + lbDataImport.getName() + htmlEnd);
		lbDataImport.setBorder(new LineBorder(Color.WHITE));
		lbDataImport.setBackground(Color.WHITE);
		
		
	}
	
	protected void addInternalFrameListener() {
		iFrame.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				writeToPreferences();
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});
	}
	
	public void writeToPreferences() {
		Preferences pref = ClientPreferences.getUserPreferences();
		
		Preferences prefNuclet = pref.node("nuclet");
		
		prefNuclet.put("loadonstart", String.valueOf(this.cbShowOnStartup.isSelected()));
	}
	
	protected void loadApplicationState() {
		loadNucletInformation();
		loadEntities();		
		loadRelationship();
		loadLayouts();
		loadSubProcessInformation();
		loadWorkflowInformation();
		loadExplorerTreeInformation();
	}
	
	protected void loadExplorerTreeInformation() {
		int iExplorerTreeCounter = 0;
					
		for (EntityMetaDataVO eMeta : MetaDataClientProvider.getInstance().getAllEntities()) {
			if (eMeta.isTreeRelation()) {
				iExplorerTreeCounter++;
			}
		}
		if(iExplorerTreeCounter > 0) {
			lbExplorerTreeState.setIcon(new ImageIcon(urlOkay));
		}
		else {
			lbExplorerTreeState.setIcon(new ImageIcon(urlNotOkay));
		}
	}
	
	protected void loadWorkflowInformation() {
		try {			
			if(MasterDataCache.getInstance().get(NuclosEntity.SEARCHFILTER.getEntityName()).isEmpty()) 
				lbWorkflowState.setIcon(new ImageIcon(urlNotOkay));
			else
				lbWorkflowState.setIcon(new ImageIcon(urlOkay));
		}
		catch(CommonFinderException e) {
			LOG.info("loadWorkflowInformation failed: " + e);
		}
	}
	
	protected void loadSubProcessInformation() {
		try {			
			if(MasterDataCache.getInstance().get(NuclosEntity.STATEMODEL.getEntityName()).isEmpty())
				lbSubProcessState.setIcon(new ImageIcon(urlNotOkay));
			else
				lbSubProcessState.setIcon(new ImageIcon(urlOkay));
		}
		catch(CommonFinderException e) {
			LOG.info("loadSubProcessInformation failed: " + e);
		}
	}
	
	protected void loadNucletInformation() {
		try {
			sNuclet = null;
			for(MasterDataVO vo : MasterDataCache.getInstance().get(NuclosEntity.NUCLET.getEntityName())) {
				this.sNuclet = (String)vo.getField("name");				
				lbDescriptionState.setIcon(new ImageIcon(urlOkay));
				return;
			}
			lbDescriptionState.setIcon(new ImageIcon(urlNotOkay));
		}
		catch(CommonFinderException e) {
			sNuclet = null;
			LOG.info("loadNucletInformation failed: " + e);
		}
	}
	
	protected void loadEntities() {
		int iEntityCounter = 0;
		for(EntityMetaDataVO metaVO : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(metaVO.getEntity().startsWith("nuclos_"))
				continue;
			iEntityCounter++;
		}
		if(iEntityCounter > 0) {
			lbEntitiesState.setIcon(new ImageIcon(urlOkay));
		}
		else {
			lbEntitiesState.setIcon(new ImageIcon(urlNotOkay));
		}
	}
	
	protected void loadRelationship() {
		try {
			if(MasterDataCache.getInstance().get(NuclosEntity.ENTITYRELATION.getEntityName()).isEmpty())
				lbRelationState.setIcon(new ImageIcon(urlNotOkay));
			else
				lbRelationState.setIcon(new ImageIcon(urlOkay));
		}
		catch(CommonFinderException e) {
			LOG.warn("loadRelationship failed: " + e, e);
		}		
	}
	
	protected void loadLayouts() {
		int iLayoutCounter = 0;
		try {
			for(MasterDataVO vo : MasterDataCache.getInstance().get(NuclosEntity.LAYOUT.getEntityName())) {
				if(vo.isSystemRecord())
					continue;
				String name = (String)vo.getField("name");
				if(name.equals("Allgemeine Suche") || name.equals("Test-Modul")) 
					continue;
				iLayoutCounter++;
			}
			if(iLayoutCounter > 0) {
				lbLayoutsState.setIcon(new ImageIcon(urlOkay));
			}	
			else {
				lbLayoutsState.setIcon(new ImageIcon(urlNotOkay));
			}
		}
		catch(CommonFinderException e) {
			LOG.warn("loadLayouts failed: " + e, e);
		}		
	}
	
	
	private MouseAdapter createDescriptionMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbDescription.getName() + htmlEnd);
					
					lbLink.setText(htmlStartOverview + lbDescription.getName() + htmlEnd);
					lbLink.setName("Nuclet");
					
				}
				else if(e.getClickCount() == 2) {
					openNucletController();
				}					
			}			
			
		};
	}
	
	private MouseAdapter createRelationMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbRelation.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbRelation.getName() + htmlEnd);
					lbLink.setName("Relation");
				}
				else if(e.getClickCount() == 2) {
					openRelationShipController();
				}
			}			
			
		};
	}
	
	private MouseAdapter createEntityMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbEntities.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbEntities.getName() + htmlEnd);
					lbLink.setName("EntityWizard");
				}
				else if(e.getClickCount() == 2) {
					openWizard();
				}
			}			
			
		};
	}
	
	private MouseAdapter createLayoutMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbLayouts.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbLayouts.getName() + htmlEnd);
					lbLink.setName("Layout");
				}
				else if(e.getClickCount() == 2) {
					openLayoutController();
				}
			}			
			
		};
	}
	
	private MouseAdapter createDataImportMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {			
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart +  lbDataImport.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbDataImport.getName() + htmlEnd);
					lbLink.setName("DataImport");
				}
				else if(e.getClickCount() == 2) {
					openDataimportController();
				}
			}			
			
		};
	}
	
	private MouseAdapter createTotalProcessMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clearSymbols();
				mouseLabelEntered(e, htmlMarkedStart +  lbTotalProcess.getName() + htmlEnd);
				lbLink.setText(htmlStartOverview + lbTotalProcess.getName() + htmlEnd);
				lbLink.setName("TotalProcess");				
			}			
			
		};
	}
	
	private MouseAdapter createSubProcessMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbSubProcess.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbSubProcess.getName() + htmlEnd);
					lbLink.setName("SubProcess");
				}
				else if(e.getClickCount() == 2) {
					openStatemodelController();
				}
				else {
					
				}
			}
		};
	}
	
	private MouseAdapter createWorkflowProcessMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbWorkflow.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbWorkflow.getName() + htmlEnd);
					lbLink.setName("Workflow");
				}
				else if(e.getClickCount() == 2) {
					openWorkflowWizard();
				}
			}			
			
		};
	}
	
	private MouseAdapter createExplorerTreeProcessMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbExplorerTree.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbExplorerTree.getName() + htmlEnd);
					lbLink.setName("ExplorerTree");
				}
				else if (e.getClickCount() == 2) {
					openExplorerProzessWizard();
				}
			}		
			
		};
	}
	
	private MouseAdapter createUserRightsMouseAdapter () {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if(e.getClickCount() == 1) {
					clearSymbols();
					mouseLabelEntered(e, htmlMarkedStart + lbUserRights.getName() + htmlEnd);
					lbLink.setText(htmlStartOverview + lbUserRights.getName() + htmlEnd);
					lbLink.setName("UserRights");
				}
				else if(e.getClickCount() == 2) {
					openUserRightsController();
				}
			}			
			
		};
	}
	
	private MouseAdapter createRestMouseAdapter () {
		return new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				clearSymbols();
				mouseLabelEntered(e, htmlMarkedStart + lbRest.getName() + htmlEnd);
				lbLink.setText(htmlStartOverview + lbRest.getName() + htmlEnd);
				lbLink.setName("More");
			}	
						
		};
	}

	private MouseAdapter createNucletOnlineMouseAdapter () {
		return new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				clearSymbols();
				mouseLabelEntered(e, htmlMarkedStart + lbNucletOnline.getName() + htmlEnd);
				lbLink.setText(htmlStartOverview + lbNucletOnline.getName() + htmlEnd);
				lbLink.setName("NucletsOnline");
			}	

		};
	}
	
	
	private void setWaitCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void setDefaultCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	private AbstractAction createCloseAction() {
		return new AbstractAction(getMessage("nuclos.startuppanel.19", "Assistent schlie\u00dfen")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(iFrame != null) {
					NuclosStartupPanel.this.setVisible(false);
					iFrame.dispose();
				}
			}
		};
	}

	@Override
	public void applicationChanged(ApplicationChangedEvent event) {
		NuclosEntity entity = NuclosEntity.getByName(event.getChangeText());
		if (entity != null) {
			switch (entity) {
			case LAYOUT:
				loadLayouts(); break;
			case NUCLET:
				loadNucletInformation(); break;
			case STATEMODEL:
				loadSubProcessInformation(); break;
			case SEARCHFILTER:
				loadWorkflowInformation(); break;
			}
		}
	}
	
	protected void focusLabel(StartupJLabel label) {
		label.setOpaque(true);
		label.setBorder(new LineBorder(new Color(0,72,191)));
		label.setBackground(new Color(0,72,191));
		label.setText(htmlMarkedStart + label.getName() + htmlEnd);
		label.grabFocus();
		lbLink.setText(htmlStartOverview + label.getName() + htmlEnd);
		lbLink.setName(label.getLinkName());
		
	}
	
	protected void mouseLabelEntered(MouseEvent e, String text) {
		JLabel label = (JLabel)e.getSource();
		label.setOpaque(true);
		label.setBorder(new LineBorder(new Color(0,72,191)));
		label.setBackground(new Color(0,72,191));
		label.setText(htmlMarkedStart + text + htmlEnd);
		label.grabFocus();
	}
	
	protected void openNucletController() {
		try {
			setWaitCursor();						
			if(sNuclet != null) {
				MasterDataVO voApplication = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName()).iterator().next();
				MasterDataWithDependantsVO voa = new MasterDataWithDependantsVO(voApplication, null);
				CollectableMasterDataEntity entity = new CollectableMasterDataEntity(MasterDataDelegate.getInstance().getMetaData(NuclosEntity.NUCLET.getEntityName()));
				CollectableMasterDataWithDependants clct = new CollectableMasterDataWithDependants(entity, voa);
				NuclosCollectController controller = NuclosCollectControllerFactory.getInstance().newCollectController(mainFrame.getHomePane(), NuclosEntity.NUCLET.getEntityName(), null);
				controller.runViewSingleCollectableWithId(voApplication.getId());
			}
			else {
				NuclosCollectControllerFactory.getInstance().newCollectController(mainFrame.getHomePane(), NuclosEntity.NUCLET.getEntityName(), null).runNew();
			}
		}
		catch(CommonBusinessException e) {
			LOG.warn("openNucletController failed: " + e, e);
		}						
		finally {
			setDefaultCursor();
		}			
		
	}
	
	protected void openLayoutController() {
		try {
			setWaitCursor();
			NuclosCollectControllerFactory.getInstance().newCollectController(mainFrame.getHomePane(), NuclosEntity.LAYOUT.getEntityName(), null).run();
		}
		catch(CommonBusinessException e) {
			LOG.warn("openLayoutController failed: " + e);
		}						
		finally {
			setDefaultCursor();
		}			
		
	}
	
	protected void openDataimportController() {
		try {
			setWaitCursor();
			NuclosCollectControllerFactory.getInstance().newCollectController(mainFrame.getHomePane(), NuclosEntity.IMPORT.getEntityName(), null).run();
		}
		catch(CommonBusinessException e) {
			LOG.warn("openDataimportController failed: " + e);
		}						
		finally {
			setDefaultCursor();
		}			
		
	}
	
	protected void openRelationShipController() {
		try {
			setWaitCursor();
			final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
			EntityRelationShipCollectController result = factory.newEntityRelationShipCollectController(mainFrame.getHomePane(), mainFrame, null);
			result.run();
			
		}
		catch(CommonBusinessException e) {
			LOG.warn("openRelationShipController failed: " + e);
		}						
		finally {
			setDefaultCursor();
		}			
	}
	
	protected void openUserRightsController() {
		try {
			setWaitCursor();					
			MasterDataCollectController mdcc = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(mainFrame.getHomePane(), NuclosEntity.ROLE.getEntityName(), null);
			mdcc.run();
		}
		catch(CommonBusinessException e) {
			LOG.warn("openUserRightsController failed: " + e);
		}						
		finally {
			setDefaultCursor();
		}			
	}
	
	protected void openStatemodelController() {
		try {
			setWaitCursor();
			NuclosCollectControllerFactory.getInstance().newCollectController(mainFrame.getHomePane(), NuclosEntity.STATEMODEL.getEntityName(), null).run();
		}
		catch(CommonBusinessException e) {
			LOG.warn("openStatemodelController failed: " + e);
		}						
		finally {
			setDefaultCursor();
		}					
	}

	protected void openWizard() {
		setWaitCursor();
		ShowNuclosWizard w = new ShowNuclosWizard(false);		
		w.showWizard(mainFrame.getHomePane(), mainFrame);
		setDefaultCursor();
	}

	protected void openExplorerProzessWizard() {
		setWaitCursor();
		try {
			ProzessWizardPanel panel = new ProzessWizardPanel();
			Collection<MasterDataVO> col = panel.buildTrees();
			if(col.size() > 0) {
				JOptionPane pane = new JOptionPane(getMessage("nuclos.startuppanel.14", "Explorerb\u00e4ume wurden generiert."), JOptionPane.DEFAULT_OPTION);
				JDialog dia = pane.createDialog(this, getMessage("nuclos.startuppanel.15", "Explorerb\u00e4ume generiert."));
				dia.setVisible(true);				
			}
			else {
				JOptionPane pane = new JOptionPane(getMessage("nuclos.startuppanel.16", "Bitte legen zun\u00e4chst Entit\u00e4ten an."), JOptionPane.DEFAULT_OPTION);
				JDialog dia = pane.createDialog(this, getMessage("nuclos.startuppanel.15", "Explorerb\u00e4ume generiert."));
				dia.setVisible(true);
			}
		}
		catch (Exception e) {
			LOG.warn("openStatemodelController failed: " + e, e);
		}
		finally {
			setDefaultCursor();
		}
	}
	
	protected void openWorkflowWizard() {
		setWaitCursor();
		try {
			WorkflowWizardPanel panel = new WorkflowWizardPanel(sUsername);
			panel.buildSearchFilter();
			
			JOptionPane pane = new JOptionPane(getMessage("nuclos.startuppanel.17", "Aufgabenlisten wurden generiert."), JOptionPane.DEFAULT_OPTION);
			JDialog dia = pane.createDialog(this, getMessage("nuclos.startuppanel.18", "Aufgabenlisten generiert."));
			dia.setVisible(true);
						
			NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(mainFrame.getHomePane(), NuclosEntity.SEARCHFILTER.getEntityName(), null).runViewAll();
		}
		catch (Exception e) {
			LOG.warn("openWorkflowWizard failed: " + e, e);
		}
		finally {
			loadWorkflowInformation();
			setDefaultCursor();
		}
	}

	class OverviewMouseAdapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			JLabel lb = (JLabel)e.getSource();
			openItem(lb);			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			JLabel lb = (JLabel)e.getSource();
			if(lb.getText().length() > 0)
				lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JLabel lb = (JLabel)e.getSource();
			lb.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
	}
	
	private void openItem(StartupJLabel lb) {
		if("Nuclet".equals(lb.getLinkName())) {
			openNucletController();
		}
		else if("Relation".equals(lb.getLinkName())) {
			openRelationShipController();
		}
		else if("EntityWizard".equals(lb.getLinkName())) {
			openWizard();
		}
		else if("Layout".equals(lb.getLinkName())) {
			openLayoutController();
		}
		else if("DataImport".equals(lb.getLinkName())) {
			openDataimportController();
		}
		else if("TotalProcess".equals(lb.getLinkName())) {
			
		}
		else if("SubProcess".equals(lb.getLinkName())) {
			openStatemodelController();
		}
		else if("Workflow".equals(lb.getLinkName())) {
			openWorkflowWizard();
		}
		else if("ExplorerTree".equals(lb.getLinkName())) {
			openExplorerProzessWizard();
		}
		else if("UserRights".equals(lb.getLinkName())) {
			openUserRightsController();
		}
	}
	
	private void openItem(JLabel lb) {
		if("Nuclet".equals(lb.getName())) {
			openNucletController();
		}
		else if("Relation".equals(lb.getName())) {
			openRelationShipController();
		}
		else if("EntityWizard".equals(lb.getName())) {
			openWizard();
		}
		else if("Layout".equals(lb.getName())) {
			openLayoutController();
		}
		else if("DataImport".equals(lb.getName())) {
			openDataimportController();
		}
		else if("TotalProcess".equals(lb.getName())) {
			
		}
		else if("SubProcess".equals(lb.getName())) {
			openStatemodelController();
		}
		else if("Workflow".equals(lb.getName())) {
			openWorkflowWizard();
		}
		else if("ExplorerTree".equals(lb.getName())) {
			openExplorerProzessWizard();
		}
		else if("UserRights".equals(lb.getName())) {
			openUserRightsController();
		}
	}
	
	private StartupJLabel getLabel(Pair<Integer, Integer> pair) {
		StartupJLabel lb = null;
		for(StartupJLabel label : lstLabel)	 {
			if(pair.equals(label.getPosition())){
				lb = label;				
				break;
			}
		}
		
		return lb;
	}
	
	class StartupKeyAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if(!(e.getSource() instanceof StartupJLabel))
				return;
			
			StartupJLabel lb = (StartupJLabel)e.getSource();
			Pair<Integer, Integer> xy = lb.getPosition();
			
			if(xy == null)
				return;
			Pair<Integer, Integer> yx = new Pair<Integer, Integer>(xy.getX(), xy.getY());
			final StartupJLabel label;
			int y;
			int x;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				label = getLabel(yx);
				openItem(label);
				return;
			case KeyEvent.VK_DOWN:
				y = yx.getY();
				y++;
				if(y > 3)
					return;
								
				yx.setY(y);
				label = getLabel(yx);
				break;
			case KeyEvent.VK_UP:
				y = yx.getY();
				y--;
				if(y < 1)
					return;
				yx.setY(y);
				label = getLabel(yx);
				break;
			case KeyEvent.VK_LEFT:
				x = yx.getX();
				x--;
				if(x < 1)
					return;
				yx.setX(x);
				label = getLabel(yx);
				break;
			case KeyEvent.VK_RIGHT:
				x = yx.getX();
				x++;
				if(x > 3)
					return;
				yx.setX(x);
				label = getLabel(yx);
				break;

			default:
				label = null;
				return;
			}
			if(label != null) {
				clearSymbols();
				focusLabel(label);
			}
		}
		
	}
	
	class StartupJLabel extends JLabel {
		
		private Pair<Integer, Integer> xyPosition;
		private String sLinkName;

		public StartupJLabel(String text, Pair<Integer, Integer> position, String linkName) {
			super(text);			
			this.xyPosition = position;
			this.sLinkName = linkName;
		}
		
		public Pair<Integer, Integer> getPosition() {
			return xyPosition;
		}
		
		public String getLinkName() {
			return sLinkName;
		}
		
	}

}
