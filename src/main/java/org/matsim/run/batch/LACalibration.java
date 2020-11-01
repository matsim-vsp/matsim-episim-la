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


import javax.annotation.Nullable;



/**
 * Runs for la model
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

		OpenLosAngelesScenario module = 
				new OpenLosAngelesScenario();
		Config config = module.config();
		config.global().setRandomSeed(params.seed);

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		
		//adapt episimConfig here
		//...

		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
		
		//adapt tracingConfig here
		//...
		
		ConfigBuilder builder = FixedPolicy.parse(episimConfig.getPolicy());
		
		//adapt restrictions here
		//..
		
		episimConfig.setPolicy(FixedPolicy.class, builder.build());

		return config;
	}

	public static final class Params {

		@GenerateSeeds(1)
		public long seed;
		
		@Parameter({1.E-2, 1.E-3, 1.E-4})
		double calibrationParam;

	}


}
