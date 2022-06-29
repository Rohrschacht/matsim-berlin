package org.matsim.prepare.homework1;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;

import static org.matsim.run.RunBerlinScenario.prepareConfig;
import static org.matsim.run.RunBerlinScenario.prepareScenario;

public class DumpConfigFiles {

	public static void main(String... args) {
		String namePrefix = "matsim-berlin-pre-run";
		String outputFolder = "output";

		if (args.length == 0) {
			args = new String[]{"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml"};
		}

		Config config = prepareConfig(args);
		Scenario scenario = prepareScenario(config);
		dump(config, scenario, outputFolder, namePrefix);
	}


	public static void dump(Config config, Scenario scenario, String outputFolder, String namePrefix) {
		ConfigUtils.writeConfig(config, String.format("%s/%s/%s.output_config.xml.gz", outputFolder, namePrefix, namePrefix));
		NetworkUtils.writeNetwork(scenario.getNetwork(), String.format("%s/%s/%s.output_network.xml.gz", outputFolder, namePrefix, namePrefix));
		PopulationUtils.writePopulation(scenario.getPopulation(), String.format("%s/%s/%s.output_plans.xml.gz", outputFolder, namePrefix, namePrefix));
	}


}
