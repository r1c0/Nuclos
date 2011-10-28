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
package org.nuclos.client.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.ui.LineLayout;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ApplicationProperties;

/**
 * Splash screen to show while creating the main frame.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SplashScreen extends JPanel {

   // @todo make colors configurable in nucleus-app.properties

   /**
    * the color green as in the e-plus logo
    */
   //private static final Color COLOR_EPLUS_GREEN = new Color(0, 112, 84);

/**
    * the color of the current project logo
    */
   private static final Color COLOR_ICON = new Color(0, 0, 192);

//	private static final Color COLOR_BACKGROUND = new Color(108, 173, 108);
   private static final Color COLOR_BACKGROUND = new Color(220, 220, 220);

   private static final Color COLOR_TITLE = COLOR_ICON;
//	private static final Color COLOR_TITLE = COLOR_EPLUS_GREEN;

   private final JProgressBar progressbar = new JProgressBar();
   private final JLabel labCurrentAction = new JLabel(" ");
   private final JPanel pnlProgress = new JPanel(new LineLayout(LineLayout.VERTICAL));

   public SplashScreen() {
      super(new LineLayout(LineLayout.VERTICAL, 20, true));
      this.init();
   }

   private void init() {
      this.setBackground(ApplicationProperties.getInstance().getSplashscreenBackgroundColor(COLOR_BACKGROUND));
      this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(BevelBorder.RAISED),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
      final JPanel pnlTitle = new JPanel(new GridBagLayout());
      pnlTitle.setOpaque(false);
      this.add(pnlTitle);

      Icon splashCustomer = NuclosIcons.getInstance().getSplashIcon();
      JLabel labLogo;
      if(splashCustomer == null)
         labLogo = new JLabel(NuclosIcons.getInstance().getScaledDialogIcon(48));
      else
         labLogo = new JLabel(splashCustomer);

      pnlTitle.add(labLogo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 10, 10, 10), 0, 0));
      final JLabel labTitle = new JLabel(ApplicationProperties.getInstance().getName());
      labTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
      labTitle.setForeground(ApplicationProperties.getInstance().getSplashTitleColor(COLOR_TITLE));

      pnlTitle.add(labTitle, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(0, 0, 10, 20), 0, 0));
      final JPanel pnlSubtitle = new JPanel(new LineLayout(LineLayout.VERTICAL, 10, false));
      pnlSubtitle.setOpaque(false);
      //pnlTitle.add(pnlSubtitle, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 10, 20), 0, 0));

      final JLabel labVersion = new JLabel(ApplicationProperties.getInstance().getCurrentVersion().toString());
      UIUtils.setFontStyleBold(labVersion, true);
      pnlSubtitle.add(labVersion);
      final JLabel labCreatedBy = new JLabel("Novabit Informationssysteme GmbH");
      UIUtils.setFontStyleBold(labCreatedBy, true);
      pnlSubtitle.add(labCreatedBy);

      for(JLabel l : new JLabel[] { labVersion, labCreatedBy})
         l.setForeground(ApplicationProperties.getInstance().getSplashVersionColor(Color.BLACK));

      pnlProgress.setOpaque(false);
      this.add(pnlProgress);
      pnlProgress.add(progressbar);
      Color pbfg = ApplicationProperties.getInstance().getSplashProgressColor(null);
      if(pbfg != null)
         progressbar.setForeground(pbfg);
      pnlProgress.add(labCurrentAction);
      labCurrentAction.setForeground(ApplicationProperties.getInstance().getSplashSteppingColor(Color.BLACK));
   }

   public void setActionText(String sText) {
      labCurrentAction.setText((sText == null) ? " " : sText + "...");
      this.paintProgressImmediately();
   }

   public void increaseProgress(int iProgress) {
      progressbar.setValue(progressbar.getValue() + iProgress);
      this.paintProgressImmediately();
   }

   @Override
   public void paint(Graphics g) {
      final Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      super.paint(g);
   }

   public void paintProgressImmediately() {
//		UIUtils.paintImmediately(pnlProgress);
      // repaint the whole panel each time:
      UIUtils.paintImmediately(this);
   }

}	// class SplashScreen
