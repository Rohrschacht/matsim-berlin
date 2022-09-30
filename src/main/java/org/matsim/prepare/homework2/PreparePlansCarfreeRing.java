package org.matsim.prepare.homework2;


import ch.sbb.matsim.routing.pt.raptor.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.AssertionFailedException;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.prepare.homework1.CoordinateGeometryUtils;
import org.matsim.pt.router.FakeFacility;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.run.drt.OpenBerlinIntermodalPtDrtRouterModeIdentifier;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.*;
import static org.matsim.run.RunBerlinScenario.*;

public class PreparePlansCarfreeRing {
	private static CoordinateGeometryUtils coordinateUtils; // cache
	private static RaptorParameters raptorParameters;
	private static RaptorStopFinder stopFinder;
	private static SwissRailRaptorData raptorData;
	private static Map<Id<Link>, ? extends Link> links;

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{"scenarios/homework2-1pct/input/berlin-v5.5-1pct.config.xml"};
		}

		Config config = prepareConfig(args);
		Scenario scenario = prepareScenario(config);
		Controler controler = prepareControler(scenario);
		raptorParameters = RaptorUtils.createParameters(config);

		var planOutfileName = "scenarios/homework2-1pct/input/berlin-v5.5-1pct.edited_carfree_ring_plan.xml";
		var population = scenario.getPopulation();

		makePlansInRingCarfree(population, scenario.getNetwork(), controler);
		PopulationUtils.writePopulation(population, planOutfileName);
	}

	public static void createNewPlansForPersonsInRing(Population population, Network network) {
		var umweltzone = getUmweltzone();
		var coordinateUtils = new CoordinateGeometryUtils(TRANSFORMATION_UMWELTZONE, TRANSFORMATION_UMWELTZONE_BACK);

		links = network.getLinks();
		for (Person person : population.getPersons().values()) {
			Plan original = person.getPlans().get(0);

			Predicate<Leg> isRouteInGeometry = (leg) -> {
				if (leg.getRoute() == null) {
					return false;
				}
				return coordinateUtils.isBeelineInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(),
					links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone);
			};
		}
	}

	public static Plan duplicatePlan(Plan toDuplicate) {
		Plan duplicated = new PlanImpl();
		duplicated.setPerson(toDuplicate.getPerson());
		duplicated.setType(toDuplicate.getType());
		duplicated.getPlanElements().addAll(toDuplicate.getPlanElements());
		return duplicated;
	}

	/**
	 *  {@link org.matsim.prepare.population.RemovePtRoutes#run(Plan)}
	 */
	public static void makePlansInRingCarfree(Population population, Network network, Controler controler) {
		var umweltzone = getUmweltzone();
		coordinateUtils = new CoordinateGeometryUtils(TRANSFORMATION_UMWELTZONE, TRANSFORMATION_UMWELTZONE_BACK, network);
		links = network.getLinks();
		Config config = controler.getConfig();
		Scenario scenario = controler.getScenario();
		stopFinder = new DefaultRaptorStopFinder(config, null, null);
		raptorParameters = RaptorUtils.createParameters(config);
		// raptorParameters.setSearchRadius(3000);
		raptorData = SwissRailRaptorData.create(scenario.getTransitSchedule(),
			scenario.getTransitVehicles(), RaptorUtils.createStaticConfig(config), network, new OccupancyData());

		Predicate<Leg> isLegInGeometry = (leg) -> coordinateUtils.isLegInGeometry(leg, umweltzone);
		Predicate<Leg> isRouteInGeometry = isRouteInGeometryPredicate(umweltzone, coordinateUtils, links);
		Predicate<Leg> carNotAllowed = carNotAllowedPredicate(links);

		for (Person person : population.getPersons().values()) {
			preparePlansOfPerson(isLegInGeometry, isRouteInGeometry, carNotAllowed, person);
		}
	}

	private static void preparePlansOfPerson(Predicate<Leg> isLegInGeometry, Predicate<Leg> isRouteInGeometry, Predicate<Leg> carNotAllowed, Person person) {
		Plan plan = person.getSelectedPlan();
		if (plan == null) {
			return;
		}

		final List<PlanElement> planElements = plan.getPlanElements();
		var trips = TripStructureUtils.getTrips(plan);

		Plan newPlan = duplicatePlan(plan);
		boolean planChanged = false;
		for (TripStructureUtils.Trip trip : trips) {
			// has to be done trip-wise since the routing mode has to be consistent per trip
			boolean changeFromCar = trip.getLegsOnly().stream().anyMatch(isLegInGeometry.or(carNotAllowed));
			boolean deleteRoute = trip.getLegsOnly().stream().anyMatch(isRouteInGeometry);
			if (!changeFromCar && !deleteRoute)
				continue;

			final List<PlanElement> fullTrip = getFullTrip(planElements, trip);
			final String mode = (new OpenBerlinIntermodalPtDrtRouterModeIdentifier()).identifyMainMode(fullTrip);
			if (mode.equals(TransportMode.car)) {
				fullTrip.clear();
				Link start = links.get(trip.getOriginActivity().getLinkId());
				Link end = links.get(trip.getDestinationActivity().getLinkId());
				if (changeFromCar) {
					fullTrip.add(PopulationUtils.createLeg(TransportMode.pt));
					// if start or end outside of car-free zone, create new plan with pseudo activity, that should encourage to change switch network mode
					if (!(coordinateUtils.isLinkInGeometry(start, getUmweltzone())
					 && coordinateUtils.isLinkInGeometry(end, getUmweltzone()))) {
						var newFullTrip = getFullTrip(newPlan.getPlanElements(), trip);
						newFullTrip.clear();
						fullTrip.add(PopulationUtils.createLeg(TransportMode.pt));
						newFullTrip.add(getActivityBeelineIntersection(start, end));
						newFullTrip.add(PopulationUtils.createLeg(TransportMode.pt));
						planChanged = true;
					}
				} else if (deleteRoute) {
					fullTrip.add(PopulationUtils.createLeg(TransportMode.car));
				}
				if (fullTrip.size() != 1) throw new RuntimeException(fullTrip.toString());
				// todo remove departTime from home/start activity?
			}
		}
		if (planChanged) {
			person.addPlan(newPlan);
		}
	}

	private static List<PlanElement> getFullTrip(List<PlanElement> planElements, TripStructureUtils.Trip trip) {
		return planElements.subList(
			planElements.indexOf(trip.getOriginActivity()) + 1,
			planElements.indexOf(trip.getDestinationActivity()));
	}

	private static Activity getActivityBeelineIntersection(Link start, Link end) {
		Coord fakeFacCoord;
		try {
			fakeFacCoord = coordinateUtils.getActivityIntersectionCoords(start.getCoord(), end.getCoord(), getUmweltzone());
		} catch (ArrayIndexOutOfBoundsException e) { // no intersection found - offending link must be close to the edge
			System.err.println("no intersection found for " + start.getCoord().toString() + " and " + end.getCoord().toString());
			if (!start.getAllowedModes().contains(TransportMode.car))
				fakeFacCoord = start.getCoord();
			else fakeFacCoord = end.getCoord();
		}
		var facility = new FakeFacility(fakeFacCoord);
		var nearbyStops = stopFinder.findStops(facility,
			null, null, Double.NaN, raptorParameters, raptorData, RaptorStopFinder.Direction.ACCESS);

		// todo activity types?
		Activity newActivity = PopulationUtils.createActivityFromCoord("mode-change", getCoordFromNearestStop(nearbyStops, start.getCoord()));
		newActivity.setMaximumDuration(0d);
		newActivity.setEndTimeUndefined();
		newActivity.setStartTimeUndefined();
		return newActivity;
	}

	/**
	 * {@link InitialStop} does not expose  publicly
	 */
	private static Coord getCoordFromNearestStop(List<InitialStop> nearbyStops, Coord fallback) {
		Field stopField;
		try {
			stopField = InitialStop.class.getDeclaredField("stop");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		stopField.setAccessible(true);
		for (InitialStop stop : nearbyStops) {
			try {
				TransitStopFacility transitStopFacility = (TransitStopFacility) stopField.get(stop);
				if (!coordinateUtils.isCoordInGeometry(transitStopFacility.getCoord(), getUmweltzone())) {
					return transitStopFacility.getCoord();
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		System.err.println("No nearby transit stop facility eligible. Falling back to start activity coords....");
		return fallback;
	}


	private static Predicate<Leg> carNotAllowedPredicate(Map<Id<Link>, ? extends Link> links) {
		return (leg) -> {
			if (leg == null) {
				return false;
			}
			return (TransportMode.car.equals(leg.getMode())
				&& !links.get(leg.getRoute().getStartLinkId()).getAllowedModes().contains(TransportMode.car)
				&& !links.get(leg.getRoute().getEndLinkId()).getAllowedModes().contains(TransportMode.car)
			);
		};
	}

	/**
	 * / the Route sadly does not expose each routed link, try to approximate via beeline
	 */
	private static Predicate<Leg> isRouteInGeometryPredicate(Geometry umweltzone, CoordinateGeometryUtils coordinateUtils, Map<Id<Link>, ? extends Link> links) {
		return (leg) -> {
			if (leg.getRoute() == null) {
				return false;
			}
			return coordinateUtils.isBeelineInGeometry(links.get(leg.getRoute().getStartLinkId()).getCoord(),
				links.get(leg.getRoute().getEndLinkId()).getCoord(), umweltzone);
		};
	}
}
