package org.matsim.analysis.homework2;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.prepare.homework1.CoordinateGeometryUtils;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.getUmweltzone;

public class AnalyzeNumberOfRingCrossings {
	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{"analyzing/original/berlin-v5.5-1pct.output_plans.xml.gz"};
		}

		var population = PopulationUtils.readPopulation(args[0]);
		var umweltzone = getUmweltzone();
		var coordinateGeometryUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE, CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE_BACK);

		int numberOfPersonsCrossingUmweltzone = 0;
		int numberOfCrossings = 0;

		for (Person person : population.getPersons().values()) {
			boolean personCounted = false;
			var plan = person.getSelectedPlan();
			var trips = TripStructureUtils.getTrips(plan);
			for (var trip : trips) {
				var start = trip.getOriginActivity();
				var end = trip.getDestinationActivity();

				if ((coordinateGeometryUtils.isActivityInGeometry(start, umweltzone) && !coordinateGeometryUtils.isActivityInGeometry(end, umweltzone))
					|| (!coordinateGeometryUtils.isActivityInGeometry(start, umweltzone) && coordinateGeometryUtils.isActivityInGeometry(end, umweltzone))) {
					if (!personCounted) {
						numberOfPersonsCrossingUmweltzone++;
						personCounted = true;
					}
					numberOfCrossings++;
				}
			}
		}

		System.out.printf("Number of persons that crossed the Umweltzone border: %d\n", numberOfPersonsCrossingUmweltzone);
		System.out.printf("Number of crossings at the Umweltzone border: %d\n", numberOfCrossings);
	}
}
