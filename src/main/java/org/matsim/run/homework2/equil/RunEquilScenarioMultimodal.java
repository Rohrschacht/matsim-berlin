package org.matsim.run.homework2.equil;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.multimodal.MultiModalModule;
import org.matsim.contrib.multimodal.config.MultiModalConfigGroup;
import org.matsim.contrib.multimodal.tools.PrepareMultiModalScenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.RunBerlinScenario;

public class RunEquilScenarioMultimodal {
	private static final Logger log = Logger.getLogger(RunEquilScenarioMultimodal.class);

	public static void main(String args[]) {
		for (String arg : args) {
			log.info(args);
		}
		if (args.length == 0) {
			args = new String[]{"scenarios/equil/config_multimodal.xml"};
		}

		Config config = prepareConfig(args);
		config.subtourModeChoice().setChainBasedModes(new String[]{});
		config.subtourModeChoice().setProbaForRandomSingleTripMode(0.5);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		PrepareMultiModalScenario.run(scenario);
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new MultiModalModule());

		controler.run();
	}

	public static Config prepareConfig(String[] args, ConfigGroup... customModules) {
		Config config;
		if (args == null || args.length == 0 || args[0] == null) {
			config = ConfigUtils.loadConfig("scenarios/equil/config_multimodal.xml");
		} else {
			config = ConfigUtils.loadConfig(args);
		}
		return config;
	}
}
