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
package org.nuclos.client.layout.wysiwyg.datatransfer;

import java.awt.Component;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;

/**
 * This Interface is used to "inflate" a Component containing {@link TableLayoutPanel}.<br>
 * When a {@link WYSIWYGSplitPane} is added the {@link TransferablePlaceholder} gets the Position in the Layout.<br>
 * Then the Splitpane is added in a {@link WYSIWYGLayoutEditorPanel} and two new {@link WYSIWYGLayoutEditorPanel} are added.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public interface TransferablePlaceholder {

	/**
	 * This Method is used to create a "complex" {@link WYSIWYGComponent} like a {@link WYSIWYGSplitPane}.
	 * @return the complete {@link WYSIWYGComponent} 
	 * @throws CommonBusinessException
	 */
	public Component createComponent() throws CommonBusinessException;
}
