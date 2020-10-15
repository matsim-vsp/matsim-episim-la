/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run.modules;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.model.AgeDependentInfectionModelWithSeasonality;
import org.matsim.episim.model.AgeDependentProgressionModel;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.InfectionModel;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.SymmetricContactModel;
import org.matsim.episim.policy.FixedPolicy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Scenario based on the publicly available OpenBerlin scenario (https://github.com/matsim-scenarios/matsim-berlin).
 */
public class OpenLosAngelesScenario extends AbstractModule {

	/**
	 * Activity names of the default params from {@link #addDefaultParams(EpisimConfigGroup)}.
	 */
	public static final String[] DEFAULT_ACTIVITIES = {
			"work", "university", "school", "escort", "schoolescort", "schoolpureescort", "schoolridesharing", "non-schoolescort", 
			"maintenance", "HHmaintenance", "personalmaintenance", "eatout", "eatoutbreakfast", "eatoutlunch", "eatoutdinner",
			"visiting", "discretionary", "specialevent", "atwork", "atworkbusiness", "atworklunch", "atworkother",
			"freightStart", "freightEnd"
	};

	/**
	 * Adds default parameters that should be valid for most scenarios.
	 */
	public static void addDefaultParams(EpisimConfigGroup config) {
		config.getOrAddContainerParams("pt", "tr");
		// regular out-of-home acts:
		config.getOrAddContainerParams("home").setContactIntensity(1.0);
		config.getOrAddContainerParams("work").setContactIntensity(1.5);
		config.getOrAddContainerParams("university").setContactIntensity(5.5);
		config.getOrAddContainerParams("escort").setContactIntensity(1.0);
		
		config.getOrAddContainerParams("school").setContactIntensity(11.0); // many people, small space, no air exchange
//		config.getOrAddContainerParams("schoolescort");
//		config.getOrAddContainerParams("schoolpureescort");
//		config.getOrAddContainerParams("schoolridesharing");
		
		config.getOrAddContainerParams("shop").setContactIntensity(0.9);
		config.getOrAddContainerParams("maintenance").setContactIntensity(0.9);
		config.getOrAddContainerParams("HHmaintenance").setContactIntensity(0.9);
		config.getOrAddContainerParams("personalmaintenance").setContactIntensity(0.9);
		
		config.getOrAddContainerParams("eatout").setContactIntensity(9.24);
//		config.getOrAddContainerParams("eatoutbreakfast");
//		config.getOrAddContainerParams("eatoutlunch");
//		config.getOrAddContainerParams("eatoutdinner");
		
		config.getOrAddContainerParams("visiting").setContactIntensity(9.24);
		config.getOrAddContainerParams("discretionary");
		config.getOrAddContainerParams("specialevent").setContactIntensity(9.24);
		
		config.getOrAddContainerParams("atwork").setContactIntensity(1.5);
//		config.getOrAddContainerParams("atworkbusiness");
//		config.getOrAddContainerParams("atworklunch");
//		config.getOrAddContainerParams("atworkother");
		
		config.getOrAddContainerParams("business").setContactIntensity(1.5); // 
		config.getOrAddContainerParams("non-schoolescort").setContactIntensity(1.0);

		// freight act:
		config.getOrAddContainerParams("freightStart").setContactIntensity(0.);
		config.getOrAddContainerParams("freightEnd").setContactIntensity(0.);
		
		config.getOrAddContainerParams("quarantine_home").setContactIntensity(0.3);
	}

	@Provides
	@Singleton
	public Config config() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		config.controler().setOutputDirectory("output/output_run1");
		config.global().setCoordinateSystem("EPSG:3310");	
		config.plans().setInputFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/input/los-angeles-v1.0-population-1pct_2020-03-07.xml.gz");
		
		episimConfig.setInputEventsFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-1pct/los-angeles-v1.1-1pct.output_events-reduced-for-episim.xml.gz");
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.bln);
		episimConfig.setSampleSize(0.01);
		episimConfig.setCalibrationParameter(2);		
		episimConfig.setStartDate("2020-01-01");
		
		// disease import
		// first, set the day until which we have a disease import
		// -> set to Integer.MAX_VALUE in order to not have a limitation
		episimConfig.setInitialInfections(Integer.MAX_VALUE);
		// second, set the daily infected agents rates
		// -> these numbers are given in infected agents per day
		// -> these numbers are daily numbers that are valid from provided start day
		Map<LocalDate, Integer> infectionsPerDay = new HashMap<>();
		infectionsPerDay.put(LocalDate.parse("2020-01-01"), 100);
		infectionsPerDay.put(LocalDate.parse("2020-02-01"), 50); 
		infectionsPerDay.put(LocalDate.parse("2020-03-01"), 10);
		infectionsPerDay.put(LocalDate.parse("2020-04-01"), 0);
		episimConfig.setInfections_pers_per_day(infectionsPerDay);
		
		addDefaultParams(episimConfig);

		// implement activity-specific restrictions (school closures, homeoffice, and so on)
		// the numbers in the restrict() method indicates the percentage of activities that still occur
		episimConfig.setPolicy(FixedPolicy.class, FixedPolicy.config()
				.restrict("2020-02-01", 0.5, DEFAULT_ACTIVITIES)
				.restrict("2020-10-01", 0.9, DEFAULT_ACTIVITIES)
				.build()
		);

		return config;
	}
	
	@Override
	protected void configure() {
		bind(ContactModel.class).to(SymmetricContactModel.class).in(Singleton.class);
		bind(ProgressionModel.class).to(AgeDependentProgressionModel.class).in(Singleton.class);
		bind(InfectionModel.class).to(AgeDependentInfectionModelWithSeasonality.class).in(Singleton.class);
	}

}
