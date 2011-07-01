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
package org.nuclos.tools.translation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.nuclos.tools.translation.TranslationFilter.FilterType;
import org.nuclos.tools.translation.translationdata.TranslationData;
import org.nuclos.tools.translation.translationdata.TranslationDataTable;
import org.nuclos.tools.translation.translationdata.TranslationDataTableModel;
import org.nuclos.tools.translation.translationdata.TranslationData.TranslationAction;


@SuppressWarnings("serial")
public class TranslationMainFrame extends JFrame implements MouseListener, ActionListener, TableModelListener {

	private JPanel mainPanel;
	private JLabel pSourceFilePath;
	private JTextField tfSourceFilePath;
	private JButton btnBrowse;
	private JLabel pBackupFilePath;
	private JTextField tfBackupFilePath;
	private JButton btnBackupBrowse;
	private JButton btnAnalyse;
	private JButton btnStartActions;
	private JButton btnRollbackActions;
	private JScrollPane scrollPane;

	private TranslationDataTableModel translationModel;
	private TranslationDataTable translationTable;

	private TranslationHelper helper;

	private Map<TranslationFilter.FilterType,Set<String>> filters = new HashMap<TranslationFilter.FilterType,Set<String>>();

	private TranslationData lastSelectedTD;
	private TranslationData lastSelectedTDCopy;
	
	private volatile boolean analysing = false;

	public TranslationMainFrame() {
	   super("Code Internationalizer");
	   setSize(1280, 800);
	   setMinimumSize(new Dimension(1024,768));
	   setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

	   mainPanel = new JPanel(new GridBagLayout());
	   mainPanel.setOpaque(true);
	   getContentPane().add(mainPanel);

	   initBrowseSection();
	   initBackupSection();
	   initAnalyseSection();
	   initTable();
	   initFileEditingSection();

	   setVisible(true);

	   helper = new TranslationHelper();
	}

	private void initBrowseSection() {
		GridBagConstraints gbc = gridBagConstraints(0.0, 0.0, 1, 1, 0, 0, new Insets(10, 10, 0, 10));
		pSourceFilePath = new JLabel("Quelldatei / -verzeichnis");
		mainPanel.add(pSourceFilePath,gbc);

		gbc = gridBagConstraints(1.0, 0.0, 1, 1, 0, 1, new Insets(5, 10, 10, 10));
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tfSourceFilePath = new JTextField("C:\\dev\\nucleus\\src\\java");
		mainPanel.add(tfSourceFilePath,gbc);

		Action browseAction = new AbstractAction("") {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseSourceFiles();
			}
		};
		browseAction.putValue(Action.SHORT_DESCRIPTION, String.format("Quellverzeichnis oder -datei ausw\u00e4hlen", ""));

		btnBrowse = new JButton(browseAction);
		btnBrowse.setText("Browse");
		btnBrowse.setSize(50,18);

