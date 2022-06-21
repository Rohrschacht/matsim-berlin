package org.matsim.prepare.homework1;


import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;

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

//			System.out.println("attr:" + person.getAttributes().toString());
//
//			System.out.println("legs: " + person.getAttributes().getAttribute("legs"));
//
//			System.out.println("plan attr" + person.getSelectedPlan().getAttributes().toString());
//			System.out.println("plan elem" + person.getSelectedPlan().getPlanElements().toString());
//
//			person.getSelectedPlan().getPlanElements().forEach(planElement -> System.out.println("sfkjd " + planElement));
//
//			System.out.println(person.getPlans().size());

			for (Plan plan : person.getPlans()) {
				Activity carInteraction = null;
				boolean wasCar = false;

				for (PlanElement elem : plan.getPlanElements()) {
					if (elem instanceof Leg) {
						Leg leg = (Leg) elem;
						if (leg.getMode().equals(TransportMode.car)) {
							if (coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(), umweltzone) || coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone)) {
								leg.setMode(TransportMode.bike);
								wasCar = true;
								if (carInteraction != null) {
									carInteraction.setType("bike interaction");
								}
							}
						} else {
							wasCar = false;
						}
					} else if (elem instanceof Activity) {
						Activity activity = (Activity) elem;
						if (activity.getType().equals("car interaction")) {
							carInteraction = activity;
							if (wasCar) {
								carInteraction.setType("bike interaction");
							}
						}
					}
				}
			}

//			person.getSelectedPlan()
//				.getPlanElements()
//				.stream()
//				.filter(elem -> elem instanceof Leg)
//				.map(elem -> (Leg)elem)
//				.filter(leg -> coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(), umweltzone) || coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone))
//				.filter(leg -> leg.getMode().equals(TransportMode.car))
//				.forEach(leg -> {
//					leg.setMode(TransportMode.bike);
//				});

		}
	}
}
