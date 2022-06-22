package org.matsim.analysis.homework1;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.run.homework1.RunBerlinScenarioRingCarfree;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalysisKmTravelled {
	HashMap<String, Double> travelledByLeg = new HashMap<>();
	private static final Logger log = Logger.getLogger(AnalysisKmTravelled.class);

	public void analyze(Collection<? extends Person> persons) {
		for (Person person : persons) {
			List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());
			for (Leg leg : legs) {
				Route route = leg.getRoute();

				// Simulated legs: Car, Freight
				if (route instanceof NetworkRoute) {
					NetworkRoute networkRoute = (NetworkRoute) route;
					travelledByLeg.putIfAbsent(leg.getMode(), 0.0);
					travelledByLeg.merge(leg.getMode(), networkRoute.getDistance(), Double::sum);

				} else {
					// Beeline / teleported legs: Bikes, Walk, ride, pt
					travelledByLeg.putIfAbsent(leg.getMode(), 0.0);
					travelledByLeg.merge(leg.getMode(), route.getDistance(), Double::sum);
				}
			}
		}

		for( Map.Entry<String, Double> entry : travelledByLeg.entrySet()) {
			System.out.printf( entry.getKey() + " => %.2f\n", entry.getValue()/1000);
		}
	}

	public static void main(String[] args) {
		String scenarioName;
		if (args.length == 0) {
			scenarioName =  "matsim-berlin-run-carless-ring-2";
		} else {
			scenarioName = args[0];
		}
		log.info("Scenario: " + scenarioName);
		
		var population =
			PopulationUtils.readPopulation(String.format("output/%s/%s.output_plans.xml.gz",
				scenarioName, scenarioName));

		AnalysisKmTravelled analysisKmTravelled = new AnalysisKmTravelled();
		analysisKmTravelled.analyze(population.getPersons().values());
	}
}