		gbc = gridBagConstraints(0.0, 0.0, 1, 1, 1, 1, new Insets(5, 10, 10, 10));
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(btnBrowse,gbc);
	}

	private void initBackupSection() {
		GridBagConstraints gbc = gridBagConstraints(0.0, 0.0, 1, 1, 0, 2, new Insets(10, 10, 0, 10));
		pBackupFilePath = new JLabel("Backup-Verzeichnis");
		mainPanel.add(pBackupFilePath, gbc);

		gbc = gridBagConstraints(1.0, 0.0, 1, 1, 0, 3, new Insets(5, 10, 10, 10));
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tfBackupFilePath = new JTextField("C:\\backup");
		mainPanel.add(tfBackupFilePath, gbc);

		Action browseAction = new AbstractAction("") {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseSourceFiles();
			}
		};
		browseAction.putValue(Action.SHORT_DESCRIPTION, String.format("Backup-Verzeichnis ausw\u00e4hlen", ""));

		btnBackupBrowse = new JButton(browseAction);
		btnBackupBrowse.setText("Browse");
		btnBackupBrowse.setSize(50, 18);

		gbc = gridBagConstraints(0.0, 0.0, 1, 1, 1, 3, new Insets(5, 10, 10, 10));
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(btnBackupBrowse, gbc);
	}

	private void initTable() {
		translationModel = new TranslationDataTableModel();

		translationTable = new TranslationDataTable(translationModel);
		translationTable.addMouseListener(this);
		//translationTable.addPropertyChangeListener(this);
		translationTable.addActionColumnListener(this);
		translationTable.getModel().addTableModelListener(this);
		
		GridBagConstraints gbc = gridBagConstraints(1.0, 1.0, 1, 2, 0, 5, new Insets(10, 10, 10, 10));
		gbc.fill = GridBagConstraints.BOTH;
		scrollPane = new JScrollPane(translationTable);

		mainPanel.add(scrollPane, gbc);
		
		translationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) { doPop(e); }
			@Override
			public void mousePressed(MouseEvent e) { doPop(e); }
			@Override
			public void mouseClicked(MouseEvent e) { doPop(e); }
			
			private void doPop(MouseEvent e) {
				if(e.isPopupTrigger()) {
					JPopupMenu m = new JPopupMenu();
					m.add(new AbstractAction("Select") {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(int i : translationTable.getSelectedRows())
								translationTable.setValueAt(Boolean.TRUE, i, 0);
						}
					});
					m.add(new AbstractAction("Deselect") {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(int i : translationTable.getSelectedRows())
								translationTable.setValueAt(Boolean.FALSE, i, 0);
						}
					});
					m.show(translationTable, e.getX(), e.getY());
				}
			}
		});
		
	}

	private void initAnalyseSection() {
		Action analyseAction = new AbstractAction("") {
			@Override
			public void actionPerformed(ActionEvent e) {
				startAnalysing();
			}
		};
		analyseAction.putValue(Action.SHORT_DESCRIPTION, String.format("Analyse starten", ""));

		btnAnalyse = new JButton(analyseAction);
		btnAnalyse.setText("Analyse starten");
		btnAnalyse.setSize(50, 18);
		btnAnalyse.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints gbc = gridBagConstraints(0.0, 0.0, 1, 2, 0, 4, new Insets(10, 10, 10, 10));
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(btnAnalyse, gbc);
	}

	private void initFileEditingSection() {
		Action startAction = new AbstractAction("") {
			@Override
			public void actionPerformed(ActionEvent e) {
				performSelectedActions();
			}
		};
		startAction.putValue(Action.SHORT_DESCRIPTION, String.format("ausgew\u00e4hlte Aktionen durchf\u00fchren", ""));

		btnStartActions = new JButton(startAction);
		btnStartActions.setText("Aktionen durchf\u00fchren");
		btnStartActions.setSize(50, 18);
		// btnStartActions.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints gbc = gridBagConstraints(0.0, 0.0, 1, 1, 0, 6,
				new Insets(10, 10, 10, 10));
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(btnStartActions, gbc);

		Action rollbackAction = new AbstractAction("") {
			@Override
			public void actionPerformed(ActionEvent e) {
				rollbackSelectedActions();
			}
		};
		rollbackAction.putValue(Action.SHORT_DESCRIPTION, String.format(
				"ausgew\u00e4hlte Aktionen r\u00fcckg\u00e4ngig machen", ""));

		btnRollbackActions = new JButton(rollbackAction);
		btnRollbackActions.setText("Aktionen zur\u00fccknehmen");
		btnRollbackActions.setSize(50, 18);
		// btnRollbackActions.setHorizontalAlignment(SwingConstants.CENTER);

		gbc = gridBagConstraints(0.0, 0.0, 1, 1, 1, 6, new Insets(10, 10, 10,
				10));
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(btnRollbackActions, gbc);
	}

	public void setTranslationData(List<TranslationData> data) {
		translationModel.setData(data);
		translationTable.repaint();
	}

	private void chooseSourceFiles() {
	   JFileChooser fileChooser = new JFileChooser("C:\\nucleus");
	   fileChooser.setFileFilter(new FileNameExtensionFilter("Java Dateien (*.java)", "java"));
	   fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	   fileChooser.setDialogTitle("Quellverzeichnis oder -datei ausw\u00e4hlen");
	   if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		   String fileName = fileChooser.getSelectedFile().getPath();

		   if (fileName != null)
			   tfSourceFilePath.setText(fileName);
	   }
	}

	private void loadFilters() {
		for (FilterType type : TranslationFilter.FilterType.values()) {
			filters.put(type, TranslationFilter.getFilters(type));
		}
	}

	private void startAnalysing() {
		analysing = true;
		try {
			loadFilters();
			List<TranslationData> dataList = helper.analysePaths(tfSourceFilePath.getText(), filters);

			for (Iterator<TranslationData> it = dataList.iterator(); it.hasNext();)
				if (!it.next().isVisible())
					it.remove();

			setTranslationData(dataList);

		} catch (Exception e) {}
		finally {
			analysing = false;
		}
	}

	private void performSelectedActions() {
		new TranslationDataParser(translationModel.getData(), helper.getFileData(), tfSourceFilePath.getText(), tfBackupFilePath.getText()).startParsing();
		startAnalysing();
	}

	private void rollbackSelectedActions() {
		new TranslationDataParser(translationModel.getData(), helper.getFileData(), tfSourceFilePath.getText(), tfBackupFilePath.getText()).startRollback();
	}

	public static void main(String[] args) {
		try {
			setLookAndFeel();
			new TranslationMainFrame();
		}
		catch(Exception e) {
			System.err.println("exception: "+e);
			e.printStackTrace(System.err);   // so we can get stack trace
		}
	}

	private static void setLookAndFeel() {
		try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	    }
	    catch (UnsupportedLookAndFeelException e) {}
	    catch (ClassNotFoundException e) {}
	    catch (InstantiationException e) {}
	    catch (IllegalAccessException e) {}
	}

	public static GridBagConstraints gridBagConstraints(double weightx,
		double weighty, int gridheight, int gridwidth, int gridx,
		int gridy, Insets insets) {
		GridBagConstraints result = new GridBagConstraints();
		result.weightx = weightx;
		result.weighty = weighty;
		result.anchor = GridBagConstraints.NORTHWEST;
		result.fill = GridBagConstraints.HORIZONTAL;
		result.gridheight = gridheight;
		result.gridwidth = gridwidth;
		result.gridx = gridx;
		result.gridy = gridy;
		result.insets = insets == null ? new Insets(0, 0, 0, 0) : insets;
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final List<TranslationData> tdList = translationTable.getSelectedTranslationData();

		if (!tdList.isEmpty()) {
			for ( TranslationData td : tdList) {
				if (td.getAction().equals(TranslationAction.NEW_ID) && td.getPerform())
					helper.setResourceId(td);
				else if (td.getAction().equals(TranslationAction.NEW_ID))
					helper.deleteResourceId(td);
			}

			refreshTable();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && translationTable.getSelectedRowCount() == 1) {
			for ( TranslationData td : translationTable.getSelectedTranslationData()) {
				ProcessBuilder builder = new ProcessBuilder("cmd", "/K", td.getFilePath());
				// ProcessBuilder builder = new ProcessBuilder( "cmd", "cedt.exe",
				// "/L:"+translationTable.getSelectedTranslationData().getLineNumber(),
				// filePath );
				try {
					builder.start();
				} catch (IOException e1) {
					System.err.println("Datei " + td.getFilePath() + " kann nicht ge\u00f6ffnet werden.");
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastSelectedTD = null;
		lastSelectedTDCopy = null;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (translationTable.getSelectedRowCount() == 1 && lastSelectedTDCopy == null) {
			lastSelectedTD = translationTable.getSelectedTranslationData().iterator().next();
			lastSelectedTDCopy = lastSelectedTD.copy();
		}
	}

//	public void propertyChange(PropertyChangeEvent evt) {
//		final List<TranslationData> tdList = translationTable.getSelectedTranslationData();
//
//		if (!tdList.isEmpty()) {
//			for ( TranslationData td : tdList) {
//				System.out.println("perform validatePerform: " + (td.getPerform() ? "true" : "false"));
//				if (td.getPerform()) {
//					if (td.getAction().equals(TranslationAction.NEW_ID))
//						helper.deleteResourceId(td);
//					td.setPerform(false);
//				}
//				else {
//					if (td.getAction().equals(TranslationAction.NEW_ID))
//						helper.setResourceId(td);
//					td.setPerform(true);
//				}
//			}
//			lastSelectedTDCopy = new TranslationData();
//			lastSelectedTDCopy.setVisible(false);
//		}
//		else if (lastSelectedTDCopy != null && lastSelectedTDCopy.isVisible()) {
//			if (lastSelectedTDCopy.getPerform()) {
//				if (lastSelectedTDCopy.getAction().equals(TranslationAction.NEW_ID))
//					helper.deleteResourceId(lastSelectedTD);
//				lastSelectedTD.setPerform(false);
//			}
//			else {
//				if (lastSelectedTDCopy.getAction().equals(TranslationAction.NEW_ID))
//					helper.setResourceId(lastSelectedTD);
//				lastSelectedTD.setPerform(true);
//			}
//			lastSelectedTDCopy = null;
//		}
//		refreshTable();
//	}

	private void refreshTable() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				translationTable.repaint();
			}
		});
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if(analysing)
			return;
		if(e.getType() == TableModelEvent.UPDATE) {
			int from = e.getFirstRow();
			int to   = Math.min(e.getLastRow(), translationTable.getRowCount() - 1);
			if(from != TableModelEvent.HEADER_ROW) {
				ArrayList<Integer> indices = new ArrayList<Integer>();
				for(int i = from; i <= to; i++)
					indices.add(i);
				int[] idxarr = new int[indices.size()];
				for(int i = 0, n = indices.size(); i < n; i++)
					idxarr[i] = indices.get(i);
				for(TranslationData td : translationTable.getTranslationData(idxarr)) {
					if(td.getAction() == TranslationAction.NEW_ID) {
						if(td.getPerform() && (td.getResourceId() == null || td.getResourceId().equals(""))) {
							helper.setResourceId(td);
						}
						else if(!td.getPerform() && td.getResourceId() != null && !td.getResourceId().equals("")) {
							helper.deleteResourceId(td);
						}
					}
				}
			}
		}
	}

}
