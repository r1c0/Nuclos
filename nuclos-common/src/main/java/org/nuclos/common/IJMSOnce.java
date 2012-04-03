package org.nuclos.common;

import java.io.Serializable;

public interface IJMSOnce {

	void queue(String topic, String text);

	void queue(String topic, Serializable object);

	void clear();

	void once();

}
