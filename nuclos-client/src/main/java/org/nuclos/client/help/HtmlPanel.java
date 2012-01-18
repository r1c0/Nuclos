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
package org.nuclos.client.help;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;

import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Panel to display an HTML document.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class HtmlPanel extends JPanel {

	private JEditorPane editorpn;

	public final JButton btnClose = new JButton();

	public HtmlPanel(URL url) throws IOException {
		init().setPage(url);
	}
	
	public HtmlPanel(String html) {
		init().setText(html);
	}

	public void addHyperlinkListener(HyperlinkListener listener) {
		editorpn.addHyperlinkListener(listener);
	}

	public void removeHyperlinkListener(HyperlinkListener listener) {
		editorpn.removeHyperlinkListener(listener);
	}

	private JEditorPane init() {
	    this.setLayout(new BorderLayout());

		editorpn = new JEditorPane() {

			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				super.paintComponent(g);
			}
		};
		editorpn.setContentType("text/html"); 
		editorpn.setEditable(false);
		
		final Dictionary<URL, Image> dict =  new Dictionary<URL, Image>() {
			private Map<URL, Image> cache = new HashMap<URL, Image>();
			
			@Override
            public int size() {
	            return cache.size();
            }

			@Override
            public boolean isEmpty() {
	            return cache.isEmpty();
            }

			@Override
            public Enumeration<URL> keys() {
	            return new Vector<URL>(cache.keySet()).elements();
            }

			@Override
            public Enumeration<Image> elements() {
	            return new Vector<Image>(cache.values()).elements();
            }

			@Override
            public Image get(Object key) {
				if(cache.containsKey(key))
					return cache.get(key);
				
				if(key instanceof URL) {
					String srep = ((URL) key).toExternalForm();
					if(srep.startsWith("http://classpath/")) {
						String rname = srep.substring("http://classpath/".length());
						Image image = Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource(rname));
						cache.put((URL) key, image);
						return image;
					}
				}
				
	            return null;
            }

			@Override
            public Image put(URL key, Image value) {
	            return cache.put(key, value);
            }

			@Override
            public Image remove(Object key) {
	            return cache.remove(key);
            }};
            
		editorpn.setEditorKit(new HTMLEditorKit() {
			private final ViewFactory htmlFactory = new HTMLFactory() {
				@Override
				public View create(Element elem) {
					elem.getDocument().putProperty("imageCache", dict);
					return super.create(elem);
				}};
			@Override
            public ViewFactory getViewFactory() {
	            return htmlFactory;
            }});

		final JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new BorderLayout());
		pnlCenter.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.getViewport().add(editorpn, null);
		pnlCenter.add(scrlpn, BorderLayout.CENTER);

		btnClose.setText(CommonLocaleDelegate.getInstance().getMessage("ExplorerController.27", "Schlie\u00dfen"));
		final JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		pnlButtons.add(btnClose, null);

		this.add(pnlCenter, BorderLayout.CENTER);
		this.add(pnlButtons, BorderLayout.SOUTH);
		
		return editorpn;
    }
}
