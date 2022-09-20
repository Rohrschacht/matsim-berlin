package org.matsim.analysis.homework1;

import org.matsim.core.scoring.EventsToActivities;
import org.matsim.core.scoring.EventsToLegs;
import org.matsim.core.scoring.PersonExperiencedActivity;
import org.matsim.core.scoring.PersonExperiencedLeg;

/**
 * {@link org.matsim.core.events.handler.EventHandler} trying to reconstruct the executed events as a plan.
 */
public class ExecutedPlanEventHandler implements EventsToLegs.LegHandler, EventsToActivities.ActivityHandler {


	@Override
	public void handleActivity(PersonExperiencedActivity activity) {

	}

	@Override
	public void handleLeg(PersonExperiencedLeg leg) {

	}
}
