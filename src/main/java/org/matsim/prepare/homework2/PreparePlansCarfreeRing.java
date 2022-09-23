package org.matsim.prepare.homework2;


import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.prepare.homework1.CoordinateGeometryUtils;
import org.matsim.run.drt.OpenBerlinIntermodalPtDrtRouterModeIdentifier;

import java.util.List;
import java.util.function.Predicate;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.getUmweltzone;
import static org.matsim.run.RunBerlinScenario.*;

public class PreparePlansCarfreeRing {
	public static void main(String[] args) {

		if (args.length == 0) {
			args = new String[]{"scenarios/homework2-1pct/input/berlin-v5.5-1pct.config.xml"};
		}


		Config config = prepareConfig(args);
		Scenario scenario = prepareScenario(config);
		Controler controler = prepareControler(scenario);

		var planOutfileName = "scenarios/homework2-1pct/output/berlin-v5.5-1pct.carfree_ring_plan.xml";
		var population = scenario.getPopulation();

		makePlansInRingCarfree(population, scenario.getNetwork());
		PopulationUtils.writePopulation(population, planOutfileName);
	}

	public static void makePlansInRingCarfree(Population population, Network network) {
		var umweltzone = getUmweltzone();
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE);

		var links = network.getLinks();

		for (Person person : population.getPersons().values()) {

			person.getSelectedPlan();
			for (Plan plan : person.getPlans()) {

				final List<PlanElement> planElements = plan.getPlanElements();
				var trips = TripStructureUtils.getTrips(plan);

				Predicate<Leg> isLegInGeometry = (leg) -> {
					if (leg.getRoute() == null) {
						return false;
					}
					return coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(), umweltzone)
						|| coordinateUtils.isCoordInGeometry(links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone);
				};

				for (TripStructureUtils.Trip trip : trips) {
					// has to be done trip-wise since the routing mode has to be consistent per trip
					boolean adjustmentNeeded = trip.getLegsOnly().stream().anyMatch(isLegInGeometry);
					if (!adjustmentNeeded)
						continue;

					final List<PlanElement> fullTrip =
						planElements.subList(
							planElements.indexOf(trip.getOriginActivity()) + 1,
							planElements.indexOf(trip.getDestinationActivity()));
					final String mode = (new OpenBerlinIntermodalPtDrtRouterModeIdentifier()).identifyMainMode(fullTrip);
					if (mode.equals(TransportMode.car)) {
						System.out.println(person.getId().toString());
						fullTrip.clear();
						fullTrip.add(PopulationUtils.createLeg("pt"));
						if (fullTrip.size() != 1) throw new RuntimeException(fullTrip.toString());
						// todo remove departTime from home/start activity?
					}
				}
			}
		}
	}
}
