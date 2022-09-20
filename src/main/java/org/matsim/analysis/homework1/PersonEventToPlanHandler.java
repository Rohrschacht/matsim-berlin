package org.matsim.analysis.homework1;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PersonEventToPlanHandler implements PersonLegHandler, PersonActivityHandler {

	@Getter
	private final Id<Person> personId;

	private final List<PlanElement> planElements = new ArrayList<>();

	@Override
	public void handleEvent(Activity activity) {
		planElements.add(activity);
	}

	@Override
	public void handleEvent(Leg leg) {
		planElements.add(leg);
	}



}
