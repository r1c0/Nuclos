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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;
//package org.nuclos.client.layout.wysiwyg.component;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Vector;
//
//import javax.swing.JButton;
//
//import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
//import org.nuclos.client.layout.wysiwyg.editor.ui.panels.LayoutMLDependendiesEditorPanel;
//import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable;
//
//public class LayoutMLDependencies implements WYSIWYGToolbarAttachable {
//	/**
//	 * <!ELEMENT property EMPTY> <!ATTLIST property name CDATA #REQUIRED value
//	 * CDATA #REQUIRED >
//	 */
//
//	private Vector<LayoutMLDependency> dependencies = null;
//	private WYSIWYGMetaInformation metaInf;
//
//	public LayoutMLDependencies(WYSIWYGMetaInformation metaInf) {
//		dependencies = new Vector<LayoutMLDependency>(1);
//		this.metaInf = metaInf;
//	}
//
//	public void addDependency(LayoutMLDependency layoutMLDependency) {
//			this.dependencies.add(layoutMLDependency);
//	}
//	
//	public boolean doesContainDependency(LayoutMLDependency layoutMLDependency){
//		return this.dependencies.contains(layoutMLDependency);
//	}
//
//	public Vector<LayoutMLDependency> getAllDependencies() {
//		return this.dependencies;
//	}
//
//	public WYSIWYGMetaInformation getWYSIWYGMetaInformation() {
//		return this.metaInf;
//	}
//
//	public void removeDependencyFromDependencies(LayoutMLDependency layoutMLDependency) {
//		dependencies.remove(layoutMLDependency);
//	}
//
//	@Override
//	public String toString() {
//		StringBuffer fubber = new StringBuffer();
//
//		fubber.append("<dependencies>");
//		fubber.append("\n");
//
//		for (LayoutMLDependency layoutMLDependency : getAllDependencies()) {
//			fubber.append("  " + layoutMLDependency.toString());
//		}
//
//		fubber.append("</dependencies>");
//		fubber.append("\n");
//
//		return fubber.toString();
//	}
//
//	public JButton[] getToolbarItems() {
//		JButton dependencies = new JButton("Dependencies");
//		dependencies.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				performButtonAction();
//			}
//		});
//
//		return new JButton[]{dependencies};
//	}
//
//	private final void performButtonAction() {
//		new LayoutMLDependendiesEditorPanel(this);
//	}
//}
