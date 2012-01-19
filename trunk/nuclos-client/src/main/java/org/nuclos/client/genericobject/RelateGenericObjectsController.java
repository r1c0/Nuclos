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
package org.nuclos.client.genericobject;

import java.awt.Component;
import java.util.Collection;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;

/**
 * Controller for creating a relationship between leased objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class RelateGenericObjectsController extends Controller {
	private final Logger log = Logger.getLogger(this.getClass());

	private final Collection<GenericObjectIdModuleProcess> collgoimpSource;
	private final GenericObjectIdModuleProcess goimpTarget;

	/**
	 * @param parent
	 * @param collgoimpSource
	 * @param goimpTarget
	 * @throws NuclosBusinessException
	 * @precondition goimpTarget != null
	 */
	public RelateGenericObjectsController(Component parent, Collection<GenericObjectIdModuleProcess> collgoimpSource,
			GenericObjectIdModuleProcess goimpTarget) throws NuclosBusinessException {

		super(parent);

		this.checkArguments(collgoimpSource, goimpTarget);

		// Note that this cast is okay, as we have checked the source objects' types before:
		this.collgoimpSource = collgoimpSource;
		this.goimpTarget = goimpTarget;
	}

	/**
	 * verifies that all source objects are GOIMPs and that there is no reflexive relation (target may not be contained in the source objects)
	 * @param collSourceObjects
	 * @param goimpTarget
	 * @throws NuclosBusinessException if the target object is contained in list of source objects.
	 * @precondition goimpTarget != null
	 */
	private void checkArguments(Collection<?> collSourceObjects, GenericObjectIdModuleProcess goimpTarget)
			throws NuclosBusinessException {
		for (Object o : collSourceObjects) {
			if (o instanceof GenericObjectIdModuleProcess) {
				final GenericObjectIdModuleProcess goimpSource = (GenericObjectIdModuleProcess) o;
				if (goimpSource.getGenericObjectId() == goimpTarget.getGenericObjectId()) {
					throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("RelateGenericObjectsController.1", "Beziehungen eines Objekts zu sich selbst sind nicht m\u00f6glich."));
				}
			}
			else {
				throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("GroupExplorerNode.1", "Der Datentransfer wird nicht unterst\u00fctzt."));
			}
		}
	}

	/**
	 * @return Did the user press OK?
	 * @throws CommonBusinessException
	 */
	public boolean run() throws CommonBusinessException {
		final RelateGenericObjectsPanel pnl = new RelateGenericObjectsPanel(this.collgoimpSource, this.goimpTarget);
		this.enableRadioButtons(pnl);
		this.selectFirstEnabledRadioButton(pnl);

		final ValidatingJOptionPane voptpn = new ValidatingJOptionPane(this.getParent(), CommonLocaleDelegate.getMessage("RelateGenericObjectsController.2", "Beziehung herstellen"), pnl) {
			@Override
			protected void validateInput() throws ValidatingJOptionPane.ErrorInfo {
				if (pnl.getRelationType() == RelateGenericObjectsPanel.RelationType.USERDEFINED) {
					pnl.getCouplingPanel().validateInput();
				}
			}
		};

		voptpn.setSize(voptpn.getPreferredSize());
		final int iBtn = voptpn.showDialog();
		final boolean result = (iBtn == JOptionPane.OK_OPTION);
		if (result) {
			final RelateGenericObjectsPanel.RelationType relationtype = pnl.getRelationType();
			switch (relationtype) {
				case PREDECESSOROF:
					this.relateGenericObjects(GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue(), pnl.getSuccessorOfPanel().isReversedDirection());
					break;
				case PARTOF:
					this.relateGenericObjects(GenericObjectTreeNode.SystemRelationType.PART_OF.getValue(), false);
					break;
				case USERDEFINED:
					createUserDefinedRelation(pnl.getCouplingPanel());
					break;
				default:
					assert false;
			}
		}
		return result;
	}

	private void relateGenericObjects(String relationType, boolean bReversedDirection) throws CommonBusinessException {
		relateGenericObjects(this.collgoimpSource, this.goimpTarget, relationType, bReversedDirection, null, null, null);
	}

	private void enableRadioButtons(RelateGenericObjectsPanel pnl) {
		pnl.getSuccessorOfButton().setEnabled(this.isSuccessorOfEnabled());
		pnl.getPartOfButton().setEnabled(this.isPartOfEnabled());
		pnl.getCouplingButton().setEnabled(true);
	}

	/**
	 * @return Is the successor-of relation enabled? <code>true</code> iff there is at least on object in the source list
	 * for that a generator action to the target tree node exists.
	 * 
	 * @deprecated Always return true.
	 */
	private boolean isSuccessorOfEnabled() {
		return CollectionUtils.exists(this.collgoimpSource, new Predicate<GenericObjectIdModuleProcess>() {
			@Override
			public boolean evaluate(GenericObjectIdModuleProcess goimp) {
				return isSuccessorRelationAllowed(goimp, goimpTarget);
			}
		});
	}

	/**
	 * @deprecated Always return true.
	 */
	private boolean isSuccessorRelationAllowed(GenericObjectIdModuleProcess goimpSource,
			final GenericObjectIdModuleProcess goimpTarget) {
		/** @todo respect statemnemonic! But I don't have it! Should I load it from the server?! */

		return true;
	}

	/**
	 * @return Is target contained in the list of composite processes?
	 */
	private boolean isPartOfEnabled() {
		final String sCompositeProcesses = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_COMPOSITE_PROCESSES);
		log.debug("composite processes: " + sCompositeProcesses);
		final Integer iProcessId = this.goimpTarget.getProcessId();
		if (iProcessId == null) {
			return false;
		}
		try {
			log.debug("target process id: " + iProcessId);
			final String sProcessName = MasterDataDelegate.getInstance().get(NuclosEntity.PROCESS.getEntityName(), iProcessId).getField("name", String.class);
			log.debug("target process name: " + sProcessName);
			return sCompositeProcesses != null ? sCompositeProcesses.matches("\\s*(\\S+\\s+)*" + sProcessName + "(\\s+\\S+)*\\s*") : false;
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private void selectFirstEnabledRadioButton(RelateGenericObjectsPanel pnl) {
		if (pnl.getSuccessorOfButton().isEnabled()) {
			pnl.getSuccessorOfButton().setSelected(true);
		}
		else if (pnl.getPartOfButton().isEnabled()) {
			pnl.getPartOfButton().setSelected(true);
		}
		else if (pnl.getCouplingButton().isEnabled()) {
			pnl.getCouplingButton().setSelected(true);
		}
	}

	public static void createUserDefinedRelation(OtherRelationPanel pnl) throws CommonBusinessException {
		final CollectableField clctfRelationType = pnl.getRelationComboBox().getField();
		final String relationType = (String) clctfRelationType.getValue();

		relateGenericObjects(pnl.getGoSource(), pnl.getGoTarget(), relationType, pnl.getRelationPanel().isReversedDirection(),
				pnl.getValidFromDateChooser().getDate(), pnl.getValidUntilDateChooser().getDate(), pnl.getDescriptionTextArea().getText());
	}

	/**
	 * @param collgoimpSource
	 * @param goimpTarget
	 * @param relationType
	 * @param bReversedDirection
	 * @param dateValidFrom
	 * @param dateValidUntil
	 * @param sDescription
	 * @throws CommonBusinessException
	 * @precondition goimpTarget != null
	 */
	private static void relateGenericObjects(Collection<GenericObjectIdModuleProcess> collgoimpSource,
			GenericObjectIdModuleProcess goimpTarget, String relationType, boolean bReversedDirection,
			Date dateValidFrom, Date dateValidUntil, String sDescription) throws CommonBusinessException {

		for (GenericObjectIdModuleProcess goimpSource : collgoimpSource) {
			final int iGenericObjectIdSource = (bReversedDirection ? goimpTarget : goimpSource).getGenericObjectId();
			final int iGenericObjectIdTarget = (bReversedDirection ? goimpSource : goimpTarget).getGenericObjectId();
			final int iModuleIdTarget = (bReversedDirection ? goimpSource : goimpTarget).getModuleId();

			GenericObjectDelegate.getInstance().relate(iGenericObjectIdSource, relationType,
					iGenericObjectIdTarget, iModuleIdTarget, dateValidFrom, dateValidUntil, sDescription);
		}
	}

}	// class RelateGenericObjectsController
