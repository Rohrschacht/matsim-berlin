package org.matsim.analysis.homework1;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripStructureUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalysisTrafficTime {
	private final HashMap<String, Double> travelTimeByLeg = new HashMap<>();
	private static final Logger log = Logger.getLogger(AnalysisTrafficTime.class);

	public HashMap<String, Double> travelTimeByLeg = new HashMap<>();

	public void analyze(Collection<? extends Person> persons) throws IOException {
		for (Person person : persons) {
			List<Leg> legs = TripStructureUtils.getLegs(person.getSelectedPlan());
			for (Leg leg : legs) {
				Route route = leg.getRoute();

				travelTimeByLeg.putIfAbsent(leg.getMode(), 0.0);
				travelTimeByLeg.merge(leg.getMode(), route.getTravelTime().seconds(), Double::sum);
			}
		}

		for( Map.Entry<String, Double> entry : travelTimeByLeg.entrySet()) {
			System.out.printf(entry.getKey() + " => %.2f minutes\n", entry.getValue()/60);
		}
	}

	public static void main(String[] args) throws IOException {
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

		AnalysisTrafficTime analysisTrafficTime = new AnalysisTrafficTime();
		analysisTrafficTime.analyze(population.getPersons().values());

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");
		FileWriter fileWriter = new FileWriter("trafficTime" + dtf.format(LocalDateTime.now()));
		CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
			CSVFormat.DEFAULT.withHeader("mode", "unit", "value"));

		for (Map.Entry<String, Double> entry : travelTimeByLeg.entrySet()) {
			csvPrinter.printRecord(entry.getKey(), "seconds", entry.getValue());
		}
	}
}
