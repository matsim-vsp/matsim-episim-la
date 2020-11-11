package org.matsim.run.batch;

import com.google.inject.AbstractModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.run.modules.OpenLosAngelesScenario;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;



/**
 * Runs for la model. To run all combinations set below in Params just start RunParallel.java.
 * Batch runs are useful to calibrate the model, to run multiple seeds or to investigate the effects of different restrictions.
 */
public class LACalibration implements BatchRun<LACalibration.Params> {

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

		OpenLosAngelesScenario module = new OpenLosAngelesScenario();
		Config config = module.config();
		config.global().setRandomSeed(params.seed);

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setCalibrationParameter(params.calibrationParam);
		
		Map<LocalDate, Integer> infectionsPerDay = new HashMap<>();
		
		infectionsPerDay.put(LocalDate.parse(params.startDate), params.dailyImportedCases);
		episimConfig.setInfections_pers_per_day(infectionsPerDay);
		
		episimConfig.setStartDate(params.startDate);
		
		//adapt episimConfig here
		//...

		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
		
		//adapt tracingConfig here
		//...
		
		ConfigBuilder builder = FixedPolicy.parse(episimConfig.getPolicy());
		
		//adapt restrictions here
		//...
//		builder.restrict("2020-06-10", params.remainingFraction, OpenLosAngelesScenario.DEFAULT_ACTIVITIES);
		
		episimConfig.setPolicy(FixedPolicy.class, builder.build());

		return config;
	}

	public static final class Params {
		
		@GenerateSeeds(1)
		public long seed;
		
		@Parameter({1E-3, 1E-4, 1E-5, 1E-6})
		double calibrationParam;
		
		@IntParameter({1})
		int dailyImportedCases;
		
		@StringParameter({"2020-02-15", "2020-02-22"})
		String startDate;
		
//		@Parameter({0.75, 0.5})
//		double remainingFraction;
		
	}


}
