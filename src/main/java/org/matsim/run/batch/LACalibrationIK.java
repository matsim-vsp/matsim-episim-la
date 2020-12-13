package org.matsim.run.batch;

import com.google.inject.AbstractModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.model.FaceMask;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.policy.Restriction;
import org.matsim.run.modules.OpenLosAngelesScenario;
import org.matsim.run.modules.OpenLosAngelesScenarioIK;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;



/**
 * Runs for la model. To run all combinations set below in Params just start RunParallel.java.
 * Batch runs are useful to calibrate the model, to run multiple seeds or to investigate the effects of different restrictions.
 */
public class LACalibrationIK implements BatchRun<LACalibrationIK.Params> {

	@Override
	public AbstractModule getBindings(int id, @Nullable Params params) {
		return new OpenLosAngelesScenario();
	}

	@Override
	public Metadata getMetadata() {
		return Metadata.of("la", "calibration");
	}

	@Override
	public Config prepareConfig(int id, Params params) {

		OpenLosAngelesScenarioIK module = new OpenLosAngelesScenarioIK();
		Config config = module.config();
		config.global().setRandomSeed(params.seed);

		//adapt episimConfig here
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setCalibrationParameter(params.calibrationParam);
		
		Map<LocalDate, Integer> infectionsPerDay = new HashMap<>();
		infectionsPerDay.put(LocalDate.parse(params.startDiseaseImport), params.dailyImportedCases1);
		infectionsPerDay.put(LocalDate.parse(params.endDiseaseImport), 0);
		episimConfig.setInfections_pers_per_day(infectionsPerDay);

		//adapt tracingConfig here
		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
		
		ConfigBuilder builder = FixedPolicy.parse(episimConfig.getPolicy());
		
		//adapt restrictions here
//		builder.restrict("2020-03-15", params.remainingMidMarch, OpenLosAngelesScenario.DEFAULT_ACTIVITIES);
//		builder.restrict("2020-05-01", params.remainingBeginMay, OpenLosAngelesScenario.DEFAULT_ACTIVITIES);

		builder.restrict("2020-05-01", Restriction.ofCiCorrection(params.ciFactorMay), OpenLosAngelesScenarioIK.DEFAULT_ACTIVITIES);
		builder.restrict("2020-06-01", Restriction.ofCiCorrection(params.ciFactorJune), OpenLosAngelesScenarioIK.DEFAULT_ACTIVITIES);
		builder.restrict("2020-07-01", Restriction.ofCiCorrection(params.ciFactorJuly), OpenLosAngelesScenarioIK.DEFAULT_ACTIVITIES);
		
		episimConfig.setPolicy(FixedPolicy.class, builder.build());

		return config;
	}

	public static final class Params {
		
		@GenerateSeeds(3)
		public long seed;
		
		@Parameter({4.0E-5})
		double calibrationParam;
		
		@StringParameter({"2020-03-01"})
		String startDiseaseImport;
		
		@IntParameter({1})
		int dailyImportedCases1;
		
		@StringParameter({"2020-03-15"})
		String endDiseaseImport;
		
//		@Parameter({0.6})
//		double remainingMidMarch;
//		
//		@Parameter({0.6, 0.7})
//		double remainingBeginMay;
		
		@Parameter({1.0})
		double ciFactorMay;
		
		@Parameter({1.0})
		double ciFactorJune;
		
		@Parameter({1.0})
		double ciFactorJuly;
		
	}


}
