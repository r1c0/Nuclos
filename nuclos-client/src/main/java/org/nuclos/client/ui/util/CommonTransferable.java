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

package org.nuclos.client.ui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.activation.ActivationDataFlavor;

import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.TransformerUtils;

/**
 * A {@link Transferable} implementation which consists of a base value of type V
 * and some registered flavors which will generate their data on-the-fly from the 
 * base value using a {@link Transformer}.
 */
public class CommonTransferable<V> implements Transferable {
	
	/**
	 * A generified {@link DataFlavor} class. This class is useful in conjunction with
	 * {@link CommonTransferable} to ensure type safety for the registered transformers.
	 */
	public static class CommonDataFlavor<T> extends ActivationDataFlavor {

		public CommonDataFlavor(Class<T> representationClass, String humanPresentableName) {
			this(representationClass, javaJVMLocalObjectMimeType + ";class="+representationClass.getName(), humanPresentableName);
		}

		
		public CommonDataFlavor(Class<T> representationClass, String mimeType, String humanPresentableName) {
			super(representationClass, mimeType, humanPresentableName);
		}
		
		public T extractTransferData(Transferable transferable) throws UnsupportedFlavorException, IOException {
			return getRepresentationClass().cast(transferable.getTransferData(this));
		}
		
		@Override
		public Class<T> getRepresentationClass() {
			return super.getRepresentationClass();
		}
	}

	private final V value;
	private final LinkedHashMap<DataFlavor, Transformer<? super V, ?>> flavors;
	
	public CommonTransferable(V value) {
		this.value = value;
		this.flavors = new LinkedHashMap<DataFlavor, Transformer<? super V, ?>>();
	}

	public CommonTransferable(CommonDataFlavor<? super V> flavor, V value) {
		this(value);
		registerFlavor(flavor, TransformerUtils.id());
	}
	
	public void registerFlavor(DataFlavor flavor, Transformer<? super V, ?> transformer) {
		this.flavors.put(flavor, transformer);
	}
	
	public <T> void registerFlavor(CommonDataFlavor<T> flavor, Transformer<? super V, ? extends T> transformer) {
		registerFlavor((DataFlavor) flavor, transformer);
	}
	
	public void registerToStringFlavor() {
		registerFlavor(DataFlavor.stringFlavor, TransformerUtils.toStringTransformer());
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors.keySet().toArray(new DataFlavor[flavors.size()]);
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavors.containsKey(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Transformer<? super V, ?> transformer = flavors.get(flavor);
		if (transformer == null) {
			throw new UnsupportedFlavorException(flavor);
		}
		try {
			return transformer.transform(value);
		} catch (Exception ex) {
			throw new IOException("Exception during conversion to flavor " + flavor, ex);
		}
	}
}
