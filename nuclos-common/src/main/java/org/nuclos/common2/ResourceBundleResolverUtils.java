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
/*
 * Created on 25.05.2009
 */
package org.nuclos.common2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Transformer;

/**
 * The common part of ServerLocaleDelegate and LocaleDelegate, namely the
 * recursive resolution, as a common class.
 *
 * @author marc.jackisch
 */
public class ResourceBundleResolverUtils {

	private static final Logger LOG = Logger.getLogger(ResourceBundleResolverUtils.class);

	private ResourceBundleResolverUtils() {}

	/**
	 * Calls {{@link #getMessageInternal(ResourceBundle, Transformer, String, Object...)} with
	 * a {@link RecursiveResolver}.
	 */
	public static String getMessageInternal(SpringLocaleDelegate.LookupService service, String rid, Object ... params) {
		return getMessageInternal(service, new RecursiveResolver(service, rid), rid, params);
	}

	/**
	 * Internal getMessage is the real resolver function for the getMessage call
	 * in the delegates.
	 *
	 * @param bndl             the resource bundle
	 * @param resolver         the transformer used for resolving embedded
	 *                         resources/parameters
	 * @param rid              resource id
	 * @param params           the parameters for the message format
	 * @return the formatted string
	 * @exception java.lang.RuntimeException if a reference recursion occurs, or
	 *            a reference does not exist
	 */
	public static String getMessageInternal(SpringLocaleDelegate.LookupService service, Transformer<String, String> resolver, String rid, Object...params) {
		String resourceText = null;
		try {
			resourceText = service.getResource(rid.toString());
		}
		catch(MissingResourceException e) {
			LOG.info("getMessageInternal: " + e);
		}
		if(resourceText == null)
			throw new RuntimeException("Resource id " + rid + " not found!");

		String s = StringUtils.replaceParameters(resourceText, resolver);

		return MessageFormat.format(s, params);
	}

	/**
	 * Recursive resolver which treats embedded parameters as resource ids.
	 * Internally it holds a recursion breaker list, which ensures that there
	 * are no reference loops in the resolution of a single message.
	 */
	public static class RecursiveResolver implements Transformer<String, String> {

		private final SpringLocaleDelegate.LookupService service;
		private final List<String> chain;

		public RecursiveResolver(SpringLocaleDelegate.LookupService service) {
			this.service = service;
			this.chain = Collections.emptyList();
		}

		public RecursiveResolver(SpringLocaleDelegate.LookupService service, String rid) {
			this.service = service;
			this.chain = Collections.singletonList(rid);
		}

		private RecursiveResolver(RecursiveResolver parent, String rid) {
			this.service = parent.service;
			this.chain = new ArrayList<String>(parent.chain.size() + 1);
			this.chain.addAll(parent.chain);
			this.chain.add(rid);
		}

		@Override
		public String transform(String rid) {
			if (chain.contains(rid))
				throw new RuntimeException("Recursive resource references "
					+ "starting with id " + chain.get(0) + ": "
					+ "id " + rid + " already in resolver-chain");

			return getMessageInternal(service, new RecursiveResolver(this, rid), rid, "");
		}
	}

	public static final Transformer<String, String> NO_EXPANSION = new Transformer<String, String>() {
		@Override
		public String transform(String rid) {
			return "$'{'" + rid + "'}'"; // the quoting is needed
		}
	};
}
