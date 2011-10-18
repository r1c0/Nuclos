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
package org.nuclos.client.explorer.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.ui.Icons.CompositeIcon;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;

/**
 * Shows a tooltip for each node.
 * Note that the label and thus the tooltip is reused for each node (it's amazing ;-)).
 * @todo OPTIMIZE: We probably don't need a custom renderer for the tooltip (seems to be quite expensive). And for the icon?
 */
public class ExplorerNodeRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = Logger.getLogger(ExplorerNodeRenderer.class);

	class PaintImage {
		int x;
		NuclosImage image;

		public PaintImage(int x, NuclosImage image) {
			this.x = x;
			this.image = image;
		}
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object oValue, boolean bSelected, boolean bExpanded,
			boolean bLeaf, int iRow, boolean bHasFocus) {

		final JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, oValue, bSelected, bExpanded, bLeaf, iRow,
				bHasFocus);

		String tmp = lbl.getText();

		final List<PaintImage> images = new LinkedList<PaintImage>();

		int idx = -1;
		int spaceX = SwingUtilities.computeStringWidth(lbl.getFontMetrics(lbl.getFont()), " ");

		while ((idx = tmp.indexOf("[$" + CollectableFieldFormat.class.getName() + ",")) != -1)
		{
			int formatEnd = tmp.indexOf("$]");
			String format = tmp.substring(idx, formatEnd);

			String[] formatDef = format.split(",");
			try {
				CollectableFieldFormat clctformat = CollectableFieldFormat.getInstance(Class.forName(formatDef[1]));

				int x = SwingUtilities.computeStringWidth(lbl.getFontMetrics(lbl.getFont()), tmp.substring(0, idx));

				NuclosImage img = (NuclosImage) clctformat.parse(null, formatDef[3]);

				String tmp1 = tmp.substring(0, idx);
				for (int i = 0; i < img.getWidth() / spaceX; i++) {
					tmp1 += " ";
				}
				tmp1 += tmp.substring(formatEnd + 2);
				tmp = tmp1;

				images.add(new PaintImage(x, img));
			} catch (CollectableFieldFormatException e) {
				LOG.error("format exception at " + formatDef[1], e);
			} catch (ClassNotFoundException e) {
				LOG.error("class not found for " + formatDef[1], e);
			}
		}

		final ExplorerNode<?> explorernode = (ExplorerNode<?>) oValue;

		DefaultTreeCellRenderer lbComp = new DefaultTreeCellRenderer() {

			private static final long serialVersionUID = 5423600039963175923L;

			public void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);

				Graphics2D g2d = (Graphics2D)g;
				for (PaintImage paintImage : images) {
					g2d.drawImage(new ImageIcon(paintImage.image.getContent()).getImage(), getIcon().getIconWidth()+ getIconTextGap() + paintImage.x, 1, null);
				}
			};
		};

		JComponent result = (JComponent)lbComp.getTreeCellRendererComponent(tree, oValue, bSelected, bExpanded, bLeaf, iRow,
				bHasFocus);

		lbComp.setText(tmp);

		// set tooltip text:
		final String sDescription = StringUtils.nullIfEmpty(explorernode.getTreeNode().getDescription());
		lbComp.setToolTipText(sDescription);

		// set icon:
		final Icon icon = explorernode.getIcon();
		//if (icon != null) {
			lbComp.setIcon(icon);
		//}

		if (explorernode instanceof GenericObjectExplorerNode) {
			final Icon iconRelation = ((GenericObjectExplorerNode) explorernode).getRelationIcon();
			if (iconRelation != null) {
				lbComp.setIcon(new CompositeIcon(iconRelation,icon));

				//result = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
				//result.add(new JLabel(iconRelation));
				//result.add(lbl);
				//result.setOpaque(false);
			}
		}

		return result;
	}
}
