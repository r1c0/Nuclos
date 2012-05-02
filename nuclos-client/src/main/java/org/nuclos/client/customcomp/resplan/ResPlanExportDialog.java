//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.client.customcomp.resplan;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.nuclos.client.image.ImageType;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.resplan.JResPlanComponent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Thomas Pasch
 */
@Configurable
public class ResPlanExportDialog extends JDialog {
	
	private static final Logger LOG = Logger.getLogger(ResPlanExportDialog.class);
	
	//
	
	private SpringLocaleDelegate sld;
	
	private final ResPlanPanel panel;
	
	private final JComponent parent;
	
	private File save;
	
	private JTextField file;
	
	private JComboBox fileTypes;
	
	public ResPlanExportDialog(ResPlanPanel panel, JComponent parent) {
		super(Main.getInstance().getMainFrame().getFrame(), true);
		this.panel = panel;
		this.parent = parent;
		
		setResizable(false);
		final String title = panel.getController().getTitle();
		setTitle(title);
		save = new File(System.getProperty("user.home") + File.separator + 
				title.toLowerCase().replaceAll("[ \\t\\.\\!\\?\\,\\;]", "_") + ".svg");
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate sld) {
		this.sld = sld;
	}
	
	@PostConstruct
	public void init() {
		double border = 10;
		double inset = 5;
        double size[][] =
            {{border, 100, inset, 200, inset, 30, inset, 65, border},  // Columns
             {border, 20, inset, 20, inset, 30, border}}; // Rows
        final TableLayout tl = new TableLayout(size);
		setLayout(tl);
				
		final JLabel fileLabel = new JLabel(sld.getText("nuclos.resplan.dialog.file"), SwingConstants.RIGHT);
		add(fileLabel, "1, 1");
		file = new JTextField();
		file.setText(save.getPath());
		add(file, "3, 1");
		
		final JButton browse = new JButton("...");
		add(browse, "5, 1");

		final JLabel typeLabel = new JLabel(sld.getText("nuclos.resplan.dialog.type"), SwingConstants.RIGHT);
		add(typeLabel, "1, 3");
		fileTypes = new JComboBox(new String[] {
				ImageType.SVG.getFileExtension(), 
				ImageType.EMF.getFileExtension(),
				ImageType.PNG.getFileExtension()
				});
		fileTypes.setSelectedIndex(0);
		fileTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkFile();
			}
		});
		add(fileTypes, "3, 3");		
		
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(new MyFileFilter((String) fileTypes.getSelectedItem()));
				// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				final int returnVal = chooser.showSaveDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					save = chooser.getSelectedFile();
					checkFile();
				}
			}
		});
		
		final JPanel buttons = new JPanel(new FlowLayout());
		add(buttons, "1, 5, 7, 5");
		
		final JButton export = new JButton(sld.getText("nuclos.resplan.dialog.export"));
		buttons.add(export);
		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String filePath = file.getText();
				if (!filePath.equals(save.getPath())) {
					save = new File(filePath);
				}
				checkFile();
				if (save.exists()) {
					String file = save.getAbsolutePath();
					if (save.canWrite()) {
						int ans = JOptionPane.showConfirmDialog(ResPlanExportDialog.this,
								sld.getMessage("general.overwrite.file", "general.overwrite.file", file),
								sld.getMessage("general.overwrite.file.title", "general.overwrite.file.title"), 
								JOptionPane.OK_CANCEL_OPTION);
						if (ans != JOptionPane.YES_OPTION) {
							return;
						}
					}
					else {
						JOptionPane.showMessageDialog(ResPlanExportDialog.this, 
								sld.getMessage("general.notwritable.file", "general.notwritable.file", file), 
								sld.getMessage("general.notwritable.file.title", "general.notwritable.file.title"), 
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				final JResPlanComponent<Collectable, Date, Collectable> resPlan = panel.getResPlan();
				final ImageType imageType = ImageType.getFromFileExtension((String) fileTypes.getSelectedItem());
				final ResPlanExporter exporter = new ResPlanExporter(imageType, save, 
						panel.getController().getResourceVO(), panel.getTimeGranularity(), 
						panel.getTimeHorizon(), resPlan.getModel(), resPlan.getTimeModel());
				try {
					exporter.run();
				}
				catch (IOException ex) {
					LOG.warn("ResPlan export failed: " + e.toString(), ex);
				}
				catch (XPathExpressionException ex) {
					LOG.warn("ResPlan export failed: " + e.toString(), ex);
				}
				dispose();
			}
		});
		
		final JButton cancel = new JButton(sld.getText("general.cancel"));
		buttons.add(cancel);
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	private static class MyFileFilter extends javax.swing.filechooser.FileFilter implements FileFilter {
		
		private final WildcardFileFilter delegate;
		
		private final String pattern;
		
		public MyFileFilter(String ext) {
			pattern = "*." + ext;
			delegate = new WildcardFileFilter(Collections.singletonList(pattern));
		}

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory() && pathname.canRead()) {
				return true;
			}
			return delegate.accept(pathname);
		}

		@Override
		public String getDescription() {
			return pattern;
		}
		
	}
	
	private void checkFile() {
		final String ext = "." + ((String) fileTypes.getSelectedItem());
		final String filename = save.getName();
		if (!filename.endsWith(ext)) {
			int idx = filename.lastIndexOf('.');
			if (idx >= 0) {
				save = new File(save.getParentFile(), filename.substring(0, idx) + ext);
			}
			else {
				save = new File(save.getParentFile(), filename + ext);
			}
			file.setText(save.getAbsolutePath());
		}		
	}

}
