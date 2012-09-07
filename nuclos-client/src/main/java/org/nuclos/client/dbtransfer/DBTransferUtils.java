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
package org.nuclos.client.dbtransfer;

import info.clearthought.layout.TableLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dbtransfer.TransferNuclet;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.dbtransfer.TransferFacadeRemote;

public class DBTransferUtils {

	private static final Logger LOG = Logger.getLogger(DBTransferUtils.class);
	
	//
	
	// former Spring injection
	
	private TransferFacadeRemote transferFacadeRemote;
	
	// end of former Spring injection
	
	public static final int TABLE_LAYOUT_V_GAP = 3;
	public static final int TABLE_LAYOUT_H_GAP = 5;
	
	public DBTransferUtils() {
		setTransferFacadeRemote(SpringApplicationContextHolder.getBean(TransferFacadeRemote.class));
	}
	
	final void setTransferFacadeRemote(TransferFacadeRemote transferFacadeRemote) {
		this.transferFacadeRemote = transferFacadeRemote;
	}
	
	final TransferFacadeRemote getTransferFacadeRemote() {
		return transferFacadeRemote;
	}
	
	protected void initJPanel(JPanel panel, double[] cols, double[] rows) {	
		final double size [][] = {cols, rows};
		final TableLayout layout = new TableLayout(size);
		
		layout.setVGap(TABLE_LAYOUT_V_GAP);
		layout.setHGap(TABLE_LAYOUT_H_GAP);
		
		panel.setLayout(layout);
	}
	
	protected JFileChooser getFileChooser(final String fileDescription, final String fileExtension){
		JFileChooser filechooser = new JFileChooser();
		FileFilter filefilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith(fileExtension);
			}
			@Override
			public String getDescription() {
				return fileDescription + " (*" + fileExtension + ")";
			}
		};
		filechooser.addChoosableFileFilter(filefilter);
		filechooser.setFileFilter(filefilter);
		
		return filechooser;
	}
	
	protected byte[] getBytes(InputStream in, int minBufferSize)	throws IOException {
		LinkedList<byte[]> bufs = new LinkedList<byte[]>();
		byte[] buf = new byte[Math.max(in.available(), minBufferSize)];
		int offset = 0, size = 0;
		for (int n; (n = in.read(buf, offset, buf.length - offset)) > 0;) {
			offset += n;
			if (offset == buf.length) {
				bufs.add(buf);
				size += offset;
				buf = new byte[Math.max(in.available(), minBufferSize)];
				offset = 0;
			}
		}
		if (offset == 0 && bufs.size() == 1)
			return bufs.get(0);
		else {
			bufs.add(buf);
			size += offset;
			buf = new byte[size];
			for (byte[] b : bufs) {
				System.arraycopy(
					b, 0, buf, buf.length - size, Math.min(b.length, size));
				size -= b.length;
			}
			return buf;
		}
	}
	
	protected void startProgressThread(final JProgressBar progressBar, final int runUntil, final int sleepTime) {
		new Thread("DBTransferUtils.progressThread") {
			@Override
			public void run() {
				try {
					while(progressBar.getValue()<runUntil){
						progressBar.setValue(progressBar.getValue()+1);
						sleep(sleepTime);
					}
				}
				catch(Exception e) {
					LOG.warn("startProgressThread failed: " + e);
				}
			}
		}.start();
	}

	protected TransferNuclet[] getAvaiableNuclets() {
		List<TransferNuclet> result = getTransferFacadeRemote().getAvaiableNuclets();
		result.add(0, new TransferNuclet(null, "<" + SpringLocaleDelegate.getInstance().getMessage(
				"configuration.transfer.utils.fullinstance", "komplette Nuclos Instanz") + ">", null));
		return result.toArray(new TransferNuclet[0]);
	}

}
