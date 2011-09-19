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

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import static org.nuclos.common2.StringUtils.nullIfEmpty;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class MenuGenerator {
	private static final Logger log = Logger.getLogger(MenuGenerator.class);

	private static final char MNEMONICS_CHAR = '^';
	private static final String CI_POINT = "###CustomMarker###";

	private final Map<String, Map<String, Action>> commandMap;
	private final Map<String, Map<String, JComponent>> componentMap;
	private final JMenu topMenu;
	private final Map<String, JMenu> menuIds;
	private final List<Component> exportNotJMenuComponents;

	public MenuGenerator(Map<String, Map<String, Action>> commandMap, Map<String, Map<String, JComponent>> componentMap, List<Component> exportNotJMenuComponents) {
		this.commandMap = commandMap;
		this.componentMap = componentMap;
		this.topMenu = new JMenu();
		this.menuIds = new HashMap<String, JMenu>();
		this.exportNotJMenuComponents = exportNotJMenuComponents;
	}

	public JMenuBar getJMenuBar() {
		JMenuBar mb = new JMenuBar();
		// ID for GUI-Testing
		mb.setName("menubar");
		while(topMenu.getMenuComponentCount() > 0) {
			Component c = topMenu.getMenuComponent(0);
			topMenu.remove(0);
			if(c instanceof JSeparator)
				mb.add(new JSeparator(JSeparator.VERTICAL));
			else
				mb.add(c);
		}
		return mb;
	}

	public void processMenuConfig(URL url) throws SAXException, IOException, ParserConfigurationException {
		processMenuConfig(url.openStream());
	}

	public void processMenuConfig(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} finally {
			is.close();
		}

		registerCommands(doc.getDocumentElement());
		addSubElements(doc.getDocumentElement(), topMenu);
	}

	private void registerCommands(Element root) {
		for(Element xmlCommandMap : XMLUtils.getSubElements(root, "commandmap")) {
			root.removeChild(xmlCommandMap);

			String mapName = nullIfEmpty(xmlCommandMap.getAttribute("name"));
			if (mapName == null) {
				throw new RuntimeException("commandmap without name");
			}
			if (commandMap.containsKey(mapName)) {
				throw new RuntimeException("commandmap " + mapName + " not unique");
			}

			Map<String, Action> map = new HashMap<String, Action>();
			for (Element xmlCommand : XMLUtils.getSubElements(xmlCommandMap, "command")) {
				String name = nullIfEmpty(xmlCommand.getAttribute("name"));
				String actionClass = nullIfEmpty(xmlCommand.getAttribute("action"));
				if (name == null || actionClass == null) {
					throw new RuntimeException("command without name or action");
				}

				Action action = LangUtils.instantiate(actionClass, Action.class);
				map.put(name, action);
			}

			commandMap.put(mapName, map);
		}
	}

	private void addSubElements(Element root, JMenu parent) {
		int insertIndex = findCustomInsertionIndex(parent.getMenuComponents());
		if (insertIndex == -1) {
			insertIndex = parent.getMenuComponentCount();
		}
		for(Element menuElement : XMLUtils.getSubElements(root)) {
			boolean show = true;
			if(hasCondition(menuElement)) {
				Element condition = getCondition(menuElement);
				show = evalCondition(condition);
			}
			if(show) {
				insertIndex = appendItem(parent, insertIndex, menuElement);
			}
		}
	}

	private int appendItem(JMenu parent, int insertIndex, Element menuElement) {
		replaceTranslation(menuElement);
		extractMnemonic(menuElement);

		Icon icon = null;
		if(nullIfEmpty(menuElement.getAttribute("icon")) != null)
			icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getClassLoader().getResource(menuElement.getAttribute("icon"))));

		if(menuElement.getTagName().equals("menu")) {
			JMenu menu = null;
			String internalName = nullIfEmpty(menuElement.getAttribute("name"));
			if (internalName != null) {
				menu = menuIds.get(internalName);
			}

			if (menu == null) {
				String text = nullIfEmpty(menuElement.getAttribute("text"));
				if(text == null) {
					throw new RuntimeException("Menu element without text " + menuElement);
				}
				menu = new JMenu(text);
				String mnemonic = nullIfEmpty(menuElement.getAttribute("mnemonic"));
				if(mnemonic != null)
					menu.setMnemonic(mnemonic.charAt(0));
				if(icon != null)
					menu.setIcon(icon);

				parent.insert(menu, insertIndex++);
				if (internalName != null && !menuIds.containsKey(internalName)) {
					menuIds.put(internalName, menu);
				}
			}

			addSubElements(menuElement, menu);

			boolean setItemVisible = false;
			for(int i = 0; i < menu.getItemCount(); i++)
				setItemVisible |= (menu.getItem(i) != null && menu.getItem(i).isVisible());
			menu.setVisible(setItemVisible);
		}
		else if(menuElement.getTagName().equals("menuitem")) {
			Action a;
			if(nullIfEmpty(menuElement.getAttribute("commandreference")) != null) {
				String comref = menuElement.getAttribute("commandreference");
				if (Main.isMacOSX() && ("MainController.cmdWindowClosing".equals(comref) ||
						"MainController.cmdOpenSettings".equals(comref) ||
						"MainController.cmdShowAboutDialog".equals(comref))) {
					return insertIndex; //ignore... we use mac native hander in Main.class
				}

				a = resolveReference(menuElement.getAttribute("commandreference"), commandMap);
			} else if(nullIfEmpty(menuElement.getAttribute("dummy")) != null)
				a = DUMMY_ACTION;
			else
				throw new RuntimeException("menuitem element has neither class nor ref!" + menuElement);

			if (a == null)
				return insertIndex;

			JMenuItem i = new JMenuItem(menuElement.getAttribute("text"));
			i.setHideActionText(true);
			i.setAction(a);
			i.setText(menuElement.getAttribute("text"));
			if(nullIfEmpty(menuElement.getAttribute("mnemonic")) != null)
				i.setMnemonic(menuElement.getAttribute("mnemonic").charAt(0));
			if(nullIfEmpty(menuElement.getAttribute("accelerator")) != null) {
				KeyStroke ks = KeyStroke.getKeyStroke(menuElement.getAttribute("accelerator"));
				if(ks != null)
					i.setAccelerator(ks);
			}
			if(nullIfEmpty(menuElement.getAttribute("actioncommand")) != null)
				i.setActionCommand(menuElement.getAttribute("actioncommand"));
			if(icon != null)
				i.setIcon(icon);
			if(a == DUMMY_ACTION)
				i.setForeground(Color.RED);
			// TODO: this should be part controlled by the underlying action
			if("MainController.cmdConfigurationExport".equals(nullIfEmpty(menuElement.getAttribute("commandreference")))) {
				i.setEnabled(false);
			}
			if("MainController.cmdConfigurationImport".equals(nullIfEmpty(menuElement.getAttribute("commandreference")))) {
				i.setEnabled(false);
			}
			parent.insert(i, insertIndex++);
		}
		else if(menuElement.getTagName().equals("separator")) {
			parent.insertSeparator(insertIndex++);
		}
		else if(menuElement.getTagName().equals("customInsertionPoint")) {
			parent.add(createCustomInsertionMarker(), insertIndex++);
		}
		else if(menuElement.getTagName().equals("customcomponent")) {
			JComponent c;
			if(nullIfEmpty(menuElement.getAttribute("componentreference")) != null)
				c = resolveReference(menuElement.getAttribute("componentreference"), componentMap);
			else
				throw new RuntimeException("customcomponent menu element has no component reference!" + menuElement);

			if (c != null){
				if (exportNotJMenuComponents != null && !(c instanceof JMenuItem)) {
					exportNotJMenuComponents.add(c);
				} else {
					parent.add(c, insertIndex++);
				}
			}
		}
		else if(menuElement.getTagName().equals("glue")) {
			Component glue = Box.createGlue();
			if (glue instanceof JComponent)
				((JComponent) glue).setOpaque(false);
			parent.add(glue, insertIndex++);
		}
		else if(menuElement.getTagName().equals("condition")) {
			// skip, condition already handled
		}
		else {
			throw new UnsupportedOperationException("Unsupported menu element " + menuElement.getTagName());
		}
		return insertIndex;
	}

	private static final class CustomInsertionMarker extends JMenu {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public CustomInsertionMarker() {
	        super(CI_POINT);
	        setVisible(false);
        }
	}

	public static Component createCustomInsertionMarker() {
		return new CustomInsertionMarker();
	}

	public static int findCustomInsertionIndex(Component[] components) {
		int markerIndex = -1;
		for(int i = 0, n = components.length; i < n; i++) {
			if(components[i] instanceof CustomInsertionMarker) {
				markerIndex = i;
			}
		}
		return markerIndex;
	}

	private static final Action DUMMY_ACTION = new AbstractAction() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "noch nicht implementiert");
		}
	};

	private <T> T resolveReference(String ref, Map<String, Map<String, T>> commandMap) {
		if(ref.contains(".")) {
			String[] path = ref.split("\\.");
			if(path.length == 2 && commandMap.containsKey(path[0])) {
				T a = commandMap.get(path[0]).get(path[1]);
				if(a != null)
					return a;
			}

		}
		log.warn("Cannot resolve command reference \"" + ref + "\" for menu");
		return null;
	}

	private enum ConditionTypes { AND, OR, NOT, SecurityAction, SecurityEntityReadAllowed, Dev, Appname, DynamicClass, appId };

	private boolean hasCondition(Element e) {
		return CollectionUtils.transform(XMLUtils.getSubElements(e), XMLUtils.getTagTransformer()).indexOf("condition") >= 0;
	}

	private Element getCondition(Element e) {
		return CollectionUtils.findFirst(XMLUtils.getSubElements(e), new Predicate<Element>() {
			@Override
			public boolean evaluate(Element row) {
				return XMLUtils.getTagTransformer().transform(row).equals("condition");
			}});
	}

	private boolean evalCondition(Element cond) {
		ConditionTypes t = ConditionTypes.valueOf(cond.getAttribute("type"));
		switch(t) {
		case AND: {
			boolean r = true;
			for(Element e : XMLUtils.getSubElements(cond, "condition"))
				r &= evalCondition(e);
			return r;
		}
		case OR: {
			boolean r = false;
			for(Element e : XMLUtils.getSubElements(cond, "condition"))
				r |= evalCondition(e);
			return r;
		}
		case NOT: {
			return !evalCondition(XMLUtils.getSubElements(cond, "condition").get(0));
		}
		case SecurityAction: {
			String v = nullIfEmpty(cond.getAttribute("value"));
			if(v == null)
				throw new RuntimeException("condition element of type SecurityAction has no value!" + cond);

			return SecurityCache.getInstance().isActionAllowed(v);
		}
		case SecurityEntityReadAllowed: {
			String v = nullIfEmpty(cond.getAttribute("value"));
			if(v == null)
				throw new RuntimeException("condition element of type SecurityEntityReadAllowed has no value!" + cond);

			return SecurityCache.getInstance().isReadAllowedForEntity(v);
		}
		case Dev: {
			return ApplicationProperties.getInstance().isFunctionBlockDev();
		}
		case Appname: {
			String v = nullIfEmpty(cond.getAttribute("value"));
			if(v == null)
				throw new RuntimeException("condition element of type Appname has no value!" + cond);
			return v.equals(ApplicationProperties.getInstance().getName());
		}
		case appId: {
			String v = nullIfEmpty(cond.getAttribute("value"));
			if(v == null)
				throw new RuntimeException("condition element of type Appname has no value!" + cond);
			return v.equals(ApplicationProperties.getInstance().getAppId());
		}
		case DynamicClass: {
			String v = nullIfEmpty(cond.getAttribute("name"));
			return LangUtils.instantiate(v, DynamicClassCondition.class).eval(cond);
		}
		}
		return false;
	}

	private void extractMnemonic(Element menuElement) {
		if (!menuElement.hasAttribute("mnemonic")) {
			String name = menuElement.getAttribute("text");
			int n = (name == null || name.trim().length() == 0)
			? -1 : name.indexOf(MNEMONICS_CHAR);
			if (n > -1 && n < name.length()-1) {
				String mnemonic = name.substring(n+1,n+2);
				name = name.substring(0,n) + name.substring(n+1);
				menuElement.setAttribute("text", name);
				menuElement.setAttribute("mnemonic", mnemonic);
			}
		}
	}

	private void replaceTranslation(Element menuElement) {
		if(nullIfEmpty(menuElement.getAttribute("resId")) != null)
			menuElement.setAttribute("text", getMessage(menuElement.getAttribute("resId"), menuElement.getAttribute("text"), ApplicationProperties.getInstance().getName()));
		else if(nullIfEmpty(menuElement.getAttribute("text")) != null)
			menuElement.setAttribute("text", MessageFormat.format(menuElement.getAttribute("text"), ApplicationProperties.getInstance().getName()));
	}

	//
	// Generic menu utility methods
	//

	private static final Pattern FIND_MNEMONIC = Pattern.compile("(\\^.)");

	/**
	 * Set the label, mnemonic, icon and keystroke for a given menu item.
	 *
	 * @param item       the target menu item
	 * @param initString label with an optional mnemonic marked by "^"
	 *                   (e.g. "^File" for the text "File" with the mnemonic "F"
	 * @param icon       the icon or null
	 * @param accel      the accelerator keystroke or null
	 * @return item, for better command chaining
	 */
	public static <T extends JMenuItem> T initMenuItem(T item, String initString, Icon icon, KeyStroke accel) {
		Pair<String, Character> nameAndMnemonic = getMnemonic(initString);
		item.setText(nameAndMnemonic.x);

		if (nameAndMnemonic.y != null)
			item.setMnemonic(nameAndMnemonic.y);
		if(icon != null)
			item.setIcon(icon);
		if(accel != null)
			item.setAccelerator(accel);

		return item;
	}

	public static Pair<String, Character> getMnemonic(String input) {
		Matcher m = FIND_MNEMONIC.matcher(input);
		StringBuffer sb = new StringBuffer();
		Character mnemonic = null;
		if(m.find()) {
			String mnemonicGroup = m.group(1);
			mnemonic = mnemonicGroup.charAt(1);
			m.appendReplacement(sb, Matcher.quoteReplacement(Character.toString(mnemonic)));
		}
		m.appendTail(sb);
		return new Pair<String, Character>(sb.toString(), mnemonic);
	}
}
