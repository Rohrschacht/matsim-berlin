package org.matsim.analysis.homework1;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.events.handler.EventHandler;

public interface PersonLegHandler extends EventHandler {
	void handleEvent (Leg leg);
}
