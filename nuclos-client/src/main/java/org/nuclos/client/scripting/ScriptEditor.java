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
package org.nuclos.client.scripting;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.tools.Diagnostic.Kind;

import org.nuclos.client.main.Main;
import org.nuclos.client.rule.admin.RuleEditPanel;
import org.nuclos.common.NuclosScript;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;

public class ScriptEditor extends JPanel {

    private NuclosScript script;

    private RuleEditPanel editPanel;
    private GroovySupport support;


    public ScriptEditor() {
        super(new BorderLayout());
        this.support = new GroovySupport();

        editPanel = new RuleEditPanel(null);
        editPanel.getJavaEditorPanel().setContentType("text/groovy");

        JToolBar toolBar = new JToolBar();
        toolBar.add(new AbstractAction(CommonLocaleDelegate.getInstance().getText(
        		"nuclos.resplan.wizard.step5.scriptEditor.compile", null)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                compile();
            }
        });
        toolBar.add(new AbstractAction(CommonLocaleDelegate.getInstance().getText(
        		"nuclos.resplan.wizard.step5.scriptEditor.close", null)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                compile();
                Window window = SwingUtilities.getWindowAncestor(ScriptEditor.this);
                if (window != null)
                    window.dispose();
            }
        });

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(editPanel));
    }

    public void run() {
        JDialog dialog = new JDialog(Main.getInstance().getMainFrame(), CommonLocaleDelegate.getInstance().getText(
        		"nuclos.resplan.wizard.step5.scriptEditor.title", null));
        dialog.setModal(true);
        dialog.getContentPane().add(this);
        dialog.pack();
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
    }

    public NuclosScript getScript() {
        if (this.script == null) {
        	this.script = new NuclosScript();
        }
        this.script.setLanguage("groovy");
        this.script.setSource(editPanel.getJavaEditorPanel().getText());

        return this.script;
    }

    public void setScript(NuclosScript script) {
        this.script = script;
        editPanel.getJavaEditorPanel().setText(this.script.getSource());
        compile();
    }

    public void compile() {
        editPanel.clearMessages();
        try {
            support.compile(editPanel.getJavaEditorPanel().getText());
        } catch (Exception ex) {
            editPanel.setMessages(Arrays.asList(new ErrorMessage(Kind.ERROR, "Skript", ex.getMessage())));
        }
    }

    public GroovySupport getSupport() {
        return support;
    }

}
