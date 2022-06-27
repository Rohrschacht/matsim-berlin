package org.matsim.analysis.homework1;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.prepare.homework1.CoordinateGeometryUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalysisCarFreeScenario {
	private static class AnalyzedPerson {
		Id<Person> id;
		boolean isAffected;
		String primaryLegMode;
		Coord home;
		Map<String, Double> legModeTravelTime = new HashMap<>();
		Map<String, Double> legModeDistance = new HashMap<>();

		public AnalyzedPerson(Person person) {
			this.id = person.getId();
			for (String mode : new String[] {"walk", "car", "ride", "pt", "bicycle", "freight"}) {
				legModeTravelTime.put(mode, 0d);
				legModeDistance.put(mode, 0d);
			}
		}
	}

	private static final Logger log = Logger.getLogger(AnalysisCarFreeScenario.class);

	public static void main(String[] args) throws IOException {
		String scenarioName;
		if (args.length == 0) {
			scenarioName = "matsim-berlin-run-carless-ring-2";
		} else {
			scenarioName = args[0];
		}
		log.info("Scenario: " + scenarioName);

		var population =
			PopulationUtils.readPopulation(String.format("output/%s/%s.output_plans.xml.gz",
				scenarioName, scenarioName));

		var network = NetworkUtils.readNetwork(String.format("output/%s/%s.output_network.xml.gz",
			scenarioName, scenarioName));

		var shapeUmweltzone = CoordinateGeometryUtils.getUmweltzone();
		var geometryUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE,
			network);

		// Open csv file. Naming pattern of file '{scenarioName}_{timestamp}'
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");
		FileWriter fileWriter = new FileWriter(scenarioName + "_" + dtf.format(LocalDateTime.now()) + ".csv");
		CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
			CSVFormat.DEFAULT.withHeader("personId", "isAffected", "primaryLegMode", "coordX", "coordY",
				"travelTimeWalk", "travelTimeCar", "travelTimeRide", "travelTimePt", "travelTimeBike", "travelTimeFreight",
				"travelDistanceWalk", "travelDistanceCar", "travelDistanceRide", "travelDistancePt", "travelDistanceBike",
				"travelDistanceFreight"));

		for (Person person : population.getPersons().values()) {
			AnalyzedPerson analyzedPerson = new AnalyzedPerson(person);

			// Retrieve home activity from person by using the first activity in the selected plan
			Activity homeActivity = TripStructureUtils.getActivities(person.getSelectedPlan(),
				TripStructureUtils.StageActivityHandling.StagesAsNormalActivities).get(0);
			if (!homeActivity.getType().contains("home")) {
				log.warn(String.format("%s first activity does not contain home in the type: %s", person.getId(),
					homeActivity.getType()));
			}
			analyzedPerson.home = homeActivity.getCoord();

			List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());

			analyzedPerson.primaryLegMode = legs.size() > 0 ? TripStructureUtils.identifyMainMode(legs) : "null";

			// Retrieve travel time and distance time per leg mode
			for (Leg leg : legs) {
				Route route = leg.getRoute();

				analyzedPerson.legModeTravelTime.merge(leg.getMode(), route.getTravelTime().seconds(), Double::sum);
				analyzedPerson.legModeTravelTime.merge(leg.getMode(), route.getDistance(), Double::sum);
			}
			analyzedPerson.isAffected = AffectedAgents.isAffected(person.getSelectedPlan(), shapeUmweltzone, geometryUtils);

			csvPrinter.printRecord(analyzedPerson.id, analyzedPerson.isAffected, analyzedPerson.primaryLegMode,
				analyzedPerson.home.getX(), analyzedPerson.home.getY(),
				analyzedPerson.legModeTravelTime.get("walk"), analyzedPerson.legModeTravelTime.get("car"),
				analyzedPerson.legModeTravelTime.get("ride"), analyzedPerson.legModeTravelTime.get("pt"),
				analyzedPerson.legModeTravelTime.get("bicycle"), analyzedPerson.legModeTravelTime.get("freight"),
				analyzedPerson.legModeDistance.get("walk"), analyzedPerson.legModeDistance.get("car"),
				analyzedPerson.legModeDistance.get("ride"), analyzedPerson.legModeDistance.get("pt"),
				analyzedPerson.legModeDistance.get("bicycle"), analyzedPerson.legModeDistance.get("freight"));
		}

		csvPrinter.flush();
		csvPrinter.close();
	}
}
