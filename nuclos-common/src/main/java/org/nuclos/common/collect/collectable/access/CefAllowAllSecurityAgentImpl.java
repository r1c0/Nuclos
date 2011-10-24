package org.nuclos.common.collect.collectable.access;

import org.nuclos.common.collect.collectable.Collectable;


/**
 * inner class <code>CollectableEntityFieldSecurityAgent</code>:
 * checks the permission (read, write, delete) for this <code>CollectableEntityField</code> according to the <code>Collectable</code>.
 */
public class CefAllowAllSecurityAgentImpl implements CefSecurityAgent {
	
	private Collectable clct;
	
	public CefAllowAllSecurityAgentImpl() {
	}

	/**
	 * sets the <code>Collectable</code> for the <code>CollectableEntityField</code>
	 * @param Collectable
	 */
	public void setCollectable(Collectable clct) {
		this.clct = clct;
	}

	/**
	 * @return the <code>Collectable</code> of the <code>CollectableEntityField</code>
	 */
	public Collectable getCollectable() {
		return clct;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if read permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isReadable() {
		return true;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if write permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isWritable() {
		return true;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if delete permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isRemovable() {
		return true;
	}
	
} // class CollectableEntityFieldSecurityAgent

