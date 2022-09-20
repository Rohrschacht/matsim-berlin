package org.matsim.analysis.homework1;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.events.handler.EventHandler;

public interface PersonActivityHandler extends EventHandler {
	void handleEvent (Activity activity);
}
