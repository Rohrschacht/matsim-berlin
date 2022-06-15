package org.matsim.prepare.homework1;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.gis.ShapeFileReader;

public class AnalyzeEventsInRing {
	public static void main(String[] args) {

		var shapeFileName = "shapes/Umweltzone.shp";
		var plansFileName = "scenarios/berlin-v5.5-1pct/output-berlin-v5.5-1pct/berlin-v5.5-1pct.output_plans.xml.gz";

		var umweltzone = (Geometry) ShapeFileReader.getAllFeatures(shapeFileName).stream().findFirst().get().getDefaultGeometry();
		var populationBerlin = PopulationUtils.readPopulation(plansFileName);

		var coordUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE);

		var activityCount = 0;
		var allActivityCount = 0;

		for (Person person : populationBerlin.getPersons().values()) {
			var plan = person.getSelectedPlan();
			var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
//            var trips = TripStructureUtils.getTrips(plan);

			activityCount += coordUtils.countActivitiesInGeometry(umweltzone, activities);
			allActivityCount += activities.size();
		}

		System.out.printf("%s activities inside the Ring.\n", activityCount);
		System.out.printf("%s activities inside Berlin.\n", allActivityCount);
	}
}
