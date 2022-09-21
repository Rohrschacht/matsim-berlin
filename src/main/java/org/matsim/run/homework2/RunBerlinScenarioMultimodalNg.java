package org.matsim.run.homework2;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.analysis.RunPersonTripAnalysis;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.multimodal.MultiModalModule;
import org.matsim.contrib.multimodal.tools.PrepareMultiModalScenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.AnalysisMainModeIdentifier;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.prepare.population.AssignIncome;
import org.matsim.run.BerlinExperimentalConfigGroup;
import org.matsim.run.RunBerlinScenario;
import org.matsim.run.drt.OpenBerlinIntermodalPtDrtRouterModeIdentifier;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import playground.vsp.scoring.IncomeDependentUtilityOfMoneyPersonScoringParameters;

import java.io.IOException;
import java.util.*;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

public class RunBerlinScenarioMultimodalNg {
	private static final Logger log = Logger.getLogger(RunBerlinScenario.class);

	public static void main(String[] args) {

		for (String arg : args) {
			log.info(arg);
		}

		if (args.length == 0) {
			args = new String[]{"scenarios/homework2-jbr-ng/input/berlin-ng-config.xml"};
		}


		Config config = prepareConfig(args);
		config.subtourModeChoice().setChainBasedModes(new String[]{});
		config.subtourModeChoice().setProbaForRandomSingleTripMode(0.3);

		Scenario scenario = prepareScenario(config);
		allowBikeWalkOnNetwork(scenario.getNetwork());
		PrepareMultiModalScenario.run(scenario);
		Controler controler = prepareControler(scenario);
		controler.addOverridingModule(new MultiModalModule());
		config.controler().setLastIteration(2);

		controler.run();
	}

	public static void allowBikeWalkOnNetwork(Network network) {
		for (Link link : network.getLinks().values()) {
			if (link.getAllowedModes().size() == 1 && link.getAllowedModes().contains(TransportMode.pt))
				continue;
			String type = (String) link.getAttributes().getAttribute("type");
			addBikeWalkToLink(link);
		}
	}

	public static void addBikeWalkToLink(Link link) {
		Set<String> allowedModes = link.getAllowedModes();
		Set<String> allowedModesMut = new HashSet<>(allowedModes);
		allowedModesMut.add(TransportMode.bike);
		allowedModesMut.add(TransportMode.walk);
		link.setAllowedModes(allowedModesMut);
	}

