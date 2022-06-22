package org.matsim.prepare.homework1;


import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;

import java.util.function.Predicate;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.getUmweltzone;
import static org.matsim.run.RunBerlinScenario.*;

public class PreparePlansCarfreeRing {
	public static void main(String[] args) {

		if (args.length == 0) {
			args = new String[]{"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml"};
		}


		Config config = prepareConfig(args);
		Scenario scenario = prepareScenario(config);
		Controler controler = prepareControler(scenario);


		var planFileName = "scenarios/berlin-v5.5-1pct/output/berlin-v5.5-1pct.output_plan.xml.gz";
		var PlanOutfileName = "scenarios/berlin-v5.5-1pct/output/berlin-v5.5-1pct.carfree_ring_plan.xml.gz";



		var population = scenario.getPopulation();

		// write original population
		//PopulationUtils.writePopulation(population, planFileName);

		makePlansInRingCarfree(population, scenario.getNetwork());
		PopulationUtils.writePopulation(population, PlanOutfileName);
	}

	public static void makePlansInRingCarfree(Population population, Network network) {
		var umweltzone = getUmweltzone();
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE);

		var links = network.getLinks();

		for (Person person : population.getPersons().values()) {

			person.getSelectedPlan();

			for (Plan plan : person.getPlans()) {

				var trips = TripStructureUtils.getTrips(plan);
				var legs = TripStructureUtils.getLegs(plan);

				Predicate<Leg> isLegInGeometry = (leg) -> coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(), umweltzone) || coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone);

				for (TripStructureUtils.Trip trip : trips) {
					// has to be done trip-wise since the routing mode has to be consistent per trip
					boolean changeToBike = trip.getLegsOnly().stream().anyMatch(isLegInGeometry);
					if (!changeToBike)
						continue;

					for (Leg leg : trip.getLegsOnly()) {
						var mode = leg.getAttributes().getAttribute("routingMode");
						if (TransportMode.car.equals(leg.getMode())) {
							leg.setMode("bicycle");
						}
						if (TransportMode.car.equals(mode)) {
							leg.getAttributes().putAttribute("routingMode", "bicycle");
						}
					}
					trip.getTripElements()
						.stream()
						.filter(planElement -> planElement instanceof Activity)
						.map(planElement -> ((Activity) planElement))
						.filter(activity -> "car interaction".equals(activity.getType()))
						.forEach(activity -> activity.setType("bicycle interaction"));
				}
			}
		}
	}
}
