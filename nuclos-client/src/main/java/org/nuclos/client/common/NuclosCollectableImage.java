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
package org.nuclos.client.common;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jfree.util.Log;
import org.nuclos.client.desktop.DesktopUtils;
import org.nuclos.client.entityobject.CollectableEntityObjectField;
import org.nuclos.client.genericobject.CollectableGenericObjectAttributeField;
import org.nuclos.client.masterdata.CollectableMasterDataField;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.component.CollectableMediaComponent;
import org.nuclos.client.ui.labeled.LabeledImage;
import org.nuclos.client.ui.message.MessageExchange;
import org.nuclos.client.ui.message.MessageExchange.MessageExchangeListener;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.IOUtils;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JTextField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosCollectableImage extends CollectableMediaComponent implements MessageExchangeListener {
   private NuclosImage nuclosImage;
   private boolean bScalable;
   private int inputWidth = -1;
   private int inputHeight = -1;
   
   
   /**
    * @param clctef
    * @postcondition this.isDetailsComponent()
    */
   public NuclosCollectableImage(CollectableEntityField clctef) {
      this(clctef, false);
      assert this.isDetailsComponent();
   }

   public NuclosCollectableImage(CollectableEntityField clctef, boolean bSearchable) {
      super(clctef, new LabeledImage(),bSearchable);
      this.nuclosImage = new NuclosImage();
      MessageExchange.addListener(this);
      setupDragDrop();
   }

	protected void setupDragDrop() {
		DropTarget target = new DropTarget(this.getImageLabel(), new ImageLabelDragDropListener());
		target.setActive(true);
	}


   public JLabel getImageLabel() {
      return this.getMediaComponent();
   }

   @Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return new CollectableValueField(this.nuclosImage);
   }

	@Override
	protected void updateView(CollectableField clctfValue) {
		if(clctfValue.getValue() instanceof NuclosImage) {
			NuclosImage ni = (NuclosImage)clctfValue.getValue();
			if(ni == null) {
				clearField();
			}
			else {
				if(ni.getContent() != null) {
					ImageIcon ii = new ImageIcon(ni.getContent());
					//this.getMediaComponent().setIcon(ii);
					int h = this.getLabeledComponent().getHeight();
					int w = this.getLabeledComponent().getWidth();

					if(h == 0 || w == 0 || !bScalable) {
						this.getMediaComponent().setIcon(ii);
						this.getMediaComponent().setHorizontalAlignment(SwingConstants.CENTER);
						this.getMediaComponent().setVerticalAlignment(SwingConstants.CENTER);
					}
					else {
						Image imageScaled = ii.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT);
						this.getMediaComponent().setIcon(new ImageIcon(imageScaled));
					}
					this.nuclosImage = ni;
					JComponent comp = NuclosCollectableImage.this.getJComponent();
					if(comp instanceof LabeledImage) {
						LabeledImage li = (LabeledImage)comp;
						li.setNuclosImage(ni);
					}

				}
				else {
					clearField();
				}
			}
		}
		else {
			clearField();
		}

		getImageLabel().setBorder(new LineBorder(Color.BLACK));

		// ensure the start of the text is visible (instead of the end) when the text is too long
		// to be fully displayed:

		this.adjustAppearance();
	}
	
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	
	public void setInputHeight(int inputHeight) {
		this.inputHeight = inputHeight;
	}

	private void clearField() {
		nuclosImage = null;
		this.getMediaComponent().setText(getMessage("NuclosCollectableImage.1", "Bild hier fallenlassen"));
		this.getMediaComponent().setIcon(null);
		this.getMediaComponent().setToolTipText(getMessage("NuclosCollectableImage.1", "Bild hier fallenlassen"));
		if(this.getLabeledComponent() instanceof LabeledImage) {
			LabeledImage li = (LabeledImage)this.getLabeledComponent();
			li.setNuclosImage(nuclosImage);
		}
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu result = new JPopupMenu();
		result.add(new AbstractAction(getMessage("collectableimage.filechooser.1", "Bild \u00f6ffnen")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chosser = new JFileChooser();
				chosser.setFileFilter(new ImageFileFilter());

				int choice = chosser.showOpenDialog(getLabeledComponent());
				if(choice == JFileChooser.CANCEL_OPTION)
					return;
				else {
					File file = chosser.getSelectedFile();
					loadImageFromFile(file);
				}
			}
		});

		result.add(new AbstractAction(getMessage("collectableimage.filechooser.2","Bild speichern")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent comp = NuclosCollectableImage.this.getJComponent();
				if(comp instanceof LabeledImage) {
					LabeledImage li = (LabeledImage)comp;
					NuclosImage ni = li.getNuclosImage();
					String formatname = getFormatName(ni, "JPG");

					JFileChooser chosser = new JFileChooser();
					chosser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chosser.setFileFilter(new ImageFileFilter());
					chosser.setSelectedFile(new File("image." + formatname.toLowerCase()));
					int choice = chosser.showSaveDialog(getLabeledComponent());
					if(choice == JFileChooser.CANCEL_OPTION) {
						return;
					}

					File file = chosser.getSelectedFile();
					try {
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(ni.getContent());
						fos.close();
					}
					catch(FileNotFoundException ex) {
						Errors.getInstance().showExceptionDialog(getControlComponent(), ex);
					}
					catch(IOException ex) {
						Errors.getInstance().showExceptionDialog(getControlComponent(), ex);
					}
				}

			}
		});

		result.add(new AbstractAction(getMessage("collectableimage.filechooser.3","Bild anzeigen")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JComponent comp = NuclosCollectableImage.this.getJComponent();
					if(comp instanceof LabeledImage) {
						LabeledImage li = (LabeledImage)comp;
						NuclosImage ni = li.getNuclosImage();
						String formatname = getFormatName(ni, "JPG");

						final java.io.File file = File.createTempFile("nuclos-image", "." + formatname.toLowerCase());
						IOUtils.writeToBinaryFile(file, ni.getContent());
						file.deleteOnExit();
						DesktopUtils.open(file);
					}
				}
				catch(IOException ex ) {
					Errors.getInstance().showExceptionDialog(getControlComponent(), ex);
				}
			}
		});

		result.add(new AbstractAction(getMessage("CollectableFileNameChooserBase.1", "zurücksetzen")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent comp = NuclosCollectableImage.this.getJComponent();
				if(comp instanceof LabeledImage) {
					LabeledImage li = (LabeledImage)comp;
					li.setNuclosImage(null);
					updateView(new CollectableValueField(null));
					viewToModel(new CollectableValueField(null));
				}
			}
		});

		return result;
	}

	private String getFormatName(NuclosImage ni, String defaultformat) {
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(ni.getContent()));
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				return readers.next().getFormatName();
			}
		} catch (IOException e1) {
			Log.warn("Error determining image format name.", e1);
		}
		return defaultformat;
	}
	
	public void loadImageFromIcon(ImageIcon icon) {
		BufferedImage buff = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB); 
		buff.getGraphics().drawImage(icon.getImage(), 0,0, null);
		
		File scaled = new File(IOUtils.getDefaultTempDir(), new Double(Math.random()).toString() + "tmp.png");
	    scaled.deleteOnExit();
	    try {
			ImageIO.write(buff,"PNG", scaled);
		    loadImageFromFile(scaled);
		} catch (IOException e) {
			// do nothing
		}
	}

	public void loadImageFromFile(File file) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte b[] = new byte[bis.available()];
			int c = 0;
			int counter = 0;
			while((c = bis.read()) != -1) {
				b[counter++] = (byte)c;
			}
			bis.close();

			if (inputWidth == -1 || inputHeight == -1) {
				nuclosImage = new NuclosImage(file.getName(), b, null, true);
			} else {
				nuclosImage = new NuclosImage(file.getName(), getScaled(file, inputWidth, inputHeight), null, true);
			}
			
			JComponent comp = NuclosCollectableImage.this.getJComponent();
			if(comp instanceof LabeledImage) {
				LabeledImage li = (LabeledImage)comp;
				ImageIcon ii = new ImageIcon(b);
				Image scaledImageForLabeled = ii.getImage().getScaledInstance(li.getWidth(), li.getHeight(), Image.SCALE_DEFAULT);
				li.getJMediaComponent().setIcon(new ImageIcon(scaledImageForLabeled));
				li.setNuclosImage(nuclosImage);
				nuclosImage.setThmubnail(getScaled(file, 20, 20));					
			}
			try {
				viewToModel();
				updateView(new CollectableValueField(nuclosImage));
			}
			catch(CollectableFieldFormatException e1) {
				Errors.getInstance().showExceptionDialog(this.getControlComponent(), e1);
			}
		}
		catch(FileNotFoundException e1) {
			Errors.getInstance().showExceptionDialog(this.getControlComponent(), e1);
		}
		catch(IOException e1) {
			Errors.getInstance().showExceptionDialog(this.getControlComponent(), e1);
		}
	}
	
	private byte[] getScaled(File file, int width, int height) throws FileNotFoundException, IOException {

		BufferedImage buff = ImageIO.read(file);
		BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bdest.createGraphics();
	    AffineTransform at = AffineTransform.getScaleInstance((double)width/buff.getWidth(), (double)height/buff.getHeight());
	    g.drawRenderedImage(buff,at);
	    File scaled = new File(IOUtils.getDefaultTempDir(), new Double(Math.random()).toString() + "tmp.png");
	    scaled.deleteOnExit();
	    ImageIO.write(bdest,"PNG", scaled);

		BufferedInputStream bisscaled = new BufferedInputStream(new FileInputStream(scaled));
		byte bscaled[] = new byte[bisscaled.available()];
		int cscaled = 0;
		int counterscaled = 0;
		while((cscaled = bisscaled.read()) != -1) {
			bscaled[counterscaled++] = (byte)cscaled;
		}
		bisscaled.close();
		return bscaled;
	}

	@Override
   public void setColumns(int iColumns) {

   }

	@Override
	protected void viewToModel() throws CollectableFieldFormatException {
		super.viewToModel();
	}

	@Override
   public void setComparisonOperator(ComparisonOperator compop) {
   }

   @Override
   public TableCellRenderer getTableCellRenderer() {

	   return new DefaultTableCellRenderer() {
	        @Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {

	        	Component comp = super.getTableCellRendererComponent(tbl,oValue, bSelected, bHasFocus, iRow, iColumn);
	        	
	            JLabel lbComp = new JLabel();
	            if(oValue instanceof CollectableMasterDataField) {
	            	CollectableMasterDataField field = (CollectableMasterDataField)oValue;
	            	if(field.getValue() instanceof NuclosImage) {
		            	final NuclosImage ni = (NuclosImage)field.getValue();
		            	if(ni.getThumbnail() == null)
		            		ni.produceThumbnail();
		            	if(ni.getThumbnail() != null) {
		            		ImageIcon ii = new ImageIcon(ni.getThumbnail());
		            		lbComp.setIcon(ii);
		            		lbComp.setSize(ii.getIconWidth(), ii.getIconHeight());
		            		if(ii.getIconHeight() > NuclosImage.thumbsize)
		            			tbl.setRowHeight(ii.getIconHeight());
		            	}
		            	else {
		            		lbComp.setText(ni.getFilename());
		            	}

	            	}
	            }
	            else if(oValue instanceof CollectableGenericObjectAttributeField) {
	            	CollectableGenericObjectAttributeField field = (CollectableGenericObjectAttributeField)oValue;
	            	if(field.getValue() instanceof NuclosImage) {
	            		final NuclosImage ni = (NuclosImage)field.getValue();
		            	if(ni.getThumbnail() == null)
		            		ni.produceThumbnail();
		            	if(ni.getThumbnail() != null) {
		            		ImageIcon ii = new ImageIcon(ni.getThumbnail());
		            		lbComp.setIcon(ii);
		            		lbComp.setSize(ii.getIconWidth(), ii.getIconHeight());
		            		if(ii.getIconHeight() > NuclosImage.thumbsize)
		            			tbl.setRowHeight(ii.getIconHeight());
		            	}
		            	else {
		            		lbComp.setText(ni.getFilename());
		            	}
	            	}
	            }
	            else if(oValue instanceof CollectableEntityObjectField) {
	            	CollectableEntityObjectField field = (CollectableEntityObjectField)oValue;
	            	if(field.getValue() instanceof NuclosImage) {
	            		final NuclosImage ni = (NuclosImage)field.getValue();
		            	if(ni.getThumbnail() == null)
		            		ni.produceThumbnail();
		            	if(ni.getThumbnail() != null) {
		            		ImageIcon ii = new ImageIcon(ni.getThumbnail());
		            		lbComp.setIcon(ii);
		            		lbComp.setSize(ii.getIconWidth(), ii.getIconHeight());
		            		if(ii.getIconHeight() > NuclosImage.thumbsize)
		            			tbl.setRowHeight(ii.getIconHeight());
		            	}
		            	else {
		            		lbComp.setText(ni.getFilename());
		            	}
	            	}
	            }

	            if (comp instanceof JLabel) {
	            	((JLabel)comp).setSize(lbComp.getSize());
	            	((JLabel)comp).setIcon(lbComp.getIcon());
	            	((JLabel)comp).setText(lbComp.getText());
	            	((JLabel)comp).setHorizontalAlignment(JLabel.CENTER);
	            }
	            
	            return comp;
	         }


	      };
   }


   @Override
	public void setScalable(boolean bln) {
		this.bScalable = bln;
	}


   @Override
   public void receive(Object id, ObjectType type, MessageType msg) {

   }


   class ImageFileFilter extends javax.swing.filechooser.FileFilter  {

		@Override
		public boolean accept(File f) {
			final String sName = f.getName().toUpperCase();
			if(f.isDirectory() ||sName.endsWith("JPG") || sName.endsWith("JPEG") || sName.endsWith("BMP")
				|| sName.endsWith("PNG") || sName.endsWith("GIF")) {
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			return "Bildformate";
		}

   }

	class ImageLabelDragDropListener implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {

		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			Transferable trans = dtde.getTransferable();
			DataFlavor flavor[] = trans.getTransferDataFlavors();
			if(flavor != null && flavor.length > 0) {
				try {
					if(trans.getTransferData(flavor[0]) instanceof List) {
						List files = (List) trans.getTransferData(flavor[0]);
						if(files.size() == 1) {
							if(files.get(0) instanceof File) {
								File fileDrag = (File)files.get(0);
								String filename = fileDrag.getName().toUpperCase();
								if(filename.endsWith("JPG") || filename.endsWith("JPEG") || filename.endsWith("BMP")
									|| filename.endsWith("PNG") || filename.endsWith("GIF")) {
									dtde.acceptDrag(dtde.getDropAction());
								}
								else {
									dtde.rejectDrag();
								}
							}
							else {
								dtde.rejectDrag();
							}
						}
						else {
							dtde.rejectDrag();
						}
					} else if (trans.getTransferData(flavor[0]) instanceof ImageIcon) {
						dtde.acceptDrag(dtde.getDropAction());
					} else {
						dtde.rejectDrag();
					}
				}
				catch(Exception ex) {
					// do nothing here
				}
			}
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			try {
				dtde.acceptDrop(dtde.getDropAction());
				Transferable trans = dtde.getTransferable();

				DataFlavor flavor[] = trans.getTransferDataFlavors();

				for(int i = 0; i < flavor.length; i++) {
					Object obj = trans.getTransferData(flavor[i]);
					if(obj instanceof List) {
						List files = (List) trans.getTransferData(flavor[i]);
						if(files.size() == 1) {
							if(files.get(0) instanceof File) {
								File file = (File)files.get(0);
								loadImageFromFile(file);
							}
						}
					} else if(obj instanceof ImageIcon) {
						ImageIcon icon = (ImageIcon) trans.getTransferData(flavor[i]);
						
						loadImageFromIcon(icon);
					}
				}
			} catch (Exception e) {
				// ignore this
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

	}


}	// class CollectableTextField