	public static Controler prepareControler(Scenario scenario) {
		// note that for something like signals, and presumably drt, one needs the controler object

		Gbl.assertNotNull(scenario);

		final Controler controler = new Controler(scenario);

		if (controler.getConfig().transit().isUseTransit()) {
			// use the sbb pt raptor router
			controler.addOverridingModule(new AbstractModule() {
				@Override
				public void install() {
					install(new SwissRailRaptorModule());
				}
			});
		} else {
			log.warn("Public transit will be teleported and not simulated in the mobsim! "
				+ "This will have a significant effect on pt-related parameters (travel times, modal split, and so on). "
				+ "Should only be used for testing or car-focused studies with a fixed modal split.  ");
		}


		// use the (congested) car travel time for the teleported ride mode
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addTravelTimeBinding(TransportMode.ride).to(networkTravelTime());
				addTravelDisutilityFactoryBinding(TransportMode.ride).to(carTravelDisutilityFactoryKey());
				bind(AnalysisMainModeIdentifier.class).to(OpenBerlinIntermodalPtDrtRouterModeIdentifier.class);

				//use income-dependent marginal utility of money for scoring
				bind(ScoringParametersForPerson.class).to(IncomeDependentUtilityOfMoneyPersonScoringParameters.class).in(Singleton.class);
			}
		});

		return controler;
	}

	public static Scenario prepareScenario(Config config) {
		Gbl.assertNotNull(config);

		// note that the path for this is different when run from GUI (path of original config) vs.
		// when run from command line/IDE (java root).  :-(    See comment in method.  kai, jul'18
		// yy Does this comment still apply?  kai, jul'19

		/*
		 * We need to set the DrtRouteFactory before loading the scenario. Otherwise DrtRoutes in input plans are loaded
		 * as GenericRouteImpls and will later cause exceptions in DrtRequestCreator. So we do this here, although this
		 * class is also used for runs without drt.
		 */
		final Scenario scenario = ScenarioUtils.createScenario(config);

		RouteFactories routeFactories = scenario.getPopulation().getFactory().getRouteFactories();
		routeFactories.setRouteFactory(DrtRoute.class, new DrtRouteFactory());

		ScenarioUtils.loadScenario(scenario);

		BerlinExperimentalConfigGroup berlinCfg = ConfigUtils.addOrGetModule(config, BerlinExperimentalConfigGroup.class);
		if (berlinCfg.getPopulationDownsampleFactor() != 1.0) {
			downsample(scenario.getPopulation().getPersons(), berlinCfg.getPopulationDownsampleFactor());
		}

		AssignIncome.assignIncomeToPersonSubpopulationAccordingToGermanyAverage(scenario.getPopulation());
		return scenario;
	}

	public static Config prepareConfig(String[] args, ConfigGroup... customModules) {
		return prepareConfig(RunDrtOpenBerlinScenario.AdditionalInformation.none, args, customModules);
	}

	public static Config prepareConfig(RunDrtOpenBerlinScenario.AdditionalInformation additionalInformation, String[] args,
									   ConfigGroup... customModules) {
		OutputDirectoryLogging.catchLogEntries();

		String[] typedArgs = Arrays.copyOfRange(args, 1, args.length);

		ConfigGroup[] customModulesToAdd;
		if (additionalInformation == RunDrtOpenBerlinScenario.AdditionalInformation.acceptUnknownParamsBerlinConfig) {
			customModulesToAdd = new ConfigGroup[]{new BerlinExperimentalConfigGroup(true)};
		} else {
			customModulesToAdd = new ConfigGroup[]{new BerlinExperimentalConfigGroup(false)};
		}
		ConfigGroup[] customModulesAll = new ConfigGroup[customModules.length + customModulesToAdd.length];

		int counter = 0;
		for (ConfigGroup customModule : customModules) {
			customModulesAll[counter] = customModule;
			counter++;
		}

		for (ConfigGroup customModule : customModulesToAdd) {
			customModulesAll[counter] = customModule;
			counter++;
		}

		final Config config = ConfigUtils.loadConfig(args[0], customModulesAll);

		config.controler().setRoutingAlgorithmType(FastAStarLandmarks);

		config.subtourModeChoice().setProbaForRandomSingleTripMode(0.5);

		config.plansCalcRoute().setRoutingRandomness(3.);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
		//config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
		config.plansCalcRoute().removeModeRoutingParams("undefined");

		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);

		// vsp defaults
		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.info);
		config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
		config.qsim().setUsingTravelTimeCheckInTeleportation(true);
		config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);

		// activities:
		for (long ii = 600; ii <= 97200; ii += 600) {
			config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("home_" + ii + ".0").setTypicalDuration(ii));
			config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("work_" + ii + ".0").setTypicalDuration(ii).setOpeningTime(6. * 3600.).setClosingTime(20. * 3600.));
			config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("leisure_" + ii + ".0").setTypicalDuration(ii).setOpeningTime(9. * 3600.).setClosingTime(27. * 3600.));
			config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("shopping_" + ii + ".0").setTypicalDuration(ii).setOpeningTime(8. * 3600.).setClosingTime(20. * 3600.));
			config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("other_" + ii + ".0").setTypicalDuration(ii));
		}
		config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("freight").setTypicalDuration(12. * 3600.));

		ConfigUtils.applyCommandline(config, typedArgs);

		return config;
	}

	public static void runAnalysis(Controler controler) {
		Config config = controler.getConfig();

		String modesString = "";
		for (String mode : config.planCalcScore().getAllModes()) {
			modesString = modesString + mode + ",";
		}
		// remove last ","
		if (modesString.length() < 2) {
			log.error("no valid mode found");
			modesString = null;
		} else {
			modesString = modesString.substring(0, modesString.length() - 1);
		}

		String[] args = new String[]{
			config.controler().getOutputDirectory(),
			config.controler().getRunId(),
			"null", // TODO: reference run, hard to automate
			"null", // TODO: reference run, hard to automate
			config.global().getCoordinateSystem(),
			"https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/projects/avoev/shp-files/shp-bezirke/bezirke_berlin.shp",
			TransformationFactory.DHDN_GK4,
			"SCHLUESSEL",
			"home",
			"10", // TODO: scaling factor, should be 10 for 10pct scenario and 100 for 1pct scenario
			"null", // visualizationScriptInputDirectory
			modesString
		};

		try {
			RunPersonTripAnalysis.main(args);
		} catch (IOException e) {
			log.error(e.getStackTrace());
			throw new RuntimeException(e.getMessage());
		}
	}

	private static void downsample(final Map<Id<Person>, ? extends Person> map, final double sample) {
		final Random rnd = MatsimRandom.getLocalInstance();
		log.warn("Population downsampled from " + map.size() + " agents.");
		map.values().removeIf(person -> rnd.nextDouble() > sample);
		log.warn("Population downsampled to " + map.size() + " agents.");
	}

}
