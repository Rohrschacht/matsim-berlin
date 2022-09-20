package org.matsim.analysis.homework1;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.events.EventsUtils;

import java.util.Map;

public class EventsToPlans {

	public static final String CUSTOM_ATTRIBUTE_PERSON_EXECUTED_PLAN = "executedPlan";

	// todo maybe also just a String for the populationFile?
	public static Population extendPopulationFromEvents(Population population, String eventsFile) {
		var executedPlans = readExecutedPlans(eventsFile);

		for (Person person : population.getPersons().values()) {
			if (executedPlans.containsKey(person.getId())) {
				person.getCustomAttributes().put(CUSTOM_ATTRIBUTE_PERSON_EXECUTED_PLAN, executedPlans.get(person.getId()));
			}
		}
		return population;
	}

	public static Map<Id<Person>, Plan> readExecutedPlans(String eventsFile) {
		var manager = EventsUtils.createEventsManager();
		;
	}

}
