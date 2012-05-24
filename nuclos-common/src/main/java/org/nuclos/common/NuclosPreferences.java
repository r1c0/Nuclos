package org.nuclos.common;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.api.Preferences;

public class NuclosPreferences extends ConcurrentHashMap<String, Serializable>
		implements Preferences {
	
	private static final Logger LOG = Logger.getLogger(NuclosPreferences.class);

	private static final long serialVersionUID = -7094457981078544249L;

	@Override
	public void setPreference(String preference, Serializable value) {
		put(preference, value);
	}

	@Override
	public Serializable getPreference(String preference) {
		return get(preference);
	}

	@Override
	public <S extends Serializable> S getPreference(String preference, Class<S> cls) {
		final Object value = get(preference);
		try {
			return cls.cast(value);
		}
		catch (ClassCastException e) {
			LOG.error("On " + this + " field " + preference + " value " + value + " expected type " + cls, e);
			throw e;
		}
	}

	@Override
	public Set<String> getPreferenceNames() {
		return keySet();
	}

}
