package org.matsim.run.homework2.equil;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contribs.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import org.matsim.contribs.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunEquilScenarioDiscreteModeChoice {
	private static final Logger log = Logger.getLogger(RunEquilScenarioDiscreteModeChoice.class);

	public static void main(String[] args) {
		for (String arg : args) {
			log.info(arg);
		}
		if (args.length == 0) {
			args = new String[]{"scenarios/equil/config_discrete-mode-choice.xml"};
		}

		Config config = prepareConfig(args, new DiscreteModeChoiceConfigGroup());
		Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new AbstractModule(){
				@Override public void install() {
					this.install( new DiscreteModeChoiceModule() ) ;
				}
		});

		if (config.transit().isUseTransit()) {
			controler.addOverridingModule(new AbstractModule(){
				@Override public void install() {
					this.install( new SwissRailRaptorModule() ) ;
				}
			});
		}

		controler.run();
	}

	public static Config prepareConfig(String[] args, ConfigGroup... customModules) {
		Config config = ConfigUtils.loadConfig(args, customModules);

		// mutate legs in sub-tours
		config.subtourModeChoice().setChainBasedModes(new String[]{});
		config.subtourModeChoice().setProbaForRandomSingleTripMode(0.5);


		return config;
	}
}
